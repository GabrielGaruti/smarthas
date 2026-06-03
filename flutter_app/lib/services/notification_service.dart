// lib/services/notification_service.dart
// Notificações nativas do browser via Web Notifications API
// Dispara alerta quando pressão arterial está acima do normal

import 'package:flutter/foundation.dart';
// ignore: avoid_web_libraries_in_flutter
import 'dart:html' as html;

class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  bool _permissionGranted = false;

  Future<void> initialize() async {
    if (!kIsWeb) return;
    if (!html.Notification.supported) {
      debugPrint('SmartHAS: Web Notifications não suportadas neste browser.');
      return;
    }
    try {
      final permission = await html.Notification.requestPermission();
      _permissionGranted = permission == 'granted';
      debugPrint('SmartHAS: permissão de notificação = $permission');
    } catch (e) {
      debugPrint('SmartHAS: erro ao solicitar permissão de notificação: $e');
    }
  }

  Future<void> simulateHighPressureAlert() async {
    if (!kIsWeb) return;
    if (!html.Notification.supported) return;

    if (!_permissionGranted) {
      final permission = await html.Notification.requestPermission();
      _permissionGranted = permission == 'granted';
    }

    if (_permissionGranted) {
      try {
        html.Notification(
          'Smart HAS — Alerta de Pressão',
          body: 'Pressão arterial acima do recomendado. Consulte seu médico.',
          icon: '/icons/Icon-192.png',
        );
      } catch (e) {
        debugPrint('SmartHAS: erro ao criar notificação: $e');
      }
    }
  }

  Future<String?> getToken() async => null;
}
