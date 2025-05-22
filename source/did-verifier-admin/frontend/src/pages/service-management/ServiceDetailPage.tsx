import { Box, Button, FormControl, InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getService } from '../../apis/vp-payload-api';
import CustomDialog from '../../components/dialog/CustomDialog';
import FullscreenLoader from '../../components/loading/FullscreenLoader';

type Props = {}

interface ServiceFormData {
    service: string;
    locked?: boolean;
    device: string;
    mode: string;
    offerType?: string;
    validSeconds: number;
    endpoints: string[];
}

const ServiceDetailPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const numericServiceId = id ? parseInt(id, 10) : null;
    const [isLoading, setIsLoading] = useState<boolean>(true); 
    const [serviceData, setServiceData] = useState<ServiceFormData>({
        service: '',
        locked: undefined,
        device: '',
        mode: '',
        validSeconds: 180,
        endpoints: [],
    });
    
    useEffect(() => {
        const fetchData = async () => {
            if (numericServiceId === null || isNaN(numericServiceId)) {
                await dialogs.open(CustomDialog, { 
                    title: 'Notification', 
                    message: 'Invalid Path.', 
                    isModal: true 
                },{
                    onClose: async () => navigate('/vp-policy-management/service-management', { replace: true }),
                });
                return;
            }

            setIsLoading(true);

            try {
                const { data } = await getService(numericServiceId);
                setServiceData({
                    service: data.service,
                    locked: data.locked,
                    device: data.device,
                    mode: data.mode,
                    offerType: data.offerType,
                    validSeconds: data.validSecond || 180,
                    endpoints: JSON.parse(data.endpoints),
                });
                setIsLoading(false);
            } catch (err) {
                  console.error('Failed to fetch Service information:', err);
                  setIsLoading(false);
                  navigate('/error', { state: { message: `Failed to namespace information: ${err}` } });
            }
        };

        fetchData();
    }, [numericServiceId]);

    const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
        width: 500,
        margin: 'auto',
        marginTop: theme.spacing(3),
        padding: theme.spacing(3),
        border: 'none',
        borderRadius: theme.shape.borderRadius,
        backgroundColor: '#ffffff',
        boxShadow: '0px 4px 8px 0px #0000001A',
    })), []);
        
    const StyledSubTitle = useMemo(() => styled(Typography)({
        textAlign: 'left',
        fontSize: '24px',
        fontWeight: 700,
    }), []);

    const StyledDescription = useMemo(() => styled(Box)(({ theme }) => ({
        maxWidth: 500, 
        marginTop: theme.spacing(1),
        padding: theme.spacing(0),
    })), []);

    const StyledInputArea = useMemo(() => styled(Box)(({ theme }) => ({
        marginTop: theme.spacing(2),
    })), []);

    return (
        <>
            <FullscreenLoader open={isLoading} />
            <Typography variant="h4">Service Management</Typography>
            <StyledContainer>
                <StyledSubTitle>Service Detail Information</StyledSubTitle>

                <StyledInputArea>
                    <TextField
                        fullWidth
                        label="Service"
                        value={serviceData?.service || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <FormControl fullWidth margin="normal" variant='standard'>
                        <InputLabel>Lock Status</InputLabel>
                        <Select 
                            value={serviceData?.locked === undefined ? "" : String(serviceData.locked)} 
                            label="Lock Status"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            <MenuItem value={"true"}>Locked</MenuItem>
                            <MenuItem value={"false"}>Unlocked</MenuItem>
                        </Select>
                    </FormControl>

                    <TextField
                        fullWidth
                        label="Device"
                        value={serviceData?.device || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <FormControl fullWidth margin="normal" variant='standard'>
                        <InputLabel>Submission Mode</InputLabel>
                        <Select 
                            value={serviceData?.mode || ''} 
                            label="Submission Mode"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            <MenuItem value="Direct">Direct</MenuItem>
                            <MenuItem value="Indirect">inDirect</MenuItem>
                            <MenuItem value="Proxy">Proxy</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControl fullWidth margin="normal" variant='standard'>
                        <InputLabel>Verification Type</InputLabel>
                        <Select 
                            value={serviceData?.offerType || ''} 
                            label="Verification Type"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            <MenuItem value="VerifyOffer">VP</MenuItem>
                            <MenuItem value="VerifyProofOffer">ZKP</MenuItem>
                        </Select>
                    </FormControl>

                    <TextField
                        fullWidth
                        label="Valid Seconds"
                        value={serviceData?.validSeconds || 180}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <Typography variant="h6" sx={{ mt: 3 }}>Endpoints</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell>API Address</TableCell> 
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {serviceData.endpoints?.map((endpoint, index) => (
                                    <TableRow key={index}>
                                        <TableCell>
                                            <TextField 
                                                fullWidth 
                                                size="small" 
                                                value={endpoint}
                                                InputProps={{ readOnly: true }}
                                            />
                                        </TableCell>
                                    </TableRow>
                                ))}
                                {(serviceData.endpoints?.length === 0 || !serviceData.endpoints) && (
                                    <TableRow>
                                        <TableCell align="center">No endpoints available</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/service-management')}>
                            Back
                        </Button>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/service-management/service-edit/' + numericServiceId)}>
                            Go to Edit
                        </Button>
                    </Box>

                </StyledInputArea>
            </StyledContainer>
        </>
    )
}

export default ServiceDetailPage