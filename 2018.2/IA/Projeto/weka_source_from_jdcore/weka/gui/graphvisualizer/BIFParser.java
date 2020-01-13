package weka.gui.graphvisualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import weka.core.FastVector;








































public class BIFParser
  implements GraphConstants
{
  protected FastVector m_nodes;
  protected FastVector m_edges;
  protected String graphName;
  protected String inString;
  protected InputStream inStream;
  
  public BIFParser(String input, FastVector nodes, FastVector edges)
  {
    m_nodes = nodes;m_edges = edges;inString = input;
  }
  







  public BIFParser(InputStream instream, FastVector nodes, FastVector edges)
  {
    m_nodes = nodes;m_edges = edges;inStream = instream;
  }
  












  public String parse()
    throws Exception
  {
    Document dc = null;
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    
    dbf.setIgnoringElementContentWhitespace(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    
    if (inStream != null) {
      dc = db.parse(inStream);
    } else if (inString != null) {
      dc = db.parse(new InputSource(new StringReader(inString)));
    } else {
      Messages.getInstance();throw new Exception(Messages.getString("BIFParser_Parse_Exception_Text"));
    }
    NodeList nl = dc.getElementsByTagName("NETWORK");
    
    if (nl.getLength() == 0) {
      Messages.getInstance();throw new BIFFormatException(Messages.getString("BIFParser_Parse_BIFFormatException_Text_First"));
    }
    

    NodeList templist = ((Element)nl.item(0)).getElementsByTagName("NAME");
    graphName = templist.item(0).getFirstChild().getNodeValue();
    



    nl = dc.getElementsByTagName("VARIABLE");
    for (int i = 0; i < nl.getLength(); i++)
    {
      templist = ((Element)nl.item(i)).getElementsByTagName("NAME");
      if (templist.getLength() > 1) {
        Messages.getInstance();throw new BIFFormatException(Messages.getString("BIFParser_Parse_BIFFormatException_Text_Second") + (i + 1));
      }
      String nodename = templist.item(0).getFirstChild().getNodeValue();
      
      GraphNode n = new GraphNode(nodename, nodename, 3);
      m_nodes.addElement(n);
      
      templist = ((Element)nl.item(i)).getElementsByTagName("PROPERTY");
      for (int j = 0; j < templist.getLength(); j++) {
        if (templist.item(j).getFirstChild().getNodeValue().startsWith("position"))
        {
          String xy = templist.item(j).getFirstChild().getNodeValue();
          




          x = Integer.parseInt(xy.substring(xy.indexOf('(') + 1, xy.indexOf(',')).trim());
          
          y = Integer.parseInt(xy.substring(xy.indexOf(',') + 1, xy.indexOf(')')).trim());
          
          break;
        }
      }
      
      templist = ((Element)nl.item(i)).getElementsByTagName("OUTCOME");
      outcomes = new String[templist.getLength()];
      for (int j = 0; j < templist.getLength(); j++) {
        outcomes[j] = templist.item(j).getFirstChild().getNodeValue();
      }
    }
    


    nl = dc.getElementsByTagName("DEFINITION");
    for (int i = 0; i < nl.getLength(); i++)
    {
      templist = ((Element)nl.item(i)).getElementsByTagName("FOR");
      
      String nid = templist.item(0).getFirstChild().getNodeValue();
      

      GraphNode n = (GraphNode)m_nodes.elementAt(0);
      for (int j = 1; (j < m_nodes.size()) && (!ID.equals(nid)); j++) {
        n = (GraphNode)m_nodes.elementAt(j);
      }
      
      templist = ((Element)nl.item(i)).getElementsByTagName("GIVEN");
      int parntOutcomes = 1;
      
      for (int j = 0; j < templist.getLength(); j++) {
        nid = templist.item(j).getFirstChild().getNodeValue();
        
        GraphNode n2 = (GraphNode)m_nodes.elementAt(0);
        for (int k = 1; (k < m_nodes.size()) && (!ID.equals(nid)); k++)
          n2 = (GraphNode)m_nodes.elementAt(k);
        m_edges.addElement(new GraphEdge(m_nodes.indexOf(n2), m_nodes.indexOf(n), 1));
        

        parntOutcomes *= outcomes.length;
      }
      

      templist = ((Element)nl.item(i)).getElementsByTagName("TABLE");
      if (templist.getLength() > 1) {
        Messages.getInstance();throw new BIFFormatException(Messages.getString("BIFParser_Parse_BIFFormatException_Text_Second_Alpha") + ID);
      }
      
      String probs = templist.item(0).getFirstChild().getNodeValue();
      StringTokenizer tk = new StringTokenizer(probs, " \n\t");
      
      if (parntOutcomes * outcomes.length > tk.countTokens()) {
        Messages.getInstance();Messages.getInstance();throw new BIFFormatException(Messages.getString("BIFParser_Parse_BIFFormatException_Text_Third") + ID + Messages.getString("BIFParser_Parse_BIFFormatException_Text_Fourth"));
      }
      if (parntOutcomes * outcomes.length < tk.countTokens()) {
        Messages.getInstance();Messages.getInstance();throw new BIFFormatException(Messages.getString("BIFParser_Parse_BIFFormatException_Text_Fifth") + ID + Messages.getString("BIFParser_Parse_BIFFormatException_Text_Sixth"));
      }
      
      probs = new double[parntOutcomes][outcomes.length];
      for (int r = 0; r < parntOutcomes; r++) {
        for (int c = 0; c < outcomes.length; c++) {
          try {
            probs[r][c] = Double.parseDouble(tk.nextToken());
          } catch (NumberFormatException ne) {
            throw ne;
          }
        }
      }
    }
    










    int[] noOfEdgesOfNode = new int[m_nodes.size()];
    int[] noOfPrntsOfNode = new int[m_nodes.size()];
    for (int i = 0; i < m_edges.size(); i++) {
      GraphEdge e = (GraphEdge)m_edges.elementAt(i);
      noOfEdgesOfNode[src] += 1;
      noOfPrntsOfNode[dest] += 1;
    }
    
    for (int i = 0; i < m_edges.size(); i++) {
      GraphEdge e = (GraphEdge)m_edges.elementAt(i);
      GraphNode n = (GraphNode)m_nodes.elementAt(src);
      GraphNode n2 = (GraphNode)m_nodes.elementAt(dest);
      if (edges == null) {
        edges = new int[noOfEdgesOfNode[src]][2];
        for (int k = 0; k < edges.length; k++)
          edges[k][0] = -1;
      }
      if (prnts == null) {
        prnts = new int[noOfPrntsOfNode[dest]];
        for (int k = 0; k < prnts.length; k++) {
          prnts[k] = -1;
        }
      }
      int k = 0;
      while (edges[k][0] != -1) k++;
      edges[k][0] = dest;
      edges[k][1] = type;
      
      k = 0;
      while (prnts[k] != -1) k++;
      prnts[k] = src;
    }
    


    return graphName;
  }
  











  public static void writeXMLBIF03(String filename, String graphName, FastVector nodes, FastVector edges)
  {
    try
    {
      FileWriter outfile = new FileWriter(filename);
      
      StringBuffer text = new StringBuffer();
      
      text.append("<?xml version=\"1.0\"?>\n");
      text.append("<!-- DTD for the XMLBIF 0.3 format -->\n");
      text.append("<!DOCTYPE BIF [\n");
      text.append("\t<!ELEMENT BIF ( NETWORK )*>\n");
      text.append("\t      <!ATTLIST BIF VERSION CDATA #REQUIRED>\n");
      text.append("\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n");
      
      text.append("\t<!ELEMENT NAME (#PCDATA)>\n");
      text.append("\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n");
      
      text.append("\t      <!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n");
      
      text.append("\t<!ELEMENT OUTCOME (#PCDATA)>\n");
      text.append("\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n");
      
      text.append("\t<!ELEMENT FOR (#PCDATA)>\n");
      text.append("\t<!ELEMENT GIVEN (#PCDATA)>\n");
      text.append("\t<!ELEMENT TABLE (#PCDATA)>\n");
      text.append("\t<!ELEMENT PROPERTY (#PCDATA)>\n");
      text.append("]>\n");
      text.append("\n");
      text.append("\n");
      text.append("<BIF VERSION=\"0.3\">\n");
      text.append("<NETWORK>\n");
      text.append("<NAME>" + XMLNormalize(graphName) + "</NAME>\n");
      



      for (int nodeidx = 0; nodeidx < nodes.size(); nodeidx++) {
        GraphNode n = (GraphNode)nodes.elementAt(nodeidx);
        if (nodeType == 3)
        {

          text.append("<VARIABLE TYPE=\"nature\">\n");
          text.append("\t<NAME>" + XMLNormalize(ID) + "</NAME>\n");
          
          if (outcomes != null) {
            for (int outidx = 0; outidx < outcomes.length; outidx++) {
              text.append("\t<OUTCOME>" + XMLNormalize(outcomes[outidx]) + "</OUTCOME>\n");
            }
          } else {
            text.append("\t<OUTCOME>true</OUTCOME>\n");
          }
          text.append("\t<PROPERTY>position = (" + x + "," + y + ")</PROPERTY>\n");
          text.append("</VARIABLE>\n");
        }
      }
      


      for (int nodeidx = 0; nodeidx < nodes.size(); nodeidx++) {
        GraphNode n = (GraphNode)nodes.elementAt(nodeidx);
        if (nodeType == 3)
        {

          text.append("<DEFINITION>\n");
          text.append("<FOR>" + XMLNormalize(ID) + "</FOR>\n");
          int parntOutcomes = 1;
          if (prnts != null) {
            for (int pidx = 0; pidx < prnts.length; pidx++) {
              GraphNode prnt = (GraphNode)nodes.elementAt(prnts[pidx]);
              text.append("\t<GIVEN>" + XMLNormalize(ID) + "</GIVEN>\n");
              if (outcomes != null)
                parntOutcomes *= outcomes.length;
            }
          }
          text.append("<TABLE>\n");
          for (int i = 0; i < parntOutcomes; i++) {
            if (outcomes != null) {
              for (int outidx = 0; outidx < outcomes.length; outidx++) {
                text.append(probs[i][outidx] + " ");
              }
            } else
              text.append("1");
            text.append('\n');
          }
          text.append("</TABLE>\n");
          text.append("</DEFINITION>\n");
        }
      }
      text.append("</NETWORK>\n");
      text.append("</BIF>\n");
      
      outfile.write(text.toString());
      outfile.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  




  private static String XMLNormalize(String sStr)
  {
    StringBuffer sStr2 = new StringBuffer();
    for (int iStr = 0; iStr < sStr.length(); iStr++) {
      char c = sStr.charAt(iStr);
      switch (c) {
      case '&':  sStr2.append("&amp;"); break;
      case '\'':  sStr2.append("&apos;"); break;
      case '"':  sStr2.append("&quot;"); break;
      case '<':  sStr2.append("&lt;"); break;
      case '>':  sStr2.append("&gt;"); break;
      default: 
        sStr2.append(c);
      }
    }
    return sStr2.toString();
  }
}
