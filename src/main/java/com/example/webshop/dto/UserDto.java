package com.example.webshop.dto;

import com.example.webshop.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Role role;
    private boolean enabled;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
