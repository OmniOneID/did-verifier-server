import { useDialogs } from '@toolpad/core';
import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import { postPolicy } from '../../../apis/vp-policy-api';
import { searchProfileList } from '../../../apis/vp-profile-api';
import { searchServiceList } from '../../../apis/vp-payload-api';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import { Box, Button, Paper, TextField, Typography, useTheme } from '@mui/material';

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

const PolicyRegistration = (props: Props) => {
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
          const profileData = await searchProfileList('all');
          console.log('Profile data:', profileData);
          const profileConverted = processApiResponse(profileData);
          const profileMapped = profileConverted.map(item => ({
            id: item.policyProfileId || '',
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
              title: item.service || '[No Service Name]',
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
    }, [dialogs]);

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
                onClose: async () => navigate('/vp-policy-management/policy-management', { replace: true }),
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

    return (
        <>
            <FullscreenLoader open={isLoading} />
            
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
            <Box sx={{ p: 3 }}>
                <Typography variant="h4">Create New Policy</Typography>
                <Box sx={{ maxWidth: 800, margin: 'auto', mt: 2, p: 3, border: '1px solid #ccc', borderRadius: 2 }}>
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
                            color="secondary" 
                            onClick={() => navigate('/vp-policy-management/policy-management')}
                        >
                            Cancel
                        </Button>
                        <Button 
                            variant="contained" 
                            color="secondary" 
                            onClick={handleReset}
                        >
                            Reset
                        </Button>
                        <Button 
                            variant="contained" 
                            color="primary" 
                            onClick={handleSubmit}
                            disabled={isButtonDisabled}
                        >
                            Save
                        </Button>
                    </Box>
                </Box>
            </Box>
        </>
    );
};

export default PolicyRegistration;