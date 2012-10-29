/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

/**
 *
 * @author igor
 */
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.*;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.StandardGradientPaintTransformer;

public class DialMeter extends JFrame{
    
        DialPanel p;
    
	static class DialPanel extends JPanel{

		
		DefaultValueDataset dataset;
                DefaultValueDataset dts;

		public static JFreeChart createStandardDialChart(String s, String s1, String label,  ValueDataset dt, ValueDataset valuedataset, double d, double d1, double d2, int i){
                        
                        NumberFormat f = new DecimalFormat("0K");
                        f.setMinimumFractionDigits(0);
                        
                        NumberFormat f1 = NumberFormat.getIntegerInstance();
                        f1.setMaximumIntegerDigits(8);
                    
                        
			DialPlot dialplot = new DialPlot();
			dialplot.setDataset(0,valuedataset);
                        dialplot.setDataset(1, dt);
			dialplot.setDialFrame(new StandardDialFrame());
			dialplot.setBackground(new DialBackground());
                        
			DialTextAnnotation dialtextannotation = new DialTextAnnotation(s1);
			dialtextannotation.setFont(new Font(Font.DIALOG, 1, 20));
			dialtextannotation.setRadius(0.69999999999999996D);
			dialplot.addLayer(dialtextannotation);
                        
                        DialTextAnnotation dta = new DialTextAnnotation(label);
                        dta.setFont(new Font(Font.DIALOG, 1, 20));
                        dta.setRadius(0.5);
                        dta.setAngle(90);
                        dialplot.addLayer(dta);
                        
                        
                        DialValueIndicator dvi = new DialValueIndicator(1);
                        dvi.setNumberFormat(f1);
                        dvi.setMaxTemplateValue(1000);
                        dvi.setFrameAnchor(RectangleAnchor.CENTER);
                        dvi.setTemplateValue(1000000);
                        dvi.setAngle(90);
                        dialplot.addLayer(dvi);
                        
                       
			DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
                        dialvalueindicator.setNumberFormat(f);
			dialplot.addLayer(dialvalueindicator);
                        
			StandardDialScale standarddialscale = new StandardDialScale(d, d1, -120D, -300D, 10D, 4);
			standarddialscale.setMajorTickIncrement(d2);
			standarddialscale.setMinorTickCount(i);
			standarddialscale.setTickRadius(0.88D);
                        
                        
                        //FORMATAR OS NUMEROS
                        
                        
                        
                        standarddialscale.setTickLabelFormatter(f);
                        /****************************************/
                        
			standarddialscale.setTickLabelOffset(0.14999999999999999D);
			standarddialscale.setTickLabelFont(new Font("Dialog", 1, 24));
			dialplot.addScale(0, standarddialscale);
			dialplot.addPointer(new org.jfree.chart.plot.dial.DialPointer.Pin());
			DialCap dialcap = new DialCap();
			dialplot.setCap(dialcap);
			return new JFreeChart(s, dialplot);
		}

		public DialPanel(String title, String units, String label, int min, int max, int leap){          
			super(new BorderLayout());
			dataset = new DefaultValueDataset(0D); //define a posição inicial do ponteiro
                        dts = new DefaultValueDataset(0);
			JFreeChart jfreechart = createStandardDialChart(title, units, label, dts, dataset, min, max, leap, 10); //Nome, nome do valor no ponteiro, dataset, range min/max, distancia entre valores, numero de tracos entre cada 2 valores
                        
			DialPlot dialplot = (DialPlot)jfreechart.getPlot();
                        
			/*StandardDialRange standarddialrange = new StandardDialRange(180D, 200D, Color.red); //red line
                        
			standarddialrange.setInnerRadius(0.52000000000000002D); // distancia e grossura do red line
			standarddialrange.setOuterRadius(0.55000000000000004D);
			dialplot.addLayer(standarddialrange);
			StandardDialRange standarddialrange1 = new StandardDialRange(10D, 40D, Color.orange);
			standarddialrange1.setInnerRadius(0.52000000000000002D);
			standarddialrange1.setOuterRadius(0.55000000000000004D);
			dialplot.addLayer(standarddialrange1);
			StandardDialRange standarddialrange2 = new StandardDialRange(-40D, 10D, Color.green);
			standarddialrange2.setInnerRadius(0.52000000000000002D);
			standarddialrange2.setOuterRadius(0.55000000000000004D);
			dialplot.addLayer(standarddialrange2);
                        */
                        //Background
			GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(170, 170, 220));
			DialBackground dialbackground = new DialBackground(gradientpaint);
			dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
			dialplot.setBackground(dialbackground);
                        
                        
			dialplot.removePointer(0);
			org.jfree.chart.plot.dial.DialPointer.Pointer pointer = new org.jfree.chart.plot.dial.DialPointer.Pointer();
			pointer.setFillPaint(Color.yellow);
			dialplot.addPointer(pointer);
			ChartPanel chartpanel = new ChartPanel(jfreechart);
			chartpanel.setPreferredSize(new Dimension(800, 800));
			
			add(chartpanel);
			
		}
                
                
                void addValue(int v){
                    dataset.setValue(new Integer(v));
                }
                
                void setNumberOfEvents(int v){
                    dts.setValue(v);
                }
                
                void refresh(){
                    this.repaint();
                }
	}

        
	public DialMeter(String title, String units, String label, int min, int max, int leap){
		super(title);
		setDefaultCloseOperation(3);
                p = (DialPanel) Panel(title, units,label, min, max, leap);
		setContentPane(p);
	}

	public static JPanel Panel(String title, String units, String label, int min, int max, int leap){
		return new DialPanel(title, units, label, min, max, leap);
	}
        
        public void addValue(int v){
            //Log.logInfo(this.getClass().getCanonicalName(), "ADDING VALUE: "+v, Log.getLineNumber());
            p.addValue(v/1000);
        }
        
        public void setNumberOfEvents(int v){
            p.setNumberOfEvents(v);
        }
        
        public void refresh(){
            p.refresh();
        }
        
}
