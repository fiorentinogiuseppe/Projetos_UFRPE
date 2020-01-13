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
import weka.core.Utils;





































































public class ThresholdCurve
  implements RevisionHandler
{
  public static final String RELATION_NAME = "ThresholdCurve";
  public static final String TRUE_POS_NAME = "True Positives";
  public static final String FALSE_NEG_NAME = "False Negatives";
  public static final String FALSE_POS_NAME = "False Positives";
  public static final String TRUE_NEG_NAME = "True Negatives";
  public static final String FP_RATE_NAME = "False Positive Rate";
  public static final String TP_RATE_NAME = "True Positive Rate";
  public static final String PRECISION_NAME = "Precision";
  public static final String RECALL_NAME = "Recall";
  public static final String FALLOUT_NAME = "Fallout";
  public static final String FMEASURE_NAME = "FMeasure";
  public static final String SAMPLE_SIZE_NAME = "Sample Size";
  public static final String LIFT_NAME = "Lift";
  public static final String THRESHOLD_NAME = "Threshold";
  
  public ThresholdCurve() {}
  
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
    
    double totPos = 0.0D;double totNeg = 0.0D;
    double[] probs = getProbabilities(predictions, classIndex);
    

    for (int i = 0; i < probs.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
      if (pred.actual() == Prediction.MISSING_VALUE) {
        System.err.println(getClass().getName() + " Skipping prediction with missing class value");


      }
      else if (pred.weight() < 0.0D) {
        System.err.println(getClass().getName() + " Skipping prediction with negative weight");


      }
      else if (pred.actual() == classIndex) {
        totPos += pred.weight();
      } else {
        totNeg += pred.weight();
      }
    }
    
    Instances insts = makeHeader();
    int[] sorted = Utils.sort(probs);
    TwoClassStats tc = new TwoClassStats(totPos, totNeg, 0.0D, 0.0D);
    double threshold = 0.0D;
    double cumulativePos = 0.0D;
    double cumulativeNeg = 0.0D;
    for (int i = 0; i < sorted.length; i++)
    {
      if ((i == 0) || (probs[sorted[i]] > threshold)) {
        tc.setTruePositive(tc.getTruePositive() - cumulativePos);
        tc.setFalseNegative(tc.getFalseNegative() + cumulativePos);
        tc.setFalsePositive(tc.getFalsePositive() - cumulativeNeg);
        tc.setTrueNegative(tc.getTrueNegative() + cumulativeNeg);
        threshold = probs[sorted[i]];
        insts.add(makeInstance(tc, threshold));
        cumulativePos = 0.0D;
        cumulativeNeg = 0.0D;
        if (i == sorted.length - 1) {
          break;
        }
      }
      
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(sorted[i]);
      
      if (pred.actual() == Prediction.MISSING_VALUE) {
        System.err.println(getClass().getName() + " Skipping prediction with missing class value");


      }
      else if (pred.weight() < 0.0D) {
        System.err.println(getClass().getName() + " Skipping prediction with negative weight");


      }
      else if (pred.actual() == classIndex) {
        cumulativePos += pred.weight();
      } else {
        cumulativeNeg += pred.weight();
      }
    }
    











    if ((tc.getFalseNegative() != totPos) || (tc.getTrueNegative() != totNeg)) {
      tc = new TwoClassStats(0.0D, 0.0D, totNeg, totPos);
      threshold = probs[sorted[(sorted.length - 1)]] + 1.0E-5D;
      insts.add(makeInstance(tc, threshold));
    }
    
    return insts;
  }
  








  public static double getNPointPrecision(Instances tcurve, int n)
  {
    if ((!"ThresholdCurve".equals(tcurve.relationName())) || (tcurve.numInstances() == 0))
    {
      return NaN.0D;
    }
    int recallInd = tcurve.attribute("Recall").index();
    int precisInd = tcurve.attribute("Precision").index();
    double[] recallVals = tcurve.attributeToDoubleArray(recallInd);
    int[] sorted = Utils.sort(recallVals);
    double isize = 1.0D / (n - 1);
    double psum = 0.0D;
    for (int i = 0; i < n; i++) {
      int pos = binarySearch(sorted, recallVals, i * isize);
      double recall = recallVals[sorted[pos]];
      double precis = tcurve.instance(sorted[pos]).value(precisInd);
      






      while ((pos != 0) && (pos < sorted.length - 1)) {
        pos++;
        double recall2 = recallVals[sorted[pos]];
        if (recall2 != recall) {
          double precis2 = tcurve.instance(sorted[pos]).value(precisInd);
          double slope = (precis2 - precis) / (recall2 - recall);
          double offset = precis - recall * slope;
          precis = isize * i * slope + offset;
          






          break;
        }
      }
      psum += precis;
    }
    return psum / n;
  }
  







  public static double getROCArea(Instances tcurve)
  {
    int n = tcurve.numInstances();
    if ((!"ThresholdCurve".equals(tcurve.relationName())) || (n == 0))
    {
      return NaN.0D;
    }
    int tpInd = tcurve.attribute("True Positives").index();
    int fpInd = tcurve.attribute("False Positives").index();
    double[] tpVals = tcurve.attributeToDoubleArray(tpInd);
    double[] fpVals = tcurve.attributeToDoubleArray(fpInd);
    
    double area = 0.0D;double cumNeg = 0.0D;
    double totalPos = tpVals[0];
    double totalNeg = fpVals[0];
    for (int i = 0; i < n; i++) { double cin;
      double cip;
      double cin; if (i < n - 1) {
        double cip = tpVals[i] - tpVals[(i + 1)];
        cin = fpVals[i] - fpVals[(i + 1)];
      } else {
        cip = tpVals[(n - 1)];
        cin = fpVals[(n - 1)];
      }
      area += cip * (cumNeg + 0.5D * cin);
      cumNeg += cin;
    }
    area /= totalNeg * totalPos;
    
    return area;
  }
  










  public static int getThresholdInstance(Instances tcurve, double threshold)
  {
    if ((!"ThresholdCurve".equals(tcurve.relationName())) || (tcurve.numInstances() == 0) || (threshold < 0.0D) || (threshold > 1.0D))
    {


      return -1;
    }
    if (tcurve.numInstances() == 1) {
      return 0;
    }
    double[] tvals = tcurve.attributeToDoubleArray(tcurve.numAttributes() - 1);
    int[] sorted = Utils.sort(tvals);
    return binarySearch(sorted, tvals, threshold);
  }
  








  private static int binarySearch(int[] index, double[] vals, double target)
  {
    int lo = 0;int hi = index.length - 1;
    while (hi - lo > 1) {
      int mid = lo + (hi - lo) / 2;
      double midval = vals[index[mid]];
      if (target > midval) {
        lo = mid;
      } else if (target < midval) {
        hi = mid;
      } else {
        while ((mid > 0) && (vals[index[(mid - 1)]] == target)) {
          mid--;
        }
        return mid;
      }
    }
    return lo;
  }
  







  private double[] getProbabilities(FastVector predictions, int classIndex)
  {
    double[] probs = new double[predictions.size()];
    for (int i = 0; i < probs.length; i++) {
      NominalPrediction pred = (NominalPrediction)predictions.elementAt(i);
      probs[i] = pred.distribution()[classIndex];
    }
    return probs;
  }
  





  private Instances makeHeader()
  {
    FastVector fv = new FastVector();
    fv.addElement(new Attribute("True Positives"));
    fv.addElement(new Attribute("False Negatives"));
    fv.addElement(new Attribute("False Positives"));
    fv.addElement(new Attribute("True Negatives"));
    fv.addElement(new Attribute("False Positive Rate"));
    fv.addElement(new Attribute("True Positive Rate"));
    fv.addElement(new Attribute("Precision"));
    fv.addElement(new Attribute("Recall"));
    fv.addElement(new Attribute("Fallout"));
    fv.addElement(new Attribute("FMeasure"));
    fv.addElement(new Attribute("Sample Size"));
    fv.addElement(new Attribute("Lift"));
    fv.addElement(new Attribute("Threshold"));
    return new Instances("ThresholdCurve", fv, 100);
  }
  







  private Instance makeInstance(TwoClassStats tc, double prob)
  {
    int count = 0;
    double[] vals = new double[13];
    vals[(count++)] = tc.getTruePositive();
    vals[(count++)] = tc.getFalseNegative();
    vals[(count++)] = tc.getFalsePositive();
    vals[(count++)] = tc.getTrueNegative();
    vals[(count++)] = tc.getFalsePositiveRate();
    vals[(count++)] = tc.getTruePositiveRate();
    vals[(count++)] = tc.getPrecision();
    vals[(count++)] = tc.getRecall();
    vals[(count++)] = tc.getFallout();
    vals[(count++)] = tc.getFMeasure();
    double ss = (tc.getTruePositive() + tc.getFalsePositive()) / (tc.getTruePositive() + tc.getFalsePositive() + tc.getTrueNegative() + tc.getFalseNegative());
    
    vals[(count++)] = ss;
    double expectedByChance = ss * (tc.getTruePositive() + tc.getFalseNegative());
    if (expectedByChance < 1.0D) {
      vals[(count++)] = Instance.missingValue();
    } else {
      vals[(count++)] = (tc.getTruePositive() / expectedByChance);
    }
    
    vals[(count++)] = prob;
    return new Instance(1.0D, vals);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7833 $");
  }
  






  public static void main(String[] args)
  {
    try
    {
      Instances inst = new Instances(new InputStreamReader(System.in));
      


      inst.setClassIndex(inst.numAttributes() - 1);
      ThresholdCurve tc = new ThresholdCurve();
      EvaluationUtils eu = new EvaluationUtils();
      Classifier classifier = new Logistic();
      FastVector predictions = new FastVector();
      for (int i = 0; i < 2; i++) {
        eu.setSeed(i);
        predictions.appendElements(eu.getCVPredictions(classifier, inst, 10));
      }
      
      Instances result = tc.getCurve(predictions);
      System.out.println(result);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
