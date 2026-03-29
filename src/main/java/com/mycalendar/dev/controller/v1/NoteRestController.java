package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.DeleteImageRequest;
import com.mycalendar.dev.payload.request.NoteUpsertRequest;
import com.mycalendar.dev.payload.response.NoteImageUploadResponse;
import com.mycalendar.dev.payload.response.NotePageResponse;
import com.mycalendar.dev.payload.response.NoteResponse;
import com.mycalendar.dev.service.INoteService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteRestController {

    private final INoteService noteService;

    public NoteRestController(INoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/create")
    public NoteResponse create(@Valid @RequestBody NoteUpsertRequest request) {
        return noteService.create(request);
    }

    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable Long id) {
        return noteService.getById(id);
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteUpsertRequest request) {
        return noteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Note deleted successfully",
                "data", Map.of("id", id)
        ));
    }

    @GetMapping("/all")
    public NotePageResponse list(@RequestParam(defaultValue = "1") Integer pageNo,
                                 @RequestParam(defaultValue = "20") Integer pageSize,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(defaultValue = "updatedAt") String sortBy,
                                 @RequestParam(defaultValue = "desc") String sortDirection,
                                 @RequestParam(required = false) Boolean isPinned) {
        return noteService.list(pageNo, pageSize, search, sortBy, sortDirection, isPinned);
    }

    @PostMapping(path = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteImageUploadResponse uploadImage(@RequestPart("image") MultipartFile image) {
        return noteService.uploadImage(image);
    }

    @DeleteMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> deleteUploadedImage(@Valid @RequestBody DeleteImageRequest request) {
        boolean deleted = noteService.deleteUploadedImage(request.getUrl());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", deleted ? "Image deleted successfully" : "Image not found",
                "data", Map.of("url", request.getUrl(), "deleted", deleted)
        ));
    }
}

