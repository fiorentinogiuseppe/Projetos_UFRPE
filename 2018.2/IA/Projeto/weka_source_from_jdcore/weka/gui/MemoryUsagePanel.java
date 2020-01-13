package weka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import weka.core.Memory;
import weka.core.Utils;
import weka.gui.visualize.VisualizeUtils;











































public class MemoryUsagePanel
  extends JPanel
{
  private static final long serialVersionUID = -4812319791687471721L;
  
  protected class MemoryMonitor
    extends Thread
  {
    protected int m_Interval;
    protected boolean m_Monitoring;
    
    public MemoryMonitor()
    {
      setInterval(1000);
    }
    




    public int getInterval()
    {
      return m_Interval;
    }
    




    public void setInterval(int value)
    {
      m_Interval = value;
    }
    




    public boolean isMonitoring()
    {
      return m_Monitoring;
    }
    


    public void stopMonitoring()
    {
      m_Monitoring = false;
    }
    


    public void run()
    {
      m_Monitoring = true;
      
      while (m_Monitoring) {
        try {
          Thread.sleep(m_Interval);
          

          if (m_Monitoring) {
            Runnable doUpdate = new Runnable() {
              public void run() {
                update();
              }
            };
            SwingUtilities.invokeLater(doUpdate);
          }
        }
        catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
    






    protected void update()
    {
      double perc = m_Memory.getCurrent() / m_Memory.getMax();
      perc = Math.round(perc * 1000.0D) / 10L;
      

      Messages.getInstance();setToolTipText("" + perc + Messages.getString("MemoryUsagePanel_MemoryMonitor_Update_SetToolTipText_Text"));
      

      m_History.insertElementAt(Double.valueOf(perc), 0);
      Dimension size = getSize();
      while (m_History.size() > size.getWidth()) {
        m_History.remove(m_History.size() - 1);
      }
      
      repaint();
    }
  }
  

  protected static String PROPERTY_FILE = "weka/gui/MemoryUsage.props";
  


  protected static Properties PROPERTIES;
  


  protected Vector<Double> m_History;
  


  protected Memory m_Memory;
  

  protected MemoryMonitor m_Monitor;
  

  protected JButton m_ButtonGC;
  

  protected Vector<Double> m_Percentages;
  

  protected Hashtable<Double, Color> m_Colors;
  

  protected Color m_DefaultColor;
  

  protected Color m_BackgroundColor;
  

  protected Point m_FrameLocation;
  


  static
  {
    try
    {
      PROPERTIES = Utils.readProperties(PROPERTY_FILE);
      Enumeration keys = PROPERTIES.propertyNames();
      if (!keys.hasMoreElements()) {
        Messages.getInstance();throw new Exception(Messages.getString("MemoryUsagePanel_Exception_Text"));
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("MemoryUsagePanel_Exception_JOptionPaneShowMessageDialog_Text_First") + System.getProperties().getProperty("user.home") + Messages.getString("MemoryUsagePanel_Exception_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("MemoryUsagePanel_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
    }
  }
  











  public MemoryUsagePanel()
  {
    m_Memory = new Memory();
    m_History = new Vector();
    m_Percentages = new Vector();
    m_Colors = new Hashtable();
    

    m_BackgroundColor = parseColor("BackgroundColor", Color.WHITE);
    m_DefaultColor = parseColor("DefaultColor", Color.GREEN);
    String[] percs = PROPERTIES.getProperty("Percentages", "70,80,90").split(",");
    for (int i = 0; i < percs.length; i++)
    {
      if (PROPERTIES.getProperty(percs[i]) != null)
      {
        double perc;
        
        try
        {
          perc = Double.parseDouble(percs[i]);
        }
        catch (Exception e) {
          Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_First") + percs[i] + Messages.getString("MemoryUsagePanel_Error_Second"));
          

          continue;
        }
        

        Color color = parseColor(percs[i], null);
        if (color != null)
        {


          m_Percentages.add(Double.valueOf(perc));
          m_Colors.put(Double.valueOf(perc), color);
        }
      } else {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_Third") + percs[i] + Messages.getString("MemoryUsagePanel_Error_Fourth"));
      }
    }
    

    Collections.sort(m_Percentages);
    

    setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new BorderLayout());
    add(panel, "East");
    
    Messages.getInstance();m_ButtonGC = new JButton(Messages.getString("MemoryUsagePanel_ButtonGC_JButton_Text"));
    Messages.getInstance();m_ButtonGC.setToolTipText(Messages.getString("MemoryUsagePanel_ButtonGC_SetToolTipText_Text"));
    m_ButtonGC.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt) {}

    });
    panel.add(m_ButtonGC, "North");
    
    int height;
    int width;
    try
    {
      height = Integer.parseInt(PROPERTIES.getProperty("Height", "" + (int)m_ButtonGC.getPreferredSize().getHeight()));
      width = Integer.parseInt(PROPERTIES.getProperty("Width", "400"));
    }
    catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_Fifth") + e);
      height = (int)m_ButtonGC.getPreferredSize().getHeight();
      width = 400;
    }
    setPreferredSize(new Dimension(width, height));
    
    int top;
    int left;
    try
    {
      top = Integer.parseInt(PROPERTIES.getProperty("Top", "0"));
      left = Integer.parseInt(PROPERTIES.getProperty("Left", "0"));
    }
    catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_Sixth") + e);
      top = 0;
      left = 0;
    }
    m_FrameLocation = new Point(left, top);
    
    int interval;
    try
    {
      interval = Integer.parseInt(PROPERTIES.getProperty("Interval", "1000"));
    }
    catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_Seventh") + e);
      interval = 1000;
    }
    m_Monitor = new MemoryMonitor();
    m_Monitor.setInterval(interval);
    m_Monitor.setPriority(10);
    m_Monitor.start();
  }
  










  protected Color parseColor(String prop, Color defValue)
  {
    Color result = defValue;
    try
    {
      String colorStr = PROPERTIES.getProperty(prop);
      Color color = VisualizeUtils.processColour(colorStr, result);
      if (color == null)
        throw new Exception(colorStr);
      result = color;
    }
    catch (Exception e) {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("MemoryUsagePanel_Error_Eighth") + e.getMessage() + Messages.getString("MemoryUsagePanel_Error_Nineth"));
    }
    


    return result;
  }
  




  public boolean isMonitoring()
  {
    return m_Monitor.isMonitoring();
  }
  


  public void stopMonitoring()
  {
    m_Monitor.stopMonitoring();
  }
  




  public Point getFrameLocation()
  {
    return m_FrameLocation;
  }
  











  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    
    g.setColor(m_BackgroundColor);
    g.fillRect(0, 0, getWidth(), getHeight());
    double scale = getHeight() / 100.0D;
    for (int i = 0; i < m_History.size(); i++) {
      double perc = ((Double)m_History.get(i)).doubleValue();
      

      Color color = m_DefaultColor;
      for (int n = m_Percentages.size() - 1; n >= 0; n--) {
        if (perc >= ((Double)m_Percentages.get(n)).doubleValue()) {
          color = (Color)m_Colors.get(m_Percentages.get(n));
          break;
        }
      }
      

      g.setColor(color);
      int len = (int)Math.round(perc * scale);
      g.drawLine(i, getHeight() - 1, i, getHeight() - len);
    }
  }
}
