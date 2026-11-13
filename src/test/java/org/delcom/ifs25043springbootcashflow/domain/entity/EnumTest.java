package org.delcom.ifs25043springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnumTest {

    @Test
    void roleHasAdminAndUser() {
        assertEquals(2, Role.values().length);
        assertNotNull(Role.valueOf("ADMIN"));
        assertNotNull(Role.valueOf("USER"));
    }

    @Test
    void transactionTypeHasIndonesianLabels() {
        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.getLabel());
        assertEquals(2, TransactionType.values().length);
    }
}
