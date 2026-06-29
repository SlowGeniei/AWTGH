# AWTGH (AWT Graphics Handler)

A lightweight off-EDT rendering engine for Swing. AWTGH drives a `JPanel` from a dedicated render thread, keeping the Swing Event Dispatch Thread free for UI responsiveness. Rendering logic is supplied as a simple lambda, meaning no subclassing is required.

---

## Quick Start

**Add the dependency:**
This project is currently only published to github packages. To encorporate the tool into a project, the developer must:

### For Maven
Add
```xml
<settings>
  <servers>
    <server>
      <id>AWTGH</id>
      <username>GITHUB_USER</username>
      <password>GITHUB_PAT_TOKEN</password>
    </server>
  </servers>
</settings>
```
to the project's "~/.m2/settings.xml", and
```xml
<repositories>
  <repository>
    <id>github-yourrepo</id>
    <url>https://maven.pkg.github.com/OWNER/REPO</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.yourgroup</groupId>
    <artifactId>your-artifact</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```
in the project's "pom.xml" file.

### For Gradle
Add
```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```
to the project's "build.gradle" file, and
```properties
gpr.user=GITHUB_USER
gpr.key=GITHUB_PAT_TOKEN
```
to the project's "~/.gradle/gradle.properties".

### The implementation code (For both Gradle and Maven)
**Create a `RenderPanel` and call `start()`:**

```java
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
```

See `Example_ColorOval.java` for the complete code above. It is recommended to first examine `Example_Basic.java` to familiarize yourself with the API.

---

## How It Works

AWTGH has three components:

### `RenderPanel`
The only class that needs to be interacted with directly. It extends `JPanel`, owns the render thread, and manages its lifecycle. Hand it a lambda and call `start()` to begin populating the panel with frames.

### `GraphicsProcessor`
The render thread. For each frame, it renders the provided `PaintLogic` on a fresh `BufferedImage`, then notifies the EDT to repaint. Frames are paced to the target FPS, or run unbounded. `RenderPanel` is the intended API for controlling a `GraphicsProcessor`, but direct usage is perfectly viable for lower-level applications.

### `PaintLogic`
A functional interface; essentially a lambda that receives a `Graphics2D` context, a width, and a height. It is called once per frame and runs on the rendering thread, not the EDT.

---

## Features

- **Off-EDT rendering**
	- Rendering is handled on a dedicated thread, allowing for high FPS without affecting UI responsiveness.
- **Lambda-based API**
	- Rendering logic can be fully customized without any source-code edits, through lambda-based functional interfaces.
- **Hot-swappable render logic**
	- Rendering logic can be changed while the frame-rendering pipeline is running.
- **Configurable target FPS**
	- The rendering thread limits its speed to match a passed `targetFPS` value, or can run unbounded for maximum throughput.
- **Hot-swappable frame size**
	- The pixel size of frames can be changed while the pipeline is running.
- **Clean thread shutdown on window close**
	- The rendering thread has multiple shutdown avenues to ensure the JVM exits properly.

---

## Examples

> Note: To increase the speed of development, example implementation classes were created with the help of AI. Example files have been human-tested and thoroughly showcase this tool's capabilities. `RenderPanel`, `GraphicsProcessor`, and `PaintLogic` are human-written.

- **`Example_Basic.java`**
	- The bare minimum: a static black panel with no animation.
- **`Example_ColorOval.java`**
	- The complete Quick Start example: a color-cycling circle.
- **`Example_BouncingBall.java`**
	- A ball bouncing around inside the bounds of the panel.
- **`Example_ParticleSimulation.java`**
	- A 600-particle simulation that responds to mouse input (attract, repel, or idle modes), toggled via the spacebar. Handles window resizing automatically.

---

## Source

AWTGH is designed to be lightweight and portable. To this end, there are two equally viable ways to use this tool.

### Using the Source Directly
Since AWTGH is built using only 3 source files, simply copying the files into a separate project is completely viable.
Copy these files:
```
GraphicsProcessor.java
RenderPanel.java
PaintLogic.java
```
and add them to the recipient project's /src/project/ folder. They will then compile with the rest of the project.

Alternatively, simply copy the files into the working directory of whichever IDE the developer is using.

### Using the .jar
This github repo contains a pre-compiled .jar file, which can be added to any project using the maven and/or gradle dependencies outlined in the QUICK START section.

Alternatively, a developer can compile the project for themselves by running the following commands:
```bash
git clone https://github.com/TODO/awtgh.git
cd awtgh
javac -d out src/com/gbotdays/awtgh/*.java
jar cf awtgh-custom.jar -C out .
```

The developer can then add the same dependencies as the first option, but with "system" as its scope and a new "systemPath" pair that points to the .jar.

---

## License

> TODO: Add license.