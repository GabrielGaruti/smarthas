// lib/utils/app_theme.dart
// Tema visual do SmartHAS Flutter — paleta alinhada com o Material Theme
// do projeto Android (Theme.SmartHAS)

import 'package:flutter/material.dart';

class AppTheme {
  static const Color primaryColor = Color(0xFF1976D2);
  static const Color primaryDark = Color(0xFF0D47A1);
  static const Color accentColor = Color(0xFF42A5F5);
  static const Color normalColor = Color(0xFF4CAF50);
  static const Color elevatedColor = Color(0xFFFF9800);
  static const Color hypertensionColor = Color(0xFFF44336);
  static const Color surfaceColor = Color(0xFFF5F5F5);

  static ThemeData get theme => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: primaryColor,
          brightness: Brightness.light,
        ),
        appBarTheme: const AppBarTheme(
          centerTitle: true,
          elevation: 0,
          backgroundColor: primaryColor,
          foregroundColor: Colors.white,
          titleTextStyle: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w600,
            color: Colors.white,
          ),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: primaryColor,
            foregroundColor: Colors.white,
            minimumSize: const Size(double.infinity, 48),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
        cardTheme: CardThemeData(
          elevation: 2,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        ),
      );

  /// Cor baseada na classificação da pressão — mesma lógica do HomeFragment.kt
  static Color classificationColor(String classification) {
    switch (classification) {
      case 'Normal':
        return normalColor;
      case 'Elevada':
        return elevatedColor;
      default:
        return hypertensionColor;
    }
  }
}
