package weka.gui.beans;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Instances;
import weka.gui.Logger;

































public class TrainTestSplitMaker
  extends AbstractTrainAndTestSetProducer
  implements DataSourceListener, TrainingSetListener, TestSetListener, UserRequestAcceptor, EventConstraints, Serializable
{
  private static final long serialVersionUID = 7390064039444605943L;
  private double m_trainPercentage = 66.0D;
  private int m_randomSeed = 1;
  
  private Thread m_splitThread = null;
  
  public TrainTestSplitMaker() {
    m_visual.loadIcons("weka/gui/beans/icons/TrainTestSplitMaker.gif", "weka/gui/beans/icons/TrainTestSplittMaker_animated.gif");
    


    m_visual.setText("TrainTestSplitMaker");
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("TrainTestSplitMaker_GlobalInfo_Text");
  }
  




  public String trainPercentTipText()
  {
    Messages.getInstance();return Messages.getString("TrainTestSplitMaker_TrainPercentTipText_Text");
  }
  




  public void setTrainPercent(double newTrainPercent)
  {
    m_trainPercentage = newTrainPercent;
  }
  





  public double getTrainPercent()
  {
    return m_trainPercentage;
  }
  




  public String seedTipText()
  {
    Messages.getInstance();return Messages.getString("TrainTestSplitMaker_SeedTipText_Text");
  }
  




  public void setSeed(int newSeed)
  {
    m_randomSeed = newSeed;
  }
  




  public int getSeed()
  {
    return m_randomSeed;
  }
  




  public void acceptTrainingSet(TrainingSetEvent e)
  {
    Instances trainingSet = e.getTrainingSet();
    DataSetEvent dse = new DataSetEvent(this, trainingSet);
    acceptDataSet(dse);
  }
  




  public void acceptTestSet(TestSetEvent e)
  {
    Instances testSet = e.getTestSet();
    DataSetEvent dse = new DataSetEvent(this, testSet);
    acceptDataSet(dse);
  }
  




  public void acceptDataSet(DataSetEvent e)
  {
    if (m_splitThread == null) {
      final Instances dataSet = new Instances(e.getDataSet());
      m_splitThread = new Thread() {
        public void run() {
          try {
            dataSet.randomize(new Random(m_randomSeed));
            int trainSize = (int)Math.round(dataSet.numInstances() * m_trainPercentage / 100.0D);
            
            int testSize = dataSet.numInstances() - trainSize;
            
            Instances train = new Instances(dataSet, 0, trainSize);
            Instances test = new Instances(dataSet, trainSize, testSize);
            
            TrainingSetEvent tse = new TrainingSetEvent(TrainTestSplitMaker.this, train);
            
            m_setNumber = 1;m_maxSetNumber = 1;
            if (m_splitThread != null) {
              notifyTrainingSetProduced(tse);
            }
            

            TestSetEvent teste = new TestSetEvent(TrainTestSplitMaker.this, test);
            
            m_setNumber = 1;m_maxSetNumber = 1;
            if (m_splitThread != null) {
              notifyTestSetProduced(teste);
            }
            else if (m_logger != null) {
              Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_First") + TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Second"));
              
              Messages.getInstance();m_logger.statusMessage(TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Third"));
            }
          }
          catch (Exception ex)
          {
            stop();
            if (m_logger != null) {
              Messages.getInstance();m_logger.statusMessage(TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Fourth"));
              
              Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Fifth") + TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Sixth") + ex.getMessage());
            }
            


            ex.printStackTrace();
          } finally {
            if ((isInterrupted()) && 
              (m_logger != null)) {
              Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Sixth_Alpha") + TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Seventh"));
              
              Messages.getInstance();m_logger.statusMessage(TrainTestSplitMaker.this.statusMessagePrefix() + Messages.getString("TrainTestSplitMaker_AcceptDataSet_Run_LogMessage_Text_Eighth"));
            }
            

            TrainTestSplitMaker.this.block(false);
          }
        }
      };
      m_splitThread.setPriority(1);
      m_splitThread.start();
      

      block(true);
      
      m_splitThread = null;
    }
  }
  


  protected void notifyTestSetProduced(TestSetEvent tse)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_testListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if (m_splitThread == null) {
          break;
        }
        

        ((TestSetListener)l.elementAt(i)).acceptTestSet(tse);
      }
    }
  }
  


  protected void notifyTrainingSetProduced(TrainingSetEvent tse)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_trainingListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if (m_splitThread == null) {
          break;
        }
        

        ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(tse);
      }
    }
  }
  






  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if (m_splitThread.isAlive()) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  



  public void stop()
  {
    if ((m_listenee instanceof BeanCommon))
    {
      ((BeanCommon)m_listenee).stop();
    }
    

    if (m_splitThread != null) {
      Thread temp = m_splitThread;
      m_splitThread = null;
      temp.interrupt();
      temp.stop();
    }
  }
  





  public boolean isBusy()
  {
    return m_splitThread != null;
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_splitThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      throw new IllegalArgumentException(request + " not supported (TrainTestSplitMaker)");
    }
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (m_listenee == null) {
      return false;
    }
    
    if ((m_listenee instanceof EventConstraints)) {
      if ((((EventConstraints)m_listenee).eventGeneratable("dataSet")) || (((EventConstraints)m_listenee).eventGeneratable("trainingSet")) || (((EventConstraints)m_listenee).eventGeneratable("testSet")))
      {

        return true;
      }
      return false;
    }
    
    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}
