package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Document;
import weka.core.ClassDiscovery;
import weka.core.Trie;
import weka.core.Utils;





































public class SimpleCLIPanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = -7377739469759943231L;
  protected static String FILENAME = "SimpleCLI.props";
  

  protected static String PROPERTY_FILE = "weka/gui/" + FILENAME;
  
  protected static Properties PROPERTIES;
  
  static
  {
    try
    {
      PROPERTIES = Utils.readProperties(PROPERTY_FILE);
      Enumeration keys = PROPERTIES.propertyNames();
      
      if (!keys.hasMoreElements()) {
        Messages.getInstance();throw new Exception(Messages.getString("SimpleCLIPanel_Exception_Text_First"));
      }
    }
    catch (Exception ex)
    {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("SimpleCLIPanel_Exception_JOptionPaneShowMessageDialog_Text_First") + PROPERTY_FILE + Messages.getString("SimpleCLIPanel_Exception_JOptionPaneShowMessageDialog_Text_Second") + System.getProperties().getProperty("user.home") + Messages.getString("SimpleCLIPanel_Exception_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("SimpleCLIPanel_Exception_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
    }
  }
  









  protected JTextArea m_OutputArea = new JTextArea();
  

  protected JTextField m_Input = new JTextField();
  

  protected Vector m_CommandHistory = new Vector();
  

  protected int m_HistoryPos = 0;
  

  protected PipedOutputStream m_POO = new PipedOutputStream();
  

  protected PipedOutputStream m_POE = new PipedOutputStream();
  


  protected Thread m_OutRedirector;
  


  protected Thread m_ErrRedirector;
  


  protected Thread m_RunThread;
  


  protected CommandlineCompletion m_Completion;
  



  class ReaderToTextArea
    extends Thread
  {
    protected LineNumberReader m_Input;
    


    protected JTextArea m_Output;
    



    public ReaderToTextArea(Reader input, JTextArea output)
    {
      setDaemon(true);
      m_Input = new LineNumberReader(input);
      m_Output = output;
    }
    


    public void run()
    {
      try
      {
        for (;;)
        {
          m_Output.append(m_Input.readLine() + '\n');
          m_Output.setCaretPosition(m_Output.getDocument().getLength());
        }
      } catch (Exception ex) {
        try { sleep(100L);
        }
        catch (Exception e) {}
      }
    }
  }
  






  class ClassRunner
    extends Thread
  {
    protected Method m_MainMethod;
    





    String[] m_CommandArgs;
    





    public ClassRunner(Class theClass, String[] commandArgs)
      throws Exception
    {
      setDaemon(true);
      Class[] argTemplate = { [Ljava.lang.String.class };
      m_CommandArgs = commandArgs;
      m_MainMethod = theClass.getMethod("main", argTemplate);
      if (((m_MainMethod.getModifiers() & 0x8) == 0) || ((m_MainMethod.getModifiers() & 0x1) == 0))
      {
        Messages.getInstance();Messages.getInstance();throw new NoSuchMethodException(Messages.getString("SimpleCLIPanel_ClassRunner_Exception_NoSuchMethodException_Text_First") + theClass.getName() + Messages.getString("SimpleCLIPanel_ClassRunner_Exception_NoSuchMethodException_Text_Second"));
      }
    }
    




    public void run()
    {
      PrintStream outOld = null;
      PrintStream outNew = null;
      String outFilename = null;
      

      if (m_CommandArgs.length > 2) {
        String action = m_CommandArgs[(m_CommandArgs.length - 2)];
        if (action.equals(">")) {
          outOld = System.out;
          try {
            outFilename = m_CommandArgs[(m_CommandArgs.length - 1)];
            

            if (outFilename.startsWith("~"))
              outFilename = outFilename.replaceFirst("~", System.getProperty("user.home"));
            outNew = new PrintStream(new File(outFilename));
            System.setOut(outNew);
            m_CommandArgs[(m_CommandArgs.length - 2)] = "";
            m_CommandArgs[(m_CommandArgs.length - 1)] = "";
            

            String[] newArgs = new String[m_CommandArgs.length - 2];
            System.arraycopy(m_CommandArgs, 0, newArgs, 0, m_CommandArgs.length - 2);
            m_CommandArgs = newArgs;
          }
          catch (Exception e) {
            System.setOut(outOld);
            outOld = null;
          }
        }
      }
      try
      {
        Object[] args = { m_CommandArgs };
        m_MainMethod.invoke(null, args);
        if (isInterrupted()) {
          Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_ClassRunner_Run_Error_Text_First"));
        }
      } catch (Exception ex) {
        if (ex.getMessage() == null) {
          Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_ClassRunner_Run_Error_Text_Second"));
        } else {
          Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_ClassRunner_Run_Error_Text_Third") + ex.getMessage());
        }
      } finally {
        m_RunThread = null;
      }
      

      if (outOld != null) {
        outNew.flush();
        outNew.close();
        System.setOut(outOld);
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_ClassRunner_Run_Text_First") + outFilename + Messages.getString("SimpleCLIPanel_ClassRunner_Run_Text_Second"));
      }
    }
  }
  




  public static class CommandlineCompletion
  {
    protected static Vector<String> m_Packages;
    



    protected static Trie m_Trie;
    


    protected boolean m_Debug = false;
    





    public CommandlineCompletion()
    {
      if (m_Packages == null)
      {
        Vector list = ClassDiscovery.findPackages();
        

        HashSet<String> set = new HashSet();
        for (int i = 0; i < list.size(); i++) {
          String[] parts = ((String)list.get(i)).split("\\.");
          for (int n = 1; n < parts.length; n++) {
            String pkg = "";
            for (int m = 0; m <= n; m++) {
              if (m > 0)
                pkg = pkg + ".";
              pkg = pkg + parts[m];
            }
            set.add(pkg);
          }
        }
        

        m_Packages = new Vector();
        m_Packages.addAll(set);
        Collections.sort(m_Packages);
        
        m_Trie = new Trie();
        m_Trie.addAll(m_Packages);
      }
    }
    




    public boolean getDebug()
    {
      return m_Debug;
    }
    




    public void setDebug(boolean value)
    {
      m_Debug = value;
    }
    







    public boolean isClassname(String partial)
    {
      return partial.replaceAll("[a-zA-Z0-9\\-\\.]*", "").length() == 0;
    }
    










    public String getPackage(String partial)
    {
      String result = "";
      boolean wasDot = false;
      for (int i = 0; i < partial.length(); i++) {
        char c = partial.charAt(i);
        

        if ((wasDot) && (c >= 'A') && (c <= 'Z')) {
          break;
        }
        
        if (c == '.') {
          wasDot = true;
          result = result + "" + c;
        }
        else
        {
          wasDot = false;
          result = result + "" + c;
        }
      }
      

      if (result.endsWith(".")) {
        result = result.substring(0, result.length() - 1);
      }
      return result;
    }
    








    public String getClassname(String partial)
    {
      String pkg = getPackage(partial);
      String result; String result; if (pkg.length() + 1 < partial.length()) {
        result = partial.substring(pkg.length() + 1);
      } else {
        result = "";
      }
      return result;
    }
    















    public Vector<String> getFileMatches(String partial)
    {
      Vector<String> result = new Vector();
      

      boolean caseSensitive = File.separatorChar != '\\';
      if (m_Debug) {
        Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetFileMatches_Text_First") + caseSensitive);
      }
      
      if (partial.startsWith("~")) {
        partial = System.getProperty("user.home") + partial.substring(1);
      }
      
      File file = new File(partial);
      File dir = null;
      String prefix = null;
      if (file.exists())
      {
        if (file.isDirectory()) {
          dir = file;
          prefix = null;
        }
        else {
          dir = file.getParentFile();
          prefix = file.getName();
        }
      }
      else {
        dir = file.getParentFile();
        prefix = file.getName();
      }
      
      if (m_Debug) {
        Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetFileMatches_Text_Second") + dir + Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetFileMatches_Text_Third") + prefix);
      }
      
      if (dir != null) {
        File[] files = dir.listFiles();
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            boolean match;
            boolean match;
            if ((prefix != null) && (caseSensitive)) {
              match = name.startsWith(prefix); } else { boolean match;
              if ((prefix != null) && (!caseSensitive)) {
                match = name.toLowerCase().startsWith(prefix.toLowerCase());
              } else
                match = true;
            }
            if (match) {
              if (prefix != null) {
                result.add(partial.substring(0, partial.length() - prefix.length()) + name);

              }
              else if ((partial.endsWith("\\")) || (partial.endsWith("/"))) {
                result.add(partial + name);
              } else {
                result.add(partial + File.separator + name);
              }
            }
          }
        }
        
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetFileMatches_Error_Text") + partial);
      }
      


      if (result.size() > 1) {
        Collections.sort(result);
      }
      
      if (m_Debug) {
        Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetFileMatches_Text_Third"));
        for (int i = 0; i < result.size(); i++) {
          System.out.println((String)result.get(i));
        }
      }
      return result;
    }
    















    public Vector<String> getClassMatches(String partial)
    {
      String pkg = getPackage(partial);
      String cls = getClassname(partial);
      
      if (getDebug()) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetClassMatches_Text_First") + partial + Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetClassMatches_Text_Second") + pkg + Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetClassMatches_Text_Third") + cls);
      }
      
      Vector<String> result = new Vector();
      

      if (cls.length() == 0) {
        Vector<String> list = m_Trie.getWithPrefix(pkg);
        HashSet set = new HashSet();
        for (int i = 0; i < list.size(); i++) {
          String tmpStr = (String)list.get(i);
          if (tmpStr.length() >= partial.length())
          {
            if (!tmpStr.equals(partial))
            {

              int index = tmpStr.indexOf('.', partial.length() + 1);
              if (index > -1) {
                set.add(tmpStr.substring(0, index));
              } else
                set.add(tmpStr);
            } }
        }
        result.addAll(set);
        if (result.size() > 1) {
          Collections.sort(result);
        }
      }
      
      Vector<String> list = ClassDiscovery.find(Object.class, pkg);
      Trie tmpTrie = new Trie();
      tmpTrie.addAll(list);
      list = tmpTrie.getWithPrefix(partial);
      result.addAll(list);
      

      if (result.size() > 1) {
        Collections.sort(result);
      }
      
      if (m_Debug) {
        Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetClassMatches_Text_Fifth"));
        for (int i = 0; i < result.size(); i++) {
          System.out.println((String)result.get(i));
        }
      }
      return result;
    }
    






    public Vector<String> getMatches(String partial)
    {
      if (isClassname(partial)) {
        return getClassMatches(partial);
      }
      return getFileMatches(partial);
    }
    








    public String getCommonPrefix(Vector<String> list)
    {
      Trie trie = new Trie();
      trie.addAll(list);
      String result = trie.getCommonPrefix();
      
      if (m_Debug) {
        Messages.getInstance();Messages.getInstance();System.out.println(list + Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetCommonPrefix_Text_First") + result + Messages.getString("SimpleCLIPanel_CommandlineCompletion_GetCommonPrefix_Text_Second"));
      }
      return result;
    }
  }
  




  public SimpleCLIPanel()
    throws Exception
  {
    setLayout(new BorderLayout());
    Messages.getInstance();add(new JScrollPane(m_OutputArea), Messages.getString("SimpleCLIPanel_JScrollPane_Text_First"));
    Messages.getInstance();add(m_Input, Messages.getString("SimpleCLIPanel_JScrollPane_Text_Second"));
    
    m_Input.setFont(new Font("Monospaced", 0, 12));
    m_Input.addActionListener(this);
    m_Input.setFocusTraversalKeysEnabled(false);
    m_Input.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        doHistory(e);
        doCommandlineCompletion(e);
      }
    });
    m_OutputArea.setEditable(false);
    m_OutputArea.setFont(new Font("Monospaced", 0, 12));
    

    PipedInputStream pio = new PipedInputStream(m_POO);
    System.setOut(new PrintStream(m_POO));
    Reader r = new InputStreamReader(pio);
    m_OutRedirector = new ReaderToTextArea(r, m_OutputArea);
    m_OutRedirector.start();
    


    PipedInputStream pie = new PipedInputStream(m_POE);
    System.setErr(new PrintStream(m_POE));
    r = new InputStreamReader(pie);
    m_ErrRedirector = new ReaderToTextArea(r, m_OutputArea);
    m_ErrRedirector.start();
    
    m_Completion = new CommandlineCompletion();
    
    Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_JScrollPane_Text_Third") + File.separator + Messages.getString("SimpleCLIPanel_JScrollPane_Text_Fouth"));
    


    Messages.getInstance();runCommand(Messages.getString("SimpleCLIPanel_RunCommand_Text"));
    
    loadHistory();
  }
  





  public void runCommand(String commands)
    throws Exception
  {
    System.out.println("> " + commands + '\n');
    System.out.flush();
    String[] commandArgs = Utils.splitOptions(commands);
    if (commandArgs.length == 0) {
      return;
    }
    if (commandArgs[0].equals("java"))
    {
      commandArgs[0] = "";
      try {
        if (commandArgs.length == 1) {
          Messages.getInstance();throw new Exception(Messages.getString("SimpleCLIPanel_RunCommand_Exception_Text_First"));
        }
        String className = commandArgs[1];
        commandArgs[1] = "";
        if (m_RunThread != null) {
          Messages.getInstance();throw new Exception(Messages.getString("SimpleCLIPanel_RunCommand_Exception_Text_Second"));
        }
        Class theClass = Class.forName(className);
        



        Vector argv = new Vector();
        for (int i = 2; i < commandArgs.length; i++) {
          argv.add(commandArgs[i]);
        }
        m_RunThread = new ClassRunner(theClass, (String[])argv.toArray(new String[argv.size()]));
        m_RunThread.setPriority(1);
        m_RunThread.start();
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
      }
    }
    else if (commandArgs[0].equals("cls"))
    {
      m_OutputArea.setText("");
    } else if (commandArgs[0].equals("history")) {
      Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_RunCommand_Text_First"));
      for (int i = 0; i < m_CommandHistory.size(); i++)
        System.out.println(m_CommandHistory.get(i));
      System.out.println();
    } else if (commandArgs[0].equals("break")) {
      if (m_RunThread == null) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_First"));
      } else {
        Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_RunCommand_Text_Second"));
        m_RunThread.interrupt();
      }
    } else if (commandArgs[0].equals("kill")) {
      if (m_RunThread == null) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Second"));
      } else {
        Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_RunCommand_Text_Third"));
        m_RunThread.stop();
        m_RunThread = null;
      }
    } else if (commandArgs[0].equals("exit"))
    {

      Container parent = getParent();
      Container frame = null;
      boolean finished = false;
      while (!finished) {
        if (((parent instanceof JFrame)) || ((parent instanceof Frame)) || ((parent instanceof JInternalFrame)))
        {

          frame = parent;
          finished = true;
        }
        
        if (!finished) {
          parent = parent.getParent();
          finished = parent == null;
        }
      }
      
      if (frame != null) {
        if ((frame instanceof JInternalFrame)) {
          ((JInternalFrame)frame).doDefaultCloseAction();
        } else {
          ((Window)frame).dispatchEvent(new WindowEvent((Window)frame, 201));
        }
      }
    }
    else
    {
      boolean help = (commandArgs.length > 1) && (commandArgs[0].equals("help"));
      
      if ((help) && (commandArgs[1].equals("java"))) {
        Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Third") + File.separator + Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Fourth"));


      }
      else if ((help) && (commandArgs[1].equals("break"))) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Fifth"));
      }
      else if ((help) && (commandArgs[1].equals("kill"))) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Sixth"));
      }
      else if ((help) && (commandArgs[1].equals("cls"))) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Seventh"));
      }
      else if ((help) && (commandArgs[1].equals("history"))) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Eighth"));
      }
      else if ((help) && (commandArgs[1].equals("exit"))) {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Nineth"));
      }
      else
      {
        Messages.getInstance();System.err.println(Messages.getString("SimpleCLIPanel_RunCommand_Error_Text_Tenth"));
      }
    }
  }
  








  public void doHistory(KeyEvent e)
  {
    if (e.getSource() == m_Input) {
      switch (e.getKeyCode()) {
      case 38: 
        if (m_HistoryPos > 0) {
          m_HistoryPos -= 1;
          String command = (String)m_CommandHistory.elementAt(m_HistoryPos);
          m_Input.setText(command); }
        break;
      
      case 40: 
        if (m_HistoryPos < m_CommandHistory.size()) {
          m_HistoryPos += 1;
          String command = "";
          if (m_HistoryPos < m_CommandHistory.size()) {
            command = (String)m_CommandHistory.elementAt(m_HistoryPos);
          }
          m_Input.setText(command); }
        break;
      }
      
    }
  }
  






  public void doCommandlineCompletion(KeyEvent e)
  {
    if (e.getSource() == m_Input) {
      switch (e.getKeyCode())
      {
      case 9: 
        if (e.getModifiers() == 0)
        {

          m_Input.setCursor(Cursor.getPredefinedCursor(3));
          m_OutputArea.setCursor(Cursor.getPredefinedCursor(3));
          try
          {
            String txt = m_Input.getText();
            

            if (txt.trim().startsWith("java ")) {
              int pos = m_Input.getCaretPosition();
              int nonNameCharPos = -1;
              

              for (int i = pos - 1; i >= 0; i--) {
                if ((txt.charAt(i) == '"') || (txt.charAt(i) == ' '))
                {
                  nonNameCharPos = i;
                  break;
                }
              }
              
              if (nonNameCharPos > -1) {
                String search = txt.substring(nonNameCharPos + 1, pos);
                

                Vector<String> list = m_Completion.getMatches(search);
                String common = m_Completion.getCommonPrefix(list);
                

                if ((search.toLowerCase() + File.separator).equals(common.toLowerCase())) {
                  common = search;
                }
                
                if (common.length() > search.length()) {
                  try {
                    m_Input.getDocument().remove(nonNameCharPos + 1, search.length());
                    m_Input.getDocument().insertString(nonNameCharPos + 1, common, null);
                  }
                  catch (Exception ex) {
                    ex.printStackTrace();
                  }
                  
                }
                else if (list.size() > 1) {
                  Messages.getInstance();System.out.println(Messages.getString("SimpleCLIPanel_DoCommandlineCompletion_Text"));
                  for (int i = 0; i < list.size(); i++) {
                    System.out.println("  " + (String)list.get(i));
                  }
                  
                }
                
              }
            }
          }
          finally
          {
            m_Input.setCursor(null);
            m_OutputArea.setCursor(null); } } break;
      




      case 8: 
        if (e.getModifiers() == 8) {
          String txt = m_Input.getText();
          int pos = m_Input.getCaretPosition();
          

          int start = pos;
          start--;
          while ((start >= 0) && (
            (txt.charAt(start) == '.') || (txt.charAt(start) == ' ') || (txt.charAt(start) == '\\') || (txt.charAt(start) == '/')))
          {


            start--;
          }
          



          int newPos = -1;
          for (int i = start; i >= 0; i--) {
            if ((txt.charAt(i) == '.') || (txt.charAt(i) == ' ') || (txt.charAt(i) == '\\') || (txt.charAt(i) == '/'))
            {


              newPos = i;
              break;
            }
          }
          
          try
          {
            m_Input.getDocument().remove(newPos + 1, pos - newPos - 1);
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        

        break;
      }
      
    }
  }
  


  public void actionPerformed(ActionEvent e)
  {
    try
    {
      if (e.getSource() == m_Input) {
        String command = m_Input.getText();
        int last = m_CommandHistory.size() - 1;
        if ((last < 0) || (!command.equals((String)m_CommandHistory.elementAt(last))))
        {
          m_CommandHistory.addElement(command);
          saveHistory();
        }
        m_HistoryPos = m_CommandHistory.size();
        runCommand(command);
        
        m_Input.setText("");
      }
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
  






  protected void loadHistory()
  {
    int size = Integer.parseInt(PROPERTIES.getProperty("HistorySize", "50"));
    
    m_CommandHistory.clear();
    for (int i = 0; i < size; i++) {
      String cmd = PROPERTIES.getProperty("Command" + i, "");
      if (cmd.length() == 0) break;
      m_CommandHistory.add(cmd);
    }
    


    m_HistoryPos = m_CommandHistory.size();
  }
  








  protected void saveHistory()
  {
    int size = Integer.parseInt(PROPERTIES.getProperty("HistorySize", "50"));
    

    int from = m_CommandHistory.size() - size;
    if (from < 0) {
      from = 0;
    }
    
    PROPERTIES.setProperty("HistorySize", "" + size);
    for (int i = from; i < m_CommandHistory.size(); i++) {
      PROPERTIES.setProperty("Command" + (i - from), (String)m_CommandHistory.get(i));
    }
    try {
      String filename = System.getProperties().getProperty("user.home") + File.separatorChar + FILENAME;
      BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filename));
      PROPERTIES.store(stream, "SimpleCLI");
      stream.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public static void main(String[] args)
    throws Exception
  {
    SimpleCLIPanel panel = new SimpleCLIPanel();
    JFrame f = new JFrame();
    Messages.getInstance();f.setTitle(Messages.getString("SimpleCLIPanel_Main_JFrame_SetText_Text"));
    f.getContentPane().add(panel);
    f.setDefaultCloseOperation(2);
    f.pack();
    f.setSize(600, 500);
    f.setVisible(true);
  }
}
