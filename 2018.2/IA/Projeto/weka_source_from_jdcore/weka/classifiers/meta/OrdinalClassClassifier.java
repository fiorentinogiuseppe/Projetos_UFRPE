package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;


















































































































public class OrdinalClassClassifier
  extends SingleClassifierEnhancer
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -3461971774059603636L;
  private Classifier[] m_Classifiers;
  private MakeIndicator[] m_ClassFilters;
  private ZeroR m_ZeroR;
  
  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  


  public OrdinalClassClassifier()
  {
    m_Classifier = new J48();
  }
  




  public String globalInfo()
  {
    return "Meta classifier that allows standard classification algorithms to be applied to ordinal class problems.\n\nFor more information see: \n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Mark Hall");
    result.setValue(TechnicalInformation.Field.TITLE, "A Simple Approach to Ordinal Classification");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "12th European Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "145-156");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  








  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }
    m_ZeroR = new ZeroR();
    m_ZeroR.buildClassifier(insts);
    
    int numClassifiers = insts.numClasses() - 1;
    
    numClassifiers = numClassifiers == 0 ? 1 : numClassifiers;
    
    if (numClassifiers == 1) {
      m_Classifiers = Classifier.makeCopies(m_Classifier, 1);
      m_Classifiers[0].buildClassifier(insts);
    } else {
      m_Classifiers = Classifier.makeCopies(m_Classifier, numClassifiers);
      m_ClassFilters = new MakeIndicator[numClassifiers];
      
      for (int i = 0; i < m_Classifiers.length; i++) {
        m_ClassFilters[i] = new MakeIndicator();
        m_ClassFilters[i].setAttributeIndex("" + (insts.classIndex() + 1));
        m_ClassFilters[i].setValueIndices("" + (i + 2) + "-last");
        m_ClassFilters[i].setNumeric(false);
        m_ClassFilters[i].setInputFormat(insts);
        Instances newInsts = Filter.useFilter(insts, m_ClassFilters[i]);
        m_Classifiers[i].buildClassifier(newInsts);
      }
    }
  }
  






  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    if (m_Classifiers.length == 1) {
      return m_Classifiers[0].distributionForInstance(inst);
    }
    
    double[] probs = new double[inst.numClasses()];
    
    double[][] distributions = new double[m_ClassFilters.length][0];
    for (int i = 0; i < m_ClassFilters.length; i++) {
      m_ClassFilters[i].input(inst);
      m_ClassFilters[i].batchFinished();
      
      distributions[i] = m_Classifiers[i].distributionForInstance(m_ClassFilters[i].output());
    }
    


    for (int i = 0; i < inst.numClasses(); i++) {
      if (i == 0) {
        probs[i] = distributions[0][0];
      } else if (i == inst.numClasses() - 1) {
        probs[i] = distributions[(i - 1)][1];
      } else {
        probs[i] = (distributions[(i - 1)][1] - distributions[i][1]);
        if (probs[i] <= 0.0D) {
          System.err.println("Warning: estimated probability " + probs[i] + ". Rounding to 0.");
          
          probs[i] = 0.0D;
        }
      }
    }
    
    if (Utils.gr(Utils.sum(probs), 0.0D)) {
      Utils.normalize(probs);
      return probs;
    }
    return m_ZeroR.distributionForInstance(inst);
  }
  






  public Enumeration listOptions()
  {
    Vector vec = new Vector();
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      vec.addElement(enu.nextElement());
    }
    return vec.elements();
  }
  
























































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    return super.getOptions();
  }
  





  public String toString()
  {
    if (m_Classifiers == null) {
      return "OrdinalClassClassifier: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("OrdinalClassClassifier\n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append("Classifier ").append(i + 1);
      if (m_Classifiers[i] != null) {
        if ((m_ClassFilters != null) && (m_ClassFilters[i] != null)) {
          text.append(", using indicator values: ");
          text.append(m_ClassFilters[i].getValueRange());
        }
        text.append('\n');
        text.append(m_Classifiers[i].toString() + "\n");
      } else {
        text.append(" Skipped (no training examples)\n");
      }
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.18 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new OrdinalClassClassifier(), argv);
  }
}
