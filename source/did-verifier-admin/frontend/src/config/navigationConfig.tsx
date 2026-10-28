import { type Navigation } from '@toolpad/core/AppProvider';

export const getNavigationByStatus = (serverStatus: string | null): Navigation => {
  if (serverStatus !== 'ACTIVATE') {
    return [
      { kind: 'divider' },
      { segment: 'verifier-registration', title: 'Verifier Registration' },
      { kind: 'divider' },
    ];
  }
  return [
    { kind: 'divider' },
    {
      segment: 'verifier-management',
      title: 'Verifier Management',
    },
    {
      segment: 'service-configuration',
      title: 'Service Management',
    },
    {
      segment: 'opendid',
      title: 'OpenDID',
      children: [
        {
          segment: 'vp-policy-management',
          title: 'VP',
          children: [
            { segment: 'filter-management', title: 'Filter Management' },
            { segment: 'process-management', title: 'Process Management' },
            { segment: 'profile-management', title: 'Profile Management' },
            { segment: 'policy-management', title: 'Policy Management' },
          ],
        },
        {
          segment: 'zkp-policy-management',
          title: 'ZKP',
          children: [
            { segment: 'proof-request-configuration', title: 'Proof Request Management' },
            { segment: 'zkp-profile-management', title: 'Profile Management' },
            { segment: 'zkp-policy-management', title: 'Policy Management' },
          ],
        },
      ],
    },
    {
      segment: 'oid4vp-management',
      title: 'OID4VP',
      children: [
        { segment: 'config', title: 'Configuration' },
        { segment: 'scope-mapping', title: 'DCQL Scope Management' },
        { segment: 'policy', title: 'Policy Management' },
      ],
    },
    {
      segment: 'vp-submission-management',
      title: 'VP History',
    },
    {
      segment: 'server-configuration',
      title: 'Server Configuration',
    },
    {
      segment: 'admins',
      title: 'Admin Management',
      children: [
        { segment: 'admin-management', title: 'Admin Management' },
        { segment: 'password-policy', title: 'Password Policy Settings' },
      ],
    },
    { kind: 'divider' },
  ];
};
