package org.delcom.ifs25043springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan.
 * Berada di layer domain — tidak bergantung pada layer lain dan bebas dari
 * urusan tampilan maupun penyimpanan.
 */
public class Transaction {
    /** ID unik transaksi, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** Pemilik transaksi. */
    private final int userId;

    /** Keterangan singkat transaksi. */
    private String description;

    /** Nominal transaksi (selalu positif; tipe menentukan arah saldo). */
    private long amount;

    /** Tipe transaksi: pemasukan atau pengeluaran. */
    private final TransactionType type;

    /** Path relatif bukti transaksi (opsional). */
    private String proofPath;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    /** Membuat transaksi baru dengan waktu pencatatan saat ini. */
    public Transaction(int id, int userId, String description, long amount, TransactionType type) {
        this(id, userId, description, amount, type, null, LocalDateTime.now());
    }

    /** Membuat transaksi dengan bukti opsional dan waktu pencatatan saat ini. */
    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath
    ) {
        this(id, userId, description, amount, type, proofPath, LocalDateTime.now());
    }

    /** Membuat transaksi lengkap (dipakai saat memetakan baris database). */
    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** Mengubah nominal transaksi. */
    public void changeAmount(long amount) {
        this.amount = amount;
    }

    /** Mengubah path bukti transaksi. */
    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}
