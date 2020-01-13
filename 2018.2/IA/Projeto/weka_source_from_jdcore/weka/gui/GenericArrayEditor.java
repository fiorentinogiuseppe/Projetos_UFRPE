package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.PrintStream;
import java.lang.reflect.Array;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import weka.core.SerializedObject;
import weka.filters.Filter;

































public class GenericArrayEditor
  extends JPanel
  implements PropertyEditor
{
  private static final long serialVersionUID = 3914616975334750480L;
  private PropertyChangeSupport m_Support = new PropertyChangeSupport(this);
  private JLabel m_Label;
  
  public GenericArrayEditor() { Messages.getInstance();m_Label = new JLabel(Messages.getString("GenericArrayEditor_Label_JLabel_Text"), 0);
    

    m_ElementList = new JList();
    

    m_ElementClass = String.class;
    







    Messages.getInstance();m_DeleteBut = new JButton(Messages.getString("GenericArrayEditor_DeleteBut_JButton_Text"));
    

    Messages.getInstance();m_EditBut = new JButton(Messages.getString("GenericArrayEditor_EditBut_JButton_Text"));
    

    Messages.getInstance();m_UpBut = new JButton(Messages.getString("GenericArrayEditor_UpBut_JButton_Text"));
    

    Messages.getInstance();m_DownBut = new JButton(Messages.getString("GenericArrayEditor_DownBut_JButton_Text"));
    

    Messages.getInstance();m_AddBut = new JButton(Messages.getString("GenericArrayEditor_AddBut_JButton_Text"));
    

    m_Editor = new GenericObjectEditor();
    




    m_InnerActionListener = new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        if (e.getSource() == m_DeleteBut) {
          int[] selected = m_ElementList.getSelectedIndices();
          if (selected != null) {
            for (int i = selected.length - 1; i >= 0; i--) {
              int current = selected[i];
              m_ListModel.removeElementAt(current);
              if (m_ListModel.size() > current) {
                m_ElementList.setSelectedIndex(current);
              }
            }
            m_Support.firePropertyChange("", null, null);
          }
        } else if (e.getSource() == m_EditBut) {
          if ((m_Editor instanceof GenericObjectEditor)) {
            ((GenericObjectEditor)m_Editor).setClassType(m_ElementClass);
          }
          try {
            m_Editor.setValue(GenericObjectEditor.makeCopy(m_ElementList.getSelectedValue()));
          }
          catch (Exception ex)
          {
            m_Editor.setValue(m_ElementList.getSelectedValue());
          }
          if (m_Editor.getValue() != null) {
            int x = getLocationOnScreen().x;
            int y = getLocationOnScreen().y;
            if (PropertyDialog.getParentDialog(GenericArrayEditor.this) != null) {
              m_PD = new PropertyDialog(PropertyDialog.getParentDialog(GenericArrayEditor.this), m_Editor, x, y);
            }
            else
            {
              m_PD = new PropertyDialog(PropertyDialog.getParentFrame(GenericArrayEditor.this), m_Editor, x, y);
            }
            
            m_PD.setVisible(true);
            m_ListModel.set(m_ElementList.getSelectedIndex(), m_Editor.getValue());
            m_Support.firePropertyChange("", null, null);
          }
        } else if (e.getSource() == m_UpBut) {
          JListHelper.moveUp(m_ElementList);
          m_Support.firePropertyChange("", null, null);
        } else if (e.getSource() == m_DownBut) {
          JListHelper.moveDown(m_ElementList);
          m_Support.firePropertyChange("", null, null);
        } else if (e.getSource() == m_AddBut) {
          int selected = m_ElementList.getSelectedIndex();
          Object addObj = m_ElementEditor.getValue();
          
          try
          {
            SerializedObject so = new SerializedObject(addObj);
            addObj = so.getObject();
            if (selected != -1) {
              m_ListModel.insertElementAt(addObj, selected);
            } else {
              m_ListModel.addElement(addObj);
            }
            m_Support.firePropertyChange("", null, null);
          } catch (Exception ex) {
            Messages.getInstance();JOptionPane.showMessageDialog(GenericArrayEditor.this, Messages.getString("GenericArrayEditor_InnerActionListener_JOptionPaneShowMessageDialog_Text"), null, 0);

          }
          
        }
        
      }
      

    };
    m_InnerSelectionListener = new ListSelectionListener()
    {

      public void valueChanged(ListSelectionEvent e)
      {
        if (e.getSource() == m_ElementList)
        {
          if (m_ElementList.getSelectedIndex() != -1) {
            m_DeleteBut.setEnabled(true);
            m_EditBut.setEnabled(m_ElementList.getSelectedIndices().length == 1);
            m_UpBut.setEnabled(JListHelper.canMoveUp(m_ElementList));
            m_DownBut.setEnabled(JListHelper.canMoveDown(m_ElementList));
          }
          else
          {
            m_DeleteBut.setEnabled(false);
            m_EditBut.setEnabled(false);
            m_UpBut.setEnabled(false);
            m_DownBut.setEnabled(false);
          }
          
        }
        
      }
    };
    m_InnerMouseListener = new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if ((e.getSource() == m_ElementList) && 
          (e.getClickCount() == 2))
        {



          int index = m_ElementList.locationToIndex(e.getPoint());
          if (index > -1) {
            m_InnerActionListener.actionPerformed(new ActionEvent(m_EditBut, 0, ""));

          }
          

        }
        

      }
      


    };
    setLayout(new BorderLayout());
    add(m_Label, "Center");
    m_DeleteBut.addActionListener(m_InnerActionListener);
    m_EditBut.addActionListener(m_InnerActionListener);
    m_UpBut.addActionListener(m_InnerActionListener);
    m_DownBut.addActionListener(m_InnerActionListener);
    m_AddBut.addActionListener(m_InnerActionListener);
    m_ElementList.addListSelectionListener(m_InnerSelectionListener);
    m_ElementList.addMouseListener(m_InnerMouseListener);
    Messages.getInstance();m_AddBut.setToolTipText(Messages.getString("GenericArrayEditor_AddBut_SetToolTipText_Text"));
    Messages.getInstance();m_DeleteBut.setToolTipText(Messages.getString("GenericArrayEditor_DeleteBut_SetToolTipText_Text"));
    Messages.getInstance();m_EditBut.setToolTipText(Messages.getString("GenericArrayEditor_EditBut_SetToolTipText_Text"));
    Messages.getInstance();m_UpBut.setToolTipText(Messages.getString("GenericArrayEditor_UpBut_SetToolTipText_Text"));
    Messages.getInstance();m_DownBut.setToolTipText(Messages.getString("GenericArrayEditor_DownBut_SetToolTipText_Text"));
  }
  

  private JList m_ElementList;
  
  private Class m_ElementClass;
  
  private DefaultListModel m_ListModel;
  
  private PropertyEditor m_ElementEditor;
  private JButton m_DeleteBut;
  private JButton m_EditBut;
  private class EditorListCellRenderer
    implements ListCellRenderer
  {
    private Class m_EditorClass;
    private Class m_ValueClass;
    
    public EditorListCellRenderer(Class editorClass, Class valueClass)
    {
      m_EditorClass = editorClass;
      m_ValueClass = valueClass;
    }
    












    public Component getListCellRendererComponent(final JList list, Object value, int index, final boolean isSelected, boolean cellHasFocus)
    {
      try
      {
        final PropertyEditor e = (PropertyEditor)m_EditorClass.newInstance();
        if ((e instanceof GenericObjectEditor))
        {
          ((GenericObjectEditor)e).setClassType(m_ValueClass);
        }
        e.setValue(value);
        new JPanel()
        {
          private static final long serialVersionUID = -3124434678426673334L;
          
          public void paintComponent(Graphics g)
          {
            Insets i = getInsets();
            Rectangle box = new Rectangle(left, top, getWidth() - right, getHeight() - bottom);
            

            g.setColor(isSelected ? list.getSelectionBackground() : list.getBackground());
            

            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(isSelected ? list.getSelectionForeground() : list.getForeground());
            

            e.paintValue(g, box);
          }
          
          public Dimension getPreferredSize()
          {
            Font f = getFont();
            FontMetrics fm = getFontMetrics(f);
            return new Dimension(0, fm.getHeight());
          }
        };
      } catch (Exception ex) {}
      return null;
    }
  }
  
  private JButton m_UpBut;
  private JButton m_DownBut;
  private JButton m_AddBut;
  private PropertyEditor m_Editor;
  private PropertyDialog m_PD;
  private ActionListener m_InnerActionListener;
  private ListSelectionListener m_InnerSelectionListener;
  private MouseListener m_InnerMouseListener;
  private void updateEditorType(Object o)
  {
    m_ElementEditor = null;m_ListModel = null;
    removeAll();
    if ((o != null) && (o.getClass().isArray())) {
      Class elementClass = o.getClass().getComponentType();
      PropertyEditor editor = PropertyEditorManager.findEditor(elementClass);
      Component view = null;
      ListCellRenderer lcr = new DefaultListCellRenderer();
      if (editor != null) {
        if ((editor instanceof GenericObjectEditor)) {
          ((GenericObjectEditor)editor).setClassType(elementClass);
        }
        




        if (Array.getLength(o) > 0) {
          editor.setValue(makeCopy(Array.get(o, 0)));
        }
        else if ((editor instanceof GenericObjectEditor)) {
          ((GenericObjectEditor)editor).setDefaultValue();
        } else {
          try {
            editor.setValue(elementClass.newInstance());
          } catch (Exception ex) {
            m_ElementEditor = null;
            System.err.println(ex.getMessage());
            add(m_Label, "Center");
            m_Support.firePropertyChange("", null, null);
            validate();
            return;
          }
        }
        

        if ((editor.isPaintable()) && (editor.supportsCustomEditor())) {
          view = new PropertyPanel(editor);
          lcr = new EditorListCellRenderer(editor.getClass(), elementClass);
        } else if (editor.getTags() != null) {
          view = new PropertyValueSelector(editor);
        } else if (editor.getAsText() != null) {
          view = new PropertyText(editor);
        }
      }
      if (view == null) {
        Messages.getInstance();System.err.println(Messages.getString("GenericArrayEditor_UpdateEditorType_Error_Text") + elementClass.getName());
      }
      else {
        m_ElementEditor = editor;
        

        m_ListModel = new DefaultListModel();
        m_ElementClass = elementClass;
        for (int i = 0; i < Array.getLength(o); i++) {
          m_ListModel.addElement(Array.get(o, i));
        }
        m_ElementList.setCellRenderer(lcr);
        m_ElementList.setModel(m_ListModel);
        if (m_ListModel.getSize() > 0) {
          m_ElementList.setSelectedIndex(0);
        } else {
          m_DeleteBut.setEnabled(false);
          m_EditBut.setEnabled(false);
        }
        m_UpBut.setEnabled(JListHelper.canMoveDown(m_ElementList));
        m_DownBut.setEnabled(JListHelper.canMoveDown(m_ElementList));
        












        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(view, "Center");
        panel.add(m_AddBut, "East");
        add(panel, "North");
        add(new JScrollPane(m_ElementList), "Center");
        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(1, 4));
        panel2.add(m_DeleteBut);
        panel2.add(m_EditBut);
        panel2.add(m_UpBut);
        panel2.add(m_DownBut);
        add(panel2, "South");
        m_ElementEditor.addPropertyChangeListener(new PropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent e) {
            repaint();
          }
        });
      }
    }
    



    if (m_ElementEditor == null) {
      add(m_Label, "Center");
    }
    m_Support.firePropertyChange("", null, null);
    validate();
  }
  






  public void setValue(Object o)
  {
    updateEditorType(o);
  }
  





  public Object getValue()
  {
    if (m_ListModel == null) {
      return null;
    }
    
    int length = m_ListModel.getSize();
    Object result = Array.newInstance(m_ElementClass, length);
    for (int i = 0; i < length; i++) {
      Array.set(result, i, m_ListModel.elementAt(i));
    }
    return result;
  }
  








  public String getJavaInitializationString()
  {
    return "null";
  }
  





  public boolean isPaintable()
  {
    return true;
  }
  






  public void paintValue(Graphics gfx, Rectangle box)
  {
    FontMetrics fm = gfx.getFontMetrics();
    int vpad = (height - fm.getHeight()) / 2;
    String rep = m_ListModel.getSize() + " " + m_ElementClass.getName();
    gfx.drawString(rep, 2, fm.getAscent() + vpad + 2);
  }
  




  public String getAsText()
  {
    return null;
  }
  






  public void setAsText(String text)
  {
    throw new IllegalArgumentException(text);
  }
  




  public String[] getTags()
  {
    return null;
  }
  




  public boolean supportsCustomEditor()
  {
    return true;
  }
  




  public Component getCustomEditor()
  {
    return this;
  }
  




  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
  



  public static Object makeCopy(Object source)
  {
    Object result;
    

    try
    {
      result = GenericObjectEditor.makeCopy(source);
    }
    catch (Exception e) {
      result = null;
    }
    
    return result;
  }
  




  public static void main(String[] args)
  {
    try
    {
      GenericObjectEditor.registerEditors();
      
      GenericArrayEditor ce = new GenericArrayEditor();
      
      Filter[] initial = new Filter[0];
      









      PropertyDialog pd = new PropertyDialog((Frame)null, ce, 100, 100);
      pd.setSize(200, 200);
      pd.addWindowListener(new WindowAdapter() {
        private static final long serialVersionUID = -3124434678426673334L;
        
        public void windowClosing(WindowEvent e) { System.exit(0);
        }
      });
      ce.setValue(initial);
      pd.setVisible(true);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
