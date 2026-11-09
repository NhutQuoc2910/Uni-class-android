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
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class UriFileMultipartHelper {

    public static final long MAX_FILE_SIZE = 25 * 1024 * 1024L; // 25 MB
    private static final String DEFAULT_FILE_NAME = "tep_tin_chon.bin";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String DEFAULT_FOLDER = "materials";

    private UriFileMultipartHelper() {
        // Utility class
    }

    /**
     * Resolves a potentially relative file URL (such as "/api/v1/storage/files/...")
     * against Constants.BASE_URL so Glide and Intent.ACTION_VIEW receive a valid HTTP(S) URL.
     */
    @NonNull
    public static String resolveFullFileUrl(@Nullable String fileUrl) {
        return resolveFullFileUrl(fileUrl, Constants.BASE_URL);
    }

    /**
     * Resolves a potentially relative file URL against the provided baseUrl.
     */
    @NonNull
    public static String resolveFullFileUrl(@Nullable String fileUrl, @Nullable String baseUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return "";
        }
        String cleanUrl = fileUrl.trim();
        String lower = cleanUrl.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("content://") || lower.startsWith("file://")) {
            return cleanUrl;
        }

        String base = (baseUrl != null && !baseUrl.trim().isEmpty())
                ? baseUrl.trim()
                : "http://70.153.85.133:8080/api/v1/";
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        String origin = base;
        int apiIndex = base.indexOf("/api/");
        if (apiIndex > 0) {
            origin = base.substring(0, apiIndex);
        }

        if (cleanUrl.startsWith("/")) {
            return origin + cleanUrl;
        }
        return base + "/" + cleanUrl;
    }

    /**
     * Extracts and sanitizes the display name of a file Uri using OpenableColumns.DISPLAY_NAME.
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
        }

        if (result != null) {
            // Strip directory traversal delimiters and control characters to prevent header disruption
            result = result.replace('\\', '/');
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
            result = result.replace("\"", "").replace("\r", "").replace("\n", "").trim();
        }

        return (result != null && !result.isEmpty()) ? result : DEFAULT_FILE_NAME;
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
            String extension = null;
            try {
                extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            } catch (Exception ignored) {
            }
            if (extension == null || extension.isEmpty()) {
                String fileName = getFileName(context, uri);
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                    extension = fileName.substring(dotIndex + 1);
                }
            }
            if (extension != null && !extension.isEmpty()) {
                try {
                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    if (map != null) {
                        mimeType = map.getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
                    }
                } catch (Exception ignored) {
                }
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = inferMimeTypeFromExtension(extension);
                }
            }
        }

        return (mimeType != null && !mimeType.trim().isEmpty()) ? mimeType : DEFAULT_MIME_TYPE;
    }

    /**
     * Ensures the file name has a valid extension matching its MIME type if the URI display name
     * lacks an extension or defaulted to ".bin".
     */
    @NonNull
    public static String ensureFileExtension(@NonNull String fileName, @Nullable String mimeType) {
        String cleanName = fileName.trim();
        if (cleanName.isEmpty()) {
            cleanName = DEFAULT_FILE_NAME;
        }
        int dotIndex = cleanName.lastIndexOf('.');
        boolean hasExtension = dotIndex > 0 && dotIndex < cleanName.length() - 1;
        boolean isBinFallback = cleanName.toLowerCase(Locale.ROOT).endsWith(".bin");

        if (hasExtension && !isBinFallback) {
            return cleanName;
        }

        String ext = inferExtensionFromMimeType(mimeType);
        if (ext.isEmpty()) {
            return cleanName;
        }

        if (isBinFallback) {
            return cleanName.substring(0, cleanName.length() - 4) + "." + ext;
        }
        return cleanName + "." + ext;
    }

    @NonNull
    private static String inferExtensionFromMimeType(@Nullable String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return "";
        }
        String clean = mimeType.trim().toLowerCase(Locale.ROOT);
        int semi = clean.indexOf(';');
        if (semi != -1) {
            clean = clean.substring(0, semi).trim();
        }
        switch (clean) {
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "application/vnd.ms-excel":
                return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "xlsx";
            case "application/vnd.ms-powerpoint":
                return "ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "pptx";
            case "application/zip":
            case "application/x-zip-compressed":
            case "application/x-zip":
                return "zip";
            case "application/vnd.rar":
            case "application/x-rar-compressed":
            case "application/x-rar":
                return "rar";
            case "application/x-7z-compressed":
                return "7z";
            case "text/plain":
                return "txt";
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            case "image/gif":
                return "gif";
            case "image/heic":
                return "heic";
            case "image/heif":
                return "heif";
            default:
                try {
                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    if (map != null) {
                        String mapped = map.getExtensionFromMimeType(clean);
                        if (mapped != null) {
                            return mapped;
                        }
                    }
                } catch (Exception ignored) {
                }
                return "";
        }
    }

    @NonNull
    private static String inferMimeTypeFromExtension(@Nullable String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return DEFAULT_MIME_TYPE;
        }
        switch (extension.trim().toLowerCase(Locale.ROOT)) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/vnd.rar";
            case "7z":
                return "application/x-7z-compressed";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            case "gif":
                return "image/gif";
            case "heic":
                return "image/heic";
            case "heif":
                return "image/heif";
            default:
                return DEFAULT_MIME_TYPE;
        }
    }

    /**
     * Reads bytes from a content Uri with an OOM protection limit (25MB).
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
            long totalBytes = 0;
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                totalBytes += len;
                if (totalBytes > MAX_FILE_SIZE) {
                    throw new IOException("Kích thước tệp vượt quá giới hạn tối đa cho phép (25MB)");
                }
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
        String rawFileName = getFileName(context, uri);
        String mimeType = getMimeType(context, uri);
        String fileName = ensureFileExtension(rawFileName, mimeType);
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
