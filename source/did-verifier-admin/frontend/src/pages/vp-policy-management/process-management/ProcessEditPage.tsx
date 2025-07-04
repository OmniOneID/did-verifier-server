import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import { Box, Button, FormControl, FormHelperText, IconButton, InputLabel, MenuItem, Paper, Select, SelectChangeEvent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getProcess, putProcess } from '../../../apis/vp-process-api';
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

const authtypeMapping: { [key: string]: string } = {
    0 : "FREE",
    1 : "NO_AUTHENTICATION",
    2 : "PIN",
    4 : "BIO",
    6 : "PIN or BIO",
    32774 : "PIN and BIO",
};

const eccCurveOptions = [
    { value: "Secp256r1", label: "Secp256r1", disabled: false },
    { value: "Secp256k1", label: "Secp256k1", disabled: true }
];

const cipherOptions = [
    { value: "AES-128-CBC", label: "AES-128-CBC", disabled: true },
    { value: "AES-256-CBC", label: "AES-256-CBC", disabled: false },
    { value: "AES-128-ECB", label: "AES-128-ECB", disabled: true },
    { value: "AES-256-ECB", label: "AES-256-ECB", disabled: true }
];

const paddingOptions = [
    { value: "PKCS5", label: "PKCS5", disabled: false },
    { value: "NOPAD", label: "NOPAD", disabled: true }
];

interface ProcessFormData {
    id: number;
    title: string;
    reqE2e: {
      curve: string;  // EccCurveType      
      cipher: string; // SymmetricCipherType
      padding: string; // SymmetricPaddingType 
    };
    authType: number;
    endpoints: string[];      
    createdAt: string;
}

interface ErrorState {
    title?: string;
    authType?: string;
    reqE2eCurve?: string;
    reqE2eCipher?: string;
    reqE2ePadding?: string;
    endpoints?: string;
}

const ProcessEditPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();

    const processId = id ? parseInt(id, 10) : null;

    const [formData, setFormData] = useState<ProcessFormData>({
        id: 0,
        title: '',
        reqE2e: {
            curve: '',
            cipher: '',
            padding: '',            
        },
        authType: 0,
        endpoints: [],
        createdAt: '',
    });

    const [initialData, setInitialData] = useState<ProcessFormData | null>(null);
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState<ErrorState>({});
    const [newEndpoint, setNewEndpoint] = useState('');

    const handleChange = (field: keyof ProcessFormData) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
        const newValue = event.target.value;
        setFormData((prev) => ({ ...prev, [field]: newValue }));
    };

    const handleE2eChange = (field: keyof ProcessFormData['reqE2e']) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const newValue = event.target.value;
        setFormData((prev) => ({ 
            ...prev, 
            reqE2e: { ...prev.reqE2e, [field]: newValue } 
        }));
    };

    const handleAuthTypeChange = (event: SelectChangeEvent<string>) => {
        const newValue = parseInt(event.target.value, 10);
        setFormData((prev) => ({ ...prev, authType: newValue }));
    };

    const handleReset = () => {
      if (initialData) {
        setFormData(initialData);
        setIsButtonDisabled(true);
        setErrors({});
      }      
    };
    const handleCancel = () => {
        navigate(`/vp-policy-management/process-management/${id}`);        
    };

    const handleAddEndpoint = () => {
      if (newEndpoint.trim() === '') return;
      setFormData((prev) => ({ 
        ...prev, 
        endpoints: [...prev.endpoints, newEndpoint] 
      }));
      setNewEndpoint('');
      
      // Clear the endpoints error when adding an endpoint
      if (errors.endpoints) {
        setErrors(prev => ({ ...prev, endpoints: undefined }));
      }
    };

    const handleRemoveEndpoint = (index: number) => {
      const newEndpoints = [...formData.endpoints];
      newEndpoints.splice(index, 1);
      setFormData((prev) => ({ ...prev, endpoints: newEndpoints }));
      
      // Re-validate endpoints after removal
      if (newEndpoints.length === 0) {
        setErrors(prev => ({ ...prev, endpoints: "Endpoints is required." }));
      }
    };

    const validate = () => {
      let tempErrors: ErrorState = {};

      // Basic validations
      if (!formData.title.trim()) tempErrors.title = "Title is required.";
      else if (formData.title.length > 100) tempErrors.title = "Title must be less than 100 characters.";
      
      if (formData.authType === undefined) tempErrors.authType = "Authentication type is required.";
      
      // ReqE2e validations
      if (!formData.reqE2e.curve.trim()) tempErrors.reqE2eCurve = "Curve is required.";
      if (!formData.reqE2e.cipher.trim()) tempErrors.reqE2eCipher = "Cipher is required.";
      if (!formData.reqE2e.padding.trim()) tempErrors.reqE2ePadding = "Padding is required.";
      
      // Endpoints validation
      if (formData.endpoints.length === 0) tempErrors.endpoints = "Endpoints is required.";

      setErrors(tempErrors);
      return Object.keys(tempErrors).length === 0;
    };

    const handleSubmit = async () => {
      if (!validate()) return;

      const result = await dialogs.open(CustomConfirmDialog, {
          title: 'Confirmation',
          message: 'Are you sure you want to update this Process?',
          isModal: true,
      });

      if (result) {
        setIsLoading(true);

        try {
          const response = await putProcess(formData);
          setIsLoading(false);
          setInitialData(response.data);
          dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Process update completed.',
              isModal: true,
          }, {
              onClose: async () => navigate('/vp-policy-management/process-management'),
          });
        } catch (error) {
          setIsLoading(false);
          dialogs.open(CustomDialog, {
              title: 'Notification',
              message: `Failed to update Process: ${error}`,
              isModal: true,
          });
        }
      }
    };

    useEffect(() => {
      const fetchData = async () => {
          if (processId === null || isNaN(processId)) {
              await dialogs.open(CustomDialog, { 
                  title: 'Notification', 
                  message: 'Invalid Path.', 
                  isModal: true 
              }, {
                  onClose: async () => navigate('/vp-policy-management/process-management', { replace: true }),
              });
              return;
          }

          setIsLoading(true);

          try {
              const { data } = await getProcess(processId);
              setFormData({
                id: data.id,
                title: data.title,
                reqE2e: data.reqE2e || {
                  curve: '',
                  cipher: '',
                  padding: '',
                },
                authType: data.authType,
                endpoints: data.endpoints || [],
                createdAt: data.createdAt,
              });
              setInitialData({
                id: data.id,
                title: data.title,
                reqE2e: data.reqE2e || {
                  curve: '',
                  cipher: '',
                  padding: '',
                },
                authType: data.authType,
                endpoints: data.endpoints || [],
                createdAt: data.createdAt,
              });
              setIsButtonDisabled(true);
              setIsLoading(false);
          } catch (err) {
                console.error('Failed to fetch Process information:', err);
                setIsLoading(false);
                navigate('/error', { state: { message: `Failed to fetch process information: ${err}` } });
          }
      };

      fetchData();
    }, [processId, dialogs, navigate]);

    useEffect(() => {
      if (!initialData) return;
      const isModified = JSON.stringify(formData) !== JSON.stringify(initialData);
      setIsButtonDisabled(!isModified);
    }, [formData, initialData]);

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
        <Typography variant="h4">Process Management</Typography>
        <StyledContainer> 
          <StyledTitle>Process Update</StyledTitle>
          <StyledInputArea>
            <TextField 
                fullWidth
                required
                label="Title" 
                variant="outlined"
                margin="normal" 
                size="small"
                value={formData.title} 
                onChange={handleChange('title')} 
                error={!!errors.title} 
                helperText={errors.title} 
            />

            <FormControl fullWidth margin="normal" error={!!errors.authType}>
                <InputLabel>Auth Type</InputLabel>
                <Select 
                    value={String(formData.authType)} 
                    onChange={handleAuthTypeChange}
                    label="Auth Type"
                >
                    {Object.entries(authtypeMapping).map(([key, value]) => {
                        const isSelectable = key === '6';
                        return (
                            <MenuItem 
                                key={key} 
                                value={key}
                                disabled={!isSelectable}
                                sx={{
                                    color: isSelectable ? 'black' : '#9e9e9e',
                                    '&.Mui-disabled': {
                                        color: '#9e9e9e'
                                    }
                                }}
                            >
                                {value}
                            </MenuItem>
                        );
                    })}
                </Select>
                {errors.authType && <FormHelperText>{errors.authType}</FormHelperText>}
            </FormControl>

            {/* ReqE2e Section */}
            <Typography variant="h6" sx={{ mt: 3 }}>ReqE2e Information</Typography>
                <TableContainer component={Paper} sx={{ mb: 3 }}>
                <Table>
                    <TableHead>
                        <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                            <TableCell>Property</TableCell>
                            <TableCell>Value</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                    <TableRow>
                        <TableCell>Curve</TableCell>
                        <TableCell>
                        <FormControl fullWidth size="small" error={!!errors.reqE2eCurve}>
                            <Select
                            value={formData.reqE2e?.curve || ''}
                            onChange={(e) => handleE2eChange('curve')(e as React.ChangeEvent<HTMLInputElement>)}
                            >
                            {eccCurveOptions.map((option) => (
                                <MenuItem 
                                    key={option.value} 
                                    value={option.value}
                                    disabled={option.disabled}
                                    sx={{
                                        color: option.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                                        '&.Mui-disabled': {
                                            color: 'rgba(0, 0, 0, 0.38)'
                                        }
                                    }}
                                >
                                    {option.label}
                                </MenuItem>
                            ))}
                            </Select>
                            {errors.reqE2eCurve && <FormHelperText>{errors.reqE2eCurve}</FormHelperText>}
                        </FormControl>
                        </TableCell>
                    </TableRow>
                    <TableRow>
                        <TableCell>Cipher</TableCell>
                        <TableCell>
                        <FormControl fullWidth size="small" error={!!errors.reqE2eCipher}>
                            <Select
                            value={formData.reqE2e?.cipher || ''}
                            onChange={(e) => handleE2eChange('cipher')(e as React.ChangeEvent<HTMLInputElement>)}
                            >
                            {cipherOptions.map((option) => (
                                <MenuItem 
                                    key={option.value} 
                                    value={option.value}
                                    disabled={option.disabled}
                                    sx={{
                                        color: option.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                                        '&.Mui-disabled': {
                                            color: 'rgba(0, 0, 0, 0.38)'
                                        }
                                    }}
                                >
                                    {option.label}
                                </MenuItem>
                            ))}
                            </Select>
                            {errors.reqE2eCipher && <FormHelperText>{errors.reqE2eCipher}</FormHelperText>}
                        </FormControl>
                        </TableCell>
                    </TableRow>
                    <TableRow>
                        <TableCell>Padding</TableCell>
                        <TableCell>
                        <FormControl fullWidth size="small" error={!!errors.reqE2ePadding}>
                            <Select
                            value={formData.reqE2e?.padding || ''}
                            onChange={(e) => handleE2eChange('padding')(e as React.ChangeEvent<HTMLInputElement>)}
                            >
                            {paddingOptions.map((option) => (
                                <MenuItem 
                                    key={option.value} 
                                    value={option.value}
                                    disabled={option.disabled}
                                    sx={{
                                        color: option.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                                        '&.Mui-disabled': {
                                            color: 'rgba(0, 0, 0, 0.38)'
                                        }
                                    }}
                                >
                                    {option.label}
                                </MenuItem>
                            ))}
                            </Select>
                            {errors.reqE2ePadding && <FormHelperText>{errors.reqE2ePadding}</FormHelperText>}
                        </FormControl>
                        </TableCell>
                    </TableRow>
                    </TableBody>
                </Table>
                </TableContainer>

            {/* Endpoints Section */}
            <Typography variant="h6" sx={{ mt: 3 }}>Endpoints *</Typography>
            {errors.endpoints && (
                <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                    {errors.endpoints}
                </Typography>
            )}
            <Box sx={{ display: 'flex', mb: 2 }}>
                <TextField 
                    fullWidth
                    required
                    size="small"
                    value={newEndpoint}
                    onChange={(e) => setNewEndpoint(e.target.value)}
                    placeholder="Enter endpoint" 
                    error={!!errors.endpoints}
                />
                <Button 
                    variant="contained" 
                    startIcon={<AddCircleOutlineIcon />} 
                    sx={{ ml: 1 }} 
                    onClick={handleAddEndpoint}
                >
                    Add
                </Button>
            </Box>
            <TableContainer 
                component={Paper}
                sx={{ 
                    border: errors.endpoints ? '1px solid #d32f2f' : 'inherit'
                }}
            >
                <Table>
                    <TableHead>
                        <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                            <TableCell>Endpoint</TableCell>
                            <TableCell>Action</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {formData.endpoints.map((endpoint, index) => (
                            <TableRow key={index}>
                                <TableCell>{endpoint}</TableCell>
                                <TableCell>
                                    <IconButton onClick={() => handleRemoveEndpoint(index)} sx={{ color: '#FF8400' }}>
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                        {formData.endpoints.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={2} align="center">No endpoints available</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
                
            <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 3 }}>
                <Button variant="contained" color="primary" disabled={isButtonDisabled} onClick={handleSubmit}>Update</Button>
                <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
                <Button variant="outlined" color="secondary" onClick={handleCancel}>Cancel</Button>
            </Box>
          </StyledInputArea>
        </StyledContainer>
      </>
    )
}

export default ProcessEditPage