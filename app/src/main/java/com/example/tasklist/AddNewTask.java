package com.example.tasklist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.example.tasklist.Model.ToDoModel;
import com.example.tasklist.Utils.DatabaseHandler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.security.PublicKey;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";

    private EditText newTaskText;
    private Button newTaskSaveButton;
    private DatabaseHandler db;
    public static AddNewTask newInstance(){
        return new  AddNewTask();

    }
    @Override
    public  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL,R.style.DialogStyle);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
       View view = inflater.inflate(R.layout.new_task,container,false);
       getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
       return  view;

    }
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        newTaskText = getView().findViewById(R.id.newTaskText);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);



        boolean isUpdate= false;
        final Bundle bundle = getArguments();
        if(bundle!=null){
            isUpdate = true;
            String task = bundle.getString("task");
            newTaskText.setText(task);
            if(task.length()>0){
                newTaskSaveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.purple_200));
            }
            db= new DatabaseHandler(getActivity());
            db.openDatabase();
            newTaskText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().equals("")){
                        newTaskSaveButton.setEnabled(false);
                        newTaskSaveButton.setTextColor(Color.GRAY);
                    }
                    else{
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(getContext(),R.color.purple_200));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            }) ;

            boolean finalIsUpdate = isUpdate;
            newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text  = newTaskText.getText().toString();
                    if(finalIsUpdate){
                        db.updateTask(bundle.getInt("id"),text);
                    }
                    else{
                        ToDoModel task  = new ToDoModel();
                        task.setTask(text);
                        task.setStatus(0);
                        db.insertTask(task);
                    }
                    dismiss();
                }
            });

        }
    }
    @Override
    public void onDismiss(DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).handleDialogClose(dialog);
        }
    }
}
