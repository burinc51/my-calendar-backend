package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventResponse;
import io.micrometer.common.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface IEventService {

    EventResponse saveOrUpdate(EventRequest request, @Nullable MultipartFile file);

    PaginationResponse<EventResponse> getAllEventByGroup(Long groupId, PaginationRequest request);

}
