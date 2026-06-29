//File under MIT license.
//For details, see file "~/LICENSE" in github repo "https://github.com/SlowGeniei/AWTGH"

package com.gbotdays.awtgh.examples;

import java.awt.Color;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.gbotdays.awtgh.source.RenderPanel;

/**
 * An example class that showcases
 * a possible use of AWTGH; that being:
 * fast refresh with medium-complexity rendering
 * logic.
 */
public class Example_BouncingBall {

	int panelX = 900;
	int panelY = 500;
	JFrame jf;
	
	RenderPanel renderPanel;
	
	public Example_BouncingBall () {
		

        int width = 900;
        int height = 500;
        int radius = 30;

        /* If a user is unaccustomed to the below declarations:
         * This is a common method of allowing variable-editing while within
         * a lambda.
         * */
        double[] x = {width / 2.0};
        double[] y = {height / 2.0};
        double[] vx = {4.0};
        double[] vy = {3.0};

        JFrame frame = new JFrame("AWTGH : bouncing ball");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        RenderPanel panel = new RenderPanel(width, height, 60, (g, w, h) -> {

            x[0] += vx[0];
            y[0] += vy[0];

            if (x[0] - radius < 0) {x[0] = radius; vx[0] = Math.abs(vx[0]);}
            if (x[0] + radius > w) {x[0] = w - radius; vx[0] = -Math.abs(vx[0]);}
            if (y[0] - radius < 0) {y[0] = radius; vy[0] = Math.abs(vy[0]);}
            if (y[0] + radius > h) {y[0] = h - radius; vy[0] = -Math.abs(vy[0]);}

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillOval((int)(x[0] - radius), (int)(y[0] - radius), radius * 2, radius * 2);
        });

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        panel.start();
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            new Example_BouncingBall();
	        }
	    });
	}
}
