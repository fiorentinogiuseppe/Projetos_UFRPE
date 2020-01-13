package weka.attributeSelection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Random;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

























































































public class AttributeSelection
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 4170171824147584330L;
  private Instances m_trainInstances;
  private ASEvaluation m_ASEvaluator;
  private ASSearch m_searchMethod;
  private int m_numFolds;
  private StringBuffer m_selectionResults;
  private boolean m_doRank;
  private boolean m_doXval;
  private int m_seed;
  private int m_numToSelect;
  private int[] m_selectedAttributeSet;
  private double[][] m_attributeRanking;
  private AttributeTransformer m_transformer = null;
  


  private Remove m_attributeFilter = null;
  


  private double[][] m_rankResults = (double[][])null;
  private double[] m_subsetResults = null;
  private int m_trials = 0;
  



  public int numberAttributesSelected()
    throws Exception
  {
    int[] att = selectedAttributes();
    return att.length - 1;
  }
  



  public int[] selectedAttributes()
    throws Exception
  {
    if (m_selectedAttributeSet == null) {
      throw new Exception("Attribute selection has not been performed yet!");
    }
    return m_selectedAttributeSet;
  }
  




  public double[][] rankedAttributes()
    throws Exception
  {
    if (m_attributeRanking == null) {
      throw new Exception("Ranking has not been performed");
    }
    return m_attributeRanking;
  }
  



  public void setEvaluator(ASEvaluation evaluator)
  {
    m_ASEvaluator = evaluator;
  }
  



  public void setSearch(ASSearch search)
  {
    m_searchMethod = search;
    
    if ((m_searchMethod instanceof RankedOutputSearch)) {
      setRanking(((RankedOutputSearch)m_searchMethod).getGenerateRanking());
    }
  }
  



  public void setFolds(int folds)
  {
    m_numFolds = folds;
  }
  



  public void setRanking(boolean r)
  {
    m_doRank = r;
  }
  



  public void setXval(boolean x)
  {
    m_doXval = x;
  }
  



  public void setSeed(int s)
  {
    m_seed = s;
  }
  



  public String toResultsString()
  {
    return m_selectionResults.toString();
  }
  





  public Instances reduceDimensionality(Instances in)
    throws Exception
  {
    if (m_attributeFilter == null) {
      throw new Exception("No feature selection has been performed yet!");
    }
    
    if (m_transformer != null) {
      Instances transformed = new Instances(m_transformer.transformedHeader(), in.numInstances());
      
      for (int i = 0; i < in.numInstances(); i++) {
        transformed.add(m_transformer.convertInstance(in.instance(i)));
      }
      return Filter.useFilter(transformed, m_attributeFilter);
    }
    
    return Filter.useFilter(in, m_attributeFilter);
  }
  





  public Instance reduceDimensionality(Instance in)
    throws Exception
  {
    if (m_attributeFilter == null) {
      throw new Exception("No feature selection has been performed yet!");
    }
    if (m_transformer != null) {
      in = m_transformer.convertInstance(in);
    }
    m_attributeFilter.input(in);
    m_attributeFilter.batchFinished();
    Instance result = m_attributeFilter.output();
    return result;
  }
  




  public AttributeSelection()
  {
    setFolds(10);
    setRanking(false);
    setXval(false);
    setSeed(1);
    setEvaluator(new CfsSubsetEval());
    setSearch(new GreedyStepwise());
    m_selectionResults = new StringBuffer();
    m_selectedAttributeSet = null;
    m_attributeRanking = ((double[][])null);
  }
  











  public static String SelectAttributes(ASEvaluation ASEvaluator, String[] options)
    throws Exception
  {
    Instances train = null;
    ASSearch searchMethod = null;
    String[] optionsTmp = (String[])options.clone();
    boolean helpRequested = false;
    String trainFileName;
    try
    {
      trainFileName = Utils.getOption('i', options);
      helpRequested = Utils.getFlag('h', optionsTmp);
      
      if ((helpRequested) || (trainFileName.length() == 0)) {
        String searchName = Utils.getOption('s', optionsTmp);
        if (searchName.length() != 0) {
          String[] searchOptions = Utils.splitOptions(searchName);
          searchMethod = (ASSearch)Class.forName(searchOptions[0]).newInstance();
        }
        
        if (helpRequested) {
          throw new Exception("Help requested.");
        }
        throw new Exception("No training file given.");
      }
    }
    catch (Exception e) {
      throw new Exception('\n' + e.getMessage() + makeOptionString(ASEvaluator, searchMethod));
    }
    

    ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainFileName);
    train = source.getDataSet();
    return SelectAttributes(ASEvaluator, options, train);
  }
  




  public String CVResultsString()
    throws Exception
  {
    StringBuffer CvString = new StringBuffer();
    
    if (((m_subsetResults == null) && (m_rankResults == null)) || (m_trainInstances == null))
    {
      throw new Exception("Attribute selection has not been performed yet!");
    }
    
    int fieldWidth = (int)(Math.log(m_trainInstances.numAttributes()) + 1.0D);
    
    CvString.append("\n\n=== Attribute selection " + m_numFolds + " fold cross-validation ");
    

    if ((!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) && (m_trainInstances.classAttribute().isNominal()))
    {

      CvString.append("(stratified), seed: ");
      CvString.append(m_seed + " ===\n\n");
    }
    else {
      CvString.append("seed: " + m_seed + " ===\n\n");
    }
    
    if (((m_searchMethod instanceof RankedOutputSearch)) && (m_doRank == true)) {
      CvString.append("average merit      average rank  attribute\n");
      

      for (int i = 0; i < m_rankResults[0].length; i++) {
        m_rankResults[0][i] /= m_numFolds;
        double var = m_rankResults[0][i] * m_rankResults[0][i] * m_numFolds;
        var = m_rankResults[2][i] - var;
        var /= m_numFolds;
        
        if (var <= 0.0D) {
          var = 0.0D;
          m_rankResults[2][i] = 0.0D;
        }
        else {
          m_rankResults[2][i] = Math.sqrt(var);
        }
        
        m_rankResults[1][i] /= m_numFolds;
        var = m_rankResults[1][i] * m_rankResults[1][i] * m_numFolds;
        var = m_rankResults[3][i] - var;
        var /= m_numFolds;
        
        if (var <= 0.0D) {
          var = 0.0D;
          m_rankResults[3][i] = 0.0D;
        }
        else {
          m_rankResults[3][i] = Math.sqrt(var);
        }
      }
      

      int[] s = Utils.sort(m_rankResults[1]);
      for (int i = 0; i < s.length; i++) {
        if (m_rankResults[1][s[i]] > 0.0D) {
          CvString.append(Utils.doubleToString(m_rankResults[0][s[i]], 6, 3) + " +-" + Utils.doubleToString(m_rankResults[2][s[i]], 6, 3) + "   " + Utils.doubleToString(m_rankResults[1][s[i]], fieldWidth + 2, 1) + " +-" + Utils.doubleToString(m_rankResults[3][s[i]], 5, 2) + "  " + Utils.doubleToString(s[i] + 1, fieldWidth, 0) + " " + m_trainInstances.attribute(s[i]).name() + "\n");



        }
        



      }
      



    }
    else
    {



      CvString.append("number of folds (%)  attribute\n");
      
      for (int i = 0; i < m_subsetResults.length; i++) {
        if (((m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) || (i != m_trainInstances.classIndex()))
        {
          CvString.append(Utils.doubleToString(m_subsetResults[i], 12, 0) + "(" + Utils.doubleToString(m_subsetResults[i] / m_numFolds * 100.0D, 3, 0) + " %)  " + Utils.doubleToString(i + 1, fieldWidth, 0) + " " + m_trainInstances.attribute(i).name() + "\n");
        }
      }
    }
    










    return CvString.toString();
  }
  









  public void selectAttributesCVSplit(Instances split)
    throws Exception
  {
    double[][] attributeRanking = (double[][])null;
    





    if (m_trainInstances == null) {
      m_trainInstances = split;
    }
    

    if ((m_rankResults == null) && (m_subsetResults == null)) {
      m_subsetResults = new double[split.numAttributes()];
      m_rankResults = new double[4][split.numAttributes()];
    }
    
    m_ASEvaluator.buildEvaluator(split);
    
    int[] attributeSet = m_searchMethod.search(m_ASEvaluator, split);
    


    attributeSet = m_ASEvaluator.postProcess(attributeSet);
    
    if (((m_searchMethod instanceof RankedOutputSearch)) && (m_doRank == true))
    {
      attributeRanking = ((RankedOutputSearch)m_searchMethod).rankedAttributes();
      


      for (int j = 0; j < attributeRanking.length; j++)
      {
        m_rankResults[0][((int)attributeRanking[j][0])] += attributeRanking[j][1];
        

        m_rankResults[2][((int)attributeRanking[j][0])] += attributeRanking[j][1] * attributeRanking[j][1];
        

        m_rankResults[1][((int)attributeRanking[j][0])] += j + 1;
        
        m_rankResults[3][((int)attributeRanking[j][0])] += (j + 1) * (j + 1);
      }
    }
    else {
      for (int j = 0; j < attributeSet.length; j++) {
        m_subsetResults[attributeSet[j]] += 1.0D;
      }
    }
    
    m_trials += 1;
  }
  








  public String CrossValidateAttributes()
    throws Exception
  {
    Instances cvData = new Instances(m_trainInstances);
    

    Random random = new Random(m_seed);
    cvData.randomize(random);
    
    if ((!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)))
    {
      if (cvData.classAttribute().isNominal()) {
        cvData.stratify(m_numFolds);
      }
    }
    

    for (int i = 0; i < m_numFolds; i++)
    {
      Instances train = cvData.trainCV(m_numFolds, i, random);
      selectAttributesCVSplit(train);
    }
    
    return CVResultsString();
  }
  






  public void SelectAttributes(Instances data)
    throws Exception
  {
    m_transformer = null;
    m_attributeFilter = null;
    m_trainInstances = data;
    
    if ((m_doXval == true) && ((m_ASEvaluator instanceof AttributeTransformer))) {
      throw new Exception("Can't cross validate an attribute transformer.");
    }
    
    if (((m_ASEvaluator instanceof SubsetEvaluator)) && ((m_searchMethod instanceof Ranker)))
    {
      throw new Exception(m_ASEvaluator.getClass().getName() + " must use a search method other than Ranker");
    }
    

    if (((m_ASEvaluator instanceof AttributeEvaluator)) && (!(m_searchMethod instanceof Ranker)))
    {



      throw new Exception("AttributeEvaluators must use the Ranker search method");
    }
    

    if ((m_searchMethod instanceof RankedOutputSearch)) {
      m_doRank = ((RankedOutputSearch)m_searchMethod).getGenerateRanking();
    }
    
    if ((!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)))
    {




      if (m_trainInstances.classIndex() < 0) {
        m_trainInstances.setClassIndex(m_trainInstances.numAttributes() - 1);
      }
    }
    

    m_ASEvaluator.buildEvaluator(m_trainInstances);
    if ((m_ASEvaluator instanceof AttributeTransformer)) {
      m_trainInstances = ((AttributeTransformer)m_ASEvaluator).transformedHeader();
      
      m_transformer = ((AttributeTransformer)m_ASEvaluator);
    }
    int fieldWidth = (int)(Math.log(m_trainInstances.numAttributes()) + 1.0D);
    

    int[] attributeSet = m_searchMethod.search(m_ASEvaluator, m_trainInstances);
    



    try
    {
      BeanInfo bi = Introspector.getBeanInfo(m_searchMethod.getClass());
      


      PropertyDescriptor[] properties = bi.getPropertyDescriptors();
      for (int i = 0; i < properties.length; i++) {
        String name = properties[i].getDisplayName();
        Method meth = properties[i].getReadMethod();
        Object retType = meth.getReturnType();
        if (retType.equals(ASEvaluation.class)) {
          Class[] args = new Class[0];
          ASEvaluation tempEval = (ASEvaluation)meth.invoke(m_searchMethod, (Object[])args);
          
          if ((tempEval instanceof AttributeTransformer))
          {
            m_trainInstances = ((AttributeTransformer)tempEval).transformedHeader();
            
            m_transformer = ((AttributeTransformer)tempEval);
          }
        }
      }
    } catch (IntrospectionException ex) {
      System.err.println("AttributeSelection: Couldn't introspect");
    }
    



    attributeSet = m_ASEvaluator.postProcess(attributeSet);
    if (!m_doRank) {
      m_selectionResults.append(printSelectionResults());
    }
    
    if (((m_searchMethod instanceof RankedOutputSearch)) && (m_doRank == true)) {
      m_attributeRanking = ((RankedOutputSearch)m_searchMethod).rankedAttributes();
      
      m_selectionResults.append(printSelectionResults());
      m_selectionResults.append("Ranked attributes:\n");
      

      m_numToSelect = ((RankedOutputSearch)m_searchMethod).getCalculatedNumToSelect();
      


      int f_p = 0;
      int w_p = 0;
      
      for (int i = 0; i < m_numToSelect; i++) {
        double precision = Math.abs(m_attributeRanking[i][1]) - (int)Math.abs(m_attributeRanking[i][1]);
        
        double intPart = (int)Math.abs(m_attributeRanking[i][1]);
        
        if (precision > 0.0D) {
          precision = Math.abs(Math.log(Math.abs(precision)) / Math.log(10.0D)) + 3.0D;
        }
        
        if (precision > f_p) {
          f_p = (int)precision;
        }
        
        if (intPart == 0.0D) {
          if (w_p < 2) {
            w_p = 2;
          }
        } else if (Math.abs(Math.log(Math.abs(m_attributeRanking[i][1])) / Math.log(10.0D)) + 1.0D > w_p)
        {
          if (m_attributeRanking[i][1] > 0.0D) {
            w_p = (int)Math.abs(Math.log(Math.abs(m_attributeRanking[i][1])) / Math.log(10.0D)) + 1;
          }
        }
      }
      

      for (int i = 0; i < m_numToSelect; i++) {
        m_selectionResults.append(Utils.doubleToString(m_attributeRanking[i][1], f_p + w_p + 1, f_p) + Utils.doubleToString(m_attributeRanking[i][0] + 1.0D, fieldWidth + 1, 0) + " " + m_trainInstances.attribute((int)m_attributeRanking[i][0]).name() + "\n");
      }
      










      if (m_trainInstances.classIndex() >= 0) {
        if (((!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator))) || ((m_ASEvaluator instanceof AttributeTransformer)))
        {


          m_selectedAttributeSet = new int[m_numToSelect + 1];
          m_selectedAttributeSet[m_numToSelect] = m_trainInstances.classIndex();
        }
        else {
          m_selectedAttributeSet = new int[m_numToSelect];
        }
      } else {
        m_selectedAttributeSet = new int[m_numToSelect];
      }
      
      m_selectionResults.append("\nSelected attributes: ");
      
      for (int i = 0; i < m_numToSelect; i++) {
        m_selectedAttributeSet[i] = ((int)m_attributeRanking[i][0]);
        
        if (i == m_numToSelect - 1) {
          m_selectionResults.append((int)m_attributeRanking[i][0] + 1 + " : " + (i + 1) + "\n");

        }
        else
        {

          m_selectionResults.append((int)m_attributeRanking[i][0] + 1);
          m_selectionResults.append(",");
        }
      }
    }
    else
    {
      if (((!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator))) || (m_trainInstances.classIndex() >= 0))
      {



        m_selectedAttributeSet = new int[attributeSet.length + 1];
        m_selectedAttributeSet[attributeSet.length] = m_trainInstances.classIndex();
      }
      else
      {
        m_selectedAttributeSet = new int[attributeSet.length];
      }
      
      for (int i = 0; i < attributeSet.length; i++) {
        m_selectedAttributeSet[i] = attributeSet[i];
      }
      
      m_selectionResults.append("Selected attributes: ");
      
      for (int i = 0; i < attributeSet.length; i++) {
        if (i == attributeSet.length - 1) {
          m_selectionResults.append(attributeSet[i] + 1 + " : " + attributeSet.length + "\n");

        }
        else
        {

          m_selectionResults.append(attributeSet[i] + 1 + ",");
        }
      }
      
      for (int i = 0; i < attributeSet.length; i++) {
        m_selectionResults.append("                     " + m_trainInstances.attribute(attributeSet[i]).name() + "\n");
      }
    }
    




    if (m_doXval == true) {
      m_selectionResults.append(CrossValidateAttributes());
    }
    

    if ((m_selectedAttributeSet != null) && (!m_doXval)) {
      m_attributeFilter = new Remove();
      m_attributeFilter.setAttributeIndicesArray(m_selectedAttributeSet);
      m_attributeFilter.setInvertSelection(true);
      m_attributeFilter.setInputFormat(m_trainInstances);
    }
    

    m_trainInstances = new Instances(m_trainInstances, 0);
    m_ASEvaluator.clean();
  }
  













  public static String SelectAttributes(ASEvaluation ASEvaluator, String[] options, Instances train)
    throws Exception
  {
    int seed = 1;int folds = 10;
    


    String[] searchOptions = null;
    ASSearch searchMethod = null;
    boolean doCrossVal = false;
    int classIndex = -1;
    boolean helpRequested = false;
    AttributeSelection trainSelector = new AttributeSelection();
    try
    {
      if (Utils.getFlag('h', options)) {
        helpRequested = true;
      }
      

      if (train.classIndex() != -1) {
        classIndex = train.classIndex() + 1;
      }
      
      String classString = Utils.getOption('c', options);
      
      if (classString.length() != 0) {
        if (classString.equals("first")) {
          classIndex = 1;
        } else if (classString.equals("last")) {
          classIndex = train.numAttributes();
        } else {
          classIndex = Integer.parseInt(classString);
        }
      }
      
      if ((classIndex != -1) && ((classIndex == 0) || (classIndex > train.numAttributes())))
      {
        throw new Exception("Class index out of range.");
      }
      
      if (classIndex != -1) {
        train.setClassIndex(classIndex - 1);
      }
      




      String foldsString = Utils.getOption('x', options);
      
      if (foldsString.length() != 0) {
        folds = Integer.parseInt(foldsString);
        doCrossVal = true;
      }
      
      trainSelector.setFolds(folds);
      trainSelector.setXval(doCrossVal);
      
      String seedString = Utils.getOption('n', options);
      
      if (seedString.length() != 0) {
        seed = Integer.parseInt(seedString);
      }
      
      trainSelector.setSeed(seed);
      
      String searchName = Utils.getOption('s', options);
      
      if ((searchName.length() == 0) && (!(ASEvaluator instanceof AttributeEvaluator)))
      {
        throw new Exception("No search method given.");
      }
      String searchClassName;
      if (searchName.length() != 0) {
        searchName = searchName.trim();
        
        int breakLoc = searchName.indexOf(' ');
        String searchClassName = searchName;
        String searchOptionsString = "";
        
        if (breakLoc != -1) {
          searchClassName = searchName.substring(0, breakLoc);
          searchOptionsString = searchName.substring(breakLoc).trim();
          searchOptions = Utils.splitOptions(searchOptionsString);
        }
      }
      else {
        try {
          searchClassName = new String("weka.attributeSelection.Ranker");
          searchMethod = (ASSearch)Class.forName(searchClassName).newInstance();
        }
        catch (Exception e)
        {
          throw new Exception("Can't create Ranker object");
        }
      }
      


      if (searchMethod == null) {
        searchMethod = ASSearch.forName(searchClassName, searchOptions);
      }
      

      trainSelector.setSearch(searchMethod);
    }
    catch (Exception e) {
      throw new Exception('\n' + e.getMessage() + makeOptionString(ASEvaluator, searchMethod));
    }
    

    try
    {
      if ((ASEvaluator instanceof OptionHandler)) {
        ((OptionHandler)ASEvaluator).setOptions(options);


      }
      



    }
    catch (Exception e)
    {



      throw new Exception("\n" + e.getMessage() + makeOptionString(ASEvaluator, searchMethod));
    }
    
    try
    {
      Utils.checkForRemainingOptions(options);
    }
    catch (Exception e) {
      throw new Exception('\n' + e.getMessage() + makeOptionString(ASEvaluator, searchMethod));
    }
    

    if (helpRequested) {
      System.out.println(makeOptionString(ASEvaluator, searchMethod));
      System.exit(0);
    }
    

    trainSelector.setEvaluator(ASEvaluator);
    

    trainSelector.SelectAttributes(train);
    

    return trainSelector.toResultsString();
  }
  





  private String printSelectionResults()
  {
    StringBuffer text = new StringBuffer();
    text.append("\n\n=== Attribute Selection on all input data ===\n\nSearch Method:\n");
    
    text.append(m_searchMethod.toString());
    text.append("\nAttribute ");
    
    if ((m_ASEvaluator instanceof SubsetEvaluator)) {
      text.append("Subset Evaluator (");
    }
    else {
      text.append("Evaluator (");
    }
    
    if ((!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)) && (!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)))
    {
      text.append("supervised, ");
      text.append("Class (");
      
      if (m_trainInstances.attribute(m_trainInstances.classIndex()).isNumeric())
      {
        text.append("numeric): ");
      }
      else {
        text.append("nominal): ");
      }
      
      text.append(m_trainInstances.classIndex() + 1 + " " + m_trainInstances.attribute(m_trainInstances.classIndex()).name() + "):\n");


    }
    else
    {

      text.append("unsupervised):\n");
    }
    
    text.append(m_ASEvaluator.toString() + "\n");
    return text.toString();
  }
  










  private static String makeOptionString(ASEvaluation ASEvaluator, ASSearch searchMethod)
    throws Exception
  {
    StringBuffer optionsText = new StringBuffer("");
    
    optionsText.append("\n\nGeneral options:\n\n");
    optionsText.append("-h\n\tdisplay this help\n");
    optionsText.append("-i <name of input file>\n");
    optionsText.append("\tSets training file.\n");
    optionsText.append("-c <class index>\n");
    optionsText.append("\tSets the class index for supervised attribute\n");
    optionsText.append("\tselection. Default=last column.\n");
    optionsText.append("-s <class name>\n");
    optionsText.append("\tSets search method for subset evaluators.\n");
    optionsText.append("-x <number of folds>\n");
    optionsText.append("\tPerform a cross validation.\n");
    optionsText.append("-n <random number seed>\n");
    optionsText.append("\tUse in conjunction with -x.\n");
    

    if ((ASEvaluator instanceof OptionHandler)) {
      optionsText.append("\nOptions specific to " + ASEvaluator.getClass().getName() + ":\n\n");
      

      Enumeration enu = ((OptionHandler)ASEvaluator).listOptions();
      
      while (enu.hasMoreElements()) {
        Option option = (Option)enu.nextElement();
        optionsText.append(option.synopsis() + '\n');
        optionsText.append(option.description() + "\n");
      }
    }
    
    if (searchMethod != null) {
      if ((searchMethod instanceof OptionHandler)) {
        optionsText.append("\nOptions specific to " + searchMethod.getClass().getName() + ":\n\n");
        

        Enumeration enu = ((OptionHandler)searchMethod).listOptions();
        
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          optionsText.append(option.synopsis() + '\n');
          optionsText.append(option.description() + "\n");
        }
        
      }
    }
    else if ((ASEvaluator instanceof SubsetEvaluator)) {
      System.out.println("No search method given.");
    }
    

    return optionsText.toString();
  }
  




  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("The first argument must be the name of an attribute/subset evaluator");
      }
      

      String EvaluatorName = args[0];
      args[0] = "";
      ASEvaluation newEval = ASEvaluation.forName(EvaluatorName, null);
      System.out.println(SelectAttributes(newEval, args));
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11851 $");
  }
}
