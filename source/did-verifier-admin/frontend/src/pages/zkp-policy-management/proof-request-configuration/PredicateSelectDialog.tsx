import {
  Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Select, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography
} from '@mui/material';
import { DialogProps } from '@toolpad/core/useDialogs';
import React, { useEffect, useState } from 'react';
import { getCredentialSchemas } from '../../../apis/zkp-proof-api';

interface Attribute {
  id: number;
  zkpNamespaceId: string;
  label: string;
  caption: string;
  type: string;
}

interface CredentialDefinition {
  id: number;
  tag: string;
  credentialDefinitionId: string;
}

interface CredentialSchema {
  id: string;
  name: string;
  version: string;
  tag: string;
  attrTypes: {
    namespace: {
      id: string;
      name: string;
    };
    items: {
      label: string;
      caption: string;
      type: string;
    }[];
  }[];
}

interface CredentialSchemaOption {
  id: number;
  name: string;
  credentialSchemaId: string;
  credentialSchema: CredentialSchema;
  credentialDefinitions: CredentialDefinition[];
}

interface PredicateDialogResult {
  attributeName: string;
  label: string;
  type: string;
  predicateType: string;
  predicateValue: string;
  definitionId: string;
  namespaceIdentifier: string;
}

interface PredicateSelectDialogProps extends DialogProps<PredicateDialogResult[], unknown> {}

const predicateTypeOptions = [
 { value: "GE", label: "Greater or Equal" },
 { value: "LE", label: "Less or Equal" },
 { value: "GT", label: "Greater Than" },
 { value: "LT", label: "Less Than" }
];

const PredicateSelectDialog: React.FC<PredicateSelectDialogProps> = ({ open, onClose }) => {
  const [schemaId, setSchemaId] = useState<number | ''>('');
  const [schemaOptions, setSchemaOptions] = useState<CredentialSchemaOption[]>([]);
  const [attributes, setAttributes] = useState<Attribute[]>([]);
  const [selectedMap, setSelectedMap] = useState<Record<number, boolean>>({});
  const [predicateTypeMap, setPredicateTypeMap] = useState<Record<number, string>>({});
  const [predicateValueMap, setPredicateValueMap] = useState<Record<number, string>>({});
  const [definitionMap, setDefinitionMap] = useState<Record<number, string[]>>({});
  const [errorSet, setErrorSet] = useState<Set<number>>(new Set());

  useEffect(() => {
    const fetchSchemas = async () => {
      try {
        const res = await getCredentialSchemas();
        const options = res.data
          .filter((schema: any) => Array.isArray(schema.credentialDefinitions) && schema.credentialDefinitions.length > 0)
          .map((schema: any) => ({
            id: schema.id,
            name: schema.name,
            credentialSchemaId: schema.credentialSchemaId,
            credentialSchema: schema.credentialSchema,
            credentialDefinitions: schema.credentialDefinitions.map((def: any) => ({
              id: def.id,
              tag: def.credentialDefinitionTag,
              credentialDefinitionId: def.credentialDefinitionId
            }))
          }));
        setSchemaOptions(options);
      } catch (error) {
        console.error('Failed to fetch schemas with definitions:', error);
      }
    };

    fetchSchemas();
  }, []);

  useEffect(() => {
    if (!schemaId) return;
    const selectedSchema = schemaOptions.find(s => s.id === schemaId);
    if (!selectedSchema) return;

    const attrs: Attribute[] = [];
    selectedSchema.credentialSchema.attrTypes.forEach(group => {
      const nsId = group.namespace.id;
      group.items.forEach(item => {
        if (item.type === 'Number') {
          attrs.push({
            id: Math.random(),
            zkpNamespaceId: nsId,
            label: item.label,
            caption: item.caption,
            type: item.type
          });
        }
      });
    });

    setAttributes(attrs);
    setSelectedMap({});
    setPredicateTypeMap({});
    setPredicateValueMap({});
    setDefinitionMap({});
    setErrorSet(new Set());
  }, [schemaId, schemaOptions]);

  const handleToggle = (id: number) => setSelectedMap(prev => ({ ...prev, [id]: !prev[id] }));
  const handlePredicateTypeChange = (id: number) => (e: any) => setPredicateTypeMap(prev => ({ ...prev, [id]: e.target.value }));
  const handlePredicateValueChange = (id: number) => (e: any) => setPredicateValueMap(prev => ({ ...prev, [id]: e.target.value }));
  const handleDefinitionChange = (id: number) => (e: any) => {
    const selected = e.target.value as string[];
    setDefinitionMap(prev => ({ ...prev, [id]: selected }));
  };


  const handleAdd = () => {
    const selectedSchema = schemaOptions.find(s => s.id === schemaId);
    if (!selectedSchema) return;

    const selectedAttributes = attributes.filter(attr => selectedMap[attr.id]);
    const invalids = selectedAttributes.filter(attr => {
      return !predicateTypeMap[attr.id] || !predicateValueMap[attr.id] || !definitionMap[attr.id];
    }).map(attr => attr.id);

    if (invalids.length > 0) {
      setErrorSet(new Set(invalids));
      return;
    }

    const results: PredicateDialogResult[] = [];

    selectedAttributes.forEach(attr => {
      const defs = definitionMap[attr.id] || [];
      defs.forEach(defId => {
        results.push({
          attributeName: `${attr.zkpNamespaceId}.${attr.label}`,
          label: attr.caption,
          type: attr.type,
          predicateType: predicateTypeMap[attr.id],
          predicateValue: predicateValueMap[attr.id],
          definitionId: defId,
          namespaceIdentifier: selectedSchema.credentialSchemaId
        });
      });
    });

    onClose(results);
  };

  const handleClose = (event: unknown, reason?: string) => {
    if (reason === 'backdropClick') return;
    onClose([]);
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
      <Box sx={{ px: 2 }}>
        <DialogTitle sx={{ p: 0, pt: 2, fontWeight: 700 }}>Add Requested Predicates</DialogTitle>
        <Box sx={{ height: '1px', backgroundColor: '#BFBFBF', width: '100%', mt: 1 }} />
      </Box>
      <DialogContent sx={{ px: 2 }}>
        <Typography sx={{ mt: 2, mb: 1 }}>Select Schema*</Typography>
        <Select
          value={schemaId}
          onChange={(e) => setSchemaId(Number(e.target.value))}
          fullWidth
          size="small"
          displayEmpty
        >
          <MenuItem value="" disabled>Select a schema</MenuItem>
          {schemaOptions.map(schema => (
            <MenuItem key={schema.id} value={schema.id}>{schema.name}</MenuItem>
          ))}
        </Select>

        <Typography sx={{ mt: 3, mb: 1 }}>Select Attributes in Schema*</Typography>
        <TableContainer sx={{ border: '1px solid #ddd' }}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: "#f5f5f5" }}>
              <TableRow>
                <TableCell></TableCell>
                <TableCell>Namespace ID</TableCell>
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
                    <Checkbox checked={!!selectedMap[attr.id]} onChange={() => handleToggle(attr.id)} />
                  </TableCell>
                  <TableCell>{attr.zkpNamespaceId}</TableCell>
                  <TableCell>{attr.caption}</TableCell>
                  <TableCell>{attr.type}</TableCell>
                  <TableCell>
                    <Select
                      size="small"
                      value={predicateTypeMap[attr.id] || ''}
                      onChange={handlePredicateTypeChange(attr.id)}
                      fullWidth
                      error={selectedMap[attr.id] && !predicateTypeMap[attr.id] && errorSet.has(attr.id)}
                      displayEmpty
                    >
                      <MenuItem disabled value="">Select</MenuItem>
                      {predicateTypeOptions.map(opt => (
                        <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                      ))}
                    </Select>
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      size="small"
                      value={predicateValueMap[attr.id] || ''}
                      onChange={handlePredicateValueChange(attr.id)}
                      error={selectedMap[attr.id] && !predicateValueMap[attr.id] && errorSet.has(attr.id)}
                      fullWidth
                    />
                  </TableCell>
                  <TableCell>
                    <Select
                      multiple
                      size="small"
                      fullWidth
                      value={definitionMap[attr.id] || []}
                      onChange={handleDefinitionChange(attr.id)}
                      error={selectedMap[attr.id] && (!definitionMap[attr.id] || definitionMap[attr.id].length === 0) && errorSet.has(attr.id)}
                      displayEmpty
                      renderValue={(selected) => {
                        const defs = schemaOptions.find(s => s.id === schemaId)?.credentialDefinitions || [];
                        return (selected as string[]).map(defId =>
                          defs.find(d => d.credentialDefinitionId === defId)?.tag || defId
                        ).join(', ');
                      }}
                    >
                      <MenuItem value="" disabled>Select</MenuItem>
                      {(schemaOptions.find(s => s.id === schemaId)?.credentialDefinitions || []).map(def => (
                        <MenuItem key={def.id} value={def.credentialDefinitionId}>
                          {def.tag}
                        </MenuItem>
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
        <Button variant="contained" onClick={handleAdd} disabled={!schemaId} sx={{ width: '40%', height: '44px' }}>
          Add
        </Button>
        <Button variant="outlined" onClick={() => onClose([])} sx={{ width: '40%', height: '44px', mr: 2 }}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PredicateSelectDialog;
