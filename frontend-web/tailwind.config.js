/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        surface: {
          1: "rgba(15, 23, 42, 0.55)",
          2: "rgba(15, 23, 42, 0.72)",
          3: "rgba(15, 23, 42, 0.90)",
        },
        glass: {
          DEFAULT: "rgba(30, 41, 59, 0.65)",
          border: "rgba(148, 163, 184, 0.18)",
        },
      },

      boxShadow: {
        card: "0 10px 40px rgba(0,0,0,0.35)",
        "card-hover": "0 16px 50px rgba(0,0,0,0.45)",
        glass: "0 6px 24px rgba(0,0,0,0.25)",
        glow: "0 0 20px rgba(59,130,246,0.35)",
      },

      backdropBlur: {
        sm: "6px",
        DEFAULT: "14px",
        md: "20px",
        lg: "28px",
      },

      backgroundImage: {
        "primary-gradient": "linear-gradient(135deg, #3B82F6, #6366F1)",
        "soft-radial":
          "radial-gradient(circle at 20% 20%, rgba(59,130,246,0.08), transparent 40%)",
      },

      transitionTimingFunction: {
        smooth: "cubic-bezier(0.4, 0, 0.2, 1)",
      },

      animation: {
        "fade-in": "fadeIn 0.4s ease-out",
      },

      keyframes: {
        fadeIn: {
          "0%": { opacity: 0 },
          "100%": { opacity: 1 },
        },
      },
    },
  },
  plugins: [
    function ({ matchUtilities, theme }) {
      matchUtilities(
        {
          "animate-delay": (value) => ({
            animationDelay: value,
          }),
        },
        {
          values: theme("transitionDelay"),
        },
      );
    },
  ],
};
