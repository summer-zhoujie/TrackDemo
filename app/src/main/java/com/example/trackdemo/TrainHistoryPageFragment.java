package com.example.trackdemo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trackdemo.bean.TrainData;
import com.example.trackdemo.db.LocalDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 运动的历史记录
 */
public class TrainHistoryPageFragment extends Fragment {

    private TextView tvTotalDistanceValue;
    private RecyclerView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_history_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        tvTotalDistanceValue = view.findViewById(R.id.tv_total_distance_value);
        list = view.findViewById(R.id.list);

        float totalDistance = LocalDBHelper.getInstance(getContext()).getTotalDistance();
        tvTotalDistanceValue.setText(totalDistance + "");
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        final Adapter adapter = new Adapter();
        list.setAdapter(adapter);
        adapter.setNewData(LocalDBHelper.getInstance(getContext()).getAllRecords());
        adapter.setOnItemClickListener((parent, view1, position, id) -> {
            TrainResultActivity.launch(getActivity(), adapter.getDatas().get(position));
        });
    }

    private static class Adapter extends RecyclerView.Adapter {

        private List<TrainData> datas = new ArrayList<>();
        private AdapterView.OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setNewData(List<TrainData> newValue) {
            datas.clear();
            if (newValue != null) {
                datas.addAll(newValue);
            }
            notifyDataSetChanged();
        }

        public List<TrainData> getDatas() {
            return datas;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            final View inflate = LayoutInflater.from(context).inflate(R.layout.train_history_item, parent, false);
            return new ViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder h = (ViewHolder) holder;
            TrainData data = datas.get(position);

            final Context context = h.ivTracker.getContext();
            Glide.with(context)
                    .load(data.url)
                    .placeholder(R.drawable.load_default)
                    .into(h.ivTracker);
            h.tvTrainType.setText(data.getTrainTypeDesc(h.tvTrainType.getContext()));
            h.tvDistance.setText(data.distance + context.getString(R.string.km));
            h.tvTime.setText(data.getTimDesc());
            h.tvKcal.setText(data.kcal + context.getString(R.string.kcal_unit));
            h.tvSpeed.setText(data.getSpeedSecondDesc() + context.getString(R.string.unit_speed));
            h.itemView.setOnClickListener(l);
            h.itemView.setTag(position);
        }

        final View.OnClickListener l = v -> {
            int position = (int) v.getTag();
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, v, position, 0);
            }
        };

        @Override
        public int getItemCount() {
            return datas.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivTracker;
            public TextView tvTrainType;
            public TextView tvDistance;
            public LinearLayout llContent;
            public TextView tvTime;
            public TextView tvKcal;
            public TextView tvSpeed;

            public ViewHolder(View view) {
                super(view);
                ivTracker = view.findViewById(R.id.iv_tracker);
                tvTrainType = view.findViewById(R.id.tv_train_type);
                tvDistance = view.findViewById(R.id.tv_distance);
                llContent = view.findViewById(R.id.ll_content);
                tvTime = view.findViewById(R.id.tv_time);
                tvKcal = view.findViewById(R.id.tv_kcal);
                tvSpeed = view.findViewById(R.id.tv_speed);
            }
        }
    }
}