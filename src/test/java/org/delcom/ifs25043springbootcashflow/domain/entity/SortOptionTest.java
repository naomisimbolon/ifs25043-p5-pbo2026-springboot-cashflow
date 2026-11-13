package org.delcom.ifs25043springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    private List<Transaction> transactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                1, 1, "Gaji", 5000000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 1, 8, 0)));
        transactions.add(new Transaction(
                2, 1, "Makan", 25000, TransactionType.EXPENSE, null,
                LocalDateTime.of(2026, 11, 2, 9, 0)));
        transactions.add(new Transaction(
                3, 1, "Bonus", 1000000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 3, 10, 0)));
        transactions.add(new Transaction(
                4, 1, "Listrik", 150000, TransactionType.EXPENSE, null,
                LocalDateTime.of(2026, 11, 4, 11, 0)));
        return transactions;
    }

    @Test
    void hasMinimumFourOptionsWithLabels() {
        assertTrue(SortOption.values().length >= 4);
        assertEquals("Jumlah terkecil", SortOption.AMOUNT_SMALLEST_FIRST.getLabel());
        assertEquals("Jumlah terkecil", SortOption.AMOUNT_SMALLEST_FIRST.toString());
    }

    @Test
    void sortsByAmount() {
        List<Transaction> ascending = transactions();
        ascending.sort(SortOption.AMOUNT_SMALLEST_FIRST.comparator());
        assertEquals(25000, ascending.get(0).getAmount());
        assertEquals(5000000, ascending.get(3).getAmount());

        List<Transaction> descending = transactions();
        descending.sort(SortOption.AMOUNT_LARGEST_FIRST.comparator());
        assertEquals(5000000, descending.get(0).getAmount());
        assertEquals(25000, descending.get(3).getAmount());
    }

    @Test
    void sortsByType() {
        List<Transaction> incomeFirst = transactions();
        incomeFirst.sort(SortOption.INCOME_FIRST.comparator());
        assertEquals(TransactionType.INCOME, incomeFirst.get(0).getType());
        assertEquals(TransactionType.EXPENSE, incomeFirst.get(3).getType());

        List<Transaction> expenseFirst = transactions();
        expenseFirst.sort(SortOption.EXPENSE_FIRST.comparator());
        assertEquals(TransactionType.EXPENSE, expenseFirst.get(0).getType());
        assertEquals(TransactionType.INCOME, expenseFirst.get(3).getType());
    }

    @Test
    void sortsByCreatedAt() {
        List<Transaction> newest = transactions();
        newest.sort(SortOption.NEWEST_FIRST.comparator());
        assertEquals(4, newest.get(0).getId());
        assertEquals(1, newest.get(3).getId());

        List<Transaction> oldest = transactions();
        oldest.sort(SortOption.OLDEST_FIRST.comparator());
        assertEquals(1, oldest.get(0).getId());
        assertEquals(4, oldest.get(3).getId());
    }
}
