package GGUM_Team3.Server.user.controller;

import GGUM_Team3.Server.sercurity.GoogleTokenVerifier;
import GGUM_Team3.Server.sercurity.TokenProvider;
import GGUM_Team3.Server.user.dto.ResponseDTO;
import GGUM_Team3.Server.user.dto.UserDTO;
import GGUM_Team3.Server.user.entity.UserEntity;
import GGUM_Team3.Server.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/signup")
    public ResponseEntity<?>registerUser(@RequestBody UserDTO userDTO){
        try {
            UserEntity user = UserEntity.builder()
                    .email(userDTO.getEmail())
                    .username(userDTO.getUsername())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .build();

            UserEntity registeredUser = userService.create(user);
            UserDTO responseUserDTO = userDTO.builder()
                    .email(registeredUser.getEmail())
                    .id(registeredUser.getId())
                    .username(registeredUser.getUsername())
                    .build();
            return ResponseEntity.ok().body(responseUserDTO);
        } catch(Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?>authenticate(@RequestBody UserDTO userDTO){
        UserEntity user = userService.getByCredentials(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);

        if(user !=null){
            final String token = tokenProvider.create(user);
            final UserDTO responseUserDTO = UserDTO.builder()
                    .email(user.getEmail())
                    .id(user.getId())
                    .token(token)
                    .build();

            return ResponseEntity.ok().body(responseUserDTO);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed")
                    .build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuthenticate(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        String userId = googleTokenVerifier.verify(token);

        if (userId != null) {

            UserEntity user = userService.getById(userId);
            if (user == null) {
                // 새로운 Google 사용자일 경우 UserEntity 생성 및 저장
                user = UserEntity.builder()
                        .id(userId)
                        .email(googleTokenVerifier.getEmail(token))
                        .username(googleTokenVerifier.getName(token))
                        .build();
                userService.create(user);
            }

            final String jwtToken = tokenProvider.create(user);
            final UserDTO responseUserDTO = UserDTO.builder()
                    .email(user.getEmail())
                    .id(user.getId())
                    .token(jwtToken)
                    .build();

            return ResponseEntity.ok().body(responseUserDTO);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Google token validation failed")
                    .build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }
}
