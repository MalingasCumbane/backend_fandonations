package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CountryJpaRepository extends JpaRepository<CountryJpaEntity, UUID> {
    List<CountryJpaEntity> findAllByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
}
