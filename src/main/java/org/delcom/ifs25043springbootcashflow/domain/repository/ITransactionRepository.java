package org.delcom.ifs25043springbootcashflow.domain.repository;

import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;

import java.util.List;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi pemasukan/pengeluaran dengan ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /**
     * Menyimpan transaksi pemasukan baru.
     *
     * @param userId      pemilik transaksi
     * @param description keterangan transaksi
     * @param amount      nominal transaksi (positif)
     * @param proofPath   path bukti opsional
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveIncome(int userId, String description, long amount, String proofPath);

    /**
     * Menyimpan transaksi pengeluaran baru.
     *
     * @param userId      pemilik transaksi
     * @param description keterangan transaksi
     * @param amount      nominal transaksi (positif)
     * @param proofPath   path bukti opsional
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveExpense(int userId, String description, long amount, String proofPath);

    /** Mengambil semua transaksi milik satu pengguna. */
    List<Transaction> findByUserId(int userId);
}
