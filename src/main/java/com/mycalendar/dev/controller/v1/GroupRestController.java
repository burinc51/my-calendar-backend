package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.AddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.RemoveMembersRequest;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.service.IGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group")
public class GroupRestController {

    // TODO: update group, fix response get by Id
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
        groupService.addMember(groupId, requestAddMemberRequest.getMemberId(), requestAddMemberRequest.getUserAdminId());
        return ResponseEntity.ok("Member added successfully.");
    }

    @GetMapping("/{id}")
    public GroupResponse getById(@PathVariable Long id) {
        return groupService.getGroupById(id);
    }

    @DeleteMapping("/{groupId}/members")
    public ResponseEntity<String> removeMembers(
            @PathVariable Long groupId,
            @RequestBody RemoveMembersRequest request) {
        groupService.removeMembers(groupId, request.getMemberIds(), request.getUserAdminId());
        return ResponseEntity.ok("Members removed successfully.");
    }

    @DeleteMapping("")
    public ResponseEntity<String> delete(@RequestParam Long groupId, @RequestParam Long userAdminId) {
        groupService.delete(groupId, userAdminId);
        return ResponseEntity.ok("Group deleted successfully.");
    }
}
