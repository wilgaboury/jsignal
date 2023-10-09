package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigui.*;
import org.lwjgl.util.yoga.Yoga;

import static org.lwjgl.opengl.GL33C.*;

public class Triangle {
    private static final float[] vertices = {-0.5f,-0.5f,0f,
            0.5f, -0.5f, 0f,
            0f,0.5f,0f};
    private static final int[] indices = {0,1,2};

    public static Component create() {
        Ref<Mesh> mesh = new Ref<>();
        ReactiveUtil.useContext(Window.CONTEXT).invokeDuringRender(
                () -> mesh.set(MeshLoader.createMesh(vertices, indices)));

        return () -> new Node() {
            @Override
            public void layout(long node) {
                Yoga.YGNodeStyleSetWidth(node, 50f);
                Yoga.YGNodeStyleSetHeight(node, 50f);
            }

            @Override
            public void render(long node) {
                float width = Yoga.YGNodeLayoutGetWidth(node);
                float height = Yoga.YGNodeLayoutGetHeight(node);
                float x = Yoga.YGNodeLayoutGetLeft(node);
                float y = Yoga.YGNodeLayoutGetTop(node);

                glBindVertexArray(mesh.get().getVaoID());
                glEnableVertexAttribArray(0);
                glDrawElements(GL_TRIANGLES, mesh.get().getVertexCount(), GL_UNSIGNED_INT, 0);
                glDisableVertexAttribArray(0);
                glBindVertexArray(0);
            }
        };
    }
}
