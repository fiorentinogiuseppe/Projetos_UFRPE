package weka.estimators;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
































public class EstimatorUtils
  implements RevisionHandler
{
  public EstimatorUtils() {}
  
  public static double findMinDistance(Instances inst, int attrIndex)
  {
    double min = Double.MAX_VALUE;
    int numInst = inst.numInstances();
    
    if (numInst < 2) return min;
    int begin = -1;
    Instance instance = null;
    do {
      begin++;
      if (begin < numInst)
        instance = inst.instance(begin);
    } while ((begin < numInst) && (instance.isMissing(attrIndex)));
    
    double secondValue = inst.instance(begin).value(attrIndex);
    for (int i = begin; (i < numInst) && (!inst.instance(i).isMissing(attrIndex)); i++) {
      double firstValue = secondValue;
      secondValue = inst.instance(i).value(attrIndex);
      if (secondValue != firstValue) {
        double diff = secondValue - firstValue;
        if ((diff < min) && (diff > 0.0D)) {
          min = diff;
        }
      }
    }
    return min;
  }
  








  public static int getMinMax(Instances inst, int attrIndex, double[] minMax)
    throws Exception
  {
    double min = NaN.0D;
    double max = NaN.0D;
    Instance instance = null;
    int numNotMissing = 0;
    if ((minMax == null) || (minMax.length < 2)) {
      throw new Exception("Error in Program, privat method getMinMax");
    }
    
    Enumeration enumInst = inst.enumerateInstances();
    if (enumInst.hasMoreElements()) {
      do {
        instance = (Instance)enumInst.nextElement();
      } while ((instance.isMissing(attrIndex)) && (enumInst.hasMoreElements()));
      

      if (!instance.isMissing(attrIndex)) {
        numNotMissing++;
        min = instance.value(attrIndex);
        max = instance.value(attrIndex);
      }
      while (enumInst.hasMoreElements()) {
        instance = (Instance)enumInst.nextElement();
        if (!instance.isMissing(attrIndex)) {
          numNotMissing++;
          if (instance.value(attrIndex) < min) {
            min = instance.value(attrIndex);
          }
          else if (instance.value(attrIndex) > max) {
            max = instance.value(attrIndex);
          }
        }
      }
    }
    
    minMax[0] = min;
    minMax[1] = max;
    return numNotMissing;
  }
  











  public static Vector getInstancesFromClass(Instances data, int attrIndex, int classIndex, double classValue, Instances workData)
  {
    Vector dataPlusInfo = new Vector(0);
    int num = 0;
    int numClassValue = 0;
    
    for (int i = 0; i < data.numInstances(); i++) {
      if (!data.instance(i).isMissing(attrIndex)) {
        num++;
        if (data.instance(i).value(classIndex) == classValue) {
          workData.add(data.instance(i));
          numClassValue++;
        }
      }
    }
    
    Double alphaFactor = new Double(numClassValue / num);
    dataPlusInfo.add(workData);
    dataPlusInfo.add(alphaFactor);
    return dataPlusInfo;
  }
  








  public static Instances getInstancesFromClass(Instances data, int classIndex, double classValue)
  {
    Instances workData = new Instances(data, 0);
    for (int i = 0; i < data.numInstances(); i++) {
      if (data.instance(i).value(classIndex) == classValue) {
        workData.add(data.instance(i));
      }
    }
    
    return workData;
  }
  














  public static void writeCurve(String f, Estimator est, double min, double max, int numPoints)
    throws Exception
  {
    PrintWriter output = null;
    StringBuffer text = new StringBuffer("");
    
    if (f.length() != 0)
    {
      String name = f + ".curv";
      output = new PrintWriter(new FileOutputStream(name));
    } else {
      return;
    }
    
    double diff = (max - min) / (numPoints - 1.0D);
    try {
      text.append("" + min + " " + est.getProbability(min) + " \n");
      
      for (double value = min + diff; value < max; value += diff) {
        text.append("" + value + " " + est.getProbability(value) + " \n");
      }
      text.append("" + max + " " + est.getProbability(max) + " \n");
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
    output.println(text.toString());
    

    if (output != null) {
      output.close();
    }
  }
  
















  public static void writeCurve(String f, Estimator est, Estimator classEst, double classIndex, double min, double max, int numPoints)
    throws Exception
  {
    PrintWriter output = null;
    StringBuffer text = new StringBuffer("");
    
    if (f.length() != 0)
    {
      String name = f + ".curv";
      output = new PrintWriter(new FileOutputStream(name));
    } else {
      return;
    }
    
    double diff = (max - min) / (numPoints - 1.0D);
    try {
      text.append("" + min + " " + est.getProbability(min) * classEst.getProbability(classIndex) + " \n");
      


      for (double value = min + diff; value < max; value += diff) {
        text.append("" + value + " " + est.getProbability(value) * classEst.getProbability(classIndex) + " \n");
      }
      

      text.append("" + max + " " + est.getProbability(max) * classEst.getProbability(classIndex) + " \n");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
    output.println(text.toString());
    

    if (output != null) {
      output.close();
    }
  }
  









  public static Instances getInstancesFromValue(Instances data, int index, double v)
  {
    Instances workData = new Instances(data, 0);
    for (int i = 0; i < data.numInstances(); i++) {
      if (data.instance(i).value(index) == v) {
        workData.add(data.instance(i));
      }
    }
    return workData;
  }
  



  public static String cutpointsToString(double[] cutPoints, boolean[] cutAndLeft)
  {
    StringBuffer text = new StringBuffer("");
    if (cutPoints == null) {
      text.append("\n# no cutpoints found - attribute \n");
    } else {
      text.append("\n#* " + cutPoints.length + " cutpoint(s) -\n");
      for (int i = 0; i < cutPoints.length; i++) {
        text.append("# " + cutPoints[i] + " ");
        text.append("" + cutAndLeft[i] + "\n");
      }
      text.append("# end\n");
    }
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
