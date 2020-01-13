package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.Utils;
import weka.experiment.Stats;























public class AttributeSummaryPanel
  extends JPanel
{
  static final long serialVersionUID = -5434987925737735880L;
  
  static { Messages.getInstance(); } protected static final String NO_SOURCE = Messages.getString("AttributeSummaryPanel_NO_SOURCE_Text");
  

  protected JLabel m_AttributeNameLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_AttributeTypeLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_MissingLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_UniqueLab = new JLabel(NO_SOURCE);
  

  protected JLabel m_DistinctLab = new JLabel(NO_SOURCE);
  

  protected JTable m_StatsTable = new JTable()
  {
    private static final long serialVersionUID = 7165142874670048578L;
    






    public boolean isCellEditable(int row, int column)
    {
      return false;
    }
  };
  


  protected Instances m_Instances;
  

  protected AttributeStats[] m_AttributeStats;
  


  public AttributeSummaryPanel()
  {
    JPanel simple = new JPanel();
    GridBagLayout gbL = new GridBagLayout();
    simple.setLayout(gbL);
    Messages.getInstance();JLabel lab = new JLabel(Messages.getString("AttributeSummaryPanel_Lab_JLabel_Text_First"), 4);
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    GridBagConstraints gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 0;gridx = 0;
    gbL.setConstraints(lab, gbC);
    simple.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 0;gridx = 1;
    weightx = 100.0D;gridwidth = 3;
    gbL.setConstraints(m_AttributeNameLab, gbC);
    simple.add(m_AttributeNameLab);
    m_AttributeNameLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
    
    Messages.getInstance();lab = new JLabel(Messages.getString("AttributeSummaryPanel_Lab_JLabel_Text_Second"), 4);
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 0;gridx = 4;
    gbL.setConstraints(lab, gbC);
    simple.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 0;gridx = 5;
    weightx = 100.0D;
    gbL.setConstraints(m_AttributeTypeLab, gbC);
    simple.add(m_AttributeTypeLab);
    m_AttributeTypeLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
    

    Messages.getInstance();lab = new JLabel(Messages.getString("AttributeSummaryPanel_Lab_JLabel_Text_Third"), 4);
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;gridx = 0;
    gbL.setConstraints(lab, gbC);
    simple.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 1;gridx = 1;
    weightx = 100.0D;
    gbL.setConstraints(m_MissingLab, gbC);
    simple.add(m_MissingLab);
    m_MissingLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
    
    Messages.getInstance();lab = new JLabel(Messages.getString("AttributeSummaryPanel_Lab_JLabel_Text_Fourth"), 4);
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;gridx = 2;
    gbL.setConstraints(lab, gbC);
    simple.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 1;gridx = 3;
    weightx = 100.0D;
    gbL.setConstraints(m_DistinctLab, gbC);
    simple.add(m_DistinctLab);
    m_DistinctLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
    
    Messages.getInstance();lab = new JLabel(Messages.getString("AttributeSummaryPanel_Lab_JLabel_Text_Fifth"), 4);
    lab.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 0));
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;gridx = 4;
    gbL.setConstraints(lab, gbC);
    simple.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 1;gridx = 5;
    weightx = 100.0D;
    gbL.setConstraints(m_UniqueLab, gbC);
    simple.add(m_UniqueLab);
    m_UniqueLab.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
    
    setLayout(new BorderLayout());
    add(simple, "North");
    add(new JScrollPane(m_StatsTable), "Center");
    m_StatsTable.getSelectionModel().setSelectionMode(0);
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    m_AttributeStats = new AttributeStats[inst.numAttributes()];
    m_AttributeNameLab.setText(NO_SOURCE);
    m_AttributeTypeLab.setText(NO_SOURCE);
    m_MissingLab.setText(NO_SOURCE);
    m_UniqueLab.setText(NO_SOURCE);
    m_DistinctLab.setText(NO_SOURCE);
    m_StatsTable.setModel(new DefaultTableModel());
  }
  





  public void setAttribute(final int index)
  {
    setHeader(index);
    if (m_AttributeStats[index] == null) {
      Thread t = new Thread() {
        public void run() {
          m_AttributeStats[index] = m_Instances.attributeStats(index);
          
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              setDerived(val$index);
              m_StatsTable.sizeColumnsToFit(-1);
              m_StatsTable.revalidate();
              m_StatsTable.repaint();
            }
          });
        }
      };
      t.setPriority(1);
      t.start();
    } else {
      setDerived(index);
    }
  }
  






  protected void setDerived(int index)
  {
    AttributeStats as = m_AttributeStats[index];
    long percent = Math.round(100.0D * missingCount / totalCount);
    m_MissingLab.setText("" + missingCount + " (" + percent + "%)");
    percent = Math.round(100.0D * uniqueCount / totalCount);
    m_UniqueLab.setText("" + uniqueCount + " (" + percent + "%)");
    m_DistinctLab.setText("" + distinctCount);
    setTable(as, index);
  }
  






  protected void setTable(AttributeStats as, int index)
  {
    if (nominalCounts != null) {
      Attribute att = m_Instances.attribute(index); Object[] 
        tmp20_17 = new Object[3];Messages.getInstance();tmp20_17[0] = Messages.getString("AttributeSummaryPanel_SetTable_ColNames_Text_First"); Object[] tmp32_20 = tmp20_17;Messages.getInstance();tmp32_20[1] = Messages.getString("AttributeSummaryPanel_SetTable_ColNames_Text_Second"); Object[] tmp44_32 = tmp32_20;Messages.getInstance();tmp44_32[2] = Messages.getString("AttributeSummaryPanel_SetTable_ColNames_Text_Third");Object[] colNames = tmp44_32;
      Object[][] data = new Object[nominalCounts.length][3];
      for (int i = 0; i < nominalCounts.length; i++) {
        data[i][0] = new Integer(i + 1);
        data[i][1] = att.value(i);
        data[i][2] = new Integer(nominalCounts[i]);
      }
      m_StatsTable.setModel(new DefaultTableModel(data, colNames));
      m_StatsTable.getColumnModel().getColumn(0).setMaxWidth(60);
      DefaultTableCellRenderer tempR = new DefaultTableCellRenderer();
      tempR.setHorizontalAlignment(4);
      m_StatsTable.getColumnModel().getColumn(0).setCellRenderer(tempR);
    } else if (numericStats != null) {
      Object[] tmp224_221 = new Object[2];Messages.getInstance();tmp224_221[0] = Messages.getString("AttributeSummaryPanel_SetTable_ColNames_Text_Fourth"); Object[] tmp236_224 = tmp224_221;Messages.getInstance();tmp236_224[1] = Messages.getString("AttributeSummaryPanel_SetTable_ColNames_Text_Fifth");Object[] colNames = tmp236_224;
      Object[][] data = new Object[4][2];
      Messages.getInstance();data[0][0] = Messages.getString("AttributeSummaryPanel_SetTable_Data_0_Text");data[0][1] = Utils.doubleToString(numericStats.min, 3);
      Messages.getInstance();data[1][0] = Messages.getString("AttributeSummaryPanel_SetTable_Data_1_Text");data[1][1] = Utils.doubleToString(numericStats.max, 3);
      Messages.getInstance();data[2][0] = Messages.getString("AttributeSummaryPanel_SetTable_Data_2_Text");data[2][1] = Utils.doubleToString(numericStats.mean, 3);
      Messages.getInstance();data[3][0] = Messages.getString("AttributeSummaryPanel_SetTable_Data_3_Text");data[3][1] = Utils.doubleToString(numericStats.stdDev, 3);
      m_StatsTable.setModel(new DefaultTableModel(data, colNames));
    } else {
      m_StatsTable.setModel(new DefaultTableModel());
    }
    m_StatsTable.getColumnModel().setColumnMargin(4);
  }
  






  protected void setHeader(int index)
  {
    Attribute att = m_Instances.attribute(index);
    m_AttributeNameLab.setText(att.name());
    switch (att.type()) {
    case 1: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeNOMINAL_Text"));
      break;
    case 0: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeNUMERIC_Text"));
      break;
    case 2: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeSTRING_Text"));
      break;
    case 3: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeDATE_Text"));
      break;
    case 4: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeRELATIONAL_Text"));
      break;
    default: 
      Messages.getInstance();m_AttributeTypeLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_AttributeDEFAULT_TEXT"));
    }
    
    Messages.getInstance();m_MissingLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_MissingLab_SetText_Text"));
    Messages.getInstance();m_UniqueLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_UniqueLab_SetText_Text"));
    Messages.getInstance();m_DistinctLab.setText(Messages.getString("AttributeSummaryPanel_SetHeader_DistinctLab_SetText_Text"));
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributeSummaryPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      AttributeSummaryPanel p = new AttributeSummaryPanel();
      Messages.getInstance();p.setBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSummaryPanel_Main_P_SetBorder_BorderFactoryCreateTitledBorder")));
      jf.getContentPane().add(p, "Center");
      final JComboBox j = new JComboBox();
      j.setEnabled(false);
      j.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          val$p.setAttribute(j.getSelectedIndex());
        }
      });
      jf.getContentPane().add(j, "North");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      if (args.length == 1) {
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances inst = new Instances(r);
        p.setInstances(inst);
        p.setAttribute(0);
        String[] names = new String[inst.numAttributes()];
        for (int i = 0; i < names.length; i++) {
          names[i] = inst.attribute(i).name();
        }
        j.setModel(new DefaultComboBoxModel(names));
        j.setEnabled(true);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
