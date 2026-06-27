package com.gbotdays.awtgh;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * An example class that showcases
 * AWTGH through a simple animation.
 */
public class Example_ColorOval {

	int panelX = 900;
	int panelY = 500;
	JFrame jf;
	
	RenderPanel panel;
	
	public Example_ColorOval () {
		
		jf = new JFrame();
		jf.setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
		
		panel = new RenderPanel(900, 500, 60, (g, w, h) -> {
		    long t = System.currentTimeMillis();
		    float hue = (t % 3000) / 3000f;

		    g.setColor(Color.BLACK);
		    g.fillRect(0, 0, w, h);

		    g.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
		    g.fillOval(w / 2 - 50, h / 2 - 50, 100, 100);
		});
		
		panel.start();
		
		jf.add(panel);
		jf.pack();
		
		jf.setVisible (true);
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            new Example_ColorOval();
	        }
	    });
	}
}
