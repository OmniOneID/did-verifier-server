import {
  Box, Button, IconButton, MenuItem, Paper, Select, SelectChangeEvent,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography, useTheme, FormControl, InputLabel, styled,
  FormHelperText
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import DeleteIcon from "@mui/icons-material/Delete";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDialog from "../../../components/dialog/CustomDialog";
import CustomConfirmDialog from "../../../components/dialog/CustomConfirmDialog";
import AttributeSelectDialog from "./AttributeSelectDialog";
import PredicateSelectDialog from "./PredicateSelectDialog";
import { useDialogs } from "@toolpad/core";
import { curveTypes } from "../../../constants/curve-types";
import { cipherTypes } from "../../../constants/cipher-types";
import { paddingTypes } from "../../../constants/padding-types";
import { postProofRequest, verifyNameUnique } from "../../../apis/zkp-proof-api";

interface AttributeItem {
  attributeName: string;
  definitionId: string;
}

interface PredicateItem {
  attributeName: string;
  predicateType: string;
  predicateValue: string;
  definitionId: string[];
}

interface AttributeDialogResult {
  attributeName: string;
  label: string;
  type: string;
  definitionId: string;
  namespaceIdentifier: string;
}

interface PredicateDialogResult {
  attributeName: string;
  label: string;
  type: string;
  predicateType: string;
  predicateValue: string;
  definitionId: string;
  namespaceIdentifier: string;
}

interface FormData {
  name: string;
  version: string;
  curve: string;
  cipher: string;
  padding: string;
  attributes: AttributeItem[];
  predicates: PredicateItem[];
}

interface ErrorState {
  name?: string;
  version?: string;
  curve?: string;
  cipher?: string;
  padding?: string;
  attributesOrPredicates?: string;
}

const ProofRequestConfigurationRegistrationPage = () => {
  const theme = useTheme();
  const dialogs = useDialogs();
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>({
    name: "",
    version: "",
    curve: "Secp256r1",
    cipher: "AES-256-CBC",
    padding: "PKCS5",
    attributes: [],
    predicates: [],
  });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<ErrorState>({});
  const [isNameIsValid, setIsNameValid] = useState(false);
  const [nameCheckMessage, setNameCheckMessage] = useState<string>('');
  const [nameCheckStatus, setNameCheckStatus] = useState<'success' | 'error' | ''>('');

  const handleChange = (field: keyof FormData) =>
  (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));

    if (field in errors) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    if (field === "name") {
      setIsNameValid(false);
      setErrors((prev) => ({ ...prev, name: undefined }));
      setNameCheckMessage('');
      setNameCheckStatus('');
    }
  };

  const handleOpenAttributeDialog = async () => {
    const result = await dialogs.open(AttributeSelectDialog, []) as AttributeDialogResult[];
    if (!result || !Array.isArray(result)) return;

    const updatedAttributes = [
      ...formData.attributes.filter(
        existing => !result.some(newItem => newItem.attributeName === existing.attributeName)
      ),
      ...result.map(attr => ({
        attributeName: attr.attributeName,
        definitionId: attr.definitionId,
      }))
    ];

    setFormData(prev => ({
      ...prev,
      attributes: updatedAttributes,
    }));

    if (updatedAttributes.length > 0) {
      setErrors(prev => ({ ...prev, attributesOrPredicates: undefined }));
    }
  };

  const handleOpenPredicateDialog = async () => {
    const result = await dialogs.open(PredicateSelectDialog, []) as PredicateDialogResult[];
    if (!result || !Array.isArray(result)) return;

    const expanded = result.flatMap(attr => ({
      attributeName: attr.attributeName,
      predicateType: attr.predicateType,
      predicateValue: attr.predicateValue,
      definitionId: [attr.definitionId],
    }));

    const updatedPredicates = [
      ...formData.predicates.filter(
        existing => !result.some(newItem => newItem.attributeName === existing.attributeName)
      ),
      ...expanded
    ];

    setFormData(prev => ({
      ...prev,
      predicates: updatedPredicates,
    }));
    
    if (updatedPredicates.length > 0) {
      setErrors(prev => ({ ...prev, attributesOrPredicates: undefined }));
    }
  };

  const validate = (): boolean => {
    const tempErrors: ErrorState = {};
    
    // Name validation
    if (!formData.name.trim()) {
      tempErrors.name = "Name is required";
    } else if (formData.name.length < 4 || formData.name.length > 40) {
      tempErrors.name = "Name must be between 4 and 40 characters";
    } else if (!isNameIsValid) {
      tempErrors.name = "Please check name availability.";
    }
    
    // Version validation
    if (!formData.version.trim()) {
      tempErrors.version = "Version is required";
    } else if (formData.version.length < 1 || formData.version.length > 10) {
      tempErrors.version = "Version must be between 1 and 10 characters";
    }
    
    // Curve validation
    if (!formData.curve) {
      tempErrors.curve = "Curve is required";
    }
    
    // Cipher validation
    if (!formData.cipher) {
      tempErrors.curve = "Cipher is required";
    }
    
    // Padding validation
    if (!formData.padding) {
      tempErrors.padding = "Padding is required";
    }
    
    // Attributes or Predicates validation
    if (formData.attributes.length === 0 && formData.predicates.length === 0) {
      tempErrors.attributesOrPredicates = "At least one attribute or predicate is required";
    }
    
    setErrors(tempErrors);
    
    // Return true if there are no errors
    return Object.keys(tempErrors).length === 0;
  };
  
  const prepareRequestData = () => {
    const requestData = {
      name: formData.name,
      version: formData.version,
      curve: formData.curve,
      cipher: formData.cipher,
      padding: formData.padding,
      requestedAttributes: {} as Record<string, any>,
      requestedPredicates: {} as Record<string, any>,
    };

    const attributeGroups = formData.attributes.reduce((acc, item) => {
      if (!acc[item.attributeName]) {
        acc[item.attributeName] = [];
      }
      acc[item.attributeName].push(item.definitionId);
      return acc;
    }, {} as Record<string, string[]>);

    let attributeCounter = 1;
    Object.entries(attributeGroups).forEach(([attributeName, definitionIds]) => {
      const restrictions = definitionIds.map(id => ({ credDefId: id }));
      
      requestData.requestedAttributes[`attributeReferent${attributeCounter}`] = {
        name: attributeName,
        restrictions: restrictions,
      };
      
      attributeCounter++;
    });

    const predicateGroups = formData.predicates.reduce((acc, item) => {
      const key = `${item.attributeName}__${item.predicateType}__${item.predicateValue}`;
      if (!acc[key]) {
        acc[key] = {
          attributeName: item.attributeName,
          predicateType: item.predicateType,
          predicateValue: item.predicateValue,
          definitionIds: [] as string[],
        };
      }
      acc[key].definitionIds.push(...item.definitionId);
      return acc;
    }, {} as Record<string, { attributeName: string; predicateType: string; predicateValue: string; definitionIds: string[] }>);

    let predicateCounter = 1;
    Object.values(predicateGroups).forEach((group) => {
      const restrictions = group.definitionIds.map(id => ({ credDefId: id }));
      
      requestData.requestedPredicates[`predicateReferent${predicateCounter}`] = {
        name: group.attributeName,
        pType: group.predicateType,
        pValue: parseInt(group.predicateValue, 10),
        restrictions: restrictions,
      };
      
      predicateCounter++;
    });

    return requestData;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }
    
    const confirmed = await dialogs.open(CustomConfirmDialog, {
      title: "Confirmation",
      message: "Are you sure you want to register Proof Request?",
      isModal: true,
    });

    if (confirmed) {
      setIsLoading(true);
      try {
        const requestData = prepareRequestData();
        await postProofRequest(requestData);
        
        setIsLoading(false);

        await dialogs.open(CustomDialog, {
          title: 'Notification',
          message: 'Completed register Proof Request.',
          isModal: true,
        }, {
          onClose: async () => navigate('/zkp-policy-management/proof-request-configuration'),
        });
      } catch (err) {
        setIsLoading(false);
        await dialogs.open(CustomDialog, {
          title: "Error",
          message: `Failed to register Proof Request: ${err}`,
          isModal: true,
        });
      }
    }
  };

  const handleCheckDuplicateName = async () => {
    if (!formData.name.trim()) {
      setNameCheckMessage('Please enter a name first.');
      setNameCheckStatus('error');
      return;
    }

    try {
      const response = await verifyNameUnique(formData.name);
      console.log('Name check response:', response); // 디버깅용 로그
      
      if (response.data.unique === false) {
        // 중복인 경우
        setErrors((prev) => ({ ...prev, name: 'Name already exists.' }));
        setIsNameValid(false);
        setNameCheckMessage('This name is already in use. Please choose a different one.');
        setNameCheckStatus('error');
      } else {        
        // 중복이 아닌 경우
        setIsNameValid(true);
        setErrors((prev) => ({ ...prev, name: undefined }));
        setNameCheckMessage('This name is available for use.');
        setNameCheckStatus('success');
      }
    } catch (error) {
      console.error('Name check error:', error); // 디버깅용 로그
      setIsNameValid(false);
      setNameCheckMessage('Failed to check name availability. Please try again.');
      setNameCheckStatus('error');
    }
  };

  const handleReset = () => {
    setFormData({
      name: "",
      version: "",
      curve: "Secp256r1",
      cipher: "AES-256-CBC",
      padding: "PKCS5",
      attributes: [],
      predicates: [],
    });
    setErrors({});
    setIsNameValid(false);
    setNameCheckMessage('');
    setNameCheckStatus('');
  };

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 900,
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
      <Typography variant="h4">Proof Request Configuration</Typography>
      <StyledContainer>
        <StyledTitle>Proof Request Registration</StyledTitle>
        <StyledInputArea>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
               <TextField 
                label="Name *" 
                fullWidth 
                size="small"
                margin="normal" 
                value={formData.name} 
                onChange={handleChange("name")} 
                error={!!errors.name}
                helperText={errors.name || nameCheckMessage}
                sx={{ 
                  width: '60%',
                  '& .MuiFormHelperText-root': {
                    color: nameCheckStatus === 'success' ? 'green' : 
                           nameCheckStatus === 'error' ? 'red' : 'inherit',
                    fontWeight: nameCheckStatus ? 500 : 'inherit'
                  }
                }}
              />
               <Button 
                  variant="contained" 
                  onClick={handleCheckDuplicateName}
                  disabled={!formData.name}
                  sx={{ 
                      minWidth: 150,  
                      whiteSpace: 'nowrap', 
                      textTransform: 'none' 
                  }}
              >
                  Check Availability
              </Button>
            </Box>

            <TextField 
              label="Version *" 
              fullWidth 
              size="small" 
              margin="normal" 
              value={formData.version} 
              onChange={handleChange("version")} 
              sx={{ width: '60%' }}
              error={!!errors.version}
              helperText={errors.version}
            />

            <FormControl fullWidth margin="normal" error={!!errors.curve} sx={{ width: '60%' }} size="small">
                <InputLabel>Curve *</InputLabel>
                <Select 
                  value={formData.curve} 
                  onChange={(e) => {
                    setFormData(prev => ({ ...prev, curve: e.target.value }));
                    setErrors(prev => ({ ...prev, curve: undefined }));
                  }}>
                  {curveTypes.map((curve) => (
                      <MenuItem 
                        key={curve.value} 
                        value={curve.value}
                        disabled={curve.disabled}
                        sx={{
                          color: curve.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                          '&.Mui-disabled': {
                            color: 'rgba(0, 0, 0, 0.38)'
                          }
                        }}
                      >
                          {curve.label}
                      </MenuItem>
                  ))}
                </Select>
                {errors.curve && <FormHelperText>{errors.curve}</FormHelperText>}
            </FormControl>

            <FormControl fullWidth size="small" margin="normal" error={!!errors.cipher} sx={{ width: '60%' }}>
              <InputLabel>Cipher</InputLabel>
              <Select 
                value={formData.cipher} 
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, cipher: e.target.value }));
                  setErrors(prev => ({ ...prev, cipher: undefined }));
                }}>
                {cipherTypes.map((cipher) => (
                    <MenuItem 
                      key={cipher.value} 
                      value={cipher.value}
                      disabled={cipher.disabled}
                      sx={{
                        color: cipher.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                        '&.Mui-disabled': {
                          color: 'rgba(0, 0, 0, 0.38)'
                        }
                      }}
                    >
                        {cipher.label}
                    </MenuItem>
                ))}
              </Select>
              {errors.cipher && <FormHelperText>{errors.cipher}</FormHelperText>}
            </FormControl>

            <FormControl fullWidth size="small" margin="normal" error={!!errors.padding} sx={{ width: '60%' }}>
              <InputLabel>Padding</InputLabel>
              <Select 
                value={formData.padding} 
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, padding: e.target.value }));
                  setErrors(prev => ({ ...prev, padding: undefined }));
                }}>
                {paddingTypes.map((padding) => (
                    <MenuItem 
                      key={padding.value} 
                      value={padding.value}
                      disabled={padding.disabled}
                      sx={{
                        color: padding.disabled ? 'rgba(0, 0, 0, 0.38)' : 'inherit',
                        '&.Mui-disabled': {
                          color: 'rgba(0, 0, 0, 0.38)'
                        }
                      }}
                    >
                        {padding.label}
                    </MenuItem>
                ))}
              </Select>
              {errors.padding && <FormHelperText>{errors.padding}</FormHelperText>}
            </FormControl>

            <Typography variant="h6" sx={{ mt: 3 }}>Requested Attributes</Typography>
            {errors.attributesOrPredicates && (
              <Typography color="error" variant="caption" sx={{ mt: 1, display: "block" }}>
                {errors.attributesOrPredicates}
              </Typography>
            )}
            <Button variant="contained" startIcon={<AddCircleOutlineIcon />} sx={{ my: 2 }} onClick={handleOpenAttributeDialog}>
            Add Attribute
            </Button>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                    <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                        <TableCell>Attribute Name</TableCell>
                        <TableCell>Credential Definition</TableCell>
                        <TableCell sx={{ width: 100 }}>Delete</TableCell>
                    </TableRow>
                    </TableHead>
                    <TableBody>
                    {Object.entries(
                      formData.attributes.reduce((acc, item) => {
                        if (!acc[item.attributeName]) {
                          acc[item.attributeName] = [];
                        }
                        acc[item.attributeName].push(item.definitionId);
                        return acc;
                      }, {} as Record<string, string[]>)
                    ).map(([attributeName, definitionIds], index) => (
                      <TableRow key={index}>
                        <TableCell>{attributeName}</TableCell>
                        <TableCell>
                          {definitionIds.map((id, i) => (
                            <Typography key={i} variant="body2">
                              {id}
                            </Typography>
                          ))}
                        </TableCell>
                        <TableCell>
                          {/* 삭제 버튼은 첫 definition만 기준으로 제거 */}
                          <IconButton
                            onClick={() => {
                              const updated = formData.attributes.filter(attr => attr.attributeName !== attributeName);
                              setFormData(prev => ({
                                ...prev,
                                attributes: updated,
                              }));
                              
                              // Check if both attributes and predicates are empty after removal
                              if (updated.length === 0 && formData.predicates.length === 0) {
                                setErrors(prev => ({ 
                                  ...prev, 
                                  attributesOrPredicates: "At least one attribute or predicate is required" 
                                }));
                              }
                            }}
                          >
                            <DeleteIcon sx={{ color: "#FF8400" }} />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
            </TableContainer>

            <Typography variant="h6" sx={{ mt: 3 }}>Requested Predicates</Typography>
            <Button
              variant="contained"
              startIcon={<AddCircleOutlineIcon />}
              sx={{ my: 2 }}
              onClick={handleOpenPredicateDialog}
            >
              Add Predicate
            </Button>

            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                    <TableCell>Attribute Name</TableCell>
                    <TableCell>Predicate Type</TableCell>
                    <TableCell>Predicate Value</TableCell>
                    <TableCell>Credential Definition</TableCell>
                    <TableCell>Delete</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {Object.entries(
                    formData.predicates.reduce((acc, item) => {
                      const key = `${item.attributeName}__${item.predicateType}__${item.predicateValue}`;
                      if (!acc[key]) {
                        acc[key] = {
                          attributeName: item.attributeName,
                          predicateType: item.predicateType,
                          predicateValue: item.predicateValue,
                          definitionIds: [] as string[],
                        };
                      }
                      acc[key].definitionIds.push(...item.definitionId);
                      return acc;
                    }, {} as Record<string, { attributeName: string; predicateType: string; predicateValue: string; definitionIds: string[] }>)
                  ).map(([_, group], index) => (
                    <TableRow key={index}>
                      <TableCell>{group.attributeName}</TableCell>
                      <TableCell>
                        <Typography variant="body2">{group.predicateType}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">{group.predicateValue}</Typography>
                      </TableCell>
                      <TableCell>
                        {group.definitionIds.map((defId, i) => (
                          <Typography key={i} variant="body2">{defId}</Typography>
                        ))}
                      </TableCell>
                      <TableCell>
                        <IconButton
                          onClick={() => {
                            const updated = formData.predicates.filter(
                              p =>
                                !(
                                  p.attributeName === group.attributeName &&
                                  p.predicateType === group.predicateType &&
                                  p.predicateValue === group.predicateValue
                                )
                            );
                            
                            setFormData(prev => ({
                              ...prev,
                              predicates: updated,
                            }));
                            
                            // Check if both attributes and predicates are empty after removal
                            if (updated.length === 0 && formData.attributes.length === 0) {
                              setErrors(prev => ({ 
                                ...prev, 
                                attributesOrPredicates: "At least one attribute or predicate is required" 
                              }));
                            }
                          }}
                        >
                          <DeleteIcon sx={{ color: "#FF8400" }} />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 4 }}>
              <Button variant="contained" color="primary" onClick={handleSubmit}>Register</Button>
              <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
              <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
            </Box>
        </StyledInputArea>
      </StyledContainer>
    </>
  );
};

export default ProofRequestConfigurationRegistrationPage;