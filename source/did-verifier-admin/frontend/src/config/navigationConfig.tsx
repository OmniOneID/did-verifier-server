import CategoryIcon from '@mui/icons-material/Category';
import DescriptionIcon from '@mui/icons-material/Description';
import DnsIcon from '@mui/icons-material/Dns';
import FilterListIcon from '@mui/icons-material/FilterList';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import RuleIcon from '@mui/icons-material/Rule';
import SchemaIcon from '@mui/icons-material/Schema';
import StorageIcon from '@mui/icons-material/Storage';
import SupervisorAccountIcon from '@mui/icons-material/SupervisorAccount';
import ProcessIcon from '@mui/icons-material/Sync';
import { type Navigation } from '@toolpad/core/AppProvider';

export const getNavigationByStatus = (serverStatus: string | null): Navigation=> {
  if (serverStatus !== 'ACTIVATE') {
    return [{ segment: 'verifier-registration', title: 'Verifier Registration', icon: <StorageIcon /> }];
  } 
  return [
    { 
      segment: 'verifier-management', 
      title: 'Verifier Management', 
      icon: <StorageIcon />,
    },
    { 
      segment: 'vp-policy-management', 
      title: 'VP Policy Management', 
      icon: <DescriptionIcon />,
      children: [
        { segment: 'service-management', title: 'Service Management', icon: <CategoryIcon /> },        
        { segment: 'filter-management', title: 'Filter Management', icon: <FilterListIcon /> },
        { segment: 'process-management', title: 'Process Management', icon: <ProcessIcon /> },
        { segment: 'profile-management', title: 'Profile Management', icon: <SchemaIcon /> },
        { segment: 'policy-management', title: 'Policy Management', icon: <RuleIcon /> },
      ],
    },
    {
      segment: 'vp-submission-management',
      title: 'VP Submission Management', 
      icon: <HowToRegIcon />,
    },
    {
      segment: 'admin-management',
      title: 'Admin Management', 
      icon: <SupervisorAccountIcon />,
    },
    {
      segment: 'server-management',
      title: 'Server Management', 
      icon: <DnsIcon />,
    },
  ];
};
