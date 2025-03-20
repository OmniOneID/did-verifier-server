import { Box, Link, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { deleteService, fetchServices } from "../../../apis/vp-payload-api";
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../../utils/error-handler';

type Props = {}

type ServiceRow = {
  id: string | number;
  service: string;
  device: string;
  locked: boolean;
  mode: string;
};

const modeMapping: { [key: string]: string } = {
  Direct: "Direct",
  Indirect: "inDirect",
  Proxy: "Proxy",
};

const lockedMapping: { [key: string]: string } = {
  true: "locked",
  false: "unlocked",
};

const ServiceManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<ServiceRow[]>([]);

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(() => {
    return rows.find(row => row.id === selectedRow) || null;
  }, [rows, selectedRow]);
  
  const handleDelete = async () => {
    const id = selectedRowData?.id as number;
    if (id) {
      const result = await dialogs.open(CustomConfirmDialog, {
        title: 'Confirmation',
        message: 'Are you sure you want to delete Service?',
        isModal: true,
      });
  
      if (result) {
        setLoading(true);
        deleteService(id)
          .then(() => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Service delete completed.',
              isModal: true,
            }, {
              onClose: async () => {
                setPaginationModel(prev => ({ ...prev }));
              },
            });
          })
          .catch((error) => {
            console.error("Failed to delete Service. ", error);
            const result = dialogs.open(CustomConfirmDialog, {
              title: 'Confirmation',
              message: formatErrorMessage(error, "Failed to delete Service!! "),
              isModal: true,
            });            
          })
          .finally(() => setLoading(false));
      }
    }
  };
  
  useEffect(() => {
    setLoading(true);
    fetchServices(paginationModel.page, paginationModel.pageSize, null, null)
      .then((response) => {
        setRows(response.data.content);
        setTotalRows(response.data.totalElements);
      })
      .catch((error) => {
        console.error("Failed to retrieve Services. ", error);
        navigate('/error', { state: { message: `Failed to retrieve Services: ${error}` } });
      })
      .finally(() => setLoading(false));
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
        <StyledSubTitle>Service Management</StyledSubTitle>
        <CustomDataGrid 
            rows={rows} 
            columns={[
              { 
                field: 'service', 
                headerName: "Service Name", 
                width: 150,
                renderCell: (params) => (
                  <Link 
                    component="button"
                    variant='body2'
                    onClick={() => navigate(`/vp-policy-management/service-management/${params.row.id}`)}
                    sx={{ cursor: 'pointer', color: 'primary.main' }}
                  >
                    {params.value}
                  </Link>),
              },
              { field: 'device', headerName: "Device", width: 100},
              { field: 'locked', headerName: "Lock Status", width: 100,
                renderCell: (params) => lockedMapping[params.value],
              },
              { field: 'mode', headerName: "Submissin Mode", width: 200,
                renderCell: (params) => modeMapping[params.value],
              },
            ]} 
            selectedRow={selectedRow} 
            setSelectedRow={setSelectedRow}
            onEdit={() => {
              if (selectedRowData) {
                navigate(`/vp-policy-management/service-management/service-edit/${selectedRowData.id}`);
              }
            }}
            onRegister={() => navigate('/vp-policy-management/service-management/service-registration')}
            onDelete={handleDelete}
            additionalButtons={[
            
            ]}
            paginationMode="server" 
            totalRows={totalRows} 
            paginationModel={paginationModel} 
            setPaginationModel={setPaginationModel} 
          />
        </StyledContainer>
    </>
  )
}

export default ServiceManagementPage