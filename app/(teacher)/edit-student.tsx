import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TextInput, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Colors, Radius, Shadows, Spacing } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { useNotifications } from '@/contexts/NotificationContext';
import { useAlert } from '@/template';

export default function EditStudentDetails() {
  const router = useRouter();
  const { addNotification } = useNotifications();
  const { showAlert } = useAlert();

  const [admissionNo, setAdmissionNo] = useState('');
  const [studentName, setStudentName] = useState('');
  
  // Fields to edit
  const [penNo, setPenNo] = useState('');
  const [aadhar, setAadhar] = useState('');
  const [address, setAddress] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [uid, setUid] = useState('');
  const [dateOfAdmission, setDateOfAdmission] = useState('');

  const handleSearch = () => {
    if (!admissionNo) return;
    // Mock fetching student details
    setStudentName('Sample Student');
    setPenNo('PEN123456');
    setAadhar('1234 5678 9012');
    setAddress('KV Pattom Campus, TVM');
    setEmail('parent@example.com');
    setPhone('9876543210');
    setUid('UID-987');
    setDateOfAdmission('2023-06-01');
  };

  const handleSave = () => {
    // Mock save
    addNotification({
      title: 'Student Details Updated',
      message: `Updated records for ${studentName} (${admissionNo}).`,
      type: 'info',
    });
    showAlert('Success', 'Student details updated successfully!');
    router.back();
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Edit Student Details</Text>
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.searchBox}>
          <Text style={styles.label}>Admission Number</Text>
          <View style={styles.inputWrap}>
            <TextInput
              style={styles.input}
              placeholder="Enter Admission No."
              value={admissionNo}
              onChangeText={setAdmissionNo}
            />
            <PrimaryButton label="Search" onPress={handleSearch} size="sm" />
          </View>
        </View>

        {studentName ? (
          <View style={styles.form}>
            <Text style={styles.studentName}>{studentName}</Text>
            
            <View style={styles.field}>
              <Text style={styles.label}>PEN No.</Text>
              <TextInput style={styles.input} value={penNo} onChangeText={setPenNo} />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>Aadhar Number</Text>
              <TextInput style={styles.input} value={aadhar} onChangeText={setAadhar} keyboardType="numeric" />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>Address</Text>
              <TextInput style={[styles.input, { height: 60 }]} value={address} onChangeText={setAddress} multiline />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>Parent Email</Text>
              <TextInput style={styles.input} value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>Phone Number</Text>
              <TextInput style={styles.input} value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>UID</Text>
              <TextInput style={styles.input} value={uid} onChangeText={setUid} />
            </View>
            <View style={styles.field}>
              <Text style={styles.label}>Date of Admission</Text>
              <TextInput style={styles.input} value={dateOfAdmission} onChangeText={setDateOfAdmission} placeholder="YYYY-MM-DD" />
            </View>

            <PrimaryButton label="Save Changes" onPress={handleSave} style={{ marginTop: Spacing.xl }} />
          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.surface, borderBottomWidth: 1, borderBottomColor: Colors.border },
  backBtn: { padding: Spacing.sm, marginRight: Spacing.sm },
  title: { fontSize: 20, fontWeight: '700', color: Colors.textPrimary },
  content: { padding: Spacing.xl },
  searchBox: { backgroundColor: Colors.surface, padding: Spacing.lg, borderRadius: Radius.lg, ...Shadows.card, marginBottom: Spacing.xl },
  inputWrap: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, marginTop: Spacing.sm },
  label: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: 6 },
  input: { flex: 1, backgroundColor: Colors.surfaceMuted, borderWidth: 1, borderColor: Colors.border, borderRadius: Radius.md, paddingHorizontal: Spacing.md, paddingVertical: 10, fontSize: 15, color: Colors.textPrimary },
  form: { backgroundColor: Colors.surface, padding: Spacing.lg, borderRadius: Radius.lg, ...Shadows.card },
  studentName: { fontSize: 22, fontWeight: '800', color: Colors.primary, marginBottom: Spacing.xl, textAlign: 'center' },
  field: { marginBottom: Spacing.lg },
});
