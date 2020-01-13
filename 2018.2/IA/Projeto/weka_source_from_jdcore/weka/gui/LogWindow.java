package weka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintStream;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import weka.core.Tee;
import weka.core.Utils;

































public class LogWindow
  extends JFrame
  implements CaretListener, ChangeListener
{
  private static final long serialVersionUID = 5650947361381061112L;
  public static final String STYLE_STDOUT = "stdout";
  public static final String STYLE_STDERR = "stderr";
  public static final Color COLOR_STDOUT = Color.BLACK;
  

  public static final Color COLOR_STDERR = Color.RED;
  

  public static final boolean DEBUG = false;
  

  public boolean m_UseWordwrap = true;
  

  protected JTextPane m_Output = new JTextPane();
  

  protected JButton m_ButtonClear;
  

  protected JButton m_ButtonClose;
  

  protected JLabel m_LabelCurrentSize;
  

  protected JSpinner m_SpinnerMaxSize;
  

  protected JCheckBox m_CheckBoxWordwrap;
  

  protected static Tee m_TeeOut = null;
  

  protected static Tee m_TeeErr = null;
  

  protected class LogWindowPrintStream
    extends PrintStream
  {
    protected LogWindow m_Parent = null;
    

    protected String m_Style = null;
    







    public LogWindowPrintStream(LogWindow parent, PrintStream stream, String style)
    {
      super();
      
      m_Parent = parent;
      m_Style = style;
    }
    




    public synchronized void flush() {}
    



    public synchronized void print(int x)
    {
      print(new Integer(x).toString());
    }
    


    public synchronized void print(boolean x)
    {
      print(new Boolean(x).toString());
    }
    







    public synchronized void print(String x)
    {
      StyledDocument doc = m_Parent.m_Output.getStyledDocument();
      
      try
      {
        doc.insertString(doc.getLength(), x, doc.getStyle(m_Style));
        

        m_Parent.m_Output.setCaretPosition(doc.getLength());
        

        m_Parent.trim();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    







    public synchronized void print(Object x)
    {
      if ((x instanceof Throwable)) {
        Throwable t = (Throwable)x;
        StackTraceElement[] trace = t.getStackTrace();
        String line = t.getMessage() + "\n";
        for (int i = 0; i < trace.length; i++)
          line = line + "\t" + trace[i].toString() + "\n";
        x = line;
      }
      
      if (x == null) {
        print("null");
      } else {
        print(x.toString());
      }
    }
    

    public synchronized void println()
    {
      print("\n");
    }
    


    public synchronized void println(int x)
    {
      print(x);
      println();
    }
    


    public synchronized void println(boolean x)
    {
      print(x);
      println();
    }
    


    public synchronized void println(String x)
    {
      print(x);
      println();
    }
    


    public synchronized void println(Object x)
    {
      print(x);
      println();
    }
  }
  


  public LogWindow()
  {
    super(Messages.getString("LogWindow_Text"));Messages.getInstance();m_ButtonClear = new JButton(Messages.getString("LogWindow_ButtonClear_JButton_Text"));Messages.getInstance();m_ButtonClose = new JButton(Messages.getString("LogWindow_ButtonClose_JButton_Text"));Messages.getInstance();m_LabelCurrentSize = new JLabel(Messages.getString("LogWindow_LabelCurrentSize_JLabel_Text"));m_SpinnerMaxSize = new JSpinner();Messages.getInstance();m_CheckBoxWordwrap = new JCheckBox(Messages.getString("LogWindow_CheckBoxWordwrap_JCheckBox_Text"));
    
    createFrame();
    





    StyledDocument doc = m_Output.getStyledDocument();
    Style style = StyleContext.getDefaultStyleContext().getStyle("default");
    
    style = doc.addStyle("stdout", style);
    StyleConstants.setFontFamily(style, "monospaced");
    StyleConstants.setForeground(style, COLOR_STDOUT);
    
    style = StyleContext.getDefaultStyleContext().getStyle("default");
    
    style = doc.addStyle("stderr", style);
    StyleConstants.setFontFamily(style, "monospaced");
    StyleConstants.setForeground(style, COLOR_STDERR);
    

    boolean teeDone = (m_TeeOut != null) || (m_TeeErr != null);
    
    if (!teeDone) {
      m_TeeOut = new Tee(System.out);
      System.setOut(m_TeeOut);
    }
    m_TeeOut.add(new LogWindowPrintStream(this, m_TeeOut.getDefault(), "stdout"));
    


    if (!teeDone) {
      m_TeeErr = new Tee(System.err);
      System.setErr(m_TeeErr);
    }
    m_TeeErr.add(new LogWindowPrintStream(this, m_TeeErr.getDefault(), "stderr"));
  }
  












  protected void createFrame()
  {
    setSize(600, 400);
    int width = getBoundswidth;
    setLocation(getGraphicsConfigurationgetBoundswidth - width, getLocationy);
    
    getContentPane().setLayout(new BorderLayout());
    

    getContentPane().add(new JScrollPane(m_Output), "Center");
    setWordwrap(m_UseWordwrap);
    

    JPanel panel = new JPanel(new BorderLayout());
    getContentPane().add(panel, "South");
    JPanel panel3 = new JPanel(new BorderLayout());
    panel.add(panel3, "South");
    JPanel panel2 = new JPanel(new FlowLayout(2));
    panel3.add(panel2, "East");
    
    m_ButtonClear.setMnemonic('C');
    m_ButtonClear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clear();
      }
    });
    panel2.add(m_ButtonClear);
    
    m_ButtonClose.setMnemonic('l');
    m_ButtonClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    panel2.add(m_ButtonClose);
    

    panel2 = new JPanel(new GridLayout(1, 3));
    panel3.add(panel2, "West");
    

    JPanel panel4 = new JPanel(new FlowLayout());
    panel2.add(panel4);
    SpinnerNumberModel model = (SpinnerNumberModel)m_SpinnerMaxSize.getModel();
    model.setMinimum(new Integer(1));
    model.setStepSize(new Integer(1000));
    model.setValue(new Integer(100000));
    model.addChangeListener(this);
    
    Messages.getInstance();JLabel label = new JLabel(Messages.getString("LogWindow_CreateFrame_JLabel_Text"));
    label.setDisplayedMnemonic('m');
    label.setLabelFor(m_SpinnerMaxSize);
    
    panel4.add(label);
    panel4.add(m_SpinnerMaxSize);
    

    panel4 = new JPanel(new FlowLayout());
    panel2.add(panel4);
    panel4.add(m_LabelCurrentSize);
    

    panel4 = new JPanel(new FlowLayout());
    panel2.add(panel4);
    m_CheckBoxWordwrap.setSelected(m_UseWordwrap);
    m_CheckBoxWordwrap.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setWordwrap(m_CheckBoxWordwrap.isSelected());
      }
    });
    panel4.add(m_CheckBoxWordwrap);
  }
  


  public void clear()
  {
    m_Output.setText("");
  }
  


  public void close()
  {
    setVisible(false);
  }
  







  public void trim()
  {
    StyledDocument doc = m_Output.getStyledDocument();
    

    int size = doc.getLength();
    int maxSize = ((Integer)m_SpinnerMaxSize.getValue()).intValue();
    if (size > maxSize) {
      try
      {
        int pos = size - maxSize;
        while (!doc.getText(pos, 1).equals("\n"))
          pos++;
        while (doc.getText(pos, 1).equals("\n")) {
          pos++;
        }
        doc.remove(0, pos);
      }
      catch (Exception ex) {}
    }
    





    m_Output.setCaretPosition(doc.getLength());
  }
  




  protected String colorToString(Color c)
  {
    String result = "#" + Utils.padLeft(Integer.toHexString(c.getRed()), 2) + Utils.padLeft(Integer.toHexString(c.getGreen()), 2) + Utils.padLeft(Integer.toHexString(c.getBlue()), 2);
    


    result = result.replaceAll("\\ ", "0").toUpperCase();
    
    return result;
  }
  







  public void setWordwrap(boolean wrap)
  {
    m_UseWordwrap = wrap;
    if (m_CheckBoxWordwrap.isSelected() != m_UseWordwrap) {
      m_CheckBoxWordwrap.setSelected(m_UseWordwrap);
    }
    
    Container parent = m_Output.getParent();
    JTextPane outputOld = m_Output;
    if (m_UseWordwrap) {
      m_Output = new JTextPane();
    } else
      m_Output = new JTextPane() {
        private static final long serialVersionUID = -8275856175921425981L;
        
        public void setSize(Dimension d) { if (width < getGraphicsConfigurationgetBoundswidth)
            width = getGraphicsConfigurationgetBoundswidth;
          super.setSize(d);
        }
        
        public boolean getScrollableTracksViewportWidth() {
          return false;
        }
      };
    m_Output.setEditable(false);
    m_Output.addCaretListener(this);
    m_Output.setDocument(outputOld.getDocument());
    m_Output.setCaretPosition(m_Output.getDocument().getLength());
    


    parent.add(m_Output);
    parent.remove(outputOld);
  }
  


  public void caretUpdate(CaretEvent e)
  {
    Messages.getInstance();m_LabelCurrentSize.setText(Messages.getString("LogWindow_CaretUpdate_Text") + m_Output.getStyledDocument().getLength());
  }
  







  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource() == m_SpinnerMaxSize.getModel()) {
      trim();
      validate();
      caretUpdate(null);
    }
  }
  




  public static void main(String[] args)
  {
    LookAndFeel.setLookAndFeel();
    
    LogWindow log = new LogWindow();
    log.setVisible(true);
    log.setDefaultCloseOperation(2);
    

    Messages.getInstance();System.out.print(Messages.getString("LogWindow_Main_Text_First"));
    Messages.getInstance();System.err.print(Messages.getString("LogWindow_Main_Error_Text_First"));
    Messages.getInstance();System.out.print(Messages.getString("LogWindow_Main_Text_Second"));
    System.out.println();
    System.err.println(new Date());
  }
}
