import React, { useEffect, useRef, useState } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import LottieView from 'lottie-react-native';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';
import { MaterialCommunityIcons } from '@expo/vector-icons';

interface GamifiedProgressProps {
  currentLevel: number;
  xp: number;
  targetXp: number;
  title?: string;
}

export default function GamifiedProgress({ currentLevel, xp, targetXp, title = 'Engagement Level' }: GamifiedProgressProps) {
  const progressAnim = useRef(new Animated.Value(0)).current;
  const [hasLeveledUp, setHasLeveledUp] = useState(false);
  
  const percentage = Math.min((xp / targetXp) * 100, 100);

  useEffect(() => {
    Animated.timing(progressAnim, {
      toValue: percentage,
      duration: 1000,
      useNativeDriver: false,
    }).start();

    // Trigger level up animation if XP is exactly equal to target and we haven't shown it recently
    if (percentage === 100 && !hasLeveledUp) {
      setHasLeveledUp(true);
      setTimeout(() => setHasLeveledUp(false), 5000); // Reset after 5 seconds
    }
  }, [percentage]);

  const widthInterpolated = progressAnim.interpolate({
    inputRange: [0, 100],
    outputRange: ['0%', '100%']
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.titleRow}>
          <MaterialCommunityIcons name="star-shooting" size={20} color={Colors.saffron} />
          <Text style={styles.title}>{title}</Text>
        </View>
        <View style={styles.levelBadge}>
          <Text style={styles.levelText}>Lv. {currentLevel}</Text>
        </View>
      </View>

      <View style={styles.progressBarBg}>
        <Animated.View style={[styles.progressBarFill, { width: widthInterpolated }]} />
      </View>
      
      <View style={styles.footer}>
        <Text style={styles.xpText}>{xp} / {targetXp} XP</Text>
        <Text style={styles.hintText}>Keep interacting to level up!</Text>
      </View>

      {hasLeveledUp && (
        <View style={styles.lottieContainer} pointerEvents="none">
          {/* Fallback to simple icon since we might not have a local lottie json file immediately available */}
          <View style={styles.mockLottie}>
             <MaterialCommunityIcons name="party-popper" size={50} color={Colors.saffron} />
             <Text style={styles.levelUpText}>Level Up!</Text>
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: Colors.surface,
    borderRadius: Radius.lg,
    padding: Spacing.lg,
    ...Shadows.card,
    overflow: 'hidden',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    color: Colors.textPrimary,
  },
  levelBadge: {
    backgroundColor: Colors.warningBg,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: Radius.pill,
    borderWidth: 1,
    borderColor: Colors.saffronLight,
  },
  levelText: {
    fontSize: 12,
    fontWeight: '800',
    color: Colors.warning,
  },
  progressBarBg: {
    height: 12,
    backgroundColor: Colors.surfaceMuted,
    borderRadius: Radius.pill,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: Colors.success,
    borderRadius: Radius.pill,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  xpText: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.textSecondary,
  },
  hintText: {
    fontSize: 11,
    color: Colors.textMuted,
  },
  lottieContainer: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255,255,255,0.7)',
    zIndex: 10,
  },
  mockLottie: {
    alignItems: 'center',
    transform: [{ scale: 1.2 }],
  },
  levelUpText: {
    fontSize: 20,
    fontWeight: '900',
    color: Colors.saffron,
    marginTop: 8,
    textShadowColor: 'rgba(0,0,0,0.1)',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
  }
});
