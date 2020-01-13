package weka.gui.visualize;

import java.io.File;
import javax.swing.JComponent;






























































public abstract class JComponentWriter
{
  protected static final boolean DEBUG = false;
  private JComponent component;
  private File outputFile;
  protected double m_xScale;
  protected double m_yScale;
  protected boolean m_ScalingEnabled;
  protected boolean m_UseCustomDimensions;
  protected int m_CustomWidth;
  protected int m_CustomHeight;
  
  public JComponentWriter()
  {
    this(null);
  }
  




  public JComponentWriter(JComponent c)
  {
    this(c, null);
  }
  





  public JComponentWriter(JComponent c, File f)
  {
    component = c;
    outputFile = f;
    
    initialize();
  }
  


  protected void initialize()
  {
    m_xScale = 1.0D;
    m_yScale = 1.0D;
    m_ScalingEnabled = true;
    m_UseCustomDimensions = false;
    m_CustomWidth = -1;
    m_CustomHeight = -1;
  }
  




  public void setComponent(JComponent c)
  {
    component = c;
  }
  




  public JComponent getComponent()
  {
    return component;
  }
  




  public void setFile(File f)
  {
    outputFile = f;
  }
  




  public File getFile()
  {
    return outputFile;
  }
  






  public abstract String getDescription();
  






  public abstract String getExtension();
  





  public boolean getScalingEnabled()
  {
    return m_ScalingEnabled;
  }
  




  public void setScalingEnabled(boolean enabled)
  {
    m_ScalingEnabled = enabled;
  }
  




  public void setScale(double x, double y)
  {
    if (getScalingEnabled()) {
      m_xScale = x;
      m_yScale = y;
    }
    else {
      m_xScale = 1.0D;
      m_yScale = 1.0D;
    }
  }
  







  public double getXScale()
  {
    return m_xScale;
  }
  




  public double getYScale()
  {
    return m_xScale;
  }
  




  public boolean getUseCustomDimensions()
  {
    return m_UseCustomDimensions;
  }
  




  public void setUseCustomDimensions(boolean value)
  {
    m_UseCustomDimensions = value;
  }
  





  public void setCustomWidth(int value)
  {
    m_CustomWidth = value;
  }
  





  public int getCustomWidth()
  {
    return m_CustomWidth;
  }
  





  public void setCustomHeight(int value)
  {
    m_CustomHeight = value;
  }
  





  public int getCustomHeight()
  {
    return m_CustomHeight;
  }
  






  protected abstract void generateOutput()
    throws Exception;
  





  public void toOutput()
    throws Exception
  {
    if (getFile() == null) {
      Messages.getInstance();throw new Exception(Messages.getString("JComponentWriter_ToOutput_Exception_Text_First")); }
    if (getComponent() == null) {
      Messages.getInstance();throw new Exception(Messages.getString("JComponentWriter_ToOutput_Exception_Text_Second"));
    }
    
    int oldWidth = getComponent().getWidth();
    int oldHeight = getComponent().getHeight();
    if (getUseCustomDimensions()) {
      getComponent().setSize(getCustomWidth(), getCustomHeight());
    }
    generateOutput();
    

    if (getUseCustomDimensions()) {
      getComponent().setSize(oldWidth, oldHeight);
    }
  }
  





  public static void toOutput(JComponentWriter writer, JComponent comp, File file)
    throws Exception
  {
    toOutput(writer, comp, file, -1, -1);
  }
  










  public static void toOutput(JComponentWriter writer, JComponent comp, File file, int width, int height)
    throws Exception
  {
    writer.setComponent(comp);
    writer.setFile(file);
    

    if ((width != -1) && (height != -1)) {
      writer.setUseCustomDimensions(true);
      writer.setCustomWidth(width);
      writer.setCustomHeight(height);
    }
    
    writer.toOutput();
  }
}
