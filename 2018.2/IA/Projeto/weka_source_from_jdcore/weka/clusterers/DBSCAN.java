package weka.clusterers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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











































































public class DBSCAN
  extends AbstractClusterer
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -1666498248451219728L;
  private double epsilon = 0.9D;
  



  private int minPoints = 6;
  




  private ReplaceMissingValues replaceMissingValues_Filter;
  



  private int numberOfGeneratedClusters;
  



  private String database_distanceType = "weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject";
  




  private String database_Type = "weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase";
  


  private Database database;
  


  private int clusterID;
  


  private int processed_InstanceID;
  


  private double elapsedTime;
  



  public DBSCAN() {}
  



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
    
    long time_1 = System.currentTimeMillis();
    
    processed_InstanceID = 0;
    numberOfGeneratedClusters = 0;
    clusterID = 0;
    
    replaceMissingValues_Filter = new ReplaceMissingValues();
    replaceMissingValues_Filter.setInputFormat(instances);
    Instances filteredInstances = Filter.useFilter(instances, replaceMissingValues_Filter);
    
    database = databaseForName(getDatabase_Type(), filteredInstances);
    for (int i = 0; i < database.getInstances().numInstances(); i++) {
      DataObject dataObject = dataObjectForName(getDatabase_distanceType(), database.getInstances().instance(i), Integer.toString(i), database);
      


      database.insert(dataObject);
    }
    database.setMinMaxValues();
    
    Iterator iterator = database.dataObjectIterator();
    while (iterator.hasNext()) {
      DataObject dataObject = (DataObject)iterator.next();
      if ((dataObject.getClusterLabel() == -1) && 
        (expandCluster(dataObject))) {
        clusterID += 1;
        numberOfGeneratedClusters += 1;
      }
    }
    

    long time_2 = System.currentTimeMillis();
    elapsedTime = ((time_2 - time_1) / 1000.0D);
  }
  




  private boolean expandCluster(DataObject dataObject)
  {
    List seedList = database.epsilonRangeQuery(getEpsilon(), dataObject);
    
    if (seedList.size() < getMinPoints()) {
      dataObject.setClusterLabel(Integer.MIN_VALUE);
      return false;
    }
    

    for (int i = 0; i < seedList.size(); i++) {
      DataObject seedListDataObject = (DataObject)seedList.get(i);
      
      seedListDataObject.setClusterLabel(clusterID);
      if (seedListDataObject.equals(dataObject)) {
        seedList.remove(i);
        i--;
      }
    }
    

    for (int j = 0; j < seedList.size(); j++) {
      DataObject seedListDataObject = (DataObject)seedList.get(j);
      List seedListDataObject_Neighbourhood = database.epsilonRangeQuery(getEpsilon(), seedListDataObject);
      

      if (seedListDataObject_Neighbourhood.size() >= getMinPoints()) {
        for (int i = 0; i < seedListDataObject_Neighbourhood.size(); i++) {
          DataObject p = (DataObject)seedListDataObject_Neighbourhood.get(i);
          if ((p.getClusterLabel() == -1) || (p.getClusterLabel() == Integer.MIN_VALUE)) {
            if (p.getClusterLabel() == -1) {
              seedList.add(p);
            }
            p.setClusterLabel(clusterID);
          }
        }
      }
      seedList.remove(j);
      j--;
    }
    
    return true;
  }
  






  public int clusterInstance(Instance instance)
    throws Exception
  {
    if (processed_InstanceID >= database.size()) processed_InstanceID = 0;
    int cnum = database.getDataObject(Integer.toString(processed_InstanceID++)).getClusterLabel();
    if (cnum == Integer.MIN_VALUE) {
      throw new Exception();
    }
    return cnum;
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
    



    vector.addElement(new Option("\tindex (database) used for DBSCAN (default = weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase)", "I", 1, "-I <String>"));
    



    vector.addElement(new Option("\tdistance-type (default = weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject)", "D", 1, "-D <String>"));
    



    return vector.elements();
  }
  























  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('E', options);
    if (optionString.length() != 0) {
      setEpsilon(Double.parseDouble(optionString));
    }
    
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinPoints(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setDatabase_Type(optionString);
    }
    
    optionString = Utils.getOption('D', options);
    if (optionString.length() != 0) {
      setDatabase_distanceType(optionString);
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[8];
    int current = 0;
    
    options[(current++)] = "-E";
    options[(current++)] = ("" + getEpsilon());
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMinPoints());
    options[(current++)] = "-I";
    options[(current++)] = ("" + getDatabase_Type());
    options[(current++)] = "-D";
    options[(current++)] = ("" + getDatabase_distanceType());
    
    return options;
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
  



  public String globalInfo()
  {
    return "Basic implementation of DBSCAN clustering algorithm that should *not* be used as a reference for runtime benchmarks: more sophisticated implementations exist! Clustering of new instances is not supported. More info:\n\n " + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Martin Ester and Hans-Peter Kriegel and Joerg Sander and Xiaowei Xu");
    result.setValue(TechnicalInformation.Field.TITLE, "A Density-Based Algorithm for Discovering Clusters in Large Spatial Databases with Noise");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Second International Conference on Knowledge Discovery and Data Mining");
    result.setValue(TechnicalInformation.Field.EDITOR, "Evangelos Simoudis and Jiawei Han and Usama M. Fayyad");
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "226-231");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "AAAI Press");
    
    return result;
  }
  




  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("DBSCAN clustering results\n========================================================================================\n\n");
    
    stringBuffer.append("Clustered DataObjects: " + database.size() + "\n");
    stringBuffer.append("Number of attributes: " + database.getInstances().numAttributes() + "\n");
    stringBuffer.append("Epsilon: " + getEpsilon() + "; minPoints: " + getMinPoints() + "\n");
    stringBuffer.append("Index: " + getDatabase_Type() + "\n");
    stringBuffer.append("Distance-type: " + getDatabase_distanceType() + "\n");
    stringBuffer.append("Number of generated clusters: " + numberOfGeneratedClusters + "\n");
    DecimalFormat decimalFormat = new DecimalFormat(".##");
    stringBuffer.append("Elapsed time: " + decimalFormat.format(elapsedTime) + "\n\n");
    
    for (int i = 0; i < database.size(); i++) {
      DataObject dataObject = database.getDataObject(Integer.toString(i));
      stringBuffer.append("(" + Utils.doubleToString(Double.parseDouble(dataObject.getKey()), Integer.toString(database.size()).length(), 0) + ".) " + Utils.padRight(dataObject.toString(), 69) + "  -->  " + (dataObject.getClusterLabel() == Integer.MIN_VALUE ? "NOISE\n" : new StringBuilder().append(dataObject.getClusterLabel()).append("\n").toString()));
    }
    



    return stringBuffer.toString() + "\n";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9434 $");
  }
  





  public static void main(String[] args)
  {
    runClusterer(new DBSCAN(), args);
  }
}
