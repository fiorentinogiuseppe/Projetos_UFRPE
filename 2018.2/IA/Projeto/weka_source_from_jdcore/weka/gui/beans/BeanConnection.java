package weka.gui.beans;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;




































public class BeanConnection
  implements Serializable
{
  private static final long serialVersionUID = 8804264241791332064L;
  public static Vector CONNECTIONS = new Vector();
  

  private BeanInstance m_source;
  

  private BeanInstance m_target;
  

  private String m_eventName;
  

  private boolean m_hidden = false;
  


  public static void reset()
  {
    CONNECTIONS = new Vector();
  }
  




  public static Vector getConnections()
  {
    return CONNECTIONS;
  }
  




  public static void setConnections(Vector connections)
  {
    CONNECTIONS = connections;
  }
  









  private static boolean previousLink(BeanInstance source, BeanInstance target, int index)
  {
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      BeanInstance compSource = bc.getSource();
      BeanInstance compTarget = bc.getTarget();
      
      if ((compSource == source) && (compTarget == target) && (index < i)) {
        return true;
      }
    }
    return false;
  }
  





  private static boolean checkForSource(BeanInstance candidate, Vector listToCheck)
  {
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      if (bc.getSource() == candidate)
      {



        for (int j = 0; j < listToCheck.size(); j++) {
          BeanInstance tempTarget = (BeanInstance)listToCheck.elementAt(j);
          if (bc.getTarget() == tempTarget)
            return true;
        }
      }
    }
    return false;
  }
  




  private static boolean checkTargetConstraint(BeanInstance candidate, Vector listToCheck)
  {
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      if (bc.getTarget() == candidate) {
        for (int j = 0; j < listToCheck.size(); j++) {
          BeanInstance tempSource = (BeanInstance)listToCheck.elementAt(j);
          if (bc.getSource() == tempSource) {
            return false;
          }
        }
      }
    }
    return true;
  }
  







  public static Vector associatedConnections(Vector subFlow)
  {
    Vector associatedConnections = new Vector();
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      BeanInstance tempSource = bc.getSource();
      BeanInstance tempTarget = bc.getTarget();
      boolean sourceInSubFlow = false;
      boolean targetInSubFlow = false;
      for (int j = 0; j < subFlow.size(); j++) {
        BeanInstance toCheck = (BeanInstance)subFlow.elementAt(j);
        if (toCheck == tempSource) {
          sourceInSubFlow = true;
        }
        if (toCheck == tempTarget) {
          targetInSubFlow = true;
        }
        if ((sourceInSubFlow) && (targetInSubFlow)) {
          associatedConnections.add(bc);
          break;
        }
      }
    }
    return associatedConnections;
  }
  






  public static Vector inputs(Vector subset)
  {
    Vector result = new Vector();
    for (int i = 0; i < subset.size(); i++) {
      BeanInstance temp = (BeanInstance)subset.elementAt(i);
      

      if (checkTargetConstraint(temp, subset)) {
        result.add(temp);
      }
    }
    
    return result;
  }
  






  private static boolean checkForTarget(BeanInstance candidate, Vector listToCheck)
  {
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      if (bc.getTarget() == candidate)
      {



        for (int j = 0; j < listToCheck.size(); j++) {
          BeanInstance tempSource = (BeanInstance)listToCheck.elementAt(j);
          if (bc.getSource() == tempSource)
            return true;
        }
      }
    }
    return false;
  }
  
  private static boolean isInList(BeanInstance candidate, Vector listToCheck)
  {
    for (int i = 0; i < listToCheck.size(); i++) {
      BeanInstance temp = (BeanInstance)listToCheck.elementAt(i);
      if (candidate == temp) {
        return true;
      }
    }
    return false;
  }
  




  private static boolean checkSourceConstraint(BeanInstance candidate, Vector listToCheck)
  {
    boolean result = true;
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      if (bc.getSource() == candidate) {
        BeanInstance cTarget = bc.getTarget();
        
        if (!isInList(cTarget, listToCheck)) {
          return true;
        }
        for (int j = 0; j < listToCheck.size(); j++) {
          BeanInstance tempTarget = (BeanInstance)listToCheck.elementAt(j);
          if (bc.getTarget() == tempTarget) {
            result = false;
          }
        }
      }
    }
    return result;
  }
  






  public static Vector outputs(Vector subset)
  {
    Vector result = new Vector();
    for (int i = 0; i < subset.size(); i++) {
      BeanInstance temp = (BeanInstance)subset.elementAt(i);
      if (checkForTarget(temp, subset))
      {
        if (checkSourceConstraint(temp, subset)) {
          try
          {
            BeanInfo bi = Introspector.getBeanInfo(temp.getBean().getClass());
            EventSetDescriptor[] esd = bi.getEventSetDescriptors();
            if ((esd != null) && (esd.length > 0)) {
              result.add(temp);
            }
          }
          catch (IntrospectionException ex) {}
        }
      }
    }
    
    return result;
  }
  





  public static void paintConnections(Graphics gx)
  {
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      if (!bc.isHidden()) {
        BeanInstance source = bc.getSource();
        BeanInstance target = bc.getTarget();
        EventSetDescriptor srcEsd = bc.getSourceEventSetDescriptor();
        BeanVisual sourceVisual = (source.getBean() instanceof Visible) ? ((Visible)source.getBean()).getVisual() : null;
        

        BeanVisual targetVisual = (target.getBean() instanceof Visible) ? ((Visible)target.getBean()).getVisual() : null;
        

        if ((sourceVisual != null) && (targetVisual != null)) {
          Point bestSourcePt = sourceVisual.getClosestConnectorPoint(new Point(target.getX() + target.getWidth() / 2, target.getY() + target.getHeight() / 2));
          


          Point bestTargetPt = targetVisual.getClosestConnectorPoint(new Point(source.getX() + source.getWidth() / 2, source.getY() + source.getHeight() / 2));
          


          gx.setColor(Color.red);
          boolean active = true;
          if (((source.getBean() instanceof EventConstraints)) && 
            (!((EventConstraints)source.getBean()).eventGeneratable(srcEsd.getName())))
          {
            gx.setColor(Color.gray);
            active = false;
          }
          
          gx.drawLine((int)bestSourcePt.getX(), (int)bestSourcePt.getY(), (int)bestTargetPt.getX(), (int)bestTargetPt.getY());
          
          double angle;
          
          try
          {
            double a = (bestSourcePt.getY() - bestTargetPt.getY()) / (bestSourcePt.getX() - bestTargetPt.getX());
            


            angle = Math.atan(a);
          } catch (Exception ex) {
            angle = 1.5707963267948966D;
          }
          
          Point arrowstart = new Point(x, y);
          
          Point arrowoffset = new Point((int)(7.0D * Math.cos(angle)), (int)(7.0D * Math.sin(angle)));
          Point arrowend;
          Point arrowend;
          if (bestSourcePt.getX() >= bestTargetPt.getX())
          {
            arrowend = new Point(x + x, y + y);
          }
          else {
            arrowend = new Point(x - x, y - y);
          }
          
          int[] xs = { x, x + (int)(7.0D * Math.cos(angle + 1.5707963267948966D)), x + (int)(7.0D * Math.cos(angle - 1.5707963267948966D)) };
          

          int[] ys = { y, y + (int)(7.0D * Math.sin(angle + 1.5707963267948966D)), y + (int)(7.0D * Math.sin(angle - 1.5707963267948966D)) };
          

          gx.fillPolygon(xs, ys, 3);
          


          int midx = (int)bestSourcePt.getX();
          midx += (int)((bestTargetPt.getX() - bestSourcePt.getX()) / 2.0D);
          int midy = (int)bestSourcePt.getY();
          midy += (int)((bestTargetPt.getY() - bestSourcePt.getY()) / 2.0D) - 2;
          gx.setColor(active ? Color.blue : Color.gray);
          if (previousLink(source, target, i)) {
            midy -= 15;
          }
          gx.drawString(srcEsd.getName(), midx, midy);
        }
      }
    }
  }
  






  public static Vector getClosestConnections(Point pt, int delta)
  {
    Vector closestConnections = new Vector();
    
    for (int i = 0; i < CONNECTIONS.size(); i++) {
      BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
      BeanInstance source = bc.getSource();
      BeanInstance target = bc.getTarget();
      EventSetDescriptor srcEsd = bc.getSourceEventSetDescriptor();
      BeanVisual sourceVisual = (source.getBean() instanceof Visible) ? ((Visible)source.getBean()).getVisual() : null;
      

      BeanVisual targetVisual = (target.getBean() instanceof Visible) ? ((Visible)target.getBean()).getVisual() : null;
      

      if ((sourceVisual != null) && (targetVisual != null)) {
        Point bestSourcePt = sourceVisual.getClosestConnectorPoint(new Point(target.getX() + target.getWidth() / 2, target.getY() + target.getHeight() / 2));
        


        Point bestTargetPt = targetVisual.getClosestConnectorPoint(new Point(source.getX() + source.getWidth() / 2, source.getY() + source.getHeight() / 2));
        



        int minx = (int)Math.min(bestSourcePt.getX(), bestTargetPt.getX());
        int maxx = (int)Math.max(bestSourcePt.getX(), bestTargetPt.getX());
        int miny = (int)Math.min(bestSourcePt.getY(), bestTargetPt.getY());
        int maxy = (int)Math.max(bestSourcePt.getY(), bestTargetPt.getY());
        
        if ((pt.getX() >= minx - delta) && (pt.getX() <= maxx + delta) && (pt.getY() >= miny - delta) && (pt.getY() <= maxy + delta))
        {


          double a = bestSourcePt.getY() - bestTargetPt.getY();
          double b = bestTargetPt.getX() - bestSourcePt.getX();
          double c = bestSourcePt.getX() * bestTargetPt.getY() - bestTargetPt.getX() * bestSourcePt.getY();
          

          double distance = Math.abs(a * pt.getX() + b * pt.getY() + c);
          distance /= Math.abs(Math.sqrt(a * a + b * b));
          
          if (distance <= delta) {
            closestConnections.addElement(bc);
          }
        }
      }
    }
    return closestConnections;
  }
  










  public static void removeConnections(BeanInstance instance)
  {
    Vector instancesToRemoveFor = new Vector();
    if ((instance.getBean() instanceof MetaBean)) {
      instancesToRemoveFor = ((MetaBean)instance.getBean()).getBeansInSubFlow();
    }
    else {
      instancesToRemoveFor.add(instance);
    }
    Vector removeVector = new Vector();
    for (int j = 0; j < instancesToRemoveFor.size(); j++) {
      BeanInstance tempInstance = (BeanInstance)instancesToRemoveFor.elementAt(j);
      
      for (int i = 0; i < CONNECTIONS.size(); i++)
      {

        BeanConnection bc = (BeanConnection)CONNECTIONS.elementAt(i);
        BeanInstance tempTarget = bc.getTarget();
        BeanInstance tempSource = bc.getSource();
        
        EventSetDescriptor tempEsd = bc.getSourceEventSetDescriptor();
        if (tempInstance == tempTarget)
        {
          try {
            Method deregisterMethod = tempEsd.getRemoveListenerMethod();
            Object targetBean = tempTarget.getBean();
            Object[] args = new Object[1];
            args[0] = targetBean;
            deregisterMethod.invoke(tempSource.getBean(), args);
            
            removeVector.addElement(bc);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        } else if (tempInstance == tempSource) {
          removeVector.addElement(bc);
          if ((tempTarget.getBean() instanceof BeanCommon))
          {

            ((BeanCommon)tempTarget.getBean()).disconnectionNotification(tempEsd.getName(), tempSource.getBean());
          }
        }
      }
    }
    

    for (int i = 0; i < removeVector.size(); i++)
    {
      CONNECTIONS.removeElement((BeanConnection)removeVector.elementAt(i));
    }
  }
  


  public static void doMetaConnection(BeanInstance source, BeanInstance target, final EventSetDescriptor esd, final JComponent displayComponent)
  {
    Object targetBean = target.getBean();
    BeanInstance realTarget = null;
    BeanInstance realSource = source;
    if ((targetBean instanceof MetaBean)) {
      Vector receivers = ((MetaBean)targetBean).getSuitableTargets(esd);
      BeanConnection bc; if (receivers.size() == 1) {
        realTarget = (BeanInstance)receivers.elementAt(0);
        bc = new BeanConnection(realSource, realTarget, esd);

      }
      else
      {
        int menuItemCount = 0;
        JPopupMenu targetConnectionMenu = new JPopupMenu();
        Messages.getInstance();targetConnectionMenu.insert(new JLabel(Messages.getString("BeanConnection_DoMetaConnection_TargetConnectionMenu_Insert_Text_First"), 0), menuItemCount++);
        

        for (int i = 0; i < receivers.size(); i++) {
          final BeanInstance tempTarget = (BeanInstance)receivers.elementAt(i);
          
          String tName = "" + (i + 1) + ": " + ((tempTarget.getBean() instanceof BeanCommon) ? ((BeanCommon)tempTarget.getBean()).getCustomName() : tempTarget.getBean().getClass().getName());
          


          JMenuItem targetItem = new JMenuItem(tName);
          targetItem.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e) {
              BeanConnection bc = new BeanConnection(val$realSource, tempTarget, esd);
              

              displayComponent.repaint();
            }
          });
          targetConnectionMenu.add(targetItem);
          menuItemCount++;
        }
        targetConnectionMenu.show(displayComponent, target.getX(), target.getY());
      }
    }
  }
  










  public BeanConnection(BeanInstance source, BeanInstance target, EventSetDescriptor esd)
  {
    m_source = source;
    m_target = target;
    
    m_eventName = esd.getName();
    


    Method registrationMethod = esd.getAddListenerMethod();
    


    Object targetBean = m_target.getBean();
    
    Object[] args = new Object[1];
    args[0] = targetBean;
    Class listenerClass = esd.getListenerType();
    if (listenerClass.isInstance(targetBean)) {
      try {
        registrationMethod.invoke(m_source.getBean(), args);
        


        if ((targetBean instanceof BeanCommon)) {
          ((BeanCommon)targetBean).connectionNotification(esd.getName(), m_source.getBean());
        }
        
        CONNECTIONS.addElement(this);
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("BeanConnection_Error_Text_First"));
        ex.printStackTrace();
      }
    } else {
      Messages.getInstance();System.err.println(Messages.getString("BeanConnection_Error_Text_Second"));
    }
  }
  




  public void setHidden(boolean hidden)
  {
    m_hidden = hidden;
  }
  




  public boolean isHidden()
  {
    return m_hidden;
  }
  


  public void remove()
  {
    EventSetDescriptor tempEsd = getSourceEventSetDescriptor();
    try
    {
      Method deregisterMethod = tempEsd.getRemoveListenerMethod();
      Object targetBean = getTarget().getBean();
      Object[] args = new Object[1];
      args[0] = targetBean;
      deregisterMethod.invoke(getSource().getBean(), args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    
    if ((getTarget().getBean() instanceof BeanCommon))
    {
      ((BeanCommon)getTarget().getBean()).disconnectionNotification(tempEsd.getName(), getSource().getBean());
    }
    


    CONNECTIONS.remove(this);
  }
  




  public BeanInstance getSource()
  {
    return m_source;
  }
  




  public BeanInstance getTarget()
  {
    return m_target;
  }
  




  public String getEventName()
  {
    return m_eventName;
  }
  





  protected EventSetDescriptor getSourceEventSetDescriptor()
  {
    JComponent bc = (JComponent)m_source.getBean();
    try {
      BeanInfo sourceInfo = Introspector.getBeanInfo(bc.getClass());
      if (sourceInfo == null) {
        Messages.getInstance();System.err.println(Messages.getString("BeanConnection_GetSourceEventSetDescriptor_Error_Text_First"));
      } else {
        EventSetDescriptor[] esds = sourceInfo.getEventSetDescriptors();
        for (int i = 0; i < esds.length; i++) {
          if (esds[i].getName().compareTo(m_eventName) == 0) {
            return esds[i];
          }
        }
      }
    } catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("BeanConnection_GetSourceEventSetDescriptor_Error_Text_Second"));
    }
    return null;
  }
}
