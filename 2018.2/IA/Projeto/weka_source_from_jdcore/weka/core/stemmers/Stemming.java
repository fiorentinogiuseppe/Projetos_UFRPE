package weka.core.stemmers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;





































public class Stemming
  implements RevisionHandler
{
  public Stemming() {}
  
  protected static String makeOptionsString(Stemmer stemmer)
  {
    Vector options = new Vector();
    

    options.add(new Option("\tDisplays this help.", "h", 0, "-h"));
    



    options.add(new Option("\tThe file to process.", "i", 1, "-i <input-file>"));
    



    options.add(new Option("\tThe file to output the processed data to (default stdout).", "o", 1, "-o <output-file>"));
    



    options.add(new Option("\tUses lowercase strings.", "l", 0, "-l"));
    




    if ((stemmer instanceof OptionHandler)) {
      Enumeration enm = ((OptionHandler)stemmer).listOptions();
      while (enm.hasMoreElements()) {
        options.add(enm.nextElement());
      }
    }
    
    StringBuffer result = new StringBuffer();
    result.append("\nStemmer options:\n\n");
    Enumeration enm = options.elements();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      result.append(option.synopsis() + "\n");
      result.append(option.description() + "\n");
    }
    
    return result.toString();
  }
  















  public static void useStemmer(Stemmer stemmer, String[] options)
    throws Exception
  {
    if (Utils.getFlag('h', options)) {
      System.out.println(makeOptionsString(stemmer));
      return;
    }
    

    String tmpStr = Utils.getOption('i', options);
    if (tmpStr.length() == 0) {
      throw new IllegalArgumentException("No input file defined!" + makeOptionsString(stemmer));
    }
    
    Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpStr)));
    

    StringBuffer input = new StringBuffer();
    

    tmpStr = Utils.getOption('o', options);
    Writer output; Writer output; if (tmpStr.length() == 0) {
      output = new BufferedWriter(new OutputStreamWriter(System.out));
    }
    else {
      output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpStr)));
    }
    

    boolean lowerCase = Utils.getFlag('l', options);
    

    if ((stemmer instanceof OptionHandler)) {
      ((OptionHandler)stemmer).setOptions(options);
    }
    try
    {
      Utils.checkForRemainingOptions(options);
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(makeOptionsString(stemmer)); return;
    }
    

    int character;
    
    while ((character = reader.read()) != -1) {
      char ch = (char)character;
      if (Character.isWhitespace(ch)) {
        if (input.length() > 0) {
          output.write(stemmer.stem(input.toString()));
          input = new StringBuffer();
        }
        output.write(ch);

      }
      else if (lowerCase) {
        input.append(Character.toLowerCase(ch));
      } else {
        input.append(ch);
      }
    }
    output.flush();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
