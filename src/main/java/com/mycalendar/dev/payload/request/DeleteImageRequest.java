package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteImageRequest {
    @NotBlank(message = "Image URL is required")
    private String url;
}

