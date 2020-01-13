package weka.associations.tertius;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;


























public class SimpleLinkedList
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -1491148276509976299L;
  private Entry first;
  private Entry last;
  
  public class LinkedListIterator
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -2448555236100426759L;
    
    public LinkedListIterator() {}
    
    SimpleLinkedList.Entry current = first;
    SimpleLinkedList.Entry lastReturned = null;
    
    public boolean hasNext() {
      return current.next != last;
    }
    
    public Object next() {
      if (current == last) {
        throw new NoSuchElementException();
      }
      current = current.next;
      lastReturned = current;
      return current.element;
    }
    
    public void remove() {
      if ((lastReturned == last) || (lastReturned == null))
      {
        throw new IllegalStateException();
      }
      lastReturned.previous.next = lastReturned.next;
      lastReturned.next.previous = lastReturned.previous;
      current = lastReturned.previous;
      lastReturned = null;
    }
    
    public void addBefore(Object o) {
      if (lastReturned == null) {
        throw new IllegalStateException();
      }
      SimpleLinkedList.Entry newEntry = new SimpleLinkedList.Entry(o, lastReturned, lastReturned.previous);
      lastReturned.previous.next = newEntry;
      lastReturned.previous = newEntry;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.6 $");
    }
  }
  
  public class LinkedListInverseIterator implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 6290379064027832108L;
    
    public LinkedListInverseIterator() {}
    
    SimpleLinkedList.Entry current = last;
    SimpleLinkedList.Entry lastReturned = null;
    
    public boolean hasPrevious() {
      return current.previous != first;
    }
    
    public Object previous() {
      if (current == first) {
        throw new NoSuchElementException();
      }
      current = current.previous;
      lastReturned = current;
      return current.element;
    }
    
    public void remove() {
      if ((lastReturned == first) || (lastReturned == null))
      {
        throw new IllegalStateException();
      }
      lastReturned.previous.next = lastReturned.next;
      lastReturned.next.previous = lastReturned.previous;
      current = lastReturned.next;
      lastReturned = null;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.6 $");
    }
  }
  

  private static class Entry
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 7888492479685339831L;
    
    Object element;
    Entry next;
    Entry previous;
    
    Entry(Object element, Entry next, Entry previous)
    {
      this.element = element;
      this.next = next;
      this.previous = previous;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.6 $");
    }
  }
  


  public SimpleLinkedList()
  {
    first = new Entry(null, null, null);
    last = new Entry(null, null, null);
    first.next = last;
    last.previous = first;
  }
  
  public Object removeFirst() {
    if (first.next == last) {
      throw new NoSuchElementException();
    }
    Object result = first.next.element;
    first.next.next.previous = first;
    first.next = first.next.next;
    return result;
  }
  
  public Object getFirst() {
    if (first.next == last) {
      throw new NoSuchElementException();
    }
    return first.next.element;
  }
  
  public Object getLast() {
    if (last.previous == first) {
      throw new NoSuchElementException();
    }
    return last.previous.element;
  }
  
  public void addFirst(Object o) {
    Entry newEntry = new Entry(o, first.next, first);
    first.next.previous = newEntry;
    first.next = newEntry;
  }
  
  public void add(Object o) {
    Entry newEntry = new Entry(o, last, last.previous);
    last.previous.next = newEntry;
    last.previous = newEntry;
  }
  
  public void addAll(SimpleLinkedList list) {
    last.previous.next = first.next;
    first.next.previous = last.previous;
    last = last;
  }
  
  public void clear() {
    first.next = last;
    last.previous = first;
  }
  
  public boolean isEmpty() {
    return first.next == last;
  }
  
  public LinkedListIterator iterator() {
    return new LinkedListIterator();
  }
  
  public LinkedListInverseIterator inverseIterator() {
    return new LinkedListInverseIterator();
  }
  
  public int size() {
    int result = 0;
    LinkedListIterator iter = new LinkedListIterator();
    while (iter.hasNext()) {
      result++;
      iter.next();
    }
    return result;
  }
  
  public void merge(SimpleLinkedList list, Comparator comp) {
    LinkedListIterator iter1 = iterator();
    LinkedListIterator iter2 = list.iterator();
    Object elem1 = iter1.next();
    Object elem2 = iter2.next();
    while (elem2 != null) {
      if ((elem1 == null) || (comp.compare(elem2, elem1) < 0))
      {
        iter1.addBefore(elem2);
        elem2 = iter2.next();
      } else {
        elem1 = iter1.next();
      }
    }
  }
  
  public void sort(Comparator comp) {
    LinkedListIterator iter = iterator();
    if (iter.hasNext()) {
      SimpleLinkedList lower = new SimpleLinkedList();
      SimpleLinkedList upper = new SimpleLinkedList();
      Object ref = iter.next();
      
      while (iter.hasNext()) {
        Object elem = iter.next();
        if (comp.compare(elem, ref) < 0) {
          lower.add(elem);
        } else {
          upper.add(elem);
        }
      }
      lower.sort(comp);
      upper.sort(comp);
      clear();
      addAll(lower);
      add(ref);
      addAll(upper);
    }
  }
  
  public String toString() {
    StringBuffer text = new StringBuffer();
    LinkedListIterator iter = iterator();
    text.append("[");
    while (iter.hasNext()) {
      text.append(String.valueOf(iter.next()));
      if (iter.hasNext()) {
        text.append(", ");
      }
    }
    text.append("]");
    return text.toString();
  }
  







  private synchronized void writeObject(ObjectOutputStream s)
    throws IOException
  {
    s.defaultWriteObject();
    

    s.writeInt(size());
    

    for (Entry e = first.next; e != last; e = next) {
      s.writeObject(element);
    }
  }
  



  private synchronized void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    

    int size = s.readInt();
    

    first = new Entry(null, null, null);
    last = new Entry(null, null, null);
    first.next = last;
    last.previous = first;
    

    for (int i = 0; i < size; i++) {
      add(s.readObject());
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
