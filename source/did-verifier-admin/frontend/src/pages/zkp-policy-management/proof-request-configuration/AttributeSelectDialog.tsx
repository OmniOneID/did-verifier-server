import {
  Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Select, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography
} from '@mui/material';
import { DialogProps } from '@toolpad/core/useDialogs';
import React, { useEffect, useState } from 'react';
import { getZkpNamespaceAll, getZkpAttributes, getCredentialDefinitionsByNamespace } from '../../../apis/zkp-proof-api';

interface Attribute {
  id: number;
  zkpNamespaceId: number;
  attributeName: string;
  label: string;
  type: string;
}

interface NamespaceOption {
  id: number;
  name: string;
  namespaceId: string;
  ref: string;
}

interface CredentialDefinition {
  id: number;
  tag: string;
}

interface DialogResult {
  attributeName: string;
  label: string;
  type: string;
  definitionTag: string;
  namespaceIdentifier: string;
}

type AttributeSelectDialogProps = DialogProps<DialogResult[], unknown>;

const AttributeSelectDialog: React.FC<AttributeSelectDialogProps> = ({ open, onClose }) => {
  const [namespaceId, setNamespaceId] = useState<number | ''>('');
  const [namespaceOptions, setNamespaceOptions] = useState<NamespaceOption[]>([]);
  const [attributes, setAttributes] = useState<Attribute[]>([]);
  const [selectedMap, setSelectedMap] = useState<Record<number, boolean>>({});
  const [definitionOptions, setDefinitionOptions] = useState<CredentialDefinition[]>([]);
  const [definitionMap, setDefinitionMap] = useState<Record<number, string>>({}); // attr.id -> selected tag

  useEffect(() => {
    const fetchNamespaces = async () => {
      try {
        const res = await getZkpNamespaceAll();
        const options = res.data.map((ns: any) => ({
          id: ns.id,
          name: ns.name,
          namespaceId: ns.namespaceId,
          ref: ns.ref,
        }));
        setNamespaceOptions(options);
      } catch (error) {
        console.error('Failed to fetch namespaces:', error);
      }
    };

    fetchNamespaces();
  }, []);

  useEffect(() => {
    const fetchAttributesAndDefinitions = async () => {
      if (!namespaceId) return;
      try {
        const [attrRes, defRes] = await Promise.all([
          getZkpAttributes(namespaceId),
          getCredentialDefinitionsByNamespace(namespaceId),
        ]);

        const attrs = attrRes.data.map((item: any) => ({
          id: item.id,
          zkpNamespaceId: item.zkpNamespaceId,
          attributeName: item.name, // attributeName 명확하게 지정
          label: item.label,
          type: item.type,
        }));

        const defs = defRes.data.map((item: any) => ({
          id: item.id,
          tag: item.tag,
        }));

        setAttributes(attrs);
        setDefinitionOptions(defs);
        setDefinitionMap({});
        setSelectedMap({});
      } catch (error) {
        console.error(`Failed to fetch data for namespace ${namespaceId}:`, error);
        setAttributes([]);
        setDefinitionOptions([]);
      }
    };

    fetchAttributesAndDefinitions();
  }, [namespaceId]);

  const handleToggle = (id: number) => {
    setSelectedMap((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleDefinitionChange = (attributeId: number) => (event: any) => {
    setDefinitionMap((prev) => ({ ...prev, [attributeId]: event.target.value }));
  };

  const handleClose = (event: unknown, reason?: string) => {
    if (reason === 'backdropClick') return;
    onClose([]);
  };

  const handleAdd = async () => {
    const selectedAttributes = attributes.filter(attr => selectedMap[attr.id]);
    const selectedNamespace = namespaceOptions.find(ns => ns.id === namespaceId);

    const result: DialogResult[] = selectedAttributes.map(attr => ({
      attributeName: attr.attributeName,
      label: attr.label,
      type: attr.type,
      definitionTag: definitionMap[attr.id] || '',
      namespaceIdentifier: selectedNamespace?.namespaceId ?? '',
    }));

    await onClose(result);
  };

  return (
    <Dialog open={open} onClose={handleClose} disableEscapeKeyDown fullWidth>
      <Box sx={{ px: 2 }}>
        <DialogTitle sx={{ p: 0, pt: 2, fontWeight: 700 }}>Add Requested Attributes</DialogTitle>
        <Box sx={{ height: '1px', backgroundColor: '#BFBFBF', width: '100%', mt: 1 }} />
      </Box>

      <DialogContent sx={{ px: 2 }}>
        <Typography sx={{ mt: 2, mb: 1 }}>Select Schema*</Typography>
        <Select
          value={namespaceId}
          onChange={(e) => setNamespaceId(Number(e.target.value))}
          fullWidth
          size="small"
          displayEmpty
        >
          <MenuItem value="" disabled>Select a schema</MenuItem>
          {namespaceOptions.map(ns => (
            <MenuItem key={ns.id} value={ns.id}>{ns.name}</MenuItem>
          ))}
        </Select>

        <Typography sx={{ mt: 3, mb: 1 }}>Select Attributes in Schema*</Typography>
        <TableContainer sx={{ border: '1px solid #ddd' }}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: "#f5f5f5" }}>
              <TableRow>
                <TableCell>Select</TableCell>
                <TableCell sx={{ width: 150 }}>Attribute Name</TableCell>
                <TableCell sx={{ width: 150 }}>Attribute Label</TableCell>
                <TableCell sx={{ width: 150 }}>Attribute Type</TableCell>
                <TableCell sx={{ width: 250 }}>Credential Definition</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {attributes.map(attr => (
                <TableRow key={attr.id}>
                  <TableCell>
                    <Checkbox
                      checked={!!selectedMap[attr.id]}
                      onChange={() => handleToggle(attr.id)}
                    />
                  </TableCell>
                  <TableCell>{attr.attributeName}</TableCell>
                  <TableCell>{attr.label}</TableCell>
                  <TableCell>{attr.type}</TableCell>
                  <TableCell>
                    <Select
                      fullWidth
                      size="small"
                      value={definitionMap[attr.id] || ''}
                      onChange={handleDefinitionChange(attr.id)}
                      displayEmpty
                    >
                      <MenuItem value="" disabled>Select tag</MenuItem>
                      {definitionOptions.map(def => (
                        <MenuItem key={def.id} value={def.tag}>{def.tag}</MenuItem>
                      ))}
                    </Select>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>

      <DialogActions sx={{ px: 2, pt: 0, display: 'flex', justifyContent: 'center', mt: 2, mb: 2 }}>
        <Button variant="outlined" onClick={() => onClose([])} sx={{ width: '40%', height: '44px', mr: 2 }}>
          닫기
        </Button>
        <Button variant="contained" onClick={handleAdd} disabled={!namespaceId} sx={{ width: '40%', height: '44px' }}>
          추가
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AttributeSelectDialog;
