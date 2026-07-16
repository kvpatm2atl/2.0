import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors, Radius, Shadows, Spacing } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { ScreenHeader } from '@/components/layout/ScreenHeader';
import { Card } from '@/components/ui/Card';
import { getSupabaseClient, useAlert } from '@/template';
import { Image } from 'expo-image';

const supabase = getSupabaseClient();

export default function StudentLookup() {
  const [studentId, setStudentId] = useState('');
  const [loading, setLoading] = useState(false);
  const [student, setStudent] = useState<any>(null);
  const { showAlert } = useAlert();

  const handleSearch = async () => {
    if (!studentId.trim()) {
      showAlert('Error', 'Please enter a Student ID or Admission No.');
      return;
    }
    setLoading(true);
    setStudent(null);
    
    const { data, error } = await supabase
      .from('students')
      .select('*')
      .or(`admission_no.eq."${studentId}",pen_no.eq."${studentId}"`)
      .single();

    setLoading(false);

    if (error || !data) {
      showAlert('Not Found', 'No student found with this ID.');
    } else {
      setStudent(data);
    }
  };

  return (
    <View style={{ flex: 1, backgroundColor: Colors.background }}>
      <SafeAreaView edges={['top']}>
        <ScreenHeader title="Student Lookup" subtitle="Scan or Search by ID" />
      </SafeAreaView>

      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.content}>
          <Card>
            <Text style={styles.label}>Enter Student ID or PEN No.</Text>
            <View style={styles.searchRow}>
              <MaterialCommunityIcons name="badge-account-horizontal-outline" size={24} color={Colors.textMuted} />
              <TextInput
                style={styles.input}
                value={studentId}
                onChangeText={setStudentId}
                placeholder="e.g. 271808221..."
                placeholderTextColor={Colors.textMuted}
                autoCapitalize="none"
              />
            </View>
            <PrimaryButton 
              label={loading ? 'Searching...' : 'Search Student'} 
              onPress={handleSearch} 
              loading={loading}
              style={{ marginTop: Spacing.md }}
            />
          </Card>

          {student && (
            <View style={{ marginTop: Spacing.lg }}>
              <Text style={styles.sectionTitle}>Student Details</Text>
              <Card style={{ marginTop: Spacing.sm }}>
                <View style={styles.profileRow}>
                  {student.profile_photo ? (
                    <Image source={{ uri: student.profile_photo }} style={styles.photo} />
                  ) : (
                    <View style={styles.avatarPlaceholder}>
                      <Text style={styles.avatarText}>
                        {student.name?.split(' ').map((w: string) => w[0]).slice(0,2).join('')}
                      </Text>
                    </View>
                  )}
                  <View style={{ flex: 1, marginLeft: 12 }}>
                    <Text style={styles.studentName}>{student.name}</Text>
                    <Text style={styles.studentSub}>Class {student.section} · Roll No: {student.roll_no || 'N/A'}</Text>
                    <Text style={styles.studentSub}>Adm No: {student.admission_no}</Text>
                  </View>
                </View>

                <View style={styles.detailsGrid}>
                  <DetailCell label="Father" value={student.father_name || 'N/A'} icon="human-male" />
                  <DetailCell label="Mother" value={student.mother_name || 'N/A'} icon="human-female" />
                  <DetailCell label="Phone" value={student.phone || student.emergency_contact || 'N/A'} icon="phone" />
                  <DetailCell label="Blood" value={student.blood_group || 'N/A'} icon="water" />
                </View>

                <View style={{ marginTop: Spacing.md }}>
                  <Text style={styles.label}>Address</Text>
                  <Text style={styles.addressText}>{student.address || 'Address not available'}</Text>
                </View>
              </Card>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

function DetailCell({ label, value, icon }: { label: string; value: string; icon: any }) {
  return (
    <View style={styles.detailCell}>
      <MaterialCommunityIcons name={icon} size={16} color={Colors.primary} />
      <View style={{ marginLeft: 6, flex: 1 }}>
        <Text style={styles.detailLabel}>{label}</Text>
        <Text style={styles.detailValue} numberOfLines={1}>{value}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  content: { padding: Spacing.xl, paddingBottom: 60 },
  label: { fontSize: 13, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.5 },
  searchRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.surfaceMuted, borderRadius: Radius.md, paddingHorizontal: 12, marginTop: 8, borderWidth: 1, borderColor: Colors.border },
  input: { flex: 1, paddingVertical: 12, paddingHorizontal: 8, fontSize: 16, color: Colors.textPrimary },
  sectionTitle: { fontSize: 16, fontWeight: '800', color: Colors.textPrimary },
  profileRow: { flexDirection: 'row', alignItems: 'center', paddingBottom: Spacing.md, borderBottomWidth: 1, borderBottomColor: Colors.border },
  photo: { width: 60, height: 60, borderRadius: 16 },
  avatarPlaceholder: { width: 60, height: 60, borderRadius: 16, backgroundColor: Colors.surfaceMuted, alignItems: 'center', justifyContent: 'center' },
  avatarText: { fontSize: 20, fontWeight: '800', color: Colors.textSecondary },
  studentName: { fontSize: 18, fontWeight: '800', color: Colors.textPrimary },
  studentSub: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  detailsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, marginTop: Spacing.md },
  detailCell: { width: '47%', flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.surfaceTint, padding: 10, borderRadius: Radius.md },
  detailLabel: { fontSize: 10, fontWeight: '700', color: Colors.textMuted, textTransform: 'uppercase' },
  detailValue: { fontSize: 13, fontWeight: '600', color: Colors.textPrimary, marginTop: 1 },
  addressText: { fontSize: 14, color: Colors.textPrimary, marginTop: 4, lineHeight: 20 },
});
