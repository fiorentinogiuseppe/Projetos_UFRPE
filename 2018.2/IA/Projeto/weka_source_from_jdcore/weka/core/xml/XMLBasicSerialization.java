package weka.core.xml;

import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.DefaultListModel;
import org.w3c.dom.Element;
import weka.classifiers.CostMatrix;
import weka.core.RevisionUtils;
































































public class XMLBasicSerialization
  extends XMLSerialization
{
  public static final String VAL_MAPPING = "mapping";
  public static final String VAL_KEY = "key";
  public static final String VAL_VALUE = "value";
  public static final String VAL_CELLS = "cells";
  
  public XMLBasicSerialization()
    throws Exception
  {}
  
  public void clear()
    throws Exception
  {
    super.clear();
    

    m_CustomMethods.register(this, DefaultListModel.class, "DefaultListModel");
    m_CustomMethods.register(this, HashMap.class, "Map");
    m_CustomMethods.register(this, HashSet.class, "Collection");
    m_CustomMethods.register(this, Hashtable.class, "Map");
    m_CustomMethods.register(this, LinkedList.class, "Collection");
    m_CustomMethods.register(this, Properties.class, "Map");
    m_CustomMethods.register(this, Stack.class, "Collection");
    m_CustomMethods.register(this, TreeMap.class, "Map");
    m_CustomMethods.register(this, TreeSet.class, "Collection");
    m_CustomMethods.register(this, Vector.class, "Collection");
    

    m_CustomMethods.register(this, weka.core.matrix.Matrix.class, "Matrix");
    m_CustomMethods.register(this, weka.core.Matrix.class, "MatrixOld");
    m_CustomMethods.register(this, CostMatrix.class, "CostMatrixOld");
  }
  
















  public Element writeDefaultListModel(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    DefaultListModel model = (DefaultListModel)o;
    Element node = addElement(parent, name, o.getClass().getName(), false);
    
    for (int i = 0; i < model.getSize(); i++) {
      invokeWriteToXML(node, model.get(i), Integer.toString(i));
    }
    return node;
  }
  














  public Object readDefaultListModel(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Vector children = XMLDocument.getChildTags(node);
    DefaultListModel model = new DefaultListModel();
    

    int index = children.size() - 1;
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      int currIndex = Integer.parseInt(child.getAttribute("name"));
      if (currIndex > index)
        index = currIndex;
    }
    model.setSize(index + 1);
    

    for (i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      model.set(Integer.parseInt(child.getAttribute("name")), invokeReadFromXML(child));
    }
    


    return model;
  }
  
















  public Element writeCollection(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Iterator iter = ((Collection)o).iterator();
    Element node = addElement(parent, name, o.getClass().getName(), false);
    
    int i = 0;
    while (iter.hasNext()) {
      invokeWriteToXML(node, iter.next(), Integer.toString(i));
      i++;
    }
    
    return node;
  }
  















  public Object readCollection(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Vector children = XMLDocument.getChildTags(node);
    Vector v = new Vector();
    

    int index = children.size() - 1;
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      int currIndex = Integer.parseInt(child.getAttribute("name"));
      if (currIndex > index)
        index = currIndex;
    }
    v.setSize(index + 1);
    


    for (i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      v.set(Integer.parseInt(child.getAttribute("name")), invokeReadFromXML(child));
    }
    



    Collection coll = (Collection)Class.forName(node.getAttribute("class")).newInstance();
    
    coll.addAll(v);
    
    return coll;
  }
  


















  public Element writeMap(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    Map map = (Map)o;
    Iterator iter = map.keySet().iterator();
    Element node = addElement(parent, name, o.getClass().getName(), false);
    
    while (iter.hasNext()) {
      Object key = iter.next();
      Element child = addElement(node, "mapping", Object.class.getName(), false);
      
      invokeWriteToXML(child, key, "key");
      invokeWriteToXML(child, map.get(key), "value");
    }
    
    return node;
  }
  


















  public Object readMap(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    Map map = (Map)Class.forName(node.getAttribute("class")).newInstance();
    
    Vector children = XMLDocument.getChildTags(node);
    
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      Vector cchildren = XMLDocument.getChildTags(child);
      Object key = null;
      Object value = null;
      
      for (int n = 0; n < cchildren.size(); n++) {
        Element cchild = (Element)cchildren.get(n);
        String name = cchild.getAttribute("name");
        if (name.equals("key")) {
          key = invokeReadFromXML(cchild);
        } else if (name.equals("value")) {
          value = invokeReadFromXML(cchild);
        } else {
          System.out.println("WARNING: '" + name + "' is not a recognized name for maps!");
        }
      }
      
      map.put(key, value);
    }
    
    return map;
  }
  















  public Element writeMatrix(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.core.matrix.Matrix matrix = (weka.core.matrix.Matrix)o;
    Element node = addElement(parent, name, o.getClass().getName(), false);
    
    invokeWriteToXML(node, matrix.getArray(), "cells");
    
    return node;
  }
  














  public Object readMatrix(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    weka.core.matrix.Matrix matrix = null;
    Vector children = XMLDocument.getChildTags(node);
    for (int i = 0; i < children.size(); i++) {
      Element child = (Element)children.get(i);
      String name = child.getAttribute("name");
      
      if (name.equals("cells")) {
        Object o = invokeReadFromXML(child);
        matrix = new weka.core.matrix.Matrix((double[][])o);
      }
    }
    

    return matrix;
  }
  

















  public Element writeMatrixOld(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    weka.core.Matrix matrix = (weka.core.Matrix)o;
    Element node = addElement(parent, name, o.getClass().getName(), false);
    
    double[][] array = new double[matrix.numRows()][];
    for (int i = 0; i < array.length; i++)
      array[i] = matrix.getRow(i);
    invokeWriteToXML(node, array, "cells");
    
    return node;
  }
  










  public Object readMatrixOld(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    weka.core.matrix.Matrix matrixNew = (weka.core.matrix.Matrix)readMatrix(node);
    weka.core.Matrix matrix = new weka.core.Matrix(matrixNew.getArrayCopy());
    
    return matrix;
  }
  












  public Element writeCostMatrixOld(Element parent, Object o, String name)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), name);
    }
    m_CurrentNode = parent;
    
    return writeMatrixOld(parent, o, name);
  }
  











  public Object readCostMatrixOld(Element node)
    throws Exception
  {
    if (DEBUG) {
      trace(new Throwable(), node.getAttribute("name"));
    }
    m_CurrentNode = node;
    
    weka.core.matrix.Matrix matrixNew = (weka.core.matrix.Matrix)readMatrix(node);
    StringWriter writer = new StringWriter();
    matrixNew.write(writer);
    CostMatrix matrix = new CostMatrix(new StringReader(writer.toString()));
    
    return matrix;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
