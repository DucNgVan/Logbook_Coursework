package com.example.cal_lb.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cal_lb.model.HistoryItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatorViewModel extends ViewModel {
    private final MutableLiveData<String> _equation = new MutableLiveData<>("");
    public final LiveData<String> equation = _equation;

    private final MutableLiveData<String> _result = new MutableLiveData<>("0");
    public final LiveData<String> result = _result;

    private final MutableLiveData<List<HistoryItem>> _history = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<HistoryItem>> history = _history;

    private final List<String> tokens = new ArrayList<>();
    private String currentOperand = "";
    private String lastResult = "0";
    private boolean hasJustCalculated = false;

    private final DecimalFormat scientificFormat = new DecimalFormat("0.########E0", DecimalFormatSymbols.getInstance(Locale.US));

    public void onAction(String action) {
        switch (action) {
            case "AC":
                clearAll();
                break;
            case "+/-":
                toggleSign();
                break;
            case "%":
                applyPercentage();
                break;
            case "÷":
            case "×":
            case "-":
            case "+":
                handleOperator(action);
                break;
            case "=":
                calculateResult();
                break;
            case ".":
                appendDot();
                break;
            case "⌫":
                handleBackspace();
                break;
            default:
                if (action.matches("[0-9]")) {
                    appendDigit(action);
                }
                break;
        }
    }

    private void clearAll() {
        tokens.clear();
        currentOperand = "";
        lastResult = "0";
        hasJustCalculated = false;
        _equation.setValue("");
        _result.setValue("0");
    }

    public void clearHistory() {
        _history.setValue(new ArrayList<>());
    }

    private void appendDigit(String digit) {
        if (hasJustCalculated) {
            tokens.clear();
            currentOperand = digit;
            hasJustCalculated = false;
        } else {
            if (currentOperand.equals("0")) {
                currentOperand = digit;
            } else {
                currentOperand += digit;
            }
        }
        updateDisplay();
    }

    private void appendDot() {
        if (hasJustCalculated) {
            tokens.clear();
            currentOperand = "0.";
            hasJustCalculated = false;
        } else {
            if (currentOperand.isEmpty()) {
                currentOperand = "0.";
            } else if (!currentOperand.contains(".")) {
                currentOperand += ".";
            }
        }
        updateDisplay();
    }

    private void toggleSign() {
        if (!currentOperand.isEmpty()) {
            if (currentOperand.startsWith("-")) {
                currentOperand = currentOperand.substring(1);
            } else {
                currentOperand = "-" + currentOperand;
            }
        } else if (hasJustCalculated) {
            try {
                BigDecimal val = parseNumber(lastResult).negate();
                lastResult = formatNumber(val);
                _result.setValue(lastResult);
            } catch (Exception ignored) {}
        }
        updateDisplay();
    }

    private void applyPercentage() {
        if (!currentOperand.isEmpty()) {
            try {
                BigDecimal val = parseNumber(currentOperand);
                BigDecimal pct = val.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                currentOperand = formatNumber(pct);
            } catch (Exception ignored) {}
        } else if (hasJustCalculated) {
            try {
                BigDecimal val = parseNumber(lastResult);
                BigDecimal pct = val.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                lastResult = formatNumber(pct);
                _result.setValue(lastResult);
            } catch (Exception ignored) {}
        }
        updateDisplay();
    }

    private void handleOperator(String operator) {
        if (hasJustCalculated) {
            tokens.clear();
            tokens.add(lastResult);
            tokens.add(operator);
            currentOperand = "";
            hasJustCalculated = false;
        } else if (!currentOperand.isEmpty()) {
            tokens.add(currentOperand);
            tokens.add(operator);
            currentOperand = "";
        } else if (!tokens.isEmpty()) {
            // Replace last operator
            tokens.set(tokens.size() - 1, operator);
        } else {
            // Start with 0 if empty
            tokens.add("0");
            tokens.add(operator);
        }
        updateDisplay();
    }

    private void handleBackspace() {
        if (hasJustCalculated) {
            hasJustCalculated = false;
            _equation.setValue("");
            return;
        }

        if (!currentOperand.isEmpty()) {
            currentOperand = currentOperand.substring(0, currentOperand.length() - 1);
            if (currentOperand.equals("-")) {
                currentOperand = "";
            }
        } else if (!tokens.isEmpty()) {
            // Remove last operator or number from tokens
            String removed = tokens.remove(tokens.size() - 1);
            if (isNumber(removed)) {
                currentOperand = removed;
            }
        }
        updateDisplay();
    }

    private void calculateResult() {
        List<String> evalTokens = buildEvalTokens();
        if (evalTokens.isEmpty()) return;

        String fullExpr = buildExpressionString(evalTokens);
        String evalResult = evaluateTokens(evalTokens);

        if ("Error".equals(evalResult)) {
            _equation.setValue(fullExpr + " =");
            _result.setValue("Error");
            hasJustCalculated = true;
            return;
        }

        lastResult = evalResult;

        // Save to History
        List<HistoryItem> currentHistory = new ArrayList<>(_history.getValue());
        currentHistory.add(0, new HistoryItem(fullExpr, evalResult));
        _history.setValue(currentHistory);

        _equation.setValue(fullExpr + " =");
        _result.setValue(evalResult);

        tokens.clear();
        currentOperand = "";
        hasJustCalculated = true;
    }

    private void updateDisplay() {
        if (hasJustCalculated) return;

        List<String> currentTokens = buildEvalTokens();
        if (currentTokens.isEmpty()) {
            _equation.setValue("");
            _result.setValue("0");
            return;
        }

        String fullExpr = buildExpressionString(tokens) + (currentOperand.isEmpty() ? "" : (tokens.isEmpty() ? "" : " ") + currentOperand);
        _equation.setValue(fullExpr);

        // Preview live result
        String liveRes = evaluateTokens(currentTokens);
        if (!"Error".equals(liveRes)) {
            _result.setValue(liveRes);
        } else {
            _result.setValue(currentOperand.isEmpty() ? "0" : currentOperand);
        }
    }

    private List<String> buildEvalTokens() {
        List<String> list = new ArrayList<>(tokens);
        if (!currentOperand.isEmpty()) {
            list.add(currentOperand);
        }
        // Remove trailing operators for calculation
        while (!list.isEmpty() && isOperator(list.get(list.size() - 1))) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    private String buildExpressionString(List<String> tokenList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenList.size(); i++) {
            sb.append(tokenList.get(i));
            if (i < tokenList.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String evaluateTokens(List<String> inputTokens) {
        if (inputTokens.isEmpty()) return "0";
        List<String> workTokens = new ArrayList<>(inputTokens);

        // Pass 1: Multiplication and Division
        for (int i = 0; i < workTokens.size(); i++) {
            String token = workTokens.get(i);
            if (token.equals("×") || token.equals("÷")) {
                if (i == 0 || i == workTokens.size() - 1) return "Error";
                try {
                    BigDecimal num1 = parseNumber(workTokens.get(i - 1));
                    BigDecimal num2 = parseNumber(workTokens.get(i + 1));
                    BigDecimal res;
                    if (token.equals("×")) {
                        res = num1.multiply(num2);
                    } else {
                        if (num2.compareTo(BigDecimal.ZERO) == 0) {
                            return "Error";
                        }
                        res = num1.divide(num2, 12, RoundingMode.HALF_UP);
                    }
                    String resStr = formatNumber(res);
                    workTokens.set(i - 1, resStr);
                    workTokens.remove(i);
                    workTokens.remove(i);
                    i--; // re-check index
                } catch (Exception e) {
                    return "Error";
                }
            }
        }

        // Pass 2: Addition and Subtraction
        for (int i = 0; i < workTokens.size(); i++) {
            String token = workTokens.get(i);
            if (token.equals("+") || token.equals("-")) {
                if (i == 0 || i == workTokens.size() - 1) return "Error";
                try {
                    BigDecimal num1 = parseNumber(workTokens.get(i - 1));
                    BigDecimal num2 = parseNumber(workTokens.get(i + 1));
                    BigDecimal res;
                    if (token.equals("+")) {
                        res = num1.add(num2);
                    } else {
                        res = num1.subtract(num2);
                    }
                    String resStr = formatNumber(res);
                    workTokens.set(i - 1, resStr);
                    workTokens.remove(i);
                    workTokens.remove(i);
                    i--;
                } catch (Exception e) {
                    return "Error";
                }
            }
        }

        return workTokens.get(0);
    }

    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("×") || s.equals("÷");
    }

    private boolean isNumber(String s) {
        try {
            parseNumber(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private BigDecimal parseNumber(String s) {
        return new BigDecimal(s);
    }

    public void recallHistory(HistoryItem item) {
        tokens.clear();
        currentOperand = "";
        lastResult = item.getResult();
        hasJustCalculated = true;
        _equation.setValue(item.getEquation() + " =");
        _result.setValue(item.getResult());
    }

    private String formatNumber(BigDecimal val) {
        if (val == null) return "0";
        BigDecimal absVal = val.abs();
        String plain = val.stripTrailingZeros().toPlainString();
        if (plain.length() > 10 || absVal.compareTo(new BigDecimal("10000000000")) >= 0 || 
           (absVal.compareTo(new BigDecimal("0.0000001")) < 0 && absVal.compareTo(BigDecimal.ZERO) != 0)) {
            return scientificFormat.format(val.doubleValue());
        }
        return plain;
    }
}