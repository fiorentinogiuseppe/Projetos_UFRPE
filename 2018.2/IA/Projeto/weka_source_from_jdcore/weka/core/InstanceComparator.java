package weka.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Comparator;









































public class InstanceComparator
  implements Comparator, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -6589278678230949683L;
  protected boolean m_IncludeClass;
  
  public InstanceComparator()
  {
    this(true);
  }
  



  public InstanceComparator(boolean includeClass)
  {
    setIncludeClass(includeClass);
  }
  




  public void setIncludeClass(boolean includeClass)
  {
    m_IncludeClass = includeClass;
  }
  


  public boolean getIncludeClass()
  {
    return m_IncludeClass;
  }
  



















  public int compare(Object o1, Object o2)
  {
    Instance inst1 = (Instance)o1;
    Instance inst2 = (Instance)o2;
    int classindex;
    int classindex;
    if (inst1.classIndex() == -1) {
      classindex = inst1.numAttributes() - 1;
    } else {
      classindex = inst1.classIndex();
    }
    int result = 0;
    label328: for (int i = 0; i < inst1.numAttributes(); i++)
    {
      if ((getIncludeClass()) || (i != classindex))
      {



        if ((inst1.isMissing(i)) || (inst2.isMissing(i))) {
          if ((!inst1.isMissing(i)) || (!inst2.isMissing(i)))
          {


            if (inst1.isMissing(i)) {
              result = -1; break;
            }
            result = 1;
            break;
          } } else { Instances data1;
          Instances data2;
          int n;
          InstanceComparator comp;
          switch (inst1.attribute(i).type()) {
          case 2: 
            result = inst1.stringValue(i).compareTo(inst2.stringValue(i));
            break;
          case 4: 
            data1 = inst1.relationalValue(i);
            data2 = inst2.relationalValue(i);
            n = 0;
            comp = new InstanceComparator();
          default:  while ((n < data1.numInstances()) && (n < data2.numInstances()) && (result == 0)) {
              result = comp.compare(data1.instance(n), data2.instance(n));
              n++; continue;
              


              if (Utils.eq(inst1.value(i), inst2.value(i))) {
                break label328;
              }
              
              if (inst1.value(i) < inst2.value(i)) {
                result = -1;
              } else {
                result = 1;
              }
            }
          }
          
          if (result != 0)
            break;
        } }
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7988 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    if (args.length == 0) {
      return;
    }
    
    Instances inst = new Instances(new BufferedReader(new FileReader(args[0])));
    inst.setClassIndex(inst.numAttributes() - 1);
    

    Comparator comp = new InstanceComparator();
    System.out.println("\nIncluding the class");
    System.out.println("comparing 1. instance with 1.: " + comp.compare(inst.instance(0), inst.instance(0)));
    System.out.println("comparing 1. instance with 2.: " + comp.compare(inst.instance(0), inst.instance(1)));
    System.out.println("comparing 2. instance with 1.: " + comp.compare(inst.instance(1), inst.instance(0)));
    

    comp = new InstanceComparator(false);
    System.out.println("\nExcluding the class");
    System.out.println("comparing 1. instance with 1.: " + comp.compare(inst.instance(0), inst.instance(0)));
    System.out.println("comparing 1. instance with 2.: " + comp.compare(inst.instance(0), inst.instance(1)));
    System.out.println("comparing 2. instance with 1.: " + comp.compare(inst.instance(1), inst.instance(0)));
  }
}
