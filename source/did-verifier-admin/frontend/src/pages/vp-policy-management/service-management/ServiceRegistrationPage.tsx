import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import { Box, Button, FormControl, FormHelperText, IconButton, InputLabel, MenuItem, Paper, Select, SelectChangeEvent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { postService } from "../../../apis/vp-payload-api";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import { ipRegex, urlRegex } from '../../../utils/regex';

type Props = {}

interface ServiceFormData {
    service: string;
    locked?: boolean;
    device: string;
    mode: string;
    endpoints: string[];
}

interface ErrorState {
    service?: string;
    locked?: string;
    device?: string;
    mode?: string;
    endpoints?: string[];
    errorEndpointsMessage?: string;
}

const ServiceRegistrationPage = (props: Props) => {
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const [formData, setFormData] = useState<ServiceFormData>({
        service: '',
        locked: undefined,
        device: '',
        mode: '',
        endpoints: [],
    });
    const [errors, setErrors] = useState<ErrorState>({});
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (field: keyof ServiceFormData) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
            const newValue = event.target.value;
            setFormData((prev) => ({ ...prev, [field]: newValue }));
    };

    const handleEndpointChange = (index: number, event: React.ChangeEvent<HTMLInputElement>) => {
        const newEndpoints = [...formData.endpoints];
        newEndpoints[index] = event.target.value;
        setFormData((prev) => ({ ...prev, endpoints: newEndpoints }));
    };

    const handleAddEndpoint = () => {
        setFormData((prev) => ({ ...prev, endpoints: [...prev.endpoints, ''] }));
    };

    const handleRemoveEndpoint = (index: number) => {
        const newEndpoints = [...formData.endpoints];
        newEndpoints.splice(index, 1);
        setFormData((prev) => ({ ...prev, endpoints: newEndpoints }));
    };

    const handleReset = () => {
        setErrors({});
        setIsButtonDisabled(true);
        setFormData({ service: '', locked: undefined, device: '', mode: '', endpoints: [] });
    };

    const handleSubmit = async () => {
        if (!validate()) return;

        const result = await dialogs.open(CustomConfirmDialog, {
            title: 'Confirmation',
            message: 'Are you sure you want to register Service?',
            isModal: true,
        });

        if (result) {
            setIsLoading(true);

            let requestObject = {
                service: formData.service,
                locked: formData.locked,
                device: formData.device,
                mode: formData.mode,
                endpoints: JSON.stringify(formData.endpoints),
                validSecond: 180
            }

            await postService(requestObject).then((response) => {
                setIsLoading(false);
                dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: 'Service registration completed.',
                    isModal: true,
                },{
                    onClose: async (result) =>  navigate('/vp-policy-management/service-management'),
                });
    
            }).catch((error) => {
                setIsLoading(false);
                dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: `Failed to register Service: ${error}`,
                    isModal: true,
                });
            });
        }
    };

    const validate = () => {
        let tempErrors: ErrorState = {};

        tempErrors.service = validateService(formData.service);
        tempErrors.locked = validateLocked(formData.locked);
        tempErrors.device = validateDevice(formData.device);
        tempErrors.mode = validateMode(formData.mode);

        if (formData.endpoints.length === 0) {
            tempErrors.errorEndpointsMessage = "At least one endpoint is required.";
        } else {
            const seen = new Set<string>();
            const duplicateIndices: number[] = [];

            formData.endpoints.forEach((value, index) => {
                if (seen.has(value) && value.trim() !== "") {
                    duplicateIndices.push(index);
                }
                seen.add(value);
            });

            const endpointErrors = formData.endpoints.map((item, index) => {
                if (!item.trim()) return "Endpoint is required.";
                if (duplicateIndices.includes(index)) return "Duplicate endpoint is not allowed.";
                if (!urlRegex.test(item) && !ipRegex.test(item)) return "Invalid endpoint format.";
                return "";
            });

            tempErrors.endpoints = endpointErrors.every(err => err === "") ? undefined : endpointErrors;
        }

        setErrors(tempErrors);
        return Object.values(tempErrors).every((error) => !error);
    };

    const validateService = (serivce?: string): string | undefined => {
        if (!serivce) return 'Please enter a serivce.';
        if (serivce.length > 50) return 'Service name must be less than 50 characters.';
        return undefined;
    };

    const validateLocked = (locked?: boolean): string | undefined => {
        if (locked === undefined) return 'Please select a lock status.';
        return undefined;
    };

    const validateDevice = (device?: string): string | undefined => {
        if (!device) return 'Please enter a device.';
        if (device.length > 50) return 'Device name must be less than 50 characters.';
        return undefined;
    };

    const validateMode = (mode?: string): string | undefined => {
        if (!mode) return 'Please select a submission mode.';
        return undefined;
    }; 

    const validateItem = (item: string): { endpoint?: string } => {
        let itemErrors: { endpoint?: string } = {};
    
        if (!item.trim()) itemErrors.endpoint = "Endpoint is required.";
        else if (!urlRegex.test(item) && !ipRegex.test(item)) itemErrors.endpoint = "Invalid endpoint format.";
    
        return itemErrors;
    };

    useEffect(() => {
        const isModified = Object.values(formData).some((value) => {
            if (Array.isArray(value)) return value.length > 0;
            return value !== '' && value !== undefined;
        });
        setIsButtonDisabled(!isModified);
    }, [formData]);

    const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
        width: 500,
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
            <Typography variant="h4">Service Management</Typography>
            <StyledContainer>
                <StyledTitle>Service Registration</StyledTitle>
                <StyledInputArea>
                    <TextField 
                        fullWidth
                        label="Service" 
                        variant="outlined"
                        margin="normal" 
                        size="small"
                        value={formData.service} 
                        onChange={handleChange('service')} 
                        error={!!errors.service} 
                        helperText={errors.service} 
                    />

                    <FormControl fullWidth margin="normal" error={!!errors.locked}>
                        <InputLabel>Lock Status</InputLabel>
                        <Select 
                           value={formData.locked === undefined ? "" : String(formData.locked)}
                            onChange={(event) => setFormData((prev) => ({
                                ...prev, 
                                locked: event.target.value === "true"
                            }))}  
                            label="Lock Status"
                        >
                            <MenuItem value={"true"}>Locked</MenuItem>
                            <MenuItem value={"false"}>Unlocked</MenuItem>
                        </Select>
                        {errors.locked && <FormHelperText>{errors.locked}</FormHelperText>}
                    </FormControl>

                    <TextField 
                        fullWidth
                        label="Device" 
                        variant="outlined"
                        margin="normal" 
                        size="small"
                        value={formData.device} 
                        onChange={handleChange('device')} 
                        error={!!errors.device} 
                        helperText={errors.device} 
                    />

                    <FormControl fullWidth margin="normal" error={!!errors.mode}>
                        <InputLabel>Submission Mode</InputLabel>
                        <Select 
                            value={formData.mode} 
                            onChange={handleChange('mode')}
                            label="Submission Mode"
                        >
                            <MenuItem value="Direct">Direct</MenuItem>
                            <MenuItem value="Indirect">inDirect</MenuItem>
                            <MenuItem value="Proxy">Proxy</MenuItem>
                        </Select>
                        {errors.mode && <FormHelperText>{errors.mode}</FormHelperText>}
                    </FormControl>
                    
                    <Typography variant="h6" sx={{ mt: 3 }}>Endpoints</Typography>
                    {errors.errorEndpointsMessage && (
                        <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>{errors.errorEndpointsMessage}</Typography>
                    )}
                    <Button variant="contained" startIcon={<AddCircleOutlineIcon />} sx={{ mt: 2, mb: 2 }} onClick={handleAddEndpoint}>
                        Add Endpoint
                    </Button>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell sx={{ width: '80%' }}>API Address</TableCell>
                                    <TableCell>Delete</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.endpoints.map((endpoint, index) => (
                                    <TableRow key={index}>
                                        <TableCell sx={{ verticalAlign: 'top', width: '80%' }}>
                                            <TextField fullWidth size="small" value={endpoint} onChange={(event) => handleEndpointChange(index, event)} error={!!errors.endpoints?.[index]} helperText={errors.endpoints?.[index]} />
                                        </TableCell>
                                        <TableCell sx={{ verticalAlign: 'top', width: '20%', textAlign: 'center' }}>
                                            <IconButton onClick={() => handleRemoveEndpoint(index)} sx={{ color: '#FF8400' }}>
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="contained" color="primary" onClick={handleSubmit} disabled={isButtonDisabled}>Register</Button>
                        <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
                        <Button variant="outlined" color="secondary" onClick={() => navigate('/vp-policy-management/service-management')}>Back</Button>                        
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    );
}

export default ServiceRegistrationPage;
