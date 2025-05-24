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
import { fetchProofRequests, deleteProofRequest } from '../../../apis/zkp-proof-api';

type ProofRequestRow = {
  id: number;
  name: string;
  version: string;
  profileCount: number;
  createdAt: string;
  updatedAt: string;
};

const ProofRequestConfigurationPage = () => {
  const navigate = useNavigate();
  const dialogs = useDialogs();

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<ProofRequestRow[]>([]);
  const [totalRows, setTotalRows] = useState(0);
  const [selectedRow, setSelectedRow] = useState<string| number | null>(null);

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
      const id = selectedRowData?.id as number;
      const policyCount = selectedRowData?.profileCount as number;

      if (policyCount > 0) {
        await dialogs.open(CustomDialog, {
          title: 'Notification',
          message: 'This Proof Request is in use by one or more profiles and cannot be deleted.',
          isModal: true,
        });
        return;
      }

      if (id) {
        const result = await dialogs.open(CustomConfirmDialog, {
          title: 'Confirmation',
          message: 'Are you sure you want to delete Proof Request?',
          isModal: true,
        });

        if (result) {
          setLoading(true);
          deleteProofRequest(id)
            .then(() => {
              dialogs.open(CustomDialog, {
                title: 'Notification',
                message: 'Proof Request delete completed.',
                isModal: true,
              }, {
                onClose: async () => {
                  setPaginationModel(prev => ({ ...prev }));
                },
              });
            })
            .catch((error) => {
              const result = dialogs.open(CustomDialog, {
                title: 'Notification',
                message: formatErrorMessage(error, "Failed to delete Proof Request!! "),
                isModal: true,
              });
            })
            .finally(() => setLoading(false));
        }
      }
   };

  useEffect(() => {
    setLoading(true)
    fetchProofRequests(paginationModel.page, paginationModel.pageSize, null, null)
    .then((response) => {
      setLoading(false);
      setRows(response.data.content);
      setTotalRows(response.data.totalElements);
    })
    .catch((err) => {
      setLoading(false);
      console.error("Failed to retrieve Proof Request Configurations", err);
      dialogs.open(CustomDialog, {
          title: 'Notification',
          message: formatErrorMessage(err, "Failed to fetch Proof Request Configuration list."),
          isModal: true,
      });
    });
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
        <StyledSubTitle>Proof Request Configuration</StyledSubTitle>
        <CustomDataGrid
            rows={rows}
            columns={[
              { field: 'name', headerName: 'Name', width: 200,
                renderCell: (params) => (
                  <Link
                    component="button"
                    variant='body2'
                    onClick={() => navigate(`/zkp-policy-management/proof-request-configuration/${params.row.id}`)}
                    sx={{ cursor: 'pointer', color: 'primary.main' }}
                  >
                    {params.value}
                  </Link>
                ),
  
                },
              { field: 'version', headerName: 'Version', width: 100 },
              { field: 'profileCount', headerName: 'Profile Count', width: 150 },
              { field: 'createdAt', headerName: 'Registered At', width: 150 },
              { field: 'updatedAt', headerName: 'Updated At', width: 150 },
            ]}
            selectedRow={selectedRow}
            setSelectedRow={setSelectedRow}
            onRegister={() => navigate('/zkp-policy-management/proof-request-configuration/proof-request-configuration-registration')}
            paginationMode="server"
            totalRows={totalRows}
            paginationModel={paginationModel}
            setPaginationModel={setPaginationModel}
            onEdit={() => {
              if (selectedRowData) {
                navigate(`/zkp-policy-management/proof-request-configuration/proof-request-edit/${selectedRowData.id}`);
              }
            }}
            onDelete={handleDelete}
          />
      </StyledContainer>
    </>
  )
}

export default ProofRequestConfigurationPage