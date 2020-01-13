// Decompiled using: fernflower
// Took: 407ms

package weka.core;

import java.io.Serializable;
import java.util.Enumeration;

public class FastVector implements Copyable, Serializable, RevisionHandler {
   private static final long serialVersionUID = -2173635135622930169L;
   private Object[] m_Objects;
   private int m_Size = 0;
   private int m_CapacityIncrement = 1;
   private int m_CapacityMultiplier = 2;

   public FastVector() {
      this.m_Objects = new Object[0];
   }

   public FastVector(int capacity) {
      this.m_Objects = new Object[capacity];
   }

   public final void addElement(Object element) {
      if (this.m_Size == this.m_Objects.length) {
         Object[] newObjects = new Object[this.m_CapacityMultiplier * (this.m_Objects.length + this.m_CapacityIncrement)];
         System.arraycopy(this.m_Objects, 0, newObjects, 0, this.m_Size);
         this.m_Objects = newObjects;
      }

      this.m_Objects[this.m_Size] = element;
      ++this.m_Size;
   }

   public final int capacity() {
      return this.m_Objects.length;
   }

   public final Object copy() {
      FastVector copy = new FastVector(this.m_Objects.length);
      copy.m_Size = this.m_Size;
      copy.m_CapacityIncrement = this.m_CapacityIncrement;
      copy.m_CapacityMultiplier = this.m_CapacityMultiplier;
      System.arraycopy(this.m_Objects, 0, copy.m_Objects, 0, this.m_Size);
      return copy;
   }

   public final Object copyElements() {
      FastVector copy = new FastVector(this.m_Objects.length);
      copy.m_Size = this.m_Size;
      copy.m_CapacityIncrement = this.m_CapacityIncrement;
      copy.m_CapacityMultiplier = this.m_CapacityMultiplier;

      for(int i = 0; i < this.m_Size; ++i) {
         copy.m_Objects[i] = ((Copyable)this.m_Objects[i]).copy();
      }

      return copy;
   }

   public final Object elementAt(int index) {
      return this.m_Objects[index];
   }

   public final Enumeration elements() {
      return new FastVector.FastVectorEnumeration(this);
   }

   public final Enumeration elements(int index) {
      return new FastVector.FastVectorEnumeration(this, index);
   }

   public boolean contains(Object o) {
      if (o == null) {
         return false;
      } else {
         for(int i = 0; i < this.m_Objects.length; ++i) {
            if (o.equals(this.m_Objects[i])) {
               return true;
            }
         }

         return false;
      }
   }

   public final Object firstElement() {
      return this.m_Objects[0];
   }

   public final int indexOf(Object element) {
      for(int i = 0; i < this.m_Size; ++i) {
         if (element.equals(this.m_Objects[i])) {
            return i;
         }
      }

      return -1;
   }

   public final void insertElementAt(Object element, int index) {
      if (this.m_Size < this.m_Objects.length) {
         System.arraycopy(this.m_Objects, index, this.m_Objects, index + 1, this.m_Size - index);
         this.m_Objects[index] = element;
      } else {
         Object[] newObjects = new Object[this.m_CapacityMultiplier * (this.m_Objects.length + this.m_CapacityIncrement)];
         System.arraycopy(this.m_Objects, 0, newObjects, 0, index);
         newObjects[index] = element;
         System.arraycopy(this.m_Objects, index, newObjects, index + 1, this.m_Size - index);
         this.m_Objects = newObjects;
      }

      ++this.m_Size;
   }

   public final Object lastElement() {
      return this.m_Objects[this.m_Size - 1];
   }

   public final void removeElementAt(int index) {
      System.arraycopy(this.m_Objects, index + 1, this.m_Objects, index, this.m_Size - index - 1);
      this.m_Objects[this.m_Size - 1] = null;
      --this.m_Size;
   }

   public final void removeAllElements() {
      this.m_Objects = new Object[this.m_Objects.length];
      this.m_Size = 0;
   }

   public final void appendElements(FastVector toAppend) {
      this.setCapacity(this.size() + toAppend.size());
      System.arraycopy(toAppend.m_Objects, 0, this.m_Objects, this.size(), toAppend.size());
      this.m_Size = this.m_Objects.length;
   }

   public final Object[] toArray() {
      Object[] newObjects = new Object[this.size()];
      System.arraycopy(this.m_Objects, 0, newObjects, 0, this.size());
      return newObjects;
   }

   public final void setCapacity(int capacity) {
      Object[] newObjects = new Object[capacity];
      System.arraycopy(this.m_Objects, 0, newObjects, 0, Math.min(capacity, this.m_Size));
      this.m_Objects = newObjects;
      if (this.m_Objects.length < this.m_Size) {
         this.m_Size = this.m_Objects.length;
      }

   }

   public final void setElementAt(Object element, int index) {
      this.m_Objects[index] = element;
   }

   public final int size() {
      return this.m_Size;
   }

   public final void swap(int first, int second) {
      Object help = this.m_Objects[first];
      this.m_Objects[first] = this.m_Objects[second];
      this.m_Objects[second] = help;
   }

   public final void trimToSize() {
      Object[] newObjects = new Object[this.m_Size];
      System.arraycopy(this.m_Objects, 0, newObjects, 0, this.m_Size);
      this.m_Objects = newObjects;
   }

   public String getRevision() {
      return RevisionUtils.extract("$Revision: 1.16 $");
   }

   public class FastVectorEnumeration implements Enumeration, RevisionHandler {
      private int m_Counter;
      private FastVector m_Vector;
      private int m_SpecialElement;

      public FastVectorEnumeration(FastVector vector) {
         this.m_Counter = 0;
         this.m_Vector = vector;
         this.m_SpecialElement = -1;
      }

      public FastVectorEnumeration(FastVector vector, int special) {
         this.m_Vector = vector;
         this.m_SpecialElement = special;
         if (special == 0) {
            this.m_Counter = 1;
         } else {
            this.m_Counter = 0;
         }

      }

      public final boolean hasMoreElements() {
         return this.m_Counter < this.m_Vector.size();
      }

      public final Object nextElement() {
         Object result = this.m_Vector.elementAt(this.m_Counter);
         ++this.m_Counter;
         if (this.m_Counter == this.m_SpecialElement) {
            ++this.m_Counter;
         }

         return result;
      }

      public String getRevision() {
         return RevisionUtils.extract("$Revision: 1.16 $");
      }
   }
}
