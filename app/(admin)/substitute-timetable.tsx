import React from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';

export default function SubstituteTimetableScreen() {
  const router = useRouter();

  // Mock substitute data
  const substitutes = [
    { period: 1, class: '11A', subject: 'Chemistry', originalTeacher: 'BROMLY THOMAS', substituteTeacher: 'SANTHA D' },
    { period: 3, class: '12B', subject: 'Chemistry', originalTeacher: 'BROMLY THOMAS', substituteTeacher: 'T KUMARI JAYA' },
  ];

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Substitute Timetable</Text>
        <Pressable style={styles.backBtn}>
          <MaterialCommunityIcons name="printer" size={24} color={Colors.textPrimary} />
        </Pressable>
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.dateText}>{new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</Text>
        <Text style={styles.sectionTitle}>Generated Substitutes</Text>
        
        {substitutes.length === 0 ? (
          <Text style={styles.emptyText}>No substitutes generated for today.</Text>
        ) : (
          substitutes.map((sub, index) => (
            <Card key={index} style={styles.subCard}>
              <View style={styles.periodBadge}>
                <Text style={styles.periodText}>P{sub.period}</Text>
              </View>
              <View style={styles.subInfo}>
                <Text style={styles.classText}>{sub.class} - {sub.subject}</Text>
                <Text style={styles.teacherSubText}>
                  <Text style={styles.strike}>{sub.originalTeacher}</Text> 
                  {' '} → <Text style={styles.newTeacher}>{sub.substituteTeacher}</Text>
                </Text>
              </View>
            </Card>
          ))
        )}
      </ScrollView>

      <View style={styles.footer}>
        <PrimaryButton 
          label="Download Timetable" 
          onPress={() => {}} 
          style={styles.downloadBtn}
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
  content: { padding: Spacing.lg },
  dateText: { fontSize: 14, color: Colors.textSecondary, marginBottom: Spacing.lg, textAlign: 'center', fontWeight: '600' },
  sectionTitle: { fontSize: 16, fontWeight: '600', color: Colors.textPrimary, marginBottom: Spacing.md },
  emptyText: { fontSize: 15, color: Colors.textMuted, textAlign: 'center', marginTop: Spacing.xl },
  subCard: { flexDirection: 'row', alignItems: 'center', marginBottom: Spacing.sm, padding: Spacing.md },
  periodBadge: { width: 40, height: 40, borderRadius: 20, backgroundColor: Colors.infoBg, alignItems: 'center', justifyContent: 'center', marginRight: Spacing.md },
  periodText: { color: Colors.info, fontWeight: '800', fontSize: 15 },
  subInfo: { flex: 1 },
  classText: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary },
  teacherSubText: { fontSize: 14, marginTop: 4, color: Colors.textSecondary },
  strike: { textDecorationLine: 'line-through', color: Colors.textMuted },
  newTeacher: { color: Colors.success, fontWeight: '700' },
  footer: { padding: Spacing.lg, backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: Colors.border },
  downloadBtn: { backgroundColor: Colors.primary },
});
