package com.gueg.tasks.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gueg.tasks.classes.Date;
import com.gueg.tasks.fragments.DatePickerFragment;
import com.gueg.tasks.utilities.DateUtility;
import com.gueg.tasks.interfaces.OnPickerSet;
import com.gueg.tasks.R;
import com.gueg.tasks.widgets.SpinnerDate;
import com.gueg.tasks.widgets.SpinnerTime;
import com.gueg.tasks.classes.Task;
import com.gueg.tasks.classes.Time;
import com.gueg.tasks.fragments.TimePickerFragment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class NewTaskActivity extends android.support.v4.app.FragmentActivity implements Serializable, OnPickerSet {

    private static int RESULT_PICK_CONTACT = 1;

    TextView task_attendee_name;
    TextView task_attendee_email;
    ImageButton task_attendee_cancel;
    boolean isContactSelected;
    boolean willNotify;
    EditText task_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtask);

        task_name = findViewById(R.id.newtask_name);
        final EditText task_text = findViewById(R.id.newtask_text);
        final SpinnerDate task_date = findViewById(R.id.newtask_date);
        final SpinnerTime task_time = findViewById(R.id.newtask_time);
        final RadioButton radio_priority_low = findViewById(R.id.newtask_priority_low);
        final RadioButton radio_priority_normal = findViewById(R.id.newtask_priority_normal);
        final RadioButton radio_priority_high = findViewById(R.id.newtask_priority_high);
        //final RadioButton radio_priority_urgent = (RadioButton) findViewById(R.id.newtask_priority_urgent);
        final EditText task_category = findViewById(R.id.newtask_category);
        task_attendee_name = findViewById(R.id.newtask_attendee_name);
        task_attendee_email = findViewById(R.id.newtask_attendee_email);
        task_attendee_cancel = findViewById(R.id.newtask_pic_contact);
        ImageButton task_reminder = findViewById(R.id.newtask_reminder);
        final Spinner task_repeat = findViewById(R.id.newtask_repeat);
        final Spinner task_repeat_until = findViewById(R.id.newtask_repeat_until);


        String ds = getIntent().getStringExtra("DATE");
        Date date = null;
        if(ds!=null)
             date = new Date(Long.parseLong(ds));





        ImageButton btn_ok = findViewById(R.id.newtask_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(task_name.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Entrer un nom de tâche.",Toast.LENGTH_SHORT).show();
                }
                else {
                    int priority;
                    if (radio_priority_low.isChecked())
                        priority = Task.PRIORITY_LOW;
                    else if (radio_priority_normal.isChecked())
                        priority = Task.PRIORITY_NORMAL;
                    else if (radio_priority_high.isChecked())
                        priority = Task.PRIORITY_HIGH;
                    else // radio_priority_urgent.isChecked();
                        priority = Task.PRIORITY_URGENT;


                    Task task = new Task(
                            task_name.getText().toString(),
                            task_date.getDate(),
                            task_time.getTime(),
                            priority,
                            task_text.getText().toString(),
                            task_category.getText().toString(),
                            task_attendee_name.getText().toString(),
                            task_attendee_email.getText().toString(),
                            willNotify,
                            (String)task_repeat.getSelectedItem(),
                            (String)task_repeat_until.getSelectedItem());

                    Intent data = new Intent();
                    data.putExtra("TASK",task);
                    setResult(Activity.RESULT_OK,data);
                    finish();
                }
            }
        });
        ImageButton btn_cancel = findViewById(R.id.newtask_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent results = new Intent();
                setResult(Activity.RESULT_CANCELED,results);
                finish();
            }
        });
        ImageButton btn_delete = findViewById(R.id.newtask_delete);
        btn_delete.setVisibility(View.GONE);




        task_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(Objects.equals(task_date.getItemAtPosition(position).toString(), "Choisir une date")) {
                    OnPickerSet listener = new OnPickerSet() {
                        @Override
                        public void onTimeSet(Time time) {}
                        @Override
                        public void onDateSet(long date) {
                            task_date.setDateFromPicker(date);
                        }
                    };
                    DatePickerFragment dialog = new DatePickerFragment();
                    dialog.setOnPickerSetListener(listener);
                    dialog.show(getSupportFragmentManager(), "DATEPICKER");
                }
                else if(task_date.getSelectedItemPosition()==0&&task_time.getSelectedItemPosition()!=0) {
                    task_time.setSelection(0);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        if(date!=null)
            task_date.setDateFromPicker(date.getTime());


        task_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(task_date.getSelectedItem().equals("")&&!task_time.getSelectedItem().equals("")&&!task_time.getSelectedItem().equals("Choisir une heure")) {
                    if(DateUtility.isTimePast(
                            new Time(
                                    DateUtility.convertTime(task_time.getSelectedItem().toString())))) {
                        task_date.setSelection(2);
                    }
                    else {
                        task_date.setSelection(1);
                    }
                }
                if(Objects.equals(task_time.getItemAtPosition(position).toString(), "Choisir une heure")) {
                    OnPickerSet listener = new OnPickerSet() {
                        @Override
                        public void onTimeSet(Time time) {
                            task_time.setTimeFromPicker(time);
                        }
                        @Override
                        public void onDateSet(long date) {}
                    };
                    TimePickerFragment dialog = new TimePickerFragment();
                    dialog.setOnPickerSetListener(listener);
                    dialog.show(getSupportFragmentManager(), "TIMEPICKER");
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });


        View.OnClickListener contactPicker = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        };

        isContactSelected = false;
        task_attendee_name.setOnClickListener(contactPicker);
        task_attendee_email.setOnClickListener(contactPicker);
        task_attendee_email.setText("");

        task_attendee_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isContactSelected)
                    cancelPick();
            }
        });
        task_attendee_cancel.setBackgroundResource(R.drawable.newtask_attendees);



        task_reminder.setBackgroundResource(R.drawable.newtask_reminder_on);
        willNotify = true;
        task_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(willNotify) {
                    willNotify=false;
                    v.setBackgroundResource(R.drawable.newtask_reminder_off);
                } else {
                    willNotify=true;
                    v.setBackgroundResource(R.drawable.newtask_reminder_on);
                }
            }
        });



        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.addAll(Arrays.asList("","Chaque semaine","Chaque mois","Chaque année"));
        task_repeat.setAdapter(spinnerArrayAdapter);
        task_repeat.setSelection(0);
        task_repeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                    task_repeat_until.setSelection(0);
                else if(task_repeat_until.getSelectedItemPosition()==0)
                    task_repeat_until.setSelection(1);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter2.addAll(Arrays.asList("","Pendant un mois","Pendant 6 mois","Pendant 1 an","Pendant 5 ans"));
        task_repeat_until.setAdapter(spinnerArrayAdapter2);
        task_repeat_until.setSelection(0);
        task_repeat_until.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(task_repeat.getSelectedItemPosition()==0&&position!=0) {
                    task_repeat.setSelection(1);
                } else if(position==0)
                    task_repeat.setSelection(0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });



    }

    public void onTimeSet(Time time) {}
    public void onDateSet(long date) {}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            if (requestCode == RESULT_PICK_CONTACT)
                    contactPicked(data);
    }

    private void cancelPick() {
        task_attendee_name.setText("");
        task_attendee_email.setText("");
        task_attendee_cancel.setBackgroundResource(R.drawable.newtask_attendees);
        isContactSelected=false;
    }

    private void contactPicked(Intent data) {
        Cursor cursor;
        try {
            String name;
            String email;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int  emailIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            email = cursor.getString(emailIndex);
            name = cursor.getString(nameIndex);

            cursor.close();

            isContactSelected=true;
            task_attendee_name.setText(name);
            task_attendee_email.setText(email);
            task_attendee_cancel.setBackgroundResource(R.drawable.newtask_attendee_cancel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        if(task_name.getText().toString().isEmpty())
            super.onBackPressed();
        else {
            showExitDialog();
        }
    }

    private void showExitDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        NewTaskActivity.super.onBackPressed();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Annuler la création de l'évènement?").setPositiveButton("Oui", dialogClickListener)
                .setNegativeButton("Non", dialogClickListener).show();
    }


}
