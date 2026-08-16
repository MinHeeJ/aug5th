/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        background: "#fff7e8",
        foreground: "#000000",
        border: "#000000",
        card: "#ffffff",
        primary: "#ffdc58",
        "primary-hover": "#ffd12e",
        muted: "#efe7d6",
        "muted-foreground": "#6b6355",
        accent: "#ffe7a3",
        destructive: "#e63946",
      },
      fontFamily: {
        head: ["Space Grotesk", "system-ui", "sans-serif"],
        body: ["Inter", "system-ui", "sans-serif"],
      },
      boxShadow: {
        hard: "4px 4px 0 0 #000000",
        "hard-lg": "7px 7px 0 0 #000000",
      },
    },
  },
  plugins: [],
};
