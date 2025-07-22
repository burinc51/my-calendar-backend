package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Note;
import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.NoteResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.RoleResponse;
import com.mycalendar.dev.repository.NoteRepository;
import com.mycalendar.dev.service.INoteService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService implements INoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public NoteResponse saveNote(Note note) {
        Note noteService = noteRepository.save(note);
        return EntityMapper.mapToEntity(noteService, NoteResponse.class);
    }

    @Override
    public NoteResponse getNoteById(UUID noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Note", "id", noteId.toString()));
        return EntityMapper.mapToEntity(note, NoteResponse.class);
    }

    @Override
    public void delete(UUID noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + noteId));
        noteRepository.delete(note);
    }

    @Override
    public PaginationResponse getAllNotes(PaginationRequest request) {
        Map<String, Object> keywordMap = request.getFilter();
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        Specification<Note> spec = new GenericSpecification<Note>().getSpecification(keywordMap, fields);

        Page<Note> pages = noteRepository.findAll(spec, request.getPageRequest());

        List<NoteResponse> content = pages.stream()
                .map(note -> EntityMapper.mapToEntity(note, NoteResponse.class))
                .collect(Collectors.toList());

        return new PaginationResponse(content, request.getPageNumber(), request.getPageSize(), pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }

}
