// PDF Import Service — parses uploaded PDF text into teacher/student records
// Uses basic text extraction for structured KVS teacher lists and student rolls

import { getSupabaseClient } from '@/template';
import { Alert } from 'react-native';

const supabase = getSupabaseClient();

/* ---------- Types ---------- */
export interface ParsedTeacher {
  name: string;
  email: string;
  phone: string;
  designation: string;
  subject: string;
}

export interface ParsedStudent {
  name: string;
  admission_no: string;
  section: string;
  roll_no: number | null;
  father_name: string;
  mother_name: string;
  date_of_birth: string;
  gender: string;
}

/* ---------- Text-Based PDF Parsing Helpers ---------- */

/**
 * Attempts to parse teacher records from raw text extracted from a PDF.
 * Expects a table-like structure with columns for: S.No / Name / Email / Phone / Designation / Subject
 * Each row separated by newlines.
 */
export function parseTeachersFromText(text: string): ParsedTeacher[] {
  const lines = text
    .split('\n')
    .map(l => l.trim())
    .filter(l => l.length > 0);

  const teachers: ParsedTeacher[] = [];

  for (const line of lines) {
    // Skip header rows
    if (/^(s\.?\s*no|sl|#|name|teacher)/i.test(line)) continue;

    // Try splitting by common delimiters: tabs, pipes, multiple spaces
    const parts = line.split(/\t|\|/).map(p => p.trim()).filter(Boolean);
    if (parts.length < 3) {
      // Try splitting by 2+ spaces
      const spaceParts = line.split(/\s{2,}/).map(p => p.trim()).filter(Boolean);
      if (spaceParts.length >= 3) {
        parts.length = 0;
        parts.push(...spaceParts);
      }
    }

    if (parts.length >= 3) {
      // Skip if first part looks like a serial number
      let idx = 0;
      if (/^\d+\.?$/.test(parts[0])) idx = 1;

      const name = parts[idx] ?? '';
      const email = parts.find(p => p.includes('@')) ?? '';
      const phone = parts.find(p => /^\+?\d{10,}$/.test(p.replace(/[\s-]/g, ''))) ?? '';
      const designation = parts.find(p => /^(PGT|TGT|PRT|Principal|Vice|HM|Librarian|Counselor)/i.test(p)) ?? '';
      const subject = parts.find(p =>
        /^(Math|Physics|Chemistry|Biology|English|Hindi|Sanskrit|Social|Computer|Economics|Commerce|Accountancy|History|Geography|Political|Physical|Art|Music)/i.test(p)
      ) ?? '';

      if (name && name.length > 1) {
        teachers.push({
          name: name.replace(/^\d+\.?\s*/, ''),
          email: email.toLowerCase(),
          phone,
          designation,
          subject,
        });
      }
    }
  }

  return teachers;
}

/**
 * Attempts to parse student records from raw text extracted from a PDF.
 * Expects columns: S.No / Name / Admission No / Section / Roll No / Father / Mother / DOB / Gender
 */
export function parseStudentsFromText(text: string): ParsedStudent[] {
  const lines = text
    .split('\n')
    .map(l => l.trim())
    .filter(l => l.length > 0);

  const students: ParsedStudent[] = [];

  for (const line of lines) {
    if (/^(s\.?\s*no|sl|#|name|student|admission)/i.test(line)) continue;

    const parts = line.split(/\t|\|/).map(p => p.trim()).filter(Boolean);
    if (parts.length < 3) {
      const spaceParts = line.split(/\s{2,}/).map(p => p.trim()).filter(Boolean);
      if (spaceParts.length >= 3) {
        parts.length = 0;
        parts.push(...spaceParts);
      }
    }

    if (parts.length >= 3) {
      let idx = 0;
      if (/^\d+\.?$/.test(parts[0])) idx = 1;

      const name = parts[idx] ?? '';
      const admission_no = parts.find(p => /^\d{4,}$/.test(p.replace(/[\/\-]/g, ''))) ?? '';
      const section = parts.find(p => /^\d{1,2}[A-Z]$/i.test(p)) ?? '';
      const roll_no_match = parts.find(p => /^\d{1,3}$/.test(p) && parseInt(p) < 100);
      const roll_no = roll_no_match ? parseInt(roll_no_match) : null;
      const dob = parts.find(p => /\d{2}[\/-]\d{2}[\/-]\d{4}/.test(p)) ?? '';
      const gender = parts.find(p => /^(Male|Female|M|F|Other)$/i.test(p)) ?? '';

      // Try to find father/mother names — look for entries after the student name
      const nameIdx = parts.indexOf(name);
      const remaining = parts.slice(nameIdx + 1);
      const nameEntries = remaining.filter(p =>
        p.length > 1 &&
        !/^\d/.test(p) &&
        !p.includes('@') &&
        !/^(Male|Female|M|F)$/i.test(p) &&
        !/^\d{1,2}[A-Z]$/i.test(p)
      );

      if (name && name.length > 1) {
        students.push({
          name: name.replace(/^\d+\.?\s*/, ''),
          admission_no,
          section: section.toUpperCase(),
          roll_no,
          father_name: nameEntries[0] ?? '',
          mother_name: nameEntries[1] ?? '',
          date_of_birth: dob,
          gender: gender.length === 1 ? (gender.toUpperCase() === 'M' ? 'Male' : 'Female') : gender,
        });
      }
    }
  }

  return students;
}

/* ---------- Database Operations ---------- */

/**
 * Imports parsed teachers into Supabase user_profiles table.
 * Creates auth accounts for each teacher with a default password.
 */
export async function importTeachersToSupabase(
  teachers: ParsedTeacher[],
  onProgress?: (current: number, total: number) => void,
): Promise<{ success: number; failed: number; errors: string[] }> {
  let success = 0;
  let failed = 0;
  const errors: string[] = [];

  for (let i = 0; i < teachers.length; i++) {
    const t = teachers[i];
    onProgress?.(i + 1, teachers.length);

    try {
      // Insert into user_profiles directly (auth signup requires admin key)
      const { error } = await supabase.from('user_profiles').insert({
        name: t.name,
        email: t.email || `${t.name.toLowerCase().replace(/\s+/g, '.')}@kvs.in`,
        phone: t.phone,
        role: 'teacher',
        designation: t.designation,
        subject: t.subject,
      });

      if (error) {
        failed++;
        errors.push(`${t.name}: ${error.message}`);
      } else {
        success++;
      }
    } catch (err: any) {
      failed++;
      errors.push(`${t.name}: ${err.message}`);
    }
  }

  return { success, failed, errors };
}

/**
 * Imports parsed students into Supabase students table.
 */
export async function importStudentsToSupabase(
  students: ParsedStudent[],
  onProgress?: (current: number, total: number) => void,
): Promise<{ success: number; failed: number; errors: string[] }> {
  let success = 0;
  let failed = 0;
  const errors: string[] = [];

  for (let i = 0; i < students.length; i++) {
    const s = students[i];
    onProgress?.(i + 1, students.length);

    try {
      const { error } = await supabase.from('students').insert({
        name: s.name,
        admission_no: s.admission_no || `AUTO-${Date.now()}-${i}`,
        section: s.section,
        roll_no: s.roll_no,
        father_name: s.father_name,
        mother_name: s.mother_name,
        date_of_birth: s.date_of_birth || null,
        gender: s.gender || null,
        attendance_pct: 0,
      });

      if (error) {
        failed++;
        errors.push(`${s.name}: ${error.message}`);
      } else {
        success++;
      }
    } catch (err: any) {
      failed++;
      errors.push(`${s.name}: ${err.message}`);
    }
  }

  return { success, failed, errors };
}
