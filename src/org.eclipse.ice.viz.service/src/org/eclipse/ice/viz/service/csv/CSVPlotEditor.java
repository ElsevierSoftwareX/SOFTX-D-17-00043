/*******************************************************************************
 * Copyright (c) 2014, 2015- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz, Kasper Gammeltoft
 *******************************************************************************/
package org.eclipse.ice.viz.service.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.ice.viz.service.ISeries;
import org.eclipse.ice.viz.service.styles.BasicErrorStyle;
import org.eclipse.ice.viz.service.styles.XYZAxisStyle;
import org.eclipse.ice.viz.service.styles.XYZPlotStyle;
import org.eclipse.ice.viz.service.styles.XYZSeriesStyle;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap;
import org.eclipse.nebula.visualization.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.nebula.visualization.widgets.figures.IntensityGraphFigure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an editor for displaying an SWT XYGraph used to draw plots from CSV
 * files. It is opened by the associated visualization views in
 * org.eclipse.ice.viz.
 * 
 * @author Matthew Wang
 * @author Taylor Patterson
 * @author Anna Wojtowicz
 * @author Jordan Deyton
 * @author Alex McCaskey
 * @author Kasper Gammeltoft - Viz refactor, enabled the editor to work properly
 *         with ISeries rather than SeriesProvider
 */
public class CSVPlotEditor extends EditorPart {

	/**
	 * The ID for this editor.
	 */
	public static final String ID = "org.eclipse.ice.viz.service.csv.CSVPlotEditor";

	/**
	 * The top level composite that holds the editor's contents.
	 */
	public Composite vizComposite;

	/**
	 * The {@code Composite} that contains the time slider.
	 */
	private Composite sliderComposite;

	/**
	 * The canvas used for rendering the plot.
	 */
	private Canvas plotCanvas;

	/**
	 * LightweightSystem for an SWT XYGraph
	 */
	private LightweightSystem lws;

	/**
	 * The xy graph used to display the csv input
	 */
	private XYGraph xyGraph;

	/**
	 * The widget that holds the toolbar for the xy graph
	 */
	private ToolbarArmedXYGraph graphToolbar;

	/**
	 * Error map, keeps track of which series should be used as error data for
	 * other series.
	 */
	private Map<ISeries, List<ISeries>> seriesMap;

	/**
	 * A map of existing traces that are tracing certain series. This is useful
	 * for not adding identical traces and being able to remove traces later
	 */
	private Map<ISeries, Trace> existingTraces;

	/**
	 * The independent series, to use as the x axis for plotting. Should be set
	 * to non null {@link CSVSeries}
	 */
	private ISeries independentSeries;

	/**
	 * Logger for handling event messages and other information.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(CSVPlotEditor.class);

	/**
	 * The constructor
	 */
	public CSVPlotEditor() {
		seriesMap = new HashMap<ISeries, List<ISeries>>();
		existingTraces = new HashMap<ISeries, Trace>();
		xyGraph = null;
		graphToolbar = null;
		return;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		return;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public void doSaveAs() {
		return;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// Set the site, input and name
		setSite(site);
		setInput(input);
		setPartName("CSV Plot Editor");

		return;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * This operation sets up the Composite that contains the VisIt canvas and
	 * create the VisIt widget.
	 * 
	 * @param parent
	 *            The parent Composite to create the Control in.
	 */
	@Override
	public void createPartControl(Composite parent) {

		// Create a top level composite to hold the canvas or text
		vizComposite = new Composite(parent, SWT.NONE);
		vizComposite.setLayout(new GridLayout(1, true));

		// Set up the canvas where the graph is displayed
		plotCanvas = new Canvas(vizComposite, SWT.BORDER);
		plotCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// MAGIC
		lws = new LightweightSystem(plotCanvas);

		return;
	}

	/*
	 * Overrides a super class method.
	 */
	@Override
	public void setFocus() {
		return;
	}

	/**
	 * This operation refreshes the VisIt view.
	 */
	public void refresh() {
		setFocus();
	}

	/**
	 * Removes the selected series from this editor. Should remove if the series
	 * is
	 * 
	 * @param series
	 */
	public void removeSeries(ISeries series) {

		// Searches the series map to see if it is there and removes the series
		// if so
		if (seriesMap.containsKey(series)) {
			seriesMap.remove(series);
			// See if it is an error series to a parent in the map and remove it
		} else if (seriesMap.containsKey(series.getParentSeries())) {
			// Updates the trace for the parent, so that
			seriesMap.get(series.getParentSeries()).remove(series);
			// TODO- Refresh the plot that had this error series on it
		}

		// Remove the trace as well if it is being plotted on the graph
		if (existingTraces.containsKey(series)) {
			xyGraph.removeTrace(existingTraces.remove(series));
			// TODO- Run on separate thread?
			xyGraph.repaint();
		}
	}

	/**
	 * This function sets up the SWT XYGraph
	 * 
	 * @param displayPlotProvider
	 *            The PlotProvider containing the information to create the plot
	 */
	public void showPlotProvider(PlotProvider displayPlotProvider) {

		// If it is not a contour plot then plot the regular series
		if (!displayPlotProvider.isContour()) {
			// Get the time to show
			List<Double> times = displayPlotProvider.getTimes();
			double time = (times.isEmpty() ? 0.0 : times.get(0));

			// Get the list of series from the data provider
			List<ISeries> seriesList = displayPlotProvider
					.getSeriesAtTime(time);
			ISeries indepSeries = displayPlotProvider.getIndependentSeries();

			boolean resetAllTraces = false;

			// Refresh the current traces
			if (!indepSeries.equals(independentSeries)) {
				resetAllTraces = true;
				independentSeries = indepSeries;

			}

			// Go through and add the series to the series map to find all error
			// series first
			for (ISeries series : seriesList) {
				// If the series has an error style, then it should go in the
				// map
				if (series.getStyle() instanceof BasicErrorStyle) {
					// See if the parent is already in the map
					ISeries parent = series.getParentSeries();
					// If the parent is not a key yet, then create the new map
					// entry
					if (!seriesMap.containsKey(parent)) {
						List<ISeries> errors = new ArrayList<ISeries>();
						errors.add(series);
						// Otherwise, just add this series to the existing list
						// of error series for the parent. Do not add the same
						// error series twice!
					} else if (!seriesMap.get(parent).contains(series)) {
						seriesMap.get(parent).add(series);
					}
					// If it is a regular series add it to the map with no error
					// series, if it has not already been added
				} else if (!seriesMap.containsKey(series)) {
					seriesMap.put(series, new ArrayList<ISeries>());
				}
			}

			// Show the graph
			showXYGraph(displayPlotProvider, time, resetAllTraces);

			// Re-create the slider only if there are alternate times to use.
			if (displayPlotProvider.getTimes().size() > 1) {
				if (sliderComposite != null) {
					sliderComposite.dispose();
					sliderComposite = createSliderComp(vizComposite,
							displayPlotProvider);
				}
			}
			// Otherwise, dispose the slider if it is not needed and exists.
			else if (sliderComposite != null) {
				sliderComposite.dispose();
				sliderComposite = null;
			}

		} else {
			// Plot as a contour plot
			// Get the series list
			ArrayList<ISeries> seriesProviderList = displayPlotProvider
					.getSeriesAtTime(displayPlotProvider.getTimes().get(0));
			// Get the first series
			final ISeries plotSeries = displayPlotProvider
					.getIndependentSeries();

			// Create a new intensity graph
			final IntensityGraphFigure intensityGraph = new IntensityGraphFigure();

			// Set the minimum and maximum
			double[] bounds = plotSeries.getBounds();
			intensityGraph.setMax(bounds[0] + bounds[1]);
			intensityGraph.setMin(bounds[0]);

			// FIXME Set the data width and data height
			// intensityGraph.setDataHeight(seriesProvider.getDataHeight());
			// intensityGraph.setDataWidth(seriesProvider.getDataWidth());

			// Stick with predefined colormap
			intensityGraph.setColorMap(
					new ColorMap(PredefinedColorMap.JET, true, true));

			// Set the contents
			lws.setContents(intensityGraph);

			// Try to get valid data from the series
			double[] newArray = getDoubleValue(plotSeries);

			// Sets the data array.
			intensityGraph.setDataArray(newArray);

			// Add controls for setting the min and max values
			Composite minMaxComp = new Composite(vizComposite, SWT.NONE);
			minMaxComp.setLayoutData(
					new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
			minMaxComp.setLayout(new GridLayout(5, false));

			Label minLabel = new Label(minMaxComp, SWT.NONE);
			minLabel.setText("Minimum: ");
			final Text minText = new Text(minMaxComp, SWT.BORDER);
			minText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Label maxLabel = new Label(minMaxComp, SWT.NONE);
			maxLabel.setText("Maximum: ");
			final Text maxText = new Text(minMaxComp, SWT.BORDER);
			maxText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Button applyMinMaxButton = new Button(minMaxComp, SWT.BORDER);
			applyMinMaxButton.setText("Apply");
			applyMinMaxButton.setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			vizComposite.layout();

			applyMinMaxButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					// Set the minimum and maximum
					intensityGraph
							.setMax(Double.parseDouble(maxText.getText()));
					intensityGraph
							.setMin(Double.parseDouble(minText.getText()));

					// Set the contents
					lws.setContents(intensityGraph);

					return;
				}
			});
		}

		return;
	}

	/**
	 * Configures the specified axis for the style given.
	 * 
	 * @param style
	 *            The XYZAxisStyle to use when configuring the axis properties
	 * @param axis
	 *            The Axis to configure
	 */
	private void configureAxis(Axis axis, XYZAxisStyle style) {
		// Set the default behavior if there is no axis style
		if (style == null) {
			axis.setShowMajorGrid(true);
			axis.setAutoScale(true);

			// Otherwise, set the config options from the axis style
		} else {
			// Set the title
			axis.setTitle((String) style.getProperty(XYZAxisStyle.AXIS_TITLE));
			// Set the axis title font, if specified
			if (style.getProperty(XYZAxisStyle.TITLE_FONT) != null) {
				axis.setTitleFont(
						(Font) style.getProperty(XYZAxisStyle.TITLE_FONT));
			}
			// Set the axis font, if specified
			if (style.getProperty(XYZAxisStyle.SCALE_FONT) != null) {
				axis.setFont((Font) style.getProperty(XYZAxisStyle.SCALE_FONT));
			}
			// Set the axis color, if specified
			if (style.getProperty(XYZAxisStyle.AXIS_COLOR) != null) {
				axis.setForegroundColor(
						(Color) style.getProperty(XYZAxisStyle.AXIS_COLOR));
			}
			// Set the log scale, true for use log, false for use linear scale
			axis.setLogScale((boolean) style.getProperty(XYZAxisStyle.IS_LOG));
			// Set the auto scale feature, true for does auto scale, false for
			// does not
			axis.setAutoScale(
					(boolean) style.getProperty(XYZAxisStyle.AUTO_SCALE));
			// Set the major grid lines to appear, true for show the grid lines,
			// false for do not
			axis.setShowMajorGrid(
					(boolean) style.getProperty(XYZAxisStyle.SHOW_GRID_LINE));
			// Set the grid line to either be dashed for true and solid for
			// false
			axis.setDashGridLine((boolean) style
					.getProperty(XYZAxisStyle.GRID_LINE_IS_DASHED));
			// Sets the grid line color if it is available
			if (style.getProperty(XYZAxisStyle.GRID_LINE_COLOR) != null) {
				axis.setMajorGridColor((Color) style
						.getProperty(XYZAxisStyle.GRID_LINE_COLOR));
			}

		}

	}

	/**
	 * @param plotProvider
	 * @param time
	 */
	private void showXYGraph(PlotProvider plotProvider, double time,
			boolean resetTraces) {
		if (this.xyGraph == null) {
			xyGraph = new XYGraph();
			graphToolbar = new ToolbarArmedXYGraph(xyGraph);
			lws.setContents(graphToolbar);
			existingTraces.clear();

			// Set the title as the source
			xyGraph.setTitle(plotProvider.getPlotTitle());

			// Set all of the xy graph settings from the plot style
			XYZPlotStyle plotStyle = plotProvider.getPlotStyle();
			if (plotStyle != null) {
				xyGraph.setTransparent((boolean) plotStyle
						.getProperty(XYZPlotStyle.IS_TRANSPARENT));
				xyGraph.setBackgroundColor(
						(Color) plotStyle.getProperty(XYZPlotStyle.PLOT_COLOR));
				xyGraph.setShowLegend((boolean) plotStyle
						.getProperty(XYZPlotStyle.SHOW_LEGEND));
				xyGraph.getPlotArea().setShowBorder((boolean) plotStyle
						.getProperty(XYZPlotStyle.SHOW_PLOT_BORDER));

			}

			// Get the axes styles from the plot provider
			XYZAxisStyle xStyle = plotProvider.getXAxisStyle();
			XYZAxisStyle yStyle = plotProvider.getYAxisStyle();

			// Configure the axes
			configureAxis(xyGraph.primaryXAxis, xStyle);
			configureAxis(xyGraph.primaryYAxis, yStyle);
		}

		// Make sure the lightweight system is displaying the xy graph
		lws.setContents(graphToolbar);

		// Get the series
		ArrayList<ISeries> seriesToPlot = plotProvider.getSeriesAtTime(time);
		if (seriesToPlot == null) {
			seriesToPlot = new ArrayList<ISeries>();
		}
		// Local flag for signaling if there is x error
		boolean xError = false;
		// Only used locally. 0 is for no error, 1 is for plus error, 2 is for
		// minus error, and 3 is for both.
		int xErrorType = 0;

		// Get the x values and error for plotting
		double[] xValues = getDoubleValue(independentSeries);
		double[] xPlusError = null;
		double[] xMinusError = null;
		if (seriesMap.containsKey(independentSeries)) {
			List<ISeries> errors = seriesMap.get(independentSeries);
			if (errors.size() > 0) {
				// TODO: Get the error arrays in a better way!!
				ISeries error = errors.get(0);
				// Gets the errors and sets local flags for error type
				xPlusError = getDoubleValue(error);
				xMinusError = getDoubleValue(error);
				if (xPlusError != null) {
					xError = true;
					// There is plus error!
					xErrorType = 1;
				}
				if (xMinusError != null) {
					xError = true;
					// If there is plus error, then the type is both plus and
					// minus error
					if (xErrorType == 1) {
						xErrorType = 3;
						// Otherwise, the type is just minus error
					} else {
						xErrorType = 2;
					}
				}
			}

		}

		// Iterate over all of the series and see if any need to be added or
		// reset
		for (ISeries iseries : seriesToPlot) {
			CSVSeries series = (CSVSeries) iseries;

			// Creates a new trace and adds it to the graph if it does not
			// already exist
			if (!existingTraces.containsKey(series)
					&& !(series.getStyle() instanceof BasicErrorStyle)) {
				final CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(
						false);
				// Sets the size of the buffer
				traceDataProvider.setBufferSize(series.size());
				double[] yValues = getDoubleValue(series);
				double[] yPlusError = null;
				double[] yMinusError = null;
				// Flag to signal if there is y error for this series
				boolean yError = false;
				// Same error types as above for the x error
				int yErrorType = 0;

				if (seriesMap.containsKey(series)) {
					List<ISeries> errors = seriesMap.get(series);
					if (errors != null && errors.size() > 0) {
						// TODO: Get the error arrays in a better way!!
						ISeries error = errors.get(0);
						yPlusError = getDoubleValue(error);
						yMinusError = getDoubleValue(error);
						if (yPlusError != null) {
							yError = true;
							// There is plus error!
							yErrorType = 1;
						}
						if (yMinusError != null) {
							yError = true;
							// If there is plus error, then the type is both
							// plus and
							// minus error
							if (yErrorType == 1) {
								yErrorType = 3;
								// Otherwise, the type is just minus error
							} else {
								yErrorType = 2;
							}
						}
					}

				}

				// TODO Make some sort of better implementation for multiple
				// error sets on one data set.

				// Set the data to be plotted
				for (int i = 0; i < series.size(); i++) {
					double xPlus = 0;
					double xMinus = 0;
					double yPlus = 0;
					double yMinus = 0;
					// Get the error if it exists
					if (xError) {
						if (xErrorType == 1 || xErrorType == 3) {
							xPlus = xPlusError[i];
						}
						if (xErrorType == 2 || xErrorType == 3) {
							xMinus = xMinusError[i];
						}
					}
					// If there is error, get it
					if (yError) {
						// Gets the error associated with the point
						if (yErrorType == 1 || yErrorType == 3) {
							yPlus = yPlusError[i];
						}
						// Gets the error associated with the point
						if (yErrorType == 2 || yErrorType == 3) {
							yMinus = yMinusError[i];
						}
					}
					// Create the new point add add it to the trace
					Sample point = new Sample(xValues[i], yValues[i], yPlus,
							yMinus, xPlus, xMinus, Double.toString(xValues[i])
									+ ", " + Double.toString(yValues[i]));

					// Add the new point to the trace
					traceDataProvider.addSample(point);
				}

				// Creates a new trace with the name, axis,and provider to plot
				Trace trace = new Trace(series.getLabel(), xyGraph.primaryXAxis,
						xyGraph.primaryYAxis, traceDataProvider);

				// Get the series style to configure the trace
				XYZSeriesStyle style = (XYZSeriesStyle) series.getStyle();

				// Sets trace properties
				trace.setAntiAliasing((boolean) style
						.getProperty(XYZSeriesStyle.ANTI_ALIASING));
				trace.setAreaAlpha(
						(int) style.getProperty(XYZSeriesStyle.AREA_ALPHA));

				// Sets the trace color if it is set in the style
				if (style.getProperty(XYZSeriesStyle.COLOR) != null) {
					trace.setTraceColor(
							(Color) style.getProperty(XYZSeriesStyle.COLOR));
				}

				// Sets the trace type if it has been set
				if (style.getProperty(XYZSeriesStyle.TYPE) != null) {
					trace.setTraceType(
							(TraceType) style.getProperty(XYZSeriesStyle.TYPE));
				}

				// Sets the point style for this trace
				if (style.getProperty(XYZSeriesStyle.POINT) != null) {
					trace.setPointStyle((PointStyle) style
							.getProperty(XYZSeriesStyle.POINT));
				}

				// Sets the line's width for this trace.
				trace.setLineWidth(
						(int) style.getProperty(XYZSeriesStyle.LINE_WIDTH));

				// Set the point size (radius) for the points on the trace
				trace.setPointSize(
						(int) style.getProperty(XYZSeriesStyle.POINT_SIZE));

				// If there is error, then enable error bars and the proper
				// error options on the trace
				if (xError || yError) {
					trace.setErrorBarEnabled(true);
					// Get the error style from the series map
					BasicErrorStyle errStyle = null;
					errStyle = (BasicErrorStyle) (seriesMap.get(series)).get(0)
							.getStyle();
					trace.setErrorBarCapWidth((int) errStyle
							.getProperty(BasicErrorStyle.ERROR_BAR_CAP_WIDTH));
					// Set the error bar color if available
					if (errStyle.getProperty(BasicErrorStyle.COLOR) != null) {
						trace.setErrorBarColor((Color) errStyle
								.getProperty(BasicErrorStyle.COLOR));
					} else {
						// Sets the color for the error bars to default to red
						trace.setErrorBarColor(XYGraphMediaFactory.getInstance()
								.getColor(new RGB(255, 50, 50)));
					}
					// Set the error bar type, dependent on which type of error
					// is present for the series
					switch (xErrorType) {
					case 1:
						trace.setXErrorBarType(ErrorBarType.PLUS);
						break;
					case 2:
						trace.setXErrorBarType(ErrorBarType.MINUS);
						break;
					case 3:
						trace.setXErrorBarType(ErrorBarType.BOTH);
						break;
					default:
						trace.setXErrorBarType(ErrorBarType.NONE);
					}

					// Do the same for the y error
					switch (yErrorType) {
					case 1:
						trace.setYErrorBarType(ErrorBarType.PLUS);
						break;
					case 2:
						trace.setYErrorBarType(ErrorBarType.MINUS);
						break;
					case 3:
						trace.setYErrorBarType(ErrorBarType.BOTH);
						break;
					default:
						trace.setYErrorBarType(ErrorBarType.NONE);
					}
				}

				// Add the trace that was just created
				this.existingTraces.put(series, trace);
				xyGraph.addTrace(trace);

			}

		}

		// Repaint the figure
		// TODO- Run on separate thread?
		xyGraph.repaint();

		// TODO Should we reset the scale of the graph? It has its perks but
		// likely not
		// newXYGraph.performAutoScale();

		return;

	}

	/**
	 * Gets the double values of the specified series for all of its data
	 * points. If the series data points cannot be cast to Double, then throws
	 * an exception.
	 * 
	 * @param series
	 *            The series to retrieve the data from
	 * @return Returns an array of double values to use
	 */
	private double[] getDoubleValue(ISeries series) {
		// Try to get valid data from the series, and convert to double values
		CSVSeries csvData = (CSVSeries) series;
		double[] newArray = new double[csvData.size()];
		for (int i = 0; i < csvData.size(); i++) {
			newArray[i] = (Double) csvData.get(i);
		}
		return newArray;
	}

	/**
	 * This method implements the Composite to hold the slider for moving
	 * through plotted file sets.
	 * 
	 * @param sliderComp
	 *            The Composite to create the widgets in
	 * @param displayPlotProvider
	 *            The PlotProvider containing the information to create the plot
	 */
	private Composite createSliderComp(Composite parent,
			final PlotProvider displayPlotProvider) {

		final Composite sliderComp = new Composite(parent, SWT.NONE);

		// Set the layout and layout data for this Composite
		sliderComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sliderComp.setLayout(new GridLayout(5, false));

		// The label for the slider
		Label timeButtons = new Label(sliderComp, SWT.FILL);
		timeButtons.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		timeButtons.setText("Slider: ");

		// The slider itself
		final Slider slider = new Slider(sliderComp, SWT.HORIZONTAL);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Set the selection, minimum, maximum, thumb, increment, and page
		// increment
		final int spinnerSliderMin = 0;
		final int spinnerSliderMax = displayPlotProvider.getTimes().size() - 1;
		slider.setValues(0, spinnerSliderMin, spinnerSliderMax, 1, 1, 1);

		// Create the label for the selected time
		final Label timeSelectedLabel = new Label(sliderComp, SWT.NONE);
		timeSelectedLabel.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		// Initially set it to the selected index
		timeSelectedLabel
				.setText(displayPlotProvider.getTimes().get(0).toString());

		// The buttons for moving up and moving down
		Button downSpinnerButton = new Button(sliderComp, SWT.PUSH);
		Button upSpinnerButton = new Button(sliderComp, SWT.PUSH);
		upSpinnerButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		upSpinnerButton.setText(">");
		downSpinnerButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		downSpinnerButton.setText("<");

		final Runnable setPlotTime = new Runnable() {
			@Override
			public void run() {
				// Get the slider's selection which is an index to the times
				int sliderValue = slider.getSelection();

				double time = displayPlotProvider.getTimes().get(sliderValue);
				showXYGraph(displayPlotProvider, time, false);
				timeSelectedLabel.setText(Double.toString(time));

				// Refresh the slider composite
				vizComposite.layout();
				sliderComp.layout();

				return;
			}
		};

		// The listener for the slider.
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Apply the new time to the plot.
				setPlotTime.run();
			}
		});

		// The listener for the up button in the slider.
		upSpinnerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Get the slider's selection which is an index to the times
				int sliderValue = slider.getSelection();

				// Check that the index is not out of bounds
				if (sliderValue < spinnerSliderMax) {
					// increment the index from the up button click
					sliderValue++;
					// Move the slider to the index of the selection
					slider.setSelection(sliderValue);

					// Apply the new time to the plot.
					setPlotTime.run();
				}

				return;
			}
		});
		// The listener for the down button in the slider.
		downSpinnerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Get the slider's selection which is an index to the times
				int sliderValue = slider.getSelection();

				// Check that the index is within bounds
				if (sliderValue > spinnerSliderMin) {
					// Decrement the index
					sliderValue--;
					// Move the slider to the index of the selection
					slider.setSelection(sliderValue);

					// Apply the new time to the plot.
					setPlotTime.run();
				}

				return;
			}
		});

		return sliderComp;
	}

	/**
	 * Gets the canvas used to render the CSV plot.
	 * 
	 * @return The plot canvas.
	 */
	public Canvas getPlotCanvas() {
		return plotCanvas;
	}
}
