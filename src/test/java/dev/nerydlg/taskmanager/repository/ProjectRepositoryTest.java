package dev.nerydlg.taskmanager.repository;

import dev.nerydlg.taskmanager.entity.Project;
import dev.nerydlg.taskmanager.service.StorageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRepositoryTest {

    private ProjectRepository subject;

    @BeforeAll
    static void beforeAll() {
        File testDb = new File("test.db");
        testDb.delete();
    }

    @Test
    void whenTrySave_ThenANewRowHasToBeCreated() throws SQLException {
        // GIVEN
        StorageService storageService = StorageService.getInstance();
        storageService.open("test.db");
        subject = new ProjectRepository();
        String projectName = "Test Project";
        Project project = new Project(projectName);
        // WHEN
        assertDoesNotThrow(() -> subject.save(project));

        // THEN
        Project saved = subject.findWithFilters(project);
        assertNotNull(saved.id());
        assertEquals(project.name(), saved.name());
        assertEquals(project.status(), saved.status());
        assertEquals(project.createdAt().truncatedTo(ChronoUnit.SECONDS), saved.createdAt().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(project.updatedAt().truncatedTo(ChronoUnit.SECONDS), saved.updatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void whenTryUpdate_ThenShouldReturnTheSameProject() throws SQLException {
        // GIVEN
        StorageService storageService = StorageService.getInstance();
        storageService.open("test.db");
        subject = new ProjectRepository();
        String projectName = "Test Project 2";
        String newName = "New Name";
        Project project = new Project(projectName);
        project = subject.save(project);
        // WHEN
        Project updatedProject = new Project(project.id(), newName, 1,project.createdAt(), LocalDateTime.now());
        assertDoesNotThrow(()->subject.update(updatedProject));
        // THEN
        Project updated = subject.findWithFilters(updatedProject);
        assertNotNull(updated.id());
        assertEquals(updatedProject.id(), updated.id());
        assertEquals(updatedProject.name(), updated.name());
        assertEquals(updatedProject.status(), updated.status());
        assertEquals(updatedProject.updatedAt().truncatedTo(ChronoUnit.SECONDS), updated.updatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void whenSearchForAll_ThenShouldReturnResults() throws SQLException {
        // GIVEN
        StorageService storageService = StorageService.getInstance();
        storageService.open("test.db");
        subject = new ProjectRepository();
        String projectName = "Test Project 3";
        Project project = new Project(projectName);
        subject.save(project);
        // WHEN
        List<Project> projects = subject.findAll();
        int count = subject.countProjects();
        // THEN
        assertNotNull(projects);
        assertEquals(count, projects.size());
    }
}