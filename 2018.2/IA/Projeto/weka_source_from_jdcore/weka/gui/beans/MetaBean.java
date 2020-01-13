package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import weka.gui.Logger;

































public class MetaBean
  extends JPanel
  implements BeanCommon, Visible, EventConstraints, Serializable, UserRequestAcceptor
{
  private static final long serialVersionUID = -6582768902038027077L;
  protected BeanVisual m_visual = new BeanVisual("Group", "weka/gui/beans/icons/DiamondPlain.gif", "weka/gui/beans/icons/DiamondPlain.gif");
  



  private transient Logger m_log = null;
  private transient JWindow m_previewWindow = null;
  private transient Timer m_previewTimer = null;
  
  protected Vector m_subFlow = new Vector();
  protected Vector m_inputs = new Vector();
  protected Vector m_outputs = new Vector();
  

  protected Vector m_associatedConnections = new Vector();
  

  protected ImageIcon m_subFlowPreview = null;
  private Vector m_originalCoords;
  
  public MetaBean() { setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  
  public void setAssociatedConnections(Vector ac) {
    m_associatedConnections = ac;
  }
  
  public Vector getAssociatedConnections() {
    return m_associatedConnections;
  }
  
  public void setSubFlow(Vector sub) {
    m_subFlow = sub;
  }
  
  public Vector getSubFlow() {
    return m_subFlow;
  }
  
  public void setInputs(Vector inputs) {
    m_inputs = inputs;
  }
  
  public Vector getInputs() {
    return m_inputs;
  }
  
  public void setOutputs(Vector outputs) {
    m_outputs = outputs;
  }
  
  public Vector getOutputs() {
    return m_outputs;
  }
  
  private Vector getBeans(Vector beans, int type) {
    Vector comps = new Vector();
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance temp = (BeanInstance)beans.elementAt(i);
      
      if ((temp.getBean() instanceof MetaBean)) {
        switch (type) {
        case 0: 
          comps.addAll(((MetaBean)temp.getBean()).getBeansInSubFlow());
          break;
        case 1: 
          comps.addAll(((MetaBean)temp.getBean()).getBeansInInputs());
          break;
        case 2: 
          comps.addAll(((MetaBean)temp.getBean()).getBeansInOutputs());
        }
        
      } else {
        comps.add(temp);
      }
    }
    return comps;
  }
  
  private boolean beanSetContains(Vector set, BeanInstance toCheck) {
    boolean ok = false;
    
    for (int i = 0; i < set.size(); i++) {
      BeanInstance temp = (BeanInstance)set.elementAt(i);
      if (toCheck == temp) {
        ok = true;
        break;
      }
    }
    return ok;
  }
  
  public boolean subFlowContains(BeanInstance toCheck) {
    return beanSetContains(m_subFlow, toCheck);
  }
  
  public boolean inputsContains(BeanInstance toCheck) {
    return beanSetContains(m_inputs, toCheck);
  }
  
  public boolean outputsContains(BeanInstance toCheck) {
    return beanSetContains(m_outputs, toCheck);
  }
  




  public Vector getBeansInSubFlow()
  {
    return getBeans(m_subFlow, 0);
  }
  




  public Vector getBeansInInputs()
  {
    return getBeans(m_inputs, 1);
  }
  




  public Vector getBeansInOutputs()
  {
    return getBeans(m_outputs, 2);
  }
  
  private Vector getBeanInfos(Vector beans, int type) {
    Vector infos = new Vector();
    for (int i = 0; i < beans.size(); i++) {
      BeanInstance temp = (BeanInstance)beans.elementAt(i);
      if ((temp.getBean() instanceof MetaBean)) {
        switch (type) {
        case 0: 
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoSubFlow());
          break;
        case 1: 
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoInputs());
          break;
        case 2: 
          infos.addAll(((MetaBean)temp.getBean()).getBeanInfoOutputs());
        }
      } else {
        try {
          infos.add(Introspector.getBeanInfo(temp.getBean().getClass()));
        } catch (IntrospectionException ex) {
          ex.printStackTrace();
        }
      }
    }
    return infos;
  }
  
  public Vector getBeanInfoSubFlow() {
    return getBeanInfos(m_subFlow, 0);
  }
  
  public Vector getBeanInfoInputs() {
    return getBeanInfos(m_inputs, 1);
  }
  
  public Vector getBeanInfoOutputs() {
    return getBeanInfos(m_outputs, 2);
  }
  









  public Vector getOriginalCoords()
  {
    return m_originalCoords;
  }
  




  public void setOriginalCoords(Vector value)
  {
    m_originalCoords = value;
  }
  













  public void shiftBeans(BeanInstance toShiftTo, boolean save)
  {
    if (save) {
      m_originalCoords = new Vector();
    }
    int targetX = toShiftTo.getX();
    int targetY = toShiftTo.getY();
    
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if (save) {
        Point p = new Point(temp.getX(), temp.getY());
        m_originalCoords.add(p);
      }
      temp.setX(targetX);temp.setY(targetY);
    }
  }
  
  public void restoreBeans() {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      Point p = (Point)m_originalCoords.elementAt(i);
      JComponent c = (JComponent)temp.getBean();
      Dimension d = c.getPreferredSize();
      int dx = (int)(d.getWidth() / 2.0D);
      int dy = (int)(d.getHeight() / 2.0D);
      temp.setX((int)p.getX() + dx);
      temp.setY((int)p.getY() + dy);
    }
  }
  






  public boolean eventGeneratable(EventSetDescriptor esd)
  {
    String eventName = esd.getName();
    return eventGeneratable(eventName);
  }
  








  public boolean eventGeneratable(String eventName)
  {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance output = (BeanInstance)m_subFlow.elementAt(i);
      if (((output.getBean() instanceof EventConstraints)) && 
        (((EventConstraints)output.getBean()).eventGeneratable(eventName))) {
        return true;
      }
    }
    
    return false;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    Vector targets = getSuitableTargets(esd);
    for (int i = 0; i < targets.size(); i++) {
      BeanInstance input = (BeanInstance)targets.elementAt(i);
      if ((input.getBean() instanceof BeanCommon))
      {
        if (((BeanCommon)input.getBean()).connectionAllowed(esd)) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }
  
  public boolean connectionAllowed(String eventName) {
    return false;
  }
  









  public synchronized void connectionNotification(String eventName, Object source) {}
  









  public synchronized void disconnectionNotification(String eventName, Object source) {}
  









  public void stop()
  {
    for (int i = 0; i < m_inputs.size(); i++) {
      Object temp = m_inputs.elementAt(i);
      if ((temp instanceof BeanCommon)) {
        ((BeanCommon)temp).stop();
      }
    }
  }
  





  public boolean isBusy()
  {
    boolean result = false;
    for (int i = 0; i < m_subFlow.size(); i++) {
      Object temp = m_subFlow.elementAt(i);
      if (((temp instanceof BeanCommon)) && 
        (((BeanCommon)temp).isBusy())) {
        result = true;
        break;
      }
    }
    
    return result;
  }
  




  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  


  public BeanVisual getVisual()
  {
    return m_visual;
  }
  


  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/DiamondPlain.gif", "weka/gui/beans/icons/DiamondPlain.gif");
  }
  





  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector();
    if (m_subFlowPreview != null) {
      String text = "Show preview";
      if (m_previewWindow != null) {
        text = "$" + text;
      }
      newVector.addElement(text);
    }
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if ((temp.getBean() instanceof UserRequestAcceptor)) {
        String prefix = "";
        if ((temp.getBean() instanceof BeanCommon)) {
          prefix = ((BeanCommon)temp.getBean()).getCustomName();
        } else {
          prefix = temp.getBean().getClass().getName();
          prefix = prefix.substring(prefix.lastIndexOf('.') + 1, prefix.length());
        }
        prefix = "" + (i + 1) + ": (" + prefix + ")";
        Enumeration en = ((UserRequestAcceptor)temp.getBean()).enumerateRequests();
        while (en.hasMoreElements()) {
          String req = (String)en.nextElement();
          if (req.charAt(0) == '$') {
            prefix = '$' + prefix;
            req = req.substring(1, req.length());
          }
          
          if (req.charAt(0) == '?') {
            prefix = '?' + prefix;
            req = req.substring(1, req.length());
          }
          newVector.add(prefix + " " + req);
        }
      } else if ((temp.getBean() instanceof Startable)) {
        String prefix = "";
        if ((temp.getBean() instanceof BeanCommon)) {
          prefix = ((BeanCommon)temp.getBean()).getCustomName();
        } else {
          prefix = temp.getBean().getClass().getName();
          prefix = prefix.substring(prefix.lastIndexOf('.') + 1, prefix.length());
        }
        prefix = "" + (i + 1) + ": (" + prefix + ")";
        String startMessage = ((Startable)temp.getBean()).getStartMessage();
        if (startMessage.charAt(0) == '$') {
          prefix = '$' + prefix;
          startMessage = startMessage.substring(1, startMessage.length());
        }
        newVector.add(prefix + " " + startMessage);
      }
    }
    
    return newVector.elements();
  }
  
  public void setSubFlowPreview(ImageIcon sfp) {
    m_subFlowPreview = sfp;
  }
  
  private void showPreview() {
    if (m_previewWindow == null)
    {
      JLabel jl = new JLabel(m_subFlowPreview);
      
      jl.setLocation(0, 0);
      m_previewWindow = new JWindow();
      
      m_previewWindow.getContentPane().add(jl);
      m_previewWindow.validate();
      m_previewWindow.setSize(m_subFlowPreview.getIconWidth(), m_subFlowPreview.getIconHeight());
      
      m_previewWindow.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          m_previewWindow.dispose();
          m_previewWindow = null;
        }
        
      });
      m_previewWindow.setLocation(getParentgetLocationOnScreenx + getX() + getWidth() / 2 - m_subFlowPreview.getIconWidth() / 2, getParentgetLocationOnScreeny + getY() + getHeight() / 2 - m_subFlowPreview.getIconHeight() / 2);
      




      m_previewWindow.setVisible(true);
      m_previewTimer = new Timer(8000, new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          if (m_previewWindow != null) {
            m_previewWindow.dispose();
            m_previewWindow = null;
            m_previewTimer = null;
          }
        }
      });
      m_previewTimer.setRepeats(false);
      m_previewTimer.start();
    }
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Show preview") == 0) {
      showPreview();
      return;
    }
    
    if (request.indexOf(":") < 0) {
      return;
    }
    String tempI = request.substring(0, request.indexOf(':'));
    int index = Integer.parseInt(tempI);
    index--;
    String req = request.substring(request.indexOf(')') + 1, request.length()).trim();
    

    Object target = ((BeanInstance)m_subFlow.elementAt(index)).getBean();
    if (((target instanceof Startable)) && (req.equals(((Startable)target).getStartMessage()))) {
      try {
        ((Startable)target).start();
      } catch (Exception ex) {
        if (m_log != null) {
          String compName = (target instanceof BeanCommon) ? ((BeanCommon)target).getCustomName() : "";
          m_log.logMessage("Problem starting subcomponent " + compName);
        }
      }
    } else {
      ((UserRequestAcceptor)target).performRequest(req);
    }
  }
  




  public void setLog(Logger logger)
  {
    m_log = logger;
  }
  
  public void removePropertyChangeListenersSubFlow(PropertyChangeListener pcl) {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().removePropertyChangeListener(pcl);
      }
      
      if ((temp.getBean() instanceof MetaBean)) {
        ((MetaBean)temp.getBean()).removePropertyChangeListenersSubFlow(pcl);
      }
    }
  }
  
  public void addPropertyChangeListenersSubFlow(PropertyChangeListener pcl) {
    for (int i = 0; i < m_subFlow.size(); i++) {
      BeanInstance temp = (BeanInstance)m_subFlow.elementAt(i);
      if ((temp.getBean() instanceof Visible)) {
        ((Visible)temp.getBean()).getVisual().addPropertyChangeListener(pcl);
      }
      
      if ((temp.getBean() instanceof MetaBean)) {
        ((MetaBean)temp.getBean()).addPropertyChangeListenersSubFlow(pcl);
      }
    }
  }
  





  public boolean canAcceptConnection(Class listenerClass)
  {
    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance input = (BeanInstance)m_inputs.elementAt(i);
      if (listenerClass.isInstance(input.getBean())) {
        return true;
      }
    }
    return false;
  }
  






  public Vector getSuitableTargets(EventSetDescriptor esd)
  {
    Class listenerClass = esd.getListenerType();
    Vector targets = new Vector();
    for (int i = 0; i < m_inputs.size(); i++) {
      BeanInstance input = (BeanInstance)m_inputs.elementAt(i);
      if (listenerClass.isInstance(input.getBean())) {
        targets.add(input);
      }
    }
    return targets;
  }
}
