package org.delcom.ifs25043springbootcashflow.usecase;

import org.delcom.ifs25043springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25043springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25043springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25043springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi yang tersedia (admin). */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan baru setelah validasi. */
    public Transaction addIncome(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.INCOME, proofPath);
    }

    /** Menambahkan transaksi pengeluaran baru setelah validasi. */
    public Transaction addExpense(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.EXPENSE, proofPath);
    }

    /** Menambahkan transaksi baru setelah validasi keterangan, jumlah, dan tipe. */
    public Transaction addTransaction(
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath
    ) throws ValidationException {
        validateDescription(description);
        validateAmount(amount);
        if (type == null) {
            throw new ValidationException("Tipe transaksi tidak boleh kosong");
        }
        String safeProofPath = proofPath == null || proofPath.isBlank() ? null : proofPath.trim();
        if (type == TransactionType.INCOME) {
            return transactionRepository.saveIncome(userId, description.trim(), amount, safeProofPath);
        }
        return transactionRepository.saveExpense(userId, description.trim(), amount, safeProofPath);
    }

    /**
     * Mengubah keterangan, jumlah, dan/atau bukti transaksi milik pengguna.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            Long amount,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        if (description != null) {
            validateDescription(description);
            transaction.changeDescription(description.trim());
        }
        if (amount != null) {
            validateAmount(amount);
            transaction.changeAmount(amount);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath.isBlank() ? null : proofPath.trim());
        }
        transactionRepository.update(transaction);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getDescription() != null
                        && transaction.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total pemasukan pengguna. */
    public long getTotalIncome(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Total pengeluaran pengguna. */
    public long getTotalExpense(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Saldo pengguna: total pemasukan dikurangi total pengeluaran. */
    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    /** Jumlah transaksi pengguna. */
    public int getTransactionCount(int userId) {
        return transactionRepository.findByUserId(userId).size();
    }

    /**
     * Mengubah teks jumlah menjadi angka.
     * Pemisah ribuan titik/koma diabaikan agar input "1.000.000" tetap valid.
     */
    public long parseAmount(String rawAmount) throws ValidationException {
        if (rawAmount == null || rawAmount.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        String normalized = rawAmount.trim().replace(".", "").replace(",", "");
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private void validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
    }

    private void validateAmount(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
    }
}
