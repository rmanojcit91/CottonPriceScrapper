package com.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartGenerator {

    public void generateChart(List<CottonPrice> prices, String filePath) throws IOException {
        DefaultCategoryDataset dataset = createDataset(prices);
        JFreeChart chart = createChart(dataset);

        ChartUtils.saveChartAsPNG(new File(filePath), chart, 800, 600);
    }

    private DefaultCategoryDataset createDataset(List<CottonPrice> prices) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CottonPrice price : prices) {
            dataset.addValue(price.getPrice(), price.getCottonType(), price.getDate().toString());
        }

        return dataset;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Cotton Price Trends",
                "Date",
                "Price",
                dataset
        );

        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);

        return lineChart;
    }
}
