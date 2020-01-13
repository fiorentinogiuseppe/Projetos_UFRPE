package weka.attributeSelection;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Statistics;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.experiment.PairedStats;
import weka.experiment.Stats;























































































































public class RaceSearch
  extends ASSearch
  implements RankedOutputSearch, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 4015453851212985720L;
  private Instances m_Instances = null;
  
  private static final int FORWARD_RACE = 0;
  
  private static final int BACKWARD_RACE = 1;
  private static final int SCHEMATA_RACE = 2;
  private static final int RANK_RACE = 3;
  public static final Tag[] TAGS_SELECTION = { new Tag(0, "Forward selection race"), new Tag(1, "Backward elimination race"), new Tag(2, "Schemata race"), new Tag(3, "Rank race") };
  






  private int m_raceType = 0;
  
  private static final int TEN_FOLD = 0;
  
  private static final int LEAVE_ONE_OUT = 1;
  public static final Tag[] XVALTAGS_SELECTION = { new Tag(0, "10 Fold"), new Tag(1, "Leave-one-out") };
  




  private int m_xvalType = 0;
  

  private int m_classIndex;
  

  private int m_numAttribs;
  

  private int m_totalEvals;
  

  private double m_bestMerit = -1.7976931348623157E308D;
  

  private HoldOutSubsetEvaluator m_theEvaluator = null;
  

  private double m_sigLevel = 0.001D;
  

  private double m_delta = 0.001D;
  


  private int m_samples = 20;
  


  private int m_numFolds = 10;
  


  private ASEvaluation m_ASEval = new GainRatioAttributeEval();
  


  private int[] m_Ranking;
  

  private boolean m_debug = false;
  


  private boolean m_rankingRequested = false;
  


  private double[][] m_rankedAtts;
  


  private int m_rankedSoFar;
  

  private int m_numToSelect = -1;
  
  private int m_calculatedNumToSelect = -1;
  

  private double m_threshold = -1.7976931348623157E308D;
  

  public RaceSearch() {}
  

  public String globalInfo()
  {
    return "Races the cross validation error of competing attribute subsets. Use in conjuction with a ClassifierSubsetEval. RaceSearch has four modes:\n\nforward selection races all single attribute additions to a base set (initially  no attributes), selects the winner to become the new base set and then iterates until there is no improvement over the base set. \n\nBackward elimination is similar but the initial base set has all attributes included and races all single attribute deletions. \n\nSchemata search is a bit different. Each iteration a series of races are run in parallel. Each race in a set determines whether a particular attribute should be included or not---ie the race is between the attribute being \"in\" or \"out\". The other attributes for this race are included or excluded randomly at each point in the evaluation. As soon as one race has a clear winner (ie it has been decided whether a particular attribute should be inor not) then the next set of races begins, using the result of the winning race from the previous iteration as new base set.\n\nRank race first ranks the attributes using an attribute evaluator and then races the ranking. The race includes no attributes, the top ranked attribute, the top two attributes, the top three attributes, etc.\n\nIt is also possible to generate a raked list of attributes through the forward racing process. If generateRanking is set to true then a complete forward race will be run---that is, racing continues until all attributes have been selected. The order that they are added in determines a complete ranking of all the attributes.\n\nRacing uses paired and unpaired t-tests on cross-validation errors of competing subsets. When there is a significant difference between the means of the errors of two competing subsets then the poorer of the two can be eliminated from the race. Similarly, if there is no significant difference between the mean errors of two competing subsets and they are within some threshold of each other, then one can be eliminated from the race.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  









































  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Andrew W. Moore and Mary S. Lee");
    result.setValue(TechnicalInformation.Field.TITLE, "Efficient Algorithms for Minimizing Cross Validation Error");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Eleventh International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1994");
    result.setValue(TechnicalInformation.Field.PAGES, "190-198");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    
    return result;
  }
  




  public String raceTypeTipText()
  {
    return "Set the type of search.";
  }
  





  public void setRaceType(SelectedTag d)
  {
    if (d.getTags() == TAGS_SELECTION) {
      m_raceType = d.getSelectedTag().getID();
    }
    if ((m_raceType == 2) && (!m_rankingRequested)) {
      try {
        setFoldsType(new SelectedTag(1, XVALTAGS_SELECTION));
        
        setSignificanceLevel(0.01D);
      }
      catch (Exception ex) {}
    } else {
      try {
        setFoldsType(new SelectedTag(0, XVALTAGS_SELECTION));
        
        setSignificanceLevel(0.001D);
      }
      catch (Exception ex) {}
    }
  }
  




  public SelectedTag getRaceType()
  {
    return new SelectedTag(m_raceType, TAGS_SELECTION);
  }
  




  public String significanceLevelTipText()
  {
    return "Set the significance level to use for t-test comparisons.";
  }
  



  public void setSignificanceLevel(double sig)
  {
    m_sigLevel = sig;
  }
  



  public double getSignificanceLevel()
  {
    return m_sigLevel;
  }
  




  public String thresholdTipText()
  {
    return "Set the error threshold by which to consider two subsets equivalent.";
  }
  




  public void setThreshold(double t)
  {
    m_delta = t;
  }
  



  public double getThreshold()
  {
    return m_delta;
  }
  




  public String foldsTypeTipText()
  {
    return "Set the number of folds to use for x-val error estimation; leave-one-out is selected automatically for schemata search.";
  }
  






  public void setFoldsType(SelectedTag d)
  {
    if (d.getTags() == XVALTAGS_SELECTION) {
      m_xvalType = d.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getFoldsType()
  {
    return new SelectedTag(m_xvalType, XVALTAGS_SELECTION);
  }
  




  public String debugTipText()
  {
    return "Turn on verbose output for monitoring the search's progress.";
  }
  



  public void setDebug(boolean d)
  {
    m_debug = d;
  }
  



  public boolean getDebug()
  {
    return m_debug;
  }
  




  public String attributeEvaluatorTipText()
  {
    return "Attribute evaluator to use for generating an initial ranking. Use in conjunction with a rank race";
  }
  




  public void setAttributeEvaluator(ASEvaluation newEvaluator)
  {
    m_ASEval = newEvaluator;
  }
  



  public ASEvaluation getAttributeEvaluator()
  {
    return m_ASEval;
  }
  




  public String generateRankingTipText()
  {
    return "Use the racing process to generate a ranked list of attributes. Using this mode forces the race to be a forward type and then races until all attributes have been added, thus giving a ranked list";
  }
  





  public void setGenerateRanking(boolean doRank)
  {
    m_rankingRequested = doRank;
    if (m_rankingRequested) {
      try {
        setRaceType(new SelectedTag(0, TAGS_SELECTION));
      }
      catch (Exception ex) {}
    }
  }
  






  public boolean getGenerateRanking()
  {
    return m_rankingRequested;
  }
  




  public String numToSelectTipText()
  {
    return "Specify the number of attributes to retain. Use in conjunction with generateRanking. The default value (-1) indicates that all attributes are to be retained. Use either this option or a threshold to reduce the attribute set.";
  }
  








  public void setNumToSelect(int n)
  {
    m_numToSelect = n;
  }
  



  public int getNumToSelect()
  {
    return m_numToSelect;
  }
  






  public int getCalculatedNumToSelect()
  {
    if (m_numToSelect >= 0) {
      m_calculatedNumToSelect = m_numToSelect;
    }
    return m_calculatedNumToSelect;
  }
  




  public String selectionThresholdTipText()
  {
    return "Set threshold by which attributes can be discarded. Default value results in no attributes being discarded. Use in conjunction with generateRanking";
  }
  






  public void setSelectionThreshold(double threshold)
  {
    m_threshold = threshold;
  }
  



  public double getSelectionThreshold()
  {
    return m_threshold;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tType of race to perform.\n\t(default = 0).", "R", 1, "-R <0 = forward | 1 = backward race | 2 = schemata | 3 = rank>"));
    



    newVector.addElement(new Option("\tSignificance level for comaparisons\n\t(default = 0.001(forward/backward/rank)/0.01(schemata)).", "L", 1, "-L <significance>"));
    



    newVector.addElement(new Option("\tThreshold for error comparison.\n\t(default = 0.001).", "T", 1, "-T <threshold>"));
    



    newVector.addElement(new Option("\tAttribute ranker to use if doing a \n\trank search. Place any\n\tevaluator options LAST on \n\tthe command line following a \"--\".\n\teg. -A weka.attributeSelection.GainRatioAttributeEval ... -- -M.\n\t(default = GainRatioAttributeEval)", "A", 1, "-A <attribute evaluator>"));
    







    newVector.addElement(new Option("\tFolds for cross validation\n\t(default = 0 (1 if schemata race)", "F", 1, "-F <0 = 10 fold | 1 = leave-one-out>"));
    



    newVector.addElement(new Option("\tGenerate a ranked list of attributes.\n\tForces the search to be forward\n\tand races until all attributes have\n\tselected, thus producing a ranking.", "Q", 0, "-Q"));
    





    newVector.addElement(new Option("\tSpecify number of attributes to retain from \n\tthe ranking. Overides -T. Use in conjunction with -Q", "N", 1, "-N <num to select>"));
    



    newVector.addElement(new Option("\tSpecify a theshold by which attributes\n\tmay be discarded from the ranking.\n\tUse in conjuction with -Q", "J", 1, "-J <threshold>"));
    




    newVector.addElement(new Option("\tVerbose output for monitoring the search.", "Z", 0, "-Z"));
    


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
    
    String optionString = Utils.getOption('R', options);
    if (optionString.length() != 0) {
      setRaceType(new SelectedTag(Integer.parseInt(optionString), TAGS_SELECTION));
    }
    

    optionString = Utils.getOption('F', options);
    if (optionString.length() != 0) {
      setFoldsType(new SelectedTag(Integer.parseInt(optionString), XVALTAGS_SELECTION));
    }
    

    optionString = Utils.getOption('L', options);
    if (optionString.length() != 0) {
      setSignificanceLevel(Double.parseDouble(optionString));
    }
    
    optionString = Utils.getOption('T', options);
    if (optionString.length() != 0) {
      setThreshold(Double.parseDouble(optionString));
    }
    
    optionString = Utils.getOption('A', options);
    if (optionString.length() != 0) {
      setAttributeEvaluator(ASEvaluation.forName(optionString, Utils.partitionOptions(options)));
    }
    

    setGenerateRanking(Utils.getFlag('Q', options));
    
    optionString = Utils.getOption('J', options);
    if (optionString.length() != 0) {
      setSelectionThreshold(Double.parseDouble(optionString));
    }
    
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setNumToSelect(Integer.parseInt(optionString));
    }
    
    setDebug(Utils.getFlag('Z', options));
  }
  



  public String[] getOptions()
  {
    int current = 0;
    String[] evaluatorOptions = new String[0];
    
    if ((m_ASEval != null) && ((m_ASEval instanceof OptionHandler)))
    {
      evaluatorOptions = ((OptionHandler)m_ASEval).getOptions();
    }
    String[] options = new String[17 + evaluatorOptions.length];
    
    options[(current++)] = "-R";options[(current++)] = ("" + m_raceType);
    options[(current++)] = "-L";options[(current++)] = ("" + getSignificanceLevel());
    options[(current++)] = "-T";options[(current++)] = ("" + getThreshold());
    options[(current++)] = "-F";options[(current++)] = ("" + m_xvalType);
    if (getGenerateRanking()) {
      options[(current++)] = "-Q";
    }
    options[(current++)] = "-N";options[(current++)] = ("" + getNumToSelect());
    options[(current++)] = "-J";options[(current++)] = ("" + getSelectionThreshold());
    if (getDebug()) {
      options[(current++)] = "-Z";
    }
    
    if (getAttributeEvaluator() != null) {
      options[(current++)] = "-A";
      options[(current++)] = getAttributeEvaluator().getClass().getName();
      options[(current++)] = "--";
      System.arraycopy(evaluatorOptions, 0, options, current, evaluatorOptions.length);
      
      current += evaluatorOptions.length;
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  











  public int[] search(ASEvaluation ASEval, Instances data)
    throws Exception
  {
    if (!(ASEval instanceof SubsetEvaluator)) {
      throw new Exception(ASEval.getClass().getName() + " is not a " + "Subset evaluator! (RaceSearch)");
    }
    


    if ((ASEval instanceof UnsupervisedSubsetEvaluator)) {
      throw new Exception("Can't use an unsupervised subset evaluator (RaceSearch).");
    }
    

    if (!(ASEval instanceof HoldOutSubsetEvaluator)) {
      throw new Exception("Must use a HoldOutSubsetEvaluator, eg. weka.attributeSelection.ClassifierSubsetEval (RaceSearch)");
    }
    


    if (!(ASEval instanceof ErrorBasedMeritEvaluator)) {
      throw new Exception("Only error based subset evaluators can be used, eg. weka.attributeSelection.ClassifierSubsetEval (RaceSearch)");
    }
    


    m_Instances = new Instances(data);
    m_Instances.deleteWithMissingClass();
    if (m_Instances.numInstances() == 0) {
      throw new Exception("All train instances have missing class! (RaceSearch)");
    }
    if ((m_rankingRequested) && (m_numToSelect > m_Instances.numAttributes() - 1)) {
      throw new Exception("More attributes requested than exist in the data (RaceSearch).");
    }
    
    m_theEvaluator = ((HoldOutSubsetEvaluator)ASEval);
    m_numAttribs = m_Instances.numAttributes();
    m_classIndex = m_Instances.classIndex();
    
    if (m_rankingRequested) {
      m_rankedAtts = new double[m_numAttribs - 1][2];
      m_rankedSoFar = 0;
    }
    
    if (m_xvalType == 1) {
      m_numFolds = m_Instances.numInstances();
    } else {
      m_numFolds = 10;
    }
    
    Random random = new Random(1L);
    m_Instances.randomize(random);
    int[] bestSubset = null;
    
    switch (m_raceType) {
    case 0: 
    case 1: 
      bestSubset = hillclimbRace(m_Instances, random);
      break;
    case 2: 
      bestSubset = schemataRace(m_Instances, random);
      break;
    case 3: 
      bestSubset = rankRace(m_Instances, random);
    }
    
    
    return bestSubset;
  }
  
  public double[][] rankedAttributes() throws Exception {
    if (!m_rankingRequested) {
      throw new Exception("Need to request a ranked list of attributes before attributes can be ranked (RaceSearch).");
    }
    
    if (m_rankedAtts == null) {
      throw new Exception("Search must be performed before attributes can be ranked (RaceSearch).");
    }
    

    double[][] final_rank = new double[m_rankedSoFar][2];
    for (int i = 0; i < m_rankedSoFar; i++) {
      final_rank[i][0] = m_rankedAtts[i][0];
      final_rank[i][1] = m_rankedAtts[i][1];
    }
    
    if (m_numToSelect <= 0) {
      if (m_threshold == -1.7976931348623157E308D) {
        m_calculatedNumToSelect = final_rank.length;
      } else {
        determineNumToSelectFromThreshold(final_rank);
      }
    }
    
    return final_rank;
  }
  
  private void determineNumToSelectFromThreshold(double[][] ranking) {
    int count = 0;
    for (int i = 0; i < ranking.length; i++) {
      if (ranking[i][1] > m_threshold) {
        count++;
      }
    }
    m_calculatedNumToSelect = count;
  }
  


  private String printSets(char[][] raceSets)
  {
    StringBuffer temp = new StringBuffer();
    for (int i = 0; i < raceSets.length; i++) {
      for (int j = 0; j < m_numAttribs; j++) {
        temp.append(raceSets[i][j]);
      }
      temp.append('\n');
    }
    return temp.toString();
  }
  






  private int[] schemataRace(Instances data, Random random)
    throws Exception
  {
    int numRaces = m_numAttribs - 1;
    Random r = new Random(42L);
    int numInstances = data.numInstances();
    



    Stats[][] raceStats = new Stats[numRaces][2];
    
    char[][][] parallelRaces = new char[numRaces][2][m_numAttribs - 1];
    char[] base = new char[m_numAttribs];
    for (int i = 0; i < m_numAttribs; i++) {
      base[i] = '*';
    }
    
    int count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (i != m_classIndex) {
        parallelRaces[count][0] = ((char[])(char[])base.clone());
        parallelRaces[count][1] = ((char[])(char[])base.clone());
        parallelRaces[count][0][i] = 49;
        parallelRaces[(count++)][1][i] = 48;
      }
    }
    
    if (m_debug) {
      System.err.println("Initial sets:\n");
      for (int i = 0; i < numRaces; i++) {
        System.err.print(printSets(parallelRaces[i]) + "--------------\n");
      }
    }
    
    BitSet randomB = new BitSet(m_numAttribs);
    char[] randomBC = new char[m_numAttribs];
    

    boolean[] attributeConstraints = new boolean[m_numAttribs];
    
    int evaluationCount = 0;
    while (numRaces > 0) {
      boolean won = false;
      for (int i = 0; i < numRaces; i++) {
        raceStats[i][0] = new Stats();
        raceStats[i][1] = new Stats();
      }
      

      int sampleCount = 0;
      label1039:
      while (!won)
      {
        for (int i = 0; i < m_numAttribs; i++) {
          if (i != m_classIndex) {
            if (attributeConstraints[i] == 0) {
              if (r.nextDouble() < 0.5D) {
                randomB.set(i);
              } else {
                randomB.clear(i);
              }
            }
            else if (base[i] == '1') {
              randomB.set(i);
            } else {
              randomB.clear(i);
            }
          }
        }
        


        int testIndex = Math.abs(r.nextInt() % numInstances);
        



        Instances trainCV = data.trainCV(numInstances, testIndex, new Random(1L));
        Instances testCV = data.testCV(numInstances, testIndex);
        Instance testInstance = testCV.instance(0);
        sampleCount++;
        




        m_theEvaluator.buildEvaluator(trainCV);
        

        double error = -m_theEvaluator.evaluateSubset(randomB, testInstance, true);
        


        evaluationCount++;
        

        for (int i = 0; i < m_numAttribs; i++) {
          if (randomB.get(i)) {
            randomBC[i] = '1';
          } else {
            randomBC[i] = '0';
          }
        }
        

        for (int i = 0; i < numRaces; i++)
        {


          if ((0count + 1count) / 2.0D > numInstances) {
            break label1324;
          }
          
          for (int j = 0; j < 2; j++) {
            boolean matched = true;
            for (int k = 0; k < m_numAttribs; k++) {
              if ((parallelRaces[i][j][k] != '*') && 
                (parallelRaces[i][j][k] != randomBC[k])) {
                matched = false;
                break;
              }
            }
            
            if (matched)
            {
              raceStats[i][j].add(error);
              


              if ((0count > m_samples) && (1count > m_samples))
              {
                raceStats[i][0].calculateDerived();
                raceStats[i][1].calculateDerived();
                



                double prob = ttest(raceStats[i][0], raceStats[i][1]);
                
                if (prob < m_sigLevel) {
                  if (0mean < 1mean) {
                    base = (char[])parallelRaces[i][0].clone();
                    m_bestMerit = 0mean;
                    if (m_debug) {
                      System.err.println("contender 0 won ");
                    }
                  } else {
                    base = (char[])parallelRaces[i][1].clone();
                    m_bestMerit = 1mean;
                    if (m_debug) {
                      System.err.println("contender 1 won");
                    }
                  }
                  if (m_debug) {
                    System.err.println(new String(parallelRaces[i][0]) + " " + new String(parallelRaces[i][1]));
                    
                    System.err.println("Means : " + 0mean + " vs" + 1mean);
                    
                    System.err.println("Evaluations so far : " + evaluationCount);
                  }
                  
                  won = true;
                  
                  break label1039;
                }
              }
            }
          }
        }
      }
      
      numRaces--;
      
      if ((numRaces > 0) && (won)) {
        parallelRaces = new char[numRaces][2][m_numAttribs - 1];
        raceStats = new Stats[numRaces][2];
        
        for (int i = 0; i < m_numAttribs; i++) {
          if ((i != m_classIndex) && (attributeConstraints[i] == 0) && (base[i] != '*'))
          {
            attributeConstraints[i] = true;
            break;
          }
        }
        count = 0;
        for (int i = 0; i < numRaces; i++) {
          parallelRaces[i][0] = ((char[])(char[])base.clone());
          parallelRaces[i][1] = ((char[])(char[])base.clone());
          for (int j = count; j < m_numAttribs; j++) {
            if ((j != m_classIndex) && (parallelRaces[i][0][j] == '*')) {
              parallelRaces[i][0][j] = 49;
              parallelRaces[i][1][j] = 48;
              count = j + 1;
              break;
            }
          }
        }
        
        if (m_debug) {
          System.err.println("Next sets:\n");
          for (int i = 0; i < numRaces; i++) {
            System.err.print(printSets(parallelRaces[i]) + "--------------\n");
          }
        }
      }
    }
    label1324:
    if (m_debug) {
      System.err.println("Total evaluations : " + evaluationCount);
    }
    
    return attributeList(base);
  }
  


  private double ttest(Stats c1, Stats c2)
    throws Exception
  {
    double n1 = count;double n2 = count;
    double v1 = stdDev * stdDev;
    double v2 = stdDev * stdDev;
    double av1 = mean;
    double av2 = mean;
    
    double df = n1 + n2 - 2.0D;
    double cv = ((n1 - 1.0D) * v1 + (n2 - 1.0D) * v2) / df;
    double t = (av1 - av2) / Math.sqrt(cv * (1.0D / n1 + 1.0D / n2));
    
    return Statistics.incompleteBeta(df / 2.0D, 0.5D, df / (df + t * t));
  }
  







  private int[] rankRace(Instances data, Random random)
    throws Exception
  {
    char[] baseSet = new char[m_numAttribs];
    

    for (int i = 0; i < m_numAttribs; i++) {
      if (i == m_classIndex) {
        baseSet[i] = '-';
      } else {
        baseSet[i] = '0';
      }
    }
    
    int numCompetitors = m_numAttribs - 1;
    char[][] raceSets = new char[numCompetitors + 1][m_numAttribs];
    
    if ((m_ASEval instanceof AttributeEvaluator))
    {
      Ranker ranker = new Ranker();
      m_ASEval.buildEvaluator(data);
      m_Ranking = ranker.search(m_ASEval, data);
    } else {
      GreedyStepwise fs = new GreedyStepwise();
      
      fs.setGenerateRanking(true);
      m_ASEval.buildEvaluator(data);
      fs.search(m_ASEval, data);
      double[][] rankres = fs.rankedAttributes();
      m_Ranking = new int[rankres.length];
      for (int i = 0; i < rankres.length; i++) {
        m_Ranking[i] = ((int)rankres[i][0]);
      }
    }
    

    raceSets[0] = ((char[])(char[])baseSet.clone());
    for (int i = 0; i < m_Ranking.length; i++) {
      raceSets[(i + 1)] = ((char[])(char[])raceSets[i].clone());
      raceSets[(i + 1)][m_Ranking[i]] = 49;
    }
    
    if (m_debug) {
      System.err.println("Initial sets:\n" + printSets(raceSets));
    }
    

    double[] winnerInfo = raceSubsets(raceSets, data, true, random);
    double bestSetError = winnerInfo[1];
    char[] bestSet = (char[])raceSets[((int)winnerInfo[0])].clone();
    m_bestMerit = bestSetError;
    return attributeList(bestSet);
  }
  









  private int[] hillclimbRace(Instances data, Random random)
    throws Exception
  {
    char[] baseSet = new char[m_numAttribs];
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (i != m_classIndex) {
        if (m_raceType == 0) {
          baseSet[i] = '0';
        } else {
          baseSet[i] = '1';
        }
      } else {
        baseSet[i] = '-';
      }
    }
    
    int numCompetitors = m_numAttribs - 1;
    char[][] raceSets = new char[numCompetitors + 1][m_numAttribs];
    
    raceSets[0] = ((char[])(char[])baseSet.clone());
    int count = 1;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (i != m_classIndex) {
        raceSets[count] = ((char[])(char[])baseSet.clone());
        if (m_raceType == 1) {
          raceSets[(count++)][i] = 48;
        } else {
          raceSets[(count++)][i] = 49;
        }
      }
    }
    
    if (m_debug) {
      System.err.println("Initial sets:\n" + printSets(raceSets));
    }
    

    double[] winnerInfo = raceSubsets(raceSets, data, true, random);
    double baseSetError = winnerInfo[1];
    m_bestMerit = baseSetError;
    baseSet = (char[])raceSets[((int)winnerInfo[0])].clone();
    if (m_rankingRequested) {
      m_rankedAtts[m_rankedSoFar][0] = ((int)(winnerInfo[0] - 1.0D));
      m_rankedAtts[m_rankedSoFar][1] = winnerInfo[1];
      m_rankedSoFar += 1;
    }
    
    boolean improved = true;
    


    while (improved)
    {
      numCompetitors--;
      if (numCompetitors == 0) {
        break;
      }
      int j = 0;
      


      raceSets = new char[numCompetitors + 1][m_numAttribs];
      for (int i = 0; i < numCompetitors + 1; i++) {
        raceSets[i] = ((char[])(char[])baseSet.clone());
        if (i > 0) {
          for (int k = j; k < m_numAttribs; k++) {
            if (m_raceType == 1) {
              if ((k != m_classIndex) && (raceSets[i][k] != '0')) {
                raceSets[i][k] = 48;
                j = k + 1;
                break;
              }
            }
            else if ((k != m_classIndex) && (raceSets[i][k] != '1')) {
              raceSets[i][k] = 49;
              j = k + 1;
              break;
            }
          }
        }
      }
      

      if (m_debug) {
        System.err.println("Next set : \n" + printSets(raceSets));
      }
      improved = false;
      winnerInfo = raceSubsets(raceSets, data, true, random);
      String bs = new String(baseSet);
      String win = new String(raceSets[((int)winnerInfo[0])]);
      if (bs.compareTo(win) != 0)
      {

        if ((winnerInfo[1] < baseSetError) || (m_rankingRequested)) {
          improved = true;
          baseSetError = winnerInfo[1];
          m_bestMerit = baseSetError;
          
          if (m_rankingRequested) {
            for (int i = 0; i < baseSet.length; i++) {
              if (win.charAt(i) != bs.charAt(i)) {
                m_rankedAtts[m_rankedSoFar][0] = i;
                m_rankedAtts[m_rankedSoFar][1] = winnerInfo[1];
                m_rankedSoFar += 1;
              }
            }
          }
          baseSet = (char[])raceSets[((int)winnerInfo[0])].clone();
        }
      }
    }
    




    return attributeList(baseSet);
  }
  


  private int[] attributeList(char[] list)
  {
    int count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (list[i] == '1') {
        count++;
      }
    }
    
    int[] rlist = new int[count];
    count = 0;
    for (int i = 0; i < m_numAttribs; i++) {
      if (list[i] == '1') {
        rlist[(count++)] = i;
      }
    }
    
    return rlist;
  }
  












  private double[] raceSubsets(char[][] raceSets, Instances data, boolean baseSetIncluded, Random random)
    throws Exception
  {
    ASEvaluation[] evaluators = ASEvaluation.makeCopies(m_theEvaluator, raceSets.length);
    


    boolean[] eliminated = new boolean[raceSets.length];
    

    Stats[] individualStats = new Stats[raceSets.length];
    

    PairedStats[][] testers = new PairedStats[raceSets.length][raceSets.length];
    


    int startPt = m_rankingRequested ? 1 : 0;
    
    for (int i = 0; i < raceSets.length; i++) {
      individualStats[i] = new Stats();
      for (int j = i + 1; j < raceSets.length; j++) {
        testers[i][j] = new PairedStats(m_sigLevel);
      }
    }
    
    BitSet[] raceBitSets = new BitSet[raceSets.length];
    for (int i = 0; i < raceSets.length; i++) {
      raceBitSets[i] = new BitSet(m_numAttribs);
      for (int j = 0; j < m_numAttribs; j++) {
        if (raceSets[i][j] == '1') {
          raceBitSets[i].set(j);
        }
      }
    }
    





    double[] errors = new double[raceSets.length];
    int eliminatedCount = 0;
    int processedCount = 0;
    


    processedCount = 0;
    for (int i = 0; i < m_numFolds; i++)
    {


      Instances trainCV = data.trainCV(m_numFolds, i, new Random(1L));
      Instances testCV = data.testCV(m_numFolds, i);
      


      for (int j = startPt; j < raceSets.length; j++) {
        if (eliminated[j] == 0) {
          evaluators[j].buildEvaluator(trainCV);
        }
      }
      
      for (int z = 0; z < testCV.numInstances(); z++) {
        Instance testInst = testCV.instance(z);
        processedCount++;
        


        for (int zz = startPt; zz < raceSets.length; zz++) {
          if (eliminated[zz] == 0) {
            if (z == 0) {
              errors[zz] = (-((HoldOutSubsetEvaluator)evaluators[zz]).evaluateSubset(raceBitSets[zz], testInst, true));

            }
            else
            {
              errors[zz] = (-((HoldOutSubsetEvaluator)evaluators[zz]).evaluateSubset(raceBitSets[zz], testInst, false));
            }
          }
        }
        




        for (int j = startPt; j < raceSets.length; j++) {
          if (eliminated[j] == 0) {
            individualStats[j].add(errors[j]);
            for (int k = j + 1; k < raceSets.length; k++) {
              if (eliminated[k] == 0) {
                testers[j][k].add(errors[j], errors[k]);
              }
            }
          }
        }
        


        if ((processedCount > m_samples - 1) && (eliminatedCount < raceSets.length - 1))
        {
          for (int j = 0; j < raceSets.length; j++) {
            if (eliminated[j] == 0) {
              for (int k = j + 1; k < raceSets.length; k++) {
                if (eliminated[k] == 0) {
                  testers[j][k].calculateDerived();
                  
                  if ((differencesSignificance == 0) && ((Utils.eq(differencesStats.mean, 0.0D)) || (Utils.gr(m_delta, Math.abs(differencesStats.mean)))))
                  {





                    if (Utils.eq(differencesStats.mean, 0.0D))
                    {
                      if (baseSetIncluded) {
                        if (j != 0) {
                          eliminated[j] = true;
                        } else {
                          eliminated[k] = true;
                        }
                        eliminatedCount++;
                      } else {
                        eliminated[j] = true;
                      }
                      if (m_debug) {
                        System.err.println("Eliminating (identical) " + j + " " + raceBitSets[j].toString() + " vs " + k + " " + raceBitSets[k].toString() + " after " + processedCount + " evaluations\n" + "\nerror " + j + " : " + xStats.mean + " vs " + k + " : " + yStats.mean + " diff : " + differencesStats.mean);



                      }
                      




                    }
                    else
                    {




                      if (xStats.mean > yStats.mean)
                      {
                        eliminated[j] = true;
                        eliminatedCount++;
                        if (!m_debug) break;
                        System.err.println("Eliminating (near identical) " + j + " " + raceBitSets[j].toString() + " vs " + k + " " + raceBitSets[k].toString() + " after " + processedCount + " evaluations\n" + "\nerror " + j + " : " + xStats.mean + " vs " + k + " : " + yStats.mean + " diff : " + differencesStats.mean); break;
                      }
                      














                      eliminated[k] = true;
                      eliminatedCount++;
                      if (m_debug) {
                        System.err.println("Eliminating (near identical) " + k + " " + raceBitSets[k].toString() + " vs " + j + " " + raceBitSets[j].toString() + " after " + processedCount + " evaluations\n" + "\nerror " + k + " : " + yStats.mean + " vs " + j + " : " + xStats.mean + " diff : " + differencesStats.mean);




                      }
                      




                    }
                    





                  }
                  else if (differencesSignificance != 0) {
                    if (differencesSignificance > 0) {
                      eliminated[j] = true;
                      eliminatedCount++;
                      if (!m_debug) break;
                      System.err.println("Eliminating (-worse) " + j + " " + raceBitSets[j].toString() + " vs " + k + " " + raceBitSets[k].toString() + " after " + processedCount + " evaluations" + "\nerror " + j + " : " + xStats.mean + " vs " + k + " : " + yStats.mean); break;
                    }
                    











                    eliminated[k] = true;
                    eliminatedCount++;
                    if (m_debug) {
                      System.err.println("Eliminating (worse) " + k + " " + raceBitSets[k].toString() + " vs " + j + " " + raceBitSets[j].toString() + " after " + processedCount + " evaluations" + "\nerror " + k + " : " + yStats.mean + " vs " + j + " : " + xStats.mean);
                    }
                  }
                }
              }
            }
          }
        }
        













        if ((eliminatedCount == raceSets.length - 1) && (baseSetIncluded) && (eliminated[0] == 0) && (!m_rankingRequested)) {
          break label1711;
        }
      }
    }
    
    label1711:
    if (m_debug) {
      System.err.println("*****eliminated count: " + eliminatedCount);
    }
    double bestError = Double.MAX_VALUE;
    int bestIndex = 0;
    
    for (int i = startPt; i < raceSets.length; i++) {
      if (eliminated[i] == 0) {
        individualStats[i].calculateDerived();
        if (m_debug) {
          System.err.println("Remaining error: " + raceBitSets[i].toString() + " " + mean);
        }
        
        if (mean < bestError) {
          bestError = mean;
          bestIndex = i;
        }
      }
    }
    
    double[] retInfo = new double[2];
    retInfo[0] = bestIndex;
    retInfo[1] = bestError;
    
    if (m_debug) {
      System.err.print("Best set from race : ");
      
      for (int i = 0; i < m_numAttribs; i++) {
        if (raceSets[bestIndex][i] == '1') {
          System.err.print('1');
        } else {
          System.err.print('0');
        }
      }
      System.err.println(" :" + bestError + " Processed : " + processedCount + "\n" + individualStats[bestIndex].toString());
    }
    
    return retInfo;
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("\tRaceSearch.\n\tRace type : ");
    switch (m_raceType) {
    case 0: 
      text.append("forward selection race\n\tBase set : no attributes");
      break;
    case 1: 
      text.append("backward elimination race\n\tBase set : all attributes");
      break;
    case 2: 
      text.append("schemata race\n\tBase set : no attributes");
      break;
    case 3: 
      text.append("rank race\n\tBase set : no attributes\n\t");
      text.append("Attribute evaluator : " + getAttributeEvaluator().getClass().getName() + " ");
      
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
    }
    
    

    text.append("\n\tCross validation mode : ");
    if (m_xvalType == 0) {
      text.append("10 fold");
    } else {
      text.append("Leave-one-out");
    }
    
    text.append("\n\tMerit of best subset found : ");
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
  



  protected void resetOptions()
  {
    m_sigLevel = 0.001D;
    m_delta = 0.001D;
    m_ASEval = new GainRatioAttributeEval();
    m_Ranking = null;
    m_raceType = 0;
    m_debug = false;
    m_theEvaluator = null;
    m_bestMerit = -1.7976931348623157E308D;
    m_numFolds = 10;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.26 $");
  }
}
