package com.mycalendar.dev.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Data
public class PaginationRequest {
    @Schema(example = "1")
    private int pageNumber;
    @Schema(example = "10")
    private int pageSize;
    @Schema(example = "id")
    private String sortBy;
    @Schema(example = "DESC")
    private String sortOrder;
    private Map<String, Object> filter;

    @JsonIgnore
    public PageRequest getPageRequest() {
        Sort sortDir = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber - 1, pageSize, sortDir);
    }
}
