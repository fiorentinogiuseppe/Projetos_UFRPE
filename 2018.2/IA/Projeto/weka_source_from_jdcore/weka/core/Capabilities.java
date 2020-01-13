package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import weka.classifiers.UpdateableClassifier;
import weka.clusterers.UpdateableClusterer;
import weka.core.converters.ConverterUtils.DataSource;




































































public class Capabilities
  implements Cloneable, Serializable, RevisionHandler
{
  static final long serialVersionUID = -5478590032325567849L;
  public static final String PROPERTIES_FILE = "weka/core/Capabilities.props";
  protected static Properties PROPERTIES;
  private static final int ATTRIBUTE = 1;
  private static final int CLASS = 2;
  private static final int ATTRIBUTE_CAPABILITY = 4;
  private static final int CLASS_CAPABILITY = 8;
  private static final int OTHER_CAPABILITY = 16;
  protected CapabilitiesHandler m_Owner;
  protected HashSet m_Capabilities;
  protected HashSet m_Dependencies;
  
  public static enum Capability
  {
    NOMINAL_ATTRIBUTES(5, "Nominal attributes"), 
    
    BINARY_ATTRIBUTES(5, "Binary attributes"), 
    
    UNARY_ATTRIBUTES(5, "Unary attributes"), 
    
    EMPTY_NOMINAL_ATTRIBUTES(5, "Empty nominal attributes"), 
    
    NUMERIC_ATTRIBUTES(5, "Numeric attributes"), 
    
    DATE_ATTRIBUTES(5, "Date attributes"), 
    
    STRING_ATTRIBUTES(5, "String attributes"), 
    
    RELATIONAL_ATTRIBUTES(5, "Relational attributes"), 
    
    MISSING_VALUES(4, "Missing values"), 
    

    NO_CLASS(8, "No class"), 
    
    NOMINAL_CLASS(10, "Nominal class"), 
    
    BINARY_CLASS(10, "Binary class"), 
    
    UNARY_CLASS(10, "Unary class"), 
    
    EMPTY_NOMINAL_CLASS(10, "Empty nominal class"), 
    
    NUMERIC_CLASS(10, "Numeric class"), 
    
    DATE_CLASS(10, "Date class"), 
    
    STRING_CLASS(10, "String class"), 
    
    RELATIONAL_CLASS(10, "Relational class"), 
    
    MISSING_CLASS_VALUES(8, "Missing class values"), 
    

    ONLY_MULTIINSTANCE(16, "Only multi-Instance data");
    

    private int m_Flags = 0;
    



    private String m_Display;
    



    private Capability(int flags, String display)
    {
      m_Flags = flags;
      m_Display = display;
    }
    




    public boolean isAttribute()
    {
      return (m_Flags & 0x1) == 1;
    }
    




    public boolean isClass()
    {
      return (m_Flags & 0x2) == 2;
    }
    




    public boolean isAttributeCapability()
    {
      return (m_Flags & 0x4) == 4;
    }
    




    public boolean isOtherCapability()
    {
      return (m_Flags & 0x10) == 16;
    }
    




    public boolean isClassCapability()
    {
      return (m_Flags & 0x8) == 8;
    }
    




    public String toString()
    {
      return m_Display;
    }
  }
  










  protected Exception m_FailReason = null;
  

  protected int m_MinimumNumberInstances = 1;
  


  protected boolean m_Test;
  


  protected boolean m_InstancesTest;
  


  protected boolean m_AttributeTest;
  


  protected boolean m_MissingValuesTest;
  

  protected boolean m_MissingClassValuesTest;
  

  protected boolean m_MinimumNumberInstancesTest;
  


  public Capabilities(CapabilitiesHandler owner)
  {
    setOwner(owner);
    m_Capabilities = new HashSet();
    m_Dependencies = new HashSet();
    

    if (PROPERTIES == null) {
      try {
        PROPERTIES = Utils.readProperties("weka/core/Capabilities.props");
      }
      catch (Exception e) {
        e.printStackTrace();
        PROPERTIES = new Properties();
      }
    }
    
    m_Test = Boolean.parseBoolean(PROPERTIES.getProperty("Test", "true"));
    m_InstancesTest = ((Boolean.parseBoolean(PROPERTIES.getProperty("InstancesTest", "true"))) && (m_Test));
    m_AttributeTest = ((Boolean.parseBoolean(PROPERTIES.getProperty("AttributeTest", "true"))) && (m_Test));
    m_MissingValuesTest = ((Boolean.parseBoolean(PROPERTIES.getProperty("MissingValuesTest", "true"))) && (m_Test));
    m_MissingClassValuesTest = ((Boolean.parseBoolean(PROPERTIES.getProperty("MissingClassValuesTest", "true"))) && (m_Test));
    m_MinimumNumberInstancesTest = ((Boolean.parseBoolean(PROPERTIES.getProperty("MinimumNumberInstancesTest", "true"))) && (m_Test));
    
    if (((owner instanceof UpdateableClassifier)) || ((owner instanceof UpdateableClusterer)))
    {
      setMinimumNumberInstances(0);
    }
  }
  






  public Object clone()
  {
    Capabilities result = new Capabilities(m_Owner);
    result.assign(this);
    
    return result;
  }
  




  public void assign(Capabilities c)
  {
    for (Capability cap : )
    {
      if (c.handles(cap)) {
        enable(cap);
      } else {
        disable(cap);
      }
      if (c.hasDependency(cap)) {
        enableDependency(cap);
      } else {
        disableDependency(cap);
      }
    }
    setMinimumNumberInstances(c.getMinimumNumberInstances());
  }
  





  public void and(Capabilities c)
  {
    for (Capability cap : )
    {
      if ((handles(cap)) && (c.handles(cap))) {
        m_Capabilities.add(cap);
      } else {
        m_Capabilities.remove(cap);
      }
      if ((hasDependency(cap)) && (c.hasDependency(cap))) {
        m_Dependencies.add(cap);
      } else {
        m_Dependencies.remove(cap);
      }
    }
    
    if (c.getMinimumNumberInstances() > getMinimumNumberInstances()) {
      setMinimumNumberInstances(c.getMinimumNumberInstances());
    }
  }
  




  public void or(Capabilities c)
  {
    for (Capability cap : )
    {
      if ((handles(cap)) || (c.handles(cap))) {
        m_Capabilities.add(cap);
      } else {
        m_Capabilities.remove(cap);
      }
      if ((hasDependency(cap)) || (c.hasDependency(cap))) {
        m_Dependencies.add(cap);
      } else {
        m_Dependencies.remove(cap);
      }
    }
    if (c.getMinimumNumberInstances() < getMinimumNumberInstances()) {
      setMinimumNumberInstances(c.getMinimumNumberInstances());
    }
  }
  







  public boolean supports(Capabilities c)
  {
    boolean result = true;
    
    for (Capability cap : Capability.values()) {
      if ((c.handles(cap)) && (!handles(cap))) {
        result = false;
        break;
      }
    }
    
    return result;
  }
  










  public boolean supportsMaybe(Capabilities c)
  {
    boolean result = true;
    
    for (Capability cap : Capability.values()) {
      if ((c.handles(cap)) && (!handles(cap)) && (!hasDependency(cap))) {
        result = false;
        break;
      }
    }
    
    return result;
  }
  




  public void setOwner(CapabilitiesHandler value)
  {
    m_Owner = value;
  }
  




  public CapabilitiesHandler getOwner()
  {
    return m_Owner;
  }
  




  public void setMinimumNumberInstances(int value)
  {
    if (value >= 0) {
      m_MinimumNumberInstances = value;
    }
  }
  



  public int getMinimumNumberInstances()
  {
    return m_MinimumNumberInstances;
  }
  




  public Iterator capabilities()
  {
    return m_Capabilities.iterator();
  }
  




  public Iterator dependencies()
  {
    return m_Dependencies.iterator();
  }
  












  public void enable(Capability c)
  {
    if (c == Capability.NOMINAL_ATTRIBUTES) {
      enable(Capability.BINARY_ATTRIBUTES);
    }
    else if (c == Capability.BINARY_ATTRIBUTES) {
      enable(Capability.UNARY_ATTRIBUTES);
    }
    else if (c == Capability.UNARY_ATTRIBUTES) {
      enable(Capability.EMPTY_NOMINAL_ATTRIBUTES);

    }
    else if (c == Capability.NOMINAL_CLASS) {
      enable(Capability.BINARY_CLASS);
    }
    
    m_Capabilities.add(c);
  }
  












  public void enableDependency(Capability c)
  {
    if (c == Capability.NOMINAL_ATTRIBUTES) {
      enableDependency(Capability.BINARY_ATTRIBUTES);
    }
    else if (c == Capability.BINARY_ATTRIBUTES) {
      enableDependency(Capability.UNARY_ATTRIBUTES);
    }
    else if (c == Capability.UNARY_ATTRIBUTES) {
      enableDependency(Capability.EMPTY_NOMINAL_ATTRIBUTES);

    }
    else if (c == Capability.NOMINAL_CLASS) {
      enableDependency(Capability.BINARY_CLASS);
    }
    
    m_Dependencies.add(c);
  }
  





  public void enableAllClasses()
  {
    for (Capability cap : ) {
      if (cap.isClass()) {
        enable(cap);
      }
    }
  }
  




  public void enableAllClassDependencies()
  {
    for (Capability cap : ) {
      if (cap.isClass()) {
        enableDependency(cap);
      }
    }
  }
  




  public void enableAllAttributes()
  {
    for (Capability cap : ) {
      if (cap.isAttribute()) {
        enable(cap);
      }
    }
  }
  




  public void enableAllAttributeDependencies()
  {
    for (Capability cap : ) {
      if (cap.isAttribute()) {
        enableDependency(cap);
      }
    }
  }
  

  public void enableAll()
  {
    enableAllAttributes();
    enableAllAttributeDependencies();
    enableAllClasses();
    enableAllClassDependencies();
    enable(Capability.MISSING_VALUES);
    enable(Capability.MISSING_CLASS_VALUES);
  }
  











  public void disable(Capability c)
  {
    if (c == Capability.NOMINAL_ATTRIBUTES) {
      disable(Capability.BINARY_ATTRIBUTES);
    }
    else if (c == Capability.BINARY_ATTRIBUTES) {
      disable(Capability.UNARY_ATTRIBUTES);
    }
    else if (c == Capability.UNARY_ATTRIBUTES) {
      disable(Capability.EMPTY_NOMINAL_ATTRIBUTES);

    }
    else if (c == Capability.NOMINAL_CLASS) {
      disable(Capability.BINARY_CLASS);
    }
    else if (c == Capability.BINARY_CLASS) {
      disable(Capability.UNARY_CLASS);
    }
    else if (c == Capability.UNARY_CLASS) {
      disable(Capability.EMPTY_NOMINAL_CLASS);
    }
    
    m_Capabilities.remove(c);
  }
  











  public void disableDependency(Capability c)
  {
    if (c == Capability.NOMINAL_ATTRIBUTES) {
      disableDependency(Capability.BINARY_ATTRIBUTES);
    }
    else if (c == Capability.BINARY_ATTRIBUTES) {
      disableDependency(Capability.UNARY_ATTRIBUTES);
    }
    else if (c == Capability.UNARY_ATTRIBUTES) {
      disableDependency(Capability.EMPTY_NOMINAL_ATTRIBUTES);

    }
    else if (c == Capability.NOMINAL_CLASS) {
      disableDependency(Capability.BINARY_CLASS);
    }
    else if (c == Capability.BINARY_CLASS) {
      disableDependency(Capability.UNARY_CLASS);
    }
    else if (c == Capability.UNARY_CLASS) {
      disableDependency(Capability.EMPTY_NOMINAL_CLASS);
    }
    
    m_Dependencies.remove(c);
  }
  





  public void disableAllClasses()
  {
    for (Capability cap : ) {
      if (cap.isClass()) {
        disable(cap);
      }
    }
  }
  




  public void disableAllClassDependencies()
  {
    for (Capability cap : ) {
      if (cap.isClass()) {
        disableDependency(cap);
      }
    }
  }
  




  public void disableAllAttributes()
  {
    for (Capability cap : ) {
      if (cap.isAttribute()) {
        disable(cap);
      }
    }
  }
  




  public void disableAllAttributeDependencies()
  {
    for (Capability cap : ) {
      if (cap.isAttribute()) {
        disableDependency(cap);
      }
    }
  }
  

  public void disableAll()
  {
    disableAllAttributes();
    disableAllAttributeDependencies();
    disableAllClasses();
    disableAllClassDependencies();
    disable(Capability.MISSING_VALUES);
    disable(Capability.MISSING_CLASS_VALUES);
    disable(Capability.NO_CLASS);
  }
  








  public Capabilities getClassCapabilities()
  {
    Capabilities result = new Capabilities(getOwner());
    
    for (Capability cap : Capability.values()) {
      if ((cap.isClassCapability()) && 
        (handles(cap))) {
        m_Capabilities.add(cap);
      }
    }
    
    return result;
  }
  








  public Capabilities getAttributeCapabilities()
  {
    Capabilities result = new Capabilities(getOwner());
    
    for (Capability cap : Capability.values()) {
      if ((cap.isAttributeCapability()) && 
        (handles(cap))) {
        m_Capabilities.add(cap);
      }
    }
    
    return result;
  }
  







  public Capabilities getOtherCapabilities()
  {
    Capabilities result = new Capabilities(getOwner());
    
    for (Capability cap : Capability.values()) {
      if ((cap.isOtherCapability()) && 
        (handles(cap))) {
        m_Capabilities.add(cap);
      }
    }
    
    return result;
  }
  





  public boolean handles(Capability c)
  {
    return m_Capabilities.contains(c);
  }
  







  public boolean hasDependency(Capability c)
  {
    return m_Dependencies.contains(c);
  }
  




  public boolean hasDependencies()
  {
    return m_Dependencies.size() > 0;
  }
  




  public Exception getFailReason()
  {
    return m_FailReason;
  }
  








  protected String createMessage(String msg)
  {
    String result = "";
    
    if (getOwner() != null) {
      result = getOwner().getClass().getName();
    } else {
      result = "<anonymous>";
    }
    result = result + ": " + msg;
    
    return result;
  }
  








  public boolean test(Attribute att)
  {
    return test(att, false);
  }
  















  public boolean test(Attribute att, boolean isClass)
  {
    boolean result = true;
    

    if (!m_AttributeTest)
      return result;
    String errorStr;
    String errorStr;
    if (isClass) {
      errorStr = "class";
    } else
      errorStr = "attributes";
    Capability cap;
    Capability cap; Capability cap; Capability cap; switch (att.type()) {case 1:  Capability capEmpty;
      Capability capBinary;
      Capability capUnary; Capability capEmpty; if (isClass) {
        Capability cap = Capability.NOMINAL_CLASS;
        Capability capBinary = Capability.BINARY_CLASS;
        Capability capUnary = Capability.UNARY_CLASS;
        capEmpty = Capability.EMPTY_NOMINAL_CLASS;
      }
      else {
        cap = Capability.NOMINAL_ATTRIBUTES;
        capBinary = Capability.BINARY_ATTRIBUTES;
        capUnary = Capability.UNARY_ATTRIBUTES;
        capEmpty = Capability.EMPTY_NOMINAL_ATTRIBUTES;
      }
      
      if ((!handles(cap)) || (att.numValues() <= 2))
      {
        if ((!handles(capBinary)) || (att.numValues() != 2))
        {
          if ((!handles(capUnary)) || (att.numValues() != 1))
          {
            if ((!handles(capEmpty)) || (att.numValues() != 0))
            {

              if (att.numValues() == 0) {
                m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle empty nominal " + errorStr + "!"));
                
                result = false;
              }
              if (att.numValues() == 1) {
                m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle unary " + errorStr + "!"));
                
                result = false;
              }
              else if (att.numValues() == 2) {
                m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle binary " + errorStr + "!"));
                
                result = false;
              }
              else {
                m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle multi-valued nominal " + errorStr + "!"));
                
                result = false;
              } } } } }
      break;
    
    case 0: 
      if (isClass) {
        cap = Capability.NUMERIC_CLASS;
      } else {
        cap = Capability.NUMERIC_ATTRIBUTES;
      }
      if (!handles(cap)) {
        m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle numeric " + errorStr + "!"));
        
        result = false;
      }
      
      break;
    case 3: 
      if (isClass) {
        cap = Capability.DATE_CLASS;
      } else {
        cap = Capability.DATE_ATTRIBUTES;
      }
      if (!handles(cap)) {
        m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle date " + errorStr + "!"));
        
        result = false;
      }
      
      break;
    case 2: 
      if (isClass) {
        cap = Capability.STRING_CLASS;
      } else {
        cap = Capability.STRING_ATTRIBUTES;
      }
      if (!handles(cap)) {
        m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle string " + errorStr + "!"));
        
        result = false;
      }
      break;
    case 4: 
      Capability cap;
      if (isClass) {
        cap = Capability.RELATIONAL_CLASS;
      } else {
        cap = Capability.RELATIONAL_ATTRIBUTES;
      }
      if (!handles(cap)) {
        m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle relational " + errorStr + "!"));
        
        result = false;
      }
      


      break;
    default: 
      m_FailReason = new UnsupportedAttributeTypeException(createMessage("Cannot handle unknown attribute type '" + att.type() + "'!"));
      

      result = false;
    }
    
    return result;
  }
  










  public boolean test(Instances data)
  {
    return test(data, 0, data.numAttributes() - 1);
  }
  



























  public boolean test(Instances data, int fromIndex, int toIndex)
  {
    if (!m_InstancesTest) {
      return true;
    }
    
    if ((m_Capabilities.size() == 0) || ((m_Capabilities.size() == 1) && (handles(Capability.NO_CLASS))))
    {
      System.err.println(createMessage("No capabilities set!"));
    }
    
    if (toIndex - fromIndex < 0) {
      m_FailReason = new WekaException(createMessage("No attributes!"));
      
      return false;
    }
    


    boolean testClass = (data.classIndex() > -1) && (data.classIndex() >= fromIndex) && (data.classIndex() <= toIndex);
    



    for (int i = fromIndex; i <= toIndex; i++) {
      Attribute att = data.attribute(i);
      

      if (i != data.classIndex())
      {


        if (!test(att)) {
          return false;
        }
      }
    }
    if ((!handles(Capability.NO_CLASS)) && (data.classIndex() == -1)) {
      m_FailReason = new UnassignedClassException(createMessage("Class attribute not set!"));
      
      return false;
    }
    

    if ((handles(Capability.NO_CLASS)) && (data.classIndex() > -1)) {
      Capabilities cap = getClassCapabilities();
      cap.disable(Capability.NO_CLASS);
      Iterator iter = cap.capabilities();
      if (!iter.hasNext()) {
        m_FailReason = new WekaException(createMessage("Cannot handle any class attribute!"));
        
        return false;
      }
    }
    
    if ((testClass) && (!handles(Capability.NO_CLASS))) {
      Attribute att = data.classAttribute();
      if (!test(att, true)) {
        return false;
      }
      



      if (m_MissingClassValuesTest) {
        if (!handles(Capability.MISSING_CLASS_VALUES)) {
          for (i = 0; i < data.numInstances(); i++) {
            if (data.instance(i).classIsMissing()) {
              m_FailReason = new WekaException(createMessage("Cannot handle missing class values!"));
              
              return false;
            }
          }
        }
        
        if (m_MinimumNumberInstancesTest) {
          int hasClass = 0;
          
          for (i = 0; i < data.numInstances(); i++) {
            if (!data.instance(i).classIsMissing()) {
              hasClass++;
            }
          }
          
          if (hasClass < getMinimumNumberInstances()) {
            m_FailReason = new WekaException(createMessage("Not enough training instances with class labels (required: " + getMinimumNumberInstances() + ", provided: " + hasClass + ")!"));
            



            return false;
          }
        }
      }
    }
    


    if ((m_MissingValuesTest) && 
      (!handles(Capability.MISSING_VALUES))) {
      boolean missing = false;
      for (i = 0; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        
        if ((inst instanceof SparseInstance)) {
          for (int m = 0; m < inst.numValues(); m++) {
            int n = inst.index(m);
            

            if (n >= fromIndex)
            {
              if (n > toIndex) {
                break;
              }
              
              if (n != inst.classIndex())
              {


                if (inst.isMissing(n)) {
                  missing = true;
                  break;
                } }
            }
          }
        }
        for (int n = fromIndex; n <= toIndex; n++)
        {
          if (n != inst.classIndex())
          {

            if (inst.isMissing(n)) {
              missing = true;
              break;
            }
          }
        }
        
        if (missing) {
          m_FailReason = new NoSupportForMissingValuesException(createMessage("Cannot handle missing values!"));
          
          return false;
        }
      }
    }
    


    if ((m_MinimumNumberInstancesTest) && 
      (data.numInstances() < getMinimumNumberInstances())) {
      m_FailReason = new WekaException(createMessage("Not enough training instances (required: " + getMinimumNumberInstances() + ", provided: " + data.numInstances() + ")!"));
      



      return false;
    }
    


    if (handles(Capability.ONLY_MULTIINSTANCE))
    {
      if (data.numAttributes() != 3) {
        m_FailReason = new WekaException(createMessage("Incorrect Multi-Instance format, must be 'bag-id, bag, class'!"));
        
        return false;
      }
      

      if ((!data.attribute(0).isNominal()) || (!data.attribute(1).isRelationValued()) || (data.classIndex() != data.numAttributes() - 1))
      {

        m_FailReason = new WekaException(createMessage("Incorrect Multi-Instance format, must be 'NOMINAL att, RELATIONAL att, CLASS att'!"));
        
        return false;
      }
      

      if ((getOwner() instanceof MultiInstanceCapabilitiesHandler)) {
        MultiInstanceCapabilitiesHandler handler = (MultiInstanceCapabilitiesHandler)getOwner();
        Capabilities cap = handler.getMultiInstanceCapabilities();
        boolean result;
        boolean result; if ((data.numInstances() > 0) && (data.attribute(1).numValues() > 0)) {
          result = cap.test(data.attribute(1).relation(0));
        } else {
          result = cap.test(data.attribute(1).relation());
        }
        if (!result) {
          m_FailReason = m_FailReason;
          return false;
        }
      }
    }
    

    return true;
  }
  







  public void testWithFail(Attribute att)
    throws Exception
  {
    test(att, false);
  }
  







  public void testWithFail(Attribute att, boolean isClass)
    throws Exception
  {
    if (!test(att, isClass)) {
      throw m_FailReason;
    }
  }
  







  public void testWithFail(Instances data, int fromIndex, int toIndex)
    throws Exception
  {
    if (!test(data, fromIndex, toIndex)) {
      throw m_FailReason;
    }
  }
  





  public void testWithFail(Instances data)
    throws Exception
  {
    if (!test(data)) {
      throw m_FailReason;
    }
  }
  






  public String toString()
  {
    StringBuffer result = new StringBuffer();
    

    Vector sorted = new Vector(m_Capabilities);
    Collections.sort(sorted);
    result.append("Capabilities: " + sorted.toString() + "\n");
    

    sorted = new Vector(m_Dependencies);
    Collections.sort(sorted);
    result.append("Dependencies: " + sorted.toString() + "\n");
    

    result.append("min # Instance: " + getMinimumNumberInstances() + "\n");
    
    return result.toString();
  }
  







  public String toSource(String objectname)
  {
    return toSource(objectname, 0);
  }
  














  public String toSource(String objectname, int indent)
  {
    StringBuffer result = new StringBuffer();
    
    String capsName = Capabilities.class.getName();
    String capName = Capability.class.getName().replaceAll("\\$", ".");
    
    String indentStr = "";
    for (int i = 0; i < indent; i++) {
      indentStr = indentStr + " ";
    }
    
    result.append(indentStr + capsName + " " + objectname + " = new " + capsName + "(this);\n");
    
    List<Capability> capsList = new ArrayList();
    boolean hasNominalAtt = false;
    boolean hasBinaryAtt = false;
    boolean hasUnaryAtt = false;
    boolean hasEmptyNomAtt = false;
    boolean hasNominalClass = false;
    
    result.append("\n");
    for (Capability cap : Capability.values())
    {
      if (handles(cap)) {
        if (cap == Capability.NOMINAL_ATTRIBUTES) {
          hasNominalAtt = true;
        }
        if (cap == Capability.NOMINAL_CLASS) {
          hasNominalClass = true;
        }
        if (cap == Capability.BINARY_ATTRIBUTES) {
          hasBinaryAtt = true;
        }
        if (cap == Capability.UNARY_ATTRIBUTES) {
          hasUnaryAtt = true;
        }
        if (cap == Capability.EMPTY_NOMINAL_ATTRIBUTES) {
          hasEmptyNomAtt = true;
        }
        capsList.add(cap);
      }
    }
    
    for (Capability cap : capsList) {
      if (((cap != Capability.BINARY_ATTRIBUTES) || (!hasNominalAtt)) && ((cap != Capability.UNARY_ATTRIBUTES) || (!hasBinaryAtt)) && ((cap != Capability.EMPTY_NOMINAL_ATTRIBUTES) || (!hasUnaryAtt)) && ((cap != Capability.BINARY_CLASS) || (!hasNominalClass)))
      {




        result.append(indentStr + objectname + ".enable(" + capName + "." + cap.name() + ");\n");
        

        if (hasDependency(cap)) {
          result.append(indentStr + objectname + ".enableDependency(" + capName + "." + cap.name() + ");\n");
        }
      }
    }
    
    result.append("\n");
    result.append(indentStr + objectname + ".setMinimumNumberInstances(" + getMinimumNumberInstances() + ");\n");
    


    result.append("\n");
    
    return result.toString();
  }
  








  public static Capabilities forInstances(Instances data)
    throws Exception
  {
    return forInstances(data, false);
  }
  
















  public static Capabilities forInstances(Instances data, boolean multi)
    throws Exception
  {
    Capabilities result = new Capabilities(null);
    

    if (data.classIndex() == -1) {
      result.enable(Capability.NO_CLASS);
    }
    else {
      switch (data.classAttribute().type()) {
      case 1: 
        if (data.classAttribute().numValues() == 1) {
          result.enable(Capability.UNARY_CLASS);
        } else if (data.classAttribute().numValues() == 2) {
          result.enable(Capability.BINARY_CLASS);
        } else
          result.enable(Capability.NOMINAL_CLASS);
        break;
      
      case 0: 
        result.enable(Capability.NUMERIC_CLASS);
        break;
      
      case 2: 
        result.enable(Capability.STRING_CLASS);
        break;
      
      case 3: 
        result.enable(Capability.DATE_CLASS);
        break;
      
      case 4: 
        result.enable(Capability.RELATIONAL_CLASS);
        break;
      
      default: 
        throw new UnsupportedAttributeTypeException("Unknown class attribute type '" + data.classAttribute() + "'!");
      }
      
      

      for (int i = 0; i < data.numInstances(); i++) {
        if (data.instance(i).classIsMissing()) {
          result.enable(Capability.MISSING_CLASS_VALUES);
          break;
        }
      }
    }
    

    for (int i = 0; i < data.numAttributes(); i++)
    {
      if (i != data.classIndex())
      {

        switch (data.attribute(i).type()) {
        case 1: 
          result.enable(Capability.UNARY_ATTRIBUTES);
          if (data.attribute(i).numValues() == 2) {
            result.enable(Capability.BINARY_ATTRIBUTES);
          } else if (data.attribute(i).numValues() > 2) {
            result.enable(Capability.NOMINAL_ATTRIBUTES);
          }
          break;
        case 0: 
          result.enable(Capability.NUMERIC_ATTRIBUTES);
          break;
        
        case 3: 
          result.enable(Capability.DATE_ATTRIBUTES);
          break;
        
        case 2: 
          result.enable(Capability.STRING_ATTRIBUTES);
          break;
        
        case 4: 
          result.enable(Capability.RELATIONAL_ATTRIBUTES);
          break;
        
        default: 
          throw new UnsupportedAttributeTypeException("Unknown attribute type '" + data.attribute(i).type() + "'!");
        }
        
      }
    }
    
    boolean missing = false;
    for (i = 0; i < data.numInstances(); i++) {
      Instance inst = data.instance(i);
      
      if ((inst instanceof SparseInstance)) {
        for (int m = 0; m < inst.numValues(); m++) {
          int n = inst.index(m);
          

          if (n != inst.classIndex())
          {

            if (inst.isMissing(n)) {
              missing = true;
              break;
            }
          }
        }
      }
      for (int n = 0; n < data.numAttributes(); n++)
      {
        if (n != inst.classIndex())
        {

          if (inst.isMissing(n)) {
            missing = true;
            break;
          }
        }
      }
      
      if (missing) {
        result.enable(Capability.MISSING_VALUES);
        break;
      }
    }
    

    if ((multi) && 
      (data.numAttributes() == 3) && (data.attribute(0).isNominal()) && (data.attribute(1).isRelationValued()) && (data.classIndex() == data.numAttributes() - 1))
    {


      Capabilities multiInstance = new Capabilities(null);
      multiInstance.or(result.getClassCapabilities());
      multiInstance.enable(Capability.NOMINAL_ATTRIBUTES);
      multiInstance.enable(Capability.RELATIONAL_ATTRIBUTES);
      multiInstance.enable(Capability.ONLY_MULTIINSTANCE);
      result.assign(multiInstance);
    }
    

    return result;
  }
  





















  public static void main(String[] args)
    throws Exception
  {
    if (args.length == 0) {
      System.out.println("\nUsage: " + Capabilities.class.getName() + " -file <dataset> [-c <class index>]\n");
      

      return;
    }
    

    String tmpStr = Utils.getOption("file", args);
    if (tmpStr.length() == 0) {
      throw new Exception("No file provided with option '-file'!");
    }
    String filename = tmpStr;
    
    tmpStr = Utils.getOption("c", args);
    int classIndex; int classIndex; if (tmpStr.length() != 0) { int classIndex;
      if (tmpStr.equals("first")) {
        classIndex = 0; } else { int classIndex;
        if (tmpStr.equals("last")) {
          classIndex = -2;
        } else
          classIndex = Integer.parseInt(tmpStr) - 1;
      }
    } else {
      classIndex = -3;
    }
    

    ConverterUtils.DataSource source = new ConverterUtils.DataSource(filename);
    Instances data; Instances data; if (classIndex == -3) {
      data = source.getDataSet(); } else { Instances data;
      if (classIndex == -2) {
        data = source.getDataSet(source.getStructure().numAttributes() - 1);
      } else {
        data = source.getDataSet(classIndex);
      }
    }
    Capabilities cap = forInstances(data);
    System.out.println("File: " + filename);
    System.out.println("Class index: " + (data.classIndex() == -1 ? "not set" : new StringBuilder().append("").append(data.classIndex() + 1).toString()));
    System.out.println("Capabilities:");
    Iterator iter = cap.capabilities();
    while (iter.hasNext()) {
      System.out.println("- " + iter.next());
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9140 $");
  }
}
