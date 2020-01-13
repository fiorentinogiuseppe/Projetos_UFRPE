package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import weka.core.Instances;


























public class InstancesSummaryPanel
  extends JPanel
{
  private static final long serialVersionUID = -5243579535296681063L;
  
  static { Messages.getInstance(); } protected static final String NO_SOURCE = Messages.getString("InstancesSummaryPanel_NO_SOURCE_Text");
  


  protected JLabel m_RelationNameLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_NumInstancesLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_NumAttributesLab = new JLabel(NO_SOURCE);
  




  protected Instances m_Instances;
  




  protected boolean m_showZeroInstancesAsUnknown = false;
  



  public InstancesSummaryPanel()
  {
    GridBagLayout gbLayout = new GridBagLayout();
    setLayout(gbLayout);
    Messages.getInstance();JLabel lab = new JLabel(Messages.getString("InstancesSummaryPanel_Lab_JLabel_Text_First"), 4);
    
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    GridBagConstraints gbConstraints = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 0;
    gridx = 0;
    gbLayout.setConstraints(lab, gbConstraints);
    add(lab);
    gbConstraints = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 0;
    gridx = 1;
    weightx = 100.0D;
    gridwidth = 3;
    gbLayout.setConstraints(m_RelationNameLab, gbConstraints);
    add(m_RelationNameLab);
    m_RelationNameLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
    
    Messages.getInstance();lab = new JLabel(Messages.getString("InstancesSummaryPanel_Lab_JLabel_Text_Second"), 4);
    
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    gbConstraints = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;
    gridx = 0;
    gbLayout.setConstraints(lab, gbConstraints);
    add(lab);
    gbConstraints = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 1;
    gridx = 1;
    weightx = 100.0D;
    gbLayout.setConstraints(m_NumInstancesLab, gbConstraints);
    add(m_NumInstancesLab);
    m_NumInstancesLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
    
    Messages.getInstance();lab = new JLabel(Messages.getString("InstancesSummaryPanel_Lab_JLabel_Text_Third"), 4);
    
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    gbConstraints = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;
    gridx = 2;
    gbLayout.setConstraints(lab, gbConstraints);
    add(lab);
    gbConstraints = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 1;
    gridx = 3;
    weightx = 100.0D;
    gbLayout.setConstraints(m_NumAttributesLab, gbConstraints);
    add(m_NumAttributesLab);
    m_NumAttributesLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
  }
  







  public void setShowZeroInstancesAsUnknown(boolean zeroAsUnknown)
  {
    m_showZeroInstancesAsUnknown = zeroAsUnknown;
  }
  






  public boolean getShowZeroInstancesAsUnknown()
  {
    return m_showZeroInstancesAsUnknown;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    m_RelationNameLab.setText(m_Instances.relationName());
    m_RelationNameLab.setToolTipText(m_Instances.relationName());
    m_NumInstancesLab.setText("" + ((m_showZeroInstancesAsUnknown) && (m_Instances.numInstances() == 0) ? "?" : new StringBuilder().append("").append(m_Instances.numInstances()).toString()));
    


    m_NumAttributesLab.setText("" + m_Instances.numAttributes());
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("InstancesSummaryPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      InstancesSummaryPanel p = new InstancesSummaryPanel();
      Messages.getInstance();p.setBorder(BorderFactory.createTitledBorder(Messages.getString("InstancesSummaryPanel_Main_P_SetBorder_BorderFactoryCreateTitledBorder_Text")));
      



      jf.getContentPane().add(p, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      if (args.length == 1) {
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        p.setInstances(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
