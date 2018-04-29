package com.gueg.tasks.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gueg.tasks.classes.Date;
import com.gueg.tasks.R;
import com.gueg.tasks.classes.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class CalendarView extends LinearLayout
{

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // events backup
    private HashSet<Task> eventBkp;

    // categories shown
    private List<String> categoriesToShow;

    // date format
    private String dateFormat;

    // event mark type
    private boolean calendarDayCircle;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;

    int[] rainbow = new int[] {
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    int[] monthSeason = new int[] {2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs)
    {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try
        {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        }
        finally
        {
            ta.recycle();
        }
    }


    private void assignUiElements()
    {
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers()
    {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        txtDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.setTime(Calendar.getInstance().getTime());
                updateCalendar();
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View cell, int position, long id) {
                if (eventHandler != null) {
                    eventHandler.onDayPress((Date)view.getItemAtPosition(position));
                }
            }
        });

        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id)
            {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar()
    {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Task> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) -2;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(new Date(calendar.getTime().getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        if(events!=null)
            eventBkp=events;
        calendarDayCircle = getContext().getSharedPreferences("com.gueg.tasks", Context.MODE_PRIVATE).getBoolean("prefCalendarDay",false);

        if(calendarDayCircle)
            grid.setAdapter(new CalendarAdapter(getContext(), cells, events));
        else
            grid.setAdapter(new CalendarAdapter(getContext(), cells, events,0));


        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.FRANCE);
        txtDate.setText(sdf.format(currentDate.getTime()));

        // set header color according to current season
        int month = currentDate.get(Calendar.MONTH);
        int season = monthSeason[month];
        int color = rainbow[season];

        header.setBackgroundColor(getResources().getColor(color));
    }


    private class CalendarAdapter extends ArrayAdapter<Date>
    {
        // days with events
        private HashSet<Task> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Task> eventDays) {
            super(context,R.layout.calendar_day_small,days);

            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Task> eventDays, @SuppressWarnings("UnusedParameters") int rectangle) {
            super(context,R.layout.calendar_day_normal,days);

            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View view, @NonNull ViewGroup parent)
        {
            // day in question
            Date date = getItem(position);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int curDay = cal.get(Calendar.DAY_OF_MONTH);
            int curMonth = cal.get(Calendar.MONTH);
            int curYear = cal.get(Calendar.YEAR);

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null) {
                if(calendarDayCircle)
                    view = inflater.inflate(R.layout.calendar_day_small, parent, false);
                else
                    view = inflater.inflate(R.layout.calendar_day_normal, parent, false);
            }



            int eventForThisDate = 0;
            if(eventDays==null)
                eventDays=eventBkp;
            if (eventDays != null) {
                for (Task task : eventDays) {
                    if (task.getDate().isEqual(getItem(position))||task.willRepeat()&&task.isDateInRepeatDates(date))  {
                        if(isCategoryShown(task.getCategory())) {
                            // mark this day for event
                            eventForThisDate++;
                            if (task.hasCustomColor()) {
                                switch (eventForThisDate) {
                                    case 1:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev1)).setImageDrawable(new ColorDrawable(task.getColor()));
                                        break;
                                    case 2:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev2)).setImageDrawable(new ColorDrawable(task.getColor()));
                                        break;
                                    case 3:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev3)).setImageDrawable(new ColorDrawable(task.getColor()));
                                        break;
                                    case 4:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev4)).setImageDrawable(new ColorDrawable(task.getColor()));
                                        break;
                                }
                            } else {
                                switch (eventForThisDate) {
                                    case 1:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev1)).setImageDrawable(getColor(task.getPriority()));
                                        break;
                                    case 2:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev2)).setImageDrawable(getColor(task.getPriority()));
                                        break;
                                    case 3:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev3)).setImageDrawable(getColor(task.getPriority()));
                                        break;
                                    case 4:
                                        ((ImageView) view.findViewById(R.id.calendar_day_ev4)).setImageDrawable(getColor(task.getPriority()));
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            // clear styling
            ((TextView)view.findViewById(R.id.calendar_day_text)).setTypeface(null, Typeface.NORMAL);
            ((TextView)view.findViewById(R.id.calendar_day_text)).setTextColor(Color.BLACK);


            cal.setTime(today);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            if(currentDate.get(Calendar.MONTH)==c.get(Calendar.MONTH)) {        // if selected month is actual month with today's date in it
                int todayDay = cal.get(Calendar.DAY_OF_MONTH);
                int todayMonth = cal.get(Calendar.MONTH);
                int todayYear = cal.get(Calendar.YEAR);

                if (curMonth != todayMonth) {
                    // if this day is outside current month, grey it out
                    ((TextView) view.findViewById(R.id.calendar_day_text)).setTextColor(getResources().getColor(R.color.greyed_out));
                } else if (curDay == todayDay && curYear == todayYear) {
                    // if it is today, set it to blue/bold
                    ((TextView) view.findViewById(R.id.calendar_day_text)).setTypeface(null, Typeface.BOLD);
                    ((TextView) view.findViewById(R.id.calendar_day_text)).setTextColor(getResources().getColor(R.color.today));
                }
            } else {
                if (currentDate.get(Calendar.MONTH) != curMonth) {
                    ((TextView) view.findViewById(R.id.calendar_day_text)).setTextColor(getResources().getColor(R.color.greyed_out));
                }
            }

            // set text
            assert date != null;
            ((TextView)view.findViewById(R.id.calendar_day_text)).setText(String.valueOf(date.getDate()));

            return view;
        }

        ColorDrawable getColor(int priority) {
            ColorDrawable d;
            if(priority==Task.PRIORITY_LOW)
                d = new ColorDrawable(getContext().getResources().getColor(R.color.colorPriorityLow));
            else if(priority==Task.PRIORITY_NORMAL)
                d = new ColorDrawable(getContext().getResources().getColor(R.color.colorPriorityNormal));
            else if(priority==Task.PRIORITY_HIGH)
                d = new ColorDrawable(getContext().getResources().getColor(R.color.colorPriorityHigh));
            else // priority==Task.PRIORITY_URGENT
                d = new ColorDrawable(getContext().getResources().getColor(R.color.colorPriorityUrgent));
            return d;
        }
    }

    public void setCategoriesToShow(List<String> cat) {
        categoriesToShow=cat;
    }

    private boolean isCategoryShown(String category) {
        if(category.equals(""))
            category="Sans catégorie";
        for(String cat : categoriesToShow)
            if(cat.equals(category))
                return true;
        return false;
    }




    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onDayLongPress(Date date);
        void onDayPress(Date date);
    }
}
