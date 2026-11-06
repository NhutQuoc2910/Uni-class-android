package com.ppquang06.uniclassapp.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QrDecodeAndDeleteFlowTest {

    private final Map<Bitmap, int[]> bitmapPixelMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        bitmapPixelMap.clear();
        Constructor<Bitmap> bitmapConstructor = Bitmap.class.getDeclaredConstructor();
        bitmapConstructor.setAccessible(true);

        QrCodeGenerator.setTestBitmapBridge(
                (width, height, pixels) -> {
                    try {
                        Bitmap dummyBitmap = bitmapConstructor.newInstance();
                        bitmapPixelMap.put(dummyBitmap, pixels);
                        return dummyBitmap;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                bitmap -> bitmapPixelMap.get(bitmap)
        );
    }

    @After
    public void tearDown() {
        QrCodeGenerator.setTestBitmapBridge(null, null);
        bitmapPixelMap.clear();
    }

    @Test
    public void testQrCodeGenerator_roundTripDecoding() {
        String testPayload = "UNICLASS:NT118.Q11:SES-ATT-12345:1700000000";
        int dimension = 256;

        Bitmap generatedBitmap = QrCodeGenerator.generateQrBitmap(testPayload, dimension);
        assertNotNull("Generated bitmap should not be null", generatedBitmap);

        String decoded = QrCodeGenerator.decodeQrBitmap(generatedBitmap);
        assertEquals("Round trip decoded string should match original payload", testPayload, decoded);
    }

    @Test
    public void testQrCodeGenerator_decodePixelsDirectRoundTrip() {
        String testPayload = "UNICLASS:CS50:SES-SESSION-999:1730000000";
        int dimension = 256;

        Bitmap bitmap = QrCodeGenerator.generateQrBitmap(testPayload, dimension);
        int[] pixels = bitmapPixelMap.get(bitmap);
        assertNotNull("Pixels should be non-null", pixels);

        String decoded = QrCodeGenerator.decodeQrPixels(pixels, dimension, dimension);
        assertEquals("Decoded pixels should match original payload", testPayload, decoded);
    }

    @Test
    public void testQrCodeGenerator_decodeNullOrInvalidReturnsNull() {
        assertNull(QrCodeGenerator.decodeQrBitmap(null));
        assertNull(QrCodeGenerator.decodeQrPixels(null, 0, 0));
        assertNull(QrCodeGenerator.decodeQrPixels(new int[100], 10, 10));
    }

    @Test
    public void testNotificationAdapter_downloadQrButtonVisibilityLogic() {
        NotificationItemResponse attItem = new NotificationItemResponse();
        attItem.setId("att-9988");
        attItem.setTitle("Phiên điểm danh");

        NotificationItemResponse normalItem = new NotificationItemResponse();
        normalItem.setId("norm-1234");
        normalItem.setTitle("Thông báo thường");

        NotificationItemResponse nullIdItem = new NotificationItemResponse();
        nullIdItem.setId(null);

        NotificationAdapter.OnDownloadQrClickListener listener = item -> {};

        // att- item with listener -> shows download QR
        assertTrue(NotificationAdapter.shouldShowDownloadQr(attItem, listener));

        // att- item without listener -> hides download QR
        assertFalse(NotificationAdapter.shouldShowDownloadQr(attItem, null));

        // regular item with listener -> hides download QR
        assertFalse(NotificationAdapter.shouldShowDownloadQr(normalItem, listener));

        // null id item with listener -> hides download QR
        assertFalse(NotificationAdapter.shouldShowDownloadQr(nullIdItem, listener));

        // null item -> hides download QR
        assertFalse(NotificationAdapter.shouldShowDownloadQr(null, listener));
    }

    @Test
    public void testNotificationAdapter_deleteButtonVisibilityLogic() {
        NotificationAdapter.OnDeleteNotificationListener deleteListener = item -> {};

        // delete listener set -> shows delete button
        assertTrue(NotificationAdapter.shouldShowDelete(deleteListener));

        // null delete listener -> hides delete button
        assertFalse(NotificationAdapter.shouldShowDelete(null));
    }

    @Test
    public void testNotificationAdapter_listenersSetters() {
        NotificationAdapter adapter = new NotificationAdapter();
        assertNull(adapter.getOnDownloadQrClickListener());
        assertNull(adapter.getOnDeleteNotificationListener());
        assertNull(adapter.getOnNotificationClickListener());

        NotificationAdapter.OnDownloadQrClickListener downloadListener = item -> {};
        NotificationAdapter.OnDeleteNotificationListener deleteListener = item -> {};
        NotificationAdapter.OnNotificationClickListener clickListener = item -> {};

        adapter.setOnDownloadQrClickListener(downloadListener);
        adapter.setOnDeleteNotificationListener(deleteListener);
        adapter.setOnNotificationClickListener(clickListener);

        assertEquals(downloadListener, adapter.getOnDownloadQrClickListener());
        assertEquals(deleteListener, adapter.getOnDeleteNotificationListener());
        assertEquals(clickListener, adapter.getOnNotificationClickListener());
    }

    @Test
    public void testNotificationAdapter_fromAnnouncementMapping() {
        ClassDetailResponse.AnnouncementItem announcement = new ClassDetailResponse.AnnouncementItem();
        announcement.setId("att-session-uuid-123");
        announcement.setTitle("Điểm danh đang mở");
        announcement.setContent("Phiên điểm danh đang mở. Nhấn 'Tải mã QR' để lưu ảnh QR.");
        announcement.setUrgent(true);
        announcement.setUnread(true);
        announcement.setCreatedAt("2026-11-06T12:00:00Z");

        NotificationItemResponse mapped = NotificationAdapter.fromAnnouncement(announcement, "NT118");
        assertEquals("att-session-uuid-123", mapped.getId());
        assertEquals("Điểm danh đang mở", mapped.getTitle());
        assertEquals("Phiên điểm danh đang mở. Nhấn 'Tải mã QR' để lưu ảnh QR.", mapped.getBody());
        assertEquals("NT118", mapped.getCourseCode());
        assertEquals("URGENT", mapped.getCategory());
        assertTrue(mapped.isUnread());

        // Also test mapping a list
        List<NotificationItemResponse> list = NotificationAdapter.fromAnnouncements(
                Collections.singletonList(announcement),
                "NT118"
        );
        assertEquals(1, list.size());
        assertEquals("att-session-uuid-123", list.get(0).getId());

        // And verify shouldShowDownloadQr works with mapped announcement item
        NotificationAdapter.OnDownloadQrClickListener listener = item -> {};
        assertTrue(NotificationAdapter.shouldShowDownloadQr(list.get(0), listener));
    }
}
