package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
























































public class C45Saver
  extends AbstractFileSaver
  implements BatchConverter, IncrementalConverter, OptionHandler
{
  static final long serialVersionUID = -821428878384253377L;
  
  public C45Saver()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "Writes to a destination that is in the format used by the C4.5 algorithm.\nTherefore it outputs a names and a data file.";
  }
  





  public String getFileDescription()
  {
    return "C4.5 file format";
  }
  




  public void resetOptions()
  {
    super.resetOptions();
    setFileExtension(".names");
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  








  public void writeIncremental(Instance inst)
    throws IOException
  {
    int writeMode = getWriteMode();
    Instances structure = getInstances();
    PrintWriter outW = null;
    
    if (structure != null) {
      if (structure.classIndex() == -1) {
        structure.setClassIndex(structure.numAttributes() - 1);
        System.err.println("No class specified. Last attribute is used as class attribute.");
      }
      
      if (structure.attribute(structure.classIndex()).isNumeric()) {
        throw new IOException("To save in C4.5 format the class attribute cannot be numeric.");
      }
    }
    
    if ((getRetrieval() == 1) || (getRetrieval() == 0)) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }
    if ((retrieveFile() == null) || (getWriter() == null)) {
      throw new IOException("C4.5 format requires two files. Therefore no output to standard out can be generated.\nPlease specifiy output files using the -o option.");
    }
    

    outW = new PrintWriter(getWriter());
    
    if (writeMode == 1) {
      if (structure == null) {
        setWriteMode(2);
        if (inst != null) {
          System.err.println("Structure(Header Information) has to be set in advance");
        }
      }
      else {
        setWriteMode(3);
      }
      writeMode = getWriteMode();
    }
    if (writeMode == 2) {
      if (outW != null) {
        outW.close();
      }
      cancel();
    }
    if (writeMode == 3) {
      setWriteMode(0);
      
      for (int i = 0; i < structure.attribute(structure.classIndex()).numValues(); 
          i++) {
        outW.write(structure.attribute(structure.classIndex()).value(i));
        if (i < structure.attribute(structure.classIndex()).numValues() - 1) {
          outW.write(",");
        } else {
          outW.write(".\n");
        }
      }
      for (int i = 0; i < structure.numAttributes(); i++) {
        if (i != structure.classIndex()) {
          outW.write(structure.attribute(i).name() + ": ");
          if ((structure.attribute(i).isNumeric()) || (structure.attribute(i).isDate()))
          {
            outW.write("continuous.\n");
          } else {
            Attribute temp = structure.attribute(i);
            for (int j = 0; j < temp.numValues(); j++) {
              outW.write(temp.value(j));
              if (j < temp.numValues() - 1) {
                outW.write(",");
              } else {
                outW.write(".\n");
              }
            }
          }
        }
      }
      outW.flush();
      outW.close();
      
      writeMode = getWriteMode();
      
      String out = retrieveFile().getAbsolutePath();
      setFileExtension(".data");
      out = out.substring(0, out.lastIndexOf('.')) + getFileExtension();
      File namesFile = new File(out);
      try {
        setFile(namesFile);
      } catch (Exception ex) {
        throw new IOException("Cannot create data file, only names file created.");
      }
      
      if ((retrieveFile() == null) || (getWriter() == null)) {
        throw new IOException("Cannot create data file, only names file created.");
      }
      
      outW = new PrintWriter(getWriter());
    }
    if (writeMode == 0) {
      if (structure == null) {
        throw new IOException("No instances information available.");
      }
      if (inst != null)
      {
        for (int j = 0; j < inst.numAttributes(); j++) {
          if (j != structure.classIndex()) {
            if (inst.isMissing(j)) {
              outW.write("?,");
            } else if ((structure.attribute(j).isNominal()) || (structure.attribute(j).isString()))
            {
              outW.write(structure.attribute(j).value((int)inst.value(j)) + ",");
            }
            else {
              outW.write("" + inst.value(j) + ",");
            }
          }
        }
        
        if (inst.isMissing(structure.classIndex())) {
          outW.write("?");
        }
        else {
          outW.write(structure.attribute(structure.classIndex()).value((int)inst.value(structure.classIndex())));
        }
        
        outW.write("\n");
        
        m_incrementalCounter += 1;
        if (m_incrementalCounter > 100) {
          m_incrementalCounter = 0;
          outW.flush();
        }
      }
      else
      {
        if (outW != null) {
          outW.flush();
          outW.close();
        }
        setFileExtension(".names");
        m_incrementalCounter = 0;
        resetStructure();
        outW = null;
        resetWriter();
      }
    }
  }
  






  public void writeBatch()
    throws IOException
  {
    Instances instances = getInstances();
    
    if (instances == null) {
      throw new IOException("No instances to save");
    }
    if (instances.classIndex() == -1) {
      instances.setClassIndex(instances.numAttributes() - 1);
      System.err.println("No class specified. Last attribute is used as class attribute.");
    }
    
    if (instances.attribute(instances.classIndex()).isNumeric()) {
      throw new IOException("To save in C4.5 format the class attribute cannot be numeric.");
    }
    
    if (getRetrieval() == 2) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }
    
    setRetrieval(1);
    if ((retrieveFile() == null) || (getWriter() == null)) {
      throw new IOException("C4.5 format requires two files. Therefore no output to standard out can be generated.\nPlease specifiy output files using the -o option.");
    }
    
    setWriteMode(0);
    
    setFileExtension(".names");
    PrintWriter outW = new PrintWriter(getWriter());
    for (int i = 0; i < instances.attribute(instances.classIndex()).numValues(); i++) {
      outW.write(instances.attribute(instances.classIndex()).value(i));
      if (i < instances.attribute(instances.classIndex()).numValues() - 1) {
        outW.write(",");
      } else {
        outW.write(".\n");
      }
    }
    for (int i = 0; i < instances.numAttributes(); i++) {
      if (i != instances.classIndex()) {
        outW.write(instances.attribute(i).name() + ": ");
        if ((instances.attribute(i).isNumeric()) || (instances.attribute(i).isDate()))
        {
          outW.write("continuous.\n");
        } else {
          Attribute temp = instances.attribute(i);
          for (int j = 0; j < temp.numValues(); j++) {
            outW.write(temp.value(j));
            if (j < temp.numValues() - 1) {
              outW.write(",");
            } else {
              outW.write(".\n");
            }
          }
        }
      }
    }
    outW.flush();
    outW.close();
    

    String out = retrieveFile().getAbsolutePath();
    setFileExtension(".data");
    out = out.substring(0, out.lastIndexOf('.')) + getFileExtension();
    File namesFile = new File(out);
    try {
      setFile(namesFile);
    } catch (Exception ex) {
      throw new IOException("Cannot create data file, only names file created (Reason: " + ex.toString() + ").");
    }
    

    if ((retrieveFile() == null) || (getWriter() == null)) {
      throw new IOException("Cannot create data file, only names file created.");
    }
    outW = new PrintWriter(getWriter());
    
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance temp = instances.instance(i);
      for (int j = 0; j < temp.numAttributes(); j++) {
        if (j != instances.classIndex()) {
          if (temp.isMissing(j)) {
            outW.write("?,");
          } else if ((instances.attribute(j).isNominal()) || (instances.attribute(j).isString()))
          {
            outW.write(instances.attribute(j).value((int)temp.value(j)) + ",");
          } else {
            outW.write("" + temp.value(j) + ",");
          }
        }
      }
      
      if (temp.isMissing(instances.classIndex())) {
        outW.write("?");
      }
      else {
        outW.write(instances.attribute(instances.classIndex()).value((int)temp.value(instances.classIndex())));
      }
      
      outW.write("\n");
    }
    outW.flush();
    outW.close();
    setFileExtension(".names");
    setWriteMode(1);
    outW = null;
    resetWriter();
    setWriteMode(2);
  }
  





  public Enumeration listOptions()
  {
    FastVector result = new FastVector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    
    result.addElement(new Option("The class index", "c", 1, "-c <the class index>"));
    


    return result.elements();
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    String outputString = Utils.getOption('o', options);
    String inputString = Utils.getOption('i', options);
    String indexString = Utils.getOption('c', options);
    
    ArffLoader loader = new ArffLoader();
    
    resetOptions();
    

    int index = -1;
    if (indexString.length() != 0) {
      if (indexString.equals("first")) {
        index = 0;
      }
      else if (indexString.equals("last")) {
        index = -1;
      } else {
        index = Integer.parseInt(indexString);
      }
    }
    

    if (inputString.length() != 0) {
      try {
        File input = new File(inputString);
        loader.setFile(input);
        Instances inst = loader.getDataSet();
        if (index == -1) {
          inst.setClassIndex(inst.numAttributes() - 1);
        } else {
          inst.setClassIndex(index);
        }
        setInstances(inst);
      } catch (Exception ex) {
        throw new IOException("No data set loaded. Data set has to be arff format (Reason: " + ex.toString() + ").");
      }
    }
    


    if (outputString.length() != 0)
    {
      if (!outputString.endsWith(getFileExtension())) {
        if (outputString.lastIndexOf('.') != -1) {
          outputString = outputString.substring(0, outputString.lastIndexOf('.')) + getFileExtension();
        }
        else
        {
          outputString = outputString + getFileExtension();
        }
      }
      try {
        File output = new File(outputString);
        setFile(output);
      } catch (Exception ex) {
        throw new IOException("Cannot create output file.");
      }
    }
    
    if (getInstances() != null) {
      if (index == -1) {
        index = getInstances().numAttributes() - 1;
      }
      getInstances().setClassIndex(index);
    }
  }
  






  public String[] getOptions()
  {
    String[] options = new String[10];
    int current = 0;
    if (retrieveFile() != null) {
      options[(current++)] = "-o";
      options[(current++)] = ("" + retrieveFile());
    }
    
    if (getInstances() != null) {
      options[(current++)] = "-i";
      options[(current++)] = ("" + getInstances().relationName());
      options[(current++)] = "-c";
      options[(current++)] = ("" + getInstances().classIndex());
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new C45Saver(), args);
  }
}
