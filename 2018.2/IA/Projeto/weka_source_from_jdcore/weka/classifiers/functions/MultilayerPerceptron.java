package weka.classifiers.functions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import weka.classifiers.Classifier;
import weka.classifiers.functions.neural.LinearUnit;
import weka.classifiers.functions.neural.NeuralConnection;
import weka.classifiers.functions.neural.NeuralNode;
import weka.classifiers.functions.neural.SigmoidUnit;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;




































































































public class MultilayerPerceptron
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, Randomizable
{
  private static final long serialVersionUID = -5990607817048210779L;
  private Classifier m_ZeroR;
  
  public static void main(String[] argv)
  {
    runClassifier(new MultilayerPerceptron(), argv);
  }
  





  protected class NeuralEnd
    extends NeuralConnection
  {
    static final long serialVersionUID = 7305185603191183338L;
    




    private int m_link;
    



    private boolean m_input;
    




    public NeuralEnd(String id)
    {
      super();
      
      m_link = 0;
      m_input = true;
    }
    










    public boolean onUnit(Graphics g, int x, int y, int w, int h)
    {
      FontMetrics fm = g.getFontMetrics();
      int l = (int)(m_x * w) - fm.stringWidth(m_id) / 2;
      int t = (int)(m_y * h) - fm.getHeight() / 2;
      if ((x < l) || (x > l + fm.stringWidth(m_id) + 4) || (y < t) || (y > t + fm.getHeight() + fm.getDescent() + 4))
      {
        return false;
      }
      return true;
    }
    








    public void drawNode(Graphics g, int w, int h)
    {
      if ((m_type & 0x1) == 1) {
        g.setColor(Color.green);
      }
      else {
        g.setColor(Color.orange);
      }
      
      FontMetrics fm = g.getFontMetrics();
      int l = (int)(m_x * w) - fm.stringWidth(m_id) / 2;
      int t = (int)(m_y * h) - fm.getHeight() / 2;
      g.fill3DRect(l, t, fm.stringWidth(m_id) + 4, fm.getHeight() + fm.getDescent() + 4, true);
      

      g.setColor(Color.black);
      
      g.drawString(m_id, l + 2, t + fm.getHeight() + 2);
    }
    








    public void drawHighlight(Graphics g, int w, int h)
    {
      g.setColor(Color.black);
      FontMetrics fm = g.getFontMetrics();
      int l = (int)(m_x * w) - fm.stringWidth(m_id) / 2;
      int t = (int)(m_y * h) - fm.getHeight() / 2;
      g.fillRect(l - 2, t - 2, fm.stringWidth(m_id) + 8, fm.getHeight() + fm.getDescent() + 8);
      
      drawNode(g, w, h);
    }
    






    public double outputValue(boolean calculate)
    {
      if ((Double.isNaN(m_unitValue)) && (calculate)) {
        if (m_input) {
          if (m_currentInstance.isMissing(m_link)) {
            m_unitValue = 0.0D;
          }
          else
          {
            m_unitValue = m_currentInstance.value(m_link);
          }
        }
        else
        {
          m_unitValue = 0.0D;
          for (int noa = 0; noa < m_numInputs; noa++) {
            m_unitValue += m_inputList[noa].outputValue(true);
          }
          
          if ((m_numeric) && (m_normalizeClass))
          {

            m_unitValue = (m_unitValue * m_attributeRanges[m_instances.classIndex()] + m_attributeBases[m_instances.classIndex()]);
          }
        }
      }
      

      return m_unitValue;
    }
    









    public double errorValue(boolean calculate)
    {
      if ((!Double.isNaN(m_unitValue)) && (Double.isNaN(m_unitError)) && (calculate))
      {

        if (m_input) {
          m_unitError = 0.0D;
          for (int noa = 0; noa < m_numOutputs; noa++) {
            m_unitError += m_outputList[noa].errorValue(true);
          }
          
        }
        else if (m_currentInstance.classIsMissing()) {
          m_unitError = 0.1D;
        }
        else if (m_instances.classAttribute().isNominal()) {
          if (m_currentInstance.classValue() == m_link) {
            m_unitError = (1.0D - m_unitValue);
          }
          else {
            m_unitError = (0.0D - m_unitValue);
          }
        }
        else if (m_numeric)
        {
          if (m_normalizeClass) {
            if (m_attributeRanges[m_instances.classIndex()] == 0.0D) {
              m_unitError = 0.0D;
            }
            else {
              m_unitError = ((m_currentInstance.classValue() - m_unitValue) / m_attributeRanges[m_instances.classIndex()]);
            }
            

          }
          else
          {
            m_unitError = (m_currentInstance.classValue() - m_unitValue);
          }
        }
      }
      
      return m_unitError;
    }
    








    public void reset()
    {
      if ((!Double.isNaN(m_unitValue)) || (!Double.isNaN(m_unitError))) {
        m_unitValue = NaN.0D;
        m_unitError = NaN.0D;
        m_weightsUpdated = false;
        for (int noa = 0; noa < m_numInputs; noa++) {
          m_inputList[noa].reset();
        }
      }
    }
    



    public void saveWeights()
    {
      for (int i = 0; i < m_numInputs; i++) {
        m_inputList[i].saveWeights();
      }
    }
    



    public void restoreWeights()
    {
      for (int i = 0; i < m_numInputs; i++) {
        m_inputList[i].restoreWeights();
      }
    }
    






    public void setLink(boolean input, int val)
      throws Exception
    {
      m_input = input;
      
      if (input) {
        m_type = 1;
      }
      else {
        m_type = 2;
      }
      if ((val < 0) || ((input) && (val > m_instances.numAttributes())) || ((!input) && (m_instances.classAttribute().isNominal()) && (val > m_instances.classAttribute().numValues())))
      {

        m_link = 0;
      }
      else {
        m_link = val;
      }
    }
    


    public int getLink()
    {
      return m_link;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 10073 $");
    }
  }
  





  private class NodePanel
    extends JPanel
    implements RevisionHandler
  {
    static final long serialVersionUID = -3067621833388149984L;
    




    public NodePanel()
    {
      addMouseListener(new MouseAdapter()
      {
        public void mousePressed(MouseEvent e)
        {
          if (!m_stopped) {
            return;
          }
          if (((e.getModifiers() & 0x10) == 16) && (!e.isAltDown()))
          {
            Graphics g = getGraphics();
            int x = e.getX();
            int y = e.getY();
            int w = getWidth();
            int h = getHeight();
            FastVector tmp = new FastVector(4);
            for (int noa = 0; noa < m_numAttributes; noa++) {
              if (m_inputs[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_inputs[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, true);
                

                return;
              }
            }
            for (int noa = 0; noa < m_numClasses; noa++) {
              if (m_outputs[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_outputs[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, true);
                

                return;
              }
            }
            for (int noa = 0; noa < m_neuralNodes.length; noa++) {
              if (m_neuralNodes[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_neuralNodes[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, true);
                

                return;
              }
            }
            
            NeuralNode temp = new NeuralNode(String.valueOf(m_nextId), m_random, m_sigmoidUnit);
            
            MultilayerPerceptron.access$1308(MultilayerPerceptron.this);
            temp.setX(e.getX() / w);
            temp.setY(e.getY() / h);
            tmp.addElement(temp);
            MultilayerPerceptron.this.addNode(temp);
            MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, true);

          }
          else
          {
            Graphics g = getGraphics();
            int x = e.getX();
            int y = e.getY();
            int w = getWidth();
            int h = getHeight();
            FastVector tmp = new FastVector(4);
            for (int noa = 0; noa < m_numAttributes; noa++) {
              if (m_inputs[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_inputs[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, false);
                

                return;
              }
            }
            

            for (int noa = 0; noa < m_numClasses; noa++) {
              if (m_outputs[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_outputs[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, false);
                

                return;
              }
            }
            for (int noa = 0; noa < m_neuralNodes.length; noa++) {
              if (m_neuralNodes[noa].onUnit(g, x, y, w, h)) {
                tmp.addElement(m_neuralNodes[noa]);
                MultilayerPerceptron.NodePanel.this.selection(tmp, (e.getModifiers() & 0x2) == 2, false);
                

                return;
              }
            }
            MultilayerPerceptron.NodePanel.this.selection(null, (e.getModifiers() & 0x2) == 2, false);
          }
        }
      });
    }
    












    private void selection(FastVector v, boolean ctrl, boolean left)
    {
      if (v == null)
      {
        m_selected.removeAllElements();
        repaint();
        return;
      }
      


      if (((ctrl) || (m_selected.size() == 0)) && (left)) {
        boolean removed = false;
        for (int noa = 0; noa < v.size(); noa++) {
          removed = false;
          for (int nob = 0; nob < m_selected.size(); nob++) {
            if (v.elementAt(noa) == m_selected.elementAt(nob))
            {
              m_selected.removeElementAt(nob);
              removed = true;
              break;
            }
          }
          if (!removed) {
            m_selected.addElement(v.elementAt(noa));
          }
        }
        repaint();
        return;
      }
      

      if (left)
      {
        for (int noa = 0; noa < m_selected.size(); noa++) {
          for (int nob = 0; nob < v.size(); nob++) {
            NeuralConnection.connect((NeuralConnection)m_selected.elementAt(noa), (NeuralConnection)v.elementAt(nob));
          }
          
        }
        
      }
      else if (m_selected.size() > 0)
      {

        for (int noa = 0; noa < m_selected.size(); noa++) {
          for (int nob = 0; nob < v.size(); nob++) {
            NeuralConnection.disconnect((NeuralConnection)m_selected.elementAt(noa), (NeuralConnection)v.elementAt(nob));
            


            NeuralConnection.disconnect((NeuralConnection)v.elementAt(nob), (NeuralConnection)m_selected.elementAt(noa));

          }
          

        }
        
      }
      else
      {
        for (int noa = 0; noa < v.size(); noa++) {
          ((NeuralConnection)v.elementAt(noa)).removeAllInputs();
          ((NeuralConnection)v.elementAt(noa)).removeAllOutputs();
          MultilayerPerceptron.this.removeNode((NeuralConnection)v.elementAt(noa));
        }
      }
      repaint();
    }
    




    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      int x = getWidth();
      int y = getHeight();
      if ((25 * m_numAttributes > 25 * m_numClasses) && (25 * m_numAttributes > y))
      {
        setSize(x, 25 * m_numAttributes);
      }
      else if (25 * m_numClasses > y) {
        setSize(x, 25 * m_numClasses);
      }
      else {
        setSize(x, y);
      }
      
      y = getHeight();
      for (int noa = 0; noa < m_numAttributes; noa++) {
        m_inputs[noa].drawInputLines(g, x, y);
      }
      for (int noa = 0; noa < m_numClasses; noa++) {
        m_outputs[noa].drawInputLines(g, x, y);
        m_outputs[noa].drawOutputLines(g, x, y);
      }
      for (int noa = 0; noa < m_neuralNodes.length; noa++) {
        m_neuralNodes[noa].drawInputLines(g, x, y);
      }
      for (int noa = 0; noa < m_numAttributes; noa++) {
        m_inputs[noa].drawNode(g, x, y);
      }
      for (int noa = 0; noa < m_numClasses; noa++) {
        m_outputs[noa].drawNode(g, x, y);
      }
      for (int noa = 0; noa < m_neuralNodes.length; noa++) {
        m_neuralNodes[noa].drawNode(g, x, y);
      }
      
      for (int noa = 0; noa < m_selected.size(); noa++) {
        ((NeuralConnection)m_selected.elementAt(noa)).drawHighlight(g, x, y);
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 10073 $");
    }
  }
  



  class ControlPanel
    extends JPanel
    implements RevisionHandler
  {
    static final long serialVersionUID = 7393543302294142271L;
    


    public JButton m_startStop;
    


    public JButton m_acceptButton;
    


    public JPanel m_epochsLabel;
    


    public JLabel m_totalEpochsLabel;
    

    public JTextField m_changeEpochs;
    

    public JLabel m_learningLabel;
    

    public JLabel m_momentumLabel;
    

    public JTextField m_changeLearning;
    

    public JTextField m_changeMomentum;
    

    public JPanel m_errorLabel;
    


    public ControlPanel()
    {
      setBorder(BorderFactory.createTitledBorder("Controls"));
      
      m_totalEpochsLabel = new JLabel("Num Of Epochs  ");
      m_epochsLabel = new JPanel()
      {
        private static final long serialVersionUID = 2562773937093221399L;
        
        public void paintComponent(Graphics g) {
          super.paintComponent(g);
          g.setColor(m_controlPanel.m_totalEpochsLabel.getForeground());
          g.drawString("Epoch  " + m_epoch, 0, 10);
        }
      };
      m_epochsLabel.setFont(m_totalEpochsLabel.getFont());
      
      m_changeEpochs = new JTextField();
      m_changeEpochs.setText("" + m_numEpochs);
      m_errorLabel = new JPanel()
      {
        private static final long serialVersionUID = 4390239056336679189L;
        
        public void paintComponent(Graphics g) {
          super.paintComponent(g);
          g.setColor(m_controlPanel.m_totalEpochsLabel.getForeground());
          if (m_valSize == 0) {
            g.drawString("Error per Epoch = " + Utils.doubleToString(m_error, 7), 0, 10);
          }
          else
          {
            g.drawString("Validation Error per Epoch = " + Utils.doubleToString(m_error, 7), 0, 10);
          }
          
        }
      };
      m_errorLabel.setFont(m_epochsLabel.getFont());
      
      m_learningLabel = new JLabel("Learning Rate = ");
      m_momentumLabel = new JLabel("Momentum = ");
      m_changeLearning = new JTextField();
      m_changeMomentum = new JTextField();
      m_changeLearning.setText("" + m_learningRate);
      m_changeMomentum.setText("" + m_momentum);
      setLayout(new BorderLayout(15, 10));
      
      m_stopIt = true;
      m_accepted = false;
      m_startStop = new JButton("Start");
      m_startStop.setActionCommand("Start");
      
      m_acceptButton = new JButton("Accept");
      m_acceptButton.setActionCommand("Accept");
      
      JPanel buttons = new JPanel();
      buttons.setLayout(new BoxLayout(buttons, 1));
      buttons.add(m_startStop);
      buttons.add(m_acceptButton);
      add(buttons, "West");
      JPanel data = new JPanel();
      data.setLayout(new BoxLayout(data, 1));
      
      Box ab = new Box(0);
      ab.add(m_epochsLabel);
      data.add(ab);
      
      ab = new Box(0);
      Component b = Box.createGlue();
      ab.add(m_totalEpochsLabel);
      ab.add(m_changeEpochs);
      m_changeEpochs.setMaximumSize(new Dimension(200, 20));
      ab.add(b);
      data.add(ab);
      
      ab = new Box(0);
      ab.add(m_errorLabel);
      data.add(ab);
      
      add(data, "Center");
      
      data = new JPanel();
      data.setLayout(new BoxLayout(data, 1));
      ab = new Box(0);
      b = Box.createGlue();
      ab.add(m_learningLabel);
      ab.add(m_changeLearning);
      m_changeLearning.setMaximumSize(new Dimension(200, 20));
      ab.add(b);
      data.add(ab);
      
      ab = new Box(0);
      b = Box.createGlue();
      ab.add(m_momentumLabel);
      ab.add(m_changeMomentum);
      m_changeMomentum.setMaximumSize(new Dimension(200, 20));
      ab.add(b);
      data.add(ab);
      
      add(data, "East");
      
      m_startStop.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("Start")) {
            m_stopIt = false;
            m_startStop.setText("Stop");
            m_startStop.setActionCommand("Stop");
            int n = Integer.valueOf(m_changeEpochs.getText()).intValue();
            
            m_numEpochs = n;
            m_changeEpochs.setText("" + m_numEpochs);
            
            double m = Double.valueOf(m_changeLearning.getText()).doubleValue();
            
            setLearningRate(m);
            m_changeLearning.setText("" + m_learningRate);
            
            m = Double.valueOf(m_changeMomentum.getText()).doubleValue();
            setMomentum(m);
            m_changeMomentum.setText("" + m_momentum);
            
            blocker(false);
          }
          else if (e.getActionCommand().equals("Stop")) {
            m_stopIt = true;
            m_startStop.setText("Start");
            m_startStop.setActionCommand("Start");
          }
          
        }
      });
      m_acceptButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_accepted = true;
          blocker(false);
        }
        
      });
      m_changeEpochs.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int n = Integer.valueOf(m_changeEpochs.getText()).intValue();
          if (n > 0) {
            m_numEpochs = n;
            blocker(false);
          }
        }
      });
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 10073 $");
    }
  }
  





  private boolean m_useDefaultModel = false;
  

  private Instances m_instances;
  

  private Instance m_currentInstance;
  

  private boolean m_numeric;
  

  private double[] m_attributeRanges;
  

  private double[] m_attributeBases;
  

  private NeuralEnd[] m_outputs;
  

  private NeuralEnd[] m_inputs;
  

  private NeuralConnection[] m_neuralNodes;
  

  private int m_numClasses = 0;
  

  private int m_numAttributes = 0;
  


  private NodePanel m_nodePanel;
  


  private ControlPanel m_controlPanel;
  


  private int m_nextId;
  


  private FastVector m_selected;
  


  private FastVector m_graphers;
  


  private int m_numEpochs;
  


  private boolean m_stopIt;
  


  private boolean m_stopped;
  


  private boolean m_accepted;
  


  private JFrame m_win;
  


  private boolean m_autoBuild;
  


  private boolean m_gui;
  


  private int m_valSize;
  


  private int m_driftThreshold;
  

  private int m_randomSeed;
  

  private Random m_random;
  

  private boolean m_useNomToBin;
  

  private NominalToBinary m_nominalToBinaryFilter;
  

  private String m_hiddenLayers;
  

  private boolean m_normalizeAttributes;
  

  private boolean m_decay;
  

  private double m_learningRate;
  

  private double m_momentum;
  

  private int m_epoch;
  

  private double m_error;
  

  private boolean m_reset;
  

  private boolean m_normalizeClass;
  

  private SigmoidUnit m_sigmoidUnit;
  

  private LinearUnit m_linearUnit;
  


  public MultilayerPerceptron()
  {
    m_instances = null;
    m_currentInstance = null;
    m_controlPanel = null;
    m_nodePanel = null;
    m_epoch = 0;
    m_error = 0.0D;
    

    m_outputs = new NeuralEnd[0];
    m_inputs = new NeuralEnd[0];
    m_numAttributes = 0;
    m_numClasses = 0;
    m_neuralNodes = new NeuralConnection[0];
    m_selected = new FastVector(4);
    m_graphers = new FastVector(2);
    m_nextId = 0;
    m_stopIt = true;
    m_stopped = true;
    m_accepted = false;
    m_numeric = false;
    m_random = null;
    m_nominalToBinaryFilter = new NominalToBinary();
    m_sigmoidUnit = new SigmoidUnit();
    m_linearUnit = new LinearUnit();
    



    m_normalizeClass = true;
    m_normalizeAttributes = true;
    m_autoBuild = true;
    m_gui = false;
    m_useNomToBin = true;
    m_driftThreshold = 20;
    m_numEpochs = 500;
    m_valSize = 0;
    m_randomSeed = 0;
    m_hiddenLayers = "a";
    m_learningRate = 0.3D;
    m_momentum = 0.2D;
    m_reset = true;
    m_decay = false;
  }
  


  public void setDecay(boolean d)
  {
    m_decay = d;
  }
  


  public boolean getDecay()
  {
    return m_decay;
  }
  









  public void setReset(boolean r)
  {
    if (m_gui) {
      r = false;
    }
    m_reset = r;
  }
  



  public boolean getReset()
  {
    return m_reset;
  }
  




  public void setNormalizeNumericClass(boolean c)
  {
    m_normalizeClass = c;
  }
  


  public boolean getNormalizeNumericClass()
  {
    return m_normalizeClass;
  }
  



  public void setNormalizeAttributes(boolean a)
  {
    m_normalizeAttributes = a;
  }
  


  public boolean getNormalizeAttributes()
  {
    return m_normalizeAttributes;
  }
  



  public void setNominalToBinaryFilter(boolean f)
  {
    m_useNomToBin = f;
  }
  


  public boolean getNominalToBinaryFilter()
  {
    return m_useNomToBin;
  }
  




  public void setSeed(int l)
  {
    if (l >= 0) {
      m_randomSeed = l;
    }
  }
  


  public int getSeed()
  {
    return m_randomSeed;
  }
  





  public void setValidationThreshold(int t)
  {
    if (t > 0) {
      m_driftThreshold = t;
    }
  }
  


  public int getValidationThreshold()
  {
    return m_driftThreshold;
  }
  






  public void setLearningRate(double l)
  {
    if ((l > 0.0D) && (l <= 1.0D)) {
      m_learningRate = l;
      
      if (m_controlPanel != null) {
        m_controlPanel.m_changeLearning.setText("" + l);
      }
    }
  }
  


  public double getLearningRate()
  {
    return m_learningRate;
  }
  




  public void setMomentum(double m)
  {
    if ((m >= 0.0D) && (m <= 1.0D)) {
      m_momentum = m;
      
      if (m_controlPanel != null) {
        m_controlPanel.m_changeMomentum.setText("" + m);
      }
    }
  }
  


  public double getMomentum()
  {
    return m_momentum;
  }
  





  public void setAutoBuild(boolean a)
  {
    if (!m_gui) {
      a = true;
    }
    m_autoBuild = a;
  }
  


  public boolean getAutoBuild()
  {
    return m_autoBuild;
  }
  











  public void setHiddenLayers(String h)
  {
    String tmp = "";
    StringTokenizer tok = new StringTokenizer(h, ",");
    if (tok.countTokens() == 0) {
      return;
    }
    


    boolean first = true;
    while (tok.hasMoreTokens()) {
      String c = tok.nextToken().trim();
      
      if ((c.equals("a")) || (c.equals("i")) || (c.equals("o")) || (c.equals("t")))
      {
        tmp = tmp + c;
      }
      else {
        double dval = Double.valueOf(c).doubleValue();
        int val = (int)dval;
        
        if ((val == dval) && ((val != 0) || ((tok.countTokens() == 0) && (first))) && (val >= 0))
        {
          tmp = tmp + val;
        }
        else {
          return;
        }
      }
      
      first = false;
      if (tok.hasMoreTokens()) {
        tmp = tmp + ", ";
      }
    }
    m_hiddenLayers = tmp;
  }
  



  public String getHiddenLayers()
  {
    return m_hiddenLayers;
  }
  




  public void setGUI(boolean a)
  {
    m_gui = a;
    if (!a) {
      setAutoBuild(true);
    }
    else
    {
      setReset(false);
    }
  }
  


  public boolean getGUI()
  {
    return m_gui;
  }
  



  public void setValidationSetSize(int a)
  {
    if ((a < 0) || (a > 99)) {
      return;
    }
    m_valSize = a;
  }
  


  public int getValidationSetSize()
  {
    return m_valSize;
  }
  







  public void setTrainingTime(int n)
  {
    if (n > 0) {
      m_numEpochs = n;
    }
  }
  


  public int getTrainingTime()
  {
    return m_numEpochs;
  }
  




  private void addNode(NeuralConnection n)
  {
    NeuralConnection[] temp1 = new NeuralConnection[m_neuralNodes.length + 1];
    for (int noa = 0; noa < m_neuralNodes.length; noa++) {
      temp1[noa] = m_neuralNodes[noa];
    }
    
    temp1[(temp1.length - 1)] = n;
    m_neuralNodes = temp1;
  }
  





  private boolean removeNode(NeuralConnection n)
  {
    NeuralConnection[] temp1 = new NeuralConnection[m_neuralNodes.length - 1];
    int skip = 0;
    for (int noa = 0; noa < m_neuralNodes.length; noa++) {
      if (n == m_neuralNodes[noa]) {
        skip++;
      }
      else if (noa - skip < temp1.length) {
        temp1[(noa - skip)] = m_neuralNodes[noa];
      }
      else {
        return false;
      }
    }
    m_neuralNodes = temp1;
    return true;
  }
  









  private Instances setClassType(Instances inst)
    throws Exception
  {
    if (inst != null)
    {
      double min = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      
      m_attributeRanges = new double[inst.numAttributes()];
      m_attributeBases = new double[inst.numAttributes()];
      for (int noa = 0; noa < inst.numAttributes(); noa++) {
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < inst.numInstances(); i++) {
          if (!inst.instance(i).isMissing(noa)) {
            double value = inst.instance(i).value(noa);
            if (value < min) {
              min = value;
            }
            if (value > max) {
              max = value;
            }
          }
        }
        
        m_attributeRanges[noa] = ((max - min) / 2.0D);
        m_attributeBases[noa] = ((max + min) / 2.0D);
        if ((noa != inst.classIndex()) && (m_normalizeAttributes)) {
          for (int i = 0; i < inst.numInstances(); i++) {
            if (m_attributeRanges[noa] != 0.0D) {
              inst.instance(i).setValue(noa, (inst.instance(i).value(noa) - m_attributeBases[noa]) / m_attributeRanges[noa]);

            }
            else
            {
              inst.instance(i).setValue(noa, inst.instance(i).value(noa) - m_attributeBases[noa]);
            }
          }
        }
      }
      
      if (inst.classAttribute().isNumeric()) {
        m_numeric = true;
      }
      else {
        m_numeric = false;
      }
    }
    return inst;
  }
  





  public synchronized void blocker(boolean tf)
  {
    if (tf) {
      try {
        wait();

      }
      catch (InterruptedException e) {}
    } else {
      notifyAll();
    }
  }
  



  private void updateDisplay()
  {
    if (m_gui) {
      m_controlPanel.m_errorLabel.repaint();
      m_controlPanel.m_epochsLabel.repaint();
    }
  }
  



  private void resetNetwork()
  {
    for (int noc = 0; noc < m_numClasses; noc++) {
      m_outputs[noc].reset();
    }
  }
  



  private void calculateOutputs()
  {
    for (int noc = 0; noc < m_numClasses; noc++)
    {
      m_outputs[noc].outputValue(true);
    }
  }
  




  private double calculateErrors()
    throws Exception
  {
    double ret = 0.0D;double temp = 0.0D;
    for (int noc = 0; noc < m_numAttributes; noc++)
    {
      m_inputs[noc].errorValue(true);
    }
    
    for (int noc = 0; noc < m_numClasses; noc++) {
      temp = m_outputs[noc].errorValue(false);
      ret += temp * temp;
    }
    return ret;
  }
  






  private void updateNetworkWeights(double l, double m)
  {
    for (int noc = 0; noc < m_numClasses; noc++)
    {
      m_outputs[noc].updateWeights(l, m);
    }
  }
  


  private void setupInputs()
    throws Exception
  {
    m_inputs = new NeuralEnd[m_numAttributes];
    int now = 0;
    for (int noa = 0; noa < m_numAttributes + 1; noa++) {
      if (m_instances.classIndex() != noa) {
        m_inputs[(noa - now)] = new NeuralEnd(m_instances.attribute(noa).name());
        
        m_inputs[(noa - now)].setX(0.1D);
        m_inputs[(noa - now)].setY((noa - now + 1.0D) / (m_numAttributes + 1));
        m_inputs[(noa - now)].setLink(true, noa);
      }
      else {
        now = 1;
      }
    }
  }
  



  private void setupOutputs()
    throws Exception
  {
    m_outputs = new NeuralEnd[m_numClasses];
    for (int noa = 0; noa < m_numClasses; noa++) {
      if (m_numeric) {
        m_outputs[noa] = new NeuralEnd(m_instances.classAttribute().name());
      }
      else {
        m_outputs[noa] = new NeuralEnd(m_instances.classAttribute().value(noa));
      }
      
      m_outputs[noa].setX(0.9D);
      m_outputs[noa].setY((noa + 1.0D) / (m_numClasses + 1));
      m_outputs[noa].setLink(false, noa);
      NeuralNode temp = new NeuralNode(String.valueOf(m_nextId), m_random, m_sigmoidUnit);
      
      m_nextId += 1;
      temp.setX(0.75D);
      temp.setY((noa + 1.0D) / (m_numClasses + 1));
      addNode(temp);
      NeuralConnection.connect(temp, m_outputs[noa]);
    }
  }
  




  private void setupHiddenLayer()
  {
    StringTokenizer tok = new StringTokenizer(m_hiddenLayers, ",");
    int val = 0;
    int prev = 0;
    int num = tok.countTokens();
    
    for (int noa = 0; noa < num; noa++)
    {


      String c = tok.nextToken().trim();
      if (c.equals("a")) {
        val = (m_numAttributes + m_numClasses) / 2;
      }
      else if (c.equals("i")) {
        val = m_numAttributes;
      }
      else if (c.equals("o")) {
        val = m_numClasses;
      }
      else if (c.equals("t")) {
        val = m_numAttributes + m_numClasses;
      }
      else {
        val = Double.valueOf(c).intValue();
      }
      for (int nob = 0; nob < val; nob++) {
        NeuralNode temp = new NeuralNode(String.valueOf(m_nextId), m_random, m_sigmoidUnit);
        
        m_nextId += 1;
        temp.setX(0.5D / num * noa + 0.25D);
        temp.setY((nob + 1.0D) / (val + 1));
        addNode(temp);
        if (noa > 0)
        {
          for (int noc = m_neuralNodes.length - nob - 1 - prev; 
              noc < m_neuralNodes.length - nob - 1; noc++) {
            NeuralConnection.connect(m_neuralNodes[noc], temp);
          }
        }
      }
      prev = val;
    }
    tok = new StringTokenizer(m_hiddenLayers, ",");
    String c = tok.nextToken();
    if (c.equals("a")) {
      val = (m_numAttributes + m_numClasses) / 2;
    }
    else if (c.equals("i")) {
      val = m_numAttributes;
    }
    else if (c.equals("o")) {
      val = m_numClasses;
    }
    else if (c.equals("t")) {
      val = m_numAttributes + m_numClasses;
    }
    else {
      val = Double.valueOf(c).intValue();
    }
    
    if (val == 0) {
      for (int noa = 0; noa < m_numAttributes; noa++) {
        for (int nob = 0; nob < m_numClasses; nob++) {
          NeuralConnection.connect(m_inputs[noa], m_neuralNodes[nob]);
        }
      }
    }
    else {
      for (int noa = 0; noa < m_numAttributes; noa++) {
        for (int nob = m_numClasses; nob < m_numClasses + val; nob++) {
          NeuralConnection.connect(m_inputs[noa], m_neuralNodes[nob]);
        }
      }
      for (int noa = m_neuralNodes.length - prev; noa < m_neuralNodes.length; 
          noa++) {
        for (int nob = 0; nob < m_numClasses; nob++) {
          NeuralConnection.connect(m_neuralNodes[noa], m_neuralNodes[nob]);
        }
      }
    }
  }
  





  private void setEndsToLinear()
  {
    for (int noa = 0; noa < m_neuralNodes.length; noa++) {
      if ((m_neuralNodes[noa].getType() & 0x8) == 8)
      {
        ((NeuralNode)m_neuralNodes[noa]).setMethod(m_linearUnit);
      }
      else {
        ((NeuralNode)m_neuralNodes[noa]).setMethod(m_sigmoidUnit);
      }
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances i)
    throws Exception
  {
    getCapabilities().testWithFail(i);
    

    i = new Instances(i);
    i.deleteWithMissingClass();
    
    m_ZeroR = new ZeroR();
    m_ZeroR.buildClassifier(i);
    
    if (i.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_useDefaultModel = true;
      return;
    }
    
    m_useDefaultModel = false;
    

    m_epoch = 0;
    m_error = 0.0D;
    m_instances = null;
    m_currentInstance = null;
    m_controlPanel = null;
    m_nodePanel = null;
    

    m_outputs = new NeuralEnd[0];
    m_inputs = new NeuralEnd[0];
    m_numAttributes = 0;
    m_numClasses = 0;
    m_neuralNodes = new NeuralConnection[0];
    
    m_selected = new FastVector(4);
    m_graphers = new FastVector(2);
    m_nextId = 0;
    m_stopIt = true;
    m_stopped = true;
    m_accepted = false;
    m_instances = new Instances(i);
    m_random = new Random(m_randomSeed);
    m_instances.randomize(m_random);
    
    if (m_useNomToBin) {
      m_nominalToBinaryFilter = new NominalToBinary();
      m_nominalToBinaryFilter.setInputFormat(m_instances);
      m_instances = Filter.useFilter(m_instances, m_nominalToBinaryFilter);
    }
    
    m_numAttributes = (m_instances.numAttributes() - 1);
    m_numClasses = m_instances.numClasses();
    

    setClassType(m_instances);
    



    Instances valSet = null;
    
    int numInVal = (int)(m_valSize / 100.0D * m_instances.numInstances());
    if (m_valSize > 0) {
      if (numInVal == 0) {
        numInVal = 1;
      }
      valSet = new Instances(m_instances, 0, numInVal);
    }
    

    setupInputs();
    
    setupOutputs();
    if (m_autoBuild) {
      setupHiddenLayer();
    }
    


    if (m_gui) {
      m_win = new JFrame();
      
      m_win.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          boolean k = m_stopIt;
          m_stopIt = true;
          int well = JOptionPane.showConfirmDialog(m_win, "Are You Sure...\nClick Yes To Accept The Neural Network\n Click No To Return", "Accept Neural Network", 0);
          






          if (well == 0) {
            m_win.setDefaultCloseOperation(2);
            m_accepted = true;
            blocker(false);
          }
          else {
            m_win.setDefaultCloseOperation(0);
          }
          m_stopIt = k;
        }
        
      });
      m_win.getContentPane().setLayout(new BorderLayout());
      m_win.setTitle("Neural Network");
      m_nodePanel = new NodePanel();
      




      m_nodePanel.setPreferredSize(new Dimension(640, 480));
      m_nodePanel.revalidate();
      
      JScrollPane sp = new JScrollPane(m_nodePanel, 22, 31);
      

      m_controlPanel = new ControlPanel();
      
      m_win.getContentPane().add(sp, "Center");
      m_win.getContentPane().add(m_controlPanel, "South");
      m_win.setSize(640, 480);
      m_win.setVisible(true);
    }
    

    if (m_gui) {
      blocker(true);
      m_controlPanel.m_changeEpochs.setEnabled(false);
      m_controlPanel.m_changeLearning.setEnabled(false);
      m_controlPanel.m_changeMomentum.setEnabled(false);
    }
    


    if (m_numeric) {
      setEndsToLinear();
    }
    if (m_accepted) {
      m_win.dispose();
      m_controlPanel = null;
      m_nodePanel = null;
      m_instances = new Instances(m_instances, 0);
      m_currentInstance = null;
      return;
    }
    

    double right = 0.0D;
    double driftOff = 0.0D;
    double lastRight = Double.POSITIVE_INFINITY;
    double bestError = Double.POSITIVE_INFINITY;
    
    double totalWeight = 0.0D;
    double totalValWeight = 0.0D;
    double origRate = m_learningRate;
    

    if (numInVal == m_instances.numInstances()) {
      numInVal--;
    }
    if (numInVal < 0) {
      numInVal = 0;
    }
    for (int noa = numInVal; noa < m_instances.numInstances(); noa++) {
      if (!m_instances.instance(noa).classIsMissing()) {
        totalWeight += m_instances.instance(noa).weight();
      }
    }
    if (m_valSize != 0) {
      for (int noa = 0; noa < valSet.numInstances(); noa++) {
        if (!valSet.instance(noa).classIsMissing()) {
          totalValWeight += valSet.instance(noa).weight();
        }
      }
    }
    m_stopped = false;
    

    for (int noa = 1; noa < m_numEpochs + 1; noa++) {
      right = 0.0D;
      for (int nob = numInVal; nob < m_instances.numInstances(); nob++) {
        m_currentInstance = m_instances.instance(nob);
        
        if (!m_currentInstance.classIsMissing())
        {


          resetNetwork();
          calculateOutputs();
          double tempRate = m_learningRate * m_currentInstance.weight();
          if (m_decay) {
            tempRate /= noa;
          }
          
          right += calculateErrors() / m_instances.numClasses() * m_currentInstance.weight();
          
          updateNetworkWeights(tempRate, m_momentum);
        }
      }
      

      right /= totalWeight;
      if ((Double.isInfinite(right)) || (Double.isNaN(right))) {
        if (!m_reset) {
          m_instances = null;
          throw new Exception("Network cannot train. Try restarting with a smaller learning rate.");
        }
        


        if (m_learningRate <= Utils.SMALL) {
          throw new IllegalStateException("Learning rate got too small (" + m_learningRate + " <= " + Utils.SMALL + ")!");
        }
        
        m_learningRate /= 2.0D;
        buildClassifier(i);
        m_learningRate = origRate;
        m_instances = new Instances(m_instances, 0);
        m_currentInstance = null;
        return;
      }
      


      if (m_valSize != 0) {
        right = 0.0D;
        for (int nob = 0; nob < valSet.numInstances(); nob++) {
          m_currentInstance = valSet.instance(nob);
          if (!m_currentInstance.classIsMissing())
          {
            resetNetwork();
            calculateOutputs();
            right += calculateErrors() / valSet.numClasses() * m_currentInstance.weight();
          }
        }
        





        if (right < lastRight)
        {
          if (right < bestError) {
            bestError = right;
            
            for (int noc = 0; noc < m_numClasses; noc++) {
              m_outputs[noc].saveWeights();
            }
            driftOff = 0.0D;
          }
        }
        else {
          driftOff += 1.0D;
        }
        lastRight = right;
        if ((driftOff > m_driftThreshold) || (noa + 1 >= m_numEpochs)) {
          for (int noc = 0; noc < m_numClasses; noc++) {
            m_outputs[noc].restoreWeights();
          }
          m_accepted = true;
        }
        right /= totalValWeight;
      }
      m_epoch = noa;
      m_error = right;
      
      updateDisplay();
      

      if (m_gui) {
        while (((m_stopIt) || ((m_epoch >= m_numEpochs) && (m_valSize == 0))) && (!m_accepted))
        {
          m_stopIt = true;
          m_stopped = true;
          if ((m_epoch >= m_numEpochs) && (m_valSize == 0))
          {
            m_controlPanel.m_startStop.setEnabled(false);
          }
          else {
            m_controlPanel.m_startStop.setEnabled(true);
          }
          m_controlPanel.m_startStop.setText("Start");
          m_controlPanel.m_startStop.setActionCommand("Start");
          m_controlPanel.m_changeEpochs.setEnabled(true);
          m_controlPanel.m_changeLearning.setEnabled(true);
          m_controlPanel.m_changeMomentum.setEnabled(true);
          
          blocker(true);
          if (m_numeric) {
            setEndsToLinear();
          }
        }
        m_controlPanel.m_changeEpochs.setEnabled(false);
        m_controlPanel.m_changeLearning.setEnabled(false);
        m_controlPanel.m_changeMomentum.setEnabled(false);
        
        m_stopped = false;
        
        if (m_accepted) {
          m_win.dispose();
          m_controlPanel = null;
          m_nodePanel = null;
          m_instances = new Instances(m_instances, 0);
          m_currentInstance = null;
          return;
        }
      }
      if (m_accepted) {
        m_instances = new Instances(m_instances, 0);
        m_currentInstance = null;
        return;
      }
    }
    if (m_gui) {
      m_win.dispose();
      m_controlPanel = null;
      m_nodePanel = null;
    }
    m_instances = new Instances(m_instances, 0);
    m_currentInstance = null;
  }
  







  public double[] distributionForInstance(Instance i)
    throws Exception
  {
    if (m_useDefaultModel) {
      return m_ZeroR.distributionForInstance(i);
    }
    
    if (m_useNomToBin) {
      m_nominalToBinaryFilter.input(i);
      m_currentInstance = m_nominalToBinaryFilter.output();
    }
    else {
      m_currentInstance = i;
    }
    

    m_currentInstance = ((Instance)m_currentInstance.copy());
    
    if (m_normalizeAttributes) {
      for (int noa = 0; noa < m_instances.numAttributes(); noa++) {
        if (noa != m_instances.classIndex()) {
          if (m_attributeRanges[noa] != 0.0D) {
            m_currentInstance.setValue(noa, (m_currentInstance.value(noa) - m_attributeBases[noa]) / m_attributeRanges[noa]);

          }
          else
          {
            m_currentInstance.setValue(noa, m_currentInstance.value(noa) - m_attributeBases[noa]);
          }
        }
      }
    }
    
    resetNetwork();
    


    double[] theArray = new double[m_numClasses];
    for (int noa = 0; noa < m_numClasses; noa++) {
      theArray[noa] = m_outputs[noa].outputValue(true);
    }
    if (m_instances.classAttribute().isNumeric()) {
      return theArray;
    }
    

    double count = 0.0D;
    for (int noa = 0; noa < m_numClasses; noa++) {
      count += theArray[noa];
    }
    if (count <= 0.0D) {
      return m_ZeroR.distributionForInstance(i);
    }
    for (int noa = 0; noa < m_numClasses; noa++) {
      theArray[noa] /= count;
    }
    return theArray;
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(14);
    
    newVector.addElement(new Option("\tLearning Rate for the backpropagation algorithm.\n\t(Value should be between 0 - 1, Default = 0.3).", "L", 1, "-L <learning rate>"));
    


    newVector.addElement(new Option("\tMomentum Rate for the backpropagation algorithm.\n\t(Value should be between 0 - 1, Default = 0.2).", "M", 1, "-M <momentum>"));
    


    newVector.addElement(new Option("\tNumber of epochs to train through.\n\t(Default = 500).", "N", 1, "-N <number of epochs>"));
    


    newVector.addElement(new Option("\tPercentage size of validation set to use to terminate\n\ttraining (if this is non zero it can pre-empt num of epochs.\n\t(Value should be between 0 - 100, Default = 0).", "V", 1, "-V <percentage size of validation set>"));
    



    newVector.addElement(new Option("\tThe value used to seed the random number generator\n\t(Value should be >= 0 and and a long, Default = 0).", "S", 1, "-S <seed>"));
    


    newVector.addElement(new Option("\tThe consequetive number of errors allowed for validation\n\ttesting before the netwrok terminates.\n\t(Value should be > 0, Default = 20).", "E", 1, "-E <threshold for number of consequetive errors>"));
    



    newVector.addElement(new Option("\tGUI will be opened.\n\t(Use this to bring up a GUI).", "G", 0, "-G"));
    


    newVector.addElement(new Option("\tAutocreation of the network connections will NOT be done.\n\t(This will be ignored if -G is NOT set)", "A", 0, "-A"));
    


    newVector.addElement(new Option("\tA NominalToBinary filter will NOT automatically be used.\n\t(Set this to not use a NominalToBinary filter).", "B", 0, "-B"));
    


    newVector.addElement(new Option("\tThe hidden layers to be created for the network.\n\t(Value should be a list of comma separated Natural \n\tnumbers or the letters 'a' = (attribs + classes) / 2, \n\t'i' = attribs, 'o' = classes, 't' = attribs .+ classes)\n\tfor wildcard values, Default = a).", "H", 1, "-H <comma seperated numbers for nodes on each layer>"));
    





    newVector.addElement(new Option("\tNormalizing a numeric class will NOT be done.\n\t(Set this to not normalize the class if it's numeric).", "C", 0, "-C"));
    


    newVector.addElement(new Option("\tNormalizing the attributes will NOT be done.\n\t(Set this to not normalize the attributes).", "I", 0, "-I"));
    


    newVector.addElement(new Option("\tReseting the network will NOT be allowed.\n\t(Set this to not allow the network to reset).", "R", 0, "-R"));
    


    newVector.addElement(new Option("\tLearning rate decay will occur.\n\t(Set this to cause the learning rate to decay).", "D", 0, "-D"));
    




    return newVector.elements();
  }
  







































































  public void setOptions(String[] options)
    throws Exception
  {
    String learningString = Utils.getOption('L', options);
    if (learningString.length() != 0) {
      setLearningRate(new Double(learningString).doubleValue());
    } else {
      setLearningRate(0.3D);
    }
    String momentumString = Utils.getOption('M', options);
    if (momentumString.length() != 0) {
      setMomentum(new Double(momentumString).doubleValue());
    } else {
      setMomentum(0.2D);
    }
    String epochsString = Utils.getOption('N', options);
    if (epochsString.length() != 0) {
      setTrainingTime(Integer.parseInt(epochsString));
    } else {
      setTrainingTime(500);
    }
    String valSizeString = Utils.getOption('V', options);
    if (valSizeString.length() != 0) {
      setValidationSetSize(Integer.parseInt(valSizeString));
    } else {
      setValidationSetSize(0);
    }
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setSeed(Integer.parseInt(seedString));
    } else {
      setSeed(0);
    }
    String thresholdString = Utils.getOption('E', options);
    if (thresholdString.length() != 0) {
      setValidationThreshold(Integer.parseInt(thresholdString));
    } else {
      setValidationThreshold(20);
    }
    String hiddenLayers = Utils.getOption('H', options);
    if (hiddenLayers.length() != 0) {
      setHiddenLayers(hiddenLayers);
    } else {
      setHiddenLayers("a");
    }
    if (Utils.getFlag('G', options)) {
      setGUI(true);
    } else {
      setGUI(false);
    }
    

    if (Utils.getFlag('A', options)) {
      setAutoBuild(false);
    } else {
      setAutoBuild(true);
    }
    if (Utils.getFlag('B', options)) {
      setNominalToBinaryFilter(false);
    } else {
      setNominalToBinaryFilter(true);
    }
    if (Utils.getFlag('C', options)) {
      setNormalizeNumericClass(false);
    } else {
      setNormalizeNumericClass(true);
    }
    if (Utils.getFlag('I', options)) {
      setNormalizeAttributes(false);
    } else {
      setNormalizeAttributes(true);
    }
    if (Utils.getFlag('R', options)) {
      setReset(false);
    } else {
      setReset(true);
    }
    if (Utils.getFlag('D', options)) {
      setDecay(true);
    } else {
      setDecay(false);
    }
    
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[21];
    int current = 0;
    options[(current++)] = "-L";options[(current++)] = ("" + getLearningRate());
    options[(current++)] = "-M";options[(current++)] = ("" + getMomentum());
    options[(current++)] = "-N";options[(current++)] = ("" + getTrainingTime());
    options[(current++)] = "-V";options[(current++)] = ("" + getValidationSetSize());
    options[(current++)] = "-S";options[(current++)] = ("" + getSeed());
    options[(current++)] = "-E";options[(current++)] = ("" + getValidationThreshold());
    options[(current++)] = "-H";options[(current++)] = getHiddenLayers();
    if (getGUI()) {
      options[(current++)] = "-G";
    }
    if (!getAutoBuild()) {
      options[(current++)] = "-A";
    }
    if (!getNominalToBinaryFilter()) {
      options[(current++)] = "-B";
    }
    if (!getNormalizeNumericClass()) {
      options[(current++)] = "-C";
    }
    if (!getNormalizeAttributes()) {
      options[(current++)] = "-I";
    }
    if (!getReset()) {
      options[(current++)] = "-R";
    }
    if (getDecay()) {
      options[(current++)] = "-D";
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  



  public String toString()
  {
    if (m_useDefaultModel) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    StringBuffer model = new StringBuffer(m_neuralNodes.length * 100);
    



    for (int noa = 0; noa < m_neuralNodes.length; noa++) {
      NeuralNode con = (NeuralNode)m_neuralNodes[noa];
      
      double[] weights = con.getWeights();
      NeuralConnection[] inputs = con.getInputs();
      if ((con.getMethod() instanceof SigmoidUnit)) {
        model.append("Sigmoid ");
      }
      else if ((con.getMethod() instanceof LinearUnit)) {
        model.append("Linear ");
      }
      model.append("Node " + con.getId() + "\n    Inputs    Weights\n");
      model.append("    Threshold    " + weights[0] + "\n");
      for (int nob = 1; nob < con.getNumInputs() + 1; nob++) {
        if ((inputs[(nob - 1)].getType() & 0x1) == 1)
        {
          model.append("    Attrib " + m_instances.attribute(((NeuralEnd)inputs[(nob - 1)]).getLink()).name() + "    " + weights[nob] + "\n");

        }
        else
        {

          model.append("    Node " + inputs[(nob - 1)].getId() + "    " + weights[nob] + "\n");
        }
      }
    }
    

    for (int noa = 0; noa < m_outputs.length; noa++) {
      NeuralConnection[] inputs = m_outputs[noa].getInputs();
      model.append("Class " + m_instances.classAttribute().value(m_outputs[noa].getLink()) + "\n    Input\n");
      


      for (int nob = 0; nob < m_outputs[noa].getNumInputs(); nob++) {
        if ((inputs[nob].getType() & 0x1) == 1)
        {
          model.append("    Attrib " + m_instances.attribute(((NeuralEnd)inputs[nob]).getLink()).name() + "\n");

        }
        else
        {
          model.append("    Node " + inputs[nob].getId() + "\n");
        }
      }
    }
    return model.toString();
  }
  



  public String globalInfo()
  {
    return "A Classifier that uses backpropagation to classify instances.\nThis network can be built by hand, created by an algorithm or both. The network can also be monitored and modified during training time. The nodes in this network are all sigmoid (except for when the class is numeric in which case the the output nodes become unthresholded linear units).";
  }
  








  public String learningRateTipText()
  {
    return "The amount the weights are updated.";
  }
  



  public String momentumTipText()
  {
    return "Momentum applied to the weights during updating.";
  }
  


  public String autoBuildTipText()
  {
    return "Adds and connects up hidden layers in the network.";
  }
  


  public String seedTipText()
  {
    return "Seed used to initialise the random number generator.Random numbers are used for setting the initial weights of the connections betweem nodes, and also for shuffling the training data.";
  }
  




  public String validationThresholdTipText()
  {
    return "Used to terminate validation testing.The value here dictates how many times in a row the validation set error can get worse before training is terminated.";
  }
  




  public String GUITipText()
  {
    return "Brings up a gui interface. This will allow the pausing and altering of the nueral network during training.\n\n* To add a node left click (this node will be automatically selected, ensure no other nodes were selected).\n* To select a node left click on it either while no other node is selected or while holding down the control key (this toggles that node as being selected and not selected.\n* To connect a node, first have the start node(s) selected, then click either the end node or on an empty space (this will create a new node that is connected with the selected nodes). The selection status of nodes will stay the same after the connection. (Note these are directed connections, also a connection between two nodes will not be established more than once and certain connections that are deemed to be invalid will not be made).\n* To remove a connection select one of the connected node(s) in the connection and then right click the other node (it does not matter whether the node is the start or end the connection will be removed).\n* To remove a node right click it while no other nodes (including it) are selected. (This will also remove all connections to it)\n.* To deselect a node either left click it while holding down control, or right click on empty space.\n* The raw inputs are provided from the labels on the left.\n* The red nodes are hidden layers.\n* The orange nodes are the output nodes.\n* The labels on the right show the class the output node represents. Note that with a numeric class the output node will automatically be made into an unthresholded linear unit.\n\nAlterations to the neural network can only be done while the network is not running, This also applies to the learning rate and other fields on the control panel.\n\n* You can accept the network as being finished at any time.\n* The network is automatically paused at the beginning.\n* There is a running indication of what epoch the network is up to and what the (rough) error for that epoch was (or for the validation if that is being used). Note that this error value is based on a network that changes as the value is computed. (also depending on whether the class is normalized will effect the error reported for numeric classes.\n* Once the network is done it will pause again and either wait to be accepted or trained more.\n\nNote that if the gui is not set the network will not require any interaction.\n";
  }
  














































  public String validationSetSizeTipText()
  {
    return "The percentage size of the validation set.(The training will continue until it is observed that the error on the validation set has been consistently getting worse, or if the training time is reached).\nIf This is set to zero no validation set will be used and instead the network will train for the specified number of epochs.";
  }
  







  public String trainingTimeTipText()
  {
    return "The number of epochs to train through. If the validation set is non-zero then it can terminate the network early";
  }
  





  public String nominalToBinaryFilterTipText()
  {
    return "This will preprocess the instances with the filter. This could help improve performance if there are nominal attributes in the data.";
  }
  




  public String hiddenLayersTipText()
  {
    return "This defines the hidden layers of the neural network. This is a list of positive whole numbers. 1 for each hidden layer. Comma seperated. To have no hidden layers put a single 0 here. This will only be used if autobuild is set. There are also wildcard values 'a' = (attribs + classes) / 2, 'i' = attribs, 'o' = classes , 't' = attribs + classes.";
  }
  






  public String normalizeNumericClassTipText()
  {
    return "This will normalize the class if it's numeric. This could help improve performance of the network, It normalizes the class to be between -1 and 1. Note that this is only internally, the output will be scaled back to the original range.";
  }
  




  public String normalizeAttributesTipText()
  {
    return "This will normalize the attributes. This could help improve performance of the network. This is not reliant on the class being numeric. This will also normalize nominal attributes as well (after they have been run through the nominal to binary filter if that is in use) so that the nominal values are between -1 and 1";
  }
  






  public String resetTipText()
  {
    return "This will allow the network to reset with a lower learning rate. If the network diverges from the answer this will automatically reset the network with a lower learning rate and begin training again. This option is only available if the gui is not set. Note that if the network diverges but isn't allowed to reset it will fail the training process and return an error message.";
  }
  







  public String decayTipText()
  {
    return "This will cause the learning rate to decrease. This will divide the starting learning rate by the epoch number, to determine what the current learning rate should be. This may help to stop the network from diverging from the target output, as well as improve general performance. Note that the decaying learning rate will not be shown in the gui, only the original learning rate. If the learning rate is changed in the gui, this is treated as the starting learning rate.";
  }
  











  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10073 $");
  }
}
