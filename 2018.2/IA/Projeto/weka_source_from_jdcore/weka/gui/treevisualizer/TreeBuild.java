package weka.gui.treevisualizer;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Hashtable;
import java.util.Vector;

























































public class TreeBuild
{
  private String m_graphName;
  private Vector m_aNodes;
  private Vector m_aEdges;
  private Vector m_nodes;
  private Vector m_edges;
  private InfoObject m_grObj;
  private InfoObject m_noObj;
  private InfoObject m_edObj;
  private boolean m_digraph;
  private StreamTokenizer m_st;
  private Hashtable m_colorTable;
  
  public TreeBuild()
  {
    m_colorTable = new Hashtable();
    
    Colors ab = new Colors();
    for (int noa = 0; noa < m_cols.length; noa++) {
      m_colorTable.put(m_cols[noa].m_name, m_cols[noa].m_col);
    }
  }
  







  public Node create(Reader t)
  {
    m_nodes = new Vector(50, 50);
    m_edges = new Vector(50, 50);
    m_grObj = new InfoObject("graph");
    m_noObj = new InfoObject("node");
    m_edObj = new InfoObject("edge");
    m_digraph = false;
    
    m_st = new StreamTokenizer(new BufferedReader(t));
    setSyntax();
    
    graph();
    
    Node top = generateStructures();
    
    return top;
  }
  












  private Node generateStructures()
  {
    m_aNodes = new Vector(50, 50);
    m_aEdges = new Vector(50, 50);
    for (int noa = 0; noa < m_nodes.size(); noa++) {
      InfoObject t = (InfoObject)m_nodes.elementAt(noa);
      String id = m_id;
      String label;
      String label; if (m_label == null) { String label;
        if (m_noObj.m_label == null) {
          label = "";
        }
        else {
          label = m_noObj.m_label;
        }
      }
      else {
        label = m_label; }
      Integer shape;
      Integer shape;
      if (m_shape == null) { Integer shape;
        if (m_noObj.m_shape == null) {
          shape = new Integer(2);
        }
        else {
          shape = getShape(m_noObj.m_shape);
        }
      }
      else {
        shape = getShape(m_shape);
      }
      if (shape == null)
        shape = new Integer(2);
      Integer style;
      Integer style;
      if (m_style == null) { Integer style;
        if (m_noObj.m_style == null) {
          style = new Integer(1);
        }
        else {
          style = getStyle(m_noObj.m_style);
        }
      }
      else {
        style = getStyle(m_style);
      }
      if (style == null)
        style = new Integer(1);
      int fontsize;
      int fontsize;
      if (m_fontSize == null) { int fontsize;
        if (m_noObj.m_fontSize == null) {
          fontsize = 12;
        }
        else {
          fontsize = Integer.valueOf(m_noObj.m_fontSize).intValue();
        }
      }
      else {
        fontsize = Integer.valueOf(m_fontSize).intValue(); }
      Color fontcolor;
      Color fontcolor;
      if (m_fontColor == null) { Color fontcolor;
        if (m_noObj.m_fontColor == null) {
          fontcolor = Color.black;
        }
        else {
          fontcolor = (Color)m_colorTable.get(m_noObj.m_fontColor.toLowerCase());
        }
      }
      else
      {
        fontcolor = (Color)m_colorTable.get(m_fontColor.toLowerCase());
      }
      if (fontcolor == null)
        fontcolor = Color.black;
      Color color;
      Color color;
      if (m_color == null) { Color color;
        if (m_noObj.m_color == null) {
          color = Color.gray;
        }
        else {
          color = (Color)m_colorTable.get(m_noObj.m_color.toLowerCase());
        }
      }
      else {
        color = (Color)m_colorTable.get(m_color.toLowerCase());
      }
      if (color == null) {
        color = Color.gray;
      }
      
      m_aNodes.addElement(new Node(label, id, style.intValue(), shape.intValue(), color, m_data));
    }
    


    for (int noa = 0; noa < m_edges.size(); noa++) {
      InfoObject t = (InfoObject)m_edges.elementAt(noa);
      String id = m_id;
      String label;
      String label; if (m_label == null) { String label;
        if (m_noObj.m_label == null) {
          label = "";
        }
        else {
          label = m_noObj.m_label;
        }
      }
      else {
        label = m_label; }
      Integer shape;
      Integer shape;
      if (m_shape == null) { Integer shape;
        if (m_noObj.m_shape == null) {
          shape = new Integer(2);
        }
        else {
          shape = getShape(m_noObj.m_shape);
        }
      }
      else {
        shape = getShape(m_shape);
      }
      if (shape == null)
        shape = new Integer(2);
      Integer style;
      Integer style;
      if (m_style == null) { Integer style;
        if (m_noObj.m_style == null) {
          style = new Integer(1);
        }
        else {
          style = getStyle(m_noObj.m_style);
        }
      }
      else {
        style = getStyle(m_style);
      }
      if (style == null)
        style = new Integer(1);
      int fontsize;
      int fontsize;
      if (m_fontSize == null) { int fontsize;
        if (m_noObj.m_fontSize == null) {
          fontsize = 12;
        }
        else {
          fontsize = Integer.valueOf(m_noObj.m_fontSize).intValue();
        }
      }
      else {
        fontsize = Integer.valueOf(m_fontSize).intValue(); }
      Color fontcolor;
      Color fontcolor;
      if (m_fontColor == null) { Color fontcolor;
        if (m_noObj.m_fontColor == null) {
          fontcolor = Color.black;
        }
        else {
          fontcolor = (Color)m_colorTable.get(m_noObj.m_fontColor.toLowerCase());
        }
      }
      else
      {
        fontcolor = (Color)m_colorTable.get(m_fontColor.toLowerCase());
      }
      if (fontcolor == null)
        fontcolor = Color.black;
      Color color;
      Color color;
      if (m_color == null) { Color color;
        if (m_noObj.m_color == null) {
          color = Color.white;
        }
        else {
          color = (Color)m_colorTable.get(m_noObj.m_color.toLowerCase());
        }
      }
      else {
        color = (Color)m_colorTable.get(m_color.toLowerCase());
      }
      if (color == null) {
        color = Color.white;
      }
      
      m_aEdges.addElement(new Edge(label, m_source, m_target));
    }
    

    Node sour = null;Node targ = null;
    
    for (int noa = 0; noa < m_aEdges.size(); noa++) {
      boolean f_set = false;
      boolean s_set = false;
      Edge y = (Edge)m_aEdges.elementAt(noa);
      for (int nob = 0; nob < m_aNodes.size(); nob++) {
        Node x = (Node)m_aNodes.elementAt(nob);
        if (x.getRefer().equals(y.getRtarget())) {
          f_set = true;
          targ = x;
        }
        if (x.getRefer().equals(y.getRsource())) {
          s_set = true;
          sour = x;
        }
        if ((f_set == true) && (s_set == true)) {
          break;
        }
      }
      if (targ != sour) {
        y.setTarget(targ);
        y.setSource(sour);
      }
      else {
        System.out.println("logic error");
      }
    }
    
    for (int noa = 0; noa < m_aNodes.size(); noa++) {
      if (((Node)m_aNodes.elementAt(noa)).getParent(0) == null) {
        sour = (Node)m_aNodes.elementAt(noa);
      }
    }
    
    return sour;
  }
  





  private Integer getShape(String sh)
  {
    if ((sh.equalsIgnoreCase("box")) || (sh.equalsIgnoreCase("rectangle"))) {
      return new Integer(1);
    }
    if (sh.equalsIgnoreCase("oval")) {
      return new Integer(2);
    }
    if (sh.equalsIgnoreCase("diamond")) {
      return new Integer(3);
    }
    
    return null;
  }
  







  private Integer getStyle(String sty)
  {
    if (sty.equalsIgnoreCase("filled")) {
      return new Integer(1);
    }
    
    return null;
  }
  





  private void setSyntax()
  {
    m_st.resetSyntax();
    m_st.eolIsSignificant(false);
    m_st.slashStarComments(true);
    m_st.slashSlashComments(true);
    
    m_st.whitespaceChars(0, 32);
    m_st.wordChars(33, 255);
    m_st.ordinaryChar(91);
    m_st.ordinaryChar(93);
    m_st.ordinaryChar(123);
    m_st.ordinaryChar(125);
    m_st.ordinaryChar(45);
    m_st.ordinaryChar(62);
    m_st.ordinaryChar(47);
    m_st.ordinaryChar(42);
    m_st.quoteChar(34);
    m_st.whitespaceChars(59, 59);
    m_st.ordinaryChar(61);
  }
  


  private void alterSyntax()
  {
    m_st.resetSyntax();
    m_st.wordChars(0, 255);
    m_st.slashStarComments(false);
    m_st.slashSlashComments(false);
    m_st.ordinaryChar(10);
    m_st.ordinaryChar(13);
  }
  





  private void nextToken(String r)
  {
    int t = 0;
    try {
      t = m_st.nextToken();
    }
    catch (IOException e) {}
    
    if (t == -1) {
      System.out.println("eof , " + r);
    }
    else if (t == -2) {
      System.out.println("got a number , " + r);
    }
  }
  



  private void graph()
  {
    boolean flag = true;
    


    nextToken("expected 'digraph'");
    
    if (m_st.sval.equalsIgnoreCase("digraph")) {
      m_digraph = true;
    }
    else {
      System.out.println("expected 'digraph'");
    }
    
    nextToken("expected a Graph Name");
    if (m_st.sval != null) {
      m_graphName = m_st.sval;
    }
    else {
      System.out.println("expected a Graph Name");
    }
    
    nextToken("expected '{'");
    
    if (m_st.ttype == 123) {
      stmtList();
    }
    else {
      System.out.println("expected '{'");
    }
  }
  




  private void stmtList()
  {
    boolean flag = true;
    

    while (flag) {
      nextToken("expects a STMT_LIST item or '}'");
      if (m_st.ttype == 125) {
        flag = false;
      }
      else if ((m_st.sval.equalsIgnoreCase("graph")) || (m_st.sval.equalsIgnoreCase("node")) || (m_st.sval.equalsIgnoreCase("edge")))
      {

        m_st.pushBack();
        attrStmt();
      }
      else if (m_st.sval != null) {
        nodeId(m_st.sval, 0);
      }
      else {
        System.out.println("expects a STMT_LIST item or '}'");
      }
    }
  }
  




  private void attrStmt()
  {
    nextToken("expected 'graph' or 'node' or 'edge'");
    
    if (m_st.sval.equalsIgnoreCase("graph")) {
      nextToken("expected a '['");
      if (m_st.ttype == 91) {
        attrList(m_grObj);
      }
      else {
        System.out.println("expected a '['");
      }
    }
    else if (m_st.sval.equalsIgnoreCase("node")) {
      nextToken("expected a '['");
      if (m_st.ttype == 91) {
        attrList(m_noObj);
      }
      else {
        System.out.println("expected a '['");
      }
    }
    else if (m_st.sval.equalsIgnoreCase("edge")) {
      nextToken("expected a '['");
      if (m_st.ttype == 91) {
        attrList(m_edObj);
      }
      else {
        System.out.println("expected a '['");
      }
    }
    else
    {
      System.out.println("expected 'graph' or 'node' or 'edge'");
    }
  }
  








  private void nodeId(String s, int t)
  {
    nextToken("error occurred in node_id");
    
    if (m_st.ttype == 125)
    {
      if (t == 0) {
        m_nodes.addElement(new InfoObject(s));
      }
      m_st.pushBack();
    }
    else if (m_st.ttype == 45) {
      nextToken("error occurred checking for an edge");
      if (m_st.ttype == 62) {
        edgeStmt(s);
      }
      else {
        System.out.println("error occurred checking for an edge");
      }
    }
    else if (m_st.ttype == 91)
    {
      if (t == 0) {
        m_nodes.addElement(new InfoObject(s));
        attrList((InfoObject)m_nodes.lastElement());
      }
      else {
        attrList((InfoObject)m_edges.lastElement());
      }
    }
    else if (m_st.sval != null)
    {
      if (t == 0) {
        m_nodes.addElement(new InfoObject(s));
      }
      m_st.pushBack();
    }
    else {
      System.out.println("error occurred in node_id");
    }
  }
  




  private void edgeStmt(String i)
  {
    nextToken("error getting target of edge");
    
    if (m_st.sval != null) {
      m_edges.addElement(new InfoObject("an edge ,no id"));
      m_edges.lastElement()).m_source = i;
      m_edges.lastElement()).m_target = m_st.sval;
      nodeId(m_st.sval, 1);
    }
    else {
      System.out.println("error getting target of edge");
    }
  }
  




  private void attrList(InfoObject a)
  {
    boolean flag = true;
    
    while (flag) {
      nextToken("error in attr_list");
      
      if (m_st.ttype == 93) {
        flag = false;
      }
      else if (m_st.sval.equalsIgnoreCase("color")) {
        nextToken("error getting color");
        if (m_st.ttype == 61) {
          nextToken("error getting color");
          if (m_st.sval != null) {
            m_color = m_st.sval;
          }
          else {
            System.out.println("error getting color");
          }
        }
        else {
          System.out.println("error getting color");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("fontcolor")) {
        nextToken("error getting font color");
        if (m_st.ttype == 61) {
          nextToken("error getting font color");
          if (m_st.sval != null) {
            m_fontColor = m_st.sval;
          }
          else {
            System.out.println("error getting font color");
          }
        }
        else {
          System.out.println("error getting font color");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("fontsize")) {
        nextToken("error getting font size");
        if (m_st.ttype == 61) {
          nextToken("error getting font size");
          if (m_st.sval != null) {
            m_fontSize = m_st.sval;
          }
          else {
            System.out.println("error getting font size");
          }
        }
        else {
          System.out.println("error getting font size");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("label")) {
        nextToken("error getting label");
        if (m_st.ttype == 61) {
          nextToken("error getting label");
          if (m_st.sval != null) {
            m_label = m_st.sval;
          }
          else {
            System.out.println("error getting label");
          }
        }
        else {
          System.out.println("error getting label");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("shape")) {
        nextToken("error getting shape");
        if (m_st.ttype == 61) {
          nextToken("error getting shape");
          if (m_st.sval != null) {
            m_shape = m_st.sval;
          }
          else {
            System.out.println("error getting shape");
          }
        }
        else {
          System.out.println("error getting shape");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("style")) {
        nextToken("error getting style");
        if (m_st.ttype == 61) {
          nextToken("error getting style");
          if (m_st.sval != null) {
            m_style = m_st.sval;
          }
          else {
            System.out.println("error getting style");
          }
        }
        else {
          System.out.println("error getting style");
        }
      }
      else if (m_st.sval.equalsIgnoreCase("data")) {
        nextToken("error getting data");
        if (m_st.ttype == 61)
        {

          alterSyntax();
          m_data = new String("");
          for (;;)
          {
            nextToken("error getting data");
            if ((m_st.sval != null) && (m_data != null) && (m_st.sval.equals(","))) {
              break;
            }
            
            if (m_st.sval != null) {
              m_data = m_data.concat(m_st.sval);
            }
            else if (m_st.ttype == 13) {
              m_data = m_data.concat("\r");
            }
            else if (m_st.ttype == 10) {
              m_data = m_data.concat("\n");
            }
            else {
              System.out.println("error getting data");
            }
          }
          setSyntax();
        }
        else {
          System.out.println("error getting data");
        }
      }
    }
  }
  



  private class InfoObject
  {
    public String m_id;
    


    public String m_color;
    


    public String m_fontColor;
    


    public String m_fontSize;
    


    public String m_label;
    

    public String m_shape;
    

    public String m_style;
    

    public String m_source;
    

    public String m_target;
    

    public String m_data;
    


    public InfoObject(String i)
    {
      m_id = i;
      m_color = null;
      m_fontColor = null;
      m_fontSize = null;
      m_label = null;
      m_shape = null;
      m_style = null;
      m_source = null;
      m_target = null;
      m_data = null;
    }
  }
}
