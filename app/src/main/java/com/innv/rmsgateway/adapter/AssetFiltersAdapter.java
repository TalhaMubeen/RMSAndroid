package com.innv.rmsgateway.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.activity.AssetsActivity;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;

import java.util.ArrayList;
import java.util.List;

public class AssetFiltersAdapter extends RecyclerView.Adapter<AssetFiltersAdapter.MyViewHolder> {
    Context _context;
    AssetsActivity assetsActivity;
    LayoutInflater mInflater;
    List<MyViewHolder> viewList = new ArrayList<>();
    boolean showOne = false;

    public int getSelectedFilterPosition() {
        return selectedFilterPosition;
    }

    public void setSelectedFilterPosition(int selectedFilterPosition) {
        this.selectedFilterPosition = selectedFilterPosition;
        showOne = true;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    int selectedFilterPosition = 0;
    String selectedText;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilterText;
        View viewFilterColor;
        LinearLayout selected_filter;
        CardView filter;

        MyViewHolder(View itemView) {
            super(itemView);
            this.viewFilterColor = (View) itemView.findViewById(R.id.viewFilterColor);
            this.tvFilterText = (TextView) itemView.findViewById(R.id.tvFilterText);
            this.selected_filter = (LinearLayout) itemView.findViewById(R.id.selected_filter);
            this.filter = (CardView) itemView.findViewById(R.id.filter);

            this.filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!showOne) {

                        for (AssetFiltersAdapter.MyViewHolder holder : viewList) {
                            LinearLayout selection = holder.selected_filter;
                            selection.setBackgroundColor(ContextCompat.getColor(_context, R.color.card_background_color_lite));
                            selection.setVisibility(View.INVISIBLE);
                        }

                        selectedFilterPosition = getAdapterPosition();
                        selected_filter.setVisibility(View.VISIBLE);
                        selected_filter.setBackgroundColor(ContextCompat.getColor(_context, R.color.colorPrimary));
                        selectedText = tvFilterText.getText().toString();
                        assetsActivity.scrollToSelection(selectedFilterPosition, selectedText);
                        assetsActivity.updateData();
                    }
                }
            });
        }
    }

    public AssetFiltersAdapter(Context ctx) {
        _context = ctx;
        assetsActivity = (AssetsActivity)_context;
        this.mInflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public AssetFiltersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.mInflater.inflate(R.layout.asset_filter_view, parent, false);
        MyViewHolder holder = new AssetFiltersAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(AssetFiltersAdapter.MyViewHolder holder, int position) {
        if (showOne) {
            position = selectedFilterPosition;
        }
        TextView tvFilterText = holder.tvFilterText;
        View viewFilterColor = holder.viewFilterColor;
        tvFilterText.setText(this._context.getResources().getString(Globals.AlertType[position]));
        switch (position) {
            case 0:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                break;
            case 1:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_alert)));
                break;
            case 2:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_warning)));
                break;
            case 3:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_normal)));
                break;
            case 4:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_defrost)));
                break;
            case 5:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_offline)));
                break;
            case 6:
                viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_dark_grey)));
                break;
            default:
                break;
        }

        if (position == selectedFilterPosition) {
            holder.selected_filter.setVisibility(View.VISIBLE);
            holder.selected_filter.setBackgroundColor(ContextCompat.getColor(_context, R.color.colorPrimary));
            selectedText = tvFilterText.getText().toString();
        }else{
            holder.selected_filter.setVisibility(View.INVISIBLE);
        }

        viewList.add(holder);
    }

    @Override
    public int getItemCount() {
        return showOne ? 1 : Globals.AlertType.length;
    }
}

