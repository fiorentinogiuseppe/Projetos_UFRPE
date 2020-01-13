package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import weka.experiment.Experiment;



























public class SetupModePanel
  extends JPanel
{
  private static final long serialVersionUID = -3758035565520727822L;
  protected JRadioButton m_SimpleSetupRBut;
  protected JRadioButton m_AdvancedSetupRBut;
  protected SimpleSetupPanel m_simplePanel;
  protected SetupPanel m_advancedPanel;
  
  public SetupModePanel()
  {
    Messages.getInstance();m_SimpleSetupRBut = new JRadioButton(Messages.getString("SetupModePanel_SimpleSetupRBut_JRadioButton_Text"));
    


    Messages.getInstance();m_AdvancedSetupRBut = new JRadioButton(Messages.getString("SetupModePanel_AdvancedSetupRBut_JRadioButton_Text"));
    


    m_simplePanel = new SimpleSetupPanel();
    

    m_advancedPanel = new SetupPanel();
    





    m_simplePanel.setModePanel(this);
    
    m_SimpleSetupRBut.setMnemonic('S');
    m_SimpleSetupRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchToSimple(null);
      }
      
    });
    m_AdvancedSetupRBut.setMnemonic('A');
    m_AdvancedSetupRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchToAdvanced(null);
      }
      
    });
    ButtonGroup modeBG = new ButtonGroup();
    modeBG.add(m_SimpleSetupRBut);
    modeBG.add(m_AdvancedSetupRBut);
    m_SimpleSetupRBut.setSelected(true);
    
    JPanel modeButtons = new JPanel();
    modeButtons.setLayout(new GridLayout(1, 0));
    modeButtons.add(m_SimpleSetupRBut);
    modeButtons.add(m_AdvancedSetupRBut);
    
    JPanel switchPanel = new JPanel();
    switchPanel.setLayout(new GridLayout(1, 0));
    Messages.getInstance();switchPanel.add(new JLabel(Messages.getString("SetupModePanel_SwitchPanel_JPanel_Text")));
    switchPanel.add(modeButtons);
    
    setLayout(new BorderLayout());
    add(switchPanel, "North");
    add(m_simplePanel, "Center");
  }
  





  public void switchToAdvanced(Experiment exp)
  {
    if (exp == null) {
      exp = m_simplePanel.getExperiment();
    }
    if (exp != null) {
      m_AdvancedSetupRBut.setSelected(true);
      m_advancedPanel.setExperiment(exp);
    }
    remove(m_simplePanel);
    m_simplePanel.removeNotesFrame();
    add(m_advancedPanel, "Center");
    validate();
    repaint();
  }
  





  public void switchToSimple(Experiment exp)
  {
    if (exp == null) {
      exp = m_advancedPanel.getExperiment();
    }
    if ((exp != null) && (!m_simplePanel.setExperiment(exp))) {
      m_AdvancedSetupRBut.setSelected(true);
      switchToAdvanced(exp);
    } else {
      remove(m_advancedPanel);
      m_advancedPanel.removeNotesFrame();
      add(m_simplePanel, "Center");
      validate();
      repaint();
    }
  }
  





  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_simplePanel.addPropertyChangeListener(l);
    m_advancedPanel.addPropertyChangeListener(l);
  }
  





  public Experiment getExperiment()
  {
    if (m_SimpleSetupRBut.isSelected()) return m_simplePanel.getExperiment();
    return m_advancedPanel.getExperiment();
  }
}
