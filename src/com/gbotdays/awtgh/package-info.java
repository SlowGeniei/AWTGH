/**
 * A lightweight off-EDT rendering engine for Swing applications.
 *
 * <p>This package provides a simple three-component architecture for rendering
 * graphics on a dedicated thread, keeping the Swing Event Dispatch Thread free
 * for UI responsiveness.
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link com.gbotdays.awtgh.PaintLogic}: a functional interface that defines
 *       per-frame rendering logic. Generally supplied as a lambda.</li>
 *   <li>{@link com.gbotdays.awtgh.GraphicsProcessor}: a dedicated rendering thread
 *       that invokes {@code PaintLogic} each frame, producing a {@link java.awt.image.BufferedImage}.</li>
 *   <li>{@link com.gbotdays.awtgh.RenderPanel}: a {@link javax.swing.JPanel} that owns
 *       a {@code GraphicsProcessor}, displays completed frames, and manages thread
 *       lifecycle alongside its parent window.</li>
 * </ul>
 *
 * <h2>Threading model</h2>
 * <p>Rendering occurs entirely on {@code GraphicsProcessor}'s dedicated thread.
 * Completed frames are handed to the EDT via {@link javax.swing.SwingUtilities#invokeLater},
 * which triggers a repaint on {@code RenderPanel}. The two threads communicate
 * through a {@code volatile} {@link java.awt.image.BufferedImage} reference,
 * avoiding the need for explicit locking.
 *
 * <h2>Usage</h2>
 * <p>In typical use, only {@link com.gbotdays.awtgh.RenderPanel} and
 * {@link com.gbotdays.awtgh.PaintLogic} are referenced directly.
 * Construct a {@code RenderPanel} with a size, target FPS, and a
 * {@code PaintLogic} lambda, then add it to a {@link javax.swing.JFrame}:
 *
 * <pre>{@code
 * RenderPanel panel = new RenderPanel(new Dimension(900, 500), 30, (g, w, h) -> {
 *     //Rendering logic
 * });
 * frame.add(panel);
 * frame.pack();
 * }</pre>
 *
 * <p>See {@code Example_BouncingBall.java} for a complete working setup.
 * <p>For increased customizability, {@link com.gbotdays.awtgh.GraphicsProcessor#run()} can be manually edited to include
 * rendering logic that lambda's format can't support.
 */
package com.gbotdays.awtgh;