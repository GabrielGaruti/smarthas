// lib/repositories/auth_repository.dart
// Espelha AuthRepository.kt do Android — mesma lógica de negócio,
// adaptada para Flutter com SharedPreferences (equivalente ao TokenManager.kt)

import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';
import '../models/user.dart';

class AuthRepository {
  // Mesmas chaves do TokenManager.kt
  static const String _keyToken = 'jwt_token';
  static const String _keyUserEmail = 'user_email';
  static const String _keyUserName = 'user_name';

  final ApiService _apiService;

  AuthRepository({required ApiService apiService}) : _apiService = apiService;

  /// Login — delega para ApiService e persiste token localmente
  Future<bool> login(String email, String password) async {
    try {
      final response = await _apiService.login(email, password);
      await _saveSession(response.token, response.user);
      return true;
    } catch (_) {
      return false;
    }
  }

  /// Register — sem persistência de token (igual ao Android)
  Future<void> register(String fullName, String email, String password) {
    return _apiService.register(fullName, email, password);
  }

  Future<void> _saveSession(String token, User user) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyToken, token);
    await prefs.setString(_keyUserEmail, user.email);
    await prefs.setString(_keyUserName, user.fullName);
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyToken);
  }

  Future<String?> getUserEmail() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyUserEmail);
  }

  Future<String?> getUserName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyUserName);
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null;
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
