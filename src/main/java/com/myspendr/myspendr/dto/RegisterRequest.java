package com.myspendr.myspendr.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nome;
    private String cognome;
    private String email;
    private String username;
    private String password;
}