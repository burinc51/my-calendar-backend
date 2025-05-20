package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.DataExistsException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.SignUpRequest;
import com.mycalendar.dev.payload.request.UserUpdateRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.repository.RoleRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IUserService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void create(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new DataExistsException("username", signUpRequest.getUsername());
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new DataExistsException("email", signUpRequest.getEmail());
        }

        User user = EntityMapper.mapToEntity(signUpRequest, User.class);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setActivateCode(UUID.randomUUID().toString());
        user.setActive(false);

        Set<Role> roles = new HashSet<>();

        for (String roleName : signUpRequest.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role", "name", roleName.toLowerCase()));
            roles.add(role);
        }

        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public void update(UserUpdateRequest userUpdateRequest, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));

        user.setName(userUpdateRequest.getName());

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public PaginationResponse getAll(int page, int size, String filter, String sort, String keyword) {
        Sort sortDir = sort.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(filter).ascending() : Sort.by(filter).descending();
        Pageable pageable = PageRequest.of(page, size, sortDir);

        GenericSpecification<User> genericSpec = new GenericSpecification<>();
        List<String> fields = List.of("id", "name", "username", "email");

        Specification<User> spec = genericSpec.getSpecification(keyword, fields);
        Page<User> pages = userRepository.findAll(spec, pageable);
        List<UserResponse> content = pages.stream()
                .map(user -> EntityMapper.mapToEntity(user, UserResponse.class))
                .collect(Collectors.toList());

        return new PaginationResponse(content, page, size, pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }


    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
        return EntityMapper.mapToEntity(user, UserResponse.class);
    }

    @Override
    public void addRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role", "id", roleId.toString()));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role", "id", roleId.toString()));
        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
