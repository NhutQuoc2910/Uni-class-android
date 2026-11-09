package com.ppquang06.uniclassapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.IOException;

import okhttp3.RequestBody;
import okio.Buffer;

public class UriFileMultipartHelperTest {

    @Test
    public void maxFileSize_isConfiguredTo25MB() {
        assertEquals(25 * 1024 * 1024L, UriFileMultipartHelper.MAX_FILE_SIZE);
    }

    @Test
    public void getFileName_returnsDefaultWhenUriNull() {
        String name = UriFileMultipartHelper.getFileName(null, null);
        assertEquals("tep_tin_chon.bin", name);
    }

    @Test
    public void getMimeType_returnsDefaultWhenUriNull() {
        String mime = UriFileMultipartHelper.getMimeType(null, null);
        assertEquals("application/octet-stream", mime);
    }

    @Test
    public void resolveFullFileUrl_resolvesRelativeStoragePathsCorrectly() {
        String baseUrl = "http://70.153.85.133:8080/api/v1/";

        assertEquals(
                "http://70.153.85.133:8080/api/v1/storage/files/avatars/test.jpg",
                UriFileMultipartHelper.resolveFullFileUrl("/api/v1/storage/files/avatars/test.jpg", baseUrl)
        );
        assertEquals(
                "http://70.153.85.133:8080/api/v1/storage/files/assignments/hw.pdf",
                UriFileMultipartHelper.resolveFullFileUrl("storage/files/assignments/hw.pdf", baseUrl)
        );
        assertEquals(
                "https://example.supabase.co/storage/v1/object/public/uniclass-files/materials/slide.pptx",
                UriFileMultipartHelper.resolveFullFileUrl(
                        "https://example.supabase.co/storage/v1/object/public/uniclass-files/materials/slide.pptx",
                        baseUrl
                )
        );
        assertEquals("", UriFileMultipartHelper.resolveFullFileUrl(null, baseUrl));
        assertEquals("", UriFileMultipartHelper.resolveFullFileUrl("   ", baseUrl));
    }

    @Test
    public void ensureFileExtension_infersExtensionWhenMissingOrBinFallback() {
        assertEquals(
                "tep_tin_chon.jpg",
                UriFileMultipartHelper.ensureFileExtension("tep_tin_chon.bin", "image/jpeg")
        );
        assertEquals(
                "1000000033.png",
                UriFileMultipartHelper.ensureFileExtension("1000000033", "image/png")
        );
        assertEquals(
                "bai_tap_1.pdf",
                UriFileMultipartHelper.ensureFileExtension("bai_tap_1.pdf", "application/pdf")
        );
        assertEquals(
                "report.docx",
                UriFileMultipartHelper.ensureFileExtension(
                        "report",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
    }

    @Test
    public void createFolderRequestBody_createsPlainTextRequestBody() throws IOException {
        RequestBody body = UriFileMultipartHelper.createFolderRequestBody("assignments");
        assertNotNull(body);
        assertNotNull(body.contentType());
        assertEquals("text/plain; charset=utf-8", body.contentType().toString());

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        assertEquals("assignments", buffer.readUtf8());
    }

    @Test
    public void createFolderRequestBody_defaultsToMaterialsWhenNullOrEmpty() throws IOException {
        RequestBody body = UriFileMultipartHelper.createFolderRequestBody(null);
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        assertEquals("materials", buffer.readUtf8());

        RequestBody body2 = UriFileMultipartHelper.createFolderPart("   ");
        Buffer buffer2 = new Buffer();
        body2.writeTo(buffer2);
        assertEquals("materials", buffer2.readUtf8());
    }
}
