package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import weka.core.ClassDiscovery;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericPropertiesCreator;









































































public class ConverterUtils
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -2460855349276148760L;
  
  public static class DataSource
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -613122395928757332L;
    protected File m_File;
    protected URL m_URL;
    protected Loader m_Loader;
    protected boolean m_Incremental;
    protected int m_BatchCounter;
    protected Instance m_IncrementalBuffer;
    protected Instances m_BatchBuffer;
    
    public DataSource(String location)
      throws Exception
    {
      if ((location.startsWith("http://")) || (location.startsWith("https://")) || (location.startsWith("ftp://")) || (location.startsWith("file://")))
      {


        m_URL = new URL(location);
      } else {
        m_File = new File(location);
      }
      
      if (isArff(location)) {
        m_Loader = new ArffLoader();
      }
      else {
        if (m_File != null) {
          m_Loader = ConverterUtils.getLoaderForFile(location);
        } else {
          m_Loader = ConverterUtils.getURLLoaderForFile(location);
        }
        
        if (m_Loader == null) {
          throw new IllegalArgumentException("No suitable converter found for '" + location + "'!");
        }
      }
      
      m_Incremental = (m_Loader instanceof IncrementalConverter);
      
      reset();
    }
    






    public DataSource(Instances inst)
    {
      m_BatchBuffer = inst;
      m_Loader = null;
      m_File = null;
      m_URL = null;
      m_Incremental = false;
    }
    






    public DataSource(Loader loader)
    {
      m_BatchBuffer = null;
      m_Loader = loader;
      m_File = null;
      m_URL = null;
      m_Incremental = (m_Loader instanceof IncrementalConverter);
      
      initBatchBuffer();
    }
    







    public DataSource(InputStream stream)
    {
      m_BatchBuffer = null;
      m_Loader = new ArffLoader();
      try {
        m_Loader.setSource(stream);
      }
      catch (Exception e) {
        m_Loader = null;
      }
      m_File = null;
      m_URL = null;
      m_Incremental = (m_Loader instanceof IncrementalConverter);
      
      initBatchBuffer();
    }
    


    protected void initBatchBuffer()
    {
      try
      {
        if (!isIncremental()) {
          m_BatchBuffer = m_Loader.getDataSet();
        } else {
          m_BatchBuffer = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    






    public static boolean isArff(String location)
    {
      if ((location.toLowerCase().endsWith(ArffLoader.FILE_EXTENSION.toLowerCase())) || (location.toLowerCase().endsWith(ArffLoader.FILE_EXTENSION_COMPRESSED.toLowerCase())))
      {
        return true;
      }
      return false;
    }
    




    public boolean isIncremental()
    {
      return m_Incremental;
    }
    





    public Loader getLoader()
    {
      return m_Loader;
    }
    






    public Instances getDataSet()
      throws Exception
    {
      Instances result = null;
      

      reset();
      try
      {
        if (m_BatchBuffer == null) {
          result = m_Loader.getDataSet();
        } else {
          result = m_BatchBuffer;
        }
      } catch (Exception e) {
        e.printStackTrace();
        result = null;
      }
      
      return result;
    }
    








    public Instances getDataSet(int classIndex)
      throws Exception
    {
      Instances result = getDataSet();
      if (result != null) {
        result.setClassIndex(classIndex);
      }
      return result;
    }
    



    public void reset()
      throws Exception
    {
      if (m_File != null) {
        ((AbstractFileLoader)m_Loader).setFile(m_File);
      } else if (m_URL != null) {
        ((URLSourcedLoader)m_Loader).setURL(m_URL.toString());
      } else if (m_Loader != null) {
        m_Loader.reset();
      }
      m_BatchCounter = 0;
      m_IncrementalBuffer = null;
      
      if (m_Loader != null) {
        if (!isIncremental()) {
          m_BatchBuffer = m_Loader.getDataSet();
        } else {
          m_BatchBuffer = null;
        }
      }
    }
    



    public Instances getStructure()
      throws Exception
    {
      if (m_BatchBuffer == null) {
        return m_Loader.getStructure();
      }
      return new Instances(m_BatchBuffer, 0);
    }
    







    public Instances getStructure(int classIndex)
      throws Exception
    {
      Instances result = getStructure();
      if (result != null) {
        result.setClassIndex(classIndex);
      }
      return result;
    }
    









    public boolean hasMoreElements(Instances structure)
    {
      boolean result = false;
      
      if (isIncremental())
      {
        if (m_IncrementalBuffer != null) {
          result = true;
        } else {
          try
          {
            m_IncrementalBuffer = m_Loader.getNextInstance(structure);
            result = m_IncrementalBuffer != null;
          }
          catch (Exception e) {
            e.printStackTrace();
            result = false;
          }
        }
      }
      else {
        result = m_BatchCounter < m_BatchBuffer.numInstances();
      }
      
      return result;
    }
    








    public Instance nextElement(Instances dataset)
    {
      Instance result = null;
      
      if (isIncremental())
      {
        if (m_IncrementalBuffer != null) {
          result = m_IncrementalBuffer;
          m_IncrementalBuffer = null;
        }
        else {
          try {
            result = m_Loader.getNextInstance(dataset);
          }
          catch (Exception e) {
            e.printStackTrace();
            result = null;
          }
          
        }
      }
      else if (m_BatchCounter < m_BatchBuffer.numInstances()) {
        result = m_BatchBuffer.instance(m_BatchCounter);
        m_BatchCounter += 1;
      }
      

      if (result != null) {
        result.setDataset(dataset);
      }
      
      return result;
    }
    








    public static Instances read(String location)
      throws Exception
    {
      DataSource source = new DataSource(location);
      Instances result = source.getDataSet();
      
      return result;
    }
    








    public static Instances read(InputStream stream)
      throws Exception
    {
      DataSource source = new DataSource(stream);
      Instances result = source.getDataSet();
      
      return result;
    }
    








    public static Instances read(Loader loader)
      throws Exception
    {
      DataSource source = new DataSource(loader);
      Instances result = source.getDataSet();
      
      return result;
    }
    




    public static void main(String[] args)
      throws Exception
    {
      if (args.length != 1) {
        System.out.println("\nUsage: " + DataSource.class.getName() + " <file>\n");
        System.exit(1);
      }
      
      DataSource loader = new DataSource(args[0]);
      
      System.out.println("Incremental? " + loader.isIncremental());
      System.out.println("Loader: " + loader.getLoader().getClass().getName());
      System.out.println("Data:\n");
      Instances structure = loader.getStructure();
      System.out.println(structure);
      while (loader.hasMoreElements(structure)) {
        System.out.println(loader.nextElement(structure));
      }
      Instances inst = loader.getDataSet();
      loader = new DataSource(inst);
      System.out.println("\n\nProxy-Data:\n");
      System.out.println(loader.getStructure());
      while (loader.hasMoreElements(structure)) {
        System.out.println(loader.nextElement(inst));
      }
    }
    



    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6416 $");
    }
  }
  






  public static class DataSink
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -1504966891136411204L;
    





    protected Saver m_Saver = null;
    

    protected OutputStream m_Stream = null;
    




    public DataSink(String filename)
      throws Exception
    {
      m_Stream = null;
      
      if (ConverterUtils.DataSource.isArff(filename)) {
        m_Saver = new ArffSaver();
      } else {
        m_Saver = ConverterUtils.getSaverForFile(filename);
      }
      ((AbstractFileSaver)m_Saver).setFile(new File(filename));
    }
    





    public DataSink(Saver saver)
    {
      m_Saver = saver;
      m_Stream = null;
    }
    






    public DataSink(OutputStream stream)
    {
      m_Saver = null;
      m_Stream = stream;
    }
    






    public void write(Instances data)
      throws Exception
    {
      if (m_Saver != null) {
        m_Saver.setInstances(data);
        m_Saver.writeBatch();
      }
      else {
        m_Stream.write(data.toString().getBytes());
        m_Stream.flush();
      }
    }
    







    public static void write(String filename, Instances data)
      throws Exception
    {
      DataSink sink = new DataSink(filename);
      sink.write(data);
    }
    







    public static void write(Saver saver, Instances data)
      throws Exception
    {
      DataSink sink = new DataSink(saver);
      sink.write(data);
    }
    







    public static void write(OutputStream stream, Instances data)
      throws Exception
    {
      DataSink sink = new DataSink(stream);
      sink.write(data);
    }
    





    public static void main(String[] args)
      throws Exception
    {
      if (args.length != 2) {
        System.out.println("\nUsage: " + ConverterUtils.DataSource.class.getName() + " <input-file> <output-file>\n");
        
        System.exit(1);
      }
      

      Instances data = ConverterUtils.DataSource.read(args[0]);
      

      write(args[1], data);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 6416 $");
    }
  }
  


  public static final String CORE_FILE_LOADERS = ArffLoader.class.getName() + "," + C45Loader.class.getName() + "," + CSVLoader.class.getName() + "," + DatabaseConverter.class.getName() + "," + LibSVMLoader.class.getName() + "," + SerializedInstancesLoader.class.getName() + "," + TextDirectoryLoader.class.getName() + "," + XRFFLoader.class.getName();
  










  public static final String CORE_FILE_SAVERS = ArffSaver.class.getName() + "," + C45Saver.class.getName() + "," + CSVSaver.class.getName() + "," + DatabaseConverter.class.getName() + "," + LibSVMSaver.class.getName() + "," + SerializedInstancesSaver.class.getName() + "," + XRFFSaver.class.getName();
  



  protected static Hashtable<String, String> m_FileLoaders;
  



  protected static Hashtable<String, String> m_URLFileLoaders;
  



  protected static Hashtable<String, String> m_FileSavers;
  




  static
  {
    try
    {
      GenericPropertiesCreator creator = new GenericPropertiesCreator();
      creator.execute(false);
      Properties props = creator.getOutputProperties();
      

      m_FileLoaders = new Hashtable();
      m_URLFileLoaders = new Hashtable();
      m_FileSavers = new Hashtable();
      

      m_FileLoaders = getFileConverters(props.getProperty(Loader.class.getName(), CORE_FILE_LOADERS), new String[] { FileSourcedConverter.class.getName() });
      



      m_URLFileLoaders = getFileConverters(props.getProperty(Loader.class.getName(), CORE_FILE_LOADERS), new String[] { FileSourcedConverter.class.getName(), URLSourcedLoader.class.getName() });
      





      m_FileSavers = getFileConverters(props.getProperty(Saver.class.getName(), CORE_FILE_SAVERS), new String[] { FileSourcedConverter.class.getName() });



    }
    catch (Exception e) {}finally
    {


      if (m_FileLoaders.size() == 0) {
        Vector classnames = GenericObjectEditor.getClassnames(AbstractFileLoader.class.getName());
        if (classnames.size() > 0) {
          m_FileLoaders = getFileConverters(classnames, new String[] { FileSourcedConverter.class.getName() });
        }
        else
        {
          m_FileLoaders = getFileConverters(CORE_FILE_LOADERS, new String[] { FileSourcedConverter.class.getName() });
        }
      }
      


      if (m_URLFileLoaders.size() == 0) {
        Vector classnames = GenericObjectEditor.getClassnames(AbstractFileLoader.class.getName());
        if (classnames.size() > 0) {
          m_URLFileLoaders = getFileConverters(classnames, new String[] { FileSourcedConverter.class.getName(), URLSourcedLoader.class.getName() });

        }
        else
        {

          m_URLFileLoaders = getFileConverters(CORE_FILE_LOADERS, new String[] { FileSourcedConverter.class.getName(), URLSourcedLoader.class.getName() });
        }
      }
      




      if (m_FileSavers.size() == 0) {
        Vector classnames = GenericObjectEditor.getClassnames(AbstractFileSaver.class.getName());
        if (classnames.size() > 0) {
          m_FileSavers = getFileConverters(classnames, new String[] { FileSourcedConverter.class.getName() });
        }
        else
        {
          m_FileSavers = getFileConverters(CORE_FILE_SAVERS, new String[] { FileSourcedConverter.class.getName() });
        }
      }
    }
  }
  













  protected static Hashtable<String, String> getFileConverters(String classnames, String[] intf)
  {
    Vector list = new Vector();
    String[] names = classnames.split(",");
    for (int i = 0; i < names.length; i++) {
      list.add(names[i]);
    }
    return getFileConverters(list, intf);
  }
  
















  protected static Hashtable<String, String> getFileConverters(Vector classnames, String[] intf)
  {
    Hashtable<String, String> result = new Hashtable();
    
    for (int i = 0; i < classnames.size(); i++) {
      String classname = (String)classnames.get(i);
      

      for (int n = 0; n < intf.length; n++) {
        if (ClassDiscovery.hasInterface(intf[n], classname)) {}
      }
      FileSourcedConverter converter;
      String[] ext;
      try
      {
        cls = Class.forName(classname);
        converter = (FileSourcedConverter)cls.newInstance();
        ext = converter.getFileExtensions();
      }
      catch (Exception e) {
        Class cls = null;
        converter = null;
        ext = new String[0];
      }
      
      if (converter != null)
      {

        for (n = 0; n < ext.length; n++)
          result.put(ext[n], classname);
      }
    }
    return result;
  }
  






  public static void getFirstToken(StreamTokenizer tokenizer)
    throws IOException
  {
    while (tokenizer.nextToken() == 10) {}
    if ((ttype == 39) || (ttype == 34))
    {
      ttype = -3;
    } else if ((ttype == -3) && (sval.equals("?")))
    {
      ttype = 63;
    }
  }
  





  public static void getToken(StreamTokenizer tokenizer)
    throws IOException
  {
    tokenizer.nextToken();
    if (ttype == 10) {
      return;
    }
    
    if ((ttype == 39) || (ttype == 34))
    {
      ttype = -3;
    } else if ((ttype == -3) && (sval.equals("?")))
    {
      ttype = 63;
    }
  }
  







  public static void errms(StreamTokenizer tokenizer, String theMsg)
    throws IOException
  {
    throw new IOException(theMsg + ", read " + tokenizer.toString());
  }
  










  protected static Vector<String> getConverters(Hashtable<String, String> ht)
  {
    Vector<String> result = new Vector();
    

    Enumeration<String> enm = ht.elements();
    while (enm.hasMoreElements()) {
      String converter = (String)enm.nextElement();
      if (!result.contains(converter)) {
        result.add(converter);
      }
    }
    
    Collections.sort(result);
    
    return result;
  }
  











  protected static Object getConverterForFile(String filename, Hashtable<String, String> ht)
  {
    Object result = null;
    
    int index = filename.lastIndexOf('.');
    if (index > -1) {
      String extension = filename.substring(index).toLowerCase();
      result = getConverterForExtension(extension, ht);
      
      if ((extension.equals(".gz")) && (result == null)) {
        index = filename.lastIndexOf('.', index - 1);
        extension = filename.substring(index).toLowerCase();
        result = getConverterForExtension(extension, ht);
      }
    }
    
    return result;
  }
  










  protected static Object getConverterForExtension(String extension, Hashtable<String, String> ht)
  {
    Object result = null;
    String classname = (String)ht.get(extension);
    if (classname != null) {
      try {
        result = Class.forName(classname).newInstance();
      }
      catch (Exception e) {
        result = null;
        e.printStackTrace();
      }
    }
    
    return result;
  }
  









  public static boolean isCoreFileLoader(String classname)
  {
    String[] classnames = CORE_FILE_LOADERS.split(",");
    boolean result = Arrays.binarySearch(classnames, classname) >= 0;
    
    return result;
  }
  




  public static Vector<String> getFileLoaders()
  {
    return getConverters(m_FileLoaders);
  }
  






  public static AbstractFileLoader getLoaderForFile(String filename)
  {
    return (AbstractFileLoader)getConverterForFile(filename, m_FileLoaders);
  }
  






  public static AbstractFileLoader getLoaderForFile(File file)
  {
    return getLoaderForFile(file.getAbsolutePath());
  }
  






  public static AbstractFileLoader getLoaderForExtension(String extension)
  {
    return (AbstractFileLoader)getConverterForExtension(extension, m_FileLoaders);
  }
  




  public static Vector<String> getURLFileLoaders()
  {
    return getConverters(m_URLFileLoaders);
  }
  






  public static AbstractFileLoader getURLLoaderForFile(String filename)
  {
    return (AbstractFileLoader)getConverterForFile(filename, m_URLFileLoaders);
  }
  






  public static AbstractFileLoader getURLLoaderForFile(File file)
  {
    return getURLLoaderForFile(file.getAbsolutePath());
  }
  






  public static AbstractFileLoader getURLLoaderForExtension(String extension)
  {
    return (AbstractFileLoader)getConverterForExtension(extension, m_URLFileLoaders);
  }
  









  public static boolean isCoreFileSaver(String classname)
  {
    String[] classnames = CORE_FILE_SAVERS.split(",");
    boolean result = Arrays.binarySearch(classnames, classname) >= 0;
    
    return result;
  }
  




  public static Vector<String> getFileSavers()
  {
    return getConverters(m_FileSavers);
  }
  






  public static AbstractFileSaver getSaverForFile(String filename)
  {
    return (AbstractFileSaver)getConverterForFile(filename, m_FileSavers);
  }
  






  public static AbstractFileSaver getSaverForFile(File file)
  {
    return getSaverForFile(file.getAbsolutePath());
  }
  






  public static AbstractFileSaver getSaverForExtension(String extension)
  {
    return (AbstractFileSaver)getConverterForExtension(extension, m_FileSavers);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6416 $");
  }
  
  public ConverterUtils() {}
}
