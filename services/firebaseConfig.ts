// Firebase configuration for web hosting
// Auth, Storage, and Database remain on Supabase — Firebase is ONLY for static hosting

import { initializeApp } from "firebase/app";

const firebaseConfig = {
  apiKey: "AIzaSyA5pBptELsn3sRQIcla7gjWMFkTmM35IVM",
  authDomain: "kvsedu-b441b.firebaseapp.com",
  projectId: "kvsedu-b441b",
  storageBucket: "kvsedu-b441b.firebasestorage.app",
  messagingSenderId: "850688221783",
  appId: "1:850688221783:web:836fd5960a83fc7ff9b912"
};

const app = initializeApp(firebaseConfig);

export { app };
export default firebaseConfig;
