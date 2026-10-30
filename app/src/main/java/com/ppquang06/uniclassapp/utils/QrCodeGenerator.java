package com.ppquang06.uniclassapp.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for generating real ISO/IEC 18004 QR code bitmaps using ZXing for attendance sessions.
 */
public final class QrCodeGenerator {

    private static final String TAG = "QrCodeGenerator";
    private static final int MATRIX_SIZE = 29;

    private QrCodeGenerator() {}

    /**
     * Generates a real 2D QR Code Bitmap using ZXing QRCodeWriter for the given content payload.
     * Any standard barcode reader can decode the resulting QR code.
     *
     * @param content Dynamic payload (e.g., UNICLASS:<classCode>:<sessionId>:<timestamp>).
     * @param dimensionPx Width and height of the resulting Bitmap in pixels.
     * @return Generated Bitmap with quiet zone margin and valid QR structure.
     */
    public static Bitmap generateQrBitmap(String content, int dimensionPx) {
        if (dimensionPx <= 0) {
            dimensionPx = 512;
        }
        if (content == null || content.isEmpty()) {
            content = "UNICLASS";
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, dimensionPx, dimensionPx, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[width * height];
            int darkColor = 0xFF0F172A; // Dark Slate #0F172A matching EduLens text_primary
            int lightColor = Color.WHITE;

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? darkColor : lightColor;
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "Error encoding QR code with ZXing", e);
            Bitmap fallback = Bitmap.createBitmap(dimensionPx, dimensionPx, Bitmap.Config.ARGB_8888);
            fallback.eraseColor(Color.WHITE);
            return fallback;
        }
    }

    /**
     * Constructs a 29x29 QR matrix with standard finder patterns and payload-derived data cells.
     * Retained for backward-compatible deterministic unit testing.
     */
    public static boolean[][] generateMatrix(String content) {
        boolean[][] grid = new boolean[MATRIX_SIZE][MATRIX_SIZE];
        boolean[][] reserved = new boolean[MATRIX_SIZE][MATRIX_SIZE];

        // 1. Finder patterns at Top-Left, Top-Right, Bottom-Left (7x7)
        placeFinderPattern(grid, reserved, 0, 0);
        placeFinderPattern(grid, reserved, 0, MATRIX_SIZE - 7);
        placeFinderPattern(grid, reserved, MATRIX_SIZE - 7, 0);

        // 2. Timing patterns (Row 6 and Col 6)
        for (int i = 7; i < MATRIX_SIZE - 7; i++) {
            grid[6][i] = (i % 2 == 0);
            reserved[6][i] = true;
            grid[i][6] = (i % 2 == 0);
            reserved[i][6] = true;
        }

        // 3. Alignment pattern at (MATRIX_SIZE - 9, MATRIX_SIZE - 9)
        placeAlignmentPattern(grid, reserved, MATRIX_SIZE - 9, MATRIX_SIZE - 9);

        // 4. Populate remaining modules deterministically using hash of content
        byte[] hash = computeHash(content != null ? content : "UNICLASS");
        int bitIndex = 0;

        for (int col = MATRIX_SIZE - 1; col >= 0; col--) {
            for (int row = 0; row < MATRIX_SIZE; row++) {
                if (!reserved[row][col]) {
                    int byteIdx = (bitIndex / 8) % hash.length;
                    int bitInByte = bitIndex % 8;
                    boolean isBitSet = ((hash[byteIdx] >> bitInByte) & 1) == 1;

                    // Apply a standard XOR mask pattern ((row + col) % 2 == 0)
                    boolean mask = ((row + col) % 2 == 0);
                    grid[row][col] = isBitSet ^ mask;

                    bitIndex++;
                }
            }
        }

        return grid;
    }

    private static void placeFinderPattern(boolean[][] grid, boolean[][] reserved, int startR, int startC) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                int row = startR + r;
                int col = startC + c;
                reserved[row][col] = true;

                // 7x7 outer square or 3x3 inner core
                boolean isBorder = (r == 0 || r == 6 || c == 0 || c == 6);
                boolean isInnerCore = (r >= 2 && r <= 4 && c >= 2 && c <= 4);
                grid[row][col] = isBorder || isInnerCore;
            }
        }
    }

    private static void placeAlignmentPattern(boolean[][] grid, boolean[][] reserved, int startR, int startC) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                int row = startR + r;
                int col = startC + c;
                reserved[row][col] = true;

                boolean isBorder = (r == 0 || r == 4 || c == 0 || c == 4);
                boolean isCenter = (r == 2 && c == 2);
                grid[row][col] = isBorder || isCenter;
            }
        }
    }

    private static byte[] computeHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            byte[] bytes = new byte[32];
            byte[] src = text.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < 32; i++) {
                bytes[i] = (byte) (src[i % src.length] ^ (i * 31));
            }
            return bytes;
        }
    }
}
