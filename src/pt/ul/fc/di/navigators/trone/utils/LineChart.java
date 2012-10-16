/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author igor
 */
public class LineChart extends ApplicationFrame {
    ChartPanel cp;
    DefaultCategoryDataset dataset;
    static String yAxisName;
    static String xAxisName;
    static String name;
    
    public LineChart(String n, String yan, String xan){
         super(n);
         this.name = n;
         dataset = new DefaultCategoryDataset();
         this.yAxisName = yan;
         this.xAxisName = xan;
         JFreeChart chart = createChart(dataset);
         
         cp = new ChartPanel(chart);       
         
         cp.setPreferredSize(new Dimension(500, 270));
         setContentPane(cp);
    }
    
    public void addValue(int value, int time, int lim){
        if(time>=lim)
            this.dataset.removeValue( "Classes", ""+(time-lim));
        
        this.dataset.addValue(value, "Classes", ""+time);
    }
    
    public void addValueNoRange(int value, int time){
        this.dataset.addValue(value, "Classes", ""+time);
    }
    
    public void refresh(){
        this.cp.repaint();
    }
    
    
    
    private static JFreeChart createChart(CategoryDataset dataset) {
         // create the chart...
         JFreeChart chart = ChartFactory.createLineChart(
         name, // chart title
         xAxisName, // domain axis label
         yAxisName, // range axis label
         dataset, // data
         PlotOrientation.VERTICAL, // orientation
         false, // include legend
         true, // tooltips
         false // urls
         );
         
         /*chart.addSubtitle(new TextTitle("Number of Classes By Release"));
         TextTitle source = new TextTitle(
         "Source: Java In A Nutshell (4th Edition) "
         + "by David Flanagan (OReilly)"
         );
         //source.setFont(new Font("SansSerif", Font.NORMAL, 10));
         source.setPosition(RectangleEdge.BOTTOM);
         source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
         chart.addSubtitle(source);*/
         
         chart.setBackgroundPaint(Color.white);
         CategoryPlot plot = (CategoryPlot) chart.getPlot();
         plot.setBackgroundPaint(Color.lightGray);
         plot.setRangeGridlinePaint(Color.white);
         // customise the range axis...
         NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         // customise the renderer...
         LineAndShapeRenderer renderer
         = (LineAndShapeRenderer) plot.getRenderer();
         //renderer.setShapesVisible(true);
         //renderer.setShapesV
         renderer.setDrawOutlines(true);
         renderer.setUseFillPaint(true);
         //renderer.setFillPaint(Color.white);
         return chart;
     }
    
    public void saveChart(String file, int testTime){
        try {
            ChartUtilities.saveChartAsJPEG(new File(file), cp.getChart(), 800, 800);
            ChartUtilities.saveChartAsJPEG(new File("Full_"+file), cp.getChart(), (testTime*40), 800);
            
        } catch (IOException ex) {
            Logger.getLogger(LineChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
