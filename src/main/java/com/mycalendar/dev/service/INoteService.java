package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.NoteUpsertRequest;
import com.mycalendar.dev.payload.response.NoteImageUploadResponse;
import com.mycalendar.dev.payload.response.NotePageResponse;
import com.mycalendar.dev.payload.response.NoteResponse;
import org.springframework.web.multipart.MultipartFile;

public interface INoteService {
    NoteResponse create(NoteUpsertRequest request);

    NoteResponse getById(Long id);

    NoteResponse update(Long id, NoteUpsertRequest request);

    void delete(Long id);

    NotePageResponse list(Integer pageNo,
                          Integer pageSize,
                          String search,
                          String sortBy,
                          String sortDirection,
                          Boolean isPinned);

    NoteImageUploadResponse uploadImage(MultipartFile image);

    boolean deleteUploadedImage(String url);
}

