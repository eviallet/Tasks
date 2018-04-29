package com.gueg.tasks.classes;

import java.io.Serializable;
import java.util.Calendar;

public class Task implements Serializable {
    public static int PRIORITY_LOW = 0;
    public static int PRIORITY_NORMAL = 1;
    public static int PRIORITY_HIGH = 2;
    public static int PRIORITY_URGENT = 3;

    public static int SORT_DATE = 0;
    public static int SORT_ALPHABETICALLY = 1;


    private String mName;
    private Date mDate;
    private Time mTime;
    private int mPriority;
    private boolean isDone;
    private String mDescription;
    private String mCategory;
    private boolean mHasCustomColor;
    private int mColor;
    private String mAttendeeName;
    private String mAttendeeMail;
    private boolean mNotify;
    private long mTaskID;
    private boolean mWillRepeat;
    private String mRepeat;
    private String mRepeatUntil;
    private long mSqlId;
    private Date mCompletionDate;


    /**
     * Used with NewTaskActivity or EditTaskActivity
     */
    public Task(String newName, Date newDate, Time newTime, int newPriority, String text, String category, String attendeeName, String attendeeMail, boolean notif, String repeat, String until) {
        mName = newName;
        mDate = newDate;
        mTime = newTime;
        mPriority = newPriority;
        mDescription = text;
        mCategory = category;
        mHasCustomColor = false;
        mAttendeeName = attendeeName;
        mAttendeeMail = attendeeMail;
        mNotify = notif;
        mWillRepeat = !repeat.isEmpty();
        mRepeat = repeat;
        mRepeatUntil = until;
        mTaskID=-1;
        mSqlId = -1;
        mCompletionDate = new Date(0);
    }

    /**
     * Used while parsing events with CalendarUtility
     */
    public Task(String title,Date dtstart, Time timestart, String description, String attendeeName, String attendeeMail, long id) {
        mName = title;
        mDate = dtstart;
        mTime = timestart;
        mPriority = PRIORITY_NORMAL;
        if(description!=null)
            mDescription = description;
        else
            mDescription ="";
        mCategory = "";
        mHasCustomColor = false;
        if(attendeeName!=null)
            mAttendeeName = attendeeName;
        else
            mAttendeeName = "";
        if(attendeeMail!=null)
            mAttendeeMail = attendeeMail;
        else
            mAttendeeMail = "";
        mNotify = true;
        mRepeat = "";
        mRepeatUntil = "";
        mWillRepeat = !mRepeat.isEmpty();
        mTaskID=id;
        mSqlId = -1;
        mCompletionDate = new Date(0);
    }


    /**
     * Used while loading entries from sql
     */
    public Task(String title, Date date, Time time, int priority, boolean isdone, Date completionDate ,String description, String category, boolean hascustomcolor, int color, String attendeeName, String attendeeMail,
                boolean notify, long taskid, boolean willrepeat, String repeat, String repeatuntil, long sqlid) {
        mName = title;
        mDate = date;
        mTime = time;
        mPriority = priority;
        isDone = isdone;
        mCompletionDate = completionDate;
        mDescription = description;
        mCategory = category;
        mHasCustomColor = hascustomcolor;
        mColor = color;
        mAttendeeName = attendeeName;
        mAttendeeMail = attendeeMail;
        mNotify = notify;
        mTaskID = taskid;
        mWillRepeat = willrepeat;
        mRepeat = repeat;
        mRepeatUntil = repeatuntil;
        mSqlId = sqlid;
    }



    public void setSqlId(long id) {
        mSqlId = id;
    }

    public void setCustomColor(int color) {
        mColor = color;
        mHasCustomColor = true;
    }

    public void setTime(Time time) {
        mTime = time;
    }
    public void setDate(Date date) {
        mDate = date;
    }


    public void clearCustomColor() {
        mHasCustomColor = false;
    }

    public void setTaskId(long id) {
        mTaskID = id;
    }

    public boolean isEqual(Task t) {
        if(mTaskID!=-1&&t.getTaskId()!=-1)
            return mTaskID==t.getTaskId();
        else
            return t.getName().equals(mName)&&t.getDate().equals(mDate)&&t.getDescription().equals(mDescription);
    }

    public void toggleIsDone() {
        isDone = !isDone;
        if(isDone)
            mCompletionDate = new Date();
        else
            mCompletionDate = new Date(0);
    }





    public Date getCompletionDate() {
        return mCompletionDate;
    }

    public long getSqlId() {
        return mSqlId;
    }

    public long getNotificationTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        cal.set(Calendar.HOUR_OF_DAY,(int)mTime.toHours());
        cal.set(Calendar.MINUTE,(int)mTime.toMinutes());
        return cal.getTime().getTime();
    }

    public int getColor() {
        return mColor;
    }

    public boolean willRepeat() {
        return mWillRepeat;
    }

    public String getRepeat() {
        return mRepeat;
    }

    public Date[] getRepeatDates() {
        Date[] res;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        switch (mRepeat) {
            case "Chaque semaine":
                switch(mRepeatUntil) {
                    case "Pendant un mois":
                        res= new Date[4];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.WEEK_OF_YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    case "Pendant 6 mois":
                        res= new Date[24];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.WEEK_OF_YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    case "Pendant 1 an":
                        res= new Date[52];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.WEEK_OF_YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    default:
                        res= new Date[52*5];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.WEEK_OF_YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                }
                break;
            case "Chaque mois":
                switch(mRepeatUntil) {
                    case "Pendant un mois":
                        res= new Date[1];
                        for(int i=0;i<res.length;i++) {
                            cal.add(Calendar.MONTH,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    case "Pendant 6 mois":
                        res= new Date[6];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.MONTH,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    case "Pendant 1 an":
                        res= new Date[12];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.MONTH,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    default:
                        res= new Date[60];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.MONTH,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                }
                break;
            default:
                switch(mRepeatUntil) {
                    case "Pendant un mois":
                        res = new Date[0];
                        break;
                    case "Pendant 6 mois":
                        res = new Date[0];
                        break;
                    case "Pendant 1 an":
                        res= new Date[2];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                    default:
                        res= new Date[6];
                        res[0] = new Date(cal.getTime().getTime());
                        for(int i=1;i<res.length;i++) {
                            cal.add(Calendar.YEAR,1);
                            res[i]=new Date(cal.getTime().getTime());
                        }
                        break;
                }
                break;
        }

        return res;
    }

    public boolean isDateInRepeatDates(Date t) {
        Date [] dates = getRepeatDates();
        for(Date d : dates) {
            if(d.isEqual(t))
                return true;
        }
        return false;
    }

    public String getRepeatUntil() {
        return mRepeatUntil;
    }

    public long getTaskId() {
        return mTaskID;
    }

    public String getName() {
        return mName;
    }

    public int getPriority() {
        return mPriority;
    }

    public Date getDate() {
        return mDate;
    }

    public Time getTime() {
        return mTime;
    }

    public boolean isReminderEnabled() {
        return mNotify;
    }

    public String getAttendeeName() {
        return mAttendeeName;
    }

    public String getAttendeeMail() {
        return mAttendeeMail;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCategory() {
        return mCategory;
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean isDateSet() {
        return !mDate.toString().isEmpty();
    }

    public boolean isTimeSet() {
        return !mTime.toString().isEmpty();
    }

    public boolean hasContent() {
        return !mDescription.isEmpty();
    }

    public boolean hasCustomColor() {
        return mHasCustomColor;
    }

    public boolean hasAttendee() {
        return !mAttendeeName.isEmpty();
    }

    public boolean hasCategory() {
        return !mCategory.isEmpty();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isAfter(Task t, int sortType) {
        if (sortType == SORT_DATE) {
            if (mDate.toDays() > t.getDate().toDays()) {
                return true;
            } else if (mDate.toDays() == t.getDate().toDays()) {
                if (mTime.getTime() > t.getTime().getTime()) {
                    return true;
                } else {
                    return mTime.getTime() == t.getTime().getTime() && mPriority < t.getPriority();
                }
            } else {
                return false;
            }
        } else {    // sortType == SORT_ALPHABETICALLY
            if (mPriority > t.getPriority()) {
                return false;
            } else if (mPriority == t.getPriority()) {
                int comp = mName.toLowerCase().compareTo(t.getName().toLowerCase());
                return comp > 0; // comp>0 : t.getName() before mName
            } else  // mPriority < t.getPriority()
                return true;
        }

    }

}
