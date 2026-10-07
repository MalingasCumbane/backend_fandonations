package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.domain.model.User;
import com.donations.donations.domain.repository.UserRepository;
import com.donations.donations.infrastructure.persistence.entity.UserJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.UserMapper;
import com.donations.donations.infrastructure.persistence.repository.SpringDataUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepo springDataUserRepo;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toEntity(user);
        UserJpaEntity savedEntity = springDataUserRepo.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepo.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepo.findByEmail(email).map(userMapper::toDomain);
    }
}
