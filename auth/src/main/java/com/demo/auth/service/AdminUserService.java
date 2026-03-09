package com.demo.auth.service;

import com.demo.auth.models.dtos.UserDetailsResponse;
import com.demo.auth.models.entities.User;
import com.demo.auth.repositories.AdminUserRepository;
import com.demo.auth.exception.custom.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final EmailNotificationService emailNotificationService;

    public Page<UserDetailsResponse> getUsers(int page, int size, String sortBy, String sortDir,
            String search, Boolean enabled, Boolean locked) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern)));
            }

            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            if (locked != null) {
                predicates.add(cb.equal(root.get("accountLocked"), locked));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> usersPage = adminUserRepository.findAll(spec, pageable);

        return usersPage.map(this::mapToUserDetailsResponse);
    }

    private UserDetailsResponse mapToUserDetailsResponse(User user) {
        return new UserDetailsResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountLocked(),
                user.getFailedAttempts(),
                user.getRoles(),
                user.getProvider());
    }

    public void lockUser(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new AuthException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        if (user.isAccountLocked()) {
            throw new AuthException(org.springframework.http.HttpStatus.BAD_REQUEST, "User is already locked");
        }

        user.setAccountLocked(true);
        adminUserRepository.save(user);

        log.info("Admin locked account for user: {}", user.getEmail());
        emailNotificationService.sendAccountLockedEmail(user.getEmail());
    }

    public void unlockUser(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new AuthException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isAccountLocked()) {
            throw new AuthException(org.springframework.http.HttpStatus.BAD_REQUEST, "User is not locked");
        }

        user.setAccountLocked(false);
        user.setFailedAttempts(0); // Reset failed attempts when unlocked
        adminUserRepository.save(user);

        log.info("Admin unlocked account for user: {}", user.getEmail());
        emailNotificationService.sendAccountUnlockedEmail(user.getEmail());
    }
}
