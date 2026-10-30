package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.api.StompWebSocketClient;
import com.ppquang06.uniclassapp.data.api.StompWebSocketClient.StompFrame;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.utils.Constants;

import org.junit.Before;
import org.junit.Test;

public class StompWebSocketClientTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    // =========================================================================
    // 1. STOMP Frame Builder Tests
    // =========================================================================

    @Test
    public void testBuildConnectFrameWithToken() {
        String frame = StompWebSocketClient.buildConnectFrame("mock-jwt-token-123");

        assertTrue(frame.startsWith("CONNECT\n"));
        assertTrue(frame.contains("accept-version:1.1,1.2\n"));
        assertTrue(frame.contains("heart-beat:10000,10000\n"));
        assertTrue(frame.contains("Authorization:Bearer mock-jwt-token-123\n"));
        assertTrue(frame.endsWith("\n\0"));
    }

    @Test
    public void testBuildConnectFrameWithExistingBearerPrefix() {
        String frame = StompWebSocketClient.buildConnectFrame("Bearer already-prefixed-token");

        assertTrue(frame.contains("Authorization:Bearer already-prefixed-token\n"));
        assertFalse(frame.contains("Bearer Bearer"));
    }

    @Test
    public void testBuildConnectFrameWithoutToken() {
        String frameNull = StompWebSocketClient.buildConnectFrame(null);
        assertFalse(frameNull.contains("Authorization:"));

        String frameEmpty = StompWebSocketClient.buildConnectFrame("   ");
        assertFalse(frameEmpty.contains("Authorization:"));
    }

    @Test
    public void testBuildSubscribeFrame() {
        String frame = StompWebSocketClient.buildSubscribeFrame("sub-1", "/topic/attendance-sessions/123/live");

        assertTrue(frame.startsWith("SUBSCRIBE\n"));
        assertTrue(frame.contains("id:sub-1\n"));
        assertTrue(frame.contains("destination:/topic/attendance-sessions/123/live\n"));
        assertTrue(frame.contains("ack:auto\n"));
        assertTrue(frame.endsWith("\n\0"));
    }

    @Test
    public void testBuildUnsubscribeFrame() {
        String frame = StompWebSocketClient.buildUnsubscribeFrame("sub-42");

        assertTrue(frame.startsWith("UNSUBSCRIBE\n"));
        assertTrue(frame.contains("id:sub-42\n"));
        assertTrue(frame.endsWith("\n\0"));
    }

    @Test
    public void testBuildSendFrame() {
        String jsonPayload = "{\"message\":\"hello\"}";
        String frame = StompWebSocketClient.buildSendFrame("/app/chat", jsonPayload);

        assertTrue(frame.startsWith("SEND\n"));
        assertTrue(frame.contains("destination:/app/chat\n"));
        assertTrue(frame.contains("content-type:application/json\n"));
        assertTrue(frame.contains(jsonPayload));
        assertTrue(frame.endsWith(jsonPayload + "\0"));
    }

    @Test
    public void testBuildDisconnectFrame() {
        String frame = StompWebSocketClient.buildDisconnectFrame();

        assertEquals("DISCONNECT\n\n\0", frame);
    }

    // =========================================================================
    // 2. STOMP Frame Parser Tests
    // =========================================================================

    @Test
    public void testParseConnectedFrame() {
        String raw = "CONNECTED\nversion:1.2\nheart-beat:10000,10000\nsession:sess-001\n\n\0";
        StompFrame frame = StompWebSocketClient.parseFrame(raw);

        assertNotNull(frame);
        assertEquals("CONNECTED", frame.getCommand());
        assertEquals("1.2", frame.getHeader("version"));
        assertEquals("10000,10000", frame.getHeader("heart-beat"));
        assertEquals("sess-001", frame.getHeader("session"));
        assertEquals("", frame.getBody());
    }

    @Test
    public void testParseMessageFrameWithLiveAttendanceJson() {
        String liveJson = "{\"sessionId\":\"sess-abc\",\"presentCount\":18,\"totalStudents\":45,\"status\":\"OPEN\"}";
        String raw = "MESSAGE\ndestination:/topic/attendance-sessions/sess-abc/live\nsubscription:sub-1\ncontent-type:application/json\n\n"
                + liveJson + "\0";

        StompFrame frame = StompWebSocketClient.parseFrame(raw);

        assertNotNull(frame);
        assertEquals("MESSAGE", frame.getCommand());
        assertEquals("/topic/attendance-sessions/sess-abc/live", frame.getHeader("destination"));
        assertEquals("sub-1", frame.getHeader("subscription"));
        assertEquals("application/json", frame.getHeader("content-type"));
        assertEquals(liveJson, frame.getBody());

        // Verify JSON deserializes correctly into LiveAttendanceSessionResponse
        LiveAttendanceSessionResponse response = gson.fromJson(frame.getBody(), LiveAttendanceSessionResponse.class);
        assertNotNull(response);
        assertEquals("sess-abc", response.getSessionId());
        assertEquals(18, response.getPresentCount());
        assertEquals(45, response.getTotalStudents());
        assertEquals("OPEN", response.getStatus());
    }

    @Test
    public void testParseMessageFrameWithCrlfLineEndings() {
        String raw = "MESSAGE\r\ndestination:/topic/test\r\nsubscription:sub-99\r\n\r\n{\"key\":\"value\"}\0";
        StompFrame frame = StompWebSocketClient.parseFrame(raw);

        assertNotNull(frame);
        assertEquals("MESSAGE", frame.getCommand());
        assertEquals("/topic/test", frame.getHeader("destination"));
        assertEquals("sub-99", frame.getHeader("subscription"));
        assertEquals("{\"key\":\"value\"}", frame.getBody());
    }

    @Test
    public void testParseHeartbeatPingReturnsNull() {
        assertNull(StompWebSocketClient.parseFrame(null));
        assertNull(StompWebSocketClient.parseFrame(""));
        assertNull(StompWebSocketClient.parseFrame("\n"));
        assertNull(StompWebSocketClient.parseFrame("\r\n"));
        assertNull(StompWebSocketClient.parseFrame("   \n\r\n"));
    }

    @Test
    public void testParseErrorFrame() {
        String raw = "ERROR\nmessage:Access Denied\ncontent-type:text/plain\n\nToken expired or invalid\0";
        StompFrame frame = StompWebSocketClient.parseFrame(raw);

        assertNotNull(frame);
        assertEquals("ERROR", frame.getCommand());
        assertEquals("Access Denied", frame.getHeader("message"));
        assertEquals("Token expired or invalid", frame.getBody());
    }

    // =========================================================================
    // 3. WebSocket URL Conversion Tests
    // =========================================================================

    @Test
    public void testConvertToWebSocketUrl() {
        assertEquals("ws://70.153.85.133:8080/ws",
                StompWebSocketClient.convertToWebSocketUrl("http://70.153.85.133:8080/api/v1/"));

        assertEquals("wss://api.uniclass.edu.vn:8443/ws",
                StompWebSocketClient.convertToWebSocketUrl("https://api.uniclass.edu.vn:8443/api/v1/"));

        assertEquals("wss://edu.domain.com/ws",
                StompWebSocketClient.convertToWebSocketUrl("https://edu.domain.com/classes"));

        assertEquals("ws://10.0.2.2:8080/ws",
                StompWebSocketClient.convertToWebSocketUrl("http://10.0.2.2:8080"));

        assertEquals("ws://localhost:8080/ws",
                StompWebSocketClient.convertToWebSocketUrl(null));

        assertEquals("ws://localhost:8080/ws",
                StompWebSocketClient.convertToWebSocketUrl("   "));
    }

    @Test
    public void testGetDefaultWebSocketUrlMatchesBaseUrl() {
        String defaultWs = StompWebSocketClient.getDefaultWebSocketUrl();
        assertNotNull(defaultWs);
        assertTrue(defaultWs.startsWith("ws://") || defaultWs.startsWith("wss://"));
        assertTrue(defaultWs.endsWith("/ws"));
        assertEquals(StompWebSocketClient.convertToWebSocketUrl(Constants.BASE_URL), defaultWs);
    }

    // =========================================================================
    // 4. Client Lifecycle & Subscription Management Tests
    // =========================================================================

    @Test
    public void testSubscribeGeneratesUniqueIds() {
        StompWebSocketClient client = new StompWebSocketClient();
        assertFalse(client.isConnected());

        String sub1 = client.subscribe("/topic/one", payload -> {});
        String sub2 = client.subscribe("/topic/two", payload -> {});

        assertNotNull(sub1);
        assertNotNull(sub2);
        assertFalse(sub1.equals(sub2));

        client.unsubscribe(sub1);
        client.disconnect();
        assertFalse(client.isConnected());
    }
}
