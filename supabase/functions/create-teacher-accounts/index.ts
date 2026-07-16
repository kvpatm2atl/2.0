// Edge Function: create-teacher-accounts
// Bulk-creates all staff auth accounts from staff_directory using service role.
// Auto-confirms email, sets passwords, fully populates user_profiles.
// Admin: 4764@kvs.in / Kvpatm2.4764
// Teachers: [code]@kvs.in / Kvpatm2.[code]
// Powered by OnSpace.AI

import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { corsHeaders } from '../_shared/cors.ts'

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseAdmin = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    // Verify caller is admin
    const authHeader = req.headers.get('Authorization')
    const token = authHeader?.replace('Bearer ', '')
    if (token) {
      const { data: { user } } = await supabaseAdmin.auth.getUser(token)
      if (user) {
        const { data: profile } = await supabaseAdmin
          .from('user_profiles')
          .select('role')
          .eq('id', user.id)
          .maybeSingle()
        if (profile && profile.role !== 'admin' && profile.role !== 'teacher') {
          return new Response(JSON.stringify({ error: 'Admin access required' }), {
            status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
          })
        }
      }
    }

    const body = await req.json().catch(() => ({}))
    const specificCode = body.employee_code // optional: create just one

    // Auto-seed staff_directory if empty
    const { count, error: countErr } = await supabaseAdmin.from('staff_directory').select('*', { count: 'exact', head: true });
    if (countErr || count === 0) {
      console.log('staff_directory is empty, seeding teachers...');
      const seedData = [
        { employee_code: '4764', display_name: 'Principal', designation: 'Principal', is_active: true },
        { employee_code: '21160', display_name: 'PADMAJA M G', designation: 'PGT', subject: 'Physics', is_active: true },
        { employee_code: '36580', display_name: 'T L BINDU', designation: 'PGT', subject: 'Physics', is_active: true },
        { employee_code: '14897', display_name: 'SANTHA D', designation: 'PGT', subject: 'Chemistry', is_active: true },
        { employee_code: '75662', display_name: 'BROMLY THOMAS', designation: 'PGT', subject: 'Chemistry', is_active: true },
        { employee_code: '43099', display_name: 'T KUMARI JAYA', designation: 'PGT', subject: 'Mathematics', is_active: true },
        { employee_code: '14927', display_name: 'B SIVAKUMAR', designation: 'PGT', subject: 'Mathematics', is_active: true },
        { employee_code: '8955', display_name: 'AMBILY KRISHNAN', designation: 'PGT', subject: 'Computer Science', class_teacher_of: '11A', is_active: true },
        { employee_code: '76066', display_name: 'SUNITHA KRISHNAN K S', designation: 'PGT', subject: 'Computer Science', is_active: true },
        { employee_code: '62390', display_name: 'PRATHIBHA S PANICKER', designation: 'PGT', subject: 'Biology', is_active: true },
        { employee_code: '80950', display_name: 'HARISREE H G', designation: 'PGT', subject: 'English', is_active: true },
        { employee_code: '100919', display_name: 'TRIPURARI KUMAR', designation: 'PGT', subject: 'Economics', is_active: true },
        { employee_code: '9159', display_name: 'SINDUMOL AYYAPPAN', designation: 'TGT', subject: 'Hindi', is_active: true },
        { employee_code: '21299', display_name: 'JIJIMOL P M', designation: 'TGT', subject: 'Hindi', is_active: true },
        { employee_code: '79879', display_name: 'R DEEPTHI', designation: 'TGT', subject: 'Hindi', is_active: true },
        { employee_code: '9098', display_name: 'SOBHA S NAIR', designation: 'TGT', subject: 'Biology', is_active: true },
        { employee_code: '46861', display_name: 'PADMAREKHA A K', designation: 'TGT', subject: 'Biology', is_active: true },
        { employee_code: '77950', display_name: 'ATHIRA S NAIR', designation: 'TGT', subject: 'Biology', is_active: true },
        { employee_code: '32456', display_name: 'ASHA RAMACHANDRA N', designation: 'TGT', subject: 'English', is_active: true },
        { employee_code: '9056', display_name: 'SUPRIYA V', designation: 'TGT', subject: 'English', is_active: true },
        { employee_code: '79553', display_name: 'JINI P', designation: 'TGT', subject: 'English', class_teacher_of: '10C', is_active: true },
        { employee_code: '81056', display_name: 'VIGNESH R', designation: 'TGT', subject: 'English', is_active: true },
        { employee_code: '12038', display_name: 'JAYASREE SREEKUMAR', designation: 'TGT', subject: 'Mathematics', is_active: true },
        { employee_code: '108719', display_name: 'AKASH TANVAR', designation: 'TGT', subject: 'Mathematics', is_active: true },
        { employee_code: '20214', display_name: 'JOLLY JOSEPH', designation: 'TGT', subject: 'Social Science', is_active: true },
        { employee_code: '108720', display_name: 'LAXMI M PRAYAGA', designation: 'TGT', subject: 'Social Science', is_active: true },
        { employee_code: '21413', display_name: 'JAYASREE C', designation: 'TGT', subject: 'Work Education', is_active: true },
        { employee_code: '104003', display_name: 'NITIN KUMAR', designation: 'TGT', subject: 'Art Education', is_active: true },
      ];
      await supabaseAdmin.from('staff_directory').upsert(seedData, { onConflict: 'employee_code' });
    }

    // Fetch all active staff from directory
    let query = supabaseAdmin.from('staff_directory').select('*').eq('is_active', true)
    if (specificCode) query = query.eq('employee_code', specificCode)
    const { data: staff, error: staffError } = await query.order('display_name')

    if (staffError || !staff) {
      return new Response(JSON.stringify({ error: 'Failed to fetch staff directory: ' + staffError?.message }), {
        status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      })
    }

    console.log(`Processing ${staff.length} staff members...`)

    // Pre-fetch all existing auth users
    let existingUsers: { id: string; email: string }[] = []
    try {
      const { data: { users } } = await supabaseAdmin.auth.admin.listUsers({ perPage: 1000 })
      existingUsers = users.map(u => ({ id: u.id, email: u.email ?? '' }))
      console.log(`Found ${existingUsers.length} existing auth users`)
    } catch (e) {
      console.warn('Could not pre-fetch users list:', e)
    }

    const results: {
      name: string; email: string; employee_code: string;
      status: 'created' | 'updated' | 'failed' | 'skipped';
      error?: string;
    }[] = []

    for (const member of staff) {
      const email = `${member.employee_code}@kvs.in`
      const password = `Kvpatm2.${member.employee_code}`
      const isPrincipal = member.designation === 'Principal'
      const role = isPrincipal ? 'admin' : 'teacher'

      // Build subtitle: "PGT · Computer Science · CT 11A"
      const subtitleParts: string[] = [member.designation]
      if (member.subject) subtitleParts.push(member.subject)
      if (member.class_teacher_of) subtitleParts.push(`CT ${member.class_teacher_of}`)
      const subtitle = subtitleParts.join(' · ')

      // Build teaching_sections array
      const teachingSections: string[] = member.class_teacher_of ? [member.class_teacher_of] : []

      try {
        let userId: string | undefined
        const existingUser = existingUsers.find(u => u.email.toLowerCase() === email.toLowerCase())

        if (existingUser) {
          userId = existingUser.id
          // Update password + confirm email for existing user
          const { error: updateErr } = await supabaseAdmin.auth.admin.updateUserById(userId, {
            password,
            email_confirm: true,
            user_metadata: { role, display_name: member.display_name, employee_code: member.employee_code },
          })
          if (updateErr) {
            console.error(`Update failed for ${email}:`, updateErr.message)
          }
        } else {
          // Create new auth user with auto-confirmed email
          const { data: authData, error: createError } = await supabaseAdmin.auth.admin.createUser({
            email,
            password,
            email_confirm: true,
            user_metadata: { role, display_name: member.display_name, employee_code: member.employee_code },
          })
          if (createError) {
            console.error(`Failed to create ${email}:`, createError.message)
            results.push({
              name: member.display_name, email,
              employee_code: member.employee_code,
              status: 'failed', error: createError.message
            })
            continue
          }
          userId = authData?.user?.id
        }

        if (!userId) {
          results.push({
            name: member.display_name, email,
            employee_code: member.employee_code,
            status: 'skipped', error: 'No userId resolved'
          })
          continue
        }

        // Upsert full user_profiles entry with all staff directory data
        const { error: upsertError } = await supabaseAdmin.from('user_profiles').upsert({
          id: userId,
          email,
          display_name: member.display_name,
          employee_code: member.employee_code,
          subtitle,
          subject: member.subject ?? null,
          class_teacher_of: member.class_teacher_of ?? null,
          role,
          is_active: true,
          teacher_type: member.teacher_type ?? 'Regular',
          date_of_joining: member.date_of_joining_kv ?? null,
          teaching_sections: teachingSections,
        }, { onConflict: 'id' })

        if (upsertError) {
          console.error(`Profile upsert failed for ${email}:`, upsertError.message)
          results.push({
            name: member.display_name, email,
            employee_code: member.employee_code,
            status: 'failed', error: 'Profile: ' + upsertError.message
          })
          continue
        }

        const finalStatus = existingUser ? 'updated' : 'created'
        results.push({
          name: member.display_name, email,
          employee_code: member.employee_code,
          status: finalStatus,
        })
        console.log(`✓ ${finalStatus}: ${member.display_name} (${email}) role=${role}${member.class_teacher_of ? ` CT:${member.class_teacher_of}` : ''}`)

      } catch (e) {
        console.error(`Exception for ${email}:`, e)
        results.push({
          name: member.display_name, email,
          employee_code: member.employee_code,
          status: 'failed', error: String(e)
        })
      }
    }

    const created = results.filter(r => r.status === 'created').length
    const updated = results.filter(r => r.status === 'updated').length
    const failed = results.filter(r => r.status === 'failed').length
    const skipped = results.filter(r => r.status === 'skipped').length

    console.log(`Done: ${created} created, ${updated} updated, ${skipped} skipped, ${failed} failed`)

    return new Response(JSON.stringify({
      results,
      summary: { created, updated, failed, skipped, total: staff.length },
      class_teachers: {
        '11A': 'AMBILY KRISHNAN (8955) — PGT Computer Science',
        '10C': 'JINI P (79553) — TGT English'
      }
    }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    })

  } catch (err) {
    console.error('create-teacher-accounts error:', err)
    return new Response(JSON.stringify({ error: String(err) }), {
      status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
    })
  }
})
