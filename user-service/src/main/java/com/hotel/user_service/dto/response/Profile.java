package com.hotel.user_service.dto.response;

import java.sql.Date;
import java.time.LocalDateTime;

public class Profile {
    public String email;
    public String fullName;
    public String phone;
    public Date birth;
    public String address;
    public LocalDateTime created;
}
