package weka.gui.beans;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.Beans;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JComponent;



































public class BeanInstance
  implements Serializable
{
  private static final long serialVersionUID = -7575653109025406342L;
  private static Vector COMPONENTS = new Vector();
  

  public static final int IDLE = 0;
  

  public static final int BEAN_EXECUTING = 1;
  

  private Object m_bean;
  
  private int m_x;
  
  private int m_y;
  

  public static void reset(JComponent container)
  {
    removeAllBeansFromContainer(container);
    COMPONENTS = new Vector();
  }
  




  public static void removeAllBeansFromContainer(JComponent container)
  {
    if (container != null) {
      if (COMPONENTS != null) {
        for (int i = 0; i < COMPONENTS.size(); i++) {
          BeanInstance tempInstance = (BeanInstance)COMPONENTS.elementAt(i);
          Object tempBean = tempInstance.getBean();
          if (Beans.isInstanceOf(tempBean, JComponent.class)) {
            container.remove((JComponent)tempBean);
          }
        }
      }
      container.revalidate();
    }
  }
  




  public static void addAllBeansToContainer(JComponent container)
  {
    if (container != null) {
      if (COMPONENTS != null) {
        for (int i = 0; i < COMPONENTS.size(); i++) {
          BeanInstance tempInstance = (BeanInstance)COMPONENTS.elementAt(i);
          Object tempBean = tempInstance.getBean();
          if (Beans.isInstanceOf(tempBean, JComponent.class)) {
            container.add((JComponent)tempBean);
          }
        }
      }
      container.revalidate();
    }
  }
  




  public static Vector getBeanInstances()
  {
    return COMPONENTS;
  }
  






  public static void setBeanInstances(Vector beanInstances, JComponent container)
  {
    reset(container);
    
    if (container != null) {
      for (int i = 0; i < beanInstances.size(); i++) {
        Object bean = ((BeanInstance)beanInstances.elementAt(i)).getBean();
        if (Beans.isInstanceOf(bean, JComponent.class)) {
          container.add((JComponent)bean);
        }
      }
      container.revalidate();
      container.repaint();
    }
    COMPONENTS = beanInstances;
  }
  





  public static void paintLabels(Graphics gx)
  {
    gx.setFont(new Font(null, 0, 9));
    FontMetrics fm = gx.getFontMetrics();
    int hf = fm.getAscent();
    for (int i = 0; i < COMPONENTS.size(); i++) {
      BeanInstance bi = (BeanInstance)COMPONENTS.elementAt(i);
      if ((bi.getBean() instanceof Visible))
      {

        int cx = bi.getX();int cy = bi.getY();
        int width = ((JComponent)bi.getBean()).getWidth();
        int height = ((JComponent)bi.getBean()).getHeight();
        String label = ((Visible)bi.getBean()).getVisual().getText();
        int labelwidth = fm.stringWidth(label);
        if (labelwidth < width) {
          gx.drawString(label, cx + width / 2 - labelwidth / 2, cy + height + hf + 2);

        }
        else
        {
          int mid = label.length() / 2;
          
          int closest = label.length();
          int closestI = -1;
          for (int z = 0; z < label.length(); z++) {
            if ((label.charAt(z) < 'a') && 
              (Math.abs(mid - z) < closest)) {
              closest = Math.abs(mid - z);
              closestI = z;
            }
          }
          
          if (closestI != -1) {
            String left = label.substring(0, closestI);
            String right = label.substring(closestI, label.length());
            if ((left.length() > 1) && (right.length() > 1)) {
              gx.drawString(left, cx + width / 2 - fm.stringWidth(left) / 2, cy + height + hf * 1 + 2);
              
              gx.drawString(right, cx + width / 2 - fm.stringWidth(right) / 2, cy + height + hf * 2 + 2);
            }
            else {
              gx.drawString(label, cx + width / 2 - fm.stringWidth(label) / 2, cy + height + hf * 1 + 2);
            }
          }
          else {
            gx.drawString(label, cx + width / 2 - fm.stringWidth(label) / 2, cy + height + hf * 1 + 2);
          }
        }
      }
    }
  }
  






  public static BeanInstance findInstance(Point p)
  {
    Rectangle tempBounds = new Rectangle();
    for (int i = 0; i < COMPONENTS.size(); i++)
    {
      BeanInstance t = (BeanInstance)COMPONENTS.elementAt(i);
      JComponent temp = (JComponent)t.getBean();
      
      tempBounds = temp.getBounds(tempBounds);
      if (tempBounds.contains(p)) {
        return t;
      }
    }
    return null;
  }
  








  public static Vector findInstances(Rectangle boundingBox)
  {
    Graphics gx = null;
    FontMetrics fm = null;
    


    int startX = (int)boundingBox.getX();
    int startY = (int)boundingBox.getY();
    int endX = (int)boundingBox.getMaxX();
    int endY = (int)boundingBox.getMaxY();
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    Vector result = new Vector();
    for (int i = 0; i < COMPONENTS.size(); i++) {
      BeanInstance t = (BeanInstance)COMPONENTS.elementAt(i);
      int centerX = t.getX() + t.getWidth() / 2;
      int centerY = t.getY() + t.getHeight() / 2;
      if (boundingBox.contains(centerX, centerY)) {
        result.addElement(t);
        



        if (gx == null) {
          gx = ((JComponent)t.getBean()).getGraphics();
          gx.setFont(new Font(null, 0, 9));
          fm = gx.getFontMetrics();
        }
        
        String label = "";
        if ((t.getBean() instanceof Visible)) {
          label = ((Visible)t.getBean()).getVisual().getText();
        }
        int labelwidth = fm.stringWidth(label);
        int heightMultiplier = labelwidth > t.getWidth() ? 2 : 1;
        




        int brx = 0;
        int blx = 0;
        if (centerX - labelwidth / 2 - 2 < t.getX()) {
          blx = centerX - labelwidth / 2 - 2;
          brx = centerX + labelwidth / 2 + 2;
        } else {
          blx = t.getX() - 2;
          brx = t.getX() + t.getWidth() + 2;
        }
        
        if (blx < minX) {
          minX = blx;
        }
        if (brx > maxX) {
          maxX = brx;
        }
        if (t.getY() - 2 < minY) {
          minY = t.getY() - 2;
        }
        if (t.getY() + t.getHeight() + 2 > maxY) {
          maxY = t.getY() + t.getHeight() + 2;
        }
      }
    }
    boundingBox.setBounds(minX, minY, maxX - minX, maxY - minY);
    
    return result;
  }
  







  public BeanInstance(JComponent container, Object bean, int x, int y)
  {
    m_bean = bean;
    m_x = x;
    m_y = y;
    addBean(container);
  }
  








  public BeanInstance(JComponent container, String beanName, int x, int y)
  {
    m_x = x;
    m_y = y;
    
    try
    {
      m_bean = Beans.instantiate(null, beanName);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    
    addBean(container);
  }
  




  public void removeBean(JComponent container)
  {
    for (int i = 0; i < COMPONENTS.size(); i++) {
      if ((BeanInstance)COMPONENTS.elementAt(i) == this) {
        Messages.getInstance();System.err.println(Messages.getString("BeanInstance_RemoveBean_Error_Text"));
        COMPONENTS.removeElementAt(i);
      }
    }
    if (container != null) {
      container.remove((JComponent)m_bean);
      container.revalidate();
      container.repaint();
    }
  }
  











  public void addBean(JComponent container)
  {
    if (COMPONENTS.contains(this)) {
      return;
    }
    

    if (!Beans.isInstanceOf(m_bean, JComponent.class)) {
      Messages.getInstance();System.err.println(Messages.getString("BeanInstance_AddBean_Error_Text"));
      return;
    }
    
    COMPONENTS.addElement(this);
    

    JComponent c = (JComponent)m_bean;
    Dimension d = c.getPreferredSize();
    int dx = (int)(d.getWidth() / 2.0D);
    int dy = (int)(d.getHeight() / 2.0D);
    m_x -= dx;
    m_y -= dy;
    c.setLocation(m_x, m_y);
    
    c.validate();
    

    if (container != null) {
      container.add(c);
      container.revalidate();
    }
  }
  




  public Object getBean()
  {
    return m_bean;
  }
  




  public int getX()
  {
    return m_x;
  }
  




  public int getY()
  {
    return m_y;
  }
  




  public int getWidth()
  {
    return ((JComponent)m_bean).getWidth();
  }
  




  public int getHeight()
  {
    return ((JComponent)m_bean).getHeight();
  }
  





  public void setXY(int newX, int newY)
  {
    setX(newX);
    setY(newY);
    if ((getBean() instanceof MetaBean)) {
      ((MetaBean)getBean()).shiftBeans(this, false);
    }
  }
  




  public void setX(int newX)
  {
    m_x = newX;
    ((JComponent)m_bean).setLocation(m_x, m_y);
    ((JComponent)m_bean).validate();
  }
  




  public void setY(int newY)
  {
    m_y = newY;
    ((JComponent)m_bean).setLocation(m_x, m_y);
    ((JComponent)m_bean).validate();
  }
}
