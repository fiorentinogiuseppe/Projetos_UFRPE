package weka.classifiers.evaluation;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;





























public class EvaluationUtils
  implements RevisionHandler
{
  public EvaluationUtils() {}
  
  private int m_Seed = 1;
  
  public void setSeed(int seed) {
    m_Seed = seed;
  }
  
  public int getSeed() { return m_Seed; }
  











  public FastVector getCVPredictions(Classifier classifier, Instances data, int numFolds)
    throws Exception
  {
    FastVector predictions = new FastVector();
    Instances runInstances = new Instances(data);
    Random random = new Random(m_Seed);
    runInstances.randomize(random);
    if ((runInstances.classAttribute().isNominal()) && (numFolds > 1)) {
      runInstances.stratify(numFolds);
    }
    int inst = 0;
    for (int fold = 0; fold < numFolds; fold++) {
      Instances train = runInstances.trainCV(numFolds, fold, random);
      Instances test = runInstances.testCV(numFolds, fold);
      FastVector foldPred = getTrainTestPredictions(classifier, train, test);
      predictions.appendElements(foldPred);
    }
    return predictions;
  }
  










  public FastVector getTrainTestPredictions(Classifier classifier, Instances train, Instances test)
    throws Exception
  {
    classifier.buildClassifier(train);
    return getTestPredictions(classifier, test);
  }
  









  public FastVector getTestPredictions(Classifier classifier, Instances test)
    throws Exception
  {
    FastVector predictions = new FastVector();
    for (int i = 0; i < test.numInstances(); i++) {
      if (!test.instance(i).classIsMissing()) {
        predictions.addElement(getPrediction(classifier, test.instance(i)));
      }
    }
    return predictions;
  }
  










  public Prediction getPrediction(Classifier classifier, Instance test)
    throws Exception
  {
    double actual = test.classValue();
    double[] dist = classifier.distributionForInstance(test);
    if (test.classAttribute().isNominal()) {
      return new NominalPrediction(actual, dist, test.weight());
    }
    return new NumericPrediction(actual, dist[0], test.weight());
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
}
