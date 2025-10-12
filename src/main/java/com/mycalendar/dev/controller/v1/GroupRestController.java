package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/add-member")
    public ResponseEntity<String> addMemberGroup(@RequestBody GroupAddMemberRequest request) {
        groupService.addMemberToGroup(request);
        return ResponseEntity.ok("Member added successfully.");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getGroupsByUserId(@PathVariable Long userId) {
        List<GroupResponse> response = groupService.getGroupsByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
