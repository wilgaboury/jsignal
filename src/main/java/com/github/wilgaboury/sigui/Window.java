package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.yoga.Yoga;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createContext;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public static final Context<Window> CONTEXT = createContext(null);

    static final Set<Window> windows = new HashSet<>();

    private final long window;
    private final Signal<Optional<MetaNode>> root;

    public Window(long window, Component root) {
        this.window = window;
        this.root = MetaNode.create(root);
    }

    public static Window create(Component root) {
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        long handle = glfwCreateWindow(300, 300, "", NULL, NULL);
        if ( handle == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        var window = new Window(handle, root);
        window.layout();
        windows.add(window);

        glfwSetKeyCallback(handle, (h, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                SiguiThread.invokeLater(() -> window.close());
                // glfwSetWindowShouldClose(h, true); // We will detect this in the rendering loop
        });

        // center the window
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(handle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(handle);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        SiguiThread.invokeLater(() -> {
            glfwShowWindow(handle);
        });

        return window;
    }

    public void close() {
        glfwDestroyWindow(window);
        windows.remove(this);
    }

    void layout() {
        root.get().ifPresent(r -> {
            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                glfwGetWindowSize(window, width, height);

                Yoga.nYGNodeCalculateLayout(r.getYoga(), width.get(0), height.get(0), Yoga.YGDirectionLTR);
            }
        });
    }

    void render() {
        if (glfwWindowShouldClose(window)) {
            close();
            return;
        }

        glfwMakeContextCurrent(window);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        root.get().ifPresent(r -> r.visit(n -> n.getNode().render(n.getYoga())));

        glfwSwapBuffers(window); // swap the color buffers
    }
}
