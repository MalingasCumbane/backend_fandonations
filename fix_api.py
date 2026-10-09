with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/lib/api.ts', 'r') as f:
    content = f.read()

loading_logic = """
let activeRequests = 0;
type Listener = (isLoading: boolean) => void;
const listeners = new Set<Listener>();

function updateLoadingState(delta: number) {
  activeRequests += delta;
  if (activeRequests < 0) activeRequests = 0;
  const isLoading = activeRequests > 0;
  listeners.forEach(l => l(isLoading));
}

export function subscribeToLoading(listener: Listener) {
  listener(activeRequests > 0);
  listeners.add(listener);
  return () => listeners.delete(listener);
}

api.interceptors.request.use((config) => {
  updateLoadingState(1);
  return config;
}, (error) => {
  updateLoadingState(-1);
  return Promise.reject(error);
});
"""

if "updateLoadingState" not in content:
    content = content.replace("api.interceptors.response.use(", loading_logic + "\napi.interceptors.response.use(")
    
    # replace response success
    content = content.replace("(response) => response,", "(response) => {\n    updateLoadingState(-1);\n    return response;\n  },")
    
    # replace response error
    content = content.replace("console.error('API Error:', error);", "updateLoadingState(-1);\n    console.error('API Error:', error);")

    with open('/Volumes/MC/Malingas/Projs/Donations/fans-donations/src/lib/api.ts', 'w') as f:
        f.write(content)
