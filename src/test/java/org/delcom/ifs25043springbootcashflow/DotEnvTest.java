package org.delcom.ifs25043springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {

    private static final String KEY = "APP_PORT_TEST_CASHFLOW";

    @AfterEach
    void clearProperty() {
        System.clearProperty(KEY);
        System.clearProperty("APP_PORT_QUOTED_CASHFLOW");
        System.clearProperty("APP_PORT_SINGLE_CASHFLOW");
        System.clearProperty("MISMATCH_Q_CASHFLOW");
        System.clearProperty("MISMATCH_S_CASHFLOW");
        System.clearProperty("SHORT_Q_CASHFLOW");
    }

    @Test
    void loadReadsEnvFile(@TempDir Path tempDir) throws Exception {
        Path env = tempDir.resolve(".env");
        Files.writeString(env, """
                # komentar
                %s=9090

                APP_PORT_QUOTED_CASHFLOW="7070"
                APP_PORT_SINGLE_CASHFLOW='6060'
                =invalid
                NOEQUALS
                """.formatted(KEY));

        DotEnv.load(env);

        assertEquals("9090", System.getProperty(KEY));
        assertEquals("7070", System.getProperty("APP_PORT_QUOTED_CASHFLOW"));
        assertEquals("6060", System.getProperty("APP_PORT_SINGLE_CASHFLOW"));
    }

    @Test
    void loadIgnoresMissingFileAndExistingProperty(@TempDir Path tempDir) throws Exception {
        DotEnv.load(tempDir.resolve("tidak-ada.env"));

        System.setProperty(KEY, "1111");
        Path env = tempDir.resolve(".env");
        Files.writeString(env, KEY + "=2222\n");
        DotEnv.load(env);
        assertEquals("1111", System.getProperty(KEY));
    }

    @Test
    void loadDefaultLooksForDotEnvInWorkingDirectory() throws Exception {
        DotEnv.load();
    }

    @Test
    void applyLineSkipsBlankCommentAndBadKeys() {
        DotEnv.applyLine("   ");
        DotEnv.applyLine("# only comment");
        DotEnv.applyLine("=novalue");
        DotEnv.applyLine(" =novalue");
        DotEnv.applyLine("PLAIN");
        DotEnv.applyLine("MISMATCH_Q_CASHFLOW=\"abc'");
        DotEnv.applyLine("MISMATCH_S_CASHFLOW='abc\"");
        DotEnv.applyLine("SHORT_Q_CASHFLOW=\"");
        assertNull(System.getProperty("PLAIN"));
        assertEquals("\"abc'", System.getProperty("MISMATCH_Q_CASHFLOW"));
        assertEquals("'abc\"", System.getProperty("MISMATCH_S_CASHFLOW"));
        assertEquals("\"", System.getProperty("SHORT_Q_CASHFLOW"));
        System.clearProperty("MISMATCH_Q_CASHFLOW");
        System.clearProperty("MISMATCH_S_CASHFLOW");
        System.clearProperty("SHORT_Q_CASHFLOW");
    }

    @Test
    void applyLineDoesNotOverrideExistingEnvironmentVariable() {
        String existingKey = System.getenv().keySet().iterator().next();
        String before = System.getProperty(existingKey);
        DotEnv.applyLine(existingKey + "=should-not-win");
        assertEquals(before, System.getProperty(existingKey));
        if (before != null) {
            System.setProperty(existingKey, before);
        } else {
            System.clearProperty(existingKey);
        }
    }

    @Test
    void privateConstructorCovered() throws Exception {
        Constructor<DotEnv> constructor = DotEnv.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
