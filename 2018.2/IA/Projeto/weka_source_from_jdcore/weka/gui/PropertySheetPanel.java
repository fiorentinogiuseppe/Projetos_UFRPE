package weka.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.MultiInstanceCapabilitiesHandler;

























public class PropertySheetPanel
  extends JPanel
  implements PropertyChangeListener
{
  private static final long serialVersionUID = -8939835593429918345L;
  private Object m_Target;
  private PropertyDescriptor[] m_Properties;
  private MethodDescriptor[] m_Methods;
  private PropertyEditor[] m_Editors;
  private Object[] m_Values;
  private JComponent[] m_Views;
  private JLabel[] m_Labels;
  private String[] m_TipTexts;
  private StringBuffer m_HelpText;
  private JDialog m_HelpDialog;
  private CapabilitiesHelpDialog m_CapabilitiesDialog;
  private JButton m_HelpBut;
  private JButton m_CapabilitiesBut;
  private JTextArea m_CapabilitiesText;
  
  protected class CapabilitiesHelpDialog
    extends JDialog
    implements PropertyChangeListener
  {
    private static final long serialVersionUID = -1404770987103289858L;
    private CapabilitiesHelpDialog m_Self;
    
    public CapabilitiesHelpDialog(Frame owner)
    {
      super();
      
      initialize();
    }
    




    public CapabilitiesHelpDialog(Dialog owner)
    {
      super();
      
      initialize();
    }
    


    protected void initialize()
    {
      Messages.getInstance();setTitle(Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_Initialize_SetTitle_Text"));
      

      m_Self = this;
      
      m_CapabilitiesText = new JTextArea();
      m_CapabilitiesText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      m_CapabilitiesText.setLineWrap(true);
      m_CapabilitiesText.setWrapStyleWord(true);
      m_CapabilitiesText.setEditable(false);
      updateText();
      addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          m_Self.dispose();
          if (m_CapabilitiesDialog == m_Self) {
            m_CapabilitiesBut.setEnabled(true);
          }
        }
      });
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(new JScrollPane(m_CapabilitiesText), "Center");
      
      pack();
    }
    


    protected void updateText()
    {
      StringBuffer helpText = new StringBuffer();
      
      if ((m_Target instanceof CapabilitiesHandler)) {
        helpText.append(PropertySheetPanel.addCapabilities("CAPABILITIES", ((CapabilitiesHandler)m_Target).getCapabilities()));
      }
      

      if ((m_Target instanceof MultiInstanceCapabilitiesHandler)) {
        helpText.append(PropertySheetPanel.addCapabilities("MI CAPABILITIES", ((MultiInstanceCapabilitiesHandler)m_Target).getMultiInstanceCapabilities()));
      }
      


      m_CapabilitiesText.setText(helpText.toString());
      m_CapabilitiesText.setCaretPosition(0);
    }
    





    public void propertyChange(PropertyChangeEvent evt)
    {
      updateText();
    }
  }
  








  public static String listCapabilities(Capabilities c)
  {
    String result = "";
    Iterator iter = c.capabilities();
    while (iter.hasNext()) {
      if (result.length() != 0) {
        result = result + ", ";
      }
      result = result + iter.next().toString();
    }
    
    return result;
  }
  










  public static String addCapabilities(String title, Capabilities c)
  {
    String result = title + "\n";
    

    String caps = listCapabilities(c.getClassCapabilities());
    if (caps.length() != 0) {
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_First");
      


      result = result + caps;
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Second");
    }
    




    caps = listCapabilities(c.getAttributeCapabilities());
    if (caps.length() != 0) {
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Third");
      


      result = result + caps;
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Fourth");
    }
    




    caps = listCapabilities(c.getOtherCapabilities());
    if (caps.length() != 0) {
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Fifth");
      


      result = result + caps;
      Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Sixth");
    }
    




    Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Seventh");
    


    Messages.getInstance();result = result + Messages.getString("PropertySheetPanel_CapabilitiesHelpDialog_AddCapabilities_SetTitle_Text_Eighth") + c.getMinimumNumberInstances() + "\n";
    



    result = result + "\n";
    
    return result;
  }
  











































  private int m_NumEditable = 0;
  




  private JPanel m_aboutPanel;
  




  public PropertySheetPanel()
  {
    setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
  }
  






  public JPanel getAboutPanel()
  {
    return m_aboutPanel;
  }
  

  private final PropertyChangeSupport support = new PropertyChangeSupport(this);
  






  public void propertyChange(PropertyChangeEvent evt)
  {
    wasModified(evt);
    support.firePropertyChange("", null, null);
  }
  





  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    support.addPropertyChangeListener(l);
  }
  





  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    support.removePropertyChangeListener(l);
  }
  







  public synchronized void setTarget(Object targ)
  {
    int componentOffset = 0;
    

    removeAll();
    
    setLayout(new BorderLayout());
    JPanel scrollablePanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(scrollablePanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, "Center");
    
    GridBagLayout gbLayout = new GridBagLayout();
    
    scrollablePanel.setLayout(gbLayout);
    setVisible(false);
    m_NumEditable = 0;
    m_Target = targ;
    try {
      BeanInfo bi = Introspector.getBeanInfo(m_Target.getClass());
      m_Properties = bi.getPropertyDescriptors();
      m_Methods = bi.getMethodDescriptors();
    } catch (IntrospectionException ex) {
      Messages.getInstance();System.err.println(Messages.getString("PropertySheetPanel_SetTarget_IntrospectionException_Error_Text"));
      
      return;
    }
    
    JTextArea jt = new JTextArea();
    m_HelpText = null;
    

    for (MethodDescriptor m_Method : m_Methods) {
      String name = m_Method.getDisplayName();
      Method meth = m_Method.getMethod();
      if ((name.equals("globalInfo")) && 
        (meth.getReturnType().equals(String.class))) {
        try {
          Object[] args = new Object[0];
          String globalInfo = (String)meth.invoke(m_Target, args);
          String summary = globalInfo;
          int ci = globalInfo.indexOf('.');
          if (ci != -1) {
            summary = globalInfo.substring(0, ci + 1);
          }
          String className = targ.getClass().getName();
          Messages.getInstance();m_HelpText = new StringBuffer(Messages.getString("PropertySheetPanel_SetTarget_HelpText_Text_First"));
          
          Messages.getInstance();m_HelpText.append(className).append(Messages.getString("PropertySheetPanel_SetTarget_HelpText_Text_Second"));
          

          Messages.getInstance();Messages.getInstance();m_HelpText.append(Messages.getString("PropertySheetPanel_SetTarget_HelpText_Text_Third")).append(globalInfo).append(Messages.getString("PropertySheetPanel_SetTarget_HelpText_Text_Fourth"));
          






          Messages.getInstance();m_HelpBut = new JButton(Messages.getString("PropertySheetPanel_SetTarget_HelpBut_JButton_Text"));
          
          Messages.getInstance();m_HelpBut.setToolTipText(Messages.getString("PropertySheetPanel_SetTarget_HelpBut_SetToolTipText_Text") + className);
          


          m_HelpBut.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent a) {
              openHelpFrame();
              m_HelpBut.setEnabled(false);
            }
          });
          
          if ((m_Target instanceof CapabilitiesHandler)) {
            Messages.getInstance();m_CapabilitiesBut = new JButton(Messages.getString("PropertySheetPanel_SetTarget_CapabilitiesBut_JButton_Text"));
            
            Messages.getInstance();m_CapabilitiesBut.setToolTipText(Messages.getString("PropertySheetPanel_SetTarget_CapabilitiesBut_SetToolTipText_Text") + className);
            





            m_CapabilitiesBut.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent a) {
                openCapabilitiesHelpDialog();
                m_CapabilitiesBut.setEnabled(false);
              }
            });
          } else {
            m_CapabilitiesBut = null;
          }
          
          jt.setColumns(30);
          jt.setFont(new Font("SansSerif", 0, 12));
          jt.setEditable(false);
          jt.setLineWrap(true);
          jt.setWrapStyleWord(true);
          jt.setText(summary);
          jt.setBackground(getBackground());
          JPanel jp = new JPanel();
          Messages.getInstance();jp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("PropertySheetPanel_SetTarget_Jp_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
          





          jp.setLayout(new BorderLayout());
          jp.add(jt, "Center");
          JPanel p2 = new JPanel();
          p2.setLayout(new BorderLayout());
          p2.add(m_HelpBut, "North");
          if (m_CapabilitiesBut != null) {
            JPanel p3 = new JPanel();
            p3.setLayout(new BorderLayout());
            p3.add(m_CapabilitiesBut, "North");
            p2.add(p3, "Center");
          }
          jp.add(p2, "East");
          GridBagConstraints gbConstraints = new GridBagConstraints();
          
          fill = 1;
          
          gridwidth = 2;
          insets = new Insets(0, 5, 0, 5);
          gbLayout.setConstraints(jp, gbConstraints);
          m_aboutPanel = jp;
          scrollablePanel.add(m_aboutPanel);
          componentOffset = 1;
        }
        catch (Exception ex) {}
      }
    }
    



    m_Editors = new PropertyEditor[m_Properties.length];
    m_Values = new Object[m_Properties.length];
    m_Views = new JComponent[m_Properties.length];
    m_Labels = new JLabel[m_Properties.length];
    m_TipTexts = new String[m_Properties.length];
    boolean firstTip = true;
    for (int i = 0; i < m_Properties.length; i++)
    {

      if ((!m_Properties[i].isHidden()) && (!m_Properties[i].isExpert()))
      {


        String name = m_Properties[i].getDisplayName();
        Class type = m_Properties[i].getPropertyType();
        Method getter = m_Properties[i].getReadMethod();
        Method setter = m_Properties[i].getWriteMethod();
        

        if ((getter != null) && (setter != null))
        {


          JComponent view = null;
          try
          {
            Object[] args = new Object[0];
            Object value = getter.invoke(m_Target, args);
            m_Values[i] = value;
            
            PropertyEditor editor = null;
            Class pec = m_Properties[i].getPropertyEditorClass();
            if (pec != null) {
              try {
                editor = (PropertyEditor)pec.newInstance();
              }
              catch (Exception ex) {}
            }
            
            if (editor == null) {
              editor = PropertyEditorManager.findEditor(type);
            }
            m_Editors[i] = editor;
            

            if (editor == null)
            {
              String getterClass = m_Properties[i].getReadMethod().getDeclaringClass().getName();
              






              continue;
            }
            if ((editor instanceof GenericObjectEditor)) {
              ((GenericObjectEditor)editor).setClassType(type);
            }
            

            if (value == null)
            {
              String getterClass = m_Properties[i].getReadMethod().getDeclaringClass().getName();
              





              continue;
            }
            
            editor.setValue(value);
            

            String tipName = name + "TipText";
            for (MethodDescriptor m_Method : m_Methods) {
              String mname = m_Method.getDisplayName();
              Method meth = m_Method.getMethod();
              if ((mname.equals(tipName)) && 
                (meth.getReturnType().equals(String.class))) {
                try {
                  String tempTip = (String)meth.invoke(m_Target, args);
                  int ci = tempTip.indexOf('.');
                  if (ci < 0) {
                    m_TipTexts[i] = tempTip;
                  } else {
                    m_TipTexts[i] = tempTip.substring(0, ci);
                  }
                  if (m_HelpText != null) {
                    if (firstTip) {
                      Messages.getInstance();m_HelpText.append(Messages.getString("PropertySheetPanel_SetTarget_HelpText_Text_Fifth"));
                      
                      firstTip = false;
                    }
                    m_HelpText.append(name).append(" -- ");
                    m_HelpText.append(tempTip).append("\n\n");
                  }
                }
                catch (Exception ex) {}
              }
            }
            





            if ((editor.isPaintable()) && (editor.supportsCustomEditor())) {
              view = new PropertyPanel(editor);
            } else if (editor.getTags() != null) {
              view = new PropertyValueSelector(editor);
            } else if (editor.getAsText() != null)
            {
              view = new PropertyText(editor);
            } else {
              Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("PropertySheetPanel_SetTarget_Error_Text_First") + name + Messages.getString("PropertySheetPanel_SetTarget_Error_Text_Second"));
              



              continue;
            }
            
            editor.addPropertyChangeListener(this);
          }
          catch (InvocationTargetException ex) {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("PropertySheetPanel_SetTarget_Error_Text_Third") + name + Messages.getString("PropertySheetPanel_SetTarget_Error_Text_Fourth") + ex.getTargetException());
            




            ex.getTargetException().printStackTrace();
            continue;
          } catch (Exception ex) {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("PropertySheetPanel_SetTarget_Error_Text_Fifth") + name + Messages.getString("PropertySheetPanel_SetTarget_Error_Text_Sixth") + ex);
            



            ex.printStackTrace();
            continue;
          }
          
          m_Labels[i] = new JLabel(name, 4);
          m_Labels[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
          m_Views[i] = view;
          GridBagConstraints gbConstraints = new GridBagConstraints();
          anchor = 13;
          fill = 2;
          gridy = (i + componentOffset);
          gridx = 0;
          gbLayout.setConstraints(m_Labels[i], gbConstraints);
          scrollablePanel.add(m_Labels[i]);
          JPanel newPanel = new JPanel();
          if (m_TipTexts[i] != null) {
            m_Views[i].setToolTipText(m_TipTexts[i]);
          }
          newPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
          newPanel.setLayout(new BorderLayout());
          newPanel.add(m_Views[i], "Center");
          gbConstraints = new GridBagConstraints();
          anchor = 17;
          fill = 1;
          gridy = (i + componentOffset);
          gridx = 1;
          weightx = 100.0D;
          gbLayout.setConstraints(newPanel, gbConstraints);
          scrollablePanel.add(newPanel);
          m_NumEditable += 1;
        }
      } }
    if (m_NumEditable == 0) {
      Messages.getInstance();JLabel empty = new JLabel(Messages.getString("PropertySheetPanel_SetTarget_Empty_JLabel_Text"), 0);
      

      Dimension d = empty.getPreferredSize();
      empty.setPreferredSize(new Dimension(width * 2, height * 2));
      empty.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 10));
      GridBagConstraints gbConstraints = new GridBagConstraints();
      anchor = 10;
      fill = 2;
      gridy = componentOffset;
      gridx = 0;
      gbLayout.setConstraints(empty, gbConstraints);
      scrollablePanel.add(empty);
    }
    
    validate();
    setVisible(true);
  }
  



  protected void openHelpFrame()
  {
    JTextArea ta = new JTextArea();
    ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    
    ta.setEditable(false);
    ta.setText(m_HelpText.toString());
    ta.setCaretPosition(0);
    JDialog jdtmp;
    JDialog jdtmp; if (PropertyDialog.getParentDialog(this) != null) {
      Messages.getInstance();jdtmp = new JDialog(PropertyDialog.getParentDialog(this), Messages.getString("PropertySheetPanel_OpenHelpFrame_Jdtmp_JDialog_Text_First"));
    }
    else
    {
      Messages.getInstance();jdtmp = new JDialog(PropertyDialog.getParentFrame(this), Messages.getString("PropertySheetPanel_OpenHelpFrame_Jdtmp_JDialog_Text_Second"));
    }
    

    final JDialog jd = jdtmp;
    jd.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e) {
        jd.dispose();
        if (m_HelpDialog == jd) {
          m_HelpBut.setEnabled(true);
        }
      }
    });
    jd.getContentPane().setLayout(new BorderLayout());
    jd.getContentPane().add(new JScrollPane(ta), "Center");
    jd.pack();
    jd.setSize(400, 350);
    jd.setLocation(m_aboutPanel.getTopLevelAncestor().getLocationOnScreen().x + m_aboutPanel.getTopLevelAncestor().getSize().width, m_aboutPanel.getTopLevelAncestor().getLocationOnScreen().y);
    

    jd.setVisible(true);
    m_HelpDialog = jd;
  }
  


  protected void openCapabilitiesHelpDialog()
  {
    if (PropertyDialog.getParentDialog(this) != null) {
      m_CapabilitiesDialog = new CapabilitiesHelpDialog(PropertyDialog.getParentDialog(this));
    }
    else {
      m_CapabilitiesDialog = new CapabilitiesHelpDialog(PropertyDialog.getParentFrame(this));
    }
    
    m_CapabilitiesDialog.setSize(400, 350);
    m_CapabilitiesDialog.setLocation(m_aboutPanel.getTopLevelAncestor().getLocationOnScreen().x + m_aboutPanel.getTopLevelAncestor().getSize().width, m_aboutPanel.getTopLevelAncestor().getLocationOnScreen().y);
    


    m_CapabilitiesDialog.setVisible(true);
    addPropertyChangeListener(m_CapabilitiesDialog);
  }
  





  public int editableProperties()
  {
    return m_NumEditable;
  }
  







  synchronized void wasModified(PropertyChangeEvent evt)
  {
    if ((evt.getSource() instanceof PropertyEditor)) {
      PropertyEditor editor = (PropertyEditor)evt.getSource();
      for (int i = 0; i < m_Editors.length; i++) {
        if (m_Editors[i] == editor) {
          PropertyDescriptor property = m_Properties[i];
          Object value = editor.getValue();
          m_Values[i] = value;
          Method setter = property.getWriteMethod();
          try {
            Object[] args = { value };
            args[0] = value;
            setter.invoke(m_Target, args);
          } catch (InvocationTargetException ex) {
            if ((ex.getTargetException() instanceof PropertyVetoException)) {
              Messages.getInstance();String message = Messages.getString("PropertySheetPanel_WasModified_Message_Text") + ex.getTargetException().getMessage();
              

              System.err.println(message);
              Component jf;
              Component jf;
              if ((evt.getSource() instanceof JPanel)) {
                jf = ((JPanel)evt.getSource()).getParent();
              } else {
                jf = new JFrame();
              }
              Messages.getInstance();JOptionPane.showMessageDialog(jf, message, Messages.getString("PropertySheetPanel_WasModified_JOptionPaneShowMessageDialog_Text_First"), 2);
              







              if ((jf instanceof JFrame)) {
                ((JFrame)jf).dispose();
              }
            }
            else {
              Messages.getInstance();Messages.getInstance();System.err.println(ex.getTargetException().getClass().getName() + Messages.getString("PropertySheetPanel_WasModified_Error_Text_First") + property.getName() + Messages.getString("PropertySheetPanel_WasModified_Error_Text_Second") + ex.getTargetException().getMessage());
              

              Component jf;
              

              Component jf;
              
              if ((evt.getSource() instanceof JPanel)) {
                jf = ((JPanel)evt.getSource()).getParent();
              } else {
                jf = new JFrame();
              }
              Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(jf, ex.getTargetException().getClass().getName() + Messages.getString("PropertySheetPanel_WasModified_JOptionPaneShowMessageDialog_Second") + property.getName() + Messages.getString("PropertySheetPanel_WasModified_JOptionPaneShowMessageDialog_Third") + ex.getTargetException().getMessage(), Messages.getString("PropertySheetPanel_WasModified_JOptionPaneShowMessageDialog_Fourth"), 2);
              

















              if ((jf instanceof JFrame)) {
                ((JFrame)jf).dispose();
              }
            }
          }
          catch (Exception ex) {
            Messages.getInstance();System.err.println(Messages.getString("PropertySheetPanel_WasModified_JOptionPaneShowMessageDialog_Fifth") + property.getName());
          }
          




          if ((m_Views[i] == null) || (!(m_Views[i] instanceof PropertyPanel)))
            break;
          m_Views[i].repaint();
          revalidate(); break;
        }
      }
    }
    




    for (int i = 0; i < m_Properties.length; i++) {
      Object o;
      try {
        Method getter = m_Properties[i].getReadMethod();
        Method setter = m_Properties[i].getWriteMethod();
        
        if ((getter != null) && (setter == null)) {
          continue;
        }
        

        Object[] args = new Object[0];
        o = getter.invoke(m_Target, args);
      } catch (Exception ex) {
        o = null;
      }
      if ((o != m_Values[i]) && ((o == null) || (!o.equals(m_Values[i]))))
      {


        m_Values[i] = o;
        
        if (m_Editors[i] != null)
        {


          m_Editors[i].removePropertyChangeListener(this);
          m_Editors[i].setValue(o);
          m_Editors[i].addPropertyChangeListener(this);
          if (m_Views[i] != null)
          {
            m_Views[i].repaint();
          }
        }
      }
    }
    if (Beans.isInstanceOf(m_Target, Component.class)) {
      ((Component)Beans.getInstanceOf(m_Target, Component.class)).repaint();
    }
  }
}
