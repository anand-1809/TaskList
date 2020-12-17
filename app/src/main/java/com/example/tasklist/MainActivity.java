package com.example.tasklist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.tasklist.Adapter.ToDoAdapter;
import com.example.tasklist.database.TodoDao;
import com.example.tasklist.database.TodoDatabase;
import com.example.tasklist.database.TodoEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView taskRecycleView;
    private ToDoAdapter taskAdapter;
    private List<TodoEntity> todoEntityList;
    private TodoDao todoDao;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        TodoDatabase todoDatabase = Room.databaseBuilder(MainActivity.this, TodoDatabase.class, "TODO").build();
        todoDao = todoDatabase.todoDao();

        todoEntityList = new ArrayList<>();
        taskRecycleView = findViewById(R.id.taskRecycleView);
        taskRecycleView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new ToDoAdapter(MainActivity.this, todoEntityList);
        taskRecycleView.setAdapter(taskAdapter);

        fab = findViewById(R.id.fab);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(taskRecycleView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTodoList();
    }

    private void getTodoList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                todoEntityList = todoDao.getAll();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        taskAdapter.setTask(todoEntityList);
                    }
                });
            }
        }).start();
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        getTodoList();
    }
}