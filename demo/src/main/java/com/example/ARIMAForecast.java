package com.example;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class ARIMAForecast extends JFrame {

    public ARIMAForecast(String title, double[] original, double[] forecast) {
        super(title);

        // Create dataset
        XYSeriesCollection dataset = createDataset(original, forecast);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "ARIMA Forecast",
                "Month",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the plot
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        plot.setRenderer(renderer);

        // Add the chart to a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private XYSeriesCollection createDataset(double[] original, double[] forecast) {
        XYSeries originalSeries = new XYSeries("Original");
        XYSeries forecastSeries = new XYSeries("Forecast");

        for (int i = 0; i < original.length; i++) {
            originalSeries.add(i + 1, original[i]);
        }

        for (int i = 0; i < forecast.length; i++) {
            forecastSeries.add(original.length + i + 1, forecast[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(originalSeries);
        dataset.addSeries(forecastSeries);

        return dataset;
    }

    public static void main(String[] args) {
        // Sample data: replace with your actual data
        double[] data = {100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650};

        int p = 1; // AR order
        int d = 1; // Differencing order
        int q = 1; // MA order

        double[] forecast = arimaForecast(data, p, d, q, 12);

        // Create and display the plot
        SwingUtilities.invokeLater(() -> {
            ARIMAForecast plot = new ARIMAForecast("ARIMA Forecast", data, forecast);
            plot.setSize(800, 600);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plot.setVisible(true);
        });
    }

    public static double[] arimaForecast(double[] data, int p, int d, int q, int steps) {
        // Differencing
        double[] differenced = difference(data, d);

        // Fit ARIMA model (for simplicity, using only AR component)
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.setNoIntercept(true);

        int n = differenced.length;
        double[] y = new double[n - p];
        double[][] x = new double[n - p][p];

        for (int i = 0; i < n - p; i++) {
            y[i] = differenced[i + p];
            for (int j = 0; j < p; j++) {
                x[i][j] = differenced[i + p - j - 1];
            }
        }

        regression.newSampleData(y, x);
        double[] params = regression.estimateRegressionParameters();

        // Forecasting
        double[] forecast = new double[steps];
        double[] lastValues = new double[p];

        System.arraycopy(differenced, differenced.length - p, lastValues, 0, p);

        for (int i = 0; i < steps; i++) {
            double nextValue = 0;
            for (int j = 0; j < p; j++) {
                nextValue += params[j] * lastValues[p - j - 1];
            }
            forecast[i] = nextValue;

            // Shift last values
            System.arraycopy(lastValues, 1, lastValues, 0, p - 1);
            lastValues[p - 1] = nextValue;
        }

        // Reverse differencing
        forecast = reverseDifference(data, forecast, d);

        return forecast;
    }

    public static double[] difference(double[] data, int d) {
        double[] differenced = new double[data.length - d];
        for (int i = d; i < data.length; i++) {
            differenced[i - d] = data[i] - data[i - d];
        }
        return differenced;
    }

    public static double[] reverseDifference(double[] original, double[] differenced, int d) {
        double[] reversed = new double[differenced.length];
        for (int i = 0; i < differenced.length; i++) {
            reversed[i] = differenced[i] + original[original.length - differenced.length + i];
        }
        return reversed;
    }
}
