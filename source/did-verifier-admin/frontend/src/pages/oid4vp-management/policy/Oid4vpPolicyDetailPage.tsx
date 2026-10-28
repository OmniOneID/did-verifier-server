import { Box, Button, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getOid4vpPolicy } from '../../../apis/oid4vp-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

interface PolicyData {
  id: number;
  policyId: string;
  policyTitle: string;
  scope: string;
  createdAt: string;
}

const Oid4vpPolicyDetailPage = () => {
  const { id } = useParams();
  const policyId = id ? parseInt(id, 10) : null;
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [policyData, setPolicyData] = useState<PolicyData | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (policyId === null || isNaN(policyId)) {
        await dialogs.open(CustomDialog, {
          title: 'Error',
          message: 'Invalid policy ID.',
          isModal: true,
        }, {
          onClose: async () => navigate('/oid4vp-management/policy', { replace: true }),
        });
        return;
      }

      try {
        setIsLoading(true);
        const { data } = await getOid4vpPolicy(policyId);
        setPolicyData({
          id: data.id,
          policyId: data.policyId || '',
          policyTitle: data.policyTitle || '',
          scope: data.scope || '',
          createdAt: data.createdAt || '',
        });
      } catch (err) {
        console.error('Failed to fetch policy:', err);
        await dialogs.open(CustomDialog, {
          title: 'Error',
          message: `Failed to fetch policy: ${err}`,
          isModal: true,
        }, {
          onClose: async () => navigate('/oid4vp-management/policy', { replace: true }),
        });
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
    // dialogs omitted: useDialogs() returns a new ref on every dialog open/close app-wide,
    // so keeping it here would re-trigger this effect each time dialogs.open() fires on error.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [policyId, navigate]);

  const handleEdit = () => {
    if (policyData) {
      navigate(`/oid4vp-management/policy/policy-edit/${policyData.id}`);
    }
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
      <Typography variant="h4">OID4VP Management</Typography>
      <StyledContainer>
        <StyledTitle>OID4VP Policy Detail</StyledTitle>
        {policyData && (
          <StyledInputArea>
            <TextField
              fullWidth
              label="Policy ID"
              value={policyData.policyId}
              variant="outlined"
              margin="normal"
              InputProps={{ readOnly: true }}
            />
            <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
              The policy ID is automatically assigned.
            </Typography>
            <TextField
              fullWidth
              label="Policy Title"
              value={policyData.policyTitle}
              variant="outlined"
              margin="normal"
              InputProps={{ readOnly: true }}
            />

            <Typography variant="h6" sx={{ mt: 3 }}>DCQL Scope Mapping</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              <TextField
                sx={{ flex: 1 }}
                label="Scope"
                value={policyData.scope}
                variant="outlined"
                size="small"
                InputProps={{ readOnly: true }}
              />
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
              <Button variant="outlined" color="primary" onClick={() => navigate('/oid4vp-management/policy')}>
                Back
              </Button>
              <Button variant="outlined" color="primary" onClick={handleEdit}>
                Go to Edit
              </Button>
            </Box>
          </StyledInputArea>
        )}
      </StyledContainer>
    </>
  );
};

export default Oid4vpPolicyDetailPage;
