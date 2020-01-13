package weka.gui.explorer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.datagenerators.DataGenerator;
import weka.datagenerators.classifiers.classification.RDG1;
import weka.gui.GenericObjectEditor;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.SysErrLog;

































public class DataGeneratorPanel
  extends JPanel
{
  private static final long serialVersionUID = -2520408165350629380L;
  protected GenericObjectEditor m_GeneratorEditor = new GenericObjectEditor();
  

  protected Instances m_Instances = null;
  

  protected StringWriter m_Output = new StringWriter();
  

  protected Logger m_Log = new SysErrLog();
  







  public DataGeneratorPanel()
  {
    setLayout(new BorderLayout());
    
    add(new PropertyPanel(m_GeneratorEditor), "Center");
    

    m_GeneratorEditor.setClassType(DataGenerator.class);
    m_GeneratorEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        repaint();
      }
      

    });
    setGenerator(null);
  }
  




  public void setLog(Logger value)
  {
    m_Log = value;
  }
  




  public Instances getInstances()
  {
    return m_Instances;
  }
  




  public String getOutput()
  {
    return m_Output.toString();
  }
  




  public void setGenerator(DataGenerator value)
  {
    if (value != null) {
      m_GeneratorEditor.setValue(value);
    } else {
      m_GeneratorEditor.setValue(new RDG1());
    }
  }
  




  public DataGenerator getGenerator()
  {
    return (DataGenerator)m_GeneratorEditor.getValue();
  }
  











  public boolean execute()
  {
    boolean result = true;
    DataGenerator generator = (DataGenerator)m_GeneratorEditor.getValue();
    String relName = generator.getRelationName();
    
    String cname = generator.getClass().getName().replaceAll(".*\\.", "");
    String cmd = generator.getClass().getName();
    if ((generator instanceof OptionHandler)) {
      cmd = cmd + " " + Utils.joinOptions(generator.getOptions());
    }
    try {
      Messages.getInstance();m_Log.logMessage(Messages.getString("DataGeneratorPanel_Execute_Log_LogMessage_Text_First") + cname);
      Messages.getInstance();m_Log.logMessage(Messages.getString("DataGeneratorPanel_Execute_Log_LogMessage_Text_Second") + cmd);
      m_Output = new StringWriter();
      generator.setOutput(new PrintWriter(m_Output));
      DataGenerator.makeData(generator, generator.getOptions());
      m_Instances = new Instances(new StringReader(getOutput()));
      Messages.getInstance();m_Log.logMessage(Messages.getString("DataGeneratorPanel_Execute_Log_LogMessage_Text_Third") + cname);
    }
    catch (Exception e) {
      e.printStackTrace();
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("DataGeneratorPanel_Execute_JOptionPaneShowMessageDialog_Text_First") + e.getMessage(), Messages.getString("DataGeneratorPanel_Execute_JOptionPaneShowMessageDialog_Text_Second"), 0);
      

      m_Instances = null;
      m_Output = new StringWriter();
      result = false;
    }
    
    generator.setRelationName(relName);
    
    return result;
  }
  
  static {}
}
