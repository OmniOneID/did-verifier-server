import { Box, Button, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getOid4vpPolicy, putOid4vpPolicy, searchScopeMappingList } from '../../../apis/oid4vp-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

interface PolicyFormData {
  id: number;
  policyTitle: string;
  scope: string;
}

interface ErrorState {
  policyTitle?: string;
  scope?: string;
}

const Oid4vpPolicyEditPage = () => {
  const { id } = useParams();
  const policyId = id ? parseInt(id, 10) : null;
  const navigate = useNavigate();
  const dialogs = useDialogs();

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errors, setErrors] = useState<ErrorState>({});
  const [originalData, setOriginalData] = useState<PolicyFormData | null>(null);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);

  const [scopeSearchOpen, setScopeSearchOpen] = useState(false);
  const [scopeList, setScopeList] = useState<{ id: string; title: string }[]>([]);
  const [scopeLoading, setScopeLoading] = useState(false);

  const [policyData, setPolicyData] = useState<PolicyFormData>({
    id: 0,
    policyTitle: '',
    scope: '',
  });

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
        const formValues: PolicyFormData = {
          id: data.id,
          policyTitle: data.policyTitle || '',
          scope: data.scope || '',
        };
        setPolicyData(formValues);
        setOriginalData(formValues);
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

  useEffect(() => {
    if (!originalData) return;
    const isChanged =
      policyData.policyTitle !== originalData.policyTitle ||
      policyData.scope !== originalData.scope;
    setIsButtonDisabled(!isChanged);
  }, [policyData, originalData]);

  const processApiResponse = (data: any): any[] => {
    if (!data) return [];
    if (Array.isArray(data)) return data;
    if (data.data && Array.isArray(data.data)) return data.data;
    if (data.content && Array.isArray(data.content)) return data.content;
    return [];
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setPolicyData(prev => ({ ...prev, [name]: value }));
  };

  const handleScopeSearch = async (searchTerm?: string) => {
    setScopeSearchOpen(true);
    try {
      setScopeLoading(true);
      const response = await searchScopeMappingList(searchTerm || 'all');
      setScopeList(processApiResponse(response).map(item => ({
        id: item.scope || '',
        title: item.scope || '[No Scope]',
      })));
    } catch (err) {
      console.error('Failed to search scopes:', err);
    } finally {
      setScopeLoading(false);
    }
  };

  const validate = () => {
    const tempErrors: ErrorState = {};
    if (!policyData.policyTitle) tempErrors.policyTitle = 'Policy title is required';
    if (!policyData.scope) tempErrors.scope = 'Scope selection is required';
    setErrors(tempErrors);
    return !Object.values(tempErrors).some(error => error !== undefined);
  };

  const handleReset = () => {
    if (originalData) {
      setPolicyData(originalData);
      setErrors({});
    }
  };

  const handleSubmit = async () => {
    if (!validate()) {
      await dialogs.open(CustomDialog, {
        title: 'Validation Error',
        message: 'Please fill in all required fields correctly.',
        isModal: true,
      });
      return;
    }

    try {
      setIsLoading(true);
      await putOid4vpPolicy(policyData);
      setIsLoading(false);

      await dialogs.open(CustomDialog, {
        title: 'Success',
        message: 'OID4VP Policy has been updated successfully.',
        isModal: true,
      }, {
        onClose: async () => navigate(`/oid4vp-management/policy/${policyId}`, { replace: true }),
      });
    } catch (err) {
      console.error('Failed to update policy:', err);
      setIsLoading(false);
      await dialogs.open(CustomDialog, {
        title: 'Error',
        message: `Failed to update policy: ${err}`,
        isModal: true,
      });
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

      <SearchDialog
        open={scopeSearchOpen}
        onClose={() => setScopeSearchOpen(false)}
        onSelect={(selected: any) => setPolicyData(prev => ({
          ...prev,
          scope: selected.id?.toString() || selected.title || '',
        }))}
        onSearch={handleScopeSearch}
        title="Scope Search"
        items={scopeList}
        loading={scopeLoading}
        idField="id"
      />

      {policyData && (
        <StyledContainer>
          <StyledTitle>OID4VP Policy Edit</StyledTitle>
          <StyledInputArea>
            <TextField
              fullWidth
              required
              label="Policy Title"
              name="policyTitle"
              value={policyData.policyTitle}
              variant="outlined"
              margin="normal"
              onChange={handleInputChange}
              error={!!errors.policyTitle}
              helperText={errors.policyTitle}
            />

            <Typography variant="h6" sx={{ mt: 3 }}>DCQL Scope Mapping</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
              <TextField
                sx={{ flex: 1 }}
                required
                label="Scope"
                value={policyData.scope}
                variant="outlined"
                size="small"
                InputProps={{ readOnly: true }}
                error={!!errors.scope}
                helperText={errors.scope}
              />
              <Button variant="contained" size="small" onClick={() => handleScopeSearch()}>
                Search
              </Button>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
              <Button variant="contained" color="primary" onClick={handleSubmit} disabled={isButtonDisabled}>
                Save
              </Button>
              <Button variant="contained" color="secondary" onClick={handleReset}>
                Reset
              </Button>
              <Button variant="outlined" color="secondary" onClick={() => navigate(`/oid4vp-management/policy/${policyId}`)}>
                Cancel
              </Button>
            </Box>
          </StyledInputArea>
        </StyledContainer>
      )}
    </>
  );
};

export default Oid4vpPolicyEditPage;
