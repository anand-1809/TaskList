package com.example.tasklist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    void insert(TodoEntity todoEntity);

    @Query("SELECT * FROM todo_table;")
    List<TodoEntity> getAll();

    @Query("UPDATE todo_table SET status = :status,  task = :task WHERE id = :id;")
    void edit(long id, long status, String task);

    @Delete
    void delete(TodoEntity todoEntity);
}
