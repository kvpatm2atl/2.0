import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable, Switch, ActivityIndicator, Alert, TextInput } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { Colors, Radius, Spacing, Typography } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';

export default function AbsenceManagementScreen() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [teachers, setTeachers] = useState([
    { id: '1', name: 'AMBILY KRISHNAN', role: 'PGT Computer Science', absent: false },
    { id: '2', name: 'JINI P', role: 'TGT English', absent: false },
    { id: '3', name: 'BROMLY THOMAS', role: 'PGT Chemistry', absent: true },
  ]);
  const [generating, setGenerating] = useState(false);

  const filteredTeachers = teachers.filter(t => 
    t.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    t.role.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const toggleAbsence = (id: string) => {
    setTeachers(prev => prev.map(t => t.id === id ? { ...t, absent: !t.absent } : t));
  };

  const handleGenerate = () => {
    const absentCount = teachers.filter(t => t.absent).length;
    if (absentCount === 0) {
      Alert.alert('No Absences', 'Please mark at least one teacher as absent before generating.');
      return;
    }
    setGenerating(true);
    setTimeout(() => {
      setGenerating(false);
      Alert.alert('Success', 'Substitute timetable generated successfully.');
      router.push('/(admin)/substitute-timetable' as any);
    }, 1500);
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Absence Management</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={styles.searchContainer}>
        <View style={styles.inputWrap}>
          <MaterialCommunityIcons name="magnify" size={20} color={Colors.textSecondary} />
          <TextInput 
            placeholder="Search teachers..." 
            value={searchQuery}
            onChangeText={setSearchQuery}
            style={{ flex: 1, fontSize: 16 }}
          />
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.sectionTitle}>Mark Absences</Text>
        {filteredTeachers.map(teacher => (
          <Card key={teacher.id} style={styles.teacherCard}>
            <View style={styles.teacherInfo}>
              <Text style={styles.teacherName}>{teacher.name}</Text>
              <Text style={styles.teacherRole}>{teacher.role}</Text>
            </View>
            <Switch
              value={teacher.absent}
              onValueChange={() => toggleAbsence(teacher.id)}
              trackColor={{ false: '#d1d5db', true: Colors.danger }}
              thumbColor={'#fff'}
            />
          </Card>
        ))}
      </ScrollView>

      <View style={styles.footer}>
        <PrimaryButton 
          label={generating ? "Generating..." : "Generate Substitute Timetable"} 
          onPress={handleGenerate} 
          loading={generating}
          style={styles.generateBtn}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: Colors.border },
  backBtn: { padding: Spacing.xs },
  title: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary },
  searchContainer: { padding: Spacing.lg, backgroundColor: '#fff' },
  inputWrap: { flexDirection: 'row', alignItems: 'center', gap: 8, backgroundColor: Colors.surfaceMuted, borderRadius: Radius.md, paddingHorizontal: 12, paddingVertical: 10, borderWidth: 1, borderColor: Colors.border },
  content: { padding: Spacing.lg },
  sectionTitle: { fontSize: 16, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.md },
  teacherCard: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: Spacing.sm },
  teacherInfo: { flex: 1 },
  teacherName: { fontSize: 16, fontWeight: '600', color: Colors.textPrimary },
  teacherRole: { fontSize: 13, color: Colors.textSecondary, marginTop: 4 },
  footer: { padding: Spacing.lg, backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: Colors.border },
  generateBtn: { backgroundColor: Colors.primary },
});
