package weka.classifiers;

import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
























































public class CheckSource
  implements OptionHandler, RevisionHandler
{
  protected Classifier m_Classifier = null;
  

  protected Classifier m_SourceCode = null;
  

  protected File m_Dataset = null;
  

  protected int m_ClassIndex = -1;
  

  public CheckSource() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe classifier (incl. options) that was used to generate\n\tthe source code.", "W", 1, "-W <classname and options>"));
    



    result.addElement(new Option("\tThe classname of the generated source code.", "S", 1, "-S <classname>"));
    


    result.addElement(new Option("\tThe training set with which the source code was generated.", "t", 1, "-t <file>"));
    


    result.addElement(new Option("\tThe class index of the training set. 'first' and 'last' are\n\tvalid indices.\n\t(default: last)", "c", 1, "-c <index>"));
    




    return result.elements();
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0) {
      String[] spec = Utils.splitOptions(tmpStr);
      if (spec.length == 0)
        throw new IllegalArgumentException("Invalid classifier specification string");
      String classname = spec[0];
      spec[0] = "";
      setClassifier((Classifier)Utils.forName(Classifier.class, classname, spec));
    }
    else {
      throw new Exception("No classifier (classname + options) provided!"); }
    String classname;
    String[] spec;
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() > 0) {
      spec = Utils.splitOptions(tmpStr);
      if (spec.length != 1)
        throw new IllegalArgumentException("Invalid source code specification string");
      classname = spec[0];
      spec[0] = "";
      setSourceCode((Classifier)Utils.forName(Classifier.class, classname, spec));
    }
    else {
      throw new Exception("No source code (classname) provided!");
    }
    
    tmpStr = Utils.getOption('t', options);
    if (tmpStr.length() != 0) {
      setDataset(new File(tmpStr));
    } else {
      throw new Exception("No dataset provided!");
    }
    tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() != 0) {
      if (tmpStr.equals("first")) {
        setClassIndex(0);
      } else if (tmpStr.equals("last")) {
        setClassIndex(-1);
      } else {
        setClassIndex(Integer.parseInt(tmpStr) - 1);
      }
    } else {
      setClassIndex(-1);
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getClassifier() != null) {
      result.add("-W");
      result.add(getClassifier().getClass().getName() + " " + Utils.joinOptions(getClassifier().getOptions()));
    }
    

    if (getSourceCode() != null) {
      result.add("-S");
      result.add(getSourceCode().getClass().getName());
    }
    
    if (getDataset() != null) {
      result.add("-t");
      result.add(m_Dataset.getAbsolutePath());
    }
    
    result.add("-c");
    if (getClassIndex() == -1) {
      result.add("last");
    } else if (getClassIndex() == 0) {
      result.add("first");
    } else {
      result.add("" + (getClassIndex() + 1));
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setClassifier(Classifier value)
  {
    m_Classifier = value;
  }
  




  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  




  public void setSourceCode(Classifier value)
  {
    m_SourceCode = value;
  }
  




  public Classifier getSourceCode()
  {
    return m_SourceCode;
  }
  




  public void setDataset(File value)
  {
    if (!value.exists()) {
      throw new IllegalArgumentException("Dataset '" + value.getAbsolutePath() + "' does not exist!");
    }
    
    m_Dataset = value;
  }
  




  public File getDataset()
  {
    return m_Dataset;
  }
  




  public void setClassIndex(int value)
  {
    m_ClassIndex = value;
  }
  




  public int getClassIndex()
  {
    return m_ClassIndex;
  }
  















  public boolean execute()
    throws Exception
  {
    boolean result = true;
    

    if (getClassifier() == null)
      throw new Exception("No classifier set!");
    if (getSourceCode() == null)
      throw new Exception("No source code set!");
    if (getDataset() == null)
      throw new Exception("No dataset set!");
    if (!getDataset().exists()) {
      throw new Exception("Dataset '" + getDataset().getAbsolutePath() + "' does not exist!");
    }
    

    ConverterUtils.DataSource source = new ConverterUtils.DataSource(getDataset().getAbsolutePath());
    Instances data = source.getDataSet();
    if (getClassIndex() == -1) {
      data.setClassIndex(data.numAttributes() - 1);
    } else
      data.setClassIndex(getClassIndex());
    boolean numeric = data.classAttribute().isNumeric();
    

    Classifier cls = Classifier.makeCopy(getClassifier());
    cls.buildClassifier(data);
    
    Classifier code = getSourceCode();
    

    for (int i = 0; i < data.numInstances(); i++)
    {
      double predClassifier = cls.classifyInstance(data.instance(i));
      double predSource = code.classifyInstance(data.instance(i));
      boolean different;
      boolean different;
      if ((Double.isNaN(predClassifier)) && (Double.isNaN(predSource))) {
        different = false;
      } else {
        boolean different;
        if (numeric) {
          different = !Utils.eq(predClassifier, predSource);
        } else {
          different = (int)predClassifier != (int)predSource;
        }
      }
      if (different) {
        result = false;
        if (numeric) {
          System.out.println(i + 1 + ". instance (Classifier/Source code): " + predClassifier + " != " + predSource);
        }
        else
        {
          System.out.println(i + 1 + ". instance (Classifier/Source code): " + data.classAttribute().value((int)predClassifier) + " != " + data.classAttribute().value((int)predSource));
        }
      }
    }
    


    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  








  public static void main(String[] args)
    throws Exception
  {
    CheckSource check = new CheckSource();
    if (Utils.getFlag('h', args)) {
      StringBuffer text = new StringBuffer();
      text.append("\nHelp requested:\n\n");
      Enumeration enm = check.listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        text.append(option.synopsis() + "\n");
        text.append(option.description() + "\n");
      }
      System.out.println("\n" + text + "\n");
    }
    else {
      check.setOptions(args);
      if (check.execute()) {
        System.out.println("Tests OK!");
      } else {
        System.out.println("Tests failed!");
      }
    }
  }
}
