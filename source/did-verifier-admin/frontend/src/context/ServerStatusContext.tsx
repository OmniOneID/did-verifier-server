import React, { ReactNode, createContext, useCallback, useContext, useState } from 'react';
import { VerifierInfoResDto } from '../apis/models/VerifierInfoResDto';

export type ServerStatus = 'ACTIVATE' | 'DEACTIVATE' | 'REQUIRED_ENROLL_ENTITY';

interface ServerStatusContextType {
  serverStatus: ServerStatus | null;
  setServerStatus: (status: ServerStatus | null) => void;
  isLoading: boolean;
  setIsLoading: (loading: boolean, message?: string) => void;
  isLoadingMessage: string;
  verifierInfo: VerifierInfoResDto | null;
  setVerifierInfo: (info: VerifierInfoResDto | null) => void;
}

export const ServerStatusContext = createContext<ServerStatusContextType>({
  serverStatus: null,
  setServerStatus: () => {},
  isLoading: false,
  setIsLoading: () => {},
  isLoadingMessage: '',
  verifierInfo: null,
  setVerifierInfo: () => {},
});

export const ServerStatusProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [serverStatus, setServerStatus] = useState<ServerStatus | null>(null);
  const [isLoading, setIsLoadingState] = useState<boolean>(false);
  const [isLoadingMessage, setIsLoadingMessage] = useState<string>('');
  const [verifierInfo, setVerifierInfo] = useState<VerifierInfoResDto | null>(null);

  const setIsLoading = useCallback((loading: boolean, message?: string) => {
    setIsLoadingState(loading);
    setIsLoadingMessage(message ?? '처리 중입니다...');
  }, []);

  return (
    <ServerStatusContext.Provider 
    value={{ 
        serverStatus, 
        setServerStatus, 
        isLoading, 
        setIsLoading, 
        isLoadingMessage,
        verifierInfo,
        setVerifierInfo,
      }}
    >
      {children}
    </ServerStatusContext.Provider>
  );
};

export const useServerStatus = () => useContext(ServerStatusContext);
