package weka.attributeSelection;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























































public class ExhaustiveSearch
  extends ASSearch
  implements OptionHandler
{
  static final long serialVersionUID = 5741842861142379712L;
  private BitSet m_bestGroup;
  private double m_bestMerit;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private boolean m_verbose;
  private int m_evaluations;
  
  public String globalInfo()
  {
    return "ExhaustiveSearch : \n\nPerforms an exhaustive search through the space of attribute subsets starting from the empty set of attrubutes. Reports the best subset found.";
  }
  




  public ExhaustiveSearch()
  {
    resetOptions();
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tOutput subsets as the search progresses.\n\t(default = false).", "V", 0, "-V"));
    


    return newVector.elements();
  }
  
















  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    setVerbose(Utils.getFlag('V', options));
  }
  




  public String verboseTipText()
  {
    return "Print progress information. Sends progress info to the terminal as the search progresses.";
  }
  




  public void setVerbose(boolean v)
  {
    m_verbose = v;
  }
  



  public boolean getVerbose()
  {
    return m_verbose;
  }
  



  public String[] getOptions()
  {
    String[] options = new String[1];
    int current = 0;
    
    if (m_verbose) {
      options[(current++)] = "-V";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("\tExhaustive Search.\n\tStart set: ");
    
    text.append("no attributes\n");
    
    text.append("\tNumber of evaluations: " + m_evaluations + "\n");
    text.append("\tMerit of best subset found: " + Utils.doubleToString(Math.abs(m_bestMerit), 8, 3) + "\n");
    

    return text.toString();
  }
  









  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    boolean done = false;
    


    BigInteger space = BigInteger.ZERO;
    
    m_evaluations = 0;
    m_numAttribs = data.numAttributes();
    m_bestGroup = new BitSet(m_numAttribs);
    
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    if ((ASEval instanceof UnsupervisedSubsetEvaluator)) {
      m_hasClass = false;
    }
    else {
      m_hasClass = true;
      m_classIndex = data.classIndex();
    }
    
    SubsetEvaluator ASEvaluator = (SubsetEvaluator)ASEval;
    m_numAttribs = data.numAttributes();
    
    double best_merit = ASEvaluator.evaluateSubset(m_bestGroup);
    m_evaluations += 1;
    int sizeOfBest = countFeatures(m_bestGroup);
    
    BitSet tempGroup = new BitSet(m_numAttribs);
    double tempMerit = ASEvaluator.evaluateSubset(tempGroup);
    
    if (m_verbose) {
      System.out.println("Zero feature subset (" + Utils.doubleToString(Math.abs(tempMerit), 8, 5) + ")");
    }
    



    if (tempMerit >= best_merit) {
      int tempSize = countFeatures(tempGroup);
      if ((tempMerit > best_merit) || (tempSize < sizeOfBest))
      {
        best_merit = tempMerit;
        m_bestGroup = ((BitSet)tempGroup.clone());
        sizeOfBest = tempSize;
      }
    }
    
    int numatts = m_hasClass ? m_numAttribs - 1 : m_numAttribs;
    

    BigInteger searchSpaceEnd = BigInteger.ONE.add(BigInteger.ONE).pow(numatts).subtract(BigInteger.ONE);
    

    while (!done)
    {
      space = space.add(BigInteger.ONE);
      if (space.equals(searchSpaceEnd)) {
        done = true;
      }
      tempGroup.clear();
      for (int i = 0; i < numatts; i++) {
        if (space.testBit(i)) {
          if (!m_hasClass) {
            tempGroup.set(i);
          } else {
            int j = i >= m_classIndex ? i + 1 : i;
            

            tempGroup.set(j);
          }
        }
      }
      
      tempMerit = ASEvaluator.evaluateSubset(tempGroup);
      m_evaluations += 1;
      if (tempMerit >= best_merit) {
        int tempSize = countFeatures(tempGroup);
        if ((tempMerit > best_merit) || (tempSize < sizeOfBest))
        {
          best_merit = tempMerit;
          m_bestGroup = ((BitSet)tempGroup.clone());
          sizeOfBest = tempSize;
          if (m_verbose) {
            System.out.println("New best subset (" + Utils.doubleToString(Math.abs(best_merit), 8, 5) + "): " + printSubset(m_bestGroup));
          }
        }
      }
    }
    



    m_bestMerit = best_merit;
    
    return attributeList(m_bestGroup);
  }
  




  private int countFeatures(BitSet featureSet)
  {
    int count = 0;
    for (int i = 0; i < m_numAttribs; i++) {
      if (featureSet.get(i)) {
        count++;
      }
    }
    return count;
  }
  




  private String printSubset(BitSet temp)
  {
    StringBuffer text = new StringBuffer();
    
    for (int j = 0; j < m_numAttribs; j++) {
      if (temp.get(j)) {
        text.append(j + 1 + " ");
      }
    }
    return text.toString();
  }
  




  private int[] attributeList(BitSet group)
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
  







  private void generateNextSubset(int size, BitSet temp)
  {
    int counter = 0;
    boolean done = false;
    BitSet temp2 = (BitSet)temp.clone();
    
    System.err.println("Size: " + size);
    for (int i = 0; i < m_numAttribs; i++) {
      temp2.clear(i);
    }
    label225:
    while ((!done) && (counter < size)) {
      for (i = m_numAttribs - 1 - counter;; i--) { if (i < 0) break label225;
        if (temp.get(i))
        {
          temp.clear(i);
          

          if (i != m_numAttribs - 1 - counter) {
            int newP = i + 1;
            if (newP == m_classIndex) {
              newP++;
            }
            
            if (newP < m_numAttribs) {
              temp.set(newP);
              
              for (int j = 0; j < counter; j++) {
                if (newP + 1 + j == m_classIndex) {
                  newP++;
                }
                
                if (newP + 1 + j < m_numAttribs) {
                  temp.set(newP + 1 + j);
                }
              }
              done = true; break;
            }
            counter++;
            
            break;
          }
          counter++;
          break;
        }
      }
    }
    

    if (temp.cardinality() < size) {
      temp.clear();
    }
    System.err.println(printSubset(temp).toString());
  }
  


  private void resetOptions()
  {
    m_verbose = false;
    m_evaluations = 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.15 $");
  }
}
