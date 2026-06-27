package com.brace.server.upload.dto;

import lombok.Builder;

public class UploadResDTO {

    @Builder
    public record presignedUrl(
            String uploadUrl,
            String fileUrl,
            String objectKey,
            String contentType,
            Long expiresInSeconds
    ) {
    }
}
