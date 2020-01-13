package weka.clusterers;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Drawable;
import weka.core.FastVector;
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
import weka.experiment.Stats;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;






































































































public class Cobweb
  extends RandomizableClusterer
  implements Drawable, TechnicalInformationHandler, UpdateableClusterer
{
  static final long serialVersionUID = 928406656495092318L;
  
  private class CNode
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 3452097436933325631L;
    private AttributeStats[] m_attStats;
    private int m_numAttributes;
    protected Instances m_clusterInstances = null;
    



    private FastVector m_children = null;
    



    private double m_totalInstances = 0.0D;
    



    private int m_clusterNum = -1;
    




    public CNode(int numAttributes)
    {
      m_numAttributes = numAttributes;
    }
    





    public CNode(int numAttributes, Instance leafInstance)
    {
      this(numAttributes);
      if (m_clusterInstances == null) {
        m_clusterInstances = new Instances(leafInstance.dataset(), 1);
      }
      m_clusterInstances.add(leafInstance);
      updateStats(leafInstance, false);
    }
    






    protected void addInstance(Instance newInstance)
      throws Exception
    {
      if (m_clusterInstances == null) {
        m_clusterInstances = new Instances(newInstance.dataset(), 1);
        m_clusterInstances.add(newInstance);
        updateStats(newInstance, false);
        return; }
      if (m_children == null)
      {

        m_children = new FastVector();
        CNode tempSubCluster = new CNode(Cobweb.this, m_numAttributes, m_clusterInstances.instance(0));
        


        for (int i = 1; i < m_clusterInstances.numInstances(); i++) {
          m_clusterInstances.add(m_clusterInstances.instance(i));
          
          tempSubCluster.updateStats(m_clusterInstances.instance(i), false);
        }
        m_children = new FastVector();
        m_children.addElement(tempSubCluster);
        m_children.addElement(new CNode(Cobweb.this, m_numAttributes, newInstance));
        
        m_clusterInstances.add(newInstance);
        updateStats(newInstance, false);
        


        if (categoryUtility() < m_cutoff)
        {
          m_children = null;
        }
        return;
      }
      

      CNode bestHost = findHost(newInstance, false);
      if (bestHost != null)
      {
        bestHost.addInstance(newInstance);
      }
    }
    









    private double[] cuScoresForChildren(Instance newInstance)
      throws Exception
    {
      double[] categoryUtils = new double[m_children.size()];
      

      for (int i = 0; i < m_children.size(); i++) {
        CNode temp = (CNode)m_children.elementAt(i);
        
        temp.updateStats(newInstance, false);
        categoryUtils[i] = categoryUtility();
        

        temp.updateStats(newInstance, true);
      }
      return categoryUtils;
    }
    


    private double cuScoreForBestTwoMerged(CNode merged, CNode a, CNode b, Instance newInstance)
      throws Exception
    {
      double mergedCU = -1.7976931348623157E308D;
      

      m_clusterInstances = new Instances(m_clusterInstances, 1);
      
      merged.addChildNode(a);
      merged.addChildNode(b);
      merged.updateStats(newInstance, false);
      
      m_children.removeElementAt(m_children.indexOf(a));
      m_children.removeElementAt(m_children.indexOf(b));
      m_children.addElement(merged);
      mergedCU = categoryUtility();
      
      merged.updateStats(newInstance, true);
      m_children.removeElementAt(m_children.indexOf(merged));
      m_children.addElement(a);
      m_children.addElement(b);
      return mergedCU;
    }
    










    private CNode findHost(Instance newInstance, boolean structureFrozen)
      throws Exception
    {
      if (!structureFrozen) {
        updateStats(newInstance, false);
      }
      

      double[] categoryUtils = cuScoresForChildren(newInstance);
      

      CNode newLeaf = new CNode(Cobweb.this, m_numAttributes, newInstance);
      m_children.addElement(newLeaf);
      double bestHostCU = categoryUtility();
      CNode finalBestHost = newLeaf;
      


      m_children.removeElementAt(m_children.size() - 1);
      

      int best = 0;
      int secondBest = 0;
      for (int i = 0; i < categoryUtils.length; i++) {
        if (categoryUtils[i] > categoryUtils[secondBest]) {
          if (categoryUtils[i] > categoryUtils[best]) {
            secondBest = best;
            best = i;
          } else {
            secondBest = i;
          }
        }
      }
      
      CNode a = (CNode)m_children.elementAt(best);
      CNode b = (CNode)m_children.elementAt(secondBest);
      if (categoryUtils[best] > bestHostCU) {
        bestHostCU = categoryUtils[best];
        finalBestHost = a;
      }
      

      if (structureFrozen) {
        if (finalBestHost == newLeaf) {
          return null;
        }
        return finalBestHost;
      }
      

      double mergedCU = -1.7976931348623157E308D;
      CNode merged = new CNode(Cobweb.this, m_numAttributes);
      if (a != b) {
        mergedCU = cuScoreForBestTwoMerged(merged, a, b, newInstance);
        
        if (mergedCU > bestHostCU) {
          bestHostCU = mergedCU;
          finalBestHost = merged;
        }
      }
      

      double splitCU = -1.7976931348623157E308D;
      double splitBestChildCU = -1.7976931348623157E308D;
      double splitPlusNewLeafCU = -1.7976931348623157E308D;
      double splitPlusMergeBestTwoCU = -1.7976931348623157E308D;
      if (m_children != null) {
        FastVector tempChildren = new FastVector();
        
        for (int i = 0; i < m_children.size(); i++) {
          CNode existingChild = (CNode)m_children.elementAt(i);
          if (existingChild != a) {
            tempChildren.addElement(existingChild);
          }
        }
        for (int i = 0; i < m_children.size(); i++) {
          CNode promotedChild = (CNode)m_children.elementAt(i);
          tempChildren.addElement(promotedChild);
        }
        
        tempChildren.addElement(newLeaf);
        
        FastVector saveStatusQuo = m_children;
        m_children = tempChildren;
        splitPlusNewLeafCU = categoryUtility();
        
        tempChildren.removeElementAt(tempChildren.size() - 1);
        
        categoryUtils = cuScoresForChildren(newInstance);
        

        best = 0;
        secondBest = 0;
        for (int i = 0; i < categoryUtils.length; i++) {
          if (categoryUtils[i] > categoryUtils[secondBest]) {
            if (categoryUtils[i] > categoryUtils[best]) {
              secondBest = best;
              best = i;
            } else {
              secondBest = i;
            }
          }
        }
        CNode sa = (CNode)m_children.elementAt(best);
        CNode sb = (CNode)m_children.elementAt(secondBest);
        splitBestChildCU = categoryUtils[best];
        

        CNode mergedSplitChildren = new CNode(Cobweb.this, m_numAttributes);
        if (sa != sb) {
          splitPlusMergeBestTwoCU = cuScoreForBestTwoMerged(mergedSplitChildren, sa, sb, newInstance);
        }
        
        splitCU = splitBestChildCU > splitPlusNewLeafCU ? splitBestChildCU : splitPlusNewLeafCU;
        
        splitCU = splitCU > splitPlusMergeBestTwoCU ? splitCU : splitPlusMergeBestTwoCU;
        

        if (splitCU > bestHostCU) {
          bestHostCU = splitCU;
          finalBestHost = this;
        }
        else
        {
          m_children = saveStatusQuo;
        }
      }
      
      if (finalBestHost != this)
      {
        m_clusterInstances.add(newInstance);
      } else {
        m_numberSplits += 1;
      }
      
      if (finalBestHost == merged) {
        m_numberMerges += 1;
        m_children.removeElementAt(m_children.indexOf(a));
        m_children.removeElementAt(m_children.indexOf(b));
        m_children.addElement(merged);
      }
      
      if (finalBestHost == newLeaf) {
        finalBestHost = new CNode(Cobweb.this, m_numAttributes);
        m_children.addElement(finalBestHost);
      }
      
      if (bestHostCU < m_cutoff) {
        if (finalBestHost == this)
        {


          m_clusterInstances.add(newInstance);
        }
        m_children = null;
        finalBestHost = null;
      }
      
      if (finalBestHost == this)
      {

        updateStats(newInstance, true);
      }
      
      return finalBestHost;
    }
    





    protected void addChildNode(CNode child)
    {
      for (int i = 0; i < m_clusterInstances.numInstances(); i++) {
        Instance temp = m_clusterInstances.instance(i);
        m_clusterInstances.add(temp);
        updateStats(temp, false);
      }
      
      if (m_children == null) {
        m_children = new FastVector();
      }
      m_children.addElement(child);
    }
    





    protected double categoryUtility()
      throws Exception
    {
      if (m_children == null) {
        throw new Exception("categoryUtility: No children!");
      }
      
      double totalCU = 0.0D;
      
      for (int i = 0; i < m_children.size(); i++) {
        CNode child = (CNode)m_children.elementAt(i);
        totalCU += categoryUtilityChild(child);
      }
      
      totalCU /= m_children.size();
      return totalCU;
    }
    






    protected double categoryUtilityChild(CNode child)
      throws Exception
    {
      double sum = 0.0D;
      for (int i = 0; i < m_numAttributes; i++) {
        if (m_clusterInstances.attribute(i).isNominal()) {
          for (int j = 0; 
              j < m_clusterInstances.attribute(i).numValues(); j++) {
            double x = child.getProbability(i, j);
            double y = getProbability(i, j);
            sum += x * x - y * y;
          }
          
        } else {
          sum += Cobweb.m_normal / child.getStandardDev(i) - Cobweb.m_normal / getStandardDev(i);
        }
      }
      

      return m_totalInstances / m_totalInstances * sum;
    }
    








    protected double getProbability(int attIndex, int valueIndex)
      throws Exception
    {
      if (!m_clusterInstances.attribute(attIndex).isNominal()) {
        throw new Exception("getProbability: attribute is not nominal");
      }
      
      if (m_attStats[attIndex].totalCount <= 0) {
        return 0.0D;
      }
      
      return m_attStats[attIndex].nominalCounts[valueIndex] / m_attStats[attIndex].totalCount;
    }
    






    protected double getStandardDev(int attIndex)
      throws Exception
    {
      if (!m_clusterInstances.attribute(attIndex).isNumeric()) {
        throw new Exception("getStandardDev: attribute is not numeric");
      }
      
      m_attStats[attIndex].numericStats.calculateDerived();
      double stdDev = m_attStats[attIndex].numericStats.stdDev;
      if ((Double.isNaN(stdDev)) || (Double.isInfinite(stdDev))) {
        return m_acuity;
      }
      
      return Math.max(m_acuity, stdDev);
    }
    








    protected void updateStats(Instance updateInstance, boolean delete)
    {
      if (m_attStats == null) {
        m_attStats = new AttributeStats[m_numAttributes];
        for (int i = 0; i < m_numAttributes; i++) {
          m_attStats[i] = new AttributeStats();
          if (m_clusterInstances.attribute(i).isNominal()) {
            m_attStats[i].nominalCounts = new int[m_clusterInstances.attribute(i).numValues()];
          }
          else {
            m_attStats[i].numericStats = new Stats();
          }
        }
      }
      for (int i = 0; i < m_numAttributes; i++) {
        if (!updateInstance.isMissing(i)) {
          double value = updateInstance.value(i);
          if (m_clusterInstances.attribute(i).isNominal()) {
            int tmp153_152 = ((int)value); int[] tmp153_147 = m_attStats[i].nominalCounts;tmp153_147[tmp153_152] = ((int)(tmp153_147[tmp153_152] + (delete ? -1.0D * updateInstance.weight() : updateInstance.weight()))); AttributeStats 
            

              tmp184_183 = m_attStats[i];184183totalCount = ((int)(184183totalCount + (delete ? -1.0D * updateInstance.weight() : updateInstance.weight())));


          }
          else if (delete) {
            m_attStats[i].numericStats.subtract(value, updateInstance.weight());
          }
          else {
            m_attStats[i].numericStats.add(value, updateInstance.weight());
          }
        }
      }
      
      m_totalInstances += (delete ? -1.0D * updateInstance.weight() : updateInstance.weight());
    }
    






    private void assignClusterNums(int[] cl_num)
      throws Exception
    {
      if ((m_children != null) && (m_children.size() < 2)) {
        throw new Exception("assignClusterNums: tree not built correctly!");
      }
      
      m_clusterNum = cl_num[0];
      cl_num[0] += 1;
      if (m_children != null) {
        for (int i = 0; i < m_children.size(); i++) {
          CNode child = (CNode)m_children.elementAt(i);
          child.assignClusterNums(cl_num);
        }
      }
    }
    






    protected void dumpTree(int depth, StringBuffer text)
    {
      if (depth == 0) {
        determineNumberOfClusters();
      }
      if (m_children == null) {
        text.append("\n");
        for (int j = 0; j < depth; j++) {
          text.append("|   ");
        }
        text.append("leaf " + m_clusterNum + " [" + m_clusterInstances.numInstances() + "]");
      }
      else {
        for (int i = 0; i < m_children.size(); i++) {
          text.append("\n");
          for (int j = 0; j < depth; j++) {
            text.append("|   ");
          }
          text.append("node " + m_clusterNum + " [" + m_clusterInstances.numInstances() + "]");
          

          ((CNode)m_children.elementAt(i)).dumpTree(depth + 1, text);
        }
      }
    }
    





    protected String dumpData()
      throws Exception
    {
      if (m_children == null) {
        return m_clusterInstances.toString();
      }
      

      CNode tempNode = new CNode(Cobweb.this, m_numAttributes);
      m_clusterInstances = new Instances(m_clusterInstances, 1);
      for (int i = 0; i < m_children.size(); i++) {
        tempNode.addChildNode((CNode)m_children.elementAt(i));
      }
      Instances tempInst = m_clusterInstances;
      tempNode = null;
      
      Add af = new Add();
      af.setAttributeName("Cluster");
      String labels = "";
      for (int i = 0; i < m_children.size(); i++) {
        CNode temp = (CNode)m_children.elementAt(i);
        labels = labels + "C" + m_clusterNum;
        if (i < m_children.size() - 1) {
          labels = labels + ",";
        }
      }
      af.setNominalLabels(labels);
      af.setInputFormat(tempInst);
      tempInst = Filter.useFilter(tempInst, af);
      tempInst.setRelationName("Cluster " + m_clusterNum);
      
      int z = 0;
      for (int i = 0; i < m_children.size(); i++) {
        CNode temp = (CNode)m_children.elementAt(i);
        for (int j = 0; j < m_clusterInstances.numInstances(); j++) {
          tempInst.instance(z).setValue(m_numAttributes, i);
          z++;
        }
      }
      return tempInst.toString();
    }
    





    protected void graphTree(StringBuffer text)
      throws Exception
    {
      text.append("N" + m_clusterNum + " [label=\"" + (m_children == null ? "leaf " : "node ") + m_clusterNum + " " + " (" + m_clusterInstances.numInstances() + ")\" " + (m_children == null ? "shape=box style=filled " : "") + (m_saveInstances ? "data =\n" + dumpData() + "\n,\n" : "") + "]\n");
      










      if (m_children != null) {
        for (int i = 0; i < m_children.size(); i++) {
          CNode temp = (CNode)m_children.elementAt(i);
          text.append("N" + m_clusterNum + "->" + "N" + m_clusterNum + "\n");
        }
        



        for (int i = 0; i < m_children.size(); i++) {
          CNode temp = (CNode)m_children.elementAt(i);
          temp.graphTree(text);
        }
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6790 $");
    }
  }
  



  protected static final double m_normal = 1.0D / (2.0D * Math.sqrt(3.141592653589793D));
  



  protected double m_acuity = 1.0D;
  



  protected double m_cutoff = 0.01D * m_normal;
  



  protected CNode m_cobwebTree = null;
  








  protected int m_numberOfClusters = -1;
  

  protected boolean m_numberOfClustersDetermined = false;
  


  protected int m_numberSplits;
  


  protected int m_numberMerges;
  


  protected boolean m_saveInstances = false;
  




  public Cobweb()
  {
    m_SeedDefault = 42;
    setSeed(m_SeedDefault);
  }
  




  public String globalInfo()
  {
    return "Class implementing the Cobweb and Classit clustering algorithms.\n\nNote: the application of node operators (merging, splitting etc.) in terms of ordering and priority differs (and is somewhat ambiguous) between the original Cobweb and Classit papers. This algorithm always compares the best host, adding a new leaf, merging the two best hosts, and splitting the best host when considering where to place a new instance.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "D. Fisher");
    result.setValue(TechnicalInformation.Field.YEAR, "1987");
    result.setValue(TechnicalInformation.Field.TITLE, "Knowledge acquisition via incremental conceptual clustering");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "2");
    result.setValue(TechnicalInformation.Field.NUMBER, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "139-172");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "J. H. Gennari and P. Langley and D. Fisher");
    additional.setValue(TechnicalInformation.Field.YEAR, "1990");
    additional.setValue(TechnicalInformation.Field.TITLE, "Models of incremental concept formation");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Artificial Intelligence");
    additional.setValue(TechnicalInformation.Field.VOLUME, "40");
    additional.setValue(TechnicalInformation.Field.PAGES, "11-61");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  




  public void buildClusterer(Instances data)
    throws Exception
  {
    m_numberOfClusters = -1;
    m_cobwebTree = null;
    m_numberSplits = 0;
    m_numberMerges = 0;
    

    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    
    if (getSeed() >= 0) {
      data.randomize(new Random(getSeed()));
    }
    
    for (int i = 0; i < data.numInstances(); i++) {
      updateClusterer(data.instance(i));
    }
    
    updateFinished();
  }
  


  public void updateFinished()
  {
    determineNumberOfClusters();
  }
  







  public int clusterInstance(Instance instance)
    throws Exception
  {
    CNode host = m_cobwebTree;
    CNode temp = null;
    
    determineNumberOfClusters();
    do
    {
      if (m_children == null) {
        temp = null;
        break;
      }
      

      temp = host.findHost(instance, true);
      

      if (temp != null) {
        host = temp;
      }
    } while (temp != null);
    
    return m_clusterNum;
  }
  





  protected void determineNumberOfClusters()
  {
    if ((!m_numberOfClustersDetermined) && (m_cobwebTree != null))
    {
      int[] numClusts = new int[1];
      numClusts[0] = 0;
      try {
        m_cobwebTree.assignClusterNums(numClusts);
      }
      catch (Exception e) {
        e.printStackTrace();
        numClusts[0] = 0;
      }
      m_numberOfClusters = numClusts[0];
      
      m_numberOfClustersDetermined = true;
    }
  }
  




  public int numberOfClusters()
  {
    determineNumberOfClusters();
    return m_numberOfClusters;
  }
  




  public void updateClusterer(Instance newInstance)
    throws Exception
  {
    m_numberOfClustersDetermined = false;
    
    if (m_cobwebTree == null) {
      m_cobwebTree = new CNode(newInstance.numAttributes(), newInstance);
    } else {
      m_cobwebTree.addInstance(newInstance);
    }
  }
  



  /**
   * @deprecated
   */
  public void addInstance(Instance newInstance)
    throws Exception
  {
    updateClusterer(newInstance);
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tAcuity.\n\t(default=1.0)", "A", 1, "-A <acuity>"));
    



    result.addElement(new Option("\tCutoff.\n\t(default=0.002)", "C", 1, "-C <cutoff>"));
    



    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('A', options);
    if (optionString.length() != 0) {
      Double temp = new Double(optionString);
      setAcuity(temp.doubleValue());
    }
    else {
      m_acuity = 1.0D;
    }
    optionString = Utils.getOption('C', options);
    if (optionString.length() != 0) {
      Double temp = new Double(optionString);
      setCutoff(temp.doubleValue());
    }
    else {
      m_cutoff = (0.01D * m_normal);
    }
    
    super.setOptions(options);
  }
  




  public String acuityTipText()
  {
    return "set the minimum standard deviation for numeric attributes";
  }
  



  public void setAcuity(double a)
  {
    m_acuity = a;
  }
  



  public double getAcuity()
  {
    return m_acuity;
  }
  




  public String cutoffTipText()
  {
    return "set the category utility threshold by which to prune nodes";
  }
  



  public void setCutoff(double c)
  {
    m_cutoff = c;
  }
  



  public double getCutoff()
  {
    return m_cutoff;
  }
  




  public String saveInstanceDataTipText()
  {
    return "save instance information for visualization purposes";
  }
  





  public boolean getSaveInstanceData()
  {
    return m_saveInstances;
  }
  





  public void setSaveInstanceData(boolean newsaveInstances)
  {
    m_saveInstances = newsaveInstances;
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-A");
    result.add("" + m_acuity);
    result.add("-C");
    result.add("" + m_cutoff);
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    if (m_cobwebTree == null) {
      return "Cobweb hasn't been built yet!";
    }
    
    m_cobwebTree.dumpTree(0, text);
    return "Number of merges: " + m_numberMerges + "\nNumber of splits: " + m_numberSplits + "\nNumber of clusters: " + numberOfClusters() + "\n" + text.toString() + "\n\n";
  }
  









  public int graphType()
  {
    return 1;
  }
  




  public String graph()
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    text.append("digraph CobwebTree {\n");
    m_cobwebTree.graphTree(text);
    text.append("}\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6790 $");
  }
  





  public String seedTipText()
  {
    String result = super.seedTipText() + " Use -1 for no randomization.";
    
    return result;
  }
  




  public static void main(String[] argv)
  {
    runClusterer(new Cobweb(), argv);
  }
}
