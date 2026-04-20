import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField,
    Box,
    Typography,
    IconButton,
    Collapse,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Alert
} from "@mui/material";
import React, { useEffect, useState } from "react";
import { usePasswordPolicy } from "../../hooks/usePasswordPolicy";
import { ValidationRuleResult } from "../../constants/password-policy";
import { Visibility, VisibilityOff, Check, Close, ExpandMore, ExpandLess, Info, Shield, AccessTime, VpnKey } from "@mui/icons-material";

// Types for password reset reasons
type PasswordResetReason = 'FIRST_LOGIN' | 'EXPIRED' | 'ADMIN_FORCED';

interface PasswordResetDialogProps {
    open: boolean;
    onClose: () => void;
    onSubmit: (newPassword: string) => void;
    passwordResetReason?: PasswordResetReason | null;
    isPasswordExpired?: boolean;
}

interface ErrorState {
    newPassword?: string;
    confirmPassword?: string;
}

const PasswordResetDialog: React.FC<PasswordResetDialogProps> = ({
                                                                     open,
                                                                     onClose,
                                                                     onSubmit,
                                                                     passwordResetReason,
                                                                     isPasswordExpired
                                                                 }) => {
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [errors, setErrors] = useState<ErrorState>({});
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Password visibility states
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    // Requirements visibility
    const [showRequirements, setShowRequirements] = useState(false);

    // Focus and touch states for better UX
    const [newPasswordTouched, setNewPasswordTouched] = useState(false);
    const [confirmPasswordTouched, setConfirmPasswordTouched] = useState(false);

    // Password policy hook
    const {
        policy,
        isLoading: isPolicyLoading,
        validatePassword,
        getValidationResults,
        getValidationSummary
    } = usePasswordPolicy();

    // Real-time password validation results
    const [validationResults, setValidationResults] = useState<ValidationRuleResult[]>([]);
    const [validationSummary, setValidationSummary] = useState({
        isValid: false,
        passedCount: 0,
        totalCount: 0,
        failedRules: [] as string[]
    });

    // Get message and UI info based on password reset reason
    const getPasswordResetInfo = () => {
        switch (passwordResetReason) {
            case 'FIRST_LOGIN':
                return {
                    title: 'Set New Password',
                    message: 'Welcome! For security reasons, please set a new password for your account.',
                    icon: <Shield sx={{ color: 'info.main' }} />,
                    severity: 'info' as const
                };
            case 'EXPIRED':
                return {
                    title: 'Password Expired',
                    message: 'Your password has expired. Please set a new password to continue.',
                    icon: <AccessTime sx={{ color: 'warning.main' }} />,
                    severity: 'warning' as const
                };
            case 'ADMIN_FORCED':
                return {
                    title: 'Password Reset Required',
                    message: 'Your password has been reset by an administrator. Please set a new password.',
                    icon: <VpnKey sx={{ color: 'error.main' }} />,
                    severity: 'error' as const
                };
            default:
                return {
                    title: 'Reset Password',
                    message: 'Please set a new password.',
                    icon: <VpnKey sx={{ color: 'primary.main' }} />,
                    severity: 'info' as const
                };
        }
    };

    const passwordResetInfo = getPasswordResetInfo();

    const handleConfirm = async () => {
        if (!validate()) return;
        setIsSubmitting(true);
        await onSubmit(newPassword);
    };

    const handleChange = (setter: React.Dispatch<React.SetStateAction<string>>) =>
        (event: React.ChangeEvent<HTMLInputElement>) => setter(event.target.value);

    const handleNewPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const password = event.target.value;
        setNewPassword(password);

        // Update real-time validation when policy is available
        if (policy && password) {
            const results = getValidationResults(password);
            const summary = getValidationSummary(password);
            setValidationResults(results);
            setValidationSummary(summary);

            // Auto-show requirements when user starts typing
            if (!showRequirements) {
                setShowRequirements(true);
            }

            // Auto-hide requirements when password becomes valid and is not focused
            if (summary.isValid && document.activeElement?.id !== 'new-password-reset') {
                setTimeout(() => {
                    setShowRequirements(false);
                }, 1500);
            }
        } else {
            setValidationResults([]);
            setValidationSummary({
                isValid: false,
                passedCount: 0,
                totalCount: 0,
                failedRules: []
            });
        }
    };

    const handleNewPasswordBlur = () => {
        setNewPasswordTouched(true);
    };

    const handleConfirmPasswordBlur = () => {
        setConfirmPasswordTouched(true);
    };

    const validate = () => {
        let tempErrors: ErrorState = {};

        // Validate new password using policy
        if (!newPassword.trim()) {
            tempErrors.newPassword = "Please enter a new password.";
        } else if (policy && newPasswordTouched && !validatePassword(newPassword)) {
            tempErrors.newPassword = "Password does not meet policy requirements.";
        } else if (!policy && (newPassword.length < 8 || newPassword.length > 64)) {
            // Fallback validation if policy is not available
            tempErrors.newPassword = "Password must be between 8 and 64 characters.";
        }

        // Validate password confirmation
        if (!confirmPassword.trim()) {
            tempErrors.confirmPassword = "Please confirm your new password.";
        } else if (confirmPasswordTouched && confirmPassword !== newPassword) {
            tempErrors.confirmPassword = "Passwords do not match.";
        }

        setErrors(tempErrors);
        return Object.values(tempErrors).every((error) => !error);
    };



    // Update button state based on validation
    useEffect(() => {
        const hasBasicInput = newPassword.trim() && confirmPassword.trim();
        const isPasswordValid = policy ? validatePassword(newPassword) : newPassword.length >= 8;
        const passwordsMatch = newPassword === confirmPassword;

        setIsButtonDisabled(!hasBasicInput || !isPasswordValid || !passwordsMatch);
    }, [newPassword, confirmPassword, policy, validatePassword]);

    // Reset form when dialog opens
    useEffect(() => {
        if (open) {
            setNewPassword("");
            setConfirmPassword("");
            setErrors({});
            setIsButtonDisabled(true);
            setValidationResults([]);
            setValidationSummary({
                isValid: false,
                passedCount: 0,
                totalCount: 0,
                failedRules: []
            });
            setShowNewPassword(false);
            setShowConfirmPassword(false);
            setShowRequirements(false);
            setNewPasswordTouched(false);
            setConfirmPasswordTouched(false);
        }
    }, [open]);

    // Validate form on changes
    useEffect(() => {
        validate();
    }, [newPassword, confirmPassword, newPasswordTouched, confirmPasswordTouched]);

    // Show loading if policy is being loaded
    if (isPolicyLoading) {
        return (
            <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" sx={{ maxWidth: 500, margin: "0 auto" }}>
                <DialogContent sx={{ textAlign: 'center', py: 4 }}>
                    <Typography>Loading password policy...</Typography>
                </DialogContent>
            </Dialog>
        );
    }

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" sx={{ maxWidth: 500, margin: "0 auto" }} disableEscapeKeyDown={isSubmitting}>
            <Box sx={{ px: 2 }}>
                <DialogTitle sx={{ p: 0, pt: 2, fontWeight: 700 }}>
                    {passwordResetInfo.title}
                </DialogTitle>
                <Box sx={{ height: "1px", backgroundColor: "var(--G40, #BFBFBF)", width: "100%", mt: 1 }} />
            </Box>

            <DialogContent sx={{ px: 2, pt: 3, position: 'relative' }}>
                {isSubmitting && (
                    <Box
                        sx={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                            zIndex: 10,
                            borderRadius: '4px'
                        }}
                    >
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                            <Box
                                sx={{
                                    width: 48,
                                    height: 48,
                                    border: '4px solid #f3f3f3',
                                    borderTop: '4px solid #FF8400',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite',
                                    '@keyframes spin': {
                                        '0%': { transform: 'rotate(0deg)' },
                                        '100%': { transform: 'rotate(360deg)' },
                                    },
                                }}
                            />
                            <Typography sx={{ fontSize: '16px', fontWeight: 500, color: '#333' }}>
                                Updating your password...
                            </Typography>
                        </Box>
                    </Box>
                )}

                {/* Password Reset Reason Message */}
                <Alert
                    severity={passwordResetInfo.severity}
                    sx={{ mb: 2 }}
                    variant="outlined"
                    icon={passwordResetInfo.icon}
                >
                    {passwordResetInfo.message}
                </Alert>
                {/* New Password */}
                <TextField
                    id="new-password-reset"
                    fullWidth
                    label="New Password *"
                    type={showNewPassword ? "text" : "password"}
                    variant="outlined"
                    margin="normal"
                    value={newPassword}
                    onChange={handleNewPasswordChange}
                    onBlur={handleNewPasswordBlur}
                    error={!!errors.newPassword}
                    helperText={errors.newPassword}
                    disabled={isSubmitting}
                    InputProps={{
                        endAdornment: (
                            <IconButton
                                aria-label="toggle password visibility"
                                onClick={() => setShowNewPassword(!showNewPassword)}
                                edge="end"
                                disabled={isSubmitting}
                            >
                                {showNewPassword ? <VisibilityOff /> : <Visibility />}
                            </IconButton>
                        ),
                    }}
                />



                {/* Password Requirements Toggle */}
                {newPassword && policy && (
                    <Box sx={{ mt: 1 }}>
                        <Button
                            size="small"
                            variant="text"
                            color="inherit"
                            startIcon={<Info fontSize="small" />}
                            endIcon={showRequirements ? <ExpandLess /> : <ExpandMore />}
                            onClick={() => setShowRequirements(!showRequirements)}
                            disabled={isSubmitting}
                            sx={{
                                textTransform: 'none',
                                fontSize: '0.875rem',
                                color: 'text.secondary',
                                p: 0.5,
                                minWidth: 'auto'
                            }}
                        >
                            Requirements ({validationSummary.passedCount}/{validationSummary.totalCount})
                        </Button>

                        {/* Password Requirements List */}
                        <Collapse in={showRequirements}>
                            <Box sx={{
                                mt: 1,
                                backgroundColor: 'rgba(0,0,0,0.02)',
                                borderRadius: 1,
                                border: '1px solid rgba(0,0,0,0.1)'
                            }}>
                                <List dense sx={{ py: 1 }}>
                                    {validationResults.map((result, index) => (
                                        <ListItem key={index} sx={{ py: 0.25, px: 2 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                {result.isValid ? (
                                                    <Check
                                                        fontSize="small"
                                                        sx={{ color: 'success.main' }}
                                                    />
                                                ) : (
                                                    <Close
                                                        fontSize="small"
                                                        sx={{ color: 'text.disabled' }}
                                                    />
                                                )}
                                            </ListItemIcon>
                                            <ListItemText
                                                primary={result.message}
                                                slotProps={{
                                                    primary: {
                                                        variant: 'body2',
                                                        sx: {
                                                            color: result.isValid ? 'success.main' : 'text.secondary',
                                                            fontSize: '0.875rem'
                                                        }
                                                    }
                                                }}
                                            />
                                        </ListItem>
                                    ))}
                                </List>
                            </Box>
                        </Collapse>
                    </Box>
                )}

                {/* Confirm Password */}
                <TextField
                    fullWidth
                    label="Confirm Password *"
                    type={showConfirmPassword ? "text" : "password"}
                    variant="outlined"
                    margin="normal"
                    value={confirmPassword}
                    onChange={handleChange(setConfirmPassword)}
                    onBlur={handleConfirmPasswordBlur}
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword}
                    disabled={isSubmitting}
                    InputProps={{
                        endAdornment: (
                            <IconButton
                                aria-label="toggle password visibility"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                edge="end"
                                disabled={isSubmitting}
                            >
                                {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                            </IconButton>
                        ),
                    }}
                />
            </DialogContent>

            <DialogActions sx={{ px: 2, pt: 0, display: "flex", gap: 2, mt: 0 }}>
                <Button
                    variant="outlined"
                    onClick={onClose}
                    color="primary"
                    sx={{ flexGrow: 1, height: "48px" }}
                    disabled={isSubmitting}
                >
                    Cancel
                </Button>
                <Button
                    variant="contained"
                    onClick={handleConfirm}
                    color="primary"
                    disabled={isButtonDisabled || isSubmitting}
                    sx={{ flexGrow: 1, height: "48px" }}
                >
                    {isSubmitting ? 'Updating...' : (passwordResetReason === 'FIRST_LOGIN' ? 'Set Password' : 'Reset Password')}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default PasswordResetDialog;
