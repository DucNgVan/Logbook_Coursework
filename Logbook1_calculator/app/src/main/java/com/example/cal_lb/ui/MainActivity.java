package com.example.cal_lb.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cal_lb.adapter.HistoryAdapter;
import com.example.cal_lb.databinding.ActivityMainBinding;
import com.example.cal_lb.model.HistoryItem;
import com.example.cal_lb.viewmodel.CalculatorViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements HistoryAdapter.OnItemClickListener {

    private ActivityMainBinding binding;
    private CalculatorViewModel viewModel;
    private HistoryAdapter historyAdapter;
    private boolean showFullNumber = false;

    private final DecimalFormat scientificFormat = new DecimalFormat("0.########E0", DecimalFormatSymbols.getInstance(Locale.US));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        setupHistory();
        setupKeypad();
        observeViewModel();

        binding.tvResult.setOnClickListener(v -> {
            String text = binding.tvResult.getText().toString();
            if (text.contains("E") || text.contains("e") || isAnyPartLong(text)) {
                showFullNumber = !showFullNumber;
                updateResultDisplay(viewModel.result.getValue());
            }
        });

        binding.btnClearHistory.setOnClickListener(v -> viewModel.clearHistory());
    }

    private boolean isAnyPartLong(String text) {
        String[] parts = text.split(" ");
        for (String part : parts) {
            if (part.contains("E") || part.contains("e")) return true;
            if (part.length() > 10 && part.matches("-?\\d+(\\.\\d+)?")) return true;
        }
        return false;
    }

    private void setupHistory() {
        historyAdapter = new HistoryAdapter(this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(historyAdapter);
    }

    @Override
    public void onItemClick(HistoryItem item) {
        viewModel.recallHistory(item);
        showFullNumber = false;
    }

    private void setupKeypad() {
        for (int i = 0; i < binding.keypad.getChildCount(); i++) {
            View child = binding.keypad.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setOnClickListener(v -> viewModel.onAction(button.getText().toString()));
            }
        }
    }

    private void observeViewModel() {
        viewModel.equation.observe(this, eq -> {
            binding.tvEquation.setText(eq);
            autoScrollToBottom(binding.svEquation);
        });

        viewModel.result.observe(this, res -> {
            updateResultDisplay(res);
            autoScrollToBottom(binding.svResult);
        });

        viewModel.history.observe(this, history -> {
            boolean isEmpty = (history == null || history.isEmpty());
            binding.tvEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            
            historyAdapter.submitList(history, () -> {
                if (!isEmpty) {
                    binding.rvHistory.scrollToPosition(0);
                }
            });
        });
    }

    private void updateResultDisplay(String res) {
        if (res == null) return;

        String[] parts = res.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.matches("-?\\d+(\\.\\d+)?([eE]-?\\d+)?")) {
                try {
                    BigDecimal val = new BigDecimal(part);
                    String plain = val.stripTrailingZeros().toPlainString();
                    if (showFullNumber) {
                        sb.append(plain);
                    } else if (plain.length() > 10 || val.abs().compareTo(new BigDecimal("10000000000")) >= 0 || 
                       (val.abs().compareTo(new BigDecimal("0.0000001")) < 0 && val.compareTo(BigDecimal.ZERO) != 0)) {
                        sb.append(scientificFormat.format(val.doubleValue()));
                    } else {
                        sb.append(plain);
                    }
                } catch (Exception e) {
                    sb.append(part);
                }
            } else {
                sb.append(part);
            }
            sb.append(" ");
        }
        binding.tvResult.setText(sb.toString().trim());
    }

    private void autoScrollToBottom(final View scrollContainer) {
        scrollContainer.post(() -> {
            if (scrollContainer instanceof android.widget.HorizontalScrollView) {
                ((android.widget.HorizontalScrollView) scrollContainer).fullScroll(View.FOCUS_RIGHT);
            }
        });
    }
}