import { Box, Typography, styled } from '@mui/material';
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

const transactionStatusMapping: { [key: string]: string } = {
  COMPLETED: "Completed",
  PENDING: "Pending",
  FAILED: "Failed",
  EXPIRED: "Expired"
};

const VpHistoryPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<VpSubmitRow[]>([]);

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(() => {
    return rows.find(row => row.id === selectedRow) || null;
  }, [rows, selectedRow]);
  
  useEffect(() => {
    setLoading(true);
    // 실제 API 호출 부분
    fetchSubmits(paginationModel.page, paginationModel.pageSize, null, null)
      .then((response) => {
        setRows(response.data.content);
        setTotalRows(response.data.totalElements);
      })
      .catch((error) => {
        console.error("Failed to retrieve VP Submits. ", error);
        navigate('/error', { state: { message: `Failed to retrieve VP Submits: ${error}` } });
      })
      .finally(() => setLoading(false));
      
    /*
    setTimeout(() => {
      const mockData = {
        content: [
          {
            id: 1,
            vp: '{"@context":["https://www.w3.org/2018/credentials/v1"],"holder":"did:omnione:12345abcde","type":["VerifiablePresentation"]}',
            holderDID: 'did:omnione:12345abcde',
            transactionId: 101,
            transactionStatus: 'COMPLETED',
            createdAt: '2025-03-15 09:30:22'
          },
          {
            id: 2,
            vp: '{"@context":["https://www.w3.org/2018/credentials/v1"],"holder":"did:omnione:67890fghij","type":["VerifiablePresentation"]}',
            holderDID: 'did:omnione:67890fghij',
            transactionId: 102,
            transactionStatus: 'PENDING',
            createdAt: '2025-03-16 14:25:41'
          },
          {
            id: 3,
            vp: '{"@context":["https://www.w3.org/2018/credentials/v1"],"holder":"did:omnione:klmno12345","type":["VerifiablePresentation"]}',
            holderDID: 'did:omnione:klmno12345',
            transactionId: 103,
            transactionStatus: 'FAILED',
            createdAt: '2025-03-17 11:12:05'
          },
          {
            id: 4,
            vp: '{"@context":["https://www.w3.org/2018/credentials/v1"],"holder":"did:omnione:pqrst67890","type":["VerifiablePresentation"]}',
            holderDID: 'did:omnione:pqrst67890',
            transactionId: 104,
            transactionStatus: 'COMPLETED',
            createdAt: '2025-03-17 16:05:33'
          },
          {
            id: 5,
            vp: '{"@context":["https://www.w3.org/2018/credentials/v1"],"holder":"did:omnione:uvwxy12345","type":["VerifiablePresentation"]}',
            holderDID: 'did:omnione:uvwxy12345',
            transactionId: 105,
            transactionStatus: 'EXPIRED',
            createdAt: '2025-03-18 08:42:17'
          }
        ],
        totalElements: 5
      };
      
      setRows(mockData.content);
      setTotalRows(mockData.totalElements);
      setLoading(false);
    }, 1000);
    */
  }, [paginationModel]);

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
        <StyledSubTitle>VP Submit List</StyledSubTitle>
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
                      params.value === 'EXPIRED' ? '#eceff1' : '#f5f5f5',
                    color: 
                      params.value === 'COMPLETED' ? '#2e7d32' : 
                      params.value === 'PENDING' ? '#f57c00' : 
                      params.value === 'FAILED' ? '#c62828' : 
                      params.value === 'EXPIRED' ? '#546e7a' : '#212121',
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