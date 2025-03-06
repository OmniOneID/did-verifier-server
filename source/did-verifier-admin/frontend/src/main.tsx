import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router';
import App from './App';
import Layout from './layout/Layout';
import AdminManagementPage from './pages/admins/AdminManagementPage';
import SignInPage from './pages/auth/SignIn';
import ErrorPage from './pages/ErrorPage';
import ServerManagementPage from './pages/servers/ServerManagementPage';
import VerifierManagementPage from './pages/verifier/VerifierManagementPage';
import VerifierRegisterPage from './pages/verifier/VerifierRegistrationPage';
import FilterManagementPage from './pages/vp-policy-management/filter-management/FilterManagementPage';
import PolicyManagementPage from './pages/vp-policy-management/policy-management/PolicyManagementPage';
import ProcessManagementPage from './pages/vp-policy-management/process-management/ProcessManagementPage';
import ProfileManagementPage from './pages/vp-policy-management/profile-management/ProfileManagementPage';
import ServiceManagementPage from './pages/vp-policy-management/service-management/ServiceManagementPage';
import VpPolicyManagementPage from './pages/vp-policy-management/VpPolicyManagementPage';
import VpSubmissionManagementPage from './pages/vp-submission-management/VpSubmissionManagementPage';
import ServiceRegistrationPage from './pages/vp-policy-management/service-management/ServiceRegistrationPage';
import ServiceDetailPage from './pages/vp-policy-management/service-management/ServiceDetailPage';
import ServiceEditPage from './pages/vp-policy-management/service-management/ServiceEditPage';

const router = createBrowserRouter([
  {
    Component: App,
    children: [
      {
        path: '/',
        Component: Layout,
        children: [
          {
            path: '/verifier-registration',
            Component: VerifierRegisterPage,
          },
          {
            path: '/verifier-management',
            Component: VerifierManagementPage,
          },
          {
            path: '/vp-policy-management/service-management/service-registration',
            Component: ServiceRegistrationPage,
          },
          {
            path: '/vp-policy-management/service-management/service-edit/:id',
            Component: ServiceEditPage,
          },
          {
            path: '/vp-policy-management/service-management/:id',
            Component: ServiceDetailPage,
          },
          {
            path: '/vp-policy-management/service-management',
            Component: ServiceManagementPage,
          },
          {
            path: '/vp-policy-management/profile-management',
            Component: ProfileManagementPage,
          },
          {
            path: '/vp-policy-management/filter-management',
            Component: FilterManagementPage,
          },
          {
            path: '/vp-policy-management/process-management',
            Component: ProcessManagementPage,
          },
          {
            path: '/vp-policy-management/policy-management',
            Component: PolicyManagementPage,
          },
          {
            path: '/vp-policy-management',
            Component: VpPolicyManagementPage,
          },
          {
            path: '/vp-submission-management',
            Component: VpSubmissionManagementPage,
          },
          {
            path: '/admin-management',
            Component: AdminManagementPage,
          },
          {
            path: '/server-management',
            Component: ServerManagementPage,
          },
        ],
      },
      {
        path: '/sign-in',
        Component: SignInPage,
      },
      {
        path: '/error',
        Component: ErrorPage,
      },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
