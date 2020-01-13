package weka.core.xml;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;









































































public class XMLDocument
  implements RevisionHandler
{
  public static final String PI = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
  public static final String DTD_DOCTYPE = "DOCTYPE";
  public static final String DTD_ELEMENT = "ELEMENT";
  public static final String DTD_ATTLIST = "ATTLIST";
  public static final String DTD_OPTIONAL = "?";
  public static final String DTD_AT_LEAST_ONE = "+";
  public static final String DTD_ZERO_OR_MORE = "*";
  public static final String DTD_SEPARATOR = "|";
  public static final String DTD_CDATA = "CDATA";
  public static final String DTD_ANY = "ANY";
  public static final String DTD_PCDATA = "#PCDATA";
  public static final String DTD_IMPLIED = "#IMPLIED";
  public static final String DTD_REQUIRED = "#REQUIRED";
  public static final String ATT_VERSION = "version";
  public static final String ATT_NAME = "name";
  public static final String VAL_YES = "yes";
  public static final String VAL_NO = "no";
  protected DocumentBuilderFactory m_Factory = null;
  

  protected DocumentBuilder m_Builder = null;
  

  protected boolean m_Validating = false;
  

  protected Document m_Document = null;
  

  protected String m_DocType = null;
  

  protected String m_RootNode = null;
  

  protected XPath m_XPath = null;
  



  public XMLDocument()
    throws Exception
  {
    m_Factory = DocumentBuilderFactory.newInstance();
    m_XPath = XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom").newXPath();
    setDocType(null);
    setRootNode(null);
    setValidating(false);
  }
  





  public XMLDocument(String xml)
    throws Exception
  {
    this();
    read(xml);
  }
  





  public XMLDocument(File file)
    throws Exception
  {
    this();
    read(file);
  }
  





  public XMLDocument(InputStream stream)
    throws Exception
  {
    this();
    read(stream);
  }
  





  public XMLDocument(Reader reader)
    throws Exception
  {
    this();
    read(reader);
  }
  




  public DocumentBuilderFactory getFactory()
  {
    return m_Factory;
  }
  




  public DocumentBuilder getBuilder()
  {
    return m_Builder;
  }
  




  public boolean getValidating()
  {
    return m_Validating;
  }
  





  public void setValidating(boolean validating)
    throws Exception
  {
    m_Validating = validating;
    m_Factory.setValidating(validating);
    m_Builder = m_Factory.newDocumentBuilder();
    clear();
  }
  




  public Document getDocument()
  {
    return m_Document;
  }
  




  public void setDocument(Document newDocument)
  {
    m_Document = newDocument;
  }
  





  public void setDocType(String docType)
  {
    m_DocType = docType;
  }
  




  public String getDocType()
  {
    return m_DocType;
  }
  





  public void setRootNode(String rootNode)
  {
    if (rootNode == null) {
      m_RootNode = "root";
    } else {
      m_RootNode = rootNode;
    }
  }
  



  public String getRootNode()
  {
    return m_RootNode;
  }
  





  public void clear()
  {
    newDocument(getDocType(), getRootNode());
  }
  







  public Document newDocument(String docType, String rootNode)
  {
    m_Document = getBuilder().newDocument();
    m_Document.appendChild(m_Document.createElement(rootNode));
    setDocType(docType);
    
    return getDocument();
  }
  






  public Document read(String xml)
    throws Exception
  {
    if (xml.toLowerCase().indexOf("<?xml") > -1) {
      return read(new ByteArrayInputStream(xml.getBytes()));
    }
    return read(new File(xml));
  }
  





  public Document read(File file)
    throws Exception
  {
    m_Document = getBuilder().parse(file);
    return getDocument();
  }
  





  public Document read(InputStream stream)
    throws Exception
  {
    m_Document = getBuilder().parse(stream);
    return getDocument();
  }
  





  public Document read(Reader reader)
    throws Exception
  {
    m_Document = getBuilder().parse(new InputSource(reader));
    return getDocument();
  }
  





  public void write(String file)
    throws Exception
  {
    write(new File(file));
  }
  




  public void write(File file)
    throws Exception
  {
    write(new BufferedWriter(new FileWriter(file)));
  }
  






  public void write(OutputStream stream)
    throws Exception
  {
    String xml = toString();
    stream.write(xml.getBytes(), 0, xml.length());
    stream.flush();
  }
  




  public void write(Writer writer)
    throws Exception
  {
    writer.write(toString());
    writer.flush();
  }
  





  public static Vector getChildTags(Node parent)
  {
    return getChildTags(parent, "");
  }
  










  public static Vector getChildTags(Node parent, String name)
  {
    Vector result = new Vector();
    
    NodeList list = parent.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      if ((list.item(i) instanceof Element))
      {

        if ((name.length() == 0) || 
          (((Element)list.item(i)).getTagName().equals(name)))
        {

          result.add(list.item(i)); }
      }
    }
    return result;
  }
  




  protected Object eval(String xpath, QName type)
  {
    Object result;
    


    try
    {
      result = m_XPath.evaluate(xpath, m_Document, type);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  






  public NodeList findNodes(String xpath)
  {
    return (NodeList)eval(xpath, XPathConstants.NODESET);
  }
  






  public Node getNode(String xpath)
  {
    return (Node)eval(xpath, XPathConstants.NODE);
  }
  





  public Boolean evalBoolean(String xpath)
  {
    return (Boolean)eval(xpath, XPathConstants.BOOLEAN);
  }
  






  public Double evalDouble(String xpath)
  {
    return (Double)eval(xpath, XPathConstants.NUMBER);
  }
  





  public String evalString(String xpath)
  {
    return (String)eval(xpath, XPathConstants.STRING);
  }
  











  public static String getContent(Element node)
  {
    String result = "";
    NodeList list = node.getChildNodes();
    
    for (int i = 0; i < list.getLength(); i++) {
      Node item = list.item(i);
      if (item.getNodeType() == 3) {
        result = result + item.getNodeValue();
      }
    }
    return result.trim();
  }
  















  protected StringBuffer toString(StringBuffer buf, Node parent, int depth)
  {
    String indent = "";
    for (int i = 0; i < depth; i++) {
      indent = indent + "   ";
    }
    if (parent.getNodeType() == 3) {
      if (!parent.getNodeValue().trim().equals("")) {
        buf.append(indent + parent.getNodeValue().trim() + "\n");
      }
    }
    else if (parent.getNodeType() == 8) {
      buf.append(indent + "<!--" + parent.getNodeValue() + "-->\n");
    }
    else {
      buf.append(indent + "<" + parent.getNodeName());
      
      if (parent.hasAttributes()) {
        NamedNodeMap atts = parent.getAttributes();
        for (int n = 0; n < atts.getLength(); n++) {
          Node node = atts.item(n);
          buf.append(" " + node.getNodeName() + "=\"" + node.getNodeValue() + "\"");
        }
      }
      
      if (parent.hasChildNodes()) {
        NodeList list = parent.getChildNodes();
        
        if ((list.getLength() == 1) && (list.item(0).getNodeType() == 3)) {
          buf.append(">");
          buf.append(list.item(0).getNodeValue().trim());
          buf.append("</" + parent.getNodeName() + ">\n");
        }
        else {
          buf.append(">\n");
          for (int n = 0; n < list.getLength(); n++) {
            Node node = list.item(n);
            toString(buf, node, depth + 1);
          }
          buf.append(indent + "</" + parent.getNodeName() + ">\n");
        }
      }
      else {
        buf.append("/>\n");
      }
    }
    
    return buf;
  }
  


  public void print()
  {
    System.out.println(toString());
  }
  






  public String toString()
  {
    String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n";
    if (getDocType() != null) {
      header = header + getDocType() + "\n\n";
    }
    return toString(new StringBuffer(header), getDocument().getDocumentElement(), 0).toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
  








  public static void main(String[] args)
    throws Exception
  {
    if (args.length > 0) {
      XMLDocument doc = new XMLDocument();
      

      doc.read(args[0]);
      

      doc.print();
      

      if (args.length > 1) {
        doc.write(args[1]);
      }
    }
  }
}
