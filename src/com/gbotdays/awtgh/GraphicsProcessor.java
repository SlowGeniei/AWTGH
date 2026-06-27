package com.gbotdays.awtgh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

/**
 * A dedicated rendering thread that produces frames as {@link BufferedImage} objects.
 * <p>
 * Each frame is built by invoking the supplied {@link PaintLogic} on a fresh
 * {@link BufferedImage}, which is then stored in {@link #latestFinishedBuffer} for
 * retrieval by the local {@link RenderPanel} instance. Once a frame is rendered,
 * the {@link RenderPanel} is notified via {@link SwingUtilities#invokeLater} to
 * repaint on the EDT.
 * <p>
 * Frame pacing is achieved by sleeping for the remainder of each frame's allotted
 * time after rendering completes. Because frame time is calculated using integer
 * milliseconds, the actual frame rate will be slightly lower than {@link #TARGET_FPS}.
 */
public class GraphicsProcessor extends Thread {
	
    /**
     * The target number of frames to render per second.
     * <p>
     * A value that is {@code <= 0} or {@code > 1000} results in an un-bounded
     * frame rate ({@link #FRAME_TIME} is set to {@code 0}).
     */
	private final long TARGET_FPS;
	
    /**
     * The number of milliseconds allotted to each frame. Derived from {@link #TARGET_FPS}
     * as {@code 1000 / TARGET_FPS} using truncated integer division.
     * <p>
     * Because only whole milliseconds are used, the actual frame rate will be slightly
     * lower than {@link #TARGET_FPS}. Set to {@code 0} when the frame rate is un-bounded.
     */
	private final long FRAME_TIME;
	
    /**
     * The most recently completed frame.
     * <p>
     * Written by the rendering thread in {@link #paintBuffer()} and read by the
     * EDT via {@link RenderPanel#paintComponent}. Declared {@code volatile} to
     * ensure security across threads.
     */
	private volatile BufferedImage latestFinishedBuffer;
	
	/**
     * Controls the rendering loop in {@link #run()}.
     * <p>
     * Set to {@code false} by {@link #stopThread()} to signal the rendering loop to exit
     * after the current frame completes. Allows for safe thread shutdown.
     * <p>Declared {@code volatile} to ensure cross-thread viewing security.
     */
	private volatile boolean isRunning = true;
	
	/**
	 * The width of the drawing area.
	 * Marked as {@code volatile} because external threads are
	 * able to edit this variable, via {@link #setWidth(int)}, mid-frame.
	 */
	volatile private int imageWidth;
	
	/**
	 * The height of the drawing area.
	 * Marked as {@code volatile} because external threads are
	 * able to edit this variable, via {@link #setHeight(int)}, mid-frame.
	 */
	volatile private int imageHeight;
	
	/**The JPanel that retrieves and displays completed frames*/
	private RenderPanel renderPanel;
	
    /**
     * The rendering logic used to render each frame.
     * Marked as {@code volatile} because external threads are
     * able to switch out rendering logic mid-frame.
     * */
	volatile private PaintLogic paintLogic;
	
    /**
     * Constructs a new {@link GraphicsProcessor} and pre-fills the initial buffer
     * with a dark gray color so that {@link #latestFinishedBuffer} is never {@code null}.
     * <p>
     * The thread is designed to be shutdown through {@link #stopThread()}.
     * Handles shutdowns that don't call {@link #stopThread()} gracefully.
     *
     * @param imageWidth   The width, in pixels, of the drawing area.
     * @param imageHeight  The height, in pixels, of the drawing area.
     * @param targetFPS    The target number of frames to render per second.
     *                     A value that is {@code <= 0} or {@code > 1000} results in
     *                     an un-bounded frame rate.
     * @param renderPanel  The {@link RenderPanel} that will display completed frames.
     * @param paintLogic   The rendering logic to be executed each frame.
     */
	public GraphicsProcessor (int imageWidth, int imageHeight, int targetFPS, RenderPanel renderPanel, PaintLogic paintLogic) {

		//Saving the rendering logic
		this.paintLogic = paintLogic;
		
		//Initialize the FPS logic variables
		this.TARGET_FPS = targetFPS;
		if (targetFPS > 1000 || targetFPS <= 0) {
			this.FRAME_TIME = 0;
		} else {this.FRAME_TIME = 1000 / TARGET_FPS;}
		
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.renderPanel = renderPanel;
		
		//Create an initial buffer so latestFinishedBuffer is always initialized
		latestFinishedBuffer = new BufferedImage (imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = latestFinishedBuffer.createGraphics();
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, imageWidth, imageHeight);
		g.dispose();
		
		setDaemon(true); //Simple defensive programming.
	}
	
    /**
     * Signals the rendering loop to stop after the current frame completes.
     * <p>
     * Use this over {@link GraphicsProcessor#stop()}, which could interrupt an
     * in-progress frame and leave {@link #latestFinishedBuffer} in an inconsistent state.
     * <p>
     * Sets {@link #isRunning} to {@code false}.
     */
	void stopThread () {
		isRunning = false;
	}
	
	/**
	 * Gets the latest completed frame in the form of a {@link BufferedImage}
	 * @return the latest generated {@link BufferedImage}.
	 */
	public BufferedImage getLatestBuffer () {return latestFinishedBuffer;}
	
	/**
	 * Sets rendering logic without rebuilding
	 * the {@link GraphicsProcessor} object.
	 * <p>
	 * The new logic will be used for subsequent frames.
	 * @param paintLogic	The rendering logic
	 */
	public void setPaintLogic (PaintLogic paintLogic) {this.paintLogic = paintLogic;}
	
	/**
	 * Allows for editing of the size of generated frames without discarding this instance
	 * @param height	The height, in pixels, of subsequent frames
	 */
	public void setHeight (int height) {this.imageHeight = height;}
	
	/**
	 * Allows for editing of the size of generated frames without discarding this instance
	 * @param width		The width, in pixels, of subsequent frames
	 */
	public void setWidth (int width) {this.imageWidth = width;}
	
    /**
     * Renders a frame and stores the result in {@link #latestFinishedBuffer}.
     * <p>
     * Allocates a new {@link BufferedImage}, invokes {@link PaintLogic#paint} on it,
     * then updates {@link #latestFinishedBuffer}.
     * The {@link Graphics2D} context is disposed of before this method returns.
     */
	private void paintBuffer () {
		
		BufferedImage buffer = new BufferedImage (imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = buffer.createGraphics();
		
		paintLogic.paint(g, imageWidth, imageHeight);
		
		g.dispose();
		
		//Save the new buffer (the old buffer will now be garbage collected)
		latestFinishedBuffer = buffer;
	}
	
    /**
     * The main rendering loop. Invoked by {@link Thread#start()} and runs either until
     * {@link #isRunning} is set to {@code false} (likely by {@link #stopThread()}), or until the
     * JVM is force-quit.
     * <p>
     * Each iteration: renders a frame via {@link #paintBuffer()}, schedules a
     * repaint on the EDT via {@link SwingUtilities#invokeLater}, then sleeps for
     * the remainder of the frame's allotted time. If the frame took longer than
     * {@link #FRAME_TIME} to render, the sleep is skipped and the next frame
     * begins immediately.
     */
	@Override
	public void run () {
		
		//The infinite loop
		while (isRunning) {
			long startTime = System.currentTimeMillis();
			
			//The drawing logic
			paintBuffer();
			
			//Notify the JPanel
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run () {renderPanel.repaint();}
			});
			
			//This block makes sure each frame takes - at minimum - FRAME_TIME milliseconds
			long totalTime = System.currentTimeMillis() - startTime;
			if (totalTime < FRAME_TIME) {
				
				try {sleep (FRAME_TIME - totalTime);}
				catch (InterruptedException e) {
					
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
