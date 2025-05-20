package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.SignUpRequest;
import com.mycalendar.dev.payload.request.UserUpdateRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.service.implement.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycalendar.dev.constant.PageConstant.*;

@RestController
@RequestMapping("/v1/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<String> create(@Valid @RequestBody SignUpRequest signUpRequest) {
        userService.create(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
    }

    @PostMapping("/add-role/{userId}/{roleId}")
    public ResponseEntity<String> addRole(@Valid @PathVariable Long userId, @Valid @PathVariable Long roleId) {
        userService.addRole(userId, roleId);

        return ResponseEntity.ok("Role added successfully.");
    }

    @PostMapping("/remove-role/{userId}/{roleId}")
    public ResponseEntity<String> removeRole(@Valid @PathVariable Long userId, @Valid @PathVariable Long roleId) {
        userService.removeRole(userId, roleId);

        return ResponseEntity.ok("Role removed successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@Valid @RequestBody UserUpdateRequest userUpdateRequest, @PathVariable Long id) {
        userService.update(userUpdateRequest, id);
        return ResponseEntity.ok("User updated successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @GetMapping()
    public PaginationResponse getAll(@RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO, required = false) int page,
                                     @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE, required = false) int size,
                                     @RequestParam(value = "sortBy", defaultValue = DEFAULT_SORT_BY, required = false) String filter,
                                     @RequestParam(value = "sortDir", defaultValue = DEFAULT_SORT_DIR, required = false) String sort,
                                     @RequestParam(value = "search", required = false) String keyword) {
        return userService.getAll(page, size, filter, sort, keyword);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}