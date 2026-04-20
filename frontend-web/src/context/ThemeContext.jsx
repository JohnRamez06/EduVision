import React, { createContext, useState } from 'react'
export const ThemeContext = createContext(null)
export function ThemeProvider({ children }) {
  const [dark, setDark] = useState(true)
  return <ThemeContext.Provider value={{ dark, toggle: () => setDark(dark) }}>{children}</ThemeContext.Provider>
}
