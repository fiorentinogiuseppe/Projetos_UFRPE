package weka.gui.experiment;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import weka.core.Utils;
import weka.experiment.PairedCorrectedTTester;
import weka.experiment.ResultMatrixPlainText;



































public class ExperimenterDefaults
  implements Serializable
{
  static final long serialVersionUID = -2835933184632147981L;
  public static final String PROPERTY_FILE = "weka/gui/experiment/Experimenter.props";
  protected static Properties PROPERTIES;
  
  static
  {
    try
    {
      PROPERTIES = Utils.readProperties("weka/gui/experiment/Experimenter.props");
    }
    catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("ExperimenterDefaults_STATIC_Error_Text"));
      e.printStackTrace();
      PROPERTIES = new Properties();
    }
  }
  







  public static String get(String property, String defaultValue)
  {
    return PROPERTIES.getProperty(property, defaultValue);
  }
  




  public static final Properties getProperties()
  {
    return PROPERTIES;
  }
  




  public static final String getExtension()
  {
    return get("Extension", ".exp");
  }
  




  public static final String getDestination()
  {
    Messages.getInstance();return get("Destination", Messages.getString("ExperimenterDefaults_GetDestination_Text"));
  }
  




  public static final String getExperimentType()
  {
    Messages.getInstance();return get("ExperimentType", Messages.getString("ExperimenterDefaults_GetExperimentType_Text"));
  }
  




  public static final boolean getUseClassification()
  {
    return Boolean.valueOf(get("UseClassification", "true")).booleanValue();
  }
  




  public static final int getFolds()
  {
    return Integer.parseInt(get("Folds", "10"));
  }
  




  public static final double getTrainPercentage()
  {
    return Integer.parseInt(get("TrainPercentage", "66"));
  }
  




  public static final int getRepetitions()
  {
    return Integer.parseInt(get("Repetitions", "10"));
  }
  




  public static final boolean getDatasetsFirst()
  {
    return Boolean.valueOf(get("DatasetsFirst", "true")).booleanValue();
  }
  







  public static final File getInitialDatasetsDirectory()
  {
    String dir = get("InitialDatasetsDirectory", "");
    if (dir.equals("")) {
      dir = System.getProperty("user.dir");
    }
    return new File(dir);
  }
  




  public static final boolean getUseRelativePaths()
  {
    return Boolean.valueOf(get("UseRelativePaths", "false")).booleanValue();
  }
  






  public static final String getTester()
  {
    return get("Tester", new PairedCorrectedTTester().getDisplayName());
  }
  




  public static final String getRow()
  {
    return get("Row", "Key_Dataset");
  }
  




  public static final String getColumn()
  {
    return get("Column", "Key_Scheme,Key_Scheme_options,Key_Scheme_version_ID");
  }
  




  public static final String getComparisonField()
  {
    return get("ComparisonField", "percent_correct");
  }
  




  public static final double getSignificance()
  {
    return Double.parseDouble(get("Significance", "0.05"));
  }
  




  public static final String getSorting()
  {
    return get("Sorting", "");
  }
  




  public static final boolean getShowStdDevs()
  {
    return Boolean.valueOf(get("ShowStdDev", "false")).booleanValue();
  }
  




  public static final boolean getShowAverage()
  {
    return Boolean.valueOf(get("ShowAverage", "false")).booleanValue();
  }
  




  public static final int getMeanPrecision()
  {
    return Integer.parseInt(get("MeanPrecision", "2"));
  }
  




  public static final int getStdDevPrecision()
  {
    return Integer.parseInt(get("StdDevPrecision", "2"));
  }
  







  public static final String getOutputFormat()
  {
    return get("OutputFormat", ResultMatrixPlainText.class.getName());
  }
  




  public static final boolean getRemoveFilterClassnames()
  {
    return Boolean.valueOf(get("RemoveFilterClassnames", "false")).booleanValue();
  }
  








  public static void main(String[] args)
  {
    Messages.getInstance();System.out.println(Messages.getString("ExperimenterDefaults_Main_Text"));
    Enumeration names = PROPERTIES.propertyNames();
    

    Vector sorted = new Vector();
    while (names.hasMoreElements())
      sorted.add(names.nextElement());
    Collections.sort(sorted);
    names = sorted.elements();
    

    while (names.hasMoreElements()) {
      String name = names.nextElement().toString();
      System.out.println("- " + name + ": " + PROPERTIES.getProperty(name, ""));
    }
    System.out.println();
  }
  
  public ExperimenterDefaults() {}
}
