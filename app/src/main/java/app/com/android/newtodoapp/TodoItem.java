package app.com.android.newtodoapp;

import java.util.List;


/**
 * Created by Asus1 on 6/14/2016.
 */
public class TodoItem {

    protected int id;
    protected String todoTitle;
    protected String date;
    protected String location;
    protected List<CheckBoxItem> actionList;
    protected String time;
    protected boolean isExpired;
    protected boolean isDone;

    public boolean isExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<CheckBoxItem> getActionList() {
        return actionList;
    }

    public void setActionList(List<CheckBoxItem> actionList) {
        this.actionList = actionList;
    }

    public String getTodoTitle() {
        return todoTitle;
    }

    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
