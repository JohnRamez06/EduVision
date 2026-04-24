/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          950: '#070B14',
          900: '#0F172A',
          800: '#1E293B',
          700: '#334155',
          600: '#475569',
        },
      },
      animation: {
        'float':      'float 7s ease-in-out infinite',
        'float-slow': 'float 11s ease-in-out infinite',
        'float-fast': 'float 5s ease-in-out infinite',
        'fade-up':    'fadeUp 0.9s ease-out forwards',
        'fade-up-d1': 'fadeUp 0.9s 0.15s ease-out both',
        'fade-up-d2': 'fadeUp 0.9s 0.3s ease-out both',
        'fade-up-d3': 'fadeUp 0.9s 0.45s ease-out both',
        'fade-up-d4': 'fadeUp 0.9s 0.6s ease-out both',
        'glow-pulse': 'glowPulse 3s ease-in-out infinite',
        'spin-slow':  'spin 25s linear infinite',
        'orbit':      'orbit 12s linear infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px) rotate(0deg)' },
          '33%':       { transform: 'translateY(-18px) rotate(2deg)' },
          '66%':       { transform: 'translateY(-8px) rotate(-1deg)' },
        },
        fadeUp: {
          '0%':   { opacity: '0', transform: 'translateY(28px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        glowPulse: {
          '0%, 100%': { filter: 'drop-shadow(0 0 12px rgba(59,130,246,0.6))' },
          '50%':       { filter: 'drop-shadow(0 0 32px rgba(139,92,246,0.9)) drop-shadow(0 0 60px rgba(59,130,246,0.4))' },
        },
        orbit: {
          '0%':   { transform: 'rotate(0deg) translateX(120px) rotate(0deg)' },
          '100%': { transform: 'rotate(360deg) translateX(120px) rotate(-360deg)' },
        },
      },
      backdropBlur: { xs: '2px' },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
      },
    },
  },
  plugins: [],
}
