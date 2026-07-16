import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import * as DocumentPicker from 'expo-document-picker';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { TextInput } from 'react-native';
import { PrimaryButton } from '@/components/ui/PrimaryButton';

const FormInput = ({ label, ...props }: any) => (
  <View style={styles.inputWrap}>
    <Text style={styles.inputLabel}>{label}</Text>
    <TextInput style={styles.textInput} placeholderTextColor={Colors.textMuted} {...props} />
  </View>
);

export default function TeacherRegistrationScreen() {
  const router = useRouter();
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    subject: '',
    designation: '',
  });

  const handleRegister = () => {
    if (!form.name || !form.email) {
      Alert.alert('Error', 'Name and Email are required.');
      return;
    }
    Alert.alert('Success', 'Teacher registered successfully.');
    router.back();
  };

  const handleBulkImport = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/pdf',
        copyToCacheDirectory: true,
      });

      if (!result.canceled) {
        Alert.alert('File Selected', `Importing teachers from ${result.assets[0].name}...`);
        // Here we would normally call a backend endpoint to parse the PDF and create users
        setTimeout(() => {
          Alert.alert('Import Complete', 'Successfully imported teachers from PDF.');
        }, 2000);
      }
    } catch (err) {
      console.log('Error picking document', err);
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Teacher Registration</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.bulkSection}>
          <Text style={styles.sectionTitle}>Bulk Import</Text>
          <Text style={styles.bulkText}>Upload a PDF file containing teacher details to register them in bulk.</Text>
          <PrimaryButton 
            label="Import from PDF" 
            onPress={handleBulkImport} 
            style={styles.importBtn}
          />
        </View>

        <View style={styles.divider} />

        <Text style={styles.sectionTitle}>Individual Registration</Text>
        
        <View style={styles.formGroup}>
          <FormInput 
            label="Full Name" 
            placeholder="Enter teacher's name"
            value={form.name}
            onChangeText={(t: string) => setForm({ ...form, name: t })}
          />
          <FormInput 
            label="Email Address" 
            placeholder="Enter email (e.g. employee_code@kvs.in)"
            keyboardType="email-address"
            autoCapitalize="none"
            value={form.email}
            onChangeText={(t: string) => setForm({ ...form, email: t })}
          />
          <FormInput 
            label="Phone Number" 
            placeholder="Enter contact number"
            keyboardType="phone-pad"
            value={form.phone}
            onChangeText={(t: string) => setForm({ ...form, phone: t })}
          />
          <FormInput 
            label="Designation" 
            placeholder="e.g. PGT, TGT, PRT"
            value={form.designation}
            onChangeText={(t: string) => setForm({ ...form, designation: t })}
          />
          <FormInput 
            label="Subject" 
            placeholder="e.g. Mathematics, Physics"
            value={form.subject}
            onChangeText={(t: string) => setForm({ ...form, subject: t })}
          />
        </View>

        <PrimaryButton 
          label="Register Teacher" 
          onPress={handleRegister} 
          style={styles.submitBtn}
        />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: Colors.border },
  backBtn: { padding: Spacing.xs },
  title: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary },
  content: { padding: Spacing.lg },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary, marginBottom: Spacing.md },
  bulkSection: { backgroundColor: '#F0F9FF', padding: Spacing.lg, borderRadius: Radius.md, marginBottom: Spacing.lg },
  bulkText: { color: Colors.textSecondary, marginBottom: Spacing.md, fontSize: 14 },
  importBtn: { borderColor: Colors.primary },
  divider: { height: 1, backgroundColor: Colors.border, marginVertical: Spacing.lg },
  formGroup: { gap: Spacing.md, marginBottom: Spacing.xl },
  inputWrap: { marginBottom: Spacing.sm },
  inputLabel: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: 6 },
  textInput: { backgroundColor: Colors.surfaceMuted, borderRadius: Radius.md, paddingHorizontal: 14, paddingVertical: 12, fontSize: 15, color: Colors.textPrimary, borderWidth: 1, borderColor: Colors.border },
  submitBtn: { backgroundColor: Colors.primary },
});
