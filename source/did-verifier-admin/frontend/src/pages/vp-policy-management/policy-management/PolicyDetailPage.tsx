import { Box, Button, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { deletePolicy, getPolicy } from '../../../apis/vp-policy-api';
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
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
  createdAt: string;
  updatedAt?: string;
}

const PolicyDetailPage = (props: Props) => {
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
          onClose: async () => navigate('/vp-policy-management', { replace: true }),
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
          onClose: async () => navigate('/vp-policy-management', { replace: true }),
        });
      }
    };

    fetchPolicyData();
  }, [policyId, dialogs, navigate]);
  
  const handleEdit = () => {
    if (policyData) {
      navigate(`/vp-policy-management/policy-management/policy-edit/${policyData.id}`);
    }
  };
  
  const handleDelete = async () => {
    if (!policyData) return;
    
    const confirmResult = await dialogs.open(CustomConfirmDialog, {
      title: 'Confirm Delete',
      message: 'Are you sure you want to delete this policy? This action cannot be undone.',
      isModal: true
    });
    
    if (confirmResult) {
      try {
        setIsLoading(true);
        await deletePolicy(policyData.id);
        setIsLoading(false);
        
        // Show success dialog
        await dialogs.open(CustomDialog, { 
          title: 'Success', 
          message: 'Policy has been deleted successfully.', 
          isModal: true 
        }, {
          onClose: async () => navigate('/vp-policy-management', { replace: true }),
        });
      } catch (err) {
        console.error('Failed to delete policy:', err);
        setIsLoading(false);
        
        await dialogs.open(CustomDialog, { 
          title: 'Error', 
          message: `Failed to delete policy: ${err}`, 
          isModal: true 
        });
      }
    }
  };
  
  const handleBack = () => {
    navigate('/vp-policy-management');
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
      <Typography variant="h4">Policy Management</Typography>
      <StyledContainer>
        <StyledTitle>Policy Detail Information</StyledTitle>
        
        {policyData && (
          <StyledInputArea>
            <TextField
              fullWidth
              label="Policy Title"
              name="policyTitle"
              value={policyData?.policyTitle || ''}
              variant="outlined" 
              margin="normal"
              InputProps={{ readOnly: true }}
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
                InputProps={{ readOnly: true }}
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
                InputProps={{ readOnly: true }}
              />
            </Box>

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
              <Button 
                variant="contained" 
                color="error" 
                onClick={handleDelete}
              >
                Delete
              </Button>
            </Box>
          </StyledInputArea>
        )}
      </StyledContainer>
    </>
  );
};

export default PolicyDetailPage;