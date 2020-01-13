package weka.classifiers.bayes.net;

import java.io.File;
import java.io.PrintStream;
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
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.estimators.Estimator;









































































public class BIFReader
  extends BayesNet
  implements TechnicalInformationHandler
{
  protected int[] m_nPositionX;
  protected int[] m_nPositionY;
  private int[] m_order;
  static final long serialVersionUID = -8358864680379881429L;
  String m_sFile;
  
  public String globalInfo()
  {
    return "Builds a description of a Bayes Net classifier stored in XML BIF 0.3 format.\n\nFor more details on XML BIF see:\n\n" + getTechnicalInformation().toString();
  }
  







  public BIFReader processFile(String sFile)
    throws Exception
  {
    m_sFile = sFile;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    Document doc = factory.newDocumentBuilder().parse(new File(sFile));
    doc.normalize();
    
    buildInstances(doc, sFile);
    buildStructure(doc);
    return this;
  }
  
  public BIFReader processString(String sStr) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(sStr)));
    doc.normalize();
    buildInstances(doc, "from-string");
    buildStructure(doc);
    return this;
  }
  








  public String getFileName()
  {
    return m_sFile;
  }
  









  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Fabio Cozman and Marek Druzdzel and Daniel Garcia");
    result.setValue(TechnicalInformation.Field.YEAR, "1998");
    result.setValue(TechnicalInformation.Field.TITLE, "XML BIF version 0.3");
    result.setValue(TechnicalInformation.Field.URL, "http://www-2.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/");
    
    return result;
  }
  







  void buildStructure(Document doc)
    throws Exception
  {
    m_Distributions = new Estimator[m_Instances.numAttributes()][];
    for (int iNode = 0; iNode < m_Instances.numAttributes(); iNode++)
    {
      String sName = m_Instances.attribute(iNode).name();
      Element definition = getDefinition(doc, sName);
      











      FastVector nodelist = getParentNodes(definition);
      for (int iParent = 0; iParent < nodelist.size(); iParent++) {
        Node parentName = ((Node)nodelist.elementAt(iParent)).getFirstChild();
        String sParentName = ((CharacterData)parentName).getData();
        int nParent = getNode(sParentName);
        m_ParentSets[iNode].addParent(nParent, m_Instances);
      }
      
      int nCardinality = m_ParentSets[iNode].getCardinalityOfParents();
      int nValues = m_Instances.attribute(iNode).numValues();
      m_Distributions[iNode] = new Estimator[nCardinality];
      for (int i = 0; i < nCardinality; i++) {
        m_Distributions[iNode][i] = new DiscreteEstimatorBayes(nValues, 0.0D);
      }
      








      String sTable = getTable(definition);
      StringTokenizer st = new StringTokenizer(sTable.toString());
      

      for (int i = 0; i < nCardinality; i++) {
        DiscreteEstimatorBayes d = (DiscreteEstimatorBayes)m_Distributions[iNode][i];
        for (int iValue = 0; iValue < nValues; iValue++) {
          String sWeight = st.nextToken();
          d.addValue(iValue, new Double(sWeight).doubleValue());
        }
      }
    }
  }
  



  public void Sync(BayesNet other)
    throws Exception
  {
    int nAtts = m_Instances.numAttributes();
    if (nAtts != m_Instances.numAttributes()) {
      throw new Exception("Cannot synchronize networks: different number of attributes.");
    }
    m_order = new int[nAtts];
    for (int iNode = 0; iNode < nAtts; iNode++) {
      String sName = other.getNodeName(iNode);
      m_order[getNode(sName)] = iNode;
    }
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
  







  void buildInstances(Document doc, String sName)
    throws Exception
  {
    NodeList nodelist = selectAllNames(doc);
    if (nodelist.getLength() > 0) {
      sName = ((CharacterData)nodelist.item(0).getFirstChild()).getData();
    }
    

    nodelist = selectAllVariables(doc);
    int nNodes = nodelist.getLength();
    
    FastVector attInfo = new FastVector(nNodes);
    

    m_nPositionX = new int[nodelist.getLength()];
    m_nPositionY = new int[nodelist.getLength()];
    

    for (int iNode = 0; iNode < nodelist.getLength(); iNode++)
    {


      FastVector valueslist = selectOutCome(nodelist.item(iNode));
      
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
      

      FastVector nodelist2 = selectName(nodelist.item(iNode));
      if (nodelist2.size() == 0) {
        throw new Exception("No name specified for variable");
      }
      String sNodeName = ((CharacterData)((Node)nodelist2.elementAt(0)).getFirstChild()).getData();
      
      Attribute att = new Attribute(sNodeName, nomStrings);
      attInfo.addElement(att);
      
      valueslist = selectProperty(nodelist.item(iNode));
      nValues = valueslist.size();
      
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
            m_nPositionX[iNode] = Integer.parseInt(sX);
            m_nPositionY[iNode] = Integer.parseInt(sY);
          } catch (NumberFormatException e) {
            System.err.println("Wrong number format in position :(" + sX + "," + sY + ")");
            m_nPositionX[iNode] = 0;
            m_nPositionY[iNode] = 0;
          }
        }
      }
    }
    

    m_Instances = new Instances(sName, attInfo, 100);
    m_Instances.setClassIndex(nNodes - 1);
    setUseADTree(false);
    initStructure();
  }
  










  NodeList selectAllNames(Document doc)
    throws Exception
  {
    NodeList nodelist = doc.getElementsByTagName("NAME");
    return nodelist;
  }
  
  NodeList selectAllVariables(Document doc) throws Exception
  {
    NodeList nodelist = doc.getElementsByTagName("VARIABLE");
    return nodelist;
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
  
  FastVector getParentNodes(Node definition) throws Exception
  {
    FastVector nodelist = selectElements(definition, "GIVEN");
    return nodelist;
  }
  
  String getTable(Node definition) throws Exception
  {
    FastVector nodelist = selectElements(definition, "TABLE");
    String sTable = getContent((Element)nodelist.elementAt(0));
    sTable = sTable.replaceAll("\\n", " ");
    return sTable;
  }
  
  FastVector selectOutCome(Node item) throws Exception
  {
    FastVector nodelist = selectElements(item, "OUTCOME");
    return nodelist;
  }
  
  FastVector selectName(Node item) throws Exception
  {
    FastVector nodelist = selectElements(item, "NAME");
    return nodelist;
  }
  
  FastVector selectProperty(Node item) throws Exception
  {
    FastVector nodelist = selectElements(item, "PROPERTY");
    return nodelist;
  }
  
  FastVector selectElements(Node item, String sElement) throws Exception {
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
  


  public int missingArcs(BayesNet other)
  {
    try
    {
      Sync(other);
      int nMissing = 0;
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
          int nParent = m_ParentSets[iAttribute].getParent(iParent);
          if ((!other.getParentSet(m_order[iAttribute]).contains(m_order[nParent])) && (!other.getParentSet(m_order[nParent]).contains(m_order[iAttribute]))) {
            nMissing++;
          }
        }
      }
      return nMissing;
    } catch (Exception e) {
      System.err.println(e.getMessage()); }
    return 0;
  }
  




  public int extraArcs(BayesNet other)
  {
    try
    {
      Sync(other);
      int nExtra = 0;
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        for (int iParent = 0; iParent < other.getParentSet(m_order[iAttribute]).getNrOfParents(); iParent++) {
          int nParent = m_order[other.getParentSet(m_order[iAttribute]).getParent(iParent)];
          if ((!m_ParentSets[iAttribute].contains(nParent)) && (!m_ParentSets[nParent].contains(iAttribute))) {
            nExtra++;
          }
        }
      }
      return nExtra;
    } catch (Exception e) {
      System.err.println(e.getMessage()); }
    return 0;
  }
  









  public double divergence(BayesNet other)
  {
    try
    {
      Sync(other);
      
      double D = 0.0D;
      int nNodes = m_Instances.numAttributes();
      int[] nCard = new int[nNodes];
      for (int iNode = 0; iNode < nNodes; iNode++) {
        nCard[iNode] = m_Instances.attribute(iNode).numValues();
      }
      
      int[] x = new int[nNodes];
      
      int i = 0;
      while (i < nNodes)
      {
        x[i] += 1;
        while ((i < nNodes) && (x[i] == m_Instances.attribute(i).numValues())) {
          x[i] = 0;
          i++;
          if (i < nNodes) {
            x[i] += 1;
          }
        }
        if (i < nNodes) {
          i = 0;
          
          double P = 1.0D;
          for (int iNode = 0; iNode < nNodes; iNode++) {
            int iCPT = 0;
            for (int iParent = 0; iParent < m_ParentSets[iNode].getNrOfParents(); iParent++) {
              int nParent = m_ParentSets[iNode].getParent(iParent);
              iCPT = iCPT * nCard[nParent] + x[nParent];
            }
            P *= m_Distributions[iNode][iCPT].getProbability(x[iNode]);
          }
          
          double Q = 1.0D;
          for (int iNode = 0; iNode < nNodes; iNode++) {
            int iCPT = 0;
            for (int iParent = 0; iParent < other.getParentSet(m_order[iNode]).getNrOfParents(); iParent++) {
              int nParent = m_order[other.getParentSet(m_order[iNode]).getParent(iParent)];
              iCPT = iCPT * nCard[nParent] + x[nParent];
            }
            Q *= m_Distributions[m_order[iNode]][iCPT].getProbability(x[iNode]);
          }
          

          if ((P > 0.0D) && (Q > 0.0D)) {
            D += P * Math.log(Q / P);
          }
        }
      }
      return D;
    } catch (Exception e) {
      System.err.println(e.getMessage()); }
    return 0.0D;
  }
  



  public int reversedArcs(BayesNet other)
  {
    try
    {
      Sync(other);
      int nReversed = 0;
      for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
        for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
          int nParent = m_ParentSets[iAttribute].getParent(iParent);
          if ((!other.getParentSet(m_order[iAttribute]).contains(m_order[nParent])) && (other.getParentSet(m_order[nParent]).contains(m_order[iAttribute]))) {
            nReversed++;
          }
        }
      }
      return nReversed;
    } catch (Exception e) {
      System.err.println(e.getMessage()); }
    return 0;
  }
  




  public int getNode(String sNodeName)
    throws Exception
  {
    int iNode = 0;
    while (iNode < m_Instances.numAttributes()) {
      if (m_Instances.attribute(iNode).name().equals(sNodeName)) {
        return iNode;
      }
      iNode++;
    }
    throw new Exception("Could not find node [[" + sNodeName + "]]");
  }
  




  public BIFReader() {}
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.15 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      BIFReader br = new BIFReader();
      br.processFile(args[0]);
      System.out.println(br.toString());
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }
}
