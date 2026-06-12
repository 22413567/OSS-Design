package com.onestep.onestep1000li.service;

import com.onestep.onestep1000li.entity.User;
import com.onestep.onestep1000li.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. 회원가입 기능
    public User registerUser(String userId, String password, String nickname) {
        // 공백 및 특수문자(<, >, ', ") 포함 여부 검증
        if (isInvalidInput(userId) || isInvalidInput(password) || isInvalidInput(nickname)) {
            throw new IllegalArgumentException("공백 기호 혹은 특수문자(<, >, ', \")는 회원 정보로 사용할 수 없습니다.");
        }

        // 이미 사용 중인 아이디인지 DB에서 확인
        if (userRepository.existsById(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 새로운 유저 정보
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password); 
        user.setNickname(nickname);

        return userRepository.save(user);
    }

    // 문자열에 공백이나 특정 특수문자가 있는지 검사
    private boolean isInvalidInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }
        // (공백, 탭, 줄바꿈) 또는 <, >, ', " 가 하나라도 포함되어 있는지 확인
        return input.matches(".*[\\s<>'\"].*");
    }

    // 로그인 기능 
    public User login(String userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) {
                return user; 
            }
        }
        return null;
    }
}