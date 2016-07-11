package app.com.android.newtodoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.Collection;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author : Koday
 */
public class RowItemAdapter extends ArrayAdapter<CheckBoxItem> {

    private Context mContext;
    private List<CheckBoxItem> mItemList;
    private ViewHolder mViewHolder;
    private ImageView mDeleteImage;
    ViewGroup mParent;

    public RowItemAdapter(Context context, List<CheckBoxItem> rowItemList) {
        super(context, R.layout.row_item_layout, rowItemList);
        this.mContext = context;
        this.mItemList = rowItemList;
    }

    static class ViewHolder {
        @BindView(R.id.todo_check)CheckBox itemCheck;
        @BindView(R.id.todo_text)EditText itemText;
        @BindView(R.id.delete_item)ImageView deleteImage;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
        @Override
        public boolean equals(Object o){

            return  (o == this) ||
                    ((o instanceof ViewHolder) &&
                            ((ViewHolder)o).itemText.getText().toString().equals(this.itemText.getText().toString()) &&
                            ((ViewHolder)o).itemCheck.isChecked() == this.itemCheck.isChecked());
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mParent = parent;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_item_layout, parent, false);
            mViewHolder = new ViewHolder(view);
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }

        mViewHolder.itemText.setText(mItemList.get(position).getActionItem());
        mViewHolder.itemCheck.setChecked(mItemList.get(position).isSelected());
        mViewHolder.deleteImage.setTag(position);
        mViewHolder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                CheckBoxItem item = getItem(position);
                remove(item);
                notifyDataSetChanged();
            }
        });
        mViewHolder.itemText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RelativeLayout layout = (RelativeLayout) v.getParent();
                    mDeleteImage = (ImageView) layout.findViewById(R.id.delete_item);
                    mDeleteImage.setVisibility(View.VISIBLE);
                }
                else
                    mDeleteImage.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

}
