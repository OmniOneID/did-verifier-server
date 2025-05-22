import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router';
import App from './App';
import Layout from './layout/Layout';
import AdminManagementPage from './pages/admins/AdminManagementPage';
import AdminDetailPage from './pages/admins/AdminDetailPage';
import AdminRegisterPage from './pages/admins/AdminRegisterPage';
import SignInPage from './pages/auth/SignIn';
import ErrorPage from './pages/ErrorPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import ServerManagementPage from './pages/servers/ServerManagementPage';
import VerifierManagementPage from './pages/verifier/VerifierManagementPage';
import VerifierRegisterPage from './pages/verifier/VerifierRegistrationPage';
import FilterManagementPage from './pages/vp-policy-management/filter-management/FilterManagementPage';
import PolicyManagementPage from './pages/vp-policy-management/policy-management/PolicyManagementPage';
import ProcessManagementPage from './pages/vp-policy-management/process-management/ProcessManagementPage';
import ProfileManagementPage from './pages/vp-policy-management/profile-management/ProfileManagementPage';
import ServiceManagementPage from './pages/service-management/ServiceManagementPage';
import VpPolicyManagementPage from './pages/vp-policy-management/VpPolicyManagementPage';
import VpHistoryPage from './pages/vp-submission-management/VpHistoryPage';
import ServiceRegistrationPage from './pages/service-management/ServiceRegistrationPage';
import ServiceDetailPage from './pages/service-management/ServiceDetailPage';
import ServiceEditPage from './pages/service-management/ServiceEditPage';
import FilterEditPage from './pages/vp-policy-management/filter-management/FilterEditPage';
import FilterDetailPage from './pages/vp-policy-management/filter-management/FilterDetailPage';
import FilterRegistrationPage from './pages/vp-policy-management/filter-management/FilterRegistrationPage';
import ProcessEditPage from './pages/vp-policy-management/process-management/ProcessEditPage';
import ProcessDetailPage from './pages/vp-policy-management/process-management/ProcessDetailPage';
import ProcessRegistrationPage from './pages/vp-policy-management/process-management/ProcessRegistrationPage';
import ProfileDetailPage from './pages/vp-policy-management/profile-management/ProfileDetailPage';
import ProfileEditPage from './pages/vp-policy-management/profile-management/ProfileEditPage';
import ProfileRegistrationPage from './pages/vp-policy-management/profile-management/ProfileRegistrationPage';
import PolicyDetailPage from './pages/vp-policy-management/policy-management/PolicyDetailPage';
import PolicyEditPage from './pages/vp-policy-management/policy-management/PolicyEditPage';
import PolicyRegistrationPage from './pages/vp-policy-management/policy-management/PolicyRegistrationPage';
import ProofRequestConfigurationPage from './pages/zkp-policy-management/proof-request-configuration/ProofRequestConfigurationPage';
import ZkpPolicyManagementPage from './pages/zkp-policy-management/zkp-policy-management/ZkpPolicyManagementPage';
import ZkpProfileManagementPage from './pages/zkp-policy-management/zkp-profile-management/ZkpProfileManagementPage';
import ProofRequestConfigurationRegistrationPage from './pages/zkp-policy-management/proof-request-configuration/ProofRequestConfigurationRegistrationPage';


const router = createBrowserRouter([
  {
    Component: App,
    children: [
      {
        path: '/',
        Component: Layout,
        children: [
          {
            path: '/',
            Component: DashboardPage
          },
          {
            path: '/verifier-registration',
            Component: VerifierRegisterPage,
          },
          {
            path: '/verifier-management',
            Component: VerifierManagementPage,
          },
          // Service Configuration
          {
            path: '/service-configuration',
            Component: ServiceManagementPage,
          },
          // VP Policy Management
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
            path: '/vp-policy-management/profile-management/profile-edit/:id',
            Component: ProfileEditPage,
          },
          {
            path: '/vp-policy-management/profile-management/profile-registration',
            Component: ProfileRegistrationPage,
          },
          {
            path: '/vp-policy-management/profile-management/:id',
            Component: ProfileDetailPage,
          },
          {
            path: '/vp-policy-management/filter-management',
            Component: FilterManagementPage,
          },
          {
            path: '/vp-policy-management/filter-management/filter-edit/:id',
            Component: FilterEditPage,            
          },
          {
            path: '/vp-policy-management/filter-management/filter-registration',
            Component: FilterRegistrationPage,
          },
          {
            path: '/vp-policy-management/filter-management/:id',
            Component: FilterDetailPage,
          },
          {
            path: '/vp-policy-management/process-management',
            Component: ProcessManagementPage,
          },
          {
            path: '/vp-policy-management/process-management/process-edit/:id',
            Component: ProcessEditPage,
          },
          {
            path: '/vp-policy-management/process-management/process-registration',
            Component: ProcessRegistrationPage,
          },
          {
            path: '/vp-policy-management/process-management/:id',
            Component: ProcessDetailPage,
          },
          {
            path: '/vp-policy-management/policy-management',
            Component: PolicyManagementPage,
          },
          {
            path: '/vp-policy-management/policy-management/policy-edit/:id',
            Component: PolicyEditPage,
          },
          {
            path: '/vp-policy-management/policy-management/policy-registration',
            Component: PolicyRegistrationPage,
          },
          {
            path: '/vp-policy-management/policy-management/:id',
            Component: PolicyDetailPage,
          },
          {
            path: '/vp-policy-management',
            Component: VpPolicyManagementPage,
          },
          {
            path: '/vp-submission-management',
            Component: VpHistoryPage,
          },
          // Admin Management
          {
            path: 'admin-management/admin-registration',
            Component: AdminRegisterPage,
          },
          {
            path: 'admin-management/:id',
            Component: AdminDetailPage,
          },
          {
            path: 'admin-management',
            Component: AdminManagementPage,
          },
          {
            path: '/server-management',
            Component: ServerManagementPage,
          },
          // ZKP Policy Management
          {
            path: '/zkp-policy-management/proof-request-configuration/proof-request-configuration-registration',
            Component: ProofRequestConfigurationRegistrationPage,
          },
          {
            path: '/zkp-policy-management/proof-request-configuration',
            Component: ProofRequestConfigurationPage,
          },
          {
            path: '/zkp-policy-management/zkp-profile-management',
            Component: ZkpProfileManagementPage,
          },
          {
            path: '/zkp-policy-management/zkp-policy-management',
            Component: ZkpPolicyManagementPage,
          },
          {
            path: '/zkp-policy-management',
            Component: ProofRequestConfigurationPage,
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
