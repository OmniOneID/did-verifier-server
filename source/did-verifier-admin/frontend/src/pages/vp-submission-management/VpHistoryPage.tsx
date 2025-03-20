import { Box, FormControl, InputLabel, MenuItem, Select, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { fetchSubmits } from "../../apis/vp-submit-api";
import CustomDataGrid from "../../components/data-grid/CustomDataGrid";
import FullscreenLoader from "../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../utils/error-handler';

type Props = {}

type VpSubmitRow = {
  id: string | number;
  vp: string;
  holderDID: string;
  transactionId: number;
  transactionStatus: string;
  createdAt: string;
};

type TransactionStatusType = 'ALL' | 'COMPLETED' | 'PENDING' | 'FAILED' | 'CANCELED';

const transactionStatusMapping: { [key: string]: string } = {
  COMPLETED: "Completed",
  PENDING: "Pending",
  FAILED: "Failed",
  CANCELED: "Canceled",
};

const VpHistoryPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<VpSubmitRow[]>([]);
  const [statusFilter, setStatusFilter] = useState<TransactionStatusType>('ALL');

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(() => {
    return rows.find(row => row.id === selectedRow) || null;
  }, [rows, selectedRow]);
  
  const fetchData = async () => {
    setLoading(true);
    try {
      // Pass the status filter to the API if it's not 'ALL'
      const statusParam = statusFilter !== 'ALL' ? statusFilter : null;
      const response = await fetchSubmits(paginationModel.page, paginationModel.pageSize, statusParam, null);
      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      console.error("Failed to retrieve VP Submits. ", error);
      navigate('/error', { state: { message: `Failed to retrieve VP Submits: ${error}` } });
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchData();
  }, [paginationModel, statusFilter]);

  const handleStatusFilterChange = (event: any) => {
    setStatusFilter(event.target.value as TransactionStatusType);
    // Reset to first page when filter changes
    setPaginationModel(prev => ({
      ...prev,
      page: 0
    }));
  };

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    margin: 'auto',
    marginTop: theme.spacing(1),
    padding: theme.spacing(3),
    border: 'none',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: '#ffffff',
    boxShadow: '0px 4px 8px 0px #0000001A',
  })), []);

  const StyledSubTitle = useMemo(() => styled(Typography)({
      textAlign: 'left',
      fontSize: '24px',
      fontWeight: 700,
  }), []);

  return (
    <>
      <FullscreenLoader open={loading} />
      <StyledContainer>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <StyledSubTitle>VP Submit List</StyledSubTitle>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel id="transaction-status-filter-label">Transaction Status</InputLabel>
            <Select
              labelId="transaction-status-filter-label"
              id="transaction-status-filter"
              value={statusFilter}
              label="Transaction Status"
              onChange={handleStatusFilterChange}
              size="small"
            >
              <MenuItem value="ALL">All Status</MenuItem>
              {Object.entries(transactionStatusMapping).map(([value, label]) => (
                <MenuItem key={value} value={value}>{label}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        <CustomDataGrid 
            rows={rows} 
            columns={[
              { 
                field: 'transactionStatus', 
                headerName: "Transaction Status", 
                width: 150,
                renderCell: (params) => (
                  <Box sx={{ 
                    padding: '4px 8px',
                    borderRadius: '4px',
                    backgroundColor: 
                      params.value === 'COMPLETED' ? '#e8f5e9' : 
                      params.value === 'PENDING' ? '#fff8e1' : 
                      params.value === 'FAILED' ? '#ffebee' : 
                      params.value === 'CANCELED' ? '#eceff1' : '#f5f5f5',
                    color: 
                      params.value === 'COMPLETED' ? '#2e7d32' : 
                      params.value === 'PENDING' ? '#f57c00' : 
                      params.value === 'FAILED' ? '#c62828' : 
                      params.value === 'CANCELED' ? '#546e7a' : '#212121',
                  }}>
                    {transactionStatusMapping[params.value] || params.value}
                  </Box>
                ),
              },
              { 
                field: 'holderDID', 
                headerName: "Holder DID", 
                width: 250,
                renderCell: (params) => (
                  <Typography 
                    variant="body2"
                    sx={{ 
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {params.value}
                  </Typography>
                ),
              },
              { 
                field: 'createdAt', 
                headerName: "Created At", 
                width: 180 
              },
            ]} 
            selectedRow={selectedRow} 
            setSelectedRow={setSelectedRow}
            paginationMode="server" 
            totalRows={totalRows} 
            paginationModel={paginationModel} 
            setPaginationModel={setPaginationModel}            
          />
        </StyledContainer>
    </>
  )
}

export default VpHistoryPage;