package weka.classifiers.pmml.consumer;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.pmml.DerivedFieldMetaInfo;
import weka.core.pmml.FieldMetaInfo.Optype;
import weka.core.pmml.MappingInfo;
import weka.core.pmml.MiningSchema;
import weka.core.pmml.NormContinuous;
import weka.core.pmml.TargetMetaInfo;
import weka.gui.Logger;





































public class NeuralNetwork
  extends PMMLClassifier
{
  private static final long serialVersionUID = -4545904813133921249L;
  
  static class NeuralInput
    implements Serializable
  {
    private static final long serialVersionUID = -1902233762824835563L;
    private DerivedFieldMetaInfo m_field;
    private String m_ID = null;
    
    private String getID() {
      return m_ID;
    }
    
    protected NeuralInput(Element input, MiningSchema miningSchema) throws Exception {
      m_ID = input.getAttribute("id");
      
      NodeList fL = input.getElementsByTagName("DerivedField");
      if (fL.getLength() != 1) {
        throw new Exception("[NeuralInput] expecting just one derived field!");
      }
      
      Element dF = (Element)fL.item(0);
      Instances allFields = miningSchema.getFieldsAsInstances();
      ArrayList<Attribute> fieldDefs = new ArrayList();
      for (int i = 0; i < allFields.numAttributes(); i++) {
        fieldDefs.add(allFields.attribute(i));
      }
      m_field = new DerivedFieldMetaInfo(dF, fieldDefs, miningSchema.getTransformationDictionary());
    }
    
    protected double getValue(double[] incoming) throws Exception {
      return m_field.getDerivedValue(incoming);
    }
    
    public String toString() {
      StringBuffer temp = new StringBuffer();
      
      temp.append("Nueral input (" + getID() + ")\n");
      temp.append(m_field);
      
      return temp.toString();
    }
  }
  




  class NeuralLayer
    implements Serializable
  {
    private static final long serialVersionUID = -8386042001675763922L;
    


    private int m_numNeurons = 0;
    

    private NeuralNetwork.ActivationFunction m_layerActivationFunction = null;
    

    private double m_layerThreshold = NaN.0D;
    

    private double m_layerWidth = NaN.0D;
    

    private double m_layerAltitude = NaN.0D;
    

    private NeuralNetwork.Normalization m_layerNormalization = null;
    

    private NeuralNetwork.Neuron[] m_layerNeurons = null;
    

    private HashMap<String, Double> m_layerOutput = new HashMap();
    
    protected NeuralLayer(Element layerE)
    {
      String activationFunction = layerE.getAttribute("activationFunction");
      if ((activationFunction != null) && (activationFunction.length() > 0)) {
        for (NeuralNetwork.ActivationFunction a : NeuralNetwork.ActivationFunction.values()) {
          if (a.toString().equals(activationFunction)) {
            m_layerActivationFunction = a;
            break;
          }
          
        }
      } else {
        m_layerActivationFunction = m_activationFunction;
      }
      
      String threshold = layerE.getAttribute("threshold");
      if ((threshold != null) && (threshold.length() > 0)) {
        m_layerThreshold = Double.parseDouble(threshold);
      }
      else {
        m_layerThreshold = m_threshold;
      }
      
      String width = layerE.getAttribute("width");
      if ((width != null) && (width.length() > 0)) {
        m_layerWidth = Double.parseDouble(width);
      }
      else {
        m_layerWidth = m_width;
      }
      
      String altitude = layerE.getAttribute("altitude");
      if ((altitude != null) && (altitude.length() > 0)) {
        m_layerAltitude = Double.parseDouble(altitude);
      }
      else {
        m_layerAltitude = m_altitude;
      }
      
      String normMethod = layerE.getAttribute("normalizationMethod");
      if ((normMethod != null) && (normMethod.length() > 0)) {
        for (NeuralNetwork.Normalization n : NeuralNetwork.Normalization.values()) {
          if (n.toString().equals(normMethod)) {
            m_layerNormalization = n;
            break;
          }
          
        }
      } else {
        m_layerNormalization = m_normalizationMethod;
      }
      
      NodeList neuronL = layerE.getElementsByTagName("Neuron");
      m_numNeurons = neuronL.getLength();
      m_layerNeurons = new NeuralNetwork.Neuron[m_numNeurons];
      for (int i = 0; i < neuronL.getLength(); i++) {
        Node neuronN = neuronL.item(i);
        if (neuronN.getNodeType() == 1) {
          m_layerNeurons[i] = new NeuralNetwork.Neuron((Element)neuronN, this);
        }
      }
    }
    
    protected NeuralNetwork.ActivationFunction getActivationFunction() {
      return m_layerActivationFunction;
    }
    
    protected double getThreshold() {
      return m_layerThreshold;
    }
    
    protected double getWidth() {
      return m_layerWidth;
    }
    
    protected double getAltitude() {
      return m_layerAltitude;
    }
    
    protected NeuralNetwork.Normalization getNormalization() {
      return m_layerNormalization;
    }
    







    protected HashMap<String, Double> computeOutput(HashMap<String, Double> incoming)
      throws Exception
    {
      m_layerOutput.clear();
      
      double normSum = 0.0D;
      for (int i = 0; i < m_layerNeurons.length; i++) {
        double neuronOut = m_layerNeurons[i].getValue(incoming);
        String neuronID = m_layerNeurons[i].getID();
        
        if (m_layerNormalization == NeuralNetwork.Normalization.SOFTMAX) {
          normSum += Math.exp(neuronOut);
        } else if (m_layerNormalization == NeuralNetwork.Normalization.SIMPLEMAX) {
          normSum += neuronOut;
        }
        
        m_layerOutput.put(neuronID, Double.valueOf(neuronOut));
      }
      

      if (m_layerNormalization != NeuralNetwork.Normalization.NONE) {
        for (int i = 0; i < m_layerNeurons.length; i++) {
          double val = ((Double)m_layerOutput.get(m_layerNeurons[i].getID())).doubleValue();
          
          if (m_layerNormalization == NeuralNetwork.Normalization.SOFTMAX) {
            val = Math.exp(val) / normSum;
          } else {
            val /= normSum;
          }
          m_layerOutput.put(m_layerNeurons[i].getID(), Double.valueOf(val));
        }
      }
      return m_layerOutput;
    }
    
    public String toString() {
      StringBuffer temp = new StringBuffer();
      
      temp.append("activation: " + getActivationFunction() + "\n");
      if (!Double.isNaN(getThreshold())) {
        temp.append("threshold: " + getThreshold() + "\n");
      }
      if (!Double.isNaN(getWidth())) {
        temp.append("width: " + getWidth() + "\n");
      }
      if (!Double.isNaN(getAltitude())) {
        temp.append("altitude: " + getAltitude() + "\n");
      }
      temp.append("normalization: " + m_layerNormalization + "\n");
      for (int i = 0; i < m_numNeurons; i++) {
        temp.append(m_layerNeurons[i] + "\n");
      }
      
      return temp.toString();
    }
  }
  




  static class Neuron
    implements Serializable
  {
    private static final long serialVersionUID = -3817434025682603443L;
    


    private String m_ID = null;
    

    private NeuralNetwork.NeuralLayer m_layer;
    

    private double m_bias = 0.0D;
    

    private double m_neuronWidth = NaN.0D;
    

    private double m_neuronAltitude = NaN.0D;
    

    private String[] m_connectionIDs = null;
    

    private double[] m_weights = null;
    
    protected Neuron(Element neuronE, NeuralNetwork.NeuralLayer layer) {
      m_layer = layer;
      
      m_ID = neuronE.getAttribute("id");
      
      String bias = neuronE.getAttribute("bias");
      if ((bias != null) && (bias.length() > 0)) {
        m_bias = Double.parseDouble(bias);
      }
      
      String width = neuronE.getAttribute("width");
      if ((width != null) && (width.length() > 0)) {
        m_neuronWidth = Double.parseDouble(width);
      }
      
      String altitude = neuronE.getAttribute("altitude");
      if ((altitude != null) && (altitude.length() > 0)) {
        m_neuronAltitude = Double.parseDouble(altitude);
      }
      

      NodeList conL = neuronE.getElementsByTagName("Con");
      m_connectionIDs = new String[conL.getLength()];
      m_weights = new double[conL.getLength()];
      for (int i = 0; i < conL.getLength(); i++) {
        Node conN = conL.item(i);
        if (conN.getNodeType() == 1) {
          Element conE = (Element)conN;
          m_connectionIDs[i] = conE.getAttribute("from");
          String weight = conE.getAttribute("weight");
          m_weights[i] = Double.parseDouble(weight);
        }
      }
    }
    
    protected String getID() {
      return m_ID;
    }
    











    protected double getValue(HashMap<String, Double> incoming)
      throws Exception
    {
      double z = 0.0D;
      double result = NaN.0D;
      
      double width = Double.isNaN(m_neuronWidth) ? m_layer.getWidth() : m_neuronWidth;
      


      z = m_bias;
      for (int i = 0; i < m_connectionIDs.length; i++) {
        Double inVal = (Double)incoming.get(m_connectionIDs[i]);
        if (inVal == null) {
          throw new Exception("[Neuron] unable to find connection " + m_connectionIDs[i] + " in input Map!");
        }
        

        if (m_layer.getActivationFunction() != NeuralNetwork.ActivationFunction.RADIALBASIS)
        {
          double inV = inVal.doubleValue() * m_weights[i];
          z += inV;
        }
        else {
          double inV = Math.pow(inVal.doubleValue() - m_weights[i], 2.0D);
          z += inV;
        }
      }
      

      if (m_layer.getActivationFunction() == NeuralNetwork.ActivationFunction.RADIALBASIS) {
        z /= 2.0D * (width * width);
      }
      
      double threshold = m_layer.getThreshold();
      double altitude = Double.isNaN(m_neuronAltitude) ? m_layer.getAltitude() : m_neuronAltitude;
      


      double fanIn = m_connectionIDs.length;
      result = m_layer.getActivationFunction().eval(z, threshold, altitude, fanIn);
      
      return result;
    }
    
    public String toString() {
      StringBuffer temp = new StringBuffer();
      temp.append("Nueron (" + m_ID + ") [bias:" + m_bias);
      if (!Double.isNaN(m_neuronWidth)) {
        temp.append(" width:" + m_neuronWidth);
      }
      if (!Double.isNaN(m_neuronAltitude)) {
        temp.append(" altitude:" + m_neuronAltitude);
      }
      temp.append("]\n");
      temp.append("  con. (ID:weight): ");
      for (int i = 0; i < m_connectionIDs.length; i++) {
        temp.append(m_connectionIDs[i] + ":" + Utils.doubleToString(m_weights[i], 2));
        if (((i + 1) % 10 == 0) || (i == m_connectionIDs.length - 1)) {
          temp.append("\n                    ");
        } else {
          temp.append(", ");
        }
      }
      return temp.toString();
    }
  }
  


  static class NeuralOutputs
    implements Serializable
  {
    private static final long serialVersionUID = -233611113950482952L;
    

    private String[] m_outputNeurons = null;
    




    private int[] m_categoricalIndexes = null;
    

    private Attribute m_classAttribute = null;
    

    private NormContinuous m_regressionMapping = null;
    
    protected NeuralOutputs(Element outputs, MiningSchema miningSchema) throws Exception {
      m_classAttribute = miningSchema.getMiningSchemaAsInstances().classAttribute();
      
      int vals = m_classAttribute.isNumeric() ? 1 : m_classAttribute.numValues();
      


      m_outputNeurons = new String[vals];
      m_categoricalIndexes = new int[vals];
      
      NodeList outputL = outputs.getElementsByTagName("NeuralOutput");
      if (outputL.getLength() != m_outputNeurons.length) {
        throw new Exception("[NeuralOutputs] the number of neural outputs does not match the number expected!");
      }
      

      for (int i = 0; i < outputL.getLength(); i++) {
        Node outputN = outputL.item(i);
        if (outputN.getNodeType() == 1) {
          Element outputE = (Element)outputN;
          
          m_outputNeurons[i] = outputE.getAttribute("outputNeuron");
          
          if (m_classAttribute.isNumeric())
          {
            NodeList contL = outputE.getElementsByTagName("NormContinuous");
            if (contL.getLength() != 1) {
              throw new Exception("[NeuralOutputs] Should be exactly one norm continuous element for numeric class!");
            }
            
            Node normContNode = contL.item(0);
            String attName = ((Element)normContNode).getAttribute("field");
            Attribute dummyTargetDef = new Attribute(attName);
            ArrayList<Attribute> dummyFieldDefs = new ArrayList();
            dummyFieldDefs.add(dummyTargetDef);
            
            m_regressionMapping = new NormContinuous((Element)normContNode, FieldMetaInfo.Optype.CONTINUOUS, dummyFieldDefs);
            
            break;
          }
          

          NodeList discL = outputE.getElementsByTagName("NormDiscrete");
          if (discL.getLength() != 1) {
            throw new Exception("[NeuralOutputs] Should be only one norm discrete element per derived field/neural output for a nominal class!");
          }
          
          Node normDiscNode = discL.item(0);
          String attValue = ((Element)normDiscNode).getAttribute("value");
          int index = m_classAttribute.indexOfValue(attValue);
          if (index < 0) {
            throw new Exception("[NeuralOutputs] Can't find specified target value " + attValue + " in class attribute " + m_classAttribute.name());
          }
          
          m_categoricalIndexes[i] = index;
        }
      }
    }
    








    protected void getOuput(HashMap<String, Double> incoming, double[] preds)
      throws Exception
    {
      if (preds.length != m_outputNeurons.length) {
        throw new Exception("[NeuralOutputs] Incorrect number of predictions requested: " + preds.length + "requested, " + m_outputNeurons.length + " expected");
      }
      
      for (int i = 0; i < m_outputNeurons.length; i++) {
        Double neuronOut = (Double)incoming.get(m_outputNeurons[i]);
        if (neuronOut == null) {
          throw new Exception("[NeuralOutputs] Unable to find output neuron " + m_outputNeurons[i] + " in the incoming HashMap!!");
        }
        
        if (m_classAttribute.isNumeric())
        {
          preds[0] = neuronOut.doubleValue();
          
          preds[0] = m_regressionMapping.getResultInverse(preds);

        }
        else
        {
          preds[m_categoricalIndexes[i]] = neuronOut.doubleValue();
        }
      }
      
      if (m_classAttribute.isNominal())
      {
        double min = preds[Utils.minIndex(preds)];
        if (min < 0.0D) {
          for (int i = 0; i < preds.length; i++) {
            preds[i] -= min;
          }
        }
        
        Utils.normalize(preds);
      }
    }
    
    public String toString() {
      StringBuffer temp = new StringBuffer();
      
      for (int i = 0; i < m_outputNeurons.length; i++) {
        temp.append("Output neuron (" + m_outputNeurons[i] + ")\n");
        temp.append("mapping:\n");
        if (m_classAttribute.isNumeric()) {
          temp.append(m_regressionMapping + "\n");
        } else {
          temp.append(m_classAttribute.name() + " = " + m_classAttribute.value(m_categoricalIndexes[i]) + "\n");
        }
      }
      

      return temp.toString();
    }
  }
  


  static enum MiningFunction
  {
    CLASSIFICATION, 
    REGRESSION;
    
    private MiningFunction() {} }
  
  protected MiningFunction m_functionType = MiningFunction.CLASSIFICATION;
  


  static abstract enum ActivationFunction
  {
    THRESHOLD("threshold"), 
    






    LOGISTIC("logistic"), 
    



    TANH("tanh"), 
    






    IDENTITY("identity"), 
    



    EXPONENTIAL("exponential"), 
    



    RECIPROCAL("reciprocal"), 
    



    SQUARE("square"), 
    



    GAUSS("gauss"), 
    



    SINE("sine"), 
    



    COSINE("cosine"), 
    



    ELLICOT("ellicot"), 
    



    ARCTAN("arctan"), 
    



    RADIALBASIS("radialBasis");
    

    private final String m_stringVal;
    

    abstract double eval(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4);
    

    private ActivationFunction(String name)
    {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  

  protected ActivationFunction m_activationFunction = ActivationFunction.ARCTAN;
  


  static enum Normalization
  {
    NONE("none"), 
    SIMPLEMAX("simplemax"), 
    SOFTMAX("softmax");
    
    private final String m_stringVal;
    
    private Normalization(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  

  protected Normalization m_normalizationMethod = Normalization.NONE;
  

  protected double m_threshold = 0.0D;
  

  protected double m_width = NaN.0D;
  

  protected double m_altitude = 1.0D;
  

  protected int m_numberOfInputs = 0;
  

  protected int m_numberOfLayers = 0;
  

  protected NeuralInput[] m_inputs = null;
  

  protected HashMap<String, Double> m_inputMap = new HashMap();
  

  protected NeuralLayer[] m_layers = null;
  

  protected NeuralOutputs m_outputs = null;
  
  public NeuralNetwork(Element model, Instances dataDictionary, MiningSchema miningSchema)
    throws Exception
  {
    super(dataDictionary, miningSchema);
    
    String fn = model.getAttribute("functionName");
    if (fn.equals("regression")) {
      m_functionType = MiningFunction.REGRESSION;
    }
    
    String act = model.getAttribute("activationFunction");
    if ((act == null) || (act.length() == 0)) {
      throw new Exception("[NeuralNetwork] no activation functon defined");
    }
    

    for (ActivationFunction a : ActivationFunction.values()) {
      if (a.toString().equals(act)) {
        m_activationFunction = a;
        break;
      }
    }
    

    String norm = model.getAttribute("normalizationMethod");
    if ((norm != null) && (norm.length() > 0)) {
      for (Normalization n : Normalization.values()) {
        if (n.toString().equals(norm)) {
          m_normalizationMethod = n;
          break;
        }
      }
    }
    
    String thresh = model.getAttribute("threshold");
    if ((thresh != null) && (thresh.length() > 0)) {
      m_threshold = Double.parseDouble(thresh);
    }
    String width = model.getAttribute("width");
    if ((width != null) && (width.length() > 0)) {
      m_width = Double.parseDouble(width);
    }
    String alt = model.getAttribute("altitude");
    if ((alt != null) && (alt.length() > 0)) {
      m_altitude = Double.parseDouble(alt);
    }
    

    NodeList inputL = model.getElementsByTagName("NeuralInput");
    m_numberOfInputs = inputL.getLength();
    m_inputs = new NeuralInput[m_numberOfInputs];
    for (int i = 0; i < m_numberOfInputs; i++) {
      Node inputN = inputL.item(i);
      if (inputN.getNodeType() == 1) {
        NeuralInput nI = new NeuralInput((Element)inputN, m_miningSchema);
        m_inputs[i] = nI;
      }
    }
    

    NodeList layerL = model.getElementsByTagName("NeuralLayer");
    m_numberOfLayers = layerL.getLength();
    m_layers = new NeuralLayer[m_numberOfLayers];
    for (int i = 0; i < m_numberOfLayers; i++) {
      Node layerN = layerL.item(i);
      if (layerN.getNodeType() == 1) {
        NeuralLayer nL = new NeuralLayer((Element)layerN);
        m_layers[i] = nL;
      }
    }
    

    NodeList outputL = model.getElementsByTagName("NeuralOutputs");
    if (outputL.getLength() != 1) {
      throw new Exception("[NeuralNetwork] Should be just one NeuralOutputs element defined!");
    }
    
    m_outputs = new NeuralOutputs((Element)outputL.item(0), m_miningSchema);
  }
  


  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  







  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    if (!m_initialized) {
      mapToMiningSchema(inst.dataset());
    }
    double[] preds = null;
    
    if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
      preds = new double[1];
    } else {
      preds = new double[m_miningSchema.getFieldsAsInstances().classAttribute().numValues()];
    }
    
    double[] incoming = m_fieldsMap.instanceToSchema(inst, m_miningSchema);
    
    boolean hasMissing = false;
    for (int i = 0; i < incoming.length; i++) {
      if ((i != m_miningSchema.getFieldsAsInstances().classIndex()) && (Double.isNaN(incoming[i])))
      {
        hasMissing = true;
        
        break;
      }
    }
    
    if (hasMissing) {
      if (!m_miningSchema.hasTargetMetaData()) {
        String message = "[NeuralNetwork] WARNING: Instance to predict has missing value(s) but there is no missing value handling meta data and no prior probabilities/default value to fall back to. No prediction will be made (" + ((m_miningSchema.getFieldsAsInstances().classAttribute().isNominal()) || (m_miningSchema.getFieldsAsInstances().classAttribute().isString()) ? "zero probabilities output)." : "NaN output).");
        






        if (m_log == null) {
          System.err.println(message);
        } else {
          m_log.logMessage(message);
        }
        
        if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
          preds[0] = Instance.missingValue();
        }
        return preds;
      }
      
      TargetMetaInfo targetData = m_miningSchema.getTargetMetaData();
      if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
        preds[0] = targetData.getDefaultValue();
      } else {
        Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
        for (int i = 0; i < miningSchemaI.classAttribute().numValues(); i++) {
          preds[i] = targetData.getPriorProbability(miningSchemaI.classAttribute().value(i));
        }
      }
      return preds;
    }
    


    m_inputMap.clear();
    for (int i = 0; i < m_inputs.length; i++) {
      double networkInVal = m_inputs[i].getValue(incoming);
      String ID = m_inputs[i].getID();
      m_inputMap.put(ID, Double.valueOf(networkInVal));
    }
    

    HashMap<String, Double> layerOut = m_layers[0].computeOutput(m_inputMap);
    for (int i = 1; i < m_layers.length; i++) {
      layerOut = m_layers[i].computeOutput(layerOut);
    }
    

    m_outputs.getOuput(layerOut, preds);
    

    return preds;
  }
  
  public String toString() {
    StringBuffer temp = new StringBuffer();
    
    temp.append("PMML version " + getPMMLVersion());
    if (!getCreatorApplication().equals("?")) {
      temp.append("\nApplication: " + getCreatorApplication());
    }
    temp.append("\nPMML Model: Neural network");
    temp.append("\n\n");
    temp.append(m_miningSchema);
    
    temp.append("Inputs:\n");
    for (int i = 0; i < m_inputs.length; i++) {
      temp.append(m_inputs[i] + "\n");
    }
    
    for (int i = 0; i < m_layers.length; i++) {
      temp.append("Layer: " + (i + 1) + "\n");
      temp.append(m_layers[i] + "\n");
    }
    
    temp.append("Outputs:\n");
    temp.append(m_outputs);
    
    return temp.toString();
  }
}
