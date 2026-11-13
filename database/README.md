# Persiapan PostgreSQL

Aplikasi memakai PostgreSQL dengan konfigurasi default berikut (lihat `.env`):

| Variabel | Nilai |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_DATABASE` | `pbo2026_cashflow` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

## 1. Install PostgreSQL

1. Unduh PostgreSQL 17 untuk Windows: <https://www.enterprisedb.com/downloads/postgres-postgresql-downloads>
2. Saat instalasi, isi password superuser `postgres` dengan `postgres` (atau sesuaikan `.env`).
3. Pastikan service `postgresql-x64-17` berjalan (`services.msc`).

> Tidak bisa instalasi / tidak punya hak admin? Pakai **PostgreSQL portable**
> yang disertakan terpisah (`postgresql-portable`) — cukup jalankan
> `start-postgres.ps1`, tanpa instalasi dan tanpa service Windows.

## 2. Buat database

Cara otomatis (Windows, butuh `psql`):

```powershell
powershell -ExecutionPolicy Bypass -File database\setup.ps1
```

Cara manual:

```powershell
psql -U postgres -h localhost -p 5432 -d postgres -f database\01-create-database.sql
```

Atau lewat pgAdmin: klik kanan **Databases → Create → Database**, isi nama `pbo2026_cashflow`.

Tabel `users` dan `transactions` dibuat otomatis oleh `SchemaInitializer` saat aplikasi
pertama kali dijalankan. Jika ingin membuatnya manual, jalankan:

```powershell
psql -U postgres -h localhost -p 5432 -d pbo2026_cashflow -f database\02-schema.sql
```

## 3. Sesuaikan `.env`

```env
APP_PORT=8080
DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=pbo2026_cashflow
DB_USERNAME=postgres
DB_PASSWORD=postgres
ROOT_USERNAME=admin
ROOT_PASSWORD=admin
UPLOAD_DIR=uploads
```

## 4. Jalankan aplikasi

```powershell
.\mvnw.cmd spring-boot:run
```

Buka <http://localhost:8080>, lalu login dengan akun admin dari `ROOT_USERNAME` / `ROOT_PASSWORD`.

## Catatan

- Folder `uploads/` dibuat otomatis dan dipakai untuk menyimpan bukti transaksi serta foto profil.
- Data tetap tersimpan setelah aplikasi di-restart karena memakai PostgreSQL (bukan database in-memory).
- Jika port 5432 dipakai PostgreSQL lain, ubah `DB_PORT` di `.env`.
