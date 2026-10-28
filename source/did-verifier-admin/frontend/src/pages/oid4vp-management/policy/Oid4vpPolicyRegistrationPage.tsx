import { Box, Button, TextField, Typography, styled } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { postOid4vpPolicy, searchScopeMappingList } from '../../../apis/oid4vp-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

interface PolicyFormData {
  policyTitle: string;
  scope: string;
}

interface ErrorState {
  policyTitle?: string;
  scope?: string;
}

const Oid4vpPolicyRegistrationPage = () => {
  const navigate = useNavigate();
  const dialogs = useDialogs();

  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [errors, setErrors] = useState<ErrorState>({});
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);

  const [scopeSearchOpen, setScopeSearchOpen] = useState(false);
  const [scopeList, setScopeList] = useState<{ id: string; title: string }[]>([]);
  const [scopeLoading, setScopeLoading] = useState(false);

  const [policyData, setPolicyData] = useState<PolicyFormData>({
    policyTitle: '',
    scope: '',
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setPolicyData(prev => ({ ...prev, [name]: value }));
  };

  useEffect(() => {
    const isModified =
      policyData.policyTitle !== '' ||
      policyData.scope !== '';
    setIsButtonDisabled(!isModified);
  }, [policyData]);

  const processApiResponse = (data: any): any[] => {
    if (!data) return [];
    if (Array.isArray(data)) return data;
    if (data.data && Array.isArray(data.data)) return data.data;
    if (data.content && Array.isArray(data.content)) return data.content;
    return [];
  };

  const handleScopeSearch = async (searchTerm?: string) => {
    setScopeSearchOpen(true);
    try {
      setScopeLoading(true);
      const response = await searchScopeMappingList(searchTerm || 'all');
      const mapped = processApiResponse(response).map(item => ({
        id: item.scope || '',
        title: item.scope || '[No Scope]',
      }));
      setScopeList(mapped);
    } catch (err) {
      console.error('Failed to search scopes:', err);
    } finally {
      setScopeLoading(false);
    }
  };

  const handleScopeSelect = (selected: any) => {
    setPolicyData(prev => ({
      ...prev,
      scope: selected.id?.toString() || selected.title || '',
    }));
  };

  const validate = () => {
    const tempErrors: ErrorState = {};
    if (!policyData.policyTitle) tempErrors.policyTitle = 'Policy title is required';
    if (!policyData.scope) tempErrors.scope = 'Scope selection is required';
    setErrors(tempErrors);
    return !Object.values(tempErrors).some(error => error !== undefined);
  };

  const handleReset = () => {
    setPolicyData({
      policyTitle: '',
      scope: '',
    });
    setErrors({});
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
      await postOid4vpPolicy(policyData);
      setIsLoading(false);

      await dialogs.open(CustomDialog, {
        title: 'Success',
        message: 'OID4VP Policy has been created successfully.',
        isModal: true,
      }, {
        onClose: async () => navigate('/oid4vp-management/policy', { replace: true }),
      });
    } catch (err) {
      console.error('Failed to create OID4VP policy:', err);
      setIsLoading(false);
      await dialogs.open(CustomDialog, {
        title: 'Error',
        message: `Failed to create OID4VP policy: ${err}`,
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
        onSelect={handleScopeSelect}
        onSearch={handleScopeSearch}
        title="Scope Search"
        items={scopeList}
        loading={scopeLoading}
        idField="id"
      />

      <StyledContainer>
        <StyledTitle>OID4VP Policy Registration</StyledTitle>
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
              Register
            </Button>
            <Button variant="contained" color="secondary" onClick={handleReset}>
              Reset
            </Button>
            <Button variant="outlined" color="secondary" onClick={() => navigate('/oid4vp-management/policy')}>
              Cancel
            </Button>
          </Box>
        </StyledInputArea>
      </StyledContainer>
    </>
  );
};

export default Oid4vpPolicyRegistrationPage;
