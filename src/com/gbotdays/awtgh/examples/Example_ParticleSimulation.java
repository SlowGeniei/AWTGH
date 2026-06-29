//File under MIT license.
//For details, see file "~/LICENSE" in github repo "https://github.com/SlowGeniei/AWTGH"

package com.gbotdays.awtgh.examples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.gbotdays.awtgh.source.PaintLogic;
import com.gbotdays.awtgh.source.RenderPanel;

/**
 * An example class that showcases the versatility of AWTGH.
 * This example is a 600 particle simulation that responds in
 * real-time to user-input.
 * 
 * <p>The field of particles responds to the mouse cursor. Press SPACE to
 * cycle through three interaction modes:
 * <ul>
 *   <li><b>ATTRACT</b>: particles are pulled toward the cursor</li>
 *   <li><b>REPEL</b>: particles are pushed away from the cursor</li>
 *   <li><b>IDLE</b>: particles drift with no cursor influence</li>
 * </ul>
 *
 * <p>The window handles resizing gracefully.
 */
public class Example_ParticleSimulation {

    private static final int    PARTICLE_COUNT  = 600;
    private static final double SPEED_LIMIT     = 6.0;
    private static final double FORCE_RADIUS    = 180.0;
    private static final double FORCE_STRENGTH  = 0.45;
    private static final double FRICTION        = 0.97;
    private static final float  TRAIL_ALPHA     = 0.18f;
    
    private enum Mode { ATTRACT, REPEL, IDLE }

    private static class Particle {

        double x, y;
        double vx, vy;
        Color  color;

        Particle (double x, double y, Color color) {
            this.x = x; this.y = y;
            this.color = color;
        }

        void update (double mouseX, double mouseY, Mode mode, int w, int h) {

            if (mode != Mode.IDLE) {
                double dx = mouseX - x;
                double dy = mouseY - y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < FORCE_RADIUS && dist > 1.0) {
                    double force = (FORCE_RADIUS - dist) / FORCE_RADIUS * FORCE_STRENGTH;
                    double sign  = (mode == Mode.ATTRACT) ? 1.0 : -1.0;
                    vx += sign * (dx / dist) * force;
                    vy += sign * (dy / dist) * force;
                }
            }

            // Friction
            vx *= FRICTION;
            vy *= FRICTION;

            // Speed cap
            double speed = Math.sqrt(vx * vx + vy * vy);
            if (speed > SPEED_LIMIT) {
                vx = vx / speed * SPEED_LIMIT;
                vy = vy / speed * SPEED_LIMIT;
            }

            // Move
            x += vx;
            y += vy;

            // Wrap around edges
            if (x < 0)  x += w;
            if (x >= w) x -= w;
            if (y < 0)  y += h;
            if (y >= h) y -= h;
        }
    }

    private volatile double mouseX = 0;
    private volatile double mouseY = 0;
    private volatile Mode mode = Mode.ATTRACT;

    public Example_ParticleSimulation () {

        int startWidth  = 900;
        int startHeight = 500;

        List<Particle> particles = buildParticles(PARTICLE_COUNT, startWidth, startHeight);

        JFrame frame = new JFrame("awtgh : particle system; SPACE to change mode");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        RenderPanel panel = new RenderPanel(startWidth, startHeight, 60, buildLogic(particles));

        panel.addMouseMotionListener(new MouseMotionAdapter() {
        	
            @Override public void mouseMoved   (MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            @Override public void mouseDragged (MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });

        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
        	
            @Override public void keyPressed (KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                	
                    Mode[] values = Mode.values();
                    mode = values[(mode.ordinal() + 1) % values.length];
                }
            }
        });

        panel.addComponentListener(new ComponentAdapter() {
        	
            @Override public void componentResized (ComponentEvent e) {
            	
                Dimension size = panel.getSize();
                panel.setFrameSize(size);

                Random rng = new Random();
                int w = size.width;
                int h = size.height;
                for (Particle p : particles) {
                	
                    if (p.x >= w) p.x = rng.nextDouble() * w;
                    if (p.y >= h) p.y = rng.nextDouble() * h;
                }
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setResizable(true);
        frame.setVisible(true);
        panel.requestFocusInWindow();
        panel.start();
    }

    private PaintLogic buildLogic (List<Particle> particles) {

        Color fadeColor = new Color(0f, 0f, 0f, TRAIL_ALPHA);

        return (g, w, h) -> {

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(fadeColor);
            g.fillRect(0, 0, w, h);

            Mode currentMode = mode;
            for (Particle p : particles) {
            	
                p.update(mouseX, mouseY, currentMode, w, h);
                g.setColor(p.color);
                g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
            }

            if (currentMode != Mode.IDLE) {
            	
                Color ringColor = (currentMode == Mode.ATTRACT)
                        ? new Color(1f, 1f, 1f, 0.12f)
                        : new Color(1f, 0.3f, 0.2f, 0.15f);
                g.setColor(ringColor);
                g.setStroke(new BasicStroke(1.0f));
                int r = (int) FORCE_RADIUS;
                g.drawOval((int)(mouseX - r), (int)(mouseY - r), r * 2, r * 2);
            }

            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.setColor(new Color(1f, 1f, 1f, 0.4f));
            g.drawString("Mode: " + currentMode.name(), 12, h - 12);
        };
    }

    private static List<Particle> buildParticles (int count, int w, int h) {

        List<Particle> list = new ArrayList<>(count);
        Random rng = new Random();

        for (int i = 0; i < count; i++) {
        	
            float hue = (float) i / count;
            Color color = Color.getHSBColor(hue, 0.7f, 0.9f);
            list.add(new Particle(rng.nextDouble() * w, rng.nextDouble() * h, color));
        }

        return list;
    }

    public static void main (String[] args) {
    	
        SwingUtilities.invokeLater(Example_ParticleSimulation::new);
    }
}