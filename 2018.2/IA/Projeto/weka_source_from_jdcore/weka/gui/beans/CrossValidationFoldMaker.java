package weka.gui.beans;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Instances;
import weka.gui.Logger;































public class CrossValidationFoldMaker
  extends AbstractTrainAndTestSetProducer
  implements DataSourceListener, TrainingSetListener, TestSetListener, UserRequestAcceptor, EventConstraints, Serializable
{
  private static final long serialVersionUID = -6350179298851891512L;
  private int m_numFolds = 10;
  private int m_randomSeed = 1;
  
  private transient Thread m_foldThread = null;
  
  public CrossValidationFoldMaker() {
    m_visual.loadIcons("weka/gui/beans/icons/CrossValidationFoldMaker.gif", "weka/gui/beans/icons/CrossValidationFoldMaker_animated.gif");
    


    m_visual.setText("CrossValidationFoldMaker");
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
    Messages.getInstance();return Messages.getString("CrossValidationFoldMaker_GlobalInfo_Text");
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
    if (e.isStructureOnly())
    {
      TrainingSetEvent tse = new TrainingSetEvent(this, e.getDataSet());
      TestSetEvent tsee = new TestSetEvent(this, e.getDataSet());
      notifyTrainingSetProduced(tse);
      notifyTestSetProduced(tsee);
      return;
    }
    if (m_foldThread == null) {
      final Instances dataSet = new Instances(e.getDataSet());
      m_foldThread = new Thread() {
        public void run() {
          boolean errorOccurred = false;
          try {
            Random random = new Random(getSeed());
            dataSet.randomize(random);
            if ((dataSet.classIndex() >= 0) && (dataSet.attribute(dataSet.classIndex()).isNominal()))
            {
              dataSet.stratify(getFolds());
              if (m_logger != null) {
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_First") + getCustomName() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Second"));
              }
            }
            
            for (int i = 0; i < getFolds(); i++) {
              if (m_foldThread == null) {
                if (m_logger == null) break;
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Third") + getCustomName() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Fourth")); break;
              }
              


              Instances train = dataSet.trainCV(getFolds(), i, random);
              Instances test = dataSet.testCV(getFolds(), i);
              

              TrainingSetEvent tse = new TrainingSetEvent(this, train);
              m_setNumber = (i + 1);m_maxSetNumber = getFolds();
              String msg = getCustomName() + "$" + hashCode() + "|";
              
              if (m_logger != null) {
                Messages.getInstance();Messages.getInstance();Messages.getInstance();m_logger.statusMessage(msg + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_First") + getSeed() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Second") + getFolds() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Third") + (i + 1));
              }
              
              if (m_foldThread != null)
              {
                notifyTrainingSetProduced(tse);
              }
              


              TestSetEvent teste = new TestSetEvent(this, test);
              m_setNumber = (i + 1);m_maxSetNumber = getFolds();
              
              if (m_logger != null) {
                Messages.getInstance();Messages.getInstance();Messages.getInstance();m_logger.statusMessage(msg + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Fourth") + getSeed() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Fifth") + getFolds() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Sixth") + (i + 1));
              }
              
              if (m_foldThread != null) {
                CrossValidationFoldMaker.this.notifyTestSetProduced(teste);
              }
            }
          }
          catch (Exception ex) {
            errorOccurred = true;
            stop();
            if (m_logger != null) {
              Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Fifth") + getCustomName() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Sixth") + ex.getMessage());
            }
            

            ex.printStackTrace();
          } finally {
            m_foldThread = null;
            if (errorOccurred) {
              if (m_logger != null) {
                Messages.getInstance();m_logger.statusMessage(getCustomName() + "$" + hashCode() + "|" + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Seventh"));
              }
              

            }
            else if (isInterrupted()) {
              Messages.getInstance();Messages.getInstance();String msg = Messages.getString("CrossValidationFoldMaker_AcceptDataSet_Msg_Text_First") + getCustomName() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_Msg_Text_Second");
              if (m_logger != null) {
                Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Fifth") + getCustomName() + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_LogMessage_Text_Sixth_Alpha"));
                Messages.getInstance();m_logger.statusMessage(getCustomName() + "$" + hashCode() + "|" + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Eighth"));
              }
              else
              {
                System.err.println(msg);
              }
            } else {
              String msg = getCustomName() + "$" + hashCode() + "|";
              
              if (m_logger != null) {
                Messages.getInstance();m_logger.statusMessage(msg + Messages.getString("CrossValidationFoldMaker_AcceptDataSet_StatusMessage_Text_Nineth"));
              }
            }
            CrossValidationFoldMaker.this.block(false);
          }
        }
      };
      m_foldThread.setPriority(1);
      m_foldThread.start();
      

      block(true);
      
      m_foldThread = null;
    }
  }
  



  private void notifyTestSetProduced(TestSetEvent tse)
  {
    Vector l;
    

    synchronized (this) {
      l = (Vector)m_testListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        if (m_foldThread == null) {
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
        if (m_foldThread == null) {
          break;
        }
        

        ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(tse);
      }
    }
  }
  




  public void setFolds(int numFolds)
  {
    m_numFolds = numFolds;
  }
  




  public int getFolds()
  {
    return m_numFolds;
  }
  




  public String foldsTipText()
  {
    Messages.getInstance();return Messages.getString("CrossValidationFoldMaker_FoldsTipText_Text");
  }
  




  public void setSeed(int randomSeed)
  {
    m_randomSeed = randomSeed;
  }
  




  public int getSeed()
  {
    return m_randomSeed;
  }
  




  public String seedTipText()
  {
    Messages.getInstance();return Messages.getString("CrossValidationFoldMaker_SeedTipText_Text");
  }
  





  public boolean isBusy()
  {
    return m_foldThread != null;
  }
  



  public void stop()
  {
    if ((m_listenee instanceof BeanCommon))
    {
      ((BeanCommon)m_listenee).stop();
    }
    

    if (m_foldThread != null) {
      Thread temp = m_foldThread;
      m_foldThread = null;
      temp.interrupt();
      temp.stop();
    }
  }
  






  private synchronized void block(boolean tf)
  {
    if (tf) {
      try
      {
        if (m_foldThread.isAlive()) {
          wait();
        }
      }
      catch (InterruptedException ex) {}
    } else {
      notifyAll();
    }
  }
  




  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if (m_foldThread != null) {
      newVector.addElement("Stop");
    }
    return newVector.elements();
  }
  





  public void performRequest(String request)
  {
    if (request.compareTo("Stop") == 0) {
      stop();
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("CrossValidationFoldMaker_PerformRequest_IllegalArgumentException_Text"));
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
}
