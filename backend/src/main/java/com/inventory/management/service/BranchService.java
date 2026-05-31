package com.inventory.management.service;

import com.inventory.management.domain.entity.Branch;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.BranchRepository;
import com.inventory.management.dto.request.CreateBranchRequest;
import com.inventory.management.dto.response.BranchResponse;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<BranchResponse> findAll(String search, Boolean active, Pageable pageable) {
        return PageResponse.of(
                branchRepository.findAllFiltered(search == null ? "" : search, active, pageable).map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> findAllActive() {
        return branchRepository.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public BranchResponse create(CreateBranchRequest req, User currentUser) {
        if (branchRepository.existsByName(req.name())) {
            throw new BusinessException("Sucursal con ese nombre ya existe", HttpStatus.CONFLICT);
        }
        Branch branch = Branch.builder()
                .name(req.name())
                .address(req.address())
                .phone(req.phone())
                .email(req.email())
                .active(true)
                .build();
        branch = branchRepository.save(branch);
        auditService.logAsync(currentUser, "CREATE_BRANCH", "BRANCH",
                String.valueOf(branch.getId()), null, toResponse(branch), null);
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse update(Long id, CreateBranchRequest req, User currentUser) {
        Branch branch = getOrThrow(id);
        BranchResponse before = toResponse(branch);

        if (branchRepository.existsByNameAndIdNot(req.name(), id)) {
            throw new BusinessException("Sucursal con ese nombre ya existe", HttpStatus.CONFLICT);
        }
        branch.setName(req.name());
        branch.setAddress(req.address());
        branch.setPhone(req.phone());
        branch.setEmail(req.email());
        branch = branchRepository.save(branch);

        auditService.logAsync(currentUser, "UPDATE_BRANCH", "BRANCH",
                String.valueOf(id), before, toResponse(branch), null);
        return toResponse(branch);
    }

    @Transactional
    public void toggleActive(Long id, User currentUser) {
        Branch branch = getOrThrow(id);
        branch.setActive(!branch.getActive());
        branchRepository.save(branch);
        auditService.logAsync(currentUser,
                branch.getActive() ? "ACTIVATE_BRANCH" : "DEACTIVATE_BRANCH",
                "BRANCH", String.valueOf(id), null, null, null);
    }

    public Branch getOrThrow(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    private BranchResponse toResponse(Branch b) {
        return new BranchResponse(b.getId(), b.getName(), b.getAddress(),
                b.getPhone(), b.getEmail(), b.getActive(), b.getCreatedAt());
    }
}
