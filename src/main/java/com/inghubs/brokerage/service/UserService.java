package com.inghubs.brokerage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.inghubs.brokerage.dto.model.User;
import com.inghubs.brokerage.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  public List<User> getUsers() {
    return repository.findAll();
  }

  public Optional<User> getById(Long id) {
    return repository.findById(id);
  }

  public Optional<User> getByUsername(String username) {
    return repository.findByUsernameAndIsEnabledTrue(username);
  }

  public User createUser(User dto) {
    dto.setPassword(passwordEncoder.encode(dto.getPassword()));
    return repository.save(dto);
  }

  public User updateUser(User dto) {
    var existing = getById(dto.getId());
    if (existing.isEmpty()) {
      throw new EntityNotFoundException();
    }
    return repository.save(existing.get());
  }
}