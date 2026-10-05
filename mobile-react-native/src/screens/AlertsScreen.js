import React, { useCallback, useState } from 'react';
import {
  View, Text, FlatList, Button, StyleSheet, ActivityIndicator, Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { api } from '../api/client';

// Fase 6: lista os alertas clinicos gerados no banco (PRC_SHAS_REGISTRAR_ALERTA).
export default function AlertsScreen() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setAlerts(await api.getAlerts());
    } catch (e) {
      Alert.alert('Erro', e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const resolve = async (id) => {
    try {
      await api.resolveAlert(id);
      load();
    } catch (e) {
      Alert.alert('Erro', e.message);
    }
  };

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#b91c1c" /></View>;
  }

  return (
    <FlatList
      contentContainerStyle={styles.container}
      data={alerts}
      keyExtractor={(a) => String(a.id)}
      ListEmptyComponent={<Text style={styles.empty}>Nenhum alerta. Continue monitorando!</Text>}
      renderItem={({ item }) => (
        <View style={[styles.card, item.status !== 'ABERTO' && styles.resolved]}>
          <View style={[styles.badge, { backgroundColor: sevColor(item.severity) }]}>
            <Text style={styles.badgeText}>{item.severity}</Text>
          </View>
          <Text style={styles.msg}>{item.message}</Text>
          <Text style={styles.date}>{new Date(item.createdAt).toLocaleString('pt-BR')}</Text>
          {item.status === 'ABERTO' ? (
            <View style={styles.button}>
              <Button title="Marcar como resolvido" color="#6b7280" onPress={() => resolve(item.id)} />
            </View>
          ) : (
            <Text style={styles.ok}>Resolvido</Text>
          )}
        </View>
      )}
    />
  );
}

function sevColor(sev) {
  if (sev === 'CRITICO') return '#7f1d1d';
  if (sev === 'ALTO') return '#dc2626';
  return '#f59e0b';
}

const styles = StyleSheet.create({
  container: { padding: 16, backgroundColor: '#f4f6fb', flexGrow: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  card: { backgroundColor: '#fff', borderRadius: 12, padding: 16, marginBottom: 12, elevation: 2 },
  resolved: { opacity: 0.55 },
  badge: { alignSelf: 'flex-start', paddingHorizontal: 10, paddingVertical: 3, borderRadius: 999 },
  badgeText: { color: '#fff', fontWeight: '700', fontSize: 12 },
  msg: { color: '#1f2937', marginTop: 8, fontSize: 15 },
  date: { color: '#6b7280', marginTop: 6, fontSize: 12 },
  button: { marginTop: 10 },
  ok: { color: '#16a34a', marginTop: 8, fontWeight: '600' },
  empty: { color: '#6b7280', textAlign: 'center', marginTop: 40 },
});
