import { CssBaseline, GlobalStyles } from '@mui/material';
import type { Navigation, Session } from '@toolpad/core/AppProvider';
import { ReactRouterAppProvider } from '@toolpad/core/react-router';
import { DialogsProvider } from '@toolpad/core/useDialogs';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Outlet, useNavigate } from 'react-router';
import { getVerifierInfo } from './apis/verifier-api';
import LoadingOverlay from './components/loading/LoadingOverlay';
import { getNavigationByStatus } from './config/navigationConfig';
import { ServerStatusProvider, useServerStatus } from './context/ServerStatusContext';
import { ExtendedSession, SessionContext } from './context/SessionContext';
import customTheme from './theme';

function AppContent() {
  const navigate = useNavigate();
  
  const { serverStatus, setServerStatus, setVerifierInfo } = useServerStatus();
  const [isLoading, setIsLoading] = useState(true);

  const [session, setSessionState] = useState<ExtendedSession | null>(() => {
    const storedSession = localStorage.getItem('session');
    return storedSession ? JSON.parse(storedSession) : null;
  });

  const [navigation, setNavigation] = useState<Navigation>(getNavigationByStatus(null));

  const setSession = useCallback((newSession: ExtendedSession | null) => {
    setSessionState(newSession);
    if (newSession) {
      localStorage.setItem('session', JSON.stringify(newSession));
    } else {
      localStorage.removeItem('session'); 
    }
  }, []);

  const signIn = useCallback(() => {
    navigate('/sign-in');
  }, [navigate]);

  const signOut = useCallback(() => {
    setSession(null);
    navigate('/sign-in');
  }, [navigate]);

  useEffect(() => {
    const fetchVerifierInfo = () => {
      setIsLoading(false);

      getVerifierInfo()
      .then(({ data }) => {
        setServerStatus(data.status);
        setVerifierInfo(data);
        setNavigation(getNavigationByStatus(data.status));
        setIsLoading(false);
      })
      .catch((err) => {
        navigate('/error', { state: { message: `Failed to connect server: ${err}` } });
        setIsLoading(false);
      });
    };

    fetchVerifierInfo();

    const handlePopState = (event: PopStateEvent) => {
      fetchVerifierInfo();
    };
    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, []);

  useEffect(() => {
    if (serverStatus !== null) {
      setNavigation(getNavigationByStatus(serverStatus));
    }
  }, [serverStatus]);

  const sessionContextValue = useMemo(() => ({ session, setSession }), [session, setSession]);

  if (isLoading) {
    return <LoadingOverlay />;
  }

  return (
    <SessionContext.Provider value={sessionContextValue}>
      <DialogsProvider>
        <ReactRouterAppProvider
          navigation={navigation}
          session={session}
          authentication={{ signIn, signOut }}
          theme={customTheme}
        >
          <CssBaseline />
          <Outlet />
        </ReactRouterAppProvider>
      </DialogsProvider>
    </SessionContext.Provider>
  );
}

export default function App() {
  return (
    <ServerStatusProvider>
      <GlobalStyles styles={{ body: { padding: "10px" } }} />
      <AppContent />
    </ServerStatusProvider>
  );
}
