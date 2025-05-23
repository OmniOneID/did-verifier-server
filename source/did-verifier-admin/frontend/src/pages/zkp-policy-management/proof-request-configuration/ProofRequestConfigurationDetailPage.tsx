import React, { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, FormControl, InputLabel, Select, MenuItem, styled, Button
} from '@mui/material';
import { useParams, useNavigate } from 'react-router';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import { getProofRequest } from '../../../apis/zkp-proof-api';
import { useDialogs } from '@toolpad/core';
import CustomDialog from '../../../components/dialog/CustomDialog';
import { formatErrorMessage } from '../../../utils/error-handler';

const ProofRequestConfigurationDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [data, setData] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  const numericId = id ? parseInt(id, 10) : null;
  useEffect(() => {
    const fetchData = async () => {
      if (numericId === null || isNaN(numericId)) {
        await dialogs.open(CustomDialog, {
          title: "Notification",
          message: "Invalid Path.",
          isModal: true,
        }, {
          onClose: async () =>
            navigate("/zkp-policy-management/proof-request-configuration", { replace: true }),
        });
        return;
      }

      setIsLoading(true);
      try {
        const { data } = await getProofRequest(numericId);
        setData(data);
        setIsLoading(false);
      } catch (err) {
        console.error("Failed to fetch Proof Request Configuration:", err);
        setIsLoading(false);
        navigate("/error", {
          state: { message: formatErrorMessage(err, "Failed to load Proof Request Configuration information.") },
        });
      }
    };

    fetchData();
  }, [numericId]);

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 900,
    margin: 'auto',
    marginTop: theme.spacing(2),
    padding: theme.spacing(3),
    backgroundColor: '#ffffff',
    boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.1)',
    borderRadius: theme.shape.borderRadius,
  })), []);

  const renderAttributeTable = () => {
    const entries = Object.entries(data?.requestedAttributes || {});
    return (
      <TableContainer component={Paper} sx={{ my: 2 }}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
              <TableCell>Attribute Name</TableCell>
              <TableCell>Credential Definitions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entries.map(([key, value]: any, index) => (
              <TableRow key={index}>
                <TableCell>{value.name}</TableCell>
                <TableCell>
                  {value.restrictions.map((r: any, i: number) => (
                    <Typography key={i}>{r.credDefId}</Typography>
                  ))}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  };

  const renderPredicateTable = () => {
    const entries = Object.entries(data?.requestedPredicates || {});
    return (
      <TableContainer component={Paper} sx={{ my: 2 }}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
              <TableCell>Attribute Name</TableCell>
              <TableCell>Predicate Type</TableCell>
              <TableCell>Predicate Value</TableCell>
              <TableCell>Credential Definitions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entries.map(([key, value]: any, index) => (
              <TableRow key={index}>
                <TableCell>{value.name}</TableCell>
                <TableCell>{value.pType}</TableCell>
                <TableCell>{value.pValue}</TableCell>
                <TableCell>
                  {value.restrictions.map((r: any, i: number) => (
                    <Typography key={i}>{r.credDefId}</Typography>
                  ))}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  };

  return (
    <>
      <FullscreenLoader open={isLoading} />
      <Typography variant="h4">Proof Request Configuration</Typography>
      {data && (
        <StyledContainer>
          <Typography variant="h4" gutterBottom>Proof Request Detail Information</Typography>

          <TextField
            label="Name"
            fullWidth
            size="small"
            margin="normal"
            value={data.name}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
          />

          <TextField
            label="Version"
            fullWidth
            size="small"
            margin="normal"
            value={data.version}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
          />

          <TextField
            label="Curve"
            fullWidth
            size="small"
            margin="normal"
            value={data.curve}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
          />

          <TextField
            label="Cipher"
            fullWidth
            size="small"
            margin="normal"
            value={data.cipher}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
          />

          <TextField
            label="Padding"
            fullWidth
            size="small"
            margin="normal"
            value={data.padding}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
          />

          <TextField
            label="Created At"
            fullWidth
            size="small"
            margin="normal"
            value={data.createdAt}
            slotProps={{ input: { readOnly: true } }}
            sx={{ width: '60%' }}
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

          <Typography variant="h6" sx={{ mt: 4 }}>Requested Attributes</Typography>
          {renderAttributeTable()}

          <Typography variant="h6" sx={{ mt: 4 }}>Requested Predicates</Typography>
          {renderPredicateTable()}

          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 4 }}>
            <Button variant="contained" color="primary" onClick={() => navigate(-1)}>Back</Button>
          </Box>
        </StyledContainer>
      )}
    </>
  );
};

export default ProofRequestConfigurationDetailPage;
