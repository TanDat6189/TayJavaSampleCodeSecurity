package vn.tayjava.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.tayjava.dto.request.ResetPasswordDTO;
import vn.tayjava.dto.request.SignInRequest;
import vn.tayjava.dto.respond.TokenResponse;
import vn.tayjava.exception.InvalidDataExcetion;
import vn.tayjava.model.RedisToken;
import vn.tayjava.model.Token;
import vn.tayjava.model.User;
import vn.tayjava.repository.UserRepository;
import vn.tayjava.util.TokenType;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.REFERER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

//    private final TokenService tokenServices;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;

    private  final UserService userService;

    public TokenResponse accessToken(SignInRequest signInRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        var user = userRepository.findByUsername(signInRequest.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Username or Password is incorrect"));

        String accessToken = jwtService.generateToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        //Save token to database
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
//        tokenServices.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public TokenResponse refreshToken(HttpServletRequest request) {

        //Validate
        String refreshToken = request.getHeader("x-token");
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataExcetion("Token must be not blank");
        }

        //Extract user from token
        final String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);

        //Check it into Database
        var user = userService.getByUsername(username);

        if (!jwtService.isValid(refreshToken, TokenType.REFRESH_TOKEN, user)) {
            throw new InvalidDataExcetion("Not allow access with this token");
        }

        // create new accessToken
        String accessToken = jwtService.generateToken(user);

        //save token to db
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
//        tokenServices.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public String removeToken(HttpServletRequest request) {
        //Validate
        String refreshToken = request.getHeader(REFERER);
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataExcetion("Token must be not blank");
        }

        //Extract user from token
        final String username = jwtService.extractUsername(refreshToken, TokenType.ACCESS_TOKEN);

        //Check token in DB
//        Token currentToken = tokenServices.getByUsername(username);

        //Delete token permanent
//        tokenServices.delete(currentToken);
        redisTokenService.delete(username);
        return "Deleted!";
    }

    public String forgotPassword(String email) {

        //Check email exist or not
        User user = userService.getByEmail(email);

        //User is active or inactivated
        if (!user.isEnabled()) {
            throw new InvalidDataExcetion("User not active");
        }

        //Generate reset token
        String resetToken = jwtService.generateResetToken(user);

        //save to db
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());

        //Send email confirmLink
        String confirmLink = String.format("curl --location 'http://localhost:80/auth/reset-password' \\\n" +
                "--header 'accept: */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n"  +
                "--data '%s'", resetToken);

        log.info("ConfirmLin={}", confirmLink);

        return "Sent";
    }

    public String resetPassword(String secretKey) {

        log.info("----- ResetPassword -----");
        final String username = jwtService.extractUsername(secretKey, TokenType.RESET_TOKEN);
        Optional<User> user = userRepository.findByUsername(username);
        if (!jwtService.isValid(secretKey, TokenType.RESET_TOKEN, user.get())) {
            throw new InvalidDataExcetion("Not allow access with this token");
        }

        //check token by username
        redisTokenService.getById(user.get().getUsername());

        return "Reset";
    }

    public String changePassword(ResetPasswordDTO request) {

        User user = isValidUserByToken(request.getSecretKey());

        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataExcetion("Password not match");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.saveUser(user);

        return "Changed";
    }

    private User isValidUserByToken(String secretKey) {
        final String username = jwtService.extractUsername(secretKey, TokenType.RESET_TOKEN);

        User user = userService.getByUsername(username);
        //User is active or inactivated
        if (!user.isEnabled()) {
            throw new InvalidDataExcetion("User not active");
        }

        if (!jwtService.isValid(secretKey, TokenType.RESET_TOKEN, user)) {
            throw new InvalidDataExcetion("Not allow access with this token");
        }

        return user;
    }
}
