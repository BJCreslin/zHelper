package ru.zhelper.zhelper.models.dto;

import lombok.Data;

import java.util.Set;

@Data
public class SignUpRequest {
    private String userName;
    private String email;
    private Set<String> roles;
    private String password;
}
