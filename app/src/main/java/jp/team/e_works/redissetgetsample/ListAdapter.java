package jp.team.e_works.redissetgetsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<ListItem> {
    private int mResource;
    private List<ListItem> mItems;
    private LayoutInflater mInflater;

    public ListAdapter(Context context, int resource, List<ListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }

        ListItem item = mItems.get(position);

        TextView key = (TextView) view.findViewById(R.id.list_key);
        key.setText(item.getKey());

        TextView value = (TextView) view.findViewById(R.id.list_value);
        value.setText(item.getValue());

        return view;
    }
}
