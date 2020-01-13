package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventSetDescriptor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.gui.Logger;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;





























public class CostBenefitAnalysis
  extends JPanel
  implements BeanCommon, ThresholdDataListener, Visible, UserRequestAcceptor, Serializable, BeanContextChild
{
  private static final long serialVersionUID = 8647471654613320469L;
  protected BeanVisual m_visual;
  protected transient JFrame m_popupFrame;
  protected boolean m_framePoppedUp = false;
  


  private transient AnalysisPanel m_analysisPanel;
  


  protected boolean m_design;
  


  protected transient BeanContext m_beanContext = null;
  



  protected BeanContextChildSupport m_bcSupport = new BeanContextChildSupport(this);
  



  protected Object m_listenee;
  




  protected static class AnalysisPanel
    extends JPanel
  {
    private static final long serialVersionUID = 5364871945448769003L;
    



    protected VisualizePanel m_performancePanel = new VisualizePanel();
    

    protected VisualizePanel m_costBenefitPanel = new VisualizePanel();
    


    protected Attribute m_classAttribute;
    


    protected PlotData2D m_masterPlot;
    


    protected PlotData2D m_costBenefit;
    

    protected int[] m_shapeSizes;
    

    protected int m_previousShapeIndex = -1;
    

    protected JSlider m_thresholdSlider = new JSlider(0, 100, 0);
    
    protected JRadioButton m_percPop;
    
    protected JRadioButton m_percOfTarget;
    
    protected JRadioButton m_threshold;
    
    protected JLabel m_percPopLab;
    
    protected JLabel m_percOfTargetLab;
    
    protected JLabel m_thresholdLab;
    
    protected JLabel m_conf_predictedA;
    
    protected JLabel m_conf_predictedB;
    
    protected JLabel m_conf_actualA;
    
    protected JLabel m_conf_actualB;
    
    protected ConfusionCell m_conf_aa;
    
    protected ConfusionCell m_conf_ab;
    
    protected ConfusionCell m_conf_ba;
    
    protected ConfusionCell m_conf_bb;
    
    protected JLabel m_cost_predictedA;
    
    protected JLabel m_cost_predictedB;
    
    protected JLabel m_cost_actualA;
    
    protected JLabel m_cost_actualB;
    
    protected JTextField m_cost_aa;
    
    protected JTextField m_cost_ab;
    
    protected JTextField m_cost_ba;
    protected JTextField m_cost_bb;
    protected JButton m_maximizeCB;
    protected JButton m_minimizeCB;
    protected JRadioButton m_costR;
    protected JRadioButton m_benefitR;
    protected JLabel m_costBenefitL;
    protected JLabel m_costBenefitV;
    protected JLabel m_randomV;
    protected JLabel m_gainV;
    protected int m_originalPopSize;
    protected JTextField m_totalPopField;
    protected int m_totalPopPrevious;
    protected JLabel m_classificationAccV;
    protected double m_tpPrevious;
    protected double m_fpPrevious;
    protected double m_tnPrevious;
    protected double m_fnPrevious;
    
    protected static class ConfusionCell
      extends JPanel
    {
      private static final long serialVersionUID = 6148640235434494767L;
      private JLabel m_conf_cell = new JLabel("-", 4);
      JLabel m_conf_perc = new JLabel("-", 4);
      
      private JPanel m_percentageP;
      
      protected double m_percentage = 0.0D;
      
      public ConfusionCell() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        
        add(m_conf_cell, "North");
        
        m_percentageP = new JPanel() {
          public void paintComponent(Graphics gx) {
            super.paintComponent(gx);
            
            if (m_percentage > 0.0D) {
              gx.setColor(Color.BLUE);
              int height = getHeight();
              double width = getWidth();
              int barWidth = (int)(m_percentage * width);
              gx.fillRect(0, 0, barWidth, height);
            }
            
          }
        };
        Dimension d = new Dimension(30, 5);
        m_percentageP.setMinimumSize(d);
        m_percentageP.setPreferredSize(d);
        JPanel percHolder = new JPanel();
        percHolder.setLayout(new BorderLayout());
        percHolder.add(m_percentageP, "Center");
        percHolder.add(m_conf_perc, "East");
        
        add(percHolder, "South");
      }
      







      public void setCellValue(double cellValue, double max, double scaleFactor, int precision)
      {
        if (!Instance.isMissingValue(cellValue)) {
          m_percentage = (cellValue / max);
        } else {
          m_percentage = 0.0D;
        }
        
        m_conf_cell.setText(Utils.doubleToString(cellValue * scaleFactor, 0));
        m_conf_perc.setText(Utils.doubleToString(m_percentage * 100.0D, precision) + "%");
        

        m_percentageP.repaint();
      }
    }
    
    public AnalysisPanel()
    {
      Messages.getInstance();m_percPop = new JRadioButton(Messages.getString("CostBenefitAnalysis_PpercPop_JRadioButton_Text"));
      Messages.getInstance();m_percOfTarget = new JRadioButton(Messages.getString("CostBenefitAnalysis_PercOfTarget_JRadioButton_Text"));
      Messages.getInstance();m_threshold = new JRadioButton(Messages.getString("CostBenefitAnalysis_Threshold_JRadioButton_Text"));
      
      m_percPopLab = new JLabel();
      m_percOfTargetLab = new JLabel();
      m_thresholdLab = new JLabel();
      

      Messages.getInstance();m_conf_predictedA = new JLabel(Messages.getString("CostBenefitAnalysis_Conf_PredictedA_JLabel_Text"), 4);
      Messages.getInstance();m_conf_predictedB = new JLabel(Messages.getString("CostBenefitAnalysis_Conf_PredictedB_JLabel_Text"), 4);
      Messages.getInstance();m_conf_actualA = new JLabel(Messages.getString("CostBenefitAnalysis_Conf_ActualA_JLabel_Text"));
      Messages.getInstance();m_conf_actualB = new JLabel(Messages.getString("CostBenefitAnalysis_Conf_ActualB_JLabel_Text"));
      m_conf_aa = new ConfusionCell();
      m_conf_ab = new ConfusionCell();
      m_conf_ba = new ConfusionCell();
      m_conf_bb = new ConfusionCell();
      

      Messages.getInstance();m_cost_predictedA = new JLabel(Messages.getString("CostBenefitAnalysis_Cost_PredictedA_JLabel_Text"), 4);
      Messages.getInstance();m_cost_predictedB = new JLabel(Messages.getString("CostBenefitAnalysis_Cost_PredictedB_JLabel_Text"), 4);
      Messages.getInstance();m_cost_actualA = new JLabel(Messages.getString("CostBenefitAnalysis_Cost_ActualA_JLabel_Text"));
      Messages.getInstance();m_cost_actualB = new JLabel(Messages.getString("CostBenefitAnalysis_Cost_ActualB_JLabel_Text"));
      m_cost_aa = new JTextField("0.0", 5);
      m_cost_ab = new JTextField("1.0", 5);
      m_cost_ba = new JTextField("1.0", 5);
      m_cost_bb = new JTextField("0.0", 5);
      Messages.getInstance();m_maximizeCB = new JButton(Messages.getString("CostBenefitAnalysis_MaximizeCB_JButton_Text"));
      Messages.getInstance();m_minimizeCB = new JButton(Messages.getString("CostBenefitAnalysis_MinimizeCB_JButton_Text"));
      Messages.getInstance();m_costR = new JRadioButton(Messages.getString("CostBenefitAnalysis_CostR_JRadioButton_Text"));
      Messages.getInstance();m_benefitR = new JRadioButton(Messages.getString("CostBenefitAnalysis_BenefitR_JRadioButton_Text"));
      Messages.getInstance();m_costBenefitL = new JLabel(Messages.getString("CostBenefitAnalysis_CostBenefitL_JLabel_Text"), 4);
      m_costBenefitV = new JLabel("0");
      m_randomV = new JLabel("0");
      m_gainV = new JLabel("0");
      



      m_totalPopField = new JTextField(6);
      


      m_classificationAccV = new JLabel("-");
      
















































































      setLayout(new BorderLayout());
      m_performancePanel.setShowAttBars(false);
      m_performancePanel.setShowClassPanel(false);
      m_costBenefitPanel.setShowAttBars(false);
      m_costBenefitPanel.setShowClassPanel(false);
      
      Dimension size = new Dimension(500, 400);
      m_performancePanel.setPreferredSize(size);
      m_performancePanel.setMinimumSize(size);
      
      size = new Dimension(500, 400);
      m_costBenefitPanel.setMinimumSize(size);
      m_costBenefitPanel.setPreferredSize(size);
      
      m_thresholdSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          CostBenefitAnalysis.AnalysisPanel.this.updateInfoForSliderValue(m_thresholdSlider.getValue() / 100.0D);
        }
        
      });
      JPanel plotHolder = new JPanel();
      plotHolder.setLayout(new GridLayout(1, 2));
      plotHolder.add(m_performancePanel);
      plotHolder.add(m_costBenefitPanel);
      add(plotHolder, "Center");
      
      JPanel lowerPanel = new JPanel();
      lowerPanel.setLayout(new BorderLayout());
      
      ButtonGroup bGroup = new ButtonGroup();
      bGroup.add(m_percPop);
      bGroup.add(m_percOfTarget);
      bGroup.add(m_threshold);
      
      ButtonGroup bGroup2 = new ButtonGroup();
      bGroup2.add(m_costR);
      bGroup2.add(m_benefitR);
      ActionListener rl = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (m_costR.isSelected()) {
            Messages.getInstance();m_costBenefitL.setText(Messages.getString("CostBenefitAnalysis_AnalysisPanel_CostBenefitL_SetText_Text_First"));
          } else {
            Messages.getInstance();m_costBenefitL.setText(Messages.getString("CostBenefitAnalysis_AnalysisPanel_CostBenefitL_SetText_Text_Second"));
          }
          
          double gain = Double.parseDouble(m_gainV.getText());
          gain = -gain;
          m_gainV.setText(Utils.doubleToString(gain, 2));
        }
      };
      m_costR.addActionListener(rl);
      m_benefitR.addActionListener(rl);
      m_costR.setSelected(true);
      
      m_percPop.setSelected(true);
      JPanel threshPanel = new JPanel();
      threshPanel.setLayout(new BorderLayout());
      JPanel radioHolder = new JPanel();
      radioHolder.setLayout(new FlowLayout());
      radioHolder.add(m_percPop);
      radioHolder.add(m_percOfTarget);
      radioHolder.add(m_threshold);
      threshPanel.add(radioHolder, "North");
      threshPanel.add(m_thresholdSlider, "South");
      
      JPanel threshInfoPanel = new JPanel();
      threshInfoPanel.setLayout(new GridLayout(3, 2));
      Messages.getInstance();threshInfoPanel.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_JLabel_Text_First"), 4));
      threshInfoPanel.add(m_percPopLab);
      Messages.getInstance();threshInfoPanel.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_JLabel_Text_Second"), 4));
      threshInfoPanel.add(m_percOfTargetLab);
      Messages.getInstance();threshInfoPanel.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_JLabel_Text_Third"), 4));
      threshInfoPanel.add(m_thresholdLab);
      
      JPanel threshHolder = new JPanel();
      Messages.getInstance();threshHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_ThreshHolder_SetBorder_BorderFactory_CreateTitledBorder_Text")));
      threshHolder.setLayout(new BorderLayout());
      threshHolder.add(threshPanel, "Center");
      threshHolder.add(threshInfoPanel, "East");
      
      lowerPanel.add(threshHolder, "North");
      

      JPanel matrixHolder = new JPanel();
      matrixHolder.setLayout(new GridLayout(1, 2));
      

      JPanel confusionPanel = new JPanel();
      confusionPanel.setLayout(new GridLayout(3, 3));
      confusionPanel.add(m_conf_predictedA);
      confusionPanel.add(m_conf_predictedB);
      confusionPanel.add(new JLabel());
      confusionPanel.add(m_conf_aa);
      confusionPanel.add(m_conf_ab);
      confusionPanel.add(m_conf_actualA);
      confusionPanel.add(m_conf_ba);
      confusionPanel.add(m_conf_bb);
      confusionPanel.add(m_conf_actualB);
      JPanel tempHolderCA = new JPanel();
      tempHolderCA.setLayout(new BorderLayout());
      Messages.getInstance();tempHolderCA.setBorder(BorderFactory.createTitledBorder(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_TempHolderCA_SetBorder_BorderFactory_CreateTitledBorder_Text")));
      tempHolderCA.add(confusionPanel, "Center");
      
      JPanel accHolder = new JPanel();
      accHolder.setLayout(new FlowLayout(0));
      Messages.getInstance();accHolder.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_ThreshInfoPanel_AccHolder_Add_JLabel_Text")));
      accHolder.add(m_classificationAccV);
      tempHolderCA.add(accHolder, "South");
      
      matrixHolder.add(tempHolderCA);
      

      JPanel costPanel = new JPanel();
      Messages.getInstance();costPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("CostBenefitAnalysis_AnalysisPanel_CostPanel_SetBorder_BorderFactory_CcreateTitledBorder_Text")));
      costPanel.setLayout(new BorderLayout());
      
      JPanel cmHolder = new JPanel();
      cmHolder.setLayout(new GridLayout(3, 3));
      cmHolder.add(m_cost_predictedA);
      cmHolder.add(m_cost_predictedB);
      cmHolder.add(new JLabel());
      cmHolder.add(m_cost_aa);
      cmHolder.add(m_cost_ab);
      cmHolder.add(m_cost_actualA);
      cmHolder.add(m_cost_ba);
      cmHolder.add(m_cost_bb);
      cmHolder.add(m_cost_actualB);
      costPanel.add(cmHolder, "Center");
      
      FocusListener fl = new FocusListener()
      {
        public void focusGained(FocusEvent e) {}
        
        public void focusLost(FocusEvent e)
        {
          if (CostBenefitAnalysis.AnalysisPanel.this.constructCostBenefitData()) {
            try {
              m_costBenefitPanel.setMasterPlot(m_costBenefit);
              m_costBenefitPanel.validate();m_costBenefitPanel.repaint();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            CostBenefitAnalysis.AnalysisPanel.this.updateCostBenefit();
          }
          
        }
      };
      ActionListener al = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (CostBenefitAnalysis.AnalysisPanel.this.constructCostBenefitData()) {
            try {
              m_costBenefitPanel.setMasterPlot(m_costBenefit);
              m_costBenefitPanel.validate();m_costBenefitPanel.repaint();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            CostBenefitAnalysis.AnalysisPanel.this.updateCostBenefit();
          }
          
        }
      };
      m_cost_aa.addFocusListener(fl);
      m_cost_aa.addActionListener(al);
      m_cost_ab.addFocusListener(fl);
      m_cost_ab.addActionListener(al);
      m_cost_ba.addFocusListener(fl);
      m_cost_ba.addActionListener(al);
      m_cost_bb.addFocusListener(fl);
      m_cost_bb.addActionListener(al);
      
      m_totalPopField.addFocusListener(fl);
      m_totalPopField.addActionListener(al);
      
      JPanel cbHolder = new JPanel();
      cbHolder.setLayout(new BorderLayout());
      JPanel tempP = new JPanel();
      tempP.setLayout(new GridLayout(3, 2));
      tempP.add(m_costBenefitL);
      tempP.add(m_costBenefitV);
      Messages.getInstance();tempP.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_CbHolder_TempP_JPanel_Add_JLabel_Text_First"), 4));
      tempP.add(m_randomV);
      Messages.getInstance();tempP.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_CbHolder_TempP_JPanel_Add_JLabel_Text_Second"), 4));
      tempP.add(m_gainV);
      cbHolder.add(tempP, "North");
      JPanel butHolder = new JPanel();
      butHolder.setLayout(new GridLayout(2, 1));
      butHolder.add(m_maximizeCB);
      butHolder.add(m_minimizeCB);
      m_maximizeCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          CostBenefitAnalysis.AnalysisPanel.this.findMaxMinCB(true);
        }
        
      });
      m_minimizeCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          CostBenefitAnalysis.AnalysisPanel.this.findMaxMinCB(false);
        }
        
      });
      cbHolder.add(butHolder, "South");
      costPanel.add(cbHolder, "East");
      
      JPanel popCBR = new JPanel();
      popCBR.setLayout(new GridLayout(1, 2));
      JPanel popHolder = new JPanel();
      popHolder.setLayout(new FlowLayout(0));
      Messages.getInstance();popHolder.add(new JLabel(Messages.getString("CostBenefitAnalysis_AnalysisPanel_PopHolder_Add_JLabel_Text")));
      popHolder.add(m_totalPopField);
      
      JPanel radioHolder2 = new JPanel();
      radioHolder2.setLayout(new FlowLayout(2));
      radioHolder2.add(m_costR);
      radioHolder2.add(m_benefitR);
      popCBR.add(popHolder);
      popCBR.add(radioHolder2);
      
      costPanel.add(popCBR, "South");
      
      matrixHolder.add(costPanel);
      

      lowerPanel.add(matrixHolder, "South");
      











      add(lowerPanel, "South");
    }
    
    private void findMaxMinCB(boolean max)
    {
      double maxMin = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
      


      Instances cBCurve = m_costBenefit.getPlotInstances();
      int maxMinIndex = 0;
      
      for (int i = 0; i < cBCurve.numInstances(); i++) {
        Instance current = cBCurve.instance(i);
        if (max) {
          if (current.value(1) > maxMin) {
            maxMin = current.value(1);
            maxMinIndex = i;
          }
        }
        else if (current.value(1) < maxMin) {
          maxMin = current.value(1);
          maxMinIndex = i;
        }
      }
      



      int indexOfSampleSize = m_masterPlot.getPlotInstances().attribute("Sample Size").index();
      
      int indexOfPercOfTarget = m_masterPlot.getPlotInstances().attribute("Recall").index();
      
      int indexOfThreshold = m_masterPlot.getPlotInstances().attribute("Threshold").index();
      
      int indexOfMetric;
      int indexOfMetric;
      if (m_percPop.isSelected()) {
        indexOfMetric = indexOfSampleSize; } else { int indexOfMetric;
        if (m_percOfTarget.isSelected()) {
          indexOfMetric = indexOfPercOfTarget;
        } else {
          indexOfMetric = indexOfThreshold;
        }
      }
      double valueOfMetric = m_masterPlot.getPlotInstances().instance(maxMinIndex).value(indexOfMetric);
      valueOfMetric *= 100.0D;
      

      m_thresholdSlider.setValue((int)valueOfMetric);
      


      updateInfoGivenIndex(maxMinIndex);
    }
    
    private void updateCostBenefit() {
      double value = m_thresholdSlider.getValue() / 100.0D;
      Instances plotInstances = m_masterPlot.getPlotInstances();
      int indexOfSampleSize = m_masterPlot.getPlotInstances().attribute("Sample Size").index();
      
      int indexOfPercOfTarget = m_masterPlot.getPlotInstances().attribute("Recall").index();
      
      int indexOfThreshold = m_masterPlot.getPlotInstances().attribute("Threshold").index();
      
      int indexOfMetric;
      int indexOfMetric;
      if (m_percPop.isSelected()) {
        indexOfMetric = indexOfSampleSize; } else { int indexOfMetric;
        if (m_percOfTarget.isSelected()) {
          indexOfMetric = indexOfPercOfTarget;
        } else {
          indexOfMetric = indexOfThreshold;
        }
      }
      int index = findIndexForValue(value, plotInstances, indexOfMetric);
      updateCBRandomGainInfo(index);
    }
    
    private void updateCBRandomGainInfo(int index) {
      double requestedPopSize = m_originalPopSize;
      try {
        requestedPopSize = Double.parseDouble(m_totalPopField.getText());
      } catch (NumberFormatException e) {}
      double scaleFactor = requestedPopSize / m_originalPopSize;
      
      double CB = m_costBenefit.getPlotInstances().instance(index).value(1);
      
      m_costBenefitV.setText(Utils.doubleToString(CB, 2));
      
      double totalRandomCB = 0.0D;
      Instance first = m_masterPlot.getPlotInstances().instance(0);
      double totalPos = first.value(m_masterPlot.getPlotInstances().attribute("True Positives").index()) * scaleFactor;
      
      double totalNeg = first.value(m_masterPlot.getPlotInstances().attribute("False Positives")) * scaleFactor;
      

      double posInSample = totalPos * (Double.parseDouble(m_percPopLab.getText()) / 100.0D);
      double negInSample = totalNeg * (Double.parseDouble(m_percPopLab.getText()) / 100.0D);
      double posOutSample = totalPos - posInSample;
      double negOutSample = totalNeg - negInSample;
      
      double tpCost = 0.0D;
      try {
        tpCost = Double.parseDouble(m_cost_aa.getText());
      } catch (NumberFormatException n) {}
      double fpCost = 0.0D;
      try {
        fpCost = Double.parseDouble(m_cost_ba.getText());
      } catch (NumberFormatException n) {}
      double tnCost = 0.0D;
      try {
        tnCost = Double.parseDouble(m_cost_bb.getText());
      } catch (NumberFormatException n) {}
      double fnCost = 0.0D;
      try {
        fnCost = Double.parseDouble(m_cost_ab.getText());
      }
      catch (NumberFormatException n) {}
      totalRandomCB += posInSample * tpCost;
      totalRandomCB += negInSample * fpCost;
      totalRandomCB += posOutSample * fnCost;
      totalRandomCB += negOutSample * tnCost;
      
      m_randomV.setText(Utils.doubleToString(totalRandomCB, 2));
      double gain = m_costR.isSelected() ? totalRandomCB - CB : CB - totalRandomCB;
      

      m_gainV.setText(Utils.doubleToString(gain, 2));
      

      Instance currentInst = m_masterPlot.getPlotInstances().instance(index);
      double tp = currentInst.value(m_masterPlot.getPlotInstances().attribute("True Positives").index());
      
      double tn = currentInst.value(m_masterPlot.getPlotInstances().attribute("True Negatives").index());
      
      m_classificationAccV.setText(Utils.doubleToString((tp + tn) / (totalPos + totalNeg) * 100.0D, 4) + "%");
    }
    
    private void updateInfoGivenIndex(int index)
    {
      Instances plotInstances = m_masterPlot.getPlotInstances();
      int indexOfSampleSize = m_masterPlot.getPlotInstances().attribute("Sample Size").index();
      
      int indexOfPercOfTarget = m_masterPlot.getPlotInstances().attribute("Recall").index();
      
      int indexOfThreshold = m_masterPlot.getPlotInstances().attribute("Threshold").index();
      


      m_percPopLab.setText(Utils.doubleToString(100.0D * plotInstances.instance(index).value(indexOfSampleSize), 4));
      
      m_percOfTargetLab.setText(Utils.doubleToString(100.0D * plotInstances.instance(index).value(indexOfPercOfTarget), 4));
      
      m_thresholdLab.setText(Utils.doubleToString(plotInstances.instance(index).value(indexOfThreshold), 4));
      








      if (m_previousShapeIndex >= 0) {
        m_shapeSizes[m_previousShapeIndex] = 1;
      }
      
      m_shapeSizes[index] = 10;
      m_previousShapeIndex = index;
      


      int tp = plotInstances.attribute("True Positives").index();
      int fp = plotInstances.attribute("False Positives").index();
      int tn = plotInstances.attribute("True Negatives").index();
      int fn = plotInstances.attribute("False Negatives").index();
      Instance temp = plotInstances.instance(index);
      double totalInstances = temp.value(tp) + temp.value(fp) + temp.value(tn) + temp.value(fn);
      
      double requestedPopSize = totalInstances;
      try {
        requestedPopSize = Double.parseDouble(m_totalPopField.getText());
      }
      catch (NumberFormatException e) {}
      m_conf_aa.setCellValue(temp.value(tp), totalInstances, requestedPopSize / totalInstances, 2);
      
      m_conf_ab.setCellValue(temp.value(fn), totalInstances, requestedPopSize / totalInstances, 2);
      
      m_conf_ba.setCellValue(temp.value(fp), totalInstances, requestedPopSize / totalInstances, 2);
      
      m_conf_bb.setCellValue(temp.value(tn), totalInstances, requestedPopSize / totalInstances, 2);
      

      updateCBRandomGainInfo(index);
      
      repaint();
    }
    
    private void updateInfoForSliderValue(double value) {
      int indexOfSampleSize = m_masterPlot.getPlotInstances().attribute("Sample Size").index();
      
      int indexOfPercOfTarget = m_masterPlot.getPlotInstances().attribute("Recall").index();
      
      int indexOfThreshold = m_masterPlot.getPlotInstances().attribute("Threshold").index();
      
      int indexOfMetric;
      int indexOfMetric;
      if (m_percPop.isSelected()) {
        indexOfMetric = indexOfSampleSize; } else { int indexOfMetric;
        if (m_percOfTarget.isSelected()) {
          indexOfMetric = indexOfPercOfTarget;
        } else {
          indexOfMetric = indexOfThreshold;
        }
      }
      Instances plotInstances = m_masterPlot.getPlotInstances();
      int index = findIndexForValue(value, plotInstances, indexOfMetric);
      updateInfoGivenIndex(index);
    }
    


    private int findIndexForValue(double value, Instances plotInstances, int indexOfMetric)
    {
      int index = -1;
      int lower = 0;
      int upper = plotInstances.numInstances() - 1;
      int mid = (upper - lower) / 2;
      boolean done = false;
      while (!done) {
        if (upper - lower <= 1)
        {

          double comp1 = plotInstances.instance(upper).value(indexOfMetric);
          double comp2 = plotInstances.instance(lower).value(indexOfMetric);
          if (Math.abs(comp1 - value) < Math.abs(comp2 - value)) {
            index = upper; break;
          }
          index = lower;
          

          break;
        }
        double comparisonVal = plotInstances.instance(mid).value(indexOfMetric);
        if (value > comparisonVal) {
          if (m_threshold.isSelected()) {
            lower = mid;
            mid += (upper - lower) / 2;
          } else {
            upper = mid;
            mid -= (upper - lower) / 2;
          }
        } else if (value < comparisonVal) {
          if (m_threshold.isSelected()) {
            upper = mid;
            mid -= (upper - lower) / 2;
          } else {
            lower = mid;
            mid += (upper - lower) / 2;
          }
        } else {
          index = mid;
          done = true;
        }
      }
      

      if (!m_threshold.isSelected()) {
        while ((index + 1 < plotInstances.numInstances()) && 
          (plotInstances.instance(index + 1).value(indexOfMetric) == plotInstances.instance(index).value(indexOfMetric)))
        {
          index++;
        }
      }
      


      while ((index - 1 >= 0) && 
        (plotInstances.instance(index - 1).value(indexOfMetric) == plotInstances.instance(index).value(indexOfMetric)))
      {
        index--;
      }
      



      return index;
    }
    







    public synchronized void setDataSet(PlotData2D data, Attribute classAtt)
      throws Exception
    {
      m_masterPlot = new PlotData2D(data.getPlotInstances());
      boolean[] connectPoints = new boolean[m_masterPlot.getPlotInstances().numInstances()];
      for (int i = 1; i < connectPoints.length; i++) {
        connectPoints[i] = true;
      }
      m_masterPlot.setConnectPoints(connectPoints);
      
      m_masterPlot.m_alwaysDisplayPointsOfThisSize = 10;
      setClassForConfusionMatrix(classAtt);
      m_performancePanel.setMasterPlot(m_masterPlot);
      m_performancePanel.validate();m_performancePanel.repaint();
      
      m_shapeSizes = new int[m_masterPlot.getPlotInstances().numInstances()];
      for (int i = 0; i < m_shapeSizes.length; i++) {
        m_shapeSizes[i] = 1;
      }
      m_masterPlot.setShapeSize(m_shapeSizes);
      constructCostBenefitData();
      m_costBenefitPanel.setMasterPlot(m_costBenefit);
      m_costBenefitPanel.validate();m_costBenefitPanel.repaint();
      
      m_totalPopPrevious = 0;
      m_fpPrevious = 0.0D;
      m_tpPrevious = 0.0D;
      m_tnPrevious = 0.0D;
      m_fnPrevious = 0.0D;
      m_previousShapeIndex = -1;
      

      Instance first = m_masterPlot.getPlotInstances().instance(0);
      double totalPos = first.value(m_masterPlot.getPlotInstances().attribute("True Positives").index());
      
      double totalNeg = first.value(m_masterPlot.getPlotInstances().attribute("False Positives"));
      
      m_originalPopSize = ((int)(totalPos + totalNeg));
      m_totalPopField.setText("" + m_originalPopSize);
      
      m_performancePanel.setYIndex(5);
      m_performancePanel.setXIndex(10);
      m_costBenefitPanel.setXIndex(0);
      m_costBenefitPanel.setYIndex(1);
      
      updateInfoForSliderValue(m_thresholdSlider.getValue() / 100.0D);
    }
    
    private void setClassForConfusionMatrix(Attribute classAtt) {
      m_classAttribute = classAtt;
      Messages.getInstance();m_conf_actualA.setText(Messages.getString("CostBenefitAnalysis_AnalysisPanel_SetClassForConfusionMatrix_Conf_ActualA_SetText_Text") + classAtt.value(0));
      m_conf_actualA.setToolTipText(classAtt.value(0));
      String negClasses = "";
      for (int i = 1; i < classAtt.numValues(); i++) {
        negClasses = negClasses + classAtt.value(i);
        if (i < classAtt.numValues() - 1) {
          negClasses = negClasses + ",";
        }
      }
      Messages.getInstance();m_conf_actualB.setText(Messages.getString("CostBenefitAnalysis_AnalysisPanel_SetClassForConfusionMatrix_Conf_ActualB_SetText_Text") + negClasses);
      m_conf_actualB.setToolTipText(negClasses);
    }
    
    private boolean constructCostBenefitData() {
      double tpCost = 0.0D;
      try {
        tpCost = Double.parseDouble(m_cost_aa.getText());
      } catch (NumberFormatException n) {}
      double fpCost = 0.0D;
      try {
        fpCost = Double.parseDouble(m_cost_ba.getText());
      } catch (NumberFormatException n) {}
      double tnCost = 0.0D;
      try {
        tnCost = Double.parseDouble(m_cost_bb.getText());
      } catch (NumberFormatException n) {}
      double fnCost = 0.0D;
      try {
        fnCost = Double.parseDouble(m_cost_ab.getText());
      }
      catch (NumberFormatException n) {}
      double requestedPopSize = m_originalPopSize;
      try {
        requestedPopSize = Double.parseDouble(m_totalPopField.getText());
      }
      catch (NumberFormatException e) {}
      double scaleFactor = 1.0D;
      if (m_originalPopSize != 0) {
        scaleFactor = requestedPopSize / m_originalPopSize;
      }
      
      if ((tpCost == m_tpPrevious) && (fpCost == m_fpPrevious) && (tnCost == m_tnPrevious) && (fnCost == m_fnPrevious) && (requestedPopSize == m_totalPopPrevious))
      {

        return false;
      }
      

      FastVector fv = new FastVector();
      fv.addElement(new Attribute("Sample Size"));
      fv.addElement(new Attribute("Cost/Benefit"));
      fv.addElement(new Attribute("Threshold"));
      Instances costBenefitI = new Instances("Cost/Benefit Curve", fv, 100);
      

      Instances performanceI = m_masterPlot.getPlotInstances();
      
      for (int i = 0; i < performanceI.numInstances(); i++) {
        Instance current = performanceI.instance(i);
        
        double[] vals = new double[3];
        vals[0] = current.value(10);
        vals[1] = ((current.value(0) * tpCost + current.value(1) * fnCost + current.value(2) * fpCost + current.value(3) * tnCost) * scaleFactor);
        


        vals[2] = current.value(current.numAttributes() - 1);
        Instance newInst = new Instance(1.0D, vals);
        costBenefitI.add(newInst);
      }
      
      costBenefitI.compactify();
      

      m_costBenefit = new PlotData2D(costBenefitI);
      m_costBenefit.m_alwaysDisplayPointsOfThisSize = 10;
      m_costBenefit.setPlotName("Cost/benefit curve");
      boolean[] connectPoints = new boolean[costBenefitI.numInstances()];
      
      for (int i = 0; i < connectPoints.length; i++) {
        connectPoints[i] = true;
      }
      try {
        m_costBenefit.setConnectPoints(connectPoints);
        m_costBenefit.setShapeSize(m_shapeSizes);
      }
      catch (Exception ex) {}
      

      m_tpPrevious = tpCost;
      m_fpPrevious = fpCost;
      m_tnPrevious = tnCost;
      m_fnPrevious = fnCost;
      
      return true;
    }
  }
  


  public CostBenefitAnalysis()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    
    if (!GraphicsEnvironment.isHeadless()) {
      appearanceFinal();
    }
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("CostBenefitAnalysis_GlobalInfo_Text");
  }
  



  public void acceptDataSet(ThresholdDataEvent e)
  {
    if (!GraphicsEnvironment.isHeadless()) {
      try {
        setCurveData(e.getDataSet(), e.getClassAttribute());
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("CostBenefitAnalysis_AcceptDataSet_Error_Text"));
        ex.printStackTrace();
      }
    }
  }
  







  public void setCurveData(PlotData2D curveData, Attribute origClassAtt)
    throws Exception
  {
    if (m_analysisPanel == null) {
      m_analysisPanel = new AnalysisPanel();
    }
    m_analysisPanel.setDataSet(curveData, origClassAtt);
  }
  
  public BeanVisual getVisual() {
    return m_visual;
  }
  
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }
  
  public void useDefaultVisual() {
    m_visual.loadIcons("weka/gui/beans/icons/DefaultDataVisualizer.gif", "weka/gui/beans/icons/DefaultDataVisualizer_animated.gif");
  }
  
  public Enumeration enumerateRequests()
  {
    Vector newVector = new Vector(0);
    if ((m_analysisPanel != null) && 
      (m_analysisPanel.m_masterPlot != null)) {
      newVector.addElement("Show analysis");
    }
    
    return newVector.elements();
  }
  
  public void performRequest(String request) {
    if (request.compareTo("Show analysis") == 0)
    {
      try {
        if (!m_framePoppedUp) {
          m_framePoppedUp = true;
          
          Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("CostBenefitAnalysis_PerformRequest_Jf_JFrame_Text"));
          
          jf.setSize(1000, 600);
          jf.getContentPane().setLayout(new BorderLayout());
          jf.getContentPane().add(m_analysisPanel, "Center");
          jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              jf.dispose();
              m_framePoppedUp = false;
            }
          });
          jf.setVisible(true);
          m_popupFrame = jf;
        } else {
          m_popupFrame.toFront();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        m_framePoppedUp = false;
      }
    } else {
      Messages.getInstance();throw new IllegalArgumentException(request + Messages.getString("CostBenefitAnalysis_PerformRequest_IllegalArgumentException_Text"));
    }
  }
  
  public void addVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  
  public BeanContext getBeanContext() {
    return m_beanContext;
  }
  
  public void removeVetoableChangeListener(String name, VetoableChangeListener vcl)
  {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }
  
  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }
  
  protected void setUpFinal() {
    if (m_analysisPanel == null) {
      m_analysisPanel = new AnalysisPanel();
    }
    add(m_analysisPanel, "Center");
  }
  
  protected void appearanceDesign() {
    removeAll();
    m_visual = new BeanVisual("CostBenefitAnalysis", "weka/gui/beans/icons/ModelPerformanceChart.gif", "weka/gui/beans/icons/ModelPerformanceChart_animated.gif");
    


    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  
  public void setBeanContext(BeanContext bc) throws PropertyVetoException {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } else {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      
      if (!GraphicsEnvironment.isHeadless()) {
        appearanceFinal();
      }
    }
  }
  






  public boolean connectionAllowed(String eventName)
  {
    return m_listenee == null;
  }
  








  public void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public void disconnectionNotification(String eventName, Object source)
  {
    if (m_listenee == source) {
      m_listenee = null;
    }
  }
  





  public String getCustomName()
  {
    return m_visual.getText();
  }
  





  public boolean isBusy()
  {
    return false;
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public void setLog(Logger logger) {}
  



  public void stop() {}
  



  public static void main(String[] args)
  {
    try
    {
      Instances train = new Instances(new BufferedReader(new FileReader(args[0])));
      train.setClassIndex(train.numAttributes() - 1);
      ThresholdCurve tc = new ThresholdCurve();
      
      EvaluationUtils eu = new EvaluationUtils();
      

      Classifier classifier = new NaiveBayes();
      FastVector predictions = new FastVector();
      eu.setSeed(1);
      predictions.appendElements(eu.getCVPredictions(classifier, train, 10));
      Instances result = tc.getCurve(predictions, 0);
      PlotData2D pd = new PlotData2D(result);
      m_alwaysDisplayPointsOfThisSize = 10;
      
      boolean[] connectPoints = new boolean[result.numInstances()];
      for (int i = 1; i < connectPoints.length; i++) {
        connectPoints[i] = true;
      }
      pd.setConnectPoints(connectPoints);
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("CostBenefitAnalysis_PerformRequest_Main_JF_JFrame_Text"));
      
      jf.setSize(1000, 600);
      
      jf.getContentPane().setLayout(new BorderLayout());
      AnalysisPanel analysisPanel = new AnalysisPanel();
      

      jf.getContentPane().add(analysisPanel, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      jf.setVisible(true);
      
      analysisPanel.setDataSet(pd, train.classAttribute());
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
