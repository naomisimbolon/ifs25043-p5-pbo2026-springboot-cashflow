package org.delcom.ifs25043springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {
    /** Urutkan nominal dari yang terkecil. */
    AMOUNT_SMALLEST_FIRST(
            "Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount)
    ),

    /** Urutkan nominal dari yang terbesar. */
    AMOUNT_LARGEST_FIRST(
            "Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).reversed()
    ),

    /** Tampilkan pemasukan terlebih dahulu. */
    INCOME_FIRST(
            "Pemasukan dulu",
            Comparator.comparingInt(transaction ->
                    transaction.getType() == TransactionType.INCOME ? 0 : 1)
    ),

    /** Tampilkan pengeluaran terlebih dahulu. */
    EXPENSE_FIRST(
            "Pengeluaran dulu",
            Comparator.comparingInt(transaction ->
                    transaction.getType() == TransactionType.EXPENSE ? 0 : 1)
    ),

    /** Tampilkan transaksi terbaru terlebih dahulu. */
    NEWEST_FIRST(
            "Terbaru dulu",
            Comparator.comparing(Transaction::getCreatedAt).reversed()
    ),

    /** Tampilkan transaksi terlama terlebih dahulu. */
    OLDEST_FIRST(
            "Terlama dulu",
            Comparator.comparing(Transaction::getCreatedAt)
    );

    /** Label berbahasa Indonesia untuk ditampilkan di UI. */
    private final String label;

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return label;
    }
}
