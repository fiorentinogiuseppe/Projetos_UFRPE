package weka.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;


































































































































































public class Attribute
  implements Copyable, Serializable, RevisionHandler
{
  static final long serialVersionUID = -742180568732916383L;
  public static final int NUMERIC = 0;
  public static final int NOMINAL = 1;
  public static final int STRING = 2;
  public static final int DATE = 3;
  public static final int RELATIONAL = 4;
  public static final int ORDERING_SYMBOLIC = 0;
  public static final int ORDERING_ORDERED = 1;
  public static final int ORDERING_MODULO = 2;
  public static final String ARFF_ATTRIBUTE = "@attribute";
  public static final String ARFF_ATTRIBUTE_INTEGER = "integer";
  public static final String ARFF_ATTRIBUTE_REAL = "real";
  public static final String ARFF_ATTRIBUTE_NUMERIC = "numeric";
  public static final String ARFF_ATTRIBUTE_STRING = "string";
  public static final String ARFF_ATTRIBUTE_DATE = "date";
  public static final String ARFF_ATTRIBUTE_RELATIONAL = "relational";
  public static final String ARFF_END_SUBRELATION = "@end";
  public static final String DUMMY_STRING_VAL = "*WEKA*DUMMY*STRING*FOR*STRING*ATTRIBUTES*";
  private static final int STRING_COMPRESS_THRESHOLD = 200;
  private String m_Name;
  private int m_Type;
  private FastVector m_Values;
  private Hashtable m_Hashtable;
  private Instances m_Header;
  private SimpleDateFormat m_DateFormat;
  private int m_Index;
  private ProtectedProperties m_Metadata;
  private int m_Ordering;
  private boolean m_IsRegular;
  private boolean m_IsAveragable;
  private boolean m_HasZeropoint;
  private double m_Weight;
  private double m_LowerBound;
  private boolean m_LowerBoundIsOpen;
  private double m_UpperBound;
  private boolean m_UpperBoundIsOpen;
  
  public Attribute(String attributeName)
  {
    this(attributeName, new ProtectedProperties(new Properties()));
  }
  









  public Attribute(String attributeName, ProtectedProperties metadata)
  {
    m_Name = attributeName;
    m_Index = -1;
    m_Values = null;
    m_Hashtable = null;
    m_Header = null;
    m_Type = 0;
    setMetadata(metadata);
  }
  










  public Attribute(String attributeName, String dateFormat)
  {
    this(attributeName, dateFormat, new ProtectedProperties(new Properties()));
  }
  














  public Attribute(String attributeName, String dateFormat, ProtectedProperties metadata)
  {
    m_Name = attributeName;
    m_Index = -1;
    m_Values = null;
    m_Hashtable = null;
    m_Header = null;
    m_Type = 3;
    if (dateFormat != null) {
      m_DateFormat = new SimpleDateFormat(dateFormat);
    } else {
      m_DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }
    m_DateFormat.setLenient(false);
    setMetadata(metadata);
  }
  












  public Attribute(String attributeName, FastVector attributeValues)
  {
    this(attributeName, attributeValues, new ProtectedProperties(new Properties()));
  }
  























  public Attribute(String attributeName, FastVector attributeValues, ProtectedProperties metadata)
  {
    m_Name = attributeName;
    m_Index = -1;
    if (attributeValues == null) {
      m_Values = new FastVector();
      m_Hashtable = new Hashtable();
      m_Header = null;
      m_Type = 2;
      


      addStringValue("*WEKA*DUMMY*STRING*FOR*STRING*ATTRIBUTES*");
    } else {
      m_Values = new FastVector(attributeValues.size());
      m_Hashtable = new Hashtable(attributeValues.size());
      m_Header = null;
      for (int i = 0; i < attributeValues.size(); i++) {
        Object store = attributeValues.elementAt(i);
        if (((String)store).length() > 200) {
          try {
            store = new SerializedObject(attributeValues.elementAt(i), true);
          } catch (Exception ex) {
            System.err.println("Couldn't compress nominal attribute value - storing uncompressed.");
          }
        }
        
        if (m_Hashtable.containsKey(store)) {
          throw new IllegalArgumentException("A nominal attribute (" + attributeName + ") cannot" + " have duplicate labels (" + store + ").");
        }
        

        m_Values.addElement(store);
        m_Hashtable.put(store, new Integer(i));
      }
      m_Type = 1;
    }
    setMetadata(metadata);
  }
  






  public Attribute(String attributeName, Instances header)
  {
    this(attributeName, header, new ProtectedProperties(new Properties()));
  }
  










  public Attribute(String attributeName, Instances header, ProtectedProperties metadata)
  {
    if (header.numInstances() > 0) {
      throw new IllegalArgumentException("Header for relation-valued attribute should not contain any instances");
    }
    

    m_Name = attributeName;
    m_Index = -1;
    m_Values = new FastVector();
    m_Hashtable = new Hashtable();
    m_Header = header;
    m_Type = 4;
    setMetadata(metadata);
  }
  






  public Object copy()
  {
    Attribute copy = new Attribute(m_Name);
    
    m_Index = m_Index;
    m_Type = m_Type;
    m_Values = m_Values;
    m_Hashtable = m_Hashtable;
    m_DateFormat = m_DateFormat;
    m_Header = m_Header;
    copy.setMetadata(m_Metadata);
    
    return copy;
  }
  






  public final Enumeration enumerateValues()
  {
    if ((isNominal()) || (isString())) {
      final Enumeration ee = m_Values.elements();
      new Enumeration()
      {
        public boolean hasMoreElements() { return ee.hasMoreElements(); }
        
        public Object nextElement() {
          Object oo = ee.nextElement();
          if ((oo instanceof SerializedObject)) {
            return ((SerializedObject)oo).getObject();
          }
          return oo;
        }
      };
    }
    
    return null;
  }
  






  public final boolean equals(Object other)
  {
    if ((other == null) || (!other.getClass().equals(getClass()))) {
      return false;
    }
    Attribute att = (Attribute)other;
    if (!m_Name.equals(m_Name)) {
      return false;
    }
    if ((isNominal()) && (att.isNominal())) {
      if (m_Values.size() != m_Values.size()) {
        return false;
      }
      for (int i = 0; i < m_Values.size(); i++) {
        if (!m_Values.elementAt(i).equals(m_Values.elementAt(i))) {
          return false;
        }
      }
      return true;
    }
    if ((isRelationValued()) && (att.isRelationValued())) {
      if (!m_Header.equalHeaders(m_Header)) {
        return false;
      }
      return true;
    }
    return type() == att.type();
  }
  






  public final int index()
  {
    return m_Index;
  }
  









  public final int indexOfValue(String value)
  {
    if ((!isNominal()) && (!isString()))
      return -1;
    Object store = value;
    if (value.length() > 200) {
      try {
        store = new SerializedObject(value, true);
      } catch (Exception ex) {
        System.err.println("Couldn't compress string attribute value - searching uncompressed.");
      }
    }
    
    Integer val = (Integer)m_Hashtable.get(store);
    if (val == null) return -1;
    return val.intValue();
  }
  






  public final boolean isNominal()
  {
    return m_Type == 1;
  }
  






  public final boolean isNumeric()
  {
    return (m_Type == 0) || (m_Type == 3);
  }
  






  public final boolean isRelationValued()
  {
    return m_Type == 4;
  }
  






  public final boolean isString()
  {
    return m_Type == 2;
  }
  






  public final boolean isDate()
  {
    return m_Type == 3;
  }
  






  public final String name()
  {
    return m_Name;
  }
  







  public final int numValues()
  {
    if ((!isNominal()) && (!isString()) && (!isRelationValued())) {
      return 0;
    }
    return m_Values.size();
  }
  








  public final String toString()
  {
    StringBuffer text = new StringBuffer();
    
    text.append("@attribute").append(" ").append(Utils.quote(m_Name)).append(" ");
    switch (m_Type) {
    case 1: 
      text.append('{');
      Enumeration enu = enumerateValues();
      while (enu.hasMoreElements()) {
        text.append(Utils.quote((String)enu.nextElement()));
        if (enu.hasMoreElements())
          text.append(',');
      }
      text.append('}');
      break;
    case 0: 
      text.append("numeric");
      break;
    case 2: 
      text.append("string");
      break;
    case 3: 
      text.append("date").append(" ").append(Utils.quote(m_DateFormat.toPattern()));
      break;
    case 4: 
      text.append("relational").append("\n");
      Enumeration enm = m_Header.enumerateAttributes();
      while (enm.hasMoreElements()) {
        text.append(enm.nextElement()).append("\n");
      }
      text.append("@end").append(" ").append(Utils.quote(m_Name));
      break;
    default: 
      text.append("UNKNOWN");
    }
    
    return text.toString();
  }
  






  public final int type()
  {
    return m_Type;
  }
  






  public final String getDateFormat()
  {
    if (isDate()) {
      return m_DateFormat.toPattern();
    }
    return "";
  }
  








  public final String value(int valIndex)
  {
    if ((!isNominal()) && (!isString())) {
      return "";
    }
    Object val = m_Values.elementAt(valIndex);
    

    if ((val instanceof SerializedObject)) {
      val = ((SerializedObject)val).getObject();
    }
    return (String)val;
  }
  







  public final Instances relation()
  {
    if (!isRelationValued()) {
      return null;
    }
    return m_Header;
  }
  








  public final Instances relation(int valIndex)
  {
    if (!isRelationValued()) {
      return null;
    }
    return (Instances)m_Values.elementAt(valIndex);
  }
  











  public Attribute(String attributeName, int index)
  {
    this(attributeName);
    m_Index = index;
  }
  














  public Attribute(String attributeName, String dateFormat, int index)
  {
    this(attributeName, dateFormat);
    m_Index = index;
  }
  
















  public Attribute(String attributeName, FastVector attributeValues, int index)
  {
    this(attributeName, attributeValues);
    m_Index = index;
  }
  












  public Attribute(String attributeName, Instances header, int index)
  {
    this(attributeName, header);
    m_Index = index;
  }
  












  public int addStringValue(String value)
  {
    if (!isString()) {
      return -1;
    }
    Object store = value;
    
    if (value.length() > 200) {
      try {
        store = new SerializedObject(value, true);
      } catch (Exception ex) {
        System.err.println("Couldn't compress string attribute value - storing uncompressed.");
      }
    }
    
    Integer index = (Integer)m_Hashtable.get(store);
    if (index != null) {
      return index.intValue();
    }
    int intIndex = m_Values.size();
    m_Values.addElement(store);
    m_Hashtable.put(store, new Integer(intIndex));
    return intIndex;
  }
  
















  public int addStringValue(Attribute src, int index)
  {
    if (!isString()) {
      return -1;
    }
    Object store = m_Values.elementAt(index);
    Integer oldIndex = (Integer)m_Hashtable.get(store);
    if (oldIndex != null) {
      return oldIndex.intValue();
    }
    int intIndex = m_Values.size();
    m_Values.addElement(store);
    m_Hashtable.put(store, new Integer(intIndex));
    return intIndex;
  }
  








  public int addRelation(Instances value)
  {
    if (!isRelationValued()) {
      return -1;
    }
    if (!m_Header.equalHeaders(value)) {
      throw new IllegalArgumentException("Incompatible value for relation-valued attribute.");
    }
    
    Integer index = (Integer)m_Hashtable.get(value);
    if (index != null) {
      return index.intValue();
    }
    int intIndex = m_Values.size();
    m_Values.addElement(value);
    m_Hashtable.put(value, new Integer(intIndex));
    return intIndex;
  }
  







  final void addValue(String value)
  {
    m_Values = ((FastVector)m_Values.copy());
    m_Hashtable = ((Hashtable)m_Hashtable.clone());
    forceAddValue(value);
  }
  










  public final Attribute copy(String newName)
  {
    Attribute copy = new Attribute(newName);
    
    m_Index = m_Index;
    m_DateFormat = m_DateFormat;
    m_Type = m_Type;
    m_Values = m_Values;
    m_Hashtable = m_Hashtable;
    m_Header = m_Header;
    copy.setMetadata(m_Metadata);
    
    return copy;
  }
  











  final void delete(int index)
  {
    if ((!isNominal()) && (!isString()) && (!isRelationValued())) {
      throw new IllegalArgumentException("Can only remove value of nominal, string or relation- valued attribute!");
    }
    

    m_Values = ((FastVector)m_Values.copy());
    m_Values.removeElementAt(index);
    if (!isRelationValued()) {
      Hashtable hash = new Hashtable(m_Hashtable.size());
      Enumeration enu = m_Hashtable.keys();
      while (enu.hasMoreElements()) {
        Object string = enu.nextElement();
        Integer valIndexObject = (Integer)m_Hashtable.get(string);
        int valIndex = valIndexObject.intValue();
        if (valIndex > index) {
          hash.put(string, new Integer(valIndex - 1));
        } else if (valIndex < index) {
          hash.put(string, valIndexObject);
        }
      }
      m_Hashtable = hash;
    }
  }
  








  final void forceAddValue(String value)
  {
    Object store = value;
    if (value.length() > 200) {
      try {
        store = new SerializedObject(value, true);
      } catch (Exception ex) {
        System.err.println("Couldn't compress string attribute value - storing uncompressed.");
      }
    }
    
    m_Values.addElement(store);
    m_Hashtable.put(store, new Integer(m_Values.size() - 1));
  }
  








  final void setIndex(int index)
  {
    m_Index = index;
  }
  












  final void setValue(int index, String string)
  {
    switch (m_Type) {
    case 1: 
    case 2: 
      m_Values = ((FastVector)m_Values.copy());
      m_Hashtable = ((Hashtable)m_Hashtable.clone());
      Object store = string;
      if (string.length() > 200) {
        try {
          store = new SerializedObject(string, true);
        } catch (Exception ex) {
          System.err.println("Couldn't compress string attribute value - storing uncompressed.");
        }
      }
      
      m_Hashtable.remove(m_Values.elementAt(index));
      m_Values.setElementAt(store, index);
      m_Hashtable.put(store, new Integer(index));
      break;
    default: 
      throw new IllegalArgumentException("Can only set values for nominal or string attributes!");
    }
    
  }
  









  final void setValue(int index, Instances data)
  {
    if (isRelationValued()) {
      if (!data.equalHeaders(m_Header)) {
        throw new IllegalArgumentException("Can't set relational value. Headers not compatible.");
      }
      
      m_Values = ((FastVector)m_Values.copy());
      m_Values.setElementAt(data, index);
    } else {
      throw new IllegalArgumentException("Can only set value for relation-valued attributes!");
    }
  }
  









  public String formatDate(double date)
  {
    switch (m_Type) {
    case 3: 
      return m_DateFormat.format(new Date(date));
    }
    throw new IllegalArgumentException("Can only format date values for date attributes!");
  }
  










  public double parseDate(String string)
    throws ParseException
  {
    switch (m_Type) {
    case 3: 
      long time = m_DateFormat.parse(string).getTime();
      
      return time;
    }
    throw new IllegalArgumentException("Can only parse date values for date attributes!");
  }
  







  public final ProtectedProperties getMetadata()
  {
    return m_Metadata;
  }
  









  public final int ordering()
  {
    return m_Ordering;
  }
  





  public final boolean isRegular()
  {
    return m_IsRegular;
  }
  





  public final boolean isAveragable()
  {
    return m_IsAveragable;
  }
  






  public final boolean hasZeropoint()
  {
    return m_HasZeropoint;
  }
  





  public final double weight()
  {
    return m_Weight;
  }
  








  public void setWeight(double value)
  {
    m_Weight = value;
    

    Properties props = new Properties();
    Enumeration names = m_Metadata.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String)names.nextElement();
      if (!name.equals("weight"))
        props.setProperty(name, m_Metadata.getProperty(name));
    }
    props.setProperty("weight", "" + m_Weight);
    m_Metadata = new ProtectedProperties(props);
  }
  





  public final double getLowerNumericBound()
  {
    return m_LowerBound;
  }
  





  public final boolean lowerNumericBoundIsOpen()
  {
    return m_LowerBoundIsOpen;
  }
  





  public final double getUpperNumericBound()
  {
    return m_UpperBound;
  }
  





  public final boolean upperNumericBoundIsOpen()
  {
    return m_UpperBoundIsOpen;
  }
  







  public final boolean isInRange(double value)
  {
    if ((m_Type == 3) || (Instance.isMissingValue(value))) return true;
    if (m_Type != 0)
    {
      int intVal = (int)value;
      if ((intVal < 0) || (intVal >= m_Hashtable.size())) return false;
    }
    else {
      if (m_LowerBoundIsOpen) {
        if (value <= m_LowerBound) return false;
      }
      else if (value < m_LowerBound) { return false;
      }
      if (m_UpperBoundIsOpen) {
        if (value >= m_UpperBound) return false;
      }
      else if (value > m_UpperBound) { return false;
      }
    }
    return true;
  }
  
















  private void setMetadata(ProtectedProperties metadata)
  {
    m_Metadata = metadata;
    
    if (m_Type == 3) {
      m_Ordering = 1;
      m_IsRegular = true;
      m_IsAveragable = false;
      m_HasZeropoint = false;
    }
    else
    {
      String orderString = m_Metadata.getProperty("ordering", "");
      
      String def;
      String def;
      if ((m_Type == 0) && (orderString.compareTo("modulo") != 0) && (orderString.compareTo("symbolic") != 0))
      {

        def = "true"; } else {
        def = "false";
      }
      
      m_IsAveragable = (m_Metadata.getProperty("averageable", def).compareTo("true") == 0);
      
      m_HasZeropoint = (m_Metadata.getProperty("zeropoint", def).compareTo("true") == 0);
      

      if ((m_IsAveragable) || (m_HasZeropoint)) def = "true";
      m_IsRegular = (m_Metadata.getProperty("regular", def).compareTo("true") == 0);
      


      if (orderString.compareTo("symbolic") == 0) {
        m_Ordering = 0;
      } else if (orderString.compareTo("ordered") == 0) {
        m_Ordering = 1;
      } else if (orderString.compareTo("modulo") == 0) {
        m_Ordering = 2;
      }
      else if ((m_Type == 0) || (m_IsAveragable) || (m_HasZeropoint))
        m_Ordering = 1; else {
        m_Ordering = 0;
      }
    }
    

    if ((m_IsAveragable) && (!m_IsRegular)) {
      throw new IllegalArgumentException("An averagable attribute must be regular");
    }
    if ((m_HasZeropoint) && (!m_IsRegular)) {
      throw new IllegalArgumentException("A zeropoint attribute must be regular");
    }
    if ((m_IsRegular) && (m_Ordering == 0)) {
      throw new IllegalArgumentException("A symbolic attribute cannot be regular");
    }
    if ((m_IsAveragable) && (m_Ordering != 1)) {
      throw new IllegalArgumentException("An averagable attribute must be ordered");
    }
    if ((m_HasZeropoint) && (m_Ordering != 1)) {
      throw new IllegalArgumentException("A zeropoint attribute must be ordered");
    }
    

    m_Weight = 1.0D;
    String weightString = m_Metadata.getProperty("weight");
    if (weightString != null) {
      try {
        m_Weight = Double.valueOf(weightString).doubleValue();
      }
      catch (NumberFormatException e) {
        throw new IllegalArgumentException("Not a valid attribute weight: '" + weightString + "'");
      }
    }
    


    if (m_Type == 0) { setNumericRange(m_Metadata.getProperty("range"));
    }
  }
  










  private void setNumericRange(String rangeString)
  {
    m_LowerBound = Double.NEGATIVE_INFINITY;
    m_LowerBoundIsOpen = false;
    m_UpperBound = Double.POSITIVE_INFINITY;
    m_UpperBoundIsOpen = false;
    
    if (rangeString == null) { return;
    }
    
    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(rangeString));
    
    tokenizer.resetSyntax();
    tokenizer.whitespaceChars(0, 32);
    tokenizer.wordChars(33, 255);
    tokenizer.ordinaryChar(91);
    tokenizer.ordinaryChar(40);
    tokenizer.ordinaryChar(44);
    tokenizer.ordinaryChar(93);
    tokenizer.ordinaryChar(41);
    

    try
    {
      tokenizer.nextToken();
      
      if (ttype == 91) { m_LowerBoundIsOpen = false;
      } else if (ttype == 40) m_LowerBoundIsOpen = true; else {
        throw new IllegalArgumentException("Expected opening brace on range, found: " + tokenizer.toString());
      }
      


      tokenizer.nextToken();
      if (ttype != -3) {
        throw new IllegalArgumentException("Expected lower bound in range, found: " + tokenizer.toString());
      }
      
      if (sval.compareToIgnoreCase("-inf") == 0) {
        m_LowerBound = Double.NEGATIVE_INFINITY;
      } else if (sval.compareToIgnoreCase("+inf") == 0) {
        m_LowerBound = Double.POSITIVE_INFINITY;
      } else if (sval.compareToIgnoreCase("inf") == 0)
        m_LowerBound = Double.NEGATIVE_INFINITY; else {
        try {
          m_LowerBound = Double.valueOf(sval).doubleValue();
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Expected lower bound in range, found: '" + sval + "'");
        }
      }
      

      if (tokenizer.nextToken() != 44) {
        throw new IllegalArgumentException("Expected comma in range, found: " + tokenizer.toString());
      }
      


      tokenizer.nextToken();
      if (ttype != -3) {
        throw new IllegalArgumentException("Expected upper bound in range, found: " + tokenizer.toString());
      }
      
      if (sval.compareToIgnoreCase("-inf") == 0) {
        m_UpperBound = Double.NEGATIVE_INFINITY;
      } else if (sval.compareToIgnoreCase("+inf") == 0) {
        m_UpperBound = Double.POSITIVE_INFINITY;
      } else if (sval.compareToIgnoreCase("inf") == 0)
        m_UpperBound = Double.POSITIVE_INFINITY; else {
        try {
          m_UpperBound = Double.valueOf(sval).doubleValue();
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Expected upper bound in range, found: '" + sval + "'");
        }
      }
      

      tokenizer.nextToken();
      
      if (ttype == 93) { m_UpperBoundIsOpen = false;
      } else if (ttype == 41) m_UpperBoundIsOpen = true; else {
        throw new IllegalArgumentException("Expected closing brace on range, found: " + tokenizer.toString());
      }
      


      if (tokenizer.nextToken() != -1) {
        throw new IllegalArgumentException("Expected end of range string, found: " + tokenizer.toString());
      }
    }
    catch (IOException e)
    {
      throw new IllegalArgumentException("IOException reading attribute range string: " + e.getMessage());
    }
    

    if (m_UpperBound < m_LowerBound) {
      throw new IllegalArgumentException("Upper bound (" + m_UpperBound + ") on numeric range is" + " less than lower bound (" + m_LowerBound + ")!");
    }
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9518 $");
  }
  








  public static void main(String[] ops)
  {
    try
    {
      Attribute length = new Attribute("length");
      Attribute weight = new Attribute("weight");
      

      Attribute date = new Attribute("date", "yyyy-MM-dd HH:mm:ss");
      
      System.out.println(date);
      double dd = date.parseDate("2001-04-04 14:13:55");
      System.out.println("Test date = " + dd);
      System.out.println(date.formatDate(dd));
      
      dd = new Date().getTime();
      System.out.println("Date now = " + dd);
      System.out.println(date.formatDate(dd));
      

      FastVector my_nominal_values = new FastVector(3);
      my_nominal_values.addElement("first");
      my_nominal_values.addElement("second");
      my_nominal_values.addElement("third");
      

      Attribute position = new Attribute("position", my_nominal_values);
      

      System.out.println("Name of \"position\": " + position.name());
      

      Enumeration attValues = position.enumerateValues();
      while (attValues.hasMoreElements()) {
        String string = (String)attValues.nextElement();
        System.out.println("Value of \"position\": " + string);
      }
      

      Attribute copy = (Attribute)position.copy();
      

      System.out.println("Copy is the same as original: " + copy.equals(position));
      

      System.out.println("Index of attribute \"weight\" (should be -1): " + weight.index());
      


      System.out.println("Index of value \"first\" of \"position\" (should be 0): " + position.indexOfValue("first"));
      


      System.out.println("\"position\" is numeric: " + position.isNumeric());
      System.out.println("\"position\" is nominal: " + position.isNominal());
      System.out.println("\"position\" is string: " + position.isString());
      

      System.out.println("Name of \"position\": " + position.name());
      

      System.out.println("Number of values for \"position\": " + position.numValues());
      

      for (int i = 0; i < position.numValues(); i++) {
        System.out.println("Value " + i + ": " + position.value(i));
      }
      

      System.out.println(position);
      

      switch (position.type()) {
      case 0: 
        System.out.println("\"position\" is numeric");
        break;
      case 1: 
        System.out.println("\"position\" is nominal");
        break;
      case 2: 
        System.out.println("\"position\" is string");
        break;
      case 3: 
        System.out.println("\"position\" is date");
        break;
      case 4: 
        System.out.println("\"position\" is relation-valued");
        break;
      default: 
        System.out.println("\"position\" has unknown type");
      }
      
      FastVector atts = new FastVector(1);
      atts.addElement(position);
      Instances relation = new Instances("Test", atts, 0);
      Attribute relationValuedAtt = new Attribute("test", relation);
      System.out.println(relationValuedAtt);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
