package app.com.android.newtodoapp;


import android.content.Context;
import android.view.View;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;

/**
 * Created by Asus1 on 6/15/2016.
 */
public class TodoCard extends CardWithList {


    private TodoItem mTodoItemHeader;
    private int mListId;

    public TodoCard(Context context, int listId, TodoItem todoItemHeader) {
        super(context);
        mTodoItemHeader = todoItemHeader;
        mListId = listId;
    }


    public int getCardListId(){
        return mListId;
    }

    @Override
    protected CardHeader initCardHeader() {
        return new CardHeader(getContext(), R.layout.todo_item_layout){
            @Override
            public void setupInnerViewElements(ViewGroup parent, View view) {
                TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
                String emptyString = view.getResources().getString(R.string.not_specified);
                TextView locationTv = (TextView)view.findViewById(R.id.location_tv);
                TextView dateTv = (TextView)view.findViewById(R.id.date_tv);
                if(locationTv!=null){
                    String locationText = Utils.isNullOrEmpty(mTodoItemHeader.getLocation())? emptyString:mTodoItemHeader.getLocation();
                    locationTv.setText(locationText);
                }
                if(dateTv!=null){
                    String dateText = Utils.isNullOrEmpty(mTodoItemHeader.getDate())? emptyString:mTodoItemHeader.getDate();
                    dateTv.setText(dateText);
                }
                if(titleTv!=null){
                    String titleText = Utils.isNullOrEmpty(mTodoItemHeader.getTodoTitle())? emptyString:mTodoItemHeader.getTodoTitle();
                    titleTv.setText(titleText);

                    if(mTodoItemHeader.isDone()){
                        titleTv.setBackgroundColor(getContext().getResources().getColor(R.color.header_background_done));
                    }else if(mTodoItemHeader.isExpired()){
                        titleTv.setBackgroundColor(getContext().getResources().getColor(R.color.header_background_expired));
                    }else{
                        titleTv.setBackgroundColor(getContext().getResources().getColor(R.color.header_background_normal));
                    }
                }

            }
        };
    }

    @Override
    protected void initCard() {
        setSwipeable(true);
        setId(String.valueOf(mListId));
    }

    @Override
    protected List<ListObject> initChildren() {
        List<ListObject> mObjectList = new ArrayList<>();
        for(CheckBoxItem checkBoxItem:mTodoItemHeader.getActionList()){
            ActionItem actionItem = new ActionItem(this);
            actionItem.setIsSelected(checkBoxItem.isSelected());
            actionItem.setActionItem(checkBoxItem.getActionItem());
            mObjectList.add(actionItem);
        }
        return mObjectList;
    }

    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {
        TextView itemTv = (TextView)convertView.findViewById(R.id.td_text);
        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.td_checkbox);
        itemTv.setText(((ActionItem)object).getActionItem());
        checkBox.setChecked(((ActionItem)object).isSelected);
        return convertView;
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.checkbox_item;
    }

    public class ActionItem extends CardWithList.DefaultListObject{

        private String actionItem;
        private boolean isSelected;

        public ActionItem(Card parentCard) {
            super(parentCard);
        }

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
    }

}