package weka.datagenerators;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.Utils;






































public abstract class ClassificationGenerator
  extends DataGenerator
{
  private static final long serialVersionUID = -5261662546673517844L;
  protected int m_NumExamples;
  
  public ClassificationGenerator()
  {
    setNumExamples(defaultNumExamples());
  }
  




  public Enumeration listOptions()
  {
    Vector result = enumToVector(super.listOptions());
    
    result.addElement(new Option("\tThe number of examples to generate (default " + defaultNumExamples() + ")", "n", 1, "-n <num>"));
    



    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('n', options);
    if (tmpStr.length() != 0) {
      setNumExamples(Integer.parseInt(tmpStr));
    } else {
      setNumExamples(defaultNumExamples());
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-n");
    result.add("" + getNumExamples());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected int defaultNumExamples()
  {
    return 100;
  }
  



  public void setNumExamples(int numExamples)
  {
    m_NumExamples = numExamples;
  }
  



  public int getNumExamples()
  {
    return m_NumExamples;
  }
  





  public String numExamplesTipText()
  {
    return "The number of examples to generate.";
  }
}
