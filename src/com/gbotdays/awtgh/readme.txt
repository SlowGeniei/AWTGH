AWTGH (AWT Graphics Handler): A lightweight off-EDT rendering engine for Swing.

AWTGH drives a JPanel from a dedicated render thread, keeping the Swing Event
Dispatch Thread free for UI responsiveness. Rendering logic is supplied as a
simple lambda; no subclassing required.


QUICK START
This is all you need for a simple color-changing circle.

Add the dependency:

    Maven:
    <dependency>
        <groupId>com.gbotdays</groupId>
        <artifactId>awtgh</artifactId>
        <version>0.1.0</version>
    </dependency>

    Gradle:
    implementation 'com.gbotdays:awtgh:0.1.0'

Create a RenderPanel and call start():

	JFrame frame = new JFrame("awtgh");
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	RenderPanel panel = new RenderPanel(900, 500, 60, (g, w, h) -> {
		long t = System.currentTimeMillis();
        	float hue = (t % 3000) / 3000f;

        	g.setColor(Color.BLACK);
        	g.fillRect(0, 0, w, h);

        	g.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
        	g.fillOval(w / 2 - 50, h / 2 - 50, 100, 100);
	});

	frame.add(panel);
	frame.pack();
	frame.setVisible(true);
	panel.start();

See Example_ColorOval.java for the complete above code.
It is recommended to, first, examine Example_Basic.java to
familiarize oneself with the API. See below EXAMPLES.


HOW IT WORKS

awtgh has three components:

	RenderPanel:
		The only class that needs to be interacted with directly. It extends
		JPanel, owns the render thread, and manages its lifecycle. Hand it a lambda, and call
		start() to begin populating the panel with frames.

	GraphicsProcessor:
		 This is the render thread. For each frame, it renders the passed PaintLogic
		on a fresh BufferedImage, and then notifies the EDT to repaint. Frames are paced to
		the passed target FPS, or are unbounded. RenderPanel is designed as the API through which
		the user can control a GraphicsProcessor, but direct usage is perfectly viable for lower-level applications.

	PaintLogic:
		This class is a functional interface; effectively just a lambda that receives
		a Graphics2D context, a width, and a height. It is called once per frame and runs,
		crucially, on the rendering thread, not the EDT.


FEATURES

	Off-EDT rendering:
		Rendering is handled on a dedicated thread, allowing for high FPS without affecting UI
		responsiveness.
	Lambda-based API:
		Rendering logic is capable of being completely custom without any source-code edits through
		lambda-based function interfaces.
	Hot-swappable render logic:
		Rendering logic can be changed while the frame-rendering pipeline is running.
	Configurable target FPS:
		The rendering thread limits it speed to match a passed targetFPS variable. It can also run un-bounded
		for maximum possible frames.
	Hot-swappable frame size:
		The pixel size of frames can be changed while the frame-rendering pipeline is running.
	Clean thread shutdown on window close:
		The rendering thread has multiple shutdown avenues to ensure that the JVM exits properly.


EXAMPLES

	Note: To increase the speed of development, example implementation classes were created with the help
	of AI. Example files have been human-tested, and thoroughly showcase this tool's capabilities. RenderPanel,
	GraphicsProcessor, and PaintLogic classes are human-written.

	Example_Basic.java
		The barest minimum. This example contains only a black square, with no animation.
	Example_ColorOval.java
		This is the complete code for the QUICK START example. This example features a color-changing
		circle.
	Example_BouncingBall.java
		This example contains a ball that bounces around inside the rectangle defined by the width
		and height variables.
	Exampe_ParticleSimulation.java
		A complex example that showcases the versatility of awtgh. It contains a 600-particle simulation that
		swaps between being attracted to the mouse, repelled by the mouse, or drifting free of the mouse through
		keyboard inputs. Automatically resizes the output frame's dimensions when the window resizes.


BUILDING FROM SOURCE

	//TODO: Add GitHub repo

LICENSE

	//TODO: Add License