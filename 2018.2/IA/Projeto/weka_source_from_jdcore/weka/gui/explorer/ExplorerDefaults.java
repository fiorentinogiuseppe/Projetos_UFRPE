package weka.gui.explorer;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import weka.associations.Apriori;
import weka.associations.Associator;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Utils;
import weka.filters.Filter;





















public class ExplorerDefaults
  implements Serializable
{
  private static final long serialVersionUID = 4954795757927524225L;
  public static final String PROPERTY_FILE = "weka/gui/explorer/Explorer.props";
  protected static Properties PROPERTIES;
  
  static
  {
    try
    {
      PROPERTIES = Utils.readProperties("weka/gui/explorer/Explorer.props");
    }
    catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("ExplorerDefaults_Error_Text"));
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
  








  protected static Object getObject(String property, String defaultValue)
  {
    return getObject(property, defaultValue, Object.class);
  }
  













  protected static Object getObject(String property, String defaultValue, Class cls)
  {
    Object result = null;
    try
    {
      String tmpStr = get(property, defaultValue);
      String[] tmpOptions = Utils.splitOptions(tmpStr);
      if (tmpOptions.length != 0) {
        tmpStr = tmpOptions[0];
        tmpOptions[0] = "";
        result = Utils.forName(cls, tmpStr, tmpOptions);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  





  public static boolean getInitGenericObjectEditorFilter()
  {
    return Boolean.parseBoolean(get("InitGenericObjectEditorFilter", "false"));
  }
  









  public static String[] getTabs()
  {
    String tabs = get("Tabs", "weka.gui.explorer.ClassifierPanel,weka.gui.explorer.ClustererPanel,weka.gui.explorer.AssociationsPanel,weka.gui.explorer.AttributeSelectionPanel,weka.gui.explorer.VisualizePanel");
    String[] result = tabs.split(",");
    
    return result;
  }
  















  public static String getInitialDirectory()
  {
    String result = get("InitialDirectory", "%c");
    result = result.replaceAll("%t", System.getProperty("java.io.tmpdir"));
    result = result.replaceAll("%h", System.getProperty("user.home"));
    result = result.replaceAll("%c", System.getProperty("user.dir"));
    result = result.replaceAll("%%", System.getProperty("%"));
    
    return result;
  }
  




  public static Object getFilter()
  {
    return getObject("Filter", "", Filter.class);
  }
  






  public static Object getClassifier()
  {
    Object result = getObject("Classifier", ZeroR.class.getName(), Classifier.class);
    


    if (result == null) {
      result = new ZeroR();
    }
    return result;
  }
  




  public static int getClassifierTestMode()
  {
    return Integer.parseInt(get("ClassifierTestMode", "1"));
  }
  




  public static int getClassifierCrossvalidationFolds()
  {
    return Integer.parseInt(get("ClassifierCrossvalidationFolds", "10"));
  }
  




  public static int getClassifierPercentageSplit()
  {
    return Integer.parseInt(get("ClassifierPercentageSplit", "66"));
  }
  




  public static boolean getClassifierOutputModel()
  {
    return Boolean.parseBoolean(get("ClassifierOutputModel", "true"));
  }
  




  public static boolean getClassifierOutputPerClassStats()
  {
    return Boolean.parseBoolean(get("ClassifierOutputPerClassStats", "true"));
  }
  





  public static boolean getClassifierOutputEntropyEvalMeasures()
  {
    return Boolean.parseBoolean(get("ClassifierOutputEntropyEvalMeasures", "false"));
  }
  




  public static boolean getClassifierOutputConfusionMatrix()
  {
    return Boolean.parseBoolean(get("ClassifierOutputConfusionMatrix", "true"));
  }
  




  public static boolean getClassifierOutputPredictions()
  {
    return Boolean.parseBoolean(get("ClassifierOutputPredictions", "false"));
  }
  





  public static String getClassifierOutputAdditionalAttributes()
  {
    return get("ClassifierOutputAdditionalAttributes", "");
  }
  





  public static boolean getClassifierStorePredictionsForVis()
  {
    return Boolean.parseBoolean(get("ClassifierStorePredictionsForVis", "true"));
  }
  




  public static boolean getClassifierCostSensitiveEval()
  {
    return Boolean.parseBoolean(get("ClassifierCostSensitiveEval", "false"));
  }
  





  public static int getClassifierRandomSeed()
  {
    return Integer.parseInt(get("ClassifierRandomSeed", "1"));
  }
  





  public static boolean getClassifierPreserveOrder()
  {
    return Boolean.parseBoolean(get("ClassifierPreserveOrder", "false"));
  }
  





  public static boolean getClassifierOutputSourceCode()
  {
    return Boolean.parseBoolean(get("ClassifierOutputSourceCode", "false"));
  }
  




  public static String getClassifierSourceCodeClass()
  {
    return get("ClassifierSourceCodeClass", "Foobar");
  }
  






  public static Object getClusterer()
  {
    Object result = getObject("Clusterer", EM.class.getName(), Clusterer.class);
    


    if (result == null) {
      result = new EM();
    }
    return result;
  }
  




  public static int getClustererTestMode()
  {
    return Integer.parseInt(get("ClustererTestMode", "3"));
  }
  





  public static boolean getClustererStoreClustersForVis()
  {
    return Boolean.parseBoolean(get("ClustererStoreClustersForVis", "true"));
  }
  






  public static Object getAssociator()
  {
    Object result = getObject("Associator", Apriori.class.getName(), Associator.class);
    


    if (result == null) {
      result = new Apriori();
    }
    return result;
  }
  







  public static Object getASEvaluator()
  {
    Object result = getObject("ASEvaluation", CfsSubsetEval.class.getName(), ASEvaluation.class);
    


    if (result == null) {
      result = new CfsSubsetEval();
    }
    return result;
  }
  







  public static Object getASSearch()
  {
    Object result = getObject("ASSearch", BestFirst.class.getName(), ASSearch.class);
    


    if (result == null) {
      result = new BestFirst();
    }
    return result;
  }
  





  public static int getASTestMode()
  {
    return Integer.parseInt(get("ASTestMode", "0"));
  }
  





  public static int getASCrossvalidationFolds()
  {
    return Integer.parseInt(get("ASCrossvalidationFolds", "10"));
  }
  




  public static int getASRandomSeed()
  {
    return Integer.parseInt(get("ASRandomSeed", "1"));
  }
  








  public static void main(String[] args)
  {
    Messages.getInstance();System.out.println(Messages.getString("ExplorerDefaults_Main_Message_Text"));
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
  
  public ExplorerDefaults() {}
}
