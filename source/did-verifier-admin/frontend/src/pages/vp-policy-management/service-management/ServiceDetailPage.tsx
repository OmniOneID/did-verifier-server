import { useDialogs } from '@toolpad/core';
import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router';
import CustomDialog from '../../../components/dialog/CustomDialog';
import { getService } from '../../../apis/vp-policy-api';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material';

type Props = {}

interface ServiceFormData {
    service: string;
    locked?: boolean;
    device: string;
    mode: string;
    endpoints: string;
}

const ServiceDetailPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();

    const numericServiceId = id ? parseInt(id, 10) : null;
    const [isLoading, setIsLoading] = useState<boolean>(true); 
    const [serviceData, serServiceData] = useState<ServiceFormData>({
        service: '',
        locked: undefined,
        device: '',
        mode: '',
        endpoints: '',
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
                serServiceData({
                    service: data.service,
                    locked: data.locked,
                    device: data.device,
                    mode : data.mode,
                    endpoints : data.endpoints,
                });
                setIsLoading(false);
            } catch (err) {
                  console.error('Failed to fetch Serivce information:', err);
                  setIsLoading(false);
                  navigate('/error', { state: { message: `Failed to namespace information: ${err}` } });
            }
        };

        fetchData();
    }, [numericServiceId]);

    return (
        <>
            <FullscreenLoader open={isLoading} />
            <Box sx={{ p: 3 }}>
                <Typography variant="h4">Service Detail Information</Typography>

                <Box sx={{ maxWidth: 500, margin: 'auto', mt: 2, p: 3, border: '1px solid #ccc', borderRadius: 2 }}>
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
                        <InputLabel>Submittion Mode</InputLabel>
                        <Select 
                            value={serviceData?.mode || ''} 
                            label="Submittion Mode"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            <MenuItem value="Direct">Direct</MenuItem>
                            <MenuItem value="Indirect">inDirect</MenuItem>
                            <MenuItem value="Proxy">Proxy</MenuItem>
                        </Select>
                    </FormControl>

                    <TextField
                        fullWidth
                        label="API Address"
                        value={serviceData?.endpoints || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="contained" color="secondary" onClick={() => navigate('/vp-policy-management/service-management')}>
                            Back
                        </Button>
                        <Button variant="contained" color="primary" onClick={() => navigate('/vp-policy-management/service-management/service-edit/' + numericServiceId)}>
                            Edit
                        </Button>
                    </Box>

                </Box>
            </Box>
        </>
    )
}

export default ServiceDetailPage