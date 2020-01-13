package weka.associations;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;







































































































public class FPGrowth
  extends AbstractAssociator
  implements OptionHandler, TechnicalInformationHandler
{
  private static final long serialVersionUID = 3620717108603442911L;
  
  public static class BinaryItem
    implements Serializable, Comparable<BinaryItem>
  {
    private static final long serialVersionUID = -3372941834914147669L;
    protected int m_frequency;
    protected Attribute m_attribute;
    protected int m_valueIndex;
    
    public BinaryItem(Attribute att, int valueIndex)
      throws Exception
    {
      if ((att.isNumeric()) || ((att.isNominal()) && (att.numValues() > 2))) {
        throw new Exception("BinaryItem must be constructed using a nominal attribute with at most 2 values!");
      }
      
      m_attribute = att;
      if (m_attribute.numValues() == 1) {
        m_valueIndex = 0;
      } else {
        m_valueIndex = valueIndex;
      }
    }
    




    public void increaseFrequency(int f)
    {
      m_frequency += f;
    }
    




    public void decreaseFrequency(int f)
    {
      m_frequency -= f;
    }
    


    public void increaseFrequency()
    {
      m_frequency += 1;
    }
    


    public void decreaseFrequency()
    {
      m_frequency -= 1;
    }
    




    public int getFrequency()
    {
      return m_frequency;
    }
    




    public Attribute getAttribute()
    {
      return m_attribute;
    }
    




    public int getValueIndex()
    {
      return m_valueIndex;
    }
    




    public String toString()
    {
      return toString(false);
    }
    





    public String toString(boolean freq)
    {
      String result = m_attribute.name() + "=" + m_attribute.value(m_valueIndex);
      if (freq) {
        result = result + ":" + m_frequency;
      }
      return result;
    }
    
    public String toXML() {
      String result = "<ITEM name=\"" + m_attribute.name() + "\" value=\"=" + m_attribute.value(m_valueIndex) + "\"/>";
      

      return result;
    }
    





    public int compareTo(BinaryItem comp)
    {
      if (m_frequency == comp.getFrequency())
      {
        return -1 * m_attribute.name().compareTo(comp.getAttribute().name());
      }
      if (comp.getFrequency() < m_frequency) {
        return -1;
      }
      return 1;
    }
    
    public boolean equals(Object compareTo) {
      if (!(compareTo instanceof BinaryItem)) {
        return false;
      }
      
      BinaryItem b = (BinaryItem)compareTo;
      if ((m_attribute.equals(b.getAttribute())) && (m_frequency == b.getFrequency())) {
        return true;
      }
      
      return false;
    }
    
    public int hashCode() {
      return (m_attribute.name().hashCode() ^ m_attribute.numValues()) * m_frequency;
    }
  }
  




  protected static class FrequentBinaryItemSet
    implements Serializable, Cloneable
  {
    private static final long serialVersionUID = -6543815873565829448L;
    


    protected ArrayList<FPGrowth.BinaryItem> m_items = new ArrayList();
    



    protected int m_support;
    



    public FrequentBinaryItemSet(ArrayList<FPGrowth.BinaryItem> items, int support)
    {
      m_items = items;
      m_support = support;
      Collections.sort(m_items);
    }
    




    public void addItem(FPGrowth.BinaryItem i)
    {
      m_items.add(i);
      Collections.sort(m_items);
    }
    




    public void setSupport(int support)
    {
      m_support = support;
    }
    




    public int getSupport()
    {
      return m_support;
    }
    




    public Collection<FPGrowth.BinaryItem> getItems()
    {
      return m_items;
    }
    





    public FPGrowth.BinaryItem getItem(int index)
    {
      return (FPGrowth.BinaryItem)m_items.get(index);
    }
    




    public int numberOfItems()
    {
      return m_items.size();
    }
    




    public String toString()
    {
      StringBuffer buff = new StringBuffer();
      Iterator<FPGrowth.BinaryItem> i = m_items.iterator();
      
      while (i.hasNext()) {
        buff.append(((FPGrowth.BinaryItem)i.next()).toString() + " ");
      }
      buff.append(": " + m_support);
      return buff.toString();
    }
    




    public Object clone()
    {
      ArrayList<FPGrowth.BinaryItem> items = new ArrayList(m_items);
      return new FrequentBinaryItemSet(items, m_support);
    }
  }
  



  protected static class FrequentItemSets
    implements Serializable
  {
    private static final long serialVersionUID = 4173606872363973588L;
    

    protected ArrayList<FPGrowth.FrequentBinaryItemSet> m_sets = new ArrayList();
    



    protected int m_numberOfTransactions;
    



    public FrequentItemSets(int numTransactions)
    {
      m_numberOfTransactions = numTransactions;
    }
    





    public FPGrowth.FrequentBinaryItemSet getItemSet(int index)
    {
      return (FPGrowth.FrequentBinaryItemSet)m_sets.get(index);
    }
    




    public Iterator<FPGrowth.FrequentBinaryItemSet> iterator()
    {
      return m_sets.iterator();
    }
    





    public int getNumberOfTransactions()
    {
      return m_numberOfTransactions;
    }
    




    public void addItemSet(FPGrowth.FrequentBinaryItemSet setToAdd)
    {
      m_sets.add(setToAdd);
    }
    




    public void sort(Comparator<FPGrowth.FrequentBinaryItemSet> comp)
    {
      Collections.sort(m_sets, comp);
    }
    




    public int size()
    {
      return m_sets.size();
    }
    



    public void sort()
    {
      Comparator<FPGrowth.FrequentBinaryItemSet> compF = new Comparator() {
        public int compare(FPGrowth.FrequentBinaryItemSet one, FPGrowth.FrequentBinaryItemSet two) {
          Collection<FPGrowth.BinaryItem> compOne = one.getItems();
          Collection<FPGrowth.BinaryItem> compTwo = two.getItems();
          


          if (compOne.size() < compTwo.size())
            return -1;
          if (compOne.size() > compTwo.size()) {
            return 1;
          }
          
          Iterator<FPGrowth.BinaryItem> twoIterator = compTwo.iterator();
          for (FPGrowth.BinaryItem oneI : compOne) {
            FPGrowth.BinaryItem twoI = (FPGrowth.BinaryItem)twoIterator.next();
            int result = oneI.compareTo(twoI);
            if (result != 0) {
              return result;
            }
          }
          return 0;




        }
        





      };
      sort(compF);
    }
    





    public String toString(int numSets)
    {
      if (m_sets.size() == 0) {
        return "No frequent items sets found!";
      }
      
      StringBuffer result = new StringBuffer();
      result.append("" + m_sets.size() + " frequent item sets found");
      if (numSets > 0) {
        result.append(" , displaying " + numSets);
      }
      result.append(":\n\n");
      
      int count = 0;
      for (FPGrowth.FrequentBinaryItemSet i : m_sets) {
        if ((numSets > 0) && (count > numSets)) {
          break;
        }
        result.append(i.toString() + "\n");
        count++;
      }
      
      return result.toString();
    }
  }
  



  protected static class ShadowCounts
    implements Serializable
  {
    private static final long serialVersionUID = 4435433714185969155L;
    


    private ArrayList<Integer> m_counts = new ArrayList();
    


    protected ShadowCounts() {}
    

    public int getCount(int recursionLevel)
    {
      if (recursionLevel >= m_counts.size()) {
        return 0;
      }
      return ((Integer)m_counts.get(recursionLevel)).intValue();
    }
    










    public void increaseCount(int recursionLevel, int incr)
    {
      if (recursionLevel == m_counts.size())
      {
        m_counts.add(Integer.valueOf(incr));
      } else if (recursionLevel == m_counts.size() - 1)
      {
        int n = ((Integer)m_counts.get(recursionLevel)).intValue();
        m_counts.set(recursionLevel, Integer.valueOf(n + incr));
      }
    }
    




    public void removeCount(int recursionLevel)
    {
      if (recursionLevel < m_counts.size()) {
        m_counts.remove(recursionLevel);
      }
    }
  }
  



  protected static class FPTreeNode
    implements Serializable
  {
    private static final long serialVersionUID = 4396315323673737660L;
    

    protected FPTreeNode m_levelSibling;
    

    protected FPTreeNode m_parent;
    

    protected FPGrowth.BinaryItem m_item;
    

    protected int m_ID;
    

    protected Map<FPGrowth.BinaryItem, FPTreeNode> m_children = new HashMap();
    


    protected FPGrowth.ShadowCounts m_projectedCounts = new FPGrowth.ShadowCounts();
    





    public FPTreeNode(FPTreeNode parent, FPGrowth.BinaryItem item)
    {
      m_parent = parent;
      m_item = item;
    }
    










    public void addItemSet(Collection<FPGrowth.BinaryItem> itemSet, Map<FPGrowth.BinaryItem, FPGrowth.FPTreeRoot.Header> headerTable, int incr)
    {
      Iterator<FPGrowth.BinaryItem> i = itemSet.iterator();
      
      if (i.hasNext()) {
        FPGrowth.BinaryItem first = (FPGrowth.BinaryItem)i.next();
        
        FPTreeNode aChild;
        if (!m_children.containsKey(first))
        {
          FPTreeNode aChild = new FPTreeNode(this, first);
          m_children.put(first, aChild);
          

          if (!headerTable.containsKey(first)) {
            headerTable.put(first, new FPGrowth.FPTreeRoot.Header());
          }
          

          ((FPGrowth.FPTreeRoot.Header)headerTable.get(first)).addToList(aChild);
        }
        else {
          aChild = (FPTreeNode)m_children.get(first);
        }
        

        ((FPGrowth.FPTreeRoot.Header)headerTable.get(first)).getProjectedCounts().increaseCount(0, incr);
        

        aChild.increaseProjectedCount(0, incr);
        

        itemSet.remove(first);
        aChild.addItemSet(itemSet, headerTable, incr);
      }
    }
    







    public void increaseProjectedCount(int recursionLevel, int incr)
    {
      m_projectedCounts.increaseCount(recursionLevel, incr);
    }
    





    public void removeProjectedCount(int recursionLevel)
    {
      m_projectedCounts.removeCount(recursionLevel);
    }
    





    public int getProjectedCount(int recursionLevel)
    {
      return m_projectedCounts.getCount(recursionLevel);
    }
    




    public FPTreeNode getParent()
    {
      return m_parent;
    }
    




    public FPGrowth.BinaryItem getItem()
    {
      return m_item;
    }
    






    public String toString(int recursionLevel)
    {
      return toString("", recursionLevel);
    }
    







    public String toString(String prefix, int recursionLevel)
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append(prefix);
      buffer.append("|  ");
      buffer.append(m_item.toString());
      buffer.append(" (");
      buffer.append(m_projectedCounts.getCount(recursionLevel));
      buffer.append(")\n");
      
      for (FPTreeNode node : m_children.values()) {
        buffer.append(node.toString(prefix + "|  ", recursionLevel));
      }
      return buffer.toString();
    }
    
    protected int assignIDs(int lastID) {
      int currentLastID = lastID + 1;
      m_ID = currentLastID;
      if (m_children != null) {
        Collection<FPTreeNode> kids = m_children.values();
        for (FPTreeNode n : kids) {
          currentLastID = n.assignIDs(currentLastID);
        }
      }
      return currentLastID;
    }
    





    public void graphFPTree(StringBuffer text)
    {
      if (m_children != null) {
        Collection<FPTreeNode> kids = m_children.values();
        for (FPTreeNode n : kids) {
          text.append("N" + m_ID);
          text.append(" [label=\"");
          text.append(n.getItem().toString() + " (" + n.getProjectedCount(0) + ")\\n");
          text.append("\"]\n");
          n.graphFPTree(text);
          text.append("N" + m_ID + "->" + "N" + m_ID + "\n");
        }
      }
    }
  }
  



  private static class FPTreeRoot
    extends FPGrowth.FPTreeNode
  {
    private static final long serialVersionUID = 632150939785333297L;
    



    protected static class Header
      implements Serializable
    {
      private static final long serialVersionUID = -6583156284891368909L;
      

      protected List<FPGrowth.FPTreeNode> m_headerList = new LinkedList();
      

      protected FPGrowth.ShadowCounts m_projectedHeaderCounts = new FPGrowth.ShadowCounts();
      

      protected Header() {}
      

      public void addToList(FPGrowth.FPTreeNode toAdd)
      {
        m_headerList.add(toAdd);
      }
      




      public List<FPGrowth.FPTreeNode> getHeaderList()
      {
        return m_headerList;
      }
      




      public FPGrowth.ShadowCounts getProjectedCounts()
      {
        return m_projectedHeaderCounts;
      }
    }
    

    protected Map<FPGrowth.BinaryItem, Header> m_headerTable = new HashMap();
    



    public FPTreeRoot()
    {
      super(null);
    }
    





    public void addItemSet(Collection<FPGrowth.BinaryItem> itemSet, int incr)
    {
      super.addItemSet(itemSet, m_headerTable, incr);
    }
    




    public Map<FPGrowth.BinaryItem, Header> getHeaderTable()
    {
      return m_headerTable;
    }
    
    public boolean isEmpty(int recursionLevel) {
      for (FPGrowth.FPTreeNode c : m_children.values()) {
        if (c.getProjectedCount(recursionLevel) > 0) {
          return false;
        }
      }
      return true;
    }
    







    public String toString(String pad, int recursionLevel)
    {
      StringBuffer result = new StringBuffer();
      result.append(pad);
      result.append("+ ROOT\n");
      
      for (FPGrowth.FPTreeNode node : m_children.values()) {
        result.append(node.toString(pad + "|  ", recursionLevel));
      }
      return result.toString();
    }
    






    public String printHeaderTable(int recursionLevel)
    {
      StringBuffer buffer = new StringBuffer();
      for (FPGrowth.BinaryItem item : m_headerTable.keySet()) {
        buffer.append(item.toString());
        buffer.append(" : ");
        buffer.append(((Header)m_headerTable.get(item)).getProjectedCounts().getCount(recursionLevel));
        buffer.append("\n");
      }
      return buffer.toString();
    }
    
    public void graphHeaderTable(StringBuffer text, int maxID)
    {
      for (FPGrowth.BinaryItem item : m_headerTable.keySet()) {
        Header h = (Header)m_headerTable.get(item);
        List<FPGrowth.FPTreeNode> headerList = h.getHeaderList();
        if (headerList.size() > 1) {
          text.append("N" + maxID + " [label=\"" + ((FPGrowth.FPTreeNode)headerList.get(0)).getItem().toString() + " (" + h.getProjectedCounts().getCount(0) + ")" + "\" shape=plaintext]\n");
          


          text.append("N" + maxID + "->" + "N" + get1m_ID + "\n");
          for (int i = 1; i < headerList.size() - 1; i++) {
            text.append("N" + getm_ID + "->" + "N" + get1m_ID + "\n");
          }
          maxID++;
        }
      }
    }
  }
  






  public static class AssociationRule
    implements Serializable, Comparable<AssociationRule>
  {
    private static final long serialVersionUID = -661269018702294489L;
    





    public static abstract enum METRIC_TYPE
    {
      CONFIDENCE("conf"), 
      





      LIFT("lift"), 
      









      LEVERAGE("lev"), 
      










      CONVICTION("conv");
      




      private final String m_stringVal;
      




      private METRIC_TYPE(String name)
      {
        m_stringVal = name;
      }
      
      abstract double compute(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
      
      public String toString()
      {
        return m_stringVal;
      }
      
      public String toStringMetric(int premiseSupport, int consequenceSupport, int totalSupport, int totalTransactions)
      {
        return m_stringVal + ":(" + Utils.doubleToString(compute(premiseSupport, consequenceSupport, totalSupport, totalTransactions), 2) + ")";
      }
      

      public String toXML(int premiseSupport, int consequenceSupport, int totalSupport, int totalTransactions)
      {
        String result = "<CRITERE name=\"" + m_stringVal + "\" value=\" " + Utils.doubleToString(compute(premiseSupport, consequenceSupport, totalSupport, totalTransactions), 2) + "\"/>";
        


        return result;
      }
    }
    

    public static final Tag[] TAGS_SELECTION = { new Tag(METRIC_TYPE.CONFIDENCE.ordinal(), "Confidence"), new Tag(METRIC_TYPE.LIFT.ordinal(), "Lift"), new Tag(METRIC_TYPE.LEVERAGE.ordinal(), "Leverage"), new Tag(METRIC_TYPE.CONVICTION.ordinal(), "Conviction") };
    






    protected METRIC_TYPE m_metricType = METRIC_TYPE.CONFIDENCE;
    



    protected Collection<FPGrowth.BinaryItem> m_premise;
    



    protected Collection<FPGrowth.BinaryItem> m_consequence;
    



    protected int m_premiseSupport;
    



    protected int m_consequenceSupport;
    


    protected int m_totalSupport;
    


    protected int m_totalTransactions;
    



    public AssociationRule(Collection<FPGrowth.BinaryItem> premise, Collection<FPGrowth.BinaryItem> consequence, METRIC_TYPE metric, int premiseSupport, int consequenceSupport, int totalSupport, int totalTransactions)
    {
      m_premise = premise;
      m_consequence = consequence;
      m_metricType = metric;
      m_premiseSupport = premiseSupport;
      m_consequenceSupport = consequenceSupport;
      m_totalSupport = totalSupport;
      m_totalTransactions = totalTransactions;
    }
    




    public Collection<FPGrowth.BinaryItem> getPremise()
    {
      return m_premise;
    }
    




    public Collection<FPGrowth.BinaryItem> getConsequence()
    {
      return m_consequence;
    }
    




    public METRIC_TYPE getMetricType()
    {
      return m_metricType;
    }
    




    public double getMetricValue()
    {
      return m_metricType.compute(m_premiseSupport, m_consequenceSupport, m_totalSupport, m_totalTransactions);
    }
    





    public int getPremiseSupport()
    {
      return m_premiseSupport;
    }
    




    public int getConsequenceSupport()
    {
      return m_consequenceSupport;
    }
    




    public int getTotalSupport()
    {
      return m_totalSupport;
    }
    




    public int getTotalTransactions()
    {
      return m_totalTransactions;
    }
    





    public int compareTo(AssociationRule other)
    {
      return -Double.compare(getMetricValue(), other.getMetricValue());
    }
    




    public boolean equals(Object other)
    {
      if (!(other instanceof AssociationRule)) {
        return false;
      }
      
      AssociationRule otherRule = (AssociationRule)other;
      boolean result = (m_premise.equals(otherRule.getPremise())) && (m_consequence.equals(otherRule.getConsequence())) && (getMetricValue() == otherRule.getMetricValue());
      


      return result;
    }
    
    public boolean containsItems(ArrayList<Attribute> items, boolean useOr) {
      int numItems = items.size();
      int count = 0;
      
      for (FPGrowth.BinaryItem i : m_premise) {
        if (items.contains(i.getAttribute())) {
          if (useOr) {
            return true;
          }
          count++;
        }
      }
      

      for (FPGrowth.BinaryItem i : m_consequence) {
        if (items.contains(i.getAttribute())) {
          if (useOr) {
            return true;
          }
          count++;
        }
      }
      

      if ((!useOr) && 
        (count == numItems)) {
        return true;
      }
      

      return false;
    }
    




    public String toString()
    {
      StringBuffer result = new StringBuffer();
      
      result.append(m_premise.toString() + ": " + m_premiseSupport + " ==> " + m_consequence.toString() + ": " + m_totalSupport + "   ");
      

      for (METRIC_TYPE m : METRIC_TYPE.values()) {
        if (m.equals(m_metricType)) {
          result.append("<" + m.toStringMetric(m_premiseSupport, m_consequenceSupport, m_totalSupport, m_totalTransactions) + "> ");
        }
        else
        {
          result.append("" + m.toStringMetric(m_premiseSupport, m_consequenceSupport, m_totalSupport, m_totalTransactions) + " ");
        }
      }
      

      return result.toString();
    }
    
    public String toXML() {
      StringBuffer result = new StringBuffer();
      result.append("  <RULE>\n    <LHS>");
      
      for (FPGrowth.BinaryItem b : m_premise) {
        result.append("\n      ");
        result.append(b.toXML());
      }
      result.append("\n    </LHS>\n    <RHS>");
      
      for (FPGrowth.BinaryItem b : m_consequence) {
        result.append("\n      ");
        result.append(b.toXML());
      }
      result.append("\n    </RHS>");
      


      result.append("\n    <CRITERE name=\"support\" value=\"" + m_totalSupport + "\"/>");
      
      for (METRIC_TYPE m : METRIC_TYPE.values()) {
        result.append("\n    ");
        result.append(m.toXML(m_premiseSupport, m_consequenceSupport, m_totalSupport, m_totalTransactions));
      }
      
      result.append("\n  </RULE>\n");
      
      return result.toString();
    }
    
    private static void nextSubset(boolean[] subset) {
      for (int i = 0; i < subset.length; i++) {
        if (subset[i] == 0) {
          subset[i] = true;
          break;
        }
        subset[i] = false;
      }
    }
    

    private static Collection<FPGrowth.BinaryItem> getPremise(FPGrowth.FrequentBinaryItemSet fis, boolean[] subset)
    {
      boolean ok = false;
      for (int i = 0; i < subset.length; i++) {
        if (subset[i] == 0) {
          ok = true;
          break;
        }
      }
      
      if (!ok) {
        return null;
      }
      
      List<FPGrowth.BinaryItem> premise = new ArrayList();
      ArrayList<FPGrowth.BinaryItem> items = new ArrayList(fis.getItems());
      

      for (int i = 0; i < subset.length; i++) {
        if (subset[i] != 0) {
          premise.add(items.get(i));
        }
      }
      return premise;
    }
    
    private static Collection<FPGrowth.BinaryItem> getConsequence(FPGrowth.FrequentBinaryItemSet fis, boolean[] subset)
    {
      List<FPGrowth.BinaryItem> consequence = new ArrayList();
      ArrayList<FPGrowth.BinaryItem> items = new ArrayList(fis.getItems());
      
      for (int i = 0; i < subset.length; i++) {
        if (subset[i] == 0) {
          consequence.add(items.get(i));
        }
      }
      return consequence;
    }
    

















    public static List<AssociationRule> generateRulesBruteForce(FPGrowth.FrequentItemSets largeItemSets, METRIC_TYPE metricToUse, double metricThreshold, int upperBoundMinSuppAsInstances, int lowerBoundMinSuppAsInstances, int totalTransactions)
    {
      List<AssociationRule> rules = new ArrayList();
      largeItemSets.sort();
      Map<Collection<FPGrowth.BinaryItem>, Integer> frequencyLookup = new HashMap();
      

      Iterator<FPGrowth.FrequentBinaryItemSet> setI = largeItemSets.iterator();
      
      while (setI.hasNext()) {
        FPGrowth.FrequentBinaryItemSet fis = (FPGrowth.FrequentBinaryItemSet)setI.next();
        frequencyLookup.put(fis.getItems(), Integer.valueOf(fis.getSupport()));
        if (fis.getItems().size() > 1)
        {
          boolean[] subset = new boolean[fis.getItems().size()];
          Collection<FPGrowth.BinaryItem> premise = null;
          Collection<FPGrowth.BinaryItem> consequence = null;
          while ((premise = getPremise(fis, subset)) != null) {
            if ((premise.size() > 0) && (premise.size() < fis.getItems().size())) {
              consequence = getConsequence(fis, subset);
              int totalSupport = fis.getSupport();
              int supportPremise = ((Integer)frequencyLookup.get(premise)).intValue();
              int supportConsequence = ((Integer)frequencyLookup.get(consequence)).intValue();
              

              AssociationRule candidate = new AssociationRule(premise, consequence, metricToUse, supportPremise, supportConsequence, totalSupport, totalTransactions);
              

              if ((candidate.getMetricValue() > metricThreshold) && (candidate.getTotalSupport() >= lowerBoundMinSuppAsInstances) && (candidate.getTotalSupport() <= upperBoundMinSuppAsInstances))
              {


                rules.add(candidate);
              }
            }
            nextSubset(subset);
          }
        }
      }
      return rules;
    }
    
    public static List<AssociationRule> pruneRules(List<AssociationRule> rulesToPrune, ArrayList<Attribute> itemsToConsider, boolean useOr)
    {
      ArrayList<AssociationRule> result = new ArrayList();
      
      for (AssociationRule r : rulesToPrune) {
        if (r.containsItems(itemsToConsider, useOr)) {
          result.add(r);
        }
      }
      
      return result;
    }
  }
  

  protected int m_numRulesToFind = 10;
  


  protected double m_upperBoundMinSupport = 1.0D;
  

  protected double m_lowerBoundMinSupport = 0.1D;
  

  protected double m_delta = 0.05D;
  






  protected boolean m_findAllRulesForSupportLevel = false;
  



  protected int m_positiveIndex = 2;
  
  protected FPGrowth.AssociationRule.METRIC_TYPE m_metric = FPGrowth.AssociationRule.METRIC_TYPE.CONFIDENCE;
  

  protected double m_metricThreshold = 0.9D;
  

  protected FrequentItemSets m_largeItemSets;
  

  protected List<AssociationRule> m_rules;
  

  protected int m_maxItems = -1;
  




  protected String m_transactionsMustContain = "";
  

  protected boolean m_mustContainOR = false;
  

  protected String m_rulesMustContain = "";
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    



    result.enable(Capabilities.Capability.UNARY_ATTRIBUTES);
    result.enable(Capabilities.Capability.BINARY_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  





  public String globalInfo()
  {
    return "Class implementing the FP-growth algorithm for finding large item sets without candidate generation. Iteratively reduces the minimum support until it finds the required number of rules with the given minimum metric. For more information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "J. Han and J.Pei and Y. Yin");
    result.setValue(TechnicalInformation.Field.TITLE, "Mining frequent patterns without candidate generation");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 2000 ACM-SIGMID International Conference on Management of Data");
    
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.PAGES, "1-12");
    
    return result;
  }
  


  private boolean passesMustContain(Instance inst, boolean[] transactionsMustContainIndexes, int numInTransactionsMustContainList)
  {
    boolean result = false;
    
    if ((inst instanceof SparseInstance)) {
      int containsCount = 0;
      for (int i = 0; i < inst.numValues(); i++) {
        int attIndex = inst.index(i);
        
        if (m_mustContainOR) {
          if (transactionsMustContainIndexes[attIndex] != 0)
          {

            return true;
          }
        }
        else if (transactionsMustContainIndexes[attIndex] != 0) {
          containsCount++;
        }
      }
      

      if ((!m_mustContainOR) && 
        (containsCount == numInTransactionsMustContainList)) {
        return true;
      }
    }
    else {
      int containsCount = 0;
      for (int i = 0; i < transactionsMustContainIndexes.length; i++) {
        if ((transactionsMustContainIndexes[i] != 0) && 
          ((int)inst.value(i) == m_positiveIndex - 1)) {
          if (m_mustContainOR)
          {


            return true;
          }
          containsCount++;
        }
      }
      


      if ((!m_mustContainOR) && 
        (containsCount == numInTransactionsMustContainList)) {
        return true;
      }
    }
    

    return result;
  }
  






  protected ArrayList<BinaryItem> getSingletons(Instances data)
    throws Exception
  {
    ArrayList<BinaryItem> singletons = new ArrayList();
    
    for (int i = 0; i < data.numAttributes(); i++) {
      singletons.add(new BinaryItem(data.attribute(i), m_positiveIndex - 1));
    }
    
    for (int i = 0; i < data.numInstances(); i++) {
      Instance current = data.instance(i);
      if ((current instanceof SparseInstance)) {
        for (int j = 0; j < current.numValues(); j++) {
          int attIndex = current.index(j);
          ((BinaryItem)singletons.get(attIndex)).increaseFrequency();
        }
      } else {
        for (int j = 0; j < data.numAttributes(); j++) {
          if ((!current.isMissing(j)) && (
            (current.attribute(j).numValues() == 1) || (current.value(j) == m_positiveIndex - 1)))
          {
            ((BinaryItem)singletons.get(j)).increaseFrequency();
          }
        }
      }
    }
    

    return singletons;
  }
  
























  protected FPTreeRoot buildFPTree(ArrayList<BinaryItem> singletons, Instances data, int minSupport)
  {
    FPTreeRoot tree = new FPTreeRoot();
    
    for (int i = 0; i < data.numInstances(); i++) {
      Instance current = data.instance(i);
      ArrayList<BinaryItem> transaction = new ArrayList();
      if ((current instanceof SparseInstance)) {
        for (int j = 0; j < current.numValues(); j++) {
          int attIndex = current.index(j);
          if (((BinaryItem)singletons.get(attIndex)).getFrequency() >= minSupport) {
            transaction.add(singletons.get(attIndex));
          }
        }
        Collections.sort(transaction);
        tree.addItemSet(transaction, 1);
      } else {
        for (int j = 0; j < data.numAttributes(); j++) {
          if ((!current.isMissing(j)) && (
            (current.attribute(j).numValues() == 1) || (current.value(j) == m_positiveIndex - 1)))
          {
            if (((BinaryItem)singletons.get(j)).getFrequency() >= minSupport) {
              transaction.add(singletons.get(j));
            }
          }
        }
        
        Collections.sort(transaction);
        tree.addItemSet(transaction, 1);
      }
    }
    
    return tree;
  }
  












  protected void mineTree(FPTreeRoot tree, FrequentItemSets largeItemSets, int recursionLevel, FrequentBinaryItemSet conditionalItems, int minSupport)
  {
    if (!tree.isEmpty(recursionLevel)) {
      if ((m_maxItems > 0) && (recursionLevel >= m_maxItems))
      {
        return;
      }
      
      Map<BinaryItem, FPGrowth.FPTreeRoot.Header> headerTable = tree.getHeaderTable();
      Set<BinaryItem> keys = headerTable.keySet();
      
      Iterator<BinaryItem> i = keys.iterator();
      while (i.hasNext()) {
        BinaryItem item = (BinaryItem)i.next();
        FPGrowth.FPTreeRoot.Header itemHeader = (FPGrowth.FPTreeRoot.Header)headerTable.get(item);
        

        int support = itemHeader.getProjectedCounts().getCount(recursionLevel);
        if (support >= minSupport)
        {
          for (FPTreeNode n : itemHeader.getHeaderList())
          {
            int currentCount = n.getProjectedCount(recursionLevel);
            if (currentCount > 0) {
              FPTreeNode temp = n.getParent();
              while (temp != tree)
              {
                temp.increaseProjectedCount(recursionLevel + 1, currentCount);
                

                ((FPGrowth.FPTreeRoot.Header)headerTable.get(temp.getItem())).getProjectedCounts().increaseCount(recursionLevel + 1, currentCount);
                

                temp = temp.getParent();
              }
            }
          }
          
          FrequentBinaryItemSet newConditional = (FrequentBinaryItemSet)conditionalItems.clone();
          


          newConditional.addItem(item);
          newConditional.setSupport(support);
          

          largeItemSets.addItemSet(newConditional);
          

          mineTree(tree, largeItemSets, recursionLevel + 1, newConditional, minSupport);
          


          for (FPTreeNode n : itemHeader.getHeaderList()) {
            FPTreeNode temp = n.getParent();
            while (temp != tree) {
              temp.removeProjectedCount(recursionLevel + 1);
              temp = temp.getParent();
            }
          }
          


          for (FPGrowth.FPTreeRoot.Header h : headerTable.values()) {
            h.getProjectedCounts().removeCount(recursionLevel + 1);
          }
        }
      }
    }
  }
  


  public FPGrowth()
  {
    resetOptions();
  }
  


  public void resetOptions()
  {
    m_delta = 0.05D;
    m_metricThreshold = 0.9D;
    m_numRulesToFind = 10;
    m_lowerBoundMinSupport = 0.1D;
    m_upperBoundMinSupport = 1.0D;
    
    m_positiveIndex = 2;
    m_transactionsMustContain = "";
    m_rulesMustContain = "";
    m_mustContainOR = false;
  }
  





  public String positiveIndexTipText()
  {
    return "Set the index of binary valued attributes that is to be considered the positive index. Has no effect for sparse data (in this case the first index (i.e. non-zero values) is always treated as  positive. Also has no effect for unary valued attributes (i.e. when using the Weka Apriori-style format for market basket data, which uses missing value \"?\" to indicate absence of an item.";
  }
  












  public void setPositiveIndex(int index)
  {
    m_positiveIndex = index;
  }
  






  public int getPositiveIndex()
  {
    return m_positiveIndex;
  }
  




  public void setNumRulesToFind(int numR)
  {
    m_numRulesToFind = numR;
  }
  




  public int getNumRulesToFind()
  {
    return m_numRulesToFind;
  }
  





  public String numRulesToFindTipText()
  {
    return "The number of rules to output";
  }
  




  public void setMetricType(SelectedTag d)
  {
    int ordinal = d.getSelectedTag().getID();
    for (FPGrowth.AssociationRule.METRIC_TYPE m : FPGrowth.AssociationRule.METRIC_TYPE.values()) {
      if (m.ordinal() == ordinal) {
        m_metric = m;
        break;
      }
    }
  }
  




  public void setMaxNumberOfItems(int max)
  {
    m_maxItems = max;
  }
  




  public int getMaxNumberOfItems()
  {
    return m_maxItems;
  }
  





  public String maxNumberOfItemsTipText()
  {
    return "The maximum number of items to include in frequent item sets. -1 means no limit.";
  }
  





  public SelectedTag getMetricType()
  {
    return new SelectedTag(m_metric.ordinal(), AssociationRule.TAGS_SELECTION);
  }
  





  public String metricTypeTipText()
  {
    return "Set the type of metric by which to rank rules. Confidence is the proportion of the examples covered by the premise that are also covered by the consequence(Class association rules can only be mined using confidence). Lift is confidence divided by the proportion of all examples that are covered by the consequence. This is a measure of the importance of the association that is independent of support. Leverage is the proportion of additional examples covered by both the premise and consequence above those expected if the premise and consequence were independent of each other. The total number of examples that this represents is presented in brackets following the leverage. Conviction is another measure of departure from independence.";
  }
  















  public String minMetricTipText()
  {
    return "Minimum metric score. Consider only rules with scores higher than this value.";
  }
  






  public double getMinMetric()
  {
    return m_metricThreshold;
  }
  





  public void setMinMetric(double v)
  {
    m_metricThreshold = v;
  }
  





  public String transactionsMustContainTipText()
  {
    return "Limit input to FPGrowth to those transactions (instances) that contain these items. Provide a comma separated list of attribute names.";
  }
  









  public void setTransactionsMustContain(String list)
  {
    m_transactionsMustContain = list;
  }
  







  public String getTransactionsMustContain()
  {
    return m_transactionsMustContain;
  }
  





  public String rulesMustContainTipText()
  {
    return "Only print rules that contain these items. Provide a comma separated list of attribute names.";
  }
  







  public void setRulesMustContain(String list)
  {
    m_rulesMustContain = list;
  }
  






  public String getRulesMustContain()
  {
    return m_rulesMustContain;
  }
  





  public String useORForMustContainListTipText()
  {
    return "Use OR instead of AND for transactions/rules must contain lists.";
  }
  






  public void setUseORForMustContainList(boolean b)
  {
    m_mustContainOR = b;
  }
  





  public boolean getUseORForMustContainList()
  {
    return m_mustContainOR;
  }
  




  public String deltaTipText()
  {
    return "Iteratively decrease support by this factor. Reduces support until min support is reached or required number of rules has been generated.";
  }
  







  public double getDelta()
  {
    return m_delta;
  }
  





  public void setDelta(double v)
  {
    m_delta = v;
  }
  




  public String lowerBoundMinSupportTipText()
  {
    return "Lower bound for minimum support as a fraction or number of instances.";
  }
  





  public double getLowerBoundMinSupport()
  {
    return m_lowerBoundMinSupport;
  }
  





  public void setLowerBoundMinSupport(double v)
  {
    m_lowerBoundMinSupport = v;
  }
  




  public String upperBoundMinSupportTipText()
  {
    return "Upper bound for minimum support as a fraction ornumber of instances. Start iteratively decreasing minimum support from this value.";
  }
  







  public double getUpperBoundMinSupport()
  {
    return m_upperBoundMinSupport;
  }
  





  public void setUpperBoundMinSupport(double v)
  {
    m_upperBoundMinSupport = v;
  }
  





  public String findAllRulesForSupportLevelTipText()
  {
    return "Find all rules that meet the lower bound on minimum support and the minimum metric constraint. Turning this mode on will disable the iterative support reduction procedure to find the specified number of rules.";
  }
  











  public void setFindAllRulesForSupportLevel(boolean s)
  {
    m_findAllRulesForSupportLevel = s;
  }
  






  public boolean getFindAllRulesForSupportLevel()
  {
    return m_findAllRulesForSupportLevel;
  }
  













  public List<AssociationRule> getAssociationRules()
  {
    return m_rules;
  }
  




  public Enumeration<Option> listOptions()
  {
    Vector<Option> newVector = new Vector();
    
    String string00 = "\tSet the index of the attribute value to consider as 'positive'\n\tfor binary attributes in normal dense instances. Index 2 is always\n\tused for sparse instances. (default = 2)";
    

    String string0 = "\tThe maximum number of items to include in large items sets (and rules). (default = -1, i.e. no limit.)";
    


    String string1 = "\tThe required number of rules. (default = " + m_numRulesToFind + ")";
    
    String string2 = "\tThe minimum metric score of a rule. (default = " + m_metricThreshold + ")";
    
    String string3 = "\tThe metric by which to rank rules. (default = confidence)";
    
    String string4 = "\tThe lower bound for the minimum support as a fraction or number of instances. (default = " + m_lowerBoundMinSupport + ")";
    

    String string5 = "\tUpper bound for minimum support as a fraction or number of instances. (default = 1.0)";
    
    String string6 = "\tThe delta by which the minimum support is decreased in\n\teach iteration as a fraction or number of instances. (default = " + m_delta + ")";
    
    String string7 = "\tFind all rules that meet the lower bound on\n\tminimum support and the minimum metric constraint.\n\tTurning this mode on will disable the iterative support reduction\n\tprocedure to find the specified number of rules.";
    


    String string8 = "\tOnly consider transactions that contain these items (default = no restriction)";
    String string9 = "\tOnly print rules that contain these items. (default = no restriction)";
    String string10 = "\tUse OR instead of AND for must contain list(s). Use in conjunction\n\twith -transactions and/or -rules";
    

    newVector.add(new Option(string00, "P", 1, "-P <attribute index of positive value>"));
    newVector.add(new Option(string0, "I", 1, "-I <max items>"));
    newVector.add(new Option(string1, "N", 1, "-N <require number of rules>"));
    newVector.add(new Option(string3, "T", 1, "-T <0=confidence | 1=lift | 2=leverage | 3=Conviction>"));
    
    newVector.add(new Option(string2, "C", 1, "-C <minimum metric score of a rule>"));
    newVector.add(new Option(string5, "U", 1, "-U <upper bound for minimum support>"));
    newVector.add(new Option(string4, "M", 1, "-M <lower bound for minimum support>"));
    newVector.add(new Option(string6, "D", 1, "-D <delta for minimum support>"));
    newVector.add(new Option(string7, "S", 0, "-S"));
    newVector.add(new Option(string8, "transactions", 1, "-transactions <comma separated list of attribute names>"));
    
    newVector.add(new Option(string9, "rules", 1, "-rules <comma separated list of attribute names>"));
    
    newVector.add(new Option(string10, "use-or", 0, "-use-or"));
    
    return newVector.elements();
  }
  





















































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    String positiveIndexString = Utils.getOption('P', options);
    String maxItemsString = Utils.getOption('I', options);
    String numRulesString = Utils.getOption('N', options);
    String minMetricString = Utils.getOption('C', options);
    String metricTypeString = Utils.getOption("T", options);
    String lowerBoundSupportString = Utils.getOption("M", options);
    String upperBoundSupportString = Utils.getOption("U", options);
    String deltaString = Utils.getOption("D", options);
    String transactionsString = Utils.getOption("transactions", options);
    String rulesString = Utils.getOption("rules", options);
    
    if (positiveIndexString.length() != 0) {
      setPositiveIndex(Integer.parseInt(positiveIndexString));
    }
    
    if (maxItemsString.length() != 0) {
      setMaxNumberOfItems(Integer.parseInt(maxItemsString));
    }
    
    if (metricTypeString.length() != 0) {
      setMetricType(new SelectedTag(Integer.parseInt(metricTypeString), AssociationRule.TAGS_SELECTION));
    }
    

    if (numRulesString.length() != 0) {
      setNumRulesToFind(Integer.parseInt(numRulesString));
    }
    
    if (minMetricString.length() != 0) {
      setMinMetric(Double.parseDouble(minMetricString));
    }
    
    if (deltaString.length() != 0) {
      setDelta(Double.parseDouble(deltaString));
    }
    
    if (lowerBoundSupportString.length() != 0) {
      setLowerBoundMinSupport(Double.parseDouble(lowerBoundSupportString));
    }
    
    if (upperBoundSupportString.length() != 0) {
      setUpperBoundMinSupport(Double.parseDouble(upperBoundSupportString));
    }
    
    if (transactionsString.length() != 0) {
      setTransactionsMustContain(transactionsString);
    }
    
    if (rulesString.length() > 0) {
      setRulesMustContain(rulesString);
    }
    
    setUseORForMustContainList(Utils.getFlag("use-or", options));
    
    setFindAllRulesForSupportLevel(Utils.getFlag('S', options));
  }
  




  public String[] getOptions()
  {
    ArrayList<String> options = new ArrayList();
    
    options.add("-P");options.add("" + getPositiveIndex());
    options.add("-I");options.add("" + getMaxNumberOfItems());
    options.add("-N");options.add("" + getNumRulesToFind());
    options.add("-T");options.add("" + getMetricType().getSelectedTag().getID());
    options.add("-C");options.add("" + getMinMetric());
    options.add("-D");options.add("" + getDelta());
    options.add("-U");options.add("" + getUpperBoundMinSupport());
    options.add("-M");options.add("" + getLowerBoundMinSupport());
    if (getFindAllRulesForSupportLevel()) {
      options.add("-S");
    }
    
    if (getTransactionsMustContain().length() > 0) {
      options.add("-transactions");options.add(getTransactionsMustContain());
    }
    
    if (getRulesMustContain().length() > 0) {
      options.add("-rules");options.add(getRulesMustContain());
    }
    
    if (getUseORForMustContainList()) {
      options.add("-use-or");
    }
    
    return (String[])options.toArray(new String[1]);
  }
  
  private Instances parseTransactionsMustContain(Instances data) {
    String[] split = m_transactionsMustContain.trim().split(",");
    boolean[] transactionsMustContainIndexes = new boolean[data.numAttributes()];
    int numInTransactionsMustContainList = split.length;
    
    for (int i = 0; i < split.length; i++) {
      String attName = split[i].trim();
      Attribute att = data.attribute(attName);
      if (att == null) {
        System.err.println("[FPGrowth] : WARNING - can't find attribute " + attName + " in the data.");
        
        numInTransactionsMustContainList--;
      } else {
        transactionsMustContainIndexes[att.index()] = true;
      }
    }
    
    if (numInTransactionsMustContainList == 0) {
      return data;
    }
    Instances newInsts = new Instances(data, 0);
    for (int i = 0; i < data.numInstances(); i++) {
      if (passesMustContain(data.instance(i), transactionsMustContainIndexes, numInTransactionsMustContainList))
      {
        newInsts.add(data.instance(i));
      }
    }
    newInsts.compactify();
    return newInsts;
  }
  
  private ArrayList<Attribute> parseRulesMustContain(Instances data)
  {
    ArrayList<Attribute> result = new ArrayList();
    
    String[] split = m_rulesMustContain.trim().split(",");
    
    for (int i = 0; i < split.length; i++) {
      String attName = split[i].trim();
      Attribute att = data.attribute(attName);
      if (att == null) {
        System.err.println("[FPGrowth] : WARNING - can't find attribute " + attName + " in the data.");
      }
      else {
        result.add(att);
      }
    }
    
    return result;
  }
  








  public void buildAssociations(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    boolean breakOnNext = false;
    

    if (m_transactionsMustContain.length() > 0) {
      data = parseTransactionsMustContain(data);
      getCapabilities().testWithFail(data);
    }
    
    ArrayList<Attribute> rulesMustContain = null;
    if (m_rulesMustContain.length() > 0) {
      rulesMustContain = parseRulesMustContain(data);
    }
    

    int upperBoundMinSuppAsInstances = m_upperBoundMinSupport > 1.0D ? (int)m_upperBoundMinSupport : (int)Math.ceil(m_upperBoundMinSupport * data.numInstances());
    


    int lowerBoundMinSuppAsInstances = m_lowerBoundMinSupport > 1.0D ? (int)m_lowerBoundMinSupport : (int)Math.ceil(m_lowerBoundMinSupport * data.numInstances());
    



    double upperBoundMinSuppAsFraction = m_upperBoundMinSupport > 1.0D ? m_upperBoundMinSupport / data.numInstances() : m_upperBoundMinSupport;
    


    double lowerBoundMinSuppAsFraction = m_lowerBoundMinSupport > 1.0D ? m_lowerBoundMinSupport / data.numInstances() : m_lowerBoundMinSupport;
    


    double deltaAsFraction = m_delta > 1.0D ? m_delta / data.numInstances() : m_delta;
    



    double currentSupport = 1.0D;
    
    if (m_findAllRulesForSupportLevel) {
      currentSupport = lowerBoundMinSuppAsFraction;
    }
    
    ArrayList<BinaryItem> singletons = getSingletons(data);
    







    do
    {
      int currentSupportAsInstances = currentSupport > 1.0D ? (int)currentSupport : (int)Math.ceil(currentSupport * data.numInstances());
      






      FPTreeRoot tree = buildFPTree(singletons, data, currentSupportAsInstances);
      



      FrequentItemSets largeItemSets = new FrequentItemSets(data.numInstances());
      

      FrequentBinaryItemSet conditionalItems = new FrequentBinaryItemSet(new ArrayList(), 0);
      
      mineTree(tree, largeItemSets, 0, conditionalItems, currentSupportAsInstances);
      
      m_largeItemSets = largeItemSets;
      






      tree = null;
      
      m_rules = AssociationRule.generateRulesBruteForce(m_largeItemSets, m_metric, m_metricThreshold, upperBoundMinSuppAsInstances, lowerBoundMinSuppAsInstances, data.numInstances());
      



      if ((rulesMustContain != null) && (rulesMustContain.size() > 0)) {
        m_rules = AssociationRule.pruneRules(m_rules, rulesMustContain, m_mustContainOR);
      }
      


      if ((m_findAllRulesForSupportLevel) || 
        (breakOnNext)) {
        break;
      }
      currentSupport -= deltaAsFraction;
      if (currentSupport < lowerBoundMinSuppAsFraction) {
        if (currentSupport + deltaAsFraction <= lowerBoundMinSuppAsFraction)
          break;
        currentSupport = lowerBoundMinSuppAsFraction;
        breakOnNext = true;



      }
      



    }
    while (m_rules.size() < m_numRulesToFind);
    
    Collections.sort(m_rules);
  }
  








  public String toString()
  {
    if (m_rules == null) {
      return "FPGrowth hasn't been trained yet!";
    }
    
    StringBuffer result = new StringBuffer();
    int numRules = m_rules.size() < m_numRulesToFind ? m_rules.size() : m_numRulesToFind;
    


    if (m_rules.size() == 0) {
      return "No rules found!";
    }
    result.append("FPGrowth found " + m_rules.size() + " rules");
    if (!m_findAllRulesForSupportLevel) {
      result.append(" (displaying top " + numRules + ")");
    }
    
    if ((m_transactionsMustContain.length() > 0) || (m_rulesMustContain.length() > 0))
    {
      result.append("\n");
      if (m_transactionsMustContain.length() > 0) {
        result.append("\nUsing only transactions that contain: " + m_transactionsMustContain);
      }
      
      if (m_rulesMustContain.length() > 0) {
        result.append("\nShowing only rules that contain: " + m_rulesMustContain);
      }
    }
    

    result.append("\n\n");
    

    int count = 0;
    for (AssociationRule r : m_rules) {
      result.append(Utils.doubleToString(count + 1.0D, (int)(Math.log(numRules) / Math.log(10.0D) + 1.0D), 0) + ". ");
      
      result.append(r + "\n");
      count++;
      if ((!m_findAllRulesForSupportLevel) && (count == m_numRulesToFind)) {
        break;
      }
    }
    return result.toString();
  }
  








  public String graph(FPTreeRoot tree)
  {
    StringBuffer text = new StringBuffer();
    text.append("digraph FPTree {\n");
    text.append("N0 [label=\"ROOT\"]\n");
    tree.graphFPTree(text);
    

    text.append("}\n");
    
    return text.toString();
  }
  
  public String xmlRules() {
    StringBuffer rulesBuff = new StringBuffer();
    
    rulesBuff.append("<?xml version=\"1.0\" encoding=\"iso-8859-15\"?>\n");
    rulesBuff.append("<RULES>\n");
    int count = 0;
    for (AssociationRule r : m_rules) {
      rulesBuff.append(r.toXML());
      count++;
      if ((!m_findAllRulesForSupportLevel) && (count == m_numRulesToFind)) {
        break;
      }
    }
    rulesBuff.append("</RULES>\n");
    
    return rulesBuff.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7092 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new FPGrowth(), args);
  }
}
