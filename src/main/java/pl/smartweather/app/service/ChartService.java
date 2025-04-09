package pl.smartweather.app.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import pl.smartweather.app.model.entity.WeatherInformation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
@Service
public class ChartService {
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 400;

    public byte[] generateTemperatureChart(List<WeatherInformation> hourForecast) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (WeatherInformation hour : hourForecast) {
            String hourLabel = hour.getHour();
            dataset.addValue(hour.getTemperature(), "Temperature", hourLabel);
            dataset.addValue(hour.getFeelsLike(), "Feels like", hourLabel);
        }

        JFreeChart lineChart = ChartFactory.createLineChart("Temperature Forecast",
                "Hour", "°C", dataset);
        CategoryAxis xAxis = lineChart.getCategoryPlot().getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);

        lineChart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        BufferedImage chartImage = lineChart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(out, chartImage);

        return out.toByteArray();
    }

    public byte[] generateRainChart(List<WeatherInformation> hourForecast) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (WeatherInformation hour : hourForecast) {
            String hourLabel = hour.getHour();
            dataset.addValue(hour.getChanceOfRain(), "Chance of rain [%]", hourLabel);
            dataset.addValue(hour.getCloud(), "Cloud [%]", hourLabel);
        }

        JFreeChart lineChart = ChartFactory.createLineChart("Rain / Cloud [%]",
                "Hour", "%", dataset);
        CategoryAxis xAxis = lineChart.getCategoryPlot().getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);

        lineChart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesPaint(1, Color.GRAY);

        BufferedImage chartImage = lineChart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(out, chartImage);

        return out.toByteArray();
    }
}
