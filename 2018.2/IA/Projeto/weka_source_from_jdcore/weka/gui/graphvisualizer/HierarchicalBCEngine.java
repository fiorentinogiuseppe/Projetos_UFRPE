package weka.gui.graphvisualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import weka.core.FastVector;










































































public class HierarchicalBCEngine
  implements GraphConstants, LayoutEngine
{
  protected FastVector m_nodes;
  protected FastVector m_edges;
  protected FastVector layoutCompleteListeners;
  protected int[][] graphMatrix;
  protected int[][] nodeLevels;
  protected int m_nodeWidth;
  protected int m_nodeHeight;
  protected JRadioButton m_jRbNaiveLayout;
  protected JRadioButton m_jRbPriorityLayout;
  protected JRadioButton m_jRbTopdown;
  protected JRadioButton m_jRbBottomup;
  protected JCheckBox m_jCbEdgeConcentration;
  protected JPanel m_controlsPanel;
  protected JProgressBar m_progress;
  protected boolean m_completeReLayout = false;
  



  private int origNodesSize;
  



  public HierarchicalBCEngine(FastVector nodes, FastVector edges, int nodeWidth, int nodeHeight)
  {
    m_nodes = nodes;m_edges = edges;m_nodeWidth = nodeWidth;
    m_nodeHeight = nodeHeight;
    makeGUIPanel(false);
  }
  










  public HierarchicalBCEngine(FastVector nodes, FastVector edges, int nodeWidth, int nodeHeight, boolean edgeConcentration)
  {
    m_nodes = nodes;m_edges = edges;m_nodeWidth = nodeWidth;
    m_nodeHeight = nodeHeight;
    makeGUIPanel(edgeConcentration);
  }
  




  public HierarchicalBCEngine() {}
  




  protected void makeGUIPanel(boolean edgeConc)
  {
    Messages.getInstance();m_jRbNaiveLayout = new JRadioButton(Messages.getString("HierarchicalBCEngine_JRbTopdown_JRadioButton_Text"));
    Messages.getInstance();m_jRbPriorityLayout = new JRadioButton(Messages.getString("HierarchicalBCEngine_JRbPriorityLayout_JRadioButton_Text"));
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_jRbNaiveLayout);
    bg.add(m_jRbPriorityLayout);
    m_jRbPriorityLayout.setSelected(true);
    
    ActionListener a = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        m_completeReLayout = true;
      }
      
    };
    Messages.getInstance();m_jRbTopdown = new JRadioButton(Messages.getString("HierarchicalBCEngine_JRbTopdown_JRadioButton_Text"));
    Messages.getInstance();m_jRbBottomup = new JRadioButton(Messages.getString("HierarchicalBCEngine_JRbBottomup_JRadioButton_Text"));
    m_jRbTopdown.addActionListener(a);
    m_jRbBottomup.addActionListener(a);
    bg = new ButtonGroup();
    bg.add(m_jRbTopdown);
    bg.add(m_jRbBottomup);
    m_jRbBottomup.setSelected(true);
    
    Messages.getInstance();m_jCbEdgeConcentration = new JCheckBox(Messages.getString("HierarchicalBCEngine_JP1_JPanel_BorderFactoryCreateTitledBorder_Text"), edgeConc);
    m_jCbEdgeConcentration.setSelected(edgeConc);
    m_jCbEdgeConcentration.addActionListener(a);
    
    JPanel jp1 = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gridwidth = 0;
    anchor = 18;
    weightx = 1.0D;
    fill = 2;
    jp1.add(m_jRbNaiveLayout, gbc);
    jp1.add(m_jRbPriorityLayout, gbc);
    Messages.getInstance();jp1.setBorder(BorderFactory.createTitledBorder(Messages.getString("HierarchicalBCEngine_JP1_SetBorder_Text")));
    
    JPanel jp2 = new JPanel(new GridBagLayout());
    jp2.add(m_jRbTopdown, gbc);
    jp2.add(m_jRbBottomup, gbc);
    Messages.getInstance();jp2.setBorder(BorderFactory.createTitledBorder(Messages.getString("HierarchicalBCEngine_JP2_BorderFactoryCreateTitledBorder_Text")));
    

    m_progress = new JProgressBar(0, 11);
    m_progress.setBorderPainted(false);
    m_progress.setStringPainted(true);
    m_progress.setString("");
    
    m_progress.setValue(0);
    
    m_controlsPanel = new JPanel(new GridBagLayout());
    m_controlsPanel.add(jp1, gbc);
    m_controlsPanel.add(jp2, gbc);
    m_controlsPanel.add(m_jCbEdgeConcentration, gbc);
  }
  
  public FastVector getNodes()
  {
    return m_nodes;
  }
  




  public JPanel getControlPanel()
  {
    return m_controlsPanel;
  }
  


  public JProgressBar getProgressBar()
  {
    return m_progress;
  }
  





  public void setNodesEdges(FastVector nodes, FastVector edges)
  {
    m_nodes = nodes;m_edges = edges;
  }
  






  public void setNodeSize(int nodeWidth, int nodeHeight)
  {
    m_nodeWidth = nodeWidth;
    m_nodeHeight = nodeHeight;
  }
  




  public void addLayoutCompleteEventListener(LayoutCompleteEventListener l)
  {
    if (layoutCompleteListeners == null)
      layoutCompleteListeners = new FastVector();
    layoutCompleteListeners.addElement(l);
  }
  



  public void removeLayoutCompleteEventListener(LayoutCompleteEventListener e)
  {
    if (layoutCompleteListeners != null)
    {

      for (int i = 0; i < layoutCompleteListeners.size(); i++) {
        LayoutCompleteEventListener l = (LayoutCompleteEventListener)layoutCompleteListeners.elementAt(i);
        if (l == e) {
          layoutCompleteListeners.removeElementAt(i);
          return;
        }
      }
      Messages.getInstance();System.err.println(Messages.getString("HierarchicalBCEngine_RemoveLayoutCompleteEventListener_Error_Text_First"));
    }
    else {
      Messages.getInstance();System.err.println(Messages.getString("HierarchicalBCEngine_RemoveLayoutCompleteEventListener_Error_Text_Second"));
    }
  }
  


  public void fireLayoutCompleteEvent(LayoutCompleteEvent e)
  {
    if ((layoutCompleteListeners != null) && (layoutCompleteListeners.size() != 0))
    {

      for (int i = 0; i < layoutCompleteListeners.size(); i++) {
        LayoutCompleteEventListener l = (LayoutCompleteEventListener)layoutCompleteListeners.elementAt(i);
        l.layoutCompleted(e);
      }
    }
  }
  














  public void layoutGraph()
  {
    if ((m_nodes == null) || (m_edges == null)) {
      return;
    }
    Thread th = new Thread() {
      public void run() {
        m_progress.setBorderPainted(true);
        if (nodeLevels == null) {
          makeProperHierarchy();
        }
        else if (m_completeReLayout == true) {
          clearTemps_and_EdgesFromNodes();makeProperHierarchy();
          m_completeReLayout = false;
        }
        

        if (m_jRbTopdown.isSelected()) {
          int crossbefore = crossings(nodeLevels);int crossafter = 0;int i = 0;
          do {
            m_progress.setValue(i + 4);
            Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_LayoutGraph_Progress_SetString_Text_First") + (i + 1));
            if (i != 0)
              crossbefore = crossafter;
            nodeLevels = HierarchicalBCEngine.this.minimizeCrossings(false, nodeLevels);
            crossafter = crossings(nodeLevels);
            i++;
          }
          while ((crossafter < crossbefore) && (i < 6));
        }
        else {
          int crossbefore = crossings(nodeLevels);int crossafter = 0;int i = 0;
          do {
            m_progress.setValue(i + 4);
            Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_LayoutGraph_Progress_SetString_Text_Second") + (i + 1));
            if (i != 0)
              crossbefore = crossafter;
            nodeLevels = HierarchicalBCEngine.this.minimizeCrossings(true, nodeLevels);
            crossafter = crossings(nodeLevels);
            i++;
          }
          while ((crossafter < crossbefore) && (i < 6));
        }
        



        m_progress.setValue(10);
        Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_LayoutGraph_Progress_SetString_Text_Third"));
        
        if (m_jRbNaiveLayout.isSelected()) {
          naiveLayout();
        } else
          priorityLayout1();
        m_progress.setValue(11);
        Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_LayoutGraph_Progress_SetString_Text_Fourth"));
        m_progress.repaint();
        
        fireLayoutCompleteEvent(new LayoutCompleteEvent(this));
        m_progress.setValue(0);
        m_progress.setString("");
        m_progress.setBorderPainted(false);
      }
    };
    th.start();
  }
  













  protected void clearTemps_and_EdgesFromNodes()
  {
    int curSize = m_nodes.size();
    for (int i = origNodesSize; i < curSize; i++)
      m_nodes.removeElementAt(origNodesSize);
    for (int j = 0; j < m_nodes.size(); j++) {
      m_nodes.elementAt(j)).edges = ((int[][])null);
    }
    nodeLevels = ((int[][])null);
  }
  












  protected void processGraph()
  {
    origNodesSize = m_nodes.size();
    graphMatrix = new int[m_nodes.size()][m_nodes.size()];
    







    for (int i = 0; i < m_edges.size(); i++) {
      graphMatrix[m_edges.elementAt(i)).src][m_edges.elementAt(i)).dest] = m_edges.elementAt(i)).type;
    }
  }
  



















  protected void makeProperHierarchy()
  {
    processGraph();
    
    m_progress.setValue(1);
    Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_MakeProperHierarchy_Progress_SetString_Text_First"));
    
    removeCycles();
    
    m_progress.setValue(2);
    Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_MakeProperHierarchy_Progress_SetString_Text_Second"));
    
    int[] nodesLevel = new int[m_nodes.size()];
    int depth = 0;
    for (int i = 0; i < graphMatrix.length; i++) {
      assignLevels(nodesLevel, depth, i, 0);
    }
    
    for (int i = 0; i < nodesLevel.length; i++) {
      if (nodesLevel[i] == 0) {
        int min = 65536;
        for (int j = 0; j < graphMatrix[i].length; j++) {
          if ((graphMatrix[i][j] == 1) && 
            (min > nodesLevel[j])) {
            min = nodesLevel[j];
          }
        }
        
        if ((min != 65536) && (min > 1)) {
          nodesLevel[i] = (min - 1);
        }
      }
    }
    
    int maxLevel = 0;
    for (int i = 0; i < nodesLevel.length; i++) {
      if (nodesLevel[i] > maxLevel) {
        maxLevel = nodesLevel[i];
      }
    }
    
    int[] levelCounts = new int[maxLevel + 1];
    
    for (int i = 0; i < nodesLevel.length; i++) {
      levelCounts[nodesLevel[i]] += 1;
    }
    

    int[] levelsCounter = new int[maxLevel + 1];
    nodeLevels = new int[maxLevel + 1][];
    for (int i = 0; i < nodesLevel.length; i++) {
      if (nodeLevels[nodesLevel[i]] == null) {
        nodeLevels[nodesLevel[i]] = new int[levelCounts[nodesLevel[i]]];
      }
      

      int tmp323_322 = nodesLevel[i]; int[] tmp323_317 = levelsCounter; int tmp325_324 = tmp323_317[tmp323_322];tmp323_317[tmp323_322] = (tmp325_324 + 1);nodeLevels[nodesLevel[i]][tmp325_324] = i;
    }
    
    m_progress.setValue(3);
    Messages.getInstance();m_progress.setString(Messages.getString("HierarchicalBCEngine_MakeProperHierarchy_Progress_SetString_Text_Third"));
    
    if (m_jCbEdgeConcentration.isSelected()) {
      removeGapsWithEdgeConcentration(nodesLevel);
    } else {
      removeGaps(nodesLevel);
    }
    














    for (int i = 0; i < graphMatrix.length; i++) {
      GraphNode n = (GraphNode)m_nodes.elementAt(i);
      int sum = 0;
      for (int j = 0; j < graphMatrix[i].length; j++) {
        if (graphMatrix[i][j] != 0)
          sum++;
      }
      edges = new int[sum][2];
      int j = 0; for (int k = 0; j < graphMatrix[i].length; j++) {
        if (graphMatrix[i][j] != 0) {
          edges[k][0] = j;
          edges[k][1] = graphMatrix[i][j];k++;
        }
      }
    }
  }
  









  private void removeGaps(int[] nodesLevel)
  {
    int temp = m_nodes.size();
    int temp2 = graphMatrix[0].length;int tempCnt = 1;
    
    for (int n = 0; n < temp; n++) {
      for (int i = 0; i < temp2; i++) {
        int len = graphMatrix.length;
        if (graphMatrix[n][i] > 0) {
          if (nodesLevel[i] > nodesLevel[n] + 1) {
            int[][] tempMatrix = new int[graphMatrix.length + (nodesLevel[i] - nodesLevel[n] - 1)][graphMatrix.length + (nodesLevel[i] - nodesLevel[n] - 1)];
            

            int level = nodesLevel[n] + 1;
            copyMatrix(graphMatrix, tempMatrix);
            
            String s1 = new String("S" + tempCnt++);
            m_nodes.addElement(new GraphNode(s1, s1, 1));
            int[] temp3 = new int[nodeLevels[level].length + 1];
            

            System.arraycopy(nodeLevels[level], 0, temp3, 0, nodeLevels[level].length);
            
            temp3[(temp3.length - 1)] = (m_nodes.size() - 1);
            nodeLevels[level] = temp3;level++;
            




            for (int k = len; k < len + nodesLevel[i] - nodesLevel[n] - 1 - 1; k++) {
              String s2 = new String("S" + tempCnt);
              m_nodes.addElement(new GraphNode(s2, s2, 1));
              temp3 = new int[nodeLevels[level].length + 1];
              

              System.arraycopy(nodeLevels[level], 0, temp3, 0, nodeLevels[level].length);
              
              temp3[(temp3.length - 1)] = (m_nodes.size() - 1);
              nodeLevels[(level++)] = temp3;
              
              tempMatrix[k][(k + 1)] = tempMatrix[n][i];tempCnt++;
              if (k > len) {
                tempMatrix[k][(k - 1)] = (-1 * tempMatrix[n][i]);
              }
            }
            
            tempMatrix[k][i] = tempMatrix[n][i];
            




            tempMatrix[n][len] = tempMatrix[n][i];
            
            tempMatrix[len][n] = (-1 * tempMatrix[n][i]);
            
            tempMatrix[i][k] = (-1 * tempMatrix[n][i]);
            


            if (k > len) {
              tempMatrix[k][(k - 1)] = (-1 * tempMatrix[n][i]);
            }
            

            tempMatrix[n][i] = 0;
            tempMatrix[i][n] = 0;
            
            graphMatrix = tempMatrix;


          }
          else
          {


            graphMatrix[i][n] = (-1 * graphMatrix[n][i]);
          }
        }
      }
    }
  }
  









  private void removeGapsWithEdgeConcentration(int[] nodesLevel)
  {
    int temp = m_nodes.size();int temp2 = graphMatrix[0].length;
    int tempCnt = 1;
    
    for (int n = 0; n < temp; n++) {
      for (int i = 0; i < temp2; i++) {
        if (graphMatrix[n][i] > 0) {
          if (nodesLevel[i] > nodesLevel[n] + 1)
          {


            int tempLevel = nodesLevel[n];
            boolean tempNodePresent = false;
            int k = temp;
            int tempnode = n;
            
            while (tempLevel < nodesLevel[i] - 1) {
              tempNodePresent = false;
              for (; k < graphMatrix.length; k++) {
                if (graphMatrix[tempnode][k] > 0)
                {
                  tempNodePresent = true; break;
                }
              }
              if (tempNodePresent) {
                tempnode = k;k += 1;
                tempLevel++;

              }
              else if (tempnode != n) {
                tempnode = k - 1;
              }
            }
            

            if (m_nodes.elementAt(tempnode)).nodeType == 1)
              m_nodes.elementAt(tempnode)).nodeType = 2;
            if (tempNodePresent)
            {
              graphMatrix[tempnode][i] = graphMatrix[n][i];
              











              graphMatrix[i][tempnode] = (-graphMatrix[n][i]);
              
              graphMatrix[n][i] = 0;
              graphMatrix[i][n] = 0;

            }
            else
            {
              int len = graphMatrix.length;
              int[][] tempMatrix = new int[graphMatrix.length + (nodesLevel[i] - nodesLevel[tempnode] - 1)][graphMatrix.length + (nodesLevel[i] - nodesLevel[tempnode] - 1)];
              

              int level = nodesLevel[tempnode] + 1;
              copyMatrix(graphMatrix, tempMatrix);
              
              String s1 = new String("S" + tempCnt++);
              
              m_nodes.addElement(new GraphNode(s1, s1, 1));
              
              int[] temp3 = new int[nodeLevels[level].length + 1];
              System.arraycopy(nodeLevels[level], 0, temp3, 0, nodeLevels[level].length);
              
              temp3[(temp3.length - 1)] = (m_nodes.size() - 1);
              nodeLevels[level] = temp3;
              temp3 = new int[m_nodes.size() + 1];
              System.arraycopy(nodesLevel, 0, temp3, 0, nodesLevel.length);
              temp3[(m_nodes.size() - 1)] = level;
              nodesLevel = temp3;
              level++;
              





              for (int m = len; m < len + nodesLevel[i] - nodesLevel[tempnode] - 1 - 1; m++) {
                String s2 = new String("S" + tempCnt++);
                
                m_nodes.addElement(new GraphNode(s2, s2, 1));
                temp3 = new int[nodeLevels[level].length + 1];
                

                System.arraycopy(nodeLevels[level], 0, temp3, 0, nodeLevels[level].length);
                
                temp3[(temp3.length - 1)] = (m_nodes.size() - 1);
                nodeLevels[level] = temp3;
                temp3 = new int[m_nodes.size() + 1];
                System.arraycopy(nodesLevel, 0, temp3, 0, nodesLevel.length);
                temp3[(m_nodes.size() - 1)] = level;
                nodesLevel = temp3;
                level++;
                



                tempMatrix[m][(m + 1)] = tempMatrix[n][i];
                if (m > len)
                {


                  tempMatrix[m][(m - 1)] = (-1 * tempMatrix[n][i]);
                }
              }
              








              tempMatrix[m][i] = tempMatrix[n][i];
              




              tempMatrix[tempnode][len] = tempMatrix[n][i];
              




              tempMatrix[len][tempnode] = (-1 * tempMatrix[n][i]);
              




              tempMatrix[i][m] = (-1 * tempMatrix[n][i]);
              if (m > len)
              {





                tempMatrix[m][(m - 1)] = (-1 * tempMatrix[n][i]);
              }
              

              tempMatrix[n][i] = 0;
              

              tempMatrix[i][n] = 0;
              
              graphMatrix = tempMatrix;

            }
            


          }
          else
          {


            graphMatrix[i][n] = (-1 * graphMatrix[n][i]);
          }
        }
      }
    }
  }
  








  private int indexOfElementInLevel(int element, int[] level)
    throws Exception
  {
    for (int i = 0; i < level.length; i++)
      if (level[i] == element)
        return i;
    Messages.getInstance();Messages.getInstance();throw new Exception(Messages.getString("HierarchicalBCEngine_IndexOfElementInLevel_Exception_Text_First") + m_nodes.elementAt(element)).ID + Messages.getString("HierarchicalBCEngine_IndexOfElementInLevel_Exception_Text_Second") + "weka.gui.graphvisualizer.HierarchicalBCEngine");
  }
  










  protected int crossings(int[][] levels)
  {
    int sum = 0;
    
    for (int i = 0; i < levels.length - 1; i++)
    {

      MyList upper = new MyList(null);MyList lower = new MyList(null);
      MyListNode[] lastOcrnce = new MyListNode[m_nodes.size()];
      int[] edgeOcrnce = new int[m_nodes.size()];
      
      int j = 0;int uidx = 0; for (int lidx = 0; j < levels[i].length + levels[(i + 1)].length; j++) {
        if (((j % 2 == 0) && (uidx < levels[i].length)) || (lidx >= levels[(i + 1)].length)) {
          int k1 = 0;int k2 = 0;int k3 = 0;
          GraphNode n = (GraphNode)m_nodes.elementAt(levels[i][uidx]);
          

          if (lastOcrnce[levels[i][uidx]] != null) {
            MyListNode temp = new MyListNode(-1);next = first;
            try {
              do {
                temp = next;
                if (levels[i][uidx] == n) {
                  k1 += 1;
                  k3 += k2;
                  
                  upper.remove(temp);
                }
                else {
                  k2 += 1;
                } } while (temp != lastOcrnce[levels[i][uidx]]);
            }
            catch (NullPointerException ex) {
              Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_First") + levels[i][uidx] + Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Second") + m_nodes.elementAt(levels[i][uidx])).ID + Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Third") + temp + Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Fourth") + first);
              


              ex.printStackTrace();
              System.exit(-1);
            }
            lastOcrnce[levels[i][uidx]] = null;
            sum = sum + k1 * lower.size() + k3;
          }
          

          for (int k = 0; k < edges.length; k++) {
            if (edges[k][1] > 0)
              try {
                if (indexOfElementInLevel(edges[k][0], levels[(i + 1)]) >= uidx) {
                  edgeOcrnce[edges[k][0]] = 1;
                }
              }
              catch (Exception ex) {
                ex.printStackTrace();
              }
          }
          for (int k = 0; k < levels[(i + 1)].length; k++) {
            if (edgeOcrnce[levels[(i + 1)][k]] == 1) {
              MyListNode temp = new MyListNode(levels[(i + 1)][k]);
              lower.add(temp);
              lastOcrnce[levels[(i + 1)][k]] = temp;
              edgeOcrnce[levels[(i + 1)][k]] = 0;
            }
          }
          



          uidx++;
        }
        else {
          int k1 = 0;int k2 = 0;int k3 = 0;
          GraphNode n = (GraphNode)m_nodes.elementAt(levels[(i + 1)][lidx]);
          

          if (lastOcrnce[levels[(i + 1)][lidx]] != null)
          {
            MyListNode temp = new MyListNode(-1);next = first;
            try {
              do {
                temp = next;
                if (levels[(i + 1)][lidx] == n) {
                  k1 += 1;
                  k3 += k2;
                  lower.remove(temp);
                }
                else
                {
                  k2 += 1;

                }
                
              }
              while (temp != lastOcrnce[levels[(i + 1)][lidx]]);
            }
            catch (NullPointerException ex) {
              Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.print(Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Fifth") + levels[(i + 1)][lidx] + Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Sixth") + m_nodes.elementAt(levels[(i + 1)][lidx])).ID + Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Seventh") + temp);
              


              Messages.getInstance();System.out.println(Messages.getString("HierarchicalBCEngine_Crossings_Exception_Text_Eighth") + first);
              ex.printStackTrace();
              System.exit(-1);
            }
            
            lastOcrnce[levels[(i + 1)][lidx]] = null;
            sum = sum + k1 * upper.size() + k3;
          }
          

          for (int k = 0; k < edges.length; k++) {
            if (edges[k][1] < 0)
              try {
                if (indexOfElementInLevel(edges[k][0], levels[i]) > lidx) {
                  edgeOcrnce[edges[k][0]] = 1;
                }
              }
              catch (Exception ex) {
                ex.printStackTrace();
              }
          }
          for (int k = 0; k < levels[i].length; k++) {
            if (edgeOcrnce[levels[i][k]] == 1) {
              MyListNode temp = new MyListNode(levels[i][k]);
              upper.add(temp);
              lastOcrnce[levels[i][k]] = temp;
              edgeOcrnce[levels[i][k]] = 0;
            }
          }
          




          lidx++;
        }
      }
    }
    

    return sum;
  }
  





  protected void removeCycles()
  {
    int[] visited = new int[m_nodes.size()];
    
    for (int i = 0; i < graphMatrix.length; i++) {
      if (visited[i] == 0) {
        removeCycles2(i, visited);
        visited[i] = 1;
      }
    }
  }
  

  private void removeCycles2(int nindex, int[] visited)
  {
    visited[nindex] = 2;
    for (int i = 0; i < graphMatrix[nindex].length; i++) {
      if (graphMatrix[nindex][i] == 1) {
        if (visited[i] == 0) {
          removeCycles2(i, visited);
          visited[i] = 1;
        }
        else if (visited[i] == 2) {
          if (nindex == i) {
            graphMatrix[nindex][i] = 0;
          }
          else if (graphMatrix[i][nindex] == 1)
          {
            graphMatrix[i][nindex] = 3;
            graphMatrix[nindex][i] = -3;
          }
          else
          {
            graphMatrix[i][nindex] = 2;
            graphMatrix[nindex][i] = -2;
          }
        }
      }
    }
  }
  



  protected void assignLevels(int[] levels, int depth, int i, int j)
  {
    if (i >= graphMatrix.length)
      return;
    if (j >= graphMatrix[i].length)
      return;
    if (graphMatrix[i][j] <= 0) {
      assignLevels(levels, depth, i, ++j);
    } else if ((graphMatrix[i][j] == 1) || (graphMatrix[i][j] == 3)) {
      if (depth + 1 > levels[j]) {
        levels[j] = (depth + 1);
        assignLevels(levels, depth + 1, j, 0);
      }
      assignLevels(levels, depth, i, ++j);
    }
  }
  








  private int[][] minimizeCrossings(boolean reversed, int[][] nodeLevels)
  {
    if (!reversed) {
      for (int times = 0; times < 1; times++) {
        int[][] tempLevels = new int[nodeLevels.length][];
        



        copy2DArray(nodeLevels, tempLevels);
        for (int i = 0; i < nodeLevels.length - 1; i++)
          phaseID(i, tempLevels);
        if (crossings(tempLevels) < crossings(nodeLevels)) {
          nodeLevels = tempLevels;
        }
        

        tempLevels = new int[nodeLevels.length][];
        copy2DArray(nodeLevels, tempLevels);
        for (int i = nodeLevels.length - 2; i >= 0; i--)
          phaseIU(i, tempLevels);
        if (crossings(tempLevels) < crossings(nodeLevels)) {
          nodeLevels = tempLevels;
        }
        

        tempLevels = new int[nodeLevels.length][];
        copy2DArray(nodeLevels, tempLevels);
        for (int i = 0; i < nodeLevels.length - 1; i++) {
          phaseIID(i, tempLevels);
        }
        if (crossings(tempLevels) < crossings(nodeLevels)) {
          nodeLevels = tempLevels;
        }
        




        tempLevels = new int[nodeLevels.length][];
        copy2DArray(nodeLevels, tempLevels);
        for (int i = nodeLevels.length - 2; i >= 0; i--) {
          phaseIIU(i, tempLevels);
        }
        if (crossings(tempLevels) < crossings(nodeLevels)) {
          nodeLevels = tempLevels;
        }
      }
      



      return nodeLevels;
    }
    
    for (int times = 0; times < 1; times++) {
      int[][] tempLevels = new int[nodeLevels.length][];
      



      copy2DArray(nodeLevels, tempLevels);
      for (int i = nodeLevels.length - 2; i >= 0; i--)
        phaseIU(i, tempLevels);
      if (crossings(tempLevels) < crossings(nodeLevels)) {
        nodeLevels = tempLevels;
      }
      


      tempLevels = new int[nodeLevels.length][];
      copy2DArray(nodeLevels, tempLevels);
      for (int i = 0; i < nodeLevels.length - 1; i++)
        phaseID(i, tempLevels);
      if (crossings(tempLevels) < crossings(nodeLevels)) {
        nodeLevels = tempLevels;
      }
      


      tempLevels = new int[nodeLevels.length][];
      copy2DArray(nodeLevels, tempLevels);
      for (int i = nodeLevels.length - 2; i >= 0; i--) {
        phaseIIU(i, tempLevels);
      }
      if (crossings(tempLevels) < crossings(nodeLevels)) {
        nodeLevels = tempLevels;
      }
      


      tempLevels = new int[nodeLevels.length][];
      copy2DArray(nodeLevels, tempLevels);
      for (int i = 0; i < nodeLevels.length - 1; i++) {
        phaseIID(i, tempLevels);
      }
      if (crossings(tempLevels) < crossings(nodeLevels)) {
        nodeLevels = tempLevels;
      }
    }
    

    return nodeLevels;
  }
  









  protected void phaseID(int lindex, int[][] levels)
  {
    float[] colBC = calcColBC(lindex, levels);
    


















    isort(levels[(lindex + 1)], colBC);
  }
  


















  public void phaseIU(int lindex, int[][] levels)
  {
    float[] rowBC = calcRowBC(lindex, levels);
    

















    isort(levels[lindex], rowBC);
  }
  













  public void phaseIID(int lindex, int[][] levels)
  {
    float[] colBC = calcColBC(lindex, levels);
    

    for (int i = 0; i < colBC.length - 1; i++) {
      if (colBC[i] == colBC[(i + 1)])
      {

        int[][] tempLevels = new int[levels.length][];
        copy2DArray(levels, tempLevels);
        




        int node1 = levels[(lindex + 1)][i];
        int node2 = levels[(lindex + 1)][(i + 1)];
        levels[(lindex + 1)][(i + 1)] = node1;
        levels[(lindex + 1)][i] = node2;
        
        for (int k = lindex + 1; k < levels.length - 1; k++) {
          phaseID(k, levels);
        }
        
        if (crossings(levels) <= crossings(tempLevels))
        {

          copy2DArray(levels, tempLevels);
        } else {
          copy2DArray(tempLevels, levels);
          levels[(lindex + 1)][(i + 1)] = node1;
          levels[(lindex + 1)][i] = node2;
        }
        






        for (int k = levels.length - 2; k >= 0; k--) {
          phaseIU(k, levels);
        }
        




        if (crossings(tempLevels) < crossings(levels)) {
          copy2DArray(tempLevels, levels);
        }
      }
    }
  }
  









  public void phaseIIU(int lindex, int[][] levels)
  {
    float[] rowBC = calcRowBC(lindex, levels);
    

    for (int i = 0; i < rowBC.length - 1; i++) {
      if (rowBC[i] == rowBC[(i + 1)])
      {

        int[][] tempLevels = new int[levels.length][];
        copy2DArray(levels, tempLevels);
        



        int node1 = levels[lindex][i];
        int node2 = levels[lindex][(i + 1)];
        levels[lindex][(i + 1)] = node1;
        levels[lindex][i] = node2;
        
        for (int k = lindex - 1; k >= 0; k--)
          phaseIU(k, levels);
        if (crossings(levels) <= crossings(tempLevels))
        {

          copy2DArray(levels, tempLevels);
        } else {
          copy2DArray(tempLevels, levels);
          levels[lindex][(i + 1)] = node1;
          levels[lindex][i] = node2;
        }
        






        for (int k = 0; k < levels.length - 1; k++) {
          phaseID(k, levels);
        }
        


        if (crossings(tempLevels) <= crossings(levels)) {
          copy2DArray(tempLevels, levels);
        }
      }
    }
  }
  







  protected float[] calcRowBC(int lindex, int[][] levels)
  {
    float[] rowBC = new float[levels[lindex].length];
    

    for (int i = 0; i < levels[lindex].length; i++) {
      int sum = 0;
      GraphNode n = (GraphNode)m_nodes.elementAt(levels[lindex][i]);
      
      for (int j = 0; j < edges.length; j++)
        if (edges[j][1] > 0) {
          sum++;
          try {
            rowBC[i] = (rowBC[i] + indexOfElementInLevel(edges[j][0], levels[(lindex + 1)]) + 1.0F);
          }
          catch (Exception ex) {
            return null;
          }
        }
      if (rowBC[i] != 0.0F)
        rowBC[i] /= sum;
    }
    return rowBC;
  }
  



  protected float[] calcColBC(int lindex, int[][] levels)
  {
    float[] colBC = new float[levels[(lindex + 1)].length];
    

    for (int i = 0; i < levels[(lindex + 1)].length; i++) {
      int sum = 0;
      GraphNode n = (GraphNode)m_nodes.elementAt(levels[(lindex + 1)][i]);
      
      for (int j = 0; j < edges.length; j++)
        if (edges[j][1] < 1) {
          sum++;
          try {
            colBC[i] = (colBC[i] + indexOfElementInLevel(edges[j][0], levels[lindex]) + 1.0F);
          }
          catch (Exception ex) {
            return null;
          }
        }
      if (colBC[i] != 0.0F)
        colBC[i] /= sum;
    }
    return colBC;
  }
  



  protected void printMatrices(int[][] levels)
  {
    int i = 0;
    for (i = 0; i < levels.length - 1; i++) {
      float[] rowBC = null;float[] colBC = null;
      try {
        rowBC = calcRowBC(i, levels);colBC = calcColBC(i, levels);
      }
      catch (NullPointerException ne) {
        System.out.println("i: " + i + " levels.length: " + levels.length);
        ne.printStackTrace();
        return;
      }
      
      System.out.print("\nM" + (i + 1) + "\t");
      for (int j = 0; j < levels[(i + 1)].length; j++) {
        System.out.print(m_nodes.elementAt(levels[(i + 1)][j])).ID + " ");
      }
      

      System.out.println("");
      
      for (int j = 0; j < levels[i].length; j++) {
        System.out.print(m_nodes.elementAt(levels[i][j])).ID + "\t");
        
        for (int k = 0; k < levels[(i + 1)].length; k++)
        {
          System.out.print(graphMatrix[levels[i][j]][levels[(i + 1)][k]] + " ");
        }
        



        System.out.println(rowBC[j]);
      }
      System.out.print("\t");
      for (int k = 0; k < levels[(i + 1)].length; k++)
        System.out.print(colBC[k] + " ");
    }
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("HierarchicalBCEngine_PrintMatrices_Text_Second") + i + Messages.getString("HierarchicalBCEngine_PrintMatrices_Text_First") + levels.length);
  }
  




















































  protected static void isort(int[] level, float[] BC)
  {
    for (int i = 0; i < BC.length - 1; i++)
    {
      int j = i;
      float temp = BC[(j + 1)];
      int temp2 = level[(j + 1)];
      if (temp != 0.0F)
      {
        int prej = j + 1;
        
        while ((j > -1) && ((temp < BC[j]) || (BC[j] == 0.0F))) {
          if (BC[j] == 0.0F) {
            j--;
          } else {
            BC[prej] = BC[j];
            level[prej] = level[j];
            prej = j;
            j--;
          }
        }
        
        BC[prej] = temp;
        level[prej] = temp2;
      }
    }
  }
  





  protected void copyMatrix(int[][] from, int[][] to)
  {
    for (int i = 0; i < from.length; i++) {
      for (int j = 0; j < from[i].length; j++) {
        to[i][j] = from[i][j];
      }
    }
  }
  
  protected void copy2DArray(int[][] from, int[][] to)
  {
    for (int i = 0; i < from.length; i++) {
      to[i] = new int[from[i].length];
      System.arraycopy(from[i], 0, to[i], 0, from[i].length);
    }
  }
  



















  protected void naiveLayout()
  {
    if (nodeLevels == null) {
      makeProperHierarchy();
    }
    
    int i = 0; for (int temp = 0; i < nodeLevels.length; i++) {
      for (int j = 0; j < nodeLevels[i].length; j++) {
        temp = nodeLevels[i][j];
        
        GraphNode n = (GraphNode)m_nodes.elementAt(temp);
        x = (j * m_nodeWidth);
        y = (i * 3 * m_nodeHeight);
      }
    }
  }
  

  protected int uConnectivity(int lindex, int eindex)
  {
    int n = 0;
    for (int i = 0; i < nodeLevels[(lindex - 1)].length; i++) {
      if (graphMatrix[nodeLevels[(lindex - 1)][i]][nodeLevels[lindex][eindex]] > 0)
        n++;
    }
    return n;
  }
  
  protected int lConnectivity(int lindex, int eindex) {
    int n = 0;
    for (int i = 0; i < nodeLevels[(lindex + 1)].length; i++) {
      if (graphMatrix[nodeLevels[lindex][eindex]][nodeLevels[(lindex + 1)][i]] > 0)
        n++;
    }
    return n;
  }
  
  protected int uBCenter(int lindex, int eindex, int[] horPositions) {
    int sum = 0;
    
    for (int i = 0; i < nodeLevels[(lindex - 1)].length; i++)
      if (graphMatrix[nodeLevels[(lindex - 1)][i]][nodeLevels[lindex][eindex]] > 0)
        sum += horPositions[nodeLevels[(lindex - 1)][i]];
    if (sum != 0)
    {


      sum /= uConnectivity(lindex, eindex);
    }
    return sum;
  }
  
  protected int lBCenter(int lindex, int eindex, int[] horPositions)
  {
    int sum = 0;
    
    for (int i = 0; i < nodeLevels[(lindex + 1)].length; i++)
      if (graphMatrix[nodeLevels[lindex][eindex]][nodeLevels[(lindex + 1)][i]] > 0)
        sum += horPositions[nodeLevels[(lindex + 1)][i]];
    if (sum != 0)
      sum /= lConnectivity(lindex, eindex);
    return sum;
  }
  
  private void tempMethod(int[] horPositions)
  {
    int minPosition = horPositions[0];
    
    for (int i = 0; i < horPositions.length; i++)
      if (horPositions[i] < minPosition)
        minPosition = horPositions[i];
    if (minPosition < 0) {
      minPosition *= -1;
      for (int i = 0; i < horPositions.length; i++)
      {
        horPositions[i] += minPosition;
      }
    }
    


    int i = 0; for (int temp = 0; i < nodeLevels.length; i++) {
      for (int j = 0; j < nodeLevels[i].length; j++) {
        temp = nodeLevels[i][j];
        
        GraphNode n = (GraphNode)m_nodes.elementAt(temp);
        x = (horPositions[temp] * m_nodeWidth);
        y = (i * 3 * m_nodeHeight);
      }
    }
  }
  




  protected void priorityLayout1()
  {
    int[] horPositions = new int[m_nodes.size()];
    int maxCount = 0;
    
    for (int i = 0; i < nodeLevels.length; i++) {
      int count = 0;
      for (int j = 0; j < nodeLevels[i].length; j++) {
        horPositions[nodeLevels[i][j]] = j;
        count++;
      }
      if (count > maxCount) {
        maxCount = count;
      }
    }
    

    for (int i = 1; i < nodeLevels.length; i++) {
      int[] priorities = new int[nodeLevels[i].length];
      int[] BC = new int[nodeLevels[i].length];
      for (int j = 0; j < nodeLevels[i].length; j++) {
        if (m_nodes.elementAt(nodeLevels[i][j])).ID.startsWith("S")) {
          priorities[j] = (maxCount + 1);
        } else
          priorities[j] = uConnectivity(i, j);
        BC[j] = uBCenter(i, j, horPositions);
      }
      




      priorityLayout2(nodeLevels[i], priorities, BC, horPositions);
    }
    













    for (int i = nodeLevels.length - 2; i >= 0; i--) {
      int[] priorities = new int[nodeLevels[i].length];
      int[] BC = new int[nodeLevels[i].length];
      for (int j = 0; j < nodeLevels[i].length; j++) {
        if (m_nodes.elementAt(nodeLevels[i][j])).ID.startsWith("S")) {
          priorities[j] = (maxCount + 1);
        } else
          priorities[j] = lConnectivity(i, j);
        BC[j] = lBCenter(i, j, horPositions);
      }
      priorityLayout2(nodeLevels[i], priorities, BC, horPositions);
    }
    













    for (int i = 2; i < nodeLevels.length; i++) {
      int[] priorities = new int[nodeLevels[i].length];
      int[] BC = new int[nodeLevels[i].length];
      for (int j = 0; j < nodeLevels[i].length; j++) {
        if (m_nodes.elementAt(nodeLevels[i][j])).ID.startsWith("S")) {
          priorities[j] = (maxCount + 1);
        } else
          priorities[j] = uConnectivity(i, j);
        BC[j] = uBCenter(i, j, horPositions);
      }
      




      priorityLayout2(nodeLevels[i], priorities, BC, horPositions);
    }
    











    int minPosition = horPositions[0];
    
    for (int i = 0; i < horPositions.length; i++)
      if (horPositions[i] < minPosition)
        minPosition = horPositions[i];
    if (minPosition < 0) {
      minPosition *= -1;
      for (int i = 0; i < horPositions.length; i++)
      {
        horPositions[i] += minPosition;
      }
    }
    


    int i = 0; for (int temp = 0; i < nodeLevels.length; i++) {
      for (int j = 0; j < nodeLevels[i].length; j++) {
        temp = nodeLevels[i][j];
        
        GraphNode n = (GraphNode)m_nodes.elementAt(temp);
        x = (horPositions[temp] * m_nodeWidth);
        y = (i * 3 * m_nodeHeight);
      }
    }
  }
  







  private void priorityLayout2(int[] level, int[] priorities, int[] bCenters, int[] horPositions)
  {
    int[] descOrder = new int[priorities.length];
    

    descOrder[0] = 0;
    for (int i = 0; i < priorities.length - 1; i++) {
      int j = i;
      int temp = i + 1;
      
      while ((j > -1) && (priorities[descOrder[j]] < priorities[temp])) {
        descOrder[(j + 1)] = descOrder[j];
        j--;
      }
      j++;
      descOrder[j] = temp;
    }
    







    for (int k = 0; k < descOrder.length; k++) {
      for (int i = 0; i < descOrder.length; i++)
      {
        int leftCount = 0;int rightCount = 0;
        for (int j = 0; j < priorities.length; j++) {
          if (horPositions[level[descOrder[i]]] > horPositions[level[j]]) {
            leftCount++;
          } else if (horPositions[level[descOrder[i]]] < horPositions[level[j]])
            rightCount++;
        }
        int[] leftNodes = new int[leftCount];
        int[] rightNodes = new int[rightCount];
        
        int j = 0;int l = 0; for (int r = 0; j < priorities.length; j++) {
          if (horPositions[level[descOrder[i]]] > horPositions[level[j]]) {
            leftNodes[(l++)] = j;
          } else if (horPositions[level[descOrder[i]]] < horPositions[level[j]]) {
            rightNodes[(r++)] = j;
          }
        }
        

        while (Math.abs(horPositions[level[descOrder[i]]] - 1 - bCenters[descOrder[i]]) < Math.abs(horPositions[level[descOrder[i]]] - bCenters[descOrder[i]]))
        {


          int temp = horPositions[level[descOrder[i]]];
          boolean cantMove = false;
          
          for (int j = leftNodes.length - 1; j >= 0; j--) {
            if (temp - horPositions[level[leftNodes[j]]] > 1)
              break;
            if (priorities[descOrder[i]] <= priorities[leftNodes[j]]) {
              cantMove = true; break;
            }
            temp = horPositions[level[leftNodes[j]]];
          }
          


          if (cantMove) {
            break;
          }
          temp = horPositions[level[descOrder[i]]] - 1;
          
          for (int j = leftNodes.length - 1; j >= 0; j--) {
            if (temp == horPositions[level[leftNodes[j]]])
            {



              int tmp482_481 = (horPositions[level[leftNodes[j]]] - 1);temp = tmp482_481;horPositions[level[leftNodes[j]]] = tmp482_481;
            }
          }
          





          horPositions[level[descOrder[i]]] -= 1;
        }
        


        while (Math.abs(horPositions[level[descOrder[i]]] + 1 - bCenters[descOrder[i]]) < Math.abs(horPositions[level[descOrder[i]]] - bCenters[descOrder[i]]))
        {

          int temp = horPositions[level[descOrder[i]]];
          boolean cantMove = false;
          
          for (int j = 0; j < rightNodes.length; j++) {
            if (horPositions[level[rightNodes[j]]] - temp > 1)
              break;
            if (priorities[descOrder[i]] <= priorities[rightNodes[j]]) {
              cantMove = true; break;
            }
            temp = horPositions[level[rightNodes[j]]];
          }
          


          if (cantMove) {
            break;
          }
          temp = horPositions[level[descOrder[i]]] + 1;
          
          for (int j = 0; j < rightNodes.length; j++) {
            if (temp == horPositions[level[rightNodes[j]]])
            {



              int tmp720_719 = (horPositions[level[rightNodes[j]]] + 1);temp = tmp720_719;horPositions[level[rightNodes[j]]] = tmp720_719;
            }
          }
          




          horPositions[level[descOrder[i]]] += 1;
        }
      }
    }
  }
  
  private class MyList
  {
    int size;
    
    private MyList() {}
    
    HierarchicalBCEngine.MyListNode first = null;
    HierarchicalBCEngine.MyListNode last = null;
    
    public void add(int i) {
      if (first == null) {
        first = (this.last = new HierarchicalBCEngine.MyListNode(HierarchicalBCEngine.this, i));
      }
      else if (last.next == null) {
        last.next = new HierarchicalBCEngine.MyListNode(HierarchicalBCEngine.this, i);
        last.next.previous = last;
        last = last.next;
      }
      else {
        Messages.getInstance();System.err.println(Messages.getString("HierarchicalBCEngine_MyList_Add_Error_Text_First"));
        size -= 1;
      }
      size += 1;
    }
    
    public void add(HierarchicalBCEngine.MyListNode n) {
      if (first == null) {
        first = (this.last = n);
      }
      else if (last.next == null) {
        last.next = n;
        last.next.previous = last;
        last = last.next;
      }
      else {
        Messages.getInstance();System.err.println(Messages.getString("HierarchicalBCEngine_MyList_Add_Error_Text_Second"));
        size -= 1;
      }
      
      size += 1;
    }
    
    public void remove(HierarchicalBCEngine.MyListNode n) {
      if (previous != null)
        previous.next = next;
      if (next != null)
        next.previous = previous;
      if (last == n)
        last = previous;
      if (first == n) {
        first = next;
      }
      size -= 1;
    }
    
    public void remove(int i) {
      HierarchicalBCEngine.MyListNode temp = first;
      while ((temp != null) && (n != i)) { temp = next;
      }
      if (temp == null) {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("HierarchicalBCEngine_MyList_Remove_Error_Text_First") + i + Messages.getString("HierarchicalBCEngine_MyList_Remove_Error_Text_Second"));
        return;
      }
      
      if (previous != null)
        previous.next = next;
      if (next != null)
        next.previous = previous;
      if (last == temp)
        last = previous;
      if (first == temp) {
        first = next;
      }
      size -= 1;
    }
    
    public int size() {
      return size;
    }
  }
  
  private class MyListNode {
    int n;
    MyListNode next;
    MyListNode previous;
    
    public MyListNode(int i) {
      n = i;next = null;previous = null;
    }
  }
}
