package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeExpression;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;
























































public class AddExpression
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = 402130384261736245L;
  private String m_infixExpression = "a1^2";
  


  private String m_attributeName = "expression";
  


  private boolean m_Debug = false;
  
  private AttributeExpression m_attributeExpression = null;
  


  public AddExpression() {}
  

  public String globalInfo()
  {
    return "An instance filter that creates a new attribute by applying a mathematical expression to existing attributes. The expression can contain attribute references and numeric constants. Supported operators are :\n+, -, *, /, ^, log, abs, cos, exp, sqrt, floor, ceil, rint, tan, sin, (, )\nAttributes are specified by prefixing with 'a', eg. a7 is attribute number 7 (starting from 1).\nExample expression : a1^2*a5/log(a7*4.0).";
  }
  














  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tSpecify the expression to apply. Eg a1^2*a5/log(a7*4.0).\n\tSupported opperators: ,+, -, *, /, ^, log, abs, cos, \n\texp, sqrt, floor, ceil, rint, tan, sin, (, )\n\t(default: a1^2)", "E", 1, "-E <expression>"));
    





    newVector.addElement(new Option("\tSpecify the name for the new attribute. (default is the expression provided with -E)", "N", 1, "-N <name>"));
    



    newVector.addElement(new Option("\tDebug. Names attribute with the postfix parse of the expression.", "D", 0, "-D"));
    


    return newVector.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String expString = Utils.getOption('E', options);
    if (expString.length() != 0) {
      setExpression(expString);
    } else {
      setExpression("a1^2");
    }
    
    String name = Utils.getOption('N', options);
    if (name.length() != 0) {
      setName(name);
    }
    
    setDebug(Utils.getFlag('D', options));
  }
  





  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    options[(current++)] = "-E";options[(current++)] = getExpression();
    options[(current++)] = "-N";options[(current++)] = getName();
    
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String nameTipText()
  {
    return "Set the name of the new attribute.";
  }
  





  public void setName(String name)
  {
    m_attributeName = name;
  }
  



  public String getName()
  {
    return m_attributeName;
  }
  





  public String debugTipText()
  {
    return "Set debug mode. If true then the new attribute will be named with the postfix parse of the supplied expression.";
  }
  





  public void setDebug(boolean d)
  {
    m_Debug = d;
  }
  



  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public String expressionTipText()
  {
    return "Set the math expression to apply. Eg. a1^2*a5/log(a7*4.0)";
  }
  



  public void setExpression(String expr)
  {
    m_infixExpression = expr;
  }
  



  public String getExpression()
  {
    return m_infixExpression;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    m_attributeExpression = new AttributeExpression();
    m_attributeExpression.convertInfixToPostfix(new String(m_infixExpression));
    

    super.setInputFormat(instanceInfo);
    
    Instances outputFormat = new Instances(instanceInfo, 0);
    Attribute newAttribute;
    Attribute newAttribute; if (m_Debug) {
      newAttribute = new Attribute(m_attributeExpression.getPostFixExpression());
    } else { Attribute newAttribute;
      if (m_attributeName.compareTo("expression") != 0) {
        newAttribute = new Attribute(m_attributeName);
      } else
        newAttribute = new Attribute(m_infixExpression);
    }
    outputFormat.insertAttributeAt(newAttribute, instanceInfo.numAttributes());
    
    setOutputFormat(outputFormat);
    return true;
  }
  










  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    double[] vals = new double[instance.numAttributes() + 1];
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (instance.isMissing(i)) {
        vals[i] = Instance.missingValue();
      } else {
        vals[i] = instance.value(i);
      }
    }
    
    m_attributeExpression.evaluateExpression(vals);
    
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    
    inst.setDataset(getOutputFormat());
    copyValues(inst, false, instance.dataset(), getOutputFormat());
    inst.setDataset(getOutputFormat());
    push(inst);
    return true;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new AddExpression(), args);
  }
}
