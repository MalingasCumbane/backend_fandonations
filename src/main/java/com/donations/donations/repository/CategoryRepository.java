package com.donations.donations.repository;

import com.donations.donations.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    java.util.Optional<Category> findByName(String name);

    List<Category> findAllByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
}
