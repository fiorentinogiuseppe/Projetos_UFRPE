package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import weka.experiment.Experiment;
import weka.experiment.RemoteExperiment;



































public class DistributeExperimentPanel
  extends JPanel
{
  private static final long serialVersionUID = 5206721431754800278L;
  RemoteExperiment m_Exp = null;
  

  protected JCheckBox m_enableDistributedExperiment = new JCheckBox();
  protected JButton m_configureHostNames;
  protected HostListPanel m_hostList;
  
  public DistributeExperimentPanel() { Messages.getInstance();m_configureHostNames = new JButton(Messages.getString("DistributeExperimentPanel_ConfigureHostNames_JButton_Text"));
    

    m_hostList = new HostListPanel();
    



    Messages.getInstance();m_splitByDataSet = new JRadioButton(Messages.getString("DistributeExperimentPanel_SplitByDataSet_JRadioButton_Text"));
    



    Messages.getInstance();m_splitByRun = new JRadioButton(Messages.getString("DistributeExperimentPanel_SplitByRun_JRadioButton_Text"));
    

    m_radioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DistributeExperimentPanel.this.updateRadioLinks();


      }
      


    };
    m_enableDistributedExperiment.setSelected(false);
    Messages.getInstance();m_enableDistributedExperiment.setToolTipText(Messages.getString("DistributeExperimentPanel_EnableDistributedExperiment_SetToolTipText_Text"));
    
    m_enableDistributedExperiment.setEnabled(false);
    m_configureHostNames.setEnabled(false);
    Messages.getInstance();m_configureHostNames.setToolTipText(Messages.getString("DistributeExperimentPanel_ConfigureHostNames_SetToolTipText_Text"));
    
    m_enableDistributedExperiment.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_configureHostNames.setEnabled(m_enableDistributedExperiment.isSelected());
        
        m_splitByDataSet.setEnabled(m_enableDistributedExperiment.isSelected());
        
        m_splitByRun.setEnabled(m_enableDistributedExperiment.isSelected());
      }
      

    });
    m_configureHostNames.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DistributeExperimentPanel.this.popupHostPanel();
      }
      
    });
    Messages.getInstance();m_splitByDataSet.setToolTipText(Messages.getString("DistributeExperimentPanel_SetBorder_BorderFactoryCreateTitledBorder_Text"));
    Messages.getInstance();m_splitByRun.setToolTipText(Messages.getString("DistributeExperimentPanel_SplitByRun_SetToolTipText_Text"));
    m_splitByDataSet.setSelected(true);
    m_splitByDataSet.setEnabled(false);
    m_splitByRun.setEnabled(false);
    m_splitByDataSet.addActionListener(m_radioListener);
    m_splitByRun.addActionListener(m_radioListener);
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_splitByDataSet);
    bg.add(m_splitByRun);
    
    JPanel rbuts = new JPanel();
    rbuts.setLayout(new GridLayout(1, 2));
    rbuts.add(m_splitByDataSet);
    rbuts.add(m_splitByRun);
    
    setLayout(new BorderLayout());
    Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("DistributeExperimentPanel_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    add(m_enableDistributedExperiment, "West");
    add(m_configureHostNames, "Center");
    add(rbuts, "South");
  }
  

  protected JRadioButton m_splitByDataSet;
  protected JRadioButton m_splitByRun;
  ActionListener m_radioListener;
  public DistributeExperimentPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  




  public void setExperiment(Experiment exp)
  {
    m_enableDistributedExperiment.setEnabled(true);
    m_Exp = null;
    if ((exp instanceof RemoteExperiment)) {
      m_Exp = ((RemoteExperiment)exp);
      m_enableDistributedExperiment.setSelected(true);
      m_configureHostNames.setEnabled(true);
      m_hostList.setExperiment(m_Exp);
      m_splitByDataSet.setEnabled(true);
      m_splitByRun.setEnabled(true);
      m_splitByDataSet.setSelected(m_Exp.getSplitByDataSet());
      m_splitByRun.setSelected(!m_Exp.getSplitByDataSet());
    }
  }
  

  private void popupHostPanel()
  {
    try
    {
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("DistributeExperimentPanel_PopupHostPanel_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(m_hostList, "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  



  public boolean distributedExperimentSelected()
  {
    return m_enableDistributedExperiment.isSelected();
  }
  



  public void addCheckBoxActionListener(ActionListener al)
  {
    m_enableDistributedExperiment.addActionListener(al);
  }
  


  private void updateRadioLinks()
  {
    if (m_Exp != null) {
      m_Exp.setSplitByDataSet(m_splitByDataSet.isSelected());
    }
  }
  



  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("DistributeExperimentPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(new DistributeExperimentPanel(new Experiment()), "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
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
