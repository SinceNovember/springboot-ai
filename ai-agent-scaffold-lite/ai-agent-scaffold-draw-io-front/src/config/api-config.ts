export const API_CONFIG = {
  // Use environment variable if available, otherwise default to local backend
  BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8091/api/v1',
};
