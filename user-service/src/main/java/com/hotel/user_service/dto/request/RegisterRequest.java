package com.hotel.user_service.dto.request;

import java.sql.Date;
import java.time.LocalDateTime;

public class RegisterRequest {
    public String username;
    public String password;
    public String email;
    public String fullName;
    public String phone;
    public Date birth;
    public String address;
    public int role;
}
