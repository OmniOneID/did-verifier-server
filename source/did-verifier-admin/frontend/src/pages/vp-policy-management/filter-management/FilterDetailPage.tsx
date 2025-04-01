import { Box, Button, FormControl, InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, styled, useTheme } from '@mui/material';
import { useDialogs } from '@toolpad/core';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { getFilter } from '../../../apis/vp-filter-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';

type Props = {}

interface FilterFormData {
    filterId: number;
    title: string;
    id: string;
    type: string;
    requiredClaims: string[];
    allowedIssuers: string[];
    displayClaims: string[];    
    presentAll: boolean;
    createdAt: string;
}

const FilterDetailData = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();
    const theme = useTheme();

    const filterId = id ? parseInt(id, 10) : null;
    const [isLoading, setIsLoading] = useState<boolean>(true); 
    const [filterData, setfilterData] = useState<FilterFormData>({
        filterId: 0,
        title: '',
        id: '',
        type: '',
        requiredClaims: [],
        allowedIssuers: [],
        displayClaims: [],        
        presentAll: false,
        createdAt : '',
    });
    
    useEffect(() => {
        const fetchData = async () => {
            if (filterId === null || isNaN(filterId)) {
                await dialogs.open(CustomDialog, { 
                    title: 'Notification', 
                    message: 'Invalid Path.', 
                    isModal: true 
                },{
                    onClose: async () => navigate('/vp-policy-management/filter-management', { replace: true }),
                });
                return;
            }

            setIsLoading(true);

            try {
                const { data } = await getFilter(filterId);
                setfilterData({
                    filterId: data.filterId,
                    title: data.title,
                    id: data.id,
                    type: data.type,
                    requiredClaims: data.requiredClaims,
                    allowedIssuers: data.allowedIssuers,
                    displayClaims: data.displayClaims,                    
                    presentAll: data.presentAll,
                    createdAt : data.createdAt,
                });                                    
                setIsLoading(false);
            } catch (err) {
                  console.error('Failed to fetch Filter information:', err);
                  setIsLoading(false);
                  navigate('/error', { state: { message: `Failed to namespace information: ${err}` } });
            }
        };

        fetchData();
    }, [filterId]);

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
            <Typography variant="h4">Filter Management</Typography>
            <StyledContainer>
                <StyledTitle>Filter Detail Information</StyledTitle>
                <StyledInputArea>
                    <TextField
                        fullWidth
                        label="Title"
                        value={filterData?.title || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />

                    <TextField
                        fullWidth
                        label="ID"
                        value={filterData?.id || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    <TextField
                        fullWidth
                        label="Type"
                        value={filterData?.type || ''}
                        variant="standard" 
                        margin="normal" 
                        slotProps={{ input: { readOnly: true } }} 
                    />
                    <Typography variant="h6" sx={{ mt: 3 }}>RequiredClaims</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell>Required Claim</TableCell> 
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {filterData.requiredClaims?.map((requiredClaims, index) => (
                                    <TableRow key={index}>
                                        <TableCell>
                                            <TextField fullWidth size="small" value={requiredClaims} />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Typography variant="h6" sx={{ mt: 3 }}>DisplayClaims</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell>Display Claim</TableCell> 
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {filterData.displayClaims?.map((displayClaims, index) => (
                                    <TableRow key={index}>
                                        <TableCell>
                                            <TextField fullWidth size="small" value={displayClaims} />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Typography variant="h6" sx={{ mt: 3 }}>AllowedIssuers</Typography>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: theme.palette.mode === "dark" ? theme.palette.background.paper : "#f5f5f5" }}>
                                    <TableCell>Allowed Issuer</TableCell> 
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {filterData.allowedIssuers?.map((allowedIssuers, index) => (
                                    <TableRow key={index}>
                                        <TableCell>
                                            <TextField fullWidth size="small" value={allowedIssuers} />
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <FormControl fullWidth margin="normal" variant='standard'>
                        <InputLabel>PresentAll</InputLabel>
                        <Select 
                            value={filterData?.presentAll === undefined ? "" : String(filterData.presentAll)} 
                            label="PresentAll"
                            slotProps={{ input: { readOnly: true } }} 
                        >
                            <MenuItem value={"true"}>true</MenuItem>
                            <MenuItem value={"false"}>false</MenuItem>
                        </Select>
                    </FormControl>

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/filter-management')}>
                            Back
                        </Button>
                        <Button variant="outlined" color="primary" onClick={() => navigate('/vp-policy-management/filter-management/filter-edit/' + filterId)}>
                            Go to Edit
                        </Button>
                    </Box>
                </StyledInputArea>
            </StyledContainer>
        </>
    )
}

export default FilterDetailData