package weka.core;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.util.Properties;
import java.util.Random;
import weka.gui.PropertySheetPanel;
































public final class Utils
  implements RevisionHandler
{
  public static double log2 = Math.log(2.0D);
  

  public static double SMALL = 1.0E-6D;
  







  public Utils() {}
  







  public static Properties readProperties(String resourceName)
    throws Exception
  {
    Properties defaultProps = new Properties();
    

    try
    {
      defaultProps.load(new Utils().getClass().getClassLoader().getResourceAsStream(resourceName));


    }
    catch (Exception ex)
    {

      System.err.println("Warning, unable to load properties file from system resource (Utils.java)");
    }
    



    int slInd = resourceName.lastIndexOf('/');
    if (slInd != -1) {
      resourceName = resourceName.substring(slInd + 1);
    }
    

    Properties userProps = new Properties(defaultProps);
    File propFile = new File(System.getProperties().getProperty("user.home") + File.separatorChar + resourceName);
    
    if (propFile.exists()) {
      try {
        userProps.load(new FileInputStream(propFile));
      } catch (Exception ex) {
        throw new Exception("Problem reading user properties: " + propFile);
      }
    }
    

    Properties localProps = new Properties(userProps);
    propFile = new File(resourceName);
    if (propFile.exists()) {
      try {
        localProps.load(new FileInputStream(propFile));
      } catch (Exception ex) {
        throw new Exception("Problem reading local properties: " + propFile);
      }
    }
    
    return localProps;
  }
  









  public static final double correlation(double[] y1, double[] y2, int n)
  {
    double av1 = 0.0D;double av2 = 0.0D;double y11 = 0.0D;double y22 = 0.0D;double y12 = 0.0D;
    
    if (n <= 1) {
      return 1.0D;
    }
    for (int i = 0; i < n; i++) {
      av1 += y1[i];
      av2 += y2[i];
    }
    av1 /= n;
    av2 /= n;
    for (i = 0; i < n; i++) {
      y11 += (y1[i] - av1) * (y1[i] - av1);
      y22 += (y2[i] - av2) * (y2[i] - av2);
      y12 += (y1[i] - av1) * (y2[i] - av2); }
    double c;
    double c; if (y11 * y22 == 0.0D) {
      c = 1.0D;
    } else {
      c = y12 / Math.sqrt(Math.abs(y11 * y22));
    }
    
    return c;
  }
  







  public static String removeSubstring(String inString, String substring)
  {
    StringBuffer result = new StringBuffer();
    int oldLoc = 0;int loc = 0;
    while ((loc = inString.indexOf(substring, oldLoc)) != -1) {
      result.append(inString.substring(oldLoc, loc));
      oldLoc = loc + substring.length();
    }
    result.append(inString.substring(oldLoc));
    return result.toString();
  }
  










  public static String replaceSubstring(String inString, String subString, String replaceString)
  {
    StringBuffer result = new StringBuffer();
    int oldLoc = 0;int loc = 0;
    while ((loc = inString.indexOf(subString, oldLoc)) != -1) {
      result.append(inString.substring(oldLoc, loc));
      result.append(replaceString);
      oldLoc = loc + subString.length();
    }
    result.append(inString.substring(oldLoc));
    return result.toString();
  }
  









  public static String padLeft(String inString, int length)
  {
    return fixStringLength(inString, length, false);
  }
  









  public static String padRight(String inString, int length)
  {
    return fixStringLength(inString, length, true);
  }
  










  private static String fixStringLength(String inString, int length, boolean right)
  {
    if (inString.length() < length) {
      while (inString.length() < length)
        inString = right ? inString.concat(" ") : " ".concat(inString);
    }
    if (inString.length() > length) {
      inString = inString.substring(0, length);
    }
    return inString;
  }
  














  public static String doubleToString(double value, int afterDecimalPoint)
  {
    double temp = value * Math.pow(10.0D, afterDecimalPoint);
    if (Math.abs(temp) < 9.223372036854776E18D) {
      long precisionValue = temp > 0.0D ? (temp + 0.5D) : -(Math.abs(temp) + 0.5D);
      StringBuffer stringBuffer;
      StringBuffer stringBuffer; if (precisionValue == 0L) {
        stringBuffer = new StringBuffer(String.valueOf(0));
      } else {
        stringBuffer = new StringBuffer(String.valueOf(precisionValue));
      }
      if (afterDecimalPoint == 0) {
        return stringBuffer.toString();
      }
      int dotPosition = stringBuffer.length() - afterDecimalPoint;
      while (((precisionValue < 0L) && (dotPosition < 1)) || (dotPosition < 0)) {
        if (precisionValue < 0L) {
          stringBuffer.insert(1, '0');
        } else {
          stringBuffer.insert(0, '0');
        }
        dotPosition++;
      }
      stringBuffer.insert(dotPosition, '.');
      if ((precisionValue < 0L) && (stringBuffer.charAt(1) == '.')) {
        stringBuffer.insert(1, '0');
      } else if (stringBuffer.charAt(0) == '.') {
        stringBuffer.insert(0, '0');
      }
      int currentPos = stringBuffer.length() - 1;
      
      while ((currentPos > dotPosition) && (stringBuffer.charAt(currentPos) == '0')) {
        stringBuffer.setCharAt(currentPos--, ' ');
      }
      if (stringBuffer.charAt(currentPos) == '.') {
        stringBuffer.setCharAt(currentPos, ' ');
      }
      
      return stringBuffer.toString().trim();
    }
    return new String("" + value);
  }
  










  public static String doubleToString(double value, int width, int afterDecimalPoint)
  {
    String tempString = doubleToString(value, afterDecimalPoint);
    


    if ((afterDecimalPoint >= width) || (tempString.indexOf('E') != -1))
    {

      return tempString;
    }
    

    char[] result = new char[width];
    for (int i = 0; i < result.length; i++) {
      result[i] = ' ';
    }
    int dotPosition;
    if (afterDecimalPoint > 0)
    {
      int dotPosition = tempString.indexOf('.');
      if (dotPosition == -1) {
        dotPosition = tempString.length();
      } else {
        result[(width - afterDecimalPoint - 1)] = '.';
      }
    } else {
      dotPosition = tempString.length();
    }
    
    int offset = width - afterDecimalPoint - dotPosition;
    if (afterDecimalPoint > 0) {
      offset--;
    }
    

    if (offset < 0) {
      return tempString;
    }
    

    for (int i = 0; i < dotPosition; i++) {
      result[(offset + i)] = tempString.charAt(i);
    }
    

    for (int i = dotPosition + 1; i < tempString.length(); i++) {
      result[(offset + i)] = tempString.charAt(i);
    }
    
    return new String(result);
  }
  






  public static Class getArrayClass(Class c)
  {
    if (c.getComponentType().isArray()) {
      return getArrayClass(c.getComponentType());
    }
    return c.getComponentType();
  }
  








  public static int getArrayDimensions(Class array)
  {
    if (array.getComponentType().isArray()) {
      return 1 + getArrayDimensions(array.getComponentType());
    }
    return 1;
  }
  








  public static int getArrayDimensions(Object array)
  {
    return getArrayDimensions(array.getClass());
  }
  











  public static String arrayToString(Object array)
  {
    String result = "";
    int dimensions = getArrayDimensions(array);
    
    if (dimensions == 0) {
      result = "null";
    } else { if (dimensions == 1) {
        for (int i = 0; i < Array.getLength(array); i++) {
          if (i > 0) {
            result = result + ",";
          }
          if (Array.get(array, i) == null) {
            result = result + "null";
          } else {
            result = result + Array.get(array, i).toString();
          }
        }
      }
      for (int i = 0; i < Array.getLength(array); i++) {
        if (i > 0) {
          result = result + ",";
        }
        result = result + "[" + arrayToString(Array.get(array, i)) + "]";
      }
    }
    
    return result;
  }
  






  public static boolean eq(double a, double b)
  {
    return (a == b) || ((a - b < SMALL) && (b - a < SMALL));
  }
  






  public static void checkForRemainingOptions(String[] options)
    throws Exception
  {
    int illegalOptionsFound = 0;
    StringBuffer text = new StringBuffer();
    
    if (options == null) {
      return;
    }
    for (String option : options) {
      if (option.length() > 0) {
        illegalOptionsFound++;
        text.append(option + ' ');
      }
    }
    if (illegalOptionsFound > 0) {
      throw new Exception("Illegal options: " + text);
    }
  }
  









  public static boolean getFlag(char flag, String[] options)
    throws Exception
  {
    return getFlag("" + flag, options);
  }
  









  public static boolean getFlag(String flag, String[] options)
    throws Exception
  {
    int pos = getOptionPos(flag, options);
    
    if (pos > -1) {
      options[pos] = "";
    }
    
    return pos > -1;
  }
  










  public static String getOption(char flag, String[] options)
    throws Exception
  {
    return getOption("" + flag, options);
  }
  











  public static String getOption(String flag, String[] options)
    throws Exception
  {
    int i = getOptionPos(flag, options);
    
    if (i > -1) {
      if (options[i].equals("-" + flag)) {
        if (i + 1 == options.length) {
          throw new Exception("No value given for -" + flag + " option.");
        }
        options[i] = "";
        String newString = new String(options[(i + 1)]);
        options[(i + 1)] = "";
        return newString;
      }
      if (options[i].charAt(1) == '-') {
        return "";
      }
    }
    
    return "";
  }
  







  public static int getOptionPos(char flag, String[] options)
  {
    return getOptionPos("" + flag, options);
  }
  







  public static int getOptionPos(String flag, String[] options)
  {
    if (options == null) {
      return -1;
    }
    
    for (int i = 0; i < options.length; i++) {
      if ((options[i].length() > 0) && (options[i].charAt(0) == '-')) {
        try
        {
          Double.valueOf(options[i]);
        }
        catch (NumberFormatException e) {
          if (options[i].equals("-" + flag)) {
            return i;
          }
          
          if (options[i].charAt(1) == '-') {
            return -1;
          }
        }
      }
    }
    
    return -1;
  }
  


















  public static String quote(String string)
  {
    boolean quote = false;
    

    if ((string.indexOf('\n') != -1) || (string.indexOf('\r') != -1) || (string.indexOf('\'') != -1) || (string.indexOf('"') != -1) || (string.indexOf('\\') != -1) || (string.indexOf('\t') != -1) || (string.indexOf('%') != -1) || (string.indexOf('\036') != -1))
    {


      string = backQuoteChars(string);
      quote = true;
    }
    


    if ((quote == true) || (string.indexOf('{') != -1) || (string.indexOf('}') != -1) || (string.indexOf(',') != -1) || (string.equals("?")) || (string.indexOf(' ') != -1) || (string.equals("")))
    {


      string = "'".concat(string).concat("'");
    }
    
    return string;
  }
  







  public static String unquote(String string)
  {
    if ((string.startsWith("'")) && (string.endsWith("'"))) {
      string = string.substring(1, string.length() - 1);
      
      if ((string.indexOf("\\n") != -1) || (string.indexOf("\\r") != -1) || (string.indexOf("\\'") != -1) || (string.indexOf("\\\"") != -1) || (string.indexOf("\\\\") != -1) || (string.indexOf("\\t") != -1) || (string.indexOf("\\%") != -1) || (string.indexOf("\\u001E") != -1))
      {


        string = unbackQuoteChars(string);
      }
    }
    
    return string;
  }
  












  public static String backQuoteChars(String string)
  {
    char[] charsFind = { '\\', '\'', '\t', '\n', '\r', '"', '%', '\036' };
    String[] charsReplace = { "\\\\", "\\'", "\\t", "\\n", "\\r", "\\\"", "\\%", "\\u001E" };
    
    for (int i = 0; i < charsFind.length; i++) {
      if (string.indexOf(charsFind[i]) != -1) {
        StringBuffer newStringBuffer = new StringBuffer();
        int index; while ((index = string.indexOf(charsFind[i])) != -1) {
          if (index > 0) {
            newStringBuffer.append(string.substring(0, index));
          }
          newStringBuffer.append(charsReplace[i]);
          if (index + 1 < string.length()) {
            string = string.substring(index + 1);
          } else {
            string = "";
          }
        }
        newStringBuffer.append(string);
        string = newStringBuffer.toString();
      }
    }
    
    return string;
  }
  








  public static String convertNewLines(String string)
  {
    StringBuffer newStringBuffer = new StringBuffer();
    int index; while ((index = string.indexOf('\n')) != -1) {
      if (index > 0) {
        newStringBuffer.append(string.substring(0, index));
      }
      newStringBuffer.append('\\');
      newStringBuffer.append('n');
      if (index + 1 < string.length()) {
        string = string.substring(index + 1);
      } else {
        string = "";
      }
    }
    newStringBuffer.append(string);
    string = newStringBuffer.toString();
    

    newStringBuffer = new StringBuffer();
    while ((index = string.indexOf('\r')) != -1) {
      if (index > 0) {
        newStringBuffer.append(string.substring(0, index));
      }
      newStringBuffer.append('\\');
      newStringBuffer.append('r');
      if (index + 1 < string.length()) {
        string = string.substring(index + 1);
      } else {
        string = "";
      }
    }
    newStringBuffer.append(string);
    return newStringBuffer.toString();
  }
  








  public static String revertNewLines(String string)
  {
    StringBuffer newStringBuffer = new StringBuffer();
    int index; while ((index = string.indexOf("\\n")) != -1) {
      if (index > 0) {
        newStringBuffer.append(string.substring(0, index));
      }
      newStringBuffer.append('\n');
      if (index + 2 < string.length()) {
        string = string.substring(index + 2);
      } else {
        string = "";
      }
    }
    newStringBuffer.append(string);
    string = newStringBuffer.toString();
    

    newStringBuffer = new StringBuffer();
    while ((index = string.indexOf("\\r")) != -1) {
      if (index > 0) {
        newStringBuffer.append(string.substring(0, index));
      }
      newStringBuffer.append('\r');
      if (index + 2 < string.length()) {
        string = string.substring(index + 2);
      } else {
        string = "";
      }
    }
    newStringBuffer.append(string);
    
    return newStringBuffer.toString();
  }
  








  public static String[] partitionOptions(String[] options)
  {
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals("--")) {
        options[(i++)] = "";
        String[] result = new String[options.length - i];
        for (int j = i; j < options.length; j++) {
          result[(j - i)] = options[j];
          options[j] = "";
        }
        return result;
      }
    }
    return new String[0];
  }
  













  public static String unbackQuoteChars(String string)
  {
    String[] charsFind = { "\\\\", "\\'", "\\t", "\\n", "\\r", "\\\"", "\\%", "\\u001E" };
    
    char[] charsReplace = { '\\', '\'', '\t', '\n', '\r', '"', '%', '\036' };
    int[] pos = new int[charsFind.length];
    

    String str = new String(string);
    StringBuffer newStringBuffer = new StringBuffer();
    while (str.length() > 0)
    {
      int curPos = str.length();
      int index = -1;
      for (int i = 0; i < pos.length; i++) {
        pos[i] = str.indexOf(charsFind[i]);
        if ((pos[i] > -1) && (pos[i] < curPos)) {
          index = i;
          curPos = pos[i];
        }
      }
      

      if (index == -1) {
        newStringBuffer.append(str);
        str = "";
      } else {
        newStringBuffer.append(str.substring(0, pos[index]));
        newStringBuffer.append(charsReplace[index]);
        str = str.substring(pos[index] + charsFind[index].length());
      }
    }
    
    return newStringBuffer.toString();
  }
  









  public static String[] splitOptions(String quotedOptionString)
    throws Exception
  {
    FastVector optionsVec = new FastVector();
    String str = new String(quotedOptionString);
    


    for (;;)
    {
      i = 0;
      while ((i < str.length()) && (Character.isWhitespace(str.charAt(i)))) {
        i++;
      }
      str = str.substring(i);
      

      if (str.length() == 0) {
        break;
      }
      

      if (str.charAt(0) == '"')
      {

        i = 1;
        while ((i < str.length()) && 
          (str.charAt(i) != str.charAt(0)))
        {

          if (str.charAt(i) == '\\') {
            i++;
            if (i >= str.length()) {
              throw new Exception("String should not finish with \\");
            }
          }
          i++;
        }
        if (i >= str.length()) {
          throw new Exception("Quote parse error.");
        }
        

        String optStr = str.substring(1, i);
        optStr = unbackQuoteChars(optStr);
        optionsVec.addElement(optStr);
        str = str.substring(i + 1);
      }
      else {
        i = 0;
        while ((i < str.length()) && (!Character.isWhitespace(str.charAt(i)))) {
          i++;
        }
        

        String optStr = str.substring(0, i);
        optionsVec.addElement(optStr);
        str = str.substring(i);
      }
    }
    

    String[] options = new String[optionsVec.size()];
    for (int i = 0; i < optionsVec.size(); i++) {
      options[i] = ((String)optionsVec.elementAt(i));
    }
    return options;
  }
  







  public static String joinOptions(String[] optionArray)
  {
    String optionString = "";
    for (String element : optionArray)
      if (!element.equals(""))
      {

        boolean escape = false;
        for (int n = 0; n < element.length(); n++) {
          if (Character.isWhitespace(element.charAt(n))) {
            escape = true;
            break;
          }
        }
        if (escape) {
          optionString = optionString + '"' + backQuoteChars(element) + '"';
        } else {
          optionString = optionString + element;
        }
        optionString = optionString + " ";
      }
    return optionString.trim();
  }
  


























  public static Object forName(Class classType, String className, String[] options)
    throws Exception
  {
    Class c = null;
    try {
      c = Class.forName(className);
    } catch (Exception ex) {
      throw new Exception("Can't find class called: " + className);
    }
    if (!classType.isAssignableFrom(c)) {
      throw new Exception(classType.getName() + " is not assignable from " + className);
    }
    
    Object o = c.newInstance();
    if (((o instanceof OptionHandler)) && (options != null)) {
      ((OptionHandler)o).setOptions(options);
      checkForRemainingOptions(options);
    }
    return o;
  }
  







  public static double info(int[] counts)
  {
    int total = 0;
    double x = 0.0D;
    for (int count : counts) {
      x -= xlogx(count);
      total += count;
    }
    return x + xlogx(total);
  }
  






  public static boolean smOrEq(double a, double b)
  {
    return (a - b < SMALL) || (a <= b);
  }
  






  public static boolean grOrEq(double a, double b)
  {
    return (b - a < SMALL) || (a >= b);
  }
  






  public static boolean sm(double a, double b)
  {
    return b - a > SMALL;
  }
  






  public static boolean gr(double a, double b)
  {
    return a - b > SMALL;
  }
  







  public static int kthSmallestValue(int[] array, int k)
  {
    int[] index = initialIndex(array.length);
    return array[index[select(array, index, 0, array.length - 1, k)]];
  }
  







  public static double kthSmallestValue(double[] array, int k)
  {
    int[] index = initialIndex(array.length);
    return array[index[select(array, index, 0, array.length - 1, k)]];
  }
  






  public static double log2(double a)
  {
    return Math.log(a) / log2;
  }
  







  public static int maxIndex(double[] doubles)
  {
    double maximum = 0.0D;
    int maxIndex = 0;
    
    for (int i = 0; i < doubles.length; i++) {
      if ((i == 0) || (doubles[i] > maximum)) {
        maxIndex = i;
        maximum = doubles[i];
      }
    }
    
    return maxIndex;
  }
  







  public static int maxIndex(int[] ints)
  {
    int maximum = 0;
    int maxIndex = 0;
    
    for (int i = 0; i < ints.length; i++) {
      if ((i == 0) || (ints[i] > maximum)) {
        maxIndex = i;
        maximum = ints[i];
      }
    }
    
    return maxIndex;
  }
  






  public static double mean(double[] vector)
  {
    double sum = 0.0D;
    
    if (vector.length == 0) {
      return 0.0D;
    }
    for (double element : vector) {
      sum += element;
    }
    return sum / vector.length;
  }
  







  public static int minIndex(int[] ints)
  {
    int minimum = 0;
    int minIndex = 0;
    
    for (int i = 0; i < ints.length; i++) {
      if ((i == 0) || (ints[i] < minimum)) {
        minIndex = i;
        minimum = ints[i];
      }
    }
    
    return minIndex;
  }
  







  public static int minIndex(double[] doubles)
  {
    double minimum = 0.0D;
    int minIndex = 0;
    
    for (int i = 0; i < doubles.length; i++) {
      if ((i == 0) || (doubles[i] < minimum)) {
        minIndex = i;
        minimum = doubles[i];
      }
    }
    
    return minIndex;
  }
  






  public static void normalize(double[] doubles)
  {
    double sum = 0.0D;
    for (double d : doubles) {
      sum += d;
    }
    normalize(doubles, sum);
  }
  







  public static void normalize(double[] doubles, double sum)
  {
    if (Double.isNaN(sum)) {
      throw new IllegalArgumentException("Can't normalize array. Sum is NaN.");
    }
    if (sum == 0.0D)
    {
      throw new IllegalArgumentException("Can't normalize array. Sum is zero.");
    }
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] /= sum;
    }
  }
  








  public static double[] logs2probs(double[] a)
  {
    double max = a[maxIndex(a)];
    double sum = 0.0D;
    
    double[] result = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = Math.exp(a[i] - max);
      sum += result[i];
    }
    
    normalize(result, sum);
    
    return result;
  }
  








  public static double probToLogOdds(double prob)
  {
    if ((gr(prob, 1.0D)) || (sm(prob, 0.0D))) {
      throw new IllegalArgumentException("probToLogOdds: probability must be in [0,1] " + prob);
    }
    
    double p = SMALL + (1.0D - 2.0D * SMALL) * prob;
    return Math.log(p / (1.0D - p));
  }
  







  public static int round(double value)
  {
    int roundedValue = value > 0.0D ? (int)(value + 0.5D) : -(int)(Math.abs(value) + 0.5D);
    

    return roundedValue;
  }
  










  public static int probRound(double value, Random rand)
  {
    if (value >= 0.0D) {
      double lower = Math.floor(value);
      double prob = value - lower;
      if (rand.nextDouble() < prob) {
        return (int)lower + 1;
      }
      return (int)lower;
    }
    
    double lower = Math.floor(Math.abs(value));
    double prob = Math.abs(value) - lower;
    if (rand.nextDouble() < prob) {
      return -((int)lower + 1);
    }
    return -(int)lower;
  }
  








  public static void replaceMissingWithMAX_VALUE(double[] array)
  {
    for (int i = 0; i < array.length; i++) {
      if (Instance.isMissingValue(array[i])) {
        array[i] = Double.MAX_VALUE;
      }
    }
  }
  








  public static double roundDouble(double value, int afterDecimalPoint)
  {
    double mask = Math.pow(10.0D, afterDecimalPoint);
    
    return Math.round(value * mask) / mask;
  }
  









  public static int[] sort(int[] array)
  {
    int[] index = new int[array.length];
    int[] newIndex = new int[array.length];
    


    for (int i = 0; i < index.length; i++) {
      index[i] = i;
    }
    quickSort(array, index, 0, array.length - 1);
    

    int i = 0;
    while (i < index.length) {
      int numEqual = 1;
      for (int j = i + 1; (j < index.length) && (array[index[i]] == array[index[j]]); j++) {
        numEqual++;
      }
      if (numEqual > 1) {
        int[] helpIndex = new int[numEqual];
        for (int j = 0; j < numEqual; j++) {
          helpIndex[j] = (i + j);
        }
        quickSort(index, helpIndex, 0, numEqual - 1);
        for (int j = 0; j < numEqual; j++) {
          newIndex[(i + j)] = index[helpIndex[j]];
        }
        i += numEqual;
      } else {
        newIndex[i] = index[i];
        i++;
      }
    }
    return newIndex;
  }
  










  public static int[] sort(double[] array)
  {
    int[] index = initialIndex(array.length);
    if (array.length > 1) {
      array = (double[])array.clone();
      replaceMissingWithMAX_VALUE(array);
      quickSort(array, index, 0, array.length - 1);
    }
    return index;
  }
  











  public static int[] sortWithNoMissingValues(double[] array)
  {
    int[] index = initialIndex(array.length);
    if (array.length > 1) {
      quickSort(array, index, 0, array.length - 1);
    }
    return index;
  }
  









  public static int[] stableSort(double[] array)
  {
    int[] index = initialIndex(array.length);
    
    if (array.length > 1)
    {
      int[] newIndex = new int[array.length];
      


      array = (double[])array.clone();
      replaceMissingWithMAX_VALUE(array);
      quickSort(array, index, 0, array.length - 1);
      


      int i = 0;
      while (i < index.length) {
        int numEqual = 1;
        for (int j = i + 1; (j < index.length) && (eq(array[index[i]], array[index[j]])); 
            j++) {
          numEqual++;
        }
        if (numEqual > 1) {
          int[] helpIndex = new int[numEqual];
          for (int j = 0; j < numEqual; j++) {
            helpIndex[j] = (i + j);
          }
          quickSort(index, helpIndex, 0, numEqual - 1);
          for (int j = 0; j < numEqual; j++) {
            newIndex[(i + j)] = index[helpIndex[j]];
          }
          i += numEqual;
        } else {
          newIndex[i] = index[i];
          i++;
        }
      }
      return newIndex;
    }
    return index;
  }
  







  public static double variance(double[] vector)
  {
    double sum = 0.0D;double sumSquared = 0.0D;
    
    if (vector.length <= 1) {
      return 0.0D;
    }
    for (double element : vector) {
      sum += element;
      sumSquared += element * element;
    }
    double result = (sumSquared - sum * sum / vector.length) / (vector.length - 1);
    


    if (result < 0.0D) {
      return 0.0D;
    }
    return result;
  }
  







  public static double sum(double[] doubles)
  {
    double sum = 0.0D;
    
    for (double d : doubles) {
      sum += d;
    }
    return sum;
  }
  






  public static int sum(int[] ints)
  {
    int sum = 0;
    
    for (int j : ints) {
      sum += j;
    }
    return sum;
  }
  






  public static double xlogx(int c)
  {
    if (c == 0) {
      return 0.0D;
    }
    return c * log2(c);
  }
  



  private static int[] initialIndex(int size)
  {
    int[] index = new int[size];
    for (int i = 0; i < size; i++) {
      index[i] = i;
    }
    return index;
  }
  





  private static int sortLeftRightAndCenter(double[] array, int[] index, int l, int r)
  {
    int c = (l + r) / 2;
    conditionalSwap(array, index, l, c);
    conditionalSwap(array, index, l, r);
    conditionalSwap(array, index, c, r);
    return c;
  }
  



  private static void swap(int[] index, int l, int r)
  {
    int help = index[l];
    index[l] = index[r];
    index[r] = help;
  }
  




  private static void conditionalSwap(double[] array, int[] index, int left, int right)
  {
    if (array[index[left]] > array[index[right]]) {
      int help = index[left];
      index[left] = index[right];
      index[right] = help;
    }
  }
  






  private static int partition(double[] array, int[] index, int l, int r, double pivot)
  {
    
    




    for (;;)
    {
      if (array[index[(++l)]] >= pivot)
      {

        while (array[index[(--r)]] > pivot) {}
        

        if (l >= r) {
          return l;
        }
        swap(index, l, r);
      }
    }
  }
  










  private static int partition(int[] array, int[] index, int l, int r)
  {
    double pivot = array[index[((l + r) / 2)]];
    

    while (l < r) {
      while ((array[index[l]] < pivot) && (l < r)) {
        l++;
      }
      while ((array[index[r]] > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        int help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (array[index[r]] > pivot)) {
      r--;
    }
    
    return r;
  }
  















  private static void quickSort(double[] array, int[] index, int left, int right)
  {
    int diff = right - left;
    
    switch (diff)
    {

    case 0: 
      return;
    

    case 1: 
      conditionalSwap(array, index, left, right);
      return;
    

    case 2: 
      conditionalSwap(array, index, left, left + 1);
      conditionalSwap(array, index, left, right);
      conditionalSwap(array, index, left + 1, right);
      return;
    }
    
    
    int pivotLocation = sortLeftRightAndCenter(array, index, left, right);
    

    swap(index, pivotLocation, right - 1);
    int center = partition(array, index, left, right, array[index[(right - 1)]]);
    swap(index, center, right - 1);
    

    quickSort(array, index, left, center - 1);
    quickSort(array, index, center + 1, right);
  }
  















  private static void quickSort(int[] array, int[] index, int left, int right)
  {
    if (left < right) {
      int middle = partition(array, index, left, right);
      quickSort(array, index, left, middle);
      quickSort(array, index, middle + 1, right);
    }
  }
  














  private static int select(double[] array, int[] index, int left, int right, int k)
  {
    int diff = right - left;
    switch (diff)
    {

    case 0: 
      return left;
    

    case 1: 
      conditionalSwap(array, index, left, right);
      return left + k - 1;
    

    case 2: 
      conditionalSwap(array, index, left, left + 1);
      conditionalSwap(array, index, left, right);
      conditionalSwap(array, index, left + 1, right);
      return left + k - 1;
    }
    
    
    int pivotLocation = sortLeftRightAndCenter(array, index, left, right);
    

    swap(index, pivotLocation, right - 1);
    int center = partition(array, index, left, right, array[index[(right - 1)]]);
    swap(index, center, right - 1);
    

    if (center - left + 1 >= k) {
      return select(array, index, left, center, k);
    }
    return select(array, index, center + 1, right, k - (center - left + 1));
  }
  












  public static File convertToRelativePath(File absolute)
    throws Exception
  {
    File result = null;
    

    if (File.separator.equals("\\")) {
      try
      {
        String fileStr = absolute.getPath();
        fileStr = fileStr.substring(0, 1).toLowerCase() + fileStr.substring(1);
        result = createRelativePath(new File(fileStr));
      }
      catch (Exception e) {
        result = createRelativePath(absolute);
      }
    } else {
      result = createRelativePath(absolute);
    }
    
    return result;
  }
  






  protected static File createRelativePath(File absolute)
    throws Exception
  {
    File userDir = new File(System.getProperty("user.dir"));
    String userPath = userDir.getAbsolutePath() + File.separator;
    String targetPath = new File(absolute.getParent()).getPath() + File.separator;
    
    String fileName = absolute.getName();
    StringBuffer relativePath = new StringBuffer();
    




    int subdir = targetPath.indexOf(userPath);
    if (subdir == 0) {
      if (userPath.length() == targetPath.length()) {
        relativePath.append(fileName);
      } else {
        int ll = userPath.length();
        relativePath.append(targetPath.substring(ll));
        relativePath.append(fileName);
      }
    } else {
      int sepCount = 0;
      String temp = new String(userPath);
      while (temp.indexOf(File.separator) != -1) {
        int ind = temp.indexOf(File.separator);
        sepCount++;
        temp = temp.substring(ind + 1, temp.length());
      }
      
      String targetTemp = new String(targetPath);
      String userTemp = new String(userPath);
      int tcount = 0;
      while (targetTemp.indexOf(File.separator) != -1) {
        int ind = targetTemp.indexOf(File.separator);
        int ind2 = userTemp.indexOf(File.separator);
        String tpart = targetTemp.substring(0, ind + 1);
        String upart = userTemp.substring(0, ind2 + 1);
        if (tpart.compareTo(upart) != 0) {
          if (tcount != 0) break;
          tcount = -1; break;
        }
        

        tcount++;
        targetTemp = targetTemp.substring(ind + 1, targetTemp.length());
        userTemp = userTemp.substring(ind2 + 1, userTemp.length());
      }
      if (tcount == -1)
      {
        throw new Exception("Can't construct a path to file relative to user dir.");
      }
      
      if (targetTemp.indexOf(File.separator) == -1) {
        targetTemp = "";
      }
      for (int i = 0; i < sepCount - tcount; i++) {
        relativePath.append(".." + File.separator);
      }
      relativePath.append(targetTemp + fileName);
    }
    
    return new File(relativePath.toString());
  }
  














  private static int select(int[] array, int[] index, int left, int right, int k)
  {
    if (left == right) {
      return left;
    }
    int middle = partition(array, index, left, right);
    if (middle - left + 1 >= k) {
      return select(array, index, left, middle, k);
    }
    return select(array, index, middle + 1, right, k - (middle - left + 1));
  }
  












  public static String getGlobalInfo(Object object, boolean addCapabilities)
  {
    String gi = null;
    StringBuilder result = new StringBuilder();
    try {
      BeanInfo bi = Introspector.getBeanInfo(object.getClass());
      MethodDescriptor[] methods = bi.getMethodDescriptors();
      for (MethodDescriptor method : methods) {
        String name = method.getDisplayName();
        Method meth = method.getMethod();
        if ((name.equals("globalInfo")) && 
          (meth.getReturnType().equals(String.class))) {
          Object[] args = new Object[0];
          String globalInfo = (String)meth.invoke(object, args);
          gi = globalInfo;
          break;
        }
      }
    }
    catch (Exception ex) {}
    



    int lineWidth = 180;
    
    result.append("<html>");
    
    if ((gi != null) && (gi.length() > 0))
    {
      StringBuilder firstLine = new StringBuilder();
      firstLine.append("<font color=blue>");
      boolean addFirstBreaks = true;
      int indexOfDot = gi.indexOf(".");
      if (indexOfDot > 0) {
        firstLine.append(gi.substring(0, gi.indexOf(".")));
        if (gi.length() - indexOfDot < 3) {
          addFirstBreaks = false;
        }
        gi = gi.substring(indexOfDot + 1, gi.length());
      } else {
        firstLine.append(gi);
        gi = "";
      }
      firstLine.append("</font>");
      if ((addFirstBreaks) && (!gi.startsWith("\n\n"))) {
        if (!gi.startsWith("\n")) {
          firstLine.append("<br>");
        }
        firstLine.append("<br>");
      }
      result.append(lineWrap(firstLine.toString(), lineWidth));
      
      result.append(lineWrap(gi, lineWidth).replace("\n", "<br>"));
      result.append("<br>");
    }
    
    if (addCapabilities) {
      if ((object instanceof CapabilitiesHandler)) {
        if (!result.toString().endsWith("<br><br>")) {
          result.append("<br>");
        }
        String caps = PropertySheetPanel.addCapabilities("<font color=red>CAPABILITIES</font>", ((CapabilitiesHandler)object).getCapabilities());
        

        caps = lineWrap(caps, lineWidth).replace("\n", "<br>");
        result.append(caps);
      }
      
      if ((object instanceof MultiInstanceCapabilitiesHandler)) {
        result.append("<br>");
        String caps = PropertySheetPanel.addCapabilities("<font color=red>MI CAPABILITIES</font>", ((MultiInstanceCapabilitiesHandler)object).getMultiInstanceCapabilities());
        


        caps = lineWrap(caps, lineWidth).replace("\n", "<br>");
        result.append(caps);
      }
    }
    
    result.append("</html>");
    
    if (result.toString().equals("<html></html>")) {
      return null;
    }
    return result.toString();
  }
  










  public static String lineWrap(String input, int maxLineWidth)
  {
    StringBuffer sb = new StringBuffer();
    BreakIterator biterator = BreakIterator.getLineInstance();
    biterator.setText(input);
    int linestart = 0;
    int previous = 0;
    for (;;) {
      int next = biterator.next();
      String toAdd = input.substring(linestart, previous);
      if (next == -1) {
        sb.append(toAdd);
        break;
      }
      if (next - linestart > maxLineWidth) {
        sb.append(toAdd + '\n');
        linestart = previous;
      } else {
        int newLineIndex = toAdd.lastIndexOf('\n');
        if (newLineIndex != -1) {
          sb.append(toAdd.substring(0, newLineIndex + 1));
          linestart += newLineIndex + 1;
        }
      }
      previous = next;
    }
    return sb.toString();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10570 $");
  }
  





  public static void main(String[] ops)
  {
    double[] doublesWithNaN = { 4.5D, 6.7D, NaN.0D, 3.4D, 4.8D, 1.2D, 3.4D };
    double[] doubles = { 4.5D, 6.7D, 6.7D, 3.4D, 4.8D, 1.2D, 3.4D, 6.7D, 6.7D, 3.4D };
    int[] ints = { 12, 6, 2, 18, 16, 6, 7, 5, 18, 18, 17 };
    

    try
    {
      System.out.println("First option split up:");
      if (ops.length > 0) {
        String[] firstOptionSplitUp = splitOptions(ops[0]);
        for (String element : firstOptionSplitUp) {
          System.out.println(element);
        }
      }
      System.out.println("Partitioned options: ");
      String[] partitionedOptions = partitionOptions(ops);
      for (String partitionedOption : partitionedOptions) {
        System.out.println(partitionedOption);
      }
      System.out.println("Get position of flag -f: " + getOptionPos('f', ops));
      
      System.out.println("Get flag -f: " + getFlag('f', ops));
      System.out.println("Get position of option -o: " + getOptionPos('o', ops));
      
      System.out.println("Get option -o: " + getOption('o', ops));
      System.out.println("Checking for remaining options... ");
      checkForRemainingOptions(ops);
      

      System.out.println("Original array with NaN (doubles): ");
      for (double element : doublesWithNaN) {
        System.out.print(element + " ");
      }
      System.out.println();
      System.out.println("Original array (doubles): ");
      for (double d : doubles) {
        System.out.print(d + " ");
      }
      System.out.println();
      System.out.println("Original array (ints): ");
      for (int j : ints) {
        System.out.print(j + " ");
      }
      System.out.println();
      System.out.println("Correlation: " + correlation(doubles, doubles, doubles.length));
      
      System.out.println("Mean: " + mean(doubles));
      System.out.println("Variance: " + variance(doubles));
      System.out.println("Sum (doubles): " + sum(doubles));
      System.out.println("Sum (ints): " + sum(ints));
      System.out.println("Max index (doubles): " + maxIndex(doubles));
      System.out.println("Max index (ints): " + maxIndex(ints));
      System.out.println("Min index (doubles): " + minIndex(doubles));
      System.out.println("Min index (ints): " + minIndex(ints));
      System.out.println("Median (doubles): " + kthSmallestValue(doubles, doubles.length / 2));
      
      System.out.println("Median (ints): " + kthSmallestValue(ints, ints.length / 2));
      


      System.out.println("Sorted array with NaN (doubles): ");
      int[] sorted = sort(doublesWithNaN);
      for (int i = 0; i < doublesWithNaN.length; i++) {
        System.out.print(doublesWithNaN[sorted[i]] + " ");
      }
      System.out.println();
      System.out.println("Sorted array (doubles): ");
      sorted = sort(doubles);
      for (int i = 0; i < doubles.length; i++) {
        System.out.print(doubles[sorted[i]] + " ");
      }
      System.out.println();
      System.out.println("Sorted array (ints): ");
      sorted = sort(ints);
      for (int i = 0; i < ints.length; i++) {
        System.out.print(ints[sorted[i]] + " ");
      }
      System.out.println();
      System.out.println("Indices from stable sort (doubles): ");
      sorted = stableSort(doubles);
      for (int i = 0; i < doubles.length; i++) {
        System.out.print(sorted[i] + " ");
      }
      System.out.println();
      System.out.println("Indices from sort (ints): ");
      sorted = sort(ints);
      for (int i = 0; i < ints.length; i++) {
        System.out.print(sorted[i] + " ");
      }
      System.out.println();
      System.out.println("Normalized array (doubles): ");
      normalize(doubles);
      for (double d : doubles) {
        System.out.print(d + " ");
      }
      System.out.println();
      System.out.println("Normalized again (doubles): ");
      normalize(doubles, sum(doubles));
      for (double d : doubles) {
        System.out.print(d + " ");
      }
      System.out.println();
      

      System.out.println("-4.58: " + doubleToString(-4.57826535D, 2));
      System.out.println("-6.78: " + doubleToString(-6.78214234D, 6, 2));
      

      System.out.println("5.70001 == 5.7 ? " + eq(5.70001D, 5.7D));
      System.out.println("5.70001 > 5.7 ? " + gr(5.70001D, 5.7D));
      System.out.println("5.70001 >= 5.7 ? " + grOrEq(5.70001D, 5.7D));
      System.out.println("5.7 < 5.70001 ? " + sm(5.7D, 5.70001D));
      System.out.println("5.7 <= 5.70001 ? " + smOrEq(5.7D, 5.70001D));
      

      System.out.println("Info (ints): " + info(ints));
      System.out.println("log2(4.6): " + log2(4.6D));
      System.out.println("5 * log(5): " + xlogx(5));
      System.out.println("5.5 rounded: " + round(5.5D));
      System.out.println("5.55555 rounded to 2 decimal places: " + roundDouble(5.55555D, 2));
      


      System.out.println("Array-Dimensions of 'new int[][]': " + getArrayDimensions(new int[0][]));
      
      System.out.println("Array-Dimensions of 'new int[][]{{1,2,3},{4,5,6}}': " + getArrayDimensions(new int[][] { { 1, 2, 3 }, { 4, 5, 6 } }));
      
      String[][][] s = new String[3][4][];
      System.out.println("Array-Dimensions of 'new String[3][4][]': " + getArrayDimensions(s));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
