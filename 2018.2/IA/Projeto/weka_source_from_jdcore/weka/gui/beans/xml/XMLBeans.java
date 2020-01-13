package weka.gui.beans.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.beancontext.BeanContextSupport;
import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ConverterUtils;
import weka.core.converters.DatabaseLoader;
import weka.core.converters.DatabaseSaver;
import weka.core.converters.FileSourcedConverter;
import weka.core.converters.TextDirectoryLoader;
import weka.core.xml.PropertyHandler;
import weka.core.xml.XMLBasicSerialization;
import weka.core.xml.XMLDocument;
import weka.core.xml.XMLSerializationMethodHandler;
import weka.experiment.ResultProducer;
import weka.experiment.SplitEvaluator;
import weka.gui.beans.BeanCommon;
import weka.gui.beans.BeanConnection;
import weka.gui.beans.BeanInstance;
import weka.gui.beans.BeanVisual;
import weka.gui.beans.MetaBean;
import weka.gui.beans.Visible;




































































































































public class XMLBeans
  extends XMLBasicSerialization
{
  public static final String VAL_ID = "id";
  public static final String VAL_X = "x";
  public static final String VAL_Y = "y";
  public static final String VAL_BEAN = "bean";
  public static final String VAL_CUSTOM_NAME = "custom_name";
  public static final String VAL_SOURCEID = "source_id";
  public static final String VAL_TARGETID = "target_id";
  public static final String VAL_EVENTNAME = "eventname";
  public static final String VAL_HIDDEN = "hidden";
  public static final String VAL_FILE = "file";
  public static final String VAL_DIR = "dir";
  public static final String VAL_PREFIX = "prefix";
  public static final String VAL_RELATIVE_PATH = "useRelativePath";
  public static final String VAL_OPTIONS = "options";
  public static final String VAL_SAVER = "wrappedAlgorithm";
  public static final String VAL_LOADER = "wrappedAlgorithm";
  public static final String VAL_TEXT = "text";
  public static final String VAL_BEANCONTEXT = "beanContext";
  public static final String VAL_WIDTH = "width";
  public static final String VAL_HEIGHT = "height";
  public static final String VAL_RED = "red";
  public static final String VAL_GREEN = "green";
  public static final String VAL_BLUE = "blue";
  public static final String VAL_NAME = "name";
  public static final String VAL_STYLE = "style";
  public static final String VAL_LOCATION = "location";
  public static final String VAL_SIZE = "size";
  public static final String VAL_COLOR = "color";
  public static final String VAL_FONT = "font";
  public static final String VAL_ICONPATH = "iconPath";
  public static final String VAL_ANIMATEDICONPATH = "animatedIconPath";
  public static final String VAL_ASSOCIATEDCONNECTIONS = "associatedConnections";
  public static final String VAL_INPUTS = "inputs";
  public static final String VAL_INPUTSID = "inputs_id";
  public static final String VAL_OUTPUTS = "outputs";
  public static final String VAL_OUTPUTSID = "outputs_id";
  public static final String VAL_SUBFLOW = "subFlow";
  public static final String VAL_ORIGINALCOORDS = "originalCoords";
  public static final String VAL_RELATIONNAMEFORFILENAME = "relationNameForFilename";
  public static final int INDEX_BEANINSTANCES = 0;
  public static final int INDEX_BEANCONNECTIONS = 1;
  protected JComponent m_BeanLayout;
  protected Vector m_BeanInstances;
  protected Vector m_BeanInstancesID;
  protected boolean m_IgnoreBeanConnections;
  protected MetaBean m_CurrentMetaBean;
  protected static final String REGULAR_CONNECTION = "regular_connection";
  protected Hashtable m_BeanConnectionRelation;
  public static final int DATATYPE_LAYOUT = 0;
  public static final int DATATYPE_USERCOMPONENTS = 1;
  protected int m_DataType = 0;
  


  protected BeanContextSupport m_BeanContextSupport = null;
  





  public XMLBeans(JComponent layout, BeanContextSupport context)
    throws Exception
  {
    this(layout, context, 0);
  }
  








  public XMLBeans(JComponent layout, BeanContextSupport context, int datatype)
    throws Exception
  {
    m_BeanLayout = layout;
    m_BeanContextSupport = context;
    setDataType(datatype);
  }
  




  public void setDataType(int value)
  {
    if (value == 0) {
      m_DataType = value;
    } else if (value == 1) {
      m_DataType = value;
    } else {
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_SetDataType_DataType_Text_Front") + value + Messages.getString("XMLBeans_SetDataType_DataType_Text_End"));
    }
  }
  



  public int getDataType()
  {
    return m_DataType;
  }
  







  public void clear()
    throws Exception
  {
    super.clear();
    



    m_Properties.addIgnored("UI");
    m_Properties.addIgnored("actionMap");
    m_Properties.addIgnored("alignmentX");
    m_Properties.addIgnored("alignmentY");
    m_Properties.addIgnored("autoscrolls");
    m_Properties.addIgnored("background");
    m_Properties.addIgnored("border");
    m_Properties.addIgnored("componentPopupMenu");
    m_Properties.addIgnored("debugGraphicsOptions");
    m_Properties.addIgnored("doubleBuffered");
    m_Properties.addIgnored("enabled");
    m_Properties.addIgnored("focusCycleRoot");
    m_Properties.addIgnored("focusTraversalPolicy");
    m_Properties.addIgnored("focusTraversalPolicyProvider");
    m_Properties.addIgnored("focusable");
    m_Properties.addIgnored("font");
    m_Properties.addIgnored("foreground");
    m_Properties.addIgnored("inheritsPopupMenu");
    m_Properties.addIgnored("inputVerifier");
    m_Properties.addIgnored("layout");
    m_Properties.addIgnored("locale");
    m_Properties.addIgnored("maximumSize");
    m_Properties.addIgnored("minimumSize");
    m_Properties.addIgnored("nextFocusableComponent");
    m_Properties.addIgnored("opaque");
    m_Properties.addIgnored("preferredSize");
    m_Properties.addIgnored("requestFocusEnabled");
    m_Properties.addIgnored("toolTipText");
    m_Properties.addIgnored("transferHandler");
    m_Properties.addIgnored("verifyInputWhenFocusTarget");
    m_Properties.addIgnored("visible");
    

    m_Properties.addIgnored("size");
    m_Properties.addIgnored("location");
    

    m_Properties.addAllowed(BeanInstance.class, "x");
    m_Properties.addAllowed(BeanInstance.class, "y");
    m_Properties.addAllowed(BeanInstance.class, "bean");
    m_Properties.addAllowed(weka.gui.beans.Saver.class, "wrappedAlgorithm");
    m_Properties.addAllowed(weka.gui.beans.Loader.class, "wrappedAlgorithm");
    m_Properties.addAllowed(weka.gui.beans.Saver.class, "relationNameForFilename");
    if (getDataType() == 0) {
      m_Properties.addAllowed(weka.gui.beans.Loader.class, "beanContext");
    } else
      m_Properties.addIgnored(weka.gui.beans.Loader.class, "beanContext");
    m_Properties.addAllowed(weka.gui.beans.Filter.class, "filter");
    m_Properties.addAllowed(weka.gui.beans.Associator.class, "associator");
    m_Properties.addAllowed(weka.gui.beans.Classifier.class, "wrappedAlgorithm");
    m_Properties.addAllowed(weka.gui.beans.Clusterer.class, "wrappedAlgorithm");
    m_Properties.addAllowed(weka.gui.beans.Classifier.class, "executionSlots");
    
    m_Properties.addAllowed(weka.classifiers.Classifier.class, "debug");
    m_Properties.addAllowed(weka.classifiers.Classifier.class, "options");
    m_Properties.addAllowed(weka.filters.Filter.class, "options");
    m_Properties.addAllowed(weka.associations.Associator.class, "options");
    m_Properties.addAllowed(weka.clusterers.Clusterer.class, "options");
    
    m_Properties.addAllowed(DatabaseSaver.class, "options");
    m_Properties.addAllowed(DatabaseLoader.class, "options");
    m_Properties.addAllowed(TextDirectoryLoader.class, "options");
    

    m_Properties.addAllowed(SplitEvaluator.class, "options");
    
    m_Properties.addAllowed(ResultProducer.class, "options");
    

    m_CustomMethods.register(this, Color.class, "Color");
    m_CustomMethods.register(this, Dimension.class, "Dimension");
    m_CustomMethods.register(this, Font.class, "Font");
    m_CustomMethods.register(this, Point.class, "Point");
    m_CustomMethods.register(this, ColorUIResource.class, "ColorUIResource");
    m_CustomMethods.register(this, FontUIResource.class, "FontUIResource");
    
    m_CustomMethods.register(this, BeanInstance.class, "BeanInstance");
    m_CustomMethods.register(this, BeanConnection.class, "BeanConnection");
    m_CustomMethods.register(this, BeanVisual.class, "BeanVisual");
    m_CustomMethods.register(this, weka.gui.beans.Saver.class, "BeanSaver");
    m_CustomMethods.register(this, MetaBean.class, "MetaBean");
    
    Vector<String> classnames = ConverterUtils.getFileLoaders();
    for (int i = 0; i < classnames.size(); i++)
      m_CustomMethods.register(this, Class.forName((String)classnames.get(i)), "Loader");
    classnames = ConverterUtils.getFileSavers();
    for (i = 0; i < classnames.size(); i++) {
      m_CustomMethods.register(this, Class.forName((String)classnames.get(i)), "Saver");
    }
    
    m_BeanInstances = null;
    m_BeanInstancesID = null;
    m_CurrentMetaBean = null;
    m_IgnoreBeanConnections = true;
    m_BeanConnectionRelation = null;
  }
  







  protected void addBeanInstances(Vector list)
  {
    for (int i = 0; i < list.size(); i++) {
      if ((list.get(i) instanceof BeanInstance)) {
        BeanInstance beaninst = (BeanInstance)list.get(i);
        
        m_BeanInstancesID.add(new Integer(m_BeanInstances.size()));
        m_BeanInstances.add(beaninst);
        
        if ((beaninst.getBean() instanceof MetaBean)) {
          addBeanInstances(((MetaBean)beaninst.getBean()).getBeansInSubFlow());
        }
      } else if ((list.get(i) instanceof MetaBean)) {
        addBeanInstances(((MetaBean)list.get(i)).getBeansInSubFlow());
      }
      else {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_SetDataType_AddBeanInstances_Text_Front") + list.get(i) + Messages.getString("XMLBeans_SetDataType_AddBeanInstances_Text_End"));
      }
    }
  }
  






  protected Object writePreProcess(Object o)
    throws Exception
  {
    o = super.writePreProcess(o);
    

    m_BeanInstances = new Vector();
    m_BeanInstancesID = new Vector();
    
    switch (getDataType()) {
    case 0: 
      addBeanInstances(BeanInstance.getBeanInstances());
      break;
    
    case 1: 
      addBeanInstances((Vector)o);
      break;
    
    default: 
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_WritePreProcess_Text_Front") + getDataType() + Messages.getString("XMLBeans_WritePreProcess_Text_End"));
    }
    
    
    return o;
  }
  
















  protected void writePostProcess(Object o)
    throws Exception
  {
    if (getDataType() == 0) {
      Element root = m_Document.getDocument().getDocumentElement();
      Element conns = (Element)root.getChildNodes().item(1);
      NodeList list = conns.getChildNodes();
      for (int i = 0; i < list.getLength(); i++) {
        Element child = (Element)list.item(i);
        child.setAttribute("name", "" + i);
      }
    }
  }
  



















  protected Document readPreProcess(Document document)
    throws Exception
  {
    m_BeanInstances = new Vector();
    m_BeanInstancesID = new Vector();
    

    NodeList list = document.getElementsByTagName("*");
    String clsName = BeanInstance.class.getName();
    for (int i = 0; i < list.getLength(); i++) {
      Element node = (Element)list.item(i);
      

      if (node.getAttribute("class").equals(clsName)) {
        Vector children = XMLDocument.getChildTags(node);
        int id = m_BeanInstancesID.size();
        

        for (int n = 0; n < children.size(); n++) {
          Element child = (Element)children.get(n);
          if (child.getAttribute("name").equals("id")) {
            id = readIntFromXML(child);
          }
        }
        m_BeanInstancesID.add(new Integer(id));
      }
    }
    
    m_BeanInstances.setSize(m_BeanInstancesID.size());
    

    m_CurrentMetaBean = null;
    

    m_IgnoreBeanConnections = true;
    

    m_BeanConnectionRelation = new Hashtable();
    
    return document;
  }
  









  protected void setBeanConnection(BeanConnection conn, Vector list)
  {
    boolean added = false;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) == null) {
        list.set(i, conn);
        added = true;
        break;
      }
    }
    
    if (!added) {
      list.add(conn);
    }
  }
  













  protected BeanConnection createBeanConnection(int sourcePos, int targetPos, String event, boolean hidden)
    throws Exception
  {
    BeanConnection result = null;
    

    if ((sourcePos == -1) || (targetPos == -1)) {
      return result;
    }
    BeanInstance instSource = (BeanInstance)m_BeanInstances.get(sourcePos);
    BeanInstance instTarget = (BeanInstance)m_BeanInstances.get(targetPos);
    
    BeanInfo compInfo = Introspector.getBeanInfo(((BeanInstance)m_BeanInstances.get(sourcePos)).getBean().getClass());
    EventSetDescriptor[] esds = compInfo.getEventSetDescriptors();
    
    for (int i = 0; i < esds.length; i++) {
      if (esds[i].getName().equals(event)) {
        result = new BeanConnection(instSource, instTarget, esds[i]);
        result.setHidden(hidden);
        break;
      }
    }
    
    return result;
  }
  

















  protected void rebuildBeanConnections(Vector deserialized, Object key)
    throws Exception
  {
    Vector conns = (Vector)m_BeanConnectionRelation.get(key);
    

    if (conns == null) {
      return;
    }
    for (int n = 0; n < conns.size(); n++) {
      StringTokenizer tok = new StringTokenizer(conns.get(n).toString(), ",");
      BeanConnection conn = null;
      int sourcePos = Integer.parseInt(tok.nextToken());
      int targetPos = Integer.parseInt(tok.nextToken());
      String event = tok.nextToken();
      boolean hidden = stringToBoolean(tok.nextToken());
      


      if ((!(key instanceof MetaBean)) || (getDataType() == 1)) {
        conn = createBeanConnection(sourcePos, targetPos, event, hidden);
      }
      else
      {
        Vector beanconns = BeanConnection.getConnections();
        
        for (int i = 0; i < beanconns.size(); i++) {
          conn = (BeanConnection)beanconns.get(i);
          if ((conn.getSource() == (BeanInstance)m_BeanInstances.get(sourcePos)) && (conn.getTarget() == (BeanInstance)m_BeanInstances.get(targetPos)) && (conn.getEventName().equals(event))) {
            break;
          }
          

          conn = null;
        }
      }
      

      if ((key instanceof MetaBean)) {
        setBeanConnection(conn, ((MetaBean)key).getAssociatedConnections());
      } else {
        setBeanConnection(conn, (Vector)deserialized.get(1));
      }
    }
  }
  










  protected void removeUserToolBarBeans(Vector metabeans)
  {
    for (int i = 0; i < metabeans.size(); i++) {
      MetaBean meta = (MetaBean)metabeans.get(i);
      Vector subflow = meta.getSubFlow();
      
      for (int n = 0; n < subflow.size(); n++) {
        BeanInstance beaninst = (BeanInstance)subflow.get(n);
        beaninst.removeBean(m_BeanLayout);
      }
    }
  }
  










  protected Object readPostProcess(Object o)
    throws Exception
  {
    Vector deserialized = (Vector)super.readPostProcess(o);
    

    rebuildBeanConnections(deserialized, "regular_connection");
    

    Enumeration enm = m_BeanConnectionRelation.keys();
    while (enm.hasMoreElements()) {
      Object key = enm.nextElement();
      

      if ((key instanceof MetaBean))
      {

        rebuildBeanConnections(deserialized, key);
      }
    }
    
    if (getDataType() == 1) {
      removeUserToolBarBeans(deserialized);
    }
    return deserialized;
  }
  



  protected Vector getBeanConnectionRelation(MetaBean meta)
  {
    Object key;
    


    Object key;
    

    if (meta == null) {
      key = "regular_connection";
    } else {
      key = meta;
    }
    
    if (!m_BeanConnectionRelation.containsKey(key)) {
      m_BeanConnectionRelation.put(key, new Vector());
    }
    
    Vector result = (Vector)m_BeanConnectionRelation.get(key);
    
    return result;
  }
  









  protected void addBeanConnectionRelation(MetaBean meta, String connection)
  {
    Vector relations = getBeanConnectionRelation(meta);
    

    relations.add(connection);
    Object key;
    Object key;
    if (meta == null) {
      key = "regular_connection";
    } else
      key = meta;
    m_BeanConnectionRelation.put(key, relations);
  }
  













  public Element writeColor(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Color color = (Color)o;
    Element node = addElement(parent, name, color.getClass().getName(), false);
    
    writeIntToXML(node, color.getRed(), "red");
    writeIntToXML(node, color.getGreen(), "green");
    writeIntToXML(node, color.getBlue(), "blue");
    
    return node;
  }
  















  public Object readColor(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    int red = 0;
    int green = 0;
    int blue = 0;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("red")) {
        red = readIntFromXML(child);
      } else if (name.equals("green")) {
        green = readIntFromXML(child);
      } else if (name.equals("blue")) {
        blue = readIntFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadColor_Text_First") + name + Messages.getString("XMLBeans_ReadColor_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadColor_Text_Third"));
      }
    }
    

    result = new Color(red, green, blue);
    
    return result;
  }
  













  public Element writeDimension(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Dimension dim = (Dimension)o;
    Element node = addElement(parent, name, dim.getClass().getName(), false);
    
    writeDoubleToXML(node, dim.getWidth(), "width");
    writeDoubleToXML(node, dim.getHeight(), "height");
    
    return node;
  }
  














  public Object readDimension(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    double width = 0.0D;
    double height = 0.0D;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("width")) {
        width = readDoubleFromXML(child);
      } else if (name.equals("height")) {
        height = readDoubleFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadDimension_Text_First") + name + Messages.getString("XMLBeans_ReadDimension_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadDimension_Text_Third"));
      }
    }
    
    result = new Dimension();
    ((Dimension)result).setSize(width, height);
    
    return result;
  }
  













  public Element writeFont(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Font font = (Font)o;
    Element node = addElement(parent, name, font.getClass().getName(), false);
    
    invokeWriteToXML(node, font.getName(), "name");
    writeIntToXML(node, font.getStyle(), "style");
    writeIntToXML(node, font.getSize(), "size");
    
    return node;
  }
  















  public Object readFont(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    String fontname = "";
    int style = 0;
    int size = 0;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("name")) {
        name = (String)invokeReadFromXML(child);
      } else if (name.equals("style")) {
        style = readIntFromXML(child);
      } else if (name.equals("size")) {
        size = readIntFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadFont_Text_First") + name + Messages.getString("XMLBeans_ReadFont_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadFont_Text_Third"));
      }
    }
    
    result = new Font(fontname, style, size);
    
    return result;
  }
  













  public Element writePoint(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Point p = (Point)o;
    Element node = addElement(parent, name, p.getClass().getName(), false);
    
    writeDoubleToXML(node, p.getX(), "x");
    writeDoubleToXML(node, p.getY(), "y");
    
    return node;
  }
  














  public Object readPoint(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    double x = 0.0D;
    double y = 0.0D;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("x")) {
        x = readDoubleFromXML(child);
      } else if (name.equals("y")) {
        y = readDoubleFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadPoint_Text_First") + name + Messages.getString("XMLBeans_ReadPoint_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadPoint_Text_Third"));
      }
    }
    

    result = new Point();
    ((Point)result).setLocation(x, y);
    
    return result;
  }
  













  public Element writeColorUIResource(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    ColorUIResource resource = (ColorUIResource)o;
    Element node = addElement(parent, name, resource.getClass().getName(), false);
    invokeWriteToXML(node, new Color(resource.getRGB()), "color");
    
    return node;
  }
  













  public Object readColorUIResource(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    Color color = null;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("color")) {
        color = (Color)invokeReadFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadColorUIResource_Text_First") + name + Messages.getString("XMLBeans_ReadColorUIResource_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadColorUIResource_Text_Third"));
      }
    }
    

    result = new ColorUIResource(color);
    
    return result;
  }
  













  public Element writeFontUIResource(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    FontUIResource resource = (FontUIResource)o;
    Element node = addElement(parent, name, resource.getClass().getName(), false);
    invokeWriteToXML(node, new Font(resource.getName(), resource.getStyle(), resource.getSize()), "color");
    
    return node;
  }
  













  public Object readFontUIResource(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    Font font = null;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("font")) {
        font = (Font)invokeReadFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadFontUIResource_Text_First") + name + Messages.getString("XMLBeans_ReadFontUIResource_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadFontUIResource_Text_Third"));
      }
    }
    

    result = new FontUIResource(font);
    
    return result;
  }
  













  public Element writeBeanInstance(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    BeanInstance beaninst = (BeanInstance)o;
    Element node = addElement(parent, name, beaninst.getClass().getName(), false);
    
    writeIntToXML(node, m_BeanInstances.indexOf(beaninst), "id");
    writeIntToXML(node, beaninst.getX() + beaninst.getWidth() / 2, "x");
    writeIntToXML(node, beaninst.getY() + beaninst.getHeight() / 2, "y");
    if ((beaninst.getBean() instanceof BeanCommon))
    {
      String custName = ((BeanCommon)beaninst.getBean()).getCustomName();
      invokeWriteToXML(node, custName, "custom_name");
    }
    invokeWriteToXML(node, beaninst.getBean(), "bean");
    
    return node;
  }
  


















  public Object readBeanInstance(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    int id = -1;
    int x = 0;
    int y = 0;
    Object bean = null;
    String customName = null;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("id")) {
        id = readIntFromXML(child);
      } else if (name.equals("x")) {
        x = readIntFromXML(child);
      } else if (name.equals("y")) {
        y = readIntFromXML(child);
      } else if (name.equals("custom_name")) {
        customName = (String)invokeReadFromXML(child);
      } else if (name.equals("bean")) {
        bean = invokeReadFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadBeanInstance_Text_First") + name + Messages.getString("XMLBeans_ReadBeanInstance_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadBeanInstance_Text_Third"));
      }
    }
    


    result = new BeanInstance(m_BeanLayout, bean, x, y);
    BeanInstance beaninst = (BeanInstance)result;
    

    if ((beaninst.getBean() instanceof Visible)) {
      BeanVisual visual = ((Visible)beaninst.getBean()).getVisual();
      visual.setSize(visual.getPreferredSize());
      if (visual.getParent() == null) {
        ((JPanel)beaninst.getBean()).add(visual);
      }
    }
    
    if (((beaninst.getBean() instanceof BeanCommon)) && (customName != null))
    {
      ((BeanCommon)beaninst.getBean()).setCustomName(customName);
    }
    

    if (id == -1) {
      for (i = 0; i < m_BeanInstances.size(); i++) {
        if (m_BeanInstances.get(i) == null) {
          id = ((Integer)m_BeanInstancesID.get(i)).intValue();
          break;
        }
      }
    }
    
    i = m_BeanInstancesID.indexOf(new Integer(id));
    

    m_BeanInstances.set(i, result);
    

    m_CurrentMetaBean = null;
    
    return result;
  }
  

















  public Element writeBeanConnection(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    BeanConnection beanconn = (BeanConnection)o;
    Element node = null;
    

    int sourcePos = m_BeanInstances.indexOf(beanconn.getSource());
    int targetPos = m_BeanInstances.indexOf(beanconn.getTarget());
    int target;
    int source;
    int target; if ((sourcePos > -1) && (targetPos > -1)) {
      int source = ((Integer)m_BeanInstancesID.get(sourcePos)).intValue();
      target = ((Integer)m_BeanInstancesID.get(targetPos)).intValue();
    }
    else {
      source = -1;
      target = -1;
    }
    

    if ((source > -1) && (target > -1)) {
      node = addElement(parent, name, beanconn.getClass().getName(), false);
      
      writeIntToXML(node, source, "source_id");
      writeIntToXML(node, target, "target_id");
      invokeWriteToXML(node, beanconn.getEventName(), "eventname");
      writeBooleanToXML(node, beanconn.isHidden(), "hidden");
    }
    
    return node;
  }
  


















  public Object readBeanConnection(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    int source = 0;
    int target = 0;
    String event = "";
    boolean hidden = false;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("source_id")) {
        source = readIntFromXML(child);
      } else if (name.equals("target_id")) {
        target = readIntFromXML(child);
      } else if (name.equals("eventname")) {
        event = (String)invokeReadFromXML(child);
      } else if (name.equals("hidden")) {
        hidden = readBooleanFromXML(child);
      } else {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadBeanConnection_Text_First") + name + Messages.getString("XMLBeans_ReadBeanConnection_Text_Second") + node.getAttribute("name") + Messages.getString("XMLBeans_ReadBeanConnection_Text_Third"));
      }
    }
    


    int sourcePos = m_BeanInstancesID.indexOf(new Integer(source));
    int targetPos = m_BeanInstancesID.indexOf(new Integer(target));
    


    if (m_IgnoreBeanConnections) {
      addBeanConnectionRelation(m_CurrentMetaBean, sourcePos + "," + targetPos + "," + event + "," + hidden);
      return result;
    }
    

    result = createBeanConnection(sourcePos, targetPos, event, hidden);
    
    return result;
  }
  













  public Element writeBeanLoader(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.gui.beans.Loader loader = (weka.gui.beans.Loader)o;
    Element node = addElement(parent, name, loader.getClass().getName(), false);
    
    invokeWriteToXML(node, loader.getLoader(), "wrappedAlgorithm");
    invokeWriteToXML(node, loader.getBeanContext(), "beanContext");
    
    return node;
  }
  













  public Element writeBeanSaver(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.gui.beans.Saver saver = (weka.gui.beans.Saver)o;
    Element node = addElement(parent, name, saver.getClass().getName(), false);
    invokeWriteToXML(node, Boolean.valueOf(saver.getRelationNameForFilename()), "relationNameForFilename");
    
    invokeWriteToXML(node, saver.getSaverTemplate(), "wrappedAlgorithm");
    
    return node;
  }
  















  public Element writeLoader(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.core.converters.Loader loader = (weka.core.converters.Loader)o;
    Element node = addElement(parent, name, loader.getClass().getName(), false);
    boolean known = true;
    File file = null;
    

    if ((loader instanceof AbstractFileLoader)) {
      file = ((AbstractFileLoader)loader).retrieveFile();
    } else {
      known = false;
    }
    if (!known) {
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_WriteLoader_Text_First") + loader.getClass().getName() + Messages.getString("XMLBeans_WriteLoader_Text_Second"));
    }
    
    Boolean relativeB = null;
    if ((loader instanceof FileSourcedConverter)) {
      boolean relative = ((FileSourcedConverter)loader).getUseRelativePath();
      relativeB = new Boolean(relative);
    }
    

    if ((file == null) || (file.isDirectory())) {
      invokeWriteToXML(node, "", "file");
    } else {
      boolean notAbsolute = (((AbstractFileLoader)loader).getUseRelativePath()) || (((loader instanceof EnvironmentHandler)) && (Environment.containsEnvVariables(file.getPath())));
      



      String path = notAbsolute ? file.getPath() : file.getAbsolutePath();
      



      path = path.replace('\\', '/');
      invokeWriteToXML(node, path, "file");
    }
    if (relativeB != null) {
      invokeWriteToXML(node, relativeB.toString(), "useRelativePath");
    }
    
    return node;
  }
  














  public Object readLoader(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = Class.forName(node.getAttribute("class")).newInstance();
    Vector children = XMLDocument.getChildTags(node);
    String file = "";
    Object relativeB = null;
    boolean relative = false;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("file")) {
        file = (String)invokeReadFromXML(child);
      } else if (name.equals("useRelativePath")) {
        relativeB = readFromXML(child);
        if ((relativeB instanceof Boolean)) {
          relative = ((Boolean)relativeB).booleanValue();
        }
      } else {
        readFromXML(result, name, child);
      }
    }
    
    if ((result instanceof FileSourcedConverter)) {
      ((FileSourcedConverter)result).setUseRelativePath(relative);
    }
    
    if (file.equals("")) {
      file = null;
    }
    
    if (file != null) {
      String tempFile = file;
      
      boolean containsEnv = false;
      containsEnv = Environment.containsEnvVariables(file);
      
      File fl = new File(file);
      

      if ((containsEnv) || (fl.exists())) {
        ((AbstractFileLoader)result).setSource(new File(file));
      } else {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadLoader_Text_Front") + tempFile + Messages.getString("XMLBeans_ReadLoader_Text_End"));
      }
    }
    

    return result;
  }
  

















  public Element writeSaver(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.core.converters.Saver saver = (weka.core.converters.Saver)o;
    Element node = addElement(parent, name, saver.getClass().getName(), false);
    boolean known = true;
    File file = null;
    String prefix = "";
    String dir = "";
    

    if ((saver instanceof AbstractFileSaver)) {
      file = ((AbstractFileSaver)saver).retrieveFile();
      prefix = ((AbstractFileSaver)saver).filePrefix();
      dir = ((AbstractFileSaver)saver).retrieveDir();
      

      dir = dir.replace('\\', '/');
    }
    else {
      known = false;
    }
    
    if (!known) {
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_WriteSaver_Text_Front") + saver.getClass().getName() + Messages.getString("XMLBeans_WriteSaver_Text_End"));
    }
    
    Boolean relativeB = null;
    if ((saver instanceof FileSourcedConverter)) {
      boolean relative = ((FileSourcedConverter)saver).getUseRelativePath();
      relativeB = new Boolean(relative);
    }
    


    invokeWriteToXML(node, "", "file");
    invokeWriteToXML(node, dir, "dir");
    invokeWriteToXML(node, prefix, "prefix");
    












    if (relativeB != null) {
      invokeWriteToXML(node, relativeB.toString(), "useRelativePath");
    }
    
    return node;
  }
  















  public Object readSaver(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = Class.forName(node.getAttribute("class")).newInstance();
    Vector children = XMLDocument.getChildTags(node);
    String file = null;
    String dir = null;
    String prefix = null;
    
    Object relativeB = null;
    boolean relative = false;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("file")) {
        file = (String)invokeReadFromXML(child);
      } else if (name.equals("dir")) {
        dir = (String)invokeReadFromXML(child);
      } else if (name.equals("prefix")) {
        prefix = (String)invokeReadFromXML(child);
      } else if (name.equals("useRelativePath")) {
        relativeB = readFromXML(child);
        if ((relativeB instanceof Boolean)) {
          relative = ((Boolean)relativeB).booleanValue();
        }
      } else {
        readFromXML(result, name, child);
      }
    }
    
    if ((file != null) && (file.length() == 0)) {
      file = null;
    }
    

    if ((dir != null) && (prefix != null)) {
      ((AbstractFileSaver)result).setDir(dir);
      ((AbstractFileSaver)result).setFilePrefix(prefix);
    }
    
    if ((result instanceof FileSourcedConverter)) {
      ((FileSourcedConverter)result).setUseRelativePath(relative);
    }
    
    return result;
  }
  













  public Element writeBeanVisual(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    BeanVisual visual = (BeanVisual)o;
    Element node = writeToXML(parent, o, name);
    

    invokeWriteToXML(node, visual.getIconPath(), "iconPath");
    invokeWriteToXML(node, visual.getAnimatedIconPath(), "animatedIconPath");
    
    return node;
  }
  















  public Object readBeanVisual(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    Vector children = XMLDocument.getChildTags(node);
    String text = "";
    String iconPath = "";
    String animIconPath = "";
    

    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("text")) {
        text = (String)invokeReadFromXML(child);
      } else if (name.equals("iconPath")) {
        iconPath = (String)invokeReadFromXML(child);
      } else if (name.equals("animatedIconPath")) {
        animIconPath = (String)invokeReadFromXML(child);
      }
    }
    result = new BeanVisual(text, iconPath, animIconPath);
    

    for (i = 0; i < children.size(); i++) {
      readFromXML(result, node.getAttribute("name"), (Element)children.get(i));
    }
    return result;
  }
  












  protected Vector getIDsForBeanInstances(Vector beans)
  {
    Vector result = new Vector();
    
    for (int i = 0; i < beans.size(); i++) {
      int pos = m_BeanInstances.indexOf(beans.get(i));
      result.add(m_BeanInstancesID.get(pos));
    }
    
    return result;
  }
  













  public Element writeMetaBean(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    MetaBean meta = (MetaBean)o;
    Element node = writeToXML(parent, o, name);
    
    invokeWriteToXML(node, getIDsForBeanInstances(meta.getBeansInInputs()), "inputs_id");
    invokeWriteToXML(node, getIDsForBeanInstances(meta.getBeansInOutputs()), "outputs_id");
    
    return node;
  }
  











  protected Vector getBeanInstancesForIDs(Vector ids)
  {
    Vector result = new Vector();
    
    for (int i = 0; i < ids.size(); i++) {
      int pos = m_BeanInstancesID.indexOf(ids.get(i));
      result.add(m_BeanInstances.get(pos));
    }
    
    return result;
  }
  
















  public Object readMetaBean(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = new MetaBean();
    Vector children = XMLDocument.getChildTags(node);
    Vector inputs = new Vector();
    Vector outputs = new Vector();
    Vector coords = new Vector();
    

    m_CurrentMetaBean = ((MetaBean)result);
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("associatedConnections")) {
        ((MetaBean)result).setAssociatedConnections((Vector)invokeReadFromXML(child));
      } else if (name.equals("inputs_id")) {
        inputs = (Vector)invokeReadFromXML(child);
      } else if (name.equals("outputs_id")) {
        outputs = (Vector)invokeReadFromXML(child);
      } else if (name.equals("subFlow")) {
        ((MetaBean)result).setSubFlow((Vector)invokeReadFromXML(child));
      } else if (name.equals("originalCoords")) {
        coords = (Vector)invokeReadFromXML(child);
      } else if (name.equals("inputs")) {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadMetaBean_Input_Text_Front") + name + Messages.getString("XMLBeans_ReadMetaBean_Input_Text_End"));
      } else if (name.equals("outputs")) {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("XMLBeans_ReadMetaBean_Output_Text_Front") + name + Messages.getString("XMLBeans_ReadMetaBean_Output_Text_End"));
      } else {
        readFromXML(result, name, child);
      }
    }
    MetaBean bean = (MetaBean)result;
    

    bean.setInputs(getBeanInstancesForIDs(inputs));
    bean.setOutputs(getBeanInstancesForIDs(outputs));
    bean.setOriginalCoords(coords);
    
    return result;
  }
}
