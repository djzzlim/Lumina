import js from "@eslint/js";
import security from "eslint-plugin-security";

export default [
  {
    files: ["app/src/main/assets/extensions/**/*.js"],
    plugins: {
      security,
    },
    rules: {
      // ── ESLint recommended (key rules only) ───────────────────────
      "no-eval": "error",
      "no-implied-eval": "error",
      "no-new-func": "error",
      "no-unused-vars": "warn",

      // ── Security plugin rules ──────────────────────────────────────
      "security/detect-eval-with-expression": "error",
      "security/detect-non-literal-regexp": "warn",
      "security/detect-unsafe-regex": "error",
      "security/detect-buffer-noassert": "error",
      "security/detect-disable-mustache-escape": "error",
      "security/detect-new-buffer": "error",
      "security/detect-no-csrf-before-method-override": "error",
      "security/detect-object-injection": "warn",
      "security/detect-possible-timing-attacks": "warn",
      "security/detect-pseudoRandomBytes": "error",
    },
    languageOptions: {
      ecmaVersion: 2022,
      globals: {
        // WebExtension API globals
        browser:    "readonly",
        chrome:     "readonly",
        // Standard browser globals (content script environment)
        window:         "readonly",
        document:       "readonly",
        navigator:      "readonly",
        console:        "readonly",
        fetch:          "readonly",
        Date:           "readonly",
        Math:           "readonly",
        JSON:           "readonly",
        Intl:           "readonly",
        performance:    "readonly",
        MutationObserver: "readonly",
        setTimeout:     "readonly",
        clearTimeout:   "readonly",
        setInterval:    "readonly",
        clearInterval:  "readonly",
      },
    },
  },
];
