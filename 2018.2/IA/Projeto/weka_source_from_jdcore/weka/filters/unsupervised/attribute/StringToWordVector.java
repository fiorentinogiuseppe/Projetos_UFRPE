package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.Stopwords;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.stemmers.NullStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;














































































































public class StringToWordVector
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 8249106275278565424L;
  protected Range m_SelectedRange = new Range("first-last");
  

  private TreeMap m_Dictionary = new TreeMap();
  

  private boolean m_OutputCounts = false;
  

  private String m_Prefix = "";
  


  private int[] m_DocsCounts;
  


  private int m_NumInstances = -1;
  





  private double m_AvgDocLength = -1.0D;
  




  private int m_WordsToKeep = 1000;
  



  private double m_PeriodicPruningRate = -1.0D;
  


  private boolean m_TFTransform;
  


  protected int m_filterType = 0;
  

  public static final int FILTER_NONE = 0;
  

  public static final int FILTER_NORMALIZE_ALL = 1;
  

  public static final int FILTER_NORMALIZE_TEST_ONLY = 2;
  

  public static final Tag[] TAGS_FILTER = { new Tag(0, "No normalization"), new Tag(1, "Normalize all data"), new Tag(2, "Normalize test data only") };
  



  private boolean m_IDFTransform;
  


  private boolean m_lowerCaseTokens;
  


  private boolean m_useStoplist;
  


  private Stemmer m_Stemmer = new NullStemmer();
  

  private int m_minTermFreq = 1;
  

  private boolean m_doNotOperateOnPerClassBasis = false;
  


  private File m_Stopwords = new File(System.getProperty("user.dir"));
  

  private Tokenizer m_Tokenizer = new WordTokenizer();
  




  public StringToWordVector() {}
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tOutput word counts rather than boolean word presence.\n", "C", 0, "-C"));
    


    result.addElement(new Option("\tSpecify list of string attributes to convert to words (as weka Range).\n\t(default: select all string attributes)", "R", 1, "-R <index1,index2-index4,...>"));
    



    result.addElement(new Option("\tInvert matching sense of column indexes.", "V", 0, "-V"));
    


    result.addElement(new Option("\tSpecify a prefix for the created attribute names.\n\t(default: \"\")", "P", 1, "-P <attribute name prefix>"));
    



    result.addElement(new Option("\tSpecify approximate number of word fields to create.\n\tSurplus words will be discarded..\n\t(default: 1000)", "W", 1, "-W <number of words to keep>"));
    




    result.addElement(new Option("\tSpecify the rate (e.g., every 10% of the input dataset) at which to periodically prune the dictionary.\n\t-W prunes after creating a full dictionary. You may not have enough memory for this approach.\n\t(default: no periodic pruning)", "prune-rate", 1, "-prune-rate <rate as a percentage of dataset>"));
    




    result.addElement(new Option("\tTransform the word frequencies into log(1+fij)\n\twhere fij is the frequency of word i in jth document(instance).\n", "T", 0, "-T"));
    



    result.addElement(new Option("\tTransform each word frequency into:\n\tfij*log(num of Documents/num of documents containing word i)\n\t  where fij if frequency of word i in jth document(instance)", "I", 0, "-I"));
    




    result.addElement(new Option("\tWhether to 0=not normalize/1=normalize all data/2=normalize test data only\n\tto average length of training documents (default 0=don't normalize).", "N", 1, "-N"));
    




    result.addElement(new Option("\tConvert all tokens to lowercase before adding to the dictionary.", "L", 0, "-L"));
    



    result.addElement(new Option("\tIgnore words that are in the stoplist.", "S", 0, "-S"));
    


    result.addElement(new Option("\tThe stemmering algorihtm (classname plus parameters) to use.", "stemmer", 1, "-stemmer <spec>"));
    


    result.addElement(new Option("\tThe minimum term frequency (default = 1).", "M", 1, "-M <int>"));
    


    result.addElement(new Option("\tIf this is set, the maximum number of words and the \n\tminimum term frequency is not enforced on a per-class \n\tbasis but based on the documents in all the classes \n\t(even if a class attribute is set).", "O", 0, "-O"));
    





    result.addElement(new Option("\tA file containing stopwords to override the default ones.\n\tUsing this option automatically sets the flag ('-S') to use the\n\tstoplist if the file exists.\n\tFormat: one stopword per line, lines starting with '#'\n\tare interpreted as comments and ignored.", "stopwords", 1, "-stopwords <file>"));
    






    result.addElement(new Option("\tThe tokenizing algorihtm (classname plus parameters) to use.\n\t(default: " + WordTokenizer.class.getName() + ")", "tokenizer", 1, "-tokenizer <spec>"));
    



    return result.elements();
  }
  















































































  public void setOptions(String[] options)
    throws Exception
  {
    String value = Utils.getOption('R', options);
    if (value.length() != 0) {
      setSelectedRange(value);
    } else {
      setSelectedRange("first-last");
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    value = Utils.getOption('P', options);
    if (value.length() != 0) {
      setAttributeNamePrefix(value);
    } else {
      setAttributeNamePrefix("");
    }
    value = Utils.getOption('W', options);
    if (value.length() != 0) {
      setWordsToKeep(Integer.valueOf(value).intValue());
    } else {
      setWordsToKeep(1000);
    }
    value = Utils.getOption("prune-rate", options);
    if (value.length() > 0) {
      setPeriodicPruning(Double.parseDouble(value));
    } else {
      setPeriodicPruning(-1.0D);
    }
    value = Utils.getOption('M', options);
    if (value.length() != 0) {
      setMinTermFreq(Integer.valueOf(value).intValue());
    } else {
      setMinTermFreq(1);
    }
    setOutputWordCounts(Utils.getFlag('C', options));
    
    setTFTransform(Utils.getFlag('T', options));
    
    setIDFTransform(Utils.getFlag('I', options));
    
    setDoNotOperateOnPerClassBasis(Utils.getFlag('O', options));
    
    String nString = Utils.getOption('N', options);
    if (nString.length() != 0) {
      setNormalizeDocLength(new SelectedTag(Integer.parseInt(nString), TAGS_FILTER));
    } else {
      setNormalizeDocLength(new SelectedTag(0, TAGS_FILTER));
    }
    setLowerCaseTokens(Utils.getFlag('L', options));
    
    setUseStoplist(Utils.getFlag('S', options));
    
    String stemmerString = Utils.getOption("stemmer", options);
    if (stemmerString.length() == 0) {
      setStemmer(null);
    }
    else {
      String[] stemmerSpec = Utils.splitOptions(stemmerString);
      if (stemmerSpec.length == 0)
        throw new Exception("Invalid stemmer specification string");
      String stemmerName = stemmerSpec[0];
      stemmerSpec[0] = "";
      Stemmer stemmer = (Stemmer)Class.forName(stemmerName).newInstance();
      if ((stemmer instanceof OptionHandler))
        ((OptionHandler)stemmer).setOptions(stemmerSpec);
      setStemmer(stemmer);
    }
    
    value = Utils.getOption("stopwords", options);
    if (value.length() != 0) {
      setStopwords(new File(value));
    } else {
      setStopwords(null);
    }
    String tokenizerString = Utils.getOption("tokenizer", options);
    if (tokenizerString.length() == 0) {
      setTokenizer(new WordTokenizer());
    }
    else {
      String[] tokenizerSpec = Utils.splitOptions(tokenizerString);
      if (tokenizerSpec.length == 0)
        throw new Exception("Invalid tokenizer specification string");
      String tokenizerName = tokenizerSpec[0];
      tokenizerSpec[0] = "";
      Tokenizer tokenizer = (Tokenizer)Class.forName(tokenizerName).newInstance();
      if ((tokenizer instanceof OptionHandler))
        tokenizer.setOptions(tokenizerSpec);
      setTokenizer(tokenizer);
    }
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-R");
    result.add(getSelectedRange().getRanges());
    
    if (getInvertSelection()) {
      result.add("-V");
    }
    if (!"".equals(getAttributeNamePrefix())) {
      result.add("-P");
      result.add(getAttributeNamePrefix());
    }
    
    result.add("-W");
    result.add(String.valueOf(getWordsToKeep()));
    
    result.add("-prune-rate");
    result.add(String.valueOf(getPeriodicPruning()));
    
    if (getOutputWordCounts()) {
      result.add("-C");
    }
    if (getTFTransform()) {
      result.add("-T");
    }
    if (getIDFTransform()) {
      result.add("-I");
    }
    result.add("-N");
    result.add("" + m_filterType);
    
    if (getLowerCaseTokens()) {
      result.add("-L");
    }
    if (getUseStoplist()) {
      result.add("-S");
    }
    if (getStemmer() != null) {
      result.add("-stemmer");
      String spec = getStemmer().getClass().getName();
      if ((getStemmer() instanceof OptionHandler)) {
        spec = spec + " " + Utils.joinOptions(((OptionHandler)getStemmer()).getOptions());
      }
      result.add(spec.trim());
    }
    
    result.add("-M");
    result.add(String.valueOf(getMinTermFreq()));
    
    if (getDoNotOperateOnPerClassBasis()) {
      result.add("-O");
    }
    if (!getStopwords().isDirectory()) {
      result.add("-stopwords");
      result.add(getStopwords().getAbsolutePath());
    }
    
    result.add("-tokenizer");
    String spec = getTokenizer().getClass().getName();
    if ((getTokenizer() instanceof OptionHandler)) {
      spec = spec + " " + Utils.joinOptions(getTokenizer().getOptions());
    }
    result.add(spec.trim());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  






  public StringToWordVector(int wordsToKeep)
  {
    m_WordsToKeep = wordsToKeep;
  }
  



  private class Count
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 2157223818584474321L;
    


    public int count;
    

    public int docCount;
    


    public Count(int c)
    {
      count = c;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9565 $");
    }
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  










  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    m_SelectedRange.setUpper(instanceInfo.numAttributes() - 1);
    m_AvgDocLength = -1.0D;
    m_NumInstances = -1;
    return false;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (isFirstBatchDone()) {
      FastVector fv = new FastVector();
      int firstCopy = convertInstancewoDocNorm(instance, fv);
      Instance inst = (Instance)fv.elementAt(0);
      if (m_filterType != 0) {
        normalizeInstance(inst, firstCopy);
      }
      push(inst);
      return true;
    }
    bufferInput(instance);
    return false;
  }
  








  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    



    if (!isFirstBatchDone())
    {

      if ((getInputFormat().classIndex() >= 0) && (
        (!getInputFormat().classAttribute().isNominal()) || (getInputFormatattributeStatsgetInputFormatclassIndexmissingCount == getInputFormat().numInstances())))
      {
        m_doNotOperateOnPerClassBasis = true;
      }
      


      determineDictionary();
      

      FastVector fv = new FastVector();
      int firstCopy = 0;
      for (int i = 0; i < m_NumInstances; i++) {
        firstCopy = convertInstancewoDocNorm(getInputFormat().instance(i), fv);
      }
      

      if (m_filterType != 0) {
        m_AvgDocLength = 0.0D;
        for (int i = 0; i < fv.size(); i++) {
          Instance inst = (Instance)fv.elementAt(i);
          double docLength = 0.0D;
          for (int j = 0; j < inst.numValues(); j++) {
            if (inst.index(j) >= firstCopy) {
              docLength += inst.valueSparse(j) * inst.valueSparse(j);
            }
          }
          m_AvgDocLength += Math.sqrt(docLength);
        }
        m_AvgDocLength /= m_NumInstances;
      }
      

      if (m_filterType == 1) {
        for (int i = 0; i < fv.size(); i++) {
          normalizeInstance((Instance)fv.elementAt(i), firstCopy);
        }
      }
      

      for (int i = 0; i < fv.size(); i++) {
        push((Instance)fv.elementAt(i));
      }
    }
    

    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  





  public String globalInfo()
  {
    return "Converts String attributes into a set of attributes representing word occurrence (depending on the tokenizer) information from the text contained in the strings. The set of words (attributes) is determined by the first batch filtered (typically training data).";
  }
  









  public boolean getOutputWordCounts()
  {
    return m_OutputCounts;
  }
  





  public void setOutputWordCounts(boolean outputWordCounts)
  {
    m_OutputCounts = outputWordCounts;
  }
  





  public String outputWordCountsTipText()
  {
    return "Output word counts rather than boolean 0 or 1(indicating presence or absence of a word).";
  }
  





  public Range getSelectedRange()
  {
    return m_SelectedRange;
  }
  




  public void setSelectedRange(String newSelectedRange)
  {
    m_SelectedRange = new Range(newSelectedRange);
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  







  public String getAttributeIndices()
  {
    return m_SelectedRange.getRanges();
  }
  








  public void setAttributeIndices(String rangeList)
  {
    m_SelectedRange.setRanges(rangeList);
  }
  








  public void setAttributeIndicesArray(int[] attributes)
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  





  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected attributes in the range will be worked on; if true, only non-selected attributes will be processed.";
  }
  






  public boolean getInvertSelection()
  {
    return m_SelectedRange.getInvert();
  }
  




  public void setInvertSelection(boolean invert)
  {
    m_SelectedRange.setInvert(invert);
  }
  




  public String getAttributeNamePrefix()
  {
    return m_Prefix;
  }
  




  public void setAttributeNamePrefix(String newPrefix)
  {
    m_Prefix = newPrefix;
  }
  





  public String attributeNamePrefixTipText()
  {
    return "Prefix for the created attribute names. (default: \"\")";
  }
  







  public int getWordsToKeep()
  {
    return m_WordsToKeep;
  }
  






  public void setWordsToKeep(int newWordsToKeep)
  {
    m_WordsToKeep = newWordsToKeep;
  }
  





  public String wordsToKeepTipText()
  {
    return "The number of words (per class if there is a class attribute assigned) to attempt to keep.";
  }
  






  public double getPeriodicPruning()
  {
    return m_PeriodicPruningRate;
  }
  





  public void setPeriodicPruning(double newPeriodicPruning)
  {
    m_PeriodicPruningRate = newPeriodicPruning;
  }
  





  public String periodicPruningTipText()
  {
    return "Specify the rate (x% of the input dataset) at which to periodically prune the dictionary. wordsToKeep prunes after creating a full dictionary. You may not have enough memory for this approach.";
  }
  






  public boolean getTFTransform()
  {
    return m_TFTransform;
  }
  




  public void setTFTransform(boolean TFTransform)
  {
    m_TFTransform = TFTransform;
  }
  





  public String TFTransformTipText()
  {
    return "Sets whether if the word frequencies should be transformed into:\n    log(1+fij) \n       where fij is the frequency of word i in document (instance) j.";
  }
  








  public boolean getIDFTransform()
  {
    return m_IDFTransform;
  }
  






  public void setIDFTransform(boolean IDFTransform)
  {
    m_IDFTransform = IDFTransform;
  }
  





  public String IDFTransformTipText()
  {
    return "Sets whether if the word frequencies in a document should be transformed into: \n   fij*log(num of Docs/num of Docs with word i) \n      where fij is the frequency of word i in document (instance) j.";
  }
  









  public SelectedTag getNormalizeDocLength()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  





  public void setNormalizeDocLength(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  





  public String normalizeDocLengthTipText()
  {
    return "Sets whether if the word frequencies for a document (instance) should be normalized or not.";
  }
  




  public boolean getLowerCaseTokens()
  {
    return m_lowerCaseTokens;
  }
  





  public void setLowerCaseTokens(boolean downCaseTokens)
  {
    m_lowerCaseTokens = downCaseTokens;
  }
  





  public String doNotOperateOnPerClassBasisTipText()
  {
    return "If this is set, the maximum number of words and the minimum term frequency is not enforced on a per-class basis but based on the documents in all the classes (even if a class attribute is set).";
  }
  






  public boolean getDoNotOperateOnPerClassBasis()
  {
    return m_doNotOperateOnPerClassBasis;
  }
  



  public void setDoNotOperateOnPerClassBasis(boolean newDoNotOperateOnPerClassBasis)
  {
    m_doNotOperateOnPerClassBasis = newDoNotOperateOnPerClassBasis;
  }
  





  public String minTermFreqTipText()
  {
    return "Sets the minimum term frequency. This is enforced on a per-class basis.";
  }
  




  public int getMinTermFreq()
  {
    return m_minTermFreq;
  }
  



  public void setMinTermFreq(int newMinTermFreq)
  {
    m_minTermFreq = newMinTermFreq;
  }
  





  public String lowerCaseTokensTipText()
  {
    return "If set then all the word tokens are converted to lower case before being added to the dictionary.";
  }
  





  public boolean getUseStoplist()
  {
    return m_useStoplist;
  }
  





  public void setUseStoplist(boolean useStoplist)
  {
    m_useStoplist = useStoplist;
  }
  





  public String useStoplistTipText()
  {
    return "Ignores all the words that are on the stoplist, if set to true.";
  }
  






  public void setStemmer(Stemmer value)
  {
    if (value != null) {
      m_Stemmer = value;
    } else {
      m_Stemmer = new NullStemmer();
    }
  }
  



  public Stemmer getStemmer()
  {
    return m_Stemmer;
  }
  





  public String stemmerTipText()
  {
    return "The stemming algorithm to use on the words.";
  }
  






  public void setStopwords(File value)
  {
    if (value == null) {
      value = new File(System.getProperty("user.dir"));
    }
    m_Stopwords = value;
    if ((value.exists()) && (value.isFile())) {
      setUseStoplist(true);
    }
  }
  




  public File getStopwords()
  {
    return m_Stopwords;
  }
  





  public String stopwordsTipText()
  {
    return "The file containing the stopwords (if this is a directory then the default ones are used).";
  }
  




  public void setTokenizer(Tokenizer value)
  {
    m_Tokenizer = value;
  }
  




  public Tokenizer getTokenizer()
  {
    return m_Tokenizer;
  }
  





  public String tokenizerTipText()
  {
    return "The tokenizing algorithm to use on the strings.";
  }
  





  private static void sortArray(int[] array)
  {
    int N = array.length - 1;
    
    for (int h = 1; h <= N / 9; h = 3 * h + 1) {}
    for (; 
        h > 0; h /= 3) {
      for (int i = h + 1; i <= N; i++) {
        int v = array[i];
        int j = i;
        while ((j > h) && (array[(j - h)] > v)) {
          array[j] = array[(j - h)];
          j -= h;
        }
        array[j] = v;
      }
    }
  }
  



  private void determineSelectedRange()
  {
    Instances inputFormat = getInputFormat();
    

    if (m_SelectedRange == null) {
      StringBuffer fields = new StringBuffer();
      for (int j = 0; j < inputFormat.numAttributes(); j++) {
        if (inputFormat.attribute(j).type() == 2)
          fields.append(j + 1 + ",");
      }
      m_SelectedRange = new Range(fields.toString());
    }
    m_SelectedRange.setUpper(inputFormat.numAttributes() - 1);
    

    StringBuffer fields = new StringBuffer();
    for (int j = 0; j < inputFormat.numAttributes(); j++) {
      if ((m_SelectedRange.isInRange(j)) && (inputFormat.attribute(j).type() == 2))
      {
        fields.append(j + 1 + ","); }
    }
    m_SelectedRange.setRanges(fields.toString());
    m_SelectedRange.setUpper(inputFormat.numAttributes() - 1);
  }
  





  private void determineDictionary()
  {
    Stopwords stopwords = new Stopwords();
    if (getUseStoplist()) {
      try {
        if ((getStopwords().exists()) && (!getStopwords().isDirectory())) {
          stopwords.read(getStopwords());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    

    int classInd = getInputFormat().classIndex();
    int values = 1;
    if ((!m_doNotOperateOnPerClassBasis) && (classInd != -1)) {
      values = getInputFormat().attribute(classInd).numValues();
    }
    

    TreeMap[] dictionaryArr = new TreeMap[values];
    for (int i = 0; i < values; i++) {
      dictionaryArr[i] = new TreeMap();
    }
    

    determineSelectedRange();
    

    long pruneRate = Math.round(m_PeriodicPruningRate / 100.0D * getInputFormat().numInstances());
    
    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance instance = getInputFormat().instance(i);
      int vInd = 0;
      if ((!m_doNotOperateOnPerClassBasis) && (classInd != -1)) {
        vInd = (int)instance.classValue();
      }
      

      Hashtable h = new Hashtable();
      for (int j = 0; j < instance.numAttributes(); j++) {
        if ((m_SelectedRange.isInRange(j)) && (!instance.isMissing(j)))
        {

          m_Tokenizer.tokenize(instance.stringValue(j));
          


          while (m_Tokenizer.hasMoreElements()) {
            String word = ((String)m_Tokenizer.nextElement()).intern();
            
            if (m_lowerCaseTokens == true) {
              word = word.toLowerCase();
            }
            word = m_Stemmer.stem(word);
            
            if ((m_useStoplist != true) || 
              (!stopwords.is(word)))
            {

              if (!h.containsKey(word)) {
                h.put(word, new Integer(0));
              }
              Count count = (Count)dictionaryArr[vInd].get(word);
              if (count == null) {
                dictionaryArr[vInd].put(word, new Count(1));
              } else {
                count += 1;
              }
            }
          }
        }
      }
      

      Enumeration e = h.keys();
      while (e.hasMoreElements()) {
        String word = (String)e.nextElement();
        Count c = (Count)dictionaryArr[vInd].get(word);
        if (c != null) {
          docCount += 1;
        } else {
          System.err.println("Warning: A word should definitely be in the dictionary.Please check the code");
        }
      }
      

      if ((pruneRate > 0L) && 
        (i % pruneRate == 0L) && (i > 0)) {
        for (int z = 0; z < values; z++) {
          Vector d = new Vector(1000);
          Iterator it = dictionaryArr[z].keySet().iterator();
          while (it.hasNext()) {
            String word = (String)it.next();
            Count count = (Count)dictionaryArr[z].get(word);
            if (count <= 1) d.add(word);
          }
          Iterator iter = d.iterator();
          while (iter.hasNext()) {
            String word = (String)iter.next();
            dictionaryArr[z].remove(word);
          }
        }
      }
    }
    


    int totalsize = 0;
    int[] prune = new int[values];
    for (int z = 0; z < values; z++) {
      totalsize += dictionaryArr[z].size();
      
      int[] array = new int[dictionaryArr[z].size()];
      int pos = 0;
      Iterator it = dictionaryArr[z].keySet().iterator();
      while (it.hasNext()) {
        String word = (String)it.next();
        Count count = (Count)dictionaryArr[z].get(word);
        array[pos] = count;
        pos++;
      }
      

      sortArray(array);
      if (array.length < m_WordsToKeep)
      {

        prune[z] = m_minTermFreq;
      }
      else {
        prune[z] = Math.max(m_minTermFreq, array[(array.length - m_WordsToKeep)]);
      }
    }
    



    FastVector attributes = new FastVector(totalsize + getInputFormat().numAttributes());
    


    int classIndex = -1;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if (!m_SelectedRange.isInRange(i)) {
        if (getInputFormat().classIndex() == i) {
          classIndex = attributes.size();
        }
        attributes.addElement(getInputFormat().attribute(i).copy());
      }
    }
    


    TreeMap newDictionary = new TreeMap();
    int index = attributes.size();
    for (int z = 0; z < values; z++) {
      Iterator it = dictionaryArr[z].keySet().iterator();
      while (it.hasNext()) {
        String word = (String)it.next();
        Count count = (Count)dictionaryArr[z].get(word);
        if ((count >= prune[z]) && 
          (newDictionary.get(word) == null)) {
          newDictionary.put(word, new Integer(index++));
          attributes.addElement(new Attribute(m_Prefix + word));
        }
      }
    }
    


    m_DocsCounts = new int[attributes.size()];
    Iterator it = newDictionary.keySet().iterator();
    while (it.hasNext()) {
      String word = (String)it.next();
      int idx = ((Integer)newDictionary.get(word)).intValue();
      int docsCount = 0;
      for (int j = 0; j < values; j++) {
        Count c = (Count)dictionaryArr[j].get(word);
        if (c != null)
          docsCount += docCount;
      }
      m_DocsCounts[idx] = docsCount;
    }
    

    attributes.trimToSize();
    m_Dictionary = newDictionary;
    m_NumInstances = getInputFormat().numInstances();
    

    Instances outputFormat = new Instances(getInputFormat().relationName(), attributes, 0);
    
    outputFormat.setClassIndex(classIndex);
    setOutputFormat(outputFormat);
  }
  








  private int convertInstancewoDocNorm(Instance instance, FastVector v)
  {
    TreeMap contained = new TreeMap();
    

    int firstCopy = 0;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if (!m_SelectedRange.isInRange(i)) {
        if ((getInputFormat().attribute(i).type() != 2) && (getInputFormat().attribute(i).type() != 4))
        {

          if (instance.value(i) != 0.0D) {
            contained.put(new Integer(firstCopy), new Double(instance.value(i)));
          }
          
        }
        else if (instance.isMissing(i)) {
          contained.put(new Integer(firstCopy), new Double(Instance.missingValue()));
        }
        else if (getInputFormat().attribute(i).type() == 2)
        {



          if (outputFormatPeek().attribute(firstCopy).numValues() == 0)
          {

            outputFormatPeek().attribute(firstCopy).addStringValue("Hack to defeat SparseInstance bug");
          }
          
          int newIndex = outputFormatPeek().attribute(firstCopy).addStringValue(instance.stringValue(i));
          
          contained.put(new Integer(firstCopy), new Double(newIndex));
        }
        else
        {
          if (outputFormatPeek().attribute(firstCopy).numValues() == 0) {
            Instances relationalHeader = outputFormatPeek().attribute(firstCopy).relation();
            

            outputFormatPeek().attribute(firstCopy).addRelation(relationalHeader);
          }
          int newIndex = outputFormatPeek().attribute(firstCopy).addRelation(instance.relationalValue(i));
          
          contained.put(new Integer(firstCopy), new Double(newIndex));
        }
        
        firstCopy++;
      }
    }
    
    for (int j = 0; j < instance.numAttributes(); j++)
    {
      if ((m_SelectedRange.isInRange(j)) && (!instance.isMissing(j)))
      {

        m_Tokenizer.tokenize(instance.stringValue(j));
        
        while (m_Tokenizer.hasMoreElements()) {
          String word = (String)m_Tokenizer.nextElement();
          if (m_lowerCaseTokens == true)
            word = word.toLowerCase();
          word = m_Stemmer.stem(word);
          Integer index = (Integer)m_Dictionary.get(word);
          if (index != null) {
            if (m_OutputCounts) {
              Double count = (Double)contained.get(index);
              if (count != null) {
                contained.put(index, new Double(count.doubleValue() + 1.0D));
              } else {
                contained.put(index, new Double(1.0D));
              }
            } else {
              contained.put(index, new Double(1.0D));
            }
          }
        }
      }
    }
    

    if (m_TFTransform == true) {
      Iterator it = contained.keySet().iterator();
      for (int i = 0; it.hasNext(); i++) {
        Integer index = (Integer)it.next();
        if (index.intValue() >= firstCopy) {
          double val = ((Double)contained.get(index)).doubleValue();
          val = Math.log(val + 1.0D);
          contained.put(index, new Double(val));
        }
      }
    }
    

    if (m_IDFTransform == true) {
      Iterator it = contained.keySet().iterator();
      for (int i = 0; it.hasNext(); i++) {
        Integer index = (Integer)it.next();
        if (index.intValue() >= firstCopy) {
          double val = ((Double)contained.get(index)).doubleValue();
          val *= Math.log(m_NumInstances / m_DocsCounts[index.intValue()]);
          
          contained.put(index, new Double(val));
        }
      }
    }
    

    double[] values = new double[contained.size()];
    int[] indices = new int[contained.size()];
    Iterator it = contained.keySet().iterator();
    for (int i = 0; it.hasNext(); i++) {
      Integer index = (Integer)it.next();
      Double value = (Double)contained.get(index);
      values[i] = value.doubleValue();
      indices[i] = index.intValue();
    }
    
    Instance inst = new SparseInstance(instance.weight(), values, indices, outputFormatPeek().numAttributes());
    
    inst.setDataset(outputFormatPeek());
    
    v.addElement(inst);
    
    return firstCopy;
  }
  








  private void normalizeInstance(Instance inst, int firstCopy)
    throws Exception
  {
    double docLength = 0.0D;
    
    if (m_AvgDocLength < 0.0D) {
      throw new Exception("Average document length not set.");
    }
    

    for (int j = 0; j < inst.numValues(); j++) {
      if (inst.index(j) >= firstCopy) {
        docLength += inst.valueSparse(j) * inst.valueSparse(j);
      }
    }
    docLength = Math.sqrt(docLength);
    

    for (int j = 0; j < inst.numValues(); j++) {
      if (inst.index(j) >= firstCopy) {
        double val = inst.valueSparse(j) * m_AvgDocLength / docLength;
        inst.setValueSparse(j, val);
        if (val == 0.0D) {
          System.err.println("setting value " + inst.index(j) + " to zero.");
          j--;
        }
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9565 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new StringToWordVector(), argv);
  }
}
