package com.brace.server.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UploadReqDTO {

    public record profileImagePresignedUrl(
            @NotBlank
            @Size(max = 255)
            String fileName,

            @NotBlank
            @Size(max = 100)
            String contentType
    ) {
    }
}
