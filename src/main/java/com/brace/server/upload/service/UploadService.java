package com.brace.server.upload.service;

import com.brace.server.global.code.GeneralErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.upload.dto.UploadReqDTO;
import com.brace.server.upload.dto.UploadResDTO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 300L;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.public-base-url}")
    private String publicBaseUrl;

    public UploadResDTO.presignedUrl createProfileImagePresignedUrl(
            Long userId,
            UploadReqDTO.profileImagePresignedUrl dto
    ) {
        validateS3Properties();
        validateProfileImageContentType(dto.contentType());

        String objectKey = "profile-images/%d/%s.%s".formatted(
                userId,
                UUID.randomUUID(),
                resolveExtension(dto.fileName(), dto.contentType())
        );

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(dto.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(PRESIGNED_URL_EXPIRES_IN_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        return UploadResDTO.presignedUrl.builder()
                .uploadUrl(s3Presigner.presignPutObject(presignRequest).url().toString())
                .fileUrl(resolveFileUrl(objectKey))
                .objectKey(objectKey)
                .contentType(dto.contentType())
                .expiresInSeconds(PRESIGNED_URL_EXPIRES_IN_SECONDS)
                .build();
    }

    private void validateS3Properties() {
        if (bucket == null || bucket.isBlank()) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
        }
    }

    private void validateProfileImageContentType(String contentType) {
        if (!ALLOWED_PROFILE_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
        }
    }

    private String resolveExtension(String fileName, String contentType) {
        int extensionStart = fileName.lastIndexOf(".");
        if (extensionStart >= 0 && extensionStart < fileName.length() - 1) {
            String extension = fileName.substring(extensionStart + 1).toLowerCase();
            if (extension.matches("[a-z0-9]+")) {
                return extension;
            }
        }

        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
        };
    }

    private String resolveFileUrl(String objectKey) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8).replace("+", "%20");
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/$", "") + "/" + encodedKey;
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, encodedKey);
    }
}
