import {
  Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
  MenuItem, Select, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography
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

interface DialogResult {
  attributeName: string;
  definitionId: string;
}

type AttributeSelectDialogProps = DialogProps<DialogResult[], unknown>;

const AttributeSelectDialog: React.FC<AttributeSelectDialogProps> = ({ open, onClose }) => {
  const [schemaId, setSchemaId] = useState<number | ''>('');
  const [schemaOptions, setSchemaOptions] = useState<CredentialSchemaOption[]>([]);
  const [attributes, setAttributes] = useState<Attribute[]>([]);
  const [selectedMap, setSelectedMap] = useState<Record<number, boolean>>({});
  const [definitionMap, setDefinitionMap] = useState<Record<number, string[]>>({});
  const [missingDefinitionSet, setMissingDefinitionSet] = useState<Set<number>>(new Set());

  useEffect(() => {
    const fetchSchemas = async () => {
      try {
        const res = await getCredentialSchemas();
        const options = res.data
          .filter((schema: any) =>
            Array.isArray(schema.credentialDefinitions) &&
            schema.credentialDefinitions.length > 0
          )
          .map((schema: any) => ({
            id: schema.id,
            name: schema.name,
            credentialSchemaId: schema.credentialSchemaId,
            credentialSchema: schema.credentialSchema,
            credentialDefinitions: schema.credentialDefinitions.map((def: any) => ({
              id: def.id,
              tag: def.credentialDefinitionTag,
              credentialDefinitionId: def.credentialDefinitionId,
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

    const attrTypes = selectedSchema.credentialSchema.attrTypes;
    const extractedAttributes: Attribute[] = [];

    attrTypes.forEach((group) => {
      const namespaceId = group.namespace.id;
      const items = group.items || [];
      items.forEach((item) => {
        if (item.type === 'String') {
          extractedAttributes.push({
            id: Math.random(),
            zkpNamespaceId: namespaceId,
            label: item.label,
            caption: item.caption,
            type: item.type,
          });
        }
      });
    });

    setAttributes(extractedAttributes);
    setDefinitionMap({});
    setSelectedMap({});
    setMissingDefinitionSet(new Set());
  }, [schemaId, schemaOptions]);

  const handleToggle = (id: number) => {
    setSelectedMap(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const handleDefinitionChange = (id: number) => (event: any) => {
    const values = event.target.value as string[];
    setDefinitionMap((prev) => ({ ...prev, [id]: values }));
    setMissingDefinitionSet((prev) => {
      const updated = new Set(prev);
      if (values && values.length > 0) {
        updated.delete(id);
      } else {
        updated.add(id);
      }
      return updated;
    });
  };

  const handleClose = (event: unknown, reason?: string) => {
    if (reason === 'backdropClick') return;
    onClose([]);
  };

  const handleAdd = async () => {
    const selectedSchema = schemaOptions.find(s => s.id === schemaId);
    if (!selectedSchema) return;

    const selectedAttributes = attributes.filter(attr => selectedMap[attr.id]);

    const missingDefs = selectedAttributes.filter(attr => !definitionMap[attr.id]).map(attr => attr.id);
    if (missingDefs.length > 0) {
      setMissingDefinitionSet(new Set(missingDefs));
      return;
    }

    const result: DialogResult[] = [];

    selectedAttributes.forEach(attr => {
      const selectedDefinitionIds = definitionMap[attr.id] || [];
      selectedDefinitionIds.forEach(defId => {
        result.push({
          attributeName: `${attr.zkpNamespaceId}.${attr.label}`,
          definitionId: defId,
        });
      });
    });

    console.log(result);

    await onClose(result);
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
      <Box sx={{ px: 2 }}>
        <DialogTitle sx={{ p: 0, pt: 2, fontWeight: 700 }}>Add Requested Attributes</DialogTitle>
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
                <TableCell sx={{ width: 150 }}>Namespace ID</TableCell>
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
                  <TableCell>{attr.zkpNamespaceId}</TableCell>
                  <TableCell>{attr.caption}</TableCell>
                  <TableCell>{attr.type}</TableCell>
                  <TableCell>
                    <Select
                      multiple
                      fullWidth
                      size="small"
                      value={definitionMap[attr.id] || []}
                      onChange={handleDefinitionChange(attr.id)}
                      error={selectedMap[attr.id] && (!definitionMap[attr.id] || definitionMap[attr.id].length === 0)}
                      displayEmpty
                      renderValue={(selected) => {
                        const defList = schemaOptions.find(s => s.id === schemaId)?.credentialDefinitions || [];
                        return (selected as string[])
                          .map(id => defList.find(d => d.credentialDefinitionId === id)?.tag || id)
                          .join(', ');
                      }}
                    >
                      <MenuItem disabled value="">
                        Select Definition(s)
                      </MenuItem>
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

export default AttributeSelectDialog;
