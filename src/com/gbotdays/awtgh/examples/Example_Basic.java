package com.gbotdays.awtgh.examples;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.gbotdays.awtgh.source.RenderPanel;

/**
 * A basic example class that showcases
 * the barest-minimum usage of AWTGH.
 */
public class Example_Basic {

	int panelX = 900;
	int panelY = 500;
	JFrame jf;
	
	RenderPanel renderPanel;
	
	public Example_Basic () {
		
		//Initialize JFrame
		jf = new JFrame();
		jf.setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
		
		renderPanel = new RenderPanel (panelX, panelY, 30, (g, w, h) -> {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, w, h);
		});
		
		renderPanel.start();
		
		jf.add(renderPanel);
		jf.pack();
		
		jf.setVisible (true);
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            new Example_Basic();
	        }
	    });
	}
}
