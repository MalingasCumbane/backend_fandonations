package com.donations.donations.repository;

import com.donations.donations.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    java.util.Optional<Country> findByName(String name);

    List<Country> findAllByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
}
