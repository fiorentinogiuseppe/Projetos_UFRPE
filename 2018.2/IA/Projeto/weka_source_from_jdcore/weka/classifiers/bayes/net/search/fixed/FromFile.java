package weka.classifiers.bayes.net.search.fixed;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;












































public class FromFile
  extends SearchAlgorithm
{
  static final long serialVersionUID = 7334358169507619525L;
  String m_sBIFFile = "";
  

  public FromFile() {}
  

  public String globalInfo()
  {
    return "The FromFile reads the structure of a Bayes net from a file in BIFF format.";
  }
  







  public void buildStructure(BayesNet bayesNet, Instances instances)
    throws Exception
  {
    BIFReader bifReader = new BIFReader();
    bifReader.processFile(m_sBIFFile);
    
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      int iBIFAttribute = bifReader.getNode(bayesNet.getNodeName(iAttribute));
      ParentSet bifParentSet = bifReader.getParentSet(iBIFAttribute);
      for (int iBIFParent = 0; iBIFParent < bifParentSet.getNrOfParents(); iBIFParent++) {
        String sParent = bifReader.getNodeName(bifParentSet.getParent(iBIFParent));
        int iParent = 0;
        while ((iParent < instances.numAttributes()) && (!bayesNet.getNodeName(iParent).equals(sParent))) {
          iParent++;
        }
        if (iParent >= instances.numAttributes()) {
          throw new Exception("Could not find attribute " + sParent + " from BIF file in data");
        }
        bayesNet.getParentSet(iAttribute).addParent(iParent, instances);
      }
    }
  }
  




  public void setBIFFile(String sBIFFile)
  {
    m_sBIFFile = sBIFFile;
  }
  



  public String getBIFFile()
  {
    return m_sBIFFile;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tName of file containing network structure in BIF format\n", "B", 1, "-B <BIF File>"));
    

    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      newVector.addElement(en.nextElement());
    }
    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    setBIFFile(Utils.getOption('B', options));
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[2 + superOptions.length];
    int current = 0;
    
    options[(current++)] = "-B";
    options[(current++)] = ("" + getBIFFile());
    

    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
