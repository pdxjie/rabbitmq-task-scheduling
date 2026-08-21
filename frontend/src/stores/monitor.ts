import { create } from 'zustand';
import type { SystemMetrics } from '@/types';

interface MonitorStore {
  systemMetrics: SystemMetrics | null;
  setSystemMetrics: (metrics: SystemMetrics) => void;
}

export const useMonitorStore = create<MonitorStore>((set) => ({
  systemMetrics: null,
  setSystemMetrics: (metrics) => set({ systemMetrics: metrics }),
}));
