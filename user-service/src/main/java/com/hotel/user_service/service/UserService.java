package com.hotel.user_service.service;

import com.hotel.user_service.dto.request.AccountActive;
import com.hotel.user_service.dto.request.RegisterRequest;
import com.hotel.user_service.dto.request.UpdatePassword;
import com.hotel.user_service.dto.request.UpdateProfileRequest;
import com.hotel.user_service.dto.request.UpdateRoleRequest;
import com.hotel.user_service.dto.response.Accounts;
import com.hotel.user_service.dto.response.Customer;
import com.hotel.user_service.dto.response.Profile;
import com.hotel.user_service.model.Role;
import com.hotel.user_service.model.User;
import com.hotel.user_service.repository.RoleRepository;
import com.hotel.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

            return new org.springframework.security.core.userdetails.User(user.getUsername(),
                    user.getPassword(),
                    Collections.singletonList(authority));
        } else {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
    }

    public boolean registerUser(RegisterRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.username);
        if (userOptional.isPresent()) {
            return false;
        }

        Role roles = roleRepository.findById(request.role);
        if (request.role == 1) {
            User user = User.builder()
                    .full_name(request.fullName)
                    .password(passwordEncoder.encode(request.password))
                    .username(request.username)
                    .role(roles)
                    .email(request.email)
                    .phone(request.phone)
                    .birth(request.birth)
                    .address(request.address)
                    .is_active(true)
                    .created_at(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }
        if (request.role == 2) {
            User user = User.builder()
                    .full_name(request.fullName)
                    .password(passwordEncoder.encode(request.password))
                    .username(request.username)
                    .role(roles)
                    .email(request.email)
                    .phone(request.phone)
                    .birth(request.birth)
                    .address(request.address)
                    .is_active(false)
                    .created_at(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }
        return true;
    }

    public Optional<Profile> profile(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Profile profile = new Profile();
            profile.fullName = user.getFull_name();
            profile.email = user.getEmail();
            profile.phone = user.getPhone();
            profile.birth = user.getBirth();
            profile.address = user.getAddress();
            profile.created = user.getCreated_at();
            return Optional.of(profile);
        }
        return Optional.empty();
    }

    public boolean updateProfile(int userId, UpdateProfileRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (request.fullName != null && !request.fullName.isEmpty()) {
                user.setFull_name(request.fullName);
            }
            if (request.email != null && !request.email.isEmpty()) {
                user.setEmail(request.email);
            }
            if (request.phone != null && !request.phone.isEmpty()) {
                user.setPhone(request.phone);
            }
            if (request.address != null && !request.address.isEmpty()) {
                user.setAddress(request.address);
            }
            if (request.birth != null) {
                user.setBirth(request.birth);
            }
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean updatePassword(int userId, UpdatePassword body) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(body.oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(body.newPassword));
            }
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<Customer> Customer(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Customer customer = new Customer();
            customer.name = user.getFull_name();
            customer.phone = user.getPhone();
            return Optional.of(customer);
        }
        return Optional.empty();
    }

    public boolean adminAcceptHotel(int id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.set_active(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public List<Accounts> getAllAccounts() {
        List<User> users = userRepository.findAll();
        return users.stream().filter(user -> user.getRole().getId() != 3).map(user -> {
            Accounts account = new Accounts();
            account.id = user.getId();
            account.username = user.getUsername();
            account.fullName = user.getFull_name();
            account.email = user.getEmail();
            account.role = user.getRole().getName();
            account.active = user.is_active();
            return account;
        }).toList();
    }

    public boolean lockAccount(AccountActive request) {
        Optional<User> userOptional = userRepository.findById(request.userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.set_active(request.active);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Integer checkHotelId(int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getHotelId();
        }
        return 0;
    }

    public List<Accounts> getAccountsByHotel(int hotelId) {
        List<User> users = userRepository.findByHotelId(hotelId);
        return users.stream().map(user -> {
            Accounts account = new Accounts();
            account.id = user.getId();
            account.username = user.getUsername();
            account.fullName = user.getFull_name();
            account.email = user.getEmail();
            account.role = user.getRole().getName();
            account.active = user.is_active();
            return account;
        }).toList();
    }

    public boolean addAccountHotel(RegisterRequest request, int hotelId) {
        Optional<User> userOptional = userRepository.findByUsername(request.username);
        if (userOptional.isPresent()) {
            return false;
        }

        Role roles = roleRepository.findById(request.role);
        User user = User.builder()
                .full_name(request.fullName)
                .password(passwordEncoder.encode(request.password))
                .username(request.username)
                .role(roles)
                .email(request.email)
                .phone(request.phone)
                .birth(request.birth)
                .address(request.address)
                .is_active(true)
                .created_at(LocalDateTime.now())
                .hotelId(hotelId)
                .build();
        userRepository.save(user);
        return true;
    }

    public boolean updateRole(UpdateRoleRequest request) {
        Optional<User> userOptional = userRepository.findById(request.userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Role newRole = roleRepository.findById(request.roleId);
            if (newRole != null) {
                user.setRole(newRole);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
