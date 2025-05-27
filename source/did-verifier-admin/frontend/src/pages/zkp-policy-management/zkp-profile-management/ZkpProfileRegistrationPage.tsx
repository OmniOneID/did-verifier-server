import {
  Box, Button, IconButton, MenuItem, Paper, Select, SelectChangeEvent,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography, useTheme, FormControl, InputLabel, styled,
  FormHelperText
} from "@mui/material";
import { use, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import DeleteIcon from "@mui/icons-material/Delete";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDialog from "../../../components/dialog/CustomDialog";
import CustomConfirmDialog from "../../../components/dialog/CustomConfirmDialog";
import { useDialogs } from "@toolpad/core";
import { postZkpProfile } from "../../../apis/zkp-profile-api";
import { getProofRequestAll } from "../../../apis/zkp-proof-api";
import { languageTypes } from "../../../constants/language-types";
import { encodingTypes } from "../../../constants/encoding-types";
import { formatErrorMessage } from "../../../utils/error-handler";

interface FormData {
  title: string;
  description: string;
  encoding: string;
  language: string;
  zkpProofRequestId: string;
}

interface ErrorState {
  title?: string;
  description?: string;
  encoding?: string;
  language?: string;
  zkpProofRequestId?: string;
}

const ZkpProfileRegistrationPage = () => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const theme = useTheme();
  const [errors, setErrors] = useState<ErrorState>({});
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [proofRequestOptions, setProofRequestOptions] = useState<{ id: string; name: string }[]>([]);

  const [formData, setFormData] = useState<FormData>({
    title: "",
    description: "",
    encoding: "UTF-8",
    language: "en",
    zkpProofRequestId: "",
  });

  const handleChange = (field: keyof FormData) => 
      (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
          const newValue = event.target.value;
          setFormData((prev) => ({ ...prev, [field]: newValue }));
  };

  const handleReset = () => {
    setFormData({
      title: "",
      description: "",
      encoding: "UTF-8",
      language: "en",
      zkpProofRequestId: "",
    });
    setIsButtonDisabled(true); 
  };

  const validate = () => {
    let tempErrors: ErrorState = {};

    // Title validation
    if (!formData.title.trim()) {
      tempErrors.title = "Title is required";
    } else if (formData.title.length < 4 || formData.title.length > 40) {
      tempErrors.title = "Title must be between 4 and 40 characters";
    }

    // Description validation
    if (!formData.description.trim()) {
      tempErrors.description = "Description is required";
    } else if (formData.description.length > 200) {
      tempErrors.description = "Description must be less than 200 characters";
    }

    // Encoding validation
    if (!formData.encoding) {
      tempErrors.encoding = "Encoding is required";
    }

    // Language validation
    if (!formData.language) {
      tempErrors.language = "Language is required";
    }

    // Proof Reuqest validation
    if (!formData.zkpProofRequestId) {
      tempErrors.zkpProofRequestId = "Proof Request is required";
    }


    setErrors(tempErrors);
    return Object.values(tempErrors).every((error) => !error);
  };


const handleSubmit = async () => {
    if (!validate()) return;

    const result = await dialogs.open(CustomConfirmDialog, {
      title: 'Confirmation',
      message: 'Are you sure you want to register ZKP Profile?',
      isModal: true,
    });

    if (!result) return;

    setIsLoading(true);
    try {
      const requestData = {
        title: formData.title,
        description: formData.description,
        encoding: formData.encoding,
        language: formData.language,
        zkpProofRequestId: formData.zkpProofRequestId,
      };

      await postZkpProfile(requestData);

      dialogs.open(CustomDialog, {
        title: 'Notification',
        message: 'ZKP Profile registration completed.',
        isModal: true,
      }, {
        onClose: async () => navigate('/zkp-policy-management/zkp-profile-management'),
      });
    } catch (error) {
      dialogs.open(CustomDialog, {
        title: 'Notification',
        message: formatErrorMessage(error, "Failed to register ZKP Profile"),
        isModal: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const fetchProofRequests = async () => {
      setIsLoading(true);
      try {
        const response = await getProofRequestAll();
        if (response && response.data) {
          const options = response.data.map((item: any) => ({
            id: item.id,
            name: item.name,
          }));
          setProofRequestOptions(options);
          setFormData(prev => ({ ...prev, zkpProofRequestId: options[0]?.id || '' }));
        }
      } catch (error) {
        console.error('Failed to fetch proof requests:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProofRequests();
  }, []);

  useEffect(() => {
    const hasInput =
      formData.title.trim() !== "" ||
      formData.description.trim() !== "" ||
      formData.encoding !== "UTF-8" ||
      formData.language !== "en" ||
      formData.zkpProofRequestId !== "";

    setIsButtonDisabled(!hasInput);
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
      <Typography variant="h4">ZKP Profile Management</Typography>
      <StyledContainer>
        <StyledTitle>ZKP Profile Registration</StyledTitle>
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

          <TextField
              fullWidth                                                
              label="Description"
              required
              name="description"
              value={formData?.description || ''}
              variant="outlined" 
              size='small'
              margin="normal" 
              onChange={handleChange('description')}
              multiline
              rows={2}
              error={!!errors.description}
              helperText={errors.description}
          />

          <FormControl fullWidth size="small" margin="normal" error={!!errors.encoding}>
              <InputLabel>Encoding *</InputLabel>
              <Select 
                value={formData.encoding} 
                required
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, encoding: e.target.value }));
                  setErrors(prev => ({ ...prev, encoding: undefined }));
                }}>
                {encodingTypes.map((encoding) => (
                    <MenuItem key={encoding.value} value={encoding.value}>
                        {encoding.label}
                    </MenuItem>
                ))}
              </Select>
              {errors.encoding && <FormHelperText>{errors.encoding}</FormHelperText>}
            </FormControl>

            <FormControl fullWidth size="small" margin="normal" error={!!errors.language}>
              <InputLabel>Language *</InputLabel>
              <Select 
                value={formData.language} 
                required
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, language: e.target.value }));
                  setErrors(prev => ({ ...prev, language: undefined }));
                }}>
                {languageTypes.map((language) => (
                    <MenuItem key={language.value} value={language.value}>
                        {language.label}
                    </MenuItem>
                ))}
              </Select>
              {errors.language && <FormHelperText>{errors.language}</FormHelperText>}
            </FormControl>

            <FormControl fullWidth size="small" margin="normal" error={!!errors.zkpProofRequestId}>
              <InputLabel>Proof Request *</InputLabel>
              <Select
                value={formData.zkpProofRequestId}
                required
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, zkpProofRequestId: e.target.value }));
                  setErrors(prev => ({ ...prev, zkpProofRequestId: undefined }));
                }}
              >
                {proofRequestOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>
                    {option.name}
                  </MenuItem>
                ))}
              </Select>
              {errors.zkpProofRequestId && <FormHelperText>{errors.zkpProofRequestId}</FormHelperText>}
            </FormControl>

            <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 4 }}>
              <Button variant="contained" color="primary" onClick={handleSubmit} disabled={isButtonDisabled}>Register</Button>
              <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
              <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
            </Box>
        </StyledInputArea>
      </StyledContainer>

    </>
  )
}

export default ZkpProfileRegistrationPage