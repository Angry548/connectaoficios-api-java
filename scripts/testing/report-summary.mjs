import fs from 'node:fs';
import path from 'node:path';

// Lee exclusivamente los XML generados por Maven/JaCoCo, nunca el environment.
const attributes = text => Object.fromEntries(
  [...text.matchAll(/([\w-]+)="([^"]*)"/g)].map(match => [match[1], match[2]])
);
const totals = { tests: 0, passed: 0, failures: 0, errors: 0, skipped: 0 };
const suites = [];
for (const file of fs.readdirSync('target/surefire-reports').filter(name => /^TEST-.*\.xml$/.test(name))) {
  const xml = fs.readFileSync(path.join('target/surefire-reports', file), 'utf8');
  const suite = attributes(xml.match(/<testsuite\b([^>]*)>/)[1]);
  for (const key of ['tests', 'failures', 'errors', 'skipped']) totals[key] += Number(suite[key]);
  suites.push({ name: suite.name, tests: Number(suite.tests), failures: Number(suite.failures), errors: Number(suite.errors), skipped: Number(suite.skipped) });
}
totals.passed = totals.tests - totals.failures - totals.errors - totals.skipped;
const xml = fs.readFileSync('target/site/jacoco/jacoco.xml', 'utf8');
const coverage = {};
// Los contadores finales del documento son los agregados de report, tras los de package/class.
for (const match of xml.matchAll(/<counter\b([^>]*)\/>/g)) {
  const counter = attributes(match[1]);
  const covered = Number(counter.covered), missed = Number(counter.missed);
  coverage[counter.type] = { covered, missed, percent: Number((100 * covered / (covered + missed || 1)).toFixed(2)) };
}
const result = { generatedAt: new Date().toISOString(), totals, coverage, suites };
fs.mkdirSync('reports/unit', { recursive: true });
fs.writeFileSync('reports/unit/summary.json', JSON.stringify(result, null, 2) + '\n');
console.log(JSON.stringify({ totals, coverage }, null, 2));
