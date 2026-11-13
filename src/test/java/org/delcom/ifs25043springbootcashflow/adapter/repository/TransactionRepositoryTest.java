package org.delcom.ifs25043springbootcashflow.adapter.repository;

import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAndExpenseAssignIncrementalIds() {
        Transaction income = repository.saveIncome(1, "Satu", 1000, null);
        Transaction expense = repository.saveExpense(1, "Dua", 2000, "proofs/a.png");

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, repository.findAll().size());
        assertEquals(2, repository.findByUserId(1).size());
        assertTrue(repository.findByUserId(2).isEmpty());
    }

    @Test
    void findByIdAndDelete() {
        Transaction transaction = repository.saveIncome(1, "Hapus saya", 1000, null);

        Optional<Transaction> found = repository.findById(transaction.getId());
        assertTrue(found.isPresent());
        assertEquals("Hapus saya", found.get().getDescription());

        assertTrue(repository.deleteById(transaction.getId()));
        assertFalse(repository.deleteById(transaction.getId()));
        assertTrue(repository.findById(transaction.getId()).isEmpty());
    }

    @Test
    void findByPredicateAndUpdateNoOp() {
        repository.saveIncome(1, "Belajar Java", 1000, null);
        repository.saveExpense(1, "Belajar Spring", 2000, null);

        List<Transaction> expenses = repository.findBy(
                transaction -> transaction.getType() == TransactionType.EXPENSE);
        assertEquals(1, expenses.size());

        Transaction transaction = expenses.get(0);
        transaction.changeDescription("Belajar Spring Boot");
        repository.update(transaction);

        assertEquals(
                "Belajar Spring Boot",
                repository.findById(transaction.getId()).orElseThrow().getDescription()
        );
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome(1, "A", 1000, null);
        List<Transaction> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveEntityDirectly() {
        Transaction entity = new Transaction(99, 1, "Manual", 1000, TransactionType.INCOME);
        repository.save(entity);
        assertTrue(repository.findById(99).isPresent());
    }
}
