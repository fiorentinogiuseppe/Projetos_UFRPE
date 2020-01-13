package weka.gui.visualize;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.NodePlace;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;

































public class BMPWriter
  extends JComponentWriter
{
  protected Color m_Background;
  
  public BMPWriter() {}
  
  public BMPWriter(JComponent c)
  {
    super(c);
  }
  





  public BMPWriter(JComponent c, File f)
  {
    super(c, f);
  }
  


  public void initialize()
  {
    super.initialize();
    
    setScalingEnabled(false);
  }
  





  public String getDescription()
  {
    Messages.getInstance();return Messages.getString("BMPWriter_GetDescription_Text");
  }
  





  public String getExtension()
  {
    return ".bmp";
  }
  




  public Color getBackground()
  {
    return m_Background;
  }
  




  public void setBackground(Color c)
  {
    m_Background = c;
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
    ImageIO.write(bi, "bmp", getFile());
  }
  




  public static void main(String[] args)
    throws Exception
  {
    Messages.getInstance();System.out.println(Messages.getString("BMPWriter_Main_Text_First"));
    TreeBuild builder = new TreeBuild();
    NodePlace arrange = new PlaceNode2();
    Messages.getInstance();Node top = builder.create(new StringReader(Messages.getString("BMPWriter_Main_Text_Second")));
    TreeVisualizer tv = new TreeVisualizer(null, top, arrange);
    tv.setSize(800, 600);
    
    String filename = System.getProperty("java.io.tmpdir") + File.separator + "test.bmp";
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("BMPWriter_Main_Text_Third") + filename + Messages.getString("BMPWriter_Main_Text_Fourth"));
    toOutput(new BMPWriter(), tv, new File(filename));
    
    Messages.getInstance();System.out.println(Messages.getString("BMPWriter_Main_Text_Fifth"));
  }
}
