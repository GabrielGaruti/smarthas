package com.smarthas.api.dto;

public record LoginResponse(String token, UserResponse user) { }
