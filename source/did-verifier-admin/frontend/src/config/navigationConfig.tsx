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
      segment: 'vp-policy-management', 
      title: 'VP Policy Management', 
      children: [
        { segment: 'service-management', title: 'Service Management' },        
        { segment: 'filter-management', title: 'Filter Management' },
        { segment: 'process-management', title: 'Process Management' },
        { segment: 'profile-management', title: 'Profile Management' },
        { segment: 'policy-management', title: 'Policy Management' },
      ],
    },
    {
      segment: 'vp-submission-management',
      title: 'VP Submission Management', 
    },
    {
      segment: 'admin-management',
      title: 'Admin Management', 
    },
    {
      segment: 'server-management',
      title: 'Server Management', 
    },
    {kind: 'divider',},
  ];
};
