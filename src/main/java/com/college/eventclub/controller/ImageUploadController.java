package com.college.eventclub.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.college.eventclub.model.Event;
import com.college.eventclub.service.EventService;

@RestController
@RequestMapping("/api/events")
public class ImageUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private final EventService eventService;

    public ImageUploadController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{eventId}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long eventId,
                                         @RequestParam("image") MultipartFile file,
                                         Authentication authentication) {
        try {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "Image must be under 5MB"));
            }

            // Verify ownership
            Event event = eventService.getEventById(eventId);
            if (!event.getClub().getCreatedBy().getEmail().equals(authentication.getName())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorised"));
            }

            // Save file
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = getExtension(file.getOriginalFilename());
            String filename = "event-" + eventId + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Save URL on event
            String imageUrl = "/uploads/" + filename;
            eventService.updateEventImage(eventId, imageUrl, authentication.getName());

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save image: " + e.getMessage()));
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
