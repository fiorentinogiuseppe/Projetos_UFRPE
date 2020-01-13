package weka.core.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;































public class SerialUIDChanger
  implements RevisionHandler
{
  public SerialUIDChanger() {}
  
  protected static boolean checkKOML()
    throws Exception
  {
    if (!KOML.isPresent()) {
      throw new Exception("KOML is not present!");
    }
    return true;
  }
  






  public static boolean isKOML(String filename)
  {
    return filename.toLowerCase().endsWith(".koml");
  }
  










  protected static Object readBinary(String binary)
    throws Exception
  {
    FileInputStream fi = new FileInputStream(binary);
    ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
    Object o = oi.readObject();
    oi.close();
    
    return o;
  }
  








  protected static void writeBinary(String binary, Object o)
    throws Exception
  {
    FileOutputStream fo = new FileOutputStream(binary);
    ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
    oo.writeObject(o);
    oo.close();
  }
  








  public static void binaryToKOML(String binary, String koml)
    throws Exception
  {
    checkKOML();
    

    Object o = readBinary(binary);
    if (o == null) {
      throw new Exception("Failed to deserialize object from binary file '" + binary + "'!");
    }
    
    KOML.write(koml, o);
  }
  







  public static void komlToBinary(String koml, String binary)
    throws Exception
  {
    checkKOML();
    

    Object o = KOML.read(koml);
    if (o == null) {
      throw new Exception("Failed to deserialize object from XML file '" + koml + "'!");
    }
    
    writeBinary(binary, o);
  }
  









  public static void changeUID(long oldUID, long newUID, String fromFile, String toFile)
    throws Exception
  {
    String inputFile;
    







    if (!isKOML(fromFile)) {
      String inputFile = fromFile + ".koml";
      binaryToKOML(fromFile, inputFile);
    }
    else {
      inputFile = fromFile;
    }
    

    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
    String content = "";
    String line; while ((line = reader.readLine()) != null) {
      if (!content.equals(""))
        content = content + "\n";
      content = content + line;
    }
    reader.close();
    

    content = content.replaceAll(" uid='" + Long.toString(oldUID) + "'", " uid='" + Long.toString(newUID) + "'");
    

    String tempFile = inputFile + ".temp";
    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
    writer.write(content);
    writer.flush();
    writer.close();
    

    if (!isKOML(toFile)) {
      komlToBinary(tempFile, toFile);
    }
    else {
      writer = new BufferedWriter(new FileWriter(toFile));
      writer.write(content);
      writer.flush();
      writer.close();
    }
    

    File file = new File(tempFile);
    file.delete();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  






  public static void main(String[] args)
    throws Exception
  {
    if (args.length != 4) {
      System.out.println();
      System.out.println("Usage: " + SerialUIDChanger.class.getName() + " <oldUID> <newUID> <oldFilename> <newFilename>");
      System.out.println("       <oldFilename> and <newFilename> have to be different");
      System.out.println();
    }
    else {
      if (args[2].equals(args[3])) {
        throw new Exception("Filenames have to be different!");
      }
      changeUID(Long.parseLong(args[0]), Long.parseLong(args[1]), args[2], args[3]);
    }
  }
}
