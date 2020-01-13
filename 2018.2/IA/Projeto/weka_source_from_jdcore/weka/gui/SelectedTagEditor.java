package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditorSupport;
import java.io.PrintStream;
import javax.swing.JFrame;
import weka.core.SelectedTag;
import weka.core.Tag;



































public class SelectedTagEditor
  extends PropertyEditorSupport
{
  public SelectedTagEditor() {}
  
  public String getJavaInitializationString()
  {
    SelectedTag s = (SelectedTag)getValue();
    Tag[] tags = s.getTags();
    String result = "new SelectedTag(" + s.getSelectedTag().getID() + ", {\n";
    

    for (int i = 0; i < tags.length; i++) {
      result = result + "new Tag(" + tags[i].getID() + ",\"" + tags[i].getReadable() + "\")";
      

      if (i < tags.length - 1) {
        result = result + ',';
      }
      result = result + '\n';
    }
    return result + "})";
  }
  





  public String getAsText()
  {
    SelectedTag s = (SelectedTag)getValue();
    return s.getSelectedTag().getReadable();
  }
  







  public void setAsText(String text)
  {
    SelectedTag s = (SelectedTag)getValue();
    Tag[] tags = s.getTags();
    try {
      for (int i = 0; i < tags.length; i++) {
        if (text.equals(tags[i].getReadable())) {
          setValue(new SelectedTag(tags[i].getID(), tags));
          return;
        }
      }
    } catch (Exception ex) {
      throw new IllegalArgumentException(text);
    }
  }
  





  public String[] getTags()
  {
    SelectedTag s = (SelectedTag)getValue();
    Tag[] tags = s.getTags();
    String[] result = new String[tags.length];
    for (int i = 0; i < tags.length; i++) {
      result[i] = tags[i].getReadable();
    }
    return result;
  }
  




  public static void main(String[] args)
  {
    try
    {
      GenericObjectEditor.registerEditors(); Tag[] 
        tmp7_4 = new Tag[5];Messages.getInstance();tmp7_4[0] = new Tag(1, Messages.getString("SelectedTagEditor_Main_Tags_Text_First")); Tag[] tmp27_7 = tmp7_4;Messages.getInstance();tmp27_7[1] = new Tag(2, Messages.getString("SelectedTagEditor_Main_Tags_Text_Second")); Tag[] tmp47_27 = tmp27_7;Messages.getInstance();tmp47_27[2] = new Tag(3, Messages.getString("SelectedTagEditor_Main_Tags_Text_Third")); Tag[] tmp67_47 = tmp47_27;Messages.getInstance();tmp67_47[3] = new Tag(4, Messages.getString("SelectedTagEditor_Main_Tags_Text_Fourth")); Tag[] tmp87_67 = tmp67_47;Messages.getInstance();tmp87_67[4] = new Tag(5, Messages.getString("SelectedTagEditor_Main_Tags_Text_Fifth"));Tag[] tags = tmp87_67;
      





      SelectedTag initial = new SelectedTag(1, tags);
      SelectedTagEditor ce = new SelectedTagEditor();
      ce.setValue(initial);
      PropertyValueSelector ps = new PropertyValueSelector(ce);
      JFrame f = new JFrame();
      f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
      f.getContentPane().setLayout(new BorderLayout());
      f.getContentPane().add(ps, "Center");
      f.pack();
      f.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
