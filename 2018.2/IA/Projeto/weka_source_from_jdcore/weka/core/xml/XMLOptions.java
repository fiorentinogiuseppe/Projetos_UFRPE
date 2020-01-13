package weka.core.xml;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;




































































public class XMLOptions
  implements RevisionHandler
{
  public static final String TAG_OPTION = "option";
  public static final String TAG_OPTIONS = "options";
  public static final String ATT_NAME = "name";
  public static final String ATT_TYPE = "type";
  public static final String ATT_VALUE = "value";
  public static final String VAL_TYPE_FLAG = "flag";
  public static final String VAL_TYPE_SINGLE = "single";
  public static final String VAL_TYPE_HYPHENS = "hyphens";
  public static final String VAL_TYPE_QUOTES = "quotes";
  public static final String VAL_TYPE_CLASSIFIER = "classifier";
  public static final String VAL_TYPE_OPTIONHANDLER = "optionhandler";
  public static final String ROOT_NODE = "options";
  public static final String DOCTYPE = "<!DOCTYPE options\n[\n   <!ELEMENT options (option)*>\n   <!ATTLIST options type CDATA \"optionhandler\">\n   <!ATTLIST options value CDATA \"\">\n   <!ELEMENT option (#PCDATA | options)*>\n   <!ATTLIST option name CDATA #REQUIRED>\n   <!ATTLIST option type (flag | single | hyphens | quotes) \"single\">\n]\n>";
  protected XMLDocument m_XMLDocument = null;
  




  public XMLOptions()
    throws Exception
  {
    m_XMLDocument = new XMLDocument();
    m_XMLDocument.setRootNode("options");
    m_XMLDocument.setDocType("<!DOCTYPE options\n[\n   <!ELEMENT options (option)*>\n   <!ATTLIST options type CDATA \"optionhandler\">\n   <!ATTLIST options value CDATA \"\">\n   <!ELEMENT option (#PCDATA | options)*>\n   <!ATTLIST option name CDATA #REQUIRED>\n   <!ATTLIST option type (flag | single | hyphens | quotes) \"single\">\n]\n>");
    setValidating(true);
  }
  





  public XMLOptions(String xml)
    throws Exception
  {
    this();
    getXMLDocument().read(xml);
  }
  





  public XMLOptions(File file)
    throws Exception
  {
    this();
    getXMLDocument().read(file);
  }
  





  public XMLOptions(InputStream stream)
    throws Exception
  {
    this();
    getXMLDocument().read(stream);
  }
  





  public XMLOptions(Reader reader)
    throws Exception
  {
    this();
    getXMLDocument().read(reader);
  }
  




  public boolean getValidating()
  {
    return m_XMLDocument.getValidating();
  }
  





  public void setValidating(boolean validating)
    throws Exception
  {
    m_XMLDocument.setValidating(validating);
  }
  




  public Document getDocument()
  {
    fixHyphens();
    return m_XMLDocument.getDocument();
  }
  






  public XMLDocument getXMLDocument()
  {
    fixHyphens();
    return m_XMLDocument;
  }
  













  protected void fixHyphens()
  {
    NodeList list = m_XMLDocument.findNodes("//option");
    

    Vector hyphens = new Vector();
    for (int i = 0; i < list.getLength(); i++) {
      if (((Element)list.item(i)).getAttribute("type").equals("hyphens")) {
        hyphens.add(list.item(i));
      }
    }
    
    for (i = 0; i < hyphens.size(); i++) {
      Node node = (Node)hyphens.get(i);
      

      boolean isLast = true;
      Node tmpNode = node;
      while (tmpNode.getNextSibling() != null)
      {
        if (tmpNode.getNextSibling().getNodeType() == 1) {
          isLast = false;
          break;
        }
        tmpNode = tmpNode.getNextSibling();
      }
      

      if (!isLast) {
        tmpNode = node.getParentNode();
        tmpNode.removeChild(node);
        tmpNode.appendChild(node);
      }
    }
  }
  














  protected String toCommandLine(Element parent)
  {
    Vector<String> result = new Vector();
    String[] params;
    int n;
    if (parent.getNodeName().equals("options"))
    {
      Vector list = XMLDocument.getChildTags(parent);
      
      if (parent.getAttribute("type").equals("classifier")) {
        System.err.println("Type 'classifier' is deprecated, use 'optionhandler' instead!");
        

        parent.setAttribute("type", "optionhandler");
      }
      
      if (parent.getAttribute("type").equals("optionhandler")) {
        result.add(parent.getAttribute("value"));
        

        if ((list.size() > 0) && (parent.getParentNode() != null) && ((parent.getParentNode() instanceof Element)) && (((Element)parent.getParentNode()).getNodeName().equals("option")) && (((Element)parent.getParentNode()).getAttribute("type").equals("hyphens")))
        {



          result.add("--");
        }
      }
      
      for (int i = 0; i < list.size(); i++) {
        String tmpStr = toCommandLine((Element)list.get(i));
        try {
          params = Utils.splitOptions(tmpStr);
          for (n = 0; n < params.length; n++) {
            result.add(params[n]);
          }
        } catch (Exception e) {
          System.err.println("Error splitting: " + tmpStr);
          e.printStackTrace();
        }
      }
    }
    
    if (parent.getNodeName().equals("option")) {
      Vector subList = XMLDocument.getChildTags(parent);
      NodeList subNodeList = parent.getChildNodes();
      
      result.add("-" + parent.getAttribute("name"));
      

      if (parent.getAttribute("type").equals("single")) {
        if ((subNodeList.getLength() > 0) && (subNodeList.item(0).getNodeValue().trim().length() > 0))
        {
          result.add(subNodeList.item(0).getNodeValue());
        }
      }
      else if (parent.getAttribute("type").equals("quotes")) {
        result.add(toCommandLine((Element)subList.get(0)));

      }
      else if (parent.getAttribute("type").equals("hyphens")) {
        String tmpStr = toCommandLine((Element)subList.get(0));
        try {
          params = Utils.splitOptions(tmpStr);
          for (n = 0; n < params.length; n++) {
            result.add(params[n]);
          }
        } catch (Exception e) {
          System.err.println("Error splitting: " + tmpStr);
          e.printStackTrace();
        }
      }
    }
    else
    {
      System.err.println("Unsupported tag '" + parent.getNodeName() + "' - skipped!");
    }
    
    return Utils.joinOptions((String[])result.toArray(new String[result.size()]));
  }
  




  public String toCommandLine()
    throws Exception
  {
    return toCommandLine(getDocument().getDocumentElement());
  }
  




  public String[] toArray()
    throws Exception
  {
    return Utils.splitOptions(toCommandLine());
  }
  




  public String toString()
  {
    return getXMLDocument().toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    if (args.length > 0) {
      System.out.println("\nXML:\n\n" + new XMLOptions(args[0]).toString());
      
      System.out.println("\nCommandline:\n\n" + new XMLOptions(args[0]).toCommandLine());
      
      System.out.println("\nString array:\n");
      String[] options = new XMLOptions(args[0]).toArray();
      for (int i = 0; i < options.length; i++) {
        System.out.println(options[i]);
      }
    }
  }
}
