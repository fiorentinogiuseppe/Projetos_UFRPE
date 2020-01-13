package weka.classifiers.functions.pace;

import weka.core.RevisionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.IntVector;






















































public abstract class MixtureDistribution
  implements TechnicalInformationHandler, RevisionHandler
{
  protected DiscreteFunction mixingDistribution;
  public static final int NNMMethod = 1;
  public static final int PMMethod = 2;
  
  public MixtureDistribution() {}
  
  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.PHDTHESIS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Wang, Y");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.TITLE, "A new approach to fitting linear models in high dimensional spaces");
    result.setValue(TechnicalInformation.Field.SCHOOL, "Department of Computer Science, University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Hamilton, New Zealand");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Wang, Y. and Witten, I. H.");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.TITLE, "Modeling for optimal probability prediction");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the Nineteenth International Conference in Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.PAGES, "650-657");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Sydney, Australia");
    
    return result;
  }
  




  public DiscreteFunction getMixingDistribution()
  {
    return mixingDistribution;
  }
  


  public void setMixingDistribution(DiscreteFunction d)
  {
    mixingDistribution = d;
  }
  


  public void fit(DoubleVector data)
  {
    fit(data, 1);
  }
  



  public void fit(DoubleVector data, int method)
  {
    DoubleVector data2 = (DoubleVector)data.clone();
    if (data2.unsorted()) { data2.sort();
    }
    int n = data2.size();
    int start = 0;
    
    DiscreteFunction d = new DiscreteFunction();
    for (int i = 0; i < n - 1; i++) {
      if ((separable(data2, start, i, data2.get(i + 1))) && (separable(data2, i + 1, n - 1, data2.get(i))))
      {
        DoubleVector subset = data2.subvector(start, i);
        d.plusEquals(fitForSingleCluster(subset, method).timesEquals(i - start + 1));
        
        start = i + 1;
      }
    }
    DoubleVector subset = data2.subvector(start, n - 1);
    d.plusEquals(fitForSingleCluster(subset, method).timesEquals(n - start));
    
    d.sort();
    d.normalize();
    mixingDistribution = d;
  }
  










  public DiscreteFunction fitForSingleCluster(DoubleVector data, int method)
  {
    if (data.size() < 2) return new DiscreteFunction(data);
    DoubleVector sp = supportPoints(data, 0);
    PaceMatrix fi = fittingIntervals(data);
    PaceMatrix pm = probabilityMatrix(sp, fi);
    PaceMatrix epm = new PaceMatrix(empiricalProbability(data, fi).timesEquals(1.0D / data.size()));
    


    IntVector pvt = IntVector.seq(0, sp.size() - 1);
    
    DoubleVector weights;
    switch (method) {
    case 1: 
      weights = pm.nnls(epm, pvt);
      break;
    case 2: 
      weights = pm.nnlse1(epm, pvt);
      break;
    default: 
      throw new IllegalArgumentException("unknown method");
    }
    
    DoubleVector sp2 = new DoubleVector(pvt.size());
    for (int i = 0; i < sp2.size(); i++) {
      sp2.set(i, sp.get(pvt.get(i)));
    }
    
    DiscreteFunction d = new DiscreteFunction(sp2, weights);
    d.sort();
    d.normalize();
    return d;
  }
  









  public abstract boolean separable(DoubleVector paramDoubleVector, int paramInt1, int paramInt2, double paramDouble);
  









  public abstract DoubleVector supportPoints(DoubleVector paramDoubleVector, int paramInt);
  









  public abstract PaceMatrix fittingIntervals(DoubleVector paramDoubleVector);
  








  public abstract PaceMatrix probabilityMatrix(DoubleVector paramDoubleVector, PaceMatrix paramPaceMatrix);
  








  public PaceMatrix empiricalProbability(DoubleVector data, PaceMatrix intervals)
  {
    int n = data.size();
    int k = intervals.getRowDimension();
    PaceMatrix epm = new PaceMatrix(k, 1, 0.0D);
    

    for (int j = 0; j < n; j++) {
      for (int i = 0; i < k; i++) {
        double point = 0.0D;
        if ((intervals.get(i, 0) == data.get(j)) || (intervals.get(i, 1) == data.get(j))) {
          point = 0.5D;
        } else if ((intervals.get(i, 0) < data.get(j)) && (intervals.get(i, 1) > data.get(j)))
          point = 1.0D;
        epm.setPlus(i, 0, point);
      }
    }
    return epm;
  }
  





  public String toString()
  {
    return "The mixing distribution:\n" + mixingDistribution.toString();
  }
}
