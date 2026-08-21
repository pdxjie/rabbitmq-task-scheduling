import request from '@/utils/request';
import type { ApiResponse } from '@/types';

export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar?: string;
  role: string;
  department: string;
  lastLoginTime?: string;
  createdAt?: string;
}

export interface UpdateProfileRequest {
  nickname?: string;
  email?: string;
  phone?: string;
}

export interface UpdatePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export const getUserProfile = () => {
  return request.get<ApiResponse<UserProfile>>('/user/profile', {
    headers: {
      username: localStorage.getItem('username') || 'admin',
    },
  });
};

export const updateProfile = (data: UpdateProfileRequest) => {
  return request.put<ApiResponse<void>>('/user/profile', data, {
    headers: {
      username: localStorage.getItem('username') || 'admin',
    },
  });
};

export const updatePassword = (data: UpdatePasswordRequest) => {
  return request.put<ApiResponse<void>>('/user/password', data, {
    headers: {
      username: localStorage.getItem('username') || 'admin',
    },
  });
};

export const login = (data: LoginRequest) => {
  return request.post<ApiResponse<UserProfile>>('/user/login', data);
};
