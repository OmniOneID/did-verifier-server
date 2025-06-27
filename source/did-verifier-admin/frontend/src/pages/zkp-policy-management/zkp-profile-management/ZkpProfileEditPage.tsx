import {
  Box, Button, MenuItem, Select, SelectChangeEvent, TextField,
  Typography, useTheme, FormControl, InputLabel, FormHelperText, styled
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDialog from "../../../components/dialog/CustomDialog";
import CustomConfirmDialog from "../../../components/dialog/CustomConfirmDialog";
import { useDialogs } from "@toolpad/core";
import { getZkpProfile, putZkpProfile } from "../../../apis/zkp-profile-api";
import { getProofRequestAll } from "../../../apis/zkp-proof-api";
import { encodingTypes } from "../../../constants/encoding-types";
import { languageTypes } from "../../../constants/language-types";
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

const ZkpProfileEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const theme = useTheme();

  const [formData, setFormData] = useState<FormData>({
    title: "",
    description: "",
    encoding: "UTF-8",
    language: "en",
    zkpProofRequestId: "",
  });

  const [initialFormData, setInitialFormData] = useState<FormData>({
    title: "",
    description: "",
    encoding: "UTF-8",
    language: "en",
    zkpProofRequestId: "",
  });

  const [errors, setErrors] = useState<ErrorState>({});
  const [proofRequestOptions, setProofRequestOptions] = useState<{ id: string; name: string }[]>([]);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (field: keyof FormData) => 
    (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
      const newValue = event.target.value;
      setFormData((prev) => ({ ...prev, [field]: newValue }));
  };

  const handleReset = () => {
    setFormData(initialFormData);
    setIsButtonDisabled(true);
  };

  const validate = () => {
    const tempErrors: ErrorState = {};

    if (!formData.title.trim()) {
      tempErrors.title = "Title is required";
    } else if (formData.title.length < 4 || formData.title.length > 40) {
      tempErrors.title = "Title must be between 4 and 40 characters";
    }

    if (!formData.description.trim()) {
      tempErrors.description = "Description is required";
    } else if (formData.description.length > 200) {
      tempErrors.description = "Description must be less than 200 characters";
    }

    if (!formData.encoding) tempErrors.encoding = "Encoding is required";
    if (!formData.language) tempErrors.language = "Language is required";
    if (!formData.zkpProofRequestId) tempErrors.zkpProofRequestId = "Proof Request is required";

    setErrors(tempErrors);
    return Object.values(tempErrors).every((error) => !error);
  };

  const handleSubmit = async () => {
    if (!validate()) return;

    const confirm = await dialogs.open(CustomConfirmDialog, {
      title: "Confirmation",
      message: "Are you sure you want to update ZKP Profile?",
      isModal: true,
    });

    if (!confirm) return;

    setIsLoading(true);
    try {
      const requestData = {
        id: parseInt(id as string, 10),
        title: formData.title,
        description: formData.description,
        encoding: formData.encoding,
        language: formData.language,
        zkpProofRequestId: formData.zkpProofRequestId,
      };

      await putZkpProfile(requestData);

      dialogs.open(CustomDialog, {
        title: "Notification",
        message: "ZKP Profile updated successfully.",
        isModal: true,
      }, {
        onClose: async () => navigate("/zkp-policy-management/zkp-profile-management"),
      });
    } catch (error) {
      dialogs.open(CustomDialog, {
        title: "Notification",
        message: formatErrorMessage(error, "Failed to update ZKP Profile"),
        isModal: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const fetchProfileData = async () => {
    if (!id) return;
    setIsLoading(true);
    try {
        const profile = await getZkpProfile(parseInt(id, 10));
        const loadedData: FormData = {
        title: profile.data.title,
        description: profile.data.description,
        encoding: profile.data.encoding,
        language: profile.data.language,
        zkpProofRequestId: profile.data.zkpProofRequestId.toString(),
        };
        setFormData(loadedData);
        setInitialFormData(loadedData); // 초기값 저장
    } catch (error) {
        console.error("Failed to fetch profile", error);
    } finally {
        setIsLoading(false);
    }
    };

  useEffect(() => {
    fetchProfileData();
  }, [id]);

  useEffect(() => {
    const fetchProofRequests = async () => {
      try {
        const response = await getProofRequestAll();
        if (response?.data) {
          const options = response.data.map((item: any) => ({
            id: item.id,
            name: item.name,
          }));
          setProofRequestOptions(options);
        }
      } catch (error) {
        console.error("Failed to fetch proof requests", error);
      }
    };
    fetchProofRequests();
  }, []);

  useEffect(() => {
    const isEqual = JSON.stringify(formData) === JSON.stringify(initialFormData);
    setIsButtonDisabled(isEqual);
  }, [formData, initialFormData]);

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
      <Typography variant="h4">ZKP Profile Update</Typography>
      <StyledContainer>
        <StyledTitle>ZKP Profile Edit</StyledTitle>
        <StyledInputArea>
          <TextField fullWidth required label="Title" variant="outlined" margin="normal" size="small"
            value={formData.title} onChange={handleChange('title')}
            error={!!errors.title} helperText={errors.title}
          />
          <TextField fullWidth required label="Description" variant="outlined" margin="normal" size="small"
            value={formData.description} onChange={handleChange('description')} multiline rows={2}
            error={!!errors.description} helperText={errors.description}
          />
          <FormControl fullWidth size="small" margin="normal" error={!!errors.encoding}>
            <InputLabel>Encoding *</InputLabel>
            <Select value={formData.encoding} onChange={handleChange('encoding')} required>
              {encodingTypes.map(enc => (
                <MenuItem key={enc.value} value={enc.value}>{enc.label}</MenuItem>
              ))}
            </Select>
            {errors.encoding && <FormHelperText>{errors.encoding}</FormHelperText>}
          </FormControl>
          <FormControl fullWidth size="small" margin="normal" error={!!errors.language}>
            <InputLabel>Language *</InputLabel>
            <Select value={formData.language} onChange={handleChange('language')} required>
              {languageTypes.map(lang => (
                <MenuItem key={lang.value} value={lang.value}>{lang.label}</MenuItem>
              ))}
            </Select>
            {errors.language && <FormHelperText>{errors.language}</FormHelperText>}
          </FormControl>
          <FormControl fullWidth size="small" margin="normal" error={!!errors.zkpProofRequestId}>
            <InputLabel>Proof Request *</InputLabel>
            <Select value={formData.zkpProofRequestId} onChange={handleChange('zkpProofRequestId')} required>
              {proofRequestOptions.map(opt => (
                <MenuItem key={opt.id} value={opt.id}>{opt.name}</MenuItem>
              ))}
            </Select>
            {errors.zkpProofRequestId && <FormHelperText>{errors.zkpProofRequestId}</FormHelperText>}
          </FormControl>
          <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 4 }}>
            <Button variant="contained" color="primary" onClick={handleSubmit} disabled={isButtonDisabled}>Update</Button>
            <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
          </Box>
        </StyledInputArea>
      </StyledContainer>
    </>
  );
};

export default ZkpProfileEditPage;
