package weka.gui.visualize;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JComponent;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.NodePlace;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;




































public class JPEGWriter
  extends JComponentWriter
{
  protected float m_Quality;
  protected Color m_Background;
  
  public JPEGWriter() {}
  
  public JPEGWriter(JComponent c)
  {
    super(c);
  }
  





  public JPEGWriter(JComponent c, File f)
  {
    super(c, f);
    
    m_Quality = 1.0F;
    m_Background = Color.WHITE;
  }
  


  public void initialize()
  {
    super.initialize();
    
    m_Quality = 1.0F;
    m_Background = Color.WHITE;
    setScalingEnabled(false);
  }
  





  public String getDescription()
  {
    Messages.getInstance();return Messages.getString("JPEGWriter_GetDescription_Text");
  }
  






  public String getExtension()
  {
    return ".jpg";
  }
  




  public Color getBackground()
  {
    return m_Background;
  }
  




  public void setBackground(Color c)
  {
    m_Background = c;
  }
  




  public float getQuality()
  {
    return m_Quality;
  }
  




  public void setQuality(float q)
  {
    m_Quality = q;
  }
  











  public void generateOutput()
    throws Exception
  {
    BufferedImage bi = new BufferedImage(getComponent().getWidth(), getComponent().getHeight(), 1);
    Graphics g = bi.getGraphics();
    g.setPaintMode();
    g.setColor(getBackground());
    if ((g instanceof Graphics2D))
      ((Graphics2D)g).scale(getXScale(), getYScale());
    g.fillRect(0, 0, getComponent().getWidth(), getComponent().getHeight());
    getComponent().printAll(g);
    

    ImageWriter writer = null;
    Iterator iter = ImageIO.getImageWritersByFormatName(getExtension().replace(".", ""));
    if (iter.hasNext()) {
      writer = (ImageWriter)iter.next();
    } else {
      Messages.getInstance();Messages.getInstance();throw new Exception(Messages.getString("JPEGWriter_GenerateOutput_Exception_Text_First") + getDescription() + Messages.getString("JPEGWriter_GenerateOutput_Exception_Text_Second"));
    }
    
    ImageOutputStream ios = ImageIO.createImageOutputStream(getFile());
    writer.setOutput(ios);
    

    ImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
    param.setCompressionMode(2);
    param.setCompressionQuality(getQuality());
    

    writer.write(null, new IIOImage(bi, null, null), param);
    

    ios.flush();
    writer.dispose();
    ios.close();
  }
  




  public static void main(String[] args)
    throws Exception
  {
    Messages.getInstance();System.out.println(Messages.getString("JPEGWriter_Main_Text_First"));
    TreeBuild builder = new TreeBuild();
    NodePlace arrange = new PlaceNode2();
    Messages.getInstance();Node top = builder.create(new StringReader(Messages.getString("JPEGWriter_Main_Text_Second")));
    TreeVisualizer tv = new TreeVisualizer(null, top, arrange);
    tv.setSize(800, 600);
    
    String filename = System.getProperty("java.io.tmpdir") + File.separator + "test.jpg";
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("JPEGWriter_Main_Text_Third") + filename + Messages.getString("JPEGWriter_Main_Text_Fourth"));
    toOutput(new JPEGWriter(), tv, new File(filename));
    
    Messages.getInstance();System.out.println(Messages.getString("JPEGWriter_Main_Text_Fifth"));
  }
}
