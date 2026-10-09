package com.donations.donations.repository;

public interface PasswordEncoderRepository {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
