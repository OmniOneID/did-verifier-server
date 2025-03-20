import React, { useState, useEffect } from 'react';
import { 
    Dialog, 
    DialogTitle, 
    DialogContent, 
    DialogActions, 
    Button, 
    TextField, 
    Box, 
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Radio,
    CircularProgress
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

interface SearchItem {
    id?: number;
    filterId?: number;
    payloadId?: number;
    policyProfileId?: number;
    title: string;
    service: string;
    [key: string]: any; // For additional properties
}

interface SearchDialogProps {
    open: boolean;
    onClose: () => void;
    onSelect: (item: SearchItem) => void;
    onSearch?: (searchTerm: string) => void; 
    title: string;
    service: string;
    items: SearchItem[];
    loading?: boolean;
    idField?: string; 
}

const SearchDialog: React.FC<SearchDialogProps> = ({ 
    open, 
    onClose, 
    onSelect,
    onSearch,
    title,
    items,
    loading = false,
    idField = 'id' 
}) => {
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [selectedItem, setSelectedItem] = useState<SearchItem | null>(null);
    const [filteredItems, setFilteredItems] = useState<SearchItem[]>(items);
    

    useEffect(() => {
        setFilteredItems(items);
        setSelectedItem(null);
    }, [items]);

    useEffect(() => {
        if (open) {
            setSearchTerm('');
            setSelectedItem(null);
        }
    }, [open]);

    const handleSearch = () => {
        if (onSearch) {
            onSearch(searchTerm);
        } else {
            if (searchTerm.trim() === '') {
                setFilteredItems(items);
            } else {
                const filtered = items.filter(item => 
                    item.title.toLowerCase().includes(searchTerm.toLowerCase())
                );
                setFilteredItems(filtered);
            }
        }
        


    };

    const handleSelect = () => {
        if (selectedItem) {
            onSelect(selectedItem);            
            onClose();
        }
    };

    const handleRadioChange = (item: SearchItem) => {        
        setSelectedItem(item);
    };

    const getItemId = (item: SearchItem): any => {
        return item[idField] !== undefined ? item[idField] : item.id;
    };

    return (
        <Dialog 
            open={open} 
            onClose={onClose}
            maxWidth="sm"            
        >
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <Box sx={{ display: 'flex', mb: 2, mt: 1 }}>
                    <TextField 
                        fullWidth
                        label="Search"
                        variant="outlined"
                        size="small"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyPress={(e) => {
                            if (e.key === 'Enter') {
                                handleSearch();
                            }
                        }}
                        disabled={loading}
                    />
                    <Button 
                        variant="contained" 
                        sx={{ ml: 1 }} 
                        onClick={handleSearch}
                        startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SearchIcon />}
                        disabled={loading}
                    >
                        {loading ? 'Searching...' : 'Search'}
                    </Button>
                </Box>

                <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                    <Table stickyHeader>
                        <TableHead>
                            <TableRow>
                                <TableCell width="10%">Select</TableCell>
                                <TableCell>Title</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={2} align="center" sx={{ py: 3 }}>
                                        <CircularProgress size={32} />
                                        <Typography variant="body2" sx={{ mt: 1 }}>
                                            Loading...
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ) : filteredItems.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={2} align="center">
                                        <Typography variant="body1">No items found</Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                filteredItems.map((item) => (
                                    <TableRow key={getItemId(item)}>
                                        <TableCell>
                                            <Radio 
                                                checked={selectedItem !== null && getItemId(selectedItem) === getItemId(item)}
                                                onChange={() => handleRadioChange(item)}
                                            />
                                        </TableCell>
                                        <TableCell>{item.title}</TableCell>                                        
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </DialogContent>
            <DialogActions sx={{ p: 2 }}>
                <Button onClick={onClose} variant="outlined">
                    Cancel
                </Button>
                <Button 
                    onClick={handleSelect} 
                    variant="contained" 
                    disabled={!selectedItem || loading}
                >
                    Select
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default SearchDialog;