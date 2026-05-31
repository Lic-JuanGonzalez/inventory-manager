package com.inventory.management.service;

import com.inventory.management.domain.entity.Role;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.CreateUserRequest;
import com.inventory.management.dto.request.UpdateUserRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.UserResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(String search, Boolean active, Pageable pageable) {
        return PageResponse.of(
                userRepository.findAllFiltered(search, active, pageable).map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest req, User currentUser) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email ya registrado: " + req.email(), HttpStatus.CONFLICT);
        }
        Role role = em.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", req.role())
                .getSingleResult();

        User user = User.builder()
                .name(req.name())
                .lastName(req.lastName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(role)
                .active(true)
                .build();
        user = userRepository.save(user);
        auditService.logAsync(currentUser, "CREATE_USER", "USER",
                String.valueOf(user.getId()), null, toResponse(user), null);
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req, User currentUser) {
        User user = getOrThrow(id);
        UserResponse before = toResponse(user);

        if (!user.getEmail().equals(req.email()) && userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email ya registrado: " + req.email(), HttpStatus.CONFLICT);
        }

        Role role = em.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", req.role())
                .getSingleResult();

        user.setName(req.name());
        user.setLastName(req.lastName());
        user.setEmail(req.email());
        user.setRole(role);
        user.setActive(req.active());
        user = userRepository.save(user);

        auditService.logAsync(currentUser, "UPDATE_USER", "USER",
                String.valueOf(id), before, toResponse(user), null);
        return toResponse(user);
    }

    @Transactional
    public void deactivate(Long id, User currentUser) {
        User user = getOrThrow(id);
        if (!user.getActive()) {
            throw new BusinessException("Usuario ya está inactivo");
        }
        user.setActive(false);
        userRepository.save(user);
        auditService.logAsync(currentUser, "DEACTIVATE_USER", "USER",
                String.valueOf(id), null, null, null);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getName(), u.getLastName(), u.getFullName(),
                u.getEmail(), u.getRole().getName(), u.getActive(), u.getCreatedAt()
        );
    }
}
