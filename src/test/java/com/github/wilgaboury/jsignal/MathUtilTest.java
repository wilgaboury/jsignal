package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.sigui.MathUtil;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.junit.jupiter.api.Test;

public class MathUtilTest {
    @Test
    public void testMatrixInverse() {
        Matrix33 mat = new Matrix33(1, 2, 3, 4, 5, 6, 7, 2, 9);
        Matrix33 inv = MathUtil.inverse(mat);
        System.out.println(inv);

        mat = new Matrix33(1f, -0f, -380f, -0f, 1f, -0f, 0f, -0f, 1f);
        var point = MathUtil.apply(mat, new Point(0, 0));
        System.out.println("hi");
    }
}
