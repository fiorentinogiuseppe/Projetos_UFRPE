package weka.classifiers.functions.supportVector;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




































































public class Puk
  extends CachedKernel
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 1682161522559978851L;
  protected double[] m_kernelPrecalc;
  protected double m_omega = 1.0D;
  

  protected double m_sigma = 1.0D;
  

  protected double m_factor = 1.0D;
  








  public Puk() {}
  








  public Puk(Instances data, int cacheSize, double omega, double sigma)
    throws Exception
  {
    setCacheSize(cacheSize);
    setOmega(omega);
    setSigma(sigma);
    
    buildKernel(data);
  }
  





  public String globalInfo()
  {
    return "The Pearson VII function-based universal kernel.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "B. Uestuen and W.J. Melssen and L.M.C. Buydens");
    result.setValue(TechnicalInformation.Field.YEAR, "2006");
    result.setValue(TechnicalInformation.Field.TITLE, "Facilitating the application of Support Vector Regression by using a universal Pearson VII function based kernel");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Chemometrics and Intelligent Laboratory Systems");
    result.setValue(TechnicalInformation.Field.VOLUME, "81");
    result.setValue(TechnicalInformation.Field.PAGES, "29-40");
    result.setValue(TechnicalInformation.Field.PDF, "http://www.cac.science.ru.nl/research/publications/PDFs/ustun2006.pdf");
    
    return result;
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe Omega parameter.\n\t(default: 1.0)", "O", 1, "-O <num>"));
    



    result.addElement(new Option("\tThe Sigma parameter.\n\t(default: 1.0)", "S", 1, "-S <num>"));
    



    return result.elements();
  }
  
































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('O', options);
    if (tmpStr.length() != 0) {
      setOmega(Double.parseDouble(tmpStr));
    } else {
      setOmega(1.0D);
    }
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSigma(Double.parseDouble(tmpStr));
    } else {
      setSigma(1.0D);
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-O");
    result.add("" + getOmega());
    
    result.add("-S");
    result.add("" + getSigma());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  









  protected double evaluate(int id1, int id2, Instance inst1)
    throws Exception
  {
    if (id1 == id2)
      return 1.0D;
    double precalc1;
    double precalc1;
    if (id1 == -1) {
      precalc1 = dotProd(inst1, inst1);
    } else
      precalc1 = m_kernelPrecalc[id1];
    Instance inst2 = m_data.instance(id2);
    double squaredDifference = -2.0D * dotProd(inst1, inst2) + precalc1 + m_kernelPrecalc[id2];
    double intermediate = m_factor * Math.sqrt(squaredDifference);
    double result = 1.0D / Math.pow(1.0D + intermediate * intermediate, getOmega());
    return result;
  }
  





  public void setOmega(double value)
  {
    m_omega = value;
    m_factor = computeFactor(m_omega, m_sigma);
  }
  




  public double getOmega()
  {
    return m_omega;
  }
  





  public String omegaTipText()
  {
    return "The Omega value.";
  }
  




  public void setSigma(double value)
  {
    m_sigma = value;
    m_factor = computeFactor(m_omega, m_sigma);
  }
  




  public double getSigma()
  {
    return m_sigma;
  }
  





  public String sigmaTipText()
  {
    return "The Sigma value.";
  }
  






  protected double computeFactor(double omega, double sigma)
  {
    double root = Math.sqrt(Math.pow(2.0D, 1.0D / omega) - 1.0D);
    return 2.0D * root / sigma;
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    
    m_factor = computeFactor(m_omega, m_sigma);
    m_kernelPrecalc = new double[data.numInstances()];
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildKernel(Instances data)
    throws Exception
  {
    if (!getChecksTurnedOff()) {
      getCapabilities().testWithFail(data);
    }
    initVars(data);
    
    for (int i = 0; i < data.numInstances(); i++) {
      m_kernelPrecalc[i] = dotProd(data.instance(i), data.instance(i));
    }
  }
  



  public String toString()
  {
    return "Puk kernel";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5518 $");
  }
}
