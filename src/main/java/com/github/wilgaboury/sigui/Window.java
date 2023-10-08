package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.yoga.Yoga;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createContext;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
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
        long handle = glfwCreateWindow(500, 500, "", NULL, NULL);
        var window = new Window(handle, root);
        windows.add(window);
        return window;
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
        root.get().ifPresent(r -> r.visit(n -> n.getNode().render(n.getYoga())));
    }
}
