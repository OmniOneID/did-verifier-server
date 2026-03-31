import { Box, Link, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { deleteProcess, fetchProcesses } from '../../../apis/vp-process-api';
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../../utils/error-handler';


type Props = {}

type ProcessRow = {
  id: number;
  title: string;
  reqE2e: {
    curve: string;
    cipher: string;
    padding: string;
    publicKey: string;
  };
  authType: number;
  endpoints: string[];
  createdAt: string;
};

const modeMapping: { [key: string]: string } = {
  0 : "No Restrict Auth",
  1 : "No Authentication",
  2 : "PIN",
  4 : "BIO",
  5 : "PIN or BIO",
  6 : "PIN and BIO",
};


const ProcessManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<ProcessRow[]>([]);
  const [searchText, setSearchText] = useState<string>('');
  const [selectedSearch, setSelectedSearch] = useState<string>('title');

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
      const response = await fetchProcesses(
        paginationModel.page,
        paginationModel.pageSize,
        selectedSearch && searchText.trim() ? selectedSearch : null,
        selectedSearch && searchText.trim() ? searchText.trim() : null
      );
      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      console.error("Failed to retrieve Processes. ", error);
      navigate('/error', { state: { message: `Failed to retrieve Processes: ${error}` } });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.page, paginationModel.pageSize, selectedSearch, searchText, navigate]);

  const getData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetchProcesses(
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
        message: formatErrorMessage(err, "Failed to fetch Process list"),
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
    const id = selectedRowData?.id as number;
    if (id) {
      const result = await dialogs.open(CustomConfirmDialog, {
        title: 'Confirmation',
        message: 'Are you sure you want to delete Service?',
        isModal: true,
      });

      if (result) {
        setLoading(true);
        deleteProcess(id)
          .then(() => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Process delete completed.',
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
              message: formatErrorMessage(error, "Failed to delete Process"),
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
        <StyledSubTitle>Process Management</StyledSubTitle>
        <CustomDataGrid
          rows={rows}
          columns={[
            {
              field: 'title',
              headerName: "Title",
              width: 150,
              renderCell: (params) => (
                <Link
                  component="button"
                  variant='body2'
                  onClick={() => navigate(`/vp-policy-management/process-management/${params.row.id}`)}
                  sx={{ cursor: 'pointer', color: 'primary.main' }}
                >
                  {params.value}
                </Link>),
            },
            {
              field: 'authType',
              headerName: "Auth Type",
              width: 100,
              renderCell: (params) => modeMapping[params.value] || params.value,
            },
            {
              field: 'reqE2e.curve',
              headerName: "Curve",
              width: 120,
              renderCell: (params) => params.row.reqE2e?.curve || '',
            },
            {
              field: 'reqE2e.cipher',
              headerName: "Cipher",
              width: 120,
              renderCell: (params) => params.row.reqE2e?.cipher || '',
            },
            {
              field: 'reqE2e.padding',
              headerName: "Padding",
              width: 120,
              renderCell: (params) => params.row.reqE2e?.padding || '',
            },
          ]}
          selectedRow={selectedRow}
          setSelectedRow={setSelectedRow}
          enableSearch={true}
          searchText={searchText}
          setSearchText={setSearchText}
          selectedSearch={selectedSearch}
          setSelectedSearch={setSelectedSearch}
          searchOptions={[
            { value: 'title', label: 'Title' },
            { value: 'authType', label: 'Auth Type' },
          ]}
          selectableFields={[
            {
              field: 'authType',
              options: [
                { value: '0', label: 'No Restrict Auth' },
                { value: '1', label: 'No Authentication' },
                { value: '2', label: 'PIN' },
                { value: '4', label: 'BIO' },
                { value: '5', label: 'PIN or BIO' },
                { value: '6', label: 'PIN and BIO' },
              ],
            },
          ]}
          onSearch={handleSearch}
          onRefresh={getData}
          onEdit={() => {
            if (selectedRowData) {
              navigate(`/vp-policy-management/process-management/process-edit/${selectedRowData.id}`);
            }
          }}
          onRegister={() => navigate('/vp-policy-management/process-management/process-registration')}
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

export default ProcessManagementPage
