package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group")
public class GroupRestController {

    private final IGroupService groupService;

    public GroupRestController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody GroupRequest request) {
        groupService.create(request);
        return ResponseEntity.ok("Group created successfully.");
    }

    @PostMapping("/all")
    public PaginationResponse<GroupResponse> getGroups(@RequestBody PaginationRequest request) {
        return groupService.getAllGroup(request);
    }
}
