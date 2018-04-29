package com.gueg.tasks.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.gueg.tasks.interfaces.OnMainActivityCallListener;
import com.gueg.tasks.R;
import com.gueg.tasks.utilities.TasksManager;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class CategoriesListAdapter extends BaseAdapter {

    SharedPreferences sharedPrefColors;

    OnMainActivityCallListener mListener;
    Context mContext;
    ArrayList<String> mList = new ArrayList<>();
    private Activity activity;
    private static LayoutInflater inflater=null;
    TasksManager mTasksManager;

    public CategoriesListAdapter(Activity a, ArrayList<String> d, TasksManager tm, OnMainActivityCallListener listener) {

        activity = a;
        mContext = a.getApplicationContext();
        mList = d;
        mTasksManager = tm;
        mListener = listener;

        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mList.size();
    }

    public String getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{

        public CheckBox checkbox;
        public ImageButton image;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        final ViewHolder holder;

        if(convertView==null){

            vi = inflater.inflate(R.layout.row_categorieslist,parent,false);


            holder = new ViewHolder();
            holder.checkbox = (CheckBox) vi.findViewById(R.id.row_categorieslist_checkbox);
            holder.image=(ImageButton) vi.findViewById(R.id.row_categorieslist_color);

            vi.setTag( holder );
        }
        else {
            holder=(ViewHolder)vi.getTag();
        }

        holder.checkbox.setText(getItem(position));
        holder.checkbox.setChecked(!mTasksManager.isCategoryHidden(getItem(position)));

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTasksManager.categoryManager(buttonView.getText().toString(),isChecked);
            }
        });

        if(!getItem(position).equals("Sans catégorie")&&!getItem(position).equals("Tâches terminées")) {

            sharedPrefColors = activity.getSharedPreferences("com.gueg.tasks.CATEGORIES_COLORS", Context.MODE_PRIVATE);

            final int initialColor = sharedPrefColors.getInt(getItem(position), activity.getResources().getColor(R.color.colorTaskTime));


            holder.image.setBackgroundColor(initialColor);


            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AmbilWarnaDialog dialog = new AmbilWarnaDialog(mListener.getMAContext(), initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            setCategoryNewColor(getItem(position), color);
                            holder.image.setBackgroundColor(color);
                        }

                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                        }

                    });
                    mListener.showDialog(dialog);
                }
            });
        }


        return vi;
    }

    public void setCategoryNewColor(String key, int newColor) {
        SharedPreferences.Editor editor = sharedPrefColors.edit();
        editor.putInt(key,newColor);
        editor.apply();
        mTasksManager.updateTaskColor();
    }




}

