package weka.core.neighboursearch.covertrees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;







































public class Stack<T>
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 5604056321825539264L;
  public int length;
  public ArrayList<T> elements;
  
  public Stack()
  {
    length = 0;
    elements = new ArrayList();
  }
  



  public Stack(int capacity)
  {
    length = 0;
    elements = new ArrayList(capacity);
  }
  



  public T last()
  {
    return elements.get(length - 1);
  }
  




  public T element(int i)
  {
    return elements.get(i);
  }
  





  public void set(int i, T e)
  {
    elements.set(i, e);
  }
  









  public List subList(int beginIdx, int uptoLength)
  {
    return elements.subList(beginIdx, uptoLength);
  }
  
  public void clear()
  {
    elements.clear();
    length = 0;
  }
  




  public void addAll(Collection c)
  {
    elements.addAll(c);
    length = c.size();
  }
  








  public void replaceAllBy(Stack<T> s)
  {
    elements.clear();
    elements.addAll(elements);
    length = elements.size();
  }
  




  public T pop()
  {
    length -= 1;
    return elements.remove(length);
  }
  




  public void push(T new_ele)
  {
    length += 1;
    elements.add(new_ele);
  }
  





  public void push(Stack<T> v, T new_ele)
  {
    length += 1;
    elements.add(new_ele);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
