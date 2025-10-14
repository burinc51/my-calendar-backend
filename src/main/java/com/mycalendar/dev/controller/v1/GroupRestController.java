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
import java.util.Map;

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

    @PutMapping("/update/{groupId}")
    public GroupResponse updateGroup(@RequestBody GroupRequest request, @PathVariable Long groupId) {
        return groupService.update(request, groupId);
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroupById(@PathVariable Long groupId) {
        return groupService.getGroupById(groupId);
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

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupResponse response = groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable Long groupId, @RequestParam Long requestUserId) {
        groupService.deleteGroup(groupId, requestUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Group deleted successfully",
                "groupId", groupId
        ));
    }
}
