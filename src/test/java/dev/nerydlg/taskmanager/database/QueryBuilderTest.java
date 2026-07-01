package dev.nerydlg.taskmanager.database;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {


    @Test
    void whenCreateQuerySelectAll_ThenReturnAQuery() {
        // GIVEN
        String expected = "SELECT * FROM test;";
        Query query = QueryBuilder.create()
                .selectAll()
                .from("test")
                .build();
        // WHEN
        String actual = query.getQuery();
        // THEN
        assertEquals(expected, actual);
    }

    @Test
    void whenCreateQuerySelectAllWithWhere_ThenReturnAValidQuery(){
        // GIVEN
        String expected = "SELECT * FROM test WHERE id = ?;";
        // WHEN
        Query query = QueryBuilder.create()
                .selectAll()
                .from("test")
                .where("id", Operator.EQUALS, FieldType.INTEGER, 1)
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }

    @Test
    void whenCreateQueryWithSelectFields_ThenReturnAValidQuery() {
        // GIVEN
        Fields field1 = new Fields("id", FieldType.INTEGER);
        Fields field2 = new Fields("name", FieldType.TEXT);
        Fields field3 = new Fields("other", FieldType.DATE);
        String expected = "SELECT id, name, other FROM test WHERE id = ?;";
        // WHEN
        Query query = QueryBuilder.create()
                .select("id", "name", "other")
                .from("test")
                .where("id", Operator.EQUALS, FieldType.INTEGER, 1)
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }

    @Test
    void whenCreateInsertQueryWithFields_ThenReturnValidQuery() {
        // GIVEN
        String expected = "INSERT INTO test (id, name, other) VALUES (?, ?, ?);";
        // WHEN
        Query query = QueryBuilder.create()
                .insert()
                .table("test")
                .into("id", "name", "other")
                .values(List.of(1, "2026-01-01", "value"),
                        List.of(FieldType.INTEGER, FieldType.DATE, FieldType.TEXT))
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }


    @Test
    void whenCreateDeleteQueryWithFields_ThenReturnValidQuery() {
        // GIVEN
        String expected = "DELETE FROM test WHERE id = ?;";
        // WHEN
        Query query = QueryBuilder.create()
                .delete()
                .from("test")
                .where("id", Operator.EQUALS, FieldType.INTEGER, 1)
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }

    @Test
    void whenCreateQueryUpdate_ThenReturnValidQuery() {
        // GIVEN
        String expected = "UPDATE test SET name = ?, other = ? WHERE id = ?;";
        // WHEN
        Query query = QueryBuilder.create()
                .update()
                .table("test")
                .set("name", "other")
                .where("id", Operator.EQUALS, FieldType.INTEGER, 1)
                .setValues(List.of("name", "2026-01-01"), List.of(FieldType.TEXT, FieldType.DATE))
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }

    @Test
    void whenCreateQueryWithMultipleConditions_ThenReturnValidQuery() {
        // GIVEN
        Fields field1 = new Fields("id", FieldType.INTEGER);
        Fields field2 = new Fields("name", FieldType.TEXT);
        Fields field3 = new Fields("other", FieldType.DATE);
        Condition condition1 = new Condition(field1, Operator.EQUALS, 1);
        Condition condition2 = new Condition(field2, Operator.EQUALS, 1);
        Condition condition3 = new Condition(field3, Operator.EQUALS, 1);
        String expected = "SELECT * FROM test WHERE id = ? AND name = ? OR other = ?;";
        // WHEN
        Query query = QueryBuilder.create()
                .selectAll()
                .from("test")
                .where("id", Operator.EQUALS, FieldType.INTEGER, 1)
                .and("name", Operator.EQUALS, FieldType.TEXT, "otherName")
                .or("other", Operator.EQUALS, FieldType.DATE, "2026-01-01")
                .build();
        // THEN
        assertEquals(expected, query.getQuery());
    }
}