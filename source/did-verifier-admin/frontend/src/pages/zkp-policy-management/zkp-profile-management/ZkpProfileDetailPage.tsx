import {
  Box, Button, Typography, TextField, FormControl, InputLabel, Select, MenuItem, useTheme, styled
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import { getZkpProfile } from "../../../apis/zkp-profile-api";
import { formatErrorMessage } from "../../../utils/error-handler";
import { encodingTypes } from "../../../constants/encoding-types";
import { languageTypes } from "../../../constants/language-types";

interface DetailData {
  id: number;
  profileId: string;
  type: string;
  title: string;
  description: string;
  encoding: string;
  language: string;
  zkpProofRequestId: number;
  zkpProofRequestName: string;
  createdAt: string;
  updatedAt: string;
}

const ZkpProfileDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const theme = useTheme();
  const [data, setData] = useState<DetailData | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchDetail = async () => {
      if (!id) return;
      setIsLoading(true);
      try {
        const response = await getZkpProfile(parseInt(id, 10));
        setData(response.data);
      } catch (error) {
        console.error(formatErrorMessage(error, "Failed to fetch profile detail"));
      } finally {
        setIsLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

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
      <Typography variant="h4">ZKP Profile Management</Typography>
      {data && (
        <StyledContainer>
          <StyledTitle>ZKP Profile Detail Information</StyledTitle>
          <StyledInputArea>
            <TextField 
              label="Profile ID" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.profileId} 
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Type" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.type} 
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Title" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.title} 
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Description" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.description}  
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Encoding" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={
                encodingTypes.find(item => item.value === data.encoding)?.label || data.encoding
              }  
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Language" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={
                languageTypes.find(item => item.value === data.language)?.label || data.language
              }  
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Proof Request" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.zkpProofRequestName}  
              slotProps={{ input: { readOnly: true } }}
            />
            <TextField 
              label="Created At" 
              variant="standard" 
              fullWidth 
              margin="normal" 
              size="small" 
              value={data.createdAt}  
              slotProps={{ input: { readOnly: true } }}
            />
            
            {data?.updatedAt && (
                <TextField 
                    fullWidth 
                    label="Updated At" 
                    variant="standard" 
                    margin="normal" 
                    value={data?.updatedAt || ''} 
                    slotProps={{ input: { readOnly: true } }} 
                />
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
              <Button variant="outlined" onClick={() => navigate(-1)}>Back</Button>
              <Button variant="outlined" color="primary" onClick={() => navigate('/zkp-policy-management/zkp-profile-management/profile-edit/' + id)}>
                  Go to Edit
              </Button>
            </Box>
          </StyledInputArea>
        </StyledContainer>
      )}
    </>
  );
};

export default ZkpProfileDetailPage;
