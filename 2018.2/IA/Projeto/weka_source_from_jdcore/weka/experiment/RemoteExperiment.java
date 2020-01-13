package weka.experiment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.rmi.Naming;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import weka.core.FastVector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Queue;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.xml.KOML;
import weka.core.xml.XMLOptions;
import weka.experiment.xml.XMLExperiment;







































































































































































public class RemoteExperiment
  extends Experiment
{
  static final long serialVersionUID = -7357668825635314937L;
  private FastVector m_listeners = new FastVector();
  

  protected DefaultListModel m_remoteHosts = new DefaultListModel();
  

  private Queue m_remoteHostsQueue = new Queue();
  


  private int[] m_remoteHostsStatus;
  


  private int[] m_remoteHostFailureCounts;
  


  protected static final int AVAILABLE = 0;
  


  protected static final int IN_USE = 1;
  

  protected static final int CONNECTION_FAILED = 2;
  

  protected static final int SOME_OTHER_FAILURE = 3;
  

  protected static final int MAX_FAILURES = 3;
  

  private boolean m_experimentAborted = false;
  


  private int m_removedHosts;
  

  private int m_failedCount;
  

  private int m_finishedCount;
  

  private Experiment m_baseExperiment = null;
  

  protected Experiment[] m_subExperiments;
  

  private Queue m_subExpQueue = new Queue();
  



  protected int[] m_subExpComplete;
  


  protected boolean m_splitByDataSet = true;
  








  public boolean getSplitByDataSet()
  {
    return m_splitByDataSet;
  }
  







  public void setSplitByDataSet(boolean sd)
  {
    m_splitByDataSet = sd;
  }
  



  public RemoteExperiment()
    throws Exception
  {
    this(new Experiment());
  }
  



  public RemoteExperiment(Experiment base)
    throws Exception
  {
    setBaseExperiment(base);
  }
  




  public void addRemoteExperimentListener(RemoteExperimentListener r)
  {
    m_listeners.addElement(r);
  }
  



  public Experiment getBaseExperiment()
  {
    return m_baseExperiment;
  }
  




  public void setBaseExperiment(Experiment base)
    throws Exception
  {
    if (base == null) {
      throw new Exception("Base experiment is null!");
    }
    m_baseExperiment = base;
    setRunLower(m_baseExperiment.getRunLower());
    setRunUpper(m_baseExperiment.getRunUpper());
    setResultListener(m_baseExperiment.getResultListener());
    setResultProducer(m_baseExperiment.getResultProducer());
    setDatasets(m_baseExperiment.getDatasets());
    setUsePropertyIterator(m_baseExperiment.getUsePropertyIterator());
    setPropertyPath(m_baseExperiment.getPropertyPath());
    setPropertyArray(m_baseExperiment.getPropertyArray());
    setNotes(m_baseExperiment.getNotes());
    m_ClassFirst = m_baseExperiment.m_ClassFirst;
    m_AdvanceDataSetFirst = m_baseExperiment.m_AdvanceDataSetFirst;
  }
  





  public void setNotes(String newNotes)
  {
    super.setNotes(newNotes);
    m_baseExperiment.setNotes(newNotes);
  }
  





  public void setRunLower(int newRunLower)
  {
    super.setRunLower(newRunLower);
    m_baseExperiment.setRunLower(newRunLower);
  }
  





  public void setRunUpper(int newRunUpper)
  {
    super.setRunUpper(newRunUpper);
    m_baseExperiment.setRunUpper(newRunUpper);
  }
  





  public void setResultListener(ResultListener newResultListener)
  {
    super.setResultListener(newResultListener);
    m_baseExperiment.setResultListener(newResultListener);
  }
  






  public void setResultProducer(ResultProducer newResultProducer)
  {
    super.setResultProducer(newResultProducer);
    m_baseExperiment.setResultProducer(newResultProducer);
  }
  



  public void setDatasets(DefaultListModel ds)
  {
    super.setDatasets(ds);
    m_baseExperiment.setDatasets(ds);
  }
  





  public void setUsePropertyIterator(boolean newUsePropertyIterator)
  {
    super.setUsePropertyIterator(newUsePropertyIterator);
    m_baseExperiment.setUsePropertyIterator(newUsePropertyIterator);
  }
  






  public void setPropertyPath(PropertyNode[] newPropertyPath)
  {
    super.setPropertyPath(newPropertyPath);
    m_baseExperiment.setPropertyPath(newPropertyPath);
  }
  





  public void setPropertyArray(Object newPropArray)
  {
    super.setPropertyArray(newPropArray);
    m_baseExperiment.setPropertyArray(newPropArray);
  }
  




  public void initialize()
    throws Exception
  {
    if (m_baseExperiment == null) {
      throw new Exception("No base experiment specified!");
    }
    
    m_experimentAborted = false;
    m_finishedCount = 0;
    m_failedCount = 0;
    m_RunNumber = getRunLower();
    m_DatasetNumber = 0;
    m_PropertyNumber = 0;
    m_CurrentProperty = -1;
    m_CurrentInstances = null;
    m_Finished = false;
    
    if (m_remoteHosts.size() == 0) {
      throw new Exception("No hosts specified!");
    }
    
    m_remoteHostsStatus = new int[m_remoteHosts.size()];
    m_remoteHostFailureCounts = new int[m_remoteHosts.size()];
    
    m_remoteHostsQueue = new Queue();
    
    for (int i = 0; i < m_remoteHosts.size(); i++) {
      m_remoteHostsQueue.push(new Integer(i));
    }
    

    m_subExpQueue = new Queue();
    int numExps;
    int numExps; if (getSplitByDataSet()) {
      numExps = m_baseExperiment.getDatasets().size();
    } else {
      numExps = getRunUpper() - getRunLower() + 1;
    }
    m_subExperiments = new Experiment[numExps];
    m_subExpComplete = new int[numExps];
    
    SerializedObject so = new SerializedObject(m_baseExperiment);
    
    if (getSplitByDataSet()) {
      for (int i = 0; i < m_baseExperiment.getDatasets().size(); i++) {
        m_subExperiments[i] = ((Experiment)so.getObject());
        
        DefaultListModel temp = new DefaultListModel();
        temp.addElement(m_baseExperiment.getDatasets().elementAt(i));
        m_subExperiments[i].setDatasets(temp);
        m_subExpQueue.push(new Integer(i));
      }
    } else {
      for (int i = getRunLower(); i <= getRunUpper(); i++) {
        m_subExperiments[(i - getRunLower())] = ((Experiment)so.getObject());
        
        m_subExperiments[(i - getRunLower())].setRunLower(i);
        m_subExperiments[(i - getRunLower())].setRunUpper(i);
        
        m_subExpQueue.push(new Integer(i - getRunLower()));
      }
    }
  }
  









  private synchronized void notifyListeners(boolean status, boolean log, boolean finished, String message)
  {
    if (m_listeners.size() > 0) {
      for (int i = 0; i < m_listeners.size(); i++) {
        RemoteExperimentListener r = (RemoteExperimentListener)m_listeners.elementAt(i);
        
        r.remoteExperimentStatus(new RemoteExperimentEvent(status, log, finished, message));
      }
      
    }
    else
    {
      System.err.println(message);
    }
  }
  


  public void abortExperiment()
  {
    m_experimentAborted = true;
  }
  


  protected synchronized void incrementFinished()
  {
    m_finishedCount += 1;
  }
  




  protected synchronized void incrementFailed(int hostNum)
  {
    m_failedCount += 1;
    m_remoteHostFailureCounts[hostNum] += 1;
  }
  



  protected synchronized void waitingExperiment(int expNum)
  {
    m_subExpQueue.push(new Integer(expNum));
  }
  




  private boolean checkForAllFailedHosts()
  {
    boolean allbad = true;
    for (int i = 0; i < m_remoteHostsStatus.length; i++) {
      if (m_remoteHostsStatus[i] != 2) {
        allbad = false;
        break;
      }
    }
    if (allbad) {
      abortExperiment();
      notifyListeners(false, true, true, "Experiment aborted! All connections to remote hosts failed.");
    }
    
    return allbad;
  }
  



  private String postExperimentInfo()
  {
    StringBuffer text = new StringBuffer();
    text.append(m_finishedCount + (m_splitByDataSet ? " data sets" : " runs") + " completed successfully. " + m_failedCount + " failures during running.\n");
    


    System.err.print(text.toString());
    return text.toString();
  }
  





  protected synchronized void availableHost(int hostNum)
  {
    if (hostNum >= 0) {
      if (m_remoteHostFailureCounts[hostNum] < 3) {
        m_remoteHostsQueue.push(new Integer(hostNum));
      } else {
        notifyListeners(false, true, false, "Max failures exceeded for host " + (String)m_remoteHosts.elementAt(hostNum) + ". Removed from host list.");
        

        m_removedHosts += 1;
      }
    }
    


    if (m_failedCount == 3 * m_remoteHosts.size()) {
      abortExperiment();
      notifyListeners(false, true, true, "Experiment aborted! Max failures exceeded on all remote hosts.");
      
      return;
    }
    
    if (((getSplitByDataSet()) && (m_baseExperiment.getDatasets().size() == m_finishedCount)) || ((!getSplitByDataSet()) && (getRunUpper() - getRunLower() + 1 == m_finishedCount)))
    {


      notifyListeners(false, true, false, "Experiment completed successfully.");
      notifyListeners(false, true, true, postExperimentInfo());
      return;
    }
    
    if (checkForAllFailedHosts()) {
      return;
    }
    
    if ((m_experimentAborted) && (m_remoteHostsQueue.size() + m_removedHosts == m_remoteHosts.size()))
    {
      notifyListeners(false, true, true, "Experiment aborted. All remote tasks finished.");
    }
    

    if ((!m_subExpQueue.empty()) && (!m_experimentAborted) && 
      (!m_remoteHostsQueue.empty())) {
      try
      {
        int availHost = ((Integer)m_remoteHostsQueue.pop()).intValue();
        int waitingExp = ((Integer)m_subExpQueue.pop()).intValue();
        launchNext(waitingExp, availHost);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  







  public void launchNext(final int wexp, final int ah)
  {
    Thread subExpThread = new Thread() {
      public void run() {
        m_remoteHostsStatus[ah] = 1;
        m_subExpComplete[wexp] = 1;
        RemoteExperimentSubTask expSubTsk = new RemoteExperimentSubTask();
        expSubTsk.setExperiment(m_subExperiments[wexp]);
        String subTaskType = "run :" + m_subExperiments[wexp].getRunLower();
        

        try
        {
          String name = "//" + (String)m_remoteHosts.elementAt(ah) + "/RemoteEngine";
          

          Compute comp = (Compute)Naming.lookup(name);
          
          RemoteExperiment.this.notifyListeners(false, true, false, "Starting " + subTaskType + " on host " + (String)m_remoteHosts.elementAt(ah));
          


          Object subTaskId = comp.executeTask(expSubTsk);
          boolean finished = false;
          TaskStatusInfo is = null;
          while (!finished) {
            try {
              Thread.sleep(2000L);
              
              TaskStatusInfo cs = (TaskStatusInfo)comp.checkStatus(subTaskId);
              
              if (cs.getExecutionStatus() == 3)
              {

                RemoteExperiment.this.notifyListeners(false, true, false, cs.getStatusMessage());
                m_remoteHostsStatus[ah] = 0;
                incrementFinished();
                availableHost(ah);
                finished = true;
              } else if (cs.getExecutionStatus() == 2)
              {


                RemoteExperiment.this.notifyListeners(false, true, false, cs.getStatusMessage());
                m_remoteHostsStatus[ah] = 3;
                m_subExpComplete[wexp] = 2;
                RemoteExperiment.this.notifyListeners(false, true, false, subTaskType + " " + cs.getStatusMessage() + ". Scheduling for execution on another host.");
                

                incrementFailed(ah);
                
                waitingExperiment(wexp);
                






                availableHost(ah);
                finished = true;
              }
              else if (is == null) {
                is = cs;
                RemoteExperiment.this.notifyListeners(false, true, false, cs.getStatusMessage());
              } else {
                if (cs.getStatusMessage().compareTo(is.getStatusMessage()) != 0)
                {

                  RemoteExperiment.this.notifyListeners(false, true, false, cs.getStatusMessage());
                }
                
                is = cs;
              }
            }
            catch (InterruptedException ie) {}
          }
        }
        catch (Exception ce)
        {
          m_remoteHostsStatus[ah] = 2;
          m_subExpComplete[wexp] = 0;
          System.err.println(ce);
          ce.printStackTrace();
          RemoteExperiment.this.notifyListeners(false, true, false, "Connection to " + (String)m_remoteHosts.elementAt(ah) + " failed. Scheduling " + subTaskType + " for execution on another host.");
          



          RemoteExperiment.this.checkForAllFailedHosts();
          waitingExperiment(wexp);
        } finally {
          if (isInterrupted()) {
            System.err.println("Sub exp Interupted!");
          }
        }
      }
    };
    subExpThread.setPriority(1);
    subExpThread.start();
  }
  





  public void nextIteration()
    throws Exception
  {}
  




  public void advanceCounters() {}
  




  public void postProcess() {}
  




  public void addRemoteHost(String hostname)
  {
    m_remoteHosts.addElement(hostname);
  }
  



  public DefaultListModel getRemoteHosts()
  {
    return m_remoteHosts;
  }
  



  public void setRemoteHosts(DefaultListModel list)
  {
    m_remoteHosts = list;
  }
  



  public String toString()
  {
    String result = m_baseExperiment.toString();
    
    result = result + "\nRemote Hosts:\n";
    for (int i = 0; i < m_remoteHosts.size(); i++) {
      result = result + (String)m_remoteHosts.elementAt(i) + '\n';
    }
    return result;
  }
  


  public void runExperiment()
  {
    int totalHosts = m_remoteHostsQueue.size();
    
    for (int i = 0; i < totalHosts; i++) {
      availableHost(-1);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
  




  public static void main(String[] args)
  {
    try
    {
      RemoteExperiment exp = null;
      

      String xmlOption = Utils.getOption("xml", args);
      if (!xmlOption.equals("")) {
        args = new XMLOptions(xmlOption).toArray();
      }
      Experiment base = null;
      String expFile = Utils.getOption('l', args);
      String saveFile = Utils.getOption('s', args);
      boolean runExp = Utils.getFlag('r', args);
      FastVector remoteHosts = new FastVector();
      String runHost = " ";
      while (runHost.length() != 0) {
        runHost = Utils.getOption('h', args);
        if (runHost.length() != 0) {
          remoteHosts.addElement(runHost);
        }
      }
      if (expFile.length() == 0) {
        base = new Experiment();
        try {
          base.setOptions(args);
          Utils.checkForRemainingOptions(args);
        } catch (Exception ex) {
          ex.printStackTrace();
          String result = "Usage:\n\n-l <exp file>\n\tLoad experiment from file (default use cli options)\n-s <exp file>\n\tSave experiment to file after setting other options\n\t(default don't save)\n-h <remote host name>\n\tHost to run experiment on (may be specified more than once\n\tfor multiple remote hosts)\n-r \n\tRun experiment on (default don't run)\n-xml <filename | xml-string>\n\tget options from XML-Data instead from parameters\n\n";
          












          Enumeration enm = base.listOptions();
          while (enm.hasMoreElements()) {
            Option option = (Option)enm.nextElement();
            result = result + option.synopsis() + "\n";
            result = result + option.description() + "\n";
          }
          throw new Exception(result + "\n" + ex.getMessage());
        }
      }
      else {
        Object tmp;
        Object tmp;
        if ((KOML.isPresent()) && (expFile.toLowerCase().endsWith(".koml"))) {
          tmp = KOML.read(expFile);
        }
        else {
          Object tmp;
          if (expFile.toLowerCase().endsWith(".xml")) {
            XMLExperiment xml = new XMLExperiment();
            tmp = xml.read(expFile);
          }
          else
          {
            FileInputStream fi = new FileInputStream(expFile);
            ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
            
            tmp = oi.readObject();
            oi.close();
          } }
        if ((tmp instanceof RemoteExperiment)) {
          exp = (RemoteExperiment)tmp;
        } else {
          base = (Experiment)tmp;
        }
      }
      if (base != null) {
        exp = new RemoteExperiment(base);
      }
      for (int i = 0; i < remoteHosts.size(); i++) {
        exp.addRemoteHost((String)remoteHosts.elementAt(i));
      }
      System.err.println("Experiment:\n" + exp.toString());
      
      if (saveFile.length() != 0)
      {
        if ((KOML.isPresent()) && (saveFile.toLowerCase().endsWith(".koml"))) {
          KOML.write(saveFile, exp);


        }
        else if (saveFile.toLowerCase().endsWith(".xml")) {
          XMLExperiment xml = new XMLExperiment();
          xml.write(saveFile, exp);
        }
        else
        {
          FileOutputStream fo = new FileOutputStream(saveFile);
          ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
          
          oo.writeObject(exp);
          oo.close();
        }
      }
      
      if (runExp) {
        System.err.println("Initializing...");
        exp.initialize();
        System.err.println("Iterating...");
        exp.runExperiment();
        System.err.println("Postprocessing...");
        exp.postProcess();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
