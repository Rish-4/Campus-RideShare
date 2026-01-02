package com.rishabh.gride.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rishabh.gride.R;
import com.rishabh.gride.models.Ride;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.VH> {

    private final List<Ride> rides;

    public RideAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Ride r = rides.get(i);
        h.tvRoute.setText("Pickup → Drop");
        h.tvFare.setText("₹" + r.fare);
        h.tvStatus.setText(r.status);
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoute, tvFare, tvStatus;
        VH(View v) {
            super(v);
            tvRoute = v.findViewById(R.id.tvRoute);
            tvFare  = v.findViewById(R.id.tvFare);
            tvStatus= v.findViewById(R.id.tvStatus);
        }
    }
}
