package com.rishabh.gride.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rishabh.gride.R;
import com.rishabh.gride.models.Ride;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.VH> {

    private final List<Ride> rides;
    private final OnAcceptClick listener;

    // 🔥 Interface for Accept button click
    public interface OnAcceptClick {
        void onAccept(Ride ride);
    }

    // 🔧 Constructor
    public RideAdapter(List<Ride> rides, OnAcceptClick listener) {
        this.rides = rides;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Ride r = rides.get(position);

        //  Bind data
        h.tvPickup.setText("Pickup: " + r.pickup_address);
        h.tvDrop.setText("Drop: " + r.drop_address);
        h.tvFare.setText("Fare: ₹" + r.fare);

        if (position == 0) {
            h.itemView.setBackgroundColor(0xFFE8F5E9); // light green
        } else {
            h.itemView.setBackgroundColor(0xFFFFFFFF); // white
        }

        //  Accept button click
        h.btnAccept.setOnClickListener(v -> {
            Log.d("DRIVER", "Accept button clicked for ride: " + r.id);
            if (listener != null) {
                listener.onAccept(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    // 🔧 ViewHolder
    static class VH extends RecyclerView.ViewHolder {

        TextView tvPickup, tvDrop, tvFare;
        Button btnAccept;

        public VH(@NonNull View itemView) {
            super(itemView);

            tvPickup = itemView.findViewById(R.id.tvPickup);
            tvDrop = itemView.findViewById(R.id.tvDrop);
            tvFare = itemView.findViewById(R.id.tvFare);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}