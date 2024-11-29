package vn.tayjava.service;

import org.springframework.security.core.userdetails.UserDetails;
import vn.tayjava.util.TokenType;

public interface JWTService {
    public String generateToken(UserDetails user);

    public String generateRefreshToken(UserDetails user);

    public String generateResetToken(UserDetails user);

    String extractUsername(String token, TokenType type);

    boolean isValid(String token, TokenType type, UserDetails user);
}
