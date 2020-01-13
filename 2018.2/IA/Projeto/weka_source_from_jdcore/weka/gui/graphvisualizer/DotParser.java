package weka.gui.graphvisualizer;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import weka.core.FastVector;
























































public class DotParser
  implements GraphConstants
{
  protected FastVector m_nodes;
  protected FastVector m_edges;
  protected Reader m_input;
  protected String m_graphName;
  
  public DotParser(Reader input, FastVector nodes, FastVector edges)
  {
    m_nodes = nodes;m_edges = edges;
    m_input = input;
  }
  







  public String parse()
  {
    StreamTokenizer tk = new StreamTokenizer(new BufferedReader(m_input));
    setSyntax(tk);
    
    graph(tk);
    
    return m_graphName;
  }
  





  protected void setSyntax(StreamTokenizer tk)
  {
    tk.resetSyntax();
    tk.eolIsSignificant(false);
    tk.slashStarComments(true);
    tk.slashSlashComments(true);
    tk.whitespaceChars(0, 32);
    tk.wordChars(33, 255);
    tk.ordinaryChar(91);
    tk.ordinaryChar(93);
    tk.ordinaryChar(123);
    tk.ordinaryChar(125);
    tk.ordinaryChar(45);
    tk.ordinaryChar(62);
    tk.ordinaryChar(47);
    tk.ordinaryChar(42);
    tk.quoteChar(34);
    tk.whitespaceChars(59, 59);
    tk.ordinaryChar(61);
  }
  





  protected void graph(StreamTokenizer tk)
  {
    try
    {
      tk.nextToken();
      
      if (ttype == -3) {
        if (sval.equalsIgnoreCase("digraph")) {
          tk.nextToken();
          if (ttype == -3) {
            m_graphName = sval;
            tk.nextToken();
          }
          
          while (ttype != 123) {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("DotParser_Graph_Error_Text_First") + tk.lineno() + Messages.getString("DotParser_Graph_Error_Text_Second") + sval);
            
            tk.nextToken();
            if (ttype == -1)
              return;
          }
          stmtList(tk);
        }
        else if (sval.equalsIgnoreCase("graph")) {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_Graph_Error_Text_Third"));
        } else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_Graph_Error_Text_Fourth") + tk.lineno());
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
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
          edges[k][1] = 0;
      }
      if (prnts == null) {
        prnts = new int[noOfPrntsOfNode[dest]];
        for (int k = 0; k < prnts.length; k++)
          prnts[k] = -1;
      }
      int k = 0;
      while (edges[k][1] != 0) k++;
      edges[k][0] = dest;
      edges[k][1] = type;
      
      k = 0;
      while (prnts[k] != -1) k++;
      prnts[k] = src;
    }
  }
  
  protected void stmtList(StreamTokenizer tk) throws Exception
  {
    tk.nextToken();
    if ((ttype == 125) || (ttype == -1)) {
      return;
    }
    stmt(tk);
    stmtList(tk);
  }
  



  protected void stmt(StreamTokenizer tk)
  {
    if ((!sval.equalsIgnoreCase("graph")) && (!sval.equalsIgnoreCase("node")) && (!sval.equalsIgnoreCase("edge")))
    {
      try
      {

        nodeID(tk);
        int nodeindex = m_nodes.indexOf(new GraphNode(sval, null));
        tk.nextToken();
        
        if (ttype == 91) {
          nodeStmt(tk, nodeindex);
        } else if (ttype == 45) {
          edgeStmt(tk, nodeindex);
        } else {
          Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("DotParser_Stmt_Error_Text_First") + tk.lineno() + Messages.getString("DotParser_Stmt_Error_Text_Second"));
        }
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("DotParser_Stmt_Error_Text_Third") + tk.lineno() + Messages.getString("DotParser_Stmt_Error_Text_Fourth"));
        ex.printStackTrace();
      }
    }
  }
  
  protected void nodeID(StreamTokenizer tk)
    throws Exception
  {
    if ((ttype == 34) || (ttype == -3) || ((ttype >= 97) && (ttype <= 122)) || ((ttype >= 65) && (ttype <= 90)))
    {
      if ((m_nodes != null) && (!m_nodes.contains(new GraphNode(sval, null)))) {
        m_nodes.addElement(new GraphNode(sval, sval));
      }
      
    }
    else {
      throw new Exception();
    }
  }
  

  protected void nodeStmt(StreamTokenizer tk, int nindex)
    throws Exception
  {
    tk.nextToken();
    
    GraphNode temp = (GraphNode)m_nodes.elementAt(nindex);
    
    if ((ttype == 93) || (ttype == -1))
      return;
    if (ttype == -3)
    {
      if (sval.equalsIgnoreCase("label"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype == -3) || (ttype == 34)) {
            lbl = sval;
          } else {
            Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_First") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_Second") + tk.lineno());
          tk.pushBack();
        }
        
      }
      else if (sval.equalsIgnoreCase("color"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype != -3) && (ttype != 34))
          {

            Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_Third") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_Fourth") + tk.lineno());
          tk.pushBack();
        }
        
      }
      else if (sval.equalsIgnoreCase("style"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype != -3) && (ttype != 34))
          {

            Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_Fifth") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_NodeStmt_Error_Text_Sixth") + tk.lineno());
          tk.pushBack();
        }
      }
    }
    nodeStmt(tk, nindex);
  }
  
  protected void edgeStmt(StreamTokenizer tk, int nindex)
    throws Exception
  {
    tk.nextToken();
    
    GraphEdge e = null;
    if (ttype == 62) {
      tk.nextToken();
      if (ttype == 123) {
        for (;;) {
          tk.nextToken();
          if (ttype == 125) {
            break;
          }
          nodeID(tk);
          e = new GraphEdge(nindex, m_nodes.indexOf(new GraphNode(sval, null)), 1);
          

          if ((m_edges != null) && (!m_edges.contains(e))) {
            m_edges.addElement(e);
          }
        }
      }
      





      nodeID(tk);
      e = new GraphEdge(nindex, m_nodes.indexOf(new GraphNode(sval, null)), 1);
      

      if ((m_edges != null) && (!m_edges.contains(e))) {
        m_edges.addElement(e);
      }
      

    }
    else
    {
      if (ttype == 45) {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeStmt_Error_Text_First") + tk.lineno() + Messages.getString("DotParser_EdgeStmt_Error_Text_Second"));
        
        if (ttype == -3)
          tk.pushBack();
        return;
      }
      
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeStmt_Error_Text_Third") + tk.lineno() + Messages.getString("DotParser_EdgeStmt_Error_Text_Fourth"));
      if (ttype == -3)
        tk.pushBack();
      return;
    }
    
    tk.nextToken();
    
    if (ttype == 91) {
      edgeAttrib(tk, e);
    } else {
      tk.pushBack();
    }
  }
  
  protected void edgeAttrib(StreamTokenizer tk, GraphEdge e) throws Exception
  {
    tk.nextToken();
    
    if ((ttype == 93) || (ttype == -1))
      return;
    if (ttype == -3)
    {
      if (sval.equalsIgnoreCase("label"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype == -3) || (ttype == 34)) {
            Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Text") + sval);
          } else {
            Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text_First") + tk.lineno());
          tk.pushBack();
        }
      }
      else if (sval.equalsIgnoreCase("color"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype != -3) && (ttype != 34))
          {

            Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text_Second") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text_Third") + tk.lineno());
          tk.pushBack();
        }
        
      }
      else if (sval.equalsIgnoreCase("style"))
      {
        tk.nextToken();
        if (ttype == 61) {
          tk.nextToken();
          if ((ttype != -3) && (ttype != 34))
          {

            Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text_Fourth") + tk.lineno());
            tk.pushBack();
          }
        }
        else {
          Messages.getInstance();System.err.println(Messages.getString("DotParser_EdgeAttrib_Error_Text_Fifth") + tk.lineno());
          tk.pushBack();
        }
      }
    }
    edgeAttrib(tk, e);
  }
  











  public static void writeDOT(String filename, String graphName, FastVector nodes, FastVector edges)
  {
    try
    {
      FileWriter os = new FileWriter(filename);
      os.write("digraph ", 0, "digraph ".length());
      if (graphName != null)
        os.write(graphName + " ", 0, graphName.length() + 1);
      os.write("{\n", 0, "{\n".length());
      

      for (int i = 0; i < edges.size(); i++) {
        GraphEdge e = (GraphEdge)edges.elementAt(i);
        os.write(elementAtsrc)).ID, 0, elementAtsrc)).ID.length());
        
        os.write("->", 0, "->".length());
        os.write(elementAtdest)).ID + "\n", 0, elementAtdest)).ID.length() + 1);
      }
      

      os.write("}\n", 0, "}\n".length());
      os.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
