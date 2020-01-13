package weka.datagenerators.classifiers.classification;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.datagenerators.ClassificationGenerator;



















































































































public class Agrawal
  extends ClassificationGenerator
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 2254651939636143025L;
  protected static ClassFunction[] builtInFunctions = { new ClassFunction()new ClassFunction
  {

    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {

      if ((age < 40) || (60 <= age)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {







    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {






      if (age < 40) {
        if ((50000.0D <= salary) && (salary <= 100000.0D)) {
          return 0L;
        }
        return 1L; }
      if (age < 60) {
        if ((75000.0D <= salary) && (salary <= 125000.0D)) {
          return 0L;
        }
        return 1L;
      }
      if ((25000.0D <= salary) && (salary <= 75000.0D)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {


















    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {

















      if (age < 40) {
        if ((elevel == 0) || (elevel == 1)) {
          return 0L;
        }
        return 1L; }
      if (age < 60) {
        if ((elevel == 1) || (elevel == 2) || (elevel == 3)) {
          return 0L;
        }
        return 1L;
      }
      if ((elevel == 2) || (elevel == 3) || (elevel == 4)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {





























    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {




























      if (age < 40) {
        if ((elevel == 0) || (elevel == 1)) {
          if ((25000.0D <= salary) && (salary <= 75000.0D)) {
            return 0L;
          }
          return 1L; }
        if ((50000.0D <= salary) && (salary <= 100000.0D)) {
          return 0L;
        }
        return 1L; }
      if (age < 60) {
        if ((elevel == 1) || (elevel == 2) || (elevel == 3)) {
          if ((50000.0D <= salary) && (salary <= 100000.0D)) {
            return 0L;
          }
          return 1L; }
        if ((75000.0D <= salary) && (salary <= 125000.0D)) {
          return 0L;
        }
        return 1L;
      }
      if ((elevel == 2) || (elevel == 3) || (elevel == 4)) {
        if ((50000.0D <= salary) && (salary <= 100000.0D)) {
          return 0L;
        }
        return 1L; }
      if ((25000.0D <= salary) && (salary <= 75000.0D)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {















































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {















































      if (age < 40) {
        if ((50000.0D <= salary) && (salary <= 100000.0D)) {
          if ((100000.0D <= loan) && (loan <= 300000.0D)) {
            return 0L;
          }
          return 1L; }
        if ((200000.0D <= loan) && (loan <= 400000.0D)) {
          return 0L;
        }
        return 1L; }
      if (age < 60) {
        if ((75000.0D <= salary) && (salary <= 125000.0D)) {
          if ((200000.0D <= loan) && (loan <= 400000.0D)) {
            return 0L;
          }
          return 1L; }
        if ((300000.0D <= loan) && (loan <= 500000.0D)) {
          return 0L;
        }
        return 1L;
      }
      if ((25000.0D <= salary) && (salary <= 75000.0D)) {
        if ((300000.0D <= loan) && (loan <= 500000.0D)) {
          return 0L;
        }
        return 1L; }
      if ((100000.0D <= loan) && (loan <= 300000.0D)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {


































































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {

































































      double totalSalary = salary + commission;
      if (age < 40) {
        if ((50000.0D <= totalSalary) && (totalSalary <= 100000.0D)) {
          return 0L;
        }
        return 1L; }
      if (age < 60) {
        if ((75000.0D <= totalSalary) && (totalSalary <= 125000.0D)) {
          return 0L;
        }
        return 1L;
      }
      if ((25000.0D <= totalSalary) && (totalSalary <= 75000.0D)) {
        return 0L;
      }
      return 1L;
    }
  }, new ClassFunction()new ClassFunction
  {













































































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {













































































      double disposable = 2.0D * (salary + commission) / 3.0D - loan / 5.0D - 20000.0D;
      
      return disposable > 0.0D ? 0L : 1L;
    }
  }, new ClassFunction()new ClassFunction
  {


















































































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {


















































































      double disposable = 2.0D * (salary + commission) / 3.0D - 5000.0D * elevel - 20000.0D;
      
      return disposable > 0.0D ? 0L : 1L;
    }
  }, new ClassFunction()new ClassFunction
  {























































































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {























































































      double disposable = 2.0D * (salary + commission) / 3.0D - 5000.0D * elevel - loan / 5.0D - 10000.0D;
      
      return disposable > 0.0D ? 0L : 1L;
    }
  }, new ClassFunction()
  {




























































































    public long determineClass(double salary, double commission, int age, int elevel, int car, int zipcode, double hvalue, int hyears, double loan)
    {




























































































      double equity = 0.0D;
      if (hyears >= 20)
        equity = hvalue * (hyears - 20.0D) / 10.0D;
      double disposable = 2.0D * (salary + commission) / 3.0D - 5000.0D * elevel + equity / 5.0D - 10000.0D;
      
      return disposable > 0.0D ? 0L : 1L;
    }
  } };
  


















  public static final int FUNCTION_1 = 1;
  


















  public static final int FUNCTION_2 = 2;
  


















  public static final int FUNCTION_3 = 3;
  


















  public static final int FUNCTION_4 = 4;
  


















  public static final int FUNCTION_5 = 5;
  


















  public static final int FUNCTION_6 = 6;
  


















  public static final int FUNCTION_7 = 7;
  


















  public static final int FUNCTION_8 = 8;
  


















  public static final int FUNCTION_9 = 9;
  


















  public static final int FUNCTION_10 = 10;
  

















  public static final Tag[] FUNCTION_TAGS = { new Tag(1, "Function 1"), new Tag(2, "Function 2"), new Tag(3, "Function 3"), new Tag(4, "Function 4"), new Tag(5, "Function 5"), new Tag(6, "Function 6"), new Tag(7, "Function 7"), new Tag(8, "Function 8"), new Tag(9, "Function 9"), new Tag(10, "Function 10") };
  




  protected int m_Function;
  



  protected boolean m_BalanceClass;
  



  protected double m_PerturbationFraction;
  



  protected boolean m_nextClassShouldBeZero;
  



  protected double m_lastLabel;
  




  public Agrawal()
  {
    setFunction(defaultFunction());
    setBalanceClass(defaultBalanceClass());
    setPerturbationFraction(defaultPerturbationFraction());
  }
  





  public String globalInfo()
  {
    return "Generates a people database and is based on the paper by Agrawal et al.:\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R. Agrawal and T. Imielinski and A. Swami");
    result.setValue(TechnicalInformation.Field.YEAR, "1993");
    result.setValue(TechnicalInformation.Field.TITLE, "Database Mining: A Performance Perspective");
    result.setValue(TechnicalInformation.Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
    result.setValue(TechnicalInformation.Field.VOLUME, "5");
    result.setValue(TechnicalInformation.Field.NUMBER, "6");
    result.setValue(TechnicalInformation.Field.PAGES, "914-925");
    result.setValue(TechnicalInformation.Field.NOTE, "Special issue on Learning and Discovery in Knowledge-Based Databases");
    result.setValue(TechnicalInformation.Field.URL, "http://www.almaden.ibm.com/software/quest/Publications/ByDate.html");
    result.setValue(TechnicalInformation.Field.PDF, "http://www.almaden.ibm.com/software/quest/Publications/papers/tkde93.pdf");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.add(new Option("\tThe function to use for generating the data. (default " + defaultFunction().getSelectedTag().getID() + ")", "F", 1, "-F <num>"));
    



    result.add(new Option("\tWhether to balance the class.", "B", 0, "-B"));
    


    result.add(new Option("\tThe perturbation factor. (default " + defaultPerturbationFraction() + ")", "P", 1, "-P <num>"));
    



    return result.elements();
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('F', options);
    if (tmpStr.length() != 0) {
      setFunction(new SelectedTag(Integer.parseInt(tmpStr), FUNCTION_TAGS));
    } else {
      setFunction(defaultFunction());
    }
    setBalanceClass(Utils.getFlag('B', options));
    
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setPerturbationFraction(Double.parseDouble(tmpStr));
    } else {
      setPerturbationFraction(defaultPerturbationFraction());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-F");
    result.add("" + m_Function);
    
    if (getBalanceClass()) {
      result.add("-B");
    }
    result.add("-P");
    result.add("" + getPerturbationFraction());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected SelectedTag defaultFunction()
  {
    return new SelectedTag(1, FUNCTION_TAGS);
  }
  





  public SelectedTag getFunction()
  {
    return new SelectedTag(m_Function, FUNCTION_TAGS);
  }
  





  public void setFunction(SelectedTag value)
  {
    if (value.getTags() == FUNCTION_TAGS) {
      m_Function = value.getSelectedTag().getID();
    }
  }
  



  public String functionTipText()
  {
    return "The function to use for generating the data.";
  }
  




  protected boolean defaultBalanceClass()
  {
    return false;
  }
  




  public boolean getBalanceClass()
  {
    return m_BalanceClass;
  }
  




  public void setBalanceClass(boolean value)
  {
    m_BalanceClass = value;
  }
  





  public String balanceClassTipText()
  {
    return "Whether to balance the class.";
  }
  




  protected double defaultPerturbationFraction()
  {
    return 0.05D;
  }
  




  public double getPerturbationFraction()
  {
    return m_PerturbationFraction;
  }
  




  public void setPerturbationFraction(double value)
  {
    if ((value >= 0.0D) && (value <= 1.0D)) {
      m_PerturbationFraction = value;
    } else {
      throw new IllegalArgumentException("Perturbation fraction must be in [0,1] (provided: " + value + ")!");
    }
  }
  





  public String perturbationFractionTipText()
  {
    return "The perturbation fraction: 0 <= fraction <= 1.";
  }
  





  public boolean getSingleModeFlag()
    throws Exception
  {
    return true;
  }
  












  public Instances defineDataFormat()
    throws Exception
  {
    m_Random = new Random(getSeed());
    m_nextClassShouldBeZero = true;
    m_lastLabel = NaN.0D;
    

    setNumExamplesAct(getNumExamples());
    

    FastVector atts = new FastVector();
    
    atts.addElement(new Attribute("salary"));
    
    atts.addElement(new Attribute("commission"));
    
    FastVector attValues = new FastVector();
    atts.addElement(new Attribute("age"));
    
    attValues = new FastVector();
    for (int i = 0; i < 5; i++)
      attValues.addElement("" + i);
    atts.addElement(new Attribute("elevel", attValues));
    
    attValues = new FastVector();
    for (i = 1; i <= 20; i++)
      attValues.addElement("" + i);
    atts.addElement(new Attribute("car", attValues));
    
    attValues = new FastVector();
    for (i = 0; i < 9; i++)
      attValues.addElement("" + i);
    atts.addElement(new Attribute("zipcode", attValues));
    
    atts.addElement(new Attribute("hvalue"));
    
    atts.addElement(new Attribute("hyears"));
    
    atts.addElement(new Attribute("loan"));
    
    attValues = new FastVector();
    for (i = 0; i < 2; i++)
      attValues.addElement("" + i);
    atts.addElement(new Attribute("group", attValues));
    

    m_DatasetFormat = new Instances(getRelationNameToUse(), atts, 0);
    
    return m_DatasetFormat;
  }
  







  protected double perturbValue(double val, double min, double max)
  {
    return perturbValue(val, max - min, min, max);
  }
  










  protected double perturbValue(double val, double range, double min, double max)
  {
    val += range * (2.0D * (getRandom().nextDouble() - 0.5D)) * getPerturbationFraction();
    

    if (val < min) {
      val = min;
    } else if (val > max) {
      val = max;
    }
    return val;
  }
  





















  public Instance generateExample()
    throws Exception
  {
    Instance result = null;
    Random random = getRandom();
    
    if (m_DatasetFormat == null) {
      throw new Exception("Dataset format not defined.");
    }
    double salary = 0.0D;
    double commission = 0.0D;
    double hvalue = 0.0D;
    double loan = 0.0D;
    int age = 0;
    int elevel = 0;
    int car = 0;
    int zipcode = 0;
    int hyears = 0;
    boolean desiredClassFound = false;
    ClassFunction classFunction = builtInFunctions[(m_Function - 1)];
    
    while (!desiredClassFound)
    {
      salary = 20000.0D + 130000.0D * random.nextDouble();
      commission = salary >= 75000.0D ? 0.0D : 10000.0D + 65000.0D * random.nextDouble();
      
      age = 20 + random.nextInt(61);
      elevel = random.nextInt(5);
      car = 1 + random.nextInt(20);
      zipcode = random.nextInt(9);
      hvalue = (9.0D - zipcode) * 100000.0D * (0.5D + random.nextDouble());
      
      hyears = 1 + random.nextInt(30);
      loan = random.nextDouble() * 500000.0D;
      

      m_lastLabel = classFunction.determineClass(salary, commission, age, elevel, car, zipcode, hvalue, hyears, loan);
      
      if (!getBalanceClass()) {
        desiredClassFound = true;


      }
      else if (((m_nextClassShouldBeZero) && (m_lastLabel == 0.0D)) || ((!m_nextClassShouldBeZero) && (m_lastLabel == 1.0D)))
      {
        desiredClassFound = true;
        m_nextClassShouldBeZero = (!m_nextClassShouldBeZero);
      }
    }
    


    if (getPerturbationFraction() > 0.0D) {
      salary = perturbValue(salary, 20000.0D, 150000.0D);
      if (commission > 0.0D)
        commission = perturbValue(commission, 10000.0D, 75000.0D);
      age = (int)Math.round(perturbValue(age, 20.0D, 80.0D));
      hvalue = perturbValue(hvalue, (9.0D - zipcode) * 100000.0D, 0.0D, 135000.0D);
      
      hyears = (int)Math.round(perturbValue(hyears, 1.0D, 30.0D));
      loan = perturbValue(loan, 0.0D, 500000.0D);
    }
    

    double[] atts = new double[m_DatasetFormat.numAttributes()];
    atts[0] = salary;
    atts[1] = commission;
    atts[2] = age;
    atts[3] = elevel;
    atts[4] = (car - 1);
    atts[5] = zipcode;
    atts[6] = hvalue;
    atts[7] = hyears;
    atts[8] = loan;
    atts[9] = m_lastLabel;
    result = new Instance(1.0D, atts);
    result.setDataset(m_DatasetFormat);
    
    return result;
  }
  











  public Instances generateExamples()
    throws Exception
  {
    Instances result = new Instances(m_DatasetFormat, 0);
    m_Random = new Random(getSeed());
    
    for (int i = 0; i < getNumExamplesAct(); i++) {
      result.add(generateExample());
    }
    return result;
  }
  






  public String generateStart()
  {
    return "";
  }
  






  public String generateFinished()
    throws Exception
  {
    return "";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  




  public static void main(String[] args)
  {
    runDataGenerator(new Agrawal(), args);
  }
  
  protected static abstract interface ClassFunction
  {
    public abstract long determineClass(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble3, int paramInt5, double paramDouble4);
  }
}
