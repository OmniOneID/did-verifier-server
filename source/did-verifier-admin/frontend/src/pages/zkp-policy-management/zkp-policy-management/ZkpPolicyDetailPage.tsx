import { Box, Button, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getPolicy } from '../../../apis/zkp-policy-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface PolicyData {
  id: number;
  policyTitle: string;
  payloadId: string;
  payloadService: string;
  policyProfileId: string;
  profileTitle: string;
  createdAt?: string;
  updatedAt?: string;
}

const ZkpPolicyDetailPage = (props: Props) => {
  const { id } = useParams();
  const policyId = id ? parseInt(id, 10) : null;
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const theme = useTheme();
  
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [policyData, setPolicyData] = useState<PolicyData | null>(null);
  
  useEffect(() => {
    const fetchPolicyData = async () => {
      if (policyId === null || isNaN(policyId)) {
        await dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: 'Invalid policy ID.', 
          isModal: true 
        }, {
          onClose: async () => navigate('/zkp-policy-management/zkp-policy-management', { replace: true }),
        });
        return;
      }

      try {
        setIsLoading(true);
        const { data } = await getPolicy(policyId);
        
        setPolicyData({
          id: data.id,
          policyTitle: data.policyTitle || '',
          payloadId: data.payloadId || '',
          payloadService: data.payloadService || '',
          policyProfileId: data.policyProfileId || '',
          profileTitle: data.profileTitle || '',
          createdAt: data.createdAt || '',
          updatedAt: data.updatedAt || '',
        });
        
        setIsLoading(false);
      } catch (err) {
        console.error('Failed to fetch policy information:', err);
        setIsLoading(false);
        
        await dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: `Failed to fetch policy information: ${err}`, 
          isModal: true 
        }, {
          onClose: async () => navigate('/zkp-policy-management/zkp-policy-management', { replace: true }),
        });
      }
    };

    fetchPolicyData();
  }, [policyId, dialogs, navigate]);
  
  const handleEdit = () => {
    if (policyData) {
      navigate(`/zkp-policy-management/zkp-policy-management/zkp-policy-edit/${policyData.id}`);
    }
  };
  
  const handleBack = () => {
    navigate('/zkp-policy-management/zkp-policy-management');
  };
  
  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 800,
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
      <Typography variant="h4">ZKP Policy Management</Typography>
      <StyledContainer>
        <StyledTitle>ZKP Policy Detail Information</StyledTitle>
        
        {policyData && (
          <StyledInputArea>
            <TextField
              fullWidth
              label="Policy Title"
              name="policyTitle"
              value={policyData?.policyTitle || ''}
              variant="outlined" 
              margin="normal"
              slotProps={{ input: { readOnly: true } }}
            />
            
            <Typography variant="h6" sx={{ mt: 3 }}>Profile Information</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              {/* Hidden input for policyProfileId */}
              <input type="hidden" value={policyData?.policyProfileId || ''} />
              
              <TextField
                sx={{ flex: 1 }}
                label="Profile Title"
                value={policyData?.profileTitle || ''}
                variant="outlined" 
                size="small"
                slotProps={{ input: { readOnly: true } }}
              />
            </Box>
            
            <Typography variant="h6" sx={{ mt: 3 }}>Payload Information</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              {/* Hidden input for payloadId */}
              <input type="hidden" value={policyData?.payloadId || ''} />
              
              <TextField
                sx={{ flex: 1 }}
                label="Payload Service"
                value={policyData?.payloadService || ''}
                variant="outlined" 
                size="small"
                slotProps={{ input: { readOnly: true } }}
              />
            </Box>

            {/* Timestamp Information */}
            {(policyData.createdAt || policyData.updatedAt) && (
              <>
                <Typography variant="h6" sx={{ mt: 3 }}>Timestamp Information</Typography>
                {policyData.createdAt && (
                  <TextField
                    fullWidth
                    label="Created At"
                    value={policyData.createdAt}
                    variant="outlined" 
                    margin="normal"
                    size="small"
                    slotProps={{ input: { readOnly: true } }}
                  />
                )}
                {policyData.updatedAt && (
                  <TextField
                    fullWidth
                    label="Updated At"
                    value={policyData.updatedAt}
                    variant="outlined" 
                    margin="normal"
                    size="small"
                    slotProps={{ input: { readOnly: true } }}
                  />
                )}
              </>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
              <Button 
                variant="outlined" 
                color="primary" 
                onClick={handleBack}
              >
                Back
              </Button>
              <Button 
                variant="outlined" 
                color="primary" 
                onClick={handleEdit}
              >
                Go to Edit
              </Button>              
            </Box>
          </StyledInputArea>
        )}
      </StyledContainer>
    </>
  );
};

export default ZkpPolicyDetailPage;