package com.example.tasklist.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.tasklist.AddNewTask;
import com.example.tasklist.MainActivity;
import com.example.tasklist.R;
import com.example.tasklist.database.TodoDao;
import com.example.tasklist.database.TodoDatabase;
import com.example.tasklist.database.TodoEntity;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private final MainActivity activity;
    private final TodoDao todoDao;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<TodoEntity> todoEntityList;

    public ToDoAdapter(MainActivity activity, List<TodoEntity> todoEntityList) {
        this.activity = activity;
        this.todoEntityList = todoEntityList;
        TodoDatabase todoDatabase = Room.databaseBuilder(activity, TodoDatabase.class, "TODO").build();
        todoDao = todoDatabase.todoDao();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        final TodoEntity todoEntity = todoEntityList.get(position);
        holder.task.setText(todoEntity.task);
        holder.task.setChecked(todoEntity.status == 1);
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    todoEntity.status = 1;
                } else {
                    todoEntity.status = 0;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        todoDao.edit(todoEntity.id, todoEntity.status, todoEntity.task);
                    }
                }).start();
            }
        });
    }

    public int getItemCount() {
        return todoEntityList.size();
    }

    public void setTask(List<TodoEntity> todoEntityList) {
        this.todoEntityList = todoEntityList;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return activity;
    }

    public void deleteItem(int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodoEntity todoEntity = todoEntityList.get(position);
                todoDao.delete(todoEntity);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        todoEntityList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
            }
        }).start();
    }

    public void editItem(int position) {
        TodoEntity todoEntity = todoEntityList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", (int) todoEntity.id);
        bundle.putString("task", todoEntity.task);
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
        }
    }
}
