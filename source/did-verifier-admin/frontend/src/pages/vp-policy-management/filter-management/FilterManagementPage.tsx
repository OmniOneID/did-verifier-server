import { Box, Link, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { deleteFilter, fetchFilters } from "../../../apis/vp-filter-api";
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../../utils/error-handler';

type Props = {}

type FilterRow = {
  filterId: number;
  title: string;
  id: string;
  type: string;
  requiredClaims: string[];
  allowedIssuers: string[];
  displayClaims: string[];
  presentAll: boolean;
  createdAt: string;
};

const FilterManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<FilterRow[]>([]);
  const [searchText, setSearchText] = useState<string>('');
  const [selectedSearch, setSelectedSearch] = useState<string>('title');

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(() => {
    return rows.find(row => row.filterId === selectedRow) || null;
  }, [rows, selectedRow]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetchFilters(
        paginationModel.page,
        paginationModel.pageSize,
        selectedSearch && searchText.trim() ? selectedSearch : null,
        selectedSearch && searchText.trim() ? searchText.trim() : null
      );
      const transformedRows = response.data.content.map((row: { filterId: any; }) => ({
        ...row,
        id: row.filterId
      }));
      setRows(transformedRows);
      setTotalRows(response.data.totalElements);
    } catch (error) {
      console.error("Failed to retrieve Filter. ", error);
      navigate('/error', { state: { message: `Failed to retrieve Filters: ${error}` } });
    } finally {
      setLoading(false);
    }
  }, [paginationModel.page, paginationModel.pageSize, selectedSearch, searchText, navigate]);

  const getData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetchFilters(
        0,
        paginationModel.pageSize,
        selectedSearch && searchText.trim() ? selectedSearch : null,
        selectedSearch && searchText.trim() ? searchText.trim() : null
      );
      const transformedRows = response.data.content.map((row: { filterId: any; }) => ({
        ...row,
        id: row.filterId
      }));
      setRows(transformedRows);
      setTotalRows(response.data.totalElements);
      setPaginationModel((prev) => ({ ...prev, page: 0 }));
    } catch (err) {
      await dialogs.open(CustomDialog, {
        title: 'Notification',
        message: formatErrorMessage(err, "Failed to fetch Filter list"),
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
    const id = selectedRowData?.filterId;
    if (id) {
      const result = await dialogs.open(CustomConfirmDialog, {
        title: 'Confirmation',
        message: 'Are you sure you want to delete Filter?',
        isModal: true,
      });

      if (result) {
        setLoading(true);
        deleteFilter(id)
          .then(() => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Filter delete completed.',
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
              message: formatErrorMessage(error, "Failed to delete Filter"),
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
        <StyledSubTitle>Filter Management</StyledSubTitle>
        <CustomDataGrid
          rows={rows}
          columns={[
            {
              field: 'title',
              headerName: "Title",
              width: 200,
              renderCell: (params) => (
                <Link
                  component="button"
                  variant="body2"
                  onClick={() => navigate(`/vp-policy-management/filter-management/${params.row.filterId}`)}
                  sx={{ cursor: 'pointer', color: 'primary.main' }}
                >
                  {params.value}
                </Link>),
            },
            {
              field: 'type',
              headerName: "Type",
              width: 200,
            },
            {
              field: 'createdAt',
              headerName: "Created At",
              width: 200,
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
            { value: 'type', label: 'Type' },
          ]}
          onSearch={handleSearch}
          onRefresh={getData}
          onEdit={() => {
            if (selectedRowData) {
              navigate(`/vp-policy-management/filter-management/filter-edit/${selectedRowData.filterId}`);
            }
          }}
          onRegister={() => navigate('/vp-policy-management/filter-management/filter-registration')}
          onDelete={handleDelete}
          additionalButtons={[]}
          paginationMode="server"
          totalRows={totalRows}
          paginationModel={paginationModel}
          setPaginationModel={setPaginationModel}
        />
      </StyledContainer>
    </>
  );
};

export default FilterManagementPage;
