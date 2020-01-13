package weka.gui.boundaryvisualizer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.rmi.Naming;
import java.util.Vector;
import javax.swing.JFrame;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Queue;
import weka.core.Utils;
import weka.experiment.Compute;
import weka.experiment.RemoteExperimentEvent;
import weka.experiment.RemoteExperimentListener;
import weka.experiment.TaskStatusInfo;






























public class BoundaryPanelDistributed
  extends BoundaryPanel
{
  private static final long serialVersionUID = -1743284397893937776L;
  protected Vector m_listeners = new Vector();
  

  protected Vector m_remoteHosts = new Vector();
  

  private Queue m_remoteHostsQueue = new Queue();
  

  private int[] m_remoteHostsStatus;
  
  private int[] m_remoteHostFailureCounts;
  
  protected static final int AVAILABLE = 0;
  
  protected static final int IN_USE = 1;
  
  protected static final int CONNECTION_FAILED = 2;
  
  protected static final int SOME_OTHER_FAILURE = 3;
  
  protected static final int MAX_FAILURES = 3;
  
  private boolean m_plottingAborted = false;
  

  private int m_removedHosts;
  

  private int m_failedCount;
  

  private int m_finishedCount;
  

  private Queue m_subExpQueue = new Queue();
  

  private int m_minTaskPollTime = 1000;
  


  private int[] m_hostPollingTime;
  



  public BoundaryPanelDistributed(int panelWidth, int panelHeight)
  {
    super(panelWidth, panelHeight);
  }
  




  public void setRemoteHosts(Vector remHosts)
  {
    m_remoteHosts = remHosts;
  }
  




  public void addRemoteExperimentListener(RemoteExperimentListener r)
  {
    m_listeners.addElement(r);
  }
  
  protected void initialize() {
    super.initialize();
    
    m_plottingAborted = false;
    m_finishedCount = 0;
    m_failedCount = 0;
    

    m_remoteHostsStatus = new int[m_remoteHosts.size()];
    m_remoteHostFailureCounts = new int[m_remoteHosts.size()];
    
    m_remoteHostsQueue = new Queue();
    
    if (m_remoteHosts.size() == 0) {
      Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_Initialize_Error_Text"));
      System.exit(1);
    }
    

    m_hostPollingTime = new int[m_remoteHosts.size()];
    for (int i = 0; i < m_remoteHosts.size(); i++) {
      m_remoteHostsQueue.push(new Integer(i));
      m_hostPollingTime[i] = m_minTaskPollTime;
    }
    

    m_subExpQueue = new Queue();
    for (int i = 0; i < m_panelHeight; i++) {
      m_subExpQueue.push(new Integer(i));
    }
    
    try
    {
      m_classifier.buildClassifier(m_trainingData);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    


    boolean[] attsToWeightOn = new boolean[m_trainingData.numAttributes()];
    attsToWeightOn[m_xAttribute] = true;
    attsToWeightOn[m_yAttribute] = true;
    
    m_dataGenerator.setWeightingDimensions(attsToWeightOn);
    try {
      m_dataGenerator.buildGenerator(m_trainingData);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  







  public void start()
    throws Exception
  {
    m_stopReplotting = true;
    if (m_trainingData == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanelDistributed_Start_Error_Text_First"));
    }
    if (m_classifier == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanelDistributed_Start_Error_Text_Second"));
    }
    if (m_dataGenerator == null) {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanelDistributed_Start_Error_Text_Third"));
    }
    if ((m_trainingData.attribute(m_xAttribute).isNominal()) || (m_trainingData.attribute(m_yAttribute).isNominal()))
    {
      Messages.getInstance();throw new Exception(Messages.getString("BoundaryPanelDistributed_Start_Error_Text_Fourth"));
    }
    
    computeMinMaxAtts();
    initialize();
    

    int totalHosts = m_remoteHostsQueue.size();
    for (int i = 0; i < totalHosts; i++) {
      availableHost(-1);
      Thread.sleep(70L);
    }
  }
  






  protected synchronized void availableHost(int hostNum)
  {
    if (hostNum >= 0) {
      if (m_remoteHostFailureCounts[hostNum] < 3) {
        m_remoteHostsQueue.push(new Integer(hostNum));
      } else {
        Messages.getInstance();Messages.getInstance();notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_AvailableHost_MaxFailuresExceededForHost_Text_Front") + (String)m_remoteHosts.elementAt(hostNum) + Messages.getString("BoundaryPanelDistributed_AvailableHost_MaxFailuresExceededForHost_Text_End"));
        

        m_removedHosts += 1;
      }
    }
    


    if (m_failedCount == 3 * m_remoteHosts.size()) {
      m_plottingAborted = true;
      Messages.getInstance();notifyListeners(false, true, true, Messages.getString("BoundaryPanelDistributed_AvailableHost_PlottingAborted_MaxFailure_Text"));
      
      return;
    }
    





    if ((m_subExpQueue.size() == 0) && (m_remoteHosts.size() == m_remoteHostsQueue.size() + m_removedHosts))
    {

      if (m_plotTrainingData) {
        plotTrainingData();
      }
      Messages.getInstance();notifyListeners(false, true, true, Messages.getString("BoundaryPanelDistributed_AvailableHost_PlottingCompleted_Text"));
      return;
    }
    

    if (checkForAllFailedHosts()) {
      return;
    }
    
    if ((m_plottingAborted) && (m_remoteHostsQueue.size() + m_removedHosts == m_remoteHosts.size()))
    {

      Messages.getInstance();notifyListeners(false, true, true, Messages.getString("BoundaryPanelDistributed_AvailableHost_PlottingAborted_AllRemoteTasks_Text"));
    }
    

    if ((!m_subExpQueue.empty()) && (!m_plottingAborted) && 
      (!m_remoteHostsQueue.empty())) {
      try
      {
        int availHost = ((Integer)m_remoteHostsQueue.pop()).intValue();
        int waitingTask = ((Integer)m_subExpQueue.pop()).intValue();
        launchNext(waitingTask, availHost);
      } catch (Exception ex) {
        ex.printStackTrace();
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
      m_plottingAborted = true;
      Messages.getInstance();notifyListeners(false, true, true, Messages.getString("BoundaryPanelDistributed_AvailableHost_PlottingAborted_AllConnection_Text"));
    }
    
    return allbad;
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
  



  protected synchronized void waitingTask(int expNum)
  {
    m_subExpQueue.push(new Integer(expNum));
  }
  
  protected void launchNext(final int wtask, final int ah)
  {
    Thread subTaskThread = new Thread() {
      public void run() {
        m_remoteHostsStatus[ah] = 1;
        
        RemoteBoundaryVisualizerSubTask vSubTask = new RemoteBoundaryVisualizerSubTask();
        
        vSubTask.setXAttribute(m_xAttribute);
        vSubTask.setYAttribute(m_yAttribute);
        vSubTask.setRowNumber(wtask);
        vSubTask.setPanelWidth(m_panelWidth);
        vSubTask.setPanelHeight(m_panelHeight);
        vSubTask.setPixHeight(m_pixHeight);
        vSubTask.setPixWidth(m_pixWidth);
        vSubTask.setClassifier(m_classifier);
        vSubTask.setDataGenerator(m_dataGenerator);
        vSubTask.setInstances(m_trainingData);
        vSubTask.setMinMaxX(m_minX, m_maxX);
        vSubTask.setMinMaxY(m_minY, m_maxY);
        vSubTask.setNumSamplesPerRegion(m_numOfSamplesPerRegion);
        vSubTask.setGeneratorSamplesBase(m_samplesBase);
        try {
          Messages.getInstance();Messages.getInstance();String name = Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_RemoteHost_Text_Front") + (String)m_remoteHosts.elementAt(ah) + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_RemoteHost_Text_End");
          Compute comp = (Compute)Naming.lookup(name);
          
          Messages.getInstance();Messages.getInstance();BoundaryPanelDistributed.this.notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_StartingRow_Text_Front") + wtask + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_StartingRow_Text_End") + (String)m_remoteHosts.elementAt(ah));
          
          Object subTaskId = comp.executeTask(vSubTask);
          boolean finished = false;
          TaskStatusInfo is = null;
          long startTime = System.currentTimeMillis();
          while (!finished) {
            try {
              Thread.sleep(Math.max(m_minTaskPollTime, m_hostPollingTime[ah]));
              

              TaskStatusInfo cs = (TaskStatusInfo)comp.checkStatus(subTaskId);
              
              if (cs.getExecutionStatus() == 3)
              {

                long runTime = System.currentTimeMillis() - startTime;
                runTime /= 4L;
                if (runTime < 1000L) {
                  runTime = 1000L;
                }
                m_hostPollingTime[ah] = ((int)runTime);
                

                RemoteResult rr = (RemoteResult)cs.getTaskResult();
                double[][] probs = rr.getProbabilities();
                
                for (int i = 0; i < m_panelWidth; i++) {
                  m_probabilityCache[wtask][i] = probs[i];
                  if (i < m_panelWidth - 1) {
                    plotPoint(i, wtask, probs[i], false);
                  } else {
                    plotPoint(i, wtask, probs[i], true);
                  }
                }
                BoundaryPanelDistributed.this.notifyListeners(false, true, false, cs.getStatusMessage());
                m_remoteHostsStatus[ah] = 0;
                incrementFinished();
                availableHost(ah);
                finished = true;
              } else if (cs.getExecutionStatus() == 2)
              {



                BoundaryPanelDistributed.this.notifyListeners(false, true, false, cs.getStatusMessage());
                
                m_remoteHostsStatus[ah] = 3;
                
                Messages.getInstance();Messages.getInstance();BoundaryPanelDistributed.this.notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_SchedulingRow_Text_Front") + wtask + " " + cs.getStatusMessage() + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_SchedulingRow_Text_End"));
                incrementFailed(ah);
                
                waitingTask(wtask);
                


                availableHost(ah);
                finished = true;
              }
              else if (is == null) {
                is = cs;
                BoundaryPanelDistributed.this.notifyListeners(false, true, false, cs.getStatusMessage());
              } else {
                RemoteResult rr = (RemoteResult)cs.getTaskResult();
                if (rr != null) {
                  int percentComplete = rr.getPercentCompleted();
                  String timeRemaining = "";
                  if ((percentComplete > 0) && (percentComplete < 100)) {
                    double timeSoFar = System.currentTimeMillis() - startTime;
                    
                    double timeToGo = (100.0D - percentComplete) / percentComplete * timeSoFar;
                    

                    if (timeToGo < m_hostPollingTime[ah]) {
                      m_hostPollingTime[ah] = ((int)timeToGo);
                    }
                    Messages.getInstance();String units = Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_UnitsSeconds_Text");
                    timeToGo /= 1000.0D;
                    if (timeToGo > 60.0D) {
                      Messages.getInstance();units = Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_UnitsMinutes_Text");
                      timeToGo /= 60.0D;
                    }
                    if (timeToGo > 60.0D) {
                      Messages.getInstance();units = Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_UnitsHours_Text");
                      timeToGo /= 60.0D;
                    }
                    Messages.getInstance();Messages.getInstance();timeRemaining = Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_Text_Front") + Utils.doubleToString(timeToGo, 1) + " " + units + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_Text_End");
                  }
                  
                  if (percentComplete < 25)
                  {
                    if (percentComplete > 0) {
                      m_hostPollingTime[ah] = ((int)(25.0D / percentComplete * m_hostPollingTime[ah]));
                    }
                    else
                    {
                      m_hostPollingTime[ah] *= 2;
                    }
                    if (m_hostPollingTime[ah] > 60000) {
                      m_hostPollingTime[ah] = 60000;
                    }
                  }
                  Messages.getInstance();Messages.getInstance();Messages.getInstance();BoundaryPanelDistributed.this.notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_Row_Text_First") + wtask + " " + percentComplete + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_Row_Text_Second") + timeRemaining + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_Row_Text_Third"));
                }
                else
                {
                  Messages.getInstance();Messages.getInstance();BoundaryPanelDistributed.this.notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_RowQueue_Text_Front") + wtask + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_TimeRemaining_RowQueue_Text_End") + (String)m_remoteHosts.elementAt(ah));
                  


                  if (m_hostPollingTime[ah] < 60000) {
                    m_hostPollingTime[ah] *= 2;
                  }
                }
                
                is = cs;
              }
            }
            catch (InterruptedException ie) {
              ie.printStackTrace();
            }
          }
        } catch (Exception ce) {
          m_remoteHostsStatus[ah] = 2;
          BoundaryPanelDistributed.access$408(BoundaryPanelDistributed.this);
          System.err.println(ce);
          ce.printStackTrace();
          Messages.getInstance();Messages.getInstance();Messages.getInstance();BoundaryPanelDistributed.this.notifyListeners(false, true, false, Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_Error_Connection_Text_First") + (String)m_remoteHosts.elementAt(ah) + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_Error_Connection_Text_Second") + wtask + Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_Error_Connection_Text_Third"));
          



          BoundaryPanelDistributed.this.checkForAllFailedHosts();
          waitingTask(wtask);
        } finally {
          if (isInterrupted()) {
            Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_LaunchNext_Run_Error_Text"));
          }
        }
      }
    };
    subTaskThread.setPriority(1);
    subTaskThread.start();
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length < 8) {
        Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_Main_Error_Text_First"));
        System.exit(1);
      }
      
      Vector hostNames = new Vector();
      try
      {
        BufferedReader br = new BufferedReader(new FileReader("hosts.vis"));
        String hostName = br.readLine();
        while (hostName != null) {
          Messages.getInstance();System.out.println(Messages.getString("BoundaryPanelDistributed_Main_Error_Text_First_Alpha") + hostName);
          hostNames.add(hostName);
          hostName = br.readLine();
        }
        br.close();
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_Main_Error_Text_Second"));
        System.exit(1);
      }
      
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("BoundaryPanelDistributed_Main_JFrame_Title_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      
      Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_Main_Error_LoadingInstances_Text") + args[0]);
      Reader r = new BufferedReader(new FileReader(args[0]));
      
      final Instances i = new Instances(r);
      i.setClassIndex(Integer.parseInt(args[1]));
      

      final int xatt = Integer.parseInt(args[2]);
      final int yatt = Integer.parseInt(args[3]);
      int base = Integer.parseInt(args[4]);
      int loc = Integer.parseInt(args[5]);
      
      int bandWidth = Integer.parseInt(args[6]);
      int panelWidth = Integer.parseInt(args[7]);
      int panelHeight = Integer.parseInt(args[8]);
      
      String classifierName = args[9];
      final BoundaryPanelDistributed bv = new BoundaryPanelDistributed(panelWidth, panelHeight);
      
      bv.addRemoteExperimentListener(new RemoteExperimentListener() {
        public void remoteExperimentStatus(RemoteExperimentEvent e) {
          if (m_experimentFinished) {
            String classifierNameNew = val$classifierName.substring(val$classifierName.lastIndexOf('.') + 1, val$classifierName.length());
            

            bv.saveImage(classifierNameNew + "_" + i.relationName() + "_X" + xatt + "_Y" + yatt + ".jpg");
          }
          else {
            System.err.println(m_messageString);
          }
        }
      });
      bv.setRemoteHosts(hostNames);
      
      jf.getContentPane().add(bv, "Center");
      jf.setSize(bv.getMinimumSize());
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      jf.pack();
      jf.setVisible(true);
      
      bv.repaint();
      

      String[] argsR = null;
      if (args.length > 10) {
        argsR = new String[args.length - 10];
        for (int j = 10; j < args.length; j++) {
          argsR[(j - 10)] = args[j];
        }
      }
      Classifier c = Classifier.forName(args[9], argsR);
      KDDataGenerator dataGen = new KDDataGenerator();
      dataGen.setKernelBandwidth(bandWidth);
      bv.setDataGenerator(dataGen);
      bv.setNumSamplesPerRegion(loc);
      bv.setGeneratorSamplesBase(base);
      bv.setClassifier(c);
      bv.setTrainingData(i);
      bv.setXAttribute(xatt);
      bv.setYAttribute(yatt);
      
      try
      {
        FileInputStream fis = new FileInputStream("colors.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        FastVector colors = (FastVector)ois.readObject();
        bv.setColors(colors);
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("BoundaryPanelDistributed_Main_Error_NoColorMapFile_Text"));
      }
      bv.start();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
