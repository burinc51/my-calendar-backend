package com.mycalendar.dev.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Data
public class PaginationRequest {
    @Schema(example = "0")
    private int page;
    @Schema(example = "10")
    private int size;
    @Schema(example = "id")
    private String filter;
    @Schema(example = "asc")
    private String sort;
    private Map<String, Object> keyword;

    @JsonIgnore
    public PageRequest getPageRequest() {
        Sort sortDir = sort.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(filter).ascending()
                : Sort.by(filter).descending();
        return PageRequest.of(page, size, sortDir);
    }
}
