package com.ppquang06.uniclassapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.service.UniClassFirebaseMessagingService;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lightweight real-time notification watcher that polls {@code /api/v1/notifications}
 * while the user is active and triggers Android Heads-Up system pop-up notifications
 * whenever a new announcement, assignment, grade, or QR attendance session is detected.
 * Works seamlessly alongside FCM push notifications with automatic deduplication.
 */
public final class RealtimeNotificationPoller {

    private static final String TAG = "RealtimeNotifPoller";
    private static final long POLL_INTERVAL_MS = 8_000L;
    private static final long RECENT_THRESHOLD_SECONDS = 60L;

    private final Context appContext;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            pollOnce();
            if (running) {
                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    public RealtimeNotificationPoller(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        UniClassFirebaseMessagingService.ensureNotificationChannels(appContext);
        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(pollRunnable);
    }

    private void pollOnce() {
        try {
            if (!SessionManager.getInstance(appContext).isLoggedIn()) {
                return;
            }
            UserClassApiService apiService = ApiClient.getUserClassApiService(appContext);
            Call<ApiResponse<List<NotificationItemResponse>>> call = apiService.getNotifications();
            if (call == null) {
                return;
            }
            call.enqueue(new Callback<ApiResponse<List<NotificationItemResponse>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<NotificationItemResponse>>> c,
                                       @NonNull Response<ApiResponse<List<NotificationItemResponse>>> response) {
                    if (!running || !response.isSuccessful() || response.body() == null
                            || response.body().getData() == null) {
                        return;
                    }
                    handleNotificationsList(response.body().getData());
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<NotificationItemResponse>>> c,
                                      @NonNull Throwable t) {
                    Log.d(TAG, "Notification poll skipped: " + t.getMessage());
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "Error during notification poll", t);
        }
    }

    private void handleNotificationsList(@NonNull List<NotificationItemResponse> items) {
        boolean hasBaseline = UniClassFirebaseMessagingService.hasBaselineInitialized(appContext);
        if (!hasBaseline) {
            Set<String> baselineIds = new HashSet<>();
            for (NotificationItemResponse item : items) {
                if (item == null || TextUtils.isEmpty(item.getId())) {
                    continue;
                }
                if (isCreatedVeryRecently(item.getCreatedAt())) {
                    dispatchNewNotification(item);
                }
                baselineIds.add(item.getId());
            }
            UniClassFirebaseMessagingService.markBaselineInitialized(appContext, baselineIds);
            return;
        }

        for (NotificationItemResponse item : items) {
            if (item == null || TextUtils.isEmpty(item.getId())) {
                continue;
            }
            if (!UniClassFirebaseMessagingService.isNotificationAlreadyShown(appContext, item.getId())) {
                dispatchNewNotification(item);
            }
        }
    }

    private boolean isCreatedVeryRecently(String createdAt) {
        if (TextUtils.isEmpty(createdAt)) {
            return false;
        }
        try {
            Instant created = Instant.parse(createdAt.trim());
            Instant cutoff = Instant.now().minusSeconds(RECENT_THRESHOLD_SECONDS);
            return created.isAfter(cutoff);
        } catch (Exception e) {
            return false;
        }
    }

    private void dispatchNewNotification(@NonNull NotificationItemResponse item) {
        String id = item.getId();
        if (id.startsWith("mat-") && item.getTitle() != null && item.getTitle().startsWith("Tài liệu mới:")) {
            resolveAndShowMaterialOrAnnouncement(item);
        } else {
            showForItem(item);
        }
    }

    private void resolveAndShowMaterialOrAnnouncement(@NonNull NotificationItemResponse item) {
        final String materialId = item.getId().substring(4);
        final String targetCourseCode = item.getCourseCode();
        UserClassApiService apiService = ApiClient.getUserClassApiService(appContext);

        Call<ApiResponse<List<CourseClassItem>>> classesCall = apiService.getClasses();
        if (classesCall == null) {
            showForItem(item);
            return;
        }

        classesCall.enqueue(new Callback<ApiResponse<List<CourseClassItem>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CourseClassItem>>> call,
                                   @NonNull Response<ApiResponse<List<CourseClassItem>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showForItem(item);
                    return;
                }
                String matchedClassId = null;
                for (CourseClassItem c : response.body().getData()) {
                    if (c != null && !TextUtils.isEmpty(c.getId())) {
                        if (!TextUtils.isEmpty(targetCourseCode) && targetCourseCode.equalsIgnoreCase(c.getCode())) {
                            matchedClassId = c.getId();
                            break;
                        }
                        if (matchedClassId == null) {
                            matchedClassId = c.getId();
                        }
                    }
                }

                if (TextUtils.isEmpty(matchedClassId)) {
                    showForItem(item);
                    return;
                }

                apiService.getClassDetail(matchedClassId).enqueue(new Callback<ApiResponse<ClassDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse>> c2,
                                           @NonNull Response<ApiResponse<ClassDetailResponse>> r2) {
                        if (r2.isSuccessful() && r2.body() != null && r2.body().getData() != null) {
                            ClassDetailResponse detail = r2.body().getData();
                            for (ClassDetailResponse.AnnouncementItem ann : detail.getAnnouncements()) {
                                if (ann != null && materialId.equals(ann.getId())) {
                                    String prefix = ann.isUrgent() ? "Thông báo khẩn" : "Thông báo mới";
                                    String codeTag = !TextUtils.isEmpty(targetCourseCode) ? (" [" + targetCourseCode + "]") : "";
                                    String title = prefix + codeTag + ": " + ann.getTitle();
                                    String body = !TextUtils.isEmpty(ann.getContent()) ? ann.getContent() : ann.getTitle();
                                    String channel = ann.isUrgent()
                                            ? UniClassFirebaseMessagingService.CHANNEL_URGENT
                                            : UniClassFirebaseMessagingService.CHANNEL_CLASSES;

                                    Map<String, String> extras = new HashMap<>();
                                    extras.put("type", ann.isUrgent() ? "URGENT_ANNOUNCEMENT" : "ANNOUNCEMENT");
                                    extras.put("materialId", materialId);
                                    if (!TextUtils.isEmpty(detail.getId())) {
                                        extras.put("classId", detail.getId());
                                    }

                                    UniClassFirebaseMessagingService.showSystemNotification(
                                            appContext,
                                            item.getId(),
                                            title,
                                            body,
                                            channel,
                                            extras
                                    );
                                    return;
                                }
                            }
                        }
                        showForItem(item);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> c2,
                                          @NonNull Throwable t) {
                        showForItem(item);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CourseClassItem>>> call,
                                  @NonNull Throwable t) {
                showForItem(item);
            }
        });
    }

    private void showForItem(@NonNull NotificationItemResponse item) {
        String id = item.getId();
        String rawTitle = !TextUtils.isEmpty(item.getTitle()) ? item.getTitle() : "Thông báo mới";
        String courseCode = item.getCourseCode();
        String title = (!TextUtils.isEmpty(courseCode) && !rawTitle.contains(courseCode))
                ? ("[" + courseCode + "] " + rawTitle)
                : rawTitle;
        String body = !TextUtils.isEmpty(item.getBody()) ? item.getBody() : rawTitle;

        String channelId = UniClassFirebaseMessagingService.CHANNEL_CLASSES;
        if ("URGENT".equalsIgnoreCase(item.getCategory()) || id.startsWith("urgent-")) {
            channelId = UniClassFirebaseMessagingService.CHANNEL_URGENT;
        } else if (id.startsWith("att-")) {
            channelId = UniClassFirebaseMessagingService.CHANNEL_ATTENDANCE;
        }

        Map<String, String> extras = new HashMap<>();
        extras.put("notificationId", id);
        if (!TextUtils.isEmpty(item.getCategory())) {
            extras.put("type", item.getCategory());
        }

        UniClassFirebaseMessagingService.showSystemNotification(
                appContext,
                id,
                title,
                body,
                channelId,
                extras
        );
    }
}
