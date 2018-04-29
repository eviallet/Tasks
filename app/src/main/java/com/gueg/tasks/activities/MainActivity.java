package com.gueg.tasks.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.utilities.TasksManager;
import java.util.ArrayList;
import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements OnMainActivityCallListener {


    private SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("color")) {
                        changeColor(prefs.getInt("color",0xff3494E8));
                    }
                }
    };

    // CONSTANTES
    static int ACTIVITYRESULTS_NEWTASK = 1;
    static int ACTIVITYRESULTS_EDITTASK = 12;
    static int ANIMATION_DURATION = 200;
    static float ANIMATION_UP = -240;
    static float ANIMATION_LEFT = -240;
    private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 100;
    private static final int PERMISSIONS_REQUEST_WRITE_CALENDAR = 101;
    private static final Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/calendars");
    private static final int CALENDAR_ID = 0;
    private static final int CALENDAR_NAME = 1;

    public static final String[] FIELDS = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    };



    // FRAG MANAGER
    TasksManager tasksManager;
    FragmentManager fragmentManager;
    FrameLayout fragmentContainer;

    // VARIABLES
    boolean isHandlerRunning;
    boolean isMenuOpen;
    boolean willSwitch;
    Task taskBeeingEdited;

    // UTILITIES
    Handler handler;
    Runnable runnable;

    // LAYOUT ELEMENTS
    ArrayList<ImageButton> mBtns;
    ImageButton mBtn_menu;
    ImageButton mBtn_add;
    ImageButton mBtn_categories;
    ImageButton mBtn_settings;
    ImageButton mBtn_clean;
    ImageButton mBtn_switch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermissions();
        int calId = readCalendar();
        if(calId==-1) {
            finish();
            // TODO
        }

        // UTILITIES
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                for (ImageButton btn : mBtns) {
                    btn.setVisibility(View.INVISIBLE);
                    btn.clearAnimation();
                    if(willSwitch) {
                        switchView();
                        willSwitch = false;
                    }
                    isHandlerRunning = false;
                }
            }
        };
        isHandlerRunning = false;
        willSwitch = false;


        // FRAG MANAGER
        fragmentManager = getSupportFragmentManager();
        fragmentContainer = findViewById(R.id.main_fragment_container);
        tasksManager = new TasksManager(fragmentManager,fragmentContainer,getApplicationContext(),this, calId);
        SharedPreferences prefs = getSharedPreferences("com.gueg.tasks", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        boolean prefSwitch= prefs.getBoolean("prefSwitch", true);
        if(prefSwitch) {
            tasksManager.changeView(TasksManager.TASK_LIST);
        }
        else {
            tasksManager.changeView(TasksManager.TASK_CALENDAR);
        }
        changeColor(prefs.getInt("color",0xff3494E8));


        isMenuOpen=false;

        mBtns = new ArrayList<>();


        mBtn_add = findViewById(R.id.main_button_add);
        mBtn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,NewTaskActivity.class);
                startActivityForResult(intent,ACTIVITYRESULTS_NEWTASK);
                menuAction();
            }
        });

        mBtn_categories = findViewById(R.id.main_button_categories);
        mBtn_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasksManager.showCatManager();
                menuAction();
            }
        });

        mBtn_settings = findViewById(R.id.main_button_settings);
        mBtn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                menuAction();
            }
        });

        mBtn_clean = findViewById(R.id.main_button_clean);
        mBtn_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasksManager.showCleanTasksDialog();
                menuAction();
            }
        });


        mBtn_menu = findViewById(R.id.main_button_menu);
        mBtn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAction();
            }
        });

        mBtn_switch = findViewById(R.id.main_button_switch);
        mBtn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksManager.switchView();
                willSwitch = true;
                menuAction();
            }
        });
        if(prefSwitch)
            mBtn_switch.setImageDrawable(getDrawable(R.drawable.menu_agenda));
        else
            mBtn_switch.setImageDrawable(getDrawable(R.drawable.menu_list));

        mBtns.add(mBtn_add);
        mBtns.add(mBtn_categories);
        mBtns.add(mBtn_settings);
        mBtns.add(mBtn_clean);
        mBtns.add(mBtn_switch);

        for(ImageButton btn : mBtns)
            btn.setVisibility(View.INVISIBLE);

        mBtn_add.animate().scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
        mBtn_clean.animate().scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
        mBtn_categories.animate().scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
        mBtn_settings.animate().scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
        mBtn_switch.animate().scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();



        Intent intent = getIntent();
        if(intent.getBooleanExtra("BOOT",false))  // awakened by android boot
            tasksManager.onBoot();
        else  if(intent.getBooleanExtra("ACTION_EDIT",false)) {   // awakened by notification edit
            Task t = (Task)intent.getSerializableExtra("TASK_EDIT");
            if(t!=null) {
                tasksManager.editTask(t);
            }
        }
        else if(intent.getBooleanExtra("CLOSE",false))
            finishAndRemoveTask();
        else if(intent.getBooleanExtra("REFRESH",false))
            tasksManager.sqlUpdated();

    }   // onCreate()




    private void menuAction() {
        if(tasksManager.isCategoriesManagerOpen())
            tasksManager.hideCatManager();
        else {
            tasksManager.hideCatManager();
            if (!isMenuOpen) {       // OPENING
                if (isHandlerRunning) {
                    handler.removeCallbacks(runnable);
                }
                isMenuOpen = true;
                for (ImageButton btn : mBtns)
                    btn.setVisibility(View.VISIBLE);
                mBtn_menu.animate().rotation(90).setDuration(ANIMATION_DURATION).start();
                mBtn_add.animate().translationY(ANIMATION_UP).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();
                mBtn_categories.animate().translationY(2 * ANIMATION_UP).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();
                mBtn_clean.animate().translationY(3 * ANIMATION_UP).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();
                mBtn_settings.animate().translationX(2 * ANIMATION_LEFT).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();
                mBtn_switch.animate().translationX(ANIMATION_LEFT).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();

            } else {                  // CLOSING
                isMenuOpen = false;
                mBtn_menu.animate().rotation(0).setDuration(ANIMATION_DURATION).start();
                mBtn_add.animate().translationY(0).scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
                mBtn_clean.animate().translationY(0).scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
                mBtn_categories.animate().translationY(0).scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
                mBtn_settings.animate().translationX(0).scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();
                mBtn_switch.animate().translationX(0).scaleX(0.4f).scaleY(0.4f).setDuration(ANIMATION_DURATION).start();

                handler.postDelayed(runnable, (long) (ANIMATION_DURATION / 1.25));
                isHandlerRunning = true;
            }
        }
    }


    public void switchView() {
        if(tasksManager.getCurrentView()==TasksManager.TASK_LIST)
            mBtn_switch.setImageDrawable(getDrawable(R.drawable.menu_agenda));
        else
            mBtn_switch.setImageDrawable(getDrawable(R.drawable.menu_list));

    }

    @Override
    public TasksManager getTasksManager() {
        return tasksManager;
    }

    @Override
    public FragmentManager getFragmentManagerL() {
        return fragmentManager;
    }

    @Override
    public void showDialog(AmbilWarnaDialog dialog) {
        dialog.show();
    }

    @Override
    public Context getMAContext() {
        return this;
    }

    @Override
    public void addTaskPreset(Date date) {
        Intent intent = new Intent(MainActivity.this,NewTaskActivity.class);
        intent.putExtra("DATE",Long.toString(date.getTime()));
        startActivityForResult(intent,ACTIVITYRESULTS_NEWTASK);
    }

    public void editTask(Task taskToEdit) {
        taskBeeingEdited = taskToEdit;
        Intent intent = new Intent(MainActivity.this,EditTaskActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("TASK_EDIT",taskToEdit);
        intent.putExtras(bundle);
        startActivityForResult(intent,ACTIVITYRESULTS_EDITTASK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITYRESULTS_NEWTASK) {
            if(resultCode == Activity.RESULT_OK){
                Bundle results = data.getExtras();
                Task task = (Task) results.getSerializable("TASK");
                tasksManager.addTask(task);
            }
        }
        else if (requestCode == ACTIVITYRESULTS_EDITTASK) {
            if(resultCode == Activity.RESULT_OK){
                Bundle results = data.getExtras();
                Task task = (Task) results.getSerializable("TASK");
                tasksManager.updateTask(task);
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                tasksManager.updateTask(taskBeeingEdited);
            }
            else //noinspection StatementWithEmptyBody
                if (resultCode == EditTaskActivity.RESULT_DELETE) {
                Bundle results = data.getExtras();
                Task task = (Task) results.getSerializable("TASK");
                tasksManager.deleteTask(task);
            }
        }

    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, PERMISSIONS_REQUEST_READ_CALENDAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSIONS_REQUEST_WRITE_CALENDAR);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CALENDAR) {
            if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_CALENDAR) {
            if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        }
    }


    public int readCalendar(){

        Cursor cursor = getContentResolver().query(CALENDAR_URI, FIELDS, null, null, null);
        assert cursor != null;
        int id = -1;

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int tempid = Integer.decode(cursor.getString(CALENDAR_ID));
                    String name = cursor.getString(CALENDAR_NAME);
                    if(name.contains("@gmail.com"))
                        id = tempid;
                }
            }
        } catch (AssertionError e) {
            e.printStackTrace();
        }
        cursor.close();

        return id;
    }

    @SuppressWarnings("deprecation")
    private void changeColor(int color) {
        Drawable arrows = getResources().getDrawable(R.drawable.calendar_arrow);
        arrows.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        Drawable menu = getResources().getDrawable(R.drawable.menu_menu);
        menu.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
    }


    @Override
    public void onDestroy() {
        tasksManager.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
        finishAndRemoveTask();
        super.onDestroy();
    }


}
