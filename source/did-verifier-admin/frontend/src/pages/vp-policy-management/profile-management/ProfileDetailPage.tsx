import { Box, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getProfile } from '../../../apis/vp-profile-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface LogoImage {
  format: string;
  link: string;
  value: string;
}

interface Verifier {
  did: string;
  certVcRef: string;
  name: string;
  ref: string;
}

interface ProfileFormData {
  id: number;
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

const ProfileDetailData = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const profileId = id ? parseInt(id, 10) : null;
    const [isLoading, setIsLoading] = useState<boolean>(true); 
    const [profileData, setProfileData] = useState<ProfileFormData>({
        id: 0,
        type: '',
        title: '',
        description: '',
        encoding: '',
        language: '',
        processId: 0,
        filterId: 0,
    });
    
    useEffect(() => {
        const fetchData = async () => {
            if (profileId === null || isNaN(profileId)) {
                await dialogs.open(CustomDialog, { 
                    title: 'Notification', 
                    message: 'Invalid Path.', 
                    isModal: true 
                },{
                    onClose: async () => navigate('/vp-policy-management/profile-management', { replace: true }),
                });
                return;
            }

            setIsLoading(true);

            try {
                const { data } = await getProfile(profileId);
                setProfileData({
                    id: data.id,
                    type: data.type,
                    title: data.title,
                    description: data.description,
                    logo: data.logo,
                    verifier: data.verifier,
                    encoding: data.encoding,
                    language: data.language,
                    processId: data.processId,
                    filterId: data.filterId,
                    filterTitle: data.filterTitle,
                    processTitle: data.processTitle,
                });                                    
                setIsLoading(false);
            } catch (err) {
                  console.error('Failed to fetch profile information:', err);
                  setIsLoading(false);
                  navigate('/error', { state: { message: `Failed to fetch profile information: ${err}` } });
            }
        };

        fetchData();
    }, [profileId]);

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
            <StyledContainer>
                <StyledTitle>Profile Detail Information</StyledTitle>
                <StyledInputArea>
                    <TextField
                        fullWidth
                        label="Title"
                        value={profileData?.title || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    
                    <TextField
                        fullWidth
                        label="Type"
                        value={profileData?.type || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    
                    <TextField
                        fullWidth
                        label="Description"
                        value={profileData?.description || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                        multiline
                        rows={2}
                    />
                    
                    <TextField
                        fullWidth
                        label="Encoding"
                        value={profileData?.encoding || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    
                    <TextField
                        fullWidth
                        label="Language"
                        value={profileData?.language || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    
                    {profileData.logo && (
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
                                                    value={profileData.logo.format || ''} 
                                                    slotProps={{ input: { readOnly: true } }} 
                                                />
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Link</TableCell>
                                            <TableCell>
                                                <TextField 
                                                    fullWidth 
                                                    size="small" 
                                                    value={profileData.logo.link || ''} 
                                                    slotProps={{ input: { readOnly: true } }} 
                                                />
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Value</TableCell>
                                            <TableCell>
                                                <TextField 
                                                    fullWidth 
                                                    size="small" 
                                                    value={profileData.logo.value || ''} 
                                                    multiline
                                                    rows={2}
                                                    slotProps={{ input: { readOnly: true } }} 
                                                />
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </>
                    )}
                    
                    {profileData.verifier && (
                        <>
                            <Typography variant="h6" sx={{ mt: 3 }}>Verifier Information</Typography>
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
                                            <TableCell>DID</TableCell>
                                            <TableCell>
                                                <TextField 
                                                    fullWidth 
                                                    disabled
                                                    size="small" 
                                                    value={profileData.verifier.did || ''} 
                                                    slotProps={{ input: { readOnly: true } }} 
                                                />
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell>Cert VC Ref</TableCell>
                                            <TableCell>
                                                <TextField 
                                                    disabled
                                                    fullWidth 
                                                    size="small" 
                                                    value={profileData.verifier.certVcRef || ''} 
                                                    slotProps={{ input: { readOnly: true } }} 
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
                                                    value={profileData.verifier.name || ''} 
                        slotProps={{ input: { readOnly: true } }} 
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
                                                    value={profileData.verifier.ref || ''} 
                        slotProps={{ input: { readOnly: true } }} 
                                                />
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </>
                    )}
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Process Information</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
                        {/* Hidden input for processId */}
                        <input type="hidden" value={profileData?.processId || ''} />
                        
                        <TextField
                            sx={{ flex: 1 }}                            
                            label="Process Title"
                            value={profileData?.processTitle || ''}
                            variant="outlined" 
                            size="small"
                            InputProps={{ readOnly: true }}
                        />
                    </Box>
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Filter Information</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
                        {/* Hidden input for filterId */}
                        <input type="hidden" value={profileData?.filterId || ''} />
                        
                        <TextField
                            sx={{ flex: 1 }}                            
                            label="Filter Title"
                            value={profileData?.filterTitle || ''}
                            variant="outlined" 
                            size="small"
                            InputProps={{ readOnly: true }}
                        />
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/profile-management')}>
                            Back
                        </Button>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/profile-management/profile-edit/' + id)}>
                            Go to Edit
                        </Button>
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    )
}

export default ProfileDetailData