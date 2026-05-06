#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const XLSX = require('/root/saas-vue-src/node_modules/xlsx');

const DEFAULT_DB_NAME = 'clinic_system_new';
const DEFAULT_DB_USER = 'root';
const DEFAULT_DB_PASSWORD = 'root';
const DEFAULT_FILE = '/root/shared-uploads/20260501-140441-723576176844061-1-____2605011403.xlsx';
const DEFAULT_REPORT_DIR = '/root/shared-downloads';
const IMPORT_TIMESTAMP = formatTimestamp(new Date());

function parseArgs(argv) {
  const options = {
    file: DEFAULT_FILE,
    dbName: DEFAULT_DB_NAME,
    dbUser: DEFAULT_DB_USER,
    dbPassword: DEFAULT_DB_PASSWORD,
    reportDir: DEFAULT_REPORT_DIR
  };
  for (let index = 0; index < argv.length; index += 1) {
    const current = argv[index];
    if (current === '--file') {
      options.file = argv[index + 1];
      index += 1;
      continue;
    }
    if (current === '--db-name') {
      options.dbName = argv[index + 1];
      index += 1;
      continue;
    }
    if (current === '--db-user') {
      options.dbUser = argv[index + 1];
      index += 1;
      continue;
    }
    if (current === '--db-password') {
      options.dbPassword = argv[index + 1];
      index += 1;
      continue;
    }
    if (current === '--report-dir') {
      options.reportDir = argv[index + 1];
      index += 1;
      continue;
    }
    throw new Error(`Unknown option: ${current}`);
  }
  return options;
}

function formatTimestamp(date) {
  const pad = value => String(value).padStart(2, '0');
  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate())
  ].join('') + '-' + [
    pad(date.getHours()),
    pad(date.getMinutes()),
    pad(date.getSeconds())
  ].join('');
}

function sanitizeText(value, maxLength) {
  const text = value == null ? '' : String(value).trim();
  if (!text) {
    return null;
  }
  return text.slice(0, maxLength);
}

function normalizePhone(value) {
  const raw = value == null ? '' : String(value).trim();
  if (!raw) {
    return null;
  }
  const noDecimalTail = raw.replace(/\.0$/, '');
  const compact = noDecimalTail.replace(/[\s\-()（）]/g, '');
  const digits = compact.replace(/\D/g, '');
  const normalized = digits || compact;
  return normalized ? normalized.slice(0, 20) : null;
}

function normalizeGender(value) {
  const text = sanitizeText(value, 10);
  if (!text) {
    return null;
  }
  if (text === '男' || text === '女') {
    return text;
  }
  return null;
}

function normalizeAge(value) {
  const text = value == null ? '' : String(value).trim();
  if (!text) {
    return null;
  }
  const numeric = Number(text);
  if (!Number.isFinite(numeric)) {
    return null;
  }
  const age = Math.round(numeric);
  if (age < 0 || age > 150) {
    return null;
  }
  return age;
}

function normalizeCustomerSource(value) {
  const raw = sanitizeText(value, 100);
  if (!raw) {
    return '暂未确认';
  }
  if (raw === '微信' || raw === '大众点评' || raw === '电话' || raw === '抖音/小红书' || raw === '转介绍' || raw === '自然到店' || raw === '其他' || raw === '暂未确认') {
    return raw;
  }
  if (raw === '美团') {
    return '大众点评';
  }
  if (raw === '抖音' || raw === '小红书') {
    return '抖音/小红书';
  }
  if (raw === '114查询' || raw.includes('电话')) {
    return '电话';
  }
  if (raw === '家住附近'
    || raw === '附近居民'
    || raw === '路过'
    || raw === '自然上门'
    || raw === '在附近玩儿'
    || raw === '在附近工作') {
    return '自然到店';
  }
  if (raw === '网络咨询'
    || raw === '诊所网站'
    || raw === '网站'
    || raw === '户外广告'
    || raw === '社区活动'
    || raw === '省博'
    || raw === '公交车') {
    return '其他';
  }
  if (raw === '朋友介绍'
    || raw === '亲友介绍'
    || raw === '工厂推荐'
    || raw === '老李患者'
    || raw === '转诊'
    || raw.endsWith('介绍')
    || raw.endsWith('推荐')) {
    return '转介绍';
  }
  return '其他';
}

function escapeSql(value) {
  return String(value)
    .replace(/\\/g, '\\\\')
    .replace(/'/g, "\\'");
}

function sqlValue(value) {
  if (value === null || value === undefined) {
    return 'NULL';
  }
  if (typeof value === 'number') {
    return String(value);
  }
  return `'${escapeSql(value)}'`;
}

function loadRows(filePath) {
  const workbook = XLSX.readFile(filePath);
  const firstSheet = workbook.SheetNames[0];
  const sheet = workbook.Sheets[firstSheet];
  const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
  return rows.slice(3).filter(row => row.some(cell => String(cell == null ? '' : cell).trim() !== ''));
}

function loadExistingKeys(options) {
  const output = execFileSync(
    'mysql',
    [
      `-u${options.dbUser}`,
      `-p${options.dbPassword}`,
      '-D',
      options.dbName,
      '-Nse',
      "SELECT COALESCE(name, ''), COALESCE(phone, '') FROM patients"
    ],
    { encoding: 'utf8' }
  );
  return new Set(
    output
      .split('\n')
      .filter(Boolean)
      .map(line => {
        const [name, phone] = line.split('\t');
        return `${name}\t${phone}`;
      })
  );
}

function buildRecords(rows, existingKeys) {
  const seenKeys = new Set();
  const ready = [];
  const reportRows = [];
  const summary = {
    totalRows: 0,
    insertedRows: 0,
    skippedExisting: 0,
    skippedDuplicateInFile: 0,
    invalidPhoneCount: 0,
    invalidAgeCount: 0,
    blankGenderCount: 0
  };

  for (let index = 0; index < rows.length; index += 1) {
    const row = rows[index];
    const rowNumber = index + 4;
    const chartNo = sanitizeText(row[3], 50);
    const name = sanitizeText(row[1], 50);
    const phone = normalizePhone(row[5]);
    const age = normalizeAge(row[7]);
    const gender = normalizeGender(row[8]);
    const rawSource = sanitizeText(row[12], 100);
    const customerSource = normalizeCustomerSource(rawSource);

    summary.totalRows += 1;
    if (!phone || !/^\d{11}$/.test(phone)) {
      summary.invalidPhoneCount += 1;
    }
    if (age === null) {
      summary.invalidAgeCount += 1;
    }
    if (gender === null) {
      summary.blankGenderCount += 1;
    }

    if (!name) {
      reportRows.push([rowNumber, 'skip', '', phone || '', age == null ? '' : age, gender || '', rawSource || '', customerSource, chartNo || '', 'empty_name']);
      continue;
    }

    const key = `${name}\t${phone || ''}`;
    if (existingKeys.has(key)) {
      summary.skippedExisting += 1;
      reportRows.push([rowNumber, 'skip', name, phone || '', age == null ? '' : age, gender || '', rawSource || '', customerSource, chartNo || '', 'existing_name_phone']);
      continue;
    }
    if (seenKeys.has(key)) {
      summary.skippedDuplicateInFile += 1;
      reportRows.push([rowNumber, 'skip', name, phone || '', age == null ? '' : age, gender || '', rawSource || '', customerSource, chartNo || '', 'duplicate_name_phone_in_file']);
      continue;
    }
    seenKeys.add(key);

    ready.push({
      name,
      gender,
      age,
      phone,
      customerSource
    });
    summary.insertedRows += 1;
    reportRows.push([rowNumber, 'insert', name, phone || '', age == null ? '' : age, gender || '', rawSource || '', customerSource, chartNo || '', '']);
  }

  return { ready, reportRows, summary };
}

function buildInsertSql(records) {
  if (!records.length) {
    return 'SELECT 1;';
  }
  const statements = [];
  const chunkSize = 400;
  for (let index = 0; index < records.length; index += chunkSize) {
    const chunk = records.slice(index, index + chunkSize);
    const values = chunk.map(record => `(${[
      sqlValue(record.name),
      'NULL',
      'NULL',
      sqlValue(record.gender),
      sqlValue(record.age),
      'NULL',
      sqlValue(record.phone),
      'NULL',
      'NULL',
      'NULL',
      'NULL',
      'NULL',
      'NULL',
      sqlValue(record.customerSource)
    ].join(', ')})`);
    statements.push(
      'INSERT INTO patients (name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, relation_type, related_patient_id, related_patient_name, wechat_openid, customer_source)\n'
      + 'VALUES\n'
      + values.join(',\n')
      + ';'
    );
  }
  return statements.join('\n\n');
}

function writeReport(options, reportRows, summary) {
  fs.mkdirSync(options.reportDir, { recursive: true });
  const baseName = path.basename(options.file, path.extname(options.file));
  const csvPath = path.join(options.reportDir, `${baseName}_导入结果_${IMPORT_TIMESTAMP}.csv`);
  const jsonPath = path.join(options.reportDir, `${baseName}_导入汇总_${IMPORT_TIMESTAMP}.json`);
  const header = ['row_number', 'status', 'name', 'phone', 'age', 'gender', 'raw_source', 'mapped_source', 'chart_no', 'reason'];
  const csvLines = [header]
    .concat(reportRows)
    .map(columns => columns.map(toCsvCell).join(','))
    .join('\n');
  fs.writeFileSync(csvPath, '\ufeff' + csvLines, 'utf8');
  fs.writeFileSync(jsonPath, JSON.stringify(summary, null, 2), 'utf8');
  return { csvPath, jsonPath };
}

function toCsvCell(value) {
  const text = value == null ? '' : String(value);
  return `"${text.replace(/"/g, '""')}"`;
}

function executeImport(options, sql) {
  const sqlPath = `/tmp/patient-import-${IMPORT_TIMESTAMP}.sql`;
  fs.writeFileSync(sqlPath, sql, 'utf8');
  execFileSync(
    'mysql',
    [
      `-u${options.dbUser}`,
      `-p${options.dbPassword}`,
      '-D',
      options.dbName
    ],
    {
      input: sql,
      stdio: ['pipe', 'inherit', 'inherit']
    }
  );
  return sqlPath;
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  if (!fs.existsSync(options.file)) {
    throw new Error(`Excel file not found: ${options.file}`);
  }

  const rows = loadRows(options.file);
  const existingKeys = loadExistingKeys(options);
  const { ready, reportRows, summary } = buildRecords(rows, existingKeys);
  const sql = buildInsertSql(ready);
  executeImport(options, sql);
  const reports = writeReport(options, reportRows, summary);

  console.log(JSON.stringify({
    file: options.file,
    totalRows: summary.totalRows,
    insertedRows: summary.insertedRows,
    skippedExisting: summary.skippedExisting,
    skippedDuplicateInFile: summary.skippedDuplicateInFile,
    invalidPhoneCount: summary.invalidPhoneCount,
    invalidAgeCount: summary.invalidAgeCount,
    blankGenderCount: summary.blankGenderCount,
    reportCsv: reports.csvPath,
    reportJson: reports.jsonPath
  }, null, 2));
}

main();
