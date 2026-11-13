package org.delcom.ifs25043springbootcashflow.adapter.repository;

import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25043springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi repository transaksi menggunakan basis generic {@link InMemoryRepository}.
 * Dipakai pada unit test; produksi memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    /** Penghitung ID otomatis, bertambah setiap kali transaksi baru disimpan. */
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction saveIncome(int userId, String description, long amount, String proofPath) {
        return save(new Transaction(nextId(), userId, description, amount, TransactionType.INCOME, proofPath));
    }

    @Override
    public Transaction saveExpense(int userId, String description, long amount, String proofPath) {
        return save(new Transaction(nextId(), userId, description, amount, TransactionType.EXPENSE, proofPath));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }

    private int nextId() {
        idCounter++;
        return idCounter;
    }
}
