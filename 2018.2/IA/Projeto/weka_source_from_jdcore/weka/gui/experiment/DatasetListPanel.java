package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import weka.core.ClassDiscovery.StringCompare;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.Saver;
import weka.experiment.Experiment;
import weka.gui.ConverterFileChooser;
import weka.gui.JListHelper;
import weka.gui.ViewerDialog;


























































public class DatasetListPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = 7068857852794405769L;
  protected Experiment m_Exp;
  protected JList m_List;
  protected JButton m_AddBut;
  protected JButton m_EditBut;
  protected JButton m_DeleteBut;
  protected JButton m_UpBut;
  protected JButton m_DownBut;
  protected JCheckBox m_relativeCheck;
  protected ConverterFileChooser m_FileChooser;
  
  public DatasetListPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  
  public DatasetListPanel()
  {
    Messages.getInstance();m_AddBut = new JButton(Messages.getString("DatasetListPanel_AddBut_JButton_Text"));
    

    Messages.getInstance();m_EditBut = new JButton(Messages.getString("DatasetListPanel_EditBut_JButton_Text"));
    

    Messages.getInstance();m_DeleteBut = new JButton(Messages.getString("DatasetListPanel_DeleteBut_JButton_Text"));
    

    Messages.getInstance();m_UpBut = new JButton(Messages.getString("DatasetListPanel_UpBut_JButton_Text"));
    

    Messages.getInstance();m_DownBut = new JButton(Messages.getString("DatasetListPanel_DownBut_JButton_Text"));
    

    Messages.getInstance();m_relativeCheck = new JCheckBox(Messages.getString("DatasetListPanel_RelativeCheck_JCheckBox_Text"));
    




    m_FileChooser = new ConverterFileChooser(ExperimenterDefaults.getInitialDatasetsDirectory());
    


















    m_List = new JList();
    m_List.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        DatasetListPanel.this.setButtons(e);
      }
    });
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
        {



          int index = m_List.locationToIndex(e.getPoint());
          if (index > -1)
            actionPerformed(new ActionEvent(m_EditBut, 0, ""));
        }
      }
    };
    m_List.addMouseListener(mouseListener);
    

    m_FileChooser.setCoreConvertersOnly(true);
    m_FileChooser.setMultiSelectionEnabled(true);
    m_FileChooser.setFileSelectionMode(2);
    m_FileChooser.setAcceptAllFileFilterUsed(false);
    m_DeleteBut.setEnabled(false);
    m_DeleteBut.addActionListener(this);
    m_AddBut.setEnabled(false);
    m_AddBut.addActionListener(this);
    m_EditBut.setEnabled(false);
    m_EditBut.addActionListener(this);
    m_UpBut.setEnabled(false);
    m_UpBut.addActionListener(this);
    m_DownBut.setEnabled(false);
    m_DownBut.addActionListener(this);
    m_relativeCheck.setSelected(ExperimenterDefaults.getUseRelativePaths());
    Messages.getInstance();m_relativeCheck.setToolTipText(Messages.getString("DatasetListPanel_RelativeCheck_SetToolTipText_Text"));
    setLayout(new BorderLayout());
    Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("DatasetListPanel_RelativeCheck_SetBorder_Text")));
    JPanel topLab = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    topLab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    topLab.setLayout(gb);
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    topLab.add(m_AddBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    topLab.add(m_EditBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    topLab.add(m_DeleteBut, constraints);
    
    gridx = 0;gridy = 1;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    topLab.add(m_relativeCheck, constraints);
    
    JPanel bottomLab = new JPanel();
    gb = new GridBagLayout();
    constraints = new GridBagConstraints();
    bottomLab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    bottomLab.setLayout(gb);
    
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    bottomLab.add(m_UpBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    bottomLab.add(m_DownBut, constraints);
    
    add(topLab, "North");
    add(new JScrollPane(m_List), "Center");
    add(bottomLab, "South");
  }
  





  private void setButtons(ListSelectionEvent e)
  {
    if ((e == null) || (e.getSource() == m_List)) {
      m_DeleteBut.setEnabled(m_List.getSelectedIndex() > -1);
      m_EditBut.setEnabled(m_List.getSelectedIndices().length == 1);
      m_UpBut.setEnabled(JListHelper.canMoveUp(m_List));
      m_DownBut.setEnabled(JListHelper.canMoveDown(m_List));
    }
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_List.setModel(m_Exp.getDatasets());
    m_AddBut.setEnabled(true);
    setButtons(null);
  }
  






  protected void getFilesRecursively(File directory, Vector files)
  {
    try
    {
      String[] currentDirFiles = directory.list();
      for (int i = 0; i < currentDirFiles.length; i++) {
        currentDirFiles[i] = (directory.getCanonicalPath() + File.separator + currentDirFiles[i]);
        
        File current = new File(currentDirFiles[i]);
        if (m_FileChooser.getFileFilter().accept(current)) {
          if (current.isDirectory()) {
            getFilesRecursively(current, files);
          } else {
            files.addElement(current);
          }
        }
      }
    } catch (Exception e) {
      Messages.getInstance();System.err.println(Messages.getString("DatasetListPanel_GetFilesRecursively_Error_Text"));
    }
  }
  




  public void actionPerformed(ActionEvent e)
  {
    boolean useRelativePaths = m_relativeCheck.isSelected();
    
    if (e.getSource() == m_AddBut)
    {
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        if (m_FileChooser.isMultiSelectionEnabled()) {
          File[] selected = m_FileChooser.getSelectedFiles();
          for (int i = 0; i < selected.length; i++) {
            if (selected[i].isDirectory()) {
              Vector files = new Vector();
              getFilesRecursively(selected[i], files);
              

              Collections.sort(files, new ClassDiscovery.StringCompare());
              
              for (int j = 0; j < files.size(); j++) {
                File temp = (File)files.elementAt(j);
                if (useRelativePaths) {
                  try {
                    temp = Utils.convertToRelativePath(temp);
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                }
                m_Exp.getDatasets().addElement(temp);
              }
            } else {
              File temp = selected[i];
              if (useRelativePaths) {
                try {
                  temp = Utils.convertToRelativePath(temp);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
              m_Exp.getDatasets().addElement(temp);
            }
          }
          setButtons(null);
        } else {
          if (m_FileChooser.getSelectedFile().isDirectory()) {
            Vector files = new Vector();
            getFilesRecursively(m_FileChooser.getSelectedFile(), files);
            

            Collections.sort(files, new ClassDiscovery.StringCompare());
            
            for (int j = 0; j < files.size(); j++) {
              File temp = (File)files.elementAt(j);
              if (useRelativePaths) {
                try {
                  temp = Utils.convertToRelativePath(temp);
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
              m_Exp.getDatasets().addElement(temp);
            }
          } else {
            File temp = m_FileChooser.getSelectedFile();
            if (useRelativePaths) {
              try {
                temp = Utils.convertToRelativePath(temp);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
            m_Exp.getDatasets().addElement(temp);
          }
          setButtons(null);
        }
      }
    } else if (e.getSource() == m_DeleteBut)
    {
      int[] selected = m_List.getSelectedIndices();
      if (selected != null) {
        for (int i = selected.length - 1; i >= 0; i--) {
          int current = selected[i];
          m_Exp.getDatasets().removeElementAt(current);
          if (m_Exp.getDatasets().size() > current) {
            m_List.setSelectedIndex(current);
          } else {
            m_List.setSelectedIndex(current - 1);
          }
        }
      }
      setButtons(null);
    } else if (e.getSource() == m_EditBut)
    {
      int selected = m_List.getSelectedIndex();
      if (selected != -1) {
        ViewerDialog dialog = new ViewerDialog(null);
        String filename = m_List.getSelectedValue().toString();
        try
        {
          ConverterUtils.DataSource source = new ConverterUtils.DataSource(filename);
          int result = dialog.showDialog(source.getDataSet());
          


          source = null;
          System.gc();
          
          if ((result == 0) && (dialog.isChanged())) {
            Messages.getInstance();result = JOptionPane.showConfirmDialog(this, Messages.getString("DatasetListPanel_ActionPerformed_Result_JOptionPaneShowConfirmDialog_Text"));
            

            if (result == 0) {
              Saver saver = ConverterUtils.getSaverForFile(filename);
              saver.setFile(new File(filename));
              saver.setInstances(dialog.getInstances());
              saver.writeBatch();
            }
          }
        }
        catch (Exception ex) {
          Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_First") + filename + Messages.getString("DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_Second") + ex.toString(), Messages.getString("DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_Third"), 1);
        }
      }
      



      setButtons(null);
    } else if (e.getSource() == m_UpBut) {
      JListHelper.moveUp(m_List);
    } else if (e.getSource() == m_DownBut) {
      JListHelper.moveDown(m_List);
    }
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("DatasetListPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      DatasetListPanel dp = new DatasetListPanel();
      jf.getContentPane().add(dp, "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();System.err.println(Messages.getString("DatasetListPanel_Main_Error_Text_First"));
      Thread.currentThread();Thread.sleep(3000L);
      Messages.getInstance();System.err.println(Messages.getString("DatasetListPanel_Main_Error_Text_Second"));
      dp.setExperiment(new Experiment());
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
