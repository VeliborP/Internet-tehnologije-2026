package com.example.webshop.mapper;

import com.example.webshop.dto.ProfileForm;
import com.example.webshop.dto.UserDto;
import com.example.webshop.dto.UserForm;
import com.example.webshop.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    /** The password is never copied back to a form. */
    public UserForm toForm(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());
        return form;
    }

    public ProfileForm toProfileForm(User user) {
        ProfileForm form = new ProfileForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        return form;
    }
}
