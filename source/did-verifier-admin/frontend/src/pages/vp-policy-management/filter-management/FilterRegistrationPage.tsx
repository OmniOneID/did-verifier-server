import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import { 
  Box, 
  Button, 
  Checkbox,
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
  styled,
  CircularProgress
} from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { getVcSchemes, postFilter } from '../../../apis/vp-filter-api';
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
    schemaId?: string; // Add schemaId to store the selected schema
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
        schemaId: ''
    });

    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingClaims, setIsLoadingClaims] = useState(false);
    const [isLoadingSchemas, setIsLoadingSchemas] = useState(false);
    const [errors, setErrors] = useState<ErrorState>({});

    // For the allowed issuers input
    const [newAllowedIssuer, setNewAllowedIssuer] = useState('');

    // For the claims selector dialog
    const [claimDialogOpen, setClaimDialogOpen] = useState(false);
    const [schemaDialogOpen, setSchemaDialogOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [schemaSearchTerm, setSchemaSearchTerm] = useState('');
    
    // Store the entire VC schema data
    const [vcSchemaData, setVcSchemaData] = useState<VcSchema[]>([]);
    const [selectedSchema, setSelectedSchema] = useState<VcSchema | null>(null);
    const [availableClaims, setAvailableClaims] = useState<string[]>([]);
    const [selectedClaims, setSelectedClaims] = useState<{ [key: string]: boolean }>({});

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
        setErrors({});
        setIsButtonDisabled(true);
        setFormData({
            title: '',
            id: '',
            type: '',
            requiredClaims: [],
            allowedIssuers: [],
            displayClaims: [],
            presentAll: false,
            schemaId: ''
        });
        setNewAllowedIssuer('');
        setSelectedClaims({});
        setSelectedSchema(null);
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
        setFormData(prev => ({
            ...prev,
            id: schema.schemaId,
            schemaId: schema.schemaId
        }));
        setSchemaDialogOpen(false);
    };

    // Open the claims selector dialog
    const handleOpenClaimsDialog = async () => {
        if (!selectedSchema) {
            await dialogs.open(CustomDialog, {
                title: 'Notification',
                message: 'Please select a VC schema first.',
                isModal: true,
            });
            return;
        }
        
        setIsLoadingClaims(true);
        // Extract claims from the selected schema
        const claims = extractClaimsFromSchema(selectedSchema);
        setAvailableClaims(claims);
        
        // Reset selected claims each time the dialog opens
        setSelectedClaims({});
        setIsLoadingClaims(false);
        setClaimDialogOpen(true);
    };

    // Handle search in the claims dialog
    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
    };

    // Handle search in the schema dialog
    const handleSchemaSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSchemaSearchTerm(e.target.value);
    };

    // Handle claim selection/deselection
    const handleClaimToggle = (claim: string) => {
        setSelectedClaims(prev => ({
            ...prev,
            [claim]: !prev[claim]
        }));
    };

    // Confirm claim selections
    const handleConfirmClaimSelection = () => {
        const selectedClaimValues = availableClaims
            .filter(claim => selectedClaims[claim])
            .map(claim => claim);
        
        // Update requiredClaims
        setFormData(prev => ({
            ...prev,
            requiredClaims: selectedClaimValues,
            // Also update displayClaims with the same values
            displayClaims: selectedClaimValues
        }));
        setClaimDialogOpen(false);
    };

    const handleRemoveRequiredClaim = (index: number) => {
        const newRequiredClaims = [...formData.requiredClaims];
        const removedClaim = newRequiredClaims[index];
        newRequiredClaims.splice(index, 1);
        
        // 필요값을 삭제하면 노출값에서도 함께 삭제합니다
        setFormData((prev) => ({ 
            ...prev, 
            requiredClaims: newRequiredClaims,
            displayClaims: prev.displayClaims.filter(claim => claim !== removedClaim)
        }));
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

    const handleRemoveDisplayClaim = (index: number) => {
        const newDisplayClaims = [...formData.displayClaims];
        newDisplayClaims.splice(index, 1);
        
        // 노출값만 삭제하고 필요값은 그대로 유지합니다
        setFormData((prev) => {
            return {
                ...prev, 
                displayClaims: newDisplayClaims
                // requiredClaims는 변경하지 않음
            };
        });
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
                // Remove schemaId from the data before submission if needed
                const { schemaId, ...dataToSubmit } = formData;
                
                await postFilter(dataToSubmit);
                setIsLoading(false);
                await dialogs.open(CustomDialog, {
                    title: 'Notification',
                    message: 'Filter registration completed.',
                    isModal: true,
                }, {
                    onClose: async () => navigate('/vp-policy-management/filter-management'),
                });
            } catch (error) {
                setIsLoading(false);
                await dialogs.open(CustomDialog, {
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
            return value !== '' && value !== false && value !== undefined;
        });
        setIsButtonDisabled(!isModified);
    }, [formData]);

    // Filter claims based on search term
    const filteredClaims = useMemo(() => {
        return searchTerm.trim() === '' 
            ? availableClaims 
            : availableClaims.filter(claim => 
                claim.toLowerCase().includes(searchTerm.toLowerCase())
            );
    }, [availableClaims, searchTerm]);

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

            {/* Claims Selector Dialog */}
            <Dialog 
                open={claimDialogOpen} 
                onClose={() => setClaimDialogOpen(false)} 
                maxWidth="md" 
                fullWidth
            >
                <DialogTitle>Select Required Claims</DialogTitle>
                <DialogContent>
                    <Box sx={{ mb: 2, mt: 1, display: 'flex' }}>
                        <TextField 
                            fullWidth
                            label="Search Claims"
                            value={searchTerm}
                            onChange={handleSearchChange}
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
                                    <TableCell>Claim Name</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {isLoadingClaims ? (
                                    <TableRow>
                                        <TableCell colSpan={2} align="center" sx={{ py: 3 }}>
                                            <CircularProgress size={32} />
                                            <Typography variant="body2" sx={{ mt: 1 }}>
                                                Loading claims...
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : filteredClaims.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={2} align="center">
                                            <Typography variant="body1">No claims found</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    filteredClaims.map(claim => (
                                        <TableRow key={claim}>
                                            <TableCell padding="checkbox">
                                                <Checkbox 
                                                    checked={!!selectedClaims[claim]} 
                                                    onChange={() => handleClaimToggle(claim)}
                                                />
                                            </TableCell>
                                            <TableCell>{claim}</TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button onClick={() => setClaimDialogOpen(false)} variant="outlined">
                        Cancel
                    </Button>
                    <Button 
                        onClick={handleConfirmClaimSelection} 
                        variant="contained" 
                        color="primary"
                    >
                        Confirm
                    </Button>
                </DialogActions>
            </Dialog>

            <Typography variant="h4">Filter Management</Typography>
            <StyledContainer>
                <StyledTitle>Filter Registration</StyledTitle>
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
                            InputProps={{ readOnly: true }}
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

                    {/* Required Claims Section - New Implementation */}
                    <Typography variant="h6" sx={{ mt: 3 }}>Required Claims</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        Please enter claims through VC schema search.
                    </Typography>
                    {errors.errorRequiredClaimsMessage && (
                        <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                            {errors.errorRequiredClaimsMessage}
                        </Typography>
                    )}
                    <Box sx={{ display: 'flex', mb: 2 }}>
                        <Button 
                            fullWidth
                            variant="contained" 
                            startIcon={<AddCircleOutlineIcon />}
                            onClick={handleOpenClaimsDialog}
                            disabled={isLoadingClaims || !selectedSchema}
                        >
                            {isLoadingClaims ? 'Loading Claims...' : 'Add Required Claims'}
                        </Button>
                    </Box>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Required Claim</TableCell>
                                    <TableCell>Delete</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.requiredClaims.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={2} align="center">
                                            <Typography variant="body2">No required claims added</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    formData.requiredClaims.map((claim, index) => (
                                        <TableRow key={index}>
                                            <TableCell>{claim}</TableCell>
                                            <TableCell>
                                                <IconButton onClick={() => handleRemoveRequiredClaim(index)} sx={{ color: '#FF8400' }}>
                                                    <DeleteIcon />
                                                </IconButton>
                                            </TableCell>
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
                                    <TableCell>Delete</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {formData.displayClaims.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={2} align="center">
                                            <Typography variant="body2">No display claims added</Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    formData.displayClaims.map((claim, index) => (
                                        <TableRow key={index}>
                                            <TableCell>{claim}</TableCell>
                                            <TableCell>
                                                <IconButton onClick={() => handleRemoveDisplayClaim(index)} sx={{ color: '#FF8400' }}>
                                                    <DeleteIcon />
                                                </IconButton>
                                            </TableCell>
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
                            Cancel
                        </Button> 
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    );
}

export default FilterRegistrationPage;