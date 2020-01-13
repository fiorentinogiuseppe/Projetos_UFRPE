package weka.core;

import java.io.PrintStream;






























public class RevisionUtils
{
  public RevisionUtils() {}
  
  public static enum Type
  {
    UNKNOWN, 
    
    CVS, 
    
    SUBVERSION;
    


    private Type() {}
  }
  

  public static String extract(RevisionHandler handler)
  {
    return extract(handler.getRevision());
  }
  







  public static String extract(String s)
  {
    String result = s;
    result = result.replaceAll("\\$Revision:", "");
    result = result.replaceAll("\\$", "");
    result = result.replaceAll(" ", "");
    
    return result;
  }
  






  public static Type getType(RevisionHandler handler)
  {
    return getType(extract(handler));
  }
  











  public static Type getType(String revision)
  {
    Type result = Type.UNKNOWN;
    
    try
    {
      Integer.parseInt(revision);
      result = Type.SUBVERSION;
    }
    catch (Exception e) {}
    



    if (result == Type.UNKNOWN) {
      try
      {
        if (revision.indexOf('.') == -1) {
          throw new Exception("invalid CVS revision - not dots!");
        }
        String[] parts = revision.split("\\.");
        

        if (parts.length < 2) {
          throw new Exception("invalid CVS revision - not enough parts separated by dots!");
        }
        
        for (int i = 0; i < parts.length; i++) {
          Integer.parseInt(parts[i]);
        }
        result = Type.CVS;
      }
      catch (Exception e) {}
    }
    


    return result;
  }
  





  public static void main(String[] args)
    throws Exception
  {
    if (args.length != 1) {
      System.err.println("\nUsage: " + RevisionUtils.class.getName() + " <classname>\n");
      System.exit(1);
    }
    
    RevisionHandler handler = (RevisionHandler)Class.forName(args[0]).newInstance();
    System.out.println("Type: " + getType(handler));
    System.out.println("Revision: " + extract(handler));
  }
}
