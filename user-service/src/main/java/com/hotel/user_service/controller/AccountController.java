package com.hotel.user_service.controller;

import com.hotel.user_service.config.JwtService;
import com.hotel.user_service.dto.request.AccountActive;
import com.hotel.user_service.dto.request.LoginRequest;
import com.hotel.user_service.dto.request.RegisterRequest;
import com.hotel.user_service.dto.request.UpdatePassword;
import com.hotel.user_service.dto.request.UpdateProfileRequest;
import com.hotel.user_service.dto.request.UpdateRoleRequest;
import com.hotel.user_service.dto.response.Customer;
import com.hotel.user_service.model.User;
import com.hotel.user_service.repository.UserRepository;
import com.hotel.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping
public class AccountController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody LoginRequest body) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.username, body.password));

            String username = authentication.getName();
            Optional<User> user = userRepository.findByUsername(username);

            if (!user.get().is_active()) {
                return ResponseEntity.ok()
                        .body(Map.of("message", "Account is locked"));
            }
            String token = jwtService.generateToken(username);
            User foundUser = user.orElseThrow(() -> new BadCredentialsException("User not found"));
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", foundUser.getRole().getId());
            response.put("userId", foundUser.getId());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.ok()
                    .body(Map.of("message", "Invalid username or password"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed"));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String,Object>> register(@RequestBody RegisterRequest body) {
        Map<String, Object> response = new HashMap<>();
        try{
            boolean check = userService.registerUser(body);
            if(check){
                response.put("message", "User registered successfully");}
            else{
                response.put("message", "Username already exists");
            }
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/acceptHotel")
    public ResponseEntity<Map<String,Object>> acceptHotel(@RequestBody int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean check = userService.adminAcceptHotel(userId);
            if (check) {
                response.put("message", "Hotel account accepted successfully");
            }
            else {
                response.put("message", "Hotel account acceptance failed");
            }
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader("X-Auth-UserId") String userIdStr) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = Integer.parseInt(userIdStr);
            var profileOptional = userService.profile(userId);
            if (profileOptional.isPresent()) {
                response.put("message", "User found");
                response.put("profile", profileOptional.get());
            } else {
                response.put("message", "User not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String,Object>> updateProfile(@RequestHeader("X-Auth-UserId") String userIdStr,
                                                             @RequestBody UpdateProfileRequest body) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = Integer.parseInt(userIdStr);
            boolean check = userService.updateProfile(userId, body);
            if (check) {
                response.put("message", "Profile updated successfully");
            } else {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String,Object>> updatePassword(@RequestHeader("X-Auth-UserId") String userIdStr,
                                                             @RequestBody UpdatePassword body) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = Integer.parseInt(userIdStr);
            boolean check = userService.updatePassword(userId, body);
            if (check) {
                response.put("message", "Password updated successfully");
            }
            else {
                response.put("message", "Password updated failed");
            }
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fullName")
    public ResponseEntity<Map<String, Object>> Customer(@RequestParam("userId") int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Customer> customerOptional = userService.Customer(userId);
            if (customerOptional.isPresent()) {
                response.put("fullName", customerOptional.get().name);
                response.put("phone", customerOptional.get().phone);
            } else {
                response.put("message", "User not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getAccounts() {
        Map<String, Object> response = new HashMap<>();
        try {
            var accountsList = userService.getAllAccounts();
            if (accountsList.isEmpty()) {
                response.put("message", "No accounts found");
            } else {
                response.put("accounts", accountsList);
                response.put("message", "Accounts retrieved successfully");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    

    @PutMapping("/lockAccount")
    public ResponseEntity<Map<String,Object>> lockAccount(@RequestBody AccountActive request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean check = userService.lockAccount(request);
            if (check) {
                response.put("message", "Update account active successfully");
            } else {
                response.put("message", "Update account active failed");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/hotelId")
    public ResponseEntity<Map<String, Object>> getHotelId(@RequestParam ("ownerId") int userId) {
        Map<String, Object> response = new HashMap<>();
        Integer hotelId = userService.checkHotelId(userId);
        if (hotelId != null) {
            response.put("message", "User has a hotelId");
            response.put("hotelId", hotelId);
        } else {
            response.put("message", "User does not have a hotelId");
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/accountsByHotel")
    public ResponseEntity<Map<String, Object>> getAccountsByHotel(@RequestParam("hotelId") int hotelId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var accountsList = userService.getAccountsByHotel(hotelId);
            if (accountsList.isEmpty()) {
                response.put("message", "No accounts found for this hotel");
            } else {
                response.put("accounts", accountsList);
                response.put("message", "Accounts retrieved successfully");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/addAccountHotel")
    public ResponseEntity<Map<String, Object>> addAccountHotel(@RequestParam("hotelId") int hotelId,
                                                               @RequestBody RegisterRequest body) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean check = userService.addAccountHotel(body, hotelId);
            if (check) {
                response.put("message", "Account added successfully");
            } else {
                response.put("message", "Username already exists");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/updateRole")
    public ResponseEntity<Map<String, Object>> updateRole(@RequestBody UpdateRoleRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean check = userService.updateRole(request);
            if (check) {
                response.put("message", "Update role successfully");
            } else {
                response.put("message", "Update role failed");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}