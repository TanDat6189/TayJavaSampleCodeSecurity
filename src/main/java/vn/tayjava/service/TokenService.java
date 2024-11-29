package vn.tayjava.service;

import org.springframework.stereotype.Service;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.Token;
import vn.tayjava.model.User;
import vn.tayjava.repository.TokenRepository;

import java.util.Optional;

@Service
public record TokenService(TokenRepository tokenRepository) {

    public int save(Token token) {

        Optional<Token> optional = tokenRepository.findByUsername(token.getUsername());

        if(optional.isEmpty()) {
            tokenRepository.save(token);
            return token.getId();
        }
        else {
            Token currentToken = optional.get();
            currentToken.setAccessToken(token.getAccessToken());
            currentToken.setRefreshToken(token.getRefreshToken());
            tokenRepository.save(currentToken);
            return currentToken.getId();
        }
    }

    public String delete(Token token) {

        tokenRepository.delete(token);
        return "Deleted!";
    }

    public Token getByUsername(String username)
    {
        return tokenRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Token not exists"));
    }
}
