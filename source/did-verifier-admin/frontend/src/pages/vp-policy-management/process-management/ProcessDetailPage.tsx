import { Box, Button, FormControl, InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getProcess } from '../../../apis/vp-process-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface ProcessDetailData {
    id: number;
    title: string;
    reqE2e: {
      curve: string;        
      cipher: string; 
      padding: string; 
      publicKey?: string;
    };
    authType: number;
    endpoints: string[];      
    createdAt: string;
}

const modeMapping: { [key: string]: string } = {
    0 : "FREE",
    1 : "NO_AUTHENTICATION",
    2 : "PIN",
    4 : "BIO",
    6 : "PIN or BIO",
    32774 : "PIN and BIO",
  };
  
  const eccCurveMapping: { [key: string]: string } = {
      "Secp256r1": "Secp256r1",
      "Secp256k1": "Secp256k1",    
  };
  
  const cipherMapping: { [key: string]: string } = {
      "AES-128-CBC": "AES-128-CBC",
      "AES-256-CBC": "AES-256-CBC",
      "AES-128-ECB": "AES-128-ECB",
      "AES-256-ECB": "AES-256-ECB",    
  };
  
  const paddingMapping: { [key: string]: string } = {
      "PKCS5": "PKCS5",
      "NOPAD": "NOPAD",    
  };

const ProcessDetailPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const processId = id ? parseInt(id, 10) : null;
    const [isLoading, setIsLoading] = useState<boolean>(true); 
    const [processData, setProcessData] = useState<ProcessDetailData>({
        id: 0,
        title: '',
        reqE2e: {
          curve: '',
          cipher: '',
          padding: '',
        },
        authType: 0,
        endpoints: [],
        createdAt: '',
    });
    
    useEffect(() => {
        const fetchData = async () => {
            if (processId === null || isNaN(processId)) {
                await dialogs.open(CustomDialog, { 
                    title: 'Notification', 
                    message: 'Invalid Path.', 
                    isModal: true 
                },{
                    onClose: async () => navigate('/vp-policy-management/process-management', { replace: true }),
                });
                return;
            }

            setIsLoading(true);

            try {
                const { data } = await getProcess(processId);
                setProcessData({
                    id: data.id,
                    title: data.title,
                    reqE2e: data.reqE2e || {
                      curve: '',
                      cipher: '',
                      padding: '',
                    },
                    authType: data.authType,
                    endpoints: data.endpoints || [],
                    createdAt: data.createdAt,
                });                                    
                setIsLoading(false);
            } catch (err) {
                  console.error('Failed to fetch Process information:', err);
                  setIsLoading(false);
                  navigate('/error', { state: { message: `Failed to fetch Process information: ${err}` } });
            }
        };

        fetchData();
    }, [processId, dialogs, navigate]);

    const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
        width: 500,
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
            <Typography variant="h4">Process Management</Typography>
            <StyledContainer>
                <StyledTitle>Process Detail Information</StyledTitle>
                <StyledInputArea>
                    <TextField
                        fullWidth
                        label="Title"
                        value={processData?.title || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <FormControl fullWidth margin="normal" variant='standard'>
                        <InputLabel>Auth Type</InputLabel>
                        <Select 
                            value={processData?.authType === undefined ? "" : String(processData.authType)} 
                            label="Auth Type"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            {Object.entries(modeMapping).map(([key, value]) => (
                                <MenuItem key={key} value={key}>{value}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    
                    {/* ReqE2e Section */}
                    <Typography variant="h6" sx={{ mt: 3 }}>ReqE2e Information</Typography>
                    <TableContainer component={Paper} sx={{ mb: 3 }}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Property</TableCell>
                                    <TableCell>Value</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Curve</TableCell>
                                    <TableCell>
                                        <FormControl fullWidth size="small">
                                            <Select
                                                value={processData.reqE2e?.curve || ''}
                                                inputProps={{ readOnly: true }}
                                            >
                                                {Object.entries(eccCurveMapping).map(([key, value]) => (
                                                    <MenuItem key={key} value={key}>{value}</MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Cipher</TableCell>
                                    <TableCell>
                                        <FormControl fullWidth size="small">
                                            <Select
                                                value={processData.reqE2e?.cipher || ''}
                                                inputProps={{ readOnly: true }}
                                            >
                                                {Object.entries(cipherMapping).map(([key, value]) => (
                                                    <MenuItem key={key} value={key}>{value}</MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Padding</TableCell>
                                    <TableCell>
                                        <FormControl fullWidth size="small">
                                            <Select
                                                value={processData.reqE2e?.padding || ''}
                                                inputProps={{ readOnly: true }}
                                            >
                                                {Object.entries(paddingMapping).map(([key, value]) => (
                                                    <MenuItem key={key} value={key}>{value}</MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    </TableCell>
                                </TableRow>
                                {processData.reqE2e?.publicKey && (
                                <TableRow>
                                    <TableCell>Public Key</TableCell>
                                    <TableCell>
                                        <TextField 
                                            fullWidth 
                                            size="small" 
                                            value={processData.reqE2e.publicKey} 
                                            slotProps={{ input: { readOnly: true } }} 
                                        />
                                    </TableCell>
                                </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    <Typography variant="h6" sx={{ mt: 3 }}>Endpoints</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{backgroundColor: "#f5f5f5"}}>
                                    <TableCell>Endpoint</TableCell> 
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {processData.endpoints?.map((endpoint, index) => (
                                    <TableRow key={index}>
                                        <TableCell>
                                            <TextField 
                                                fullWidth 
                                                size="small" 
                                                value={endpoint} 
                                                slotProps={{ input: { readOnly: true } }} 
                                                />
                                        </TableCell>
                                    </TableRow>
                                ))}
                                {processData.endpoints?.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={1} align="center">No endpoints available</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/process-management')}>
                            Back
                        </Button>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/process-management/process-edit/' + processId)}>
                            Go to Edit
                        </Button>
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    )
}

export default ProcessDetailPage