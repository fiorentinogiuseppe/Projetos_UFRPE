package weka.classifiers.evaluation;

import java.io.InputStreamReader;
import java.io.PrintStream;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

















































public class CostCurve
  implements RevisionHandler
{
  public static final String RELATION_NAME = "CostCurve";
  public static final String PROB_COST_FUNC_NAME = "Probability Cost Function";
  public static final String NORM_EXPECTED_COST_NAME = "Normalized Expected Cost";
  public static final String THRESHOLD_NAME = "Threshold";
  
  public CostCurve() {}
  
  public Instances getCurve(FastVector predictions)
  {
    if (predictions.size() == 0) {
      return null;
    }
    return getCurve(predictions, ((NominalPrediction)predictions.elementAt(0)).distribution().length - 1);
  }
  










  public Instances getCurve(FastVector predictions, int classIndex)
  {
    if ((predictions.size() == 0) || (((NominalPrediction)predictions.elementAt(0)).distribution().length <= classIndex))
    {

      return null;
    }
    
    ThresholdCurve tc = new ThresholdCurve();
    Instances threshInst = tc.getCurve(predictions, classIndex);
    
    Instances insts = makeHeader();
    int fpind = threshInst.attribute("False Positive Rate").index();
    int tpind = threshInst.attribute("True Positive Rate").index();
    int threshind = threshInst.attribute("Threshold").index();
    


    for (int i = 0; i < threshInst.numInstances(); i++) {
      double fpval = threshInst.instance(i).value(fpind);
      double tpval = threshInst.instance(i).value(tpind);
      double thresh = threshInst.instance(i).value(threshind);
      double[] vals = new double[3];
      vals[0] = 0.0D;vals[1] = fpval;vals[2] = thresh;
      insts.add(new Instance(1.0D, vals));
      vals = new double[3];
      vals[0] = 1.0D;vals[1] = (1.0D - tpval);vals[2] = thresh;
      insts.add(new Instance(1.0D, vals));
    }
    
    return insts;
  }
  





  private Instances makeHeader()
  {
    FastVector fv = new FastVector();
    fv.addElement(new Attribute("Probability Cost Function"));
    fv.addElement(new Attribute("Normalized Expected Cost"));
    fv.addElement(new Attribute("Threshold"));
    return new Instances("CostCurve", fv, 100);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
  






  public static void main(String[] args)
  {
    try
    {
      Instances inst = new Instances(new InputStreamReader(System.in));
      
      inst.setClassIndex(inst.numAttributes() - 1);
      CostCurve cc = new CostCurve();
      EvaluationUtils eu = new EvaluationUtils();
      Classifier classifier = new Logistic();
      FastVector predictions = new FastVector();
      for (int i = 0; i < 2; i++) {
        eu.setSeed(i);
        predictions.appendElements(eu.getCVPredictions(classifier, inst, 10));
      }
      
      Instances result = cc.getCurve(predictions);
      System.out.println(result);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
