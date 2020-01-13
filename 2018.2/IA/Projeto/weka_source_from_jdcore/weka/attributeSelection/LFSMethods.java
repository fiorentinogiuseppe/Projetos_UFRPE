package weka.attributeSelection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Hashtable;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;



























public class LFSMethods
  implements RevisionHandler
{
  private static final int MAX_SUBSET_SIZE = 200;
  private BitSet m_bestGroup;
  private double m_bestMerit;
  private int m_evalsTotal;
  private int m_evalsCached;
  private BitSet[] m_bestGroupOfSize = new BitSet['È'];
  





  public LFSMethods() {}
  





  public BitSet getBestGroup()
  {
    return m_bestGroup;
  }
  


  public double getBestMerit()
  {
    return m_bestMerit;
  }
  


  public BitSet getBestGroupOfSize(int size)
  {
    return m_bestGroupOfSize[size];
  }
  


  public int getNumEvalsCached()
  {
    return m_evalsCached;
  }
  


  public int getNumEvalsTotal()
  {
    return m_evalsTotal;
  }
  


  public int[] rankAttributes(Instances data, SubsetEvaluator evaluator, boolean verbose)
    throws Exception
  {
    if (verbose) {
      System.out.println("Ranking attributes with " + evaluator.getClass().getName());
    }
    

    double[] merit = new double[data.numAttributes()];
    BitSet group = new BitSet(data.numAttributes());
    
    for (int k = 0; k < data.numAttributes(); k++) {
      if (k != data.classIndex()) {
        group.set(k);
        merit[k] -= evaluator.evaluateSubset(group);
        m_evalsTotal += 1;
        group.clear(k);
      } else {
        merit[k] = Double.MAX_VALUE;
      }
      
      if (verbose) {
        System.out.println(k + ": " + merit[k]);
      }
    }
    
    int[] ranking = Utils.sort(merit);
    
    if (verbose) {
      System.out.print("Ranking [ ");
      
      for (int i = 0; i < ranking.length; i++) {
        System.out.print(ranking[i] + " ");
      }
      
      System.out.println("]\n");
    }
    
    return ranking;
  }
  

















  public BitSet forwardSearch(int cacheSize, BitSet startGroup, int[] ranking, int k, boolean incrementK, int maxStale, int forceResultSize, Instances data, SubsetEvaluator evaluator, boolean verbose)
    throws Exception
  {
    if ((forceResultSize > 0) && (maxStale > 1)) {
      throw new Exception("Forcing result size only works for maxStale=1");
    }
    
    if (verbose) {
      System.out.println("Starting forward selection");
    }
    


    int bestSize = 0;
    int tempSize = 0;
    
    double tempMerit = 0.0D;
    
    LinkedList2 list = new LinkedList2(maxStale);
    Hashtable alreadyExpanded = new Hashtable(cacheSize * data.numAttributes());
    int insertCount = 0;
    int stale = 0;
    
    int thisK = k;
    int evalsTotal = 0;
    int evalsCached = 0;
    
    BitSet bestGroup = (BitSet)startGroup.clone();
    
    String hashKey = bestGroup.toString();
    double bestMerit = evaluator.evaluateSubset(bestGroup);
    
    if (verbose) {
      System.out.print("Group: ");
      printGroup(bestGroup, data.numAttributes());
      System.out.println("Merit: " + tempMerit);
      System.out.println("----------");
    }
    
    alreadyExpanded.put(hashKey, new Double(bestMerit));
    insertCount++;
    bestSize = bestGroup.cardinality();
    

    if (maxStale > 1) {
      Object[] best = new Object[1];
      best[0] = bestGroup.clone();
      list.addToList(best, bestMerit);
    }
    
    while (stale < maxStale) {
      boolean improvement = false;
      
      BitSet tempGroup;
      if (maxStale > 1) {
        if (list.size() == 0) {
          stale = maxStale;
          
          break;
        }
        
        Link2 link = list.getLinkAt(0);
        BitSet tempGroup = (BitSet)link.getData()[0];
        tempGroup = (BitSet)tempGroup.clone();
        list.removeLinkAt(0);
        
        tempSize = 0;
        
        for (int i = 0; i < data.numAttributes(); i++) {
          if (tempGroup.get(i)) {
            tempSize++;
          }
        }
      }
      else {
        tempGroup = (BitSet)bestGroup.clone();
        tempSize = bestSize;
      }
      

      if (incrementK) {
        thisK = Math.min(Math.max(thisK, k + tempSize), data.numAttributes());
      } else {
        thisK = k;
      }
      

      for (int i = 0; i < thisK; i++) {
        if ((ranking[i] != data.classIndex()) && (!tempGroup.get(ranking[i])))
        {


          tempGroup.set(ranking[i]);
          tempSize++;
          hashKey = tempGroup.toString();
          
          if (!alreadyExpanded.containsKey(hashKey)) {
            evalsTotal++;
            tempMerit = evaluator.evaluateSubset(tempGroup);
            
            if (insertCount > cacheSize * data.numAttributes()) {
              alreadyExpanded = new Hashtable(cacheSize * data.numAttributes());
              insertCount = 0;
            }
            
            alreadyExpanded.put(hashKey, new Double(tempMerit));
            insertCount++;
          } else {
            evalsCached++;
            tempMerit = ((Double)alreadyExpanded.get(hashKey)).doubleValue();
          }
          
          if (verbose) {
            System.out.print("Group: ");
            printGroup(tempGroup, data.numAttributes());
            System.out.println("Merit: " + tempMerit);
          }
          
          if ((tempMerit - bestMerit > 1.0E-5D) || ((forceResultSize >= tempSize) && (tempSize > bestSize)))
          {
            improvement = true;
            stale = 0;
            bestMerit = tempMerit;
            bestSize = tempSize;
            bestGroup = (BitSet)tempGroup.clone();
            m_bestGroupOfSize[bestSize] = ((BitSet)(BitSet)tempGroup.clone());
          }
          
          if (maxStale > 1) {
            Object[] add = new Object[1];
            add[0] = tempGroup.clone();
            list.addToList(add, tempMerit);
          }
          
          tempGroup.clear(ranking[i]);
          tempSize--;
        }
      }
      if (verbose) {
        System.out.println("----------");
      }
      

      if ((!improvement) || (forceResultSize == bestSize)) {
        stale++;
      }
      
      if ((forceResultSize > 0) && (bestSize == forceResultSize)) {
        break;
      }
    }
    
    if (verbose) {
      System.out.println("Best Group: ");
      printGroup(bestGroup, data.numAttributes());
      System.out.println();
    }
    
    m_bestGroup = bestGroup;
    m_bestMerit = bestMerit;
    m_evalsTotal += evalsTotal;
    m_evalsCached += evalsCached;
    
    return bestGroup;
  }
  

















  public BitSet floatingForwardSearch(int cacheSize, BitSet startGroup, int[] ranking, int k, boolean incrementK, int maxStale, Instances data, SubsetEvaluator evaluator, boolean verbose)
    throws Exception
  {
    if (verbose) {
      System.out.println("Starting floating forward selection");
    }
    


    int bestSize = 0;
    int tempSize = 0;
    
    double tempMerit = 0.0D;
    
    LinkedList2 list = new LinkedList2(maxStale);
    Hashtable alreadyExpanded = new Hashtable(cacheSize * data.numAttributes());
    int insertCount = 0;
    int backtrackingSteps = 0;
    

    int thisK = k;
    int evalsTotal = 0;
    int evalsCached = 0;
    
    BitSet bestGroup = (BitSet)startGroup.clone();
    
    String hashKey = bestGroup.toString();
    double bestMerit = evaluator.evaluateSubset(bestGroup);
    
    if (verbose) {
      System.out.print("Group: ");
      printGroup(bestGroup, data.numAttributes());
      System.out.println("Merit: " + tempMerit);
      System.out.println("----------");
    }
    
    alreadyExpanded.put(hashKey, new Double(bestMerit));
    insertCount++;
    bestSize = bestGroup.cardinality();
    
    if (maxStale > 1) {
      Object[] best = new Object[1];
      best[0] = bestGroup.clone();
      list.addToList(best, bestMerit);
    }
    boolean improvement;
    boolean backward = improvement = 1;
    

    for (;;)
    {
      if (backward) {
        if (!improvement) {
          backward = false;
        }
        
      }
      else
      {
        if ((!improvement) && (backtrackingSteps >= maxStale)) {
          break;
        }
        
        backward = true;
      }
      
      improvement = false;
      
      BitSet tempGroup;
      if (maxStale > 1) {
        if (list.size() == 0) {
          backtrackingSteps = maxStale;
          
          break;
        }
        
        Link2 link = list.getLinkAt(0);
        BitSet tempGroup = (BitSet)link.getData()[0];
        tempGroup = (BitSet)tempGroup.clone();
        list.removeLinkAt(0);
        
        tempSize = 0;
        
        for (int i = 0; i < data.numAttributes(); i++) {
          if (tempGroup.get(i)) {
            tempSize++;
          }
        }
      }
      else {
        tempGroup = (BitSet)bestGroup.clone();
        tempSize = bestSize;
      }
      

      if ((backward) && (tempSize <= 2)) {
        backward = false;
      }
      

      if (incrementK) {
        thisK = Math.max(thisK, Math.min(Math.max(thisK, k + tempSize), data.numAttributes()));
      }
      else {
        thisK = k;
      }
      

      for (int i = 0; i < thisK; i++) {
        if (ranking[i] != data.classIndex())
        {


          if (backward) {
            if (!tempGroup.get(ranking[i])) {
              continue;
            }
            
            tempGroup.clear(ranking[i]);
            tempSize--;
          } else {
            if ((ranking[i] == data.classIndex()) || (tempGroup.get(ranking[i]))) {
              continue;
            }
            
            tempGroup.set(ranking[i]);
            tempSize++;
          }
          
          hashKey = tempGroup.toString();
          
          if (!alreadyExpanded.containsKey(hashKey)) {
            evalsTotal++;
            tempMerit = evaluator.evaluateSubset(tempGroup);
            
            if (insertCount > cacheSize * data.numAttributes()) {
              alreadyExpanded = new Hashtable(cacheSize * data.numAttributes());
              insertCount = 0;
            }
            
            alreadyExpanded.put(hashKey, new Double(tempMerit));
            insertCount++;
          } else {
            evalsCached++;
            tempMerit = ((Double)alreadyExpanded.get(hashKey)).doubleValue();
          }
          
          if (verbose) {
            System.out.print("Group: ");
            printGroup(tempGroup, data.numAttributes());
            System.out.println("Merit: " + tempMerit);
          }
          
          if (tempMerit - bestMerit > 1.0E-5D) {
            improvement = true;
            backtrackingSteps = 0;
            bestMerit = tempMerit;
            bestSize = tempSize;
            bestGroup = (BitSet)tempGroup.clone();
          }
          
          if (maxStale > 1) {
            Object[] add = new Object[1];
            add[0] = tempGroup.clone();
            list.addToList(add, tempMerit);
          }
          
          if (backward) {
            tempGroup.set(ranking[i]);
            tempSize++;
          } else {
            tempGroup.clear(ranking[i]);
            tempSize--;
          }
        }
      }
      if (verbose) {
        System.out.println("----------");
      }
      
      if ((maxStale > 1) && (backward) && (!improvement)) {
        Object[] add = new Object[1];
        add[0] = tempGroup.clone();
        list.addToList(add, Double.MAX_VALUE);
      }
      
      if ((!backward) && (!improvement)) {
        backtrackingSteps++;
      }
    }
    
    if (verbose) {
      System.out.println("Best Group: ");
      printGroup(bestGroup, data.numAttributes());
      System.out.println();
    }
    
    m_bestGroup = bestGroup;
    m_bestMerit = bestMerit;
    m_evalsTotal += evalsTotal;
    m_evalsCached += evalsCached;
    
    return bestGroup;
  }
  


  protected static void printGroup(BitSet tt, int numAttribs)
  {
    System.out.print("{ ");
    
    for (int i = 0; i < numAttribs; i++) {
      if (tt.get(i) == true) {
        System.out.print(i + 1 + " ");
      }
    }
    
    System.out.println("}");
  }
  



  public class Link2
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -7422719407475185086L;
    


    Object[] m_data;
    


    double m_merit;
    


    public Link2(Object[] data, double mer)
    {
      m_data = data;
      m_merit = mer;
    }
    
    public Object[] getData()
    {
      return m_data;
    }
    
    public String toString() {
      return "Node: " + m_data.toString() + "  " + m_merit;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
  




  public class LinkedList2
    extends FastVector
  {
    private static final long serialVersionUID = -7776010892419656105L;
    



    int m_MaxSize;
    




    public LinkedList2(int sz)
    {
      m_MaxSize = sz;
    }
    




    public void removeLinkAt(int index)
      throws Exception
    {
      if ((index >= 0) && (index < size())) {
        removeElementAt(index);
      } else {
        throw new Exception("index out of range (removeLinkAt)");
      }
    }
    




    public LFSMethods.Link2 getLinkAt(int index)
      throws Exception
    {
      if (size() == 0) {
        throw new Exception("List is empty (getLinkAt)");
      }
      if ((index >= 0) && (index < size())) {
        return (LFSMethods.Link2)elementAt(index);
      }
      throw new Exception("index out of range (getLinkAt)");
    }
    








    public void addToList(Object[] data, double mer)
      throws Exception
    {
      LFSMethods.Link2 newL = new LFSMethods.Link2(LFSMethods.this, data, mer);
      
      if (size() == 0) {
        addElement(newL);
      }
      else if (mer > firstElementm_merit) {
        if (size() == m_MaxSize) {
          removeLinkAt(m_MaxSize - 1);
        }
        

        insertElementAt(newL, 0);
      } else {
        int i = 0;
        int size = size();
        boolean done = false;
        



        if ((size != m_MaxSize) || (mer > lastElementm_merit))
        {



          while ((!done) && (i < size)) {
            if (mer > elementAtm_merit) {
              if (size == m_MaxSize) {
                removeLinkAt(m_MaxSize - 1);
              }
              

              insertElementAt(newL, i);
              done = true;
            }
            else if (i == size - 1) {
              addElement(newL);
              done = true;
            } else {
              i++;
            }
          }
        }
      }
    }
    






    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.3 $");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
