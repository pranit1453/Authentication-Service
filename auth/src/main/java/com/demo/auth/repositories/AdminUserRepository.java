package com.demo.auth.repositories;

import com.demo.auth.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Dedicated repository for Admin User Management to avoid modifying the core
 * UserRepository.
 * Implements JpaSpecificationExecutor to allow dynamic, complex query building
 * (Pagination, Sorting, Filtering, Searching) directly from the Controller
 * parameters.
 */
@Repository
public interface AdminUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
