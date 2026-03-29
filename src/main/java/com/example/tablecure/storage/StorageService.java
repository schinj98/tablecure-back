package com.example.tablecure.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client r2Client;

    @Value("${r2.bucket-name}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    /**
     * Uploads a file to Cloudflare R2 under the given folder prefix
     * and returns its public URL.
     */
    public String upload(MultipartFile file, String folder) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed: jpg, png, webp, gif");
        }

        String ext      = extractExtension(file.getOriginalFilename());
        String key      = folder + "/" + UUID.randomUUID() + ext;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        r2Client.putObject(request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return publicUrl + "/" + key;
    }

    /**
     * Deletes a file from R2 given its full public URL.
     * Safe to call even if the URL is external (does nothing).
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(publicUrl)) {
            return; // external URL — not ours to delete
        }
        String key = fileUrl.substring(publicUrl.length() + 1); // strip leading /
        r2Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
