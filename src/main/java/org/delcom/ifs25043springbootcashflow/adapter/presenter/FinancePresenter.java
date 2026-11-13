package org.delcom.ifs25043springbootcashflow.adapter.presenter;

import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Memformat nominal uang dengan pemisah ribuan koma dan dua digit desimal.
     * Contoh: 1000000 → "Rp 1,000,000.00".
     */
    public String formatMoney(long amount) {
        String digits = String.format(Locale.US, "%,d.00", Math.abs(amount));
        return amount < 0 ? "-Rp " + digits : "Rp " + digits;
    }

    /** Memformat tipe transaksi menjadi label dalam tanda kurung siku. */
    public String formatType(TransactionType type) {
        return type == null ? "-" : "[" + type.getLabel() + "]";
    }

    /** Memformat satu transaksi menjadi baris teks untuk ditampilkan. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | %s",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatDateTime(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo dengan label "Saldo:". */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /**
     * Memformat daftar transaksi menjadi teks multi-baris.
     * Mengembalikan pesan kosong jika list tidak berisi data.
     */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(
                transactions,
                "Hasil Pencarian: \"" + keyword + "\"",
                "- Transaksi tidak ditemukan!"
        );
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah mengubah transaksi. */
    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    /** Pesan gagal saat mengubah transaksi. */
    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showDeleteSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showDeleteFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan saat opsi pengurutan tidak valid. */
    public String showInvalidSortOption() {
        return "[!] Pilihan pengurutan tidak valid!";
    }
}
