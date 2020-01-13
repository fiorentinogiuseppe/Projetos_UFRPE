package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import weka.core.Memory;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.experiment.Experiment;
import weka.experiment.RemoteExperiment;
import weka.experiment.RemoteExperimentEvent;
import weka.experiment.RemoteExperimentListener;
import weka.gui.LogPanel;





























public class RunPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = 1691868018596872051L;
  
  static { Messages.getInstance(); } protected static final String NOT_RUNNING = Messages.getString("RunPanel_NOT_RUNNING_Text");
  

  protected JButton m_StartBut;
  

  protected JButton m_StopBut;
  

  protected LogPanel m_Log;
  

  protected Experiment m_Exp;
  

  protected Thread m_RunThread;
  

  protected ResultsPanel m_ResultsPanel;
  


  class ExperimentRunner
    extends Thread
    implements Serializable
  {
    private static final long serialVersionUID = -5591889874714150118L;
    

    Experiment m_ExpCopy;
    

    public ExperimentRunner(Experiment exp)
      throws Exception
    {
      if (exp == null) {
        Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Error_Text_First"));
      } else {
        Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Error_Text_Second") + exp.toString());
      }
      Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Error_Text_Third"));
      SerializedObject so = new SerializedObject(exp);
      Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Error_Text_Fourth"));
      m_ExpCopy = ((Experiment)so.getObject());
      Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Error_Text_Fifth"));
    }
    
    public void abortExperiment() {
      if ((m_ExpCopy instanceof RemoteExperiment)) {
        ((RemoteExperiment)m_ExpCopy).abortExperiment();
        
        m_StopBut.setEnabled(false);
      }
    }
    




    public void run()
    {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(true);
      if (m_ResultsPanel != null) {
        m_ResultsPanel.setExperiment(null);
      }
      try {
        if ((m_ExpCopy instanceof RemoteExperiment))
        {
          Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Run_Error_Text"));
          ((RemoteExperiment)m_ExpCopy).addRemoteExperimentListener(new RemoteExperimentListener()
          {
            public void remoteExperimentStatus(RemoteExperimentEvent e) {
              if (m_statusMessage) {
                statusMessage(m_messageString);
              }
              if (m_logMessage) {
                logMessage(m_messageString);
              }
              if (m_experimentFinished) {
                m_RunThread = null;
                m_StartBut.setEnabled(true);
                m_StopBut.setEnabled(false);
                statusMessage(RunPanel.NOT_RUNNING);
              }
            }
          });
        }
        Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_First"));
        Messages.getInstance();statusMessage(Messages.getString("RunPanel_ExperimentRunner_Run_StatusMessage_Text_First"));
        m_ExpCopy.initialize();
        int errors = 0;
        if (!(m_ExpCopy instanceof RemoteExperiment)) {
          Messages.getInstance();statusMessage(Messages.getString("RunPanel_ExperimentRunner_Run_StatusMessage_Text_Second"));
          while ((m_RunThread != null) && (m_ExpCopy.hasMoreIterations())) {
            try {
              Messages.getInstance();String current = Messages.getString("RunPanel_ExperimentRunner_Run_Current_Text_First");
              if (m_ExpCopy.getUsePropertyIterator()) {
                int cnum = m_ExpCopy.getCurrentPropertyNumber();
                String ctype = m_ExpCopy.getPropertyArray().getClass().getComponentType().getName();
                int lastDot = ctype.lastIndexOf('.');
                if (lastDot != -1) {
                  ctype = ctype.substring(lastDot + 1);
                }
                String cname = " " + ctype + "=" + (cnum + 1) + ":" + m_ExpCopy.getPropertyArrayValue(cnum).getClass().getName();
                

                current = current + cname;
              }
              String dname = ((File)m_ExpCopy.getDatasets().elementAt(m_ExpCopy.getCurrentDatasetNumber())).getName();
              

              Messages.getInstance();Messages.getInstance();current = current + Messages.getString("RunPanel_ExperimentRunner_Run_Current_Text_Second") + dname + Messages.getString("RunPanel_ExperimentRunner_Run_Current_Text_Third") + m_ExpCopy.getCurrentRunNumber();
              
              statusMessage(current);
              m_ExpCopy.nextIteration();
            } catch (Exception ex) {
              errors++;
              logMessage(ex.getMessage());
              ex.printStackTrace();
              boolean continueAfterError = false;
              if (continueAfterError) {
                m_ExpCopy.advanceCounters();
              } else {
                m_RunThread = null;
              }
            }
          }
          Messages.getInstance();statusMessage(Messages.getString("RunPanel_ExperimentRunner_Run_StatusMessage_Text_Third"));
          m_ExpCopy.postProcess();
          if (m_RunThread == null) {
            Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Third"));
          } else {
            Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Fourth"));
          }
          if (errors == 1) {
            Messages.getInstance();Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Fifth_Front") + errors + " " + Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Fifth_End"));
          } else {
            Messages.getInstance();Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Sixth_Front") + errors + " " + Messages.getString("RunPanel_ExperimentRunner_Run_LogMessage_Text_Sixth_End"));
          }
          statusMessage(RunPanel.NOT_RUNNING);
        } else {
          Messages.getInstance();statusMessage(Messages.getString("RunPanel_ExperimentRunner_Run_StatusMessage_Text_Fourth"));
          ((RemoteExperiment)m_ExpCopy).runExperiment();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println(ex.getMessage());
        statusMessage(ex.getMessage());
      } finally {
        if (m_ResultsPanel != null) {
          m_ResultsPanel.setExperiment(m_ExpCopy);
        }
        if (!(m_ExpCopy instanceof RemoteExperiment)) {
          m_RunThread = null;
          m_StartBut.setEnabled(true);
          m_StopBut.setEnabled(false);
          Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Run_Error_Text"));
        }
      }
    }
  }
  



  public void setResultsPanel(ResultsPanel rp)
  {
    m_ResultsPanel = rp;
  }
  
  public RunPanel()
  {
    Messages.getInstance();m_StartBut = new JButton(Messages.getString("RunPanel_StartBut_JButton_Text"));
    

    Messages.getInstance();m_StopBut = new JButton(Messages.getString("RunPanel_StopBut_JButton_Text"));
    
    m_Log = new LogPanel();
    




    m_RunThread = null;
    

    m_ResultsPanel = null;
    





























































































































































    m_StartBut.addActionListener(this);
    m_StopBut.addActionListener(this);
    m_StartBut.setEnabled(false);
    m_StopBut.setEnabled(false);
    m_StartBut.setMnemonic('S');
    m_StopBut.setMnemonic('t');
    m_Log.statusMessage(NOT_RUNNING);
    

    JPanel controls = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    controls.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    controls.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    controls.add(m_StartBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    controls.add(m_StopBut, constraints);
    setLayout(new BorderLayout());
    add(controls, "North");
    add(m_Log, "Center");
  }
  





  public RunPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_StartBut.setEnabled(m_RunThread == null);
    m_StopBut.setEnabled(m_RunThread != null);
  }
  





  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_StartBut) {
      if (m_RunThread == null) {
        boolean proceed = true;
        if (Experimenter.m_Memory.memoryIsLow()) {
          proceed = Experimenter.m_Memory.showMemoryIsLow();
        }
        if (proceed) {
          try {
            m_RunThread = new ExperimentRunner(m_Exp);
            m_RunThread.setPriority(1);
            m_RunThread.start();
          } catch (Exception ex) {
            ex.printStackTrace();
            Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_ActionPerformed_LogMessage_Text_First") + ex.getMessage());
          }
        }
      }
    }
    else if (e.getSource() == m_StopBut) {
      m_StopBut.setEnabled(false);
      Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_ActionPerformed_LogMessage_Text_Second"));
      if ((m_Exp instanceof RemoteExperiment)) {
        Messages.getInstance();logMessage(Messages.getString("RunPanel_ExperimentRunner_ActionPerformed_LogMessage_Text_Third"));
      }
      ((ExperimentRunner)m_RunThread).abortExperiment();
      
      m_RunThread = null;
    }
  }
  





  protected void logMessage(String message)
  {
    m_Log.logMessage(message);
  }
  





  protected void statusMessage(String message)
  {
    m_Log.statusMessage(message);
  }
  




  public static void main(String[] args)
  {
    try
    {
      boolean readExp = Utils.getFlag('l', args);
      String expFile = Utils.getOption('f', args);
      if ((readExp) && (expFile.length() == 0)) {
        Messages.getInstance();throw new Exception(Messages.getString("RunPanel_ExperimentRunner_Main_Exception_Text"));
      }
      Experiment exp = null;
      if (readExp) {
        FileInputStream fi = new FileInputStream(expFile);
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
        
        Object to = oi.readObject();
        if ((to instanceof RemoteExperiment)) {
          exp = (RemoteExperiment)to;
        } else {
          exp = (Experiment)to;
        }
        oi.close();
      } else {
        exp = new Experiment();
      }
      Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Main_Error_Text_First") + exp.toString());
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("RunPanel_ExperimentRunner_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      RunPanel sp = new RunPanel(exp);
      
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          Messages.getInstance();System.err.println(Messages.getString("RunPanel_ExperimentRunner_Main_Error_Text_Second") + val$sp.m_Exp.toString());
          
          jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
