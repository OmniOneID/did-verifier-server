import { Box, Button, Modal, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getPolicy, getPolicyVerifyProfile } from '../../../apis/zkp-policy-api';
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
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [verifyProfileData, setVerifyProfileData] = useState<any>(null);
  
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

  const handleViewPolicyOpen = async () => {
    if (!policyData) return;
    try {
      const response = await getPolicyVerifyProfile(policyData.id);
      setVerifyProfileData(response.data);
      setIsModalOpen(true);
    } catch (err) {
      await dialogs.open(CustomDialog, {
        title: 'Error',
        message: `Failed to fetch verify profile: ${err}`,
        isModal: true,
      });
    }
  };

  const handleViewPolicyClose = () => {
    setIsModalOpen(false);
    setVerifyProfileData(null);
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
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <StyledTitle>ZKP Policy Detail Information</StyledTitle>
          {policyData && (
            <Button
              variant="outlined"
              color="secondary"
              size="small"
              onClick={handleViewPolicyOpen}
              sx={{ height: 'fit-content', whiteSpace: 'nowrap' }}
            >
              View Policy
            </Button>
          )}
        </Box>

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
        <Modal
          open={isModalOpen}
          onClose={handleViewPolicyClose}
          sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >
          <Box
            sx={{
              width: '80vw',
              maxWidth: 600,
              maxHeight: '80vh',
              bgcolor: 'background.paper',
              border: '2px solid #000',
              borderRadius: 2,
              boxShadow: 24,
              p: 4,
              overflow: 'auto',
            }}
          >
            <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>
              ZKP Policy Verify Profile
            </Typography>
            <Box
              sx={{
                whiteSpace: 'pre-wrap',
                fontFamily: 'monospace',
                fontSize: '12px',
                backgroundColor: '#f5f5f5',
                padding: 2,
                borderRadius: 1,
                maxHeight: '60vh',
                overflow: 'auto',
              }}
            >
              {verifyProfileData ? JSON.stringify(verifyProfileData, null, 2) : 'Loading...'}
            </Box>
            <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
              <Button variant="outlined" onClick={handleViewPolicyClose}>
                Close
              </Button>
            </Box>
          </Box>
        </Modal>
      </StyledContainer>
    </>
  );
};

export default ZkpPolicyDetailPage;