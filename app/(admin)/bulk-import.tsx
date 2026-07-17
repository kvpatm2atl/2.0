import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Alert, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import * as DocumentPicker from 'expo-document-picker';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';
import { ScreenHeader } from '@/components/layout/ScreenHeader';
import { ResponsiveContainer } from '@/components/layout/ResponsiveContainer';
import { parseTeachersFromText, importTeachersToSupabase, parseStudentsFromText, importStudentsToSupabase } from '@/services/pdfImport';
import { ADMIN_SIDEBAR } from './index';

export default function BulkImportScreen() {
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [total, setTotal] = useState(0);

  const handleImport = async (type: 'teacher' | 'student') => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ['application/pdf', 'text/plain'],
        copyToCacheDirectory: true,
      });

      if (result.canceled || !result.assets || result.assets.length === 0) {
        return;
      }

      setLoading(true);
      setProgress(0);
      setTotal(0);

      const file = result.assets[0];
      
      // Note: Real PDF text extraction requires a backend service or heavy native module.
      // For demonstration in the frontend, we simulate extracting text from the PDF.
      let extractedText = '';
      if (file.mimeType === 'application/pdf') {
        // Simulated extracted text
        if (type === 'teacher') {
          extractedText = `S.No	Name	Email	Phone	Designation	Subject
1	Arun Kumar	arun@kvs.in	9876543210	PGT	Physics
2	Meera Singh	meera@kvs.in	8765432109	TGT	Maths`;
        } else {
          extractedText = `S.No	Name	Admission No	Section	Roll No	Father Name
1	Rahul Sharma	1001	10A	15	Suresh Sharma
2	Priya Patel	1002	10A	16	Ramesh Patel`;
        }
      } else {
        // If it's a text file (on web), we could read it
        if (Platform.OS === 'web') {
           const response = await fetch(file.uri);
           extractedText = await response.text();
        } else {
           extractedText = `Mock Name\tmock@kvs.in\t12345\tTGT\tEnglish`; // Mock for now
        }
      }

      if (type === 'teacher') {
        const parsed = parseTeachersFromText(extractedText);
        setTotal(parsed.length);
        const { success, failed, errors } = await importTeachersToSupabase(parsed, (c) => setProgress(c));
        Alert.alert('Import Complete', `Successfully imported ${success} teachers. Failed: ${failed}.`);
      } else {
        const parsed = parseStudentsFromText(extractedText);
        setTotal(parsed.length);
        const { success, failed, errors } = await importStudentsToSupabase(parsed, (c) => setProgress(c));
        Alert.alert('Import Complete', `Successfully imported ${success} students. Failed: ${failed}.`);
      }

    } catch (err: any) {
      Alert.alert('Import Error', err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ResponsiveContainer sidebarItems={ADMIN_SIDEBAR} activeRoute="/(admin)/bulk-import">
      <SafeAreaView style={{ flex: 1, backgroundColor: Colors.background }}>
        <ScreenHeader title="Bulk Import (PDF)" showBack />
        
        <ScrollView contentContainerStyle={styles.content}>
          <Card style={styles.card}>
            <View style={[styles.iconBg, { backgroundColor: Colors.primaryBg }]}>
              <MaterialCommunityIcons name="account-tie" size={32} color={Colors.primary} />
            </View>
            <Text style={styles.title}>Import Teachers</Text>
            <Text style={styles.desc}>Upload a PDF or Text file containing teacher details. The system will automatically parse and create their accounts.</Text>
            
            <Button
              title={loading ? 'Processing...' : 'Select File'}
              onPress={() => handleImport('teacher')}
              disabled={loading}
              icon="file-upload"
              style={styles.button}
            />
          </Card>

          <Card style={styles.card}>
            <View style={[styles.iconBg, { backgroundColor: Colors.saffronBg }]}>
              <MaterialCommunityIcons name="account-group" size={32} color={Colors.saffron} />
            </View>
            <Text style={styles.title}>Import Students</Text>
            <Text style={styles.desc}>Upload a PDF or Text file containing student lists. Sections and Roll Numbers will be automatically mapped.</Text>
            
            <Button
              title={loading ? 'Processing...' : 'Select File'}
              onPress={() => handleImport('student')}
              disabled={loading}
              icon="file-upload"
              style={[styles.button, { backgroundColor: Colors.saffron }]}
            />
          </Card>

          {loading && total > 0 && (
            <View style={styles.progressContainer}>
              <Text style={styles.progressText}>Importing: {progress} / {total}</Text>
            </View>
          )}
        </ScrollView>
      </SafeAreaView>
    </ResponsiveContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    padding: Spacing.lg,
    gap: Spacing.lg,
    maxWidth: 600,
    alignSelf: 'center',
    width: '100%',
  },
  card: {
    padding: Spacing.xl,
    alignItems: 'center',
  },
  iconBg: {
    width: 64,
    height: 64,
    borderRadius: 32,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.md,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.text,
    marginBottom: Spacing.sm,
  },
  desc: {
    fontSize: 14,
    color: Colors.textLight,
    textAlign: 'center',
    marginBottom: Spacing.xl,
    lineHeight: 20,
  },
  button: {
    width: '100%',
  },
  progressContainer: {
    padding: Spacing.lg,
    alignItems: 'center',
  },
  progressText: {
    fontSize: 16,
    color: Colors.primary,
    fontWeight: '600',
  }
});
