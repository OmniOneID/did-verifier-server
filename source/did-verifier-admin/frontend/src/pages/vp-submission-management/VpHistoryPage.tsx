import { Box, FormControl, InputLabel, MenuItem, Select, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { fetchSubmits } from "../../apis/vp-submit-api";
import CustomDataGrid from "../../components/data-grid/CustomDataGrid";
import CustomDialog from '../../components/dialog/CustomDialog';
import FullscreenLoader from "../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../utils/error-handler';

type Props = {}

type VpSubmitRow = {
  id: string | number;
  vp: string;
  holderDID: string;
  transactionId: number;
  txId: string;
  transactionStatus: string;
  createdAt: string;
};

type TransactionStatusType = 'ALL' | 'COMPLETED' | 'PENDING' | 'FAILED';

const transactionStatusMapping: { [key: string]: string } = {
  COMPLETED: "Completed",
  PENDING: "Pending",
  FAILED: "Failed",
};

const VpHistoryPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<VpSubmitRow[]>([]);
  const [statusFilter, setStatusFilter] = useState<TransactionStatusType>('ALL');
  const [searchText, setSearchText] = useState<string>('');
  const [selectedSearch, setSelectedSearch] = useState<string>('');

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(() => {
    return rows.find(row => row.id === selectedRow) || null;
  }, [rows, selectedRow]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      let searchKey = null;
      let searchValue = null;

      if (statusFilter !== 'ALL') {
        searchKey = 'status';
        searchValue = statusFilter;
      }

      const response = await fetchSubmits(
        paginationModel.page,
        paginationModel.pageSize,
        searchKey,
        searchValue
      );

      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      console.error("Failed to retrieve VP Submits. ", error);
      navigate('/error', { state: { message: `Failed to retrieve VP Submits: ${error}` } });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.page, paginationModel.pageSize, statusFilter, navigate]);

  const getData = useCallback(async () => {
    setLoading(true);
    try {
      const searchKey = statusFilter !== 'ALL' ? 'status' : null;
      const searchValue = statusFilter !== 'ALL' ? statusFilter : null;

      const response = await fetchSubmits(
        0,
        paginationModel.pageSize,
        searchKey,
        searchValue
      );

      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
      setPaginationModel((prev) => ({ ...prev, page: 0 }));
    } catch (err) {
      await dialogs.open(CustomDialog, {
        title: 'Notification',
        message: formatErrorMessage(err, "Failed to fetch VP Submit list"),
        isModal: true,
      });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.pageSize, statusFilter, dialogs]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleStatusFilterChange = (event: any) => {
    setStatusFilter(event.target.value as TransactionStatusType);
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
                      params.value === 'FAILED' ? '#ffebee' : '#f5f5f5',
                    color:
                      params.value === 'COMPLETED' ? '#2e7d32' :
                      params.value === 'PENDING' ? '#f57c00' :
                      params.value === 'FAILED' ? '#c62828' : '#212121',
                  }}>
                    {transactionStatusMapping[params.value] || params.value}
                  </Box>
                ),
              },
              {
                field: 'txId',
                headerName: "Transaction ID",
                width: 200,
                renderCell: (params) => (
                  <Typography
                    variant="body2"
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      fontFamily: 'monospace',
                      fontSize: '12px',
                      color: '#1976d2',
                    }}
                  >
                    {params.value}
                  </Typography>
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
                      color: params.value === 'N/A' ? '#9e9e9e' : 'inherit',
                      fontStyle: params.value === 'N/A' ? 'italic' : 'normal',
                    }}
                  >
                    {params.value || 'N/A'}
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
            searchText={searchText}
            setSearchText={setSearchText}
            selectedSearch={selectedSearch}
            setSelectedSearch={setSelectedSearch}
            onRefresh={getData}
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
