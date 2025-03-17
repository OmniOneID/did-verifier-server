import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import { Box, Button, FormControl, FormHelperText, IconButton, InputLabel, MenuItem, Paper, Select, SelectChangeEvent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { postFilter } from '../../../apis/vp-filter-api';
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface FilterFormData {
    title: string;
    id: string;
    type: string;
    requiredClaims: string[];
    allowedIssuers: string[];
    displayClaims: string[];
    presentAll: boolean;
    
}

interface ErrorState {
    title?: string;
    id?: string;
    type?: string;    
    requiredClaims?: string;
    allowedIssuers?: string;
    displayClaims?: string;
    presentAll?: string;
    errorRequiredClaimsMessage?: string;
    errorAllowedIssuersMessage?: string;
    errorDisplayClaimsMessage?: string;
}

const FilterRegistrationPage = (props: Props) => {
    const navigate = useNavigate();
    const dialogs = useDialogs();

    const [formData, setFormData] = useState<FilterFormData>({
        title: '',
        id: '',
        type: '',
        requiredClaims: [],
        allowedIssuers: [],
        displayClaims: [],
        presentAll: false,
 
    });

    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState<ErrorState>({});

    const [newRequiredClaim, setNewRequiredClaim] = useState('');
    const [newAllowedIssuer, setNewAllowedIssuer] = useState('');
    const [newDisplayClaim, setNewDisplayClaim] = useState('');

    const handleChange = (field: keyof FilterFormData) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
        const newValue = event.target.value;
        setFormData((prev) => ({ ...prev, [field]: newValue }));
    };

    const handleReset = () => {
        setErrors({});
        setIsButtonDisabled(true);
        setFormData({
            title: '',
            id: '',
            type: '',
            requiredClaims: [],
            allowedIssuers: [],
            displayClaims: [],
            presentAll: false            
        });
        setNewRequiredClaim('');
        setNewAllowedIssuer('');
        setNewDisplayClaim('');
    };

    const handleAddRequiredClaim = () => {
        if (newRequiredClaim.trim() === '') return;
        setFormData((prev) => ({ 
            ...prev, 
            requiredClaims: [...prev.requiredClaims, newRequiredClaim] 
        }));
        setNewRequiredClaim('');
    };

    const handleRemoveRequiredClaim = (index: number) => {
        const newRequiredClaims = [...formData.requiredClaims];
        newRequiredClaims.splice(index, 1);
        setFormData((prev) => ({ ...prev, requiredClaims: newRequiredClaims }));
    };

    const handleAddAllowedIssuer = () => {
        if (newAllowedIssuer.trim() === '') return;
        setFormData((prev) => ({ 
            ...prev, 
            allowedIssuers: [...prev.allowedIssuers, newAllowedIssuer] 
        }));
        setNewAllowedIssuer('');
    };

    const handleRemoveAllowedIssuer = (index: number) => {
        const newAllowedIssuers = [...formData.allowedIssuers];
        newAllowedIssuers.splice(index, 1);
        setFormData((prev) => ({ ...prev, allowedIssuers: newAllowedIssuers }));
    };

    const handleAddDisplayClaim = () => {
        if (newDisplayClaim.trim() === '') return;
        setFormData((prev) => ({ 
            ...prev, 
            displayClaims: [...prev.displayClaims, newDisplayClaim] 
        }));
        setNewDisplayClaim('');
    };

    const handleRemoveDisplayClaim = (index: number) => {
        const newDisplayClaims = [...formData.displayClaims];
        newDisplayClaims.splice(index, 1);
        setFormData((prev) => ({ ...prev, displayClaims: newDisplayClaims }));
    };

    const validate = () => {
        let tempErrors: ErrorState = {};

        // Basic validations
        if (!formData.title.trim()) tempErrors.title = "Title is required.";
        else if (formData.title.length > 100) tempErrors.title = "Title must be less than 100 characters.";

        if (!formData.id.trim()) tempErrors.id = "ID is required.";
        
        if (!formData.type.trim()) tempErrors.type = "Type is required.";                

        // Array validations
        if (formData.requiredClaims.length === 0) {
            tempErrors.errorRequiredClaimsMessage = "At least one required claim is needed.";
        }

        if (formData.allowedIssuers.length === 0) {
            tempErrors.errorAllowedIssuersMessage = "At least one allowed issuer is needed.";
        }

        setErrors(tempErrors);
        return Object.keys(tempErrors).length === 0;
    };

    const handleSubmit = async () => {
        if (!validate()) return;

        const result = await dialogs.open(CustomConfirmDialog, {
            title: 'Confirmation',
            message: 'Are you sure you want to register this Filter?',
            isModal: true,
        });

        if (result) {
            setIsLoading(true);

            try {
                const response = await postFilter(formData);
                setIsLoading(false);
                dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: 'Filter registration completed.',
                    isModal: true,
                }, {
                    onClose: async () => navigate('/vp-policy-management/filter-management'),
                });
            } catch (error) {
                setIsLoading(false);
                dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: `Failed to register Filter: ${error}`,
                    isModal: true,
                });
            }
        }
    };

    useEffect(() => {
        const isModified = Object.values(formData).some((value) => {
            if (Array.isArray(value)) return value.length > 0;
            return value !== '' && value !== undefined;
        });
        setIsButtonDisabled(!isModified);
    }, [formData]);

    const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
        width: 600,
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
            <Typography variant="h4">Filter Management</Typography>
            <StyledContainer>
                <StyledTitle>Filter Registration</StyledTitle>
                <StyledInputArea>
                    <TextField 
                        fullWidth
                        label="Title" 
                        variant="outlined"
                        margin="normal" 
                        size="small"
                        value={formData.title} 
                        onChange={handleChange('title')} 
                        error={!!errors.title} 
                        helperText={errors.title} 
                    />

                    <TextField 
                        fullWidth
                        label="ID" 
                        variant="outlined"
                        margin="normal" 
                        size="small"
                        value={formData.id} 
                        onChange={handleChange('id')} 
                        error={!!errors.id} 
                        helperText={errors.id} 
                    />

                    <FormControl fullWidth margin="normal" error={!!errors.type}>
                        <InputLabel>Type</InputLabel>
                        <Select 
                            value={formData.type} 
                            onChange={handleChange('type')}
                            label="Type"
                        >
                            <MenuItem value="OsdSchemaCredential">OsdSchemaCredential</MenuItem>                    
                        </Select>
                        {errors.type && <FormHelperText>{errors.type}</FormHelperText>}
                    </FormControl>

                    {/* Required Claims Section */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Required Claims</Typography>
                    {errors.errorRequiredClaimsMessage && (
                        <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                            {errors.errorRequiredClaimsMessage}
                        </Typography>
                    )}
                    <Box sx={{ display: 'flex', mb: 2 }}>
                        <TextField 
                            fullWidth
                            size="small"
                            value={newRequiredClaim}
                            onChange={(e) => setNewRequiredClaim(e.target.value)}
                            placeholder="Enter claim" 
                        />
                        <Button 
                            variant="contained" 
                            startIcon={<AddCircleOutlineIcon />} 
                            sx={{ ml: 1 }} 
                            onClick={handleAddRequiredClaim}
                        >
                            Add
                        </Button>
                    </Box>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Required Claim</TableCell>
                                    <TableCell>Action</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.requiredClaims.map((claim, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{claim}</TableCell>
                                        <TableCell>
                                            <IconButton onClick={() => handleRemoveRequiredClaim(index)} color="error">
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    {/* Display Claims Section */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Display Claims</Typography>
                    <Box sx={{ display: 'flex', mb: 2 }}>
                        <TextField 
                            fullWidth
                            size="small"
                            value={newDisplayClaim}
                            onChange={(e) => setNewDisplayClaim(e.target.value)}
                            placeholder="Enter display claim" 
                        />
                        <Button 
                            variant="contained" 
                            startIcon={<AddCircleOutlineIcon />} 
                            sx={{ ml: 1 }} 
                            onClick={handleAddDisplayClaim}
                        >
                            Add
                        </Button>
                    </Box>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Display Claim</TableCell>
                                    <TableCell>Action</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.displayClaims.map((claim, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{claim}</TableCell>
                                        <TableCell>
                                            <IconButton onClick={() => handleRemoveDisplayClaim(index)} color="error">
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    {/* Allowed Issuers Section */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Allowed Issuers</Typography>
                    {errors.errorAllowedIssuersMessage && (
                        <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                            {errors.errorAllowedIssuersMessage}
                        </Typography>
                    )}
                    <Box sx={{ display: 'flex', mb: 2 }}>
                        <TextField 
                            fullWidth
                            size="small"
                            value={newAllowedIssuer}
                            onChange={(e) => setNewAllowedIssuer(e.target.value)}
                            placeholder="Enter issuer" 
                        />
                        <Button 
                            variant="contained" 
                            startIcon={<AddCircleOutlineIcon />} 
                            sx={{ ml: 1 }} 
                            onClick={handleAddAllowedIssuer}
                        >
                            Add
                        </Button>
                    </Box>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Allowed Issuer</TableCell>
                                    <TableCell>Action</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.allowedIssuers.map((issuer, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{issuer}</TableCell>
                                        <TableCell>
                                            <IconButton onClick={() => handleRemoveAllowedIssuer(index)} color="error">
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>            

                    <FormControl fullWidth margin="normal" error={!!errors.presentAll}>
                        <InputLabel>Present All</InputLabel>
                        <Select 
                            value={formData.presentAll ? "true" : "false"} 
                            onChange={(event) => setFormData((prev) => ({ 
                                ...prev, 
                                presentAll: event.target.value === "true" 
                            }))}
                            label="Present All"
                        >
                            <MenuItem value="true">true</MenuItem>
                            <MenuItem value="false">false</MenuItem>
                        </Select>
                        {errors.presentAll && <FormHelperText>{errors.presentAll}</FormHelperText>}
                    </FormControl>

                    <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 3 }}>
                        <Button variant="contained" color="primary" disabled={isButtonDisabled} onClick={handleSubmit}>Register</Button>
                        <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
                        <Button variant="outlined" color="secondary" onClick={() => navigate('/vp-policy-management/filter-management')}>
                            Back
                        </Button> 
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    );
}

export default FilterRegistrationPage;