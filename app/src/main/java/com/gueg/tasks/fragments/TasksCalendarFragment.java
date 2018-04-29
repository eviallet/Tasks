package com.gueg.tasks.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import com.gueg.tasks.widgets.CalendarView;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.utilities.TasksManager;
import java.util.ArrayList;
import java.util.HashSet;


public class TasksCalendarFragment extends Fragment {

    private TasksManager tasksManager;
    private OnMainActivityCallListener mMainActivityListener;
    private CalendarView calendar;
    private FrameLayout fragContainer;
    private TasksListFragment tasksListFragment;
    private FragmentManager fragmentManager;
    View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_taskscalendar, container, false);
        super.onCreateView(inflater,container,savedInstanceState);

        calendar = ((CalendarView)rootView.findViewById(R.id.calendar));
        tasksManager = mMainActivityListener.getTasksManager();

        calendar.updateCalendar(new HashSet<>(tasksManager.getTasksList()));

        calendar.setCategoriesToShow(tasksManager.getCategories());

        // assign event handler
        calendar.setEventHandler(new CalendarView.EventHandler()
        {
            @Override
            public void onDayLongPress(Date date) {
                mMainActivityListener.addTaskPreset(date);
            }

            @Override
            public void onDayPress(Date date) {
                tasksListFragment.setDateToShow(date);
            }
        });

        fragmentManager = getFragmentManager();
        fragContainer = (FrameLayout)rootView.findViewById(R.id.taskscalendar_fragmentcontainer);
        tasksListFragment = new TasksListFragment();
        Bundle b = new Bundle();
        b.putString("DATE",Long.toString(new Date().getTime()));
        tasksListFragment.setArguments(b);
        fragmentManager.beginTransaction().add(fragContainer.getId(),tasksListFragment).commit();


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMainActivityListener = (OnMainActivityCallListener) context;
        } catch (ClassCastException castException) {
            // The activity does not implement the listener.
        }
    }

    public void setCategoriesToShow(ArrayList<String> strList) {
        tasksListFragment.setCategoriesToShow(strList);
        calendar.setCategoriesToShow(strList);
    }

    public void notifyAdapter() {
        if(calendar!=null)
            calendar.updateCalendar(new HashSet<>(tasksManager.getTasksList()));
    }

    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);


        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }

        if (animation != null&&getView()!=null) {
            getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

            animation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    getView().setLayerType(View.LAYER_TYPE_NONE, null);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }
            });
        }

        return animation;
    }
}
