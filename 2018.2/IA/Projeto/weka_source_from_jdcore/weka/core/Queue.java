package weka.core;

import java.io.PrintStream;
import java.io.Serializable;

















































public class Queue
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -1141282001146389780L;
  public Queue() {}
  
  protected class QueueNode
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -5119358279412097455L;
    protected QueueNode m_Next;
    protected Object m_Contents;
    
    public QueueNode(Object contents)
    {
      m_Contents = contents;
      next(null);
    }
    





    public QueueNode next(QueueNode next)
    {
      return this.m_Next = next;
    }
    



    public QueueNode next()
    {
      return m_Next;
    }
    





    public Object contents(Object contents)
    {
      return this.m_Contents = contents;
    }
    



    public Object contents()
    {
      return m_Contents;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.10 $");
    }
  }
  

  protected QueueNode m_Head = null;
  

  protected QueueNode m_Tail = null;
  

  protected int m_Size = 0;
  
















  public final synchronized void removeAllElements()
  {
    m_Size = 0;
    m_Head = null;
    m_Tail = null;
  }
  














  public synchronized Object push(Object item)
  {
    QueueNode newNode = new QueueNode(item);
    
    if (m_Head == null) {
      m_Head = (this.m_Tail = newNode);
    } else {
      m_Tail = m_Tail.next(newNode);
    }
    m_Size += 1;
    return item;
  }
  












  public synchronized Object pop()
    throws RuntimeException
  {
    if (m_Head == null) {
      throw new RuntimeException("Queue is empty");
    }
    Object retval = m_Head.contents();
    m_Size -= 1;
    m_Head = m_Head.next();
    





    if (m_Head == null) {
      m_Tail = null;
    }
    return retval;
  }
  








  public synchronized Object peek()
    throws RuntimeException
  {
    if (m_Head == null) {
      throw new RuntimeException("Queue is empty");
    }
    return m_Head.contents();
  }
  





  public boolean empty()
  {
    return m_Head == null;
  }
  





  public int size()
  {
    return m_Size;
  }
  








  public String toString()
  {
    String retval = "Queue Contents " + m_Size + " elements\n";
    QueueNode current = m_Head;
    if (current == null) {
      return retval + "Empty\n";
    }
    while (current != null) {
      retval = retval + current.contents().toString() + "\n";
      current = current.next();
    }
    
    return retval;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  







  public static void main(String[] argv)
  {
    try
    {
      Queue queue = new Queue();
      for (int i = 0; i < argv.length; i++) {
        queue.push(argv[i]);
      }
      System.out.println("After pushing command line arguments");
      System.out.println(queue.toString());
      while (!queue.empty()) {
        System.out.println("Pop: " + queue.pop().toString());
      }
      
      try
      {
        queue.pop();
        System.out.println("ERROR: pop did not throw exception!");
      }
      catch (RuntimeException ex)
      {
        System.out.println("Pop on empty queue correctly gave exception.");
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}
