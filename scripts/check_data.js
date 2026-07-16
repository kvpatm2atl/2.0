const { createClient } = require('@supabase/supabase-js');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config({ path: path.resolve(__dirname, '../.env') });

const supabaseUrl = process.env.EXPO_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY || process.env.SUPABASE_ANON_KEY;

const supabase = createClient(supabaseUrl, supabaseKey);

async function run() {
  const { data: profiles, error: err } = await supabase.from('user_profiles').select('*');
  if (err) {
    console.error('Error fetching user profiles:', err);
    return;
  }
  console.log('Total user profiles in DB:', profiles.length);
  profiles.forEach(p => {
    console.log(`- ${p.display_name || p.email} (role: ${p.role}, active: ${p.is_active})`);
  });

  const { data: staff, error: err2 } = await supabase.from('staff_directory').select('*');
  if (err2) {
    console.error('Error fetching staff directory:', err2);
    return;
  }
  console.log('Total staff members in staff_directory:', staff.length);
  staff.forEach(s => {
    console.log(`- ${s.display_name} (code: ${s.employee_code}, subject: ${s.subject}, class_teacher: ${s.class_teacher_of})`);
  });
}

run();
