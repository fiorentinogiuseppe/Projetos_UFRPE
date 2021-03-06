package weka.attributeSelection;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;




















































































public class RankSearch
  extends ASSearch
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -7992268736874353755L;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private BitSet m_best_group;
  private ASEvaluation m_ASEval;
  private ASEvaluation m_SubsetEval;
  private Instances m_Instances;
  private double m_bestMerit;
  private int[] m_Ranking;
  protected int m_add = 1;
  

  protected int m_startPoint = 0;
  




  public String globalInfo()
  {
    return "RankSearch : \n\nUses an attribute/subset evaluator to rank all attributes. If a subset evaluator is specified, then a forward selection search is used to generate a ranked list. From the ranked list of attributes, subsets of increasing size are evaluated, ie. The best attribute, the best attribute plus the next best attribute, etc.... The best attribute set is reported. RankSearch is linear in the number of attributes if a simple attribute evaluator is used such as GainRatioAttributeEval. For more information see:\n\n" + getTechnicalInformation().toString();
  }
  

















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Mark Hall and Geoffrey Holmes");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.TITLE, "Benchmarking attribute selection techniques for discrete class data mining");
    
    result.setValue(TechnicalInformation.Field.JOURNAL, "IEEE Transactions on Knowledge and Data Engineering");
    result.setValue(TechnicalInformation.Field.VOLUME, "15");
    result.setValue(TechnicalInformation.Field.NUMBER, "6");
    result.setValue(TechnicalInformation.Field.PAGES, "1437-1447");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "IEEE Computer Society");
    
    return result;
  }
  


  public RankSearch()
  {
    resetOptions();
  }
  




  public String attributeEvaluatorTipText()
  {
    return "Attribute evaluator to use for generating a ranking.";
  }
  



  public void setAttributeEvaluator(ASEvaluation newEvaluator)
  {
    m_ASEval = newEvaluator;
  }
  



  public ASEvaluation getAttributeEvaluator()
  {
    return m_ASEval;
  }
  




  public String stepSizeTipText()
  {
    return "Add this many attributes from the ranking in each iteration.";
  }
  




  public void setStepSize(int ss)
  {
    if (ss > 0) {
      m_add = ss;
    }
  }
  




  public int getStepSize()
  {
    return m_add;
  }
  




  public String startPointTipText()
  {
    return "Start evaluating from this point in the ranking.";
  }
  



  public void setStartPoint(int sp)
  {
    if (sp >= 0) {
      m_startPoint = sp;
    }
  }
  



  public int getStartPoint()
  {
    return m_startPoint;
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tclass name of attribute evaluator to use for ranking. Place any\n\tevaluator options LAST on the command line following a \"--\".\n\teg.:\n\t\t-A weka.attributeSelection.GainRatioAttributeEval ... -- -M\n\t(default: weka.attributeSelection.GainRatioAttributeEval)", "A", 1, "-A <attribute evaluator>"));
    






    newVector.addElement(new Option("\tnumber of attributes to be added from the\n\tranking in each iteration (default = 1).", "S", 1, "-S <step size>"));
    



    newVector.addElement(new Option("\tpoint in the ranking to start evaluating from. \n\t(default = 0, ie. the head of the ranking).", "R", 1, "-R <start point>"));
    



    if ((m_ASEval != null) && ((m_ASEval instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to evaluator " + m_ASEval.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_ASEval).listOptions();
      
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    
    return newVector.elements();
  }
  


































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
    String optionString = Utils.getOption('S', options);
    if (optionString.length() != 0) {
      setStepSize(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setStartPoint(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('A', options);
    if (optionString.length() == 0)
      optionString = GainRatioAttributeEval.class.getName();
    setAttributeEvaluator(ASEvaluation.forName(optionString, Utils.partitionOptions(options)));
  }
  





  public String[] getOptions()
  {
    String[] evaluatorOptions = new String[0];
    
    if ((m_ASEval != null) && ((m_ASEval instanceof OptionHandler)))
    {
      evaluatorOptions = ((OptionHandler)m_ASEval).getOptions();
    }
    
    String[] options = new String[8 + evaluatorOptions.length];
    int current = 0;
    
    options[(current++)] = "-S";options[(current++)] = ("" + getStepSize());
    
    options[(current++)] = "-R";options[(current++)] = ("" + getStartPoint());
    
    if (getAttributeEvaluator() != null) {
      options[(current++)] = "-A";
      options[(current++)] = getAttributeEvaluator().getClass().getName();
    }
    
    if (evaluatorOptions.length > 0) {
      options[(current++)] = "--";
      System.arraycopy(evaluatorOptions, 0, options, current, evaluatorOptions.length);
      
      current += evaluatorOptions.length;
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  


  protected void resetOptions()
  {
    m_ASEval = new GainRatioAttributeEval();
    m_Ranking = null;
  }
  









  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    double best_merit = -1.7976931348623157E308D;
    
    BitSet best_group = null;
    
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator!");
    }
    


    m_SubsetEval = ASEval;
    m_Instances = data;
    m_numAttribs = m_Instances.numAttributes();
    




    if (((m_ASEval instanceof UnsupervisedAttributeEvaluator)) || ((m_ASEval instanceof UnsupervisedSubsetEvaluator)))
    {
      m_hasClass = false;

    }
    else
    {

      m_hasClass = true;
      m_classIndex = m_Instances.classIndex();
    }
    
    if ((m_ASEval instanceof AttributeEvaluator))
    {
      Ranker ranker = new Ranker();
      m_ASEval.buildEvaluator(m_Instances);
      if ((m_ASEval instanceof AttributeTransformer))
      {
        m_Instances = ((AttributeTransformer)m_ASEval).transformedData(m_Instances);
        
        m_SubsetEval.buildEvaluator(m_Instances);
      }
      m_Ranking = ranker.search(m_ASEval, m_Instances);
    } else {
      GreedyStepwise fs = new GreedyStepwise();
      
      fs.setGenerateRanking(true);
      m_ASEval.buildEvaluator(m_Instances);
      fs.search(m_ASEval, m_Instances);
      double[][] rankres = fs.rankedAttributes();
      m_Ranking = new int[rankres.length];
      for (int i = 0; i < rankres.length; i++) {
        m_Ranking[i] = ((int)rankres[i][0]);
      }
    }
    

    for (int i = m_startPoint; i < m_Ranking.length; i += m_add) {
      BitSet temp_group = new BitSet(m_numAttribs);
      for (int j = 0; j <= i; j++) {
        temp_group.set(m_Ranking[j]);
      }
      double temp_merit = ((SubsetEvaluator)m_SubsetEval).evaluateSubset(temp_group);
      
      if (temp_merit > best_merit) {
        best_merit = temp_merit;
        best_group = temp_group;
      }
    }
    m_bestMerit = best_merit;
    return attributeList(best_group);
  }
  




  private int[] attributeList(BitSet group)
  {
    int count = 0;
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        count++;
      }
    }
    
    int[] list = new int[count];
    count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (group.get(i)) {
        list[(count++)] = i;
      }
    }
    
    return list;
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    text.append("\tRankSearch :\n");
    text.append("\tAttribute evaluator : " + getAttributeEvaluator().getClass().getName() + " ");
    
    if ((m_ASEval instanceof OptionHandler)) {
      String[] evaluatorOptions = new String[0];
      evaluatorOptions = ((OptionHandler)m_ASEval).getOptions();
      for (int i = 0; i < evaluatorOptions.length; i++) {
        text.append(evaluatorOptions[i] + ' ');
      }
    }
    text.append("\n");
    text.append("\tAttribute ranking : \n");
    int rlength = (int)(Math.log(m_Ranking.length) / Math.log(10.0D) + 1.0D);
    for (int i = 0; i < m_Ranking.length; i++) {
      text.append("\t " + Utils.doubleToString(m_Ranking[i] + 1, rlength, 0) + " " + m_Instances.attribute(m_Ranking[i]).name() + '\n');
    }
    

    text.append("\tMerit of best subset found : ");
    int fieldwidth = 3;
    double precision = m_bestMerit - (int)m_bestMerit;
    if (Math.abs(m_bestMerit) > 0.0D) {
      fieldwidth = (int)Math.abs(Math.log(Math.abs(m_bestMerit)) / Math.log(10.0D)) + 2;
    }
    if (Math.abs(precision) > 0.0D) {
      precision = Math.abs(Math.log(Math.abs(precision)) / Math.log(10.0D)) + 3.0D;
    } else {
      precision = 2.0D;
    }
    
    text.append(Utils.doubleToString(Math.abs(m_bestMerit), fieldwidth + (int)precision, (int)precision) + "\n");
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6253 $");
  }
}
