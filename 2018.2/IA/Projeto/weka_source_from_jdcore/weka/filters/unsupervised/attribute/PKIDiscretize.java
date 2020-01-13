package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

















































































public class PKIDiscretize
  extends Discretize
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 6153101248977702675L;
  
  public PKIDiscretize() {}
  
  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    m_FindNumBins = true;
    return super.setInputFormat(instanceInfo);
  }
  





  protected void findNumBins(int index)
  {
    Instances toFilter = getInputFormat();
    

    int numOfInstances = toFilter.numInstances();
    for (int i = 0; i < toFilter.numInstances(); i++) {
      if (toFilter.instance(i).isMissing(index)) {
        numOfInstances--;
      }
    }
    m_NumBins = ((int)Math.sqrt(numOfInstances));
    
    if (m_NumBins > 0) {
      calculateCutPointsByEqualFrequencyBinning(index);
    }
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tUnsets the class index temporarily before the filter is\n\tapplied to the data.\n\t(default: no)", "unset-class-temporarily", 1, "-unset-class-temporarily"));
    




    result.addElement(new Option("\tSpecifies list of columns to Discretize. First and last are valid indexes.\n\t(default: first-last)", "R", 1, "-R <col1,col2-col4,...>"));
    




    result.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    result.addElement(new Option("\tOutput binary attributes for discretized attributes.", "D", 0, "-D"));
    


    return result.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    setIgnoreClass(Utils.getFlag("unset-class-temporarily", options));
    setMakeBinary(Utils.getFlag('D', options));
    setInvertSelection(Utils.getFlag('V', options));
    
    String convertList = Utils.getOption('R', options);
    if (convertList.length() != 0) {
      setAttributeIndices(convertList);
    } else {
      setAttributeIndices("first-last");
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getMakeBinary()) {
      result.add("-D");
    }
    if (getInvertSelection()) {
      result.add("-V");
    }
    if (!getAttributeIndices().equals("")) {
      result.add("-R");
      result.add(getAttributeIndices());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  public String globalInfo()
  {
    return "Discretizes numeric attributes using equal frequency binning, where the number of bins is equal to the square root of the number of non-missing values.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Ying Yang and Geoffrey I. Webb");
    result.setValue(TechnicalInformation.Field.TITLE, "Proportional k-Interval Discretization for Naive-Bayes Classifiers");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "12th European Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "564-575");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    result.setValue(TechnicalInformation.Field.SERIES, "LNCS");
    result.setValue(TechnicalInformation.Field.VOLUME, "2167");
    
    return result;
  }
  






  public String findNumBinsTipText()
  {
    return "Ignored.";
  }
  





  public boolean getFindNumBins()
  {
    return false;
  }
  







  public void setFindNumBins(boolean newFindNumBins) {}
  






  public String useEqualFrequencyTipText()
  {
    return "Always true.";
  }
  





  public boolean getUseEqualFrequency()
  {
    return true;
  }
  







  public void setUseEqualFrequency(boolean newUseEqualFrequency) {}
  






  public String binsTipText()
  {
    return "Ignored.";
  }
  





  public int getBins()
  {
    return 0;
  }
  






  public void setBins(int numBins) {}
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new PKIDiscretize(), argv);
  }
}
