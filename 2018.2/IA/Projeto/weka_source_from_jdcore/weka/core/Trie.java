package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;





































public class Trie
  implements Serializable, Cloneable, Collection<String>, RevisionHandler
{
  private static final long serialVersionUID = -5897980928817779048L;
  protected TrieNode m_Root;
  protected int m_HashCode;
  protected boolean m_RecalcHashCode;
  
  public static class TrieNode
    extends DefaultMutableTreeNode
    implements RevisionHandler
  {
    private static final long serialVersionUID = -2252907099391881148L;
    public static final Character STOP = Character.valueOf('\000');
    


    protected Hashtable<Character, TrieNode> m_Children;
    



    public TrieNode(char c)
    {
      this(new Character(c));
    }
    




    public TrieNode(Character c)
    {
      super();
      
      m_Children = new Hashtable(100);
    }
    




    public Character getChar()
    {
      return (Character)getUserObject();
    }
    




    public void setChar(Character value)
    {
      setUserObject(value);
    }
    










    public boolean add(String suffix)
    {
      boolean result = false;
      Character c = Character.valueOf(suffix.charAt(0));
      String newSuffix = suffix.substring(1);
      

      TrieNode child = (TrieNode)m_Children.get(c);
      if (child == null) {
        result = true;
        child = add(c);
      }
      

      if (newSuffix.length() > 0) {
        result = (child.add(newSuffix)) || (result);
      }
      return result;
    }
    







    protected TrieNode add(Character c)
    {
      TrieNode child = new TrieNode(c);
      add(child);
      m_Children.put(c, child);
      
      return child;
    }
    






    protected void remove(Character c)
    {
      TrieNode child = (TrieNode)m_Children.get(c);
      remove(child);
      m_Children.remove(c);
    }
    










    public boolean remove(String suffix)
    {
      Character c = Character.valueOf(suffix.charAt(0));
      String newSuffix = suffix.substring(1);
      TrieNode child = (TrieNode)m_Children.get(c);
      boolean result;
      boolean result; if (child == null) {
        result = false;
      } else { boolean result;
        if (newSuffix.length() == 0) {
          remove(c);
          result = true;
        }
        else {
          result = child.remove(newSuffix);
          if (child.getChildCount() == 0)
            remove(child.getChar());
        }
      }
      return result;
    }
    










    public boolean contains(String suffix)
    {
      Character c = Character.valueOf(suffix.charAt(0));
      String newSuffix = suffix.substring(1);
      TrieNode child = (TrieNode)m_Children.get(c);
      boolean result;
      boolean result; if (child == null) {
        result = false; } else { boolean result;
        if (newSuffix.length() == 0) {
          result = true;
        } else
          result = child.contains(newSuffix);
      }
      return result;
    }
    









    public Object clone()
    {
      TrieNode result = new TrieNode(getChar());
      Enumeration<Character> keys = m_Children.keys();
      while (keys.hasMoreElements()) {
        Character key = (Character)keys.nextElement();
        TrieNode child = (TrieNode)((TrieNode)m_Children.get(key)).clone();
        result.add(child);
        m_Children.put(key, child);
      }
      
      return result;
    }
    










    public boolean equals(Object obj)
    {
      TrieNode node = (TrieNode)obj;
      boolean result;
      boolean result;
      if (getChar() == null) {
        result = node.getChar() == null;
      } else {
        result = getChar().equals(node.getChar());
      }
      
      if (result) {
        Enumeration<Character> keys = m_Children.keys();
        while (keys.hasMoreElements()) {
          Character key = (Character)keys.nextElement();
          result = ((TrieNode)m_Children.get(key)).equals(m_Children.get(key));
          if (!result) {
            break;
          }
        }
      }
      return result;
    }
    










    public TrieNode find(String suffix)
    {
      Character c = Character.valueOf(suffix.charAt(0));
      String newSuffix = suffix.substring(1);
      TrieNode child = (TrieNode)m_Children.get(c);
      TrieNode result;
      TrieNode result; if (child == null) {
        result = null; } else { TrieNode result;
        if (newSuffix.length() == 0) {
          result = child;
        } else
          result = child.find(newSuffix);
      }
      return result;
    }
    





    public String getCommonPrefix()
    {
      return getCommonPrefix("");
    }
    




    public String getCommonPrefix(String startPrefix)
    {
      TrieNode startNode;
      


      TrieNode startNode;
      


      if (startPrefix.length() == 0) {
        startNode = this;
      } else
        startNode = find(startPrefix);
      String result;
      String result; if (startNode == null) {
        result = null;
      } else {
        result = startPrefix + startNode.determineCommonPrefix("");
      }
      return result;
    }
    


    protected String determineCommonPrefix(String currentPrefix)
    {
      String newPrefix;
      

      String newPrefix;
      

      if ((!isRoot()) && (getChar() != STOP)) {
        newPrefix = currentPrefix + getChar();
      } else
        newPrefix = currentPrefix;
      String result;
      String result; if (m_Children.size() == 1) {
        result = ((TrieNode)getChildAt(0)).determineCommonPrefix(newPrefix);
      } else {
        result = newPrefix;
      }
      return result;
    }
    







    public int size()
    {
      int result = 0;
      TrieNode leaf = (TrieNode)getFirstLeaf();
      while (leaf != null) {
        if (leaf != getRoot())
          result++;
        leaf = (TrieNode)leaf.getNextLeaf();
      }
      
      return result;
    }
    







    public String getString()
    {
      char[] result = new char[getLevel()];
      TrieNode node = this;
      while ((node.getParent() != null) && 
        (!node.isRoot()))
      {

        result[(node.getLevel() - 1)] = node.getChar().charValue();
        node = (TrieNode)node.getParent();
      }
      
      return new String(result);
    }
    




    public String toString()
    {
      return "" + getChar();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  





  public static class TrieIterator
    implements Iterator<String>, RevisionHandler
  {
    protected Trie.TrieNode m_Root;
    



    protected Trie.TrieNode m_LastLeaf;
    



    protected Trie.TrieNode m_CurrentLeaf;
    




    public TrieIterator(Trie.TrieNode node)
    {
      m_Root = node;
      m_CurrentLeaf = ((Trie.TrieNode)m_Root.getFirstLeaf());
      m_LastLeaf = ((Trie.TrieNode)m_Root.getLastLeaf());
    }
    




    public boolean hasNext()
    {
      return m_CurrentLeaf != null;
    }
    






    public String next()
    {
      String result = m_CurrentLeaf.getString();
      result = result.substring(0, result.length() - 1);
      if (m_CurrentLeaf != m_LastLeaf) {
        m_CurrentLeaf = ((Trie.TrieNode)m_CurrentLeaf.getNextLeaf());
      } else {
        m_CurrentLeaf = null;
      }
      return result;
    }
    




    public void remove() {}
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.2 $");
    }
  }
  














  public Trie()
  {
    m_Root = new TrieNode(null);
    m_RecalcHashCode = true;
  }
  





  public boolean add(String o)
  {
    return m_Root.add(o + TrieNode.STOP);
  }
  







  public boolean addAll(Collection<? extends String> c)
  {
    boolean result = false;
    
    Iterator<String> iter = c.iterator();
    while (iter.hasNext()) {
      result = (add((String)iter.next())) || (result);
    }
    return result;
  }
  


  public void clear()
  {
    m_Root.removeAllChildren();
    m_RecalcHashCode = true;
  }
  






  public Object clone()
  {
    Trie result = new Trie();
    m_Root = ((TrieNode)m_Root.clone());
    
    return result;
  }
  





  public boolean contains(Object o)
  {
    return m_Root.contains((String)o + TrieNode.STOP);
  }
  









  public boolean containsAll(Collection<?> c)
  {
    boolean result = true;
    
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      if (!contains(iter.next())) {
        result = false;
      }
    }
    

    return result;
  }
  





  public boolean containsPrefix(String prefix)
  {
    return m_Root.contains(prefix);
  }
  




  public boolean equals(Object o)
  {
    return m_Root.equals(((Trie)o).getRoot());
  }
  




  public String getCommonPrefix()
  {
    return m_Root.getCommonPrefix();
  }
  




  public TrieNode getRoot()
  {
    return m_Root;
  }
  









  public Vector<String> getWithPrefix(String prefix)
  {
    Vector<String> result = new Vector();
    
    if (containsPrefix(prefix)) {
      TrieNode node = m_Root.find(prefix);
      TrieIterator iter = new TrieIterator(node);
      while (iter.hasNext()) {
        result.add(iter.next());
      }
    }
    return result;
  }
  




  public int hashCode()
  {
    if (m_RecalcHashCode) {
      m_HashCode = toString().hashCode();
      m_RecalcHashCode = false;
    }
    
    return m_HashCode;
  }
  




  public boolean isEmpty()
  {
    return m_Root.getChildCount() == 0;
  }
  




  public Iterator<String> iterator()
  {
    return new TrieIterator(m_Root);
  }
  








  public boolean remove(Object o)
  {
    boolean result = m_Root.remove((String)o + TrieNode.STOP);
    
    m_RecalcHashCode = result;
    
    return result;
  }
  









  public boolean removeAll(Collection<?> c)
  {
    boolean result = false;
    
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      result = (remove(iter.next())) || (result);
    }
    
    m_RecalcHashCode = result;
    
    return result;
  }
  










  public boolean retainAll(Collection<?> c)
  {
    boolean result = false;
    Iterator iter = iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (!c.contains(o)) {
        result = (remove(o)) || (result);
      }
    }
    m_RecalcHashCode = result;
    
    return result;
  }
  




  public int size()
  {
    return m_Root.size();
  }
  




  public Object[] toArray()
  {
    return toArray(new String[0]);
  }
  












  public <T> T[] toArray(T[] a)
  {
    Vector list = new Vector();
    Iterator iter = iterator();
    while (iter.hasNext())
      list.add(iter.next());
    Object[] result;
    Object[] result; if (Array.getLength(a) != list.size()) {
      result = (Object[])Array.newInstance(a.getClass().getComponentType(), list.size());
    } else {
      result = a;
    }
    for (int i = 0; i < list.size(); i++) {
      result[i] = list.get(i);
    }
    return (Object[])result;
  }
  









  protected String toString(TrieNode node)
  {
    StringBuffer result = new StringBuffer();
    

    StringBuffer indentation = new StringBuffer();
    for (int i = 0; i < node.getLevel(); i++)
      indentation.append(" | ");
    result.append(indentation.toString());
    

    if (node.getChar() == null) {
      result.append("<root>");
    } else if (node.getChar() == TrieNode.STOP) {
      result.append("STOP");
    } else
      result.append("'" + node.getChar() + "'");
    result.append("\n");
    

    for (i = 0; i < node.getChildCount(); i++) {
      result.append(toString((TrieNode)node.getChildAt(i)));
    }
    return result.toString();
  }
  




  public String toString()
  {
    return toString(m_Root);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
  



  public static void main(String[] args)
  {
    String[] data;
    


    if (args.length == 0) {
      String[] data = new String[3];
      data[0] = "this is a test";
      data[1] = "this is another test";
      data[2] = "and something else";
    }
    else {
      data = (String[])args.clone();
    }
    

    Trie t = new Trie();
    for (int i = 0; i < data.length; i++)
      t.add(data[i]);
    System.out.println(t);
  }
}
