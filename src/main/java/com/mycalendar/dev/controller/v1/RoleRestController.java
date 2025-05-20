package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.RoleRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.RoleResponse;
import com.mycalendar.dev.service.implement.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycalendar.dev.constant.PageConstant.*;

@RestController
@RequestMapping("/v1/roles")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping()
    public ResponseEntity<String> create(@Valid @RequestBody RoleRequest roleRequest) {
        roleService.create(roleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Role created successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@Valid @RequestBody RoleRequest roleRequest, @PathVariable Long id) {
        roleService.update(roleRequest, id);
        return ResponseEntity.ok("Role updated successfully.");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok("Role deleted successfully.");
    }

    @GetMapping()
    public PaginationResponse getAll(@RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NO, required = false) int page,
                                     @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE, required = false) int size,
                                     @RequestParam(value = "sortBy", defaultValue = DEFAULT_SORT_BY, required = false) String filter,
                                     @RequestParam(value = "sortDir", defaultValue = DEFAULT_SORT_DIR, required = false) String sort,
                                     @RequestParam(value = "search", required = false) String keyword) {
        return roleService.getAll(page, size, filter, sort, keyword);
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }
}
