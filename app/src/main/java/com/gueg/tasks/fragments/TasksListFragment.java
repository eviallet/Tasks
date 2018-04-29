package com.gueg.tasks.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.adapters.TasksListAdapter;
import com.gueg.tasks.utilities.TasksManager;
import com.gueg.tasks.adapters.VerticalSpaceItemDecoration;
import java.util.ArrayList;

public class TasksListFragment extends Fragment {
    static int VERTICAL_ITEM_SPACE = 18;
    TasksManager tasksManager;
    private TasksListAdapter mAdapter;
    private OnMainActivityCallListener mMainActivityListener;
    View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_taskslist, container, false);
        super.onCreateView(inflater,container,savedInstanceState);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.taskslist_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        tasksManager = mMainActivityListener.getTasksManager();


        Bundle b = getArguments();
        String s = null;
        Date d = null;
        if(b!=null)
            s = b.getString("DATE");
        if(s!=null)
            d = new Date(Long.parseLong(s));

        mAdapter = new TasksListAdapter(tasksManager.getTasksList(),getActivity(),tasksManager,mMainActivityListener,d);

        mRecyclerView.setAdapter(mAdapter);


        return rootView;
    }



    @Override
    public void onStart() {
        setCategoriesToShow(tasksManager.getCategories());
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        mAdapter.setCategoriesToShow(strList);
    }


    public void setDateToShow(Date date) {
        mAdapter.setDateToShow(date);
    }


    public void notifyAdapter() {
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
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
