package weka.classifiers.rules;

import java.io.Serializable;
import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
































































public class Prism
  extends Classifier
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 1310258880025902106L;
  private PrismRule m_rules;
  
  public Prism() {}
  
  public String globalInfo()
  {
    return "Class for building and using a PRISM rule set for classification. Can only deal with nominal attributes. Can't deal with missing values. Doesn't do any pruning.\n\nFor more information, see \n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "J. Cendrowska");
    result.setValue(TechnicalInformation.Field.YEAR, "1987");
    result.setValue(TechnicalInformation.Field.TITLE, "PRISM: An algorithm for inducing modular rules");
    result.setValue(TechnicalInformation.Field.JOURNAL, "International Journal of Man-Machine Studies");
    result.setValue(TechnicalInformation.Field.VOLUME, "27");
    result.setValue(TechnicalInformation.Field.NUMBER, "4");
    result.setValue(TechnicalInformation.Field.PAGES, "349-370");
    
    return result;
  }
  



  private class PrismRule
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 4248784350656508583L;
    


    private int m_classification;
    


    private Instances m_instances;
    


    private Prism.Test m_test;
    


    private int m_errors;
    


    private PrismRule m_next;
    


    public PrismRule(Instances data, int cl)
      throws Exception
    {
      m_instances = data;
      m_classification = cl;
      m_test = null;
      m_next = null;
      m_errors = 0;
      Enumeration enu = data.enumerateInstances();
      while (enu.hasMoreElements()) {
        if ((int)((Instance)enu.nextElement()).classValue() != cl) {
          m_errors += 1;
        }
      }
      m_instances = new Instances(m_instances, 0);
    }
    






    public int resultRule(Instance inst)
    {
      if ((m_test == null) || (m_test.satisfies(inst))) {
        return m_classification;
      }
      return -1;
    }
    







    public int resultRules(Instance inst)
    {
      if (resultRule(inst) != -1)
        return m_classification;
      if (m_next != null) {
        return m_next.resultRules(inst);
      }
      return -1;
    }
    







    public Instances coveredBy(Instances data)
    {
      Instances r = new Instances(data, data.numInstances());
      Enumeration enu = data.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance i = (Instance)enu.nextElement();
        if (resultRule(i) != -1) {
          r.add(i);
        }
      }
      r.compactify();
      return r;
    }
    






    public Instances notCoveredBy(Instances data)
    {
      Instances r = new Instances(data, data.numInstances());
      Enumeration enu = data.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance i = (Instance)enu.nextElement();
        if (resultRule(i) == -1) {
          r.add(i);
        }
      }
      r.compactify();
      return r;
    }
    




    public String toString()
    {
      try
      {
        StringBuffer text = new StringBuffer();
        if (m_test != null) {
          text.append("If ");
          for (Prism.Test t = m_test; t != null; t = m_next) {
            if (m_attr == -1) {
              text.append("?");
            } else {
              text.append(m_instances.attribute(m_attr).name() + " = " + m_instances.attribute(m_attr).value(m_val));
            }
            
            if (m_next != null) {
              text.append("\n   and ");
            }
          }
          text.append(" then ");
        }
        text.append(m_instances.classAttribute().value(m_classification) + "\n");
        if (m_next != null) {
          text.append(m_next.toString());
        }
        return text.toString();
      } catch (Exception e) {}
      return "Can't print Prism classifier!";
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  



  private class Test
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -8925333011350280799L;
    


    private int m_attr = -1;
    

    private int m_val;
    

    private Test m_next = null;
    


    private Test() {}
    


    private boolean satisfies(Instance inst)
    {
      if ((int)inst.value(m_attr) == m_val) {
        if (m_next == null) {
          return true;
        }
        return m_next.satisfies(inst);
      }
      
      return false;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5529 $");
    }
  }
  









  public double classifyInstance(Instance inst)
  {
    int result = m_rules.resultRules(inst);
    if (result == -1) {
      return Instance.missingValue();
    }
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    PrismRule rule = null;
    Test test = null;Test oldTest = null;
    



    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    for (int cl = 0; cl < data.numClasses(); cl++) {
      Instances E = data;
      while (contains(E, cl)) {
        rule = addRule(rule, new PrismRule(E, cl));
        Instances ruleE = E;
        while (m_errors != 0) {
          test = new Test(null);
          int attUsed; int bestCovers; int bestCorrect = bestCovers = attUsed = 0;
          

          Enumeration enumAtt = ruleE.enumerateAttributes();
          while (enumAtt.hasMoreElements()) {
            Attribute attr = (Attribute)enumAtt.nextElement();
            if (isMentionedIn(attr, m_test)) {
              attUsed++;
            }
            else {
              int M = attr.numValues();
              int[] covers = new int[M];
              int[] correct = new int[M];
              for (int j = 0; j < M; j++) {
                int tmp185_184 = 0;correct[j] = tmp185_184;covers[j] = tmp185_184;
              }
              

              Enumeration enu = ruleE.enumerateInstances();
              while (enu.hasMoreElements()) {
                Instance i = (Instance)enu.nextElement();
                covers[((int)i.value(attr))] += 1;
                if ((int)i.classValue() == cl) {
                  correct[((int)i.value(attr))] += 1;
                }
              }
              

              for (int val = 0; val < M; val++) {
                int diff = correct[val] * bestCovers - bestCorrect * covers[val];
                

                if ((m_attr == -1) || (diff > 0) || ((diff == 0) && (correct[val] > bestCorrect)))
                {


                  bestCorrect = correct[val];
                  bestCovers = covers[val];
                  m_attr = attr.index();
                  m_val = val;
                  m_errors = (bestCovers - bestCorrect);
                }
              }
            } }
          if (m_attr != -1)
          {

            oldTest = addTest(rule, oldTest, test);
            ruleE = rule.coveredBy(ruleE);
            if (attUsed == data.numAttributes() - 1)
              break;
          }
        }
        E = rule.notCoveredBy(E);
      }
    }
  }
  







  private PrismRule addRule(PrismRule lastRule, PrismRule newRule)
  {
    if (lastRule == null) {
      m_rules = newRule;
    } else {
      m_next = newRule;
    }
    return newRule;
  }
  








  private Test addTest(PrismRule rule, Test lastTest, Test newTest)
  {
    if (m_test == null) {
      m_test = newTest;
    } else {
      m_next = newTest;
    }
    return newTest;
  }
  







  private static boolean contains(Instances E, int C)
    throws Exception
  {
    Enumeration enu = E.enumerateInstances();
    while (enu.hasMoreElements()) {
      if ((int)((Instance)enu.nextElement()).classValue() == C) {
        return true;
      }
    }
    return false;
  }
  







  private static boolean isMentionedIn(Attribute attr, Test t)
  {
    if (t == null) {
      return false;
    }
    if (m_attr == attr.index()) {
      return true;
    }
    return isMentionedIn(attr, m_next);
  }
  





  public String toString()
  {
    if (m_rules == null) {
      return "Prism: No model built yet.";
    }
    return "Prism rules\n----------\n" + m_rules.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5529 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new Prism(), args);
  }
}
