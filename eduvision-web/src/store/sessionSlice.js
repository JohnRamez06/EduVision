import { createSlice } from '@reduxjs/toolkit'
const sessionSlice = createSlice({ name: 'session', initialState: { current: null }, reducers: { setCurrentSession: (state, action) => { state.current = action.payload } } })
export const { setCurrentSession } = sessionSlice.actions
export default sessionSlice.reducer
