package weka.associations.tertius;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;








































































public class Rule
  implements Serializable, Cloneable, RevisionHandler
{
  private static final long serialVersionUID = -7763378359090435505L;
  private Body m_body;
  private Head m_head;
  private boolean m_repeatPredicate;
  private int m_maxLiterals;
  private boolean m_negBody;
  private boolean m_negHead;
  private boolean m_classRule;
  private boolean m_singleHead;
  private int m_numInstances;
  private ArrayList m_counterInstances;
  private int m_counter;
  private double m_confirmation;
  private double m_optimistic;
  
  public Rule(boolean repeatPredicate, int maxLiterals, boolean negBody, boolean negHead, boolean classRule, boolean horn)
  {
    m_body = new Body();
    m_head = new Head();
    m_repeatPredicate = repeatPredicate;
    m_maxLiterals = maxLiterals;
    m_negBody = ((negBody) && (!horn));
    m_negHead = ((negHead) && (!horn));
    m_classRule = classRule;
    m_singleHead = ((classRule) || (horn));
  }
  















  public Rule(Instances instances, boolean repeatPredicate, int maxLiterals, boolean negBody, boolean negHead, boolean classRule, boolean horn)
  {
    m_body = new Body(instances);
    m_head = new Head(instances);
    m_repeatPredicate = repeatPredicate;
    m_maxLiterals = maxLiterals;
    m_negBody = ((negBody) && (!horn));
    m_negHead = ((negHead) && (!horn));
    m_classRule = classRule;
    m_singleHead = ((classRule) || (horn));
    m_numInstances = instances.numInstances();
    m_counterInstances = new ArrayList(m_numInstances);
    Enumeration enu = instances.enumerateInstances();
    while (enu.hasMoreElements()) {
      m_counterInstances.add(enu.nextElement());
    }
  }
  






  public Object clone()
  {
    Object result = null;
    try {
      result = super.clone();
      
      m_body = ((Body)m_body.clone());
      m_head = ((Head)m_head.clone());
      
      if (m_counterInstances != null) {
        m_counterInstances = ((ArrayList)m_counterInstances.clone());
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(0);
    }
    return result;
  }
  






  public boolean counterInstance(Instance instance)
  {
    return (m_body.counterInstance(instance)) && (m_head.counterInstance(instance));
  }
  







  public void upDate(Instances instances)
  {
    Enumeration enu = instances.enumerateInstances();
    m_numInstances = instances.numInstances();
    m_counter = 0;
    while (enu.hasMoreElements()) {
      if (counterInstance((Instance)enu.nextElement())) {
        m_counter += 1;
      }
    }
    m_head.upDate(instances);
    m_body.upDate(instances);
  }
  





  public double getConfirmation()
  {
    return m_confirmation;
  }
  






  public double getOptimistic()
  {
    return m_optimistic;
  }
  







  public double getExpectedNumber()
  {
    return m_body.getCounterInstancesNumber() * m_head.getCounterInstancesNumber() / m_numInstances;
  }
  







  public double getExpectedFrequency()
  {
    return getExpectedNumber() / m_numInstances;
  }
  





  public int getObservedNumber()
  {
    if (m_counterInstances != null) {
      return m_counterInstances.size();
    }
    return m_counter;
  }
  






  public double getObservedFrequency()
  {
    return getObservedNumber() / m_numInstances;
  }
  





  public double getTPRate()
  {
    int tp = m_body.getCounterInstancesNumber() - getObservedNumber();
    int fn = m_numInstances - m_head.getCounterInstancesNumber() - tp;
    return tp / (tp + fn);
  }
  





  public double getFPRate()
  {
    int fp = getObservedNumber();
    int tn = m_head.getCounterInstancesNumber() - fp;
    return fp / (fp + tn);
  }
  



  public void calculateConfirmation()
  {
    double expected = getExpectedFrequency();
    double observed = getObservedFrequency();
    if ((expected == 0.0D) || (expected == 1.0D)) {
      m_confirmation = 0.0D;
    } else {
      m_confirmation = ((expected - observed) / (Math.sqrt(expected) - expected));
    }
  }
  



  public void calculateOptimistic()
  {
    int counterInstances = getObservedNumber();
    int body = m_body.getCounterInstancesNumber();
    int notHead = m_head.getCounterInstancesNumber();
    int n = m_numInstances;
    double expectedOptimistic;
    double expectedOptimistic;
    if (counterInstances <= body - notHead) {
      expectedOptimistic = notHead * (body - counterInstances) / (n * n);
    } else { double expectedOptimistic;
      if (counterInstances <= notHead - body) {
        expectedOptimistic = body * (notHead - counterInstances) / (n * n);
      }
      else {
        expectedOptimistic = (notHead + body - counterInstances) * (notHead + body - counterInstances) / (4 * n * n);
      }
    }
    
    if ((expectedOptimistic == 0.0D) || (expectedOptimistic == 1.0D)) {
      m_optimistic = 0.0D;
    } else {
      m_optimistic = (expectedOptimistic / (Math.sqrt(expectedOptimistic) - expectedOptimistic));
    }
  }
  






  public boolean isEmpty()
  {
    return (m_head.isEmpty()) && (m_body.isEmpty());
  }
  





  public int numLiterals()
  {
    return m_body.numLiterals() + m_head.numLiterals();
  }
  







  private Rule addTermToBody(Literal newLit)
  {
    if (((!m_negBody) && (newLit.negative())) || ((m_classRule) && (newLit.getPredicate().isClass())) || (((newLit instanceof IndividualLiteral)) && (((IndividualLiteral)newLit).getType() - m_body.getType() > 1) && (((IndividualLiteral)newLit).getType() - m_head.getType() > 1)))
    {





      return null;
    }
    Rule result = (Rule)clone();
    m_body.addElement(newLit);
    
    if (m_counterInstances != null) {
      for (int i = m_counterInstances.size() - 1; i >= 0; i--) {
        Instance current = (Instance)m_counterInstances.get(i);
        if (!m_body.canKeep(current, newLit)) {
          m_counterInstances.remove(i);
        }
      }
    }
    return result;
  }
  







  private Rule addTermToHead(Literal newLit)
  {
    if (((!m_negHead) && (newLit.negative())) || ((m_classRule) && (!newLit.getPredicate().isClass())) || ((m_singleHead) && (!m_head.isEmpty())) || (((newLit instanceof IndividualLiteral)) && (((IndividualLiteral)newLit).getType() != IndividualLiteral.INDIVIDUAL_PROPERTY)))
    {




      return null;
    }
    Rule result = (Rule)clone();
    m_head.addElement(newLit);
    
    if (m_counterInstances != null) {
      for (int i = m_counterInstances.size() - 1; i >= 0; i--) {
        Instance current = (Instance)m_counterInstances.get(i);
        if (!m_head.canKeep(current, newLit)) {
          m_counterInstances.remove(i);
        }
      }
    }
    return result;
  }
  













  private SimpleLinkedList refine(Predicate pred, int firstIndex, int lastIndex, boolean addToBody, boolean addToHead)
  {
    SimpleLinkedList result = new SimpleLinkedList();
    


    for (int i = firstIndex; i < lastIndex; i++) {
      Literal currentLit = pred.getLiteral(i);
      if (addToBody) {
        Rule refinement = addTermToBody(currentLit);
        if (refinement != null) {
          result.add(refinement);
        }
      }
      if (addToHead) {
        Rule refinement = addTermToHead(currentLit);
        if (refinement != null) {
          result.add(refinement);
        }
      }
      Literal negation = currentLit.getNegation();
      if (negation != null) {
        if (addToBody) {
          Rule refinement = addTermToBody(negation);
          if (refinement != null) {
            result.add(refinement);
          }
        }
        if (addToHead) {
          Rule refinement = addTermToHead(negation);
          if (refinement != null) {
            result.add(refinement);
          }
        }
      }
    }
    return result;
  }
  






  public SimpleLinkedList refine(ArrayList predicates)
  {
    SimpleLinkedList result = new SimpleLinkedList();
    



    if (numLiterals() == m_maxLiterals) {
      return result;
    }
    
    if (isEmpty())
    {
      for (int i = 0; i < predicates.size(); i++) {
        Predicate currentPred = (Predicate)predicates.get(i);
        result.addAll(refine(currentPred, 0, currentPred.numLiterals(), true, true));
      }
      
    }
    else if ((m_body.isEmpty()) || (m_head.isEmpty())) { boolean addToHead;
      LiteralSet side;
      boolean addToBody;
      boolean addToHead;
      if (m_body.isEmpty()) {
        LiteralSet side = m_head;
        boolean addToBody = true;
        addToHead = false;
      } else {
        side = m_body;
        addToBody = false;
        addToHead = true;
      }
      Literal last = side.getLastLiteral();
      Predicate currentPred = last.getPredicate();
      if (m_repeatPredicate) {
        result.addAll(refine(currentPred, currentPred.indexOf(last) + 1, currentPred.numLiterals(), addToBody, addToHead));
      }
      


      for (int i = predicates.indexOf(currentPred) + 1; i < predicates.size(); 
          i++) {
        currentPred = (Predicate)predicates.get(i);
        result.addAll(refine(currentPred, 0, currentPred.numLiterals(), addToBody, addToHead));
      }
    }
    else
    {
      Literal lastLitBody = m_body.getLastLiteral();
      Literal lastLitHead = m_head.getLastLiteral();
      Predicate lastPredBody = lastLitBody.getPredicate();
      Predicate lastPredHead = lastLitHead.getPredicate();
      int lastLitBodyIndex = lastPredBody.indexOf(lastLitBody);
      int lastLitHeadIndex = lastPredHead.indexOf(lastLitHead);
      int lastPredBodyIndex = predicates.indexOf(lastPredBody);
      int lastPredHeadIndex = predicates.indexOf(lastPredHead);
      



      boolean addToBody = (m_head.numLiterals() == 1) && ((lastPredBodyIndex < lastPredHeadIndex) || ((lastPredBodyIndex == lastPredHeadIndex) && (lastLitBodyIndex < lastLitHeadIndex)));
      


      boolean addToHead = (m_body.numLiterals() == 1) && ((lastPredHeadIndex < lastPredBodyIndex) || ((lastPredHeadIndex == lastPredBodyIndex) && (lastLitHeadIndex < lastLitBodyIndex)));
      


      if ((addToBody) || (addToHead)) { int superiorLit;
        Predicate inferiorPred;
        int inferiorLit; Predicate superiorPred; int superiorLit; if (addToBody) {
          Predicate inferiorPred = lastPredBody;
          int inferiorLit = lastLitBodyIndex;
          Predicate superiorPred = lastPredHead;
          superiorLit = lastLitHeadIndex;
        } else {
          inferiorPred = lastPredHead;
          inferiorLit = lastLitHeadIndex;
          superiorPred = lastPredBody;
          superiorLit = lastLitBodyIndex;
        }
        if (predicates.indexOf(inferiorPred) < predicates.indexOf(superiorPred))
        {
          if (m_repeatPredicate) {
            result.addAll(refine(inferiorPred, inferiorLit + 1, inferiorPred.numLiterals(), addToBody, addToHead));
          }
          

          for (int j = predicates.indexOf(inferiorPred) + 1; 
              j < predicates.indexOf(superiorPred); j++) {
            Predicate currentPred = (Predicate)predicates.get(j);
            result.addAll(refine(currentPred, 0, currentPred.numLiterals(), addToBody, addToHead));
          }
          

          if (m_repeatPredicate) {
            result.addAll(refine(superiorPred, 0, superiorLit, addToBody, addToHead));

          }
          


        }
        else if (m_repeatPredicate) {
          result.addAll(refine(inferiorPred, inferiorLit + 1, superiorLit, addToBody, addToHead));
        }
      }
      
      int superiorLit;
      Predicate superiorPred;
      int superiorLit;
      if (predicates.indexOf(lastPredBody) > predicates.indexOf(lastPredHead)) {
        Predicate superiorPred = lastPredBody;
        superiorLit = lastPredBody.indexOf(lastLitBody); } else { int superiorLit;
        if (predicates.indexOf(lastPredBody) < predicates.indexOf(lastPredHead))
        {
          Predicate superiorPred = lastPredHead;
          superiorLit = lastPredHead.indexOf(lastLitHead);
        } else {
          superiorPred = lastPredBody;
          int superiorLit; if (lastLitBodyIndex > lastLitHeadIndex) {
            superiorLit = lastPredBody.indexOf(lastLitBody);
          } else
            superiorLit = lastPredHead.indexOf(lastLitHead);
        }
      }
      if (m_repeatPredicate) {
        result.addAll(refine(superiorPred, superiorLit + 1, superiorPred.numLiterals(), true, true));
      }
      

      for (int j = predicates.indexOf(superiorPred) + 1; j < predicates.size(); 
          j++) {
        Predicate currentPred = (Predicate)predicates.get(j);
        result.addAll(refine(currentPred, 0, currentPred.numLiterals(), true, true));
      }
    }
    

    return result;
  }
  






  public boolean subsumes(Rule otherRule)
  {
    if (numLiterals() > otherRule.numLiterals()) {
      return false;
    }
    return (m_body.isIncludedIn(otherRule)) && (m_head.isIncludedIn(otherRule));
  }
  






  public boolean sameClauseAs(Rule otherRule)
  {
    return (numLiterals() == otherRule.numLiterals()) && (subsumes(otherRule));
  }
  







  public boolean equivalentTo(Rule otherRule)
  {
    return (numLiterals() == otherRule.numLiterals()) && (m_head.negationIncludedIn(m_body)) && (m_body.negationIncludedIn(m_head));
  }
  








  public boolean bodyContains(Literal lit)
  {
    return m_body.contains(lit);
  }
  






  public boolean headContains(Literal lit)
  {
    return m_head.contains(lit);
  }
  






  public boolean overFrequencyThreshold(double minFrequency)
  {
    return (m_body.overFrequencyThreshold(minFrequency)) && (m_head.overFrequencyThreshold(minFrequency));
  }
  






  public boolean hasTrueBody()
  {
    return (!m_body.isEmpty()) && (m_body.hasMaxCounterInstances());
  }
  






  public boolean hasFalseHead()
  {
    return (!m_head.isEmpty()) && (m_head.hasMaxCounterInstances());
  }
  







  public String valuesToString()
  {
    StringBuffer text = new StringBuffer();
    DecimalFormat decimalFormat = new DecimalFormat("0.000000");
    text.append(decimalFormat.format(getConfirmation()));
    text.append(" ");
    text.append(decimalFormat.format(getObservedFrequency()));
    return text.toString();
  }
  






  public String rocToString()
  {
    StringBuffer text = new StringBuffer();
    DecimalFormat decimalFormat = new DecimalFormat("0.000000");
    text.append(decimalFormat.format(getConfirmation()));
    text.append(" ");
    text.append(decimalFormat.format(getTPRate()));
    text.append(" ");
    text.append(decimalFormat.format(getFPRate()));
    return text.toString();
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    text.append(m_body.toString());
    text.append(" ==> ");
    text.append(m_head.toString());
    return text.toString();
  }
  



  public static Comparator confirmationComparator = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      Rule r1 = (Rule)o1;
      Rule r2 = (Rule)o2;
      double conf1 = r1.getConfirmation();
      double conf2 = r2.getConfirmation();
      if (conf1 > conf2)
        return -1;
      if (conf1 < conf2) {
        return 1;
      }
      return 0;
    }
  };
  





  public static Comparator observedComparator = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      Rule r1 = (Rule)o1;
      Rule r2 = (Rule)o2;
      double obs1 = r1.getObservedFrequency();
      double obs2 = r2.getObservedFrequency();
      if (obs1 < obs2)
        return -1;
      if (obs1 > obs2) {
        return 1;
      }
      return 0;
    }
  };
  




  public static Comparator optimisticComparator = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      Rule r1 = (Rule)o1;
      Rule r2 = (Rule)o2;
      double opt1 = r1.getOptimistic();
      double opt2 = r2.getOptimistic();
      if (opt1 > opt2)
        return -1;
      if (opt1 < opt2) {
        return 1;
      }
      return 0;
    }
  };
  





  public static Comparator confirmationThenObservedComparator = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      int confirmationComparison = Rule.confirmationComparator.compare(o1, o2);
      if (confirmationComparison != 0) {
        return confirmationComparison;
      }
      return Rule.observedComparator.compare(o1, o2);
    }
  };
  





  public static Comparator optimisticThenObservedComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      int optimisticComparison = Rule.optimisticComparator.compare(o1, o2);
      if (optimisticComparison != 0) {
        return optimisticComparison;
      }
      return Rule.observedComparator.compare(o1, o2);
    }
  };
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
