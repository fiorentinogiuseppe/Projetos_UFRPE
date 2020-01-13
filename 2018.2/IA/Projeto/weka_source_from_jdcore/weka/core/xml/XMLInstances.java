package weka.core.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ProtectedProperties;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.Version;































public class XMLInstances
  extends XMLDocument
  implements Serializable
{
  private static final long serialVersionUID = 3626821327547416099L;
  public static String FILE_EXTENSION = ".xrff";
  


  public static final String TAG_DATASET = "dataset";
  


  public static final String TAG_HEADER = "header";
  


  public static final String TAG_BODY = "body";
  

  public static final String TAG_NOTES = "notes";
  

  public static final String TAG_ATTRIBUTES = "attributes";
  

  public static final String TAG_ATTRIBUTE = "attribute";
  

  public static final String TAG_LABELS = "labels";
  

  public static final String TAG_LABEL = "label";
  

  public static final String TAG_METADATA = "metadata";
  

  public static final String TAG_PROPERTY = "property";
  

  public static final String TAG_INSTANCES = "instances";
  

  public static final String TAG_INSTANCE = "instance";
  

  public static final String TAG_VALUE = "value";
  

  public static final String ATT_VERSION = "version";
  

  public static final String ATT_TYPE = "type";
  

  public static final String ATT_FORMAT = "format";
  

  public static final String ATT_CLASS = "class";
  

  public static final String ATT_INDEX = "index";
  

  public static final String ATT_WEIGHT = "weight";
  

  public static final String ATT_MISSING = "missing";
  

  public static final String VAL_NUMERIC = "numeric";
  

  public static final String VAL_DATE = "date";
  

  public static final String VAL_NOMINAL = "nominal";
  

  public static final String VAL_STRING = "string";
  

  public static final String VAL_RELATIONAL = "relational";
  

  public static final String VAL_NORMAL = "normal";
  

  public static final String VAL_SPARSE = "sparse";
  

  public static final String DOCTYPE = "<!DOCTYPE dataset\n[\n   <!ELEMENT dataset (header,body)>\n   <!ATTLIST dataset name CDATA #REQUIRED>\n   <!ATTLIST dataset version CDATA \"" + Version.VERSION + "\">\n" + "\n" + "   <!" + "ELEMENT" + " " + "header" + " (" + "notes" + "?" + "," + "attributes" + ")" + ">\n" + "   <!" + "ELEMENT" + " " + "body" + " (" + "instances" + ")" + ">\n" + "   <!" + "ELEMENT" + " " + "notes" + " " + "ANY" + ">   <!--  comments, information, copyright, etc. -->\n" + "\n" + "   <!" + "ELEMENT" + " " + "attributes" + " (" + "attribute" + "+" + ")" + ">\n" + "   <!" + "ELEMENT" + " " + "attribute" + " (" + "labels" + "?" + "," + "metadata" + "?" + "," + "attributes" + "?" + ")" + ">\n" + "   <!" + "ATTLIST" + " " + "attribute" + " " + "name" + " " + "CDATA" + " " + "#REQUIRED" + ">\n" + "   <!" + "ATTLIST" + " " + "attribute" + " " + "type" + " (" + "numeric" + "|" + "date" + "|" + "nominal" + "|" + "string" + "|" + "relational" + ") " + "#REQUIRED" + ">\n" + "   <!" + "ATTLIST" + " " + "attribute" + " " + "format" + " " + "CDATA" + " " + "#IMPLIED" + ">\n" + "   <!" + "ATTLIST" + " " + "attribute" + " " + "class" + " (" + "yes" + "|" + "no" + ") \"" + "no" + "\"" + ">\n" + "   <!" + "ELEMENT" + " " + "labels" + " (" + "label" + "*" + ")" + ">   <!-- only for type \"nominal\" -->\n" + "   <!" + "ELEMENT" + " " + "label" + " " + "ANY" + ">\n" + "   <!" + "ELEMENT" + " " + "metadata" + " (" + "property" + "*" + ")" + ">\n" + "   <!" + "ELEMENT" + " " + "property" + " " + "ANY" + ">\n" + "   <!" + "ATTLIST" + " " + "property" + " " + "name" + " " + "CDATA" + " " + "#REQUIRED" + ">\n" + "\n" + "   <!" + "ELEMENT" + " " + "instances" + " (" + "instance" + "*" + ")" + ">\n" + "   <!" + "ELEMENT" + " " + "instance" + " (" + "value" + "*" + ")" + ">\n" + "   <!" + "ATTLIST" + " " + "instance" + " " + "type" + " (" + "normal" + "|" + "sparse" + ") \"" + "normal" + "\"" + ">\n" + "   <!" + "ATTLIST" + " " + "instance" + " " + "weight" + " " + "CDATA" + " " + "#IMPLIED" + ">\n" + "   <!" + "ELEMENT" + " " + "value" + " (" + "#PCDATA" + "|" + "instances" + ")" + "*" + ">\n" + "   <!" + "ATTLIST" + " " + "value" + " " + "index" + " " + "CDATA" + " " + "#IMPLIED" + ">   <!-- 1-based index (only used for instance format \"sparse\") -->\n" + "   <!" + "ATTLIST" + " " + "value" + " " + "missing" + " (" + "yes" + "|" + "no" + ") \"" + "no" + "\"" + ">\n" + "]\n" + ">";
  
































  protected int m_Precision = 6;
  



  protected Instances m_Instances;
  



  public XMLInstances()
    throws Exception
  {
    m_Instances = null;
    
    setDocType(DOCTYPE);
    setRootNode("dataset");
    setValidating(true);
  }
  




  public XMLInstances(Instances data)
    throws Exception
  {
    this();
    
    setInstances(data);
  }
  





  public XMLInstances(Reader reader)
    throws Exception
  {
    this();
    
    setXML(reader);
  }
  













  protected void addAttribute(Element parent, Attribute att)
  {
    Element node = m_Document.createElement("attribute");
    parent.appendChild(node);
    


    node.setAttribute("name", validContent(att.name()));
    

    switch (att.type()) {
    case 0: 
      node.setAttribute("type", "numeric");
      break;
    
    case 3: 
      node.setAttribute("type", "date");
      break;
    
    case 1: 
      node.setAttribute("type", "nominal");
      break;
    
    case 2: 
      node.setAttribute("type", "string");
      break;
    
    case 4: 
      node.setAttribute("type", "relational");
      break;
    
    default: 
      node.setAttribute("type", "???");
    }
    
    
    if (att.isNominal()) {
      Element child = m_Document.createElement("labels");
      node.appendChild(child);
      Enumeration enm = att.enumerateValues();
      while (enm.hasMoreElements()) {
        String tmpStr = enm.nextElement().toString();
        Element label = m_Document.createElement("label");
        child.appendChild(label);
        label.appendChild(m_Document.createTextNode(validContent(tmpStr)));
      }
    }
    

    if (att.isDate()) {
      node.setAttribute("format", validContent(att.getDateFormat()));
    }
    
    if ((m_Instances.classIndex() > -1) && 
      (att == m_Instances.classAttribute())) {
      node.setAttribute("class", "yes");
    }
    

    if ((att.getMetadata() != null) && (att.getMetadata().size() > 0)) {
      Element child = m_Document.createElement("metadata");
      node.appendChild(child);
      Enumeration enm = att.getMetadata().propertyNames();
      while (enm.hasMoreElements()) {
        String tmpStr = enm.nextElement().toString();
        Element property = m_Document.createElement("property");
        child.appendChild(property);
        property.setAttribute("name", validContent(tmpStr));
        property.appendChild(m_Document.createTextNode(validContent(att.getMetadata().getProperty(tmpStr, ""))));
      }
    }
    

    if (att.isRelationValued()) {
      Element child = m_Document.createElement("attributes");
      node.appendChild(child);
      for (int i = 0; i < att.relation().numAttributes(); i++) {
        addAttribute(child, att.relation().attribute(i));
      }
    }
  }
  







  protected String validContent(String content)
  {
    String result = content;
    


    result = result.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    




    result = result.replaceAll("\n", "&#10;").replaceAll("\r", "&#13;").replaceAll("\t", "&#9;");
    


    return result;
  }
  













  protected void addInstance(Element parent, Instance inst)
  {
    Element node = m_Document.createElement("instance");
    parent.appendChild(node);
    

    boolean sparse = inst instanceof SparseInstance;
    if (sparse) {
      node.setAttribute("type", "sparse");
    }
    
    if (inst.weight() != 1.0D) {
      node.setAttribute("weight", Utils.doubleToString(inst.weight(), m_Precision));
    }
    
    for (int i = 0; i < inst.numValues(); i++) {
      int index = inst.index(i);
      
      Element value = m_Document.createElement("value");
      node.appendChild(value);
      
      if (inst.isMissing(index)) {
        value.setAttribute("missing", "yes");
      }
      else {
        if (inst.attribute(index).isRelationValued()) {
          Element child = m_Document.createElement("instances");
          value.appendChild(child);
          for (int n = 0; n < inst.relationalValue(i).numInstances(); n++) {
            addInstance(child, inst.relationalValue(i).instance(n));
          }
        }
        if (inst.attribute(index).type() == 0) {
          value.appendChild(m_Document.createTextNode(Utils.doubleToString(inst.value(index), m_Precision)));
        } else {
          value.appendChild(m_Document.createTextNode(validContent(inst.stringValue(index))));
        }
      }
      
      if (sparse) {
        value.setAttribute("index", "" + (index + 1));
      }
    }
  }
  






  protected void headerToXML()
  {
    Element root = m_Document.getDocumentElement();
    root.setAttribute("name", validContent(m_Instances.relationName()));
    root.setAttribute("version", Version.VERSION);
    

    Element node = m_Document.createElement("header");
    root.appendChild(node);
    

    Element child = m_Document.createElement("attributes");
    node.appendChild(child);
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      addAttribute(child, m_Instances.attribute(i));
    }
  }
  






  protected void dataToXML()
  {
    Element root = m_Document.getDocumentElement();
    

    Element node = m_Document.createElement("body");
    root.appendChild(node);
    

    Element child = m_Document.createElement("instances");
    node.appendChild(child);
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      addInstance(child, m_Instances.instance(i));
    }
  }
  



  public void setInstances(Instances data)
  {
    m_Instances = new Instances(data);
    clear();
    headerToXML();
    dataToXML();
  }
  





  public Instances getInstances()
  {
    return m_Instances;
  }
  













  protected ProtectedProperties createMetadata(Element parent)
    throws Exception
  {
    ProtectedProperties result = null;
    


    Element metanode = null;
    Vector list = getChildTags(parent, "metadata");
    if (list.size() > 0) {
      metanode = (Element)list.get(0);
    }
    
    if (metanode != null) {
      Properties props = new Properties();
      list = getChildTags(metanode, "property");
      for (int i = 0; i < list.size(); i++) {
        Element node = (Element)list.get(i);
        props.setProperty(node.getAttribute("name"), getContent(node));
      }
      result = new ProtectedProperties(props);
    }
    
    return result;
  }
  












  protected FastVector createLabels(Element parent)
    throws Exception
  {
    FastVector result = new FastVector();
    


    Element labelsnode = null;
    Vector list = getChildTags(parent, "labels");
    if (list.size() > 0) {
      labelsnode = (Element)list.get(0);
    }
    
    if (labelsnode != null) {
      list = getChildTags(labelsnode, "label");
      for (int i = 0; i < list.size(); i++) {
        Element node = (Element)list.get(i);
        result.addElement(getContent(node));
      }
    }
    
    return result;
  }
  














  protected Attribute createAttribute(Element node)
    throws Exception
  {
    Attribute result = null;
    

    String name = node.getAttribute("name");
    

    String typeStr = node.getAttribute("type");
    int type; if (typeStr.equals("numeric")) {
      type = 0; } else { int type;
      if (typeStr.equals("date")) {
        type = 3; } else { int type;
        if (typeStr.equals("nominal")) {
          type = 1; } else { int type;
          if (typeStr.equals("string")) {
            type = 2; } else { int type;
            if (typeStr.equals("relational")) {
              type = 4;
            } else
              throw new Exception("Attribute type '" + typeStr + "' is not supported!");
          }
        } } }
    int type;
    ProtectedProperties metadata = createMetadata(node);
    
    switch (type) {
    case 0: 
      if (metadata == null) {
        result = new Attribute(name);
      } else
        result = new Attribute(name, metadata);
      break;
    
    case 3: 
      if (metadata == null) {
        result = new Attribute(name, node.getAttribute("format"));
      } else
        result = new Attribute(name, node.getAttribute("format"), metadata);
      break;
    
    case 1: 
      FastVector values = createLabels(node);
      if (metadata == null) {
        result = new Attribute(name, values);
      } else
        result = new Attribute(name, values, metadata);
      break;
    
    case 2: 
      if (metadata == null) {
        result = new Attribute(name, (FastVector)null);
      } else
        result = new Attribute(name, (FastVector)null, metadata);
      break;
    
    case 4: 
      Vector list = getChildTags(node, "attributes");
      node = (Element)list.get(0);
      FastVector atts = createAttributes(node, new int[1]);
      if (metadata == null) {
        result = new Attribute(name, new Instances(name, atts, 0));
      } else {
        result = new Attribute(name, new Instances(name, atts, 0), metadata);
      }
      break;
    }
    return result;
  }
  












  protected FastVector createAttributes(Element parent, int[] classIndex)
    throws Exception
  {
    FastVector result = new FastVector();
    classIndex[0] = -1;
    
    Vector list = getChildTags(parent, "attribute");
    for (int i = 0; i < list.size(); i++) {
      Element node = (Element)list.get(i);
      Attribute att = createAttribute(node);
      if (node.getAttribute("class").equals("yes"))
        classIndex[0] = i;
      result.addElement(att);
    }
    
    return result;
  }
  



















  protected Instance createInstance(Instances header, Element parent)
    throws Exception
  {
    Instance result = null;
    

    boolean sparse = parent.getAttribute("type").equals("sparse");
    double[] values = new double[header.numAttributes()];
    double weight;
    double weight;
    if (parent.getAttribute("weight").length() != 0) {
      weight = Double.parseDouble(parent.getAttribute("weight"));
    } else {
      weight = 1.0D;
    }
    Vector list = getChildTags(parent, "value");
    for (int i = 0; i < list.size(); i++) {
      Element node = (Element)list.get(i);
      int index;
      int index;
      if (sparse) {
        index = Integer.parseInt(node.getAttribute("index")) - 1;
      } else {
        index = i;
      }
      
      if (node.getAttribute("missing").equals("yes")) {
        values[index] = Instance.missingValue();
      }
      else {
        String content = getContent(node);
        switch (header.attribute(index).type()) {
        case 0: 
          values[index] = Double.parseDouble(content);
          break;
        
        case 3: 
          values[index] = header.attribute(index).parseDate(content);
          break;
        
        case 1: 
          values[index] = header.attribute(index).indexOfValue(content);
          break;
        
        case 2: 
          values[index] = header.attribute(index).addStringValue(content);
          break;
        
        case 4: 
          Vector subList = getChildTags(node, "instances");
          Element child = (Element)subList.get(0);
          Instances data = createInstances(header.attribute(index).relation(), child);
          values[index] = header.attribute(index).addRelation(data);
          break;
        
        default: 
          throw new Exception("Attribute type " + header.attribute(index).type() + " is not supported!");
        }
        
      }
    }
    


    if (sparse) {
      result = new SparseInstance(weight, values);
    } else {
      result = new Instance(weight, values);
    }
    return result;
  }
  










  protected Instances createInstances(Instances header, Element parent)
    throws Exception
  {
    Instances result = new Instances(header, 0);
    
    Vector list = getChildTags(parent, "instance");
    for (int i = 0; i < list.size(); i++) {
      result.add(createInstance(result, (Element)list.get(i)));
    }
    return result;
  }
  












  protected Instances headerFromXML()
    throws Exception
  {
    Element root = m_Document.getDocumentElement();
    

    Version version = new Version();
    if (version.isOlder(root.getAttribute("version"))) {
      System.out.println("WARNING: loading data of version " + root.getAttribute("version") + " with version " + Version.VERSION);
    }
    


    Vector list = getChildTags(root, "header");
    Element node = (Element)list.get(0);
    list = getChildTags(node, "attributes");
    node = (Element)list.get(0);
    int[] classIndex = new int[1];
    FastVector atts = createAttributes(node, classIndex);
    

    Instances result = new Instances(root.getAttribute("name"), atts, 0);
    result.setClassIndex(classIndex[0]);
    
    return result;
  }
  









  protected Instances dataFromXML(Instances header)
    throws Exception
  {
    Vector list = getChildTags(m_Document.getDocumentElement(), "body");
    Element node = (Element)list.get(0);
    list = getChildTags(node, "instances");
    node = (Element)list.get(0);
    Instances result = createInstances(header, node);
    
    return result;
  }
  




  public void setXML(Reader reader)
    throws Exception
  {
    read(reader);
    

    m_Instances = dataFromXML(headerFromXML());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    try
    {
      Reader r = null;
      if (args.length != 1) {
        throw new Exception("Usage: XMLInstances <filename>");
      }
      
      InputStream in = new FileInputStream(args[0]);
      
      if (args[0].endsWith(".gz"))
        in = new GZIPInputStream(in);
      r = new BufferedReader(new InputStreamReader(in));
      

      if (args[0].endsWith(".arff")) {
        XMLInstances i = new XMLInstances(new Instances(r));
        System.out.println(i.toString());
      }
      else {
        Instances i = new XMLInstances(r).getInstances();
        System.out.println(i.toSummaryString());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
