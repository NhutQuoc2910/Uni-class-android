package com.ppquang06.uniclassapp.data.model.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;

import org.junit.Test;

public class FileUploadResponseTest {

    private final Gson gson = new Gson();

    @Test
    public void testSerializationAndDeserialization() {
        FileUploadResponse response = new FileUploadResponse(
                "bai_tap_1.pdf",
                "http://70.153.85.133:8080/api/v1/storage/files/assignments/bai_tap_1.pdf",
                "application/pdf",
                102400L
        );

        String json = gson.toJson(response);
        FileUploadResponse deserialized = gson.fromJson(json, FileUploadResponse.class);

        assertNotNull(deserialized);
        assertEquals("bai_tap_1.pdf", deserialized.getFileName());
        assertEquals("http://70.153.85.133:8080/api/v1/storage/files/assignments/bai_tap_1.pdf", deserialized.getFileUrl());
        assertEquals("application/pdf", deserialized.getFileType());
        assertEquals(102400L, deserialized.getFileSize());
    }

    @Test
    public void testGettersAndSetters() {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName("slide.pptx");
        response.setFileUrl("https://storage.example.com/slide.pptx");
        response.setFileType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        response.setFileSize(2048000L);

        assertEquals("slide.pptx", response.getFileName());
        assertEquals("https://storage.example.com/slide.pptx", response.getFileUrl());
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", response.getFileType());
        assertEquals(2048000L, response.getFileSize());
    }
}
