import React, { useState } from 'react';
import {
  View, Text, Image, TextInput, Button, StyleSheet, Alert, ActivityIndicator, ScrollView,
} from 'react-native';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';

// Tela de login: usa Image (logo), Text, TextInput e Button (componentes nucleo do React Native).
export default function LoginScreen() {
  const { signIn } = useAuth();
  const [email, setEmail] = useState('paciente@smarthas.com');
  const [password, setPassword] = useState('123456');
  const [loading, setLoading] = useState(false);

  const onLogin = async () => {
    setLoading(true);
    try {
      const res = await api.login(email.trim(), password);
      await signIn(res.token, res.user);
    } catch (e) {
      Alert.alert('Erro', e.message || 'Nao foi possivel entrar.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Image source={require('../assets/logo.png')} style={styles.logo} />
      <Text style={styles.title}>Smart HAS</Text>
      <Text style={styles.subtitle}>Monitoramento de pressao arterial</Text>

      <Text style={styles.label}>E-mail</Text>
      <TextInput
        style={styles.input}
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        keyboardType="email-address"
        placeholder="seu@email.com"
      />

      <Text style={styles.label}>Senha</Text>
      <TextInput
        style={styles.input}
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        placeholder="******"
      />

      <View style={styles.button}>
        {loading ? (
          <ActivityIndicator color="#b91c1c" />
        ) : (
          <Button title="Entrar" color="#b91c1c" onPress={onLogin} />
        )}
      </View>

      <Text style={styles.hint}>
        Demo: paciente@smarthas.com / 123456
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, justifyContent: 'center', padding: 28, backgroundColor: '#f4f6fb' },
  logo: { width: 96, height: 96, alignSelf: 'center', marginBottom: 12 },
  title: { fontSize: 30, fontWeight: 'bold', color: '#b91c1c', textAlign: 'center' },
  subtitle: { fontSize: 14, color: '#6b7280', textAlign: 'center', marginBottom: 24 },
  label: { fontSize: 14, fontWeight: '600', marginBottom: 4, marginTop: 10 },
  input: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#d1d5db', borderRadius: 8, padding: 12, fontSize: 15 },
  button: { marginTop: 20 },
  hint: { textAlign: 'center', color: '#6b7280', fontSize: 12, marginTop: 20 },
});
