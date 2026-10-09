package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Donation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface DonationPort {
    Donation save(Donation donation);
    Optional<Donation> findById(UUID id);
    Optional<Donation> findByReference(String reference);
    List<Donation> findByCreatorId(UUID creatorId);
    java.util.List<Donation> findAll();
}
