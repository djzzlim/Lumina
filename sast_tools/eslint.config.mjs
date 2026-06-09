import js from "@eslint/js";
import security from "eslint-plugin-security";

export default [
  js.configs.recommended,
  security.configs.recommended,
  {
    files: ["../app/src/main/assets/extensions/**/*.js"],
    plugins: {
      security,
    },
    rules: {
      // ── Security plugin rules ──────────────────────────────────────
      "security/detect-eval-with-expression": "error",
      "security/detect-non-literal-regexp": "warn",
      "security/detect-non-literal-fs-filename": "warn",
      "security/detect-unsafe-regex": "error",
      "security/detect-buffer-noassert": "error",
      "security/detect-child-process": "warn",
      "security/detect-disable-mustache-escape": "error",
      "security/detect-new-buffer": "error",
      "security/detect-no-csrf-before-method-override": "error",
      "security/detect-object-injection": "warn",
      "security/detect-possible-timing-attacks": "warn",
      "security/detect-pseudoRandomBytes": "error",

      // ── General JS dangerous patterns ──────────────────────────────
      "no-eval": "error",
      "no-implied-eval": "error",
      "no-new-func": "error",
    },
    languageOptions: {
      ecmaVersion: 2022,
      // Declare browser extension + content script globals
      globals: {
        browser: "readonly",
        chrome:  "readonly",
        console: "readonly",
        fetch:   "readonly",
        Date:    "readonly",
        Math:    "readonly",
        JSON:    "readonly",
        // Content script globals
        window:      "readonly",
        document:    "readonly",
        navigator:   "readonly",
        Intl:        "readonly",
        performance: "readonly",
        MutationObserver: "readonly",
      },
    },
  },
];
