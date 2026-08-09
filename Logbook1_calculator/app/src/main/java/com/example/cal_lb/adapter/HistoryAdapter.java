package com.example.cal_lb.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cal_lb.R;
import com.example.cal_lb.databinding.ItemHistoryBinding;
import com.example.cal_lb.model.HistoryItem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<HistoryItem, HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private final OnItemClickListener listener;
    private static final DecimalFormat scientificFormat = new DecimalFormat("0.########E0", DecimalFormatSymbols.getInstance(Locale.US));

    public HistoryAdapter(OnItemClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = getItem(position);
        holder.bind(item, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;

        public ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HistoryItem item, OnItemClickListener listener) {
            binding.tvHistoryEquation.setText(formatHistoryText(item.getEquation()));
            binding.tvHistoryResult.setText(binding.getRoot().getContext().getString(
                    R.string.history_result_format, formatHistoryText(item.getResult())));
            
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }

        private String formatHistoryText(String text) {
            if (text == null || text.isEmpty()) return "";
            
            String[] tokens = text.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String token : tokens) {
                if (token.matches("-?\\d+(\\.\\d+)?([eE]-?\\d+)?")) {
                    try {
                        BigDecimal val = new BigDecimal(token);
                        String plain = val.stripTrailingZeros().toPlainString();
                        if (plain.length() > 10 || val.abs().compareTo(new BigDecimal("10000000000")) >= 0 || 
                           (val.abs().compareTo(new BigDecimal("0.0000001")) < 0 && val.compareTo(BigDecimal.ZERO) != 0)) {
                            sb.append(scientificFormat.format(val.doubleValue()));
                        } else {
                            sb.append(plain);
                        }
                    } catch (Exception e) {
                        sb.append(token);
                    }
                } else {
                    sb.append(token);
                }
                sb.append(" ");
            }
            return sb.toString().trim();
        }
    }

    public static class DiffCallback extends DiffUtil.ItemCallback<HistoryItem> {
        @Override
        public boolean areItemsTheSame(@NonNull HistoryItem oldItem, @NonNull HistoryItem newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull HistoryItem oldItem, @NonNull HistoryItem newItem) {
            return oldItem.getEquation().equals(newItem.getEquation()) &&
                    oldItem.getResult().equals(newItem.getResult());
        }
    }
}