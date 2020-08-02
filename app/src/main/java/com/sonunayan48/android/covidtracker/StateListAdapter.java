package com.sonunayan48.android.covidtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class StateListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_DATA_CODE = 978;
    private static final int ITEM_AD_CODE = 979;
    private final ListItemClickListner mListener;
    private final int animFile;
    private ArrayList<Object> stateList;
    private Context context;

    StateListAdapter(ArrayList<Object> arrayList, ListItemClickListner listner, int file) {
        stateList = arrayList;
        mListener = listner;
        animFile = file;
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0 && StateActivity.toShowAds == 1 && position % StateActivity.ADS_PERIOD == 0) {
            return ITEM_AD_CODE;
        }
        return ITEM_DATA_CODE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        switch (viewType) {
            case ITEM_AD_CODE:
                int layoutIdForAdList = R.layout.banner_ad_template;
                View adView = inflater.inflate(layoutIdForAdList, parent, shouldAttachToParentImmediately);
                return new AdViewHolder(adView);
            default:
            case ITEM_DATA_CODE:
                int layoutIdForStateList = R.layout.state_list;
                View view = inflater.inflate(layoutIdForStateList, parent, shouldAttachToParentImmediately);
                return new StateViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_AD_CODE:
                if (stateList.get(position) instanceof AdView) {
                    AdViewHolder adViewHolder = (AdViewHolder) holder;
                    adViewHolder.itemView.setAnimation(AnimationUtils.loadAnimation(context, animFile));
                    AdView adView = (AdView) stateList.get(position);
                    ViewGroup adCardView = (ViewGroup) adViewHolder.itemView;
                    if (adCardView.getChildCount() > 0) {
                        adCardView.removeAllViews();
                    }
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    adCardView.addView(adView);
                }
                break;
            default:
            case ITEM_DATA_CODE:
                if (stateList.get(position) instanceof StateClass) {
                    StateViewHolder mHolder = (StateViewHolder) holder;
                    mHolder.mParentLayout.setAnimation(AnimationUtils.loadAnimation(context, animFile));
                    mHolder.bind(position);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return stateList.size();
    }

    public interface ListItemClickListner {
        void onListClick(int itemIndex);
    }

    //ViewHolder for the state/district data
    class StateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView stateName;
        private TextView activeCases;
        private RelativeLayout mLayout;
        private LinearLayout mParentLayout;

        public StateViewHolder(@NonNull View itemView) {
            super(itemView);

            stateName = itemView.findViewById(R.id.state_name);
            activeCases = itemView.findViewById(R.id.active);
            mLayout = itemView.findViewById(R.id.layout);
            mLayout.setOnClickListener(this);
            mParentLayout = itemView.findViewById(R.id.parent_layout);
        }

        private void bind(int position) {
            Object ob = stateList.get(position);
            StateClass state = (StateClass) ob;
            String name = state.getmName();
            stateName.setText(name);
            String activeCase = context.getString(R.string.active_rv, state.getmActive());
            activeCases.setText(activeCase);
        }

        @Override
        public void onClick(View v) {
            int clickedItemPosition = getAdapterPosition();
            mListener.onListClick(clickedItemPosition);
        }
    }

    class AdViewHolder extends RecyclerView.ViewHolder {

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
