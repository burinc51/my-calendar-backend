package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupUserResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IGroupService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        GroupResponse response = groupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @GetMapping("/{groupId}/users")
    public ResponseEntity<List<GroupUserResponse>> getUsersByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getUsersByGroupId(groupId));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable Long groupId, @RequestParam Long requestUserId) {
        groupService.deleteGroup(groupId, requestUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Group deleted successfully",
                "groupId", groupId
        ));
    }

    @PostMapping("/join")
    public ResponseEntity<GroupResponse> joinGroup(@RequestBody Map<String, String> request) {
        String inviteCode = request.get("inviteCode");
        GroupResponse response = groupService.joinByCode(inviteCode);
        return ResponseEntity.ok(response);
    }
}
