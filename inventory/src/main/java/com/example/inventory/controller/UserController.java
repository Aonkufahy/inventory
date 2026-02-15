package com.example.inventory.controller;

import com.example.inventory.entity.User;
import com.example.inventory.repository.UserRepository;
import com.example.inventory.service.UserService;
import com.example.inventory.util.ApiResponse;
import com.example.inventory.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    public UserController(UserService userService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/user")
    public ApiResponse<String> register(@RequestBody User user) {
        if(userRepository.existsByUsername(user.getUsername())||userRepository.existsByEmail(user.getEmail())) {
            log.error("Username or Email already exists");
            return new ApiResponse<>("failed","you have already an account",user.getUsername());
        }
        user.setRole(User.Role.USER);
        userService.save(user);
        log.info("user registered successfully");






        return new ApiResponse<>("success","New Account Created Successfully",user.getUsername());
    }
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User user) {

        User loggedInUser = userService.login(
                user.getUsername(),
                user.getPassword()
        );

        log.info(loggedInUser.getEmail() + " logged in successfully");
        String token = JwtUtil.generateToken(loggedInUser);
        return new ApiResponse<>("Success", "You have logged in successfully "+ loggedInUser.getRole().getRedirectPath(), token);
    }
    @GetMapping("/user/home")
    public String userHome() {

        return userService.getAllProducts().toString();
    }

}

