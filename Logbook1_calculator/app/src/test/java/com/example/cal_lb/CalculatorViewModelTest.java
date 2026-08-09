package com.example.cal_lb;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.cal_lb.viewmodel.CalculatorViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class CalculatorViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private CalculatorViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new CalculatorViewModel();
    }

    @Test
    public void testSingleOperation() {
        viewModel.onAction("1");
        viewModel.onAction("5");
        viewModel.onAction("+");
        viewModel.onAction("2");
        viewModel.onAction("5");
        viewModel.onAction("=");

        assertEquals("15 + 25 =", viewModel.equation.getValue());
        assertEquals("40", viewModel.result.getValue());
    }

    @Test
    public void testMultiOperatorPrecedence() {
        // 10 + 20 × 3 = 70
        viewModel.onAction("1");
        viewModel.onAction("0");
        viewModel.onAction("+");
        viewModel.onAction("2");
        viewModel.onAction("0");
        viewModel.onAction("×");
        viewModel.onAction("3");
        viewModel.onAction("=");

        assertEquals("10 + 20 × 3 =", viewModel.equation.getValue());
        assertEquals("70", viewModel.result.getValue());
    }

    @Test
    public void testComplexMultiOperation() {
        // 100 - 20 ÷ 4 + 5 = 100
        viewModel.onAction("1");
        viewModel.onAction("0");
        viewModel.onAction("0");
        viewModel.onAction("-");
        viewModel.onAction("2");
        viewModel.onAction("0");
        viewModel.onAction("÷");
        viewModel.onAction("4");
        viewModel.onAction("+");
        viewModel.onAction("5");
        viewModel.onAction("=");

        assertEquals("100 - 20 ÷ 4 + 5 =", viewModel.equation.getValue());
        assertEquals("100", viewModel.result.getValue());
    }

    @Test
    public void testContinuousChainingAfterEquals() {
        // 5 + 5 = 10, then + 10 = 20
        viewModel.onAction("5");
        viewModel.onAction("+");
        viewModel.onAction("5");
        viewModel.onAction("=");
        assertEquals("10", viewModel.result.getValue());

        viewModel.onAction("+");
        viewModel.onAction("1");
        viewModel.onAction("0");
        viewModel.onAction("=");

        assertEquals("10 + 10 =", viewModel.equation.getValue());
        assertEquals("20", viewModel.result.getValue());
    }

    @Test
    public void testDivideByZero() {
        viewModel.onAction("1");
        viewModel.onAction("0");
        viewModel.onAction("÷");
        viewModel.onAction("0");
        viewModel.onAction("=");

        assertEquals("Error", viewModel.result.getValue());
    }

    @Test
    public void testScientificNotationForLargeNumbers() {
        // 1000000000000 (10^12) -> should format as scientific notation containing 'E'
        viewModel.onAction("1");
        for (int i = 0; i < 12; i++) {
            viewModel.onAction("0");
        }
        viewModel.onAction("×");
        viewModel.onAction("1");
        viewModel.onAction("=");

        assertTrue(viewModel.result.getValue().contains("E") || viewModel.result.getValue().contains("e"));
    }

    @Test
    public void testClearAll() {
        viewModel.onAction("1");
        viewModel.onAction("0");
        viewModel.onAction("+");
        viewModel.onAction("5");
        viewModel.onAction("AC");

        assertEquals("", viewModel.equation.getValue());
        assertEquals("0", viewModel.result.getValue());
    }

    @Test
    public void testClearHistory() {
        viewModel.onAction("5");
        viewModel.onAction("+");
        viewModel.onAction("5");
        viewModel.onAction("=");
        assertFalse(viewModel.history.getValue().isEmpty());

        viewModel.clearHistory();
        assertTrue(viewModel.history.getValue().isEmpty());
    }
}
