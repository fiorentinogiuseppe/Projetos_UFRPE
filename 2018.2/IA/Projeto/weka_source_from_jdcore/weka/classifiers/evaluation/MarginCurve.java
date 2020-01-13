package weka.classifiers.evaluation;

import java.io.InputStreamReader;
import java.io.PrintStream;
import weka.classifiers.meta.LogitBoost;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;











































public class MarginCurve
  implements RevisionHandler
{
  public MarginCurve() {}
  
  public Instances getCurve(FastVector predictions)
  {
    if (predictions.size() == 0) {
      return null;
    }
    
    Instances insts = makeHeader();
    double[] margins = getMargins(predictions);
    int[] sorted = Utils.sort(margins);
    int binMargin = 0;
    int totalMargin = 0;
    insts.add(makeInstance(-1.0D, binMargin, totalMargin));
    for (int i = 0; i < sorted.length; i++) {
      double current = margins[sorted[i]];
      double weight = ((NominalPrediction)predictions.elementAt(sorted[i])).weight();
      
      totalMargin = (int)(totalMargin + weight);
      binMargin = (int)(binMargin + weight);
      
      insts.add(makeInstance(current, binMargin, totalMargin));
      binMargin = 0;
    }
    
    return insts;
  }
  







  private double[] getMargins(FastVector predictions)
  {
    double[] margins = new double[predictions.size()];
    for (int i = 0; i < margins.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
      margins[i] = pred.margin();
    }
    return margins;
  }
  





  private Instances makeHeader()
  {
    FastVector fv = new FastVector();
    fv.addElement(new Attribute("Margin"));
    fv.addElement(new Attribute("Current"));
    fv.addElement(new Attribute("Cumulative"));
    return new Instances("MarginCurve", fv, 100);
  }
  









  private Instance makeInstance(double margin, int current, int cumulative)
  {
    int count = 0;
    double[] vals = new double[3];
    vals[(count++)] = margin;
    vals[(count++)] = current;
    vals[(count++)] = cumulative;
    return new Instance(1.0D, vals);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
  





  public static void main(String[] args)
  {
    try
    {
      Utils.SMALL = 0.0D;
      Instances inst = new Instances(new InputStreamReader(System.in));
      inst.setClassIndex(inst.numAttributes() - 1);
      MarginCurve tc = new MarginCurve();
      EvaluationUtils eu = new EvaluationUtils();
      LogitBoost classifier = new LogitBoost();
      
      classifier.setNumIterations(20);
      FastVector predictions = eu.getTrainTestPredictions(classifier, inst, inst);
      
      Instances result = tc.getCurve(predictions);
      System.out.println(result);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
