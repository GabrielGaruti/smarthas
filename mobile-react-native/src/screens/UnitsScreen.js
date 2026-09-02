import React, { useCallback, useState } from 'react';
import {
  View, Text, FlatList, StyleSheet, ActivityIndicator,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { api } from '../api/client';

// Lista as unidades de saude (hospitais, sensores IoT) vindas da API.
export default function UnitsScreen() {
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setUnits(await api.getUnits());
    } catch (e) {
      setUnits([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#b91c1c" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={units}
        keyExtractor={(item) => String(item.id)}
        ListEmptyComponent={<Text style={styles.empty}>Nenhuma unidade cadastrada.</Text>}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View style={{ flex: 1 }}>
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.type}>{item.type}</Text>
              {item.address ? <Text style={styles.addr}>{item.address}</Text> : null}
            </View>
            <View style={[styles.dot, { backgroundColor: item.active ? '#16a34a' : '#9ca3af' }]} />
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f4f6fb', padding: 16 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  row: {
    backgroundColor: '#fff', borderRadius: 12, padding: 16, marginBottom: 10,
    flexDirection: 'row', alignItems: 'center', elevation: 1,
  },
  name: { fontSize: 16, fontWeight: 'bold', color: '#1f2937' },
  type: { color: '#b91c1c', fontSize: 12, fontWeight: '600', marginTop: 2 },
  addr: { color: '#6b7280', fontSize: 13, marginTop: 2 },
  dot: { width: 14, height: 14, borderRadius: 7, marginLeft: 12 },
  empty: { textAlign: 'center', color: '#6b7280', marginTop: 40 },
});
