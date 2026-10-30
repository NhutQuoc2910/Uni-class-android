package com.ppquang06.uniclassapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
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

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UniClassFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "UniClassFCM";

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
            createNotificationChannels();

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
                if (data.containsKey("channelId")) {
                    channelId = data.get("channelId");
                } else if (data.containsKey("type")) {
                    String type = data.get("type");
                    if ("URGENT".equalsIgnoreCase(type)) {
                        channelId = CHANNEL_URGENT;
                    } else if ("ATTENDANCE".equalsIgnoreCase(type) || "ATTENDANCE_SESSION".equalsIgnoreCase(type)) {
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

            showNotification(title, body, channelId, data);
        } catch (Throwable t) {
            Log.e(TAG, "Exception processing incoming FCM message", t);
        }
    }

    private void showNotification(String title, String body, String channelId, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
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
        int requestCode = (int) (System.currentTimeMillis() % 10000);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(CHANNEL_CLASSES.equals(channelId) ? NotificationCompat.PRIORITY_DEFAULT : NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException se) {
            Log.w(TAG, "Notification permission missing (POST_NOTIFICATIONS)", se);
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT,
                    "Thông báo khẩn",
                    NotificationManager.IMPORTANCE_HIGH
            );
            urgentChannel.setDescription("Thông báo khẩn cấp từ trường học và giảng viên");
            manager.createNotificationChannel(urgentChannel);

            NotificationChannel attendanceChannel = new NotificationChannel(
                    CHANNEL_ATTENDANCE,
                    "Phiên điểm danh",
                    NotificationManager.IMPORTANCE_HIGH
            );
            attendanceChannel.setDescription("Thông báo về các phiên điểm danh đang diễn ra");
            manager.createNotificationChannel(attendanceChannel);

            NotificationChannel classesChannel = new NotificationChannel(
                    CHANNEL_CLASSES,
                    "Lớp học & Bài tập",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            classesChannel.setDescription("Thông báo về bài tập mới, tài liệu và cập nhật lớp học");
            manager.createNotificationChannel(classesChannel);
        }
    }
}
