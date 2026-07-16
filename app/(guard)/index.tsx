import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors, Radius, Shadows, Spacing } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { ScreenHeader } from '@/components/layout/ScreenHeader';
import { Card } from '@/components/ui/Card';
import { getSupabaseClient, useAlert } from '@/template';
import { Image } from 'expo-image';
import { useAuth } from '@/hooks/useAuth';

const supabase = getSupabaseClient();

export default function GuardIndex() {
  const { user, signOut } = useAuth();
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
      <SafeAreaView edges={['top']} style={{ backgroundColor: Colors.primaryDark }}>
        <View style={styles.header}>
          <View>
            <Text style={styles.headerTitle}>Security Guard</Text>
            <Text style={styles.headerSub}>{user?.name} · Gate 1</Text>
          </View>
          <Pressable onPress={signOut} style={styles.logoutBtn}>
            <MaterialCommunityIcons name="logout" size={20} color="#fff" />
          </Pressable>
        </View>
      </SafeAreaView>

      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.content}>
          <Card>
            <Text style={styles.label}>Verify Student Identity</Text>
            <View style={styles.searchRow}>
              <MaterialCommunityIcons name="badge-account-horizontal-outline" size={24} color={Colors.textMuted} />
              <TextInput
                style={styles.input}
                value={studentId}
                onChangeText={setStudentId}
                placeholder="Enter Student ID or Adm No."
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
              <Text style={styles.sectionTitle}>Verification Details</Text>
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
                  <DetailCell label="Phone" value={student.phone || student.emergency_contact || 'N/A'} icon="phone" />
                  <DetailCell label="Transport" value={student.bus_route || 'Private/Walking'} icon="bus" />
                </View>
              </Card>
            </View>
          )}

          <View style={styles.toolsRow}>
            <Pressable style={styles.toolBtn} onPress={() => showAlert('Visitor Entry', 'This will open the Visitor Registration form.', [{text:'OK'}])}>
              <View style={[styles.toolIcon, { backgroundColor: Colors.infoBg }]}>
                <MaterialCommunityIcons name="clipboard-account" size={24} color={Colors.info} />
              </View>
              <Text style={styles.toolText}>Visitor Entry</Text>
            </Pressable>
            
            <Pressable style={styles.toolBtn} onPress={() => showAlert('Emergency', 'SOS Alert sent to Admin.', [{text:'OK'}])}>
              <View style={[styles.toolIcon, { backgroundColor: Colors.dangerBg }]}>
                <MaterialCommunityIcons name="alert-octagon" size={24} color={Colors.danger} />
              </View>
              <Text style={styles.toolText}>Emergency</Text>
            </Pressable>
          </View>

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
  header: { padding: Spacing.lg, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  headerTitle: { fontSize: 22, fontWeight: '800', color: '#fff' },
  headerSub: { fontSize: 13, color: 'rgba(255,255,255,0.8)', marginTop: 2 },
  logoutBtn: { padding: 8, backgroundColor: 'rgba(255,255,255,0.2)', borderRadius: Radius.md },
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
  toolsRow: { flexDirection: 'row', gap: 12, marginTop: Spacing.xxl },
  toolBtn: { flex: 1, backgroundColor: '#fff', borderRadius: Radius.lg, padding: Spacing.lg, alignItems: 'center', justifyContent: 'center', gap: 8, ...Shadows.card },
  toolIcon: { width: 48, height: 48, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  toolText: { fontSize: 14, fontWeight: '700', color: Colors.textPrimary },
});
