package com.thanasis.firespeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpeedPointListAdapter extends ArrayAdapter<SpeedPoint> {
    private Context mContext;
    private int mResource;

    static class ViewHolder {
        TextView speed;
        TextView timestamp;
        TextView lat;
        TextView lng;
    }

    public SpeedPointListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<SpeedPoint> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String speed = getItem(position).getSpeed();
        String timestamp = getItem(position).getTimestamp();
        double lat = getItem(position).getLat();
        double lng = getItem(position).getLng();

        SpeedPoint point = new SpeedPoint(speed, timestamp, lat, lng);

        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.speed = convertView.findViewById(R.id.tv_speed);
            holder.timestamp = convertView.findViewById(R.id.tv_timestamp);
            holder.lat = convertView.findViewById(R.id.tv_lat);
            holder.lng = convertView.findViewById(R.id.tv_lng);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.speed.setText("Speed: "+speed);
        holder.timestamp.setText("Timestamp: "+timestamp);
        holder.lat.setText("Latitude: "+lat);
        holder.lng.setText("Longitude: "+lng);

        return convertView;
    }
}
