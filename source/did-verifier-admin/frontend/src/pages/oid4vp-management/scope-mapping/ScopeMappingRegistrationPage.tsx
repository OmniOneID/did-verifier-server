import {
  Box, Button, FormControlLabel, IconButton, MenuItem, Select, Switch,
  TextField, Typography, styled, InputLabel, FormControl, SelectChangeEvent,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useDialogs } from '@toolpad/core';
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { postScopeMapping } from '../../../apis/oid4vp-api';
import { getVcSchemes } from '../../../apis/vp-filter-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import SearchDialog from '../../../components/dialog/SearchDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

interface ClaimEntry {
  id: string;
  path: string;
  values: string;
}

interface MdocClaimEntry {
  id: string;
  namespace: string;
  claimName: string;
}

interface CredentialQueryData {
  credentialId: string;
  format: string;
  metaValues: string;
  claims: ClaimEntry[];
  mdocClaims: MdocClaimEntry[];
}

interface ScopeMappingFormData {
  scope: string;
  description: string;
  enabled: boolean;
}

interface ErrorState {
  scope?: string;
  credentialId?: string;
}

// TAS VC schema shapes (same as FilterEditPage's getVcSchemes()/extractClaimsFromSchema —
// reused here instead of the separate /list/admin/v1/credential-schemas/all endpoint since
// that one's response shape isn't verified against a live TAS).
interface TasClaimItem {
  id: string;
}

interface TasNamespace {
  id: string;
}

interface TasClaim {
  items: TasClaimItem[];
  namespace: TasNamespace;
}

interface VcSchema {
  schemaId: string;
  title: string;
  vcSchema: {
    credentialSubject: {
      claims: TasClaim[];
    };
  };
}

const FORMATS = ['dc+sd-jwt-did', 'vc+sd-jwt', 'opendid_vc', 'mso_mdoc', 'mso_mdoc-did'] as const;

const isMdocFormat = (format: string) => format === 'mso_mdoc' || format === 'mso_mdoc-did';

const getMetaKey = (format: string) => {
  if (format === 'opendid_vc') return 'credential_schema_id_values';
  if (isMdocFormat(format)) return 'doctype_value';
  return 'vct_values';
};

const getMetaHint = (format: string) => {
  if (format === 'opendid_vc') return 'Credential Schema ID list (use Schema Search)';
  if (isMdocFormat(format)) return 'mDoc docType (e.g. org.iso.18013.5.1.mDL)';
  return 'VCT values for dc+sd-jwt-did / vc+sd-jwt';
};

const ScopeMappingRegistrationPage = () => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [errors, setErrors] = useState<ErrorState>({});

  const [formData, setFormData] = useState<ScopeMappingFormData>({
    scope: '',
    description: '',
    enabled: true,
  });

  const [credQuery, setCredQuery] = useState<CredentialQueryData>({
    credentialId: '',
    format: 'dc+sd-jwt-did',
    metaValues: '',
    claims: [],
    mdocClaims: [],
  });

  const [tasSearchOpen, setTasSearchOpen] = useState(false);
  const [tasList, setTasList] = useState<{ id: string; title: string; vcSchema?: VcSchema['vcSchema'] }[]>([]);
  const [tasLoading, setTasLoading] = useState(false);

  // Explicit Full/Selective disclosure toggle for dc+sd-jwt-family formats.
  // Decoupled from credQuery.claims so "Selective" with an empty claim list
  // (before the user has added a row) doesn't silently snap back to "Full".
  const [selectiveMode, setSelectiveMode] = useState(false);

  const isOpendidVc = credQuery.format === 'opendid_vc';
  const isMdoc = isMdocFormat(credQuery.format);

  const buildDcqlJson = () => {
    const cred: any = {};
    if (credQuery.credentialId) cred.id = credQuery.credentialId;
    cred.format = credQuery.format;

    if (credQuery.metaValues.trim()) {
      const metaKey = getMetaKey(credQuery.format);
      if (isMdoc) {
        cred.meta = { [metaKey]: credQuery.metaValues.trim() };
      } else {
        try {
          cred.meta = { [metaKey]: JSON.parse(credQuery.metaValues) };
        } catch {
          cred.meta = { [metaKey]: [credQuery.metaValues.trim()] };
        }
      }
    }

    if (isMdoc && credQuery.mdocClaims.length > 0) {
      const claims = credQuery.mdocClaims
        .filter(c => c.namespace.trim() && c.claimName.trim())
        .map(c => ({
          ...(c.id ? { id: c.id } : {}),
          namespace: c.namespace.trim(),
          claim_name: c.claimName.trim(),
        }));
      if (claims.length > 0) cred.claims = claims;
    } else if (!isMdoc && credQuery.claims.length > 0) {
      const claims: any[] = [];
      credQuery.claims.forEach(c => {
        if (!c.path.trim()) return;
        const claim: any = {};
        if (c.id) claim.id = c.id;
        // path/values must always be arrays per DCQL — JSON.parse("1") succeeds and
        // returns the number 1, not an array, so a bare non-array result must still
        // fall back to wrapping the raw input in an array.
        try {
          const parsed = JSON.parse(c.path);
          claim.path = Array.isArray(parsed) ? parsed : [c.path];
        } catch { claim.path = [c.path]; }
        if (c.values.trim()) {
          try {
            const parsed = JSON.parse(c.values);
            claim.values = Array.isArray(parsed) ? parsed : [c.values];
          } catch { claim.values = [c.values]; }
        }
        claims.push(claim);
      });
      if (claims.length > 0) cred.claims = claims;
    }

    return { credentials: [cred] };
  };

  const jsonPreview = useMemo(() => {
    try {
      return JSON.stringify(buildDcqlJson(), null, 2);
    } catch {
      return '{}';
    }
  }, [credQuery]);

  const handleFormatChange = (e: SelectChangeEvent<string>) => {
    const format = e.target.value;
    setCredQuery(prev => ({
      ...prev,
      format,
      metaValues: '',
      claims: [],
      mdocClaims: [],
    }));
    setSelectiveMode(false);
  };

  const handleDisclosureModeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const checked = e.target.checked;
    setSelectiveMode(checked);
    if (!checked) {
      setCredQuery(prev => ({ ...prev, claims: [] }));
    }
  };

  const addMdocClaim = () => {
    setCredQuery(prev => ({
      ...prev,
      mdocClaims: [...prev.mdocClaims, { id: '', namespace: '', claimName: '' }],
    }));
  };

  const updateMdocClaim = (index: number, field: keyof MdocClaimEntry, value: string) => {
    setCredQuery(prev => ({
      ...prev,
      mdocClaims: prev.mdocClaims.map((c, i) => i === index ? { ...c, [field]: value } : c),
    }));
  };

  const removeMdocClaim = (index: number) => {
    setCredQuery(prev => ({
      ...prev,
      mdocClaims: prev.mdocClaims.filter((_, i) => i !== index),
    }));
  };

  const addClaim = () => {
    setCredQuery(prev => ({
      ...prev,
      claims: [...prev.claims, { id: '', path: '', values: '' }],
    }));
  };

  const updateClaim = (index: number, field: keyof ClaimEntry, value: string) => {
    setCredQuery(prev => ({
      ...prev,
      claims: prev.claims.map((c, i) => i === index ? { ...c, [field]: value } : c),
    }));
  };

  const removeClaim = (index: number) => {
    setCredQuery(prev => ({
      ...prev,
      claims: prev.claims.filter((_, i) => i !== index),
    }));
  };

  // Extracts full claim codes ("namespace.itemId") from a TAS VC schema — same shape and
  // logic as FilterEditPage's extractClaimsFromSchema.
  const extractClaimsFromCredentialSchema = (schema: VcSchema['vcSchema'] | undefined): string[] => {
    if (!schema) return [];
    const claims: string[] = [];
    try {
      schema.credentialSubject.claims.forEach((claim) => {
        claim.items.forEach((item) => {
          claims.push(`${claim.namespace.id}.${item.id}`);
        });
      });
    } catch (error) {
      console.error('Error extracting claims from TAS VC schema:', error);
    }
    return claims;
  };

  const handleTasSearch = async () => {
    setTasSearchOpen(true);
    try {
      setTasLoading(true);
      const response = await getVcSchemes();
      const schemas: VcSchema[] = response?.data?.vcSchemaList || [];
      setTasList(schemas.map((schema) => ({
        id: schema.schemaId,
        title: schema.title || schema.schemaId,
        vcSchema: schema.vcSchema,
      })));
    } catch (err) {
      setTasList([]);
      setTasSearchOpen(false);
      await dialogs.open(CustomDialog, {
        title: 'Error',
        message: `Failed to load credential schemas from TAS: ${err}`,
        isModal: true,
      });
    } finally {
      setTasLoading(false);
    }
  };

  // Selecting a schema fully replaces metaValues/claims rather than appending — one
  // schema selection determines exactly one consistent (schema id, claim set) pair.
  const handleTasSelect = (selected: any) => {
    const schemaId = selected.id || selected.title;
    const extractedCodes = extractClaimsFromCredentialSchema(selected.vcSchema);
    const newClaims: ClaimEntry[] = extractedCodes.map(code => ({
      id: '', path: JSON.stringify([code]), values: '',
    }));

    setCredQuery(prev => ({
      ...prev,
      metaValues: JSON.stringify([schemaId]),
      claims: newClaims,
    }));
    setSelectiveMode(true);
  };

  // Displays an opendid_vc claim's full code without the JSON array wrapper (["code"] -> code).
  const displayOpendidVcClaimCode = (path: string): string => {
    try {
      const parsed = JSON.parse(path);
      return Array.isArray(parsed) && parsed.length > 0 ? String(parsed[0]) : path;
    } catch {
      return path;
    }
  };

  const validate = () => {
    const tempErrors: ErrorState = {};
    if (!formData.scope) tempErrors.scope = 'Scope is required';
    if (!credQuery.credentialId.trim()) {
      tempErrors.credentialId = 'Credential ID is required';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(credQuery.credentialId)) {
      tempErrors.credentialId = 'Only alphanumeric characters, underscores, and hyphens allowed';
    }
    setErrors(tempErrors);
    return Object.keys(tempErrors).length === 0;
  };

  const handleReset = () => {
    setErrors({});
    setFormData({ scope: '', description: '', enabled: true });
    setCredQuery(prev => ({
      credentialId: '',
      format: prev.format,
      metaValues: '',
      claims: [],
      mdocClaims: [],
    }));
    setSelectiveMode(false);
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
      const dcqlJson = buildDcqlJson();
      await postScopeMapping({
        scope: formData.scope,
        description: formData.description,
        enabled: formData.enabled,
        dcqlQuery: JSON.stringify(dcqlJson),
      });
      setIsLoading(false);

      await dialogs.open(CustomDialog, {
        title: 'Success',
        message: 'Scope Mapping has been created successfully.',
        isModal: true,
      }, {
        onClose: async () => navigate('/oid4vp-management/scope-mapping', { replace: true }),
      });
    } catch (err) {
      console.error('Failed to create scope mapping:', err);
      setIsLoading(false);
      await dialogs.open(CustomDialog, {
        title: 'Error',
        message: `Failed to create scope mapping: ${err}`,
        isModal: true,
      });
    }
  };

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 900,
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

  const SectionLabel = useMemo(() => styled(Typography)(({ theme }) => ({
    fontSize: '13px',
    fontWeight: 700,
    color: theme.palette.primary.main,
    textTransform: 'uppercase' as const,
    borderLeft: `3px solid ${theme.palette.primary.main}`,
    paddingLeft: theme.spacing(1),
    marginTop: theme.spacing(3),
    marginBottom: theme.spacing(1.5),
  })), []);

  return (
    <>
      <FullscreenLoader open={isLoading} />
      <Typography variant="h4">OID4VP Management</Typography>

      <SearchDialog
        open={tasSearchOpen}
        onClose={() => setTasSearchOpen(false)}
        onSelect={handleTasSelect}
        title="TAS Credential Schema Search"
        items={tasList}
        loading={tasLoading}
        idField="id"
      />

      {/* Basic Info */}
      <StyledContainer>
        <StyledTitle>Scope Mapping Registration</StyledTitle>

        <SectionLabel>Basic Information</SectionLabel>
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
          <TextField
            fullWidth required
            label="Scope"
            value={formData.scope}
            variant="outlined" size="small"
            onChange={(e) => setFormData(prev => ({ ...prev, scope: e.target.value }))}
            error={!!errors.scope}
            helperText={errors.scope || 'Unique scope identifier'}
          />
          <TextField
            fullWidth
            label="Description"
            value={formData.description}
            variant="outlined" size="small"
            onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
          />
        </Box>
        <FormControlLabel
          control={
            <Switch
              checked={formData.enabled}
              onChange={(e) => setFormData(prev => ({ ...prev, enabled: e.target.checked }))}
            />
          }
          label="Enabled"
          sx={{ mt: 1 }}
        />
      </StyledContainer>

      {/* DCQL Query Builder */}
      <StyledContainer sx={{ mt: 2 }}>
        <StyledTitle sx={{ fontSize: '18px' }}>CredentialQuery</StyledTitle>

        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mt: 2 }}>
          <TextField
            fullWidth required
            label="Credential ID"
            value={credQuery.credentialId}
            variant="outlined" size="small"
            onChange={(e) => setCredQuery(prev => ({ ...prev, credentialId: e.target.value }))}
            error={!!errors.credentialId}
            helperText={errors.credentialId || 'Unique credential identifier'}
          />
          <FormControl fullWidth size="small">
            <InputLabel>Format</InputLabel>
            <Select value={credQuery.format} label="Format" onChange={handleFormatChange}>
              {FORMATS.map(f => <MenuItem key={f} value={f}>{f}</MenuItem>)}
            </Select>
          </FormControl>
        </Box>

        {/* Meta */}
        <SectionLabel>Meta</SectionLabel>
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
          <TextField
            fullWidth
            label="Meta Key"
            value={getMetaKey(credQuery.format)}
            variant="outlined" size="small"
            InputProps={{ readOnly: true }}
            sx={{ bgcolor: '#fafafa' }}
            helperText={getMetaHint(credQuery.format)}
          />
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              fullWidth
              label="Values (JSON Array)"
              value={credQuery.metaValues}
              variant="outlined" size="small"
              onChange={(e) => setCredQuery(prev => ({ ...prev, metaValues: e.target.value }))}
              placeholder='["value1", "value2"]'
              helperText={isOpendidVc ? 'Set via Schema Search' : 'JSON array format'}
              InputProps={{ readOnly: isOpendidVc }}
              sx={isOpendidVc ? { bgcolor: '#fafafa' } : undefined}
            />
            {isOpendidVc && (
              <Button variant="contained" size="small" sx={{ minWidth: 110, height: 40 }}
                onClick={handleTasSearch}>
                Schema Search
              </Button>
            )}
          </Box>
        </Box>

        {/* Claims — varies by format */}
        {isMdoc && (
          <>
            <SectionLabel>Claims (Optional) — Namespace + Claim Name</SectionLabel>
            <Box sx={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr 40px', gap: 1, mb: 1 }}>
              <Typography variant="caption" color="text.secondary">ID</Typography>
              <Typography variant="caption" color="text.secondary">Namespace</Typography>
              <Typography variant="caption" color="text.secondary">Claim Name</Typography>
              <span />
            </Box>
            {credQuery.mdocClaims.map((claim, idx) => (
              <Box key={idx} sx={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr 40px', gap: 1, mb: 1 }}>
                <TextField
                  size="small" placeholder="claim_id"
                  value={claim.id}
                  onChange={(e) => updateMdocClaim(idx, 'id', e.target.value)}
                />
                <TextField
                  size="small" placeholder="org.iso.18013.5.1"
                  value={claim.namespace}
                  onChange={(e) => updateMdocClaim(idx, 'namespace', e.target.value)}
                />
                <TextField
                  size="small" placeholder="family_name"
                  value={claim.claimName}
                  onChange={(e) => updateMdocClaim(idx, 'claimName', e.target.value)}
                />
                <IconButton size="small" color="error" onClick={() => removeMdocClaim(idx)}>
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Box>
            ))}
            <Button
              variant="outlined" size="small" startIcon={<AddIcon />}
              onClick={addMdocClaim} sx={{ mt: 1 }}
            >
              Add Claim
            </Button>
          </>
        )}
        {!isMdoc && (
          <>
            <SectionLabel>Disclosure Mode</SectionLabel>
            <FormControlLabel
              control={
                <Switch
                  checked={selectiveMode}
                  onChange={handleDisclosureModeChange}
                />
              }
              label={selectiveMode ? 'Selective claims' : 'Full credential'}
            />
            <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 1 }}>
              {selectiveMode
                ? 'Only the claims listed below are requested from the wallet.'
                : 'No claims are specified, so the wallet discloses the full credential.'}
            </Typography>
            {selectiveMode && isOpendidVc && (
              <>
                <SectionLabel>Claims</SectionLabel>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 1 }}>
                  Claims are populated by selecting a schema via Schema Search above — manual entry isn't
                  supported for opendid_vc. Remove individual claims with the delete button if needed.
                </Typography>
                {credQuery.claims.length === 0 ? (
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    No claims yet — use Schema Search to populate.
                  </Typography>
                ) : (
                  credQuery.claims.map((claim, idx) => (
                    <Box key={idx} sx={{ display: 'grid', gridTemplateColumns: '1fr 40px', gap: 1, mb: 1 }}>
                      <TextField
                        size="small"
                        value={displayOpendidVcClaimCode(claim.path)}
                        InputProps={{ readOnly: true }}
                        sx={{ bgcolor: '#fafafa' }}
                      />
                      <IconButton size="small" color="error" onClick={() => removeClaim(idx)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ))
                )}
              </>
            )}
            {selectiveMode && !isOpendidVc && (
              <>
                <SectionLabel>Claims</SectionLabel>
                <Box sx={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr 40px', gap: 1, mb: 1 }}>
                  <Typography variant="caption" color="text.secondary">ID</Typography>
                  <Typography variant="caption" color="text.secondary">Path (JSON Array)</Typography>
                  <Typography variant="caption" color="text.secondary">Values (Optional)</Typography>
                  <span />
                </Box>
                {credQuery.claims.map((claim, idx) => (
                  <Box key={idx} sx={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr 40px', gap: 1, mb: 1 }}>
                    <TextField
                      size="small" placeholder="claim_id"
                      value={claim.id}
                      onChange={(e) => updateClaim(idx, 'id', e.target.value)}
                    />
                    <TextField
                      size="small" placeholder='["fieldName"]'
                      value={claim.path}
                      onChange={(e) => updateClaim(idx, 'path', e.target.value)}
                    />
                    <TextField
                      size="small" placeholder='["val1","val2"]'
                      value={claim.values}
                      onChange={(e) => updateClaim(idx, 'values', e.target.value)}
                    />
                    <IconButton size="small" color="error" onClick={() => removeClaim(idx)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                ))}
                <Button
                  variant="outlined" size="small" startIcon={<AddIcon />}
                  onClick={addClaim} sx={{ mt: 1 }}
                >
                  Add Claim
                </Button>
              </>
            )}
          </>
        )}

        {/* JSON Preview */}
        <SectionLabel>DCQL Query JSON Preview</SectionLabel>
        <TextField
          fullWidth multiline
          minRows={6} maxRows={15}
          value={jsonPreview}
          variant="outlined"
          InputProps={{
            readOnly: true,
            sx: { fontFamily: 'monospace', fontSize: '12px', bgcolor: '#fafafa' },
          }}
        />
      </StyledContainer>

      <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3, mb: 4 }}>
        <Button variant="contained" color="primary" onClick={handleSubmit}>
          Register
        </Button>
        <Button variant="contained" color="secondary" onClick={handleReset}>
          Reset
        </Button>
        <Button variant="outlined" color="secondary" onClick={() => navigate('/oid4vp-management/scope-mapping')}>
          Cancel
        </Button>
      </Box>
    </>
  );
};

export default ScopeMappingRegistrationPage;
