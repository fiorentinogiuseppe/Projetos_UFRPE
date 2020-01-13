package weka.associations.gsp;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;




































public class Sequence
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -5001018056339156390L;
  protected int m_SupportCount;
  protected FastVector m_Elements;
  
  public Sequence()
  {
    m_SupportCount = 0;
    m_Elements = new FastVector();
  }
  




  public Sequence(FastVector elements)
  {
    m_SupportCount = 0;
    m_Elements = elements;
  }
  




  public Sequence(int supportCount)
  {
    m_SupportCount = supportCount;
    m_Elements = new FastVector();
  }
  






  public static FastVector aprioriGen(FastVector kMinusOneSequences)
    throws CloneNotSupportedException
  {
    FastVector allCandidates = generateKCandidates(kMinusOneSequences);
    FastVector prunedCandidates = pruneCadidates(allCandidates, kMinusOneSequences);
    
    return prunedCandidates;
  }
  







  public static FastVector deleteInfrequentSequences(FastVector sequences, long minSupportCount)
  {
    FastVector deletedSequences = new FastVector();
    Enumeration seqEnum = sequences.elements();
    
    while (seqEnum.hasMoreElements()) {
      Sequence currentSeq = (Sequence)seqEnum.nextElement();
      long curSupportCount = currentSeq.getSupportCount();
      
      if (curSupportCount >= minSupportCount) {
        deletedSequences.addElement(currentSeq);
      }
    }
    return deletedSequences;
  }
  





  protected static FastVector generateKCandidates(FastVector kMinusOneSequences)
    throws CloneNotSupportedException
  {
    FastVector candidates = new FastVector();
    FastVector mergeResult = new FastVector();
    
    for (int i = 0; i < kMinusOneSequences.size(); i++) {
      for (int j = 0; j < kMinusOneSequences.size(); j++) {
        Sequence originalSeq1 = (Sequence)kMinusOneSequences.elementAt(i);
        Sequence seq1 = originalSeq1.clone();
        Sequence originalSeq2 = (Sequence)kMinusOneSequences.elementAt(j);
        Sequence seq2 = originalSeq2.clone();
        Sequence subseq1 = seq1.deleteEvent("first");
        Sequence subseq2 = seq2.deleteEvent("last");
        
        if (subseq1.equals(subseq2))
        {
          if ((subseq1.getElements().size() == 0) && (subseq2.getElements().size() == 0)) {
            if (i >= j) {
              mergeResult = merge(seq1, seq2, true, true);
            } else {
              mergeResult = merge(seq1, seq2, true, false);
            }
          }
          else {
            mergeResult = merge(seq1, seq2, false, false);
          }
          candidates.appendElements(mergeResult);
        }
      }
    }
    return candidates;
  }
  










  protected static FastVector merge(Sequence seq1, Sequence seq2, boolean oneElements, boolean mergeElements)
  {
    FastVector mergeResult = new FastVector();
    

    if (oneElements) {
      Element element1 = (Element)seq1.getElements().firstElement();
      Element element2 = (Element)seq2.getElements().firstElement();
      Element element3 = null;
      if (mergeElements) {
        for (int i = 0; i < element1.getEvents().length; i++) {
          if (element1.getEvents()[i] > -1) {
            if (element2.getEvents()[i] > -1) {
              break;
            }
            element3 = Element.merge(element1, element2);
          }
        }
      }
      
      FastVector newElements1 = new FastVector();
      
      newElements1.addElement(element1);
      newElements1.addElement(element2);
      mergeResult.addElement(new Sequence(newElements1));
      
      if (element3 != null) {
        FastVector newElements2 = new FastVector();
        newElements2.addElement(element3);
        mergeResult.addElement(new Sequence(newElements2));
      }
      
      return mergeResult;
    }
    
    Element lastElementSeq1 = (Element)seq1.getElements().lastElement();
    Element lastElementSeq2 = (Element)seq2.getElements().lastElement();
    Sequence resultSeq = new Sequence();
    FastVector resultSeqElements = resultSeq.getElements();
    

    if (lastElementSeq2.containsOverOneEvent()) {
      for (int i = 0; i < seq1.getElements().size() - 1; i++) {
        resultSeqElements.addElement(seq1.getElements().elementAt(i));
      }
      resultSeqElements.addElement(Element.merge(lastElementSeq1, lastElementSeq2));
      mergeResult.addElement(resultSeq);
      
      return mergeResult;
    }
    
    for (int i = 0; i < seq1.getElements().size(); i++) {
      resultSeqElements.addElement(seq1.getElements().elementAt(i));
    }
    resultSeqElements.addElement(lastElementSeq2);
    mergeResult.addElement(resultSeq);
    
    return mergeResult;
  }
  







  public static FastVector oneElementsToSequences(FastVector elements)
  {
    FastVector sequences = new FastVector();
    Enumeration elementEnum = elements.elements();
    
    while (elementEnum.hasMoreElements()) {
      Sequence seq = new Sequence();
      FastVector seqElements = seq.getElements();
      seqElements.addElement(elementEnum.nextElement());
      sequences.addElement(seq);
    }
    return sequences;
  }
  




  public static void printSetOfSequences(FastVector setOfSequences)
  {
    Enumeration seqEnum = setOfSequences.elements();
    int i = 1;
    
    while (seqEnum.hasMoreElements()) {
      Sequence seq = (Sequence)seqEnum.nextElement();
      System.out.print("[" + i++ + "]" + " " + seq.toString());
    }
  }
  







  protected static FastVector pruneCadidates(FastVector allCandidates, FastVector kMinusOneSequences)
  {
    FastVector prunedCandidates = new FastVector();
    

    for (int i = 0; i < allCandidates.size(); i++) {
      Sequence candidate = (Sequence)allCandidates.elementAt(i);
      boolean isFrequent = true;
      FastVector canElements = candidate.getElements();
      
      for (int j = 0; j < canElements.size(); j++) {
        if (!isFrequent) break;
        Element origElement = (Element)canElements.elementAt(j);
        int[] origEvents = origElement.getEvents();
        
        for (int k = 0; k < origEvents.length; k++) {
          if (origEvents[k] > -1) {
            int helpEvent = origEvents[k];
            origEvents[k] = -1;
            
            if (origElement.isEmpty()) {
              canElements.removeElementAt(j);
              
              int containedAt = kMinusOneSequences.indexOf(candidate);
              if (containedAt != -1) {
                origEvents[k] = helpEvent;
                canElements.insertElementAt(origElement, j);
                break;
              }
              isFrequent = false;
              break;
            }
            

            int containedAt = kMinusOneSequences.indexOf(candidate);
            if (containedAt != -1) {
              origEvents[k] = helpEvent;
            }
            else {
              isFrequent = false;
              break;
            }
          }
        }
      }
      



      if (isFrequent) {
        prunedCandidates.addElement(candidate);
      }
    }
    return prunedCandidates;
  }
  









  public static String setOfSequencesToString(FastVector setOfSequences, Instances dataSet, FastVector filterAttributes)
  {
    StringBuffer resString = new StringBuffer();
    Enumeration SequencesEnum = setOfSequences.elements();
    int i = 1;
    

    while (SequencesEnum.hasMoreElements()) {
      Sequence seq = (Sequence)SequencesEnum.nextElement();
      Integer filterAttr = (Integer)filterAttributes.elementAt(0);
      boolean printSeq = true;
      
      if (filterAttr.intValue() != -1) {
        for (int j = 0; j < filterAttributes.size(); j++) {
          filterAttr = (Integer)filterAttributes.elementAt(j);
          FastVector seqElements = seq.getElements();
          
          if (printSeq) {
            for (int k = 0; k < seqElements.size(); k++) {
              Element currentElement = (Element)seqElements.elementAt(k);
              int[] currentEvents = currentElement.getEvents();
              
              if (currentEvents[filterAttr.intValue()] == -1)
              {

                printSeq = false;
                break;
              }
            }
          }
        }
      }
      if (printSeq) {
        resString.append("[" + i++ + "]" + " " + seq.toNominalString(dataSet));
      }
    }
    return resString.toString();
  }
  






  public static void updateSupportCount(FastVector candidates, FastVector dataSequences)
  {
    Enumeration canEnumeration = candidates.elements();
    
    while (canEnumeration.hasMoreElements()) {
      Enumeration dataSeqEnumeration = dataSequences.elements();
      Sequence candidate = (Sequence)canEnumeration.nextElement();
      
      while (dataSeqEnumeration.hasMoreElements()) {
        Instances dataSequence = (Instances)dataSeqEnumeration.nextElement();
        
        if (candidate.isSubsequenceOf(dataSequence)) {
          candidate.setSupportCount(candidate.getSupportCount() + 1);
        }
      }
    }
  }
  



  public Sequence clone()
  {
    try
    {
      Sequence clone = (Sequence)super.clone();
      
      clone.setSupportCount(m_SupportCount);
      FastVector cloneElements = new FastVector(m_Elements.size());
      
      for (int i = 0; i < m_Elements.size(); i++) {
        Element helpElement = (Element)m_Elements.elementAt(i);
        cloneElements.addElement(helpElement.clone());
      }
      clone.setElements(cloneElements);
      
      return clone;
    } catch (CloneNotSupportedException exc) {
      exc.printStackTrace();
    }
    return null;
  }
  







  protected Sequence deleteEvent(String position)
  {
    Sequence cloneSeq = clone();
    
    if (position.equals("first")) {
      Element element = (Element)cloneSeq.getElements().firstElement();
      element.deleteEvent("first");
      if (element.isEmpty()) {
        cloneSeq.getElements().removeElementAt(0);
      }
      return cloneSeq;
    }
    if (position.equals("last")) {
      Element element = (Element)cloneSeq.getElements().lastElement();
      element.deleteEvent("last");
      if (element.isEmpty()) {
        cloneSeq.getElements().removeElementAt(m_Elements.size() - 1);
      }
      return cloneSeq;
    }
    return null;
  }
  




  public boolean equals(Object obj)
  {
    Sequence seq2 = (Sequence)obj;
    FastVector seq2Elements = seq2.getElements();
    
    for (int i = 0; i < m_Elements.size(); i++) {
      Element thisElement = (Element)m_Elements.elementAt(i);
      Element seq2Element = (Element)seq2Elements.elementAt(i);
      if (!thisElement.equals(seq2Element)) {
        return false;
      }
    }
    return true;
  }
  




  protected FastVector getElements()
  {
    return m_Elements;
  }
  




  protected int getSupportCount()
  {
    return m_SupportCount;
  }
  






  protected boolean isSubsequenceOf(Instances dataSequence)
  {
    FastVector elements = getElements();
    Enumeration elementEnum = elements.elements();
    Element curElement = (Element)elementEnum.nextElement();
    
    for (int i = 0; i < dataSequence.numInstances(); i++) {
      if (curElement.isContainedBy(dataSequence.instance(i))) {
        if (!elementEnum.hasMoreElements()) {
          return true;
        }
        curElement = (Element)elementEnum.nextElement();
      }
    }
    

    return false;
  }
  




  protected void setElements(FastVector elements)
  {
    m_Elements = elements;
  }
  




  protected void setSupportCount(int supportCount)
  {
    m_SupportCount = supportCount;
  }
  







  public String toNominalString(Instances dataSet)
  {
    String result = "";
    
    result = result + "<";
    
    for (int i = 0; i < m_Elements.size(); i++) {
      Element element = (Element)m_Elements.elementAt(i);
      result = result + element.toNominalString(dataSet);
    }
    result = result + "> (" + getSupportCount() + ")\n";
    
    return result;
  }
  




  public String toString()
  {
    String result = "";
    
    result = result + "Sequence Output\n";
    result = result + "------------------------------\n";
    result = result + "Support Count: " + getSupportCount() + "\n";
    result = result + "contained elements/itemsets:\n";
    
    for (int i = 0; i < m_Elements.size(); i++) {
      Element element = (Element)m_Elements.elementAt(i);
      result = result + element.toString();
    }
    result = result + "\n\n";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
