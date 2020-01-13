package weka.core.pmml;



















public class PMMLUtils
{
  public PMMLUtils() {}
  

















  public static String pad(String source, String padChar, int length, boolean leftPad)
  {
    StringBuffer temp = new StringBuffer();
    
    if (leftPad) {
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
      temp.append(source);
    } else {
      temp.append(source);
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
    }
    return temp.toString();
  }
}
