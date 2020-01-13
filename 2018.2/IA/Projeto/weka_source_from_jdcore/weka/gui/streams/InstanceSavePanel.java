package weka.gui.streams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import weka.core.Instance;
import weka.core.Instances;






























public class InstanceSavePanel
  extends Panel
  implements InstanceListener
{
  private static final long serialVersionUID = -6061005366989295026L;
  private Label count_Lab;
  private int m_Count;
  private TextField arffFile_Tex;
  private boolean b_Debug;
  private PrintWriter outputWriter;
  
  public void input(Instance instance)
    throws Exception
  {
    if (b_Debug) {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_Input_Error_Text_First") + instance + Messages.getString("InstanceSavePanel_Input_Error_Text_Second")); }
    m_Count += 1;
    Messages.getInstance();count_Lab.setText("" + m_Count + Messages.getString("InstanceSavePanel_Input_Count_Lab_SetText_Second"));
    if (outputWriter != null) {
      outputWriter.println(instance.toString());
    }
  }
  
  public void inputFormat(Instances instanceInfo) {
    if (b_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel::inputFormat()\n") + instanceInfo.toString()); }
    m_Count = 0;
    Messages.getInstance();count_Lab.setText("" + m_Count + Messages.getString("InstanceSavePanel_InputFormat_Count_Lab_SetText_Second"));
    try {
      outputWriter = new PrintWriter(new FileOutputStream(arffFile_Tex.getText()));
      outputWriter.println(instanceInfo.toString());
      if (b_Debug) {
        Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_InputFormat_Error_Text_Second"));
      }
    } catch (Exception ex) { outputWriter = null;
      Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_InputFormat_Error_Text_Third") + ex.getMessage());
    }
  }
  
  public void batchFinished()
  {
    if (b_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_BatchFinished_Error_Text_First")); }
    if (outputWriter != null) {
      outputWriter.close();
    }
  }
  
  public InstanceSavePanel() {
    setLayout(new BorderLayout());
    arffFile_Tex = new TextField("arffoutput.arff");
    add("Center", arffFile_Tex);
    Messages.getInstance();count_Lab = new Label(Messages.getString("InstanceSavePanel_Count_Lab_Label_Text"));
    Messages.getInstance();add(Messages.getString("InstanceSavePanel_Count_Lab_Label_Add_Text"), count_Lab);
    
    setBackground(Color.lightGray);
  }
  
  public void setDebug(boolean debug) {
    b_Debug = debug;
  }
  
  public boolean getDebug() {
    return b_Debug;
  }
  
  public void setArffFile(String newArffFile) {
    arffFile_Tex.setText(newArffFile);
  }
  
  public String getArffFile() {
    return arffFile_Tex.getText();
  }
  
  public void instanceProduced(InstanceEvent e)
  {
    Object source = e.getSource();
    if ((source instanceof InstanceProducer)) {
      try {
        InstanceProducer a = (InstanceProducer)source;
        switch (e.getID()) {
        case 1: 
          inputFormat(a.outputFormat());
          break;
        case 2: 
          input(a.outputPeek());
          break;
        case 3: 
          batchFinished();
          break;
        default: 
          Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_InstanceProduced_InstanceProducerDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex) {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(Messages.getString("InstanceSavePanel_InstanceProduced_Error_Text"));
    }
  }
}
