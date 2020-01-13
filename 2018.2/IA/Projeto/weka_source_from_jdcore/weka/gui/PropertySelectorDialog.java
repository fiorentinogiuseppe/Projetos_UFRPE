package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import weka.experiment.AveragingResultProducer;
import weka.experiment.PropertyNode;























































public class PropertySelectorDialog
  extends JDialog
{
  private static final long serialVersionUID = -3155058124137930518L;
  protected JButton m_SelectBut;
  protected JButton m_CancelBut;
  protected DefaultMutableTreeNode m_Root;
  protected Object m_RootObject;
  protected int m_Result;
  protected Object[] m_ResultPath;
  protected JTree m_Tree;
  public static final int APPROVE_OPTION = 0;
  public static final int CANCEL_OPTION = 1;
  
  public PropertySelectorDialog(Frame parentFrame, Object rootObject)
  {
    super(parentFrame, Messages.getString("PropertySelectorDialog_Text"), Dialog.ModalityType.DOCUMENT_MODAL);Messages.getInstance();m_SelectBut = new JButton(Messages.getString("PropertySelectorDialog_SelectBut_JButton_Text"));Messages.getInstance();m_CancelBut = new JButton(Messages.getString("PropertySelectorDialog_CancelBut_JButton_Text"));
    m_CancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_Result = 1;
        setVisible(false);
      }
    });
    m_SelectBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        TreePath tPath = m_Tree.getSelectionPath();
        if (tPath == null) {
          m_Result = 1;
        } else {
          m_ResultPath = tPath.getPath();
          if ((m_ResultPath == null) || (m_ResultPath.length < 2)) {
            m_Result = 1;
          } else {
            m_Result = 0;
          }
        }
        setVisible(false);
      }
    });
    m_RootObject = rootObject;
    m_Root = new DefaultMutableTreeNode(new PropertyNode(m_RootObject));
    
    createNodes(m_Root);
    
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
    
    Box b1 = new Box(0);
    b1.add(m_SelectBut);
    b1.add(Box.createHorizontalStrut(10));
    b1.add(m_CancelBut);
    c.add(b1, "South");
    m_Tree = new JTree(m_Root);
    m_Tree.getSelectionModel().setSelectionMode(1);
    
    c.add(new JScrollPane(m_Tree), "Center");
    pack();
  }
  





  public int showDialog()
  {
    m_Result = 1;
    setVisible(true);
    return m_Result;
  }
  





  public PropertyNode[] getPath()
  {
    PropertyNode[] result = new PropertyNode[m_ResultPath.length - 1];
    for (int i = 0; i < result.length; i++) {
      result[i] = ((PropertyNode)((DefaultMutableTreeNode)m_ResultPath[(i + 1)]).getUserObject());
    }
    
    return result;
  }
  





  protected void createNodes(DefaultMutableTreeNode localNode)
  {
    PropertyNode pNode = (PropertyNode)localNode.getUserObject();
    Object localObject = value;
    PropertyDescriptor[] localProperties;
    try
    {
      BeanInfo bi = Introspector.getBeanInfo(localObject.getClass());
      localProperties = bi.getPropertyDescriptors();
    } catch (IntrospectionException ex) {
      Messages.getInstance();System.err.println(Messages.getString("PropertySelectorDialog_CreateNodes_Error_Text_First"));
      return;
    }
    

    for (int i = 0; i < localProperties.length; i++)
    {
      if ((!localProperties[i].isHidden()) && (!localProperties[i].isExpert()))
      {

        String name = localProperties[i].getDisplayName();
        Class type = localProperties[i].getPropertyType();
        Method getter = localProperties[i].getReadMethod();
        Method setter = localProperties[i].getWriteMethod();
        Object value = null;
        
        if ((getter != null) && (setter != null))
        {
          try
          {
            Object[] args = new Object[0];
            value = getter.invoke(localObject, args);
            PropertyEditor editor = null;
            Class pec = localProperties[i].getPropertyEditorClass();
            if (pec != null) {
              try {
                editor = (PropertyEditor)pec.newInstance();
              }
              catch (Exception ex) {}
            }
            if (editor == null) {
              editor = PropertyEditorManager.findEditor(type);
            }
            if ((editor == null) || (value == null)) {
              continue;
            }
          } catch (InvocationTargetException ex) {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("PropertySelectorDialog_CreateNodes_Error_Text_Second") + name + Messages.getString("PropertySelectorDialog_CreateNodes_Error_Text_Third") + ex.getTargetException());
            

            ex.getTargetException().printStackTrace();
            continue;
          } catch (Exception ex) {
            Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("PropertySelectorDialog_CreateNodes_Error_Text_Fourth") + name + Messages.getString("PropertySelectorDialog_CreateNodes_Error_Text_Fifth") + ex);
            
            ex.printStackTrace();
            continue;
          }
          
          DefaultMutableTreeNode child = new DefaultMutableTreeNode(new PropertyNode(value, localProperties[i], localObject.getClass()));
          


          localNode.add(child);
          createNodes(child);
        }
      }
    }
  }
  



  public static void main(String[] args)
  {
    try
    {
      GenericObjectEditor.registerEditors();
      
      Object rp = new AveragingResultProducer();
      
      PropertySelectorDialog jd = new PropertySelectorDialog(null, rp);
      int result = jd.showDialog();
      if (result == 0) {
        Messages.getInstance();System.err.println(Messages.getString("PropertySelectorDialog_Main_Error_Text_First"));
        PropertyNode[] path = jd.getPath();
        for (int i = 0; i < path.length; i++) {
          PropertyNode pn = path[i];
          System.err.println("" + (i + 1) + "  " + pn.toString() + " " + value.toString());
        }
      }
      else {
        Messages.getInstance();System.err.println(Messages.getString("PropertySelectorDialog_Main_Error_Text_Second"));
      }
      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
