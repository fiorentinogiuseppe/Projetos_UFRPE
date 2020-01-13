package weka.classifiers.rules.part;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;



































public class MakeDecList
  implements Serializable, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = -1427481323245079123L;
  private Vector theRules;
  private double CF = 0.25D;
  

  private int minNumObj;
  

  private ModelSelection toSelectModeL;
  

  private int numSetS = 3;
  

  private boolean reducedErrorPruning = false;
  

  private boolean unpruned = false;
  

  private int m_seed = 1;
  




  public MakeDecList(ModelSelection toSelectLocModel, int minNum)
  {
    toSelectModeL = toSelectLocModel;
    reducedErrorPruning = false;
    unpruned = true;
    minNumObj = minNum;
  }
  




  public MakeDecList(ModelSelection toSelectLocModel, double cf, int minNum)
  {
    toSelectModeL = toSelectLocModel;
    CF = cf;
    reducedErrorPruning = false;
    unpruned = false;
    minNumObj = minNum;
  }
  




  public MakeDecList(ModelSelection toSelectLocModel, int num, int minNum, int seed)
  {
    toSelectModeL = toSelectLocModel;
    numSetS = num;
    reducedErrorPruning = true;
    unpruned = false;
    minNumObj = minNum;
    m_seed = seed;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    




    int numRules = 0;
    
    theRules = new Vector();
    Instances oldPruneData; Instances oldGrowData; Instances oldPruneData; if ((reducedErrorPruning) && (!unpruned)) {
      Random random = new Random(m_seed);
      data.randomize(random);
      data.stratify(numSetS);
      Instances oldGrowData = data.trainCV(numSetS, numSetS - 1, random);
      oldPruneData = data.testCV(numSetS, numSetS - 1);
    } else {
      oldGrowData = data;
      oldPruneData = null;
    }
    
    while (Utils.gr(oldGrowData.numInstances(), 0.0D))
    {
      ClassifierDecList currentRule;
      if (unpruned) {
        ClassifierDecList currentRule = new ClassifierDecList(toSelectModeL, minNumObj);
        
        currentRule.buildRule(oldGrowData);
      } else if (reducedErrorPruning) {
        ClassifierDecList currentRule = new PruneableDecList(toSelectModeL, minNumObj);
        
        ((PruneableDecList)currentRule).buildRule(oldGrowData, oldPruneData);
      }
      else {
        currentRule = new C45PruneableDecList(toSelectModeL, CF, minNumObj);
        
        ((C45PruneableDecList)currentRule).buildRule(oldGrowData);
      }
      numRules++;
      

      Instances newGrowData = new Instances(oldGrowData, oldGrowData.numInstances());
      
      Enumeration enu = oldGrowData.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance instance = (Instance)enu.nextElement();
        double currentWeight = currentRule.weight(instance);
        if (Utils.sm(currentWeight, 1.0D)) {
          instance.setWeight(instance.weight() * (1.0D - currentWeight));
          newGrowData.add(instance);
        }
      }
      newGrowData.compactify();
      oldGrowData = newGrowData;
      

      if ((reducedErrorPruning) && (!unpruned)) {
        Instances newPruneData = new Instances(oldPruneData, oldPruneData.numInstances());
        
        enu = oldPruneData.enumerateInstances();
        while (enu.hasMoreElements()) {
          Instance instance = (Instance)enu.nextElement();
          double currentWeight = currentRule.weight(instance);
          if (Utils.sm(currentWeight, 1.0D)) {
            instance.setWeight(instance.weight() * (1.0D - currentWeight));
            newPruneData.add(instance);
          }
        }
        newPruneData.compactify();
        oldPruneData = newPruneData;
      }
      theRules.addElement(currentRule);
    }
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < theRules.size(); i++)
      text.append((ClassifierDecList)theRules.elementAt(i) + "\n");
    text.append("Number of Rules  : \t" + theRules.size() + "\n");
    
    return text.toString();
  }
  





  public double classifyInstance(Instance instance)
    throws Exception
  {
    double maxProb = -1.0D;
    
    int maxIndex = 0;
    
    double[] sumProbs = distributionForInstance(instance);
    for (int j = 0; j < sumProbs.length; j++) {
      if (Utils.gr(sumProbs[j], maxProb)) {
        maxIndex = j;
        maxProb = sumProbs[j];
      }
    }
    
    return maxIndex;
  }
  





  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] currentProbs = null;
    
    double weight = 1.0D;
    


    double[] sumProbs = new double[instance.numClasses()];
    int i = 0;
    while (Utils.gr(weight, 0.0D)) {
      double currentWeight = ((ClassifierDecList)theRules.elementAt(i)).weight(instance);
      
      if (Utils.gr(currentWeight, 0.0D)) {
        currentProbs = ((ClassifierDecList)theRules.elementAt(i)).distributionForInstance(instance);
        
        for (int j = 0; j < sumProbs.length; j++)
          sumProbs[j] += weight * currentProbs[j];
        weight *= (1.0D - currentWeight);
      }
      i++;
    }
    
    return sumProbs;
  }
  



  public int numRules()
  {
    return theRules.size();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5529 $");
  }
}
