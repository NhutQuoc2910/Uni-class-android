package com.ppquang06.uniclassapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.OutputStream;

/**
 * Helper to save generated attendance QR code bitmaps into the device gallery (MediaStore).
 */
public final class QrImageStorageHelper {

    private static final String TAG = "QrImageStorageHelper";

    private QrImageStorageHelper() {}

    /**
     * Saves a QR bitmap to the public pictures gallery under 'Pictures/UniClass'.
     *
     * @param context Android context for content resolution.
     * @param bitmap QR code bitmap to save.
     * @param title Title prefix for the file name.
     * @return Content Uri of the saved image, or null on failure.
     */
    public static Uri saveQrBitmapToGallery(Context context, Bitmap bitmap, String title) {
        if (context == null || bitmap == null) {
            return null;
        }

        String safeTitle = (title != null && !title.trim().isEmpty())
                ? title.trim().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "QR_Attendance";
        String fileName = safeTitle + "_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/UniClass");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return null;
            }

            try (OutputStream os = resolver.openOutputStream(uri)) {
                if (os == null) {
                    return null;
                }
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                if (!compressed) {
                    Log.w(TAG, "Bitmap compression returned false");
                }
                os.flush();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            }

            return uri;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save QR bitmap to gallery", e);
            if (uri != null) {
                try {
                    resolver.delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            return null;
        }
    }
}
