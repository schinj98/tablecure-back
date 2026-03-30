package com.example.tablecure.admin.controller;

import com.example.tablecure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final StorageService storageService;

    /**
     * Upload a single image to Cloudflare R2.
     *
     * POST /api/admin/upload/image
     * Content-Type: multipart/form-data
     * Field name : file
     *
     * Returns: { "url": "https://..." }
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = storageService.upload(file, "products/images");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Upload a product video to Cloudflare R2.
     *
     * POST /api/admin/upload/video
     * Content-Type: multipart/form-data
     * Field name : file  (mp4, webm, mov — max 100 MB)
     *
     * Returns: { "url": "https://..." }
     */
    @PostMapping("/video")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String url = storageService.uploadVideo(file, "products/videos");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Delete an image or video from Cloudflare R2 by its public URL.
     *
     * DELETE /api/admin/upload?url=https://...
     */
    @DeleteMapping
    public ResponseEntity<?> deleteFile(@RequestParam("url") String url) {
        try {
            storageService.delete(url);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }
}
