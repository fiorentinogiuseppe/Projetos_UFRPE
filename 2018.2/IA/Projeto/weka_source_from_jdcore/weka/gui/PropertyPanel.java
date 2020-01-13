package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import weka.core.OptionHandler;
import weka.core.Utils;






































public class PropertyPanel
  extends JPanel
{
  static final long serialVersionUID = 5370025273466728904L;
  private PropertyEditor m_Editor;
  private PropertyDialog m_PD;
  private boolean m_HasCustomPanel = false;
  



  private JPanel m_CustomPanel;
  



  public PropertyPanel(PropertyEditor pe)
  {
    this(pe, false);
  }
  







  public PropertyPanel(PropertyEditor pe, boolean ignoreCustomPanel)
  {
    m_Editor = pe;
    
    if ((!ignoreCustomPanel) && ((m_Editor instanceof CustomPanelSupplier))) {
      setLayout(new BorderLayout());
      m_CustomPanel = ((CustomPanelSupplier)m_Editor).getCustomPanel();
      add(m_CustomPanel, "Center");
      m_HasCustomPanel = true;
    } else {
      createDefaultPanel();
    }
  }
  




  protected void createDefaultPanel()
  {
    setBorder(BorderFactory.createEtchedBorder());
    Messages.getInstance();setToolTipText(Messages.getString("PropertyPanel_CreateDefaultPanel_SetToolTipText_Text"));
    setOpaque(true);
    final Component comp = this;
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 1) {
          if ((evt.getButton() == 1) && (!evt.isAltDown()) && (!evt.isShiftDown())) {
            showPropertyDialog();
          }
          else if ((evt.getButton() == 3) || ((evt.getButton() == 1) && (evt.isAltDown()) && (evt.isShiftDown())))
          {
            JPopupMenu menu = new JPopupMenu();
            

            if (m_Editor.getValue() != null) {
              Messages.getInstance();JMenuItem item = new JMenuItem(Messages.getString("PropertyPanel_CreateDefaultPanel_Item_JMenuItem_Text_First"));
              item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  showPropertyDialog();
                }
              });
              menu.add(item);
              
              Messages.getInstance();item = new JMenuItem(Messages.getString("PropertyPanel_CreateDefaultPanel_Item_JMenuItem_Text_Second"));
              item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  String str = m_Editor.getValue().getClass().getName();
                  if ((m_Editor.getValue() instanceof OptionHandler))
                    str = str + " " + Utils.joinOptions(((OptionHandler)m_Editor.getValue()).getOptions());
                  StringSelection selection = new StringSelection(str.trim());
                  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                  clipboard.setContents(selection, selection);
                }
              });
              menu.add(item);
            }
            
            Messages.getInstance();JMenuItem item = new JMenuItem(Messages.getString("PropertyPanel_CreateDefaultPanel_Item_JMenuItem_Text_Third"));
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Messages.getInstance();String str = JOptionPane.showInputDialog(val$comp, Messages.getString("PropertyPanel_CreateDefaultPanel_Str_JOptionPaneShowInputDialog_Text"));
                

                if (str != null) {
                  try {
                    String[] options = Utils.splitOptions(str);
                    String classname = options[0];
                    options[0] = "";
                    m_Editor.setValue(Utils.forName(Object.class, classname, options));

                  }
                  catch (Exception ex)
                  {
                    ex.printStackTrace();
                    Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(val$comp, Messages.getString("PropertyPanel_CreateDefaultPanel_Exception_Text_First") + ex, Messages.getString("PropertyPanel_CreateDefaultPanel_Exception_Text_Second"), 0);
                  }
                  
                }
                
              }
              

            });
            menu.add(item);
            
            menu.show(comp, evt.getX(), evt.getY());
          }
        }
      }
    });
    Dimension newPref = getPreferredSize();
    height = (getFontMetrics(getFont()).getHeight() * 5 / 4);
    width = (height * 5);
    setPreferredSize(newPref);
    
    m_Editor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        repaint();
      }
    });
  }
  



  public void showPropertyDialog()
  {
    if (m_Editor.getValue() != null) {
      if (m_PD == null) {
        int x = getLocationOnScreenx;
        int y = getLocationOnScreeny;
        if (PropertyDialog.getParentDialog(this) != null) {
          m_PD = new PropertyDialog(PropertyDialog.getParentDialog(this), m_Editor, x, y);
        } else
          m_PD = new PropertyDialog(PropertyDialog.getParentFrame(this), m_Editor, x, y);
        m_PD.setVisible(true);
      } else {
        m_PD.setVisible(true);
      }
      
      m_Editor.setValue(m_Editor.getValue());
    }
  }
  



  public void removeNotify()
  {
    super.removeNotify();
    if (m_PD != null) {
      m_PD.dispose();
      m_PD = null;
    }
  }
  





  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    if (m_HasCustomPanel) {
      m_CustomPanel.setEnabled(enabled);
    }
  }
  






  public void paintComponent(Graphics g)
  {
    if (!m_HasCustomPanel) {
      Insets i = getInsets();
      Rectangle box = new Rectangle(left, top, getSizewidth - left - right - 1, getSizeheight - top - bottom - 1);
      


      g.clearRect(left, top, getSizewidth - right - left, getSizeheight - bottom - top);
      

      m_Editor.paintValue(g, box);
    }
  }
}
