package com.smarthas.data.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class RegisterRequest(
    val email: String,
    val fullName: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Int,
    val email: String,
    val fullName: String
)

data class MeasurementRequest(
    val systolic: Int,
    val diastolic: Int,
    val date: String,
    val time: String,
    val notes: String? = null
)

data class MeasurementResponse(
    val id: Int,
    val systolic: Int,
    val diastolic: Int,
    val date: String,
    val time: String,
    val notes: String?,
    val createdAt: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
