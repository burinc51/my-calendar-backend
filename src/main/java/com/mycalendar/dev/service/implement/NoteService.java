package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Note;
import com.mycalendar.dev.exception.APIException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.NoteUpsertRequest;
import com.mycalendar.dev.payload.response.NoteImageUploadResponse;
import com.mycalendar.dev.payload.response.NotePageResponse;
import com.mycalendar.dev.payload.response.NoteResponse;
import com.mycalendar.dev.repository.NoteRepository;
import com.mycalendar.dev.service.INoteService;
import com.mycalendar.dev.util.SecurityUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService implements INoteService {

    private static final Set<String> ALLOWED_SORT_BY = Set.of("updatedAt", "createdAt", "title");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024 * 1024;

    private final NoteRepository noteRepository;

    @Value("${spring.path.file.upload}")
    private String uploadDir;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    @Transactional
    public NoteResponse create(NoteUpsertRequest request) {
        validateNoteRequest(request);

        Note note = new Note();
        note.setUserId(SecurityUtil.getCurrentUserId());
        applyRequest(note, request);

        return toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note", "id", id.toString()));
        return toResponse(note);
    }

    @Override
    @Transactional
    public NoteResponse update(Long id, NoteUpsertRequest request) {
        validateNoteRequest(request);

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note", "id", id.toString()));

        applyRequest(note, request);

        return toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note", "id", id.toString()));
        noteRepository.delete(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NotePageResponse list(Integer pageNo,
                                 Integer pageSize,
                                 String search,
                                 String sortBy,
                                 String sortDirection,
                                 Boolean isPinned) {
        int normalizedPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);

        String normalizedSortBy = ALLOWED_SORT_BY.contains(sortBy) ? sortBy : "updatedAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(normalizedPageNo - 1, normalizedPageSize, Sort.by(direction, normalizedSortBy));
        Specification<Note> specification = buildSearchSpecification(search, isPinned);

        Page<Note> page = noteRepository.findAll(specification, pageable);

        return NotePageResponse.builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNo(normalizedPageNo)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public NoteImageUploadResponse uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        if (!ALLOWED_MIME_TYPES.contains(image.getContentType())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Only image/jpeg, image/png, image/webp are allowed");
        }

        if (image.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Image size must be less than or equal to 5MB");
        }

        validateMagicNumber(image);

        Path noteUploadDir = getNoteUploadDir();
        String fileName = generateFileName(image.getContentType());
        Path target = noteUploadDir.resolve(fileName).normalize();

        if (!target.startsWith(noteUploadDir)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Files.createDirectories(noteUploadDir);
            image.transferTo(target.toFile());
        } catch (IOException e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded image");
        }

        String publicUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/images/notes/")
                .path(fileName)
                .toUriString();

        return new NoteImageUploadResponse(publicUrl, fileName, image.getContentType(), image.getSize());
    }

    @Override
    @Transactional
    public boolean deleteUploadedImage(String url) {
        if (!StringUtils.hasText(url)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Image URL is required");
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception ex) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Invalid image URL");
        }

        String path = uri.getPath();
        if (!StringUtils.hasText(path) || !path.contains("/images/notes/")) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Image URL must point to /images/notes/");
        }

        String fileName = Paths.get(path).getFileName().toString();
        Path noteUploadDir = getNoteUploadDir();
        Path target = noteUploadDir.resolve(fileName).normalize();

        if (!target.startsWith(noteUploadDir)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image");
        }
    }

    private void applyRequest(Note note, NoteUpsertRequest request) {
        note.setTitle(trimToNull(request.getTitle()));
        note.setContent(trimToNull(request.getContent()));
        note.setColor(request.getColor());
        note.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : Boolean.FALSE);
        note.setTags(normalizeTags(request.getTags()));
        note.setReminderDate(request.getReminderDate());
        note.setRecurrence(request.getRecurrence() == null ? "none" : request.getRecurrence().toLowerCase(Locale.ROOT));
        note.setStartDate(request.getStartDate());
        note.setEndDate(request.getEndDate());
        note.setLocationName(trimToNull(request.getLocationName()));
        note.setLocationLink(trimToNull(request.getLocationLink()));
    }

    private void validateNoteRequest(NoteUpsertRequest request) {
        if (request == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (!request.hasAnyContent()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Either title or content is required");
        }

        if (!request.hasValidDateRange()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "End date must be greater than or equal to start date");
        }

        if (request.getTags() != null && request.getTags().stream().anyMatch(tag -> !StringUtils.hasText(tag))) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Tags must be an array of non-empty strings");
        }
    }

    private Specification<Note> buildSearchSpecification(String search, Boolean isPinned) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isPinned != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPinned"), isPinned));
            }

            if (StringUtils.hasText(search)) {
                String value = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                Join<Note, String> tagsJoin = root.join("tags", JoinType.LEFT);
                query.distinct(true);

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), value),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), value),
                        criteriaBuilder.like(criteriaBuilder.lower(tagsJoin), value)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .userId(note.getUserId())
                .title(note.getTitle())
                .content(note.getContent())
                .color(note.getColor())
                .isPinned(note.getIsPinned())
                .tags(new ArrayList<>(note.getTags()))
                .reminderDate(note.getReminderDate())
                .recurrence(note.getRecurrence())
                .startDate(note.getStartDate())
                .endDate(note.getEndDate())
                .locationName(note.getLocationName())
                .locationLink(note.getLocationLink())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private Path getNoteUploadDir() {
        if (!StringUtils.hasText(uploadDir)) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, "spring.path.file.upload is not configured");
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve("notes").normalize();
    }

    private void validateMagicNumber(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String mimeType = file.getContentType();

            if ("image/jpeg".equals(mimeType) && !isJpeg(bytes)) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Invalid JPEG file");
            }
            if ("image/png".equals(mimeType) && !isPng(bytes)) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Invalid PNG file");
            }
            if ("image/webp".equals(mimeType) && !isWebp(bytes)) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Invalid WEBP file");
            }
        } catch (IOException e) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }
    }

    private String generateFileName(String mimeType) {
        String extension = switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new APIException(HttpStatus.BAD_REQUEST, "Unsupported mime type");
        };

        return "note-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(OffsetDateTime.now())
                + "-" + UUID.randomUUID().toString().substring(0, 8)
                + extension;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length > 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length > 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P';
    }
}

