package weka.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;








































public class CheckBoxList
  extends JList
{
  private static final long serialVersionUID = -4359573373359270258L;
  
  protected class CheckBoxListItem
  {
    private boolean m_Checked = false;
    

    private Object m_Content = null;
    




    public CheckBoxListItem(Object o)
    {
      this(o, false);
    }
    






    public CheckBoxListItem(Object o, boolean checked)
    {
      m_Checked = checked;
      m_Content = o;
    }
    


    public Object getContent()
    {
      return m_Content;
    }
    


    public void setChecked(boolean value)
    {
      m_Checked = value;
    }
    


    public boolean getChecked()
    {
      return m_Checked;
    }
    


    public String toString()
    {
      return m_Content.toString();
    }
    






    public boolean equals(Object o)
    {
      if (!(o instanceof CheckBoxListItem)) {
        Messages.getInstance();throw new IllegalArgumentException(Messages.getString("CheckBoxList_Equals_IllegalArgumentException_Text"));
      }
      return getContent().equals(((CheckBoxListItem)o).getContent());
    }
  }
  






  public class CheckBoxListModel
    extends DefaultListModel
  {
    private static final long serialVersionUID = 7772455499540273507L;
    






    public CheckBoxListModel() {}
    





    public CheckBoxListModel(Object[] listData)
    {
      for (int i = 0; i < listData.length; i++) {
        addElement(listData[i]);
      }
    }
    


    public CheckBoxListModel(Vector listData)
    {
      for (int i = 0; i < listData.size(); i++) {
        addElement(listData.get(i));
      }
    }
    




    public void add(int index, Object element)
    {
      if (!(element instanceof CheckBoxList.CheckBoxListItem)) {
        super.add(index, new CheckBoxList.CheckBoxListItem(CheckBoxList.this, element));
      } else {
        super.add(index, element);
      }
    }
    



    public void addElement(Object obj)
    {
      if (!(obj instanceof CheckBoxList.CheckBoxListItem)) {
        super.addElement(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, obj));
      } else {
        super.addElement(obj);
      }
    }
    




    public boolean contains(Object elem)
    {
      if (!(elem instanceof CheckBoxList.CheckBoxListItem)) {
        return super.contains(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, elem));
      }
      return super.contains(elem);
    }
    





    public void copyInto(Object[] anArray)
    {
      if (anArray.length < getSize()) {
        Messages.getInstance();throw new IndexOutOfBoundsException(Messages.getString("CheckBoxList_CopyInto_IndexOutOfBoundsException_Text"));
      }
      for (int i = 0; i < getSize(); i++) {
        anArray[i] = ((CheckBoxList.CheckBoxListItem)getElementAt(i)).getContent();
      }
    }
    







    public Object elementAt(int index)
    {
      return ((CheckBoxList.CheckBoxListItem)super.elementAt(index)).getContent();
    }
    






    public Object firstElement()
    {
      return ((CheckBoxList.CheckBoxListItem)super.firstElement()).getContent();
    }
    





    public Object get(int index)
    {
      return ((CheckBoxList.CheckBoxListItem)super.get(index)).getContent();
    }
    






    public Object getElementAt(int index)
    {
      return ((CheckBoxList.CheckBoxListItem)super.getElementAt(index)).getContent();
    }
    






    public int indexOf(Object elem)
    {
      if (!(elem instanceof CheckBoxList.CheckBoxListItem)) {
        return super.indexOf(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, elem));
      }
      return super.indexOf(elem);
    }
    







    public int indexOf(Object elem, int index)
    {
      if (!(elem instanceof CheckBoxList.CheckBoxListItem)) {
        return super.indexOf(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, elem), index);
      }
      return super.indexOf(elem, index);
    }
    







    public void insertElementAt(Object obj, int index)
    {
      if (!(obj instanceof CheckBoxList.CheckBoxListItem)) {
        super.insertElementAt(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, obj), index);
      } else {
        super.insertElementAt(obj, index);
      }
    }
    





    public Object lastElement()
    {
      return ((CheckBoxList.CheckBoxListItem)super.lastElement()).getContent();
    }
    






    public int lastIndexOf(Object elem)
    {
      if (!(elem instanceof CheckBoxList.CheckBoxListItem)) {
        return super.lastIndexOf(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, elem));
      }
      return super.lastIndexOf(elem);
    }
    









    public int lastIndexOf(Object elem, int index)
    {
      if (!(elem instanceof CheckBoxList.CheckBoxListItem)) {
        return super.lastIndexOf(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, elem), index);
      }
      return super.lastIndexOf(elem, index);
    }
    






    public Object remove(int index)
    {
      return ((CheckBoxList.CheckBoxListItem)super.remove(index)).getContent();
    }
    







    public boolean removeElement(Object obj)
    {
      if (!(obj instanceof CheckBoxList.CheckBoxListItem)) {
        return super.removeElement(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, obj));
      }
      return super.removeElement(obj);
    }
    







    public Object set(int index, Object element)
    {
      if (!(element instanceof CheckBoxList.CheckBoxListItem)) {
        return ((CheckBoxList.CheckBoxListItem)super.set(index, new CheckBoxList.CheckBoxListItem(CheckBoxList.this, element))).getContent();
      }
      return ((CheckBoxList.CheckBoxListItem)super.set(index, element)).getContent();
    }
    







    public void setElementAt(Object obj, int index)
    {
      if (!(obj instanceof CheckBoxList.CheckBoxListItem)) {
        super.setElementAt(new CheckBoxList.CheckBoxListItem(CheckBoxList.this, obj), index);
      } else {
        super.setElementAt(obj, index);
      }
    }
    








    public Object[] toArray()
    {
      Object[] internal = super.toArray();
      Object[] result = new Object[internal.length];
      
      for (int i = 0; i < internal.length; i++) {
        result[i] = ((CheckBoxList.CheckBoxListItem)internal[i]).getContent();
      }
      return result;
    }
    





    public boolean getChecked(int index)
    {
      return ((CheckBoxList.CheckBoxListItem)super.getElementAt(index)).getChecked();
    }
    





    public void setChecked(int index, boolean checked)
    {
      ((CheckBoxList.CheckBoxListItem)super.getElementAt(index)).setChecked(checked);
    }
  }
  








  public class CheckBoxListRenderer
    extends JCheckBox
    implements ListCellRenderer
  {
    private static final long serialVersionUID = 1059591605858524586L;
    








    public CheckBoxListRenderer() {}
    







    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      setText(value.toString());
      setSelected(((CheckBoxList)list).getChecked(index));
      setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
      setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
      setFocusPainted(false);
      
      return this;
    }
  }
  


  public CheckBoxList()
  {
    this(null);
  }
  






  public CheckBoxList(CheckBoxListModel model)
  {
    if (model == null) {
      model = new CheckBoxListModel();
    }
    setModel(model);
    setCellRenderer(new CheckBoxListRenderer());
    
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        
        if (index != -1) {
          setChecked(index, !getChecked(index));
          repaint();
        }
        
      }
    });
    addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if ((e.getKeyChar() == ' ') && (e.getModifiers() == 0)) {
          int index = getSelectedIndex();
          setChecked(index, !getChecked(index));
          e.consume();
          repaint();
        }
      }
    });
  }
  







  public void setModel(ListModel model)
  {
    if (!(model instanceof CheckBoxListModel)) {
      Messages.getInstance();throw new IllegalArgumentException(Messages.getString("CheckBoxList_SetModel_IllegalArgumentException_Text"));
    }
    super.setModel(model);
  }
  





  public void setListData(Object[] listData)
  {
    setModel(new CheckBoxListModel(listData));
  }
  



  public void setListData(Vector listData)
  {
    setModel(new CheckBoxListModel(listData));
  }
  





  public boolean getChecked(int index)
  {
    return ((CheckBoxListModel)getModel()).getChecked(index);
  }
  





  public void setChecked(int index, boolean checked)
  {
    ((CheckBoxListModel)getModel()).setChecked(index, checked);
  }
  









  public int[] getCheckedIndices()
  {
    Vector list = new Vector();
    for (int i = 0; i < getModel().getSize(); i++) {
      if (getChecked(i)) {
        list.add(new Integer(i));
      }
    }
    
    int[] result = new int[list.size()];
    for (i = 0; i < list.size(); i++) {
      result[i] = ((Integer)list.get(i)).intValue();
    }
    
    return result;
  }
}
