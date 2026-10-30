package com.ppquang06.uniclassapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class UriFileMultipartHelper {

    private static final String DEFAULT_FILE_NAME = "tep_tin_chon.bin";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String DEFAULT_FOLDER = "materials";

    private UriFileMultipartHelper() {
        // Utility class
    }

    /**
     * Extracts the display name of a file Uri using OpenableColumns.DISPLAY_NAME.
     */
    @NonNull
    public static String getFileName(@Nullable Context context, @Nullable Uri uri) {
        if (uri == null) {
            return DEFAULT_FILE_NAME;
        }

        String result = null;
        if (context != null && ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (colIndex != -1) {
                        result = cursor.getString(colIndex);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (result == null || result.trim().isEmpty()) {
            result = uri.getLastPathSegment();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return (result != null && !result.trim().isEmpty()) ? result.trim() : DEFAULT_FILE_NAME;
    }

    /**
     * Determines MIME type of a file Uri via ContentResolver, falling back to extension inspection.
     */
    @NonNull
    public static String getMimeType(@Nullable Context context, @Nullable Uri uri) {
        if (uri == null) {
            return DEFAULT_MIME_TYPE;
        }

        String mimeType = null;
        if (context != null && ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            try {
                mimeType = context.getContentResolver().getType(uri);
            } catch (Exception ignored) {
            }
        }

        if (mimeType == null || mimeType.trim().isEmpty()) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension == null || extension.isEmpty()) {
                String fileName = getFileName(context, uri);
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                    extension = fileName.substring(dotIndex + 1);
                }
            }
            if (extension != null && !extension.isEmpty()) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }
        }

        return (mimeType != null && !mimeType.trim().isEmpty()) ? mimeType : DEFAULT_MIME_TYPE;
    }

    /**
     * Reads bytes from a content Uri.
     */
    @NonNull
    public static byte[] readBytesFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Cannot open input stream for URI: " + uri);
            }
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }
    }

    /**
     * Creates a MultipartBody.Part from a file Uri for uploading.
     */
    @NonNull
    public static MultipartBody.Part createMultipartPart(
            @NonNull Context context,
            @NonNull Uri uri,
            @NonNull String partName
    ) throws IOException {
        String fileName = getFileName(context, uri);
        String mimeType = getMimeType(context, uri);
        byte[] bytes = readBytesFromUri(context, uri);

        MediaType mediaType = MediaType.parse(mimeType);
        RequestBody requestBody = RequestBody.create(mediaType, bytes);
        return MultipartBody.Part.createFormData(partName, fileName, requestBody);
    }

    /**
     * Overload defaulting part name to "file".
     */
    @NonNull
    public static MultipartBody.Part createMultipartPart(
            @NonNull Context context,
            @NonNull Uri uri
    ) throws IOException {
        return createMultipartPart(context, uri, "file");
    }

    /**
     * Creates a RequestBody part for the folder parameter.
     */
    @NonNull
    public static RequestBody createFolderRequestBody(@Nullable String folderName) {
        String folder = (folderName == null || folderName.trim().isEmpty()) ? DEFAULT_FOLDER : folderName.trim();
        return RequestBody.create(MediaType.parse("text/plain"), folder);
    }

    /**
     * Alias for createFolderRequestBody.
     */
    @NonNull
    public static RequestBody createFolderPart(@Nullable String folderName) {
        return createFolderRequestBody(folderName);
    }
}
