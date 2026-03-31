import { Box, Link, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { deleteService, fetchServices } from "../../apis/vp-payload-api";
import CustomDataGrid from "../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../components/dialog/CustomDialog';
import FullscreenLoader from "../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../utils/error-handler';

type Props = {}

type ServiceRow = {
  id: string | number;
  service: string;
  device: string;
  locked: boolean;
  mode: string;
  offerType: string;
  policyCount: number;
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

const offerTypeMapping: { [key: string]: string} = {
  VerifyOffer: "VP",
  VerifyProofOffer: "ZKP",
  IssueOffer: "-",
};

const ServiceManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<ServiceRow[]>([]);
  const [searchText, setSearchText] = useState<string>('');
  const [selectedSearch, setSelectedSearch] = useState<string>('service');

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
      const response = await fetchServices(
        paginationModel.page,
        paginationModel.pageSize,
        selectedSearch && searchText.trim() ? selectedSearch : null,
        selectedSearch && searchText.trim() ? searchText.trim() : null
      );
      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      console.error("Failed to retrieve Services. ", error);
      navigate('/error', { state: { message: `Failed to retrieve Services: ${error}` } });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.page, paginationModel.pageSize, selectedSearch, searchText, navigate]);

  const getData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetchServices(
        0,
        paginationModel.pageSize,
        selectedSearch && searchText.trim() ? selectedSearch : null,
        selectedSearch && searchText.trim() ? searchText.trim() : null
      );
      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
      setPaginationModel((prev) => ({ ...prev, page: 0 }));
    } catch (err) {
      await dialogs.open(CustomDialog, {
        title: 'Notification',
        message: formatErrorMessage(err, "Failed to fetch Service list"),
        isModal: true,
      });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.pageSize, selectedSearch, searchText, dialogs]);

  const handleSearch = useCallback(async (field: string, text: string) => {
    const trimmed = text.trim();
    if (!trimmed) return;
    setSelectedSearch(field);
    setSearchText(trimmed);
    setPaginationModel((prev) => ({ ...prev, page: 0 }));
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleDelete = async () => {
    if (!selectedRowData) return;
    const id = selectedRowData?.id as number;
    const policyCount = selectedRowData?.policyCount as number;

    if (policyCount > 0) {
      await dialogs.open(CustomDialog, {
        title: 'Notification',
        message: 'This service is in use by one or more policies and cannot be deleted.',
        isModal: true,
      });
      return;
    }

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
                getData();
              },
            });
          })
          .catch((error) => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: formatErrorMessage(error, "Failed to delete Service"),
              isModal: true,
            });
          })
          .finally(() => setLoading(false));
      }
    }
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
              { field: 'mode', headerName: "Submission Mode", width: 150,
                renderCell: (params) => modeMapping[params.value],
              },
              { field: 'offerType', headerName: "Verification Type", width: 150,
                renderCell: (params) => offerTypeMapping[params.value] || params.value,
              },
              { field: 'policyCount', headerName: "Policy Count", width: 100},
            ]}
            selectedRow={selectedRow}
            setSelectedRow={setSelectedRow}
            enableSearch={true}
            searchText={searchText}
            setSearchText={setSearchText}
            selectedSearch={selectedSearch}
            setSelectedSearch={setSelectedSearch}
            searchOptions={[
              { value: 'service', label: 'Service' },
              { value: 'device', label: 'Device' },
              { value: 'mode', label: 'Mode' },
            ]}
            selectableFields={[
              { field: 'mode', options: [
                { value: 'Direct', label: 'Direct' },
                { value: 'Indirect', label: 'Indirect' },
                { value: 'Proxy', label: 'Proxy' },
              ]},
            ]}
            onSearch={handleSearch}
            onRefresh={getData}
            onEdit={() => {
              if (selectedRowData) {
                navigate(`/vp-policy-management/service-management/service-edit/${selectedRowData.id}`);
              }
            }}
            onRegister={() => navigate('/vp-policy-management/service-management/service-registration')}
            onDelete={handleDelete}
            additionalButtons={[]}
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
