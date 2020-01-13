package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.experiment.Stats;
import weka.filters.SimpleBatchFilter;










































































public class RELAGGS
  extends SimpleBatchFilter
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -3333791375278589231L;
  protected int m_MaxCardinality = 20;
  

  protected Range m_SelectedRange = new Range("first-last");
  


  protected Hashtable<String, AttributeStats> m_AttStats = new Hashtable();
  


  public RELAGGS() {}
  

  public String globalInfo()
  {
    return "A propositionalization filter inspired by the RELAGGS algorithm.\nIt processes all relational attributes that fall into the user defined range (all others are skipped, i.e., not added to the output). Currently, the filter only processes one level of nesting.\nThe class attribute is not touched.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  
















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "M.-A. Krogel and S. Wrobel");
    result.setValue(TechnicalInformation.Field.TITLE, "Facets of Aggregation Approaches to Propositionalization");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Work-in-Progress Track at the Thirteenth International Conference on Inductive Logic Programming (ILP)");
    result.setValue(TechnicalInformation.Field.EDITOR, "T. Horvath and A. Yamamoto");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.PDF, "http://kd.cs.uni-magdeburg.de/~krogel/papers/aggs.pdf");
    
    return result;
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tSpecify list of string attributes to convert to words.\n\t(default: select all relational attributes)", "R", 1, "-R <index1,index2-index4,...>"));
    



    result.addElement(new Option("\tInverts the matching sense of the selection.", "V", 0, "-V"));
    


    result.addElement(new Option("\tMax. cardinality of nominal attributes. If a nominal attribute\n\thas more values than this upper limit, then it will be skipped.\n\t(default: 20)", "C", 1, "-C <num>"));
    




    return result.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setSelectedRange(tmpStr);
    } else {
      setSelectedRange("first-last");
    }
    setInvertSelection(Utils.getFlag('V', options));
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setMaxCardinality(Integer.parseInt(tmpStr));
    } else {
      setMaxCardinality(20);
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-R");
    result.add(getSelectedRange().getRanges());
    
    if (getInvertSelection()) {
      result.add("-V");
    }
    result.add("-C");
    result.add("" + getMaxCardinality());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String maxCardinalityTipText()
  {
    return "The maximum number of values a nominal attribute can have before it's skipped.";
  }
  





  public void setMaxCardinality(int value)
  {
    m_MaxCardinality = value;
  }
  





  public int getMaxCardinality()
  {
    return m_MaxCardinality;
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on; this is a comma separated list of attribute indices, with \"first\" and \"last\" valid values; Specify an inclusive range with \"-\"; eg: \"first-3,5,6-10,last\".";
  }
  








  public void setSelectedRange(String value)
  {
    m_SelectedRange = new Range(value);
  }
  




  public Range getSelectedRange()
  {
    return m_SelectedRange;
  }
  





  public String invertSelectionTipText()
  {
    return "Set attribute selection mode. If false, only selected attributes in the range will be worked on; if true, only non-selected attributes will be processed.";
  }
  







  public void setInvertSelection(boolean value)
  {
    m_SelectedRange.setInvert(value);
  }
  




  public boolean getInvertSelection()
  {
    return m_SelectedRange.getInvert();
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  






















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    m_SelectedRange.setUpper(inputFormat.numAttributes() - 1);
    
    FastVector atts = new FastVector();
    int clsIndex = -1;
    for (int i = 0; i < inputFormat.numAttributes(); i++)
    {
      if (i == inputFormat.classIndex()) {
        clsIndex = atts.size();
        atts.addElement(inputFormat.attribute(i).copy());


      }
      else if (!inputFormat.attribute(i).isRelationValued()) {
        atts.addElement(inputFormat.attribute(i).copy());


      }
      else if (!m_SelectedRange.isInRange(i)) {
        if (getDebug()) {
          System.out.println("Attribute " + (i + 1) + " (" + inputFormat.attribute(i).name() + ") skipped.");
        }
        

      }
      else
      {
        String prefix = inputFormat.attribute(i).name() + "_";
        Instances relFormat = inputFormat.attribute(i).relation();
        for (int n = 0; n < relFormat.numAttributes(); n++) {
          Attribute att = relFormat.attribute(n);
          
          if (att.isNumeric()) {
            atts.addElement(new Attribute(prefix + att.name() + "_MIN"));
            atts.addElement(new Attribute(prefix + att.name() + "_MAX"));
            atts.addElement(new Attribute(prefix + att.name() + "_AVG"));
            atts.addElement(new Attribute(prefix + att.name() + "_STDEV"));
            atts.addElement(new Attribute(prefix + att.name() + "_SUM"));
          }
          else if (att.isNominal()) {
            if (att.numValues() <= m_MaxCardinality) {
              for (int m = 0; m < att.numValues(); m++) {
                atts.addElement(new Attribute(prefix + att.name() + "_" + att.value(m) + "_CNT"));
              }
            }
            if (getDebug()) {
              System.out.println("Attribute " + (i + 1) + "/" + (n + 1) + " (" + inputFormat.attribute(i).name() + "/" + att.name() + ") skipped, " + att.numValues() + " > " + m_MaxCardinality + ".");

            }
            


          }
          else if (getDebug()) {
            System.out.println("Attribute " + (i + 1) + "/" + (n + 1) + " (" + inputFormat.attribute(i).name() + "/" + att.name() + ") skipped.");
          }
        }
      }
    }
    



    Instances result = new Instances(inputFormat.relationName(), atts, 0);
    result.setClassIndex(clsIndex);
    


    initOutputLocators(result, new int[0]);
    
    return result;
  }
  



















  protected Instances process(Instances instances)
    throws Exception
  {
    Instances result = getOutputFormat();
    

    m_AttStats.clear();
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      if (i != instances.classIndex())
      {

        if (instances.attribute(i).isRelationValued())
        {

          if (m_SelectedRange.isInRange(i))
          {


            for (int k = 0; k < instances.numInstances(); k++) {
              Instances relInstances = instances.instance(k).relationalValue(i);
              
              for (int n = 0; n < relInstances.numAttributes(); n++) {
                Attribute att = relInstances.attribute(n);
                AttributeStats stats = null;
                
                if ((att.isNumeric()) || ((att.isNominal()) && (att.numValues() <= m_MaxCardinality)))
                {
                  stats = relInstances.attributeStats(n);
                  m_AttStats.put(k + "-" + i + "-" + n, stats);
                }
              }
            } }
        }
      }
    }
    for (int k = 0; k < instances.numInstances(); k++) {
      Instance inst = instances.instance(k);
      Instance newInst = new Instance(result.numAttributes());
      newInst.setWeight(inst.weight());
      
      int l = 0;
      for (i = 0; i < instances.numAttributes(); i++) {
        if (!instances.attribute(i).isRelationValued()) {
          newInst.setValue(l, inst.value(i));
          l++;

        }
        else if (m_SelectedRange.isInRange(i))
        {


          Instances relInstances = inst.relationalValue(i);
          for (int n = 0; n < relInstances.numAttributes(); n++) {
            Attribute att = relInstances.attribute(n);
            AttributeStats stats = (AttributeStats)m_AttStats.get(k + "-" + i + "-" + n);
            
            if (att.isNumeric()) {
              newInst.setValue(l, numericStats.min);l++;
              newInst.setValue(l, numericStats.max);l++;
              newInst.setValue(l, numericStats.mean);l++;
              newInst.setValue(l, numericStats.stdDev);l++;
              newInst.setValue(l, numericStats.sum);l++;
            }
            else if ((att.isNominal()) && (att.numValues() <= m_MaxCardinality)) {
              for (int m = 0; m < att.numValues(); m++) {
                newInst.setValue(l, nominalCounts[m]);
                l++;
              }
            }
          }
        }
      }
      
      result.add(newInst);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5547 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new RELAGGS(), args);
  }
}
