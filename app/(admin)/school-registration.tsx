import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { TextInput } from 'react-native';
import { PrimaryButton } from '@/components/ui/PrimaryButton';

const FormInput = ({ label, ...props }: any) => (
  <View style={styles.inputWrap}>
    <Text style={styles.inputLabel}>{label}</Text>
    <TextInput style={styles.textInput} placeholderTextColor={Colors.textMuted} {...props} />
  </View>
);

export default function SchoolRegistrationScreen() {
  const router = useRouter();
  const [form, setForm] = useState({
    schoolName: '',
    address: '',
    contactEmail: '',
    contactPhone: '',
    principalName: '',
    website: '',
  });

  const handleRegister = () => {
    if (!form.schoolName || !form.contactEmail) {
      Alert.alert('Error', 'School Name and Contact Email are required.');
      return;
    }
    Alert.alert('Success', 'School registered successfully.');
    router.back();
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>School Registration</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.sectionTitle}>School Details</Text>
        
        <View style={styles.formGroup}>
          <FormInput 
            label="School Name" 
            placeholder="Enter school name"
            value={form.schoolName}
            onChangeText={(t: string) => setForm({ ...form, schoolName: t })}
          />
          <FormInput 
            label="Address" 
            placeholder="Enter full address"
            value={form.address}
            onChangeText={(t: string) => setForm({ ...form, address: t })}
          />
          <FormInput 
            label="Contact Email" 
            placeholder="e.g. admin@school.edu"
            keyboardType="email-address"
            autoCapitalize="none"
            value={form.contactEmail}
            onChangeText={(t: string) => setForm({ ...form, contactEmail: t })}
          />
          <FormInput 
            label="Contact Phone" 
            placeholder="Enter contact number"
            keyboardType="phone-pad"
            value={form.contactPhone}
            onChangeText={(t: string) => setForm({ ...form, contactPhone: t })}
          />
          <FormInput 
            label="Principal Name" 
            placeholder="Enter principal's full name"
            value={form.principalName}
            onChangeText={(t: string) => setForm({ ...form, principalName: t })}
          />
          <FormInput 
            label="Website" 
            placeholder="e.g. www.school.edu"
            keyboardType="url"
            autoCapitalize="none"
            value={form.website}
            onChangeText={(t: string) => setForm({ ...form, website: t })}
          />
        </View>

        <PrimaryButton 
          label="Register School" 
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
  sectionTitle: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary, marginBottom: Spacing.lg },
  formGroup: { gap: Spacing.md, marginBottom: Spacing.xl },
  inputWrap: { marginBottom: Spacing.sm },
  inputLabel: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: 6 },
  textInput: { backgroundColor: Colors.surfaceMuted, borderRadius: Radius.md, paddingHorizontal: 14, paddingVertical: 12, fontSize: 15, color: Colors.textPrimary, borderWidth: 1, borderColor: Colors.border },
  submitBtn: { backgroundColor: Colors.primary },
});
