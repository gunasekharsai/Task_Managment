package com.taskmanagment.app.controller;


import java.net.URLEncoder;

import com.taskmanagment.app.Dto.AttachmentResponseDto;
import com.taskmanagment.app.Dto.Responses;
import com.taskmanagment.app.Models.Attachment;
import com.taskmanagment.app.security.UserPrincipal;
import com.taskmanagment.app.service.AttachmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.taskmanagment.app.Repository.AttachmentRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
// @Tag(name = "Attachments", description = "File attachments on tasks — upload, download, delete")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    // ── Upload ────────────────────────────────────────────────────────────────

    @PostMapping("/tasks/{taskId}/attachments")
    // @Operation(summary = "Upload a file attachment to a task (max 10 MB)")
    public ResponseEntity<Responses<AttachmentResponseDto>> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId,
            @RequestParam("file") MultipartFile file) {

        AttachmentResponseDto attachment =
                attachmentService.uploadAttachment(taskId, principal.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Responses.ok("File uploaded", attachment));
    }

    // ── List attachments ──────────────────────────────────────────────────────

    @GetMapping("/tasks/{taskId}/attachments")
    // @Operation(summary = "List all attachments on a task")
    public ResponseEntity<Responses<List<AttachmentResponseDto>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String taskId) {

        return ResponseEntity.ok(Responses.ok(attachmentService.getAttachments(taskId)));
    }

    // ── Download ──────────────────────────────────────────────────────────────

   @GetMapping("/attachments/download/{attachmentId}")
//     @Operation(summary = "Download an attachment file")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String attachmentId) {

        Resource resource = attachmentService.downloadAttachment(attachmentId, principal.getId());

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        // Detect correct Content-Type from stored fileType
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(attachment.getFileType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // URL-encode filename to handle spaces and special characters
        String encodedFilename = URLEncoder.encode(
                attachment.getOriginalFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"; "
                        + "filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION)
                .body(resource);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/attachments/{attachmentId}")
    // @Operation(summary = "Delete an attachment (uploader or task creator)")
    public ResponseEntity<Responses<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String attachmentId) {

        attachmentService.deleteAttachment(attachmentId, principal.getId());
        return ResponseEntity.ok(Responses.ok("Attachment deleted", null));
    }
}