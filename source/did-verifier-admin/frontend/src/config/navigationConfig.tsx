import { type Navigation } from '@toolpad/core/AppProvider';

export const getNavigationByStatus = (serverStatus: string | null): Navigation=> {
  if (serverStatus !== 'ACTIVATE') {
    return [
      {kind: 'divider',},  
      { segment: 'verifier-registration', title: 'Verifier Registration' },
      {kind: 'divider',},
    ];
  } 
  return [
    {kind: 'divider',},
    { 
      segment: 'verifier-management', 
      title: 'Verifier Management', 
    },
    { 
      segment: 'service-configuration', 
      title: 'Service Configuration',
    },
    { 
      segment: 'vp-policy-management', 
      title: 'VP Policy Management', 
      children: [    
        { segment: 'filter-management', title: 'Filter Management' },
        { segment: 'process-management', title: 'Process Management' },
        { segment: 'profile-management', title: 'Profile Management' },
        { segment: 'policy-management', title: 'Policy Management' },
      ],
    },
    { 
      segment: 'zkp-policy-management', 
      title: 'ZKP Policy Management', 
      children: [    
        { segment: 'proof-request-configuration', title: 'Proof Request Configuration' },
        { segment: 'zkp-profile-management', title: 'Profile Management' },
        { segment: 'zkp-policy-management', title: 'Policy Management' },
      ],
    },
    {
      segment: 'vp-submission-management',
      title: 'VP History Page', 
    },
    {
      segment: 'admin-management',
      title: 'Admin Management', 
    },    
    {kind: 'divider',},
  ];
};
