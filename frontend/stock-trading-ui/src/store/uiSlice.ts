import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface UiState {
  livePrices: Record<string, { price: number; changePercent: number }>;
}

const initialState: UiState = { livePrices: {} };

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    updateLivePrice: (
      state,
      action: PayloadAction<{ symbol: string; price: number; changePercent: number }>
    ) => {
      state.livePrices[action.payload.symbol] = {
        price: action.payload.price,
        changePercent: action.payload.changePercent,
      };
    },
  },
});

export const { updateLivePrice } = uiSlice.actions;
export default uiSlice.reducer;
