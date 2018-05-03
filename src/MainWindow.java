import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import math.*;

public class MainWindow {

    // The window handle
    private long window;
    private int windowWidth = 1080;
    private int windowHeight = 720;
    private Camera camera;

    private ArrayList<BasicGameItem> basicGameItems = new ArrayList<BasicGameItem>();
    private Mesh mesh;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(windowWidth, windowHeight, "Necro IO", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        glfwWindowHint(GLFW_STENCIL_BITS, 4);
        glfwWindowHint(GLFW_SAMPLES, 4);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);

        // Set the clear color
        glClearColor(0.2f, 0.3f, 0.3f, 0.0f);

        float[] vertices = new float[]{
                // VO
                -0.5f,  0.5f,  0.5f,
                // V1
                -0.5f, -0.5f,  0.5f,
                // V2
                0.5f, -0.5f,  0.5f,
                // V3
                0.5f,  0.5f,  0.5f,
                // V4
                -0.5f,  0.5f, -0.5f,
                // V5
                0.5f,  0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,
        };

        float[] textureCoords = new float[]{
                // VO
                0.0f, 0.0f,
                // V1
                0.0f, 0.5f,
                // V2
                0.5f, 0.5f,
                // V3
                0.5f, 0.0f,
                // V4
                0.0f, 0.5f,
                // V5
                0.5f,  0.5f,
                // V6
                0.5f, 0.5f,
                // V7
                0.5f, 0.5f
        };

        int[] indices = new int[] {
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                4, 0, 3, 5, 4, 3,
                // Right face
                3, 2, 7, 5, 3, 7,
                // Left face
                6, 1, 0, 6, 0, 4,
                // Bottom face
                2, 1, 6, 2, 6, 7,
                // Back face
                7, 6, 4, 7, 4, 5,
        };

        mesh = new Mesh(vertices, textureCoords, indices);

        try {
            BasicGameItem myBasicGameItem = new BasicGameItem(mesh);
            myBasicGameItem.setPosition(-0.5f, 0, -15);
            //myBasicGameItem.setRotation(-45.0f, 0, 0);
            //myBasicGameItem.setRotation(0, -45.0f, 0);
           // myBasicGameItem.setRotation(0, 0, -90.0f);
            myBasicGameItem.setScale(new Vector3f(1.0f, 1.0f, 1.0f));
            basicGameItems.add(myBasicGameItem);

            BasicGameItem basicGameItem2 = new BasicGameItem(mesh);
            basicGameItem2.setPosition(0.5f, 0, 0);
            basicGameItem2.setScale(new Vector3f(2.0f, 2.0f, 1.0f));
            //basicGameItems.add(basicGameItem2);
        } catch(Exception e) {
            System.out.println("Could not add Game Item!");
            System.out.println(e);
        }

        Camera camera = new Camera(windowWidth /  (float)windowHeight);


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            //mesh.draw(camera.getProjectionMatrix());
            for( BasicGameItem item : basicGameItems) {
                item.draw(camera.getProjectionMatrix());
                Vector3f rot = item.getRotation();
                item.setRotation(rot.x , rot.y + 1, rot.z );
            }


            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        mesh.cleanUp();

    }

    public static void main(String[] args) {
        new MainWindow().run();
    }

}