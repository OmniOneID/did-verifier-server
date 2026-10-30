import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import { createBrowserRouter, redirect, RouterProvider } from 'react-router';
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
import ProofRequestConfigurationDetailPage from './pages/zkp-policy-management/proof-request-configuration/ProofRequestConfigurationDetailPage';
import ProofRequestConfigurationEditPage from './pages/zkp-policy-management/proof-request-configuration/ProofRequestConfigurationEditPage';
import ZkpProfileRegistrationPage from './pages/zkp-policy-management/zkp-profile-management/ZkpProfileRegistrationPage';
import ZkpProfileDetailPage from './pages/zkp-policy-management/zkp-profile-management/ZkpProfileDetailPage';
import ZkpProfileEditPage from './pages/zkp-policy-management/zkp-profile-management/ZkpProfileEditPage';
import ZkpPolicyRegistration from './pages/zkp-policy-management/zkp-policy-management/ZkpPolicyRegistrationPage';
import ZkpPolicyEditPage from './pages/zkp-policy-management/zkp-policy-management/ZkpPolicyEditPage';
import ZkpPolicyDetailPage from './pages/zkp-policy-management/zkp-policy-management/ZkpPolicyDetailPage';
import Oid4vpConfigPage from './pages/oid4vp-management/config/Oid4vpConfigPage';
import ScopeMappingManagementPage from './pages/oid4vp-management/scope-mapping/ScopeMappingManagementPage';
import ScopeMappingRegistrationPage from './pages/oid4vp-management/scope-mapping/ScopeMappingRegistrationPage';
import ScopeMappingDetailPage from './pages/oid4vp-management/scope-mapping/ScopeMappingDetailPage';
import ScopeMappingEditPage from './pages/oid4vp-management/scope-mapping/ScopeMappingEditPage';
import Oid4vpPolicyManagementPage from './pages/oid4vp-management/policy/Oid4vpPolicyManagementPage';
import Oid4vpPolicyRegistrationPage from './pages/oid4vp-management/policy/Oid4vpPolicyRegistrationPage';
import Oid4vpPolicyDetailPage from './pages/oid4vp-management/policy/Oid4vpPolicyDetailPage';
import Oid4vpPolicyEditPage from './pages/oid4vp-management/policy/Oid4vpPolicyEditPage';
import ApiLogManagementPage from './pages/log/ApiLogManagementPage';
import AuditApiLogManagementPage from './pages/log/AuditApiLogManagementPage';
import ServerConfigurationPage from './pages/server-configuration/ServerConfigurationPage';
import PasswordPolicyManagementPage from './pages/admins/password-policy/PasswordPolicyManagementPage';


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
            path: '/opendid/vp-policy-management/service-management/service-registration',
            Component: ServiceRegistrationPage,
          },
          {
            path: '/opendid/vp-policy-management/service-management/service-edit/:id',
            Component: ServiceEditPage,
          },
          {
            path: '/opendid/vp-policy-management/service-management/:id',
            Component: ServiceDetailPage,
          },
          {
            path: '/opendid/vp-policy-management/service-management',
            Component: ServiceManagementPage,
          },
          {
            path: '/opendid/vp-policy-management/profile-management',
            Component: ProfileManagementPage,
          },
          {
            path: '/opendid/vp-policy-management/profile-management/profile-edit/:id',
            Component: ProfileEditPage,
          },
          {
            path: '/opendid/vp-policy-management/profile-management/profile-registration',
            Component: ProfileRegistrationPage,
          },
          {
            path: '/opendid/vp-policy-management/profile-management/:id',
            Component: ProfileDetailPage,
          },
          {
            path: '/opendid/vp-policy-management/filter-management',
            Component: FilterManagementPage,
          },
          {
            path: '/opendid/vp-policy-management/filter-management/filter-edit/:id',
            Component: FilterEditPage,            
          },
          {
            path: '/opendid/vp-policy-management/filter-management/filter-registration',
            Component: FilterRegistrationPage,
          },
          {
            path: '/opendid/vp-policy-management/filter-management/:id',
            Component: FilterDetailPage,
          },
          {
            path: '/opendid/vp-policy-management/process-management',
            Component: ProcessManagementPage,
          },
          {
            path: '/opendid/vp-policy-management/process-management/process-edit/:id',
            Component: ProcessEditPage,
          },
          {
            path: '/opendid/vp-policy-management/process-management/process-registration',
            Component: ProcessRegistrationPage,
          },
          {
            path: '/opendid/vp-policy-management/process-management/:id',
            Component: ProcessDetailPage,
          },
          {
            path: '/opendid/vp-policy-management/policy-management',
            Component: PolicyManagementPage,
          },
          {
            path: '/opendid/vp-policy-management/policy-management/policy-edit/:id',
            Component: PolicyEditPage,
          },
          {
            path: '/opendid/vp-policy-management/policy-management/policy-registration',
            Component: PolicyRegistrationPage,
          },
          {
            path: '/opendid/vp-policy-management/policy-management/:id',
            Component: PolicyDetailPage,
          },
          {
            path: '/opendid/vp-policy-management',
            Component: VpPolicyManagementPage,
          },
          {
            path: '/vp-submission-management',
            Component: VpHistoryPage,
          },
          // Server Configuration
          {
            path: '/logs/api',
            Component: ApiLogManagementPage,
          },
          {
            path: '/logs/audit',
            Component: AuditApiLogManagementPage,
          },
          {
            path: 'server-configuration',
            Component: ServerConfigurationPage,
          },
          // Admin Management
          {
            path: 'admins/admin-management/admin-registration',
            Component: AdminRegisterPage,
          },
          {
            path: 'admins/admin-management/:id',
            Component: AdminDetailPage,
          },
          {
            path: 'admins/admin-management',
            Component: AdminManagementPage,
          },
          {
            path: 'admins/password-policy',
            Component: PasswordPolicyManagementPage,
          },
          {
            path: 'admins',
            Component: AdminManagementPage,
          },
          {
            path: '/server-management',
            Component: ServerManagementPage,
          },
          // OID4VP Management
          {
            path: '/oid4vp-management/config',
            Component: Oid4vpConfigPage,
          },
          {
            path: '/oid4vp-management/scope-mapping/scope-mapping-registration',
            Component: ScopeMappingRegistrationPage,
          },
          {
            path: '/oid4vp-management/scope-mapping/scope-mapping-edit/:id',
            Component: ScopeMappingEditPage,
          },
          {
            path: '/oid4vp-management/scope-mapping/:id',
            Component: ScopeMappingDetailPage,
          },
          {
            path: '/oid4vp-management/scope-mapping',
            Component: ScopeMappingManagementPage,
          },
          {
            path: '/oid4vp-management/policy/policy-registration',
            Component: Oid4vpPolicyRegistrationPage,
          },
          {
            path: '/oid4vp-management/policy/policy-edit/:id',
            Component: Oid4vpPolicyEditPage,
          },
          {
            path: '/oid4vp-management/policy/:id',
            Component: Oid4vpPolicyDetailPage,
          },
          {
            path: '/oid4vp-management/policy',
            Component: Oid4vpPolicyManagementPage,
          },
          // ZKP Policy Management
          {
            path: '/opendid/zkp-policy-management/proof-request-configuration/proof-request-configuration-registration',
            Component: ProofRequestConfigurationRegistrationPage,
          },
          {
            path: '/opendid/zkp-policy-management/proof-request-configuration/:id',
            Component: ProofRequestConfigurationDetailPage,
          },
          {
            path: '/opendid/zkp-policy-management/proof-request-configuration/proof-request-edit/:id',
            Component: ProofRequestConfigurationEditPage,
          },
          {
            path: '/opendid/zkp-policy-management/proof-request-configuration',
            Component: ProofRequestConfigurationPage,
          },
          // ZKP Profile Management
          {
            path: '/opendid/zkp-policy-management/zkp-profile-management/zkp-profile-registration',
            Component: ZkpProfileRegistrationPage,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-profile-management/profile-edit/:id',
            Component: ZkpProfileEditPage,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-profile-management/:id',
            Component: ZkpProfileDetailPage,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-profile-management',
            Component: ZkpProfileManagementPage,
          },

          // ZKP Policy Management
          {
            path: '/opendid/zkp-policy-management/zkp-policy-management/zkp-policy-registration',
            Component: ZkpPolicyRegistration,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-policy-management/zkp-policy-edit/:id',
            Component: ZkpPolicyEditPage,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-policy-management/:id',
            Component: ZkpPolicyDetailPage,
          },
          {
            path: '/opendid/zkp-policy-management/zkp-policy-management',
            Component: ZkpPolicyManagementPage,
          },
          {
            path: '/opendid/zkp-policy-management',
            Component: ProofRequestConfigurationPage,
          },
        ],
      },
      // Redirects for legacy URLs (menu moved under /opendid)
      {
        path: '/vp-policy-management/*',
        loader: ({ params }) => {
          const rest = params['*'] ?? '';
          return redirect(`/opendid/vp-policy-management${rest ? `/${rest}` : ''}`);
        },
      },
      {
        path: '/zkp-policy-management/*',
        loader: ({ params }) => {
          const rest = params['*'] ?? '';
          return redirect(`/opendid/zkp-policy-management${rest ? `/${rest}` : ''}`);
        },
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
