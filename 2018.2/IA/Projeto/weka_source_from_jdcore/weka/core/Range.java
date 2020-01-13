package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;








































public class Range
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 3667337062176835900L;
  Vector m_RangeStrings = new Vector();
  


  boolean m_Invert;
  

  boolean[] m_SelectFlags;
  

  int m_Upper = -1;
  





  public Range() {}
  




  public Range(String rangeList)
  {
    setRanges(rangeList);
  }
  





  public void setUpper(int newUpper)
  {
    if (newUpper >= 0) {
      m_Upper = newUpper;
      setFlags();
    }
  }
  







  public boolean getInvert()
  {
    return m_Invert;
  }
  






  public void setInvert(boolean newSetting)
  {
    m_Invert = newSetting;
  }
  





  public String getRanges()
  {
    StringBuffer result = new StringBuffer(m_RangeStrings.size() * 4);
    boolean first = true;
    char sep = ',';
    for (int i = 0; i < m_RangeStrings.size(); i++) {
      if (first) {
        result.append((String)m_RangeStrings.elementAt(i));
        first = false;
      } else {
        result.append(sep + (String)m_RangeStrings.elementAt(i));
      }
    }
    return result.toString();
  }
  










  public void setRanges(String rangeList)
  {
    Vector ranges = new Vector(10);
    

    while (!rangeList.equals("")) {
      String range = rangeList.trim();
      int commaLoc = rangeList.indexOf(',');
      if (commaLoc != -1) {
        range = rangeList.substring(0, commaLoc).trim();
        rangeList = rangeList.substring(commaLoc + 1).trim();
      } else {
        rangeList = "";
      }
      if (!range.equals("")) {
        ranges.addElement(range);
      }
    }
    m_RangeStrings = ranges;
    m_SelectFlags = null;
  }
  










  public boolean isInRange(int index)
  {
    if (m_Upper == -1) {
      throw new RuntimeException("No upper limit has been specified for range");
    }
    if (m_Invert) {
      return m_SelectFlags[index] == 0;
    }
    return m_SelectFlags[index];
  }
  







  public String toString()
  {
    if (m_RangeStrings.size() == 0) {
      return "Empty";
    }
    String result = "Strings: ";
    Enumeration enu = m_RangeStrings.elements();
    while (enu.hasMoreElements()) {
      result = result + (String)enu.nextElement() + " ";
    }
    result = result + "\n";
    
    result = result + "Invert: " + m_Invert + "\n";
    try
    {
      if (m_Upper == -1) {
        throw new RuntimeException("Upper limit has not been specified");
      }
      String cols = null;
      for (int i = 0; i < m_SelectFlags.length; i++) {
        if (isInRange(i)) {
          if (cols == null) {
            cols = "Cols: " + (i + 1);
          } else {
            cols = cols + "," + (i + 1);
          }
        }
      }
      if (cols != null) {
        result = result + cols + "\n";
      }
    } catch (Exception ex) {
      result = result + ex.getMessage();
    }
    return result;
  }
  








  public int[] getSelection()
  {
    if (m_Upper == -1) {
      throw new RuntimeException("No upper limit has been specified for range");
    }
    int[] selectIndices = new int[m_Upper + 1];
    int numSelected = 0;
    if (m_Invert)
    {
      for (int i = 0; i <= m_Upper; i++) {
        if (m_SelectFlags[i] == 0) {
          selectIndices[(numSelected++)] = i;
        }
      }
    }
    else
    {
      Enumeration enu = m_RangeStrings.elements();
      for (; enu.hasMoreElements(); 
          


          goto 119)
      {
        String currentRange = (String)enu.nextElement();
        int start = rangeLower(currentRange);
        int end = rangeUpper(currentRange);
        int i = start; if ((i <= m_Upper) && (i <= end)) {
          if (m_SelectFlags[i] != 0) {
            selectIndices[(numSelected++)] = i;
          }
          i++;
        }
      }
    }
    


    int[] result = new int[numSelected];
    System.arraycopy(selectIndices, 0, result, 0, numSelected);
    return result;
  }
  








  public static String indicesToRangeList(int[] indices)
  {
    StringBuffer rl = new StringBuffer();
    int last = -2;
    boolean range = false;
    for (int i = 0; i < indices.length; i++) {
      if (i == 0) {
        rl.append(indices[i] + 1);
      } else if (indices[i] == last) {
        range = true;
      } else {
        if (range) {
          rl.append('-').append(last);
          range = false;
        }
        rl.append(',').append(indices[i] + 1);
      }
      last = indices[i] + 1;
    }
    if (range) {
      rl.append('-').append(last);
    }
    return rl.toString();
  }
  

  protected void setFlags()
  {
    m_SelectFlags = new boolean[m_Upper + 1];
    Enumeration enu = m_RangeStrings.elements();
    for (; enu.hasMoreElements(); 
        





        goto 90)
    {
      String currentRange = (String)enu.nextElement();
      if (!isValidRange(currentRange)) {
        throw new IllegalArgumentException("Invalid range list at " + currentRange);
      }
      int start = rangeLower(currentRange);
      int end = rangeUpper(currentRange);
      int i = start; if ((i <= m_Upper) && (i <= end)) {
        m_SelectFlags[i] = true;i++;
      }
    }
  }
  







  protected int rangeSingle(String single)
  {
    if (single.toLowerCase().equals("first")) {
      return 0;
    }
    if (single.toLowerCase().equals("last")) {
      return m_Upper;
    }
    int index = Integer.parseInt(single) - 1;
    if (index < 0) {
      index = 0;
    }
    if (index > m_Upper) {
      index = m_Upper;
    }
    return index;
  }
  



  protected int rangeLower(String range)
  {
    int hyphenIndex;
    


    if ((hyphenIndex = range.indexOf('-')) >= 0) {
      return Math.min(rangeLower(range.substring(0, hyphenIndex)), rangeLower(range.substring(hyphenIndex + 1)));
    }
    
    return rangeSingle(range);
  }
  




  protected int rangeUpper(String range)
  {
    int hyphenIndex;
    


    if ((hyphenIndex = range.indexOf('-')) >= 0) {
      return Math.max(rangeUpper(range.substring(0, hyphenIndex)), rangeUpper(range.substring(hyphenIndex + 1)));
    }
    
    return rangeSingle(range);
  }
  








  protected boolean isValidRange(String range)
  {
    if (range == null) {
      return false;
    }
    int hyphenIndex;
    if ((hyphenIndex = range.indexOf('-')) >= 0) {
      if ((isValidRange(range.substring(0, hyphenIndex))) && (isValidRange(range.substring(hyphenIndex + 1))))
      {
        return true;
      }
      return false;
    }
    if (range.toLowerCase().equals("first")) {
      return true;
    }
    if (range.toLowerCase().equals("last")) {
      return true;
    }
    try {
      int index = Integer.parseInt(range);
      if ((index > 0) && (index <= m_Upper + 1)) {
        return true;
      }
      return false;
    } catch (NumberFormatException ex) {}
    return false;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.18 $");
  }
  




  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        throw new Exception("Usage: Range <rangespec>");
      }
      Range range = new Range();
      range.setRanges(argv[0]);
      range.setUpper(9);
      range.setInvert(false);
      System.out.println("Input: " + argv[0] + "\n" + range.toString());
      
      int[] rangeIndices = range.getSelection();
      for (int i = 0; i < rangeIndices.length; i++)
        System.out.print(" " + (rangeIndices[i] + 1));
      System.out.println("");
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}
