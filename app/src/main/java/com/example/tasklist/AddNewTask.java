package com.example.tasklist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.example.tasklist.database.TodoDao;
import com.example.tasklist.database.TodoDatabase;
import com.example.tasklist.database.TodoEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private final int REQUEST_CODE = 485;
    private StringBuilder result = new StringBuilder();
    private MaterialTextView mTv;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTv = getView().findViewById(R.id.phh);
        ImageButton imageButton = getView().findViewById(R.id.okd);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException a) {

                }
            }
        });

        TodoDatabase todoDatabase = Room.databaseBuilder(getActivity(), TodoDatabase.class, "TODO").build();
        TodoDao todoDao = todoDatabase.todoDao();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            mTv.setText(task);
            result = new StringBuilder(task);
        }

        boolean finalIsUpdate = isUpdate;
        Button newTaskSaveButton = getView().findViewById(R.id.kjd);
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.length() == 0) {
                    Toast.makeText(getActivity(), "Empty Text", Toast.LENGTH_SHORT).show();
                } else {
                    if (finalIsUpdate) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TodoEntity todoEntity = new TodoEntity();
                                todoEntity.id = bundle.getInt("id");
                                todoEntity.status = 1;
                                todoEntity.task = String.valueOf(result);
                                todoDao.edit(todoEntity.id, todoEntity.status, todoEntity.task);
                                saveImage();
                            }
                        }).start();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TodoEntity todoEntity = new TodoEntity();
                                todoEntity.id = Math.abs(new Random().nextInt() * 100000);
                                todoEntity.status = 0;
                                todoEntity.task = String.valueOf(result);
                                todoDao.insert(todoEntity);
                                saveImage();
                            }
                        }).start();
                    }
                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveImage() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TaskList");
        // Create a storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TaskList", "Failed to create directory");
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".png";

        String selectedOutputPath = mediaStorageDir.getPath() + File.separator + imageName;
        Log.d("TaskList", "selected camera path " + selectedOutputPath);

        mTv.setDrawingCacheEnabled(true);
        mTv.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(mTv.getDrawingCache());

        int maxSize = 1080;

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        if (bWidth > bHeight) {
            int imageHeight = (int) Math.abs(maxSize * ((float) bitmap.getWidth() / (float) bitmap.getHeight()));
            bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, imageHeight, true);
        } else {
            int imageWidth = (int) Math.abs(maxSize * ((float) bitmap.getWidth() / (float) bitmap.getHeight()));
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, maxSize, true);
        }
        mTv.setDrawingCacheEnabled(false);
        mTv.destroyDrawingCache();

        OutputStream fOut;
        try {
            File file = new File(selectedOutputPath);
            fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, selectedOutputPath);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener) {
            ((DialogCloseListener) activity).handleDialogClose(dialog);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                ArrayList<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                result.append(resultList.get(0));
                mTv.setText(result);
            }
        }
    }
}
