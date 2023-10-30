package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.sigui.Util;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.junit.jupiter.api.Test;

public class UtilTest {
    @Test
    public void testMatrixInverse() {
        Matrix33 mat = new Matrix33(1, 2, 3, 4, 5, 6, 7, 2, 9);
        Matrix33 inv = Util.inverse(mat);
        System.out.println(inv);

        mat = new Matrix33(1f, -0f, -380f, -0f, 1f, -0f, 0f, -0f, 1f);
        var point = Util.apply(mat, new Point(0, 0));
        System.out.println("hi");
    }
}
