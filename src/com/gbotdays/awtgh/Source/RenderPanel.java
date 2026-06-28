package com.gbotdays.awtgh.Source;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A Swing panel that manages and displays output from a {@link GraphicsProcessor} instance.
 * <p>
 * {@code RenderPanel} serves as the administrative layer between the user and the
 * rendering engine. It creates and owns a {@link GraphicsProcessor}, handles the
 * Swing-side display of completed frames, and ensures that the dedicated threads
 * are safely disposed of.
 * <p>
 * The parent {@link JFrame}'s default close operation should be set to
 * {@link javax.swing.WindowConstants#EXIT_ON_CLOSE} or
 * {@link javax.swing.WindowConstants#DISPOSE_ON_CLOSE}.
 * The panel registers a {@link java.awt.event.WindowListener} to call {@link #stop()}
 * on the {@code windowClosed}.
 * <p>
 * It is worth noting that this panel does not require a parent component to function.
 * The JPanel can function as an output in itself.
 */

@SuppressWarnings("serial") //SerialUID is not strictly necessary
public class RenderPanel extends JPanel {

	/**The width, in pixels, of the drawing area*/
	private int width;
	
	/**The height, in pixels, of the drawing area*/
	private int height;
	
    /**
     * The target number of frames to render per second.
     * A value that is {@code <= 0} or {@code > 1000} results in un-bounded frame rate.
     */
	private int targetFPS;
	
    /** A {@link Dimension} wrapping {@link #width} and {@link #height}. Used to size the panel.*/
	private Dimension preferredSize;
	
	/**The local instance of {@link GraphicsProcessor}*/
	private GraphicsProcessor graphicsProcessor;
	
    /**
     * The most recently completed frame, retrieved from {@link GraphicsProcessor}
     * and painted onto this panel by {@link #paintComponent(Graphics)}.
     */
	private BufferedImage latestBuffer;
	
    /** The rendering logic passed through to {@link GraphicsProcessor}.*/
	private PaintLogic paintLogic;
	
    /**
     * Creates and initializes a new {@link RenderPanel}.
     *
     * @param width			The width, in pixels, of the drawing area.
     * @param height		The height, in pixels, of the drawing area.
     * @param targetFPS		The target number of frames to render per second.
     * 						A value that is {@code <= 0} or {@code > 1000} results in
     * 						an un-bounded frame rate. Actual FPS will be slightly lower
     * 						than the target due to truncated integer millisecond timing.
     * @param paintLogic	The rendering logic, usually as a lambda, that is used to render each frame.
     */
	public RenderPanel (int width, int height, int targetFPS, PaintLogic paintLogic) {
		
		this.paintLogic = paintLogic;
		
		preferredSize = new Dimension(width, height);
		this.setPreferredSize(preferredSize);
		
		this.width = width;
		this.height = height;
		this.targetFPS = targetFPS;
	}
	
	/**
     * Creates and initializes a new {@link RenderPanel}.
     *
     * @param size			A {@link Dimension} instance that defines the pixel size of each frame
     * @param targetFPS  	The target number of frames to render per second.
     * 						A value that is {@code <= 0} or {@code > 1000} results in
     * 						an un-bounded frame rate. Actual FPS will be slightly lower
     * 						than the target due to truncated integer millisecond timing.
     * @param paintLogic	The rendering logic, usually as a lambda, that is used to render each frame.
     */
	public RenderPanel (Dimension size, int targetFPS, PaintLogic paintLogic) {
		
		this.paintLogic = paintLogic;
		
		preferredSize = size;
		
		this.width = (int) preferredSize.getWidth();
		this.height = (int) preferredSize.getHeight();
		this.targetFPS = targetFPS;
	}
	
    /**
     * Safely stops the {@link GraphicsProcessor} rendering thread.
     * <p>
     * Calling {@link GraphicsProcessor#stop()} would discard the current frame.
     */
	public void stop () {graphicsProcessor.stopThread();}
	
	/**
	 * Creates a new {@link GraphicsProcessor} instance and starts the rendering thread.
	 * <p>
	 * Calling this method overwrites the current {@link GraphicsProcessor} instance.
	 */
	public void start () {
		
		graphicsProcessor = new GraphicsProcessor (width, height, targetFPS, this, paintLogic);
		latestBuffer = graphicsProcessor.getLatestBuffer();
		graphicsProcessor.start();
	}
	
    /**
     * Replaces the active rendering logic without rebuilding {@link #graphicsProcessor}.
     * <p>
     * The new logic will be implemented as soon as the current frame is finished. 
     *
     * @param paintLogic	The new rendering logic to use
     */
	public void setPaintLogic (PaintLogic paintLogic) {
		this.paintLogic = paintLogic;
		graphicsProcessor.setPaintLogic(paintLogic);
	}
	
	/**
	 * Exposes the frame-size that is currently being used
	 * @return	The current size, in pixels, of the generated frames
	 */
	public Dimension getFrameSize () {return preferredSize;}
	
	/**
	 * Allows for editing of the size of generated frames without rebuilding {@link #graphicsProcessor}.
	 * @param preferredSize		The new size, in pixels, to use to generate frames
	 */
	public void setFrameSize (Dimension preferredSize) {
		graphicsProcessor.setWidth((int) preferredSize.getWidth());
		graphicsProcessor.setHeight((int) preferredSize.getHeight());
		this.preferredSize = preferredSize;
	}
	
    /**
     * Paints the most recently completed frame onto this panel.
     * <p>
     * Called by the EDT via Swing's repaint mechanism.
     * Updates {@link #latestBuffer} with the latest frame from {@link #graphicsProcessor}.
     * 
     * @param g		The {@link Graphics} context
     */
	@Override
	public void paintComponent (Graphics g) {
		
		super.paintComponent(g);
		if (graphicsProcessor != null) {latestBuffer = graphicsProcessor.getLatestBuffer();}
		g.drawImage(latestBuffer, 0, 0, null);
	}
	
    /**
     * Called by the EDT when this panel is added to a window hierarchy.
     * <p>
     * Registers a {@link java.awt.event.WindowListener} on the parent {@link JFrame}
     * that calls {@link #stop()} when the window is closed, ensuring the
     * {@link GraphicsProcessor} thread is shut down cleanly.
     * <p>
     * If the parent object is not a JFrame, no listener is added.
     */
	@Override
	public void addNotify() {
	    super.addNotify();
	    Window window = SwingUtilities.getWindowAncestor(this);
	    if (window instanceof JFrame frame) {
	        frame.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosed(WindowEvent e) {
	                stop();
	            }
	        });
	    }
	}
}
