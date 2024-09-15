package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Picture;
import io.github.humbleui.skija.PictureRecorder;

import java.util.function.Consumer;

public class PicturePaintCacheStrategy implements PaintCacheStrategy {
  private Picture picture;

  @Override
  public boolean isDirty() {
    return picture == null;
  }

  @Override
  public void markDirty() {
    picture.close();
    picture = null;
  }

  @Override
  public void paint(Canvas canvas, UseNode useNode, Consumer<Canvas> orElse) {
    if (picture == null) {
      try (var recorder = new PictureRecorder()) {
        var recordingCanvas = useNode.use(meta -> recorder.beginRecording(meta.getLayout().getBoundingRect()));
        orElse.accept(recordingCanvas);
        picture = recorder.finishRecordingAsPicture();
      }
    }
    canvas.drawPicture(picture);
  }
}
