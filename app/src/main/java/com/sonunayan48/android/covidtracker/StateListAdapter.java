package com.sonunayan48.android.covidtracker;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StateListAdapter extends RecyclerView.Adapter<StateListAdapter.StateViewHolder> {

    private final ListItemClickListner mListener;
    private ArrayList<StateClass> stateList;

    StateListAdapter(ArrayList<StateClass> arrayList, ListItemClickListner listner) {
        stateList = arrayList;
        mListener = listner;
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

    public interface ListItemClickListner {
        void onListClick(int itemIndex);
    }

    class StateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView stateName;
        private TextView activeCases;
        private RelativeLayout mLayout;

        public StateViewHolder(@NonNull View itemView) {
            super(itemView);

            stateName = itemView.findViewById(R.id.state_name);
            activeCases = itemView.findViewById(R.id.active);
            mLayout = itemView.findViewById(R.id.layout);
            mLayout.setOnClickListener(this);
        }

        private void bind(int position) {
            StateClass state = stateList.get(position);
            String name = state.getmName();
            stateName.setText(name);
            activeCases.setText("Active: " + state.getmActive());
        }

        @Override
        public void onClick(View v) {
            int clickedItemPosition = getAdapterPosition();
            mListener.onListClick(clickedItemPosition);
        }
    }
}
