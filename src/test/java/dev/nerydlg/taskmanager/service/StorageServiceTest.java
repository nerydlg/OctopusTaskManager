package dev.nerydlg.taskmanager.service;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    private StorageService subject;

    @Test
    void whenOpen_ThenCreateDatabaseFile() {
        // GIVEN
        subject = StorageService.getInstance();

        String storagePath = "test.db";
        File expectedFile = new File(storagePath);
        // WHEN
        subject.open(storagePath);
        subject.close();
        // THEN
        assertTrue(expectedFile.exists());
    }

    @Test
    void whenGetInstance_ThenReturnSameInstance() {
        // GIVEN
        subject = StorageService.getInstance();
        // WHEN
        subject.open("test.db");
        assertNotNull(subject);
        // THEN
        assertEquals(subject, StorageService.getInstance());
    }

    @Test
    void whenOpen_ThenCreateTables() {
        // GIVEN
        subject = StorageService.getInstance();
        String storagePath = "test.db";
        int numOfTables = 2;
        int actualNumOfTables = 0;
        // WHEN
        subject.open(storagePath);
        try {
            Statement result = subject.executeStatement("SELECT count(name) as 'count' FROM sqlite_master WHERE type = 'table' AND name LIKE 'tm_%';");
            actualNumOfTables = result.getResultSet().getInt("count");
            result.close();
        }catch (SQLException ex) {
            fail("Failed to execute statement");
        }
        subject.close();
        // THEN
        assertEquals(numOfTables, actualNumOfTables);
    }
}