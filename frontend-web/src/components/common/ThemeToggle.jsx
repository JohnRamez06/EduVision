import React, { useContext } from 'react'
import { Sun, Moon } from 'lucide-react'
import { ThemeContext } from '../../context/ThemeContext'

export default function ThemeToggle() {
  const { dark, toggle } = useContext(ThemeContext)
  return (
    <button
      onClick={toggle}
      className="w-8 h-8 rounded-lg flex items-center justify-center
                 text-slate-400 hover:text-slate-700 dark:hover:text-white
                 hover:bg-slate-200/70 dark:hover:bg-slate-800/70
                 transition-all duration-150 cursor-pointer"
      aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}
      title={dark ? 'Light mode' : 'Dark mode'}
    >
      {dark
        ? <Sun size={15} className="text-amber-400" />
        : <Moon size={15} className="text-slate-500" />
      }
    </button>
  )
}
