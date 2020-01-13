package weka.core.xml;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.Version;


































































public class XMLSerialization
  implements RevisionHandler
{
  protected static boolean DEBUG = false;
  



  protected Element m_CurrentNode = null;
  


  public static final String TAG_OBJECT = "object";
  


  public static final String ATT_VERSION = "version";
  


  public static final String ATT_NAME = "name";
  

  public static final String ATT_CLASS = "class";
  

  public static final String ATT_PRIMITIVE = "primitive";
  

  public static final String ATT_ARRAY = "array";
  

  public static final String ATT_NULL = "null";
  

  public static final String VAL_YES = "yes";
  

  public static final String VAL_NO = "no";
  

  public static final String VAL_ROOT = "__root__";
  

  public static final String ROOT_NODE = "object";
  

  public static final String ATT_PRIMITIVE_DEFAULT = "no";
  

  public static final String ATT_ARRAY_DEFAULT = "no";
  

  public static final String ATT_NULL_DEFAULT = "no";
  

  public static final String DOCTYPE = "<!DOCTYPE object\n[\n   <!ELEMENT object (#PCDATA|object)*>\n   <!ATTLIST object name      CDATA #REQUIRED>\n   <!ATTLIST object class     CDATA #REQUIRED>\n   <!ATTLIST object primitive CDATA \"no\">\n   <!ATTLIST object array     CDATA \"no\">   <!-- the dimensions of the array; no=0, yes=1 -->\n   <!ATTLIST object null      CDATA \"no\">\n   <!ATTLIST object version   CDATA \"" + Version.VERSION + "\">\n" + "]\n" + ">";
  












  protected XMLDocument m_Document = null;
  

  protected PropertyHandler m_Properties = null;
  

  protected XMLSerializationMethodHandler m_CustomMethods = null;
  


  protected Hashtable m_ClassnameOverride = null;
  




  public XMLSerialization()
    throws Exception
  {
    clear();
  }
  






  protected void trace(Throwable t, String msg)
  {
    if ((DEBUG) && (t.getStackTrace().length > 0)) {
      System.out.println("trace: " + t.getStackTrace()[0] + ": " + msg);
    }
  }
  




  public void clear()
    throws Exception
  {
    m_Document = new XMLDocument();
    m_Document.setValidating(true);
    m_Document.newDocument(DOCTYPE, "object");
    
    m_Properties = new PropertyHandler();
    m_CustomMethods = new XMLSerializationMethodHandler(this);
    
    m_ClassnameOverride = new Hashtable();
    



    m_ClassnameOverride.put(File.class, File.class.getName());
    
    setVersion(Version.VERSION);
    
    m_CurrentNode = null;
  }
  






  private void setVersion(String version)
  {
    Document doc = m_Document.getDocument();
    doc.getDocumentElement().setAttribute("version", version);
  }
  








  public String getVersion()
  {
    Document doc = m_Document.getDocument();
    String result = doc.getDocumentElement().getAttribute("version");
    
    return result;
  }
  






  private void checkVersion()
  {
    Version version = new Version();
    String versionStr = getVersion();
    if (versionStr.equals("")) {
      System.out.println("WARNING: has no version!");
    } else if (version.isOlder(versionStr)) {
      System.out.println("WARNING: loading a newer version (" + versionStr + " > " + Version.VERSION + ")!");
    } else if (version.isNewer(versionStr)) {
      System.out.println("NOTE: loading an older version (" + versionStr + " < " + Version.VERSION + ")!");
    }
  }
  











  protected Hashtable getDescriptors(Object o)
    throws Exception
  {
    Hashtable result = new Hashtable();
    
    BeanInfo info = Introspector.getBeanInfo(o.getClass());
    PropertyDescriptor[] desc = info.getPropertyDescriptors();
    for (int i = 0; i < desc.length; i++)
    {
      if ((desc[i].getReadMethod() != null) && (desc[i].getWriteMethod() != null))
      {
        if (!m_Properties.isIgnored(desc[i].getDisplayName()))
        {


          if (!m_Properties.isIgnored(o, desc[i].getDisplayName()))
          {


            if (m_Properties.isAllowed(o, desc[i].getDisplayName()))
            {

              result.put(desc[i].getDisplayName(), desc[i]); } }
        }
      }
    }
    return result;
  }
  








  protected String getPath(Element node)
  {
    String result = node.getAttribute("name");
    
    while (node.getParentNode() != node.getOwnerDocument()) {
      node = (Element)node.getParentNode();
      result = node.getAttribute("name") + "." + result;
    }
    
    return result;
  }
  






  protected String booleanToString(boolean b)
  {
    if (b) {
      return "yes";
    }
    return "no";
  }
  







  protected boolean stringToBoolean(String s)
  {
    if (s.equals(""))
      return false;
    if (s.equals("yes"))
      return true;
    if (s.equalsIgnoreCase("true"))
      return true;
    if (s.replaceAll("[0-9]*", "").equals("")) {
      return Integer.parseInt(s) != 0;
    }
    return false;
  }
  









  protected Element addElement(Element parent, String name, String classname, boolean primitive)
  {
    return addElement(parent, name, classname, primitive, 0);
  }
  










  protected Element addElement(Element parent, String name, String classname, boolean primitive, int array)
  {
    return addElement(parent, name, classname, primitive, array, false);
  }
  




  protected Element addElement(Element parent, String name, String classname, boolean primitive, int array, boolean isnull)
  {
    Element result;
    



    Element result;
    


    if (parent == null) {
      result = m_Document.getDocument().getDocumentElement();
    } else {
      result = (Element)parent.appendChild(m_Document.getDocument().createElement("object"));
    }
    

    result.setAttribute("name", name);
    result.setAttribute("class", classname);
    

    if (!booleanToString(primitive).equals("no")) {
      result.setAttribute("primitive", booleanToString(primitive));
    }
    
    if (array > 1) {
      result.setAttribute("array", Integer.toString(array));


    }
    else if (!booleanToString(array == 1).equals("no")) {
      result.setAttribute("array", booleanToString(array == 1));
    }
    
    if (!booleanToString(isnull).equals("no")) {
      result.setAttribute("null", booleanToString(isnull));
    }
    return result;
  }
  













  protected String overrideClassname(Object o)
  {
    String result = o.getClass().getName();
    

    Enumeration enm = m_ClassnameOverride.keys();
    while (enm.hasMoreElements()) {
      Class currentCls = (Class)enm.nextElement();
      if (currentCls.isInstance(o)) {
        result = (String)m_ClassnameOverride.get(currentCls);
      }
    }
    

    return result;
  }
  

















  protected String overrideClassname(String classname)
  {
    String result = classname;
    

    Enumeration enm = m_ClassnameOverride.keys();
    while (enm.hasMoreElements()) {
      Class currentCls = (Class)enm.nextElement();
      if (currentCls.getName().equals(classname)) {
        result = (String)m_ClassnameOverride.get(currentCls);
      }
    }
    

    return result;
  }
  








  protected PropertyDescriptor determineDescriptor(String className, String displayName)
  {
    PropertyDescriptor result = null;
    try
    {
      result = new PropertyDescriptor(displayName, Class.forName(className));
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  









  protected Element writeBooleanToXML(Element parent, boolean o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Boolean.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Boolean(o).toString()));
    
    return node;
  }
  









  protected Element writeByteToXML(Element parent, byte o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Byte.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Byte(o).toString()));
    
    return node;
  }
  









  protected Element writeCharToXML(Element parent, char o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Character.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Character(o).toString()));
    
    return node;
  }
  









  protected Element writeDoubleToXML(Element parent, double o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Double.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Double(o).toString()));
    
    return node;
  }
  









  protected Element writeFloatToXML(Element parent, float o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Float.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Float(o).toString()));
    
    return node;
  }
  









  protected Element writeIntToXML(Element parent, int o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Integer.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Integer(o).toString()));
    
    return node;
  }
  









  protected Element writeLongToXML(Element parent, long o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Long.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Long(o).toString()));
    
    return node;
  }
  









  protected Element writeShortToXML(Element parent, short o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Element node = addElement(parent, name, Short.TYPE.getName(), true);
    node.appendChild(node.getOwnerDocument().createTextNode(new Short(o).toString()));
    
    return node;
  }
  





  protected boolean isPrimitiveArray(Class c)
  {
    if (c.getComponentType().isArray()) {
      return isPrimitiveArray(c.getComponentType());
    }
    return c.getComponentType().isPrimitive();
  }
  






























  public Element writeToXML(Element parent, Object o, String name)
    throws Exception
  {
    Element node = null;
    

    if (DEBUG) {
      trace(new Throwable(), name);
    }
    
    if (o == null) {
      node = addElement(parent, name, "" + null, false, 0, true);
      return node;
    }
    

    Object obj = null;
    

    int array = 0;
    if (o.getClass().isArray())
      array = Utils.getArrayDimensions(o);
    boolean primitive; boolean primitive; String classname; if (array > 0) {
      String classname = Utils.getArrayClass(o.getClass()).getName();
      primitive = isPrimitiveArray(o.getClass());

    }
    else
    {
      PropertyDescriptor desc = null;
      if (parent != null)
        desc = determineDescriptor(parent.getAttribute("class"), name);
      boolean primitive;
      if (desc != null) {
        primitive = desc.getPropertyType().isPrimitive();
      } else {
        primitive = o.getClass().isPrimitive();
      }
      
      String classname;
      
      if (primitive) {
        classname = desc.getPropertyType().getName();
      }
      else {
        obj = o;
        classname = o.getClass().getName();
      }
    }
    


    if ((parent != null) && (!parent.getAttribute("array").equals("")) && (!parent.getAttribute("array").equals("no")) && (stringToBoolean(parent.getAttribute("primitive"))))
    {


      primitive = true;
      classname = parent.getAttribute("class");
      obj = null;
    }
    

    if (obj != null) {
      classname = overrideClassname(obj);
    } else {
      classname = overrideClassname(classname);
    }
    
    node = addElement(parent, name, classname, primitive, array);
    

    if (array > 0) {
      for (int i = 0; i < Array.getLength(o); i++) {
        invokeWriteToXML(node, Array.get(o, i), Integer.toString(i));
      }
    }
    


    if (primitive) {
      node.appendChild(node.getOwnerDocument().createTextNode(o.toString()));

    }
    else
    {
      Hashtable memberlist = getDescriptors(o);
      
      if (memberlist.size() == 0) {
        if (!o.toString().equals("")) {
          String tmpStr = o.toString();
          

          tmpStr = tmpStr.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
          




          tmpStr = tmpStr.replaceAll("\n", "&#10;").replaceAll("\r", "&#13;").replaceAll("\t", "&#9;");
          

          if ((o instanceof File))
          {
            tmpStr = tmpStr.replace('\\', '/');
          }
          
          node.appendChild(node.getOwnerDocument().createTextNode(tmpStr));
        }
      }
      else {
        Enumeration enm = memberlist.keys();
        while (enm.hasMoreElements()) {
          String memberName = enm.nextElement().toString();
          

          if ((!m_Properties.isIgnored(memberName)) && (!m_Properties.isIgnored(getPath(node) + "." + memberName)) && (!m_Properties.isIgnored(o, getPath(node) + "." + memberName)) && 
          




            (m_Properties.isAllowed(o, memberName)))
          {

            PropertyDescriptor desc = (PropertyDescriptor)memberlist.get(memberName);
            Method method = desc.getReadMethod();
            Object member = method.invoke(o, (Object[])null);
            invokeWriteToXML(node, member, memberName);
          }
        }
      }
    }
    
    return node;
  }
  















  protected Element invokeWriteToXML(Element parent, Object o, String name)
    throws Exception
  {
    Element node = null;
    Method method = null;
    boolean useDefault = false;
    
    m_CurrentNode = parent;
    

    if (o == null) {
      useDefault = true;
    }
    try {
      if (!useDefault) {
        boolean array = o.getClass().isArray();
        

        if (m_CustomMethods.write().contains(name)) {
          method = m_CustomMethods.write().get(o.getClass());

        }
        else if ((!array) && (m_CustomMethods.write().contains(o.getClass()))) {
          method = m_CustomMethods.write().get(o.getClass());
        } else {
          method = null;
        }
        useDefault = method == null;
      }
      

      if (!useDefault) {
        Class[] methodClasses = new Class[3];
        methodClasses[0] = Element.class;
        methodClasses[1] = Object.class;
        methodClasses[2] = String.class;
        Object[] methodArgs = new Object[3];
        methodArgs[0] = parent;
        methodArgs[1] = o;
        methodArgs[2] = name;
        node = (Element)method.invoke(this, methodArgs);
      }
      else
      {
        node = writeToXML(parent, o, name);
      }
    }
    catch (Exception e) {
      if (DEBUG) {
        e.printStackTrace();
      }
      if (m_CurrentNode != null) {
        System.out.println("Happened near: " + getPath(m_CurrentNode));
        
        m_CurrentNode = null;
      }
      System.out.println("PROBLEM (write): " + name);
      
      throw ((Exception)e.fillInStackTrace());
    }
    
    return node;
  }
  






  protected Object writePreProcess(Object o)
    throws Exception
  {
    return o;
  }
  






  protected void writePostProcess(Object o)
    throws Exception
  {}
  






  public XMLDocument toXML(Object o)
    throws Exception
  {
    clear();
    invokeWriteToXML(null, writePreProcess(o), "__root__");
    writePostProcess(o);
    return m_Document;
  }
  










  protected PropertyDescriptor getDescriptorByName(Object o, String name)
    throws Exception
  {
    PropertyDescriptor result = null;
    
    PropertyDescriptor[] desc = Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors();
    for (int i = 0; i < desc.length; i++) {
      if (desc[i].getDisplayName().equals(name)) {
        result = desc[i];
        break;
      }
    }
    
    return result;
  }
  


  protected Class determineClass(String name)
    throws Exception
  {
    Class result;
    

    Class result;
    
    if (name.equals(Boolean.TYPE.getName())) {
      result = Boolean.TYPE;
    } else { Class result;
      if (name.equals(Byte.TYPE.getName())) {
        result = Byte.TYPE;
      } else { Class result;
        if (name.equals(Character.TYPE.getName())) {
          result = Character.TYPE;
        } else { Class result;
          if (name.equals(Double.TYPE.getName())) {
            result = Double.TYPE;
          } else { Class result;
            if (name.equals(Float.TYPE.getName())) {
              result = Float.TYPE;
            } else { Class result;
              if (name.equals(Integer.TYPE.getName())) {
                result = Integer.TYPE;
              } else { Class result;
                if (name.equals(Long.TYPE.getName())) {
                  result = Long.TYPE;
                } else { Class result;
                  if (name.equals(Short.TYPE.getName())) {
                    result = Short.TYPE;
                  } else
                    result = Class.forName(name);
                } } } } } } }
    return result;
  }
  














  protected Object getPrimitive(Element node)
    throws Exception
  {
    Class cls = determineClass(node.getAttribute("class"));
    Object tmpResult = Array.newInstance(cls, 1);
    
    if (cls == Boolean.TYPE) {
      Array.set(tmpResult, 0, new Boolean(XMLDocument.getContent(node)));
    }
    else if (cls == Byte.TYPE) {
      Array.set(tmpResult, 0, new Byte(XMLDocument.getContent(node)));
    }
    else if (cls == Character.TYPE) {
      Array.set(tmpResult, 0, new Character(XMLDocument.getContent(node).charAt(0)));
    }
    else if (cls == Double.TYPE) {
      Array.set(tmpResult, 0, new Double(XMLDocument.getContent(node)));
    }
    else if (cls == Float.TYPE) {
      Array.set(tmpResult, 0, new Float(XMLDocument.getContent(node)));
    }
    else if (cls == Integer.TYPE) {
      Array.set(tmpResult, 0, new Integer(XMLDocument.getContent(node)));
    }
    else if (cls == Long.TYPE) {
      Array.set(tmpResult, 0, new Long(XMLDocument.getContent(node)));
    }
    else if (cls == Short.TYPE) {
      Array.set(tmpResult, 0, new Short(XMLDocument.getContent(node)));
    } else {
      throw new Exception("Cannot get primitive for class '" + cls.getName() + "'!");
    }
    Object result = Array.get(tmpResult, 0);
    
    return result;
  }
  






  public boolean readBooleanFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Boolean)getPrimitive(node)).booleanValue();
  }
  






  public byte readByteFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Byte)getPrimitive(node)).byteValue();
  }
  






  public char readCharFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Character)getPrimitive(node)).charValue();
  }
  






  public double readDoubleFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Double)getPrimitive(node)).doubleValue();
  }
  






  public float readFloatFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Float)getPrimitive(node)).floatValue();
  }
  






  public int readIntFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Integer)getPrimitive(node)).intValue();
  }
  






  public long readLongFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Long)getPrimitive(node)).longValue();
  }
  






  public short readShortFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    return ((Short)getPrimitive(node)).shortValue();
  }
  

















  public Object readFromXML(Object o, String name, Element child)
    throws Exception
  {
    Object result = o;
    Hashtable descriptors = getDescriptors(result);
    String methodName = child.getAttribute("name");
    

    if (m_Properties.isIgnored(getPath(child))) {
      return result;
    }
    
    if (m_Properties.isIgnored(result, getPath(child))) {
      return result;
    }
    
    if (!m_Properties.isAllowed(result, methodName)) {
      return result;
    }
    PropertyDescriptor descriptor = (PropertyDescriptor)descriptors.get(methodName);
    

    if (descriptor == null) {
      if (!m_CustomMethods.read().contains(methodName))
        System.out.println("WARNING: unknown property '" + name + "." + methodName + "'!");
      return result;
    }
    
    Method method = descriptor.getWriteMethod();
    Object[] methodArgs = new Object[1];
    Object tmpResult = invokeReadFromXML(child);
    Class paramClass = method.getParameterTypes()[0];
    

    if (paramClass.isArray())
    {
      if (Array.getLength(tmpResult) == 0)
        return result;
      methodArgs[0] = ((Object[])(Object[])tmpResult);
    }
    else
    {
      methodArgs[0] = tmpResult;
    }
    
    method.invoke(result, methodArgs);
    
    return result;
  }
  



  protected int[] getArrayDimensions(Element node)
  {
    Vector children;
    


    Vector children;
    


    if (stringToBoolean(node.getAttribute("array"))) {
      children = XMLDocument.getChildTags(node);
    } else {
      children = null;
    }
    if (children != null) {
      Vector tmpVector = new Vector();
      
      if (children.size() > 0)
      {
        int[] tmp = getArrayDimensions((Element)children.get(0));
        

        if (tmp != null) {
          for (int i = tmp.length - 1; i >= 0; i--) {
            tmpVector.add(new Integer(tmp[i]));
          }
        }
        
        tmpVector.add(0, new Integer(children.size()));
      }
      else {
        tmpVector.add(new Integer(0));
      }
      

      int[] result = new int[tmpVector.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = ((Integer)tmpVector.get(tmpVector.size() - i - 1)).intValue();
      }
    }
    int[] result = null;
    

    return result;
  }
  





















  public Object readFromXML(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    
    String name = node.getAttribute("name");
    String classname = node.getAttribute("class");
    boolean primitive = stringToBoolean(node.getAttribute("primitive"));
    boolean array = stringToBoolean(node.getAttribute("array"));
    boolean isnull = stringToBoolean(node.getAttribute("null"));
    

    if (isnull) {
      return result;
    }
    Vector children = XMLDocument.getChildTags(node);
    Class cls = determineClass(classname);
    

    if (array) {
      result = Array.newInstance(cls, getArrayDimensions(node));
      for (int i = 0; i < children.size(); i++) {
        Element child = (Element)children.get(i);
        Array.set(result, Integer.parseInt(child.getAttribute("name")), invokeReadFromXML(child));
      }
    }
    


    if (children.size() == 0)
    {
      if (primitive) {
        result = getPrimitive(node);
      }
      else
      {
        Class[] methodClasses = new Class[1];
        methodClasses[0] = String.class;
        Object[] methodArgs = new Object[1];
        methodArgs[0] = XMLDocument.getContent(node);
        try {
          Constructor constructor = cls.getConstructor(methodClasses);
          result = constructor.newInstance(methodArgs);
        }
        catch (Exception e)
        {
          try {
            result = cls.newInstance();
          }
          catch (Exception e2)
          {
            result = null;
            System.out.println("ERROR: Can't instantiate '" + classname + "'!");
          }
        }
      }
    }
    else
    {
      result = cls.newInstance();
      for (int i = 0; i < children.size(); i++) {
        result = readFromXML(result, name, (Element)children.get(i));
      }
    }
    
    return result;
  }
  












  protected Object invokeReadFromXML(Element node)
    throws Exception
  {
    boolean useDefault = false;
    Method method = null;
    m_CurrentNode = node;
    
    try
    {
      if (stringToBoolean(node.getAttribute("null"))) {
        useDefault = true;
      }
      if (!useDefault) {
        boolean array = stringToBoolean(node.getAttribute("array"));
        

        if (m_CustomMethods.read().contains(node.getAttribute("name"))) {
          method = m_CustomMethods.read().get(node.getAttribute("name"));

        }
        else if ((!array) && (m_CustomMethods.read().contains(determineClass(node.getAttribute("class"))))) {
          method = m_CustomMethods.read().get(determineClass(node.getAttribute("class")));
        } else {
          method = null;
        }
        useDefault = method == null;
      }
      

      if (!useDefault) {
        Class[] methodClasses = new Class[1];
        methodClasses[0] = Element.class;
        Object[] methodArgs = new Object[1];
        methodArgs[0] = node;
        return method.invoke(this, methodArgs);
      }
      

      return readFromXML(node);
    }
    catch (Exception e)
    {
      if (DEBUG) {
        e.printStackTrace();
      }
      if (m_CurrentNode != null) {
        System.out.println("Happened near: " + getPath(m_CurrentNode));
        
        m_CurrentNode = null;
      }
      System.out.println("PROBLEM (read): " + node.getAttribute("name"));
      
      throw ((Exception)e.fillInStackTrace());
    }
  }
  







  protected Document readPreProcess(Document document)
    throws Exception
  {
    return document;
  }
  






  protected Object readPostProcess(Object o)
    throws Exception
  {
    return o;
  }
  





  public Object fromXML(Document document)
    throws Exception
  {
    if (!document.getDocumentElement().getNodeName().equals("object"))
      throw new Exception("Expected 'object' as root element, but found '" + document.getDocumentElement().getNodeName() + "'!");
    m_Document.setDocument(readPreProcess(document));
    checkVersion();
    return readPostProcess(invokeReadFromXML(m_Document.getDocument().getDocumentElement()));
  }
  






  public Object read(String xml)
    throws Exception
  {
    return fromXML(m_Document.read(xml));
  }
  





  public Object read(File file)
    throws Exception
  {
    return fromXML(m_Document.read(file));
  }
  





  public Object read(InputStream stream)
    throws Exception
  {
    return fromXML(m_Document.read(stream));
  }
  





  public Object read(Reader reader)
    throws Exception
  {
    return fromXML(m_Document.read(reader));
  }
  






  public void write(String file, Object o)
    throws Exception
  {
    toXML(o).write(file);
  }
  





  public void write(File file, Object o)
    throws Exception
  {
    toXML(o).write(file);
  }
  





  public void write(OutputStream stream, Object o)
    throws Exception
  {
    toXML(o).write(stream);
  }
  





  public void write(Writer writer, Object o)
    throws Exception
  {
    toXML(o).write(writer);
  }
  



  public static void main(String[] args)
    throws Exception
  {
    if (args.length > 0)
    {
      if (args[0].toLowerCase().endsWith(".xml")) {
        System.out.println(new XMLSerialization().read(args[0]).toString());

      }
      else
      {
        FileInputStream fi = new FileInputStream(args[0]);
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
        
        Object o = oi.readObject();
        oi.close();
        

        new XMLSerialization().write(new BufferedOutputStream(new FileOutputStream(args[0] + ".xml")), o);
        
        FileOutputStream fo = new FileOutputStream(args[0] + ".exp");
        ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
        
        oo.writeObject(o);
        oo.close();
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
}
