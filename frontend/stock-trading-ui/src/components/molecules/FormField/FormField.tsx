import { TextField, type TextFieldProps } from '@mui/material';

export function FormField(props: TextFieldProps) {
  return <TextField fullWidth variant="outlined" margin="normal" {...props} />;
}
