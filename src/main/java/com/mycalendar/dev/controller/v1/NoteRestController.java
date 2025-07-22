package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.entity.Note;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.NoteResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.service.INoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteRestController  {
    private final INoteService noteService;

    public NoteRestController(INoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public NoteResponse createNote(@RequestBody Note note) {
        return noteService.saveNote(note);
    }

    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable UUID id) {
        return noteService.getNoteById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        noteService.delete(id);
        return ResponseEntity.ok("Note deleted successfully.");
    }

    @PostMapping("/all")
    public PaginationResponse getAllNotes(@RequestBody PaginationRequest paginationRequest) {
        return noteService.getAllNotes(paginationRequest);
    }
}
