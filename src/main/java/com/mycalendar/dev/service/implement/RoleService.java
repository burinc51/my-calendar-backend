package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.RoleRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.RoleResponse;
import com.mycalendar.dev.repository.RoleRepository;
import com.mycalendar.dev.service.IRoleService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void create(RoleRequest roleRequest) {
        Role role = EntityMapper.mapToEntity(roleRequest, Role.class);
        role.setName(roleRequest.getName().name());
        roleRepository.save(role);
    }

    @Override
    public void update(RoleRequest roleRequest, Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));

        role.setName(roleRequest.getName().name());

        roleRepository.save(role);
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));

        role.setActive(false);

        roleRepository.save(role);
    }

    @Override
    public PaginationResponse getAll(int page, int size, String filter, String sort, String keyword) {
        Sort sortDir = sort.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(filter).ascending()
                : Sort.by(filter).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sortDir);

        GenericSpecification<Role> genericSpec = new GenericSpecification<>();
        List<String> fields = List.of("id", "name");

        Specification<Role> spec = genericSpec.getSpecification(keyword, fields);
        Page<Role> pages = roleRepository.findAll(spec, pageable);
        List<RoleResponse> content = pages.stream()
                .map(user -> EntityMapper.mapToEntity(user, RoleResponse.class))
                .collect(Collectors.toList());

        return new PaginationResponse(content, page, size, pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }

    @Override
    public RoleResponse getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role", "id", id.toString()));

        return EntityMapper.mapToEntity(role, RoleResponse.class);
    }
}
