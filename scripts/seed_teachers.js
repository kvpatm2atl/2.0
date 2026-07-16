const { createClient } = require('@supabase/supabase-js');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config({ path: path.resolve(__dirname, '../.env') });

const supabaseUrl = process.env.EXPO_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY || process.env.SUPABASE_ANON_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.error('Missing Supabase URL or Key in env');
  process.exit(1);
}

const supabase = createClient(supabaseUrl, supabaseKey);

const teachersData = [
  {
    employee_code: '21160',
    display_name: 'PADMAJA M G',
    designation: 'PGT',
    subject: 'Physics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2017-09-25',
    is_active: true
  },
  {
    employee_code: '36580',
    display_name: 'T L BINDU',
    designation: 'PGT',
    subject: 'Physics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2010-06-14',
    is_active: true
  },
  {
    employee_code: '14897',
    display_name: 'SANTHA D',
    designation: 'PGT',
    subject: 'Chemistry',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2017-09-28',
    is_active: true
  },
  {
    employee_code: '75662',
    display_name: 'BROMLY THOMAS',
    designation: 'PGT',
    subject: 'Chemistry',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2021-11-08',
    is_active: true
  },
  {
    employee_code: '43099',
    display_name: 'T KUMARI JAYA',
    designation: 'PGT',
    subject: 'Mathematics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-08-07',
    is_active: true
  },
  {
    employee_code: '14927',
    display_name: 'B SIVAKUMAR',
    designation: 'PGT',
    subject: 'Mathematics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2019-08-05',
    is_active: true
  },
  {
    employee_code: '8955',
    display_name: 'AMBILY KRISHNAN',
    designation: 'PGT',
    subject: 'Computer Science',
    class_teacher_of: '11A',
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-08-14',
    is_active: true
  },
  {
    employee_code: '76066',
    display_name: 'SUNITHA KRISHNAN K S',
    designation: 'PGT',
    subject: 'Computer Science',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-08-24',
    is_active: true
  },
  {
    employee_code: '62390',
    display_name: 'PRATHIBHA S PANICKER',
    designation: 'PGT',
    subject: 'Biology',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-11-09',
    is_active: true
  },
  {
    employee_code: '80950',
    display_name: 'HARISREE H G',
    designation: 'PGT',
    subject: 'English',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2024-07-12',
    is_active: true
  },
  {
    employee_code: '100919',
    display_name: 'TRIPURARI KUMAR',
    designation: 'PGT',
    subject: 'Economics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-11-08',
    is_active: true
  },
  {
    employee_code: '9159',
    display_name: 'SINDUMOL AYYAPPAN',
    designation: 'TGT',
    subject: 'Hindi',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2025-11-01',
    is_active: true
  },
  {
    employee_code: '21299',
    display_name: 'JIJIMOL P M',
    designation: 'TGT',
    subject: 'Hindi',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2025-05-06',
    is_active: true
  },
  {
    employee_code: '79879',
    display_name: 'R DEEPTHI',
    designation: 'TGT',
    subject: 'Hindi',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2025-04-01',
    is_active: true
  },
  {
    employee_code: '9098',
    display_name: 'SOBHA S NAIR',
    designation: 'TGT',
    subject: 'Biology',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2019-06-29',
    is_active: true
  },
  {
    employee_code: '46861',
    display_name: 'PADMAREKHA A K',
    designation: 'TGT',
    subject: 'Biology',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2019-06-29',
    is_active: true
  },
  {
    employee_code: '77950',
    display_name: 'ATHIRA S NAIR',
    designation: 'TGT',
    subject: 'Biology',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2024-07-01',
    is_active: true
  },
  {
    employee_code: '32456',
    display_name: 'ASHA RAMACHANDRA N',
    designation: 'TGT',
    subject: 'English',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2025-04-04',
    is_active: true
  },
  {
    employee_code: '9056',
    display_name: 'SUPRIYA V',
    designation: 'TGT',
    subject: 'English',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2019-03-12',
    is_active: true
  },
  {
    employee_code: '79553',
    display_name: 'JINI P',
    designation: 'TGT',
    subject: 'English',
    class_teacher_of: '10C',
    teacher_type: 'Regular',
    date_of_joining_kv: '2021-10-22',
    is_active: true
  },
  {
    employee_code: '81056',
    display_name: 'VIGNESH R',
    designation: 'TGT',
    subject: 'English',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2024-07-03',
    is_active: true
  },
  {
    employee_code: '12038',
    display_name: 'JAYASREE SREEKUMAR',
    designation: 'TGT',
    subject: 'Mathematics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2024-07-02',
    is_active: true
  },
  {
    employee_code: '108719',
    display_name: 'AKASH TANVAR',
    designation: 'TGT',
    subject: 'Mathematics',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-12-14',
    is_active: true
  },
  {
    employee_code: '20214',
    display_name: 'JOLLY JOSEPH',
    designation: 'TGT',
    subject: 'Social Science',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2024-07-15',
    is_active: true
  },
  {
    employee_code: '108720',
    display_name: 'LAXMI M PRAYAGA',
    designation: 'TGT',
    subject: 'Social Science',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-12-20',
    is_active: true
  },
  {
    employee_code: '21413',
    display_name: 'JAYASREE C',
    designation: 'TGT',
    subject: 'Work Education',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2019-08-05',
    is_active: true
  },
  {
    employee_code: '104003',
    display_name: 'NITIN KUMAR',
    designation: 'TGT',
    subject: 'Art Education',
    class_teacher_of: null,
    teacher_type: 'Regular',
    date_of_joining_kv: '2023-12-05',
    is_active: true
  }
];

async function run() {
  console.log('Inserting ' + teachersData.length + ' teachers into staff_directory...');
  const { data, error } = await supabase
    .from('staff_directory')
    .upsert(teachersData, { onConflict: 'employee_code' })
    .select();

  if (error) {
    console.error('Error inserting teachers:', error);
  } else {
    console.log('Successfully inserted/updated ' + data.length + ' teachers!');
    data.forEach(t => console.log(`✓ ${t.display_name} (${t.employee_code}) - CT: ${t.class_teacher_of}`));
  }
}

run();
