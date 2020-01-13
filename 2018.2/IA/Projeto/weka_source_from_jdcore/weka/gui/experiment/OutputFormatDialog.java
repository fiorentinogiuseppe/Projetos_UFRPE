package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import weka.experiment.ResultMatrix;
import weka.experiment.ResultMatrixPlainText;
import weka.gui.GenericObjectEditor;


































public class OutputFormatDialog
  extends JDialog
{
  private static final long serialVersionUID = 2169792738187807378L;
  public static final int APPROVE_OPTION = 0;
  public static final int CANCEL_OPTION = 1;
  protected int m_Result = 1;
  

  protected static Vector m_OutputFormatClasses = null;
  

  protected static Vector m_OutputFormatNames = null;
  



  static
  {
    Vector classes = GenericObjectEditor.getClassnames(ResultMatrix.class.getName());
    

    m_OutputFormatClasses = new Vector();
    m_OutputFormatNames = new Vector();
    for (int i = 0; i < classes.size(); i++) {
      try {
        Class cls = Class.forName(classes.get(i).toString());
        ResultMatrix matrix = (ResultMatrix)cls.newInstance();
        m_OutputFormatClasses.add(cls);
        m_OutputFormatNames.add(matrix.getDisplayName());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  

  protected Class m_ResultMatrix = ResultMatrixPlainText.class;
  

  protected JComboBox m_OutputFormatComboBox = new JComboBox(m_OutputFormatNames);
  

  protected JSpinner m_MeanPrecSpinner = new JSpinner();
  

  protected JSpinner m_StdDevPrecSpinner = new JSpinner();
  

  protected JCheckBox m_ShowAverageCheckBox = new JCheckBox("");
  

  protected JCheckBox m_RemoveFilterNameCheckBox = new JCheckBox("");
  


  protected JButton m_OkButton;
  


  protected JButton m_CancelButton;
  


  protected int m_MeanPrec;
  

  protected int m_StdDevPrec;
  

  protected boolean m_RemoveFilterName;
  

  protected boolean m_ShowAverage;
  


  public OutputFormatDialog(Frame parent)
  {
    super(parent, Messages.getString("OutputFormatDialog_Title_Text"), Dialog.ModalityType.DOCUMENT_MODAL);Messages.getInstance();m_OkButton = new JButton(Messages.getString("OutputFormatDialog_OkButton_JButton_Text"));Messages.getInstance();m_CancelButton = new JButton(Messages.getString("OutputFormatDialog_CancelButton_JButton_Text"));m_MeanPrec = 2;m_StdDevPrec = 2;m_RemoveFilterName = false;m_ShowAverage = false;
    createDialog();
  }
  






  protected void createDialog()
  {
    getContentPane().setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new GridLayout(5, 2));
    getContentPane().add(panel, "Center");
    

    SpinnerNumberModel model = (SpinnerNumberModel)m_MeanPrecSpinner.getModel();
    model.setMaximum(new Integer(20));
    model.setMinimum(new Integer(0));
    model = (SpinnerNumberModel)m_StdDevPrecSpinner.getModel();
    model.setMaximum(new Integer(20));
    model.setMinimum(new Integer(0));
    Messages.getInstance();JLabel label = new JLabel(Messages.getString("OutputFormatDialog_CreateDialog_MeanPrecision_JLabel_Text"));
    label.setDisplayedMnemonic('M');
    label.setLabelFor(m_MeanPrecSpinner);
    panel.add(label);
    panel.add(m_MeanPrecSpinner);
    Messages.getInstance();label = new JLabel(Messages.getString("OutputFormatDialog_CreateDialog_StdDevPrecision_JLabel_Text"));
    label.setDisplayedMnemonic('S');
    label.setLabelFor(m_StdDevPrecSpinner);
    panel.add(label);
    panel.add(m_StdDevPrecSpinner);
    

    Messages.getInstance();label = new JLabel(Messages.getString("OutputFormatDialog_CreateDialog_OutputFormat_JLabel_Text"));
    label.setDisplayedMnemonic('F');
    label.setLabelFor(m_OutputFormatComboBox);
    panel.add(label);
    panel.add(m_OutputFormatComboBox);
    m_OutputFormatComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OutputFormatDialog.this.getData();
      }
      

    });
    Messages.getInstance();label = new JLabel(Messages.getString("OutputFormatDialog_CreateDialog_ShowAverage_JLabel_Text"));
    label.setDisplayedMnemonic('A');
    label.setLabelFor(m_ShowAverageCheckBox);
    panel.add(label);
    panel.add(m_ShowAverageCheckBox);
    

    Messages.getInstance();label = new JLabel(Messages.getString("OutputFormatDialog_CreateDialog_RemoveFilterClassnames_JLabel_Text"));
    label.setDisplayedMnemonic('R');
    label.setLabelFor(m_RemoveFilterNameCheckBox);
    panel.add(label);
    panel.add(m_RemoveFilterNameCheckBox);
    

    panel = new JPanel(new FlowLayout(2));
    getContentPane().add(panel, "South");
    m_CancelButton.setMnemonic('C');
    m_CancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 1;
        setVisible(false);
      }
    });
    m_OkButton.setMnemonic('O');
    m_OkButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OutputFormatDialog.this.getData();
        m_Result = 0;
        setVisible(false);
      }
    });
    panel.add(m_OkButton);
    panel.add(m_CancelButton);
    

    getRootPane().setDefaultButton(m_OkButton);
    
    pack();
  }
  



  private void setData()
  {
    m_MeanPrecSpinner.setValue(new Integer(m_MeanPrec));
    m_StdDevPrecSpinner.setValue(new Integer(m_StdDevPrec));
    

    m_ShowAverageCheckBox.setSelected(m_ShowAverage);
    

    m_RemoveFilterNameCheckBox.setSelected(m_RemoveFilterName);
    

    for (int i = 0; i < m_OutputFormatClasses.size(); i++) {
      if (m_OutputFormatClasses.get(i).equals(m_ResultMatrix)) {
        m_OutputFormatComboBox.setSelectedItem(m_OutputFormatNames.get(i));
        break;
      }
    }
  }
  



  private void getData()
  {
    m_MeanPrec = Integer.parseInt(m_MeanPrecSpinner.getValue().toString());
    m_StdDevPrec = Integer.parseInt(m_StdDevPrecSpinner.getValue().toString());
    

    m_ShowAverage = m_ShowAverageCheckBox.isSelected();
    

    m_RemoveFilterName = m_RemoveFilterNameCheckBox.isSelected();
    

    m_ResultMatrix = ((Class)m_OutputFormatClasses.get(m_OutputFormatComboBox.getSelectedIndex()));
  }
  





  public void setMeanPrec(int precision)
  {
    m_MeanPrec = precision;
  }
  




  public int getMeanPrec()
  {
    return m_MeanPrec;
  }
  




  public void setStdDevPrec(int precision)
  {
    m_StdDevPrec = precision;
  }
  



  public int getStdDevPrec()
  {
    return m_StdDevPrec;
  }
  




  public void setResultMatrix(Class matrix)
  {
    m_ResultMatrix = matrix;
  }
  




  public Class getResultMatrix()
  {
    return m_ResultMatrix;
  }
  




  public void setRemoveFilterName(boolean remove)
  {
    m_RemoveFilterName = remove;
  }
  




  public boolean getRemoveFilterName()
  {
    return m_RemoveFilterName;
  }
  




  public void setShowAverage(boolean show)
  {
    m_ShowAverage = show;
  }
  




  public boolean getShowAverage()
  {
    return m_ShowAverage;
  }
  


  protected void setFormat()
  {
    for (int i = 0; i < m_OutputFormatClasses.size(); i++) {
      if (m_OutputFormatNames.get(i).toString().equals(m_OutputFormatComboBox.getItemAt(i).toString()))
      {
        m_OutputFormatComboBox.setSelectedIndex(i);
        break;
      }
    }
  }
  







  public int getResult()
  {
    return m_Result;
  }
  




  public int showDialog()
  {
    m_Result = 1;
    setData();
    setVisible(true);
    return m_Result;
  }
  






  public static void main(String[] args)
  {
    OutputFormatDialog dialog = new OutputFormatDialog(null);
    if (dialog.showDialog() == 0) {
      Messages.getInstance();System.out.println(Messages.getString("OutputFormatDialog_Main_Accepted_Text"));
    } else {
      Messages.getInstance();System.out.println(Messages.getString("OutputFormatDialog_Main_Aborted_Text"));
    }
  }
}
