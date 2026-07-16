import React from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AlertProvider } from '@/template';
import { AuthProvider } from '@/contexts/AuthContext';
import { NotificationProvider } from '@/contexts/NotificationContext';
import Footer from '@/components/Footer';
import { FloatingAlerts } from '@/components/layout/FloatingAlerts';
import { View, StyleSheet } from 'react-native';
import { Colors } from '@/constants/theme';

export default function RootLayout() {
  return (
    <AlertProvider>
      <SafeAreaProvider>
        <AuthProvider>
          <NotificationProvider>
            <View style={styles.container}>
              <FloatingAlerts />
              <StatusBar style="auto" />
              <View style={styles.content}>
                <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: Colors.background } }}>
                  <Stack.Screen name="splash" />
                  <Stack.Screen name="welcome" />
                  <Stack.Screen name="index" />
                  <Stack.Screen name="login" />
                  <Stack.Screen name="pin" />
                  <Stack.Screen name="(parent)" />
                  <Stack.Screen name="(teacher)" />
                  <Stack.Screen name="(admin)" />
                  <Stack.Screen name="(conductor)" />
                  <Stack.Screen name="(bus_driver)" />
                  <Stack.Screen name="(security)" />
                </Stack>
              </View>
              <Footer />
            </View>
          </NotificationProvider>
        </AuthProvider>
      </SafeAreaProvider>
    </AlertProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    flex: 1,
  },
});
