import { Box, Button, FormControlLabel, Paper, Radio, RadioGroup, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { getVerifierInfo } from '../../../apis/verifier-api';
import { searchFilterList } from '../../../apis/vp-filter-api';
import { searchProcessList } from '../../../apis/vp-process-api';
import { postProfile } from '../../../apis/vp-profile-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface LogoImage {
  format: string;
  link?: string;
  value?: string;
}

interface Verifier {
  did: string;
  certVcRef: string;
  name: string;
  ref: string;
}

interface ProfileFormData {
  type: string;
  title: string;
  description: string;
  logo?: LogoImage;
  verifier?: Verifier;
  encoding: string;
  language: string;
  processId: number;
  filterId: number;
  filterTitle?: string;
  processTitle?: string;
}

interface ErrorState {
  type?: string;
  title?: string;
  description?: string;
  logo?: {
    format?: string;
    link?: string;
    value?: string;
  };
  encoding?: string;
  language?: string;
  processId?: string;
  filterId?: string;
}

const ProfileRegistration = (props: Props) => {
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [showLogo, setShowLogo] = useState<boolean>(false);
    const [logoType, setLogoType] = useState<'link' | 'value'>('link');
    const [errors, setErrors] = useState<ErrorState>({});
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    
    // States for search dialogs
    const [processSearchOpen, setProcessSearchOpen] = useState(false);
    const [filterSearchOpen, setFilterSearchOpen] = useState(false);
    const [processList, setProcessList] = useState<{id: number, title: string}[]>([]);
    const [filterList, setFilterList] = useState<{id: number, title: string}[]>([]);
    const [processLoading, setProcessLoading] = useState(false);
    const [filterLoading, setFilterLoading] = useState(false);

    const transformFilterList = (data: any[]): {id: number, title: string}[] => {        
     
        return data.map((item: ApiResponseItem) => ({
          id: item.filterId || item.id || 0, 
          title: item.title || item.name || `Filter ${item.filterId || item.id || 'Unknown'}`          
        }));
      };
    
    const [profileData, setProfileData] = useState<ProfileFormData>({
        type: 'VerifyProfile',
        title: '',
        description: '',
        encoding: '',
        language: '',
        processId: 0,
        filterId: 0,
        logo: {
            format: '',
            link: '',
            value: ''
        },
        verifier: {
            did: '',
            certVcRef: '',
            name: '',
            ref: ''
        }
    });
    
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setProfileData(prev => ({
            ...prev,
            [name]: value
        }));
    };
    
    const handleLogoChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setProfileData(prev => ({
            ...prev,
            logo: {
                ...prev.logo!,
                [name]: value
            }
        }));
    };
    
    const handleLogoTypeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setLogoType(e.target.value as 'link' | 'value');
    };
    
    useEffect(() => {
      const loadVerifierData = async () => {
        try {
          setIsLoading(true);
          const serverData = await getVerifierInfo();
          const verifierData: Verifier = {
            did: serverData.data.did || '',
            name: serverData.data.name || '',
            ref: serverData.data.serverUrl || '',  
            certVcRef: serverData.data.certificateUrl || '' 
          };
          setProfileData(prev => ({
            ...prev,
            verifier: verifierData
          }));          
          setIsLoading(false);
        } catch (err) {
          console.error('Failed to fetch verifier data:', err);
          setIsLoading(false);
          
          dialogs.open(CustomDialog, { 
            title: 'Error', 
            message: 'Failed to load verifier information. Please try again.', 
            isModal: true 
          });
        }
      };
      
      loadVerifierData();
    }, [dialogs]); 
    
    useEffect(() => {
        const isModified = 
            profileData.title !== '' || 
            profileData.type !== '' || 
            profileData.encoding !== '' || 
            profileData.language !== '' || 
            profileData.processId !== 0 || 
            profileData.filterId !== 0 || 
            (showLogo && (
                profileData.logo?.format !== '' || 
                (logoType === 'link' && profileData.logo?.link !== '') || 
                (logoType === 'value' && profileData.logo?.value !== '')
            ));
        
        setIsButtonDisabled(!isModified);
    }, [profileData, showLogo, logoType]);

    // Define interface for API response items
    interface ApiResponseItem {
        id?: number;
        filterId?: number;
        title?: string;
        name?: string;
        [key: string]: any; // Allow other unknown properties
    }

    // Handle API responses with proper error checking
    const processApiResponse = (data: any): {id: number, title: string}[] => {
        if (!data) return [];
        
        // Check if data is an array
        if (Array.isArray(data)) {
            return data.map((item: ApiResponseItem) => ({
                id: item.id || 0,
                title: item.title || item.name || `Item ${item.id || 'Unknown'}`,
                filterId: item.filterId || 0,
            }));
        }
        
        // Check if data has a data property that's an array
        if (data.data && Array.isArray(data.data)) {
            return data.data.map((item: ApiResponseItem) => ({
                id: item.id || 0,
                title: item.title || item.name || `Item ${item.id || 'Unknown'}`,
                filterId: item.filterId || 0,
            }));
        }
        
        // Check if data is an object with items array
        if (data.items && Array.isArray(data.items)) {
            return data.items.map((item: ApiResponseItem) => ({
                id: item.id || 0,
                title: item.title || item.name || `Item ${item.id || 'Unknown'}`,
                filterId: item.filterId || 0,
            }));
        }
        
        // If we can't find a valid structure, return empty array
        console.error('Unexpected API response format:', data);
        return [];
    };

    useEffect(() => {
      const loadInitialData = async () => {
        try {
          // Process 데이터 로드
          setProcessLoading(true);
          const processData = await searchProcessList('all');
          setProcessList(processApiResponse(processData));
          setProcessLoading(false);
          
          // Filter 데이터 로드
          setFilterLoading(true);
          const filterData = await searchFilterList('all');           
          setFilterList(transformFilterList(processApiResponse(filterData)));        
          setFilterLoading(false);
        } catch (err) {
          console.error('Failed to load initial data:', err);
          setProcessLoading(false);
          setFilterLoading(false);
          
          dialogs.open(CustomDialog, { 
            title: 'Error', 
            message: 'Failed to load process and filter data. Please try again.', 
            isModal: true 
          });
        }
      };
      
      loadInitialData();
    }, [dialogs]);
    
    // Process 검색 함수
    const handleProcessSearch = async (searchTerm?: string) => {
      setProcessSearchOpen(true);
      
      if (searchTerm !== undefined) {
        try {
          setProcessLoading(true);
          const processData = await searchProcessList(searchTerm || 'all');
          setProcessList(processApiResponse(processData));
          setProcessLoading(false);
        } catch (err) {
          console.error('Failed to search processes:', err);
          setProcessLoading(false);
          
          dialogs.open(CustomDialog, { 
            title: 'Error', 
            message: 'Failed to search processes. Please try again.', 
            isModal: true 
          });
        }
      }
    };
    
    // Filter 검색 함수
    const handleFilterSearch = async (searchTerm?: string) => {
      setFilterSearchOpen(true);      
      
        try {
            console.log('searchTerm', searchTerm);
            setFilterLoading(true);
            const filterData = await searchFilterList(searchTerm || 'all');
            setFilterList(processApiResponse(filterData));           
            setFilterLoading(false);          
        } catch (err) {
            console.error('Failed to search filters:', err);
            setFilterLoading(false);
            
            dialogs.open(CustomDialog, { 
            title: 'Error', 
            message: 'Failed to search filters. Please try again.', 
            isModal: true 
            });
        }
      
    };
    
    const handleProcessSelect = (selectedProcess: { id?: string | number, title: string }) => {
        const processId = typeof selectedProcess.id === 'number' 
            ? selectedProcess.id 
            : typeof selectedProcess.id === 'string' 
                ? parseInt(selectedProcess.id, 10) 
                : 0;
                
        setProfileData(prev => ({
            ...prev,
            processId: processId,
            processTitle: selectedProcess.title
        }));
    };
    
    const handleFilterSelect = (selectedFilter: { id?: string | number, title: string }) => {                
        const filterId = typeof selectedFilter.id === 'number' 
            ? selectedFilter.id 
            : typeof selectedFilter.id === 'string' 
                ? parseInt(selectedFilter.id, 10) 
                : 0;
                
        setProfileData(prev => ({
            ...prev,
            filterId: filterId,
            filterTitle: selectedFilter.title
        }));
    };
    
    const validate = () => {
        let tempErrors: ErrorState = {};
        console.log('profileData', profileData);
        // Validate required fields
        tempErrors.title = validateTitle(profileData.title);
        tempErrors.type = validateType(profileData.type);
        tempErrors.description = validateDescription(profileData.description);
        tempErrors.encoding = validateEncoding(profileData.encoding);
        tempErrors.language = validateLanguage(profileData.language);
        tempErrors.processId = validateProcessId(profileData.processId);
        tempErrors.filterId = validateFilterId(profileData.filterId);
        
        // Validate logo if shown
        if (showLogo) {
            tempErrors.logo = {};
            tempErrors.logo.format = validateLogoFormat(profileData.logo?.format);
            
            if (logoType === 'link') {
                tempErrors.logo.link = validateLogoLink(profileData.logo?.link);
            } else {
                tempErrors.logo.value = validateLogoValue(profileData.logo?.value);
            }
            
            // Remove logo errors if no errors
            if (!tempErrors.logo.format && 
                ((logoType === 'link' && !tempErrors.logo.link) || 
                 (logoType === 'value' && !tempErrors.logo.value))) {
                tempErrors.logo = undefined;
            }
        }
        
        setErrors(tempErrors);
        
        // Check if there are any errors
        return !Object.values(tempErrors).some(error => 
            error !== undefined && (typeof error === 'string' || Object.values(error).some(e => e !== undefined))
        );
    };
    
    const validateTitle = (title?: string): string | undefined => {
        if (!title) return 'Title is required';
        if (title.length > 100) return 'Title must be less than 100 characters';
        return undefined;
    };
    
    const validateType = (type?: string): string | undefined => {
        if (!type) return 'Type is required';
        if (type.length > 50) return 'Type must be less than 50 characters';
        return undefined;
    };
    
    const validateDescription = (description?: string): string | undefined => {
        if (description && description.length > 500) return 'Description must be less than 500 characters';
        return undefined;
    };
    
    const validateEncoding = (encoding?: string): string | undefined => {
        if (!encoding) return 'Encoding is required';
        if (encoding.length > 30) return 'Encoding must be less than 30 characters';
        return undefined;
    };
    
    const validateLanguage = (language?: string): string | undefined => {
        if (!language) return 'Language is required';
        if (language.length > 30) return 'Language must be less than 30 characters';
        return undefined;
    };
    
    const validateProcessId = (processId?: number): string | undefined => {
        if (!processId) return 'Process selection is required';
        return undefined;
    };
    
    const validateFilterId = (filterId?: number): string | undefined => {
        if (!filterId) return 'Filter selection is required';
        return undefined;
    };
    
    const validateLogoFormat = (format?: string): string | undefined => {
        if (!format) return 'Logo format is required';
        if (format.length > 20) return 'Format must be less than 20 characters';
        return undefined;
    };
    
    const validateLogoLink = (link?: string): string | undefined => {
        if (!link) return 'Logo link is required';
        if (link.length > 255) return 'Link must be less than 255 characters';
        // URL validation could be added here
        return undefined;
    };
    
    const validateLogoValue = (value?: string): string | undefined => {
        if (!value) return 'Logo value is required';
        if (value.length > 1000) return 'Value must be less than 1000 characters';
        return undefined;
    };
    
    const handleReset = () => {
        const currentVerifier: Verifier = {
            did: profileData.verifier?.did || '',
            certVcRef: profileData.verifier?.certVcRef || '',
            name: profileData.verifier?.name || '',
            ref: profileData.verifier?.ref || ''
        };
        setErrors({});
        setIsButtonDisabled(true);
        setShowLogo(false);
        setLogoType('link');
        setProfileData({
            type: 'VerifyProfile',
            title: '',
            description: '',
            encoding: '',
            language: '',
            processId: 0,
            filterId: 0,
            logo: {
                format: '',
                link: '',
                value: ''
            },
            verifier: currentVerifier
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
            const dataToSubmit = { ...profileData };
            
            // Remove logo if not shown
            if (!showLogo) {
                delete dataToSubmit.logo;
            } else {
                // Keep only the relevant field (link or value)
                if (logoType === 'link') {
                    dataToSubmit.logo!.value = '';
                } else {
                    dataToSubmit.logo!.link = '';
                }
            }
            
            // Call API
            await postProfile(dataToSubmit);
            
            setIsLoading(false);
            
            // Show success dialog
            await dialogs.open(CustomDialog, { 
                title: 'Success', 
                message: 'Profile has been created successfully.', 
                isModal: true 
            }, {
                onClose: async () => navigate('/vp-policy-management/profile-management', { replace: true }),
            });
        } catch (err) {
            console.error('Failed to create profile:', err);
            setIsLoading(false);
            
            // Show error dialog
            await dialogs.open(CustomDialog, { 
                title: 'Error', 
                message: `Failed to create profile: ${err}`, 
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
            <Typography variant="h4">Profile Management</Typography>
            
            {/* Process Search Dialog */}
            <SearchDialog
              open={processSearchOpen}
              onClose={() => setProcessSearchOpen(false)}
              onSelect={handleProcessSelect}
              onSearch={handleProcessSearch}
              title="Process Search"
              items={processList}
              loading={processLoading}
            />

            {/* Filter Search Dialog */}
            <SearchDialog
                open={filterSearchOpen}
                onClose={() => setFilterSearchOpen(false)}
                onSelect={handleFilterSelect}
                onSearch={handleFilterSearch}
                title="Filter Search"
                items={filterList}
                loading={filterLoading}
                idField="filterId" 
            />
            <StyledContainer>
                <StyledTitle>Profile Registration</StyledTitle>
                <StyledInputArea>
                    <TextField
                        fullWidth                        
                        required
                        label="Title"
                        name="title"
                        value={profileData?.title || ''}
                        variant="outlined" 
                        size='small'
                        margin="normal" 
                        onChange={handleInputChange}
                        error={!!errors.title}
                        helperText={errors.title}
                    />
                    
                    <TextField
                        fullWidth
                        required
                        label="Type"
                        name="type"
                        value={profileData?.type || ''}
                        variant="outlined" 
                        size='small'
                        margin="normal" 
                        onChange={handleInputChange}
                        error={!!errors.type}
                        helperText={errors.type}
                        disabled={true}
                    />
                    
                    <TextField
                        fullWidth                                                
                        label="Description"
                        name="description"
                        value={profileData?.description || ''}
                        variant="outlined" 
                        size='small'
                        margin="normal" 
                        onChange={handleInputChange}
                        multiline
                        rows={2}
                        error={!!errors.description}
                        helperText={errors.description}
                    />
                    
                    <TextField
                        fullWidth
                        required
                        label="Encoding"
                        name="encoding"
                        value={profileData?.encoding || ''}
                        variant="outlined" 
                        size='small'
                        margin="normal" 
                        onChange={handleInputChange}
                        error={!!errors.encoding}
                        helperText={errors.encoding}
                    />
                    
                    <TextField
                        fullWidth
                        required
                        label="Language"
                        name="language"
                        value={profileData?.language || ''}
                        variant="outlined" 
                        size='small'
                        margin="normal" 
                        onChange={handleInputChange}
                        error={!!errors.language}
                        helperText={errors.language}
                    />
                    
                    <Box sx={{ mt: 3, display: 'flex', alignItems: 'center' }}>
                        <Typography variant="h6" sx={{ mr: 2 }}>Include Logo</Typography>
                        <FormControlLabel
                            control={
                                <Radio
                                    checked={showLogo}
                                    onChange={() => setShowLogo(true)}
                                />
                            }
                            label="Yes"
                        />
                        <FormControlLabel
                            control={
                                <Radio
                                    checked={!showLogo}
                                    onChange={() => setShowLogo(false)}
                                />
                            }
                            label="No"
                        />
                    </Box>
                    
                    {showLogo && (
                        <>
                            <Typography variant="h6" sx={{ mt: 3 }}>Logo Information</Typography>
                            <TableContainer component={Paper}>
                                <Table>
                                    <TableHead>
                                        <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                            <TableCell>Property</TableCell>
                                            <TableCell>Value</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        <TableRow>
                                            <TableCell>Format</TableCell>
                                            <TableCell>
                                                <TextField 
                                                    fullWidth 
                                                    size="small" 
                                                    name="format"
                                                    value={profileData.logo?.format || ''} 
                                                    onChange={handleLogoChange}
                                                    error={!!errors.logo?.format}
                                                    helperText={errors.logo?.format}
                                                />
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>
                                                <RadioGroup
                                                    row
                                                    value={logoType}
                                                    onChange={handleLogoTypeChange}
                                                >
                                                    <FormControlLabel value="link" control={<Radio />} label="Link" />
                                                    <FormControlLabel value="value" control={<Radio />} label="Value" />
                                                </RadioGroup>
                                            </TableCell>
                                            <TableCell>
                                                {logoType === 'link' ? (
                                                    <TextField 
                                                        fullWidth 
                                                        size="small" 
                                                        name="link"
                                                        value={profileData.logo?.link || ''} 
                                                        onChange={handleLogoChange}
                                                        placeholder="Enter URL"
                                                        error={!!errors.logo?.link}
                                                        helperText={errors.logo?.link}
                                                    />
                                                ) : (
                                                    <TextField 
                                                        fullWidth 
                                                        size="small" 
                                                        name="value"
                                                        value={profileData.logo?.value || ''}
                                                        onChange={handleLogoChange}
                                                        multiline
                                                        rows={2}
                                                        placeholder="Enter value"
                                                        error={!!errors.logo?.value}
                                                        helperText={errors.logo?.value}
                                                    />
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </>
                    )}
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Verifier Information</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 2 }}>
                        Verifier information is read-only and can only be modified in the Verifier Management page.
                    </Typography>
                    <TableContainer component={Paper} sx={{ mt: 2, mb: 3 }}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell>Property</TableCell>
                                    <TableCell>Value</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow>
                                    <TableCell>DID</TableCell>
                                    <TableCell>
                                        <TextField 
                                            fullWidth 
                                            disabled
                                            size="small" 
                                            value={profileData.verifier?.did || ''} 
                                            InputProps={{ readOnly: true }}
                                        />
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Cert VC Ref</TableCell>
                                    <TableCell>
                                        <TextField 
                                            fullWidth 
                                            disabled
                                            size="small" 
                                            value={profileData.verifier?.certVcRef || ''} 
                                            InputProps={{ readOnly: true }}
                                        />
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Name</TableCell>
                                    <TableCell>
                                        <TextField 
                                            fullWidth 
                                            disabled
                                            size="small" 
                                            value={profileData.verifier?.name || ''} 
                                            InputProps={{ readOnly: true }}
                                        />
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Ref</TableCell>
                                    <TableCell>
                                        <TextField 
                                            fullWidth 
                                            disabled
                                            size="small" 
                                            value={profileData.verifier?.ref || ''} 
                                            InputProps={{ readOnly: true }}
                                        />
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </TableContainer>
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Process Information</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
                        {/* Hidden input for processId */}
                        <input type="hidden" value={profileData?.processId || ''} />
                        
                        <TextField
                            sx={{ flex: 1 }}
                            required
                            label="Process Title"
                            value={profileData?.processTitle || ''}
                            variant="outlined" 
                            size="small"
                            InputProps={{ readOnly: true }}
                            error={!!errors.processId}
                            helperText={errors.processId}
                        />
                        <Button 
                            variant="contained" 
                            size="small"
                            onClick={() => handleProcessSearch()}
                        >
                            Search
                        </Button>
                    </Box>
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Filter Information</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
                        {/* Hidden input for filterId */}
                        <input type="hidden" value={profileData?.filterId || ''} />
                        
                        <TextField
                            sx={{ flex: 1 }}
                            required
                            label="Filter Title"
                            value={profileData?.filterTitle || ''}
                            variant="outlined" 
                            size="small"
                            InputProps={{ readOnly: true }}
                            error={!!errors.filterId}
                            helperText={errors.filterId}
                        />
                        <Button 
                            variant="contained" 
                            size="small"
                            onClick={() => handleFilterSearch()}
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
                            onClick={() => navigate('/vp-policy-management/profile-management')}
                        >
                            Cancel
                        </Button>
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    );
};

export default ProfileRegistration;