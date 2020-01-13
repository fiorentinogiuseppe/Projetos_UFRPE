package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SingleIndex;
import weka.core.Utils;






























public class ThresholdVisualizePanel
  extends VisualizePanel
{
  private static final long serialVersionUID = 3070002211779443890L;
  private String m_ROCString = "";
  



  private String m_savePanelBorderText;
  



  public ThresholdVisualizePanel()
  {
    TitledBorder tb = (TitledBorder)m_plotSurround.getBorder();
    m_savePanelBorderText = tb.getTitle();
  }
  



  public void setROCString(String str)
  {
    m_ROCString = str;
  }
  



  public String getROCString()
  {
    return m_ROCString;
  }
  





  public void setUpComboBoxes(Instances inst)
  {
    super.setUpComboBoxes(inst);
    
    m_XCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ThresholdVisualizePanel.this.setBorderText();
      }
    });
    m_YCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ThresholdVisualizePanel.this.setBorderText();
      }
      

    });
    setBorderText();
  }
  





  private void setBorderText()
  {
    String xs = m_XCombo.getSelectedItem().toString();
    String ys = m_YCombo.getSelectedItem().toString();
    
    Messages.getInstance(); if (xs.equals(Messages.getString("ThresholdVisualizePanel_SetBorderText_Text_First"))) { Messages.getInstance(); if (ys.equals(Messages.getString("ThresholdVisualizePanel_SetBorderText_Text_Second"))) {
        m_plotSurround.setBorder(BorderFactory.createTitledBorder(m_savePanelBorderText + " " + m_ROCString)); return;
      } }
    m_plotSurround.setBorder(BorderFactory.createTitledBorder(m_savePanelBorderText));
  }
  




  protected void openVisibleInstances(Instances insts)
    throws Exception
  {
    super.openVisibleInstances(insts);
    
    Messages.getInstance();Messages.getInstance();setROCString(Messages.getString("ThresholdVisualizePanel_OpenVisibleInstances_Text_First") + Utils.doubleToString(ThresholdCurve.getROCArea(insts), 4) + Messages.getString("ThresholdVisualizePanel_OpenVisibleInstances_Text_Second"));
    


    setBorderText();
  }
  





































  public static void main(String[] args)
  {
    Instances inst = null;
    Classifier classifier = null;
    int runs = 2;
    int folds = 10;
    boolean compute = true;
    Instances result = null;
    SingleIndex classIndex = null;
    SingleIndex valueIndex = null;
    int seed = 1;
    
    try
    {
      if (Utils.getFlag('h', args)) {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_First") + ThresholdVisualizePanel.class.getName() + Messages.getString("ThresholdVisualizePanel_Main_Text_Second"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Third"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Fourth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Fifth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Sixth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Seventh"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Eighth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Nineth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Tenth"));
        Messages.getInstance();System.out.println(Messages.getString("ThresholdVisualizePanel_Main_Text_Eleventh"));
        return;
      }
      

      String tmpStr = Utils.getOption('l', args);
      if (tmpStr.length() != 0) {
        result = new Instances(new BufferedReader(new FileReader(tmpStr)));
        compute = false;
      }
      
      if (compute) {
        tmpStr = Utils.getOption('r', args);
        if (tmpStr.length() != 0) {
          runs = Integer.parseInt(tmpStr);
        } else {
          runs = 1;
        }
        tmpStr = Utils.getOption('x', args);
        if (tmpStr.length() != 0) {
          folds = Integer.parseInt(tmpStr);
        } else {
          folds = 10;
        }
        tmpStr = Utils.getOption('S', args);
        if (tmpStr.length() != 0) {
          seed = Integer.parseInt(tmpStr);
        } else {
          seed = 1;
        }
        tmpStr = Utils.getOption('t', args);
        if (tmpStr.length() != 0) {
          inst = new Instances(new BufferedReader(new FileReader(tmpStr)));
          inst.setClassIndex(inst.numAttributes() - 1);
        }
        
        tmpStr = Utils.getOption('W', args);
        String[] options; String[] options; if (tmpStr.length() != 0) {
          options = Utils.partitionOptions(args);
        }
        else {
          tmpStr = Logistic.class.getName();
          options = new String[0];
        }
        classifier = Classifier.forName(tmpStr, options);
        
        tmpStr = Utils.getOption('c', args);
        if (tmpStr.length() != 0) {
          classIndex = new SingleIndex(tmpStr);
        } else {
          classIndex = new SingleIndex("last");
        }
        tmpStr = Utils.getOption('C', args);
        if (tmpStr.length() != 0) {
          valueIndex = new SingleIndex(tmpStr);
        } else {
          valueIndex = new SingleIndex("first");
        }
      }
      
      if (compute) {
        if (classIndex != null) {
          classIndex.setUpper(inst.numAttributes() - 1);
          inst.setClassIndex(classIndex.getIndex());
        }
        else {
          inst.setClassIndex(inst.numAttributes() - 1);
        }
        
        if (valueIndex != null) {
          valueIndex.setUpper(inst.classAttribute().numValues() - 1);
        }
        
        ThresholdCurve tc = new ThresholdCurve();
        EvaluationUtils eu = new EvaluationUtils();
        FastVector predictions = new FastVector();
        for (int i = 0; i < runs; i++) {
          eu.setSeed(seed + i);
          predictions.appendElements(eu.getCVPredictions(classifier, inst, folds));
        }
        
        if (valueIndex != null) {
          result = tc.getCurve(predictions, valueIndex.getIndex());
        } else {
          result = tc.getCurve(predictions);
        }
      }
      
      ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
      Messages.getInstance();Messages.getInstance();vmc.setROCString(Messages.getString("ThresholdVisualizePanel_OpenVisibleInstances_Text_Third") + Utils.doubleToString(ThresholdCurve.getROCArea(result), 4) + Messages.getString("ThresholdVisualizePanel_OpenVisibleInstances_Text_Fourth"));
      
      if (compute) {
        Messages.getInstance();Messages.getInstance();vmc.setName(result.relationName() + Messages.getString("ThresholdVisualizePanel_Main_Text_Twelveth") + inst.classAttribute().value(valueIndex.getIndex()) + Messages.getString("ThresholdVisualizePanel_Main_Text_Thirteenth"));
      }
      else
      {
        Messages.getInstance();vmc.setName(result.relationName() + Messages.getString("ThresholdVisualizePanel_Main_Text_Fourteenth"));
      }
      
      PlotData2D tempd = new PlotData2D(result);
      tempd.setPlotName(result.relationName());
      tempd.addInstanceNumberAttribute();
      vmc.addPlot(tempd);
      
      String plotName = vmc.getName();
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ThresholdVisualizePanel_Main_JFrame_Text") + plotName);
      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      
      jf.getContentPane().add(vmc, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
