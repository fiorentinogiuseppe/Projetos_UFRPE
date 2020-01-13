package weka.core.neighboursearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.core.neighboursearch.covertrees.Stack;



























































































public class CoverTree
  extends NearestNeighbourSearch
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 7617412821497807586L;
  protected EuclideanDistance m_EuclideanDistance;
  protected CoverTreeNode m_Root;
  protected double[] m_DistanceList;
  protected int m_NumNodes;
  protected int m_NumLeaves;
  protected int m_MaxDepth;
  protected TreePerformanceStats m_TreeStats;
  protected double m_Base;
  protected double il2;
  
  public class CoverTreeNode
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 1808760031169036512L;
    private int nodeid;
    private Integer idx;
    private double max_dist;
    private double parent_dist;
    private Stack<CoverTreeNode> children;
    private int num_children;
    private int scale;
    
    public CoverTreeNode() {}
    
    public CoverTreeNode(double i, double arg4, Stack<CoverTreeNode> arg6, int childs, int numchilds)
    {
      idx = i;
      max_dist = md;
      parent_dist = pd;
      children = childs;
      num_children = numchilds;
      scale = s;
    }
    


    public Instance p()
    {
      return m_Instances.instance(idx.intValue());
    }
    


    public boolean isALeaf()
    {
      return num_children == 0;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  




  private class DistanceNode
    implements RevisionHandler
  {
    Stack<Double> dist;
    



    Integer idx;
    




    private DistanceNode() {}
    




    public Instance q()
    {
      return m_Instances.instance(idx.intValue());
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  

  public CoverTree()
  {
    if ((m_DistanceFunction instanceof EuclideanDistance)) {
      m_EuclideanDistance = ((EuclideanDistance)m_DistanceFunction);
    } else {
      m_DistanceFunction = (this.m_EuclideanDistance = new EuclideanDistance());
    }
    













    m_TreeStats = null;
    






    m_Base = 1.3D;
    





    il2 = (1.0D / Math.log(m_Base));
    





    if (getMeasurePerformance()) {
      m_Stats = (this.m_TreeStats = new TreePerformanceStats());
    }
  }
  




  public String globalInfo()
  {
    return "Class implementing the CoverTree datastructure.\nThe class is very much a translation of the c source code made available by the authors.\n\nFor more information and original source code see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Alina Beygelzimer and Sham Kakade and John Langford");
    result.setValue(TechnicalInformation.Field.TITLE, "Cover trees for nearest neighbor");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ICML'06: Proceedings of the 23rd international conference on Machine learning");
    result.setValue(TechnicalInformation.Field.PAGES, "97-104");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    result.setValue(TechnicalInformation.Field.ADDRESS, "New York, NY, USA");
    result.setValue(TechnicalInformation.Field.LOCATION, "Pittsburgh, Pennsylvania");
    result.setValue(TechnicalInformation.Field.HTTP, "http://hunch.net/~jl/projects/cover_tree/cover_tree.html");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSet base of the expansion constant\n\t(default = 1.3).", "B", 1, "-B <value>"));
    



    return newVector.elements();
  }
  















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String optionString = Utils.getOption('B', options);
    if (optionString.length() != 0) {
      setBase(Double.parseDouble(optionString));
    } else {
      setBase(1.3D);
    }
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-B");
    result.add("" + getBase());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  protected double dist_of_scale(int s)
  {
    return Math.pow(m_Base, s);
  }
  





  protected int get_scale(double d)
  {
    return (int)Math.ceil(il2 * Math.log(d));
  }
  




  protected CoverTreeNode new_node(Integer idx)
  {
    CoverTreeNode new_node = new CoverTreeNode();
    idx = idx;
    return new_node;
  }
  





  protected CoverTreeNode new_leaf(Integer idx)
  {
    CoverTreeNode new_leaf = new CoverTreeNode(idx, 0.0D, 0.0D, null, 0, 100);
    return new_leaf;
  }
  






  protected double max_set(Stack<DistanceNode> v)
  {
    double max = 0.0D;
    for (int i = 0; i < length; i++) {
      DistanceNode n = (DistanceNode)v.element(i);
      if (max < ((Double)dist.element(dist.length - 1)).floatValue()) {
        max = ((Double)dist.element(dist.length - 1)).floatValue();
      }
    }
    return max;
  }
  















  protected void split(Stack<DistanceNode> point_set, Stack<DistanceNode> far_set, int max_scale)
  {
    int new_index = 0;
    double fmax = dist_of_scale(max_scale);
    for (int i = 0; i < length; i++) {
      DistanceNode n = (DistanceNode)point_set.element(i);
      if (((Double)dist.element(dist.length - 1)).doubleValue() <= fmax) {
        point_set.set(new_index++, point_set.element(i));
      } else
        far_set.push(point_set.element(i));
    }
    List l = new LinkedList();
    for (int i = 0; i < new_index; i++) {
      l.add(point_set.element(i));
    }
    point_set.clear();
    point_set.addAll(l);
  }
  













  protected void dist_split(Stack<DistanceNode> point_set, Stack<DistanceNode> new_point_set, DistanceNode new_point, int max_scale)
  {
    int new_index = 0;
    double fmax = dist_of_scale(max_scale);
    for (int i = 0; i < length; i++) {
      double new_d = Math.sqrt(m_DistanceFunction.distance(new_point.q(), ((DistanceNode)point_set.element(i)).q(), fmax * fmax));
      
      if (new_d <= fmax) {
        elementdist.push(Double.valueOf(new_d));
        new_point_set.push(point_set.element(i));
      } else {
        point_set.set(new_index++, point_set.element(i));
      } }
    List l = new LinkedList();
    for (int i = 0; i < new_index; i++)
      l.add(point_set.element(i));
    point_set.clear();
    point_set.addAll(l);
  }
  


























  protected CoverTreeNode batch_insert(Integer p, int max_scale, int top_scale, Stack<DistanceNode> point_set, Stack<DistanceNode> consumed_set)
  {
    if (length == 0) {
      CoverTreeNode leaf = new_leaf(p);
      nodeid = m_NumNodes;
      m_NumNodes += 1;
      m_NumLeaves += 1;
      return leaf;
    }
    double max_dist = max_set(point_set);
    
    int next_scale = Math.min(max_scale - 1, get_scale(max_dist));
    if (next_scale == Integer.MIN_VALUE)
    {
      Stack<CoverTreeNode> children = new Stack();
      CoverTreeNode leaf = new_leaf(p);
      nodeid = m_NumNodes;
      children.push(leaf);
      m_NumLeaves += 1;
      m_NumNodes += 1;
      while (length > 0) {
        DistanceNode tmpnode = (DistanceNode)point_set.pop();
        leaf = new_leaf(idx);
        nodeid = m_NumNodes;
        children.push(leaf);
        m_NumLeaves += 1;
        m_NumNodes += 1;
        consumed_set.push(tmpnode);
      }
      CoverTreeNode n = new_node(p);
      
      nodeid = m_NumNodes;
      m_NumNodes += 1;
      scale = 100;
      max_dist = 0.0D;
      num_children = length;
      children = children;
      return n;
    }
    Stack<DistanceNode> far = new Stack();
    split(point_set, far, max_scale);
    
    CoverTreeNode child = batch_insert(p, next_scale, top_scale, point_set, consumed_set);
    

    if (length == 0)
    {

      point_set.replaceAllBy(far);
      return child;
    }
    CoverTreeNode n = new_node(p);
    nodeid = m_NumNodes;
    m_NumNodes += 1;
    Stack<CoverTreeNode> children = new Stack();
    children.push(child);
    
    while (length != 0) {
      Stack<DistanceNode> new_point_set = new Stack();
      Stack<DistanceNode> new_consumed_set = new Stack();
      DistanceNode tmpnode = (DistanceNode)point_set.pop();
      double new_dist = ((Double)dist.last()).doubleValue();
      consumed_set.push(tmpnode);
      


      dist_split(point_set, new_point_set, tmpnode, max_scale);
      

      dist_split(far, new_point_set, tmpnode, max_scale);
      
      CoverTreeNode new_child = batch_insert(idx, next_scale, top_scale, new_point_set, new_consumed_set);
      
      parent_dist = new_dist;
      
      children.push(new_child);
      


      double fmax = dist_of_scale(max_scale);
      tmpnode = null;
      for (int i = 0; i < length; i++) {
        tmpnode = (DistanceNode)new_point_set.element(i);
        dist.pop();
        if (((Double)dist.last()).doubleValue() <= fmax) {
          point_set.push(tmpnode);
        } else {
          far.push(tmpnode);
        }
      }
      
      tmpnode = null;
      for (int i = 0; i < length; i++) {
        tmpnode = (DistanceNode)new_consumed_set.element(i);
        dist.pop();
        consumed_set.push(tmpnode);
      }
    }
    point_set.replaceAllBy(far);
    scale = (top_scale - max_scale);
    max_dist = max_set(consumed_set);
    num_children = length;
    children = children;
    return n;
  }
  











  protected void buildCoverTree(Instances insts)
    throws Exception
  {
    if (insts.numInstances() == 0) {
      throw new Exception("CoverTree: Empty set of instances. Cannot build tree.");
    }
    checkMissing(insts);
    if (m_EuclideanDistance == null) {
      m_DistanceFunction = (this.m_EuclideanDistance = new EuclideanDistance(insts));
    } else {
      m_EuclideanDistance.setInstances(insts);
    }
    Stack<DistanceNode> point_set = new Stack();
    Stack<DistanceNode> consumed_set = new Stack();
    
    Instance point_p = insts.instance(0);int p_idx = 0;
    double max_dist = -1.0D;double dist = 0.0D;Instance max_q = point_p;
    
    for (int i = 1; i < insts.numInstances(); i++) {
      DistanceNode temp = new DistanceNode(null);
      dist = new Stack();
      dist = Math.sqrt(m_DistanceFunction.distance(point_p, insts.instance(i), Double.POSITIVE_INFINITY));
      if (dist > max_dist) {
        max_dist = dist;max_q = insts.instance(i);
      }
      dist.push(Double.valueOf(dist));
      idx = Integer.valueOf(i);
      point_set.push(temp);
    }
    
    max_dist = max_set(point_set);
    m_Root = batch_insert(Integer.valueOf(p_idx), get_scale(max_dist), get_scale(max_dist), point_set, consumed_set);
  }
  













  protected class MyHeap
    implements RevisionHandler
  {
    CoverTree.MyHeapElement[] m_heap = null;
    



    public MyHeap(int maxSize)
    {
      if (maxSize % 2 == 0) {
        maxSize++;
      }
      m_heap = new CoverTree.MyHeapElement[maxSize + 1];
      m_heap[0] = new CoverTree.MyHeapElement(CoverTree.this, -1.0D);
    }
    



    public int size()
    {
      return m_heap[0].index;
    }
    



    public CoverTree.MyHeapElement peek()
    {
      return m_heap[1];
    }
    



    public CoverTree.MyHeapElement get()
      throws Exception
    {
      if (m_heap[0].index == 0)
        throw new Exception("No elements present in the heap");
      CoverTree.MyHeapElement r = m_heap[1];
      m_heap[1] = m_heap[m_heap[0].index];
      m_heap[0].index -= 1;
      downheap();
      return r;
    }
    




    public void put(double d)
      throws Exception
    {
      if (m_heap[0].index + 1 > m_heap.length - 1) {
        throw new Exception("the number of elements cannot exceed the initially set maximum limit");
      }
      m_heap[0].index += 1;
      m_heap[m_heap[0].index] = new CoverTree.MyHeapElement(CoverTree.this, d);
      upheap();
    }
    






    public void putBySubstitute(double d)
      throws Exception
    {
      CoverTree.MyHeapElement head = get();
      put(d);
      if (distance == m_heap[1].distance) {
        putKthNearest(distance);
      }
      else if (distance > m_heap[1].distance) {
        m_KthNearest = null;
        m_KthNearestSize = 0;
        initSize = 10;
      }
      else if (distance < m_heap[1].distance) {
        throw new Exception("The substituted element is greater than the head element. put() should have been called in place of putBySubstitute()");
      }
    }
    



    CoverTree.MyHeapElement[] m_KthNearest = null;
    

    int m_KthNearestSize = 0;
    

    int initSize = 10;
    





    public int noOfKthNearest()
    {
      return m_KthNearestSize;
    }
    




    public void putKthNearest(double d)
    {
      if (m_KthNearest == null) {
        m_KthNearest = new CoverTree.MyHeapElement[initSize];
      }
      if (m_KthNearestSize >= m_KthNearest.length) {
        initSize += initSize;
        CoverTree.MyHeapElement[] temp = new CoverTree.MyHeapElement[initSize];
        System.arraycopy(m_KthNearest, 0, temp, 0, m_KthNearest.length);
        m_KthNearest = temp;
      }
      m_KthNearest[(m_KthNearestSize++)] = new CoverTree.MyHeapElement(CoverTree.this, d);
    }
    




    public CoverTree.MyHeapElement getKthNearest()
    {
      if (m_KthNearestSize == 0)
        return null;
      m_KthNearestSize -= 1;
      return m_KthNearest[m_KthNearestSize];
    }
    



    protected void upheap()
    {
      int i = m_heap[0].index;
      
      while ((i > 1) && (m_heap[i].distance > m_heap[(i / 2)].distance)) {
        CoverTree.MyHeapElement temp = m_heap[i];
        m_heap[i] = m_heap[(i / 2)];
        i /= 2;
        m_heap[i] = temp;
      }
    }
    



    protected void downheap()
    {
      int i = 1;
      


      while (((2 * i <= m_heap[0].index) && (m_heap[i].distance < m_heap[(2 * i)].distance)) || ((2 * i + 1 <= m_heap[0].index) && (m_heap[i].distance < m_heap[(2 * i + 1)].distance)))
      {

        if (2 * i + 1 <= m_heap[0].index) {
          if (m_heap[(2 * i)].distance > m_heap[(2 * i + 1)].distance) {
            CoverTree.MyHeapElement temp = m_heap[i];
            m_heap[i] = m_heap[(2 * i)];
            i = 2 * i;
            m_heap[i] = temp;
          }
          else {
            CoverTree.MyHeapElement temp = m_heap[i];
            m_heap[i] = m_heap[(2 * i + 1)];
            i = 2 * i + 1;
            m_heap[i] = temp;
          }
        }
        else {
          CoverTree.MyHeapElement temp = m_heap[i];
          m_heap[i] = m_heap[(2 * i)];
          i = 2 * i;
          m_heap[i] = temp;
        }
      }
    }
    




    public int totalSize()
    {
      return size() + noOfKthNearest();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  






  protected class MyHeapElement
    implements RevisionHandler
  {
    public double distance;
    





    int index = 0;
    




    public MyHeapElement(double d)
    {
      distance = d;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  





  private class d_node
    implements RevisionHandler
  {
    double dist;
    



    CoverTree.CoverTreeNode n;
    




    public d_node(double d, CoverTree.CoverTreeNode node)
    {
      dist = d;
      n = node;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  









  protected void setter(MyHeap heap, double upper_bound, int k)
    throws Exception
  {
    if (heap.size() > 0) {
      m_heap[0].index = 0;
    }
    while (heap.size() < k) {
      heap.put(upper_bound);
    }
  }
  






  protected void update(MyHeap upper_bound, double new_bound)
    throws Exception
  {
    upper_bound.putBySubstitute(new_bound);
  }
  













  protected Stack<d_node> getCoverSet(int idx, Stack<Stack<d_node>> cover_sets)
  {
    if (length <= idx) {
      int i = length - 1;
      while (i < idx) {
        i++;
        Stack<d_node> new_cover_set = new Stack();
        cover_sets.push(new_cover_set);
      }
    }
    return (Stack)cover_sets.element(idx);
  }
  


















  protected void copy_zero_set(CoverTreeNode query_chi, MyHeap new_upper_k, Stack<d_node> zero_set, Stack<d_node> new_zero_set)
    throws Exception
  {
    new_zero_set.clear();
    
    for (int i = 0; i < length; i++) {
      d_node ele = (d_node)zero_set.element(i);
      double upper_dist = peekdistance + max_dist;
      if (shell(dist, parent_dist, upper_dist)) {
        double d = Math.sqrt(m_DistanceFunction.distance(query_chi.p(), n.p(), upper_dist * upper_dist));
        
        if (m_TreeStats != null)
          m_TreeStats.incrPointCount();
        if (d <= upper_dist) {
          if (d < peekdistance)
            update(new_upper_k, d);
          d_node temp = new d_node(d, n);
          new_zero_set.push(temp);
          if (m_TreeStats != null) {
            m_TreeStats.incrLeafCount();
          }
        }
      }
    }
  }
  























  protected void copy_cover_sets(CoverTreeNode query_chi, MyHeap new_upper_k, Stack<Stack<d_node>> cover_sets, Stack<Stack<d_node>> new_cover_sets, int current_scale, int max_scale)
    throws Exception
  {
    new_cover_sets.clear();
    for (; current_scale <= max_scale; current_scale++)
    {
      Stack<d_node> cover_set_currentscale = getCoverSet(current_scale, cover_sets);
      
      for (int i = 0; i < length; i++)
      {
        d_node ele = (d_node)cover_set_currentscale.element(i);
        double upper_dist = peekdistance + max_dist + n.max_dist;
        
        if (shell(dist, parent_dist, upper_dist)) {
          double d = Math.sqrt(m_DistanceFunction.distance(query_chi.p(), n.p(), upper_dist * upper_dist));
          
          if (m_TreeStats != null)
            m_TreeStats.incrPointCount();
          if (d <= upper_dist) {
            if (d < peekdistance)
              update(new_upper_k, d);
            d_node temp = new d_node(d, n);
            ((Stack)new_cover_sets.element(current_scale)).push(temp);
            if (m_TreeStats != null) {
              m_TreeStats.incrIntNodeCount();
            }
          }
        }
      }
    }
  }
  











  void print_cover_sets(Stack<Stack<d_node>> cover_sets, Stack<d_node> zero_set, int current_scale, int max_scale)
  {
    println("cover set = ");
    for (; current_scale <= max_scale; current_scale++) {
      println("" + current_scale);
      for (int i = 0; i < elementlength; i++) {
        d_node ele = (d_node)((Stack)cover_sets.element(current_scale)).element(i);
        CoverTreeNode n = n;
        println(n.p());
      }
    }
    println("infinity");
    for (int i = 0; i < length; i++) {
      d_node ele = (d_node)zero_set.element(i);
      CoverTreeNode n = n;
      println(n.p());
    }
  }
  








  protected void SWAP(int a, int b, Stack<d_node> cover_set)
  {
    d_node tmp = (d_node)cover_set.element(a);
    cover_set.set(a, cover_set.element(b));
    cover_set.set(b, tmp);
  }
  












  protected double compare(int p1, int p2, Stack<d_node> cover_set)
  {
    return elementdist - elementdist;
  }
  





  protected void halfsort(Stack<d_node> cover_set)
  {
    if (length <= 1)
      return;
    int start = 0;
    int hi = length - 1;
    int right = hi;
    

    while (right > start) {
      int mid = start + (hi - start >> 1);
      
      boolean jumpover = false;
      if (compare(mid, start, cover_set) < 0.0D)
        SWAP(mid, start, cover_set);
      if (compare(hi, mid, cover_set) < 0.0D) {
        SWAP(mid, hi, cover_set);
      } else
        jumpover = true;
      if ((!jumpover) && (compare(mid, start, cover_set) < 0.0D)) {
        SWAP(mid, start, cover_set);
      }
      

      int left = start + 1;
      right = hi - 1;
      do
      {
        while (compare(left, mid, cover_set) < 0.0D) {
          left++;
        }
        while (compare(mid, right, cover_set) < 0.0D) {
          right--;
        }
        if (left < right) {
          SWAP(left, right, cover_set);
          if (mid == left) {
            mid = right;
          } else if (mid == right)
            mid = left;
          left++;
          right--;
        } else if (left == right) {
          left++;
          right--;
          break;
        }
      } while (left <= right);
      hi = right;
    }
  }
  










  protected boolean shell(double parent_query_dist, double child_parent_dist, double upper_bound)
  {
    return parent_query_dist - child_parent_dist <= upper_bound;
  }
  




































  protected int descend(CoverTreeNode query, MyHeap upper_k, int current_scale, int max_scale, Stack<Stack<d_node>> cover_sets, Stack<d_node> zero_set)
    throws Exception
  {
    Stack<d_node> cover_set_currentscale = getCoverSet(current_scale, cover_sets);
    
    for (int i = 0; i < length; i++) {
      d_node parent = (d_node)cover_set_currentscale.element(i);
      CoverTreeNode par = n;
      double upper_dist = peekdistance + max_dist + max_dist;
      
      if (dist <= upper_dist + max_dist) { CoverTreeNode chi;
        CoverTreeNode chi;
        if ((par == m_Root) && (num_children == 0))
        {

          chi = par;
        } else
          chi = (CoverTreeNode)children.element(0);
        if (dist <= upper_dist + max_dist)
        {

          if (num_children > 0) {
            if (max_scale < scale) {
              max_scale = scale;
            }
            d_node temp = new d_node(dist, chi);
            getCoverSet(scale, cover_sets).push(temp);
            if (m_TreeStats != null)
              m_TreeStats.incrIntNodeCount();
          } else if (dist <= upper_dist) {
            d_node temp = new d_node(dist, chi);
            zero_set.push(temp);
            if (m_TreeStats != null)
              m_TreeStats.incrLeafCount();
          }
        }
        for (int c = 1; c < num_children; c++) {
          chi = (CoverTreeNode)children.element(c);
          double upper_chi = peekdistance + max_dist + max_dist + max_dist;
          


          if (shell(dist, parent_dist, upper_chi))
          {




            double d = Math.sqrt(m_DistanceFunction.distance(query.p(), chi.p(), upper_chi * upper_chi, m_TreeStats));
            
            if (m_TreeStats != null)
              m_TreeStats.incrPointCount();
            if (d <= upper_chi) {
              if (d < peekdistance)
                update(upper_k, d);
              if (num_children > 0) {
                if (max_scale < scale) {
                  max_scale = scale;
                }
                d_node temp = new d_node(d, chi);
                getCoverSet(scale, cover_sets).push(temp);
                if (m_TreeStats != null)
                  m_TreeStats.incrIntNodeCount();
              } else if (d <= upper_chi - max_dist) {
                d_node temp = new d_node(d, chi);
                zero_set.push(temp);
                if (m_TreeStats != null)
                  m_TreeStats.incrLeafCount();
              }
            }
          }
        }
      }
    }
    return max_scale;
  }
  

















  protected void brute_nearest(int k, CoverTreeNode query, Stack<d_node> zero_set, MyHeap upper_k, Stack<NearestNeighbourSearch.NeighborList> results)
    throws Exception
  {
    if (num_children > 0) {
      Stack<d_node> new_zero_set = new Stack();
      CoverTreeNode query_chi = (CoverTreeNode)children.element(0);
      brute_nearest(k, query_chi, zero_set, upper_k, results);
      MyHeap new_upper_k = new MyHeap(k);
      
      for (int i = 1; i < children.length; i++) {
        query_chi = (CoverTreeNode)children.element(i);
        setter(new_upper_k, peekdistance + parent_dist, k);
        copy_zero_set(query_chi, new_upper_k, zero_set, new_zero_set);
        brute_nearest(k, query_chi, new_zero_set, new_upper_k, results);
      }
    } else {
      NearestNeighbourSearch.NeighborList temp = new NearestNeighbourSearch.NeighborList(this, k);
      
      for (int i = 0; i < length; i++) {
        d_node ele = (d_node)zero_set.element(i);
        if (dist <= peekdistance) {
          temp.insertSorted(dist, n.p());
        }
      }
      results.push(temp);
    }
  }
  

























  protected void internal_batch_nearest_neighbor(int k, CoverTreeNode query_node, Stack<Stack<d_node>> cover_sets, Stack<d_node> zero_set, int current_scale, int max_scale, MyHeap upper_k, Stack<NearestNeighbourSearch.NeighborList> results)
    throws Exception
  {
    if (current_scale > max_scale) {
      brute_nearest(k, query_node, zero_set, upper_k, results);

    }
    else if ((scale <= current_scale) && (scale != 100))
    {
      Stack<d_node> new_zero_set = new Stack();
      Stack<Stack<d_node>> new_cover_sets = new Stack();
      MyHeap new_upper_k = new MyHeap(k);
      
      for (int i = 1; i < num_children; i++) {
        CoverTreeNode query_chi = (CoverTreeNode)children.element(i);
        setter(new_upper_k, peekdistance + parent_dist, k);
        
        copy_zero_set(query_chi, new_upper_k, zero_set, new_zero_set);
        

        copy_cover_sets(query_chi, new_upper_k, cover_sets, new_cover_sets, current_scale, max_scale);
        

        internal_batch_nearest_neighbor(k, query_chi, new_cover_sets, new_zero_set, current_scale, max_scale, new_upper_k, results);
      }
      
      new_cover_sets = null;
      new_zero_set = null;
      new_upper_k = null;
      

      internal_batch_nearest_neighbor(k, (CoverTreeNode)children.element(0), cover_sets, zero_set, current_scale, max_scale, upper_k, results);
    }
    else {
      Stack<d_node> cover_set_i = getCoverSet(current_scale, cover_sets);
      
      halfsort(cover_set_i);
      max_scale = descend(query_node, upper_k, current_scale, max_scale, cover_sets, zero_set);
      
      cover_set_i.clear();
      current_scale++;
      internal_batch_nearest_neighbor(k, query_node, cover_sets, zero_set, current_scale, max_scale, upper_k, results);
    }
  }
  














  protected void batch_nearest_neighbor(int k, CoverTreeNode tree_root, CoverTreeNode query_root, Stack<NearestNeighbourSearch.NeighborList> results)
    throws Exception
  {
    Stack<Stack<d_node>> cover_sets = new Stack(100);
    
    Stack<d_node> zero_set = new Stack();
    MyHeap upper_k = new MyHeap(k);
    
    setter(upper_k, Double.POSITIVE_INFINITY, k);
    

    double treeroot_to_query_dist = Math.sqrt(m_DistanceFunction.distance(query_root.p(), tree_root.p(), Double.POSITIVE_INFINITY));
    


    update(upper_k, treeroot_to_query_dist);
    
    d_node temp = new d_node(treeroot_to_query_dist, tree_root);
    getCoverSet(0, cover_sets).push(temp);
    

    if (m_TreeStats != null) {
      m_TreeStats.incrPointCount();
      if (num_children > 0) {
        m_TreeStats.incrIntNodeCount();
      } else {
        m_TreeStats.incrLeafCount();
      }
    }
    internal_batch_nearest_neighbor(k, query_root, cover_sets, zero_set, 0, 0, upper_k, results);
  }
  








  protected NearestNeighbourSearch.NeighborList findKNearest(Instance target, int k)
    throws Exception
  {
    Stack<d_node> cover_set_current = new Stack();
    
    Stack<d_node> zero_set = new Stack();
    
    MyHeap upper_k = new MyHeap(k);
    double d = Math.sqrt(m_DistanceFunction.distance(m_Root.p(), target, Double.POSITIVE_INFINITY, m_TreeStats));
    
    cover_set_current.push(new d_node(d, m_Root));
    setter(upper_k, Double.POSITIVE_INFINITY, k);
    update(upper_k, d);
    
    if (m_TreeStats != null) {
      if (m_Root.num_children > 0) {
        m_TreeStats.incrIntNodeCount();
      } else
        m_TreeStats.incrLeafCount();
      m_TreeStats.incrPointCount();
    }
    

    if (m_Root.num_children == 0) {
      NearestNeighbourSearch.NeighborList list = new NearestNeighbourSearch.NeighborList(this, k);
      list.insertSorted(d, m_Root.p());
      return list;
    }
    
    while (length > 0) {
      Stack<d_node> cover_set_next = new Stack();
      for (int i = 0; i < length; i++) {
        d_node par = (d_node)cover_set_current.element(i);
        CoverTreeNode parent = n;
        for (int c = 0; c < num_children; c++) {
          CoverTreeNode child = (CoverTreeNode)children.element(c);
          double upper_bound = peekdistance;
          if (c == 0) {
            d = dist;
          } else {
            d = upper_bound + max_dist;
            d = Math.sqrt(m_DistanceFunction.distance(child.p(), target, d * d, m_TreeStats));
            if (m_TreeStats != null)
              m_TreeStats.incrPointCount();
          }
          if (d <= upper_bound + max_dist) {
            if ((c > 0) && (d < upper_bound)) {
              update(upper_k, d);
            }
            if (num_children > 0) {
              cover_set_next.push(new d_node(d, child));
              if (m_TreeStats != null) {
                m_TreeStats.incrIntNodeCount();
              }
            } else if (d <= upper_bound) {
              zero_set.push(new d_node(d, child));
              if (m_TreeStats != null)
                m_TreeStats.incrLeafCount();
            }
          }
        }
      }
      cover_set_current = cover_set_next;
    }
    
    NearestNeighbourSearch.NeighborList list = new NearestNeighbourSearch.NeighborList(this, k);
    
    double upper_bound = peekdistance;
    for (int i = 0; i < length; i++) {
      d_node tmpnode = (d_node)zero_set.element(i);
      if (dist <= upper_bound) {
        list.insertSorted(dist, n.p());
      }
    }
    if (list.currentLength() <= 0) {
      throw new Exception("Error: No neighbour found. This cannot happen");
    }
    return list;
  }
  











  public Instances kNearestNeighbours(Instance target, int k)
    throws Exception
  {
    if (m_Stats != null)
      m_Stats.searchStart();
    CoverTree querytree = new CoverTree();
    Instances insts = new Instances(m_Instances, 0);
    insts.add(target);
    querytree.setInstances(insts);
    Stack<NearestNeighbourSearch.NeighborList> result = new Stack();
    batch_nearest_neighbor(k, m_Root, m_Root, result);
    if (m_Stats != null) {
      m_Stats.searchFinish();
    }
    insts = new Instances(m_Instances, 0);
    NearestNeighbourSearch.NeighborNode node = ((NearestNeighbourSearch.NeighborList)result.element(0)).getFirst();
    m_DistanceList = new double[((NearestNeighbourSearch.NeighborList)result.element(0)).currentLength()];
    int i = 0;
    for (; node != null; 
        

        node = m_Next)
    {
      insts.add(m_Instance);
      m_DistanceList[i] = m_Distance;
      i++;
    }
    return insts;
  }
  







  public Instance nearestNeighbour(Instance target)
    throws Exception
  {
    return kNearestNeighbours(target, 1).instance(0);
  }
  







  public double[] getDistances()
    throws Exception
  {
    if ((m_Instances == null) || (m_DistanceList == null)) {
      throw new Exception("The tree has not been supplied with a set of instances or getDistances() has been called before calling kNearestNeighbours().");
    }
    
    return m_DistanceList;
  }
  





  protected void checkMissing(Instances instances)
    throws Exception
  {
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance ins = instances.instance(i);
      for (int j = 0; j < ins.numValues(); j++) {
        if ((ins.index(j) != ins.classIndex()) && 
          (ins.isMissingSparse(j))) {
          throw new Exception("ERROR: KDTree can not deal with missing values. Please run ReplaceMissingValues filter on the dataset before passing it on to the KDTree.");
        }
      }
    }
  }
  








  public void setInstances(Instances instances)
    throws Exception
  {
    super.setInstances(instances);
    buildCoverTree(instances);
  }
  








  public void update(Instance ins)
    throws Exception
  {
    throw new Exception("BottomUpConstruction method does not allow addition of new Instances.");
  }
  









  public void addInstanceInfo(Instance ins)
  {
    if (m_Instances != null) {
      try {
        m_DistanceFunction.update(ins);
      } catch (Exception ex) { ex.printStackTrace();
      }
      
    } else if (m_Instances == null) {
      throw new IllegalStateException("No instances supplied yet. Cannot update withoutsupplying a set of instances first.");
    }
  }
  





  public void setDistanceFunction(DistanceFunction df)
    throws Exception
  {
    if (!(df instanceof EuclideanDistance)) {
      throw new Exception("CoverTree currently only works with EuclideanDistanceFunction.");
    }
    m_DistanceFunction = (this.m_EuclideanDistance = (EuclideanDistance)df);
  }
  





  public String baseTipText()
  {
    return "The base for the expansion constant.";
  }
  




  public double getBase()
  {
    return m_Base;
  }
  





  public void setBase(double b)
  {
    m_Base = b;
  }
  





  public double measureTreeSize()
  {
    return m_NumNodes;
  }
  




  public double measureNumLeaves()
  {
    return m_NumLeaves;
  }
  




  public double measureMaxDepth()
  {
    return m_MaxDepth;
  }
  




  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureMaxDepth");
    Enumeration e; if (m_Stats != null) {
      for (e = m_Stats.enumerateMeasures(); e.hasMoreElements();) {
        newVector.addElement(e.nextElement());
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.compareToIgnoreCase("measureMaxDepth") == 0)
      return measureMaxDepth();
    if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0)
      return measureTreeSize();
    if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0)
      return measureNumLeaves();
    if (m_Stats != null) {
      return m_Stats.getMeasure(additionalMeasureName);
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (KDTree)");
  }
  







  protected static void print(String s)
  {
    System.out.print(s);
  }
  





  protected static void println(String s)
  {
    System.out.println(s);
  }
  




  protected static void print(Object o)
  {
    System.out.print(o);
  }
  





  protected static void println(Object o)
  {
    System.out.println(o);
  }
  




  protected static void print_space(int s)
  {
    for (int i = 0; i < s; i++) {
      System.out.print(" ");
    }
  }
  




  protected static void print(int depth, CoverTreeNode top_node)
  {
    print_space(depth);
    println(top_node.p());
    if (num_children > 0) {
      print_space(depth);
      print("scale = " + scale + "\n");
      print_space(depth);
      print("num children = " + num_children + "\n");
      System.out.flush();
      for (int i = 0; i < num_children; i++) {
        print(depth + 1, (CoverTreeNode)children.element(i));
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  




  public static void main(String[] args)
  {
    if (args.length != 1) {
      System.err.println("Usage: CoverTree <ARFF file>");
      System.exit(-1);
    }
    try {
      Instances insts = null;
      if (args[0].endsWith(".csv")) {
        CSVLoader csv = new CSVLoader();
        csv.setFile(new File(args[0]));
        insts = csv.getDataSet();
      } else {
        insts = new Instances(new BufferedReader(new FileReader(args[0])));
      }
      
      CoverTree tree = new CoverTree();
      tree.setInstances(insts);
      print("Created data tree:\n");
      print(0, m_Root);
      println("");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
