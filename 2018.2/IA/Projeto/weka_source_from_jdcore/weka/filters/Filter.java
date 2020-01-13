package weka.filters;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Queue;
import weka.core.RelationalLocator;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.StringLocator;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;
import weka.core.Version;
import weka.core.converters.ConverterUtils.DataSource;























































public abstract class Filter
  implements Serializable, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = -8835063755891851218L;
  private Instances m_OutputFormat = null;
  

  private Queue m_OutputQueue = null;
  

  protected StringLocator m_OutputStringAtts = null;
  

  protected StringLocator m_InputStringAtts = null;
  

  protected RelationalLocator m_OutputRelAtts = null;
  

  protected RelationalLocator m_InputRelAtts = null;
  

  private Instances m_InputFormat = null;
  

  protected boolean m_NewBatch = true;
  

  protected boolean m_FirstBatchDone = false;
  



  public Filter() {}
  


  public boolean isNewBatch()
  {
    return m_NewBatch;
  }
  








  public boolean isFirstBatchDone()
  {
    return m_FirstBatchDone;
  }
  








  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7880 $");
  }
  














  public Capabilities getCapabilities(Instances data)
  {
    Capabilities result = getCapabilities();
    

    if (data.classIndex() == -1) {
      Capabilities classes = result.getClassCapabilities();
      Iterator iter = classes.capabilities();
      while (iter.hasNext()) {
        Capabilities.Capability cap = (Capabilities.Capability)iter.next();
        if (cap != Capabilities.Capability.NO_CLASS) {
          result.disable(cap);
          result.disableDependency(cap);
        }
      }
    }
    

    result.disable(Capabilities.Capability.NO_CLASS);
    result.disableDependency(Capabilities.Capability.NO_CLASS);
    

    return result;
  }
  







  protected void setOutputFormat(Instances outputFormat)
  {
    if (outputFormat != null) {
      m_OutputFormat = outputFormat.stringFreeStructure();
      initOutputLocators(m_OutputFormat, null);
      

      String relationName = outputFormat.relationName() + "-" + getClass().getName();
      
      if ((this instanceof OptionHandler)) {
        String[] options = ((OptionHandler)this).getOptions();
        for (int i = 0; i < options.length; i++) {
          relationName = relationName + options[i].trim();
        }
      }
      m_OutputFormat.setRelationName(relationName);
    } else {
      m_OutputFormat = null;
    }
    m_OutputQueue = new Queue();
  }
  






  protected Instances getInputFormat()
  {
    return m_InputFormat;
  }
  






  protected Instances inputFormatPeek()
  {
    return m_InputFormat;
  }
  






  protected Instances outputFormatPeek()
  {
    return m_OutputFormat;
  }
  






  protected void push(Instance instance)
  {
    if (instance != null) {
      if (instance.dataset() != null)
        copyValues(instance, false);
      instance.setDataset(m_OutputFormat);
      m_OutputQueue.push(instance);
    }
  }
  



  protected void resetQueue()
  {
    m_OutputQueue = new Queue();
  }
  








  protected void bufferInput(Instance instance)
  {
    if (instance != null) {
      copyValues(instance, true);
      m_InputFormat.add(instance);
    }
  }
  








  protected void initInputLocators(Instances data, int[] indices)
  {
    if (indices == null) {
      m_InputStringAtts = new StringLocator(data);
      m_InputRelAtts = new RelationalLocator(data);
    }
    else {
      m_InputStringAtts = new StringLocator(data, indices);
      m_InputRelAtts = new RelationalLocator(data, indices);
    }
  }
  








  protected void initOutputLocators(Instances data, int[] indices)
  {
    if (indices == null) {
      m_OutputStringAtts = new StringLocator(data);
      m_OutputRelAtts = new RelationalLocator(data);
    }
    else {
      m_OutputStringAtts = new StringLocator(data, indices);
      m_OutputRelAtts = new RelationalLocator(data, indices);
    }
  }
  











  protected void copyValues(Instance instance, boolean isInput)
  {
    RelationalLocator.copyRelationalValues(instance, isInput ? m_InputFormat : m_OutputFormat, isInput ? m_InputRelAtts : m_OutputRelAtts);
    



    StringLocator.copyStringValues(instance, isInput ? m_InputFormat : m_OutputFormat, isInput ? m_InputStringAtts : m_OutputStringAtts);
  }
  






























  protected void copyValues(Instance instance, boolean instSrcCompat, Instances srcDataset, Instances destDataset)
  {
    RelationalLocator.copyRelationalValues(instance, instSrcCompat, srcDataset, m_InputRelAtts, destDataset, m_OutputRelAtts);
    



    StringLocator.copyStringValues(instance, instSrcCompat, srcDataset, m_InputStringAtts, getOutputFormat(), m_OutputStringAtts);
  }
  







  protected void flushInput()
  {
    if ((m_InputStringAtts.getAttributeIndices().length > 0) || (m_InputRelAtts.getAttributeIndices().length > 0))
    {
      m_InputFormat = m_InputFormat.stringFreeStructure();
      m_InputStringAtts = new StringLocator(m_InputFormat, m_InputStringAtts.getAllowedIndices());
      m_InputRelAtts = new RelationalLocator(m_InputFormat, m_InputRelAtts.getAllowedIndices());
    }
    else {
      m_InputFormat.delete();
    }
  }
  




  protected void testInputFormat(Instances instanceInfo)
    throws Exception
  {
    getCapabilities(instanceInfo).testWithFail(instanceInfo);
  }
  












  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    testInputFormat(instanceInfo);
    
    m_InputFormat = instanceInfo.stringFreeStructure();
    m_OutputFormat = null;
    m_OutputQueue = new Queue();
    m_NewBatch = true;
    m_FirstBatchDone = false;
    initInputLocators(m_InputFormat, null);
    return false;
  }
  











  public Instances getOutputFormat()
  {
    if (m_OutputFormat == null) {
      throw new NullPointerException("No output format defined.");
    }
    return new Instances(m_OutputFormat, 0);
  }
  
















  public boolean input(Instance instance)
    throws Exception
  {
    if (m_InputFormat == null) {
      throw new NullPointerException("No input instance format defined");
    }
    if (m_NewBatch) {
      m_OutputQueue = new Queue();
      m_NewBatch = false;
    }
    bufferInput(instance);
    return false;
  }
  













  public boolean batchFinished()
    throws Exception
  {
    if (m_InputFormat == null) {
      throw new NullPointerException("No input instance format defined");
    }
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    if (m_OutputQueue.empty())
    {
      if ((m_OutputStringAtts.getAttributeIndices().length > 0) || (m_OutputRelAtts.getAttributeIndices().length > 0))
      {
        m_OutputFormat = m_OutputFormat.stringFreeStructure();
        m_OutputStringAtts = new StringLocator(m_OutputFormat, m_OutputStringAtts.getAllowedIndices());
      }
    }
    
    return numPendingOutput() != 0;
  }
  








  public Instance output()
  {
    if (m_OutputFormat == null) {
      throw new NullPointerException("No output instance format defined");
    }
    if (m_OutputQueue.empty()) {
      return null;
    }
    Instance result = (Instance)m_OutputQueue.pop();
    
    return result;
  }
  








  public Instance outputPeek()
  {
    if (m_OutputFormat == null) {
      throw new NullPointerException("No output instance format defined");
    }
    if (m_OutputQueue.empty()) {
      return null;
    }
    Instance result = (Instance)m_OutputQueue.peek();
    return result;
  }
  






  public int numPendingOutput()
  {
    if (m_OutputFormat == null) {
      throw new NullPointerException("No output instance format defined");
    }
    return m_OutputQueue.size();
  }
  





  public boolean isOutputFormatDefined()
  {
    return m_OutputFormat != null;
  }
  





  public static Filter makeCopy(Filter model)
    throws Exception
  {
    return (Filter)new SerializedObject(model).getObject();
  }
  








  public static Filter[] makeCopies(Filter model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model filter set");
    }
    Filter[] filters = new Filter[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < filters.length; i++) {
      filters[i] = ((Filter)so.getObject());
    }
    return filters;
  }
  












  public static Instances useFilter(Instances data, Filter filter)
    throws Exception
  {
    for (int i = 0; i < data.numInstances(); i++) {
      filter.input(data.instance(i));
    }
    filter.batchFinished();
    Instances newData = filter.getOutputFormat();
    Instance processed;
    while ((processed = filter.output()) != null) {
      newData.add(processed);
    }
    




    return newData;
  }
  




  public String toString()
  {
    return getClass().getName();
  }
  















  public static String wekaStaticWrapper(Sourcable filter, String className, Instances input, Instances output)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    
    result.append("// Generated with Weka " + Version.VERSION + "\n");
    result.append("//\n");
    result.append("// This code is public domain and comes with no warranty.\n");
    result.append("//\n");
    result.append("// Timestamp: " + new Date() + "\n");
    result.append("// Relation: " + input.relationName() + "\n");
    result.append("\n");
    
    result.append("package weka.filters;\n");
    result.append("\n");
    result.append("import weka.core.Attribute;\n");
    result.append("import weka.core.Capabilities;\n");
    result.append("import weka.core.Capabilities.Capability;\n");
    result.append("import weka.core.FastVector;\n");
    result.append("import weka.core.Instance;\n");
    result.append("import weka.core.Instances;\n");
    result.append("import weka.filters.Filter;\n");
    result.append("\n");
    result.append("public class WekaWrapper\n");
    result.append("  extends Filter {\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns only the toString() method.\n");
    result.append("   *\n");
    result.append("   * @return a string describing the filter\n");
    result.append("   */\n");
    result.append("  public String globalInfo() {\n");
    result.append("    return toString();\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns the capabilities of this filter.\n");
    result.append("   *\n");
    result.append("   * @return the capabilities\n");
    result.append("   */\n");
    result.append("  public Capabilities getCapabilities() {\n");
    result.append(((Filter)filter).getCapabilities().toSource("result", 4));
    result.append("    return result;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * turns array of Objects into an Instance object\n");
    result.append("   *\n");
    result.append("   * @param obj\tthe Object array to turn into an Instance\n");
    result.append("   * @param format\tthe data format to use\n");
    result.append("   * @return\t\tthe generated Instance object\n");
    result.append("   */\n");
    result.append("  protected Instance objectsToInstance(Object[] obj, Instances format) {\n");
    result.append("    Instance\t\tresult;\n");
    result.append("    double[]\t\tvalues;\n");
    result.append("    int\t\ti;\n");
    result.append("\n");
    result.append("    values = new double[obj.length];\n");
    result.append("\n");
    result.append("    for (i = 0 ; i < obj.length; i++) {\n");
    result.append("      if (obj[i] == null)\n");
    result.append("        values[i] = Instance.missingValue();\n");
    result.append("      else if (format.attribute(i).isNumeric())\n");
    result.append("        values[i] = (Double) obj[i];\n");
    result.append("      else if (format.attribute(i).isNominal())\n");
    result.append("        values[i] = format.attribute(i).indexOfValue((String) obj[i]);\n");
    result.append("    }\n");
    result.append("\n");
    result.append("    // create new instance\n");
    result.append("    result = new Instance(1.0, values);\n");
    result.append("    result.setDataset(format);\n");
    result.append("\n");
    result.append("    return result;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * turns the Instance object into an array of Objects\n");
    result.append("   *\n");
    result.append("   * @param inst\tthe instance to turn into an array\n");
    result.append("   * @return\t\tthe Object array representing the instance\n");
    result.append("   */\n");
    result.append("  protected Object[] instanceToObjects(Instance inst) {\n");
    result.append("    Object[]\tresult;\n");
    result.append("    int\t\ti;\n");
    result.append("\n");
    result.append("    result = new Object[inst.numAttributes()];\n");
    result.append("\n");
    result.append("    for (i = 0 ; i < inst.numAttributes(); i++) {\n");
    result.append("      if (inst.isMissing(i))\n");
    result.append("  \tresult[i] = null;\n");
    result.append("      else if (inst.attribute(i).isNumeric())\n");
    result.append("  \tresult[i] = inst.value(i);\n");
    result.append("      else\n");
    result.append("  \tresult[i] = inst.stringValue(i);\n");
    result.append("    }\n");
    result.append("\n");
    result.append("    return result;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * turns the Instances object into an array of Objects\n");
    result.append("   *\n");
    result.append("   * @param data\tthe instances to turn into an array\n");
    result.append("   * @return\t\tthe Object array representing the instances\n");
    result.append("   */\n");
    result.append("  protected Object[][] instancesToObjects(Instances data) {\n");
    result.append("    Object[][]\tresult;\n");
    result.append("    int\t\ti;\n");
    result.append("\n");
    result.append("    result = new Object[data.numInstances()][];\n");
    result.append("\n");
    result.append("    for (i = 0; i < data.numInstances(); i++)\n");
    result.append("      result[i] = instanceToObjects(data.instance(i));\n");
    result.append("\n");
    result.append("    return result;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Only tests the input data.\n");
    result.append("   *\n");
    result.append("   * @param instanceInfo the format of the data to convert\n");
    result.append("   * @return always true, to indicate that the output format can \n");
    result.append("   *         be collected immediately.\n");
    result.append("   */\n");
    result.append("  public boolean setInputFormat(Instances instanceInfo) throws Exception {\n");
    result.append("    super.setInputFormat(instanceInfo);\n");
    result.append("    \n");
    result.append("    // generate output format\n");
    result.append("    FastVector atts = new FastVector();\n");
    result.append("    FastVector attValues;\n");
    for (int i = 0; i < output.numAttributes(); i++) {
      result.append("    // " + output.attribute(i).name() + "\n");
      if (output.attribute(i).isNumeric()) {
        result.append("    atts.addElement(new Attribute(\"" + output.attribute(i).name() + "\"));\n");

      }
      else if (output.attribute(i).isNominal()) {
        result.append("    attValues = new FastVector();\n");
        for (int n = 0; n < output.attribute(i).numValues(); n++) {
          result.append("    attValues.addElement(\"" + output.attribute(i).value(n) + "\");\n");
        }
        result.append("    atts.addElement(new Attribute(\"" + output.attribute(i).name() + "\", attValues));\n");
      }
      else
      {
        throw new UnsupportedAttributeTypeException("Attribute type '" + output.attribute(i).type() + "' (position " + (i + 1) + ") is not supported!");
      }
    }
    

    result.append("    \n");
    result.append("    Instances format = new Instances(\"" + output.relationName() + "\", atts, 0);\n");
    result.append("    format.setClassIndex(" + output.classIndex() + ");\n");
    result.append("    setOutputFormat(format);\n");
    result.append("    \n");
    result.append("    return true;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Directly filters the instance.\n");
    result.append("   *\n");
    result.append("   * @param instance the instance to convert\n");
    result.append("   * @return always true, to indicate that the output can \n");
    result.append("   *         be collected immediately.\n");
    result.append("   */\n");
    result.append("  public boolean input(Instance instance) throws Exception {\n");
    result.append("    Object[] filtered = " + className + ".filter(instanceToObjects(instance));\n");
    result.append("    push(objectsToInstance(filtered, getOutputFormat()));\n");
    result.append("    return true;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Performs a batch filtering of the buffered data, if any available.\n");
    result.append("   *\n");
    result.append("   * @return true if instances were filtered otherwise false\n");
    result.append("   */\n");
    result.append("  public boolean batchFinished() throws Exception {\n");
    result.append("    if (getInputFormat() == null)\n");
    result.append("      throw new NullPointerException(\"No input instance format defined\");;\n");
    result.append("\n");
    result.append("    Instances inst = getInputFormat();\n");
    result.append("    if (inst.numInstances() > 0) {\n");
    result.append("      Object[][] filtered = " + className + ".filter(instancesToObjects(inst));\n");
    result.append("      for (int i = 0; i < filtered.length; i++) {\n");
    result.append("        push(objectsToInstance(filtered[i], getOutputFormat()));\n");
    result.append("      }\n");
    result.append("    }\n");
    result.append("\n");
    result.append("    flushInput();\n");
    result.append("    m_NewBatch = true;\n");
    result.append("    m_FirstBatchDone = true;\n");
    result.append("\n");
    result.append("    return (inst.numInstances() > 0);\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns only the classnames and what filter it is based on.\n");
    result.append("   *\n");
    result.append("   * @return a short description\n");
    result.append("   */\n");
    result.append("  public String toString() {\n");
    result.append("    return \"Auto-generated filter wrapper, based on " + filter.getClass().getName() + " (generated with Weka " + Version.VERSION + ").\\n" + "\" + this.getClass().getName() + \"/" + className + "\";\n");
    

    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Runs the filter from commandline.\n");
    result.append("   *\n");
    result.append("   * @param args the commandline arguments\n");
    result.append("   */\n");
    result.append("  public static void main(String args[]) {\n");
    result.append("    runFilter(new WekaWrapper(), args);\n");
    result.append("  }\n");
    result.append("}\n");
    

    result.append("\n");
    result.append(filter.toSource(className, input));
    
    return result.toString();
  }
  













  public static void filterFile(Filter filter, String[] options)
    throws Exception
  {
    boolean debug = false;
    Instances data = null;
    ConverterUtils.DataSource input = null;
    PrintWriter output = null;
    
    String sourceCode = "";
    try
    {
      boolean helpRequest = Utils.getFlag('h', options);
      
      if (Utils.getFlag('d', options)) {
        debug = true;
      }
      String infileName = Utils.getOption('i', options);
      String outfileName = Utils.getOption('o', options);
      String classIndex = Utils.getOption('c', options);
      if ((filter instanceof Sourcable)) {
        sourceCode = Utils.getOption('z', options);
      }
      if ((filter instanceof OptionHandler)) {
        ((OptionHandler)filter).setOptions(options);
      }
      
      Utils.checkForRemainingOptions(options);
      if (helpRequest) {
        throw new Exception("Help requested.\n");
      }
      if (infileName.length() != 0) {
        input = new ConverterUtils.DataSource(infileName);
      } else {
        input = new ConverterUtils.DataSource(System.in);
      }
      if (outfileName.length() != 0) {
        output = new PrintWriter(new FileOutputStream(outfileName));
      } else {
        output = new PrintWriter(System.out);
      }
      
      data = input.getStructure();
      if (classIndex.length() != 0) {
        if (classIndex.equals("first")) {
          data.setClassIndex(0);
        } else if (classIndex.equals("last")) {
          data.setClassIndex(data.numAttributes() - 1);
        } else {
          data.setClassIndex(Integer.parseInt(classIndex) - 1);
        }
      }
    } catch (Exception ex) {
      String filterOptions = "";
      
      if ((filter instanceof OptionHandler)) {
        filterOptions = filterOptions + "\nFilter options:\n\n";
        Enumeration enu = ((OptionHandler)filter).listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          filterOptions = filterOptions + option.synopsis() + '\n' + option.description() + "\n";
        }
      }
      

      String genericOptions = "\nGeneral options:\n\n-h\n\tGet help on available options.\n\t(use -b -h for help on batch mode.)\n-i <file>\n\tThe name of the file containing input instances.\n\tIf not supplied then instances will be read from stdin.\n-o <file>\n\tThe name of the file output instances will be written to.\n\tIf not supplied then instances will be written to stdout.\n-c <class index>\n\tThe number of the attribute to use as the class.\n\t\"first\" and \"last\" are also valid entries.\n\tIf not supplied then no class is assigned.\n";
      













      if ((filter instanceof Sourcable)) {
        genericOptions = genericOptions + "-z <class name>\n\tOutputs the source code representing the trained filter.\n";
      }
      


      throw new Exception('\n' + ex.getMessage() + filterOptions + genericOptions);
    }
    

    if (debug) {
      System.err.println("Setting input format");
    }
    boolean printedHeader = false;
    if (filter.setInputFormat(data)) {
      if (debug) {
        System.err.println("Getting output format");
      }
      output.println(filter.getOutputFormat().toString());
      printedHeader = true;
    }
    


    while (input.hasMoreElements(data)) {
      Instance inst = input.nextElement(data);
      if (debug) {
        System.err.println("Input instance to filter");
      }
      if (filter.input(inst)) {
        if (debug) {
          System.err.println("Filter said collect immediately");
        }
        if (!printedHeader) {
          throw new Error("Filter didn't return true from setInputFormat() earlier!");
        }
        
        if (debug) {
          System.err.println("Getting output instance");
        }
        output.println(filter.output().toString());
      }
    }
    

    if (debug) {
      System.err.println("Setting end of batch");
    }
    if (filter.batchFinished()) {
      if (debug) {
        System.err.println("Filter said collect output");
      }
      if (!printedHeader) {
        if (debug) {
          System.err.println("Getting output format");
        }
        output.println(filter.getOutputFormat().toString());
      }
      if (debug) {
        System.err.println("Getting output instance");
      }
      while (filter.numPendingOutput() > 0) {
        output.println(filter.output().toString());
        if (debug) {
          System.err.println("Getting output instance");
        }
      }
    }
    if (debug) {
      System.err.println("Done");
    }
    
    if (output != null) {
      output.close();
    }
    
    if (sourceCode.length() != 0) {
      System.out.println(wekaStaticWrapper((Sourcable)filter, sourceCode, data, filter.getOutputFormat()));
    }
  }
  
















  public static void batchFilterFile(Filter filter, String[] options)
    throws Exception
  {
    Instances firstData = null;
    Instances secondData = null;
    ConverterUtils.DataSource firstInput = null;
    ConverterUtils.DataSource secondInput = null;
    PrintWriter firstOutput = null;
    PrintWriter secondOutput = null;
    
    String sourceCode = "";
    try
    {
      boolean helpRequest = Utils.getFlag('h', options);
      
      String fileName = Utils.getOption('i', options);
      if (fileName.length() != 0) {
        firstInput = new ConverterUtils.DataSource(fileName);
      } else {
        throw new Exception("No first input file given.\n");
      }
      
      fileName = Utils.getOption('r', options);
      if (fileName.length() != 0) {
        secondInput = new ConverterUtils.DataSource(fileName);
      } else {
        throw new Exception("No second input file given.\n");
      }
      
      fileName = Utils.getOption('o', options);
      if (fileName.length() != 0) {
        firstOutput = new PrintWriter(new FileOutputStream(fileName));
      } else {
        firstOutput = new PrintWriter(System.out);
      }
      
      fileName = Utils.getOption('s', options);
      if (fileName.length() != 0) {
        secondOutput = new PrintWriter(new FileOutputStream(fileName));
      } else {
        secondOutput = new PrintWriter(System.out);
      }
      String classIndex = Utils.getOption('c', options);
      if ((filter instanceof Sourcable)) {
        sourceCode = Utils.getOption('z', options);
      }
      if ((filter instanceof OptionHandler)) {
        ((OptionHandler)filter).setOptions(options);
      }
      Utils.checkForRemainingOptions(options);
      
      if (helpRequest) {
        throw new Exception("Help requested.\n");
      }
      firstData = firstInput.getStructure();
      secondData = secondInput.getStructure();
      if (!secondData.equalHeaders(firstData)) {
        throw new Exception("Input file formats differ.\n");
      }
      if (classIndex.length() != 0) {
        if (classIndex.equals("first")) {
          firstData.setClassIndex(0);
          secondData.setClassIndex(0);
        } else if (classIndex.equals("last")) {
          firstData.setClassIndex(firstData.numAttributes() - 1);
          secondData.setClassIndex(secondData.numAttributes() - 1);
        } else {
          firstData.setClassIndex(Integer.parseInt(classIndex) - 1);
          secondData.setClassIndex(Integer.parseInt(classIndex) - 1);
        }
      }
    } catch (Exception ex) {
      String filterOptions = "";
      
      if ((filter instanceof OptionHandler)) {
        filterOptions = filterOptions + "\nFilter options:\n\n";
        Enumeration enu = ((OptionHandler)filter).listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          filterOptions = filterOptions + option.synopsis() + '\n' + option.description() + "\n";
        }
      }
      

      String genericOptions = "\nGeneral options:\n\n-h\n\tGet help on available options.\n-i <filename>\n\tThe file containing first input instances.\n-o <filename>\n\tThe file first output instances will be written to.\n-r <filename>\n\tThe file containing second input instances.\n-s <filename>\n\tThe file second output instances will be written to.\n-c <class index>\n\tThe number of the attribute to use as the class.\n\t\"first\" and \"last\" are also valid entries.\n\tIf not supplied then no class is assigned.\n";
      














      if ((filter instanceof Sourcable)) {
        genericOptions = genericOptions + "-z <class name>\n\tOutputs the source code representing the trained filter.\n";
      }
      


      throw new Exception('\n' + ex.getMessage() + filterOptions + genericOptions);
    }
    
    boolean printedHeader = false;
    if (filter.setInputFormat(firstData)) {
      firstOutput.println(filter.getOutputFormat().toString());
      printedHeader = true;
    }
    


    while (firstInput.hasMoreElements(firstData)) {
      Instance inst = firstInput.nextElement(firstData);
      if (filter.input(inst)) {
        if (!printedHeader) {
          throw new Error("Filter didn't return true from setInputFormat() earlier!");
        }
        
        firstOutput.println(filter.output().toString());
      }
    }
    

    if (filter.batchFinished()) {
      if (!printedHeader) {
        firstOutput.println(filter.getOutputFormat().toString());
      }
      while (filter.numPendingOutput() > 0) {
        firstOutput.println(filter.output().toString());
      }
    }
    
    if (firstOutput != null) {
      firstOutput.close();
    }
    printedHeader = false;
    if (filter.isOutputFormatDefined()) {
      secondOutput.println(filter.getOutputFormat().toString());
      printedHeader = true;
    }
    
    while (secondInput.hasMoreElements(secondData)) {
      Instance inst = secondInput.nextElement(secondData);
      if (filter.input(inst)) {
        if (!printedHeader) {
          throw new Error("Filter didn't return true from isOutputFormatDefined() earlier!");
        }
        
        secondOutput.println(filter.output().toString());
      }
    }
    

    if (filter.batchFinished()) {
      if (!printedHeader) {
        secondOutput.println(filter.getOutputFormat().toString());
      }
      while (filter.numPendingOutput() > 0) {
        secondOutput.println(filter.output().toString());
      }
    }
    if (secondOutput != null) {
      secondOutput.close();
    }
    
    if (sourceCode.length() != 0) {
      System.out.println(wekaStaticWrapper((Sourcable)filter, sourceCode, firstData, filter.getOutputFormat()));
    }
  }
  





  protected static void runFilter(Filter filter, String[] options)
  {
    try
    {
      if (Utils.getFlag('b', options)) {
        batchFilterFile(filter, options);
      } else {
        filterFile(filter, options);
      }
    } catch (Exception e) {
      if ((e.toString().indexOf("Help requested") == -1) && (e.toString().indexOf("Filter options") == -1))
      {
        e.printStackTrace();
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("First argument must be the class name of a Filter");
      }
      String fname = args[0];
      Filter f = (Filter)Class.forName(fname).newInstance();
      args[0] = "";
      runFilter(f, args);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
