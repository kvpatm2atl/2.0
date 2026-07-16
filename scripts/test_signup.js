const { createClient } = require('@supabase/supabase-js');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config({ path: path.resolve(__dirname, '../.env') });

const supabaseUrl = process.env.EXPO_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY || process.env.SUPABASE_ANON_KEY;

const supabase = createClient(supabaseUrl, supabaseKey);

async function run() {
  // Try to sign in as admin first
  const adminEmail = 'admin@kvpattom.edu';
  const adminPass = 'KVPATTOM_64';

  console.log('Signing in as admin:', adminEmail);
  const { data: signInData, error: signInErr } = await supabase.auth.signInWithPassword({
    email: adminEmail,
    password: adminPass
  });

  if (signInErr) {
    console.error('Admin sign in error:', signInErr);
    return;
  }

  console.log('Successfully signed in as admin! Token:', signInData?.session?.access_token ? 'Found' : 'Missing');

  const email = '21160@kvs.in';
  const password = 'Kvpatm2.21160';
  
  console.log('Testing signUp for', email);
  const { data: authData, error: signUpErr } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        role: 'teacher',
        display_name: 'PADMAJA M G',
        employee_code: '21160'
      }
    }
  });

  // Note: signUp will sign in the new user in some configurations, overriding the admin session.
  // To prevent this, we might need to restore the admin session or use a separate supabase client.
  // Let's see if we get an error or if we can upsert with the admin token.
  
  const userId = authData?.user?.id || (signUpErr?.message?.includes('already registered') ? 'already-exists' : null);
  console.log('User ID from signUp:', userId);

  // If signUpErr occurred but user already exists, let's find the user's ID
  let targetUserId = userId;
  if (signUpErr && signUpErr.message.includes('already registered')) {
    // If we're logged in as admin we might be able to find the existing profile or user
    // Wait, let's restore the admin session just in case
    console.log('Restoring admin session to query existing user...');
    await supabase.auth.setSession(signInData.session);
    
    const { data: profileData, error: profileErr } = await supabase
      .from('user_profiles')
      .select('id')
      .eq('email', email)
      .maybeSingle();
      
    if (profileData) {
      targetUserId = profileData.id;
      console.log('Found existing user profile ID:', targetUserId);
    } else {
      console.error('Could not find existing user profile. Error:', profileErr);
    }
  }

  if (targetUserId && targetUserId !== 'already-exists') {
    console.log('Restoring admin session before upsert...');
    await supabase.auth.setSession(signInData.session);

    const subtitle = 'PGT Physics';
    const { error: upsertErr } = await supabase.from('user_profiles').upsert({
      id: targetUserId,
      email,
      display_name: 'PADMAJA M G',
      employee_code: '21160',
      subtitle,
      subject: 'Physics',
      class_teacher_of: null,
      phone: '',
      role: 'teacher',
      is_active: true,
      teacher_type: 'Regular',
      date_of_joining: '2017-09-25'
    });

    if (upsertErr) {
      console.error('Upsert profile error:', upsertErr);
    } else {
      console.log('Successfully upserted profile for PADMAJA M G!');
    }
  }
}

run();
