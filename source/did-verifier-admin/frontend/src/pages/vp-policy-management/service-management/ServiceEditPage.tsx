import { useDialogs } from '@toolpad/core';
import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router';
import { getService } from '../../../apis/vp-policy-api';
import CustomDialog from '../../../components/dialog/CustomDialog';
import FullscreenLoader from '../../../components/loading/FullscreenLoader';
import { Box, Button, FormControl, FormHelperText, InputLabel, MenuItem, Select, SelectChangeEvent, TextField, Typography } from '@mui/material';
import { urlRegex, ipRegex } from '../../../utils/regex';
import CustomConfirmDialog from '../../../components/dialog/CustomConfirmDialog';
import { putService } from "../../../apis/vp-policy-api";

type Props = {}

interface ServiceFormData {
  service: string;
  locked?: boolean;
  device: string;
  mode: string;
  endpoints: string;
}

interface ErrorState {
  service?: string;
  locked?: string;
  device?: string;
  mode?: string;
  endpoints?: string;
}

const ServiceEditPage = (props: Props) => {
    const { id } = useParams();
    const navigate = useNavigate();
    const dialogs = useDialogs();

    const numericServiceId = id ? parseInt(id, 10) : null;

    const [formData, setFormData] = useState<ServiceFormData>({
        service: '',
        locked: undefined,
        device: '',
        mode: '',
        endpoints: '',
    });

    const [initialData, setInitialData] = useState<ServiceFormData | null>(null);
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState<ErrorState>({});

    const handleChange = (field: keyof ServiceFormData) => 
        (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | SelectChangeEvent<string>) => {
        const newValue = event.target.value;
        setFormData((prev) => ({ ...prev, [field]: newValue }));
    };

    const handleReset = () => {
      if (initialData) {
        setFormData(initialData);
        setIsButtonDisabled(true);
      }
    };

    const validate = () => {
          let tempErrors: ErrorState = {};
  
          tempErrors.service = validateService(formData.service);
          tempErrors.locked = validateLocked(formData.locked);
          tempErrors.device = validateDevice(formData.device);
          tempErrors.mode = validateMode(formData.mode);
          tempErrors.endpoints = validateEndpoints(formData.endpoints);
  
          setErrors(tempErrors);
          return Object.values(tempErrors).every((error) => !error);
    };
    
    const validateService = (serivce?: string): string | undefined => {
        if (!serivce) return 'Please enter a serivce.';
        if (serivce.length > 50) return 'Service name must be less than 50 characters.';
        return undefined;
    };
    
    const validateLocked = (locked?: boolean): string | undefined => {
        if (locked === undefined) return 'Please select a lock status.';
        return undefined;
    };
    
    const validateDevice = (device?: string): string | undefined => {
        if (!device) return 'Please enter a device.';
        if (device.length > 50) return 'Device name must be less than 50 characters.';
        return undefined;
    };
    
    const validateMode = (mode?: string): string | undefined => {
        if (!mode) return 'Please select a submission mode.';
        return undefined;
    };
    
    const validateEndpoints = (endpoints?: string): string | undefined => {
        if (!endpoints) return 'Please enter an API address.';
        if (endpoints.length > 200) return 'API address must be less than 200 characters.';

        if (!urlRegex.test(endpoints) && !ipRegex.test(endpoints)) {
            return 'Please enter a valid URL or IP address.';
        }

        return undefined;
    };

    const handleSubmit = async () => {
      if (!validate()) return;

      const result = await dialogs.open(CustomConfirmDialog, {
          title: 'Confirmation',
          message: 'Are you sure you want to update Service?',
          isModal: true,
      });

      if (result) {
        setIsLoading(true);

        let requestObject = {
          id: numericServiceId,
          service: formData.service,
          locked: formData.locked,
          device: formData.device,
          mode: formData.mode,
          endpoints: formData.endpoints,
          validSecond: 180
        }

        await putService(requestObject).then((response) => {
          setIsLoading(false);
          setInitialData(response.data);
          dialogs.open(CustomDialog, {
              title: 'Notification',
              message: 'Service update completed.',
              isModal: true,
          },{
              onClose: async (result) =>  navigate('/vp-policy-management/service-management'),
          });

        }).catch((error) => {
          setIsLoading(false);
          dialogs.open(CustomDialog, {
              title: 'Notification',
              message: `Failed to update Service: ${error}`,
              isModal: true,
          });
        });
      }
    };

    useEffect(() => {
      const fetchData = async () => {
          if (numericServiceId === null || isNaN(numericServiceId)) {
              await dialogs.open(CustomDialog, { 
                  title: 'Notification', 
                  message: 'Invalid Path.', 
                  isModal: true 
              },{
                  onClose: async () => navigate('/vp-policy-management/service-management', { replace: true }),
              });
              return;
          }

          setIsLoading(true);

          try {
              const { data } = await getService(numericServiceId);
              const serviceData : ServiceFormData = {
                service: data.service,
                locked: data.locked,
                device: data.device,
                mode : data.mode,
                endpoints : data.endpoints,
              }
              setFormData(serviceData);
              setInitialData(serviceData);
              setIsButtonDisabled(true);
              setIsLoading(false);
          } catch (err) {
                console.error('Failed to fetch Serivce information:', err);
                setIsLoading(false);
                navigate('/error', { state: { message: `Failed to namespace information: ${err}` } });
          }
      };

      fetchData();
    }, [numericServiceId]);

    useEffect(() => {
      if (!initialData) return;
      const isModified = JSON.stringify(formData) !== JSON.stringify(initialData);
      setIsButtonDisabled(!isModified);
   }, [formData, initialData]);

    return (
      <>
        <FullscreenLoader open={isLoading} />
        <Box sx={{ p: 3 }}>
          <Typography variant="h4">Edit Service</Typography>

          <Box sx={{ maxWidth: 500, margin: 'auto', mt: 2, p: 3, border: '1px solid #ccc', borderRadius: 2 }}>
            <TextField 
                fullWidth
                label="Service" 
                variant="outlined"
                margin="normal" 
                size="small"
                value={formData.service} 
                onChange={handleChange('service')} 
                error={!!errors.service} 
                helperText={errors.service} 
                sx={{ maxLength: 50 }}
            />

            <FormControl fullWidth margin="normal" error={!!errors.locked}>
                <InputLabel>Lock Status</InputLabel>
                <Select 
                    value={formData.locked === undefined ? "" : String(formData.locked)}
                    onChange={(event) => setFormData((prev) => ({
                        ...prev, 
                        locked: event.target.value === "true"
                    }))}  
                    label="Lock Status"
                >
                    <MenuItem value={"true"}>Locked</MenuItem>
                    <MenuItem value={"false"}>Unlocked</MenuItem>
                </Select>
                {errors.locked && <FormHelperText>{errors.locked}</FormHelperText>}
            </FormControl>

            <TextField 
                fullWidth
                label="Device" 
                variant="outlined"
                margin="normal" 
                size="small"
                value={formData.device} 
                onChange={handleChange('device')} 
                error={!!errors.device} 
                helperText={errors.device} 
                sx={{ maxLength: 50 }}
            />

            <FormControl fullWidth margin="normal" error={!!errors.mode}>
                <InputLabel>Submittion Mode</InputLabel>
                <Select 
                    value={formData.mode} 
                    onChange={handleChange('mode')}
                    label="Submittion Mode"
                >
                    <MenuItem value="Direct">Direct</MenuItem>
                    <MenuItem value="Indirect">inDirect</MenuItem>
                    <MenuItem value="Proxy">Proxy</MenuItem>
                </Select>
                {errors.mode && <FormHelperText>{errors.mode}</FormHelperText>}
            </FormControl>

            <TextField 
                fullWidth
                label="API Address" 
                variant="outlined"
                margin="normal" 
                size="small"
                value={formData.endpoints} 
                onChange={handleChange('endpoints')} 
                error={!!errors.endpoints} 
                helperText={errors.endpoints} 
                sx={{ maxLength: 200 }}
            />

            <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 3 }}>
                <Button variant="contained" color="secondary" onClick={() => navigate('/vp-policy-management/service-management')}>
                  Back
                </Button>
                <Button variant="contained" color="secondary" onClick={handleReset}>Reset</Button>
                <Button variant="contained" color="primary" disabled={isButtonDisabled} onClick={handleSubmit}>Update</Button>
            </Box>
          </Box>
        </Box>
      </>
    )
}

export default ServiceEditPage