package weka.clusterers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase;
import weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI.OPTICS_Visualizer;
import weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI.SERObject;
import weka.clusterers.forOPTICSAndDBScan.Utils.EpsilonRange_ListElement;
import weka.clusterers.forOPTICSAndDBScan.Utils.UpdateQueue;
import weka.clusterers.forOPTICSAndDBScan.Utils.UpdateQueueElement;
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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;





















































































public class OPTICS
  extends AbstractClusterer
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 274552680222105221L;
  private double epsilon = 0.9D;
  



  private int minPoints = 6;
  




  private ReplaceMissingValues replaceMissingValues_Filter;
  



  private int numberOfGeneratedClusters;
  



  private String database_distanceType = "weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject";
  




  private String database_Type = "weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase";
  



  private Database database;
  



  private double elapsedTime;
  



  private boolean writeOPTICSresults = false;
  


  private FastVector resultVector;
  


  private boolean showGUI = true;
  

  private File databaseOutput = new File(".");
  





  public OPTICS() {}
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  




  public void buildClusterer(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    
    resultVector = new FastVector();
    long time_1 = System.currentTimeMillis();
    
    numberOfGeneratedClusters = 0;
    
    replaceMissingValues_Filter = new ReplaceMissingValues();
    replaceMissingValues_Filter.setInputFormat(instances);
    Instances filteredInstances = Filter.useFilter(instances, replaceMissingValues_Filter);
    
    database = databaseForName(getDatabase_Type(), filteredInstances);
    for (int i = 0; i < database.getInstances().numInstances(); i++) {
      DataObject dataObject = dataObjectForName(getDatabase_distanceType(), database.getInstances().instance(i), Integer.toString(i), database);
      


      database.insert(dataObject);
    }
    database.setMinMaxValues();
    
    UpdateQueue seeds = new UpdateQueue();
    

    Iterator iterator = database.dataObjectIterator();
    while (iterator.hasNext()) {
      DataObject dataObject = (DataObject)iterator.next();
      if (!dataObject.isProcessed()) {
        expandClusterOrder(dataObject, seeds);
      }
    }
    
    long time_2 = System.currentTimeMillis();
    elapsedTime = ((time_2 - time_1) / 1000.0D);
    
    if (writeOPTICSresults) {
      String fileName = "";
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      String timeStamp = gregorianCalendar.get(5) + "-" + (gregorianCalendar.get(2) + 1) + "-" + gregorianCalendar.get(1) + "--" + gregorianCalendar.get(11) + "-" + gregorianCalendar.get(12) + "-" + gregorianCalendar.get(13);
      




      fileName = "OPTICS_" + timeStamp + ".TXT";
      
      FileWriter fileWriter = new FileWriter(fileName);
      BufferedWriter bufferedOPTICSWriter = new BufferedWriter(fileWriter);
      for (int i = 0; i < resultVector.size(); i++) {
        bufferedOPTICSWriter.write(format_dataObject((DataObject)resultVector.elementAt(i)));
      }
      bufferedOPTICSWriter.flush();
      bufferedOPTICSWriter.close();
    }
    

    if (!databaseOutput.isDirectory()) {
      try {
        FileOutputStream fos = new FileOutputStream(databaseOutput);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(getSERObject());
        oos.flush();
        oos.close();
        fos.close();
      }
      catch (Exception e) {
        System.err.println("Error writing generated database to file '" + getDatabaseOutput() + "': " + e);
        

        e.printStackTrace();
      }
    }
    
    if (showGUI) {
      new OPTICS_Visualizer(getSERObject(), "OPTICS Visualizer - Main Window");
    }
  }
  



  private void expandClusterOrder(DataObject dataObject, UpdateQueue seeds)
  {
    List list = database.coreDistance(getMinPoints(), getEpsilon(), dataObject);
    List epsilonRange_List = (List)list.get(1);
    dataObject.setReachabilityDistance(2.147483647E9D);
    dataObject.setCoreDistance(((Double)list.get(2)).doubleValue());
    dataObject.setProcessed(true);
    
    resultVector.addElement(dataObject);
    
    if (dataObject.getCoreDistance() != 2.147483647E9D) {
      update(seeds, epsilonRange_List, dataObject);
      while (seeds.hasNext()) {
        UpdateQueueElement updateQueueElement = seeds.next();
        DataObject currentDataObject = (DataObject)updateQueueElement.getObject();
        currentDataObject.setReachabilityDistance(updateQueueElement.getPriority());
        List list_1 = database.coreDistance(getMinPoints(), getEpsilon(), currentDataObject);
        List epsilonRange_List_1 = (List)list_1.get(1);
        currentDataObject.setCoreDistance(((Double)list_1.get(2)).doubleValue());
        currentDataObject.setProcessed(true);
        
        resultVector.addElement(currentDataObject);
        
        if (currentDataObject.getCoreDistance() != 2.147483647E9D) {
          update(seeds, epsilonRange_List_1, currentDataObject);
        }
      }
    }
  }
  





  private String format_dataObject(DataObject dataObject)
  {
    StringBuffer stringBuffer = new StringBuffer();
    
    stringBuffer.append("(" + Utils.doubleToString(Double.parseDouble(dataObject.getKey()), Integer.toString(database.size()).length(), 0) + ".) " + Utils.padRight(dataObject.toString(), 40) + "  -->  c_dist: " + (dataObject.getCoreDistance() == 2.147483647E9D ? Utils.padRight("UNDEFINED", 12) : Utils.padRight(Utils.doubleToString(dataObject.getCoreDistance(), 2, 3), 12)) + " r_dist: " + (dataObject.getReachabilityDistance() == 2.147483647E9D ? Utils.padRight("UNDEFINED", 12) : Utils.doubleToString(dataObject.getReachabilityDistance(), 2, 3)) + "\n");
    











    return stringBuffer.toString();
  }
  





  private void update(UpdateQueue seeds, List epsilonRange_list, DataObject centralObject)
  {
    double coreDistance = centralObject.getCoreDistance();
    double new_r_dist = 2.147483647E9D;
    
    for (int i = 0; i < epsilonRange_list.size(); i++) {
      EpsilonRange_ListElement listElement = (EpsilonRange_ListElement)epsilonRange_list.get(i);
      DataObject neighbourhood_object = listElement.getDataObject();
      if (!neighbourhood_object.isProcessed()) {
        new_r_dist = Math.max(coreDistance, listElement.getDistance());
        seeds.add(new_r_dist, neighbourhood_object, neighbourhood_object.getKey());
      }
    }
  }
  






  public int clusterInstance(Instance instance)
    throws Exception
  {
    throw new Exception();
  }
  





  public int numberOfClusters()
    throws Exception
  {
    return numberOfGeneratedClusters;
  }
  




  public Enumeration listOptions()
  {
    Vector vector = new Vector();
    
    vector.addElement(new Option("\tepsilon (default = 0.9)", "E", 1, "-E <double>"));
    



    vector.addElement(new Option("\tminPoints (default = 6)", "M", 1, "-M <int>"));
    


    vector.addElement(new Option("\tindex (database) used for OPTICS (default = weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase)", "I", 1, "-I <String>"));
    



    vector.addElement(new Option("\tdistance-type (default = weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject)", "D", 1, "-D <String>"));
    



    vector.addElement(new Option("\twrite results to OPTICS_#TimeStamp#.TXT - File", "F", 0, "-F"));
    



    vector.addElement(new Option("\tsuppress the display of the GUI after building the clusterer", "no-gui", 0, "-no-gui"));
    



    vector.addElement(new Option("\tThe file to save the generated database to. If a directory\n\tis provided, the database doesn't get saved.\n\tThe generated file can be viewed with the OPTICS Visualizer:\n\t  java " + OPTICS_Visualizer.class.getName() + " [file.ser]\n" + "\t(default: .)", "db-output", 1, "-db-output <file>"));
    







    return vector.elements();
  }
  




































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('E', options);
    if (optionString.length() != 0) {
      setEpsilon(Double.parseDouble(optionString));
    } else {
      setEpsilon(0.9D);
    }
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinPoints(Integer.parseInt(optionString));
    } else {
      setMinPoints(6);
    }
    optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setDatabase_Type(optionString);
    } else {
      setDatabase_Type(SequentialDatabase.class.getName());
    }
    optionString = Utils.getOption('D', options);
    if (optionString.length() != 0) {
      setDatabase_distanceType(optionString);
    } else {
      setDatabase_distanceType(EuclideanDataObject.class.getName());
    }
    setWriteOPTICSresults(Utils.getFlag('F', options));
    
    setShowGUI(!Utils.getFlag("no-gui", options));
    
    optionString = Utils.getOption("db-output", options);
    if (optionString.length() != 0) {
      setDatabaseOutput(new File(optionString));
    } else {
      setDatabaseOutput(new File("."));
    }
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-E");
    result.add("" + getEpsilon());
    
    result.add("-M");
    result.add("" + getMinPoints());
    
    result.add("-I");
    result.add("" + getDatabase_Type());
    
    result.add("-D");
    result.add("" + getDatabase_distanceType());
    
    if (getWriteOPTICSresults()) {
      result.add("-F");
    }
    if (!getShowGUI()) {
      result.add("-no-gui");
    }
    result.add("-db-output");
    result.add("" + getDatabaseOutput());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public Database databaseForName(String database_Type, Instances instances)
  {
    Object o = null;
    
    Constructor co = null;
    try {
      co = Class.forName(database_Type).getConstructor(new Class[] { Instances.class });
      o = co.newInstance(new Object[] { instances });
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    return (Database)o;
  }
  







  public DataObject dataObjectForName(String database_distanceType, Instance instance, String key, Database database)
  {
    Object o = null;
    
    Constructor co = null;
    try {
      co = Class.forName(database_distanceType).getConstructor(new Class[] { Instance.class, String.class, Database.class });
      
      o = co.newInstance(new Object[] { instance, key, database });
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    return (DataObject)o;
  }
  



  public void setMinPoints(int minPoints)
  {
    this.minPoints = minPoints;
  }
  



  public void setEpsilon(double epsilon)
  {
    this.epsilon = epsilon;
  }
  



  public double getEpsilon()
  {
    return epsilon;
  }
  



  public int getMinPoints()
  {
    return minPoints;
  }
  



  public String getDatabase_distanceType()
  {
    return database_distanceType;
  }
  



  public String getDatabase_Type()
  {
    return database_Type;
  }
  



  public void setDatabase_distanceType(String database_distanceType)
  {
    this.database_distanceType = database_distanceType;
  }
  



  public void setDatabase_Type(String database_Type)
  {
    this.database_Type = database_Type;
  }
  



  public boolean getWriteOPTICSresults()
  {
    return writeOPTICSresults;
  }
  



  public void setWriteOPTICSresults(boolean writeOPTICSresults)
  {
    this.writeOPTICSresults = writeOPTICSresults;
  }
  




  public boolean getShowGUI()
  {
    return showGUI;
  }
  





  public void setShowGUI(boolean value)
  {
    showGUI = value;
  }
  






  public File getDatabaseOutput()
  {
    return databaseOutput;
  }
  






  public void setDatabaseOutput(File value)
  {
    databaseOutput = value;
  }
  



  public FastVector getResultVector()
  {
    return resultVector;
  }
  




  public String epsilonTipText()
  {
    return "radius of the epsilon-range-queries";
  }
  




  public String minPointsTipText()
  {
    return "minimun number of DataObjects required in an epsilon-range-query";
  }
  




  public String database_TypeTipText()
  {
    return "used database";
  }
  




  public String database_distanceTypeTipText()
  {
    return "used distance-type";
  }
  




  public String writeOPTICSresultsTipText()
  {
    return "if the -F option is set, the results are written to OPTICS_#TimeStamp#.TXT";
  }
  





  public String showGUITipText()
  {
    return "Defines whether the OPTICS Visualizer is displayed after the clusterer has been built or not.";
  }
  





  public String databaseOutputTipText()
  {
    return "The optional output file for the generated database object - can be viewed with the OPTICS Visualizer.\njava " + OPTICS_Visualizer.class.getName() + " [file.ser]";
  }
  






  public String globalInfo()
  {
    return "Basic implementation of OPTICS clustering algorithm that should *not* be used as a reference for runtime benchmarks: more sophisticated implementations exist! Clustering of new instances is not supported. More info:\n\n " + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Mihael Ankerst and Markus M. Breunig and Hans-Peter Kriegel and Joerg Sander");
    result.setValue(TechnicalInformation.Field.TITLE, "OPTICS: Ordering Points To Identify the Clustering Structure");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "ACM SIGMOD International Conference on Management of Data");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.PAGES, "49-60");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    
    return result;
  }
  




  public SERObject getSERObject()
  {
    SERObject serObject = new SERObject(resultVector, database.size(), database.getInstances().numAttributes(), getEpsilon(), getMinPoints(), writeOPTICSresults, getDatabase_Type(), getDatabase_distanceType(), numberOfGeneratedClusters, Utils.doubleToString(elapsedTime, 3, 3));
    








    return serObject;
  }
  




  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("OPTICS clustering results\n============================================================================================\n\n");
    
    stringBuffer.append("Clustered DataObjects: " + database.size() + "\n");
    stringBuffer.append("Number of attributes: " + database.getInstances().numAttributes() + "\n");
    stringBuffer.append("Epsilon: " + getEpsilon() + "; minPoints: " + getMinPoints() + "\n");
    stringBuffer.append("Write results to file: " + (writeOPTICSresults ? "yes" : "no") + "\n");
    stringBuffer.append("Index: " + getDatabase_Type() + "\n");
    stringBuffer.append("Distance-type: " + getDatabase_distanceType() + "\n");
    stringBuffer.append("Number of generated clusters: " + numberOfGeneratedClusters + "\n");
    DecimalFormat decimalFormat = new DecimalFormat(".##");
    stringBuffer.append("Elapsed time: " + decimalFormat.format(elapsedTime) + "\n\n");
    
    for (int i = 0; i < resultVector.size(); i++) {
      stringBuffer.append(format_dataObject((DataObject)resultVector.elementAt(i)));
    }
    return stringBuffer.toString() + "\n";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9434 $");
  }
  






  public static void main(String[] args)
  {
    runClusterer(new OPTICS(), args);
  }
}
