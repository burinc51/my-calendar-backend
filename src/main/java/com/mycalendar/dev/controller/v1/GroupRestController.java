package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.AddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.service.IGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupRestController {
    private final IGroupService groupService;

    public GroupRestController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@Valid @RequestBody GroupRequest groupRequest) {
        groupService.createGroup(groupRequest, 1L);
        return ResponseEntity.status(HttpStatus.CREATED).body("Group created successfully.");
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<String> addMember(@PathVariable Long groupId, @RequestBody AddMemberRequest requestAddMemberRequest) {
        groupService.addMember(groupId, requestAddMemberRequest.getMemberId(), requestAddMemberRequest.getUserId());
        return ResponseEntity.ok("Member added successfully.");
    }
}
