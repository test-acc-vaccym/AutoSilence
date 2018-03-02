package edu.project.app.autosilence;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sanket on 19-02-2018.
 *
 */

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationListViewHolder> {

    private ArrayList<AutoSilenceLocation> locations;
    private Context context;

    LocationListAdapter(Context context, ArrayList<AutoSilenceLocation> locations) {
        this.locations = locations;
        this.context = context;
    }

    @Override
    public LocationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_list_item, parent, false);
        return new LocationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationListViewHolder holder, int position) {
        holder.listName.setText(locations.get(position).getName());
        holder.listLat.setText(String.valueOf(locations.get(position).getLat()));
        holder.listLng.setText(String.valueOf(locations.get(position).getLng()));
        holder.listRad.setText(String.valueOf(locations.get(position).getRadius()));
        holder.listAdd.setText(locations.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class LocationListViewHolder extends RecyclerView.ViewHolder {

        TextView listName, listRad, listLat, listLng, listAdd;
        LocationListViewHolder(View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.listItemName);
            listLat = itemView.findViewById(R.id.listItemLat);
            listLng = itemView.findViewById(R.id.listItemLng);
            listRad = itemView.findViewById(R.id.listItemRadius);
            listAdd = itemView.findViewById(R.id.listItemAddress);
        }
    }
}