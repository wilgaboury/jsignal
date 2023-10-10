package com.github.wilgaboury.sigui;

import io.github.humbleui.types.IRect;

import java.util.Arrays;

public class PixelNodeBuffer {
    private int width;
    private int height;
    private MetaNode[] nodes;

    public PixelNodeBuffer() {
        width = 0;
        height = 0;
        nodes = new MetaNode[0];
    }

    public void setSize(int width, int height) {
        ensureCapacity(width * height);
        this.width = width;
        this.height = height;
    }

    public void clear() {
        Arrays.fill(nodes, null);
    }

    public MetaNode pick(int x, int y) {
        return nodes[x + y * width];
    }

    public void fill(MetaNode node, IRect rect) {
        for (int row = rect.getTop(); row <= rect.getBottom(); row++) {
            int offset = row * width;
            Arrays.fill(nodes, offset + rect.getLeft(), offset + rect.getRight(), node);
        }
    }

    private void ensureCapacity(int newSize) {
        if (width * height < newSize) {
            nodes = new MetaNode[1 << log2(newSize) + 1]; // allocates more space than needed when pow of 2
        } else {
            clear();
        }
    }

    private static int log2( int bits ) {
        return bits == 0 ? 0 : 31 - Integer.numberOfLeadingZeros( bits );
    }

    public static void main(String[] args) {
        System.out.println(log2(7));
    }
}
