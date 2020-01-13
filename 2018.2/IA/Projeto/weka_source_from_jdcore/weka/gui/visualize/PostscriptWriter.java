package weka.gui.visualize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import javax.swing.JComponent;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.NodePlace;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;
































public class PostscriptWriter
  extends JComponentWriter
{
  public PostscriptWriter()
  {
    super(null);
  }
  




  public PostscriptWriter(JComponent c)
  {
    super(c);
  }
  





  public PostscriptWriter(JComponent c, File f)
  {
    super(c, f);
  }
  



  public String getDescription()
  {
    Messages.getInstance();return Messages.getString("PostscriptWriter_GetDescription_Text");
  }
  




  public String getExtension()
  {
    return ".eps";
  }
  






  public void generateOutput()
    throws Exception
  {
    BufferedOutputStream ostrm = null;
    try
    {
      ostrm = new BufferedOutputStream(new FileOutputStream(getFile()));
      PostscriptGraphics psg = new PostscriptGraphics(getComponent().getHeight(), getComponent().getWidth(), ostrm);
      psg.setFont(getComponent().getFont());
      psg.scale(getXScale(), getYScale());
      getComponent().printAll(psg);
      psg.finished();
    }
    catch (Exception e) {
      System.err.println(e);
    }
    finally {
      if (ostrm != null) {
        try {
          ostrm.close();
        }
        catch (Exception e) {}
      }
    }
  }
  


  public static void main(String[] args)
    throws Exception
  {
    Messages.getInstance();System.out.println(Messages.getString("PostscriptWriter_Main_Text_First"));
    TreeBuild builder = new TreeBuild();
    NodePlace arrange = new PlaceNode2();
    Messages.getInstance();Node top = builder.create(new StringReader(Messages.getString("PostscriptWriter_Main_Text_Second")));
    TreeVisualizer tv = new TreeVisualizer(null, top, arrange);
    tv.setSize(800, 600);
    
    String filename = System.getProperty("java.io.tmpdir") + "test.eps";
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("PostscriptWriter_Main_Text_Third") + filename + Messages.getString("PostscriptWriter_Main_Text_Fourth"));
    toOutput(new PostscriptWriter(), tv, new File(filename));
    
    Messages.getInstance();System.out.println(Messages.getString("PostscriptWriter_Main_Text_Fifth"));
  }
}
