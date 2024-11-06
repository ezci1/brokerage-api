package com.inghubs.brokerage.service;

import java.util.List;
import java.util.Optional;

import com.inghubs.brokerage.dto.model.User;

public interface UserService {

	List<User> getUsers();

	Optional<User> getById(Long id);

	Optional<User> getByUsername(String username);

	User createUser(User dto);

	User updateUser(User dto);
}
