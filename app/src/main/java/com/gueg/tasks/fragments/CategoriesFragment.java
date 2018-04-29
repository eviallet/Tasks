package com.gueg.tasks.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.gueg.tasks.adapters.CategoriesListAdapter;
import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.utilities.TasksManager;


public class CategoriesFragment extends Fragment {

    private View rootView;
    private TasksManager mTasksManager;
    private OnMainActivityCallListener mMainActivityListener;
    private ListView mList;
    private CategoriesListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_categories, container, false);
        super.onCreateView(inflater,container,savedInstanceState);

        mList = (ListView) rootView.findViewById(R.id.fragment_categories_list);
        mTasksManager = mMainActivityListener.getTasksManager();
        mAdapter = new CategoriesListAdapter(getActivity(),mTasksManager.getAllCategories(),mTasksManager,mMainActivityListener);
        mList.setAdapter(mAdapter);


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

}
