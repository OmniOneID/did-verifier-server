import { Box, Button, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { searchServiceList } from '../../../apis/vp-payload-api';
import { getPolicy, putPolicy } from '../../../apis/vp-policy-api';
import { searchProfileList } from '../../../apis/vp-profile-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface PolicyFormData {
  id: number;
  policyTitle: string;
  payloadId: string;
  payloadService?: string;
  policyProfileId: string;
  profileTitle?: string;
}

interface ErrorState {
  policyTitle?: string;
  payloadId?: string;
  policyProfileId?: string;
}

const PolicyEditPage = (props: Props) => {
  const { id } = useParams();
  const policyId = id ? parseInt(id, 10) : null;
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const theme = useTheme();
  
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errors, setErrors] = useState<ErrorState>({});
  const [originalData, setOriginalData] = useState<PolicyFormData | null>(null);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  
  // States for search dialogs
  const [profileSearchOpen, setProfileSearchOpen] = useState(false);
  const [payloadSearchOpen, setPayloadSearchOpen] = useState(false);
  const [profileList, setProfileList] = useState<{id: string, title: string}[]>([]);
  const [payloadList, setPayloadList] = useState<{id: string, title: string}[]>([]);
  const [profileLoading, setProfileLoading] = useState(false);
  const [payloadLoading, setPayloadLoading] = useState(false);
  
  const [policyData, setPolicyData] = useState<PolicyFormData>({
    id: 0,
    policyTitle: '',
    payloadId: '',
    payloadService: '',
    policyProfileId: '',
    profileTitle: ''
  });
  
  useEffect(() => {
    const fetchPolicyData = async () => {
      if (policyId === null || isNaN(policyId)) {
        await dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: 'Invalid policy ID.', 
          isModal: true 
        }, {
          onClose: async () => navigate('/vp-policy-management', { replace: true }),
        });
        return;
      }

      try {
        setIsLoading(true);
        const { data } = await getPolicy(policyId);
        
        const policyFormData = {
          id: data.id,
          policyTitle: data.policyTitle || '',
          payloadId: data.payloadId || '',
          payloadService: data.payloadService || '',
          policyProfileId: data.policyProfileId || '',
          profileTitle: data.profileTitle || '',
        };
        
        setPolicyData(policyFormData);
        setOriginalData(policyFormData);
        
        setIsLoading(false);
      } catch (err) {
        console.error('Failed to fetch policy information:', err);
        setIsLoading(false);
        
        await dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: `Failed to fetch policy information: ${err}`, 
          isModal: true 
        }, {
          onClose: async () => navigate('/vp-policy-management', { replace: true }),
        });
      }
    };

    fetchPolicyData();
  }, [policyId, dialogs, navigate]);
  
  // Check if form data has changed
  useEffect(() => {
    if (!originalData) return;
    
    const isChanged = 
      policyData.policyTitle !== originalData.policyTitle ||
      policyData.payloadId !== originalData.payloadId ||
      policyData.policyProfileId !== originalData.policyProfileId;
    
    setIsButtonDisabled(!isChanged);
  }, [policyData, originalData]);
  
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setPolicyData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // API response processing helper
  const processApiResponse = (data: any): any[] => {
    if (!data) return [];
    
    // Check if data is an array
    if (Array.isArray(data)) {
        return data;
    }
    
    // Check if data has a data property that's an array
    if (data.data && Array.isArray(data.data)) {
        return data.data;
    }
    
    // Check if data is an object with content array (pagination)
    if (data.content && Array.isArray(data.content)) {
        return data.content;
    }
    
    // Check if data is an object with items array
    if (data.items && Array.isArray(data.items)) {
        return data.items;
    }
    
    // If we can't find a valid structure, return empty array
    console.error('Unexpected API response format:', data);
    return [];
  };
  
  // Profile search function
  const handleProfileSearch = async (searchTerm?: string) => {
    setProfileSearchOpen(true);
    
    try {
      setProfileLoading(true);
      // If no search term provided, search for 'all' to get all profiles
      const response = await searchProfileList(searchTerm || 'all');
      const processed = processApiResponse(response);
      
      const mappedProfiles = processed.map(item => ({
        id: item.policyProfileId || '',
        title: item.title || '[No Title]'
      }));
      
      setProfileList(mappedProfiles);
      setProfileLoading(false);
    } catch (err) {
      console.error('Failed to search profiles:', err);
      setProfileLoading(false);
      
      dialogs.open(CustomDialog, { 
        title: 'Error', 
        message: 'Failed to search profiles. Please try again.', 
        isModal: true 
      });
    }
  };
  
  // Payload search function
  const handlePayloadSearch = async (searchTerm?: string) => {
    setPayloadSearchOpen(true);
    
    try {
      setPayloadLoading(true);
      // If no search term provided, search for 'all' to get all payloads
      const response = await searchServiceList(searchTerm || 'all');        
      const processed = processApiResponse(response);        
      const mappedPayloads = processed.map(item => ({
        id: item.payloadId || '',
        title: item.service || '[No Service Name]'
      }));
      
      setPayloadList(mappedPayloads);
      setPayloadLoading(false);
    } catch (err) {
      console.error('Failed to search payloads:', err);
      setPayloadLoading(false);
      
      dialogs.open(CustomDialog, { 
        title: 'Error', 
        message: 'Failed to search payloads. Please try again.', 
        isModal: true 
      });
    }
  };
  
  const handleProfileSelect = (selectedProfile: { id: string, title: string }) => {
    console.log('Selected profile:', selectedProfile);
    setPolicyData(prev => ({
      ...prev,
      policyProfileId: selectedProfile.id,
      profileTitle: selectedProfile.title
    }));
  };
  
  const handlePayloadSelect = (selectedPayload: { id: string, title: string }) => {
    console.log('Selected payload:', selectedPayload);
    setPolicyData(prev => ({
      ...prev,
      payloadId: selectedPayload.id,
      payloadService: selectedPayload.title
    }));
  };
  
  const validate = () => {
    let tempErrors: ErrorState = {};
    
    // Validate required fields
    tempErrors.policyTitle = validatePolicyTitle(policyData.policyTitle);    
    tempErrors.policyProfileId = validatePolicyProfileId(policyData.policyProfileId);
    tempErrors.payloadId = validatePayloadId(policyData.payloadId);
    
    setErrors(tempErrors);
    
    // Check if there are any errors
    return !Object.values(tempErrors).some(error => error !== undefined);
  };
  
  const validatePolicyTitle = (title?: string): string | undefined => {
    if (!title) return 'Policy title is required';
    if (title.length > 100) return 'Title must be less than 100 characters';
    return undefined;
  };
  
  const validatePolicyProfileId = (policyProfileId?: string): string | undefined => {
    if (!policyProfileId) return 'Profile selection is required';
    return undefined;
  };
  
  const validatePayloadId = (payloadId?: string): string | undefined => {
    if (!payloadId) return 'Payload selection is required';
    return undefined;
  };
  
  const handleReset = () => {
    if (originalData) {
      setPolicyData(originalData);
      setErrors({});
    }
  };
  
  const handleCancel = () => {
    navigate(`/vp-policy-management/policy/${policyId}`);
  };
  
  const handleSubmit = async () => {
    if (!validate()) {
      await dialogs.open(CustomDialog, { 
        title: 'Validation Error', 
        message: 'Please fill in all required fields correctly.', 
        isModal: true 
      });
      return;
    }
    
    try {
      setIsLoading(true);
      
      // Prepare data for submission
      const dataToSubmit = { ...policyData };
      
      // Call API
      await putPolicy(dataToSubmit);
      
      setIsLoading(false);
      
      // Show success dialog
      await dialogs.open(CustomDialog, { 
        title: 'Success', 
        message: 'Policy has been updated successfully.', 
        isModal: true 
      }, {
        onClose: async () => navigate(`/vp-policy-management/policy/${policyId}`, { replace: true }),
      });
    } catch (err) {
      console.error('Failed to update policy:', err);
      setIsLoading(false);
      
      // Show error dialog
      await dialogs.open(CustomDialog, { 
        title: 'Error', 
        message: `Failed to update policy: ${err}`, 
        isModal: true 
      });
    }
  };
  
  // Load initial data for search dialogs
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        // Load Profile data
        setProfileLoading(true);
        const profileData = await searchProfileList('all');
        const profileConverted = processApiResponse(profileData);
        const profileMapped = profileConverted.map(item => ({
          id: item.policyProfileId || '',
          title: item.title || '[No Title]'
        }));
        setProfileList(profileMapped);
        setProfileLoading(false);
        
        // Load Payload data
        setPayloadLoading(true);
        const payloadData = await searchServiceList('all');                
        const processed = processApiResponse(payloadData);        
        const mappedPayloads = processed.map(item => ({
          id: item.payloadId || '',      
          title: item.service || '[No Service Name]',
        }));
        
        setPayloadList(mappedPayloads);
        setPayloadLoading(false);
      } catch (err) {
        console.error('Failed to load initial data:', err);
        setProfileLoading(false);
        setPayloadLoading(false);
        
        dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: 'Failed to load profile and payload data. Please try again.', 
          isModal: true 
        });
      }
    };
    
    loadInitialData();
  }, [dialogs]);

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 800,
    margin: 'auto',
    marginTop: theme.spacing(1),
    padding: theme.spacing(3),
    border: 'none',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: '#ffffff',
    boxShadow: '0px 4px 8px 0px #0000001A',
  })), []);

  const StyledTitle = useMemo(() => styled(Typography)({
      textAlign: 'left',
      fontSize: '24px',
      fontWeight: 700,
  }), []);

  const StyledInputArea = useMemo(() => styled(Box)(({ theme }) => ({
      marginTop: theme.spacing(2),
  })), []);

  return (
    <>
      <FullscreenLoader open={isLoading} />
      <Typography variant="h4">Policy Management</Typography>
      
      {/* Profile Search Dialog */}
      <SearchDialog
        open={profileSearchOpen}
        onClose={() => setProfileSearchOpen(false)}
        onSelect={handleProfileSelect}
        onSearch={handleProfileSearch}
        title="Profile Search"
        items={profileList}
        loading={profileLoading}
        idField="id"  
      />

      {/* Payload Search Dialog */}
      <SearchDialog
        open={payloadSearchOpen}
        onClose={() => setPayloadSearchOpen(false)}
        onSelect={handlePayloadSelect}
        onSearch={handlePayloadSearch}
        title="Payload Search"
        items={payloadList}
        loading={payloadLoading}
        idField="id"  
      />
      
      <StyledContainer>
        <StyledTitle>Policy Update</StyledTitle>
        
        {policyData && (
          <StyledInputArea>
            <TextField
              fullWidth
              required
              label="Policy Title"
              name="policyTitle"
              value={policyData?.policyTitle || ''}
              variant="outlined" 
              margin="normal" 
              onChange={handleInputChange}
              error={!!errors.policyTitle}
              helperText={errors.policyTitle}
            />
            
            <Typography variant="h6" sx={{ mt: 3 }}>Profile Information</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              {/* Hidden input for policyProfileId */}
              <input type="hidden" value={policyData?.policyProfileId || ''} />
              
              <TextField
                sx={{ flex: 1 }}
                label="Profile Title"
                value={policyData?.profileTitle || ''}
                variant="outlined" 
                size="small"
                InputProps={{ readOnly: true }}
                error={!!errors.policyProfileId}
                helperText={errors.policyProfileId}
              />
              <Button 
                variant="contained" 
                size="small"
                onClick={() => setProfileSearchOpen(true)}
              >
                Search
              </Button>
            </Box>
            
            <Typography variant="h6" sx={{ mt: 3 }}>Payload Information</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              {/* Hidden input for payloadId */}
              <input type="hidden" value={policyData?.payloadId || ''} />
              
              <TextField
                sx={{ flex: 1 }}
                label="Payload Service"                        
                value={policyData?.payloadService || ''}
                variant="outlined" 
                size="small"
                InputProps={{ readOnly: true }}
                error={!!errors.payloadId}
                helperText={errors.payloadId}
              />
              <Button 
                variant="contained" 
                size="small"
                onClick={() => setPayloadSearchOpen(true)}
              >
                Search
              </Button>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
            <Button 
                variant="contained" 
                color="primary" 
                onClick={handleSubmit}
                disabled={isButtonDisabled}
              >
                Update
              </Button>
              <Button 
                variant="contained" 
                color="secondary" 
                onClick={handleReset}
              >
                Reset
              </Button>
              <Button 
                variant="outlined" 
                color="secondary" 
                onClick={handleCancel}
              >
                Cancel
              </Button>
            </Box>
          </StyledInputArea>
        )}
      </StyledContainer>
    </>
  );
};

export default PolicyEditPage;