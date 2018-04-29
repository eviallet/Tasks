package com.gueg.tasks.adapters;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.gueg.tasks.fragments.DialogTaskContent;
import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.utilities.TasksManager;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.utilities.DateUtility;
import java.util.ArrayList;
import java.util.List;


public class TasksListAdapter extends RecyclerView.Adapter<TasksListAdapter.ViewHolder>{

    private List<Task> mList;
    private List<Task> mToShow;
    private Context mContext;
    private TasksManager tasksManager;
    private OnMainActivityCallListener mMainActivityListener;

    private List<String> categoriesToShow;
    private Date dateToShow;




    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox mCheckBox;
        TextView mTaskTitle;
        TextView mTaskDate;
        TextView mCategory;
        ImageButton mImageButton;
        ImageView mContact;
        ImageView mRepeat;

        ViewHolder(View v) {
            super(v);
            mCheckBox = (CheckBox) v.findViewById(R.id.row_taskslist_checkbox);
            mTaskTitle = (TextView) v.findViewById(R.id.row_taskslist_tasktitle);
            mTaskDate = (TextView) v.findViewById(R.id.row_taskslist_tasktime);
            mImageButton = (ImageButton) v.findViewById(R.id.row_taskslist_content);
            mCategory = (TextView) v.findViewById(R.id.row_taskslist_category);
            mContact = (ImageView) v.findViewById(R.id.row_taskslist_attendee);
            mRepeat = (ImageView) v.findViewById(R.id.row_taskslist_repeat);
        }
    }

    public TasksListAdapter(List<Task> list, Context context, TasksManager tasksM, OnMainActivityCallListener listener, Date toShow) {
        mList = list;
        mContext = context;
        tasksManager = tasksM;
        mMainActivityListener = listener;
        if(toShow!=null)
            dateToShow = toShow;
        else
            dateToShow = new Date(1);
        categoriesToShow = tasksManager.getCategories();
        mToShow = getTasksToShow(dateToShow,categoriesToShow);
    }


    @Override
    public TasksListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_taskslist, parent, false);
        return new TasksListAdapter.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final TasksListAdapter.ViewHolder holder, int position) {
        final Task task = mToShow.get(position);

        // CHECKBOX
        if(task.hasCustomColor()) {
            holder.mCheckBox.setBackgroundTintList(ColorStateList.valueOf(task.getColor()));
            holder.mCheckBox.setButtonTintList(ColorStateList.valueOf(task.getColor()));
        } else {
            holder.mCheckBox.setBackgroundTintList(getColor(task.getPriority()));
            holder.mCheckBox.setButtonTintList(getColor(task.getPriority()));
        }
        holder.mCheckBox.setChecked(task.isDone());
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksManager.onStateChanged(task);
            }
        });


        // TASKTITLE
        holder.mTaskTitle.setText(task.getName());

        holder.mTaskTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksManager.editTask(task);
            }
        });

        if(!task.hasAttendee()) {
            holder.mContact.setVisibility(View.GONE);
        }


        // TASKTIME
        if (task.isDateSet()) {
            boolean isNear = false;
            String dateProx = "";
            int prox = DateUtility.getProximity(task.getDate());
            if (prox == DateUtility.YESTERDAY) {
                isNear = true;
                dateProx = "Hier";
            } else if (prox == DateUtility.TODAY) {
                isNear = true;
                dateProx = "Aujourd'hui";
            } else if (prox == DateUtility.TOMORROW) {
                isNear = true;
                dateProx = "Demain";
            } else if (prox == DateUtility.TWODAYS) {
                isNear = true;
                dateProx = "Après-demain";
            }


            if (isNear) {
                if (task.isTimeSet())
                    holder.mTaskDate.setText(dateProx + " - " + task.getTime().toString());
                else
                    holder.mTaskDate.setText(dateProx);
            } else {
                if (task.isTimeSet())
                    holder.mTaskDate.setText(task.getDate().toString() + " - " + task.getTime().toString());
                else
                    holder.mTaskDate.setText(task.getDate().toString());
            }
        }


        // CONTENT
        if (task.hasContent()) {
            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogTaskContent dialog = new DialogTaskContent();
                    Bundle args = new Bundle();
                    args.putString("TASK_TEXT", task.getDescription());
                    dialog.setArguments(args);
                    dialog.show(mMainActivityListener.getFragmentManagerL(), "DIALOG_CONTENT");
                }
            });
        } else {
            holder.mImageButton.setVisibility(View.GONE);
        }

        if(task.hasCategory()) {
            holder.mCategory.setText(task.getCategory());
            if(task.hasCustomColor())
                holder.mCategory.setTextColor(task.getColor());
        }
        else
            holder.mCategory.setVisibility(View.GONE);

        if(!task.willRepeat())
            holder.mRepeat.setVisibility(View.GONE);
    }


    @SuppressWarnings("deprecation")
    ColorStateList getColor(int priority) {
        if(priority==Task.PRIORITY_LOW)
            return ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorPriorityLow));
        else if(priority==Task.PRIORITY_NORMAL)
            return ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorPriorityNormal));
        else if(priority==Task.PRIORITY_HIGH)
            return ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorPriorityHigh));
        else // priority==Task.PRIORITY_URGENT
            return ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorPriorityUrgent));
    }


    @Override
    public int getItemCount() {
        return mToShow.size();
    }

    public void setDateToShow(Date date) {
        dateToShow = date;
        mToShow = getTasksToShow(date,categoriesToShow);
        notifyDataSetChanged();
    }

    public void setCategoriesToShow(List<String> strList) {
        categoriesToShow = strList;
        mToShow = getTasksToShow(dateToShow,strList);
        notifyDataSetChanged();
    }

    public List<Task> getTasksToShow(Date date, List<String> strList) {
        // HIDE COMPLETED TASKS
        List<Task> res0 = new ArrayList<>();
        if(!strList.contains("Tâches terminées")) {
            for (Task task : mList)
                if (task.isDone() && !DateUtility.isDatePast(task.getCompletionDate()))
                    res0.add(task);
            else if(!task.isDone())
                res0.add(task);
        }
        else
            res0 = mList;

        // HIDE TASKS OUTSIDE OF SELECTED DAY
        List<Task> res1 = new ArrayList<>();
        Date showEveryTask = new Date(1);
        if(date.isEqual(showEveryTask)) {
            res1 = res0;
        } else {
            for (Task task : res0) {
                if (date.isEqual(task.getDate())||task.willRepeat()&&task.isDateInRepeatDates(date))
                    res1.add(task);
            }
        }

        // HIDE CATEGORIES
        List<Task> res2 = new ArrayList<>();
        for(Task task : res1) {
            if (task.hasCategory()) {
                if (strList.contains(task.getCategory()))
                    res2.add(task);
            } else {
                if (strList.contains("Sans catégorie"))
                    res2.add(task);
            }
        }
        return res2;
    }










}