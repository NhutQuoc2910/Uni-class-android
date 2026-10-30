package com.ppquang06.uniclassapp.data.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ppquang06.uniclassapp.utils.Constants;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class StompWebSocketClient {

    private static final String TAG = "StompWebSocketClient";

    public interface StompListener {
        void onConnected();
        default void onDisconnected() {}
        default void onError(Throwable t) {}
    }

    public interface StompMessageListener {
        void onMessage(String payload);
        default void onMessage(String payload, Map<String, String> headers) {
            onMessage(payload);
        }
    }

    public static class StompFrame {
        private final String command;
        private final Map<String, String> headers;
        private final String body;

        public StompFrame(String command, Map<String, String> headers, String body) {
            this.command = command != null ? command : "";
            this.headers = headers != null ? Collections.unmodifiableMap(new LinkedHashMap<>(headers)) : Collections.emptyMap();
            this.body = body != null ? body : "";
        }

        public String getCommand() {
            return command;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getHeader(String name) {
            if (headers == null || name == null) return null;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        public String getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "StompFrame{command='" + command + '\'' + ", headers=" + headers + ", body='" + body + '\'' + '}';
        }
    }

    private static class SubscriptionRecord {
        final String id;
        final String topic;
        final StompMessageListener listener;

        SubscriptionRecord(String id, String topic, StompMessageListener listener) {
            this.id = id;
            this.topic = topic;
            this.listener = listener;
        }
    }

    private final OkHttpClient okHttpClient;
    private final Map<String, SubscriptionRecord> subscriptions = new ConcurrentHashMap<>();
    private final AtomicInteger subscriptionCounter = new AtomicInteger(0);

    private WebSocket webSocket;
    private StompListener stompListener;
    private String authToken;
    private boolean isConnected = false;

    public StompWebSocketClient() {
        this(new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .build());
    }

    public StompWebSocketClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient != null ? okHttpClient : new OkHttpClient();
    }

    public synchronized void connect(String wsUrl, String authToken, StompListener listener) {
        disconnect();

        this.stompListener = listener;
        this.authToken = authToken;
        this.isConnected = false;

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        this.webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                Log.d(TAG, "WebSocket connection opened, sending STOMP CONNECT frame");
                String connectFrame = buildConnectFrame(StompWebSocketClient.this.authToken);
                ws.send(connectFrame);
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                handleRawMessage(text);
            }

            @Override
            public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket closed: " + reason + " (code: " + code + ")");
                synchronized (StompWebSocketClient.this) {
                    isConnected = false;
                }
                postToMain(() -> {
                    if (stompListener != null) {
                        stompListener.onDisconnected();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage(), t);
                synchronized (StompWebSocketClient.this) {
                    isConnected = false;
                }
                postToMain(() -> {
                    if (stompListener != null) {
                        stompListener.onError(t);
                    }
                });
            }
        });
    }

    private void handleRawMessage(String text) {
        StompFrame frame = parseFrame(text);
        if (frame == null) {
            return; // Heartbeat ping
        }

        String command = frame.getCommand();
        if ("CONNECTED".equalsIgnoreCase(command)) {
            Log.d(TAG, "STOMP session connected successfully");
            synchronized (this) {
                isConnected = true;
                // Re-send / sync active subscriptions
                for (SubscriptionRecord record : subscriptions.values()) {
                    if (webSocket != null) {
                        webSocket.send(buildSubscribeFrame(record.id, record.topic));
                    }
                }
            }
            postToMain(() -> {
                if (stompListener != null) {
                    stompListener.onConnected();
                }
            });
        } else if ("MESSAGE".equalsIgnoreCase(command)) {
            String subId = frame.getHeader("subscription");
            String destination = frame.getHeader("destination");

            List<StompMessageListener> targets = new ArrayList<>();
            synchronized (this) {
                if (subId != null && subscriptions.containsKey(subId)) {
                    targets.add(subscriptions.get(subId).listener);
                } else if (destination != null) {
                    for (SubscriptionRecord record : subscriptions.values()) {
                        if (destination.equals(record.topic)) {
                            targets.add(record.listener);
                        }
                    }
                }
            }

            final String body = frame.getBody();
            final Map<String, String> headers = frame.getHeaders();
            for (StompMessageListener l : targets) {
                postToMain(() -> l.onMessage(body, headers));
            }
        } else if ("ERROR".equalsIgnoreCase(command)) {
            String message = frame.getHeader("message");
            if (message == null || message.isEmpty()) {
                message = frame.getBody();
            }
            final String errMsg = (message != null && !message.isEmpty()) ? message : "STOMP error frame received";
            Log.e(TAG, "STOMP ERROR: " + errMsg);
            postToMain(() -> {
                if (stompListener != null) {
                    stompListener.onError(new Exception(errMsg));
                }
            });
        }
    }

    public synchronized String subscribe(String topic, StompMessageListener listener) {
        if (topic == null || listener == null) {
            throw new IllegalArgumentException("Topic and listener must not be null");
        }
        String subId = "sub-" + subscriptionCounter.incrementAndGet();
        subscriptions.put(subId, new SubscriptionRecord(subId, topic, listener));

        if (isConnected && webSocket != null) {
            webSocket.send(buildSubscribeFrame(subId, topic));
        }
        return subId;
    }

    public synchronized void unsubscribe(String subscriptionId) {
        if (subscriptionId == null) return;
        SubscriptionRecord record = subscriptions.remove(subscriptionId);
        if (record != null && isConnected && webSocket != null) {
            webSocket.send(buildUnsubscribeFrame(subscriptionId));
        }
    }

    public synchronized boolean send(String destination, String body) {
        if (isConnected && webSocket != null) {
            return webSocket.send(buildSendFrame(destination, body));
        }
        return false;
    }

    public synchronized void disconnect() {
        isConnected = false;
        subscriptions.clear();
        if (webSocket != null) {
            try {
                webSocket.send(buildDisconnectFrame());
            } catch (Exception ignored) {
            }
            try {
                boolean closed = webSocket.close(1000, "Client disconnect");
                if (!closed) {
                    webSocket.cancel();
                }
            } catch (Exception ignored) {
                try {
                    webSocket.cancel();
                } catch (Exception ignored2) {
                }
            }
            webSocket = null;
        }
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    private void postToMain(Runnable runnable) {
        try {
            if (Looper.getMainLooper() != null && Looper.myLooper() != Looper.getMainLooper()) {
                new Handler(Looper.getMainLooper()).post(runnable);
                return;
            }
        } catch (Throwable ignored) {
            // Fallback for headless JVM unit tests where Looper is not mocked
        }
        runnable.run();
    }

    // =========================================================================
    // STOMP Framing Helpers
    // =========================================================================

    public static String buildFrame(String command, Map<String, String> headers, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append(command).append("\n");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }
        }
        sb.append("\n");
        if (body != null) {
            sb.append(body);
        }
        sb.append("\0");
        return sb.toString();
    }

    public static String buildConnectFrame(String authToken) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("accept-version", "1.1,1.2");
        headers.put("heart-beat", "10000,10000");
        if (authToken != null && !authToken.trim().isEmpty()) {
            String token = authToken.trim();
            if (!token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
            headers.put("Authorization", token);
        }
        return buildFrame("CONNECT", headers, null);
    }

    public static String buildSubscribeFrame(String id, String destination) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("id", id);
        headers.put("destination", destination);
        headers.put("ack", "auto");
        return buildFrame("SUBSCRIBE", headers, null);
    }

    public static String buildUnsubscribeFrame(String id) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("id", id);
        return buildFrame("UNSUBSCRIBE", headers, null);
    }

    public static String buildSendFrame(String destination, String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("destination", destination);
        headers.put("content-type", "application/json");
        return buildFrame("SEND", headers, body);
    }

    public static String buildDisconnectFrame() {
        return buildFrame("DISCONNECT", null, null);
    }

    public static StompFrame parseFrame(String raw) {
        if (raw == null) {
            return null;
        }

        int start = 0;
        while (start < raw.length() && (raw.charAt(start) == '\n' || raw.charAt(start) == '\r' || raw.charAt(start) == ' ')) {
            start++;
        }
        if (start >= raw.length()) {
            return null; // Heartbeat ping
        }
        String content = raw.substring(start);

        int headerEnd = -1;
        int bodyStart = -1;
        int idxRN = content.indexOf("\r\n\r\n");
        int idxNN = content.indexOf("\n\n");

        if (idxRN != -1 && (idxNN == -1 || idxRN < idxNN)) {
            headerEnd = idxRN;
            bodyStart = idxRN + 4;
        } else if (idxNN != -1) {
            headerEnd = idxNN;
            bodyStart = idxNN + 2;
        }

        String headerSection;
        String body = "";
        if (headerEnd != -1) {
            headerSection = content.substring(0, headerEnd);
            body = content.substring(bodyStart);
            int nullIdx = body.indexOf('\0');
            if (nullIdx != -1) {
                body = body.substring(0, nullIdx);
            }
        } else {
            headerSection = content.replace("\0", "");
        }

        String[] lines = headerSection.split("\r?\n");
        if (lines.length == 0 || lines[0].trim().isEmpty()) {
            return null;
        }

        String command = lines[0].trim();
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String val = line.substring(colon + 1).trim();
                headers.put(key, val);
            }
        }

        return new StompFrame(command, headers, body);
    }

    // =========================================================================
    // URL Helpers
    // =========================================================================

    public static String getDefaultWebSocketUrl() {
        return convertToWebSocketUrl(Constants.BASE_URL);
    }

    public static String convertToWebSocketUrl(String httpUrl) {
        if (httpUrl == null || httpUrl.trim().isEmpty()) {
            return "ws://localhost:8080/ws";
        }
        String trimmed = httpUrl.trim();
        String scheme = "ws";
        if (trimmed.toLowerCase().startsWith("https://")) {
            scheme = "wss";
        }
        try {
            URI uri = URI.create(trimmed);
            String host = uri.getHost();
            int port = uri.getPort();
            if (host == null) {
                throw new IllegalArgumentException("No host in URL: " + httpUrl);
            }
            String portPart = (port != -1) ? (":" + port) : "";
            return scheme + "://" + host + portPart + "/ws";
        } catch (Exception e) {
            String stripped = trimmed.replaceFirst("(?i)^https?://", "");
            int slashIdx = stripped.indexOf('/');
            String hostAndPort = (slashIdx != -1) ? stripped.substring(0, slashIdx) : stripped;
            return scheme + "://" + hostAndPort + "/ws";
        }
    }
}
