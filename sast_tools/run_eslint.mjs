import { ESLint } from "/Users/lim/StudioProjects/Lumina/sast_tools/node_modules/eslint/lib/eslint/eslint.js";
import { readFileSync } from "fs";

const eslint = new ESLint({
  overrideConfigFile: "/Users/lim/StudioProjects/Lumina/sast_tools/eslint.config.mjs",
  allowInlineConfig: true,
});

const results = await eslint.lintFiles([
  "/Users/lim/StudioProjects/Lumina/app/src/main/assets/extensions/timezone_spoofer/background.js",
  "/Users/lim/StudioProjects/Lumina/app/src/main/assets/extensions/timezone_spoofer/content.js",
]);

const formatter = await eslint.loadFormatter("stylish");
const output = await formatter.format(results);
console.log(output || "✅ No issues found.");

const errors = results.reduce((acc, r) => acc + r.errorCount, 0);
const warnings = results.reduce((acc, r) => acc + r.warningCount, 0);
console.log(`\nSummary: ${errors} errors, ${warnings} warnings`);
