import { configureStore } from '@reduxjs/toolkit'
import authReducer from './authSlice'
import sessionReducer from './sessionSlice'
import alertReducer from './alertSlice'
export const store = configureStore({ reducer: { auth: authReducer, session: sessionReducer, alerts: alertReducer } })
