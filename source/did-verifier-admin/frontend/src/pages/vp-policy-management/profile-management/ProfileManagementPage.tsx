import { Link } from '@mui/material';
import { GridPaginationModel } from "@mui/x-data-grid";
import { useDialogs } from "@toolpad/core";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDataGrid from "../../../components/data-grid/CustomDataGrid";
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import CustomDialog from '../../../components/dialog/CustomDialog';
import { deleteProfile, fetchProfiles } from '../../../apis/vp-profile-api';

type Props = {}

type PolicyProfileRow = {
  id: number;
  policyProfileId: string;
  title: string;
  description: string;      
  createdAt: string;
};


const ProfileManagementPage = (props: Props) => {
  const navigate = useNavigate();
  const dialogs = useDialogs();
  const [loading, setLoading] = useState<boolean>(false);
  const [totalRows, setTotalRows] = useState<number>(0);
  const [selectedRow, setSelectedRow] = useState<string | number | null>(null);
  const [rows, setRows] = useState<PolicyProfileRow[]>([]);

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
        deleteProfile(id)
          .then(() => {
            dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Profile delete completed.',
              isModal: true,
            }, {
              onClose: async () => {
                setPaginationModel(prev => ({ ...prev }));
              },
            });
          })
          .catch((error) => {
            console.error("Failed to delete Profile. ", error);
            navigate('/error', { state: { message: `Failed to delete Profile: ${error}` } });
          })
          .finally(() => setLoading(false));
      }
    }
  };
  
  useEffect(() => {
    setLoading(true);
    fetchProfiles(paginationModel.page, paginationModel.pageSize, null, null)
      .then((response) => {
        setRows(response.data.content);
        setTotalRows(response.data.totalElements);
      })
      .catch((error) => {
        console.error("Failed to retrieve Profiles. ", error);
        navigate('/error', { state: { message: `Failed to retrieve Profiles: ${error}` } });
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
            field: 'title', 
            headerName: "Title", 
            width: 150,
            renderCell: (params) => (
              <Link 
                component="button"
                variant='body2'
                onClick={() => navigate(`/vp-policy-management/profile-management/${params.row.id}`)}
                sx={{ cursor: 'pointer', color: 'primary.main' }}
              >
                {params.value}
              </Link>),
          },
          { 
            field: 'description', 
            headerName: "Description", 
            width: 200,
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
            navigate(`/vp-policy-management/profile-management/profile-edit/${selectedRowData.id}`);
          }
        }}
        onRegister={() => navigate('/vp-policy-management/profile-management/profile-registration')}
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

export default ProfileManagementPage