package org.delcom.ifs25043springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Pemasukan menambah saldo, sedangkan pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    /** Transaksi yang menambah saldo. */
    INCOME("Pemasukan"),

    /** Transaksi yang mengurangi saldo. */
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Label berbahasa Indonesia untuk ditampilkan di UI. */
    public String getLabel() {
        return label;
    }
}
