// lib/main.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'services/api_service.dart';
import 'services/notification_service.dart';
import 'repositories/auth_repository.dart';
import 'repositories/measurement_repository.dart';
import 'providers/auth_provider.dart';
import 'providers/measurement_provider.dart';
import 'providers/map_provider.dart';
import 'utils/app_theme.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/history_screen.dart';
import 'screens/add_measurement_screen.dart';
import 'screens/map_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await NotificationService().initialize();
  } catch (e) {
    debugPrint('SmartHAS: falha ao inicializar notificações: $e');
  }
  runApp(const SmartHasApp());
}

class SmartHasApp extends StatelessWidget {
  const SmartHasApp({super.key});

  @override
  Widget build(BuildContext context) {
    final apiService = ApiService();
    final authRepository = AuthRepository(apiService: apiService);
    final measurementRepository = MeasurementRepository(apiService: apiService);

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => AuthProvider(repository: authRepository),
        ),
        ChangeNotifierProvider(
          create: (_) => MeasurementProvider(repository: measurementRepository),
        ),
        ChangeNotifierProvider(create: (_) => MapProvider()),
      ],
      child: MaterialApp(
        title: 'Smart HAS',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.theme,
        home: const _RootNavigator(),
      ),
    );
  }
}

class _RootNavigator extends StatelessWidget {
  const _RootNavigator();

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, auth, _) {
        if (auth.isLoggedIn) {
          return const _MainShell();
        }
        return _AuthFlow();
      },
    );
  }
}

class _AuthFlow extends StatefulWidget {
  @override
  State<_AuthFlow> createState() => _AuthFlowState();
}

class _AuthFlowState extends State<_AuthFlow> {
  bool _showRegister = false;

  @override
  Widget build(BuildContext context) {
    if (_showRegister) {
      return RegisterScreen(
        onRegisterSuccess: () => setState(() => _showRegister = false),
        onGoToLogin: () => setState(() => _showRegister = false),
      );
    }
    return LoginScreen(
      onLoginSuccess: () {},
      onGoToRegister: () => setState(() => _showRegister = true),
    );
  }
}

class _MainShell extends StatefulWidget {
  const _MainShell();

  @override
  State<_MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<_MainShell> {
  int _currentIndex = 0;

  final List<BottomNavigationBarItem> _navItems = const [
    BottomNavigationBarItem(
      icon: Icon(Icons.home_outlined),
      activeIcon: Icon(Icons.home),
      label: 'Início',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.add_circle_outline),
      activeIcon: Icon(Icons.add_circle),
      label: 'Medir',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.history_outlined),
      activeIcon: Icon(Icons.history),
      label: 'Histórico',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.map_outlined),
      activeIcon: Icon(Icons.map),
      label: 'Mapa',
    ),
  ];

  Widget _buildScreen(int index) {
    switch (index) {
      case 0:
        return HomeScreen(
          onNewMeasurement: () => setState(() => _currentIndex = 1),
        );
      case 1:
        return AddMeasurementScreen(
          onSuccess: () => setState(() => _currentIndex = 0),
        );
      case 2:
        return const HistoryScreen();
      case 3:
        return const MapScreen();
      default:
        return const SizedBox.shrink();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: List.generate(4, _buildScreen),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (i) => setState(() => _currentIndex = i),
        type: BottomNavigationBarType.fixed,
        selectedItemColor: AppTheme.primaryColor,
        unselectedItemColor: Colors.grey,
        items: _navItems,
      ),
    );
  }
}
