package weka.classifiers.functions.supportVector;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Copyable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.matrix.Matrix;




















































public class PrecomputedKernelMatrixKernel
  extends Kernel
  implements Copyable
{
  static final long serialVersionUID = -321831645846363333L;
  protected File m_KernelMatrixFile = new File("kernelMatrix.matrix");
  

  protected Matrix m_KernelMatrix;
  

  protected int m_Counter;
  

  public PrecomputedKernelMatrixKernel() {}
  

  public Object copy()
  {
    PrecomputedKernelMatrixKernel newK = new PrecomputedKernelMatrixKernel();
    
    newK.setKernelMatrix(m_KernelMatrix);
    newK.setKernelMatrixFile(m_KernelMatrixFile);
    m_Counter = m_Counter;
    
    return newK;
  }
  






  public String globalInfo()
  {
    return "This kernel is based on a static kernel matrix that is read from a file. Instances must have a single nominal attribute (excluding the class). This attribute must be the first attribute in the file and its values are used to reference rows/columns in the kernel matrix. The second attribute must be the class attribute.";
  }
  











  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe file name of the file that holds the kernel matrix.\n\t(default: kernelMatrix.matrix)", "M", 1, "-M <file name>"));
    



    return result.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setKernelMatrixFile(new File(tmpStr));
    } else {
      setKernelMatrixFile(new File("kernelMatrix.matrix"));
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-M");
    result.add("" + getKernelMatrixFile());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  








  public double eval(int id1, int id2, Instance inst1)
    throws Exception
  {
    if (m_KernelMatrix == null) {
      throw new IllegalArgumentException("Kernel matrix has not been loaded successfully.");
    }
    int index1 = -1;
    if (id1 > -1) {
      index1 = (int)m_data.instance(id1).value(0);
    } else {
      index1 = (int)inst1.value(0);
    }
    int index2 = (int)m_data.instance(id2).value(0);
    return m_KernelMatrix.get(index1, index2);
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    try
    {
      if (m_KernelMatrix == null) {
        m_KernelMatrix = new Matrix(new FileReader(m_KernelMatrixFile));
      }
    }
    catch (Exception e) {
      System.err.println("Problem reading matrix from " + m_KernelMatrixFile);
    }
    m_Counter += 1;
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  




  public void setKernelMatrixFile(File f)
  {
    m_KernelMatrixFile = f;
  }
  




  public File getKernelMatrixFile()
  {
    return m_KernelMatrixFile;
  }
  





  public String kernelMatrixFileTipText()
  {
    return "The file holding the kernel matrix.";
  }
  






  protected void setKernelMatrix(Matrix km)
  {
    m_KernelMatrix = km;
  }
  




  public String toString()
  {
    return "Using kernel matrix from file with name: " + getKernelMatrixFile();
  }
  






  public void clean() {}
  






  public int numEvals()
  {
    return 0;
  }
  




  public int numCacheHits()
  {
    return 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9897 $");
  }
}
