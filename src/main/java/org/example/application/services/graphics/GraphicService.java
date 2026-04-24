package org.example.application.services.graphics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

@Service
// response.setContentType("image/png")
// response.setHeader("Content-Disposition","attachment; filename=\"sales-chart.png")
public class GraphicService {
    // bar x:shops y:price
    public void generateBar(
            OutputStream out, String title, String xAxisLabel,
            String yAxisLabel,
            DefaultCategoryDataset dataset
    ) throws Exception {
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xAxisLabel,
                yAxisLabel,dataset
        );
        ChartUtils.writeChartAsPNG(out, chart, 800, 600);
    }

    public void generateTimeSeries(
            OutputStream out,
            String title,
            String xLabel,
            String yLabel,
            TimeSeriesCollection dataset,
            Integer minRange,
            Integer maxRange,
            Double step
    ) throws IOException {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                xLabel,
                yLabel,
                dataset,
                true,
                false,
                false
        );

        if (minRange != null){
            XYPlot plot = chart.getXYPlot();

            NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
            yAxis.setRange(minRange, maxRange);
            yAxis.setLowerBound(minRange);
            yAxis.setUpperBound(maxRange);
            yAxis.setUpperMargin(1);
            yAxis.setLowerMargin(0.5);

            yAxis.setTickUnit(new NumberTickUnit(step));
        }


        ChartUtils.writeChartAsPNG(out, chart, 800,400);
    }






}
