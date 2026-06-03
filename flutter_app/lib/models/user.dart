// lib/models/user.dart
// Espelha UserResponse e LoginResponse do backend (main.py)
// e ApiModels.kt do Android

class User {
  final int id;
  final String email;
  final String fullName;

  const User({
    required this.id,
    required this.email,
    required this.fullName,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      email: json['email'] as String,
      fullName: json['fullName'] as String,
    );
  }
}

class LoginResponse {
  final String token;
  final User user;

  const LoginResponse({required this.token, required this.user});

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] as String,
      user: User.fromJson(json['user'] as Map<String, dynamic>),
    );
  }
}

class WeatherData {
  final String city;
  final double temperature;
  final String description;
  final String icon;
  final int humidity;

  const WeatherData({
    required this.city,
    required this.temperature,
    required this.description,
    required this.icon,
    required this.humidity,
  });

  factory WeatherData.fromJson(Map<String, dynamic> json) {
    return WeatherData(
      city: json['name'] as String,
      temperature: (json['main']['temp'] as num).toDouble(),
      description: (json['weather'][0]['description'] as String),
      icon: json['weather'][0]['icon'] as String,
      humidity: json['main']['humidity'] as int,
    );
  }
}
