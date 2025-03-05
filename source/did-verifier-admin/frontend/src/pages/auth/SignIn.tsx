import { AuthProvider, AuthResponse, SignInPage } from '@toolpad/core/SignInPage';
import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { requestLogin, requestPasswordReset } from '../../apis/admin-api';
import { useSession } from '../../context/SessionContext';
import PasswordResetDialog from './PasswordResetDialog';

async function hashPassword(password: string): Promise<string> {
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(hashBuffer))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('');
}

export default function SignIn() {
  const { setSession } = useSession();
  const navigate = useNavigate();
  const [requirePasswordReset, setRequirePasswordReset] = useState(false);
  const [loginData, setLoginData] = useState<{ email: string; hashedPassword: string } | null>(null);
  const [rememberMe, setRememberMe] = useState<boolean>(() => {
    return localStorage.getItem('rememberMe') === 'true';
  });

  const handleSignIn = async (
    provider: AuthProvider,
    formData?: FormData,
    callbackUrl?: string
  ): Promise<AuthResponse> => {
    try {
      const email = formData?.get('email') as string;
      const password = formData?.get('password') as string;
      const hashedPassword = await hashPassword(password);

      const { data } = await requestLogin({
        loginId: email,
        loginPassword: hashedPassword,
      });

      if (data.requirePasswordReset) {
        setRequirePasswordReset(true);
        setLoginData({ email, hashedPassword });
        return {};
      }

      const session = { user: { name: email } };
      setSession(session);

      if (rememberMe) {
        localStorage.setItem('session', JSON.stringify(session));
        localStorage.setItem('rememberMe', 'true');
        localStorage.setItem('email', email);
      } else {
        sessionStorage.setItem('session', JSON.stringify(session));
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('email');
      }

      navigate('/verifier-management', { replace: true });
      return {};
    } catch (error) {
      return { error: 'Invalid username or password.' };
    }
  };

  const handlePasswordReset = async (newPassword: string) => {
    if (!loginData) return;

    try {
      const newHashedPassword = await hashPassword(newPassword);
      await requestPasswordReset({
        loginId: loginData.email,
        oldPassword: loginData.hashedPassword,
        newPassword: newHashedPassword,
      });

      const session = { user: { name: loginData.email } };
      setSession(session);
      
      navigate('/ta-management', { replace: true });
    } catch (error) {
      console.error('Failed to reset password:', error);
    } finally {
      setRequirePasswordReset(false);
      setLoginData(null);
    }
  };

  return (
    <>
      <SignInPage
        providers={[{ id: 'credentials', name: 'Credentials' }]}
        signIn={handleSignIn}
        slotProps={{
          emailField: {
            defaultValue: rememberMe ? localStorage.getItem('email') ?? '' : '',
          },
          rememberMe: {
            checked: rememberMe,
            onChange: (_event: React.SyntheticEvent, checked: boolean) => {
              setRememberMe(checked);
            },
          },
        }}
      />
      <PasswordResetDialog
        open={requirePasswordReset}
        onClose={() => setRequirePasswordReset(false)}
        onSubmit={handlePasswordReset}
      />
    </>
  );
}
