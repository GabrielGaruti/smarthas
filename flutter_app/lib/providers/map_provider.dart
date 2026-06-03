// lib/providers/map_provider.dart
import 'package:flutter/foundation.dart';

enum MapStatus { idle, loading, loaded, error }

class MapProvider extends ChangeNotifier {
  MapStatus _status = MapStatus.idle;
  String? _errorMessage;

  MapStatus get status => _status;
  String? get errorMessage => _errorMessage;

  Future<void> loadMap() async {
    _status = MapStatus.loading;
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 300));
      _status = MapStatus.loaded;
    } catch (e) {
      _status = MapStatus.error;
      _errorMessage = 'Erro ao carregar mapa: $e';
    }
    notifyListeners();
  }
}
