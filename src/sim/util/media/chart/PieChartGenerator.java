/*
  Copyright 2013 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.util.media.chart;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import sim.util.gui.*;

// From JFreeChart
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.data.statistics.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.*;
import org.jfree.chart.title.*;
import org.jfree.ui.*;

// from iText (www.lowagie.com/iText/)
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/*  // looks like we'll have to move to these soon
    import com.itextpdf.text.*;
    import com.itextpdf.text.pdf.*;
*/


/** A ChartGenerator for Pie Charts. */

public class PieChartGenerator extends ChartGenerator
    {
    public void removeSeries(int index)
        {
        super.removeSeries(index);
        update();
        }
                

    public void moveSeries(int index, boolean up)
        {
        super.moveSeries(index, up);
        update();
        }
                
    /** The total number of unique groups permitted in the generator, to keep from overwhelming JFreeChart. */
    public static final int MAXIMUM_PIE_CHART_ITEMS = 20;
    final DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();

    public Dataset getSeriesDataset() { return ((MultiplePiePlot)(chart.getPlot())).getDataset(); }
    public void setSeriesDataset(Dataset obj) 
        {
        // here we will interrupt things if they're too big
        if (((CategoryDataset)obj).getRowCount() > MAXIMUM_PIE_CHART_ITEMS)
            {
            ((MultiplePiePlot)(chart.getPlot())).setDataset(emptyDataset);
            setInvalidChartTitle("[[ Dataset has too many items. ]]");
            }
        else
            {
            ((MultiplePiePlot)(chart.getPlot())).setDataset((DefaultCategoryDataset)obj);
            if (invalidChartTitle != null)
                setInvalidChartTitle(null);
            }
        }
 
    public int getProspectiveSeriesCount(Object[] objs)
        {
        HashMap map = convertIntoAmountsAndLabels(objs);
        String[] labels = revisedLabels(map);
        return labels.length;
        }

    public int getSeriesCount()
        {
        SeriesAttributes[] sa = getSeriesAttributes();
        return sa.length;  // we do this instead of returning the columns in the dataset because hidden series don't have columns (stupid JFreeChart)
        }

    protected void buildChart()
        {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        chart = ChartFactory.createMultiplePieChart("Untitled Chart", dataset,
            org.jfree.util.TableOrder.BY_COLUMN, false, true, false);
        chart.setAntiAlias(true);
        //chartPanel = new ScrollableChartPanel(chart, true);            
        chartPanel = buildChartPanel(chart);           
        //chartHolder.getViewport().setView(chartPanel);
        setChartPanel(chartPanel);
        
        JFreeChart baseChart = (JFreeChart)   ((MultiplePiePlot)(chart.getPlot())).getPieChart();
        PiePlot base = (PiePlot)  (baseChart.getPlot());
        base.setIgnoreZeroValues(true);
        base.setLabelOutlinePaint(java.awt.Color.WHITE);
        base.setLabelShadowPaint(java.awt.Color.WHITE);
        base.setMaximumLabelWidth(0.25);  // allow bigger labels by a bit (this will make the chart smaller)
        base.setInteriorGap(0.000);  // allow stretch to compensate for the bigger label width
        base.setLabelBackgroundPaint(java.awt.Color.WHITE);
        base.setOutlinePaint(null);
        base.setBackgroundPaint(null);
        base.setShadowPaint(null);
        base.setSimpleLabels(false);  // I think they're false anyway
                
        // change the look of the series title to be smaller
        StandardChartTheme theme = new StandardChartTheme("Hi");
        TextTitle title = new TextTitle("Whatever", theme.getLargeFont());
        title.setPaint(theme.getAxisLabelPaint());
        title.setPosition(RectangleEdge.BOTTOM);
        baseChart.setTitle(title);

        // this must come last because the chart must exist for us to set its dataset
        setSeriesDataset(dataset);
        }
 
    protected void update()
        {
        // We have to rebuild the dataset from scratch (deleting and replacing it) because JFreeChart's
        // piechart facility doesn't have a way to move series.  Just like the histogram system: stupid stupid stupid.

        SeriesAttributes[] sa = getSeriesAttributes();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for(int i=0; i < sa.length; i++)
            if (sa[i].isPlotVisible())
                {
                PieChartSeriesAttributes attributes = (PieChartSeriesAttributes)(sa[i]);
                double[] values = attributes.getValues();
                String[] labels = attributes.getLabels();
                UniqueString seriesName = new UniqueString(attributes.getSeriesName());
        
                for(int j = 0; j < values.length; j++)
                    dataset.addValue(values[j], labels[j], seriesName);  // ugh
                }
                        
        setSeriesDataset(dataset);
        }

    public PieChartGenerator()
        {
        // buildChart is called by super() first
        }


    /** Adds a series, plus a (possibly null) SeriesChangeListener which will receive a <i>single</i>
        event if/when the series is deleted from the chart by the user. Returns the series attributes. */
    SeriesAttributes addSeries(double[] amounts, String[] labels, String name, SeriesChangeListener stopper)
        {
        int i = getSeriesCount();
        
        // need to have added the dataset BEFORE calling this since it'll try to change the name of the series
        PieChartSeriesAttributes csa = new PieChartSeriesAttributes(this, name, i, amounts, labels, stopper);
        seriesAttributes.add(csa);
                
        revalidate();  // display the new series panel
        update();
                
        // won't update properly unless I force it here by letting all the existing scheduled events to go through.  Dumb design.  :-(
        SwingUtilities.invokeLater(new Runnable() { public void run() { update(); } });
                
        return csa;
        }

    /** Adds a series, plus a (possibly null) SeriesChangeListener which will receive a <i>single</i>
        event if/when the series is deleted from the chart by the user. Returns the series attributes. */
    public SeriesAttributes addSeries(Object[] objs, String name, SeriesChangeListener stopper)
        {
        HashMap map = convertIntoAmountsAndLabels(objs);
        String[] labels = revisedLabels(map);
        double[] amounts = amounts(map, labels);
        return addSeries(amounts, labels, name, stopper);
        }
        
                
    // Takes objects and produces an object->count mapping
    HashMap convertIntoAmountsAndLabels(Object[] objs)
        {
        // Total the amounts
        HashMap map = new HashMap();
        for(int i = 0; i < objs.length; i++)
            {
            String label = "null";
            if (objs[i] != null)
                label = objs[i].toString();
            if (map.containsKey(label))
                map.put(label,
                    new Double(((Double)(map.get(label))).doubleValue() + 1));
            else
                map.put(label, new Double(1));
            }
        return map;
        }
        
    // Sorts labels from the mapping.  We may get rid of this later perhaps.
    String[] revisedLabels(HashMap map)
        {
        // Sort labels
        String[] labels = new String[map.size()];
        labels = (String[])(map.keySet().toArray(labels));
        Arrays.sort(labels);
        return labels;
        }
        
    // Returns the counts from the mapping, in the same order as the labels 
    double[] amounts(HashMap map, String[] revisedLabels)
        {
        // Extract amounts
        double[] amounts = new double[map.size()];
        for(int i = 0; i < amounts.length; i++)
            amounts[i] = ((Double)(map.get(revisedLabels[i]))).doubleValue();
        return amounts;
        }

    public void updateSeries(int index, Object[] objs)
        {
        if (index < 0) // this happens when we're a dead chart but the inspector doesn't know
            return;
            
        if (index >= getNumSeriesAttributes())  // this can happen when we close a window if we use the Histogram in a display
            return;

        HashMap map = convertIntoAmountsAndLabels(objs);
        String[] labels = revisedLabels(map);
        double[] amounts = amounts(map, labels);

        updateSeries(index, amounts, labels);
        }
    
    void updateSeries(int index, double[] amounts, String[] labels)
        {
        if (index < 0) // this happens when we're a dead chart but the inspector doesn't know
            return;
            
        if (index >= getNumSeriesAttributes())  // this can happen when we close a window if we use the Histogram in a display
            return;

        PieChartSeriesAttributes hsa = (PieChartSeriesAttributes)(getSeriesAttribute(index));
        hsa.setValues(amounts);
        hsa.setLabels(labels);
        }       

    // This ridiculous class exists so we can create Strings (of sorts) which are completely
    // uncomparable and have a total sort order regardless of their values.  Otherwise
    // (this is true) MultiplePiePlot won't allow multiple PiePlots with the same name.
    public class UniqueString implements java.lang.Comparable
        {
        String string;
        
        public UniqueString(String str)
            {
            string = str;
            }
        
        public boolean equals(Object obj)
            {
            return obj == this;
            }
                
        public int compareTo(Object obj)
            {
            if (obj == this) return 0;
            if (obj == null) throw new NullPointerException();
            if (!(obj instanceof UniqueString)) return -1;
            UniqueString us = (UniqueString)obj; 
            if (us.string.equals(string))  // gotcha.  Gotta differentiate
                {
                if (System.identityHashCode(this) > System.identityHashCode(us))
                    return 1; 
                else return -1;
                }
            else return us.string.compareTo(string);
            }
                
        public String toString() { return string; }
        }
    }

