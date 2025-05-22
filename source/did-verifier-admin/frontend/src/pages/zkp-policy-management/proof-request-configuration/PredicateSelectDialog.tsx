import {
  Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Select, SelectChangeEvent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, TextField, Typography
} from '@mui/material';
import { DialogProps } from '@toolpad/core/useDialogs';
import React, { useEffect, useState } from 'react';
import { getZkpNamespaceAll, getZkpAttributes, getCredentialDefinitionsByNamespace } from '../../../apis/zkp-proof-api';

interface PredicateDialogResult {
  attributeName: string;
  label: string;
  type: string;
  predicateType: string;
  predicateValue: string;
  definitionTag: string;
  namespaceIdentifier: string;
}

type PredicateSelectDialogProps = DialogProps<PredicateDialogResult[], unknown>;

const PredicateSelectDialog: React.FC<PredicateSelectDialogProps> = ({ open, onClose }) => {
  const [namespaceId, setNamespaceId] = useState<number | ''>('');
  const [namespaceOptions, setNamespaceOptions] = useState<
    { id: number; name: string; namespaceId: string }[]
  >([]);
  const [attributes, setAttributes] = useState<
    { id: number; attributeName: string; label: string; type: string; zkpNamespaceId: number }[]
  >([]);
  const [selectedMap, setSelectedMap] = useState<Record<number, boolean>>({});
  const [predicateTypeMap, setPredicateTypeMap] = useState<Record<number, string>>({});
  const [predicateValueMap, setPredicateValueMap] = useState<Record<number, string>>({});
  const [definitionMap, setDefinitionMap] = useState<Record<number, string>>({});
  const [definitionOptions, setDefinitionOptions] = useState<{ id: number; tag: string }[]>([]);

  useEffect(() => {
    const fetchNamespaces = async () => {
      const res = await getZkpNamespaceAll();
      setNamespaceOptions(res.data.map((ns: any) => ({
        id: ns.id,
        name: ns.name,
        namespaceId: ns.namespaceId,
      })));
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

        const filteredAttrs = attrRes.data
          .filter((item: any) => item.type === 'Number' || item.type === 'Date')
          .map((item: any) => ({
            id: item.id,
            attributeName: item.name,
            label: item.label,
            type: item.type,
            zkpNamespaceId: item.zkpNamespaceId,
          }));

        const defs = defRes.data.map((item: any) => ({
          id: item.id,
          tag: item.tag,
        }));

        setAttributes(filteredAttrs);
        setDefinitionOptions(defs);
        setSelectedMap({});
        setPredicateTypeMap({});
        setPredicateValueMap({});
        setDefinitionMap({});
      } catch (error) {
        console.error('Failed to fetch attributes or definitions:', error);
      }
    };

    fetchAttributesAndDefinitions();
  }, [namespaceId]);

  const handleToggle = (id: number) => {
    setSelectedMap((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handlePredicateTypeChange = (id: number) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setPredicateTypeMap(prev => ({ ...prev, [id]: e.target.value }));
  };

  const handlePredicateValueChange = (id: number) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setPredicateValueMap(prev => ({ ...prev, [id]: e.target.value }));
  };

  const handleDefinitionChange = (id: number) => (e: SelectChangeEvent<string>) => {
    setDefinitionMap(prev => ({ ...prev, [id]: e.target.value }));
  };

  const handleAdd = () => {
    const selectedAttrs = attributes.filter(attr => selectedMap[attr.id]);
    const selectedNamespace = namespaceOptions.find(ns => ns.id === namespaceId);

    const result: PredicateDialogResult[] = selectedAttrs.map(attr => ({
      attributeName: attr.attributeName,
      label: attr.label,
      type: attr.type,
      predicateType: predicateTypeMap[attr.id] || '',
      predicateValue: predicateValueMap[attr.id] || '',
      definitionTag: definitionMap[attr.id] || '',
      namespaceIdentifier: selectedNamespace?.namespaceId ?? '',
    }));

    onClose(result);
  };

  const handleClose = (event: unknown, reason?: string) => {
    if (reason === 'backdropClick') return;
    onClose([]);
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="lg">
      <Box sx={{ px: 2 }}>
        <DialogTitle sx={{ p: 0, pt: 2, fontWeight: 700 }}>Add Requested Predicates</DialogTitle>
        <Box sx={{ height: '1px', backgroundColor: '#BFBFBF', width: '100%', mt: 1 }} />
      </Box>

      <DialogContent sx={{ px: 2 }}>
        <Typography variant="body2" sx={{ my: 1 }}>
          ※ Only numeric/date-like attributes are shown
        </Typography>

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
                <TableCell>Attribute Name</TableCell>
                <TableCell>Attribute Label</TableCell>
                <TableCell>Attribute Type</TableCell>
                <TableCell>Predicate Type</TableCell>
                <TableCell>Predicate Value</TableCell>
                <TableCell>Credential Definition</TableCell>
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
                    <TextField
                      size="small"
                      value={predicateTypeMap[attr.id] || ''}
                      onChange={handlePredicateTypeChange(attr.id)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      size="small"
                      value={predicateValueMap[attr.id] || ''}
                      onChange={handlePredicateValueChange(attr.id)}
                    />
                  </TableCell>
                  <TableCell>
                    <Select
                      size="small"
                      value={definitionMap[attr.id] || ''}
                      onChange={handleDefinitionChange(attr.id)}
                      displayEmpty
                      fullWidth
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

export default PredicateSelectDialog;
