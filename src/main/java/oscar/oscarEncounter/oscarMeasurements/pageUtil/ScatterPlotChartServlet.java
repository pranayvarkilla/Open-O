//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package oscar.oscarEncounter.oscarMeasurements.pageUtil;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.oscarehr.common.dao.MeasurementDao;
import org.oscarehr.common.dao.MeasurementTypeDao;
import org.oscarehr.common.dao.ValidationsDao;
import org.oscarehr.common.model.Measurement;
import org.oscarehr.common.model.MeasurementType;
import org.oscarehr.common.model.Validations;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.oscarEncounter.pageUtil.EctSessionBean;
import oscar.util.ConversionUtils;

/**
 * A servlet that generates scatter plots and blood pressure charts using JFreeChart.
 * This servlet can handle both regular measurements (displayed as scatter plots)
 * and blood pressure measurements (displayed as line charts with systolic and diastolic values).
 */
public class ScatterPlotChartServlet extends HttpServlet {
    protected int width = 550;
    protected int height = 360;
    
    protected Font titleFont;
    protected Font axisFont;

    /**
     * Initializes the servlet by setting up the fonts used for chart titles and axes.
     */
    public void init() {
        this.titleFont = new Font("Georgia Negreta cursiva", Font.PLAIN, 14);
        this.axisFont = new Font("Arial Narrow", Font.PLAIN, 14);
    }

    /**
     * Creates a scatter plot chart for regular measurements.
     *
     * @param demo    The demographic number of the patient
     * @param type    The type of measurement
     * @param mInstrc The measuring instruction
     * @return A JFreeChart object containing the scatter plot, or null if data is invalid
     */
    private JFreeChart createScatterPlot(String demo, String type, String mInstrc) {
        long[][] results = generateResult(demo, type, mInstrc);
        String chartTitle = type + "-" + mInstrc;
        
        if (results == null) return null;
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        if (type.compareTo("BP") != 0) {
            XYSeries series = new XYSeries(chartTitle);
            
            for (int x = 0; x < results[0].length; x++) {
                series.add(results[0][x] - results[0][0], results[1][x]);
            }
            
            dataset.addSeries(series);
            
            JFreeChart chart = ChartFactory.createScatterPlot(
                chartTitle,
                "Day (note: only the last data on the same observation date is plotted)",
                "Test Results",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
            
            customizeChart(chart);
            customizeScatterPlot(chart);
            
            return chart;
        }
        return null;
    }
    
    /**
     * Creates a line chart for blood pressure measurements showing both systolic and diastolic values.
     *
     * @param demo    The demographic number of the patient
     * @param type    The type of measurement (should be "BP")
     * @param mInstrc The measuring instruction
     * @return A JFreeChart object containing the blood pressure chart, or null if data is invalid
     */
    private JFreeChart createBloodPressureChart(String demo, String type, String mInstrc) {
        long[][] results = generateResult(demo, type, mInstrc);
        
        if (results == null || type.compareTo("BP") != 0) return null;
        
        String chartTitle = type + "-" + mInstrc;
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        int offset = results[1].length / 2;
        
        // Create systolic series
        XYSeries systolicSeries = new XYSeries("Systolic");
        for (int x = 0; x < offset; x++) {
            systolicSeries.add(x + 1, results[1][x]);
        }
        dataset.addSeries(systolicSeries);
        
        // Create diastolic series
        XYSeries diastolicSeries = new XYSeries("Diastolic");
        for (int x = 0; x < offset; x++) {
            diastolicSeries.add(x + 1, results[1][x + offset]);
        }
        dataset.addSeries(diastolicSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            chartTitle,
            "Tests (note: only the last data on the same observation date is plotted)",
            "Hgmm",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        customizeChart(chart);
        customizeBloodPressureChart(chart);
        
        return chart;
    }
    
    /**
     * Applies common customization to all charts (scatter plots and blood pressure charts).
     * This includes setting fonts, background colors, and grid lines.
     *
     * @param chart The JFreeChart object to customize
     */
    private void customizeChart(JFreeChart chart) {
        chart.getTitle().setFont(titleFont);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        domainAxis.setTickLabelFont(axisFont);
        rangeAxis.setTickLabelFont(axisFont);
        domainAxis.setLabelFont(axisFont);
        rangeAxis.setLabelFont(axisFont);
    }
    
    /**
     * Applies specific customization for scatter plots.
     * Configures the renderer to show shapes but no lines between points.
     *
     * @param chart The scatter plot chart to customize
     */
    private void customizeScatterPlot(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
    }
    
    /**
     * Applies specific customization for blood pressure charts.
     * Configures the renderer to show both lines and shapes for systolic and diastolic values.
     *
     * @param chart The blood pressure chart to customize
     */
    private void customizeBloodPressureChart(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, Color.red);    // Systolic
        renderer.setSeriesPaint(1, Color.blue);   // Diastolic
        plot.setRenderer(renderer);
    }
    
    /**
     * Generates the data points for the chart from the database.
     * Handles both regular measurements and blood pressure measurements differently.
     *
     * @param demo    The demographic number of the patient
     * @param type    The type of measurement
     * @param mInstrc The measuring instruction
     * @return A 2D array containing the x and y coordinates for the chart points,
     *         or null if no data is found
     */
    private long[][] generateResult(String demo, String type, String mInstrc) {
        long[][] points = null;

        MeasurementDao dao = SpringUtils.getBean(MeasurementDao.class);
        if (isNumeric(type, mInstrc)) {
            List<Object> dates = dao.findObservationDatesByDemographicNoTypeAndMeasuringInstruction(
                ConversionUtils.fromIntString(demo), type, mInstrc);
            int nbData = dates.size();
            points = new long[2][nbData];
            
            for (int i = 0; i < nbData; i++) {
                Measurement m = dao.findByDemographicNoTypeAndDate(
                    ConversionUtils.fromIntString(demo), type, (Date) dates.get(i));
                
                if (m != null) {
                    Date dateObserved = m.getDateObserved();
                    points[0][i] = dateObserved.getTime() / 1000 / 60 / 60 / 24;
                    points[1][i] = ConversionUtils.fromLongString(m.getDataField());
                }
            }
        } else if (type.compareTo("BP") == 0) {
            List<Date> measurements = dao.findByDemographicNoTypeAndMeasuringInstruction(
                ConversionUtils.fromIntString(demo), type, mInstrc);
            int nbPatient = measurements.size();
            points = new long[2][nbPatient * 2];
            
            for (int i = 0; i < nbPatient; i++) {
                Measurement mm = dao.findByDemographicNoTypeAndDate(
                    ConversionUtils.fromIntString(demo), type, measurements.get(i));
                
                if (mm != null) {
                    String bloodPressure = mm.getDataField();
                    int slashIndex = bloodPressure.indexOf("/");
                    
                    if (slashIndex >= 0) {
                        String systolic = bloodPressure.substring(0, slashIndex);
                        Date dateObserved = mm.getDateObserved();
                        points[0][i] = dateObserved.getTime() / 1000 / 60 / 60 / 24;
                        points[1][i] = Long.parseLong(systolic);
                        
                        String diastolic = bloodPressure.substring(slashIndex + 1);
                        points[0][i + nbPatient] = dateObserved.getTime() / 1000 / 60 / 60 / 24;
                        points[1][i + nbPatient] = Long.parseLong(diastolic);
                    }
                }
            }
        }
        
        return points;
    }
    
    /**
     * Checks if a measurement type is numeric based on its validation rules.
     *
     * @param type    The type of measurement
     * @param mInstrc The measuring instruction
     * @return true if the measurement type is numeric, false otherwise
     */
    private boolean isNumeric(String type, String mInstrc) {
        boolean result = false;
        MeasurementTypeDao dao = SpringUtils.getBean(MeasurementTypeDao.class);
        List<MeasurementType> measurementTypes = dao.findByTypeAndMeasuringInstruction(type, mInstrc);
        
        if (!measurementTypes.isEmpty()) {
            String validation = measurementTypes.get(0).getValidation();
            
            ValidationsDao valDao = SpringUtils.getBean(ValidationsDao.class);
            Validations v = valDao.find(Integer.parseInt(validation));
            if (v != null && v.isNumeric() != null && v.isNumeric()) {
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Handles the HTTP service request to generate and return a chart image.
     * Supports both scatter plots for regular measurements and line charts for blood pressure.
     *
     * @param request  The HTTP servlet request
     * @param response The HTTP servlet response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException      If an I/O error occurs
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String type = request.getParameter("type");
        String mInstrc = request.getParameter("mInstrc");
        EctSessionBean bean = (EctSessionBean) request.getSession().getAttribute("EctSessionBean");
        String demographicNo = null;
        
        if (request.getParameter("demographicNo") != null) {
            demographicNo = request.getParameter("demographicNo");
        }
        
        if (demographicNo == null && bean != null) {
            demographicNo = bean.getDemographicNo();
        }
        
        try {
            JFreeChart chart = null;
            
            if (type.compareTo("BP") == 0) {
                chart = createBloodPressureChart(demographicNo, type, mInstrc);
            } else {
                chart = createScatterPlot(demographicNo, type, mInstrc);
            }
            
            if (chart != null) {
                response.setContentType("image/jpeg");
                ChartUtils.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
            }
            
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
    }
}
