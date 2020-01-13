package weka.classifiers.trees;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.io.Serializable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericObjectEditor.GOEPanel;
import weka.gui.PropertyDialog;
import weka.gui.treevisualizer.PlaceNode1;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeDisplayEvent;
import weka.gui.treevisualizer.TreeDisplayListener;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.VisualizePanel;
import weka.gui.visualize.VisualizePanelEvent;
import weka.gui.visualize.VisualizePanelListener;





































































public class UserClassifier
  extends Classifier
  implements Drawable, TreeDisplayListener, VisualizePanelListener, TechnicalInformationHandler
{
  static final long serialVersionUID = 6483901103562809843L;
  private static final int LEAF = 0;
  private static final int RECTANGLE = 1;
  private static final int POLYGON = 2;
  private static final int POLYLINE = 3;
  private static final int VLINE = 5;
  private static final int HLINE = 6;
  private transient TreeVisualizer m_tView = null;
  
  private transient VisualizePanel m_iView = null;
  
  private TreeClass m_top;
  
  private TreeClass m_focus;
  
  private int m_nextId;
  
  private transient JTabbedPane m_reps;
  private transient JFrame m_mainWin;
  private boolean m_built = false;
  



  private GenericObjectEditor m_classifiers;
  



  private PropertyDialog m_propertyDialog;
  



  public static void main(String[] argv)
  {
    runClassifier(new UserClassifier(), argv);
  }
  


  public String toString()
  {
    if (!m_built)
    {
      return "Tree Not Built";
    }
    StringBuffer text = new StringBuffer();
    try {
      m_top.toString(0, text);
      
      m_top.objectStrings(text);
    }
    catch (Exception e) {
      System.out.println("error: " + e.getMessage());
    }
    
    return text.toString();
  }
  





  public void userCommand(TreeDisplayEvent e)
  {
    if (m_propertyDialog != null) {
      m_propertyDialog.dispose();
      m_propertyDialog = null;
    }
    try {
      if (((m_iView == null) || (m_tView != null)) || 
      

        (e.getCommand() != 0))
      {

        if (e.getCommand() == 1)
        {
          if (m_top == null)
          {

            System.out.println("Error : Received event from a TreeDisplayer that is unknown to the classifier.");
          }
          else
          {
            m_tView.setHighlight(e.getID());
            




            m_focus = m_top.getNode(e.getID());
            m_iView.setInstances(m_focus.m_training);
            if (m_focus.m_attrib1 >= 0) {
              m_iView.setXIndex(m_focus.m_attrib1);
            }
            if (m_focus.m_attrib2 >= 0) {
              m_iView.setYIndex(m_focus.m_attrib2);
            }
            m_iView.setColourIndex(m_focus.m_training.classIndex());
            if (((Double)((FastVector)m_focus.m_ranges.elementAt(0)).elementAt(0)).intValue() != 0)
            {
              m_iView.setShapes(m_focus.m_ranges);
            }
            
          }
        }
        else if (e.getCommand() == 2)
        {




          m_focus = m_top.getNode(e.getID());
          m_iView.setInstances(m_focus.m_training);
          if (m_focus.m_attrib1 >= 0) {
            m_iView.setXIndex(m_focus.m_attrib1);
          }
          if (m_focus.m_attrib2 >= 0) {
            m_iView.setYIndex(m_focus.m_attrib2);
          }
          m_iView.setColourIndex(m_focus.m_training.classIndex());
          if (((Double)((FastVector)m_focus.m_ranges.elementAt(0)).elementAt(0)).intValue() != 0)
          {
            m_iView.setShapes(m_focus.m_ranges);
          }
          

          m_focus.m_set1 = null;
          m_focus.m_set2 = null;
          m_focus.setInfo(m_focus.m_attrib1, m_focus.m_attrib2, null);
          
          m_tView = new TreeVisualizer(this, graph(), new PlaceNode2());
          
          m_reps.setComponentAt(0, m_tView);
          
          m_tView.setHighlight(m_focus.m_identity);
        }
        else if (e.getCommand() == 4)
        {




          m_focus = m_top.getNode(e.getID());
          m_iView.setInstances(m_focus.m_training);
          if (m_focus.m_attrib1 >= 0) {
            m_iView.setXIndex(m_focus.m_attrib1);
          }
          if (m_focus.m_attrib2 >= 0) {
            m_iView.setYIndex(m_focus.m_attrib2);
          }
          m_iView.setColourIndex(m_focus.m_training.classIndex());
          if (((Double)((FastVector)m_focus.m_ranges.elementAt(0)).elementAt(0)).intValue() != 0)
          {
            m_iView.setShapes(m_focus.m_ranges);
          }
          
          Classifier classifierAtNode = m_focus.getClassifier();
          if (classifierAtNode != null) {
            m_classifiers.setValue(classifierAtNode);
          }
          m_propertyDialog = new PropertyDialog((Frame)null, m_classifiers, m_mainWin.getLocationOnScreen().x, m_mainWin.getLocationOnScreen().y);
          

          m_propertyDialog.setVisible(true);
          













          m_tView.setHighlight(m_focus.m_identity);




        }
        else if (e.getCommand() == 3)
        {
          int well = JOptionPane.showConfirmDialog(m_mainWin, "Are You Sure...\nClick Yes To Accept The Tree\n Click No To Return", "Accept Tree", 0);
          






          if (well == 0) {
            m_mainWin.setDefaultCloseOperation(2);
            m_mainWin.dispose();
            blocker(false);
          }
        }
      }
    }
    catch (Exception er) {
      System.out.println("Error : " + er);
      System.out.println("Part of user input so had to catch here");
      er.printStackTrace();
    }
  }
  





  public void userDataEvent(VisualizePanelEvent e)
  {
    if (m_propertyDialog != null) {
      m_propertyDialog.dispose();
      m_propertyDialog = null;
    }
    try
    {
      if (m_focus != null)
      {

        double wdom = e.getInstances1().numInstances() + e.getInstances2().numInstances();
        
        if (wdom == 0.0D) {
          wdom = 1.0D;
        }
        
        TreeClass tmp = m_focus;
        m_focus.m_set1 = new TreeClass(null, e.getAttribute1(), e.getAttribute2(), m_nextId, e.getInstances1().numInstances() / wdom, e.getInstances1(), m_focus);
        



        m_focus.m_set2 = new TreeClass(null, e.getAttribute1(), e.getAttribute2(), m_nextId, e.getInstances2().numInstances() / wdom, e.getInstances2(), m_focus);
        






        m_focus.setInfo(e.getAttribute1(), e.getAttribute2(), e.getValues());
        
        m_tView = new TreeVisualizer(this, graph(), new PlaceNode2());
        

        m_reps.setComponentAt(0, m_tView);
        
        m_focus = m_focus.m_set2;
        m_tView.setHighlight(m_focus.m_identity);
        m_iView.setInstances(m_focus.m_training);
        if (m_attrib1 >= 0) {
          m_iView.setXIndex(m_attrib1);
        }
        if (m_attrib2 >= 0) {
          m_iView.setYIndex(m_attrib2);
        }
        m_iView.setColourIndex(m_focus.m_training.classIndex());
        if (((Double)((FastVector)m_focus.m_ranges.elementAt(0)).elementAt(0)).intValue() != 0)
        {
          m_iView.setShapes(m_focus.m_ranges);
        }
      }
      else
      {
        System.out.println("Somehow the focus is null");
      }
    } catch (Exception er) {
      System.out.println("Error : " + er);
      System.out.println("Part of user input so had to catch here");
    }
  }
  





  public UserClassifier()
  {
    m_top = null;
    m_tView = null;
    m_iView = null;
    m_nextId = 0;
  }
  





  public int graphType()
  {
    return 1;
  }
  




  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    text.append("digraph UserClassifierTree {\nnode [fontsize=10]\nedge [fontsize=10 style=bold]\n");
    


    m_top.toDotty(text);
    return text.toString() + "}\n";
  }
  







  private synchronized void blocker(boolean tf)
  {
    if (tf) {
      try {
        wait();

      }
      catch (InterruptedException e) {}
    } else {
      notifyAll();
    }
  }
  






  public String globalInfo()
  {
    return "Interactively classify through visual means. You are Presented with a scatter graph of the data against two user selectable attributes, as well as a view of the decision tree. You can create binary splits by creating polygons around data plotted on the scatter graph, as well as by allowing another classifier to take over at points in the decision tree should you see fit.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Malcolm Ware and Eibe Frank and Geoffrey Holmes and Mark Hall and Ian H. Witten");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.TITLE, "Interactive machine learning: letting users build classifiers");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Int. J. Hum.-Comput. Stud.");
    result.setValue(TechnicalInformation.Field.VOLUME, "55");
    result.setValue(TechnicalInformation.Field.NUMBER, "3");
    result.setValue(TechnicalInformation.Field.PAGES, "281-292");
    result.setValue(TechnicalInformation.Field.PS, "http://www.cs.waikato.ac.nz/~ml/publications/2000/00MW-etal-Interactive-ML.ps");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public void buildClassifier(Instances i)
    throws Exception
  {
    getCapabilities().testWithFail(i);
    

    i = new Instances(i);
    i.deleteWithMissingClass();
    





    m_classifiers = new GenericObjectEditor(true);
    m_classifiers.setClassType(Classifier.class);
    m_classifiers.setValue(new ZeroR());
    
    ((GenericObjectEditor.GOEPanel)m_classifiers.getCustomEditor()).addOkListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        try
        {
          m_focus.m_set1 = null;
          m_focus.m_set2 = null;
          m_focus.setInfo(m_focus.m_attrib1, m_focus.m_attrib2, null);
          m_focus.setClassifier((Classifier)m_classifiers.getValue());
          




          m_tView = new TreeVisualizer(UserClassifier.this, graph(), new PlaceNode2());
          
          m_tView.setHighlight(m_focus.m_identity);
          m_reps.setComponentAt(0, m_tView);
          m_iView.setShapes(null);
        } catch (Exception er) {
          System.out.println("Error : " + er);
          System.out.println("Part of user input so had to catch here");
          JOptionPane.showMessageDialog(null, "Unable to use " + m_focus.getClassifier().getClass().getName() + " at this node.\n" + "This exception was produced:\n" + er.toString(), "UserClassifier", 0);


        }
        


      }
      


    });
    m_built = false;
    m_mainWin = new JFrame();
    
    m_mainWin.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        int well = JOptionPane.showConfirmDialog(m_mainWin, "Are You Sure...\nClick Yes To Accept The Tree\n Click No To Return", "Accept Tree", 0);
        






        if (well == 0) {
          m_mainWin.setDefaultCloseOperation(2);
          UserClassifier.this.blocker(false);
        }
        else
        {
          m_mainWin.setDefaultCloseOperation(0);
        }
        
      }
    });
    m_reps = new JTabbedPane();
    m_mainWin.getContentPane().add(m_reps);
    

    Instances te = new Instances(i, i.numInstances());
    for (int noa = 0; noa < i.numInstances(); noa++) {
      te.add(i.instance(noa));
    }
    
    te.deleteWithMissingClass();
    

    m_top = new TreeClass(null, 0, 0, m_nextId, 1.0D, te, null);
    m_focus = m_top;
    
    m_tView = new TreeVisualizer(this, graph(), new PlaceNode1());
    
    m_reps.add("Tree Visualizer", m_tView);
    




    m_tView.setHighlight(m_top.m_identity);
    m_iView = new VisualizePanel(this);
    
    m_iView.setInstances(m_top.m_training);
    m_iView.setColourIndex(te.classIndex());
    



    m_reps.add("Data Visualizer", m_iView);
    m_mainWin.setSize(560, 420);
    m_mainWin.setVisible(true);
    blocker(true);
    


    if (m_propertyDialog != null) {
      m_propertyDialog.dispose();
      m_propertyDialog = null;
    }
    

    m_classifiers = null;
    m_built = true;
  }
  






  public double[] distributionForInstance(Instance i)
    throws Exception
  {
    if (!m_built) {
      return null;
    }
    
    double[] res = m_top.calcClassType(i);
    if (m_top.m_training.classAttribute().isNumeric()) {
      return res;
    }
    
    double most_likely = 0.0D;double highest = -1.0D;
    double count = 0.0D;
    for (int noa = 0; noa < m_top.m_training.numClasses(); noa++) {
      count += res[noa];
      if (res[noa] > highest) {
        most_likely = noa;
        highest = res[noa];
      }
    }
    
    if (count <= 0.0D)
    {
      return null;
    }
    
    for (int noa = 0; noa < m_top.m_training.numClasses(); noa++) {
      res[noa] /= count;
    }
    

    return res;
  }
  



  private class TreeClass
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 595663560871347434L;
    


    public FastVector m_ranges;
    


    public int m_attrib1;
    


    public int m_attrib2;
    


    public TreeClass m_set1;
    


    public TreeClass m_set2;
    


    public TreeClass m_parent;
    


    public String m_identity;
    


    public double m_weight;
    


    public Instances m_training;
    


    public Classifier m_classObject;
    


    public Filter m_filter;
    



    public TreeClass(FastVector r, int a1, int a2, int id, double w, Instances i, TreeClass p)
      throws Exception
    {
      m_set1 = null;
      m_set2 = null;
      m_ranges = r;
      m_classObject = null;
      m_filter = null;
      m_training = i;
      m_attrib1 = a1;
      m_attrib2 = a2;
      m_identity = ("N" + String.valueOf(id));
      m_weight = w;
      m_parent = p;
      UserClassifier.access$708(UserClassifier.this);
      if (m_ranges == null)
      {
        setLeaf();
      }
    }
    




















    public void setClassifier(Classifier c)
      throws Exception
    {
      m_classObject = c;
      m_classObject.buildClassifier(m_training);
    }
    





    public Classifier getClassifier()
    {
      return m_classObject;
    }
    







    public void setInfo(int at1, int at2, FastVector ar)
      throws Exception
    {
      m_classObject = null;
      m_filter = null;
      m_attrib1 = at1;
      m_attrib2 = at2;
      m_ranges = ar;
      

      if (m_ranges == null) {
        setLeaf();
      }
    }
    


























    private void setLeaf()
      throws Exception
    {
      if (m_training != null)
      {
        if (m_training.classAttribute().isNominal())
        {


          m_ranges = new FastVector(1);
          m_ranges.addElement(new FastVector(m_training.numClasses() + 1));
          FastVector tmp = (FastVector)m_ranges.elementAt(0);
          tmp.addElement(new Double(0.0D));
          for (int noa = 0; noa < m_training.numClasses(); noa++) {
            tmp.addElement(new Double(0.0D));
          }
          for (int noa = 0; noa < m_training.numInstances(); noa++) {
            tmp.setElementAt(new Double(((Double)tmp.elementAt((int)m_training.instance(noa).classValue() + 1)).doubleValue() + m_training.instance(noa).weight()), (int)m_training.instance(noa).classValue() + 1);

          }
          


        }
        else
        {

          m_ranges = new FastVector(1);
          double t1 = 0.0D;
          for (int noa = 0; noa < m_training.numInstances(); noa++) {
            t1 += m_training.instance(noa).classValue();
          }
          
          if (m_training.numInstances() != 0) {
            t1 /= m_training.numInstances();
          }
          double t2 = 0.0D;
          for (int noa = 0; noa < m_training.numInstances(); noa++) {
            t2 += Math.pow(m_training.instance(noa).classValue() - t1, 2.0D);
          }
          
          if (m_training.numInstances() != 0) {
            t1 = Math.sqrt(t2 / m_training.numInstances());
            m_ranges.addElement(new FastVector(2));
            FastVector tmp = (FastVector)m_ranges.elementAt(0);
            tmp.addElement(new Double(0.0D));
            tmp.addElement(new Double(t1));
          }
          else {
            m_ranges.addElement(new FastVector(2));
            FastVector tmp = (FastVector)m_ranges.elementAt(0);
            tmp.addElement(new Double(0.0D));
            tmp.addElement(new Double(NaN.0D));
          }
        }
      }
    }
    













    public double[] calcClassType(Instance i)
      throws Exception
    {
      double x = 0.0D;double y = 0.0D;
      if (m_attrib1 >= 0) {
        x = i.value(m_attrib1);
      }
      if (m_attrib2 >= 0)
        y = i.value(m_attrib2);
      double[] rt;
      double[] rt;
      if (m_training.classAttribute().isNominal()) {
        rt = new double[m_training.numClasses()];
      }
      else {
        rt = new double[1];
      }
      

      if (m_classObject != null)
      {
        if (m_training.classAttribute().isNominal()) {
          rt[((int)m_classObject.classifyInstance(i))] = 1.0D;

        }
        else if (m_filter != null) {
          m_filter.input(i);
          rt[0] = m_classObject.classifyInstance(m_filter.output());
        }
        else {
          rt[0] = m_classObject.classifyInstance(i);
        }
        

        return rt;
      }
      if (((Double)((FastVector)m_ranges.elementAt(0)).elementAt(0)).intValue() == 0)
      {




        if (m_training.classAttribute().isNumeric())
        {
          setLinear();
          m_filter.input(i);
          rt[0] = m_classObject.classifyInstance(m_filter.output());
          return rt;
        }
        
        int totaler = 0;
        FastVector tmp = (FastVector)m_ranges.elementAt(0);
        for (int noa = 0; noa < m_training.numClasses(); noa++) {
          rt[noa] = ((Double)tmp.elementAt(noa + 1)).doubleValue();
          totaler = (int)(totaler + rt[noa]);
        }
        for (int noa = 0; noa < m_training.numClasses(); noa++) {
          rt[noa] /= totaler;
        }
        return rt;
      }
      
      for (int noa = 0; noa < m_ranges.size(); noa++)
      {
        FastVector tmp = (FastVector)m_ranges.elementAt(noa);
        
        if ((((Double)tmp.elementAt(0)).intValue() != 5) || (Instance.isMissingValue(x)))
        {


          if ((((Double)tmp.elementAt(0)).intValue() != 6) || (Instance.isMissingValue(y)))
          {


            if ((Instance.isMissingValue(x)) || (Instance.isMissingValue(y)))
            {

              rt = m_set1.calcClassType(i);
              double[] tem = m_set2.calcClassType(i);
              if (m_training.classAttribute().isNominal()) {
                for (int nob = 0; nob < m_training.numClasses(); nob++) {
                  rt[nob] *= m_set1.m_weight;
                  rt[nob] += tem[nob] * m_set2.m_weight;
                }
              }
              else {
                rt[0] *= m_set1.m_weight;
                rt[0] += tem[0] * m_set2.m_weight;
              }
              return rt;
            }
            if (((Double)tmp.elementAt(0)).intValue() == 1)
            {
              if ((x >= ((Double)tmp.elementAt(1)).doubleValue()) && (x <= ((Double)tmp.elementAt(3)).doubleValue()) && (y <= ((Double)tmp.elementAt(2)).doubleValue()) && (y >= ((Double)tmp.elementAt(4)).doubleValue()))
              {




                rt = m_set1.calcClassType(i);
                return rt;
              }
              
            }
            else if (((Double)tmp.elementAt(0)).intValue() == 2) {
              if (inPoly(tmp, x, y)) {
                rt = m_set1.calcClassType(i);
                return rt;
              }
            }
            else if ((((Double)tmp.elementAt(0)).intValue() == 3) && 
              (inPolyline(tmp, x, y))) {
              rt = m_set1.calcClassType(i);
              return rt;
            }
          }
        }
      }
      if (m_set2 != null) {
        rt = m_set2.calcClassType(i);
      }
      return rt;
    }
    







    private void setLinear()
      throws Exception
    {
      boolean[] attributeList = new boolean[m_training.numAttributes()];
      for (int noa = 0; noa < m_training.numAttributes(); noa++) {
        attributeList[noa] = false;
      }
      
      TreeClass temp = this;
      attributeList[m_training.classIndex()] = true;
      while (temp != null) {
        attributeList[m_attrib1] = true;
        attributeList[m_attrib2] = true;
        temp = m_parent;
      }
      int classind = 0;
      


      for (int noa = 0; noa < m_training.classIndex(); noa++) {
        if (attributeList[noa] != 0) {
          classind++;
        }
      }
      
      int count = 0;
      for (int noa = 0; noa < m_training.numAttributes(); noa++) {
        if (attributeList[noa] != 0) {
          count++;
        }
      }
      

      int[] attributeList2 = new int[count];
      count = 0;
      for (int noa = 0; noa < m_training.numAttributes(); noa++) {
        if (attributeList[noa] != 0) {
          attributeList2[count] = noa;
          count++;
        }
      }
      
      m_filter = new Remove();
      ((Remove)m_filter).setInvertSelection(true);
      ((Remove)m_filter).setAttributeIndicesArray(attributeList2);
      m_filter.setInputFormat(m_training);
      
      Instances temp2 = Filter.useFilter(m_training, m_filter);
      temp2.setClassIndex(classind);
      m_classObject = new LinearRegression();
      m_classObject.buildClassifier(temp2);
    }
    













    private boolean inPolyline(FastVector ob, double x, double y)
    {
      int countx = 0;
      



      for (int noa = 1; noa < ob.size() - 4; noa += 2) {
        double y1 = ((Double)ob.elementAt(noa + 1)).doubleValue();
        double y2 = ((Double)ob.elementAt(noa + 3)).doubleValue();
        double x1 = ((Double)ob.elementAt(noa)).doubleValue();
        double x2 = ((Double)ob.elementAt(noa + 2)).doubleValue();
        double vecy = y2 - y1;
        double vecx = x2 - x1;
        if ((noa == 1) && (noa == ob.size() - 6))
        {
          if (vecy != 0.0D) {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
        else if (noa == 1) {
          if (((y < y2) && (vecy > 0.0D)) || ((y > y2) && (vecy < 0.0D)))
          {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
        else if (noa == ob.size() - 6)
        {
          if (((y <= y1) && (vecy < 0.0D)) || ((y >= y1) && (vecy > 0.0D))) {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x) {
              countx++;
            }
            
          }
        }
        else if (((y1 <= y) && (y < y2)) || ((y2 < y) && (y <= y1)))
        {
          if (vecy != 0.0D)
          {



            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
      }
      


      double y1 = ((Double)ob.elementAt(ob.size() - 2)).doubleValue();
      double y2 = ((Double)ob.elementAt(ob.size() - 1)).doubleValue();
      
      if (y1 > y2)
      {
        if ((y1 >= y) && (y > y2)) {
          countx++;
        }
        

      }
      else if ((y1 >= y) || (y > y2)) {
        countx++;
      }
      

      if (countx % 2 == 1) {
        return true;
      }
      
      return false;
    }
    







    private boolean inPoly(FastVector ob, double x, double y)
    {
      int count = 0;
      


      for (int noa = 1; noa < ob.size() - 2; noa += 2) {
        double y1 = ((Double)ob.elementAt(noa + 1)).doubleValue();
        double y2 = ((Double)ob.elementAt(noa + 3)).doubleValue();
        if (((y1 <= y) && (y < y2)) || ((y2 < y) && (y <= y1)))
        {
          double vecy = y2 - y1;
          if (vecy != 0.0D)
          {


            double x1 = ((Double)ob.elementAt(noa)).doubleValue();
            double x2 = ((Double)ob.elementAt(noa + 2)).doubleValue();
            double vecx = x2 - x1;
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              count++;
            }
          }
        }
      }
      
      if (count % 2 == 1)
      {

        return true;
      }
      

      return false;
    }
    









    public TreeClass getNode(String id)
    {
      if (id.equals(m_identity)) {
        return this;
      }
      
      if (m_set1 != null) {
        TreeClass tmp = m_set1.getNode(id);
        if (tmp != null) {
          return tmp;
        }
      }
      if (m_set2 != null) {
        TreeClass tmp = m_set2.getNode(id);
        if (tmp != null) {
          return tmp;
        }
      }
      return null;
    }
    







    public void getAlternateLabel(StringBuffer s)
      throws Exception
    {
      FastVector tmp = (FastVector)m_ranges.elementAt(0);
      
      if ((m_classObject != null) && (m_training.classAttribute().isNominal())) {
        s.append("Classified by " + m_classObject.getClass().getName());
      }
      else if (((Double)tmp.elementAt(0)).intValue() == 0) {
        if (m_training.classAttribute().isNominal()) {
          double high = -1000.0D;
          int num = 0;
          double count = 0.0D;
          for (int noa = 0; noa < m_training.classAttribute().numValues(); 
              noa++) {
            if (((Double)tmp.elementAt(noa + 1)).doubleValue() > high) {
              high = ((Double)tmp.elementAt(noa + 1)).doubleValue();
              num = noa + 1;
            }
            count += ((Double)tmp.elementAt(noa + 1)).doubleValue();
          }
          s.append(m_training.classAttribute().value(num - 1) + "(" + count);
          if (count > high) {
            s.append("/" + (count - high));
          }
          s.append(")");
        }
        else {
          if ((m_classObject == null) && (((Double)tmp.elementAt(0)).intValue() == 0))
          {
            setLinear();
          }
          s.append("Standard Deviation = " + Utils.doubleToString(((Double)tmp.elementAt(1)).doubleValue(), 6));
        }
        

      }
      else
      {
        s.append("Split on ");
        s.append(m_training.attribute(m_attrib1).name() + " AND ");
        s.append(m_training.attribute(m_attrib2).name());
      }
    }
    










    public void getLabel(StringBuffer s)
      throws Exception
    {
      FastVector tmp = (FastVector)m_ranges.elementAt(0);
      

      if ((m_classObject != null) && (m_training.classAttribute().isNominal())) {
        s.append("Classified by\\n" + m_classObject.getClass().getName());
      }
      else if (((Double)tmp.elementAt(0)).intValue() == 0)
      {
        if (m_training.classAttribute().isNominal()) {
          boolean first = true;
          for (int noa = 0; noa < m_training.classAttribute().numValues(); 
              noa++) {
            if (((Double)tmp.elementAt(noa + 1)).doubleValue() > 0.0D) {
              if (first)
              {
                s.append("[" + m_training.classAttribute().value(noa));
                first = false;
              }
              else
              {
                s.append("\\n[" + m_training.classAttribute().value(noa));
              }
              s.append(", " + ((Double)tmp.elementAt(noa + 1)).doubleValue() + "]");
            }
          }
        }
        else
        {
          if ((m_classObject == null) && (((Double)tmp.elementAt(0)).intValue() == 0))
          {
            setLinear();
          }
          s.append("Standard Deviation = " + Utils.doubleToString(((Double)tmp.elementAt(1)).doubleValue(), 6));
        }
        
      }
      else
      {
        s.append("Split on\\n");
        s.append(m_training.attribute(m_attrib1).name() + " AND\\n");
        s.append(m_training.attribute(m_attrib2).name());
      }
    }
    





    public void toDotty(StringBuffer t)
      throws Exception
    {
      t.append(m_identity + " [label=\"");
      getLabel(t);
      t.append("\" ");
      

      if (((Double)((FastVector)m_ranges.elementAt(0)).elementAt(0)).intValue() == 0)
      {
        t.append("shape=box ");
      }
      else {
        t.append("shape=ellipse ");
      }
      t.append("style=filled color=gray95]\n");
      
      if (m_set1 != null) {
        t.append(m_identity + "->");
        t.append(m_set1.m_identity + " [label=\"True\"]\n");
        
        m_set1.toDotty(t);
      }
      if (m_set2 != null) {
        t.append(m_identity + "->");
        t.append(m_set2.m_identity + " [label=\"False\"]\n");
        

        m_set2.toDotty(t);
      }
    }
    





    public void objectStrings(StringBuffer t)
    {
      if (m_classObject != null) {
        t.append("\n\n" + m_identity + " {\n" + m_classObject.toString() + "\n}");
      }
      if (m_set1 != null) {
        m_set1.objectStrings(t);
      }
      if (m_set2 != null) {
        m_set2.objectStrings(t);
      }
    }
    





    public void toString(int l, StringBuffer t)
      throws Exception
    {
      if (((Double)((FastVector)m_ranges.elementAt(0)).elementAt(0)).intValue() == 0)
      {
        t.append(": " + m_identity + " ");
        getAlternateLabel(t);
      }
      if (m_set1 != null) {
        t.append("\n");
        for (int noa = 0; noa < l; noa++) {
          t.append("|   ");
        }
        
        getAlternateLabel(t);
        t.append(" (In Set)");
        m_set1.toString(l + 1, t);
      }
      if (m_set2 != null) {
        t.append("\n");
        for (int noa = 0; noa < l; noa++) {
          t.append("|   ");
        }
        getAlternateLabel(t);
        t.append(" (Not in Set)");
        m_set2.toString(l + 1, t);
      }
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5535 $");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5535 $");
  }
  
  static {}
}
