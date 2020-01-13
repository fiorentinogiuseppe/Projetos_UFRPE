package weka.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URL;










































































public class SplashWindow
  extends Window
{
  private static final long serialVersionUID = -2685134277041307795L;
  private static SplashWindow m_instance;
  private Image image;
  private boolean paintCalled = false;
  




  private SplashWindow(Frame parent, Image image)
  {
    super(parent);
    this.image = image;
    

    MediaTracker mt = new MediaTracker(this);
    mt.addImage(image, 0);
    try {
      mt.waitForID(0);
    }
    catch (InterruptedException ie) {}
    

    int imgWidth = image.getWidth(this);
    int imgHeight = image.getHeight(this);
    setSize(imgWidth, imgHeight);
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((width - imgWidth) / 2, (height - imgHeight) / 2);
    







    MouseAdapter disposeOnClick = new MouseAdapter()
    {


      public void mouseClicked(MouseEvent evt)
      {

        synchronized (SplashWindow.this) {
          paintCalled = true;
          notifyAll();
        }
        dispose();
      }
    };
    addMouseListener(disposeOnClick);
  }
  







  public void update(Graphics g)
  {
    paint(g);
  }
  


  public void paint(Graphics g)
  {
    g.drawImage(image, 0, 0, this);
    



    if (!paintCalled) {
      paintCalled = true;
      synchronized (this) { notifyAll();
      }
    }
  }
  


  public static void splash(Image image)
  {
    if ((m_instance == null) && (image != null)) {
      Frame f = new Frame();
      

      m_instance = new SplashWindow(f, image);
      

      m_instance.show();
      





      if ((!EventQueue.isDispatchThread()) && (Runtime.getRuntime().availableProcessors() == 1))
      {
        synchronized (m_instance) {
          while (!m_instancepaintCalled) {
            try { m_instance.wait();
            }
            catch (InterruptedException e) {}
          }
        }
      }
    }
  }
  

  public static void splash(URL imageURL)
  {
    if (imageURL != null) {
      splash(Toolkit.getDefaultToolkit().createImage(imageURL));
    }
  }
  


  public static void disposeSplash()
  {
    if (m_instance != null) {
      m_instance.getOwner().dispose();
      m_instance = null;
    }
  }
  





  public static void invokeMethod(String className, String methodName, String[] args)
  {
    try
    {
      Class.forName(className).getMethod(methodName, new Class[] { [Ljava.lang.String.class }).invoke(null, new Object[] { args });
    }
    catch (Exception e)
    {
      Messages.getInstance();InternalError error = new InternalError(Messages.getString("SplashWindow_InvokeMethod_Exception_InternalError_Text") + methodName);
      
      error.initCause(e);
      throw error;
    }
  }
  



  public static void invokeMain(String className, String[] args)
  {
    try
    {
      Class.forName(className).getMethod("main", new Class[] { [Ljava.lang.String.class }).invoke(null, new Object[] { args });
    }
    catch (Exception e)
    {
      Messages.getInstance();InternalError error = new InternalError(Messages.getString("SplashWindow_InvokeMain_Exception_InternalError_Text"));
      error.initCause(e);
      throw error;
    }
  }
}
