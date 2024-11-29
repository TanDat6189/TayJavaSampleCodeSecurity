package vn.tayjava.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.RedisToken;
import vn.tayjava.repository.RedisTokenRepository;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTokenRepository redisTokenRepository;

    public String save(RedisToken token) {
        RedisToken result = redisTokenRepository.save(token);
        return result.getId();
    }

    public void delete(String id) {
        redisTokenRepository.deleteById(id);
    }

    public RedisToken getById(String id) {
        return redisTokenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
    }
}
