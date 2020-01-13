package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import weka.experiment.Experiment;
































public class RunNumberPanel
  extends JPanel
{
  private static final long serialVersionUID = -1644336658426067852L;
  protected JTextField m_LowerText;
  protected JTextField m_UpperText;
  protected Experiment m_Exp;
  
  public RunNumberPanel()
  {
    Messages.getInstance();m_LowerText = new JTextField(Messages.getString("RunNumberPanel_LowerText_JTextField_Text"));
    

    Messages.getInstance();m_UpperText = new JTextField(Messages.getString("RunNumberPanel_UpperText_JTextField_Text"));
    










    m_LowerText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        m_Exp.setRunLower(getLower());
      }
    });
    m_LowerText.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        m_Exp.setRunLower(getLower());
      }
    });
    m_UpperText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        m_Exp.setRunUpper(getUpper());
      }
    });
    m_UpperText.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        m_Exp.setRunUpper(getUpper());
      }
    });
    m_LowerText.setEnabled(false);
    m_UpperText.setEnabled(false);
    

    setLayout(new GridLayout(1, 2));
    Messages.getInstance();setBorder(BorderFactory.createTitledBorder(Messages.getString("RunNumberPanel_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    Box b1 = new Box(0);
    b1.add(Box.createHorizontalStrut(10));
    Messages.getInstance();b1.add(new JLabel(Messages.getString("RunNumberPanel_B1_JLabel_Text"), 4));
    b1.add(Box.createHorizontalStrut(5));
    b1.add(m_LowerText);
    add(b1);
    Box b2 = new Box(0);
    b2.add(Box.createHorizontalStrut(10));
    Messages.getInstance();b2.add(new JLabel(Messages.getString("RunNumberPanel_B2_JLabel_Text"), 4));
    b2.add(Box.createHorizontalStrut(5));
    b2.add(m_UpperText);
    add(b2);
  }
  





  public RunNumberPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_LowerText.setText("" + m_Exp.getRunLower());
    m_UpperText.setText("" + m_Exp.getRunUpper());
    m_LowerText.setEnabled(true);
    m_UpperText.setEnabled(true);
  }
  





  public int getLower()
  {
    int result = 1;
    try {
      result = Integer.parseInt(m_LowerText.getText());
    }
    catch (Exception ex) {}
    return Math.max(1, result);
  }
  





  public int getUpper()
  {
    int result = 1;
    try {
      result = Integer.parseInt(m_UpperText.getText());
    }
    catch (Exception ex) {}
    return Math.max(1, result);
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("RunNumberPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(new RunNumberPanel(new Experiment()), "Center");
      
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
