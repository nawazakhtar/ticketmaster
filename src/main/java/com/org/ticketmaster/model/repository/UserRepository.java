package com.org.ticketmaster.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.ticketmaster.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}