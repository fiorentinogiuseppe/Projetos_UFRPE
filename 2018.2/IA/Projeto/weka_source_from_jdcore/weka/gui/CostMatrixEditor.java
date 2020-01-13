package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import weka.classifiers.CostMatrix;








































public class CostMatrixEditor
  implements PropertyEditor
{
  private CostMatrix m_matrix;
  private PropertyChangeSupport m_propSupport;
  private CustomEditor m_customEditor;
  private JFileChooser m_fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  




  private class CostMatrixTableModel
    extends AbstractTableModel
  {
    static final long serialVersionUID = -2762326138357037181L;
    



    private CostMatrixTableModel() {}
    



    public int getRowCount()
    {
      return m_matrix.size();
    }
    






    public int getColumnCount()
    {
      return m_matrix.size();
    }
    







    public Object getValueAt(int row, int column)
    {
      try
      {
        return m_matrix.getCell(row, column);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      return new Double(0.0D);
    }
    






    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
      Double val;
      




      try
      {
        val = new Double((String)aValue);
        value = val.doubleValue();
      } catch (Exception ex) { double value;
        val = null;
      }
      if (val == null) {
        m_matrix.setCell(rowIndex, columnIndex, aValue);
      } else {
        m_matrix.setCell(rowIndex, columnIndex, val);
      }
      fireTableCellUpdated(rowIndex, columnIndex);
    }
    









    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return true;
    }
    








    public Class getColumnClass(int columnIndex)
    {
      return Object.class;
    }
  }
  



  private class CustomEditor
    extends JPanel
    implements ActionListener, TableModelListener
  {
    static final long serialVersionUID = -2931593489871197274L;
    


    private CostMatrixEditor.CostMatrixTableModel m_tableModel;
    


    private JButton m_defaultButton;
    


    private JButton m_openButton;
    


    private JButton m_saveButton;
    


    private JTextField m_classesField;
    

    private JButton m_resizeButton;
    


    public CustomEditor()
    {
      Messages.getInstance();m_fileChooser.setFileFilter(new ExtensionFileFilter(CostMatrix.FILE_EXTENSION, Messages.getString("CostMatrixEditor_CustomEditor_FileChooser_SetFileFilter_Text")));
      


      m_fileChooser.setFileSelectionMode(0);
      

      Messages.getInstance();m_defaultButton = new JButton(Messages.getString("CostMatrixEditor_CustomEditor_DefaultButton_JButton_Text"));
      Messages.getInstance();m_openButton = new JButton(Messages.getString("CostMatrixEditor_CustomEditor_OpenButton_JButton_Text"));
      Messages.getInstance();m_saveButton = new JButton(Messages.getString("CostMatrixEditor_CustomEditor_SaveButton_JButton_Text"));
      Messages.getInstance();m_resizeButton = new JButton(Messages.getString("CostMatrixEditor_CustomEditor_ResizeButton_JButton_Text"));
      m_classesField = new JTextField("" + m_matrix.size());
      
      m_defaultButton.addActionListener(this);
      m_openButton.addActionListener(this);
      m_saveButton.addActionListener(this);
      m_resizeButton.addActionListener(this);
      m_classesField.addActionListener(this);
      

      JPanel classesPanel = new JPanel();
      classesPanel.setLayout(new GridLayout(1, 2, 0, 0));
      Messages.getInstance();classesPanel.add(new JLabel(Messages.getString("CostMatrixEditor_CustomEditor_ClassesPanel_JLabel_Text"), 4));
      classesPanel.add(m_classesField);
      
      JPanel rightPanel = new JPanel();
      
      GridBagLayout gridBag = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      rightPanel.setLayout(gridBag);
      gridx = 0;gridy = -1;
      insets = new Insets(2, 10, 2, 10);
      fill = 2;
      gridBag.setConstraints(m_defaultButton, gbc);
      rightPanel.add(m_defaultButton);
      
      gridBag.setConstraints(m_openButton, gbc);
      rightPanel.add(m_openButton);
      
      gridBag.setConstraints(m_saveButton, gbc);
      rightPanel.add(m_saveButton);
      
      gridBag.setConstraints(classesPanel, gbc);
      rightPanel.add(classesPanel);
      
      gridBag.setConstraints(m_resizeButton, gbc);
      rightPanel.add(m_resizeButton);
      
      JPanel fill = new JPanel();
      weightx = 1.0D;weighty = 1.0D;
      fill = 1;
      
      gridBag.setConstraints(fill, gbc);
      rightPanel.add(fill);
      
      m_tableModel = new CostMatrixEditor.CostMatrixTableModel(CostMatrixEditor.this, null);
      m_tableModel.addTableModelListener(this);
      JTable matrixTable = new JTable(m_tableModel);
      
      setLayout(new BorderLayout());
      add(matrixTable, "Center");
      add(rightPanel, "East");
    }
    





    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() == m_defaultButton) {
        m_matrix.initialize();
        matrixChanged();
      } else if (e.getSource() == m_openButton) {
        openMatrix();
      } else if (e.getSource() == m_saveButton) {
        saveMatrix();
      } else if ((e.getSource() == m_classesField) || (e.getSource() == m_resizeButton))
      {
        try {
          int newNumClasses = Integer.parseInt(m_classesField.getText());
          if ((newNumClasses > 0) && (newNumClasses != m_matrix.size())) {
            setValue(new CostMatrix(newNumClasses));
          }
        }
        catch (Exception ex) {}
      }
    }
    




    public void tableChanged(TableModelEvent e)
    {
      m_propSupport.firePropertyChange(null, null, null);
    }
    




    public void matrixChanged()
    {
      m_tableModel.fireTableStructureChanged();
      m_classesField.setText("" + m_matrix.size());
    }
    




    private void openMatrix()
    {
      int returnVal = m_fileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        File selectedFile = m_fileChooser.getSelectedFile();
        Reader reader = null;
        try {
          reader = new BufferedReader(new FileReader(selectedFile));
          m_matrix = new CostMatrix(reader);
          
          reader.close();
          matrixChanged();
        } catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("CostMatrixEditor_OpenMatrix_JOptionPaneShowMessageDialog_Text_First") + selectedFile.getName() + Messages.getString("CostMatrixEditor_OpenMatrix_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("CostMatrixEditor_OpenMatrix_JOptionPaneShowMessageDialog_Text_Third"), 0);
          




          System.out.println(ex.getMessage());
        }
      }
    }
    




    private void saveMatrix()
    {
      int returnVal = m_fileChooser.showSaveDialog(this);
      if (returnVal == 0) {
        File selectedFile = m_fileChooser.getSelectedFile();
        

        if (!selectedFile.getName().toLowerCase().endsWith(CostMatrix.FILE_EXTENSION))
        {
          selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + CostMatrix.FILE_EXTENSION);
        }
        


        Writer writer = null;
        try {
          writer = new BufferedWriter(new FileWriter(selectedFile));
          m_matrix.write(writer);
          writer.close();
        } catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("CostMatrixEditor_SaveMatrix_JOptionPaneShowMessageDialog_Text_First") + selectedFile.getName() + Messages.getString("CostMatrixEditor_SaveMatrix_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("CostMatrixEditor_SaveMatrix_JOptionPaneShowMessageDialog_Text_Third"), 0);
          




          System.out.println(ex.getMessage());
        }
      }
    }
  }
  




  public CostMatrixEditor()
  {
    m_matrix = new CostMatrix(2);
    m_propSupport = new PropertyChangeSupport(this);
    m_customEditor = new CustomEditor();
  }
  





  public void setValue(Object value)
  {
    m_matrix = ((CostMatrix)value);
    m_customEditor.matrixChanged();
  }
  





  public Object getValue()
  {
    return m_matrix;
  }
  






  public boolean isPaintable()
  {
    return true;
  }
  








  public void paintValue(Graphics gfx, Rectangle box)
  {
    gfx.drawString(m_matrix.size() + " x " + m_matrix.size() + " cost matrix", x, y + height);
  }
  








  public String getJavaInitializationString()
  {
    return "new CostMatrix(" + m_matrix.size() + ")";
  }
  





  public String getAsText()
  {
    return null;
  }
  





  public void setAsText(String text)
  {
    Messages.getInstance();throw new IllegalArgumentException(Messages.getString("CostMatrixEditor_SetAsText_IllegalArgumentException_Text"));
  }
  





  public String[] getTags()
  {
    return null;
  }
  





  public Component getCustomEditor()
  {
    return m_customEditor;
  }
  





  public boolean supportsCustomEditor()
  {
    return true;
  }
  






  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupport.addPropertyChangeListener(listener);
  }
  






  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    m_propSupport.removePropertyChangeListener(listener);
  }
}
