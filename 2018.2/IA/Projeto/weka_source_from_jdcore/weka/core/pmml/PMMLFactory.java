package weka.core.pmml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.classifiers.pmml.consumer.GeneralRegression;
import weka.classifiers.pmml.consumer.NeuralNetwork;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.classifiers.pmml.consumer.Regression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.gui.Logger;



























public class PMMLFactory
{
  public PMMLFactory() {}
  
  protected static enum ModelType
  {
    UNKNOWN_MODEL("unknown"), 
    REGRESSION_MODEL("Regression"), 
    GENERAL_REGRESSION_MODEL("GeneralRegression"), 
    NEURAL_NETWORK_MODEL("NeuralNetwork");
    
    private final String m_stringVal;
    
    private ModelType(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  





  public static PMMLModel getPMMLModel(String filename)
    throws Exception
  {
    return getPMMLModel(filename, null);
  }
  





  public static PMMLModel getPMMLModel(File file)
    throws Exception
  {
    return getPMMLModel(file, null);
  }
  





  public static PMMLModel getPMMLModel(InputStream stream)
    throws Exception
  {
    return getPMMLModel(stream, null);
  }
  






  public static PMMLModel getPMMLModel(String filename, Logger log)
    throws Exception
  {
    return getPMMLModel(new File(filename), log);
  }
  






  public static PMMLModel getPMMLModel(File file, Logger log)
    throws Exception
  {
    return getPMMLModel(new BufferedInputStream(new FileInputStream(file)), log);
  }
  
  private static boolean isPMML(Document doc) {
    NodeList tempL = doc.getElementsByTagName("PMML");
    if (tempL.getLength() == 0) {
      return false;
    }
    
    return true;
  }
  






  public static PMMLModel getPMMLModel(InputStream stream, Logger log)
    throws Exception
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(stream);
    stream.close();
    doc.getDocumentElement().normalize();
    if (!isPMML(doc)) {
      throw new IllegalArgumentException("[PMMLFactory] Source is not a PMML file!!");
    }
    


    Instances dataDictionary = getDataDictionaryAsInstances(doc);
    TransformationDictionary transDict = getTransformationDictionary(doc, dataDictionary);
    
    ModelType modelType = getModelType(doc);
    if (modelType == ModelType.UNKNOWN_MODEL) {
      throw new Exception("Unsupported PMML model type");
    }
    Element model = getModelElement(doc, modelType);
    

    MiningSchema ms = new MiningSchema(model, dataDictionary, transDict);
    



    PMMLModel theModel = getModelInstance(doc, modelType, model, dataDictionary, ms);
    if (log != null) {
      theModel.setLog(log);
    }
    return theModel;
  }
  









  protected static TransformationDictionary getTransformationDictionary(Document doc, Instances dataDictionary)
    throws Exception
  {
    TransformationDictionary transDict = null;
    
    NodeList transL = doc.getElementsByTagName("TransformationDictionary");
    
    if (transL.getLength() > 0) {
      Node transNode = transL.item(0);
      if (transNode.getNodeType() == 1) {
        transDict = new TransformationDictionary((Element)transNode, dataDictionary);
      }
    }
    
    return transDict;
  }
  






  public static void serializePMMLModel(PMMLModel model, String filename)
    throws Exception
  {
    serializePMMLModel(model, new File(filename));
  }
  






  public static void serializePMMLModel(PMMLModel model, File file)
    throws Exception
  {
    serializePMMLModel(model, new BufferedOutputStream(new FileOutputStream(file)));
  }
  






  public static void serializePMMLModel(PMMLModel model, OutputStream stream)
    throws Exception
  {
    ObjectOutputStream oo = new ObjectOutputStream(stream);
    Instances header = model.getMiningSchema().getFieldsAsInstances();
    oo.writeObject(header);
    oo.writeObject(model);
    oo.flush();
    oo.close();
  }
  













  protected static PMMLModel getModelInstance(Document doc, ModelType modelType, Element model, Instances dataDictionary, MiningSchema miningSchema)
    throws Exception
  {
    PMMLModel pmmlM = null;
    switch (1.$SwitchMap$weka$core$pmml$PMMLFactory$ModelType[modelType.ordinal()]) {
    case 1: 
      pmmlM = new Regression(model, dataDictionary, miningSchema);
      
      break;
    case 2: 
      pmmlM = new GeneralRegression(model, dataDictionary, miningSchema);
      
      break;
    case 3: 
      pmmlM = new NeuralNetwork(model, dataDictionary, miningSchema);
      break;
    default: 
      throw new Exception("[PMMLFactory] Unknown model type!!");
    }
    pmmlM.setPMMLVersion(doc);
    pmmlM.setCreatorApplication(doc);
    return pmmlM;
  }
  





  protected static ModelType getModelType(Document doc)
  {
    NodeList temp = doc.getElementsByTagName("RegressionModel");
    if (temp.getLength() > 0) {
      return ModelType.REGRESSION_MODEL;
    }
    
    temp = doc.getElementsByTagName("GeneralRegressionModel");
    if (temp.getLength() > 0) {
      return ModelType.GENERAL_REGRESSION_MODEL;
    }
    
    temp = doc.getElementsByTagName("NeuralNetwork");
    if (temp.getLength() > 0) {
      return ModelType.NEURAL_NETWORK_MODEL;
    }
    
    return ModelType.UNKNOWN_MODEL;
  }
  






  protected static Element getModelElement(Document doc, ModelType modelType)
    throws Exception
  {
    NodeList temp = null;
    Element model = null;
    switch (1.$SwitchMap$weka$core$pmml$PMMLFactory$ModelType[modelType.ordinal()]) {
    case 1: 
      temp = doc.getElementsByTagName("RegressionModel");
      break;
    case 2: 
      temp = doc.getElementsByTagName("GeneralRegressionModel");
      break;
    case 3: 
      temp = doc.getElementsByTagName("NeuralNetwork");
      break;
    default: 
      throw new Exception("[PMMLFactory] unknown/unsupported model type.");
    }
    
    if ((temp != null) && (temp.getLength() > 0)) {
      Node modelNode = temp.item(0);
      if (modelNode.getNodeType() == 1) {
        model = (Element)modelNode;
      }
    }
    
    return model;
  }
  






  /**
   * @deprecated
   */
  protected static Instances getMiningSchemaAsInstances(Element model, Instances dataDictionary)
    throws Exception
  {
    FastVector attInfo = new FastVector();
    NodeList fieldList = model.getElementsByTagName("MiningField");
    int classIndex = -1;
    int addedCount = 0;
    for (int i = 0; i < fieldList.getLength(); i++) {
      Node miningField = fieldList.item(i);
      if (miningField.getNodeType() == 1) {
        Element miningFieldEl = (Element)miningField;
        String name = miningFieldEl.getAttribute("name");
        String usage = miningFieldEl.getAttribute("usageType");
        


        Attribute miningAtt = dataDictionary.attribute(name);
        if (miningAtt != null) {
          if ((usage.length() == 0) || (usage.equals("active")) || (usage.equals("predicted"))) {
            attInfo.addElement(miningAtt);
            addedCount++;
          }
          if (usage.equals("predicted")) {
            classIndex = addedCount - 1;
          }
        } else {
          throw new Exception("Can't find mining field: " + name + " in the data dictionary.");
        }
      }
    }
    

    Instances insts = new Instances("miningSchema", attInfo, 0);
    
    if (classIndex != -1) {
      insts.setClassIndex(classIndex);
    }
    

    return insts;
  }
  










  protected static Instances getDataDictionaryAsInstances(Document doc)
    throws Exception
  {
    FastVector attInfo = new FastVector();
    NodeList dataDictionary = doc.getElementsByTagName("DataField");
    for (int i = 0; i < dataDictionary.getLength(); i++) {
      Node dataField = dataDictionary.item(i);
      if (dataField.getNodeType() == 1) {
        Element dataFieldEl = (Element)dataField;
        String name = dataFieldEl.getAttribute("name");
        String type = dataFieldEl.getAttribute("optype");
        Attribute tempAtt = null;
        if ((name != null) && (type != null)) {
          if (type.equals("continuous")) {
            tempAtt = new Attribute(name);
          } else if ((type.equals("categorical")) || (type.equals("ordinal"))) {
            NodeList valueList = dataFieldEl.getElementsByTagName("Value");
            if ((valueList == null) || (valueList.getLength() == 0))
            {

              FastVector nullV = null;
              tempAtt = new Attribute(name, nullV);
            }
            else {
              FastVector valueVector = new FastVector();
              for (int j = 0; j < valueList.getLength(); j++) {
                Node val = valueList.item(j);
                if (val.getNodeType() == 1)
                {
                  String property = ((Element)val).getAttribute("property");
                  if ((property == null) || (property.length() == 0) || (property.equals("valid"))) {
                    String value = ((Element)val).getAttribute("value");
                    valueVector.addElement(value);
                  }
                }
              }
              


              tempAtt = new Attribute(name, valueVector);
            }
          } else {
            throw new Exception("[PMMLFactory] can't handle " + type + "attributes.");
          }
          attInfo.addElement(tempAtt);
        }
      }
    }
    





    Instances insts = new Instances("dataDictionary", attInfo, 0);
    

    return insts;
  }
  
  public static String applyClassifier(PMMLModel model, Instances test) throws Exception {
    StringBuffer buff = new StringBuffer();
    if (!(model instanceof PMMLClassifier)) {
      throw new Exception("PMML model is not a classifier!");
    }
    
    double[] preds = null;
    PMMLClassifier classifier = (PMMLClassifier)model;
    for (int i = 0; i < test.numInstances(); i++) {
      buff.append("Actual: ");
      Instance temp = test.instance(i);
      if (temp.classAttribute().isNumeric()) {
        buff.append(temp.value(temp.classIndex()) + " ");
      } else {
        buff.append(temp.classAttribute().value((int)temp.value(temp.classIndex())) + " ");
      }
      preds = classifier.distributionForInstance(temp);
      buff.append(" Predicted: ");
      for (int j = 0; j < preds.length; j++) {
        buff.append("" + preds[j] + " ");
      }
      buff.append("\n");
    }
    return buff.toString();
  }
  
  private static class PMMLClassifierRunner extends Classifier { private PMMLClassifierRunner() {}
    
    public double[] distributionForInstance(Instance test) throws Exception { throw new Exception("Don't call this method!!"); }
    
    public void buildClassifier(Instances instances) throws Exception
    {
      throw new Exception("Don't call this method!!");
    }
    
    public String getRevision() {
      return RevisionUtils.extract("$Revision: 5562 $");
    }
    
    public void evaluatePMMLClassifier(String[] options) {
      runClassifier(this, options);
    }
  }
  
  public static void main(String[] args) {
    try {
      String[] optionsTmp = new String[args.length];
      for (int i = 0; i < args.length; i++) {
        optionsTmp[i] = args[i];
      }
      String pmmlFile = Utils.getOption('l', optionsTmp);
      if (pmmlFile.length() == 0) {
        throw new Exception("[PMMLFactory] must specify a PMML file using the -l option.");
      }
      
      PMMLModel model = getPMMLModel(pmmlFile, null);
      
      PMMLClassifierRunner pcr = new PMMLClassifierRunner(null);
      pcr.evaluatePMMLClassifier(args);








    }
    catch (Exception ex)
    {








      ex.printStackTrace();
    }
  }
}
