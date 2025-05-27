import { Box, Button, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { searchServiceList } from '../../../apis/vp-payload-api';
import { postPolicy } from '../../../apis/zkp-policy-api';
import { searchZkpProfileList } from '../../../apis/zkp-profile-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface PolicyFormData {
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

const ZkpPolicyRegistration = (props: Props) => {
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [errors, setErrors] = useState<ErrorState>({});
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);

    // States for search dialogs
    const [profileSearchOpen, setProfileSearchOpen] = useState(false);
    const [payloadSearchOpen, setPayloadSearchOpen] = useState(false);
    const [profileList, setProfileList] = useState<{id: string, title: string}[]>([]);
    const [payloadList, setPayloadList] = useState<{id: string, title: string}[]>([]);
    const [profileLoading, setProfileLoading] = useState(false);
    const [payloadLoading, setPayloadLoading] = useState(false);


    const [policyData, setPolicyData] = useState<PolicyFormData>({
        policyTitle: '',
        payloadId: '',
        payloadService: '',
        policyProfileId: '',
        profileTitle: ''
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setPolicyData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    useEffect(() => {
        const isModified = 
            policyData.policyTitle !== '' || 
            policyData.payloadService !== '' || 
            policyData.policyProfileId !== '' || 
            policyData.payloadId !== '';
        
        setIsButtonDisabled(!isModified);
    }, [policyData]);

    // Load initial data when component mounts
    useEffect(() => {
      const loadInitialData = async () => {
        try {
          // Load Profile data
          setProfileLoading(true);
          const profileData = await searchZkpProfileList('all');
          console.log('Profile data:', profileData);
          const profileConverted = processApiResponse(profileData);
          const profileMapped = profileConverted.map(item => ({
            id: item.profileId || '',
            title: item.title || '[No Title]'
          }));
          console.log('Profile mapped:', profileMapped);
          setProfileList(profileMapped);
          setProfileLoading(false);
          
          // Load Payload data
          setPayloadLoading(true);
          const payloadData = await searchServiceList('all');        
          console.log('Payload data:', payloadData);        
          const processed = processApiResponse(payloadData);        
          const mappedPayloads = processed.map(item => ({
              id: item.payloadId || '',      
              title: item.service || '[No Service Name]'              
          }));
          console.log('Payload mapped:', mappedPayloads);
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
    }, []);

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
            const response = await searchZkpProfileList(searchTerm || 'all');
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

    const handleProfileSelect = (selectedProfile: any) => {
        
        setPolicyData(prev => ({
            ...prev,
            policyProfileId: selectedProfile.id?.toString() || '',
            profileTitle: selectedProfile.title || ''
        }));
    };

    const handlePayloadSelect = (selectedPayload: any) => {
        
        setPolicyData(prev => ({
            ...prev,
            payloadId: selectedPayload.id?.toString() || '',
            payloadService: selectedPayload.title || ''
        }));
    };

    const validate = () => {
        let tempErrors: ErrorState = {};
        
        console.log(policyData);

        // Validate required fields
        tempErrors.policyTitle = validatePolicyTitle(policyData.policyTitle);    
        tempErrors.policyProfileId = validatepolicyProfileId(policyData.policyProfileId);
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

    const validatepolicyProfileId = (policyProfileId?: string): string | undefined => {
        if (!policyProfileId) return 'Profile selection is required';
        return undefined;
    };

    const validatePayloadId = (payloadId?: string): string | undefined => {
        if (!payloadId) return 'Payload selection is required';
        return undefined;
    };

    const handleReset = () => {
        setErrors({});
        setIsButtonDisabled(true);
        setPolicyData({
            policyTitle: '',
            payloadId: '',
            payloadService: '',
            policyProfileId: '',
            profileTitle: ''
        });
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
            await postPolicy(dataToSubmit);
            
            setIsLoading(false);
            
            // Show success dialog
            await dialogs.open(CustomDialog, { 
                title: 'Success', 
                message: 'Policy has been created successfully.', 
                isModal: true 
            }, {
                onClose: async () => navigate('/zkp-policy-management/zkp-policy-management', { replace: true }),
            });
        } catch (err) {
            console.error('Failed to create policy:', err);
            setIsLoading(false);
            
            // Show error dialog
            await dialogs.open(CustomDialog, { 
                title: 'Error', 
                message: `Failed to create policy: ${err}`, 
                isModal: true 
            });
        }
    };

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
            <Typography variant="h4">ZKP Policy Management</Typography>
            
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
                service=""
            />

            <SearchDialog
                open={payloadSearchOpen}
                onClose={() => setPayloadSearchOpen(false)}
                onSelect={handlePayloadSelect}
                onSearch={handlePayloadSearch}
                title="Payload Search"
                items={payloadList}
                loading={payloadLoading}
                idField="id"
                service=""
            />
            
            <StyledContainer>
                <StyledTitle>ZKP Policy Registration</StyledTitle>
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
                            required
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
                            required
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
                            Register
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
                            onClick={() => navigate('/vp-policy-management/policy-management')}
                        >
                            Cancel
                        </Button>
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    );
};

export default ZkpPolicyRegistration;