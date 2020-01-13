package weka.core.tokenizers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;




































public abstract class Tokenizer
  implements Enumeration, OptionHandler, Serializable, RevisionHandler
{
  public Tokenizer() {}
  
  public abstract String globalInfo();
  
  public Enumeration listOptions()
  {
    return new Vector().elements();
  }
  





  public String[] getOptions()
  {
    return new String[0];
  }
  









  public void setOptions(String[] options)
    throws Exception
  {}
  









  public abstract boolean hasMoreElements();
  








  public abstract Object nextElement();
  








  public abstract void tokenize(String paramString);
  








  public static String[] tokenize(Tokenizer tokenizer, String[] options)
    throws Exception
  {
    Vector<String> result = new Vector();
    

    tokenizer.setOptions(options);
    

    Vector<String> data = new Vector();
    

    boolean processed = false;
    for (int i = 0; i < options.length; i++) {
      if (options[i].length() != 0) {
        processed = true;
        data.add(options[i]);
      }
    }
    

    if (!processed) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String line; while ((line = reader.readLine()) != null) {
        data.add(line);
      }
    }
    

    for (i = 0; i < data.size(); i++) {
      Vector<String> tmpResult = new Vector();
      tokenizer.tokenize((String)data.get(i));
      while (tokenizer.hasMoreElements()) {
        tmpResult.add((String)tokenizer.nextElement());
      }
      result.addAll(tmpResult);
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  










  public static void runTokenizer(Tokenizer tokenizer, String[] options)
  {
    try
    {
      String[] result = tokenize(tokenizer, options);
      for (int i = 0; i < result.length; i++) {
        System.out.println(result[i]);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
