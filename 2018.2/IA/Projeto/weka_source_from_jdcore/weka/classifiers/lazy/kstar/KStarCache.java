package weka.classifiers.lazy.kstar;

import java.io.Serializable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;



































public class KStarCache
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = -7693632394267140678L;
  CacheTable m_Cache;
  
  public KStarCache()
  {
    m_Cache = new CacheTable();
  }
  






  public void store(double key, double value, double pmiss)
  {
    if (!m_Cache.containsKey(key)) {
      m_Cache.insert(key, value, pmiss);
    }
  }
  




  public boolean containsKey(double key)
  {
    if (m_Cache.containsKey(key)) {
      return true;
    }
    return false;
  }
  




  public TableEntry getCacheValues(double key)
  {
    if (m_Cache.containsKey(key)) {
      return m_Cache.getEntry(key);
    }
    return null;
  }
  



  public class CacheTable
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -8086106452588253423L;
    


    private KStarCache.TableEntry[] m_Table;
    


    private int m_Count;
    

    private int m_Threshold;
    

    private float m_LoadFactor;
    

    private final int DEFAULT_TABLE_SIZE = 101;
    

    private final float DEFAULT_LOAD_FACTOR = 0.75F;
    


    private final double EPSILON = 1.0E-5D;
    


    public CacheTable(int size, float loadFactor)
    {
      m_Table = new KStarCache.TableEntry[size];
      m_LoadFactor = loadFactor;
      m_Threshold = ((int)(size * loadFactor));
      m_Count = 0;
    }
    


    public CacheTable()
    {
      this(101, 0.75F);
    }
    


    public boolean containsKey(double key)
    {
      KStarCache.TableEntry[] table = m_Table;
      int hash = hashCode(key);
      int index = (hash & 0x7FFFFFFF) % table.length;
      for (KStarCache.TableEntry e = table[index]; e != null; e = next) {
        if ((hash == hash) && (Math.abs(key - key) < 1.0E-5D)) {
          return true;
        }
      }
      return false;
    }
    





    public void insert(double key, double value, double pmiss)
    {
      KStarCache.TableEntry[] table = m_Table;
      int hash = hashCode(key);
      int index = (hash & 0x7FFFFFFF) % table.length;
      
      for (KStarCache.TableEntry e = table[index]; e != null; e = next) {
        if ((hash == hash) && (Math.abs(key - key) < 1.0E-5D)) {
          return;
        }
      }
      

      KStarCache.TableEntry ne = new KStarCache.TableEntry(KStarCache.this, hash, key, value, pmiss, table[index]);
      
      table[index] = ne;
      m_Count += 1;
      
      if (m_Count >= m_Threshold) {
        rehash();
      }
    }
    




    public KStarCache.TableEntry getEntry(double key)
    {
      KStarCache.TableEntry[] table = m_Table;
      int hash = hashCode(key);
      int index = (hash & 0x7FFFFFFF) % table.length;
      for (KStarCache.TableEntry e = table[index]; e != null; e = next) {
        if ((hash == hash) && (Math.abs(key - key) < 1.0E-5D)) {
          return e;
        }
      }
      return null;
    }
    



    public int size()
    {
      return m_Count;
    }
    



    public boolean isEmpty()
    {
      return m_Count == 0;
    }
    


    public void clear()
    {
      KStarCache.TableEntry[] table = m_Table;
      int index = table.length; for (;;) { index--; if (index < 0) break;
        table[index] = null;
      }
      m_Count = 0;
    }
    





    private void rehash()
    {
      int oldCapacity = m_Table.length;
      KStarCache.TableEntry[] oldTable = m_Table;
      int newCapacity = oldCapacity * 2 + 1;
      KStarCache.TableEntry[] newTable = new KStarCache.TableEntry[newCapacity];
      m_Threshold = ((int)(newCapacity * m_LoadFactor));
      m_Table = newTable;
      
      for (int i = oldCapacity; i-- > 0;) {
        for (old = oldTable[i]; old != null;) {
          KStarCache.TableEntry e = old;
          old = next;
          int index = (hash & 0x7FFFFFFF) % newCapacity;
          next = newTable[index];
          newTable[index] = e;
        }
      }
      
      KStarCache.TableEntry old;
    }
    

    private int hashCode(double key)
    {
      long bits = Double.doubleToLongBits(key);
      return (int)(bits ^ bits >> 32);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.11 $");
    }
  }
  



  public class TableEntry
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 4057602386766259138L;
    


    public int hash;
    

    public double key;
    

    public double value;
    

    public double pmiss;
    

    public TableEntry next = null;
    

    public TableEntry(int hash, double key, double value, double pmiss, TableEntry next)
    {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.pmiss = pmiss;
      this.next = next;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.11 $");
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
}
