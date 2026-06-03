// lib/screens/map_screen.dart
// ignore: avoid_web_libraries_in_flutter
import 'dart:ui_web' as ui;
// ignore: avoid_web_libraries_in_flutter
import 'dart:html' as html;

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/map_provider.dart';
import '../utils/app_theme.dart';

bool _mapViewRegistered = false;

void _registerMapView() {
  if (_mapViewRegistered) return;
  _mapViewRegistered = true;

  // ignore: undefined_prefixed_name
  ui.platformViewRegistry.registerViewFactory('smarthas-map', (int viewId) {
    final iframe = html.IFrameElement()
      ..src = '/map.html'
      ..style.width = '100%'
      ..style.height = '100%'
      ..style.border = 'none'
      ..allow = 'geolocation';
    return iframe;
  });
}

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  @override
  void initState() {
    super.initState();
    _registerMapView();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<MapProvider>().loadMap();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mapa SmartHAS'),
        actions: [
          IconButton(
            icon: const Icon(Icons.my_location),
            tooltip: 'Minha localização',
            onPressed: () {
              // Reload the iframe to re-center the map
              setState(() {
                _mapViewRegistered = false;
                _registerMapView();
              });
            },
          ),
        ],
      ),
      body: Consumer<MapProvider>(
        builder: (context, mapProvider, _) {
          if (mapProvider.status == MapStatus.loading ||
              mapProvider.status == MapStatus.idle) {
            return const Center(child: CircularProgressIndicator());
          }

          if (mapProvider.status == MapStatus.error) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.map_outlined, size: 48, color: Colors.grey),
                  const SizedBox(height: 12),
                  Text(mapProvider.errorMessage ?? 'Erro ao carregar mapa'),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => mapProvider.loadMap(),
                    child: const Text('Tentar novamente'),
                  ),
                ],
              ),
            );
          }

          return Stack(
            children: [
              const HtmlElementView(viewType: 'smarthas-map'),
              // Legenda flutuante
              Positioned(
                bottom: 16,
                left: 16,
                child: Card(
                  elevation: 4,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'Legenda',
                          style: Theme.of(context)
                              .textTheme
                              .labelLarge
                              ?.copyWith(fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 8),
                        _LegendItem(
                          color: AppTheme.primaryColor,
                          label: 'Sua localização',
                        ),
                        const _LegendItem(
                          color: Colors.red,
                          label: 'Hospital / Posto de saúde',
                        ),
                        const _LegendItem(
                          color: Colors.green,
                          label: 'Sensor SmartHAS (IoT)',
                        ),
                        const _LegendItem(
                          color: Colors.orange,
                          label: 'Dispositivo wearable',
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _LegendItem extends StatelessWidget {
  final Color color;
  final String label;

  const _LegendItem({required this.color, required this.label});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 12,
            height: 12,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 8),
          Text(label, style: const TextStyle(fontSize: 12)),
        ],
      ),
    );
  }
}
