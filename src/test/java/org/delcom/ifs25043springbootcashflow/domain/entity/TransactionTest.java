package org.delcom.ifs25043springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionTest {

    @Test
    void constructorWithoutProofUsesCurrentTime() {
        Transaction transaction = new Transaction(1, 10, "Gaji", 5000000, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals(10, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5000000, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNull(transaction.getProofPath());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void constructorWithProof() {
        Transaction transaction = new Transaction(
                2,
                10,
                "Makan",
                25000,
                TransactionType.EXPENSE,
                "proofs/a.png"
        );

        assertEquals(2, transaction.getId());
        assertEquals(10, transaction.getUserId());
        assertEquals("Makan", transaction.getDescription());
        assertEquals(25000, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("proofs/a.png", transaction.getProofPath());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void fullConstructorKeepsGivenTimestamp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 1, 8, 30);
        Transaction transaction = new Transaction(
                3,
                7,
                "Listrik",
                150000,
                TransactionType.EXPENSE,
                null,
                createdAt
        );

        assertEquals(3, transaction.getId());
        assertEquals(7, transaction.getUserId());
        assertEquals("Listrik", transaction.getDescription());
        assertEquals(150000, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertNull(transaction.getProofPath());
        assertEquals(createdAt, transaction.getCreatedAt());
    }

    @Test
    void mutatorsChangeDescriptionAmountAndProof() {
        Transaction transaction = new Transaction(4, 1, "Lama", 1000, TransactionType.INCOME, "a.png");

        transaction.changeDescription("Baru");
        transaction.changeAmount(2000);
        transaction.changeProofPath("proofs/b.png");

        assertEquals("Baru", transaction.getDescription());
        assertEquals(2000, transaction.getAmount());
        assertEquals("proofs/b.png", transaction.getProofPath());

        transaction.changeProofPath(null);
        assertNull(transaction.getProofPath());
    }
}
