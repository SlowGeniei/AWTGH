package com.gbotdays.awtgh.source;

import java.awt.Graphics2D;

/**
 * A functional interface that defines the rendering logic for {@link GraphicsProcessor}.
 * <p>
 * Implement this interface as a lambda and pass it to {@link RenderPanel}
 * or {@link GraphicsProcessor} to define rendering logic.
 */
@FunctionalInterface
public interface PaintLogic {

	/**
     * Contains the rendering logic for each frame.
     * <p>
     * This method is called once per frame by {@link GraphicsProcessor} on its
     * dedicated rendering thread. The provided {@link Graphics2D} context is
     * already configured. Disposal of the {@link Graphics2D} object is handled
     * externally.
     *
     * @param g       The {@link Graphics2D} context used to draw the frame
     * @param width   The width, in pixels, of the drawing area
     * @param height  The height, in pixels, of the drawing area
     */
	void paint(Graphics2D g, int width, int height);
}
