import { Box, Button, Chip, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getScopeMapping } from '../../../apis/oid4vp-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

interface ScopeMappingData {
  id: number;
  scope: string;
  dcqlQuery: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

const ScopeMappingDetailPage = () => {
  const { id } = useParams();
  const mappingId = id ? parseInt(id, 10) : null;
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [data, setData] = useState<ScopeMappingData | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (mappingId === null || isNaN(mappingId)) {
        await dialogs.open(CustomDialog, {
          title: 'Error',
          message: 'Invalid scope mapping ID.',
          isModal: true,
        }, {
          onClose: async () => navigate('/oid4vp-management/scope-mapping', { replace: true }),
        });
        return;
      }

      try {
        setIsLoading(true);
        const { data: resp } = await getScopeMapping(mappingId);
        setData({
          id: resp.id,
          scope: resp.scope || '',
          dcqlQuery: resp.dcqlQuery || '',
          description: resp.description || '',
          enabled: resp.enabled ?? true,
          createdAt: resp.createdAt || '',
          updatedAt: resp.updatedAt || '',
        });
      } catch (err) {
        console.error('Failed to fetch scope mapping:', err);
        await dialogs.open(CustomDialog, {
          title: 'Error',
          message: `Failed to fetch scope mapping: ${err}`,
          isModal: true,
        }, {
          onClose: async () => navigate('/oid4vp-management/scope-mapping', { replace: true }),
        });
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
    // dialogs omitted: useDialogs() returns a new ref on every dialog open/close app-wide,
    // so keeping it here would re-trigger this effect each time dialogs.open() fires on error.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mappingId, navigate]);

  const handleEdit = () => {
    if (data) {
      navigate(`/oid4vp-management/scope-mapping/scope-mapping-edit/${data.id}`);
    }
  };

  const handleBack = () => {
    navigate('/oid4vp-management/scope-mapping');
  };

  const formatJson = (jsonStr: string) => {
    try {
      return JSON.stringify(JSON.parse(jsonStr), null, 2);
    } catch {
      return jsonStr;
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
        <StyledTitle>Scope Mapping Detail</StyledTitle>
        {data && (
          <StyledInputArea>
            <TextField
              fullWidth
              label="Scope"
              value={data.scope}
              variant="outlined"
              margin="normal"
              InputProps={{ readOnly: true }}
            />
            <TextField
              fullWidth
              label="Description"
              value={data.description}
              variant="outlined"
              margin="normal"
              InputProps={{ readOnly: true }}
            />
            <Box sx={{ mt: 2, mb: 1 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>Enabled</Typography>
              <Chip
                label={data.enabled ? 'Yes' : 'No'}
                color={data.enabled ? 'success' : 'default'}
                size="small"
              />
            </Box>
            <TextField
              fullWidth
              multiline
              minRows={8}
              maxRows={20}
              label="DCQL Query (JSON)"
              value={formatJson(data.dcqlQuery)}
              variant="outlined"
              margin="normal"
              InputProps={{
                readOnly: true,
                sx: { fontFamily: 'monospace', fontSize: '13px' },
              }}
            />
            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
              <Button variant="outlined" color="primary" onClick={handleBack}>
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

export default ScopeMappingDetailPage;
