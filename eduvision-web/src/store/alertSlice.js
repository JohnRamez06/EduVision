import { createSlice } from '@reduxjs/toolkit'
const alertSlice = createSlice({ name: 'alerts', initialState: { list: [] }, reducers: { addAlert: (state, action) => { state.list.unshift(action.payload) } } })
export const { addAlert } = alertSlice.actions
export default alertSlice.reducer
