package weka.gui;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

public abstract interface MainMenuExtension
{
  public abstract String getSubmenuTitle();
  
  public abstract String getMenuTitle();
  
  public abstract ActionListener getActionListener(JFrame paramJFrame);
  
  public abstract void fillFrame(Component paramComponent);
}
