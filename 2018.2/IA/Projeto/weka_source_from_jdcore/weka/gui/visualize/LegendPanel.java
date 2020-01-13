package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import weka.core.FastVector;
import weka.core.Instances;





























public class LegendPanel
  extends JScrollPane
{
  private static final long serialVersionUID = -1262384440543001505L;
  protected FastVector m_plots;
  protected JPanel m_span = null;
  


  protected FastVector m_Repainters = new FastVector();
  



  protected class LegendEntry
    extends JPanel
  {
    private static final long serialVersionUID = 3879990289042935670L;
    


    private PlotData2D m_plotData = null;
    

    private int m_dataIndex;
    

    private JLabel m_legendText;
    
    private JPanel m_pointShape;
    

    public LegendEntry(PlotData2D data, int dataIndex)
    {
      ToolTipManager.sharedInstance().setDismissDelay(5000);
      m_plotData = data;
      m_dataIndex = dataIndex;
      



      if (m_plotData.m_useCustomColour) {
        addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & 0x10) == 16) {
              Messages.getInstance();Color tmp = JColorChooser.showDialog(LegendPanel.this, Messages.getString("LegendPanel_Main_JColorChooserShowDialog_Text"), m_plotData.m_customColour);
              


              if (tmp != null) {
                m_plotData.m_customColour = tmp;
                m_legendText.setForeground(tmp);
                
                if (m_Repainters.size() > 0) {
                  for (int i = 0; i < m_Repainters.size(); i++) {
                    ((Component)m_Repainters.elementAt(i)).repaint();
                  }
                }
                repaint();
              }
            }
          }
        });
      }
      
      m_legendText = new JLabel(m_plotData.m_plotName);
      m_legendText.setToolTipText(m_plotData.getPlotNameHTML());
      if (m_plotData.m_useCustomColour) {
        m_legendText.setForeground(m_plotData.m_customColour);
      }
      setLayout(new BorderLayout());
      add(m_legendText, "Center");
      



      m_pointShape = new JPanel() {
        private static final long serialVersionUID = -7048435221580488238L;
        
        public void paintComponent(Graphics gx) {
          super.paintComponent(gx);
          if (!m_plotData.m_useCustomColour) {
            gx.setColor(Color.black);
          } else {
            gx.setColor(m_plotData.m_customColour);
          }
          Plot2D.drawDataPoint(10.0D, 10.0D, 3, m_dataIndex, gx);
        }
        
      };
      m_pointShape.setPreferredSize(new Dimension(20, 20));
      m_pointShape.setMinimumSize(new Dimension(20, 20));
      add(m_pointShape, "West");
    }
  }
  


  public LegendPanel()
  {
    setBackground(Color.blue);
    setVerticalScrollBarPolicy(22);
  }
  



  public void setPlotList(FastVector pl)
  {
    m_plots = pl;
    updateLegends();
  }
  




  public void addRepaintNotify(Component c)
  {
    m_Repainters.addElement(c);
  }
  


  private void updateLegends()
  {
    if (m_span == null) {
      m_span = new JPanel();
    }
    
    JPanel padder = new JPanel();
    JPanel padd2 = new JPanel();
    
    m_span.setPreferredSize(new Dimension(m_span.getPreferredSize().width, (m_plots.size() + 1) * 20));
    
    m_span.setMaximumSize(new Dimension(m_span.getPreferredSize().width, (m_plots.size() + 1) * 20));
    



    GridBagLayout gb = new GridBagLayout();
    GridBagLayout gb2 = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    
    m_span.removeAll();
    
    padder.setLayout(gb);
    m_span.setLayout(gb2);
    anchor = 10;
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 0, 0, 0);
    padder.add(m_span, constraints);
    
    gridx = 0;gridy = 1;weightx = 5.0D;
    fill = 1;
    gridwidth = 1;gridheight = 1;weighty = 5.0D;
    insets = new Insets(0, 0, 0, 0);
    padder.add(padd2, constraints);
    
    weighty = 0.0D;
    setViewportView(padder);
    
    anchor = 10;
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;weighty = 5.0D;
    insets = new Insets(2, 4, 2, 4);
    
    for (int i = 0; i < m_plots.size(); i++) {
      LegendEntry tmp = new LegendEntry((PlotData2D)m_plots.elementAt(i), i);
      gridy = i;
      



      m_span.add(tmp, constraints);
    }
  }
  


  public static void main(String[] args)
  {
    try
    {
      if (args.length < 1) {
        Messages.getInstance();System.err.println(Messages.getString("LegendPanel_Main_Error_Text_First"));
        System.exit(1);
      }
      
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("LegendPanel_Main_JFrame_Text"));
      
      jf.setSize(100, 100);
      jf.getContentPane().setLayout(new BorderLayout());
      LegendPanel p2 = new LegendPanel();
      jf.getContentPane().add(p2, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      FastVector plotList = new FastVector();
      for (int j = 0; j < args.length; j++) {
        Messages.getInstance();System.err.println(Messages.getString("LegendPanel_Main_Error_Text_Second") + args[j]);
        Reader r = new BufferedReader(new FileReader(args[j]));
        
        Instances i = new Instances(r);
        PlotData2D tmp = new PlotData2D(i);
        if (j != 1) {
          m_useCustomColour = true;
          m_customColour = Color.red;
        }
        tmp.setPlotName(i.relationName());
        plotList.addElement(tmp);
      }
      
      p2.setPlotList(plotList);
      jf.setVisible(true);
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
      ex.printStackTrace();
    }
  }
}
