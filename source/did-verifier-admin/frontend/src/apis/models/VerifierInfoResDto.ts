import { VerifierStatus } from '../constants/VerifierStatus';

export interface VerifierInfoResDto {
  id: number;
  did: string;
  name: string;
  status: VerifierStatus;
  serverUrl: string;
  certificateUrl: string;
  didDocument?: any;
  createdAt: string;
  updatedAt: string;
}
