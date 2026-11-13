package org.delcom.ifs25043springbootcashflow.adapter.repository;

import org.delcom.ifs25043springbootcashflow.domain.entity.Role;
import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25043springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_repo_" + System.nanoTime()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void userCrudAndFinders() {
        User saved = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(saved.getId() > 0);

        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.findById(saved.getId()).isPresent());
        assertEquals(1, userRepository.findBy(user -> user.getUsername().equals("andi")).size());

        saved.changeFullName("Andi Baru");
        saved.changePhotoPath("profiles/a.png");
        userRepository.update(saved);
        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Andi Baru", updated.getFullName());
        assertEquals("profiles/a.png", updated.getPhotoPath());

        User viaSave = userRepository.save(new User(0, "budi", "h", "Budi", Role.USER, null));
        assertTrue(viaSave.getId() > 0);

        assertTrue(userRepository.deleteById(viaSave.getId()));
        assertFalse(userRepository.deleteById(viaSave.getId()));
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());
        assertTrue(userRepository.findById(999).isEmpty());
    }

    @Test
    void transactionCrudAndFinders() {
        User owner = userRepository.saveNew("owner", "hash", "Owner", Role.USER);

        Transaction income = transactionRepository.saveIncome(owner.getId(), "Gaji", 5000000, null);
        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertNull(income.getProofPath());

        Transaction expense = transactionRepository.saveExpense(
                owner.getId(), "Makan", 25000, "proofs/a.png");
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals("proofs/a.png", expense.getProofPath());

        Transaction viaEntity = transactionRepository.save(new Transaction(
                0,
                owner.getId(),
                null,
                150000,
                TransactionType.EXPENSE,
                null,
                LocalDateTime.of(2026, 11, 1, 8, 0)
        ));
        assertTrue(viaEntity.getId() > 0);

        assertEquals(3, transactionRepository.findAll().size());
        assertEquals(3, transactionRepository.findByUserId(owner.getId()).size());
        assertTrue(transactionRepository.findByUserId(999).isEmpty());
        assertTrue(transactionRepository.findById(income.getId()).isPresent());
        assertTrue(transactionRepository.findById(999).isEmpty());
        assertEquals(1, transactionRepository.findBy(
                transaction -> "Gaji".equals(transaction.getDescription())).size());

        Transaction stored = transactionRepository.findById(viaEntity.getId()).orElseThrow();
        assertEquals("", stored.getDescription());
        assertEquals(LocalDateTime.of(2026, 11, 1, 8, 0), stored.getCreatedAt());

        income.changeDescription("Gaji Bulanan");
        income.changeAmount(6000000);
        income.changeProofPath("proofs/b.png");
        transactionRepository.update(income);
        Transaction updated = transactionRepository.findById(income.getId()).orElseThrow();
        assertEquals("Gaji Bulanan", updated.getDescription());
        assertEquals(6000000, updated.getAmount());
        assertEquals("proofs/b.png", updated.getProofPath());

        assertTrue(transactionRepository.deleteById(income.getId()));
        assertFalse(transactionRepository.deleteById(income.getId()));
        List<Transaction> remaining = transactionRepository.findByUserId(owner.getId());
        assertEquals(2, remaining.size());
    }
}
