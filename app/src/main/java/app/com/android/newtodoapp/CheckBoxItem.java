package app.com.android.newtodoapp;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Asus1 on 6/16/2016.
 */
public class CheckBoxItem implements Parcelable{

    private String actionItem;
    private boolean isSelected;

    public CheckBoxItem(){

    }

    public CheckBoxItem(String str){
        this.actionItem = str;
        this.isSelected = false;
    }
    public CheckBoxItem(String str,boolean isSelected){
        this.actionItem = str;
        this.isSelected = isSelected;
    }

    protected CheckBoxItem(Parcel in) {
        actionItem = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<CheckBoxItem> CREATOR = new Creator<CheckBoxItem>() {
        @Override
        public CheckBoxItem createFromParcel(Parcel in) {
            return new CheckBoxItem(in);
        }

        @Override
        public CheckBoxItem[] newArray(int size) {
            return new CheckBoxItem[size];
        }
    };

    public String getActionItem() {
        return actionItem;
    }

    public void setActionItem(String actionItem) {
        this.actionItem = actionItem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(actionItem);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
