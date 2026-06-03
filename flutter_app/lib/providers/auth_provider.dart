// lib/providers/auth_provider.dart
// Gerenciamento de estado de autenticação com Provider
// Espelha a lógica do AuthViewModel.kt (loginState, registerState, currentUser)

import 'package:flutter/foundation.dart';
import '../repositories/auth_repository.dart';

enum AuthStatus { idle, loading, success, error }

class AuthProvider extends ChangeNotifier {
  final AuthRepository _repository;

  AuthStatus _loginStatus = AuthStatus.idle;
  AuthStatus _registerStatus = AuthStatus.idle;
  String? _errorMessage;
  String? _userName;
  String? _userEmail;
  bool _isLoggedIn = false;

  AuthProvider({required AuthRepository repository})
      : _repository = repository {
    _checkSession();
  }

  AuthStatus get loginStatus => _loginStatus;
  AuthStatus get registerStatus => _registerStatus;
  String? get errorMessage => _errorMessage;
  String? get userName => _userName;
  String? get userEmail => _userEmail;
  bool get isLoggedIn => _isLoggedIn;

  Future<void> _checkSession() async {
    _isLoggedIn = await _repository.isLoggedIn();
    if (_isLoggedIn) {
      _userName = await _repository.getUserName();
      _userEmail = await _repository.getUserEmail();
    }
    notifyListeners();
  }

  /// Login — mesmo fluxo do AuthViewModel.kt login()
  Future<void> login(String email, String password) async {
    _loginStatus = AuthStatus.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      final success = await _repository.login(email, password);
      if (success) {
        _userName = await _repository.getUserName();
        _userEmail = await _repository.getUserEmail();
        _isLoggedIn = true;
        _loginStatus = AuthStatus.success;
      } else {
        _loginStatus = AuthStatus.error;
        _errorMessage = 'Email ou senha inválidos';
      }
    } catch (e) {
      _loginStatus = AuthStatus.error;
      _errorMessage = e.toString().replaceFirst('Exception: ', '');
    }
    notifyListeners();
  }

  /// Register — mesmo fluxo do AuthViewModel.kt register()
  Future<void> register(String fullName, String email, String password) async {
    _registerStatus = AuthStatus.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      await _repository.register(fullName, email, password);
      _registerStatus = AuthStatus.success;
    } catch (e) {
      _registerStatus = AuthStatus.error;
      _errorMessage = e.toString().replaceFirst('Exception: ', '');
    }
    notifyListeners();
  }

  Future<String?> getToken() => _repository.getToken();

  void logout() async {
    await _repository.logout();
    _isLoggedIn = false;
    _userName = null;
    _userEmail = null;
    _loginStatus = AuthStatus.idle;
    _registerStatus = AuthStatus.idle;
    notifyListeners();
  }

  void resetLoginStatus() {
    _loginStatus = AuthStatus.idle;
    _errorMessage = null;
    notifyListeners();
  }

  void resetRegisterStatus() {
    _registerStatus = AuthStatus.idle;
    _errorMessage = null;
    notifyListeners();
  }
}
