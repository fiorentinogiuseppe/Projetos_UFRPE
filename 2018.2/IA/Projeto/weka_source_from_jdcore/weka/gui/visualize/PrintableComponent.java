package weka.gui.visualize;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
































































public class PrintableComponent
  implements PrintableHandler
{
  protected JComponent m_Component;
  protected static JFileChooser m_FileChooserPanel;
  protected static JCheckBox m_CustomDimensionsCheckBox;
  protected static JTextField m_CustomWidthText;
  protected static JTextField m_CustomHeightText;
  protected static JCheckBox m_AspectRatioCheckBox;
  protected String m_SaveDialogTitle;
  protected double m_xScale;
  protected double m_yScale;
  protected double m_AspectRatio;
  protected boolean m_IgnoreChange;
  private static final boolean DEBUG = false;
  protected static boolean m_ToolTipUserAsked = false;
  

  protected static final String PROPERTY_SHOW = "PrintableComponentToolTipShow";
  

  protected static final String PROPERTY_USERASKED = "PrintableComponentToolTipUserAsked";
  

  protected static boolean m_ShowToolTip = true;
  
  static {
    try { m_ShowToolTip = Boolean.valueOf(VisualizeUtils.VISUALIZE_PROPERTIES.getProperty("PrintableComponentToolTipShow", "true")).booleanValue();
      


      m_ToolTipUserAsked = Boolean.valueOf(VisualizeUtils.VISUALIZE_PROPERTIES.getProperty("PrintableComponentToolTipUserAsked", "false")).booleanValue();


    }
    catch (Exception e)
    {

      m_ToolTipUserAsked = false;
      m_ShowToolTip = true;
    }
  }
  
  public PrintableComponent(JComponent component)
  {
    Messages.getInstance();m_SaveDialogTitle = Messages.getString("PrintableComponent_SaveDialogTitle_Text");
    

    m_xScale = 1.0D;
    

    m_yScale = 1.0D;
    




















































    m_Component = component;
    m_AspectRatio = NaN.0D;
    
    getComponent().addMouseListener(new PrintMouseListener(this));
    getComponent().setToolTipText(getToolTipText(this));
    initFileChooser();
  }
  




  public JComponent getComponent()
  {
    return m_Component;
  }
  

















  public static String getToolTipText(PrintableComponent component)
  {
    return null;
  }
  









































































  protected void initFileChooser()
  {
    if (m_FileChooserPanel != null) {
      return;
    }
    m_FileChooserPanel = new JFileChooser();
    m_FileChooserPanel.resetChoosableFileFilters();
    m_FileChooserPanel.setAcceptAllFileFilterUsed(false);
    

    JPanel accessory = new JPanel();
    accessory.setLayout(null);
    accessory.setPreferredSize(new Dimension(200, 200));
    accessory.revalidate();
    m_FileChooserPanel.setAccessory(accessory);
    
    Messages.getInstance();m_CustomDimensionsCheckBox = new JCheckBox(Messages.getString("PrintableComponent_InitFileChooser_CustomDimensionsCheckBox_JCheckBox_Text"));
    m_CustomDimensionsCheckBox.setBounds(14, 7, 200, 21);
    m_CustomDimensionsCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean custom = PrintableComponent.m_CustomDimensionsCheckBox.isSelected();
        PrintableComponent.m_CustomWidthText.setEnabled(custom);
        PrintableComponent.m_CustomHeightText.setEnabled(custom);
        PrintableComponent.m_AspectRatioCheckBox.setEnabled(custom);
        if (custom) {
          m_IgnoreChange = true;
          PrintableComponent.m_CustomWidthText.setText("" + m_Component.getWidth());
          PrintableComponent.m_CustomHeightText.setText("" + m_Component.getHeight());
          m_IgnoreChange = false;
        }
        else {
          m_IgnoreChange = true;
          PrintableComponent.m_CustomWidthText.setText("-1");
          PrintableComponent.m_CustomHeightText.setText("-1");
          m_IgnoreChange = false;
        }
      }
    });
    accessory.add(m_CustomDimensionsCheckBox);
    
    m_CustomWidthText = new JTextField(5);
    m_CustomWidthText.setText("-1");
    m_CustomWidthText.setEnabled(false);
    m_CustomWidthText.setBounds(65, 35, 50, 21);
    m_CustomWidthText.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomWidthText);
      }
      
      public void insertUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomWidthText);
      }
      
      public void removeUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomWidthText);
      }
    });
    Messages.getInstance();JLabel label = new JLabel(Messages.getString("PrintableComponent_InitFileChooser_Label_Jlabel_Text_First"));
    label.setLabelFor(m_CustomWidthText);
    label.setDisplayedMnemonic('W');
    label.setBounds(14, 35, 50, 21);
    accessory.add(label);
    accessory.add(m_CustomWidthText);
    
    m_CustomHeightText = new JTextField(5);
    m_CustomHeightText.setText("-1");
    m_CustomHeightText.setEnabled(false);
    m_CustomHeightText.setBounds(65, 63, 50, 21);
    m_CustomHeightText.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomHeightText);
      }
      
      public void insertUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomHeightText);
      }
      
      public void removeUpdate(DocumentEvent e) {
        updateDimensions(PrintableComponent.m_CustomHeightText);
      }
    });
    Messages.getInstance();label = new JLabel(Messages.getString("PrintableComponent_InitFileChooser_Label_Jlabel_Text_Second"));
    label.setLabelFor(m_CustomHeightText);
    label.setDisplayedMnemonic('H');
    label.setBounds(14, 63, 50, 21);
    accessory.add(label);
    accessory.add(m_CustomHeightText);
    
    Messages.getInstance();m_AspectRatioCheckBox = new JCheckBox(Messages.getString("PrintableComponent_InitFileChooser_AspectRatioCheckBox_JCheckBox_Text"));
    m_AspectRatioCheckBox.setBounds(14, 91, 200, 21);
    m_AspectRatioCheckBox.setEnabled(false);
    m_AspectRatioCheckBox.setSelected(true);
    m_AspectRatioCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean keep = PrintableComponent.m_AspectRatioCheckBox.isSelected();
        if (keep) {
          m_IgnoreChange = true;
          PrintableComponent.m_CustomWidthText.setText("" + m_Component.getWidth());
          PrintableComponent.m_CustomHeightText.setText("" + m_Component.getHeight());
          m_IgnoreChange = false;
        }
      }
    });
    accessory.add(m_AspectRatioCheckBox);
    

    Vector writerNames = GenericObjectEditor.getClassnames(JComponentWriter.class.getName());
    Collections.sort(writerNames);
    for (int i = 0; i < writerNames.size(); i++) {
      try {
        Class cls = Class.forName(writerNames.get(i).toString());
        JComponentWriter writer = (JComponentWriter)cls.newInstance();
        Messages.getInstance();Messages.getInstance();m_FileChooserPanel.addChoosableFileFilter(new JComponentWriterFileFilter(writer.getExtension(), writer.getDescription() + Messages.getString("PrintableComponent_InitFileChooser_JComponentWriterFileFilter_Text_First") + writer.getExtension() + Messages.getString("PrintableComponent_InitFileChooser_JComponentWriterFileFilter_Text_Second"), writer));


      }
      catch (Exception e)
      {

        System.err.println(writerNames.get(i) + ": " + e);
      }
    }
    

    if (m_FileChooserPanel.getChoosableFileFilters().length > 0) {
      m_FileChooserPanel.setFileFilter(m_FileChooserPanel.getChoosableFileFilters()[0]);
    }
  }
  







  protected void updateDimensions(JTextField sender)
  {
    if ((!m_AspectRatioCheckBox.isSelected()) || (m_IgnoreChange))
      return;
    if ((!(sender instanceof JTextField)) || (sender == null))
      return;
    if (sender.getText().length() == 0)
      return;
    int baseValue;
    int newValue;
    try {
      baseValue = Integer.parseInt(sender.getText());
      newValue = 0;
      if (baseValue <= 0) {
        return;
      }
      if (Double.isNaN(m_AspectRatio)) {
        m_AspectRatio = (getComponent().getWidth() / getComponent().getHeight());
      }
      
    }
    catch (Exception e)
    {
      return;
    }
    

    m_IgnoreChange = true;
    if (sender == m_CustomWidthText) {
      newValue = (int)(baseValue * (1.0D / m_AspectRatio));
      m_CustomHeightText.setText("" + newValue);
    }
    else if (sender == m_CustomHeightText) {
      newValue = (int)(baseValue * m_AspectRatio);
      m_CustomWidthText.setText("" + newValue);
    }
    m_IgnoreChange = false;
  }
  










  public Hashtable getWriters()
  {
    Hashtable result = new Hashtable();
    
    for (int i = 0; i < m_FileChooserPanel.getChoosableFileFilters().length; i++) {
      JComponentWriter writer = ((JComponentWriterFileFilter)m_FileChooserPanel.getChoosableFileFilters()[i]).getWriter();
      result.put(writer.getDescription(), writer);
    }
    
    return result;
  }
  







  public JComponentWriter getWriter(String name)
  {
    return (JComponentWriter)getWriters().get(name);
  }
  




  public void setSaveDialogTitle(String title)
  {
    m_SaveDialogTitle = title;
  }
  




  public String getSaveDialogTitle()
  {
    return m_SaveDialogTitle;
  }
  





  public void setScale(double x, double y)
  {
    m_xScale = x;
    m_yScale = y;
  }
  






  public double getXScale()
  {
    return m_xScale;
  }
  




  public double getYScale()
  {
    return m_xScale;
  }
  














  public void saveComponent()
  {
    m_FileChooserPanel.setDialogTitle(getSaveDialogTitle());
    do {
      int result = m_FileChooserPanel.showSaveDialog(getComponent());
      if (result != 0) {
        return;
      }
    } while (m_FileChooserPanel.getSelectedFile() == null);
    
    try
    {
      JComponentWriterFileFilter filter = (JComponentWriterFileFilter)m_FileChooserPanel.getFileFilter();
      File file = m_FileChooserPanel.getSelectedFile();
      JComponentWriter writer = filter.getWriter();
      if (!file.getAbsolutePath().toLowerCase().endsWith(writer.getExtension().toLowerCase()))
        file = new File(file.getAbsolutePath() + writer.getExtension());
      writer.setComponent(getComponent());
      writer.setFile(file);
      writer.setScale(getXScale(), getYScale());
      writer.setUseCustomDimensions(m_CustomDimensionsCheckBox.isSelected());
      if (m_CustomDimensionsCheckBox.isSelected()) {
        writer.setCustomWidth(Integer.parseInt(m_CustomWidthText.getText()));
        writer.setCustomHeight(Integer.parseInt(m_CustomHeightText.getText()));
      }
      else {
        writer.setCustomWidth(-1);
        writer.setCustomHeight(-1);
      }
      writer.toOutput();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  protected class JComponentWriterFileFilter
    extends ExtensionFileFilter
  {
    private JComponentWriter m_Writer;
    




    public JComponentWriterFileFilter(String extension, String description, JComponentWriter writer)
    {
      super(description);
      m_Writer = writer;
    }
    




    public JComponentWriter getWriter()
    {
      return m_Writer;
    }
  }
  



  private class PrintMouseListener
    extends MouseAdapter
  {
    private PrintableComponent m_Component;
    



    public PrintMouseListener(PrintableComponent component)
    {
      m_Component = component;
    }
    




    public void mouseClicked(MouseEvent e)
    {
      int modifiers = e.getModifiers();
      if (((modifiers & 0x1) == 1) && ((modifiers & 0x8) == 8) && ((modifiers & 0x10) == 16))
      {

        e.consume();
        m_Component.saveComponent();
      }
    }
  }
}
