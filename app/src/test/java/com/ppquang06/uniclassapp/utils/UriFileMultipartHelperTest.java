package com.ppquang06.uniclassapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.IOException;

import okhttp3.RequestBody;
import okio.Buffer;

public class UriFileMultipartHelperTest {

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
