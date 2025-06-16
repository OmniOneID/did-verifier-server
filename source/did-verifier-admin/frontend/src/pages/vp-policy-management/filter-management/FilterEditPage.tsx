import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import SearchIcon from '@mui/icons-material/Search';
import { 
  Box, 
  Button, 
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl, 
  FormHelperText, 
  IconButton, 
  InputLabel, 
  MenuItem, 
  Paper, 
  Radio,
  Select, 
  SelectChangeEvent, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  TextField, 
  Typography, 
  styled
} from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getFilter, putFilter, getVcSchemes } from '../../../apis/vp-filter-api';
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import DeleteIcon from "@mui/icons-material/Delete";

type Props = {}

interface FilterFormData {
    filterId: number;
    title: string;
    id: string;
    type: string;
    requiredClaims: string[];
    allowedIssuers: string[];
    displayClaims: string[];    
    presentAll: boolean;
    value: string;
    createdAt: string;
    schemaId?: string; // Add schemaId to store the selected schema
}

interface ErrorState {
    filterId?: string;
    title?: string;
    id?: string;
    type?: string;
    value?: string;
    requiredClaims?: string;
    allowedIssuers?: string;
    displayClaims?: string;
    presentAll?: string;
    errorRequiredClaimsMessage?: string;
    errorAllowedIssuersMessage?: string;
    errorDisplayClaimsMessage?: string;
}

// Interfaces for VcSchema data
interface ClaimItem {
    caption: string;
    format: string;
    hideValue: boolean;
    id: string;
    type: string;
}

interface Namespace {
    id: string;
    name: string;
    ref: string;
}

interface Claim {
    items: ClaimItem[];
    namespace: Namespace;
}

interface VcSchemaDetail {
    credentialSubject: {
        claims: Claim[];
    };
    description: string;
    metadata: {
        formatVersion: string;
        language: string;
    };
    title: string;
}

interface VcSchema {
    schemaId: string;
    issuerDid: string;
    issuerName: string;
    title: string;
    description: string;
    vcSchema: VcSchemaDetail;
}

interface VcSchemaResponse {
    count: number;
    vcSchemaList: VcSchema[];
}

const FilterEditPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();

    const numericFilterId = id ? parseInt(id, 10) : null;

    const [formData, setFormData] = useState<FilterFormData>({
        filterId: 0,
        title: '',
        id: '',
        type: '',
        requiredClaims: [],
        allowedIssuers: [],
        displayClaims: [],        
        presentAll: true,
        value: '',
        createdAt: '',
        schemaId: ''
    });

    const [initialData, setInitialData] = useState<FilterFormData | null>(null);
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingSchemas, setIsLoadingSchemas] = useState(false);
    const [errors, setErrors] = useState<ErrorState>({});

    // For the allowed issuers input
    const [newAllowedIssuer, setNewAllowedIssuer] = useState('');

    // For the schema selector dialog
    const [schemaDialogOpen, setSchemaDialogOpen] = useState(false);
    const [schemaSearchTerm, setSchemaSearchTerm] = useState('');
    
    // Store the entire VC schema data
    const [vcSchemaData, setVcSchemaData] = useState<VcSchema[]>([]);
    const [selectedSchema, setSelectedSchema] = useState<VcSchema | null>(null);

    // Fetch VC schemas
    const fetchVcSchemas = async () => {
        try {
            setIsLoadingSchemas(true);
            const response = await getVcSchemes();
            console.log('Fetched VC schemas:', response);
            
            if (response && response.data) {
                const data = response.data as VcSchemaResponse;
                setVcSchemaData(data.vcSchemaList || []);
            }
            setIsLoadingSchemas(false);
        } catch (error) {
            console.error('Failed to fetch VC schemas:', error);
            dialogs.open(CustomDialog, {
                title: 'Error',
                message: 'Failed to load VC schemas. Please try again.',
                isModal: true,
            });
            setIsLoadingSchemas(false);
        }
    };

    // Extract available claims from a selected schema
    const extractClaimsFromSchema = (schema: VcSchema | null): string[] => {
        if (!schema) return [];
        
        const claims: string[] = [];
        
        try {
            const vcClaims = schema.vcSchema.credentialSubject.claims;
            
            vcClaims.forEach(claim => {
                claim.items.forEach(item => {
                    // Create a fully qualified claim ID with namespace
                    const claimId = `${claim.namespace.id}.${item.id}`;
                    claims.push(claimId);
                });
            });
        } catch (error) {
            console.error('Error extracting claims from schema:', error);
        }
        
        return claims;
    };

    const handleChange = (field: keyof FilterFormData) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
        const newValue = event.target.value;
        setFormData((prev) => ({ ...prev, [field]: newValue }));
    };

    const handleReset = () => {
        if (initialData) {
            setFormData(initialData);
            setIsButtonDisabled(true);
            
            // Also reset the selected schema if needed
            if (initialData.schemaId) {
                const schema = vcSchemaData.find(s => s.schemaId === initialData.schemaId);
                if (schema) {
                    setSelectedSchema(schema);
                }
            } else {
                setSelectedSchema(null);
            }
        }
    };

    const handleCancel = () => {
        navigate(`/vp-policy-management/filter-management/${id}`);        
    };

    // Open the schema selector dialog
    const handleOpenSchemaDialog = async () => {
        if (vcSchemaData.length === 0) {
            await fetchVcSchemas();
        }
        setSchemaSearchTerm('');
        setSchemaDialogOpen(true);
    };

    // Handle schema selection
    const handleSchemaSelect = (schema: VcSchema) => {
        setSelectedSchema(schema);
        
        // Extract all claims from the selected schema
        const allClaims = extractClaimsFromSchema(schema);
        
        setFormData(prev => ({
            ...prev,
            id: schema.schemaId,
            schemaId: schema.schemaId,
            // Automatically populate both required and display claims with all available claims
            requiredClaims: allClaims,
            displayClaims: allClaims
        }));
        setSchemaDialogOpen(false);
    };

    // Handle search in the schema dialog
    const handleSchemaSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSchemaSearchTerm(e.target.value);
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
            message: 'Are you sure you want to update this Filter?',
            isModal: true,
        });

        if (result) {
            setIsLoading(true);

            try {
                // Remove schemaId from the data before submission if needed
                const { schemaId, ...dataToSubmit } = formData;
                
                // API 호출 전에 먼저 initialData를 업데이트하여 화면 깜빡임 방지
                setInitialData({...formData});
                
                const response = await putFilter(dataToSubmit);
                setIsLoading(false);
                await dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: 'Filter update completed.',
                    isModal: true,
                }, {
                    onClose: async () => navigate('/vp-policy-management/filter-management'),
                });
            } catch (error) {
                setIsLoading(false);
                // 에러 발생 시 initialData를 원래대로 복원
                if (initialData) {
                    setInitialData(initialData);
                }
                await dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: `Failed to update Filter: ${error}`,
                    isModal: true,
                });
            }
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            if (numericFilterId === null || isNaN(numericFilterId)) {
                await dialogs.open(CustomDialog, { 
                    title: 'Notification', 
                    message: 'Invalid Path.', 
                    isModal: true 
                }, {
                    onClose: async () => navigate('/vp-policy-management/filter-management', { replace: true }),
                });
                return;
            }

            setIsLoading(true);

            try {
                const { data } = await getFilter(numericFilterId);
                // Force presentAll to be true regardless of the data from server
                const modifiedData = { ...data, presentAll: true };
                setFormData(modifiedData);
                setInitialData(modifiedData);
                setIsButtonDisabled(true);
                setIsLoading(false);
                
                // Optionally pre-fetch VC schemas
                await fetchVcSchemas();
            } catch (err) {
                console.error('Failed to fetch Filter information:', err);
                setIsLoading(false);
                navigate('/error', { state: { message: `Failed to fetch filter information: ${err}` } });
            }
        };

        fetchData();
    }, [numericFilterId, dialogs, navigate]);

    useEffect(() => {
        if (!initialData) return;
        const isModified = JSON.stringify(formData) !== JSON.stringify(initialData);
        setIsButtonDisabled(!isModified);
    }, [formData, initialData]);

    // Filter schemas based on search term
    const filteredSchemas = useMemo(() => {
        return schemaSearchTerm.trim() === ''
            ? vcSchemaData
            : vcSchemaData.filter(schema => 
                schema.title.toLowerCase().includes(schemaSearchTerm.toLowerCase()) ||
                schema.schemaId.toLowerCase().includes(schemaSearchTerm.toLowerCase())
            );
    }, [vcSchemaData, schemaSearchTerm]);

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

            {/* VC Schema Selector Dialog */}
            <Dialog 
                open={schemaDialogOpen} 
                onClose={() => setSchemaDialogOpen(false)} 
                maxWidth="md" 
                fullWidth
            >
                <DialogTitle>Select VC Schema</DialogTitle>
                <DialogContent>
                    <Box sx={{ mb: 2, mt: 1, display: 'flex' }}>
                        <TextField 
                            fullWidth
                            label="Search Schemas"
                            value={schemaSearchTerm}
                            onChange={handleSchemaSearchChange}
                            variant="outlined"
                            size="small"
                        />
                        <IconButton sx={{ ml: 1 }}>
                            <SearchIcon />
                        </IconButton>
                    </Box>
                    <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                        <Table stickyHeader>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                                    <TableCell padding="checkbox" width="10%">Select</TableCell>
                                    <TableCell>Title</TableCell>
                                    <TableCell>Schema ID</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {isLoadingSchemas ? (
                                    <TableRow>
                                        <TableCell colSpan={3} align="center" sx={{ py: 3 }}>
                                            <CircularProgress size={32} />
                                            <Typography variant="body2" sx={{ mt: 1 }}>
                                                Loading schemas...
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : filteredSchemas.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={3} align="center">
                                            <Typography variant="body1">No schemas found</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    filteredSchemas.map(schema => (
                                        <TableRow key={schema.schemaId}>
                                            <TableCell padding="checkbox">
                                                <Radio 
                                                    checked={selectedSchema?.schemaId === schema.schemaId} 
                                                    onChange={() => setSelectedSchema(schema)}
                                                />
                                            </TableCell>
                                            <TableCell>{schema.title}</TableCell>
                                            <TableCell>{schema.schemaId}</TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={() => setSchemaDialogOpen(false)} variant="outlined">
                        Cancel
                    </Button>
                    <Button 
                        onClick={() => selectedSchema && handleSchemaSelect(selectedSchema)} 
                        variant="contained" 
                        color="primary"
                        disabled={!selectedSchema}
                    >
                        Select
                    </Button>
                </DialogActions>
            </Dialog>

            <Typography variant="h4">Filter Management</Typography>
            <StyledContainer>
                <StyledTitle>Filter Edit</StyledTitle>
                <StyledInputArea>
                    <TextField 
                        fullWidth
                        label="Title" 
                        required
                        variant="outlined"
                        margin="normal" 
                        size="small"
                        value={formData.title} 
                        onChange={handleChange('title')} 
                        error={!!errors.title} 
                        helperText={errors.title} 
                    />

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <TextField 
                            fullWidth
                            label="ID" 
                            required
                            variant="outlined"
                            margin="normal" 
                            size="small"
                            value={formData.id} 
                            onChange={handleChange('id')} 
                            error={!!errors.id} 
                            helperText={errors.id} 
                        />
                        <Button 
                            variant="contained" 
                            size="small"
                            onClick={handleOpenSchemaDialog}
                            sx={{ mt: 1, height: 40 }}
                        >
                            Search
                        </Button>
                    </Box>

                    <FormControl fullWidth margin="normal" error={!!errors.type}>
                        <InputLabel>Type *</InputLabel>
                        <Select 
                            value={formData.type} 
                            onChange={handleChange('type')}
                            label="Type"
                            required
                        >
                            <MenuItem value="OsdSchemaCredential">OsdSchemaCredential</MenuItem>                    
                        </Select>
                        {errors.type && <FormHelperText>{errors.type}</FormHelperText>}
                    </FormControl>

                    <FormControl fullWidth margin="normal" error={!!errors.presentAll}>
                        <InputLabel>Present All</InputLabel>
                        <Select 
                            value="true"
                            label="Present All"
                            disabled
                        >
                            <MenuItem value="true">true</MenuItem>
                        </Select>
                        {errors.presentAll && <FormHelperText>{errors.presentAll}</FormHelperText>}
                    </FormControl>

                    {/* Required Claims Section - New Implementation */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Required Claims</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        Claims are automatically loaded when you select a VC schema.
                    </Typography>
                    {errors.errorRequiredClaimsMessage && (
                        <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                            {errors.errorRequiredClaimsMessage}
                        </Typography>
                    )}
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Required Claim</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.requiredClaims.length === 0 ? (
                                    <TableRow>
                                        <TableCell align="center">
                                            <Typography variant="body2">No required claims added</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    formData.requiredClaims.map((claim, index) => (
                                        <TableRow key={index}>
                                            <TableCell>{claim}</TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    {/* Display Claims Section - Now auto-populated but can be deleted separately */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Display Claims</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Display Claim</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.displayClaims.length === 0 ? (
                                    <TableRow>
                                        <TableCell align="center">
                                            <Typography variant="body2">No display claims added</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    formData.displayClaims.map((claim, index) => (
                                        <TableRow key={index}>
                                            <TableCell>{claim}</TableCell>
                                        </TableRow>
                                    ))
                                )}
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
                                    <TableCell>Delete</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.allowedIssuers.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={2} align="center">
                                            <Typography variant="body2">No allowed issuers added</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    formData.allowedIssuers.map((issuer, index) => (
                                        <TableRow key={index}>
                                            <TableCell>{issuer}</TableCell>
                                            <TableCell>
                                                <IconButton onClick={() => handleRemoveAllowedIssuer(index)} sx={{ color: '#FF8400' }}>
                                                    <DeleteIcon />
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    ))
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
    );
}

export default FilterEditPage;