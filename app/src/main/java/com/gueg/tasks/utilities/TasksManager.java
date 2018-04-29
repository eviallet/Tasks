package com.gueg.tasks.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import com.gueg.tasks.R;
import com.gueg.tasks.activities.MainActivity;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.fragments.CategoriesFragment;
import com.gueg.tasks.fragments.TasksCalendarFragment;
import com.gueg.tasks.fragments.TasksListFragment;
import com.gueg.tasks.notifications.NotificationUtility;
import com.gueg.tasks.sql.SQLUtility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TasksManager {
    public static int TASK_LIST = 1;
    public static int TASK_CALENDAR = 2;
    int prevTab;

    private int animCounter;


    private List<Task> tasksList;
    private ArrayList<String> categoriesToHide;
    private int currentView;
    private FragmentManager fragmentManager;
    private FrameLayout fragmentContainer;

    private CalendarUtility calendarUtility;
    private NotificationUtility notificationUtility;

    private SQLUtility sql;

    private SharedPreferences sharedPrefs;

    private Context mContext;
    private MainActivity mActivity;

    private TasksCalendarFragment tasksCalendarFragment;
    private TasksListFragment tasksListFragment;


    @SuppressWarnings("ConstantConditions")
    public TasksManager(FragmentManager fragMan, FrameLayout fragCont, Context context, Activity activity, int calId) {
        tasksList = new ArrayList<>();
        calendarUtility = new CalendarUtility(activity,calId);
        notificationUtility = new NotificationUtility(context);
        fragmentManager = fragMan;
        fragmentContainer = fragCont;
        tasksCalendarFragment = new TasksCalendarFragment();
        tasksListFragment = new TasksListFragment();
        mContext = context;
        mActivity = (MainActivity)activity;
        sharedPrefs = context.getSharedPreferences("com.gueg.tasks.CATEGORIES",Context.MODE_PRIVATE);
        if(sharedPrefs.getStringSet("com.gueg.tasks.CATEGORIES_TO_HIDE",null)==null)
            categoriesToHide = new ArrayList<>();
        else
            categoriesToHide = new ArrayList<>(sharedPrefs.getStringSet("com.gueg.tasks.CATEGORIES_TO_HIDE", null));
        animCounter=0;
        initView();
        sql = new SQLUtility(context);
        loadSqlTasks();
        loadLocalTasks();
        sortList();
    }


    private void initView() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragmentContainer.getId(),tasksListFragment,"TASK_LIST");
        fragmentTransaction.add(fragmentContainer.getId(),tasksCalendarFragment,"TASK_CALENDAR");
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void switchView() {
        if(currentView==TASK_LIST)
            changeView(TASK_CALENDAR);
        else if(currentView==TASK_CALENDAR)
            changeView(TASK_LIST);
        else // currentView==TASK_MANAGER
            changeView(TASK_LIST);
    }

    public void changeView(int view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(animCounter%2==0)
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right);
        else
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left);

        if(view==TASK_LIST)
            fragmentTransaction.replace(fragmentContainer.getId(),tasksListFragment);
        else if(view==TASK_CALENDAR)
            fragmentTransaction.replace(fragmentContainer.getId(),tasksCalendarFragment);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
        currentView = view;
        animCounter++;
    }

    public int getCurrentView() {
        return currentView;
    }

    public void showCatManager() {
        CategoriesFragment categoriesFragment = new CategoriesFragment();
        prevTab = currentView;
        fragmentManager
                .beginTransaction()
                .replace(fragmentContainer.getId(),categoriesFragment,"CATEGORIES")
                .show(categoriesFragment)
                .commit();
    }

    public boolean isCategoriesManagerOpen() {
        CategoriesFragment catFrag = (CategoriesFragment) fragmentManager.findFragmentByTag("CATEGORIES");
        return catFrag!=null&&catFrag.isVisible();
    }

    public void hideCatManager() {
        if(isCategoriesManagerOpen()) {
            fragmentManager.popBackStack("CATEGORIES",0);
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("CATEGORIES")).commit();
            changeView(prevTab);
        }
    }





    @SuppressWarnings("unchecked")
    private void loadSqlTasks() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected List<Task> doInBackground(Object[] params) {
                return sql.readAllTasks();
            }
        };

        try {
            tasksList = (List<Task>)asyncTask.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }








    public void writeTask(Task t) {
        if(t.isDateSet()) {
            calendarUtility.writeTask(t);
        }
    }

    public void deleteTask(Task t) {
        if(t.isDateSet()&&t.getTaskId()!=-1) {
            calendarUtility.deleteTask(t);
            cancelNotification(t);
        }

        tasksList.remove(t);
        sql.deleteTask(t);
        notifyAdapters();
    }

    public void addTask(Task newTask) {
        tasksList.add(newTask);
        sortList();
        updateTaskColor();
        writeTask(newTask);
        if(newTask.isTimeSet()&&newTask.isReminderEnabled())
            notificationUtility.addNotification(newTask);

        sql.write(newTask);
        notifyAdapters();
    }

    private void notifyAdapters() {
        if(currentView==TASK_LIST)
            tasksListFragment.notifyAdapter();
        else if(currentView==TASK_CALENDAR)
            tasksCalendarFragment.notifyAdapter();
    }

    public void updateTask(Task newTask) {
        calendarUtility.deleteTask(newTask);
        notifyAdapters();
        addTask(newTask);
    }

    public void editTask(Task t) {
        boolean found=false;
        int i=0;
        while(!found) {
            if(tasksList.get(i).isEqual(t)) {
                found=true;
            } else
                i++;
        }
        tasksList.remove(i);
        sql.deleteTask(t);
        cancelNotification(t);
        mActivity.editTask(t);
        quickUpdateFragments();
    }


    @SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
    private void sortList() {
        for(int i=0;i<tasksList.size();i++) {
            Task ti = tasksList.get(i);
            for (int j = i + 1; j < tasksList.size(); j++) {
                Task tj = tasksList.get(j);

                if(ti.isDateSet()&&tj.isDateSet()) {
                    if(ti.isAfter(tj,Task.SORT_DATE)) {
                        Collections.swap(tasksList, i, j);
                        i = 0;
                        break;
                    }
                } else if(ti.isDateSet()&&!tj.isDateSet()) {
                } else if(!ti.isDateSet()&&tj.isDateSet()) {
                    Collections.swap(tasksList,i,j);
                    i=0;
                    break;
                } else if(!ti.isDateSet()&&!tj.isDateSet()) {
                    if(ti.isAfter(tj,Task.SORT_ALPHABETICALLY)) {
                        Collections.swap(tasksList,i,j);
                        i=0;
                        break;
                    }
                }
            }
        }
        for(int i=0;i<tasksList.size();i++) {
            Task ti = tasksList.get(i);
            for (int j = i + 1; j < tasksList.size(); j++) {
                Task tj = tasksList.get(j);

                if(ti.isDateSet()&&tj.isDateSet()) {
                    if(ti.isAfter(tj,Task.SORT_DATE)) {
                        Collections.swap(tasksList, i, j);
                        i = 0;
                        break;
                    }
                } else if(ti.isDateSet()&&!tj.isDateSet()) {
                } else if(!ti.isDateSet()&&tj.isDateSet()) {
                    Collections.swap(tasksList,i,j);
                    i=0;
                    break;
                } else if(!ti.isDateSet()&&!tj.isDateSet()) {
                    if(ti.isAfter(tj,Task.SORT_ALPHABETICALLY)) {
                        Collections.swap(tasksList,i,j);
                        i=0;
                        break;
                    }
                }
            }
        }
        notifyAdapters();
    }

    public void onStateChanged(Task task) {
        boolean found = false;
        int i=0;
        while(!found&&i<tasksList.size()) {
            if(tasksList.get(i).equals(task))
                found=true;
            else
                i++;
        }
        if(found) {
            tasksList.get(i).toggleIsDone();
            if(task.isReminderEnabled()&&task.isTimeSet()&&task.getTaskId()!=-1) {
                if(tasksList.get(i).isDone()) // is done
                    cancelNotification(task);
                else
                    notificationUtility.addNotification(task);
            }
            sql.updateState(task,task.isDone());

            quickUpdateFragments();
        }
    }

    private void loadLocalTasks() {
        ArrayList<Task> retrieved = calendarUtility.retrieveCalendarEvents();
        boolean isAdded;
        for(Task t : retrieved) {
            isAdded = false;
            for(Task u : tasksList) {
                if(t.isEqual(u))
                    isAdded=true;
            }
            if(!isAdded)
                tasksList.add(t);
        }

    }

    public void onBoot() {
        for(Task task : tasksList) {
            if (task.isTimeSet() && task.isReminderEnabled() && !task.isDone())
                notificationUtility.addNotification(task);
        }
    }

    public void cleanTasks() {
        List<Task> toDelete = new ArrayList<>();
        for(Task task : tasksList)
            if(task.isDone())
                toDelete.add(task);
        for(Task task : toDelete)
            deleteTask(task);
        quickUpdateFragments();
    }

    public void onDestroy() {
        sql.onDestroy();
    }














    public ArrayList<String> getCategories() {
        ArrayList<String> res = new ArrayList<>();

        for(Task task : tasksList) {
            if(task.hasCategory()&&!isCategoryHidden(task.getCategory())&&!isAlreadyAdded(res,task.getCategory()))
                res.add(task.getCategory());
            if(!isCategoryHidden("Sans catégorie")&&!isAlreadyAdded(res,"Sans catégorie"))
                res.add("Sans catégorie");
            if(!isCategoryHidden("Tâches terminées"))
                res.add("Tâches terminées");
        }

        return res;
    }

    private boolean isAlreadyAdded(ArrayList<String> list, String str) {
        for(String cat : list) {
            if(cat.equals(str))
                    return true;
        }
        return false;
    }

    public boolean isCategoryHidden(String cat) {
        for(String str : categoriesToHide) {
            if (str.equals(cat))
                return true;
        }
        return false;
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> res = new ArrayList<>();
        res.add("Sans catégorie");
        res.add("Tâches terminées");
        for(Task task : tasksList) {
            if(task.hasCategory()&&!isAlreadyAdded(res,task.getCategory()))
                res.add(task.getCategory());
        }
        return res;
    }

    public void categoryManager(String cat, boolean show) {
        if(show) {
            int i= 0 ; boolean found = false;
            while(i<categoriesToHide.size()&&!found) {
                if(categoriesToHide.get(i).equals(cat)) {
                    found=true;
                    categoriesToHide.remove(i);
                }
                i++;
            }
        } else {
            categoriesToHide.add(cat);
        }

        sharedPrefs.edit().clear().apply();
        Set<String> set = new HashSet<>();
        set.addAll(categoriesToHide);
        sharedPrefs.edit().putStringSet("com.gueg.tasks.CATEGORIES_TO_HIDE",set).apply();

        if(currentView==TASK_LIST)
            tasksListFragment.setCategoriesToShow(getCategories());
        if(currentView==TASK_CALENDAR)
            tasksCalendarFragment.setCategoriesToShow(getCategories());
    }

    @SuppressWarnings("deprecation")
    public void updateTaskColor() {
        SharedPreferences sharedPref = mContext.getSharedPreferences("com.gueg.tasks.CATEGORIES_COLORS",Context.MODE_PRIVATE);
        for(Task task : tasksList) {
            if(task.hasCustomColor())
                task.clearCustomColor();
            if(task.hasCategory())
                task.setCustomColor(sharedPref.getInt(task.getCategory(),mContext.getResources().getColor(R.color.colorTaskTime)));
        }

    }

    public List<Task> getTasksList() {
        return tasksList;
    }

    private void cancelNotification(Task t) {
        notificationUtility.cancelNotification(notificationUtility.getPendingIntent(t));
    }




    public void showCleanTasksDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        cleanTasks();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder
                .setMessage("Nettoyer les tâches terminées ?")
                .setPositiveButton("Oui", dialogClickListener)
                .setNegativeButton("Non", dialogClickListener)
                .show();
    }

    public void sqlUpdated() {
        loadSqlTasks();
        loadLocalTasks();
        sortList();
        quickUpdateFragments();
    }

    private void quickUpdateFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(currentView==TASK_LIST)
            fragmentTransaction.replace(fragmentContainer.getId(),tasksListFragment);
        else if(currentView==TASK_CALENDAR)
            fragmentTransaction.replace(fragmentContainer.getId(),tasksCalendarFragment);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

}
