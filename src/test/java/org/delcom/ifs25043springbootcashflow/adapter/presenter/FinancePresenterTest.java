package org.delcom.ifs25043springbootcashflow.adapter.presenter;

import org.delcom.ifs25043springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25043springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private FinancePresenter presenter;
    private Transaction income;

    @BeforeEach
    void setUp() {
        presenter = new FinancePresenter();
        income = new Transaction(
                1, 1, "Gaji", 1000000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 1, 8, 0));
    }

    @Test
    void formatMoneyUsesCommaThousandsAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("-Rp 1,500.00", presenter.formatMoney(-1500));
    }

    @Test
    void formatTypeUsesIndonesianLabels() {
        assertEquals("[Pemasukan]", presenter.formatType(TransactionType.INCOME));
        assertEquals("[Pengeluaran]", presenter.formatType(TransactionType.EXPENSE));
        assertEquals("-", presenter.formatType(null));
    }

    @Test
    void formatIncludesIdMoneyAndType() {
        assertEquals(
                "[1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.format(income)
        );
    }

    @Test
    void formatDateTimeAndBalance() {
        assertEquals("01/11/2026 08:00", presenter.formatDateTime(income));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4975000));
    }

    @Test
    void formatListHandlesEmptyAndFilled() {
        assertEquals("Header:\n- kosong", presenter.formatList(List.of(), "Header:", "- kosong"));
        assertTrue(presenter.formatList(List.of(income), "Header:", "- kosong")
                .contains("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]"));
    }

    @Test
    void showMessages() {
        assertTrue(presenter.showTransactions(List.of()).contains("belum tersedia"));
        assertTrue(presenter.showTransactions(List.of(income)).contains("Gaji"));
        assertTrue(presenter.showSearchResults(List.of(), "x").contains("tidak ditemukan"));
        assertTrue(presenter.showSearchResults(List.of(income), "Gaji").contains("Hasil Pencarian"));
        assertTrue(presenter.showSortedTransactions(List.of(income)).contains("Terurut"));
        assertTrue(presenter.showAddSuccess(income).contains("Berhasil menambah"));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 8.", presenter.showUpdateFailed(8));
        assertEquals("Berhasil menghapus transaksi.", presenter.showDeleteSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showDeleteFailed(9));
        assertEquals("[!] Gaji kosong", presenter.showValidationError("Gaji kosong"));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Pilihan pengurutan tidak valid!", presenter.showInvalidSortOption());
    }
}
