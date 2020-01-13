package weka.core.matrix;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;



























public class FlexibleDecimalFormat
  extends DecimalFormat
  implements RevisionHandler
{
  private static final long serialVersionUID = 110912192794064140L;
  private DecimalFormat nf = null;
  private int digits = 7;
  private boolean exp = false;
  private int intDigits = 1;
  private int decimalDigits = 0;
  private int expDecimalDigits = 0;
  private int power = 2;
  private boolean trailing = false;
  private boolean grouping = false;
  private boolean sign = false;
  
  public FlexibleDecimalFormat() {
    this(5);
  }
  
  public FlexibleDecimalFormat(int digits) {
    if (digits < 1) {
      throw new IllegalArgumentException("digits < 1");
    }
    this.digits = digits;
    intDigits = 1;
  }
  
  public FlexibleDecimalFormat(int digits, boolean trailing) {
    this(digits);
    this.trailing = trailing;
  }
  
  public FlexibleDecimalFormat(int digits, boolean exp, boolean trailing, boolean grouping)
  {
    this.trailing = trailing;
    this.exp = exp;
    this.digits = digits;
    this.grouping = grouping;
    if (exp) {
      intDigits = 1;
      decimalDigits = (digits - intDigits);
    } else {
      decimalDigits = decimalDigits;
      intDigits = Math.max(1, digits - decimalDigits);
    }
  }
  
  public FlexibleDecimalFormat(double d) {
    newFormat(d);
  }
  
  private void newFormat(double d) {
    if (needExponentialFormat(d)) {
      exp = true;
      intDigits = 1;
      expDecimalDigits = decimalDigits(d, true);
      if (d < 0.0D) {
        sign = true;
      } else {
        sign = false;
      }
    } else {
      exp = false;
      intDigits = Math.max(1, intDigits(d));
      decimalDigits = decimalDigits(d, false);
      if (d < 0.0D) {
        sign = true;
      } else {
        sign = false;
      }
    }
  }
  
  public void update(double d) {
    if (Math.abs(intDigits(d) - 1) > 99) {
      power = 3;
    }
    expDecimalDigits = Math.max(expDecimalDigits, decimalDigits(d, true));
    if (d < 0.0D) {
      sign = true;
    }
    if ((needExponentialFormat(d)) || (exp)) {
      exp = true;
    } else {
      intDigits = Math.max(intDigits, intDigits(d));
      decimalDigits = Math.max(decimalDigits, decimalDigits(d, false));
      if (d < 0.0D) {
        sign = true;
      }
    }
  }
  
  private static int intDigits(double d) {
    return (int)Math.floor(Math.log(Math.abs(d * 1.00000000000001D)) / Math.log(10.0D)) + 1;
  }
  
  private int decimalDigits(double d, boolean expo) {
    if (d == 0.0D) {
      return 0;
    }
    d = Math.abs(d);
    int e = intDigits(d);
    if (expo) {
      d /= Math.pow(10.0D, e - 1);
      e = 1;
    }
    if (e >= digits) {
      return 0;
    }
    int iD = Math.max(1, e);
    int dD = digits - e;
    if ((!trailing) && (dD > 0)) {
      FloatingPointFormat f = new FloatingPointFormat(iD + 1 + dD, dD, true);
      
      String dString = nf.format(d);
      while ((dD > 0) && 
        (dString.charAt(iD + 1 + dD - 1) == '0')) {
        dD--;
      }
    }
    


    return dD;
  }
  
  public boolean needExponentialFormat(double d) {
    if (d == 0.0D) {
      return false;
    }
    int e = intDigits(d);
    if ((e > digits + 5) || (e < -3)) {
      return true;
    }
    return false;
  }
  
  public void grouping(boolean grouping)
  {
    this.grouping = grouping;
  }
  
  private static void println(Object obj) {
    System.out.println(obj);
  }
  
  private void setFormat() {
    int dot = 1;
    if (decimalDigits == 0) {
      dot = 0;
    }
    if (exp) {
      nf = new ExponentialFormat(1 + expDecimalDigits, power, sign, (grouping) || (trailing));
    }
    else
    {
      int s = sign ? 1 : 0;
      nf = new FloatingPointFormat(s + intDigits + dot + decimalDigits, decimalDigits, (grouping) || (trailing));
    }
  }
  

  private void setFormat(double d)
  {
    newFormat(d);
    setFormat();
  }
  

  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos)
  {
    if (grouping) {
      if (nf == null) {
        setFormat();
      }
    } else {
      setFormat(number);
    }
    
    return toAppendTo.append(nf.format(number));
  }
  
  public int width()
  {
    if ((!trailing) && (!grouping)) {
      throw new RuntimeException("flexible width");
    }
    
    return format(0.0D).length();
  }
  
  public StringBuffer formatString(String str) {
    int w = width();
    int h = (w - str.length()) / 2;
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < h; i++) {
      text.append(' ');
    }
    text.append(str);
    for (int i = 0; i < w - h - str.length(); i++) {
      text.append(' ');
    }
    return text;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
