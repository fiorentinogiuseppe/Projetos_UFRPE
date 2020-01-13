package weka.gui.visualize;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.AttributedCharacterIterator;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
























public class PostscriptGraphics
  extends Graphics2D
{
  protected Rectangle m_extent;
  protected PrintStream m_printstream;
  protected GraphicsState m_psGraphicsState;
  protected GraphicsState m_localGraphicsState;
  protected static final boolean DEBUG = false;
  
  private class GraphicsState
  {
    protected Color m_currentColor;
    protected Font m_currentFont;
    protected Stroke m_currentStroke;
    protected int m_xOffset;
    protected int m_yOffset;
    protected double m_xScale;
    protected double m_yScale;
    
    GraphicsState()
    {
      m_currentColor = Color.white;
      m_currentFont = new Font("Courier", 0, 11);
      m_currentStroke = new BasicStroke();
      m_xOffset = 0;
      m_yOffset = 0;
      m_xScale = 1.0D;
      m_yScale = 1.0D;
    }
    




    GraphicsState(GraphicsState copy)
    {
      m_currentColor = m_currentColor;
      m_currentFont = m_currentFont;
      m_currentStroke = m_currentStroke;
      m_xOffset = m_xOffset;
      m_yOffset = m_yOffset;
      m_xScale = m_xScale;
      m_yScale = m_yScale;
    }
    
    protected Stroke getStroke()
    {
      return m_currentStroke;
    }
    
    protected void setStroke(Stroke s) {
      m_currentStroke = s;
    }
    
    protected Font getFont()
    {
      return m_currentFont;
    }
    
    protected void setFont(Font f) {
      m_currentFont = f;
    }
    
    protected Color getColor()
    {
      return m_currentColor;
    }
    
    protected void setColor(Color c) {
      m_currentColor = c;
    }
    
    protected void setXOffset(int xo)
    {
      m_xOffset = xo;
    }
    
    protected void setYOffset(int yo) {
      m_yOffset = yo;
    }
    
    protected int getXOffset() {
      return m_xOffset;
    }
    
    protected int getYOffset() {
      return m_yOffset;
    }
    
    protected void setXScale(double x) {
      m_xScale = x;
    }
    
    protected void setYScale(double y) {
      m_yScale = y;
    }
    
    protected double getXScale() {
      return m_xScale;
    }
    
    protected double getYScale() {
      return m_yScale;
    }
  }
  
























  protected static Hashtable m_PSFontReplacement = new Hashtable();
  static { m_PSFontReplacement.put("SansSerif.plain", "Helvetica.plain");
    m_PSFontReplacement.put("Dialog.plain", "Helvetica.plain");
    m_PSFontReplacement.put("Microsoft Sans Serif", "Helvetica.plain");
    m_PSFontReplacement.put("MicrosoftSansSerif", "Helvetica.plain");
  }
  









  public PostscriptGraphics(int width, int height, OutputStream os)
  {
    m_extent = new Rectangle(0, 0, height, width);
    m_printstream = new PrintStream(os);
    m_localGraphicsState = new GraphicsState();
    m_psGraphicsState = new GraphicsState();
    
    Header();
  }
  





  PostscriptGraphics(PostscriptGraphics copy)
  {
    m_extent = new Rectangle(m_extent);
    m_printstream = m_printstream;
    m_localGraphicsState = new GraphicsState(m_localGraphicsState);
    m_psGraphicsState = m_psGraphicsState;
  }
  


  public void finished()
  {
    m_printstream.flush();
  }
  


  private void Header()
  {
    m_printstream.println("%!PS-Adobe-3.0 EPSF-3.0");
    m_printstream.println("%%BoundingBox: 0 0 " + xScale(m_extent.width) + " " + yScale(m_extent.height));
    m_printstream.println("%%CreationDate: " + Calendar.getInstance().getTime());
    
    m_printstream.println("/Oval { % x y w h filled");
    m_printstream.println("gsave");
    m_printstream.println("/filled exch def /h exch def /w exch def /y exch def /x exch def");
    m_printstream.println("x w 2 div add y h 2 div sub translate");
    m_printstream.println("1 h w div scale");
    m_printstream.println("filled {0 0 moveto} if");
    m_printstream.println("0 0 w 2 div 0 360 arc");
    m_printstream.println("filled {closepath fill} {stroke} ifelse grestore} bind def");
    
    m_printstream.println("/Rect { % x y w h filled");
    m_printstream.println("/filled exch def /h exch def /w exch def /y exch def /x exch def");
    m_printstream.println("newpath ");
    m_printstream.println("x y moveto");
    m_printstream.println("w 0 rlineto");
    m_printstream.println("0 h neg rlineto");
    m_printstream.println("w neg 0 rlineto");
    m_printstream.println("closepath");
    m_printstream.println("filled {fill} {stroke} ifelse} bind def");
    
    m_printstream.println("%%BeginProlog\n%%EndProlog");
    m_printstream.println("%%Page 1 1");
    setFont(null);
    setColor(null);
    setStroke(null);
  }
  






  public static void addPSFontReplacement(String replace, String with)
  {
    m_PSFontReplacement.put(replace, with);
  }
  





  private int yTransform(int y)
  {
    return m_extent.height - (m_localGraphicsState.getYOffset() + y);
  }
  




  private int xTransform(int x)
  {
    return m_localGraphicsState.getXOffset() + x;
  }
  


  private int doScale(int number, double factor)
  {
    return (int)StrictMath.round(number * factor);
  }
  


  private int xScale(int x)
  {
    return doScale(x, m_localGraphicsState.getXScale());
  }
  


  private int yScale(int y)
  {
    return doScale(y, m_localGraphicsState.getYScale());
  }
  

  private void setStateToLocal()
  {
    setColor(getColor());
    setFont(getFont());
    setStroke(getStroke());
  }
  





  private String toHex(int i)
  {
    String result = Integer.toHexString(i);
    if (result.length() < 2) {
      result = "0" + result;
    }
    return result;
  }
  









  public void clearRect(int x, int y, int width, int height)
  {
    setStateToLocal();
    Color saveColor = getColor();
    setColor(Color.white);
    m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " " + xScale(width) + " " + yScale(height) + " true Rect");
    setColor(saveColor);
  }
  














  public Graphics create()
  {
    PostscriptGraphics psg = new PostscriptGraphics(this);
    return psg;
  }
  














  public void draw3DRect(int x, int y, int width, int height, boolean raised)
  {
    drawRect(x, y, width, height);
  }
  









  public void drawBytes(byte[] data, int offset, int length, int x, int y)
  {
    drawString(new String(data, offset, length), x, y);
  }
  




  public void drawChars(char[] data, int offset, int length, int x, int y)
  {
    drawString(new String(data, offset, length), x, y);
  }
  




  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer)
  {
    return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer);
  }
  






  public boolean drawImage(Image img, int x, int y, ImageObserver observer)
  {
    return drawImage(img, x, y, Color.WHITE, observer);
  }
  



  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer)
  {
    try
    {
      int[] pixels = new int[width * height];
      PixelGrabber grabber = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
      grabber.grabPixels();
      ColorModel model = ColorModel.getRGBdefault();
      

      m_printstream.println("gsave");
      m_printstream.println(xTransform(xScale(x)) + " " + (yTransform(yScale(y)) - yScale(height)) + " translate");
      m_printstream.println(xScale(width) + " " + yScale(height) + " scale");
      m_printstream.println(width + " " + height + " " + "8" + " [" + width + " 0 0 " + -height + " 0 " + height + "]");
      m_printstream.println("{<");
      

      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
          int index = i * width + j;
          m_printstream.print(toHex(model.getRed(pixels[index])));
          m_printstream.print(toHex(model.getGreen(pixels[index])));
          m_printstream.print(toHex(model.getBlue(pixels[index])));
        }
        m_printstream.println();
      }
      
      m_printstream.println(">}");
      m_printstream.println("false 3 colorimage");
      m_printstream.println("grestore");
      return true;
    }
    catch (Exception e) {
      e.printStackTrace(); }
    return false;
  }
  







  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
  {
    return drawImage(img, x, y, width, height, Color.WHITE, observer);
  }
  


  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer)
  {
    return false;
  }
  





  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
  {
    return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.WHITE, observer);
  }
  








  public void drawLine(int x1, int y1, int x2, int y2)
  {
    setStateToLocal();
    m_printstream.println(xTransform(xScale(x1)) + " " + yTransform(yScale(y1)) + " moveto " + xTransform(xScale(x2)) + " " + yTransform(yScale(y2)) + " lineto stroke");
  }
  







  public void drawOval(int x, int y, int width, int height)
  {
    setStateToLocal();
    m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " " + xScale(width) + " " + yScale(height) + " false Oval");
  }
  

















  public void drawRect(int x, int y, int width, int height)
  {
    setStateToLocal();
    m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " " + xScale(width) + " " + yScale(height) + " false Rect");
  }
  


















  protected String escape(String s)
  {
    StringBuffer result = new StringBuffer();
    
    for (int i = 0; i < s.length(); i++) {
      if ((s.charAt(i) == '(') || (s.charAt(i) == ')'))
        result.append('\\');
      result.append(s.charAt(i));
    }
    
    return result.toString();
  }
  






  public void drawString(String str, int x, int y)
  {
    setStateToLocal();
    m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " moveto" + " (" + escape(str) + ") show stroke");
  }
  









  public void fill3DRect(int x, int y, int width, int height, boolean raised)
  {
    fillRect(x, y, width, height);
  }
  












  public void fillOval(int x, int y, int width, int height)
  {
    setStateToLocal();
    m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " " + xScale(width) + " " + yScale(height) + " true Oval");
  }
  


















  public void fillRect(int x, int y, int width, int height)
  {
    if ((width == m_extent.width) && (height == m_extent.height)) {
      clearRect(x, y, width, height);
    }
    else
    {
      setStateToLocal();
      m_printstream.println(xTransform(xScale(x)) + " " + yTransform(yScale(y)) + " " + xScale(width) + " " + yScale(height) + " true Rect");
    }
  }
  












  public Shape getClip()
  {
    return null;
  }
  



  public Rectangle getClipBounds()
  {
    return new Rectangle(0, 0, m_extent.width, m_extent.height);
  }
  



  public Rectangle getClipBounds(Rectangle r)
  {
    r.setBounds(0, 0, m_extent.width, m_extent.height);
    return r;
  }
  

  public Rectangle getClipRect()
  {
    return null;
  }
  



  public Color getColor()
  {
    return m_localGraphicsState.getColor();
  }
  




  public Font getFont()
  {
    return m_localGraphicsState.getFont();
  }
  





  public FontMetrics getFontMetrics(Font f)
  {
    return Toolkit.getDefaultToolkit().getFontMetrics(f);
  }
  















  public void setColor(Color c)
  {
    if (c != null) {
      m_localGraphicsState.setColor(c);
      if (m_psGraphicsState.getColor().equals(c)) {
        return;
      }
      m_psGraphicsState.setColor(c);
    } else {
      m_localGraphicsState.setColor(Color.black);
      m_psGraphicsState.setColor(getColor());
    }
    m_printstream.print(getColor().getRed() / 255.0D);
    m_printstream.print(" ");
    m_printstream.print(getColor().getGreen() / 255.0D);
    m_printstream.print(" ");
    m_printstream.print(getColor().getBlue() / 255.0D);
    m_printstream.println(" setrgbcolor");
  }
  




  private static String replacePSFont(String font)
  {
    String result = font;
    

    if (m_PSFontReplacement.containsKey(font)) {
      result = m_PSFontReplacement.get(font).toString();
    }
    


    return result;
  }
  





  public void setFont(Font font)
  {
    if (font != null) {
      m_localGraphicsState.setFont(font);
      if ((font.getName().equals(m_psGraphicsState.getFont().getName())) && (m_psGraphicsState.getFont().getStyle() == font.getStyle()) && (m_psGraphicsState.getFont().getSize() == yScale(font.getSize())))
      {

        return; }
      m_psGraphicsState.setFont(new Font(font.getName(), font.getStyle(), yScale(getFont().getSize())));
    }
    else {
      m_localGraphicsState.setFont(new Font("Courier", 0, 11));
      m_psGraphicsState.setFont(getFont());
    }
    
    m_printstream.println("/(" + replacePSFont(getFont().getPSName()) + ")" + " findfont");
    m_printstream.println(yScale(getFont().getSize()) + " scalefont setfont");
  }
  





















  public void translate(int x, int y)
  {
    m_localGraphicsState.setXOffset(m_localGraphicsState.getXOffset() + xScale(x));
    m_localGraphicsState.setYOffset(m_localGraphicsState.getYOffset() + yScale(y));
    m_psGraphicsState.setXOffset(m_psGraphicsState.getXOffset() + xScale(x));
    m_psGraphicsState.setYOffset(m_psGraphicsState.getYOffset() + yScale(y));
  }
  


  public FontRenderContext getFontRenderContext()
  {
    return new FontRenderContext(null, true, true);
  }
  
  public Stroke getStroke() {
    return m_localGraphicsState.getStroke();
  }
  
  public Color getBackground() {
    return Color.white;
  }
  
  public Composite getComposite() {
    return AlphaComposite.getInstance(2);
  }
  
  public Paint getPaint() { return new Color(getColor().getRed(), getColor().getGreen(), getColor().getBlue()); }
  
  public AffineTransform getTransform() {
    return new AffineTransform();
  }
  

  public void scale(double d1, double d2)
  {
    m_localGraphicsState.setXScale(d1);
    m_localGraphicsState.setYScale(d2);
  }
  



  public RenderingHints getRenderingHints()
  {
    return new RenderingHints(null);
  }
  
  public Object getRenderingHint(RenderingHints.Key key)
  {
    return null;
  }
  
  public void setStroke(Stroke s) {
    if (s != null) {
      m_localGraphicsState.setStroke(s);
      if (s.equals(m_psGraphicsState.getStroke())) {
        return;
      }
      m_psGraphicsState.setStroke(s);
    } else {
      m_localGraphicsState.setStroke(new BasicStroke());
      m_psGraphicsState.setStroke(getStroke());
    }
  }
  


  public GraphicsConfiguration getDeviceConfiguration()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    return gd.getDefaultConfiguration();
  }
  
  public boolean hit(Rectangle r, Shape s, boolean onstroke) { return false; }
  


  public void drawString(String str, float x, float y)
  {
    drawString(str, (int)x, (int)y);
  }
  

  public boolean drawImage(Image im, AffineTransform at, ImageObserver io)
  {
    return false;
  }
  
  public void clipRect(int x, int y, int width, int height) {}
  
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {}
  
  public void dispose() {}
  
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
  
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
  
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {}
  
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
  
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {}
  
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
  
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
  
  public void fillPolygon(Polygon p) {}
  
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
  
  public void finalize() {}
  
  public void setClip(int x, int y, int width, int height) {}
  
  public void setClip(Shape clip) {}
  
  public void setPaintMode() {}
  
  public void setXORMode(Color c1) {}
  
  public void clip(Shape s) {}
  
  public void setBackground(Color c) {}
  
  public void setTransform(AffineTransform at) {}
  
  public void transform(AffineTransform at) {}
  
  public void shear(double d1, double d2) {}
  
  public void rotate(double d1, double d2, double d3) {}
  
  public void rotate(double d1) {}
  
  public void translate(double d1, double d2) {}
  
  public void addRenderingHints(Map m) {}
  
  public void setRenderingHints(Map m) {}
  
  public void setRenderingHint(RenderingHints.Key key, Object o) {}
  
  public void setPaint(Paint p) {}
  
  public void setComposite(Composite c) {}
  
  public void fill(Shape s) {}
  
  public void drawGlyphVector(GlyphVector gv, float f1, float f2) {}
  
  public void drawString(AttributedCharacterIterator aci, float f1, float f2) {}
  
  public void drawRenderableImage(RenderableImage ri, AffineTransform at) {}
  
  public void drawRenderedImage(RenderedImage ri, AffineTransform af) {}
  
  public void drawImage(BufferedImage bi, BufferedImageOp bio, int i1, int i2) {}
  
  public void draw(Shape s) {}
}
