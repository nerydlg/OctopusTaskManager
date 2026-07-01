package dev.nerydlg.taskmanager.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class FileManagerServiceTest {

    public FileManagerService subject;

    @Test
    void whenCallingGetLatestOpenFile_ShouldReturnAPath(){
        // GIVEN
        subject = new FileManagerService();
        String expectedPath = subject.getDefaultStoragePath();
        // WHEN
        String actual = null;
        try {
            actual = subject.getLatestStorageUsed();
        } catch (IOException e) {
            fail("Failed to get latest storage used " + e.getMessage());
        }
        // THEN
        assertEquals(expectedPath, actual);
    }

    @Test
    void whenCallingWriteMultipleTimes_ShouldKeepASingleLine(){
        // GIVEN
        subject = new FileManagerService();
        String actual = null;
        String expectedString = subject.getDefaultStoragePath();
        // WHEN
        try {
            for(int i = 0; i < 10; i++) {
                subject.writeToLatestFile(expectedString);
            }
            actual = subject.readFromLatestFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // THEN
        assertEquals(expectedString, actual);
    }

}