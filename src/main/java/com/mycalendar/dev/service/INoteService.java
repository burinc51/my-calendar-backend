package com.mycalendar.dev.service;

import com.mycalendar.dev.entity.Note;
import com.mycalendar.dev.payload.response.NoteResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.request.PaginationRequest;

import java.util.List;
import java.util.UUID;

public interface INoteService {
    NoteResponse saveNote(Note note);
    NoteResponse getNoteById(UUID noteId);
    void delete(UUID noteId);
    PaginationResponse getAllNotes(PaginationRequest paginationRequest);
}
