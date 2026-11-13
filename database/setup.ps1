<#
.SYNOPSIS
    Membuat database PostgreSQL untuk aplikasi Cashflow berdasarkan file .env.

.DESCRIPTION
    Script ini membaca DB_HOST, DB_PORT, DB_DATABASE, DB_USERNAME, dan DB_PASSWORD
    dari file .env, lalu menjalankan "CREATE DATABASE" jika database belum ada.
    Tabel users/transactions dibuat otomatis oleh aplikasi saat pertama kali dijalankan.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File database\setup.ps1
#>

param(
    [string]$EnvFile = (Join-Path $PSScriptRoot '..\.env')
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $EnvFile)) {
    Write-Host "File .env tidak ditemukan: $EnvFile" -ForegroundColor Red
    Write-Host "Salin .env.example menjadi .env lalu sesuaikan nilainya." -ForegroundColor Yellow
    exit 1
}

$config = @{}
foreach ($line in Get-Content $EnvFile) {
    if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$') {
        $config[$matches[1]] = $matches[2].Trim()
    }
}

$host_    = if ($config['DB_HOST'])     { $config['DB_HOST'] }     else { 'localhost' }
$port     = if ($config['DB_PORT'])     { $config['DB_PORT'] }     else { '5432' }
$database = if ($config['DB_DATABASE']) { $config['DB_DATABASE'] } else { 'pbo2026_cashflow' }
$username = if ($config['DB_USERNAME']) { $config['DB_USERNAME'] } else { 'postgres' }
$password = $config['DB_PASSWORD']

$psql = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psql) {
    $fallback = 'C:\Program Files\PostgreSQL\17\bin\psql.exe'
    if (Test-Path $fallback) {
        $psql = Get-Item $fallback
    } else {
        Write-Host 'psql tidak ditemukan.' -ForegroundColor Red
        Write-Host 'Tambahkan folder bin PostgreSQL ke PATH, contoh:' -ForegroundColor Yellow
        Write-Host '  C:\Program Files\PostgreSQL\17\bin'
        exit 1
    }
}

$env:PGPASSWORD = $password

Write-Host "Memeriksa koneksi ke $host_:$port sebagai $username ..."
& $psql.Source -U $username -h $host_ -p $port -d postgres -tAc 'SELECT 1' | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Gagal terhubung ke PostgreSQL. Pastikan service PostgreSQL sudah berjalan.' -ForegroundColor Red
    exit 1
}

$exists = & $psql.Source -U $username -h $host_ -p $port -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$database'"
if ($exists -eq '1') {
    Write-Host "Database '$database' sudah ada, tidak ada perubahan." -ForegroundColor Green
} else {
    & $psql.Source -U $username -h $host_ -p $port -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $database"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Gagal membuat database '$database'." -ForegroundColor Red
        exit 1
    }
    Write-Host "Database '$database' berhasil dibuat." -ForegroundColor Green
}

Write-Host ''
Write-Host 'Langkah selanjutnya:' -ForegroundColor Cyan
Write-Host '  1. Jalankan aplikasi : .\mvnw.cmd spring-boot:run'
Write-Host "  2. Buka browser      : http://localhost:$($config['APP_PORT'])"
Write-Host '  3. Login admin       : ' -NoNewline
Write-Host "$($config['ROOT_USERNAME']) / $($config['ROOT_PASSWORD'])" -ForegroundColor Yellow
