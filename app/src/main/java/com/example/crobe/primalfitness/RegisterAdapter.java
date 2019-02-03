package com.example.crobe.primalfitness;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class RegisterAdapter extends ArrayAdapter<UserItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public RegisterAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final UserItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
//        final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkToDoItem);
//        checkBox.setChecked(false);
//        checkBox.setEnabled(true);
//
//        checkBox.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                if (checkBox.isChecked()) {
//                    checkBox.setEnabled(false);
//                    if (mContext instanceof RegisterActivity) {
//                        RegisterActivity activity = (RegisterActivity) mContext;
//                        activity.checkItem(currentItem);
//                    }
//                }
//            }
//        });

        return row;
    }

}