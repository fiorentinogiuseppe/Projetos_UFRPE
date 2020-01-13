package weka.gui;

import java.beans.PropertyEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;






































class PropertyValueSelector
  extends JComboBox
{
  private static final long serialVersionUID = 128041237745933212L;
  PropertyEditor m_Editor;
  
  public PropertyValueSelector(PropertyEditor pe)
  {
    m_Editor = pe;
    Object value = m_Editor.getAsText();
    String[] tags = m_Editor.getTags();
    ComboBoxModel model = new DefaultComboBoxModel(tags) {
      private static final long serialVersionUID = 7942587653040180213L;
      
      public Object getSelectedItem() {
        return m_Editor.getAsText();
      }
      
      public void setSelectedItem(Object o) { m_Editor.setAsText((String)o);
      }
    };
    setModel(model);
    setSelectedItem(value);
  }
}
