import { useDialogs } from '@toolpad/core/useDialogs';
import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router';
import FullscreenLoader from '../../components/loading/FullscreenLoader';
import { useServerStatus } from '../../context/ServerStatusContext';

const VerifierRegisterPage = () => {
  const navigate = useNavigate();
  const { setServerStatus, setVerifierInfo, serverStatus } = useServerStatus();
  const [isError, setIsError] = useState<boolean>(false);
  const dialogs = useDialogs();
  const [isLoading, setIsLoading] = useState(false);

  const API_BASE_URL = "/verifier/admin/v1";

  if (serverStatus === 'ACTIVATE') {
    return <Navigate to="/verifier-management" replace />;
  }

  return (
    <>
        <FullscreenLoader open={isLoading} />
        Verifier Register Page
    </>

  );
};

export default VerifierRegisterPage;
