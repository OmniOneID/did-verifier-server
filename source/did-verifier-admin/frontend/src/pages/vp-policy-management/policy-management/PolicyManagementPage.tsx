import { Link } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import { deletePolicy, fetchPolicies } from '../../../apis/vp-policy-api';

type Props = {}

type PolicyRow = {
  id: number;
  policyTitle: string;     
  payloadService: string;     
  payloadId: string;
  profileId: string;
  profileTitle: string;
  createdAt: string;
};

const PolicyManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<PolicyRow[]>([]);

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
        message: 'Are you sure you want to delete this Policy?',
        isModal: true,
      });
  
      if (result) {
        setLoading(true);
        deletePolicy(id)
          .then(() => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Policy delete completed.',
              isModal: true,
            }, {
              onClose: async () => {
                setPaginationModel(prev => ({ ...prev }));
              },
            });
          })
          .catch((error) => {
            console.error("Failed to delete Policy. ", error);
            navigate('/error', { state: { message: `Failed to delete Policy: ${error}` } });
          })
          .finally(() => setLoading(false));
      }
    }
  };
  
  useEffect(() => {
    setLoading(true);
    fetchPolicies(paginationModel.page, paginationModel.pageSize, null, null)
      .then((response) => {
        setRows(response.data);
        setTotalRows(response.data.totalElements);
      })
      .catch((error) => {
        console.error("Failed to retrieve Policies. ", error);
        navigate('/error', { state: { message: `Failed to retrieve Policies: ${error}` } });
      })
      .finally(() => setLoading(false));
  }, [paginationModel]);

  return (
    <>
      <FullscreenLoader open={loading} />
      <CustomDataGrid 
        rows={rows} 
        columns={[
          { 
            field: 'policyTitle', 
            headerName: "Policy Title", 
            width: 180,
            renderCell: (params) => (
              <Link 
                component="button"
                variant='body2'
                onClick={() => navigate(`/vp-policy-management/policy-management/${params.row.id}`)}
                sx={{ cursor: 'pointer', color: 'primary.main' }}
              >
                {params.value}
              </Link>),
          },
          { 
            field: 'payloadService', 
            headerName: "Payload Service", 
            width: 180,
          },
          { 
            field: 'profileTitle', 
            headerName: "Profile Title", 
            width: 180,
          },
          { 
            field: 'createdAt', 
            headerName: "Created At", 
            width: 180,
          },
        ]} 
        selectedRow={selectedRow} 
        setSelectedRow={setSelectedRow}
        onEdit={() => {
          if (selectedRowData) {
            navigate(`/vp-policy-management/policy-edit/${selectedRowData.id}`);
          }
        }}
        onRegister={() => navigate('/vp-policy-management/policy-registration')}
        onDelete={handleDelete}
        additionalButtons={[
          
        ]}
        paginationMode="server" 
        totalRows={totalRows} 
        paginationModel={paginationModel} 
        setPaginationModel={setPaginationModel} 
      />
    </>
  )
}

export default PolicyManagementPage