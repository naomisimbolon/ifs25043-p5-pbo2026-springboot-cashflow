package org.delcom.ifs25043springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void mutatorsAndAdminFlag() {
        User user = new User(1, "andi", "hash", "Andi", Role.USER, null);
        assertEquals(1, user.getId());
        assertEquals("andi", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Andi", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isAdmin());

        user.changeFullName("Andi Baru");
        user.changePasswordHash("hash2");
        user.changePhotoPath("profiles/a.png");

        assertEquals("Andi Baru", user.getFullName());
        assertEquals("hash2", user.getPasswordHash());
        assertEquals("profiles/a.png", user.getPhotoPath());

        User admin = new User(2, "admin", "h", "Admin", Role.ADMIN, null);
        assertTrue(admin.isAdmin());
    }
}
