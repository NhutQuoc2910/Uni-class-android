package com.ppquang06.uniclassapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ppquang06.uniclassapp.MainActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UniClassFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "UniClassFCM";
    private static final String PREFS_SHOWN_NOTIFS = "uniclass_shown_notifications";
    private static final String KEY_SHOWN_SET_PREFIX = "shown_ids_";

    public static final String CHANNEL_URGENT = "channel_urgent";
    public static final String CHANNEL_ATTENDANCE = "channel_attendance";
    public static final String CHANNEL_CLASSES = "channel_classes";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(TAG, "New FCM Token received: " + token);
        try {
            SessionManager sessionManager = SessionManager.getInstance(this);
            sessionManager.saveFcmToken(token);

            if (sessionManager.isLoggedIn()) {
                UserResponse user = sessionManager.getUser();
                String fullName = user != null ? user.getFullName() : "";
                String phone = user != null ? user.getPhone() : "";
                String avatarUrl = user != null ? user.getAvatarUrl() : null;

                UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone, avatarUrl, token);
                ApiClient.getUserClassApiService(this).updateCurrentUser(request)
                        .enqueue(new Callback<ApiResponse<UserResponse>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call,
                                                   @NonNull Response<ApiResponse<UserResponse>> response) {
                                if (response.isSuccessful()) {
                                    Log.i(TAG, "Successfully synced FCM token to server");
                                } else {
                                    Log.w(TAG, "Failed to sync FCM token to server, HTTP code: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                                Log.e(TAG, "Error syncing FCM token to server", t);
                            }
                        });
            }
        } catch (Throwable t) {
            Log.e(TAG, "Exception in onNewToken", t);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            ensureNotificationChannels(this);

            String title = null;
            String body = null;
            String channelId = CHANNEL_CLASSES;

            if (remoteMessage.getNotification() != null) {
                title = remoteMessage.getNotification().getTitle();
                body = remoteMessage.getNotification().getBody();
                if (remoteMessage.getNotification().getChannelId() != null) {
                    channelId = remoteMessage.getNotification().getChannelId();
                }
            }

            Map<String, String> data = remoteMessage.getData();
            String uniqueKey = null;
            if (data != null && !data.isEmpty()) {
                if (title == null || title.isEmpty()) {
                    title = data.get("title");
                }
                if (body == null || body.isEmpty()) {
                    if (data.containsKey("body")) {
                        body = data.get("body");
                    } else if (data.containsKey("message")) {
                        body = data.get("message");
                    }
                }
                String content = data.get("content");
                if (!TextUtils.isEmpty(content) && !content.equals(body)) {
                    if (!TextUtils.isEmpty(body) && title != null && !title.contains(body)) {
                        body = body + " — " + content;
                    } else {
                        body = content;
                    }
                }

                if (data.containsKey("materialId")) {
                    uniqueKey = "mat-" + data.get("materialId");
                } else if (data.containsKey("assignmentId")) {
                    uniqueKey = "assign-" + data.get("assignmentId");
                } else if (data.containsKey("sessionId")) {
                    uniqueKey = "att-" + data.get("sessionId");
                }

                if (data.containsKey("channelId")) {
                    channelId = data.get("channelId");
                } else if (data.containsKey("type")) {
                    String type = data.get("type");
                    if (type != null && type.toUpperCase().contains("URGENT")) {
                        channelId = CHANNEL_URGENT;
                    } else if (type != null && type.toUpperCase().contains("ATTENDANCE")) {
                        channelId = CHANNEL_ATTENDANCE;
                    } else {
                        channelId = CHANNEL_CLASSES;
                    }
                }
            }

            if (title == null || title.isEmpty()) {
                title = getString(R.string.app_name);
            }
            if (body == null) {
                body = "";
            }

            if (!CHANNEL_URGENT.equals(channelId) && !CHANNEL_ATTENDANCE.equals(channelId)) {
                channelId = CHANNEL_CLASSES;
            }

            showSystemNotification(this, uniqueKey, title, body, channelId, data);
        } catch (Throwable t) {
            Log.e(TAG, "Exception processing incoming FCM message", t);
        }
    }

    public static void showSystemNotification(@NonNull Context context,
                                              @Nullable String uniqueKey,
                                              @NonNull String title,
                                              @NonNull String body,
                                              @NonNull String channelId,
                                              @Nullable Map<String, String> data) {
        ensureNotificationChannels(context);

        if (!TextUtils.isEmpty(uniqueKey)) {
            if (isNotificationAlreadyShown(context, uniqueKey)) {
                return;
            }
            markNotificationShown(context, uniqueKey);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        int requestCode = uniqueKey != null
                ? Math.abs(uniqueKey.hashCode())
                : (int) (System.currentTimeMillis() % 100000);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flags);

        String resolvedChannel = (!CHANNEL_URGENT.equals(channelId) && !CHANNEL_ATTENDANCE.equals(channelId))
                ? CHANNEL_CLASSES
                : channelId;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, resolvedChannel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 250, 150, 250})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            int notificationId = uniqueKey != null
                    ? Math.abs(uniqueKey.hashCode())
                    : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException se) {
            Log.w(TAG, "Notification permission missing (POST_NOTIFICATIONS)", se);
        }
    }

    public static synchronized boolean isNotificationAlreadyShown(@NonNull Context context, @NonNull String uniqueKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SHOWN_NOTIFS, Context.MODE_PRIVATE);
        String userKey = getUserScopeKey(context);
        Set<String> shown = prefs.getStringSet(KEY_SHOWN_SET_PREFIX + userKey, null);
        return shown != null && shown.contains(uniqueKey);
    }

    public static synchronized void markNotificationShown(@NonNull Context context, @NonNull String uniqueKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SHOWN_NOTIFS, Context.MODE_PRIVATE);
        String userKey = getUserScopeKey(context);
        Set<String> current = prefs.getStringSet(KEY_SHOWN_SET_PREFIX + userKey, null);
        Set<String> updated = current != null ? new HashSet<>(current) : new HashSet<>();
        updated.add(uniqueKey);
        prefs.edit().putStringSet(KEY_SHOWN_SET_PREFIX + userKey, updated).apply();
    }

    public static synchronized boolean hasBaselineInitialized(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SHOWN_NOTIFS, Context.MODE_PRIVATE);
        String userKey = getUserScopeKey(context);
        return prefs.getBoolean("baseline_init_" + userKey, false);
    }

    public static synchronized void markBaselineInitialized(@NonNull Context context, @NonNull Set<String> initialKeys) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SHOWN_NOTIFS, Context.MODE_PRIVATE);
        String userKey = getUserScopeKey(context);
        Set<String> current = prefs.getStringSet(KEY_SHOWN_SET_PREFIX + userKey, null);
        Set<String> updated = current != null ? new HashSet<>(current) : new HashSet<>();
        updated.addAll(initialKeys);
        prefs.edit()
                .putStringSet(KEY_SHOWN_SET_PREFIX + userKey, updated)
                .putBoolean("baseline_init_" + userKey, true)
                .apply();
    }

    @NonNull
    private static String getUserScopeKey(@NonNull Context context) {
        try {
            UserResponse user = SessionManager.getInstance(context).getUser();
            if (user != null && !TextUtils.isEmpty(user.getId())) {
                return user.getId();
            }
            if (user != null && !TextUtils.isEmpty(user.getEmail())) {
                return user.getEmail();
            }
        } catch (Exception ignored) {
        }
        return "default";
    }

    public static void ensureNotificationChannels(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT,
                    "Thông báo khẩn",
                    NotificationManager.IMPORTANCE_HIGH
            );
            urgentChannel.setDescription("Thông báo khẩn cấp từ trường học và giảng viên");
            urgentChannel.enableVibration(true);
            urgentChannel.enableLights(true);
            urgentChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(urgentChannel);

            NotificationChannel attendanceChannel = new NotificationChannel(
                    CHANNEL_ATTENDANCE,
                    "Phiên điểm danh",
                    NotificationManager.IMPORTANCE_HIGH
            );
            attendanceChannel.setDescription("Thông báo về các phiên điểm danh đang diễn ra");
            attendanceChannel.enableVibration(true);
            attendanceChannel.enableLights(true);
            attendanceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(attendanceChannel);

            NotificationChannel classesChannel = new NotificationChannel(
                    CHANNEL_CLASSES,
                    "Lớp học & Thông báo",
                    NotificationManager.IMPORTANCE_HIGH
            );
            classesChannel.setDescription("Thông báo về bài tập mới, thông báo lớp học và tài liệu");
            classesChannel.enableVibration(true);
            classesChannel.enableLights(true);
            classesChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(classesChannel);
        }
    }
}
