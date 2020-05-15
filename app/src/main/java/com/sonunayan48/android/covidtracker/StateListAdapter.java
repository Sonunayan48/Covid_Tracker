package com.sonunayan48.android.covidtracker;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StateListAdapter extends RecyclerView.Adapter<StateListAdapter.StateViewHolder> {

    private ArrayList<StateClass> stateList;

    StateListAdapter(ArrayList<StateClass> arrayList){
        stateList = arrayList;
    }
    @NonNull
    @Override
    public StateListAdapter.StateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForStateList = R.layout.state_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForStateList, parent, shouldAttachToParentImmediately);
        return new StateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateListAdapter.StateViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return stateList.size();
    }

    class StateViewHolder extends RecyclerView.ViewHolder {

        private TextView stateName;
        private TextView activeCases;
        public StateViewHolder(@NonNull View itemView) {
            super(itemView);

            stateName = itemView.findViewById(R.id.state_name);
            activeCases = itemView.findViewById(R.id.active);
        }

        private void bind(int position){
            StateClass state = stateList.get(position);
            String name = state.getmName();
            stateName.setText(name);
            activeCases.setText("Active: "+state.getmActive());
        }
    }
}
