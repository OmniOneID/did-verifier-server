// Updated ProofRequestConfigurationRegistrationPage with AttributeSelectDialog popup integration
import {
  Box, Button, IconButton, MenuItem, Paper, Select, SelectChangeEvent,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography, useTheme, FormControl, InputLabel, styled
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import DeleteIcon from "@mui/icons-material/Delete";
import FullscreenLoader from "../../../components/loading/FullscreenLoader";
import CustomDialog from "../../../components/dialog/CustomDialog";
import CustomConfirmDialog from "../../../components/dialog/CustomConfirmDialog";
import AttributeSelectDialog from "./AttributeSelectDialog";
import PredicateSelectDialog from "./PredicateSelectDialog";
import { useDialogs } from "@toolpad/core";

interface AttributeItem {
  attributeName: string;
  definitionTag: string;
}

interface PredicateItem {
  attributeName: string;
  predicateType: string;
  predicateValue: string;
  definitionTag: string;
}

interface AttributeDialogResult {
  label: string;
  type: string;
  namespaceIdentifier: string;
}

interface FormData {
  name: string;
  version: string;
  curve: string;
  cipher: string;
  padding: string;
  attributes: AttributeItem[];
  predicates: PredicateItem[];
}

const curveOptions = ["Secp256r1"];
const cipherOptions = ["AES-256-CBC"];
const paddingOptions = ["PKCS5"];
const predicateTypeOptions = ["LE", "GE", "LT"];

const ProofRequestConfigurationRegistrationPage = () => {
  const theme = useTheme();
  const dialogs = useDialogs();
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>({
    name: "",
    version: "",
    curve: "Secp256r1",
    cipher: "AES-256-CBC",
    padding: "PKCS5",
    attributes: [],
    predicates: [],
  });

  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (field: keyof FormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleOpenAttributeDialog = async () => {
    const result = await dialogs.open(AttributeSelectDialog, []) as AttributeDialogResult[];
    if (result && Array.isArray(result)) {
      const newAttributes = result.map((attr) => ({
        attributeName: attr.label,
        definitionTag: attr.namespaceIdentifier,
      }));
      setFormData((prev) => ({ ...prev, attributes: [...prev.attributes, ...newAttributes] }));
    }
  };

  const handleRemoveAttribute = (index: number) => {
    const updated = [...formData.attributes];
    updated.splice(index, 1);
    setFormData((prev) => ({ ...prev, attributes: updated }));
  };

  const handleOpenPredicateDialog = async () => {
    const result = await dialogs.open(PredicateSelectDialog, []) as AttributeDialogResult[];
    if (result && Array.isArray(result)) {
        const newPredicates = result.map((attr) => ({
        attributeName: attr.label,
        predicateType: "LE", // 기본값
        predicateValue: "",
        definitionTag: attr.namespaceIdentifier,
        }));
        setFormData((prev) => ({
        ...prev,
        predicates: [...prev.predicates, ...newPredicates],
        }));
    }
    };

    const handlePredicateFieldChange = (index: number, field: keyof PredicateItem) =>
    (e: React.ChangeEvent<HTMLInputElement | { value: unknown }>) => {
        const updated = [...formData.predicates];
        updated[index][field] = e.target.value as string;
        setFormData((prev) => ({ ...prev, predicates: updated }));
    };

    const handleRemovePredicate = (index: number) => {
    const updated = [...formData.predicates];
    updated.splice(index, 1);
    setFormData((prev) => ({ ...prev, predicates: updated }));
    };

  const handleSubmit = async () => {
    const confirmed = await dialogs.open(CustomConfirmDialog, {
      title: "Confirm",
      message: "Submit Proof Request?",
      isModal: true,
    });

    if (confirmed) {
      setIsLoading(true);
      try {
        console.log("Submitting:", formData);
        setIsLoading(false);
        await dialogs.open(CustomDialog, {
          title: "Success",
          message: "Proof Request Registered.",
          isModal: true,
        }, {
          onClose: () => navigate("/zkp-management/proof-request-management"),
        });
      } catch (err) {
        setIsLoading(false);
        await dialogs.open(CustomDialog, {
          title: "Error",
          message: `Failed to register Proof Request: ${err}`,
          isModal: true,
        });
      }
    }
  };

  const StyledContainer = useMemo(() => styled(Box)(({ theme }) => ({
    width: 900,
    margin: 'auto',
    marginTop: theme.spacing(1),
    padding: theme.spacing(3),
    border: 'none',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: '#ffffff',
    boxShadow: '0px 4px 8px 0px #0000001A',
  })), []);

  const StyledTitle = useMemo(() => styled(Typography)({
    textAlign: 'left',
    fontSize: '24px',
    fontWeight: 700,
  }), []);

  const StyledInputArea = useMemo(() => styled(Box)(({ theme }) => ({
          marginTop: theme.spacing(2),
  })), []);

  return (
    <>
      <FullscreenLoader open={isLoading} />
      <Typography variant="h4">Proof Request Configuration</Typography>
      <StyledContainer>
        <StyledTitle>Proof Request Registration</StyledTitle>
        <StyledInputArea>
            <TextField label="Name *" fullWidth size="small" margin="normal" value={formData.name} onChange={handleChange("name")} />
            <TextField label="Version *" fullWidth size="small" margin="normal" value={formData.version} onChange={handleChange("version")} />

            <FormControl fullWidth size="small" margin="normal">
                <InputLabel>Curve</InputLabel>
                <Select value={formData.curve} onChange={(e) => setFormData(prev => ({ ...prev, curve: e.target.value }))}>
                    {curveOptions.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                </Select>
            </FormControl>

            <FormControl fullWidth size="small" margin="normal">
            <InputLabel>Cipher</InputLabel>
            <Select value={formData.cipher} onChange={(e) => setFormData(prev => ({ ...prev, cipher: e.target.value }))}>
                {cipherOptions.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
            </Select>
            </FormControl>

            <FormControl fullWidth size="small" margin="normal">
            <InputLabel>Padding</InputLabel>
            <Select value={formData.padding} onChange={(e) => setFormData(prev => ({ ...prev, padding: e.target.value }))}>
                {paddingOptions.map((p) => <MenuItem key={p} value={p}>{p}</MenuItem>)}
            </Select>
            </FormControl>

            <Typography variant="h6" sx={{ mt: 3 }}>Requested Attributes</Typography>
            <Button variant="contained" startIcon={<AddCircleOutlineIcon />} sx={{ my: 2 }} onClick={handleOpenAttributeDialog}>
            Add Attribute
            </Button>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                    <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                        <TableCell>Attribute Name</TableCell>
                        <TableCell>Credential Definition</TableCell>
                        <TableCell sx={{ width: 100 }}>Delete</TableCell>
                    </TableRow>
                    </TableHead>
                    <TableBody>
                    {formData.attributes.map((item, index) => (
                        <TableRow key={index}>
                        <TableCell>{item.attributeName}</TableCell>
                        <TableCell>{item.definitionTag}</TableCell>
                        <TableCell>
                            <IconButton onClick={() => handleRemoveAttribute(index)}><DeleteIcon sx={{ color: "#FF8400" }} /></IconButton>
                        </TableCell>
                        </TableRow>
                    ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Typography variant="h6" sx={{ mt: 3 }}>Requested Predicates</Typography>
                <Button variant="contained" startIcon={<AddCircleOutlineIcon />} sx={{ my: 2 }} onClick={handleOpenPredicateDialog}>
                Add Predicate
                </Button>

                <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                    <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                        <TableCell>Attribute Name</TableCell>
                        <TableCell>Predicate Type</TableCell>
                        <TableCell>Predicate Value</TableCell>
                        <TableCell>Credential Definition</TableCell>
                        <TableCell>Delete</TableCell>
                    </TableRow>
                    </TableHead>
                    <TableBody>
                    {formData.predicates.map((item, index) => (
                        <TableRow key={index}>
                        <TableCell>{item.attributeName}</TableCell>
                        <TableCell>
                            <FormControl fullWidth size="small">
                            <Select value={item.predicateType} onChange={handlePredicateFieldChange(index, "predicateType")}>
                                {predicateTypeOptions.map((type) => (
                                <MenuItem key={type} value={type}>{type}</MenuItem>
                                ))}
                            </Select>
                            </FormControl>
                        </TableCell>
                        <TableCell>
                            <TextField size="small" fullWidth value={item.predicateValue} onChange={handlePredicateFieldChange(index, "predicateValue")} />
                        </TableCell>
                        <TableCell>{item.definitionTag}</TableCell>
                        <TableCell>
                            <IconButton onClick={() => handleRemovePredicate(index)}><DeleteIcon sx={{ color: "#FF8400" }} /></IconButton>
                        </TableCell>
                        </TableRow>
                    ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Box sx={{ display: "flex", justifyContent: "center", gap: 2, mt: 4 }}>
            <Button variant="contained" color="primary" onClick={handleSubmit}>Register</Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
            </Box>
        </StyledInputArea>
      </StyledContainer>
    </>
  );
};

export default ProofRequestConfigurationRegistrationPage;
