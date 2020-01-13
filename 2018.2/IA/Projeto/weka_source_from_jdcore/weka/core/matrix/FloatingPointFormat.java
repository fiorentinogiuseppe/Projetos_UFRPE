package weka.core.matrix;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;































public class FloatingPointFormat
  extends DecimalFormat
  implements RevisionHandler
{
  private static final long serialVersionUID = 4500373755333429499L;
  protected DecimalFormat nf;
  protected int width;
  protected int decimal;
  protected boolean trailing = true;
  


  public FloatingPointFormat()
  {
    this(8, 5);
  }
  
  public FloatingPointFormat(int digits) {
    this(8, 2);
  }
  
  public FloatingPointFormat(int w, int d) {
    width = w;
    decimal = d;
    nf = new DecimalFormat(pattern(w, d));
    nf.setPositivePrefix(" ");
    nf.setNegativePrefix("-");
  }
  
  public FloatingPointFormat(int w, int d, boolean trailingZeros) {
    this(w, d);
  }
  

  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos)
  {
    StringBuffer s = new StringBuffer(nf.format(number));
    if (s.length() > width) {
      if ((s.charAt(0) == ' ') && (s.length() == width + 1)) {
        s.deleteCharAt(0);
      }
      else {
        s.setLength(width);
        for (int i = 0; i < width; i++) {
          s.setCharAt(i, '*');
        }
      }
    } else {
      for (int i = 0; i < width - s.length(); i++)
        s.insert(0, ' ');
    }
    if ((!trailing) && (decimal > 0)) {
      while (s.charAt(s.length() - 1) == '0')
        s.deleteCharAt(s.length() - 1);
      if (s.charAt(s.length() - 1) == '.') {
        s.deleteCharAt(s.length() - 1);
      }
    }
    return toAppendTo.append(s);
  }
  
  public static String pattern(int w, int d) {
    StringBuffer s = new StringBuffer();
    s.append(padding(w - d - 3, '#'));
    if (d == 0) { s.append('0');
    } else {
      s.append("0.");
      s.append(padding(d, '0'));
    }
    return s.toString();
  }
  
  private static StringBuffer padding(int n, char c) {
    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < n; i++) {
      text.append(c);
    }
    
    return text;
  }
  
  public int width() {
    if (!trailing) throw new RuntimeException("flexible width");
    return width;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
