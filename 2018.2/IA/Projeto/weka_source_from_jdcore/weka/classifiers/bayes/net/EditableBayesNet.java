package weka.classifiers.bayes.net;

import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.DiscreteEstimatorBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.estimators.Estimator;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;































































public class EditableBayesNet
  extends BayesNet
{
  static final long serialVersionUID = 746037443258735954L;
  protected FastVector m_nPositionX;
  protected FastVector m_nPositionY;
  protected FastVector m_fMarginP;
  protected FastVector m_nEvidence;
  static final int TEST = 0;
  static final int EXECUTE = 1;
  
  public EditableBayesNet()
  {
    m_nEvidence = new FastVector(0);
    m_fMarginP = new FastVector(0);
    m_nPositionX = new FastVector();
    m_nPositionY = new FastVector();
    clearUndoStack();
  }
  
  public EditableBayesNet(Instances instances)
  {
    try {
      if (instances.classIndex() < 0) {
        instances.setClassIndex(instances.numAttributes() - 1);
      }
      m_Instances = normalizeDataSet(instances);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    int nNodes = getNrOfNodes();
    m_ParentSets = new ParentSet[nNodes];
    for (int i = 0; i < nNodes; i++) {
      m_ParentSets[i] = new ParentSet();
    }
    m_Distributions = new Estimator[nNodes][];
    for (int iNode = 0; iNode < nNodes; iNode++) {
      m_Distributions[iNode] = new Estimator[1];
      m_Distributions[iNode][0] = new DiscreteEstimatorBayes(getCardinality(iNode), 0.5D);
    }
    
    m_nEvidence = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      m_nEvidence.addElement(Integer.valueOf(-1));
    }
    m_fMarginP = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      double[] P = new double[getCardinality(i)];
      m_fMarginP.addElement(P);
    }
    
    m_nPositionX = new FastVector(nNodes);
    m_nPositionY = new FastVector(nNodes);
    for (int iNode = 0; iNode < nNodes; iNode++) {
      m_nPositionX.addElement(Integer.valueOf(iNode % 10 * 50));
      m_nPositionY.addElement(Integer.valueOf(iNode / 10 * 50));
    }
  }
  



  public EditableBayesNet(BIFReader other)
  {
    m_Instances = m_Instances;
    m_ParentSets = other.getParentSets();
    m_Distributions = other.getDistributions();
    
    int nNodes = getNrOfNodes();
    m_nPositionX = new FastVector(nNodes);
    m_nPositionY = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      m_nPositionX.addElement(Integer.valueOf(m_nPositionX[i]));
      m_nPositionY.addElement(Integer.valueOf(m_nPositionY[i]));
    }
    m_nEvidence = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      m_nEvidence.addElement(Integer.valueOf(-1));
    }
    m_fMarginP = new FastVector(nNodes);
    for (int i = 0; i < nNodes; i++) {
      double[] P = new double[getCardinality(i)];
      m_fMarginP.addElement(P);
    }
    clearUndoStack();
  }
  






  public EditableBayesNet(boolean bSetInstances)
  {
    m_nEvidence = new FastVector(0);
    m_fMarginP = new FastVector(0);
    m_nPositionX = new FastVector();
    m_nPositionY = new FastVector();
    clearUndoStack();
    if (bSetInstances) {
      m_Instances = new Instances("New Network", new FastVector(0), 0);
    }
  }
  







  public void setData(Instances instances)
    throws Exception
  {
    int[] order = new int[getNrOfNodes()];
    for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
      String sName = getNodeName(iNode);
      int nNode = 0;
      while ((nNode < getNrOfNodes()) && (!sName.equals(instances.attribute(nNode).name()))) {
        nNode++;
      }
      if (nNode >= getNrOfNodes()) {
        throw new Exception("Cannot find node named [[[" + sName + "]]] in the data");
      }
      order[iNode] = nNode;
    }
    Reorder reorderFilter = new Reorder();
    reorderFilter.setAttributeIndicesArray(order);
    reorderFilter.setInputFormat(instances);
    instances = Filter.useFilter(instances, reorderFilter);
    
    Instances newInstances = new Instances(m_Instances, 0);
    if ((m_DiscretizeFilter == null) && (m_MissingValuesFilter == null)) {
      newInstances = normalizeDataSet(instances);
    } else {
      for (int iInstance = 0; iInstance < instances.numInstances(); iInstance++) {
        newInstances.add(normalizeInstance(instances.instance(iInstance)));
      }
    }
    
    for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
      if (newInstances.attribute(iNode).numValues() != getCardinality(iNode)) {
        throw new Exception("Number of values of node [[[" + getNodeName(iNode) + "]]] differs in (discretized) dataset.");
      }
    }
    

    m_Instances = newInstances;
  }
  


  public int getNode2(String sNodeName)
  {
    int iNode = 0;
    while (iNode < m_Instances.numAttributes()) {
      if (m_Instances.attribute(iNode).name().equals(sNodeName)) {
        return iNode;
      }
      iNode++;
    }
    return -1;
  }
  

  public int getNode(String sNodeName)
    throws Exception
  {
    int iNode = getNode2(sNodeName);
    if (iNode < 0) {
      throw new Exception("Could not find node [[" + sNodeName + "]]");
    }
    return iNode;
  }
  









  public void addNode(String sName, int nCardinality)
    throws Exception
  {
    addNode(sName, nCardinality, 100 + getNrOfNodes() * 10, 100 + getNrOfNodes() * 10);
  }
  










  public void addNode(String sName, int nCardinality, int nPosX, int nPosY)
    throws Exception
  {
    if (getNode2(sName) >= 0) {
      addNode(sName + "x", nCardinality);
      return;
    }
    
    FastVector values = new FastVector(nCardinality);
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      values.addElement("Value" + (iValue + 1));
    }
    Attribute att = new Attribute(sName, values);
    m_Instances.insertAttributeAt(att, m_Instances.numAttributes());
    int nAtts = m_Instances.numAttributes();
    
    ParentSet[] parentSets = new ParentSet[nAtts];
    for (int iParentSet = 0; iParentSet < nAtts - 1; iParentSet++) {
      parentSets[iParentSet] = m_ParentSets[iParentSet];
    }
    parentSets[(nAtts - 1)] = new ParentSet();
    m_ParentSets = parentSets;
    
    Estimator[][] distributions = new Estimator[nAtts][];
    for (int iNode = 0; iNode < nAtts - 1; iNode++) {
      distributions[iNode] = m_Distributions[iNode];
    }
    distributions[(nAtts - 1)] = new Estimator[1];
    distributions[(nAtts - 1)][0] = new DiscreteEstimatorBayes(nCardinality, 0.5D);
    m_Distributions = distributions;
    
    m_nPositionX.addElement(Integer.valueOf(nPosX));
    m_nPositionY.addElement(Integer.valueOf(nPosY));
    
    m_nEvidence.addElement(Integer.valueOf(-1));
    double[] fMarginP = new double[nCardinality];
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      fMarginP[iValue] = (1.0D / nCardinality);
    }
    m_fMarginP.addElement(fMarginP);
    
    if (m_bNeedsUndoAction) {
      addUndoAction(new AddNodeAction(sName, nCardinality, nPosX, nPosY));
    }
  }
  









  public void deleteNode(String sName)
    throws Exception
  {
    int nTargetNode = getNode(sName);
    deleteNode(nTargetNode);
  }
  









  public void deleteNode(int nTargetNode)
    throws Exception
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new DeleteNodeAction(nTargetNode));
    }
    int nAtts = m_Instances.numAttributes() - 1;
    int nTargetCard = m_Instances.attribute(nTargetNode).numValues();
    
    Estimator[][] distributions = new Estimator[nAtts][];
    for (int iNode = 0; iNode < nAtts; iNode++) {
      int iNode2 = iNode;
      if (iNode >= nTargetNode) {
        iNode2++;
      }
      Estimator[] distribution = m_Distributions[iNode2];
      if (m_ParentSets[iNode2].contains(nTargetNode))
      {
        int nParentCard = m_ParentSets[iNode2].getCardinalityOfParents();
        nParentCard /= nTargetCard;
        Estimator[] distribution2 = new Estimator[nParentCard];
        for (int iParent = 0; iParent < nParentCard; iParent++) {
          distribution2[iParent] = distribution[iParent];
        }
        distribution = distribution2;
      }
      distributions[iNode] = distribution;
    }
    m_Distributions = distributions;
    
    ParentSet[] parentSets = new ParentSet[nAtts];
    for (int iParentSet = 0; iParentSet < nAtts; iParentSet++) {
      int iParentSet2 = iParentSet;
      if (iParentSet >= nTargetNode) {
        iParentSet2++;
      }
      ParentSet parentset = m_ParentSets[iParentSet2];
      parentset.deleteParent(nTargetNode, m_Instances);
      for (int iParent = 0; iParent < parentset.getNrOfParents(); iParent++) {
        int nParent = parentset.getParent(iParent);
        if (nParent > nTargetNode) {
          parentset.SetParent(iParent, nParent - 1);
        }
      }
      parentSets[iParentSet] = parentset;
    }
    m_ParentSets = parentSets;
    
    m_Instances.setClassIndex(-1);
    m_Instances.deleteAttributeAt(nTargetNode);
    m_Instances.setClassIndex(nAtts - 1);
    

    m_nPositionX.removeElementAt(nTargetNode);
    m_nPositionY.removeElementAt(nTargetNode);
    
    m_nEvidence.removeElementAt(nTargetNode);
    m_fMarginP.removeElementAt(nTargetNode);
  }
  










  public void deleteSelection(FastVector nodes)
  {
    for (int i = 0; i < nodes.size(); i++) {
      for (int j = i + 1; j < nodes.size(); j++) {
        if (((Integer)nodes.elementAt(i)).intValue() > ((Integer)nodes.elementAt(j)).intValue()) {
          int h = ((Integer)nodes.elementAt(i)).intValue();
          nodes.setElementAt(nodes.elementAt(j), i);
          nodes.setElementAt(Integer.valueOf(h), j);
        }
      }
    }
    
    if (m_bNeedsUndoAction) {
      addUndoAction(new DeleteSelectionAction(nodes));
    }
    boolean bNeedsUndoAction = m_bNeedsUndoAction;
    m_bNeedsUndoAction = false;
    try {
      for (int iNode = nodes.size() - 1; iNode >= 0; iNode--) {
        deleteNode(((Integer)nodes.elementAt(iNode)).intValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    m_bNeedsUndoAction = bNeedsUndoAction;
  }
  


  FastVector selectElements(Node item, String sElement)
    throws Exception
  {
    NodeList children = item.getChildNodes();
    FastVector nodelist = new FastVector();
    for (int iNode = 0; iNode < children.getLength(); iNode++) {
      Node node = children.item(iNode);
      if ((node.getNodeType() == 1) && (node.getNodeName().equals(sElement))) {
        nodelist.addElement(node);
      }
    }
    return nodelist;
  }
  












  public String getContent(Element node)
  {
    String result = "";
    NodeList list = node.getChildNodes();
    
    for (int i = 0; i < list.getLength(); i++) {
      Node item = list.item(i);
      if (item.getNodeType() == 3) {
        result = result + "\n" + item.getNodeValue();
      }
    }
    return result;
  }
  



  Element getDefinition(Document doc, String sName)
    throws Exception
  {
    NodeList nodelist = doc.getElementsByTagName("DEFINITION");
    for (int iNode = 0; iNode < nodelist.getLength(); iNode++) {
      Node node = nodelist.item(iNode);
      FastVector list = selectElements(node, "FOR");
      if (list.size() > 0) {
        Node forNode = (Node)list.elementAt(0);
        if (getContent((Element)forNode).trim().equals(sName)) {
          return (Element)node;
        }
      }
    }
    throw new Exception("Could not find definition for ((" + sName + "))");
  }
  










  public void paste(String sXML)
    throws Exception
  {
    try
    {
      paste(sXML, 0);
    } catch (Exception e) {
      throw e;
    }
    paste(sXML, 1);
  }
  




  void paste(String sXML, int mode)
    throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(sXML)));
    doc.normalize();
    

    NodeList nodelist = doc.getElementsByTagName("VARIABLE");
    FastVector sBaseNames = new FastVector();
    Instances instances = new Instances(m_Instances, 0);
    int nBase = instances.numAttributes();
    for (int iNode = 0; iNode < nodelist.getLength(); iNode++)
    {


      FastVector valueslist = selectElements(nodelist.item(iNode), "OUTCOME");
      
      int nValues = valueslist.size();
      
      FastVector nomStrings = new FastVector(nValues + 1);
      for (int iValue = 0; iValue < nValues; iValue++) {
        Node node = ((Node)valueslist.elementAt(iValue)).getFirstChild();
        String sValue = ((CharacterData)node).getData();
        if (sValue == null) {
          sValue = "Value" + (iValue + 1);
        }
        nomStrings.addElement(sValue);
      }
      

      FastVector nodelist2 = selectElements(nodelist.item(iNode), "NAME");
      if (nodelist2.size() == 0) {
        throw new Exception("No name specified for variable");
      }
      String sBaseName = ((CharacterData)((Node)nodelist2.elementAt(0)).getFirstChild()).getData();
      sBaseNames.addElement(sBaseName);
      String sNodeName = sBaseName;
      if (getNode2(sNodeName) >= 0) {
        sNodeName = "Copy of " + sBaseName;
      }
      int iAttempt = 2;
      while (getNode2(sNodeName) >= 0) {
        sNodeName = "Copy (" + iAttempt + ") of " + sBaseName;
        iAttempt++;
      }
      
      Attribute att = new Attribute(sNodeName, nomStrings);
      instances.insertAttributeAt(att, instances.numAttributes());
      
      valueslist = selectElements(nodelist.item(iNode), "PROPERTY");
      nValues = valueslist.size();
      
      int nPosX = iAttempt * 10;
      int nPosY = iAttempt * 10;
      for (int iValue = 0; iValue < nValues; iValue++)
      {
        Node node = ((Node)valueslist.elementAt(iValue)).getFirstChild();
        String sValue = ((CharacterData)node).getData();
        if (sValue.startsWith("position")) {
          int i0 = sValue.indexOf('(');
          int i1 = sValue.indexOf(',');
          int i2 = sValue.indexOf(')');
          String sX = sValue.substring(i0 + 1, i1).trim();
          String sY = sValue.substring(i1 + 1, i2).trim();
          try {
            nPosX = Integer.parseInt(sX) + iAttempt * 10;
            nPosY = Integer.parseInt(sY) + iAttempt * 10;
          } catch (NumberFormatException e) {
            System.err.println("Wrong number format in position :(" + sX + "," + sY + ")");
          }
        }
      }
      if (mode == 1) {
        m_nPositionX.addElement(Integer.valueOf(nPosX));
        m_nPositionY.addElement(Integer.valueOf(nPosY));
      }
    }
    


    Estimator[][] distributions = new Estimator[nBase + sBaseNames.size()][];
    ParentSet[] parentsets = new ParentSet[nBase + sBaseNames.size()];
    for (int iNode = 0; iNode < nBase; iNode++) {
      distributions[iNode] = m_Distributions[iNode];
      parentsets[iNode] = m_ParentSets[iNode];
    }
    if (mode == 1) {
      m_Instances = instances;
    }
    
    for (int iNode = 0; iNode < sBaseNames.size(); iNode++)
    {
      String sName = (String)sBaseNames.elementAt(iNode);
      Element definition = getDefinition(doc, sName);
      parentsets[(nBase + iNode)] = new ParentSet();
      


      FastVector nodelist2 = selectElements(definition, "GIVEN");
      for (int iParent = 0; iParent < nodelist2.size(); iParent++) {
        Node parentName = ((Node)nodelist2.elementAt(iParent)).getFirstChild();
        String sParentName = ((CharacterData)parentName).getData();
        int nParent = -1;
        for (int iBase = 0; iBase < sBaseNames.size(); iBase++) {
          if (sParentName.equals((String)sBaseNames.elementAt(iBase))) {
            nParent = nBase + iBase;
          }
        }
        if (nParent < 0) {
          nParent = getNode(sParentName);
        }
        parentsets[(nBase + iNode)].addParent(nParent, instances);
      }
      
      int nCardinality = parentsets[(nBase + iNode)].getCardinalityOfParents();
      int nValues = instances.attribute(nBase + iNode).numValues();
      distributions[(nBase + iNode)] = new Estimator[nCardinality];
      for (int i = 0; i < nCardinality; i++) {
        distributions[(nBase + iNode)][i] = new DiscreteEstimatorBayes(nValues, 0.0D);
      }
      
      String sTable = getContent((Element)selectElements(definition, "TABLE").elementAt(0));
      sTable = sTable.replaceAll("\\n", " ");
      StringTokenizer st = new StringTokenizer(sTable.toString());
      
      for (int i = 0; i < nCardinality; i++) {
        DiscreteEstimatorBayes d = (DiscreteEstimatorBayes)distributions[(nBase + iNode)][i];
        for (int iValue = 0; iValue < nValues; iValue++) {
          String sWeight = st.nextToken();
          d.addValue(iValue, new Double(sWeight).doubleValue());
        }
      }
      if (mode == 1) {
        m_nEvidence.insertElementAt(Integer.valueOf(-1), nBase + iNode);
        m_fMarginP.insertElementAt(new double[getCardinality(nBase + iNode)], nBase + iNode);
      }
    }
    if (mode == 1) {
      m_Distributions = distributions;
      m_ParentSets = parentsets;
    }
    
    if ((mode == 1) && (m_bNeedsUndoAction)) {
      addUndoAction(new PasteAction(sXML, nBase));
    }
  }
  









  public void addArc(String sParent, String sChild)
    throws Exception
  {
    int nParent = getNode(sParent);
    int nChild = getNode(sChild);
    addArc(nParent, nChild);
  }
  









  public void addArc(int nParent, int nChild)
    throws Exception
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new AddArcAction(nParent, nChild));
    }
    int nOldCard = m_ParentSets[nChild].getCardinalityOfParents();
    
    m_ParentSets[nChild].addParent(nParent, m_Instances);
    
    int nNewCard = m_ParentSets[nChild].getCardinalityOfParents();
    Estimator[] ds = new Estimator[nNewCard];
    for (int iParent = 0; iParent < nNewCard; iParent++) {
      ds[iParent] = Estimator.clone(m_Distributions[nChild][(iParent % nOldCard)]);
    }
    m_Distributions[nChild] = ds;
  }
  








  public void addArc(String sParent, FastVector nodes)
    throws Exception
  {
    int nParent = getNode(sParent);
    
    if (m_bNeedsUndoAction) {
      addUndoAction(new AddArcAction(nParent, nodes));
    }
    boolean bNeedsUndoAction = m_bNeedsUndoAction;
    m_bNeedsUndoAction = false;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      addArc(nParent, nNode);
    }
    m_bNeedsUndoAction = bNeedsUndoAction;
  }
  









  public void deleteArc(String sParent, String sChild)
    throws Exception
  {
    int nParent = getNode(sParent);
    int nChild = getNode(sChild);
    deleteArc(nParent, nChild);
  }
  









  public void deleteArc(int nParent, int nChild)
    throws Exception
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new DeleteArcAction(nParent, nChild));
    }
    

    int nParentCard = m_ParentSets[nChild].getCardinalityOfParents();
    int nTargetCard = m_Instances.attribute(nChild).numValues();
    nParentCard /= nTargetCard;
    Estimator[] distribution2 = new Estimator[nParentCard];
    for (int iParent = 0; iParent < nParentCard; iParent++) {
      distribution2[iParent] = m_Distributions[nChild][iParent];
    }
    m_Distributions[nChild] = distribution2;
    
    m_ParentSets[nChild].deleteParent(nParent, m_Instances);
  }
  





  public void setDistribution(String sName, double[][] P)
    throws Exception
  {
    int nTargetNode = getNode(sName);
    setDistribution(nTargetNode, P);
  }
  





  public void setDistribution(int nTargetNode, double[][] P)
    throws Exception
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new SetDistributionAction(nTargetNode, P));
    }
    Estimator[] distributions = m_Distributions[nTargetNode];
    for (int iParent = 0; iParent < distributions.length; iParent++) {
      DiscreteEstimatorBayes distribution = new DiscreteEstimatorBayes(P[0].length, 0.0D);
      for (int iValue = 0; iValue < distribution.getNumSymbols(); iValue++) {
        distribution.addValue(iValue, P[iParent][iValue]);
      }
      distributions[iParent] = distribution;
    }
  }
  




  public double[][] getDistribution(String sName)
  {
    int nTargetNode = getNode2(sName);
    return getDistribution(nTargetNode);
  }
  



  public double[][] getDistribution(int nTargetNode)
  {
    int nParentCard = m_ParentSets[nTargetNode].getCardinalityOfParents();
    int nCard = m_Instances.attribute(nTargetNode).numValues();
    double[][] P = new double[nParentCard][nCard];
    for (int iParent = 0; iParent < nParentCard; iParent++) {
      for (int iValue = 0; iValue < nCard; iValue++) {
        P[iParent][iValue] = m_Distributions[nTargetNode][iParent].getProbability(iValue);
      }
    }
    return P;
  }
  


  public String[] getValues(String sName)
  {
    int nTargetNode = getNode2(sName);
    return getValues(nTargetNode);
  }
  


  public String[] getValues(int nTargetNode)
  {
    String[] values = new String[getCardinality(nTargetNode)];
    for (int iValue = 0; iValue < values.length; iValue++) {
      values[iValue] = m_Instances.attribute(nTargetNode).value(iValue);
    }
    return values;
  }
  



  public String getValueName(int nTargetNode, int iValue)
  {
    return m_Instances.attribute(nTargetNode).value(iValue);
  }
  




  public void setNodeName(int nTargetNode, String sName)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new RenameAction(nTargetNode, getNodeName(nTargetNode), sName));
    }
    Attribute att = m_Instances.attribute(nTargetNode);
    int nCardinality = att.numValues();
    FastVector values = new FastVector(nCardinality);
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      values.addElement(att.value(iValue));
    }
    replaceAtt(nTargetNode, sName, values);
  }
  





  public void renameNodeValue(int nTargetNode, String sValue, String sNewValue)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new RenameValueAction(nTargetNode, sValue, sNewValue));
    }
    Attribute att = m_Instances.attribute(nTargetNode);
    int nCardinality = att.numValues();
    FastVector values = new FastVector(nCardinality);
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      if (att.value(iValue).equals(sValue)) {
        values.addElement(sNewValue);
      } else {
        values.addElement(att.value(iValue));
      }
    }
    replaceAtt(nTargetNode, att.name(), values);
  }
  






  public void addNodeValue(int nTargetNode, String sNewValue)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new AddValueAction(nTargetNode, sNewValue));
    }
    Attribute att = m_Instances.attribute(nTargetNode);
    int nCardinality = att.numValues();
    FastVector values = new FastVector(nCardinality);
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      values.addElement(att.value(iValue));
    }
    values.addElement(sNewValue);
    replaceAtt(nTargetNode, att.name(), values);
    

    Estimator[] distributions = m_Distributions[nTargetNode];
    int nNewCard = values.size();
    for (int iParent = 0; iParent < distributions.length; iParent++) {
      DiscreteEstimatorBayes distribution = new DiscreteEstimatorBayes(nNewCard, 0.0D);
      for (int iValue = 0; iValue < nNewCard - 1; iValue++) {
        distribution.addValue(iValue, distributions[iParent].getProbability(iValue));
      }
      distributions[iParent] = distribution;
    }
    

    for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
      if (m_ParentSets[iNode].contains(nTargetNode)) {
        distributions = m_Distributions[iNode];
        ParentSet parentSet = m_ParentSets[iNode];
        int nParentCard = parentSet.getFreshCardinalityOfParents(m_Instances);
        Estimator[] newDistributions = new Estimator[nParentCard];
        int nCard = getCardinality(iNode);
        int nParents = parentSet.getNrOfParents();
        int[] values2 = new int[nParents];
        int iOldPos = 0;
        int iTargetNode = 0;
        while (parentSet.getParent(iTargetNode) != nTargetNode) {
          iTargetNode++;
        }
        for (int iPos = 0; iPos < nParentCard; iPos++) {
          DiscreteEstimatorBayes distribution = new DiscreteEstimatorBayes(nCard, 0.0D);
          for (int iValue = 0; iValue < nCard; iValue++) {
            distribution.addValue(iValue, distributions[iOldPos].getProbability(iValue));
          }
          newDistributions[iPos] = distribution;
          
          int i = 0;
          values2[i] += 1;
          while ((i < nParents) && (values2[i] == getCardinality(parentSet.getParent(i)))) {
            values2[i] = 0;
            i++;
            if (i < nParents) {
              values2[i] += 1;
            }
          }
          if (values2[iTargetNode] != nNewCard - 1) {
            iOldPos++;
          }
        }
        m_Distributions[iNode] = newDistributions;
      }
    }
  }
  







  public void delNodeValue(int nTargetNode, String sValue)
    throws Exception
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new DelValueAction(nTargetNode, sValue));
    }
    Attribute att = m_Instances.attribute(nTargetNode);
    int nCardinality = att.numValues();
    FastVector values = new FastVector(nCardinality);
    int nValue = -1;
    for (int iValue = 0; iValue < nCardinality; iValue++) {
      if (att.value(iValue).equals(sValue)) {
        nValue = iValue;
      } else {
        values.addElement(att.value(iValue));
      }
    }
    if (nValue < 0)
    {
      throw new Exception("Node " + nTargetNode + " does not have value (" + sValue + ")");
    }
    replaceAtt(nTargetNode, att.name(), values);
    

    Estimator[] distributions = m_Distributions[nTargetNode];
    int nCard = values.size();
    for (int iParent = 0; iParent < distributions.length; iParent++) {
      DiscreteEstimatorBayes distribution = new DiscreteEstimatorBayes(nCard, 0.0D);
      double sum = 0.0D;
      for (int iValue = 0; iValue < nCard; iValue++) {
        sum += distributions[iParent].getProbability(iValue);
      }
      if (sum > 0.0D) {
        for (int iValue = 0; iValue < nCard; iValue++) {
          distribution.addValue(iValue, distributions[iParent].getProbability(iValue) / sum);
        }
      } else {
        for (int iValue = 0; iValue < nCard; iValue++) {
          distribution.addValue(iValue, 1.0D / nCard);
        }
      }
      distributions[iParent] = distribution;
    }
    

    for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
      if (m_ParentSets[iNode].contains(nTargetNode)) {
        ParentSet parentSet = m_ParentSets[iNode];
        distributions = m_Distributions[iNode];
        Estimator[] newDistributions = new Estimator[distributions.length * nCard / (nCard + 1)];
        int iCurrentDist = 0;
        
        int nParents = parentSet.getNrOfParents();
        int[] values2 = new int[nParents];
        
        int nParentCard = parentSet.getFreshCardinalityOfParents(m_Instances) * (nCard + 1) / nCard;
        int iTargetNode = 0;
        while (parentSet.getParent(iTargetNode) != nTargetNode) {
          iTargetNode++;
        }
        int[] nCards = new int[nParents];
        for (int iParent = 0; iParent < nParents; iParent++) {
          nCards[iParent] = getCardinality(parentSet.getParent(iParent));
        }
        nCards[iTargetNode] += 1;
        for (int iPos = 0; iPos < nParentCard; iPos++) {
          if (values2[iTargetNode] != nValue) {
            newDistributions[(iCurrentDist++)] = distributions[iPos];
          }
          
          int i = 0;
          values2[i] += 1;
          while ((i < nParents) && (values2[i] == nCards[i])) {
            values2[i] = 0;
            i++;
            if (i < nParents) {
              values2[i] += 1;
            }
          }
        }
        
        m_Distributions[iNode] = newDistributions;
      }
    }
    
    if (getEvidence(nTargetNode) > nValue) {
      setEvidence(nTargetNode, getEvidence(nTargetNode) - 1);
    }
  }
  





  public void setPosition(int iNode, int nX, int nY)
  {
    if (m_bNeedsUndoAction) {
      boolean isUpdate = false;
      UndoAction undoAction = null;
      try {
        if (m_undoStack.size() > 0) {
          undoAction = (UndoAction)m_undoStack.elementAt(m_undoStack.size() - 1);
          SetPositionAction posAction = (SetPositionAction)undoAction;
          if (m_nTargetNode == iNode) {
            isUpdate = true;
            posAction.setUndoPosition(nX, nY);
          }
        }
      }
      catch (Exception e) {}
      
      if (!isUpdate) {
        addUndoAction(new SetPositionAction(iNode, nX, nY));
      }
    }
    m_nPositionX.setElementAt(Integer.valueOf(nX), iNode);
    m_nPositionY.setElementAt(Integer.valueOf(nY), iNode);
  }
  






  public void setPosition(int nNode, int nX, int nY, FastVector nodes)
  {
    int dX = nX - getPositionX(nNode);
    int dY = nY - getPositionY(nNode);
    
    if (m_bNeedsUndoAction) {
      boolean isUpdate = false;
      try {
        UndoAction undoAction = null;
        if (m_undoStack.size() > 0) {
          undoAction = (UndoAction)m_undoStack.elementAt(m_undoStack.size() - 1);
          SetGroupPositionAction posAction = (SetGroupPositionAction)undoAction;
          isUpdate = true;
          int iNode = 0;
          while ((isUpdate) && (iNode < m_nodes.size())) {
            if ((Integer)m_nodes.elementAt(iNode) != (Integer)nodes.elementAt(iNode)) {
              isUpdate = false;
            }
            iNode++;
          }
          if (isUpdate == true) {
            posAction.setUndoPosition(dX, dY);
          }
        }
      }
      catch (Exception e) {}
      
      if (!isUpdate) {
        addUndoAction(new SetGroupPositionAction(nodes, dX, dY));
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionX.setElementAt(Integer.valueOf(getPositionX(nNode) + dX), nNode);
      m_nPositionY.setElementAt(Integer.valueOf(getPositionY(nNode) + dY), nNode);
    }
  }
  



  public void layoutGraph(FastVector nPosX, FastVector nPosY)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new LayoutGraphAction(nPosX, nPosY));
    }
    m_nPositionX = nPosX;
    m_nPositionY = nPosY;
  }
  


  public int getPositionX(int iNode)
  {
    return ((Integer)m_nPositionX.elementAt(iNode)).intValue();
  }
  


  public int getPositionY(int iNode)
  {
    return ((Integer)m_nPositionY.elementAt(iNode)).intValue();
  }
  



  public void alignLeft(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new alignLeftAction(nodes));
    }
    int nMinX = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nX = getPositionX(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nX < nMinX) || (iNode == 0)) {
        nMinX = nX;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionX.setElementAt(Integer.valueOf(nMinX), nNode);
    }
  }
  



  public void alignRight(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new alignRightAction(nodes));
    }
    int nMaxX = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nX = getPositionX(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nX > nMaxX) || (iNode == 0)) {
        nMaxX = nX;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionX.setElementAt(Integer.valueOf(nMaxX), nNode);
    }
  }
  



  public void alignTop(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new alignTopAction(nodes));
    }
    int nMinY = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nY = getPositionY(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nY < nMinY) || (iNode == 0)) {
        nMinY = nY;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionY.setElementAt(Integer.valueOf(nMinY), nNode);
    }
  }
  



  public void alignBottom(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new alignBottomAction(nodes));
    }
    int nMaxY = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nY = getPositionY(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nY > nMaxY) || (iNode == 0)) {
        nMaxY = nY;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionY.setElementAt(Integer.valueOf(nMaxY), nNode);
    }
  }
  



  public void centerHorizontal(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new centerHorizontalAction(nodes));
    }
    int nMinY = -1;
    int nMaxY = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nY = getPositionY(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nY < nMinY) || (iNode == 0)) {
        nMinY = nY;
      }
      if ((nY > nMaxY) || (iNode == 0)) {
        nMaxY = nY;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionY.setElementAt(Integer.valueOf((nMinY + nMaxY) / 2), nNode);
    }
  }
  



  public void centerVertical(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new centerVerticalAction(nodes));
    }
    int nMinX = -1;
    int nMaxX = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nX = getPositionX(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nX < nMinX) || (iNode == 0)) {
        nMinX = nX;
      }
      if ((nX > nMaxX) || (iNode == 0)) {
        nMaxX = nX;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionX.setElementAt(Integer.valueOf((nMinX + nMaxX) / 2), nNode);
    }
  }
  



  public void spaceHorizontal(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new spaceHorizontalAction(nodes));
    }
    int nMinX = -1;
    int nMaxX = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nX = getPositionX(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nX < nMinX) || (iNode == 0)) {
        nMinX = nX;
      }
      if ((nX > nMaxX) || (iNode == 0)) {
        nMaxX = nX;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionX.setElementAt(Integer.valueOf((int)(nMinX + iNode * (nMaxX - nMinX) / (nodes.size() - 1.0D))), nNode);
    }
  }
  



  public void spaceVertical(FastVector nodes)
  {
    if (m_bNeedsUndoAction) {
      addUndoAction(new spaceVerticalAction(nodes));
    }
    int nMinY = -1;
    int nMaxY = -1;
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nY = getPositionY(((Integer)nodes.elementAt(iNode)).intValue());
      if ((nY < nMinY) || (iNode == 0)) {
        nMinY = nY;
      }
      if ((nY > nMaxY) || (iNode == 0)) {
        nMaxY = nY;
      }
    }
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      m_nPositionY.setElementAt(Integer.valueOf((int)(nMinY + iNode * (nMaxY - nMinY) / (nodes.size() - 1.0D))), nNode);
    }
  }
  





  void replaceAtt(int nTargetNode, String sName, FastVector values)
  {
    Attribute newAtt = new Attribute(sName, values);
    if (m_Instances.classIndex() == nTargetNode) {
      m_Instances.setClassIndex(-1);
      



      m_Instances.deleteAttributeAt(nTargetNode);
      m_Instances.insertAttributeAt(newAtt, nTargetNode);
      m_Instances.setClassIndex(nTargetNode);
    }
    else
    {
      m_Instances.deleteAttributeAt(nTargetNode);
      m_Instances.insertAttributeAt(newAtt, nTargetNode);
    }
  }
  


  public double[] getMargin(int iNode)
  {
    return (double[])m_fMarginP.elementAt(iNode);
  }
  



  public void setMargin(int iNode, double[] fMarginP)
  {
    m_fMarginP.setElementAt(fMarginP, iNode);
  }
  



  public int getEvidence(int iNode)
  {
    return ((Integer)m_nEvidence.elementAt(iNode)).intValue();
  }
  




  public void setEvidence(int iNode, int iValue)
  {
    m_nEvidence.setElementAt(Integer.valueOf(iValue), iNode);
  }
  


  public FastVector getChildren(int nTargetNode)
  {
    FastVector children = new FastVector();
    for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
      if (m_ParentSets[iNode].contains(nTargetNode)) {
        children.addElement(Integer.valueOf(iNode));
      }
    }
    return children;
  }
  

  public String toXMLBIF03()
  {
    if (m_Instances == null) {
      return "<!--No model built yet-->";
    }
    
    StringBuffer text = new StringBuffer();
    text.append(getBIFHeader());
    text.append("\n");
    text.append("\n");
    text.append("<BIF VERSION=\"0.3\">\n");
    text.append("<NETWORK>\n");
    text.append("<NAME>" + XMLNormalize(m_Instances.relationName()) + "</NAME>\n");
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      text.append("<VARIABLE TYPE=\"nature\">\n");
      text.append("<NAME>" + XMLNormalize(m_Instances.attribute(iAttribute).name()) + "</NAME>\n");
      for (int iValue = 0; iValue < m_Instances.attribute(iAttribute).numValues(); iValue++) {
        text.append("<OUTCOME>" + XMLNormalize(m_Instances.attribute(iAttribute).value(iValue)) + "</OUTCOME>\n");
      }
      
      text.append("<PROPERTY>position = (" + getPositionX(iAttribute) + "," + getPositionY(iAttribute) + ")</PROPERTY>\n");
      
      text.append("</VARIABLE>\n");
    }
    
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      text.append("<DEFINITION>\n");
      text.append("<FOR>" + XMLNormalize(m_Instances.attribute(iAttribute).name()) + "</FOR>\n");
      for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
        text.append("<GIVEN>" + XMLNormalize(m_Instances.attribute(m_ParentSets[iAttribute].getParent(iParent)).name()) + "</GIVEN>\n");
      }
      

      text.append("<TABLE>\n");
      for (int iParent = 0; iParent < m_ParentSets[iAttribute].getCardinalityOfParents(); iParent++) {
        for (int iValue = 0; iValue < m_Instances.attribute(iAttribute).numValues(); iValue++) {
          text.append(m_Distributions[iAttribute][iParent].getProbability(iValue));
          text.append(' ');
        }
        text.append('\n');
      }
      text.append("</TABLE>\n");
      text.append("</DEFINITION>\n");
    }
    text.append("</NETWORK>\n");
    text.append("</BIF>\n");
    return text.toString();
  }
  


  public String toXMLBIF03(FastVector nodes)
  {
    StringBuffer text = new StringBuffer();
    text.append(getBIFHeader());
    text.append("\n");
    text.append("\n");
    text.append("<BIF VERSION=\"0.3\">\n");
    text.append("<NETWORK>\n");
    text.append("<NAME>" + XMLNormalize(m_Instances.relationName()) + "</NAME>\n");
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      text.append("<VARIABLE TYPE=\"nature\">\n");
      text.append("<NAME>" + XMLNormalize(m_Instances.attribute(nNode).name()) + "</NAME>\n");
      for (int iValue = 0; iValue < m_Instances.attribute(nNode).numValues(); iValue++) {
        text.append("<OUTCOME>" + XMLNormalize(m_Instances.attribute(nNode).value(iValue)) + "</OUTCOME>\n");
      }
      text.append("<PROPERTY>position = (" + getPositionX(nNode) + "," + getPositionY(nNode) + ")</PROPERTY>\n");
      text.append("</VARIABLE>\n");
    }
    
    for (int iNode = 0; iNode < nodes.size(); iNode++) {
      int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
      text.append("<DEFINITION>\n");
      text.append("<FOR>" + XMLNormalize(m_Instances.attribute(nNode).name()) + "</FOR>\n");
      for (int iParent = 0; iParent < m_ParentSets[nNode].getNrOfParents(); iParent++) {
        text.append("<GIVEN>" + XMLNormalize(m_Instances.attribute(m_ParentSets[nNode].getParent(iParent)).name()) + "</GIVEN>\n");
      }
      

      text.append("<TABLE>\n");
      for (int iParent = 0; iParent < m_ParentSets[nNode].getCardinalityOfParents(); iParent++) {
        for (int iValue = 0; iValue < m_Instances.attribute(nNode).numValues(); iValue++) {
          text.append(m_Distributions[nNode][iParent].getProbability(iValue));
          text.append(' ');
        }
        text.append('\n');
      }
      text.append("</TABLE>\n");
      text.append("</DEFINITION>\n");
    }
    text.append("</NETWORK>\n");
    text.append("</BIF>\n");
    return text.toString();
  }
  

  FastVector m_undoStack = new FastVector();
  

  int m_nCurrentEditAction = -1;
  

  int m_nSavedPointer = -1;
  




  boolean m_bNeedsUndoAction = true;
  
  public boolean canUndo()
  {
    return m_nCurrentEditAction >= 0;
  }
  
  public boolean canRedo()
  {
    return m_nCurrentEditAction < m_undoStack.size() - 1;
  }
  
  public boolean isChanged()
  {
    return m_nCurrentEditAction != m_nSavedPointer;
  }
  
  public void isSaved()
  {
    m_nSavedPointer = m_nCurrentEditAction;
  }
  
  public String lastActionMsg()
  {
    if (m_undoStack.size() == 0) {
      return "";
    }
    return ((UndoAction)m_undoStack.lastElement()).getRedoMsg();
  }
  



  public String undo()
  {
    if (!canUndo()) {
      return "";
    }
    UndoAction undoAction = (UndoAction)m_undoStack.elementAt(m_nCurrentEditAction);
    m_bNeedsUndoAction = false;
    undoAction.undo();
    m_bNeedsUndoAction = true;
    m_nCurrentEditAction -= 1;
    



















    return undoAction.getUndoMsg();
  }
  


  public String redo()
  {
    if (!canRedo()) {
      return "";
    }
    m_nCurrentEditAction += 1;
    UndoAction undoAction = (UndoAction)m_undoStack.elementAt(m_nCurrentEditAction);
    m_bNeedsUndoAction = false;
    undoAction.redo();
    m_bNeedsUndoAction = true;
    



















    return undoAction.getRedoMsg();
  }
  


  void addUndoAction(UndoAction action)
  {
    int iAction = m_undoStack.size() - 1;
    while (iAction > m_nCurrentEditAction) {
      m_undoStack.removeElementAt(iAction--);
    }
    if (m_nSavedPointer > m_nCurrentEditAction) {
      m_nSavedPointer = -2;
    }
    m_undoStack.addElement(action);
    
    m_nCurrentEditAction += 1;
  }
  
  public void clearUndoStack()
  {
    m_undoStack = new FastVector();
    
    m_nCurrentEditAction = -1;
    m_nSavedPointer = -1;
  }
  
  class UndoAction
    implements Serializable
  {
    static final long serialVersionUID = 1L;
    
    UndoAction() {}
    
    public void undo() {}
    
    public void redo() {}
    
    public String getUndoMsg()
    {
      return getMsg();
    }
    

    public String getRedoMsg() { return getMsg(); }
    
    String getMsg() {
      String sStr = toString();
      int iStart = sStr.indexOf('$');
      int iEnd = sStr.indexOf('@');
      StringBuffer sBuffer = new StringBuffer();
      for (int i = iStart + 1; i < iEnd; i++) {
        char c = sStr.charAt(i);
        if (Character.isUpperCase(c)) {
          sBuffer.append(' ');
        }
        sBuffer.append(sStr.charAt(i));
      }
      return sBuffer.toString();
    }
  }
  
  class AddNodeAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    String m_sName;
    int m_nPosX;
    int m_nPosY;
    int m_nCardinality;
    
    AddNodeAction(String sName, int nCardinality, int nPosX, int nPosY) {
      super();
      m_sName = sName;
      m_nCardinality = nCardinality;
      m_nPosX = nPosX;
      m_nPosY = nPosY;
    }
    
    public void undo() {
      try {
        deleteNode(getNrOfNodes() - 1);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        addNode(m_sName, m_nCardinality, m_nPosX, m_nPosY);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  

  class DeleteNodeAction
    extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    
    int m_nTargetNode;
    
    Attribute m_att;
    Estimator[] m_CPT;
    ParentSet m_ParentSet;
    FastVector m_deleteArcActions;
    int m_nPosX;
    int m_nPosY;
    
    DeleteNodeAction(int nTargetNode)
    {
      super();
      m_nTargetNode = nTargetNode;
      m_att = m_Instances.attribute(nTargetNode);
      try {
        so = new SerializedObject(m_Distributions[nTargetNode]);
        m_CPT = ((Estimator[])so.getObject());
        
        so = new SerializedObject(m_ParentSets[nTargetNode]);
        m_ParentSet = ((ParentSet)so.getObject());
      } catch (Exception e) { SerializedObject so;
        e.printStackTrace();
      }
      m_deleteArcActions = new FastVector();
      for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
        if (m_ParentSets[iNode].contains(nTargetNode)) {
          m_deleteArcActions.addElement(new EditableBayesNet.DeleteArcAction(EditableBayesNet.this, nTargetNode, iNode));
        }
      }
      m_nPosX = getPositionX(m_nTargetNode);
      m_nPosY = getPositionY(m_nTargetNode);
    }
    
    public void undo() {
      try {
        m_Instances.insertAttributeAt(m_att, m_nTargetNode);
        int nAtts = m_Instances.numAttributes();
        
        ParentSet[] parentSets = new ParentSet[nAtts];
        int nX = 0;
        for (int iParentSet = 0; iParentSet < nAtts; iParentSet++) {
          if (iParentSet == m_nTargetNode) {
            SerializedObject so = new SerializedObject(m_ParentSet);
            parentSets[iParentSet] = ((ParentSet)so.getObject());
            nX = 1;
          } else {
            parentSets[iParentSet] = m_ParentSets[(iParentSet - nX)];
            for (int iParent = 0; iParent < parentSets[iParentSet].getNrOfParents(); iParent++) {
              int nParent = parentSets[iParentSet].getParent(iParent);
              if (nParent >= m_nTargetNode) {
                parentSets[iParentSet].SetParent(iParent, nParent + 1);
              }
            }
          }
        }
        m_ParentSets = parentSets;
        
        Estimator[][] distributions = new Estimator[nAtts][];
        nX = 0;
        for (int iNode = 0; iNode < nAtts; iNode++) {
          if (iNode == m_nTargetNode) {
            SerializedObject so = new SerializedObject(m_CPT);
            distributions[iNode] = ((Estimator[])(Estimator[])so.getObject());
            nX = 1;
          } else {
            distributions[iNode] = m_Distributions[(iNode - nX)];
          }
        }
        m_Distributions = distributions;
        
        for (int deletedArc = 0; deletedArc < m_deleteArcActions.size(); deletedArc++) {
          EditableBayesNet.DeleteArcAction action = (EditableBayesNet.DeleteArcAction)m_deleteArcActions.elementAt(deletedArc);
          action.undo();
        }
        m_nPositionX.insertElementAt(Integer.valueOf(m_nPosX), m_nTargetNode);
        m_nPositionY.insertElementAt(Integer.valueOf(m_nPosY), m_nTargetNode);
        m_nEvidence.insertElementAt(Integer.valueOf(-1), m_nTargetNode);
        m_fMarginP.insertElementAt(new double[getCardinality(m_nTargetNode)], m_nTargetNode);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        deleteNode(m_nTargetNode);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  

  class DeleteSelectionAction
    extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    
    FastVector m_nodes;
    
    Attribute[] m_att;
    Estimator[][] m_CPT;
    ParentSet[] m_ParentSet;
    FastVector m_deleteArcActions;
    int[] m_nPosX;
    int[] m_nPosY;
    
    public DeleteSelectionAction(FastVector nodes)
    {
      super();
      m_nodes = new FastVector();
      int nNodes = nodes.size();
      m_att = new Attribute[nNodes];
      m_CPT = new Estimator[nNodes][];
      m_ParentSet = new ParentSet[nNodes];
      m_nPosX = new int[nNodes];
      m_nPosY = new int[nNodes];
      m_deleteArcActions = new FastVector();
      for (int iNode = 0; iNode < nodes.size(); iNode++) {
        int nTargetNode = ((Integer)nodes.elementAt(iNode)).intValue();
        m_nodes.addElement(Integer.valueOf(nTargetNode));
        m_att[iNode] = m_Instances.attribute(nTargetNode);
        try {
          SerializedObject so = new SerializedObject(m_Distributions[nTargetNode]);
          m_CPT[iNode] = ((Estimator[])(Estimator[])so.getObject());
          
          so = new SerializedObject(m_ParentSets[nTargetNode]);
          m_ParentSet[iNode] = ((ParentSet)so.getObject());
        } catch (Exception e) {
          e.printStackTrace();
        }
        m_nPosX[iNode] = getPositionX(nTargetNode);
        m_nPosY[iNode] = getPositionY(nTargetNode);
        for (int iNode2 = 0; iNode2 < getNrOfNodes(); iNode2++) {
          if ((!nodes.contains(Integer.valueOf(iNode2))) && (m_ParentSets[iNode2].contains(nTargetNode))) {
            m_deleteArcActions.addElement(new EditableBayesNet.DeleteArcAction(EditableBayesNet.this, nTargetNode, iNode2));
          }
        }
      }
    }
    
    public void undo() {
      try {
        for (int iNode = 0; iNode < m_nodes.size(); iNode++) {
          int nTargetNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
          m_Instances.insertAttributeAt(m_att[iNode], nTargetNode);
        }
        int nAtts = m_Instances.numAttributes();
        
        ParentSet[] parentSets = new ParentSet[nAtts];
        int[] offset = new int[nAtts];
        for (int iNode = 0; iNode < nAtts; iNode++) {
          offset[iNode] = iNode;
        }
        for (int iNode = m_nodes.size() - 1; iNode >= 0; iNode--) {
          int nTargetNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
          for (int i = nTargetNode; i < nAtts - 1; i++) {
            offset[i] = offset[(i + 1)];
          }
        }
        
        int iTargetNode = 0;
        for (int iParentSet = 0; iParentSet < nAtts; iParentSet++) {
          if ((iTargetNode < m_nodes.size()) && ((Integer)m_nodes.elementAt(iTargetNode) == Integer.valueOf(iParentSet)))
          {
            SerializedObject so = new SerializedObject(m_ParentSet[iTargetNode]);
            parentSets[iParentSet] = ((ParentSet)so.getObject());
            iTargetNode++;
          } else {
            parentSets[iParentSet] = m_ParentSets[(iParentSet - iTargetNode)];
            for (int iParent = 0; iParent < parentSets[iParentSet].getNrOfParents(); iParent++) {
              int nParent = parentSets[iParentSet].getParent(iParent);
              parentSets[iParentSet].SetParent(iParent, offset[nParent]);
            }
          }
        }
        m_ParentSets = parentSets;
        
        Estimator[][] distributions = new Estimator[nAtts][];
        iTargetNode = 0;
        for (int iNode = 0; iNode < nAtts; iNode++) {
          if ((iTargetNode < m_nodes.size()) && ((Integer)m_nodes.elementAt(iTargetNode) == Integer.valueOf(iNode))) {
            SerializedObject so = new SerializedObject(m_CPT[iTargetNode]);
            distributions[iNode] = ((Estimator[])(Estimator[])so.getObject());
            iTargetNode++;
          } else {
            distributions[iNode] = m_Distributions[(iNode - iTargetNode)];
          }
        }
        m_Distributions = distributions;
        
        for (int iNode = 0; iNode < m_nodes.size(); iNode++) {
          int nTargetNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
          m_nPositionX.insertElementAt(Integer.valueOf(m_nPosX[iNode]), nTargetNode);
          m_nPositionY.insertElementAt(Integer.valueOf(m_nPosY[iNode]), nTargetNode);
          m_nEvidence.insertElementAt(Integer.valueOf(-1), nTargetNode);
          m_fMarginP.insertElementAt(new double[getCardinality(nTargetNode)], nTargetNode);
        }
        for (int deletedArc = 0; deletedArc < m_deleteArcActions.size(); deletedArc++) {
          EditableBayesNet.DeleteArcAction action = (EditableBayesNet.DeleteArcAction)m_deleteArcActions.elementAt(deletedArc);
          action.undo();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        for (int iNode = m_nodes.size() - 1; iNode >= 0; iNode--) {
          int nNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
          deleteNode(nNode);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class AddArcAction
    extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    FastVector m_children;
    int m_nParent;
    Estimator[][] m_CPT;
    
    AddArcAction(int nParent, int nChild)
    {
      super();
      try {
        m_nParent = nParent;
        m_children = new FastVector();
        m_children.addElement(Integer.valueOf(nChild));
        
        SerializedObject so = new SerializedObject(m_Distributions[nChild]);
        m_CPT = new Estimator[1][];
        m_CPT[0] = ((Estimator[])(Estimator[])so.getObject());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    AddArcAction(int nParent, FastVector children) { super();
      try {
        m_nParent = nParent;
        m_children = new FastVector();
        m_CPT = new Estimator[children.size()][];
        for (iChild = 0; iChild < children.size(); iChild++) {
          int nChild = ((Integer)children.elementAt(iChild)).intValue();
          m_children.addElement(Integer.valueOf(nChild));
          SerializedObject so = new SerializedObject(m_Distributions[nChild]);
          m_CPT[iChild] = ((Estimator[])(Estimator[])so.getObject());
        }
      } catch (Exception e) { int iChild;
        e.printStackTrace();
      }
    }
    
    public void undo() {
      try {
        for (int iChild = 0; iChild < m_children.size(); iChild++) {
          int nChild = ((Integer)m_children.elementAt(iChild)).intValue();
          deleteArc(m_nParent, nChild);
          SerializedObject so = new SerializedObject(m_CPT[iChild]);
          m_Distributions[nChild] = ((Estimator[])(Estimator[])so.getObject());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        for (int iChild = 0; iChild < m_children.size(); iChild++) {
          int nChild = ((Integer)m_children.elementAt(iChild)).intValue();
          addArc(m_nParent, nChild);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class DeleteArcAction extends EditableBayesNet.UndoAction {
    static final long serialVersionUID = 1L;
    int[] m_nParents;
    int m_nChild;
    int m_nParent;
    Estimator[] m_CPT;
    
    DeleteArcAction(int nParent, int nChild) {
      super();
      try {
        m_nChild = nChild;
        m_nParent = nParent;
        m_nParents = new int[getNrOfParents(nChild)];
        for (int iParent = 0; iParent < m_nParents.length; iParent++) {
          m_nParents[iParent] = getParent(nChild, iParent);
        }
        SerializedObject so = new SerializedObject(m_Distributions[nChild]);
        m_CPT = ((Estimator[])so.getObject());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void undo() {
      try {
        SerializedObject so = new SerializedObject(m_CPT);
        m_Distributions[m_nChild] = ((Estimator[])(Estimator[])so.getObject());
        ParentSet parentSet = new ParentSet();
        for (int iParent = 0; iParent < m_nParents.length; iParent++) {
          parentSet.addParent(m_nParents[iParent], m_Instances);
        }
        m_ParentSets[m_nChild] = parentSet;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        deleteArc(m_nParent, m_nChild);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class SetDistributionAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    int m_nTargetNode;
    Estimator[] m_CPT;
    double[][] m_P;
    
    SetDistributionAction(int nTargetNode, double[][] P)
    {
      super();
      try {
        m_nTargetNode = nTargetNode;
        so = new SerializedObject(m_Distributions[nTargetNode]);
        m_CPT = ((Estimator[])so.getObject());
        
        m_P = P;
      } catch (Exception e) { SerializedObject so;
        e.printStackTrace();
      }
    }
    
    public void undo() {
      try {
        SerializedObject so = new SerializedObject(m_CPT);
        m_Distributions[m_nTargetNode] = ((Estimator[])(Estimator[])so.getObject());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        setDistribution(m_nTargetNode, m_P);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Distribution of node " + getNodeName(m_nTargetNode) + " changed";
    }
    
    public String getRedoMsg() {
      return "Distribution of node " + getNodeName(m_nTargetNode) + " changed";
    }
  }
  
  class RenameAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    int m_nTargetNode;
    String m_sNewName;
    String m_sOldName;
    
    RenameAction(int nTargetNode, String sOldName, String sNewName)
    {
      super();
      m_nTargetNode = nTargetNode;
      m_sNewName = sNewName;
      m_sOldName = sOldName;
    }
    
    public void undo() {
      setNodeName(m_nTargetNode, m_sOldName);
    }
    
    public void redo() {
      setNodeName(m_nTargetNode, m_sNewName);
    }
  }
  
  class RenameValueAction extends EditableBayesNet.RenameAction {
    static final long serialVersionUID = 1L;
    
    RenameValueAction(int nTargetNode, String sOldName, String sNewName) {
      super(nTargetNode, sOldName, sNewName);
    }
    
    public void undo() {
      renameNodeValue(m_nTargetNode, m_sNewName, m_sOldName);
    }
    
    public void redo() {
      renameNodeValue(m_nTargetNode, m_sOldName, m_sNewName);
    }
    
    public String getUndoMsg() {
      return "Value of node " + getNodeName(m_nTargetNode) + " changed from " + m_sNewName + " to " + m_sOldName;
    }
    
    public String getRedoMsg() {
      return "Value of node " + getNodeName(m_nTargetNode) + " changed from " + m_sOldName + " to " + m_sNewName;
    }
  }
  
  class AddValueAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    int m_nTargetNode;
    String m_sValue;
    
    AddValueAction(int nTargetNode, String sValue) {
      super();
      m_nTargetNode = nTargetNode;
      m_sValue = sValue;
    }
    
    public void undo() {
      try {
        delNodeValue(m_nTargetNode, m_sValue);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      addNodeValue(m_nTargetNode, m_sValue);
    }
    
    public String getUndoMsg() {
      return "Value " + m_sValue + " removed from node " + getNodeName(m_nTargetNode);
    }
    
    public String getRedoMsg() {
      return "Value " + m_sValue + " added to node " + getNodeName(m_nTargetNode);
    }
  }
  

  class DelValueAction
    extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    
    int m_nTargetNode;
    String m_sValue;
    Estimator[] m_CPT;
    FastVector m_children;
    Estimator[][] m_childAtts;
    Attribute m_att;
    
    DelValueAction(int nTargetNode, String sValue)
    {
      super();
      try {
        m_nTargetNode = nTargetNode;
        m_sValue = sValue;
        m_att = m_Instances.attribute(nTargetNode);
        so = new SerializedObject(m_Distributions[nTargetNode]);
        m_CPT = ((Estimator[])so.getObject());
        
        m_children = new FastVector();
        for (int iNode = 0; iNode < getNrOfNodes(); iNode++) {
          if (m_ParentSets[iNode].contains(nTargetNode)) {
            m_children.addElement(Integer.valueOf(iNode));
          }
        }
        m_childAtts = new Estimator[m_children.size()][];
        for (int iChild = 0; iChild < m_children.size(); iChild++) {
          int nChild = ((Integer)m_children.elementAt(iChild)).intValue();
          m_childAtts[iChild] = m_Distributions[nChild];
        }
      } catch (Exception e) { SerializedObject so;
        e.printStackTrace();
      }
    }
    
    public void undo() {
      try {
        m_Instances.insertAttributeAt(m_att, m_nTargetNode);
        SerializedObject so = new SerializedObject(m_CPT);
        m_Distributions[m_nTargetNode] = ((Estimator[])(Estimator[])so.getObject());
        for (int iChild = 0; iChild < m_children.size(); iChild++) {
          int nChild = ((Integer)m_children.elementAt(iChild)).intValue();
          m_Instances.insertAttributeAt(m_att, m_nTargetNode);
          so = new SerializedObject(m_childAtts[iChild]);
          m_Distributions[nChild] = ((Estimator[])(Estimator[])so.getObject());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        delNodeValue(m_nTargetNode, m_sValue);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Value " + m_sValue + " added to node " + getNodeName(m_nTargetNode);
    }
    
    public String getRedoMsg() {
      return "Value " + m_sValue + " removed from node " + getNodeName(m_nTargetNode);
    }
  }
  
  class alignAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    FastVector m_nodes;
    FastVector m_posX;
    FastVector m_posY;
    
    alignAction(FastVector nodes)
    {
      super();
      m_nodes = new FastVector(nodes.size());
      m_posX = new FastVector(nodes.size());
      m_posY = new FastVector(nodes.size());
      for (int iNode = 0; iNode < nodes.size(); iNode++) {
        int nNode = ((Integer)nodes.elementAt(iNode)).intValue();
        m_nodes.addElement(Integer.valueOf(nNode));
        m_posX.addElement(Integer.valueOf(getPositionX(nNode)));
        m_posY.addElement(Integer.valueOf(getPositionY(nNode)));
      }
    }
    
    public void undo() {
      try {
        for (int iNode = 0; iNode < m_nodes.size(); iNode++) {
          int nNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
          setPosition(nNode, ((Integer)m_posX.elementAt(iNode)).intValue(), ((Integer)m_posY.elementAt(iNode)).intValue());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class alignLeftAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public alignLeftAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        alignLeft(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from aliging nodes to the left.";
    }
    
    public String getRedoMsg() {
      return "Aligning " + m_nodes.size() + " nodes to the left.";
    }
  }
  
  class alignRightAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public alignRightAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        alignRight(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from aliging nodes to the right.";
    }
    
    public String getRedoMsg() {
      return "Aligning " + m_nodes.size() + " nodes to the right.";
    }
  }
  
  class alignTopAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public alignTopAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        alignTop(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from aliging nodes to the top.";
    }
    
    public String getRedoMsg() {
      return "Aligning " + m_nodes.size() + " nodes to the top.";
    }
  }
  
  class alignBottomAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public alignBottomAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        alignBottom(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from aliging nodes to the bottom.";
    }
    
    public String getRedoMsg() {
      return "Aligning " + m_nodes.size() + " nodes to the bottom.";
    }
  }
  
  class centerHorizontalAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public centerHorizontalAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        centerHorizontal(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from centering horizontally.";
    }
    
    public String getRedoMsg() {
      return "Centering " + m_nodes.size() + " nodes horizontally.";
    }
  }
  
  class centerVerticalAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public centerVerticalAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        centerVertical(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from centering vertically.";
    }
    
    public String getRedoMsg() {
      return "Centering " + m_nodes.size() + " nodes vertically.";
    }
  }
  
  class spaceHorizontalAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public spaceHorizontalAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        spaceHorizontal(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from spaceing horizontally.";
    }
    
    public String getRedoMsg() {
      return "spaceing " + m_nodes.size() + " nodes horizontally.";
    }
  }
  
  class spaceVerticalAction extends EditableBayesNet.alignAction {
    static final long serialVersionUID = 1L;
    
    public spaceVerticalAction(FastVector nodes) {
      super(nodes);
    }
    
    public void redo() {
      try {
        spaceVertical(m_nodes);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public String getUndoMsg() {
      return "Returning " + m_nodes.size() + " from spaceng vertically.";
    }
    
    public String getRedoMsg() {
      return "Spaceng " + m_nodes.size() + " nodes vertically.";
    }
  }
  

  class SetPositionAction
    extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    int m_nTargetNode;
    int m_nX;
    int m_nY;
    int m_nX2;
    int m_nY2;
    
    SetPositionAction(int nTargetNode, int nX, int nY)
    {
      super();
      m_nTargetNode = nTargetNode;
      m_nX2 = nX;
      m_nY2 = nY;
      m_nX = getPositionX(nTargetNode);
      m_nY = getPositionY(nTargetNode);
    }
    
    public void undo() {
      setPosition(m_nTargetNode, m_nX, m_nY);
    }
    
    public void redo() {
      setPosition(m_nTargetNode, m_nX2, m_nY2);
    }
    
    public void setUndoPosition(int nX, int nY) {
      m_nX2 = nX;
      m_nY2 = nY;
    }
  }
  
  class SetGroupPositionAction extends EditableBayesNet.UndoAction {
    static final long serialVersionUID = 1L;
    FastVector m_nodes;
    int m_dX;
    int m_dY;
    
    SetGroupPositionAction(FastVector nodes, int dX, int dY) {
      super();
      m_nodes = new FastVector(nodes.size());
      for (int iNode = 0; iNode < nodes.size(); iNode++) {
        m_nodes.addElement(nodes.elementAt(iNode));
      }
      m_dX = dX;
      m_dY = dY;
    }
    
    public void undo() {
      for (int iNode = 0; iNode < m_nodes.size(); iNode++) {
        int nNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
        setPosition(nNode, getPositionX(nNode) - m_dX, getPositionY(nNode) - m_dY);
      }
    }
    
    public void redo() {
      for (int iNode = 0; iNode < m_nodes.size(); iNode++) {
        int nNode = ((Integer)m_nodes.elementAt(iNode)).intValue();
        setPosition(nNode, getPositionX(nNode) + m_dX, getPositionY(nNode) + m_dY);
      }
    }
    
    public void setUndoPosition(int dX, int dY) { m_dX += dX;
      m_dY += dY;
    }
  }
  
  class LayoutGraphAction extends EditableBayesNet.UndoAction {
    static final long serialVersionUID = 1L;
    FastVector m_nPosX;
    FastVector m_nPosY;
    FastVector m_nPosX2;
    FastVector m_nPosY2;
    
    LayoutGraphAction(FastVector nPosX, FastVector nPosY) {
      super();
      m_nPosX = new FastVector(nPosX.size());
      m_nPosY = new FastVector(nPosX.size());
      m_nPosX2 = new FastVector(nPosX.size());
      m_nPosY2 = new FastVector(nPosX.size());
      for (int iNode = 0; iNode < nPosX.size(); iNode++) {
        m_nPosX.addElement(m_nPositionX.elementAt(iNode));
        m_nPosY.addElement(m_nPositionY.elementAt(iNode));
        m_nPosX2.addElement(nPosX.elementAt(iNode));
        m_nPosY2.addElement(nPosY.elementAt(iNode));
      }
    }
    
    public void undo() {
      for (int iNode = 0; iNode < m_nPosX.size(); iNode++) {
        setPosition(iNode, ((Integer)m_nPosX.elementAt(iNode)).intValue(), ((Integer)m_nPosY.elementAt(iNode)).intValue());
      }
    }
    
    public void redo() {
      for (int iNode = 0; iNode < m_nPosX.size(); iNode++) {
        setPosition(iNode, ((Integer)m_nPosX2.elementAt(iNode)).intValue(), ((Integer)m_nPosY2.elementAt(iNode)).intValue());
      }
    }
  }
  
  class PasteAction extends EditableBayesNet.UndoAction
  {
    static final long serialVersionUID = 1L;
    int m_nBase;
    String m_sXML;
    
    PasteAction(String sXML, int nBase) {
      super();
      m_sXML = sXML;
      m_nBase = nBase;
    }
    
    public void undo() {
      try {
        int iNode = getNrOfNodes() - 1;
        while (iNode >= m_nBase) {
          deleteNode(iNode);
          iNode--;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    public void redo() {
      try {
        paste(m_sXML, 1);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7836 $");
  }
  
  public static void main(String[] args) {}
}
