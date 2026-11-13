package org.delcom.ifs25043springbootcashflow.usecase;

import org.delcom.ifs25043springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25043springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25043springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25043springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER_A = 1;
    private static final int USER_B = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void addTransactionsAndCalculateTotals() throws Exception {
        Transaction income = useCase.addIncome(USER_A, "Gaji", 5000000, null);
        Transaction expense = useCase.addExpense(USER_A, "Makan", 25000, " proofs/a.png ");
        useCase.addIncome(USER_B, "Bonus", 100, null);

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertNull(income.getProofPath());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals("proofs/a.png", expense.getProofPath());

        assertEquals(2, useCase.getTransactionsByUser(USER_A).size());
        assertTrue(useCase.getTransactionsByUser(USER_B).size() == 1);
        assertEquals(3, useCase.getAllTransactions().size());
        assertEquals(5000000, useCase.getTotalIncome(USER_A));
        assertEquals(25000, useCase.getTotalExpense(USER_A));
        assertEquals(4975000, useCase.getBalance(USER_A));
        assertEquals(2, useCase.getTransactionCount(USER_A));
        assertEquals(100, useCase.getTotalIncome(USER_B));
        assertEquals(0, useCase.getTotalExpense(USER_B));
    }

    @Test
    void addValidatesDescriptionAmountAndType() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER_A, null, 1000, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER_A, " ", 1000, null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER_A, "Makan", 0, null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER_A, "Makan", -5, null));
        assertThrows(ValidationException.class, () ->
                useCase.addTransaction(USER_A, "Tanpa tipe", 1000, null, null));
    }

    @Test
    void addNormalizesBlankProofToNull() throws Exception {
        Transaction transaction = useCase.addIncome(USER_A, "Gaji", 1000, "   ");
        assertNull(transaction.getProofPath());
    }

    @Test
    void updateTransactionFieldsAndProof() throws Exception {
        Transaction transaction = useCase.addIncome(USER_A, "Lama", 1000, "proofs/a.png");

        useCase.updateTransactionForUser(USER_A, transaction.getId(), "Baru", 2000L, "proofs/b.png", false);
        Transaction updated = useCase.getTransactionsByUser(USER_A).get(0);
        assertEquals("Baru", updated.getDescription());
        assertEquals(2000, updated.getAmount());
        assertEquals("proofs/b.png", updated.getProofPath());

        useCase.updateTransactionForUser(USER_A, transaction.getId(), null, null, "  ", false);
        assertNull(useCase.getTransactionsByUser(USER_A).get(0).getProofPath());

        useCase.updateTransactionForUser(USER_A, transaction.getId(), null, null, "proofs/c.png", true);
        assertNull(useCase.getTransactionsByUser(USER_A).get(0).getProofPath());

        useCase.updateTransactionForUser(USER_A, transaction.getId(), null, null, null, false);
        assertEquals("Baru", useCase.getTransactionsByUser(USER_A).get(0).getDescription());
    }

    @Test
    void updateValidatesInputAndOwnership() throws Exception {
        Transaction transaction = useCase.addIncome(USER_A, "Gaji", 1000, null);

        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER_A, transaction.getId(), " ", null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER_A, transaction.getId(), null, 0L, null, false));
        assertThrows(EntityNotFoundException.class, () ->
                useCase.updateTransactionForUser(USER_A, 999, "X", null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER_B, transaction.getId(), "X", null, null, false));
    }

    @Test
    void removeTransactionScopedPerUser() throws Exception {
        Transaction first = useCase.addIncome(USER_A, "Satu", 1000, null);
        Transaction second = useCase.addExpense(USER_A, "Dua", 500, null);

        useCase.removeTransactionForUser(USER_A, first.getId());
        assertEquals(1, useCase.getTransactionsByUser(USER_A).size());

        useCase.removeTransaction(second.getId());
        assertTrue(useCase.getTransactionsByUser(USER_A).isEmpty());

        EntityNotFoundException missing = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.removeTransaction(999)
        );
        assertEquals(999, missing.getId());

        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER_A, 999));

        Transaction other = useCase.addIncome(USER_A, "Milik A", 1000, null);
        assertThrows(ValidationException.class, () ->
                useCase.removeTransactionForUser(USER_B, other.getId()));
    }

    @Test
    void searchIsCaseInsensitiveAndScopedPerUser() throws Exception {
        useCase.addIncome(USER_A, "Belanja Bulanan", 1000, null);
        useCase.addExpense(USER_A, "Bayar Listrik", 500, null);
        useCase.addIncome(USER_B, "Belanja Lain", 700, null);

        List<Transaction> results = useCase.searchTransactions(USER_A, "BELANJA");
        assertEquals(1, results.size());
        assertEquals("Belanja Bulanan", results.get(0).getDescription());

        assertTrue(useCase.searchTransactions(USER_A, "tidakada").isEmpty());

        repository.save(new Transaction(99, USER_A, null, 1000, TransactionType.INCOME));
        List<Transaction> all = useCase.searchTransactions(USER_A, null);
        assertEquals(2, all.size());
    }

    @Test
    void sortTransactionsUsesSelectedComparator() throws Exception {
        useCase.addIncome(USER_A, "Kecil", 1000, null);
        useCase.addIncome(USER_A, "Besar", 9000, null);

        List<Transaction> sorted = useCase.sortTransactions(USER_A, SortOption.AMOUNT_LARGEST_FIRST);
        assertEquals("Besar", sorted.get(0).getDescription());
        assertEquals("Kecil", sorted.get(1).getDescription());
    }

    @Test
    void parseAmountAcceptsSeparatorsAndRejectsInvalid() throws Exception {
        assertEquals(1000000, useCase.parseAmount("1.000.000"));
        assertEquals(1000000, useCase.parseAmount("1,000,000"));
        assertEquals(25000, useCase.parseAmount("  25000 "));

        assertThrows(ValidationException.class, () -> useCase.parseAmount(null));
        assertThrows(ValidationException.class, () -> useCase.parseAmount(""));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("   "));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("abc"));
    }

    @Test
    void totalsIgnoreTransactionsFromOtherUsers() throws Exception {
        useCase.addIncome(USER_A, "A", 1000, null);
        useCase.addExpense(USER_B, "B", 2000, null);

        assertEquals(1000, useCase.getTotalIncome(USER_A));
        assertEquals(0, useCase.getTotalExpense(USER_A));
        assertEquals(1000, useCase.getBalance(USER_A));
    }

    @Test
    void fullConstructorTransactionIsReadable() {
        Transaction transaction = new Transaction(
                1, USER_A, "Lengkap", 123, TransactionType.INCOME, null, LocalDateTime.now());
        assertEquals("Lengkap", transaction.getDescription());
    }
}
