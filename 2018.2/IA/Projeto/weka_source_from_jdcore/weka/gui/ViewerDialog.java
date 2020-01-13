package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.core.Instances;
import weka.gui.arffviewer.ArffPanel;






































public class ViewerDialog
  extends JDialog
  implements ChangeListener
{
  private static final long serialVersionUID = 6747718484736047752L;
  public static final int APPROVE_OPTION = 0;
  public static final int CANCEL_OPTION = 1;
  protected int m_Result = 1;
  


  protected JButton m_OkButton;
  


  protected JButton m_CancelButton;
  

  protected JButton m_UndoButton;
  

  protected ArffPanel m_ArffPanel;
  


  public ViewerDialog(Frame parent)
  {
    super(parent, Dialog.ModalityType.DOCUMENT_MODAL);Messages.getInstance();m_OkButton = new JButton(Messages.getString("ViewerDialog_OkButton_JButton_Text"));Messages.getInstance();m_CancelButton = new JButton(Messages.getString("ViewerDialog_CancelButton_JButton_Text"));Messages.getInstance();m_UndoButton = new JButton(Messages.getString("ViewerDialog_UndoButton_JButton_Text"));m_ArffPanel = new ArffPanel();
    createDialog();
  }
  




  protected void createDialog()
  {
    Messages.getInstance();setTitle(Messages.getString("ViewerDialog_CreateDialog_SetTitle_Text"));
    
    getContentPane().setLayout(new BorderLayout());
    

    m_ArffPanel.addChangeListener(this);
    getContentPane().add(m_ArffPanel, "Center");
    

    JPanel panel = new JPanel(new FlowLayout(2));
    getContentPane().add(panel, "South");
    m_UndoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ViewerDialog.this.undo();
      }
    });
    getContentPane().add(panel, "South");
    m_CancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 1;
        setVisible(false);
      }
    });
    m_OkButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 0;
        setVisible(false);
      }
    });
    panel.add(m_UndoButton);
    panel.add(m_OkButton);
    panel.add(m_CancelButton);
    
    pack();
  }
  


  public void setInstances(Instances inst)
  {
    m_ArffPanel.setInstances(new Instances(inst));
  }
  


  public Instances getInstances()
  {
    return m_ArffPanel.getInstances();
  }
  


  protected void setButtons()
  {
    m_OkButton.setEnabled(true);
    m_CancelButton.setEnabled(true);
    m_UndoButton.setEnabled(m_ArffPanel.canUndo());
  }
  




  public boolean isChanged()
  {
    return m_ArffPanel.isChanged();
  }
  


  private void undo()
  {
    m_ArffPanel.undo();
  }
  


  public void stateChanged(ChangeEvent e)
  {
    setButtons();
  }
  




  public int showDialog()
  {
    m_Result = 1;
    setVisible(true);
    setButtons();
    return m_Result;
  }
  





  public int showDialog(Instances inst)
  {
    setInstances(inst);
    return showDialog();
  }
}
