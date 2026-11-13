package org.delcom.ifs25043springbootcashflow.adapter.repository;

import org.delcom.ifs25043springbootcashflow.domain.entity.Role;
import org.delcom.ifs25043springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void saveFindUpdateAndDelete() {
        User user = repository.saveNew("andi", "hash", "Andi", Role.USER);
        assertEquals(1, user.getId());
        assertTrue(repository.findByUsername("ANDI").isPresent());

        user.changeFullName("Andi Baru");
        repository.update(user);
        assertEquals("Andi Baru", repository.findById(1).orElseThrow().getFullName());

        assertTrue(repository.deleteById(1));
        assertTrue(repository.findByUsername("andi").isEmpty());
    }

    @Test
    void findByPredicateAndMissingLookups() {
        repository.saveNew("andi", "hash", "Andi", Role.USER);
        repository.saveNew("admin", "hash", "Admin", Role.ADMIN);

        assertEquals(1, repository.findBy(User::isAdmin).size());
        assertTrue(repository.findByUsername("tidakada").isEmpty());
        assertTrue(repository.findById(999).isEmpty());
        assertFalse(repository.deleteById(999));
    }

    @Test
    void saveEntityStoresDirectly() {
        User saved = repository.save(new User(7, "budi", "hash", "Budi", Role.USER, null));
        assertEquals(7, saved.getId());
        assertTrue(repository.findById(7).isPresent());
    }
}
