package weka.attributeSelection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
































































public class BestFirst
  extends ASSearch
  implements OptionHandler, StartSetHandler
{
  static final long serialVersionUID = 7841338689536821867L;
  protected int m_maxStale;
  protected int m_searchDirection;
  protected static final int SELECTION_BACKWARD = 0;
  protected static final int SELECTION_FORWARD = 1;
  protected static final int SELECTION_BIDIRECTIONAL = 2;
  
  public class Link2
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -8236598311516351420L;
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
    
    public String toString()
    {
      return "Node: " + m_data.toString() + "  " + m_merit;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.29 $");
    }
  }
  




  public class LinkedList2
    extends FastVector
  {
    static final long serialVersionUID = 3250538292330398929L;
    



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
      }
      else {
        throw new Exception("index out of range (removeLinkAt)");
      }
    }
    





    public BestFirst.Link2 getLinkAt(int index)
      throws Exception
    {
      if (size() == 0) {
        throw new Exception("List is empty (getLinkAt)");
      }
      if ((index >= 0) && (index < size())) {
        return (BestFirst.Link2)elementAt(index);
      }
      
      throw new Exception("index out of range (getLinkAt)");
    }
    







    public void addToList(Object[] data, double mer)
      throws Exception
    {
      BestFirst.Link2 newL = new BestFirst.Link2(BestFirst.this, data, mer);
      
      if (size() == 0) {
        addElement(newL);
      }
      else if (mer > firstElementm_merit) {
        if (size() == m_MaxSize) {
          removeLinkAt(m_MaxSize - 1);
        }
        

        insertElementAt(newL, 0);
      }
      else {
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
            }
            else {
              i++;
            }
          }
        }
      }
    }
    






    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.29 $");
    }
  }
  














  public static final Tag[] TAGS_SELECTION = { new Tag(0, "Backward"), new Tag(1, "Forward"), new Tag(2, "Bi-directional") };
  


  protected int[] m_starting;
  


  protected Range m_startRange;
  


  protected boolean m_hasClass;
  


  protected int m_classIndex;
  


  protected int m_numAttribs;
  


  protected int m_totalEvals;
  

  protected boolean m_debug;
  

  protected double m_bestMerit;
  

  protected int m_cacheSize;
  


  public String globalInfo()
  {
    return "BestFirst:\n\nSearches the space of attribute subsets by greedy hillclimbing augmented with a backtracking facility. Setting the number of consecutive non-improving nodes allowed controls the level of backtracking done. Best first may start with the empty set of attributes and search forward, or start with the full set of attributes and search backward, or start at any point and search in both directions (by considering all possible single attribute additions and deletions at a given point).\n";
  }
  










  public BestFirst()
  {
    resetOptions();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify a starting set of attributes.\n\tEg. 1,3,5-7.", "P", 1, "-P <start set>"));
    


    newVector.addElement(new Option("\tDirection of search. (default = 1).", "D", 1, "-D <0 = backward | 1 = forward | 2 = bi-directional>"));
    


    newVector.addElement(new Option("\tNumber of non-improving nodes to\n\tconsider before terminating search.", "N", 1, "-N <num>"));
    

    newVector.addElement(new Option("\tSize of lookup cache for evaluated subsets.\n\tExpressed as a multiple of the number of\n\tattributes in the data set. (default = 1)", "S", 1, "-S <num>"));
    



    return newVector.elements();
  }
  





























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('P', options);
    if (optionString.length() != 0) {
      setStartSet(optionString);
    }
    
    optionString = Utils.getOption('D', options);
    
    if (optionString.length() != 0) {
      setDirection(new SelectedTag(Integer.parseInt(optionString), TAGS_SELECTION));
    }
    else {
      setDirection(new SelectedTag(1, TAGS_SELECTION));
    }
    
    optionString = Utils.getOption('N', options);
    
    if (optionString.length() != 0) {
      setSearchTermination(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      setLookupCacheSize(Integer.parseInt(optionString));
    }
    
    m_debug = Utils.getFlag('Z', options);
  }
  






  public void setLookupCacheSize(int size)
  {
    if (size >= 0) {
      m_cacheSize = size;
    }
  }
  





  public int getLookupCacheSize()
  {
    return m_cacheSize;
  }
  




  public String lookupCacheSizeTipText()
  {
    return "Set the maximum size of the lookup cache of evaluated subsets. This is expressed as a multiplier of the number of attributes in the data set. (default = 1).";
  }
  






  public String startSetTipText()
  {
    return "Set the start point for the search. This is specified as a comma seperated list off attribute indexes starting at 1. It can include ranges. Eg. 1,2,5-9,17.";
  }
  








  public void setStartSet(String startSet)
    throws Exception
  {
    m_startRange.setRanges(startSet);
  }
  



  public String getStartSet()
  {
    return m_startRange.getRanges();
  }
  




  public String searchTerminationTipText()
  {
    return "Specify the number of consecutive non-improving nodes to allow before terminating the search.";
  }
  







  public void setSearchTermination(int t)
    throws Exception
  {
    if (t < 1) {
      throw new Exception("Value of -N must be > 0.");
    }
    
    m_maxStale = t;
  }
  





  public int getSearchTermination()
  {
    return m_maxStale;
  }
  




  public String directionTipText()
  {
    return "Set the direction of the search.";
  }
  





  public void setDirection(SelectedTag d)
  {
    if (d.getTags() == TAGS_SELECTION) {
      m_searchDirection = d.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getDirection()
  {
    return new SelectedTag(m_searchDirection, TAGS_SELECTION);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    if (!getStartSet().equals("")) {
      options[(current++)] = "-P";
      options[(current++)] = ("" + startSetToString());
    }
    options[(current++)] = "-D";
    options[(current++)] = ("" + m_searchDirection);
    options[(current++)] = "-N";
    options[(current++)] = ("" + m_maxStale);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  








  private String startSetToString()
  {
    StringBuffer FString = new StringBuffer();
    

    if (m_starting == null) {
      return getStartSet();
    }
    for (int i = 0; i < m_starting.length; i++) {
      boolean didPrint = false;
      
      if ((!m_hasClass) || ((m_hasClass == true) && (i != m_classIndex)))
      {
        FString.append(m_starting[i] + 1);
        didPrint = true;
      }
      
      if (i == m_starting.length - 1) {
        FString.append("");

      }
      else if (didPrint) {
        FString.append(",");
      }
    }
    

    return FString.toString();
  }
  



  public String toString()
  {
    StringBuffer BfString = new StringBuffer();
    BfString.append("\tBest first.\n\tStart set: ");
    
    if (m_starting == null) {
      BfString.append("no attributes\n");
    }
    else {
      BfString.append(startSetToString() + "\n");
    }
    
    BfString.append("\tSearch direction: ");
    
    if (m_searchDirection == 0) {
      BfString.append("backward\n");
    }
    else if (m_searchDirection == 1) {
      BfString.append("forward\n");
    }
    else {
      BfString.append("bi-directional\n");
    }
    

    BfString.append("\tStale search after " + m_maxStale + " node expansions\n");
    
    BfString.append("\tTotal number of subsets evaluated: " + m_totalEvals + "\n");
    
    BfString.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");
    
    return BfString.toString();
  }
  


  protected void printGroup(BitSet tt, int numAttribs)
  {
    for (int i = 0; i < numAttribs; i++) {
      if (tt.get(i) == true) {
        System.out.print(i + 1 + " ");
      }
    }
    
    System.out.println();
  }
  








  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    m_totalEvals = 0;
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    if ((ASEval instanceof UnsupervisedSubsetEvaluator)) {
      m_hasClass = false;
    } else {
      m_hasClass = true;
      m_classIndex = data.classIndex();
    }
    
    SubsetEvaluator ASEvaluator = (SubsetEvaluator)ASEval;
    m_numAttribs = data.numAttributes();
    
    int best_size = 0;
    int size = 0;
    
    int sd = m_searchDirection;
    






    Hashtable lookup = new Hashtable(m_cacheSize * m_numAttribs);
    int insertCount = 0;
    int cacheHits = 0;
    LinkedList2 bfList = new LinkedList2(m_maxStale);
    double best_merit = -1.7976931348623157E308D;
    int stale = 0;
    BitSet best_group = new BitSet(m_numAttribs);
    
    m_startRange.setUpper(m_numAttribs - 1);
    if (!getStartSet().equals("")) {
      m_starting = m_startRange.getSelection();
    }
    
    if (m_starting != null) {
      for (int i = 0; i < m_starting.length; i++) {
        if (m_starting[i] != m_classIndex) {
          best_group.set(m_starting[i]);
        }
      }
      
      best_size = m_starting.length;
      m_totalEvals += 1;
    }
    else if (m_searchDirection == 0) {
      setStartSet("1-last");
      m_starting = new int[m_numAttribs];
      

      int i = 0; for (int j = 0; i < m_numAttribs; i++) {
        if (i != m_classIndex) {
          best_group.set(i);
          m_starting[(j++)] = i;
        }
      }
      
      best_size = m_numAttribs - 1;
      m_totalEvals += 1;
    }
    


    best_merit = ASEvaluator.evaluateSubset(best_group);
    
    Object[] best = new Object[1];
    best[0] = best_group.clone();
    bfList.addToList(best, best_merit);
    BitSet tt = (BitSet)best_group.clone();
    String hashC = tt.toString();
    lookup.put(hashC, new Double(best_merit));
    
    while (stale < m_maxStale) {
      boolean added = false;
      int done;
      if (m_searchDirection == 2)
      {
        int done = 2;
        sd = 1;
      } else {
        done = 1;
      }
      

      if (bfList.size() == 0) {
        stale = m_maxStale;
        break;
      }
      

      Link2 tl = bfList.getLinkAt(0);
      BitSet temp_group = (BitSet)tl.getData()[0];
      temp_group = (BitSet)temp_group.clone();
      
      bfList.removeLinkAt(0);
      


      int kk = 0; for (size = 0; kk < m_numAttribs; kk++) {
        if (temp_group.get(kk)) {
          size++;
        }
      }
      do
      {
        for (int i = 0; i < m_numAttribs; i++) { boolean z;
          boolean z; if (sd == 1) {
            z = (i != m_classIndex) && (!temp_group.get(i));
          } else {
            z = (i != m_classIndex) && (temp_group.get(i));
          }
          
          if (z)
          {
            if (sd == 1) {
              temp_group.set(i);
              size++;
            } else {
              temp_group.clear(i);
              size--;
            }
            


            tt = (BitSet)temp_group.clone();
            hashC = tt.toString();
            double merit;
            if (!lookup.containsKey(hashC)) {
              double merit = ASEvaluator.evaluateSubset(temp_group);
              m_totalEvals += 1;
              

              if (insertCount > m_cacheSize * m_numAttribs) {
                lookup = new Hashtable(m_cacheSize * m_numAttribs);
                insertCount = 0;
              }
              hashC = tt.toString();
              lookup.put(hashC, new Double(merit));
              insertCount++;
            } else {
              merit = ((Double)lookup.get(hashC)).doubleValue();
              cacheHits++;
            }
            

            Object[] add = new Object[1];
            add[0] = tt.clone();
            bfList.addToList(add, merit);
            
            if (m_debug) {
              System.out.print("Group: ");
              printGroup(tt, m_numAttribs);
              System.out.println("Merit: " + merit);
            }
            

            if (sd == 1) {
              z = merit - best_merit > 1.0E-5D;
            }
            else if (merit == best_merit) {
              z = size < best_size;
            } else {
              z = merit > best_merit;
            }
            

            if (z) {
              added = true;
              stale = 0;
              best_merit = merit;
              
              best_size = size;
              best_group = (BitSet)temp_group.clone();
            }
            

            if (sd == 1) {
              temp_group.clear(i);
              size--;
            } else {
              temp_group.set(i);
              size++;
            }
          }
        }
        
        if (done == 2) {
          sd = 0;
        }
        
        done--;
      } while (done > 0);
      


      if (!added) {
        stale++;
      }
    }
    
    m_bestMerit = best_merit;
    return attributeList(best_group);
  }
  



  protected void resetOptions()
  {
    m_maxStale = 5;
    m_searchDirection = 1;
    m_starting = null;
    m_startRange = new Range();
    m_classIndex = -1;
    m_totalEvals = 0;
    m_cacheSize = 1;
    m_debug = false;
  }
  





  protected int[] attributeList(BitSet group)
  {
    int count = 0;
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        count++;
      }
    }
    
    int[] list = new int[count];
    count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        list[(count++)] = i;
      }
    }
    
    return list;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.29 $");
  }
}
