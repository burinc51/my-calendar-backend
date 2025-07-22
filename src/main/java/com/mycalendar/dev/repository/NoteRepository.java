package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    @Override
    Optional<Note> findById(UUID uuid);

    Page<Note> findAll(Specification<Note> spec, Pageable pageable);
}
