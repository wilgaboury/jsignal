package org.jsignal.std;

import com.google.common.net.MediaType;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public record Svg(GraphicsNode graphics, SVGDocument document) {
  public static Svg fromBlob(Blob blob) {
    try {
      if (blob.mime() != MediaType.SVG_UTF_8) {
        throw new RuntimeException("Cannot use " + blob.mime() + " to create SVG");
      }

      var parser = XMLResourceDescriptor.getXMLParserClassName();
      var factory = new SAXSVGDocumentFactory(parser);
      SVGDocument document = factory.createSVGDocument("file://", new ByteArrayInputStream(blob.data()));
      var userAgent = new UserAgentAdapter();
      DocumentLoader loader = new DocumentLoader(userAgent);
      BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
      bridgeContext.setDynamic(true);
      var builder = new GVTBuilder();
      var graphics = builder.build(bridgeContext, document);

      return new Svg(graphics, document);
    } catch (IOException e) {
      throw new RuntimeException("IO exception in creating SVG object", e);
    }
  }
}
