package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;






























































public class BeanVisual
  extends JPanel
{
  private static final long serialVersionUID = -6677473561687129614L;
  public static final String ICON_PATH = "weka/gui/beans/icons/";
  public static final int NORTH_CONNECTOR = 0;
  public static final int SOUTH_CONNECTOR = 1;
  public static final int EAST_CONNECTOR = 2;
  public static final int WEST_CONNECTOR = 3;
  protected String m_iconPath;
  protected String m_animatedIconPath;
  protected transient ImageIcon m_icon;
  protected transient ImageIcon m_animatedIcon;
  protected String m_visualName;
  protected JLabel m_visualLabel;
  private boolean m_stationary = true;
  
  private PropertyChangeSupport m_pcs = new PropertyChangeSupport(this);
  
  private boolean m_displayConnectors = false;
  private Color m_connectorColor = Color.blue;
  








  public BeanVisual(String visualName, String iconPath, String animatedIconPath)
  {
    loadIcons(iconPath, animatedIconPath);
    m_visualName = visualName;
    
    m_visualLabel = new JLabel(m_icon);
    
    setLayout(new BorderLayout());
    


    add(m_visualLabel, "Center");
    Dimension d = m_visualLabel.getPreferredSize();
    
    Dimension d2 = new Dimension((int)d.getWidth() + 10, (int)d.getHeight() + 10);
    
    setMinimumSize(d2);
    setPreferredSize(d2);
    setMaximumSize(d2);
  }
  




  public void scale(int factor)
  {
    if (m_icon != null) {
      removeAll();
      Image pic = m_icon.getImage();
      int width = m_icon.getIconWidth();
      int height = m_icon.getIconHeight();
      int reduction = width / factor;
      width -= reduction;
      height -= reduction;
      pic = pic.getScaledInstance(width, height, 4);
      m_icon = new ImageIcon(pic);
      m_visualLabel = new JLabel(m_icon);
      add(m_visualLabel, "Center");
      Dimension d = m_visualLabel.getPreferredSize();
      
      Dimension d2 = new Dimension((int)d.getWidth() + 10, (int)d.getHeight() + 10);
      
      setMinimumSize(d2);
      setPreferredSize(d2);
      setMaximumSize(d2);
    }
  }
  












  public boolean loadIcons(String iconPath, String animatedIconPath)
  {
    boolean success = true;
    
    URL imageURL = getClass().getClassLoader().getResource(iconPath);
    if (imageURL != null)
    {

      Image pic = Toolkit.getDefaultToolkit().getImage(imageURL);
      

      m_icon = new ImageIcon(pic);
      if (m_visualLabel != null) {
        m_visualLabel.setIcon(m_icon);
      }
    }
    

    imageURL = getClass().getClassLoader().getResource(animatedIconPath);
    if (imageURL == null)
    {
      success = false;
    } else {
      Image pic2 = Toolkit.getDefaultToolkit().getImage(imageURL);
      
      m_animatedIcon = new ImageIcon(pic2);
    }
    m_iconPath = iconPath;
    m_animatedIconPath = animatedIconPath;
    return success;
  }
  




  public void setText(String text)
  {
    m_visualName = text;
    
    m_pcs.firePropertyChange("label", null, null);
  }
  




  public String getText()
  {
    return m_visualName;
  }
  



  public void setStatic()
  {
    m_visualLabel.setIcon(m_icon);
  }
  



  public void setAnimated()
  {
    m_visualLabel.setIcon(m_animatedIcon);
  }
  







  public Point getClosestConnectorPoint(Point pt)
  {
    int sourceX = getParent().getX();
    int sourceY = getParent().getY();
    int sourceWidth = getWidth();
    int sourceHeight = getHeight();
    int sourceMidX = sourceX + sourceWidth / 2;
    int sourceMidY = sourceY + sourceHeight / 2;
    int x = (int)pt.getX();
    int y = (int)pt.getY();
    
    Point closest = new Point();
    int cx = x < sourceMidX ? sourceX : Math.abs(x - sourceMidX) < Math.abs(y - sourceMidY) ? sourceMidX : sourceX + sourceWidth;
    

    int cy = y < sourceMidY ? sourceY : Math.abs(y - sourceMidY) < Math.abs(x - sourceMidX) ? sourceMidY : sourceY + sourceHeight;
    

    closest.setLocation(cx, cy);
    return closest;
  }
  





  public Point getConnectorPoint(int compassPoint)
  {
    int sourceX = getParent().getX();
    int sourceY = getParent().getY();
    int sourceWidth = getWidth();
    int sourceHeight = getHeight();
    int sourceMidX = sourceX + sourceWidth / 2;
    int sourceMidY = sourceY + sourceHeight / 2;
    
    switch (compassPoint) {
    case 0:  return new Point(sourceMidX, sourceY);
    case 1:  return new Point(sourceMidX, sourceY + sourceHeight);
    case 3:  return new Point(sourceX, sourceMidY);
    case 2:  return new Point(sourceX + sourceWidth, sourceMidY); }
    System.err.println("Unrecognised connectorPoint (BeanVisual)");
    
    return new Point(sourceX, sourceY);
  }
  




  public ImageIcon getStaticIcon()
  {
    return m_icon;
  }
  




  public ImageIcon getAnimatedIcon()
  {
    return m_animatedIcon;
  }
  




  public String getIconPath()
  {
    return m_iconPath;
  }
  




  public String getAnimatedIconPath()
  {
    return m_animatedIconPath;
  }
  





  public void setDisplayConnectors(boolean dc)
  {
    m_displayConnectors = dc;
    m_connectorColor = Color.blue;
    repaint();
  }
  






  public void setDisplayConnectors(boolean dc, Color c)
  {
    setDisplayConnectors(dc);
    m_connectorColor = c;
  }
  




  public void addPropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcs.addPropertyChangeListener(pcl);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener pcl)
  {
    m_pcs.removePropertyChangeListener(pcl);
  }
  
  public void paintComponent(Graphics gx) {
    super.paintComponent(gx);
    if (m_displayConnectors) {
      gx.setColor(m_connectorColor);
      
      int midx = (int)(getWidth() / 2.0D);
      int midy = (int)(getHeight() / 2.0D);
      gx.fillOval(midx - 2, 0, 5, 5);
      gx.fillOval(midx - 2, getHeight() - 5, 5, 5);
      gx.fillOval(0, midy - 2, 5, 5);
      gx.fillOval(getWidth() - 5, midy - 2, 5, 5);
    }
  }
  







  private void readObject(ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    try
    {
      ois.defaultReadObject();
      remove(m_visualLabel);
      m_visualLabel = new JLabel(m_icon);
      loadIcons(m_iconPath, m_animatedIconPath);
      add(m_visualLabel, "Center");
      Dimension d = m_visualLabel.getPreferredSize();
      Dimension d2 = new Dimension((int)d.getWidth() + 10, (int)d.getHeight() + 10);
      
      setMinimumSize(d2);
      setPreferredSize(d2);
      setMaximumSize(d2);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
