package weka.gui.visualize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.logging.Logger.Level;
import weka.gui.ExtensionFileFilter;















































public class VisualizePanel
  extends PrintablePanel
{
  private static final long serialVersionUID = 240108358588153943L;
  
  protected class PlotPanel
    extends PrintablePanel
    implements Plot2DCompanion
  {
    private static final long serialVersionUID = -4823674171136494204L;
    protected Plot2D m_plot2D = new Plot2D();
    

    protected Instances m_plotInstances = null;
    

    protected PlotData2D m_originalPlot = null;
    


    protected int m_xIndex = 0;
    protected int m_yIndex = 0;
    protected int m_cIndex = 0;
    protected int m_sIndex = 0;
    

    private int m_XaxisStart = 0;
    private int m_YaxisStart = 0;
    private int m_XaxisEnd = 0;
    private int m_YaxisEnd = 0;
    

    private boolean m_createShape;
    

    private FastVector m_shapes;
    

    private FastVector m_shapePoints;
    
    private Dimension m_newMousePos;
    

    public PlotPanel()
    {
      setBackground(m_plot2D.getBackground());
      setLayout(new BorderLayout());
      add(m_plot2D, "Center");
      m_plot2D.setPlotCompanion(this);
      
      m_createShape = false;
      m_shapes = null;
      m_shapePoints = null;
      m_newMousePos = new Dimension();
      
      addMouseListener(new MouseAdapter()
      {
        public void mousePressed(MouseEvent e) {
          if ((e.getModifiers() & 0x10) == 16)
          {
            if (m_sIndex != 0)
            {

              if (m_sIndex == 1) {
                m_createShape = true;
                m_shapePoints = new FastVector(5);
                m_shapePoints.addElement(new Double(m_sIndex));
                m_shapePoints.addElement(new Double(e.getX()));
                m_shapePoints.addElement(new Double(e.getY()));
                m_shapePoints.addElement(new Double(e.getX()));
                m_shapePoints.addElement(new Double(e.getY()));
                
                Graphics g = m_plot2D.getGraphics();
                g.setColor(Color.black);
                g.setXORMode(Color.white);
                g.drawRect(((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(2)).intValue(), ((Double)m_shapePoints.elementAt(3)).intValue() - ((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(4)).intValue() - ((Double)m_shapePoints.elementAt(2)).intValue());
                




                g.dispose();
              }
            }
          }
        }
        

        public void mouseClicked(MouseEvent e)
        {
          if (((m_sIndex == 2) || (m_sIndex == 3)) && ((m_createShape) || ((e.getModifiers() & 0x10) == 16)))
          {

            if (m_createShape)
            {

              Graphics g = m_plot2D.getGraphics();
              g.setColor(Color.black);
              g.setXORMode(Color.white);
              if (((e.getModifiers() & 0x10) == 16) && (!e.isAltDown()))
              {
                m_shapePoints.addElement(new Double(m_plot2D.convertToAttribX(e.getX())));
                

                m_shapePoints.addElement(new Double(m_plot2D.convertToAttribY(e.getY())));
                

                m_newMousePos.width = e.getX();
                m_newMousePos.height = e.getY();
                g.drawLine((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())), (int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())), m_newMousePos.width, m_newMousePos.height);












              }
              else if (m_sIndex == 3)
              {




                m_createShape = false;
                if (m_shapePoints.size() >= 5) {
                  double cx = Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 4)).doubleValue()));
                  



                  double cx2 = Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())) - cx;
                  




                  cx2 *= 50000.0D;
                  
                  double cy = Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 3)).doubleValue()));
                  



                  double cy2 = Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())) - cy;
                  


                  cy2 *= 50000.0D;
                  

                  double cxa = Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(3)).doubleValue()));
                  


                  double cxa2 = Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(1)).doubleValue())) - cxa;
                  


                  cxa2 *= 50000.0D;
                  

                  double cya = Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(4)).doubleValue()));
                  


                  double cya2 = Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(2)).doubleValue())) - cya;
                  



                  cya2 *= 50000.0D;
                  
                  m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribX(cxa2 + cxa)), 1);
                  

                  m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribY(cy2 + cy)), m_shapePoints.size() - 1);
                  


                  m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribX(cx2 + cx)), m_shapePoints.size() - 2);
                  


                  m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribY(cya2 + cya)), 2);
                  




                  cy = Double.POSITIVE_INFINITY;
                  cy2 = Double.NEGATIVE_INFINITY;
                  if (((Double)m_shapePoints.elementAt(1)).doubleValue() > ((Double)m_shapePoints.elementAt(3)).doubleValue())
                  {


                    if (((Double)m_shapePoints.elementAt(2)).doubleValue() == ((Double)m_shapePoints.elementAt(4)).doubleValue())
                    {


                      cy = ((Double)m_shapePoints.elementAt(2)).doubleValue();
                    }
                  }
                  
                  if (((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue() > ((Double)m_shapePoints.elementAt(m_shapePoints.size() - 4)).doubleValue())
                  {


                    if (((Double)m_shapePoints.elementAt(m_shapePoints.size() - 3)).doubleValue() == ((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())
                    {



                      cy2 = ((Double)m_shapePoints.lastElement()).doubleValue();
                    }
                  }
                  
                  m_shapePoints.addElement(new Double(cy));
                  m_shapePoints.addElement(new Double(cy2));
                  
                  if (!VisualizePanel.PlotPanel.this.inPolyline(m_shapePoints, m_plot2D.convertToAttribX(e.getX()), m_plot2D.convertToAttribY(e.getY())))
                  {

                    Double tmp = (Double)m_shapePoints.elementAt(m_shapePoints.size() - 2);
                    
                    m_shapePoints.setElementAt(m_shapePoints.lastElement(), m_shapePoints.size() - 2);
                    

                    m_shapePoints.setElementAt(tmp, m_shapePoints.size() - 1);
                  }
                  

                  if (m_shapes == null) {
                    m_shapes = new FastVector(4);
                  }
                  m_shapes.addElement(m_shapePoints);
                  
                  Messages.getInstance();m_submit.setText(Messages.getString("VisualizePanel_PlotPanel_MouseClicked_Submit_SetText_Text_First"));
                  Messages.getInstance();m_submit.setActionCommand(Messages.getString("VisualizePanel_PlotPanel_MouseClicked_Submit_SetActionCommand_Text_First"));
                  
                  m_submit.setEnabled(true);
                }
                
                m_shapePoints = null;
                repaint();

              }
              else
              {
                m_createShape = false;
                if (m_shapePoints.size() >= 7) {
                  m_shapePoints.addElement(m_shapePoints.elementAt(1));
                  m_shapePoints.addElement(m_shapePoints.elementAt(2));
                  if (m_shapes == null) {
                    m_shapes = new FastVector(4);
                  }
                  m_shapes.addElement(m_shapePoints);
                  
                  Messages.getInstance();m_submit.setText(Messages.getString("VisualizePanel_PlotPanel_MouseClicked_Submit_SetText_Text_Second"));
                  Messages.getInstance();m_submit.setActionCommand(Messages.getString("VisualizePanel_PlotPanel_MouseClicked_Submit_SetActionCommand_Text_Second"));
                  
                  m_submit.setEnabled(true);
                }
                m_shapePoints = null;
                repaint();
              }
              g.dispose();

            }
            else if ((e.getModifiers() & 0x10) == 16)
            {
              m_createShape = true;
              m_shapePoints = new FastVector(17);
              m_shapePoints.addElement(new Double(m_sIndex));
              m_shapePoints.addElement(new Double(m_plot2D.convertToAttribX(e.getX())));
              
              m_shapePoints.addElement(new Double(m_plot2D.convertToAttribY(e.getY())));
              
              m_newMousePos.width = e.getX();
              m_newMousePos.height = e.getY();
              
              Graphics g = m_plot2D.getGraphics();
              g.setColor(Color.black);
              g.setXORMode(Color.white);
              g.drawLine((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(1)).doubleValue())), (int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(2)).doubleValue())), m_newMousePos.width, m_newMousePos.height);
              





              g.dispose();
            }
            
          }
          else if ((e.getModifiers() & 0x10) == 16)
          {

            m_plot2D.searchPoints(e.getX(), e.getY(), false);
          } else {
            m_plot2D.searchPoints(e.getX(), e.getY(), true);
          }
        }
        


        public void mouseReleased(MouseEvent e)
        {
          if ((m_createShape) && 
            (((Double)m_shapePoints.elementAt(0)).intValue() == 1)) {
            m_createShape = false;
            Graphics g = m_plot2D.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            g.drawRect(((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(2)).intValue(), ((Double)m_shapePoints.elementAt(3)).intValue() - ((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(4)).intValue() - ((Double)m_shapePoints.elementAt(2)).intValue());
            






            g.dispose();
            if ((VisualizePanel.PlotPanel.this.checkPoints(((Double)m_shapePoints.elementAt(1)).doubleValue(), ((Double)m_shapePoints.elementAt(2)).doubleValue())) && (VisualizePanel.PlotPanel.this.checkPoints(((Double)m_shapePoints.elementAt(3)).doubleValue(), ((Double)m_shapePoints.elementAt(4)).doubleValue())))
            {








              if ((((Double)m_shapePoints.elementAt(1)).doubleValue() < ((Double)m_shapePoints.elementAt(3)).doubleValue()) && (((Double)m_shapePoints.elementAt(2)).doubleValue() < ((Double)m_shapePoints.elementAt(4)).doubleValue()))
              {




                if (m_shapes == null) {
                  m_shapes = new FastVector(2);
                }
                m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribX(((Double)m_shapePoints.elementAt(1)).doubleValue())), 1);
                


                m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribY(((Double)m_shapePoints.elementAt(2)).doubleValue())), 2);
                


                m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribX(((Double)m_shapePoints.elementAt(3)).doubleValue())), 3);
                


                m_shapePoints.setElementAt(new Double(m_plot2D.convertToAttribY(((Double)m_shapePoints.elementAt(4)).doubleValue())), 4);
                



                m_shapes.addElement(m_shapePoints);
                
                Messages.getInstance();m_submit.setText(Messages.getString("VisualizePanel_PlotPanel_MouseReleased_Submit_SetText_Text"));
                Messages.getInstance();m_submit.setActionCommand(Messages.getString("VisualizePanel_PlotPanel_MouseReleased_Submit_SetActionCommand_Text"));
                
                m_submit.setEnabled(true);
                
                repaint();
              }
            }
            m_shapePoints = null;
          }
          
        }
        
      });
      addMouseMotionListener(new MouseMotionAdapter()
      {
        public void mouseDragged(MouseEvent e) {
          if ((m_createShape) && 
            (((Double)m_shapePoints.elementAt(0)).intValue() == 1)) {
            Graphics g = m_plot2D.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            g.drawRect(((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(2)).intValue(), ((Double)m_shapePoints.elementAt(3)).intValue() - ((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(4)).intValue() - ((Double)m_shapePoints.elementAt(2)).intValue());
            





            m_shapePoints.setElementAt(new Double(e.getX()), 3);
            m_shapePoints.setElementAt(new Double(e.getY()), 4);
            
            g.drawRect(((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(2)).intValue(), ((Double)m_shapePoints.elementAt(3)).intValue() - ((Double)m_shapePoints.elementAt(1)).intValue(), ((Double)m_shapePoints.elementAt(4)).intValue() - ((Double)m_shapePoints.elementAt(2)).intValue());
            




            g.dispose();
          }
        }
        
        public void mouseMoved(MouseEvent e)
        {
          if ((m_createShape) && (
            (((Double)m_shapePoints.elementAt(0)).intValue() == 2) || (((Double)m_shapePoints.elementAt(0)).intValue() == 3)))
          {
            Graphics g = m_plot2D.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            g.drawLine((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())), (int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())), m_newMousePos.width, m_newMousePos.height);
            








            m_newMousePos.width = e.getX();
            m_newMousePos.height = e.getY();
            
            g.drawLine((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())), (int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())), m_newMousePos.width, m_newMousePos.height);
            







            g.dispose();
          }
          
        }
        
      });
      m_submit.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("VisualizePanel_PlotPanel_ActionPerformed_Submit_ActionCommand_Text"))) {
            if ((m_splitListener != null) && (m_shapes != null))
            {
              Instances sub_set1 = new Instances(m_plot2D.getMasterPlot().m_plotInstances, 500);
              
              Instances sub_set2 = new Instances(m_plot2D.getMasterPlot().m_plotInstances, 500);
              

              if (m_plot2D.getMasterPlot().m_plotInstances != null)
              {

                for (int noa = 0; noa < m_plot2D.getMasterPlot().m_plotInstances.numInstances(); 
                    noa++) {
                  if ((!m_plot2D.getMasterPlot().m_plotInstances.instance(noa).isMissing(m_xIndex)) && (!m_plot2D.getMasterPlot().m_plotInstances.instance(noa).isMissing(m_yIndex)))
                  {



                    if (inSplit(m_plot2D.getMasterPlot().m_plotInstances.instance(noa)))
                    {
                      sub_set1.add(m_plot2D.getMasterPlot().m_plotInstances.instance(noa));
                    }
                    else
                    {
                      sub_set2.add(m_plot2D.getMasterPlot().m_plotInstances.instance(noa));
                    }
                  }
                }
                
                FastVector tmp = m_shapes;
                cancelShapes();
                m_splitListener.userDataEvent(new VisualizePanelEvent(tmp, sub_set1, sub_set2, m_xIndex, m_yIndex));
              }
              

            }
            else if ((m_shapes != null) && (m_plot2D.getMasterPlot().m_plotInstances != null))
            {
              Instances sub_set1 = new Instances(m_plot2D.getMasterPlot().m_plotInstances, 500);
              
              int count = 0;
              for (int noa = 0; noa < m_plot2D.getMasterPlot().m_plotInstances.numInstances(); 
                  noa++) {
                if (inSplit(m_plot2D.getMasterPlot().m_plotInstances.instance(noa)))
                {
                  sub_set1.add(m_plot2D.getMasterPlot().m_plotInstances.instance(noa));
                  
                  count++;
                }
              }
              

              int[] nSizes = null;
              int[] nTypes = null;
              int x = m_xIndex;
              int y = m_yIndex;
              
              if (m_originalPlot == null)
              {

                m_originalPlot = m_plot2D.getMasterPlot();
              }
              
              if (count > 0) {
                nTypes = new int[count];
                nSizes = new int[count];
                count = 0;
                for (int noa = 0; noa < m_plot2D.getMasterPlot().m_plotInstances.numInstances(); 
                    
                    noa++) {
                  if (inSplit(m_plot2D.getMasterPlot().m_plotInstances.instance(noa)))
                  {

                    nTypes[count] = m_plot2D.getMasterPlot().m_shapeType[noa];
                    
                    nSizes[count] = m_plot2D.getMasterPlot().m_shapeSize[noa];
                    
                    count++;
                  }
                }
              }
              cancelShapes();
              
              PlotData2D newPlot = new PlotData2D(sub_set1);
              try
              {
                newPlot.setShapeSize(nSizes);
                newPlot.setShapeType(nTypes);
                
                m_plot2D.removeAllPlots();
                
                VisualizePanel.this.addPlot(newPlot);
              } catch (Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
              }
              try
              {
                setXIndex(x);
                setYIndex(y);
              } catch (Exception er) {
                Messages.getInstance();System.out.println(Messages.getString("VisualizePanel_PlotPanel_ActionPerformed_Error_Text_First") + er);
              }
            }
          }
          else
          {
            Messages.getInstance(); if (e.getActionCommand().equals(Messages.getString("VisualizePanel_PlotPanel_ActionPerformed_Reset_ActionCommand_Text"))) {
              int x = m_xIndex;
              int y = m_yIndex;
              
              m_plot2D.removeAllPlots();
              try {
                VisualizePanel.this.addPlot(m_originalPlot);
              } catch (Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
              }
              try
              {
                setXIndex(x);
                setYIndex(y);
              } catch (Exception er) {
                Messages.getInstance();System.out.println(Messages.getString("VisualizePanel_PlotPanel_ActionPerformed_Error_Text_Second") + er);
              }
            }
          }
        }
      });
      m_cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancelShapes();
          repaint();
        }
      });
    }
    



    public void removeAllPlots()
    {
      m_plot2D.removeAllPlots();
      m_legendPanel.setPlotList(m_plot2D.getPlots());
    }
    



    public FastVector getShapes()
    {
      return m_shapes;
    }
    




    public void cancelShapes()
    {
      if (m_splitListener == null) {
        Messages.getInstance();m_submit.setText(Messages.getString("VisualizePanel_PlotPanel_CancelShapes_Submit_SetText_Text"));
        Messages.getInstance();m_submit.setActionCommand(Messages.getString("VisualizePanel_PlotPanel_CancelShapes_Submit_SetActionCommand_Text"));
        
        if ((m_originalPlot == null) || (m_originalPlot.m_plotInstances == m_plotInstances))
        {
          m_submit.setEnabled(false);
        }
        else {
          m_submit.setEnabled(true);
        }
      }
      else {
        m_submit.setEnabled(false);
      }
      
      m_createShape = false;
      m_shapePoints = null;
      m_shapes = null;
      repaint();
    }
    






    public void setShapes(FastVector v)
    {
      if (v != null)
      {
        m_shapes = new FastVector(v.size());
        for (int noa = 0; noa < v.size(); noa++) {
          FastVector temp = new FastVector(((FastVector)v.elementAt(noa)).size());
          m_shapes.addElement(temp);
          for (int nob = 0; nob < ((FastVector)v.elementAt(noa)).size(); 
              nob++)
          {
            temp.addElement(((FastVector)v.elementAt(noa)).elementAt(nob));
          }
        }
      }
      else
      {
        m_shapes = null;
      }
      repaint();
    }
    






    private boolean checkPoints(double x1, double y1)
    {
      if ((x1 < 0.0D) || (x1 > getSizewidth) || (y1 < 0.0D) || (y1 > getSizeheight))
      {
        return false;
      }
      return true;
    }
    







    public boolean inSplit(Instance i)
    {
      if (m_shapes != null)
      {

        for (int noa = 0; noa < m_shapes.size(); noa++) {
          FastVector stmp = (FastVector)m_shapes.elementAt(noa);
          if (((Double)stmp.elementAt(0)).intValue() == 1)
          {
            double x1 = ((Double)stmp.elementAt(1)).doubleValue();
            double y1 = ((Double)stmp.elementAt(2)).doubleValue();
            double x2 = ((Double)stmp.elementAt(3)).doubleValue();
            double y2 = ((Double)stmp.elementAt(4)).doubleValue();
            if ((i.value(m_xIndex) >= x1) && (i.value(m_xIndex) <= x2) && (i.value(m_yIndex) <= y1) && (i.value(m_yIndex) >= y2))
            {

              return true;
            }
          }
          else if (((Double)stmp.elementAt(0)).intValue() == 2)
          {
            if (inPoly(stmp, i.value(m_xIndex), i.value(m_yIndex))) {
              return true;
            }
          }
          else if (((Double)stmp.elementAt(0)).intValue() == 3)
          {
            if (inPolyline(stmp, i.value(m_xIndex), i.value(m_yIndex))) {
              return true;
            }
          }
        }
      }
      return false;
    }
    















    private boolean inPolyline(FastVector ob, double x, double y)
    {
      int countx = 0;
      



      for (int noa = 1; noa < ob.size() - 4; noa += 2) {
        double y1 = ((Double)ob.elementAt(noa + 1)).doubleValue();
        double y2 = ((Double)ob.elementAt(noa + 3)).doubleValue();
        double x1 = ((Double)ob.elementAt(noa)).doubleValue();
        double x2 = ((Double)ob.elementAt(noa + 2)).doubleValue();
        

        double vecy = y2 - y1;
        double vecx = x2 - x1;
        if ((noa == 1) && (noa == ob.size() - 6))
        {
          if (vecy != 0.0D) {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
        else if (noa == 1) {
          if (((y < y2) && (vecy > 0.0D)) || ((y > y2) && (vecy < 0.0D)))
          {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
        else if (noa == ob.size() - 6)
        {
          if (((y <= y1) && (vecy < 0.0D)) || ((y >= y1) && (vecy > 0.0D))) {
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x) {
              countx++;
            }
          }
        }
        else if (((y1 <= y) && (y < y2)) || ((y2 < y) && (y <= y1)))
        {
          if (vecy != 0.0D)
          {



            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              countx++;
            }
          }
        }
      }
      

      double y1 = ((Double)ob.elementAt(ob.size() - 2)).doubleValue();
      double y2 = ((Double)ob.elementAt(ob.size() - 1)).doubleValue();
      
      if (y1 > y2)
      {
        if ((y1 >= y) && (y > y2)) {
          countx++;
        }
        

      }
      else if ((y1 >= y) || (y > y2)) {
        countx++;
      }
      

      if (countx % 2 == 1) {
        return true;
      }
      
      return false;
    }
    














    private boolean inPoly(FastVector ob, double x, double y)
    {
      int count = 0;
      


      for (int noa = 1; noa < ob.size() - 2; noa += 2) {
        double y1 = ((Double)ob.elementAt(noa + 1)).doubleValue();
        double y2 = ((Double)ob.elementAt(noa + 3)).doubleValue();
        if (((y1 <= y) && (y < y2)) || ((y2 < y) && (y <= y1)))
        {
          double vecy = y2 - y1;
          if (vecy != 0.0D)
          {


            double x1 = ((Double)ob.elementAt(noa)).doubleValue();
            double x2 = ((Double)ob.elementAt(noa + 2)).doubleValue();
            double vecx = x2 - x1;
            double change = (y - y1) / vecy;
            if (vecx * change + x1 >= x)
            {
              count++;
            }
          }
        }
      }
      if (count % 2 == 1)
      {

        return true;
      }
      

      return false;
    }
    






    public void setJitter(int j)
    {
      m_plot2D.setJitter(j);
    }
    






    public void setXindex(int x)
    {
      if (x != m_xIndex) {
        cancelShapes();
      }
      m_xIndex = x;
      m_plot2D.setXindex(x);
      if (m_showAttBars) {
        m_attrib.setX(x);
      }
    }
    







    public void setYindex(int y)
    {
      if (y != m_yIndex) {
        cancelShapes();
      }
      m_yIndex = y;
      m_plot2D.setYindex(y);
      if (m_showAttBars) {
        m_attrib.setY(y);
      }
    }
    




    public void setCindex(int c)
    {
      m_cIndex = c;
      m_plot2D.setCindex(c);
      if (m_showAttBars) {
        m_attrib.setCindex(c, m_plot2D.getMaxC(), m_plot2D.getMinC());
      }
      m_classPanel.setCindex(c);
      repaint();
    }
    



    public void setSindex(int s)
    {
      if (s != m_sIndex) {
        m_shapePoints = null;
        m_createShape = false;
      }
      m_sIndex = s;
      repaint();
    }
    



    public void setMasterPlot(PlotData2D newPlot)
      throws Exception
    {
      m_plot2D.removeAllPlots();
      addPlot(newPlot);
    }
    





    public void addPlot(PlotData2D newPlot)
      throws Exception
    {
      if (m_plot2D.getPlots().size() == 0) {
        m_plot2D.addPlot(newPlot);
        if ((m_plotSurround.getComponentCount() > 1) && (m_plotSurround.getComponent(1) == m_attrib) && (m_showAttBars))
        {
          try
          {
            m_attrib.setInstances(m_plotInstances);
            m_attrib.setCindex(0);m_attrib.setX(0);m_attrib.setY(0);
          }
          catch (Exception ex)
          {
            m_plotSurround.remove(m_attrib);
            Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_PlotPanel_Error_Text_First"));
            if (m_Log != null) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("VisualizePanel_PlotPanel_Log_LogMessage_Text_First"));
            }
          }
        } else if (m_showAttBars) {
          try {
            m_attrib.setInstances(m_plotInstances);
            m_attrib.setCindex(0);m_attrib.setX(0);m_attrib.setY(0);
            GridBagConstraints constraints = new GridBagConstraints();
            fill = 1;
            insets = new Insets(0, 0, 0, 0);
            gridx = 4;gridy = 0;weightx = 1.0D;
            gridwidth = 1;gridheight = 1;
            weighty = 5.0D;
            m_plotSurround.add(m_attrib, constraints);
          } catch (Exception ex) {
            Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_PlotPanel_Error_Text_Second"));
            if (m_Log != null) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("VisualizePanel_PlotPanel_Log_LogMessage_Text_Second"));
            }
          }
        }
        m_classPanel.setInstances(m_plotInstances);
        
        plotReset(m_plotInstances, newPlot.getCindex());
        if ((m_useCustomColour) && (m_showClassPanel)) {
          remove(m_classSurround);
          switchToLegend();
          m_legendPanel.setPlotList(m_plot2D.getPlots());
          m_ColourCombo.setEnabled(false);
        }
      } else {
        if ((!m_useCustomColour) && (m_showClassPanel)) {
          add(m_classSurround, "South");
          m_ColourCombo.setEnabled(true);
        }
        if (m_plot2D.getPlots().size() == 1) {
          switchToLegend();
        }
        m_plot2D.addPlot(newPlot);
        m_legendPanel.setPlotList(m_plot2D.getPlots());
      }
    }
    



    protected void switchToLegend()
    {
      if ((m_plotSurround.getComponentCount() > 1) && (m_plotSurround.getComponent(1) == m_attrib))
      {
        m_plotSurround.remove(m_attrib);
      }
      
      if ((m_plotSurround.getComponentCount() > 1) && (m_plotSurround.getComponent(1) == m_legendPanel))
      {
        return;
      }
      
      GridBagConstraints constraints = new GridBagConstraints();
      fill = 1;
      insets = new Insets(0, 0, 0, 0);
      gridx = 4;gridy = 0;weightx = 1.0D;
      gridwidth = 1;gridheight = 1;
      weighty = 5.0D;
      m_plotSurround.add(m_legendPanel, constraints);
      setSindex(0);
      m_ShapeCombo.setEnabled(false);
    }
    
    protected void switchToBars() {
      if ((m_plotSurround.getComponentCount() > 1) && (m_plotSurround.getComponent(1) == m_legendPanel))
      {
        m_plotSurround.remove(m_legendPanel);
      }
      
      if ((m_plotSurround.getComponentCount() > 1) && (m_plotSurround.getComponent(1) == m_attrib))
      {
        return;
      }
      
      if (m_showAttBars) {
        try {
          m_attrib.setInstances(m_plot2D.getMasterPlot().m_plotInstances);
          m_attrib.setCindex(0);m_attrib.setX(0);m_attrib.setY(0);
          GridBagConstraints constraints = new GridBagConstraints();
          fill = 1;
          insets = new Insets(0, 0, 0, 0);
          gridx = 4;gridy = 0;weightx = 1.0D;
          gridwidth = 1;gridheight = 1;
          weighty = 5.0D;
          m_plotSurround.add(m_attrib, constraints);
        } catch (Exception ex) {
          Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_PlotPanel_SwitchToBars_Error_Text"));
          if (m_Log != null) {
            Messages.getInstance();m_Log.logMessage(Messages.getString("VisualizePanel_PlotPanel_SwitchToBars_Log_LogMessage_Text"));
          }
        }
      }
    }
    





    private void plotReset(Instances inst, int cIndex)
    {
      if (m_splitListener == null) {
        Messages.getInstance();m_submit.setText(Messages.getString("VisualizePanel_PlotPanel_PlotReset_Submit_SetText_Text"));
        Messages.getInstance();m_submit.setActionCommand(Messages.getString("VisualizePanel_PlotPanel_PlotReset_Submit_SetActionCommand_Text"));
        
        if ((m_originalPlot == null) || (m_originalPlot.m_plotInstances == inst)) {
          m_submit.setEnabled(false);
        }
        else {
          m_submit.setEnabled(true);
        }
      }
      else {
        m_submit.setEnabled(false);
      }
      
      m_plotInstances = inst;
      if (m_splitListener != null) {
        m_plotInstances.randomize(new Random());
      }
      m_xIndex = 0;
      m_yIndex = 0;
      m_cIndex = cIndex;
      cancelShapes();
    }
    



    public void setColours(FastVector cols)
    {
      m_plot2D.setColours(cols);
      m_colorList = cols;
    }
    







    private void drawShapes(Graphics gx)
    {
      if (m_shapes != null)
      {

        for (int noa = 0; noa < m_shapes.size(); noa++) {
          FastVector stmp = (FastVector)m_shapes.elementAt(noa);
          if (((Double)stmp.elementAt(0)).intValue() == 1)
          {
            int x1 = (int)m_plot2D.convertToPanelX(((Double)stmp.elementAt(1)).doubleValue());
            
            int y1 = (int)m_plot2D.convertToPanelY(((Double)stmp.elementAt(2)).doubleValue());
            
            int x2 = (int)m_plot2D.convertToPanelX(((Double)stmp.elementAt(3)).doubleValue());
            
            int y2 = (int)m_plot2D.convertToPanelY(((Double)stmp.elementAt(4)).doubleValue());
            

            gx.setColor(Color.gray);
            gx.fillRect(x1, y1, x2 - x1, y2 - y1);
            gx.setColor(Color.black);
            gx.drawRect(x1, y1, x2 - x1, y2 - y1);

          }
          else if (((Double)stmp.elementAt(0)).intValue() == 2)
          {

            int[] ar1 = getXCoords(stmp);
            int[] ar2 = getYCoords(stmp);
            gx.setColor(Color.gray);
            gx.fillPolygon(ar1, ar2, (stmp.size() - 1) / 2);
            gx.setColor(Color.black);
            gx.drawPolyline(ar1, ar2, (stmp.size() - 1) / 2);
          }
          else if (((Double)stmp.elementAt(0)).intValue() == 3)
          {

            FastVector tmp = makePolygon(stmp);
            int[] ar1 = getXCoords(tmp);
            int[] ar2 = getYCoords(tmp);
            
            gx.setColor(Color.gray);
            gx.fillPolygon(ar1, ar2, (tmp.size() - 1) / 2);
            gx.setColor(Color.black);
            gx.drawPolyline(ar1, ar2, (tmp.size() - 1) / 2);
          }
        }
      }
      
      if (m_shapePoints != null)
      {
        if ((((Double)m_shapePoints.elementAt(0)).intValue() == 2) || (((Double)m_shapePoints.elementAt(0)).intValue() == 3))
        {
          gx.setColor(Color.black);
          gx.setXORMode(Color.white);
          
          int[] ar1 = getXCoords(m_shapePoints);
          int[] ar2 = getYCoords(m_shapePoints);
          gx.drawPolyline(ar1, ar2, (m_shapePoints.size() - 1) / 2);
          m_newMousePos.width = ((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())));
          


          m_newMousePos.height = ((int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())));
          


          gx.drawLine((int)Math.ceil(m_plot2D.convertToPanelX(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 2)).doubleValue())), (int)Math.ceil(m_plot2D.convertToPanelY(((Double)m_shapePoints.elementAt(m_shapePoints.size() - 1)).doubleValue())), m_newMousePos.width, m_newMousePos.height);
          







          gx.setPaintMode();
        }
      }
    }
    




















    private double[] lineIntersect(double x1, double y1, double x2, double y2, double x, double y, double offset)
    {
      double xn = -100.0D;double yn = -100.0D;
      
      if (x == 0.0D) {
        if (((x1 <= offset) && (offset < x2)) || ((x1 >= offset) && (offset > x2)))
        {
          double xval = x1 - x2;
          double change = (offset - x2) / xval;
          yn = (y1 - y2) * change + y2;
          if ((0.0D <= yn) && (yn <= y))
          {
            xn = offset;
          }
          else
          {
            xn = -100.0D;
          }
        }
      }
      else if ((y == 0.0D) && (
        ((y1 <= offset) && (offset < y2)) || ((y1 >= offset) && (offset > y2))))
      {
        double yval = y1 - y2;
        double change = (offset - y2) / yval;
        xn = (x1 - x2) * change + x2;
        if ((0.0D <= xn) && (xn <= x))
        {
          yn = offset;
        }
        else {
          xn = -100.0D;
        }
      }
      
      double[] ret = new double[2];
      ret[0] = xn;
      ret[1] = yn;
      return ret;
    }
    






    private FastVector makePolygon(FastVector v)
    {
      FastVector building = new FastVector(v.size() + 10);
      
      int edge1 = 0;int edge2 = 0;
      for (int noa = 0; noa < v.size() - 2; noa++) {
        building.addElement(new Double(((Double)v.elementAt(noa)).doubleValue()));
      }
      






      double x1 = m_plot2D.convertToPanelX(((Double)v.elementAt(1)).doubleValue());
      double y1 = m_plot2D.convertToPanelY(((Double)v.elementAt(2)).doubleValue());
      double x2 = m_plot2D.convertToPanelX(((Double)v.elementAt(3)).doubleValue());
      double y2 = m_plot2D.convertToPanelY(((Double)v.elementAt(4)).doubleValue());
      double[] new_coords;
      if (x1 < 0.0D)
      {
        double[] new_coords = lineIntersect(x1, y1, x2, y2, 0.0D, getHeight(), 0.0D);
        edge1 = 0;
        if (new_coords[0] < 0.0D)
        {
          if (y1 < 0.0D)
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
            edge1 = 1;
          }
          else
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
            
            edge1 = 3;
          }
        }
      }
      else if (x1 > getWidth())
      {
        double[] new_coords = lineIntersect(x1, y1, x2, y2, 0.0D, getHeight(), getWidth());
        
        edge1 = 2;
        if (new_coords[0] < 0.0D)
        {
          if (y1 < 0.0D)
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
            edge1 = 1;
          }
          else
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
            
            edge1 = 3;
          }
        }
      }
      else if (y1 < 0.0D)
      {
        double[] new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
        edge1 = 1;
      }
      else
      {
        new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
        
        edge1 = 3;
      }
      
      building.setElementAt(new Double(m_plot2D.convertToAttribX(new_coords[0])), 1);
      
      building.setElementAt(new Double(m_plot2D.convertToAttribY(new_coords[1])), 2);
      

      x1 = m_plot2D.convertToPanelX(((Double)v.elementAt(v.size() - 4)).doubleValue());
      
      y1 = m_plot2D.convertToPanelY(((Double)v.elementAt(v.size() - 3)).doubleValue());
      
      x2 = m_plot2D.convertToPanelX(((Double)v.elementAt(v.size() - 6)).doubleValue());
      
      y2 = m_plot2D.convertToPanelY(((Double)v.elementAt(v.size() - 5)).doubleValue());
      

      if (x1 < 0.0D)
      {
        new_coords = lineIntersect(x1, y1, x2, y2, 0.0D, getHeight(), 0.0D);
        edge2 = 0;
        if (new_coords[0] < 0.0D)
        {
          if (y1 < 0.0D)
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
            edge2 = 1;
          }
          else
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
            
            edge2 = 3;
          }
        }
      }
      else if (x1 > getWidth())
      {
        new_coords = lineIntersect(x1, y1, x2, y2, 0.0D, getHeight(), getWidth());
        
        edge2 = 2;
        if (new_coords[0] < 0.0D)
        {
          if (y1 < 0.0D)
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
            edge2 = 1;
          }
          else
          {
            new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
            
            edge2 = 3;
          }
        }
      }
      else if (y1 < 0.0D)
      {
        new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, 0.0D);
        edge2 = 1;
      }
      else
      {
        new_coords = lineIntersect(x1, y1, x2, y2, getWidth(), 0.0D, getHeight());
        
        edge2 = 3;
      }
      
      building.setElementAt(new Double(m_plot2D.convertToAttribX(new_coords[0])), building.size() - 2);
      
      building.setElementAt(new Double(m_plot2D.convertToAttribY(new_coords[1])), building.size() - 1);
      






      int xp = getWidth() * (edge2 & 0x1 ^ (edge2 & 0x2) / 2);
      int yp = getHeight() * ((edge2 & 0x2) / 2);
      

      if (inPolyline(v, m_plot2D.convertToAttribX(xp), m_plot2D.convertToAttribY(yp)))
      {

        building.addElement(new Double(m_plot2D.convertToAttribX(xp)));
        building.addElement(new Double(m_plot2D.convertToAttribY(yp)));
        for (int noa = (edge2 + 1) % 4; noa != edge1; noa = (noa + 1) % 4) {
          xp = getWidth() * (noa & 0x1 ^ (noa & 0x2) / 2);
          yp = getHeight() * ((noa & 0x2) / 2);
          building.addElement(new Double(m_plot2D.convertToAttribX(xp)));
          building.addElement(new Double(m_plot2D.convertToAttribY(yp)));
        }
      }
      else {
        xp = getWidth() * ((edge2 & 0x2) / 2);
        yp = getHeight() * (0x1 & (edge2 & 0x1 ^ (edge2 & 0x2) / 2 ^ 0xFFFFFFFF));
        if (inPolyline(v, m_plot2D.convertToAttribX(xp), m_plot2D.convertToAttribY(yp)))
        {

          building.addElement(new Double(m_plot2D.convertToAttribX(xp)));
          building.addElement(new Double(m_plot2D.convertToAttribY(yp)));
          for (int noa = (edge2 + 3) % 4; noa != edge1; noa = (noa + 3) % 4) {
            xp = getWidth() * ((noa & 0x2) / 2);
            yp = getHeight() * (0x1 & (noa & 0x1 ^ (noa & 0x2) / 2 ^ 0xFFFFFFFF));
            building.addElement(new Double(m_plot2D.convertToAttribX(xp)));
            building.addElement(new Double(m_plot2D.convertToAttribY(yp)));
          }
        }
      }
      return building;
    }
    





    private int[] getXCoords(FastVector v)
    {
      int cach = (v.size() - 1) / 2;
      int[] ar = new int[cach];
      for (int noa = 0; noa < cach; noa++) {
        ar[noa] = ((int)m_plot2D.convertToPanelX(((Double)v.elementAt(noa * 2 + 1)).doubleValue()));
      }
      
      return ar;
    }
    





    private int[] getYCoords(FastVector v)
    {
      int cach = (v.size() - 1) / 2;
      int[] ar = new int[cach];
      for (int noa = 0; noa < cach; noa++) {
        ar[noa] = ((int)m_plot2D.convertToPanelY(((Double)v.elementAt(noa * 2 + 2)).doubleValue()));
      }
      

      return ar;
    }
    



    public void prePlot(Graphics gx)
    {
      super.paintComponent(gx);
      if (m_plotInstances != null) {
        drawShapes(gx);
      }
    }
  }
  



  protected Color[] m_DefaultColors = { Color.blue, Color.red, Color.green, Color.cyan, Color.pink, new Color(255, 0, 255), Color.orange, new Color(255, 0, 0), new Color(0, 255, 0), Color.white };
  










  protected JComboBox m_XCombo = new JComboBox();
  

  protected JComboBox m_YCombo = new JComboBox();
  

  protected JComboBox m_ColourCombo = new JComboBox();
  


  protected JComboBox m_ShapeCombo = new JComboBox();
  


  protected JButton m_submit;
  


  protected JButton m_cancel;
  


  protected JButton m_openBut;
  


  protected JButton m_saveBut;
  


  private Dimension COMBO_SIZE;
  


  protected JFileChooser m_FileChooser;
  


  protected FileFilter m_ArffFilter;
  


  protected JLabel m_JitterLab;
  

  protected JSlider m_Jitter;
  

  protected PlotPanel m_plot;
  

  protected AttributePanel m_attrib;
  

  protected LegendPanel m_legendPanel;
  

  protected JPanel m_plotSurround;
  

  protected JPanel m_classSurround;
  

  protected ActionListener listener;
  

  protected VisualizePanelListener m_splitListener;
  

  protected String m_plotName;
  

  protected ClassPanel m_classPanel;
  

  protected FastVector m_colorList;
  

  protected String m_preferredXDimension;
  

  protected String m_preferredYDimension;
  

  protected String m_preferredColourDimension;
  

  protected boolean m_showAttBars;
  

  protected boolean m_showClassPanel;
  

  protected weka.gui.Logger m_Log;
  


  public void setLog(weka.gui.Logger newLog)
  {
    m_Log = newLog;
  }
  






  public void setShowAttBars(boolean sab)
  {
    if ((!sab) && (m_showAttBars)) {
      m_plotSurround.remove(m_attrib);
    } else if ((sab) && (!m_showAttBars)) {
      GridBagConstraints constraints = new GridBagConstraints();
      insets = new Insets(0, 0, 0, 0);
      gridx = 4;gridy = 0;weightx = 1.0D;
      gridwidth = 1;gridheight = 1;weighty = 5.0D;
      m_plotSurround.add(m_attrib, constraints);
    }
    m_showAttBars = sab;
    repaint();
  }
  




  public boolean getShowAttBars()
  {
    return m_showAttBars;
  }
  




  public void setShowClassPanel(boolean scp)
  {
    if ((!scp) && (m_showClassPanel)) {
      remove(m_classSurround);
    } else if ((scp) && (!m_showClassPanel)) {
      add(m_classSurround, "South");
    }
    m_showClassPanel = scp;
    repaint();
  }
  




  public boolean getShowClassPanel()
  {
    return m_showClassPanel;
  }
  




  public VisualizePanel(VisualizePanelListener ls)
  {
    this();
    m_splitListener = ls;
  }
  




  private void setProperties(String relationName)
  {
    if (VisualizeUtils.VISUALIZE_PROPERTIES != null) {
      String thisClass = getClass().getName();
      if (relationName == null)
      {
        String showAttBars = thisClass + ".displayAttributeBars";
        
        String val = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(showAttBars);
        
        if (val != null)
        {



          if (m_showAttBars) {
            if ((val.compareTo("true") == 0) || (val.compareTo("on") == 0))
            {
              m_showAttBars = true;
            } else {
              m_showAttBars = false;
            }
            
          }
          
        }
      }
      else
      {
        String xcolKey = thisClass + "." + relationName + ".XDimension";
        String ycolKey = thisClass + "." + relationName + ".YDimension";
        String ccolKey = thisClass + "." + relationName + ".ColourDimension";
        
        m_preferredXDimension = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(xcolKey);
        









        m_preferredYDimension = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(ycolKey);
        









        m_preferredColourDimension = VisualizeUtils.VISUALIZE_PROPERTIES.getProperty(ccolKey);
      }
    }
  }
  
  public VisualizePanel()
  {
    Messages.getInstance();m_submit = new JButton(Messages.getString("VisualizePanel_PlotPanel_Submit_JButton_Text"));
    

    Messages.getInstance();m_cancel = new JButton(Messages.getString("VisualizePanel_PlotPanel_Cancel_JButton_Text"));
    

    Messages.getInstance();m_openBut = new JButton(Messages.getString("VisualizePanel_PlotPanel_OpenBut_JButton_Text"));
    

    Messages.getInstance();m_saveBut = new JButton(Messages.getString("VisualizePanel_PlotPanel_SaveBut_JButton_Text"));
    

    COMBO_SIZE = new Dimension(250, m_saveBut.getPreferredSize().height);
    


    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    


    Messages.getInstance();m_ArffFilter = new ExtensionFileFilter(".arff", Messages.getString("VisualizePanel_PlotPanel_ArffFilter_FileFilter_Text"));
    


    Messages.getInstance();m_JitterLab = new JLabel(Messages.getString("VisualizePanel_PlotPanel_JitterLab_JLabel_Text"), 4);
    

    m_Jitter = new JSlider(0, 50, 0);
    

    m_plot = new PlotPanel();
    


    m_attrib = new AttributePanel(m_plot.m_plot2D.getBackground());
    


    m_legendPanel = new LegendPanel();
    

    m_plotSurround = new JPanel();
    

    m_classSurround = new JPanel();
    


    listener = null;
    


    m_splitListener = null;
    


    m_plotName = "";
    

    m_classPanel = new ClassPanel(m_plot.m_plot2D.getBackground());
    






    m_preferredXDimension = null;
    m_preferredYDimension = null;
    m_preferredColourDimension = null;
    

    m_showAttBars = true;
    

    m_showClassPanel = true;
    



























































































































































    setProperties(null);
    m_FileChooser.setFileFilter(m_ArffFilter);
    m_FileChooser.setFileSelectionMode(0);
    
    Messages.getInstance();m_XCombo.setToolTipText(Messages.getString("VisualizePanel_XCombo_SetToolTipText_Text"));
    Messages.getInstance();m_YCombo.setToolTipText(Messages.getString("VisualizePanel_YCombo_SetToolTipText_Text"));
    Messages.getInstance();m_ColourCombo.setToolTipText(Messages.getString("VisualizePanel_ColourCombo_SetToolTipText_Text"));
    Messages.getInstance();m_ShapeCombo.setToolTipText(Messages.getString("VisualizePanel_ShapeCombo_SetToolTipText_Text"));
    
    m_XCombo.setPreferredSize(COMBO_SIZE);
    m_YCombo.setPreferredSize(COMBO_SIZE);
    m_ColourCombo.setPreferredSize(COMBO_SIZE);
    m_ShapeCombo.setPreferredSize(COMBO_SIZE);
    
    m_XCombo.setMaximumSize(COMBO_SIZE);
    m_YCombo.setMaximumSize(COMBO_SIZE);
    m_ColourCombo.setMaximumSize(COMBO_SIZE);
    m_ShapeCombo.setMaximumSize(COMBO_SIZE);
    
    m_XCombo.setMinimumSize(COMBO_SIZE);
    m_YCombo.setMinimumSize(COMBO_SIZE);
    m_ColourCombo.setMinimumSize(COMBO_SIZE);
    m_ShapeCombo.setMinimumSize(COMBO_SIZE);
    
    m_XCombo.setEnabled(false);
    m_YCombo.setEnabled(false);
    m_ColourCombo.setEnabled(false);
    m_ShapeCombo.setEnabled(false);
    


    m_classPanel.addRepaintNotify(this);
    m_legendPanel.addRepaintNotify(this);
    



    for (int i = 0; i < m_DefaultColors.length; i++) {
      Color c = m_DefaultColors[i];
      if (c.equals(m_plot.m_plot2D.getBackground())) {
        int red = c.getRed();
        int blue = c.getBlue();
        int green = c.getGreen();
        red += (red < 128 ? (255 - red) / 2 : -(red / 2));
        blue += (blue < 128 ? (blue - red) / 2 : -(blue / 2));
        green += (green < 128 ? (255 - green) / 2 : -(green / 2));
        m_DefaultColors[i] = new Color(red, green, blue);
      }
    }
    m_classPanel.setDefaultColourList(m_DefaultColors);
    m_attrib.setDefaultColourList(m_DefaultColors);
    
    m_colorList = new FastVector(10);
    for (int noa = m_colorList.size(); noa < 10; noa++) {
      Color pc = m_DefaultColors[(noa % 10)];
      int ija = noa / 10;
      ija *= 2;
      for (int j = 0; j < ija; j++) {
        pc = pc.darker();
      }
      
      m_colorList.addElement(pc);
    }
    m_plot.setColours(m_colorList);
    m_classPanel.setColours(m_colorList);
    m_attrib.setColours(m_colorList);
    m_attrib.addAttributePanelListener(new AttributePanelListener() {
      public void attributeSelectionChange(AttributePanelEvent e) {
        if (m_xChange) {
          m_XCombo.setSelectedIndex(m_indexVal);
        } else {
          m_YCombo.setSelectedIndex(m_indexVal);
        }
        
      }
    });
    m_XCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_XCombo.getSelectedIndex();
        if (selected < 0) {
          selected = 0;
        }
        m_plot.setXindex(selected);
        

        if (listener != null) {
          listener.actionPerformed(e);
        }
        
      }
    });
    m_YCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_YCombo.getSelectedIndex();
        if (selected < 0) {
          selected = 0;
        }
        m_plot.setYindex(selected);
        

        if (listener != null) {
          listener.actionPerformed(e);
        }
        
      }
    });
    m_ColourCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_ColourCombo.getSelectedIndex();
        if (selected < 0) {
          selected = 0;
        }
        m_plot.setCindex(selected);
        
        if (listener != null) {
          listener.actionPerformed(e);
        }
        
      }
      
    });
    m_ShapeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_ShapeCombo.getSelectedIndex();
        if (selected < 0) {
          selected = 0;
        }
        m_plot.setSindex(selected);
        
        if (listener != null) {
          listener.actionPerformed(e);

        }
        
      }
      

    });
    m_Jitter.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        m_plot.setJitter(m_Jitter.getValue());
      }
      
    });
    Messages.getInstance();m_openBut.setToolTipText(Messages.getString("VisualizePanel_OpenBut_SetToolTipText_Text"));
    m_openBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openVisibleInstances();
      }
      
    });
    m_saveBut.setEnabled(false);
    Messages.getInstance();m_saveBut.setToolTipText(Messages.getString("VisualizePanel_SaveBut_SetToolTipText_Text"));
    m_saveBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VisualizePanel.this.saveVisibleInstances();
      }
      
    });
    JPanel combos = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    

    m_XCombo.setLightWeightPopupEnabled(false);
    m_YCombo.setLightWeightPopupEnabled(false);
    m_ColourCombo.setLightWeightPopupEnabled(false);
    m_ShapeCombo.setLightWeightPopupEnabled(false);
    combos.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    
    combos.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 2;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    combos.add(m_XCombo, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 2;gridheight = 1;
    combos.add(m_YCombo, constraints);
    gridx = 0;gridy = 1;weightx = 5.0D;
    gridwidth = 2;gridheight = 1;
    combos.add(m_ColourCombo, constraints);
    
    gridx = 2;gridy = 1;weightx = 5.0D;
    gridwidth = 2;gridheight = 1;
    combos.add(m_ShapeCombo, constraints);
    

    JPanel mbts = new JPanel();
    mbts.setLayout(new GridLayout(1, 4));
    mbts.add(m_submit);mbts.add(m_cancel);mbts.add(m_openBut);mbts.add(m_saveBut);
    
    gridx = 0;gridy = 2;weightx = 5.0D;
    gridwidth = 2;gridheight = 1;
    combos.add(mbts, constraints);
    

    gridx = 2;gridy = 2;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(10, 0, 0, 5);
    combos.add(m_JitterLab, constraints);
    gridx = 3;gridy = 2;
    weightx = 5.0D;
    insets = new Insets(10, 0, 0, 0);
    combos.add(m_Jitter, constraints);
    
    m_classSurround = new JPanel();
    Messages.getInstance();m_classSurround.setBorder(BorderFactory.createTitledBorder(Messages.getString("VisualizePanel_ClassSurround_BorderFactoryCreateTitledBorder_Text")));
    
    m_classSurround.setLayout(new BorderLayout());
    
    m_classPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
    m_classSurround.add(m_classPanel, "Center");
    
    GridBagLayout gb2 = new GridBagLayout();
    Messages.getInstance();m_plotSurround.setBorder(BorderFactory.createTitledBorder(Messages.getString("VisualizePanel_PlotSurround_BorderFactoryCreateTitledBorder_Text")));
    m_plotSurround.setLayout(gb2);
    
    fill = 1;
    insets = new Insets(0, 0, 0, 10);
    gridx = 0;gridy = 0;weightx = 3.0D;
    gridwidth = 4;gridheight = 1;weighty = 5.0D;
    m_plotSurround.add(m_plot, constraints);
    
    if (m_showAttBars) {
      insets = new Insets(0, 0, 0, 0);
      gridx = 4;gridy = 0;weightx = 1.0D;
      gridwidth = 1;gridheight = 1;weighty = 5.0D;
      m_plotSurround.add(m_attrib, constraints);
    }
    
    setLayout(new BorderLayout());
    add(combos, "North");
    add(m_plotSurround, "Center");
    add(m_classSurround, "South");
    
    String[] SNames = new String[4];
    Messages.getInstance();SNames[0] = Messages.getString("VisualizePanel_SNames_0_Text");
    Messages.getInstance();SNames[1] = Messages.getString("VisualizePanel_SNames_1_Text");
    Messages.getInstance();SNames[2] = Messages.getString("VisualizePanel_SNames_2_Text");
    Messages.getInstance();SNames[3] = Messages.getString("VisualizePanel_SNames_3_Text");
    
    m_ShapeCombo.setModel(new DefaultComboBoxModel(SNames));
    m_ShapeCombo.setEnabled(true);
  }
  




  protected void openVisibleInstances(Instances insts)
    throws Exception
  {
    PlotData2D tempd = new PlotData2D(insts);
    tempd.setPlotName(insts.relationName());
    tempd.addInstanceNumberAttribute();
    m_plot.m_plot2D.removeAllPlots();
    addPlot(tempd);
    

    Component parent = getParent();
    while (parent != null) {
      if ((parent instanceof JFrame)) {
        Messages.getInstance();Messages.getInstance();((JFrame)parent).setTitle(Messages.getString("VisualizePanel_OpenVisibleInstances_JFrame_Text_First") + insts.relationName() + Messages.getString("VisualizePanel_OpenVisibleInstances_JFrame_Text_Second"));
        


        break;
      }
      
      parent = parent.getParent();
    }
  }
  


  protected void openVisibleInstances()
  {
    try
    {
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        File sFile = m_FileChooser.getSelectedFile();
        if (!sFile.getName().toLowerCase().endsWith(".arff"))
        {
          sFile = new File(sFile.getParent(), sFile.getName() + ".arff");
        }
        File selected = sFile;
        Instances insts = new Instances(new BufferedReader(new FileReader(selected)));
        openVisibleInstances(insts);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      m_plot.m_plot2D.removeAllPlots();
      Messages.getInstance();JOptionPane.showMessageDialog(this, ex.getMessage(), Messages.getString("VisualizePanel_OpenVisibleInstances_JOptionPane.showMessageDialog_Text"), 0);
    }
  }
  






  private void saveVisibleInstances()
  {
    FastVector plots = m_plot.m_plot2D.getPlots();
    if (plots != null) {
      PlotData2D master = (PlotData2D)plots.elementAt(0);
      Instances saveInsts = new Instances(master.getPlotInstances());
      for (int i = 1; i < plots.size(); i++) {
        PlotData2D temp = (PlotData2D)plots.elementAt(i);
        Instances addInsts = temp.getPlotInstances();
        for (int j = 0; j < addInsts.numInstances(); j++) {
          saveInsts.add(addInsts.instance(j));
        }
      }
      try {
        int returnVal = m_FileChooser.showSaveDialog(this);
        if (returnVal == 0) {
          File sFile = m_FileChooser.getSelectedFile();
          if (!sFile.getName().toLowerCase().endsWith(".arff"))
          {
            sFile = new File(sFile.getParent(), sFile.getName() + ".arff");
          }
          
          File selected = sFile;
          Writer w = new BufferedWriter(new FileWriter(selected));
          w.write(saveInsts.toString());
          w.close();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  






  public void setColourIndex(int index)
  {
    if (index >= 0) {
      m_ColourCombo.setSelectedIndex(index);
    } else {
      m_ColourCombo.setSelectedIndex(0);
    }
    m_ColourCombo.setEnabled(false);
  }
  




  public void setXIndex(int index)
    throws Exception
  {
    if ((index >= 0) && (index < m_XCombo.getItemCount())) {
      m_XCombo.setSelectedIndex(index);
    } else {
      Messages.getInstance();throw new Exception(Messages.getString("VisualizePanel_SetXIndex_Text"));
    }
  }
  



  public int getXIndex()
  {
    return m_XCombo.getSelectedIndex();
  }
  



  public void setYIndex(int index)
    throws Exception
  {
    if ((index >= 0) && (index < m_YCombo.getItemCount())) {
      m_YCombo.setSelectedIndex(index);
    } else {
      Messages.getInstance();throw new Exception(Messages.getString("VisualizePanel_SetYIndex_Text"));
    }
  }
  



  public int getYIndex()
  {
    return m_YCombo.getSelectedIndex();
  }
  



  public int getCIndex()
  {
    return m_ColourCombo.getSelectedIndex();
  }
  



  public int getSIndex()
  {
    return m_ShapeCombo.getSelectedIndex();
  }
  



  public void setSIndex(int index)
    throws Exception
  {
    if ((index >= 0) && (index < m_ShapeCombo.getItemCount())) {
      m_ShapeCombo.setSelectedIndex(index);
    }
    else {
      Messages.getInstance();throw new Exception(Messages.getString("VisualizePanel_SetSIndex_Text"));
    }
  }
  



  public void addActionListener(ActionListener act)
  {
    listener = act;
  }
  



  public void setName(String plotName)
  {
    m_plotName = plotName;
  }
  




  public String getName()
  {
    return m_plotName;
  }
  



  public Instances getInstances()
  {
    return m_plot.m_plotInstances;
  }
  








  protected void newColorAttribute(int a, Instances i)
  {
    if (i.attribute(a).isNominal()) {
      for (int noa = m_colorList.size(); noa < i.attribute(a).numValues(); 
          noa++) {
        Color pc = m_DefaultColors[(noa % 10)];
        int ija = noa / 10;
        ija *= 2;
        for (int j = 0; j < ija; j++) {
          pc = pc.brighter();
        }
        
        m_colorList.addElement(pc);
      }
      m_plot.setColours(m_colorList);
      m_attrib.setColours(m_colorList);
      m_classPanel.setColours(m_colorList);
    }
  }
  






  public void setShapes(FastVector l)
  {
    m_plot.setShapes(l);
  }
  



  public void setInstances(Instances inst)
  {
    if ((inst.numAttributes() > 0) && (inst.numInstances() > 0)) {
      newColorAttribute(inst.numAttributes() - 1, inst);
    }
    
    PlotData2D temp = new PlotData2D(inst);
    temp.setPlotName(inst.relationName());
    try
    {
      setMasterPlot(temp);
    } catch (Exception ex) {
      System.err.println(ex);
      ex.printStackTrace();
    }
  }
  




  public void setUpComboBoxes(Instances inst)
  {
    setProperties(inst.relationName());
    int prefX = -1;
    int prefY = -1;
    if (inst.numAttributes() > 1) {
      prefY = 1;
    }
    int prefC = -1;
    String[] XNames = new String[inst.numAttributes()];
    String[] YNames = new String[inst.numAttributes()];
    String[] CNames = new String[inst.numAttributes()];
    for (int i = 0; i < XNames.length; i++) {
      String type = " ";
      switch (inst.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeNOMINAL_Text");
        break;
      case 0: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeNUMERIC_Text");
        break;
      case 2: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeSTRING_Text");
        break;
      case 3: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeDATE_Text");
        break;
      case 4: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeRELATIONAL_Text");
        break;
      default: 
        Messages.getInstance();type = type + Messages.getString("VisualizePanel_SetUpComboBoxes_AttributeDEFAULT_Text");
      }
      Messages.getInstance();XNames[i] = (Messages.getString("VisualizePanel_SetUpComboBoxes_XNames_Text") + " " + inst.attribute(i).name() + type);
      Messages.getInstance();YNames[i] = (Messages.getString("VisualizePanel_SetUpComboBoxes_YNames_Text") + " " + inst.attribute(i).name() + type);
      Messages.getInstance();CNames[i] = (Messages.getString("VisualizePanel_SetUpComboBoxes_CNames_Text") + " " + inst.attribute(i).name() + type);
      if ((m_preferredXDimension != null) && 
        (m_preferredXDimension.compareTo(inst.attribute(i).name()) == 0)) {
        prefX = i;
      }
      

      if ((m_preferredYDimension != null) && 
        (m_preferredYDimension.compareTo(inst.attribute(i).name()) == 0)) {
        prefY = i;
      }
      

      if ((m_preferredColourDimension != null) && 
        (m_preferredColourDimension.compareTo(inst.attribute(i).name()) == 0))
      {
        prefC = i;
      }
    }
    

    m_XCombo.setModel(new DefaultComboBoxModel(XNames));
    m_YCombo.setModel(new DefaultComboBoxModel(YNames));
    
    m_ColourCombo.setModel(new DefaultComboBoxModel(CNames));
    

    m_XCombo.setEnabled(true);
    m_YCombo.setEnabled(true);
    
    if (m_splitListener == null) {
      m_ColourCombo.setEnabled(true);
      m_ColourCombo.setSelectedIndex(inst.numAttributes() - 1);
    }
    Messages.getInstance();m_plotSurround.setBorder(BorderFactory.createTitledBorder(Messages.getString("VisualizePanel_SetUpComboBoxes_PlotSurround_BorderFactoryCreateTitledBorder_Text") + inst.relationName()));
    try
    {
      if (prefX != -1) {
        setXIndex(prefX);
      }
      if (prefY != -1) {
        setYIndex(prefY);
      }
      if (prefC != -1) {
        m_ColourCombo.setSelectedIndex(prefC);
      }
    } catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_SetUpComboBoxes_Error_Text"));
    }
  }
  


  public void removeAllPlots()
  {
    m_plot.removeAllPlots();
  }
  



  public void setMasterPlot(PlotData2D newPlot)
    throws Exception
  {
    m_plot.setMasterPlot(newPlot);
    setUpComboBoxes(m_plotInstances);
    m_saveBut.setEnabled(true);
    repaint();
  }
  



  public void addPlot(PlotData2D newPlot)
    throws Exception
  {
    m_plot.addPlot(newPlot);
    if (m_plot.m_plot2D.getMasterPlot() != null) {
      setUpComboBoxes(m_plotInstances);
    }
    m_saveBut.setEnabled(true);
    repaint();
  }
  




  public PlotPanel getPlotPanel()
  {
    return m_plot;
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length < 1) {
        Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_Main_Error_Text_First"));
        System.exit(1);
      }
      
      Messages.getInstance();weka.core.logging.Logger.log(Logger.Level.INFO, Messages.getString("VisualizePanel_Main_Logger_Text"));
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("VisualizePanel_Main_JFrame_Text"));
      
      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      VisualizePanel sp = new VisualizePanel();
      
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
        
      });
      jf.setVisible(true);
      if (args.length >= 1) {
        for (int j = 0; j < args.length; j++) {
          Messages.getInstance();System.err.println(Messages.getString("VisualizePanel_Main_Error_Text_Second") + args[j]);
          Reader r = new BufferedReader(new FileReader(args[j]));
          
          Instances i = new Instances(r);
          i.setClassIndex(i.numAttributes() - 1);
          PlotData2D pd1 = new PlotData2D(i);
          
          if (j == 0) {
            Messages.getInstance();pd1.setPlotName(Messages.getString("VisualizePanel_Main_Pd1_SetPlotName_Text_First"));
            sp.setMasterPlot(pd1);
          } else {
            Messages.getInstance();pd1.setPlotName(Messages.getString("VisualizePanel_Main_Pd1_SetPlotName_Text_Second") + (j + 1));
            m_useCustomColour = true;
            m_customColour = (j % 2 == 0 ? Color.red : Color.blue);
            sp.addPlot(pd1);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
