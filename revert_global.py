import re

# 1. Revert api.ts
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/lib/api.ts', 'r') as f:
    api_content = f.read()

# Just replace the interceptor part back to the original
original_api = """import axios from 'axios';

const API_URL = import.meta.env['VITE_API_URL'] || 'http://localhost:8000/api';

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true, // IMPORTANT: Allows cookies (like HttpOnly JWT) to be sent automatically
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      if (error.response.status === 401) {
        const currentPath = window.location.pathname;
        const isAuthRequest = error.config?.url?.includes('/auth/me') || error.config?.url?.includes('/auth/login');
        if (!isAuthRequest && !currentPath.includes('/entrar') && !currentPath.includes('/criar-conta')) {
          window.location.href = '/entrar';
        }
      }
    } else if (error.request) {
      console.log(error.request);
    } else {
      console.log('Error', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api;
"""
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/lib/api.ts', 'w') as f:
    f.write(original_api)

# 2. Revert __root.tsx
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/__root.tsx', 'r') as f:
    root_content = f.read()

root_content = re.sub(r'import { GlobalLoader } from "@/components/GlobalLoader";\n', '', root_content)
root_content = root_content.replace("<GlobalLoader />\n      ", "")
with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/routes/__root.tsx', 'w') as f:
    f.write(root_content)
