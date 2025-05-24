import { Box, Link, Typography, styled } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import { formatErrorMessage } from '../../../utils/error-handler';
import { fetchZkpProfiles, deleteZkpProfile } from '../../../apis/zkp-profile-api';

type ProofRequestRow = {
  id: number;
  profileId: string;
  title: string;
  description: string;
  policyCount: number;
  createdAt: string;
  updatedAt: string;
};

const ZkpProfileManagementPage = () => {
  const navigate = useNavigate();
  const dialogs = useDialogs();

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<ProofRequestRow[]>([]);
  const [totalRows, setTotalRows] = useState(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);

  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0,
    pageSize: 10,
  });

  const selectedRowData = useMemo(
    () => Array.isArray(rows) ? rows.find(row => row.id === selectedRow) || null : null,
    [rows, selectedRow]
  );

  const handleDelete = async () => {
    if (!selectedRowData) return;
    const id = selectedRowData?.id;
    const policyCount = selectedRowData?.policyCount;

    if (policyCount > 0) {
      await dialogs.open(CustomDialog, {
        title: 'Notification',
        message: 'This ZKP Profile is in use by one or more policies and cannot be deleted.',
        isModal: true,
      });
      return;
    }

    const result = await dialogs.open(CustomConfirmDialog, {
      title: 'Confirmation',
      message: 'Are you sure you want to delete this ZKP Profile?',
      isModal: true,
    });

    if (result && id) {
      setLoading(true);
      deleteZkpProfile(id)
        .then(() => {
          dialogs.open(CustomDialog, {
            title: 'Notification',
            message: 'ZKP Profile deletion completed.',
            isModal: true,
          }, {
            onClose: async () => {
              setPaginationModel(prev => ({ ...prev }));
            },
          });
        })
        .catch((error) => {
          dialogs.open(CustomDialog, {
            title: 'Notification',
            message: formatErrorMessage(error, "Failed to delete ZKP Profile."),
            isModal: true,
          });
        })
        .finally(() => setLoading(false));
    }
  };

  useEffect(() => {
    setLoading(true);
    fetchZkpProfiles(paginationModel.page, paginationModel.pageSize, null, null)
      .then((response) => {
        setRows(response.data.content);
        setTotalRows(response.data.totalElements);
      })
      .catch((err) => {
        console.error("Failed to retrieve ZKP Profiles", err);
        dialogs.open(CustomDialog, {
          title: 'Notification',
          message: formatErrorMessage(err, "Failed to fetch ZKP Profile list."),
          isModal: true,
        });
      })
      .finally(() => setLoading(false));
  }, [paginationModel]);

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    margin: 'auto',
    marginTop: theme.spacing(1),
    padding: theme.spacing(3),
    backgroundColor: '#ffffff',
    borderRadius: theme.shape.borderRadius,
    boxShadow: '0px 4px 8px rgba(0,0,0,0.1)',
  })), []);

  const StyledSubTitle = useMemo(() => styled(Typography)({
    fontSize: '24px',
    fontWeight: 700,
    textAlign: 'left',
  }), []);

  return (
    <>
      <FullscreenLoader open={loading} />
      <StyledContainer>
        <StyledSubTitle>ZKP Profile Management</StyledSubTitle>
        <CustomDataGrid
          rows={rows}
          columns={[
            {
              field: 'profileId', headerName: 'Profile ID', width: 200,
              renderCell: (params) => (
                <Link
                  component="button"
                  variant='body2'
                  onClick={() => navigate(`/zkp-policy-management/zkp-profile-management/${params.row.id}`)}
                  sx={{ cursor: 'pointer', color: 'primary.main' }}
                >
                  {params.value}
                </Link>
              ),
            },
            { field: 'title', headerName: 'Profile Title', width: 150 },
            { field: 'description', headerName: 'Profile Description', width: 200 },
            { field: 'policyCount', headerName: 'Used in Policies', width: 150 },
            { field: 'createdAt', headerName: 'Registered At', width: 150 },
            { field: 'updatedAt', headerName: 'Updated At', width: 150 },
          ]}
          selectedRow={selectedRow}
          setSelectedRow={setSelectedRow}
          onRegister={() => navigate('/zkp-policy-management/zkp-profile-management/zkp-profile-registration')}
          paginationMode="server"
          totalRows={totalRows}
          paginationModel={paginationModel}
          setPaginationModel={setPaginationModel}
          onEdit={() => {
            if (selectedRowData) {
              navigate(`/zkp-policy-management/zkp-profile-management/profile-edit/${selectedRowData.id}`);
            }
          }}
          onDelete={handleDelete}
        />
      </StyledContainer>
    </>
  );
};

export default ZkpProfileManagementPage;
