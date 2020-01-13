package weka.associations;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import weka.associations.gsp.Element;
import weka.associations.gsp.Sequence;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
































































































public class GeneralizedSequentialPatterns
  extends AbstractAssociator
  implements OptionHandler, TechnicalInformationHandler
{
  private static final long serialVersionUID = -4119691320812254676L;
  protected double m_MinSupport;
  protected int m_DataSeqID;
  protected Instances m_OriginalDataSet;
  protected FastVector m_AllSequentialPatterns;
  protected int m_Cycles;
  protected String m_CycleStart;
  protected String m_CycleEnd;
  protected String m_AlgorithmStart;
  protected String m_FilterAttributes;
  protected FastVector m_FilterAttrVector;
  protected boolean m_Debug = false;
  


  public GeneralizedSequentialPatterns()
  {
    resetOptions();
  }
  




  public String globalInfo()
  {
    return "Class implementing a GSP algorithm for discovering sequential patterns in a sequential data set.\nThe attribute identifying the distinct data sequences contained in the set can be determined by the respective option. Furthermore, the set of output results can be restricted by specifying one or more attributes that have to be contained in each element/itemset of a sequence.\n\nFor further information see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation paper = new TechnicalInformation(TechnicalInformation.Type.PROCEEDINGS);
    
    paper.setValue(TechnicalInformation.Field.AUTHOR, "Ramakrishnan Srikant and Rakesh Agrawal");
    paper.setValue(TechnicalInformation.Field.TITLE, "Mining Sequential Patterns: Generalizations and Performance Improvements");
    paper.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Database Technology EDBT '96");
    paper.setValue(TechnicalInformation.Field.YEAR, "1996");
    paper.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return paper;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tIf set, algorithm is run in debug mode and\n\tmay output additional info to the console", "D", 0, "-D"));
    



    result.addElement(new Option("\tThe miminum support threshold.\n\t(default: 0.9)", "S", 1, "-S <minimum support threshold>"));
    



    result.addElement(new Option("\tThe attribute number representing the data sequence ID.\n\t(default: 0)", "I", 1, "-I <attribute number representing the data sequence ID"));
    



    result.addElement(new Option("\tThe attribute numbers used for result filtering.\n\t(default: -1)", "F", 1, "-F <attribute numbers used for result filtering"));
    



    return result.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    setDebug(Utils.getFlag('D', options));
    
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setMinSupport(Double.parseDouble(tmpStr));
    }
    tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      setDataSeqID(Integer.parseInt(tmpStr));
    }
    tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() != 0) {
      setFilterAttributes(tmpStr);
    }
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-S");
    result.add("" + getMinSupport());
    
    result.add("-I");
    result.add("" + getDataSeqID());
    
    result.add("-F");
    result.add(getFilterAttributes());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  


  protected void resetOptions()
  {
    m_MinSupport = 0.9D;
    m_DataSeqID = 0;
    m_FilterAttributes = "-1";
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  





  public void buildAssociations(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_AllSequentialPatterns = new FastVector();
    m_Cycles = 0;
    m_FilterAttrVector = new FastVector();
    m_AlgorithmStart = getTimeAndDate();
    m_OriginalDataSet = new Instances(data);
    
    extractFilterAttributes(m_FilterAttributes);
    findFrequentSequences();
  }
  




  protected int calcFreqSequencesTotal()
  {
    int total = 0;
    Enumeration allSeqPatternsEnum = m_AllSequentialPatterns.elements();
    
    while (allSeqPatternsEnum.hasMoreElements()) {
      FastVector kSequences = (FastVector)allSeqPatternsEnum.nextElement();
      total += kSequences.size();
    }
    
    return total;
  }
  







  protected FastVector extractDataSequences(Instances originalDataSet, int dataSeqID)
  {
    FastVector dataSequences = new FastVector();
    int firstInstance = 0;
    int lastInstance = 0;
    Attribute seqIDAttribute = originalDataSet.attribute(dataSeqID);
    
    for (int i = 0; i < seqIDAttribute.numValues(); i++) {
      double sequenceID = originalDataSet.instance(firstInstance).value(dataSeqID);
      
      while ((lastInstance < originalDataSet.numInstances()) && (sequenceID == originalDataSet.instance(lastInstance).value(dataSeqID))) {
        lastInstance++;
      }
      Instances dataSequence = new Instances(originalDataSet, firstInstance, lastInstance - firstInstance);
      dataSequence.deleteAttributeAt(dataSeqID);
      dataSequences.addElement(dataSequence);
      firstInstance = lastInstance;
    }
    return dataSequences;
  }
  





  public void extractFilterAttributes(String attrNumbers)
  {
    String numbers = attrNumbers.trim();
    
    while (!numbers.equals("")) {
      int commaLoc = numbers.indexOf(',');
      
      if (commaLoc != -1) {
        String number = numbers.substring(0, commaLoc);
        numbers = numbers.substring(commaLoc + 1).trim();
        m_FilterAttrVector.addElement(Integer.decode(number));
      } else {
        m_FilterAttrVector.addElement(Integer.decode(numbers));
        break;
      }
    }
  }
  



  protected void findFrequentSequences()
    throws CloneNotSupportedException
  {
    m_CycleStart = getTimeAndDate();
    Instances originalDataSet = m_OriginalDataSet;
    FastVector dataSequences = extractDataSequences(m_OriginalDataSet, m_DataSeqID);
    long minSupportCount = Math.round(m_MinSupport * dataSequences.size());
    


    originalDataSet.deleteAttributeAt(0);
    FastVector oneElements = Element.getOneElements(originalDataSet);
    m_Cycles = 1;
    
    FastVector kSequences = Sequence.oneElementsToSequences(oneElements);
    Sequence.updateSupportCount(kSequences, dataSequences);
    kSequences = Sequence.deleteInfrequentSequences(kSequences, minSupportCount);
    
    m_CycleEnd = getTimeAndDate();
    
    if (kSequences.size() == 0) {
      return;
    }
    while (kSequences.size() > 0) {
      m_CycleStart = getTimeAndDate();
      
      m_AllSequentialPatterns.addElement(kSequences.copy());
      FastVector kMinusOneSequences = kSequences;
      kSequences = Sequence.aprioriGen(kMinusOneSequences);
      Sequence.updateSupportCount(kSequences, dataSequences);
      kSequences = Sequence.deleteInfrequentSequences(kSequences, minSupportCount);
      
      m_CycleEnd = getTimeAndDate();
      
      if (getDebug()) {
        System.out.println("Cycle " + m_Cycles + " from " + m_CycleStart + " to " + m_CycleEnd);
      }
      
      m_Cycles += 1;
    }
  }
  




  public String dataSeqIDTipText()
  {
    return "The attribute number representing the data sequence ID.";
  }
  




  public int getDataSeqID()
  {
    return m_DataSeqID;
  }
  




  public void setDataSeqID(int value)
  {
    m_DataSeqID = value;
  }
  




  public String filterAttributesTipText()
  {
    return "The attribute numbers (eg \"0, 1\") used for result filtering; only sequences containing the specified attributes in each of their elements/itemsets will be output; -1 prints all.";
  }
  








  public String getFilterAttributes()
  {
    return m_FilterAttributes;
  }
  





  public void setFilterAttributes(String value)
  {
    m_FilterAttributes = value;
  }
  




  public String minSupportTipText()
  {
    return "Minimum support threshold.";
  }
  




  public double getMinSupport()
  {
    return m_MinSupport;
  }
  




  public void setMinSupport(double value)
  {
    m_MinSupport = value;
  }
  




  public void setDebug(boolean value)
  {
    m_Debug = value;
  }
  




  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public String debugTipText()
  {
    return "If set to true, algorithm may output additional info to the console.";
  }
  






  protected String getTimeAndDate()
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    return dateFormat.format(new Date());
  }
  




  public String getAlgorithmStart()
  {
    return m_AlgorithmStart;
  }
  




  public String getCycleStart()
  {
    return m_CycleStart;
  }
  




  public String getCycleEnd()
  {
    return m_CycleEnd;
  }
  




  public String toString()
  {
    StringBuffer result = new StringBuffer();
    
    result.append("GeneralizedSequentialPatterns\n");
    result.append("=============================\n\n");
    result.append("Number of cycles performed: " + (m_Cycles - 1) + "\n");
    result.append("Total number of frequent sequences: " + calcFreqSequencesTotal() + "\n\n");
    result.append("Frequent Sequences Details (filtered):\n\n");
    for (int i = 0; i < m_AllSequentialPatterns.size(); i++) {
      result.append("- " + (i + 1) + "-sequences\n\n");
      FastVector kSequences = (FastVector)m_AllSequentialPatterns.elementAt(i);
      result.append(Sequence.setOfSequencesToString(kSequences, m_OriginalDataSet, m_FilterAttrVector) + "\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5504 $");
  }
  




  public static void main(String[] args)
  {
    runAssociator(new GeneralizedSequentialPatterns(), args);
  }
}
