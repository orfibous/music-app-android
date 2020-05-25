package edu.aueb.cs.distributedsystems;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class TextAdapterAuthor extends BaseAdapter{
    private int item;
    private int text;

    public TextAdapterAuthor(int item, int text){
        this.item = item;
        this.text = text;
    }

    private List<String> data = new ArrayList<>();

    void setData(List<String> mData){
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public String getItem(int position){
        return null;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(item, parent, false);
            convertView.setTag(new ViewHolder((TextView) convertView.findViewById(text)));

        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        final  String item = data.get(position);
        holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
        return convertView;
    }

    class ViewHolder{
        TextView info;

        ViewHolder(TextView mInfo){
            info = mInfo;
        }
    }
}