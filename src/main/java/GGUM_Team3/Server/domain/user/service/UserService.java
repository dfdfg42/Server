package GGUM_Team3.Server.domain.user.service;

import GGUM_Team3.Server.domain.user.entity.UserEntity;
import GGUM_Team3.Server.domain.user.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserEntity create(final UserEntity userEntity) {
        if(userEntity == null || userEntity.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }
        final String email = userEntity.getEmail();
        if(userRepository.existsByEmail(email)) {
            log.warn("Email already exists {}",email);
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(userEntity);
    }
    public UserEntity getByCredentials(final String email, final String password, final PasswordEncoder encoder) {
        final UserEntity originalUser = userRepository.findByEmail(email);
        if(originalUser !=null && encoder.matches(password, originalUser.getPassword())){
            return originalUser;
        }
        return null;
    }

    public UserEntity getById(String userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        return user.orElse(null);
    }

    public UserEntity getByEmail(String email) {
        return userRepository.findByEmail(email); // 이메일로 사용자 조회
    }
}
