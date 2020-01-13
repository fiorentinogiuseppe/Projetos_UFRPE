package weka.experiment.xml;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import weka.classifiers.Classifier;
import weka.core.RevisionUtils;
import weka.core.xml.PropertyHandler;
import weka.core.xml.XMLBasicSerialization;
import weka.core.xml.XMLDocument;
import weka.core.xml.XMLSerializationMethodHandler;
import weka.experiment.Experiment;
import weka.experiment.PropertyNode;
import weka.experiment.ResultProducer;
import weka.experiment.SplitEvaluator;


















































public class XMLExperiment
  extends XMLBasicSerialization
{
  public static final String NAME_CLASSFIRST = "classFirst";
  public static final String NAME_PROPERTYNODE_VALUE = "value";
  public static final String NAME_PROPERTYNODE_PARENTCLASS = "parentClass";
  public static final String NAME_PROPERTYNODE_PROPERTY = "property";
  
  public XMLExperiment()
    throws Exception
  {}
  
  public void clear()
    throws Exception
  {
    super.clear();
    

    m_Properties.addIgnored("__root__.options");
    m_Properties.addIgnored(Experiment.class, "options");
    

    m_Properties.addAllowed(Classifier.class, "debug");
    m_Properties.addAllowed(Classifier.class, "options");
    
    m_Properties.addAllowed(SplitEvaluator.class, "options");
    
    m_Properties.addAllowed(ResultProducer.class, "options");
    

    m_CustomMethods.register(this, PropertyNode.class, "PropertyNode");
  }
  








  protected void writePostProcess(Object o)
    throws Exception
  {
    Experiment exp = (Experiment)o;
    

    Element node = addElement(m_Document.getDocument().getDocumentElement(), "classFirst", Boolean.class.getName(), false);
    node.appendChild(node.getOwnerDocument().createTextNode(new Boolean(false).toString()));
  }
  











  protected Object readPostProcess(Object o)
    throws Exception
  {
    Experiment exp = (Experiment)o;
    

    Vector children = XMLDocument.getChildTags(m_Document.getDocument().getDocumentElement());
    for (int i = 0; i < children.size(); i++) {
      Element node = (Element)children.get(i);
      if (node.getAttribute("name").equals("classFirst")) {
        exp.classFirst(new Boolean(XMLDocument.getContent(node)).booleanValue());
        break;
      }
    }
    
    return o;
  }
  














  public Element writePropertyNode(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    PropertyNode pnode = (PropertyNode)o;
    Element node = (Element)parent.appendChild(m_Document.getDocument().createElement("object"));
    node.setAttribute("name", name);
    node.setAttribute("class", pnode.getClass().getName());
    node.setAttribute("primitive", "no");
    node.setAttribute("array", "no");
    
    if (value != null)
      invokeWriteToXML(node, value, "value");
    if (parentClass != null)
      invokeWriteToXML(node, parentClass.getName(), "parentClass");
    if (property != null) {
      invokeWriteToXML(node, property.getDisplayName(), "property");
    }
    
    if ((value != null) && (property != null) && (property.getPropertyType().isPrimitive()))
    {

      Vector children = XMLDocument.getChildTags(node);
      for (int i = 0; i < children.size(); i++) {
        Element child = (Element)children.get(i);
        if (child.getAttribute("name").equals("value"))
        {
          child.setAttribute("class", property.getPropertyType().getName());
          child.setAttribute("primitive", "yes");
        }
      }
    }
    return node;
  }
  
















  public Object readPropertyNode(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Object result = null;
    
    Vector children = XMLDocument.getChildTags(node);
    Object value = null;
    String parentClass = null;
    String property = null;
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      
      if (child.getAttribute("name").equals("value")) {
        if (stringToBoolean(child.getAttribute("primitive"))) {
          value = getPrimitive(child);
        } else
          value = invokeReadFromXML(child);
      }
      if (child.getAttribute("name").equals("parentClass"))
        parentClass = XMLDocument.getContent(child);
      if (child.getAttribute("name").equals("property"))
        property = XMLDocument.getContent(child); }
    Class cls;
    Class cls;
    if (parentClass != null) {
      cls = Class.forName(parentClass);
    } else {
      cls = null;
    }
    if (cls != null) {
      result = new PropertyNode(value, new PropertyDescriptor(property, cls), cls);
    } else {
      result = new PropertyNode(value);
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  









  public static void main(String[] args)
    throws Exception
  {
    if (args.length > 0)
    {
      if (args[0].toLowerCase().endsWith(".xml")) {
        System.out.println(new XMLExperiment().read(args[0]).toString());

      }
      else
      {
        FileInputStream fi = new FileInputStream(args[0]);
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
        
        Object o = oi.readObject();
        oi.close();
        


        new XMLExperiment().write(new BufferedOutputStream(new FileOutputStream(args[0] + ".xml")), o);
        
        FileOutputStream fo = new FileOutputStream(args[0] + ".exp");
        ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
        
        oo.writeObject(o);
        oo.close();
      }
    }
  }
}
