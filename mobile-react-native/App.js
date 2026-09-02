import React from 'react';
import { ActivityIndicator, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import { AuthProvider, useAuth } from './src/context/AuthContext';
import LoginScreen from './src/screens/LoginScreen';
import HomeScreen from './src/screens/HomeScreen';
import HistoryScreen from './src/screens/HistoryScreen';
import AddMeasurementScreen from './src/screens/AddMeasurementScreen';
import UnitsScreen from './src/screens/UnitsScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Abas principais do app (React Navigation - Bottom Tabs)
function AppTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#b91c1c' },
        headerTintColor: '#fff',
        tabBarActiveTintColor: '#b91c1c',
      }}
    >
      <Tab.Screen name="Inicio" component={HomeScreen} />
      <Tab.Screen name="Historico" component={HistoryScreen} />
      <Tab.Screen name="Nova" component={AddMeasurementScreen} options={{ title: 'Nova medicao' }} />
      <Tab.Screen name="Unidades" component={UnitsScreen} />
    </Tab.Navigator>
  );
}

// Decide entre fluxo de login e app autenticado (React Navigation - Stack)
function Root() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color="#b91c1c" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {user ? (
          <Stack.Screen name="SmartHAS" component={AppTabs} options={{ headerShown: false }} />
        ) : (
          <Stack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <StatusBar style="light" />
      <Root />
    </AuthProvider>
  );
}
