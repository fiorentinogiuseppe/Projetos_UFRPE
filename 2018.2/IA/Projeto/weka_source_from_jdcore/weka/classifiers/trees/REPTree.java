package weka.classifiers.trees;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.classifiers.rules.ZeroR;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
































































public class REPTree
  extends Classifier
  implements OptionHandler, WeightedInstancesHandler, Drawable, AdditionalMeasureProducer, Sourcable, Randomizable
{
  static final long serialVersionUID = -9216785998198681299L;
  protected ZeroR m_zeroR;
  
  public REPTree() {}
  
  public String globalInfo()
  {
    return "Fast decision tree learner. Builds a decision/regression tree using information gain/variance and prunes it using reduced-error pruning (with backfitting).  Only sorts values for numeric attributes once. Missing values are dealt with by splitting the corresponding instances into pieces (i.e. as in C4.5).";
  }
  




  protected class Tree
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -1635481717888437935L;
    



    protected Instances m_Info = null;
    

    protected Tree[] m_Successors;
    

    protected int m_Attribute = -1;
    

    protected double m_SplitPoint = NaN.0D;
    

    protected double[] m_Prop = null;
    


    protected double[] m_ClassProbs = null;
    



    protected double[] m_Distribution = null;
    



    protected double[] m_HoldOutDist = null;
    



    protected double m_HoldOutError = 0.0D;
    



    protected Tree() {}
    


    protected double[] distributionForInstance(Instance instance)
      throws Exception
    {
      double[] returnedDist = null;
      
      if (m_Attribute > -1)
      {

        if (instance.isMissing(m_Attribute))
        {

          returnedDist = new double[m_Info.numClasses()];
          

          for (int i = 0; i < m_Successors.length; i++) {
            double[] help = m_Successors[i].distributionForInstance(instance);
            
            if (help != null) {
              for (int j = 0; j < help.length; j++) {
                returnedDist[j] += m_Prop[i] * help[j];
              }
            }
          }
        } else if (m_Info.attribute(m_Attribute).isNominal())
        {

          returnedDist = m_Successors[((int)instance.value(m_Attribute))].distributionForInstance(instance);



        }
        else if (instance.value(m_Attribute) < m_SplitPoint) {
          returnedDist = m_Successors[0].distributionForInstance(instance);
        }
        else {
          returnedDist = m_Successors[1].distributionForInstance(instance);
        }
      }
      

      if ((m_Attribute == -1) || (returnedDist == null))
      {

        if (m_ClassProbs == null) {
          return m_ClassProbs;
        }
        return (double[])m_ClassProbs.clone();
      }
      return returnedDist;
    }
    












    public final String sourceExpression(int index)
    {
      StringBuffer expr = null;
      if (index < 0) {
        return "i[" + m_Attribute + "] == null";
      }
      if (m_Info.attribute(m_Attribute).isNominal()) {
        expr = new StringBuffer("i[");
        expr.append(m_Attribute).append("]");
        expr.append(".equals(\"").append(m_Info.attribute(m_Attribute).value(index)).append("\")");
      }
      else {
        expr = new StringBuffer("");
        if (index == 0) {
          expr.append("((Double)i[").append(m_Attribute).append("]).doubleValue() < ").append(m_SplitPoint);
        }
        else
        {
          expr.append("true");
        }
      }
      return expr.toString();
    }
    


















    public StringBuffer[] toSource(String className, Tree parent)
      throws Exception
    {
      StringBuffer[] result = new StringBuffer[2];
      double[] currentProbs;
      double[] currentProbs;
      if (m_ClassProbs == null) {
        currentProbs = m_ClassProbs;
      } else {
        currentProbs = m_ClassProbs;
      }
      long printID = REPTree.nextID();
      

      if (m_Attribute == -1) {
        result[0] = new StringBuffer("\tp = ");
        if (m_Info.classAttribute().isNumeric()) {
          result[0].append(currentProbs[0]);
        } else {
          result[0].append(Utils.maxIndex(currentProbs));
        }
        result[0].append(";\n");
        result[1] = new StringBuffer("");
      } else {
        StringBuffer text = new StringBuffer("");
        StringBuffer atEnd = new StringBuffer("");
        
        text.append("  static double N").append(Integer.toHexString(hashCode()) + printID).append("(Object []i) {\n").append("    double p = Double.NaN;\n");
        



        text.append("    /* " + m_Info.attribute(m_Attribute).name() + " */\n");
        
        text.append("    if (" + sourceExpression(-1) + ") {\n").append("      p = ");
        
        if (m_Info.classAttribute().isNumeric()) {
          text.append(currentProbs[0] + ";\n");
        } else
          text.append(Utils.maxIndex(currentProbs) + ";\n");
        text.append("    } ");
        

        for (int i = 0; i < m_Successors.length; i++) {
          text.append("else if (" + sourceExpression(i) + ") {\n");
          
          if (m_Successors[i].m_Attribute == -1) {
            double[] successorProbs = m_Successors[i].m_ClassProbs;
            if (successorProbs == null)
              successorProbs = m_ClassProbs;
            text.append("      p = ");
            if (m_Info.classAttribute().isNumeric()) {
              text.append(successorProbs[0] + ";\n");
            } else {
              text.append(Utils.maxIndex(successorProbs) + ";\n");
            }
          } else {
            StringBuffer[] sub = m_Successors[i].toSource(className, this);
            text.append("" + sub[0]);
            atEnd.append("" + sub[1]);
          }
          text.append("    } ");
          if (i == m_Successors.length - 1) {
            text.append("\n");
          }
        }
        
        text.append("    return p;\n  }\n");
        
        result[0] = new StringBuffer("    p = " + className + ".N");
        result[0].append(Integer.toHexString(hashCode()) + printID).append("(i);\n");
        
        result[1] = text.append("" + atEnd);
      }
      return result;
    }
    





    protected int toGraph(StringBuffer text, int num, Tree parent)
      throws Exception
    {
      
      




      if (m_Attribute == -1) {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + Utils.backQuoteChars(leafString(parent)) + "\"" + "shape=box]\n");
      }
      else
      {
        text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" + num + ": " + Utils.backQuoteChars(m_Info.attribute(m_Attribute).name()) + "\"]\n");
        


        for (int i = 0; i < m_Successors.length; i++) {
          text.append("N" + Integer.toHexString(hashCode()) + "->" + "N" + Integer.toHexString(m_Successors[i].hashCode()) + " [label=\"");
          



          if (m_Info.attribute(m_Attribute).isNumeric()) {
            if (i == 0) {
              text.append(" < " + Utils.doubleToString(m_SplitPoint, 2));
            }
            else {
              text.append(" >= " + Utils.doubleToString(m_SplitPoint, 2));
            }
          }
          else {
            text.append(" = " + Utils.backQuoteChars(m_Info.attribute(m_Attribute).value(i)));
          }
          text.append("\"]\n");
          num = m_Successors[i].toGraph(text, num, this);
        }
      }
      
      return num;
    }
    






    protected String leafString(Tree parent)
      throws Exception
    {
      if (m_Info.classAttribute().isNumeric()) { double classMean;
        double classMean;
        if (m_ClassProbs == null) {
          classMean = m_ClassProbs[0];
        } else {
          classMean = m_ClassProbs[0];
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(" : " + Utils.doubleToString(classMean, 2));
        double avgError = 0.0D;
        if (m_Distribution[1] > 0.0D) {
          avgError = m_Distribution[0] / m_Distribution[1];
        }
        buffer.append(" (" + Utils.doubleToString(m_Distribution[1], 2) + "/" + Utils.doubleToString(avgError, 2) + ")");
        


        avgError = 0.0D;
        if (m_HoldOutDist[0] > 0.0D) {
          avgError = m_HoldOutError / m_HoldOutDist[0];
        }
        buffer.append(" [" + Utils.doubleToString(m_HoldOutDist[0], 2) + "/" + Utils.doubleToString(avgError, 2) + "]");
        


        return buffer.toString(); }
      int maxIndex;
      int maxIndex;
      if (m_ClassProbs == null) {
        maxIndex = Utils.maxIndex(m_ClassProbs);
      } else {
        maxIndex = Utils.maxIndex(m_ClassProbs);
      }
      return " : " + m_Info.classAttribute().value(maxIndex) + " (" + Utils.doubleToString(Utils.sum(m_Distribution), 2) + "/" + Utils.doubleToString(Utils.sum(m_Distribution) - m_Distribution[maxIndex], 2) + ")" + " [" + Utils.doubleToString(Utils.sum(m_HoldOutDist), 2) + "/" + Utils.doubleToString(Utils.sum(m_HoldOutDist) - m_HoldOutDist[maxIndex], 2) + "]";
    }
    














    protected String toString(int level, Tree parent)
    {
      try
      {
        StringBuffer text = new StringBuffer();
        
        if (m_Attribute == -1)
        {

          return leafString(parent); }
        if (m_Info.attribute(m_Attribute).isNominal())
        {

          for (int i = 0; i < m_Successors.length; i++) {
            text.append("\n");
            for (int j = 0; j < level; j++) {
              text.append("|   ");
            }
            text.append(m_Info.attribute(m_Attribute).name() + " = " + m_Info.attribute(m_Attribute).value(i));
            
            text.append(m_Successors[i].toString(level + 1, this));
          }
        }
        else
        {
          text.append("\n");
          for (int j = 0; j < level; j++) {
            text.append("|   ");
          }
          text.append(m_Info.attribute(m_Attribute).name() + " < " + Utils.doubleToString(m_SplitPoint, 2));
          
          text.append(m_Successors[0].toString(level + 1, this));
          text.append("\n");
          for (int j = 0; j < level; j++) {
            text.append("|   ");
          }
          text.append(m_Info.attribute(m_Attribute).name() + " >= " + Utils.doubleToString(m_SplitPoint, 2));
          
          text.append(m_Successors[1].toString(level + 1, this));
        }
        
        return text.toString();
      } catch (Exception e) {
        e.printStackTrace(); }
      return "Decision tree: tree can't be printed";
    }
    






















    protected void buildTree(int[][][] sortedIndices, double[][][] weights, Instances data, double totalWeight, double[] classProbs, Instances header, double minNum, double minVariance, int depth, int maxDepth)
      throws Exception
    {
      m_Info = header;
      if (data.classAttribute().isNumeric()) {
        m_HoldOutDist = new double[2];
      } else {
        m_HoldOutDist = new double[data.numClasses()];
      }
      

      int helpIndex = 0;
      if (data.classIndex() == 0) {
        helpIndex = 1;
      }
      if (sortedIndices[0][helpIndex].length == 0) {
        if (data.classAttribute().isNumeric()) {
          m_Distribution = new double[2];
        } else {
          m_Distribution = new double[data.numClasses()];
        }
        m_ClassProbs = null;
        sortedIndices[0] = ((int[][])null);
        weights[0] = ((double[][])null);
        return;
      }
      
      double priorVar = 0.0D;
      if (data.classAttribute().isNumeric())
      {

        double totalSum = 0.0D;double totalSumSquared = 0.0D;double totalSumOfWeights = 0.0D;
        for (int i = 0; i < sortedIndices[0][helpIndex].length; i++) {
          Instance inst = data.instance(sortedIndices[0][helpIndex][i]);
          totalSum += inst.classValue() * weights[0][helpIndex][i];
          totalSumSquared += inst.classValue() * inst.classValue() * weights[0][helpIndex][i];
          
          totalSumOfWeights += weights[0][helpIndex][i];
        }
        priorVar = singleVariance(totalSum, totalSumSquared, totalSumOfWeights);
      }
      



      m_ClassProbs = new double[classProbs.length];
      System.arraycopy(classProbs, 0, m_ClassProbs, 0, classProbs.length);
      if ((totalWeight < 2.0D * minNum) || ((data.classAttribute().isNominal()) && (Utils.eq(m_ClassProbs[Utils.maxIndex(m_ClassProbs)], Utils.sum(m_ClassProbs)))) || ((data.classAttribute().isNumeric()) && (priorVar / totalWeight < minVariance)) || ((m_MaxDepth >= 0) && (depth >= maxDepth)))
      {













        m_Attribute = -1;
        if (data.classAttribute().isNominal())
        {

          m_Distribution = new double[m_ClassProbs.length];
          for (int i = 0; i < m_ClassProbs.length; i++) {
            m_Distribution[i] = m_ClassProbs[i];
          }
          Utils.normalize(m_ClassProbs);
        }
        else
        {
          m_Distribution = new double[2];
          m_Distribution[0] = priorVar;
          m_Distribution[1] = totalWeight;
        }
        sortedIndices[0] = ((int[][])null);
        weights[0] = ((double[][])null);
        return;
      }
      


      double[] vals = new double[data.numAttributes()];
      double[][][] dists = new double[data.numAttributes()][0][0];
      double[][] props = new double[data.numAttributes()][0];
      double[][] totalSubsetWeights = new double[data.numAttributes()][0];
      double[] splits = new double[data.numAttributes()];
      if (data.classAttribute().isNominal())
      {

        for (int i = 0; i < data.numAttributes(); i++) {
          if (i != data.classIndex()) {
            splits[i] = distribution(props, dists, i, sortedIndices[0][i], weights[0][i], totalSubsetWeights, data);
            
            vals[i] = gain(dists[i], priorVal(dists[i]));
          }
          
        }
        
      } else {
        for (int i = 0; i < data.numAttributes(); i++) {
          if (i != data.classIndex()) {
            splits[i] = numericDistribution(props, dists, i, sortedIndices[0][i], weights[0][i], totalSubsetWeights, data, vals);
          }
        }
      }
      




      m_Attribute = Utils.maxIndex(vals);
      int numAttVals = dists[m_Attribute].length;
      


      int count = 0;
      for (int i = 0; i < numAttVals; i++) {
        if (totalSubsetWeights[m_Attribute][i] >= minNum) {
          count++;
        }
        if (count > 1) {
          break;
        }
      }
      

      if ((Utils.gr(vals[m_Attribute], 0.0D)) && (count > 1))
      {

        m_SplitPoint = splits[m_Attribute];
        m_Prop = props[m_Attribute];
        double[][] attSubsetDists = dists[m_Attribute];
        double[] attTotalSubsetWeights = totalSubsetWeights[m_Attribute];
        

        vals = null;
        dists = (double[][][])null;
        props = (double[][])null;
        totalSubsetWeights = (double[][])null;
        splits = null;
        

        int[][][][] subsetIndices = new int[numAttVals][1][data.numAttributes()][0];
        
        double[][][][] subsetWeights = new double[numAttVals][1][data.numAttributes()][0];
        
        splitData(subsetIndices, subsetWeights, m_Attribute, m_SplitPoint, sortedIndices[0], weights[0], data);
        


        sortedIndices[0] = ((int[][])null);
        weights[0] = ((double[][])null);
        

        m_Successors = new Tree[numAttVals];
        for (int i = 0; i < numAttVals; i++) {
          m_Successors[i] = new Tree(REPTree.this);
          m_Successors[i].buildTree(subsetIndices[i], subsetWeights[i], data, attTotalSubsetWeights[i], attSubsetDists[i], header, minNum, minVariance, depth + 1, maxDepth);
          





          attSubsetDists[i] = null;
        }
      }
      else
      {
        m_Attribute = -1;
        sortedIndices[0] = ((int[][])null);
        weights[0] = ((double[][])null);
      }
      

      if (data.classAttribute().isNominal()) {
        m_Distribution = new double[m_ClassProbs.length];
        for (int i = 0; i < m_ClassProbs.length; i++) {
          m_Distribution[i] = m_ClassProbs[i];
        }
        Utils.normalize(m_ClassProbs);
      } else {
        m_Distribution = new double[2];
        m_Distribution[0] = priorVar;
        m_Distribution[1] = totalWeight;
      }
    }
    





    protected int numNodes()
    {
      if (m_Attribute == -1) {
        return 1;
      }
      int size = 1;
      for (int i = 0; i < m_Successors.length; i++) {
        size += m_Successors[i].numNodes();
      }
      return size;
    }
    




















    protected void splitData(int[][][][] subsetIndices, double[][][][] subsetWeights, int att, double splitPoint, int[][] sortedIndices, double[][] weights, Instances data)
      throws Exception
    {
      for (int i = 0; i < data.numAttributes(); i++) {
        if (i != data.classIndex()) {
          if (data.attribute(att).isNominal())
          {

            int[] num = new int[data.attribute(att).numValues()];
            for (int k = 0; k < num.length; k++) {
              subsetIndices[k][0][i] = new int[sortedIndices[i].length];
              subsetWeights[k][0][i] = new double[sortedIndices[i].length];
            }
            for (int j = 0; j < sortedIndices[i].length; j++) {
              Instance inst = data.instance(sortedIndices[i][j]);
              if (inst.isMissing(att))
              {

                for (int k = 0; k < num.length; k++) {
                  if (m_Prop[k] > 0.0D) {
                    subsetIndices[k][0][i][num[k]] = sortedIndices[i][j];
                    subsetWeights[k][0][i][num[k]] = (m_Prop[k] * weights[i][j]);
                    
                    num[k] += 1;
                  }
                }
              } else {
                int subset = (int)inst.value(att);
                subsetIndices[subset][0][i][num[subset]] = sortedIndices[i][j];
                
                subsetWeights[subset][0][i][num[subset]] = weights[i][j];
                num[subset] += 1;
              }
            }
          }
          

          int[] num = new int[2];
          for (int k = 0; k < 2; k++) {
            subsetIndices[k][0][i] = new int[sortedIndices[i].length];
            subsetWeights[k][0][i] = new double[weights[i].length];
          }
          for (int j = 0; j < sortedIndices[i].length; j++) {
            Instance inst = data.instance(sortedIndices[i][j]);
            if (inst.isMissing(att))
            {

              for (int k = 0; k < num.length; k++) {
                if (m_Prop[k] > 0.0D) {
                  subsetIndices[k][0][i][num[k]] = sortedIndices[i][j];
                  subsetWeights[k][0][i][num[k]] = (m_Prop[k] * weights[i][j]);
                  
                  num[k] += 1;
                }
              }
            } else {
              int subset = inst.value(att) < splitPoint ? 0 : 1;
              subsetIndices[subset][0][i][num[subset]] = sortedIndices[i][j];
              
              subsetWeights[subset][0][i][num[subset]] = weights[i][j];
              num[subset] += 1;
            }
          }
          


          for (int k = 0; k < num.length; k++) {
            int[] copy = new int[num[k]];
            System.arraycopy(subsetIndices[k][0][i], 0, copy, 0, num[k]);
            subsetIndices[k][0][i] = copy;
            double[] copyWeights = new double[num[k]];
            System.arraycopy(subsetWeights[k][0][i], 0, copyWeights, 0, num[k]);
            
            subsetWeights[k][0][i] = copyWeights;
          }
        }
      }
    }
    


















    protected double distribution(double[][] props, double[][][] dists, int att, int[] sortedIndices, double[] weights, double[][] subsetWeights, Instances data)
      throws Exception
    {
      double splitPoint = NaN.0D;
      Attribute attribute = data.attribute(att);
      double[][] dist = (double[][])null;
      

      if (attribute.isNominal())
      {

        dist = new double[attribute.numValues()][data.numClasses()];
        for (int i = 0; i < sortedIndices.length; i++) {
          Instance inst = data.instance(sortedIndices[i]);
          if (inst.isMissing(att)) {
            break;
          }
          dist[((int)inst.value(att))][((int)inst.classValue())] += weights[i];
        }
      }
      

      double[][] currDist = new double[2][data.numClasses()];
      dist = new double[2][data.numClasses()];
      

      for (int j = 0; j < sortedIndices.length; j++) {
        Instance inst = data.instance(sortedIndices[j]);
        if (inst.isMissing(att)) {
          break;
        }
        currDist[1][((int)inst.classValue())] += weights[j];
      }
      double priorVal = priorVal(currDist);
      System.arraycopy(currDist[1], 0, dist[1], 0, dist[1].length);
      

      double currSplit = data.instance(sortedIndices[0]).value(att);
      double bestVal = -1.7976931348623157E308D;
      for (int i = 0; i < sortedIndices.length; i++) {
        Instance inst = data.instance(sortedIndices[i]);
        if (inst.isMissing(att)) {
          break;
        }
        if (inst.value(att) > currSplit) {
          double currVal = gain(currDist, priorVal);
          if (currVal > bestVal) {
            bestVal = currVal;
            splitPoint = (inst.value(att) + currSplit) / 2.0D;
            

            if (splitPoint <= currSplit) {
              splitPoint = inst.value(att);
            }
            
            for (int j = 0; j < currDist.length; j++) {
              System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
            }
          }
        }
        
        currSplit = inst.value(att);
        currDist[0][((int)inst.classValue())] += weights[i];
        currDist[1][((int)inst.classValue())] -= weights[i];
      }
      


      props[att] = new double[dist.length];
      for (int k = 0; k < props[att].length; k++) {
        props[att][k] = Utils.sum(dist[k]);
      }
      if (Utils.sum(props[att]) <= 0.0D) {
        for (int k = 0; k < props[att].length; k++) {
          props[att][k] = (1.0D / props[att].length);
        }
      } else {
        Utils.normalize(props[att]);
      }
      

      while (i < sortedIndices.length) {
        Instance inst = data.instance(sortedIndices[i]);
        for (int j = 0; j < dist.length; j++) {
          dist[j][((int)inst.classValue())] += props[att][j] * weights[i];
        }
        i++;
      }
      

      subsetWeights[att] = new double[dist.length];
      for (int j = 0; j < dist.length; j++) {
        subsetWeights[att][j] += Utils.sum(dist[j]);
      }
      

      dists[att] = dist;
      return splitPoint;
    }
    




















    protected double numericDistribution(double[][] props, double[][][] dists, int att, int[] sortedIndices, double[] weights, double[][] subsetWeights, Instances data, double[] vals)
      throws Exception
    {
      double splitPoint = NaN.0D;
      Attribute attribute = data.attribute(att);
      double[][] dist = (double[][])null;
      double[] sums = null;
      double[] sumSquared = null;
      double[] sumOfWeights = null;
      double totalSum = 0.0D;double totalSumSquared = 0.0D;double totalSumOfWeights = 0.0D;
      
      int i;
      
      if (attribute.isNominal())
      {

        sums = new double[attribute.numValues()];
        sumSquared = new double[attribute.numValues()];
        sumOfWeights = new double[attribute.numValues()];
        
        for (int i = 0; i < sortedIndices.length; i++) {
          Instance inst = data.instance(sortedIndices[i]);
          if (inst.isMissing(att)) {
            break;
          }
          int attVal = (int)inst.value(att);
          sums[attVal] += inst.classValue() * weights[i];
          sumSquared[attVal] += inst.classValue() * inst.classValue() * weights[i];
          
          sumOfWeights[attVal] += weights[i];
        }
        totalSum = Utils.sum(sums);
        totalSumSquared = Utils.sum(sumSquared);
        totalSumOfWeights = Utils.sum(sumOfWeights);
      }
      else
      {
        sums = new double[2];
        sumSquared = new double[2];
        sumOfWeights = new double[2];
        double[] currSums = new double[2];
        double[] currSumSquared = new double[2];
        double[] currSumOfWeights = new double[2];
        

        for (int j = 0; j < sortedIndices.length; j++) {
          Instance inst = data.instance(sortedIndices[j]);
          if (inst.isMissing(att)) {
            break;
          }
          currSums[1] += inst.classValue() * weights[j];
          currSumSquared[1] += inst.classValue() * inst.classValue() * weights[j];
          
          currSumOfWeights[1] += weights[j];
        }
        
        totalSum = currSums[1];
        totalSumSquared = currSumSquared[1];
        totalSumOfWeights = currSumOfWeights[1];
        
        sums[1] = currSums[1];
        sumSquared[1] = currSumSquared[1];
        sumOfWeights[1] = currSumOfWeights[1];
        

        double currSplit = data.instance(sortedIndices[0]).value(att);
        double bestVal = Double.MAX_VALUE;
        for (i = 0; i < sortedIndices.length; i++) {
          Instance inst = data.instance(sortedIndices[i]);
          if (inst.isMissing(att)) {
            break;
          }
          if (inst.value(att) > currSplit) {
            double currVal = variance(currSums, currSumSquared, currSumOfWeights);
            if (currVal < bestVal) {
              bestVal = currVal;
              splitPoint = (inst.value(att) + currSplit) / 2.0D;
              

              if (splitPoint <= currSplit) {
                splitPoint = inst.value(att);
              }
              
              for (int j = 0; j < 2; j++) {
                sums[j] = currSums[j];
                sumSquared[j] = currSumSquared[j];
                sumOfWeights[j] = currSumOfWeights[j];
              }
            }
          }
          
          currSplit = inst.value(att);
          
          double classVal = inst.classValue() * weights[i];
          double classValSquared = inst.classValue() * classVal;
          
          currSums[0] += classVal;
          currSumSquared[0] += classValSquared;
          currSumOfWeights[0] += weights[i];
          
          currSums[1] -= classVal;
          currSumSquared[1] -= classValSquared;
          currSumOfWeights[1] -= weights[i];
        }
      }
      

      props[att] = new double[sums.length];
      for (int k = 0; k < props[att].length; k++) {
        props[att][k] = sumOfWeights[k];
      }
      if (Utils.sum(props[att]) <= 0.0D) {
        for (int k = 0; k < props[att].length; k++) {
          props[att][k] = (1.0D / props[att].length);
        }
      } else {
        Utils.normalize(props[att]);
      }
      


      while (i < sortedIndices.length) {
        Instance inst = data.instance(sortedIndices[i]);
        for (int j = 0; j < sums.length; j++) {
          sums[j] += props[att][j] * inst.classValue() * weights[i];
          sumSquared[j] += props[att][j] * inst.classValue() * inst.classValue() * weights[i];
          
          sumOfWeights[j] += props[att][j] * weights[i];
        }
        totalSum += inst.classValue() * weights[i];
        totalSumSquared += inst.classValue() * inst.classValue() * weights[i];
        
        totalSumOfWeights += weights[i];
        i++;
      }
      

      dist = new double[sums.length][data.numClasses()];
      for (int j = 0; j < sums.length; j++) {
        if (sumOfWeights[j] > 0.0D) {
          dist[j][0] = (sums[j] / sumOfWeights[j]);
        } else {
          dist[j][0] = (totalSum / totalSumOfWeights);
        }
      }
      

      double priorVar = singleVariance(totalSum, totalSumSquared, totalSumOfWeights);
      
      double var = variance(sums, sumSquared, sumOfWeights);
      double gain = priorVar - var;
      

      subsetWeights[att] = sumOfWeights;
      dists[att] = dist;
      vals[att] = gain;
      return splitPoint;
    }
    









    protected double variance(double[] s, double[] sS, double[] sumOfWeights)
    {
      double var = 0.0D;
      
      for (int i = 0; i < s.length; i++) {
        if (sumOfWeights[i] > 0.0D) {
          var += singleVariance(s[i], sS[i], sumOfWeights[i]);
        }
      }
      
      return var;
    }
    








    protected double singleVariance(double s, double sS, double weight)
    {
      return sS - s * s / weight;
    }
    






    protected double priorVal(double[][] dist)
    {
      return ContingencyTables.entropyOverColumns(dist);
    }
    







    protected double gain(double[][] dist, double priorVal)
    {
      return priorVal - ContingencyTables.entropyConditionedOnRows(dist);
    }
    






    protected double reducedErrorPrune()
      throws Exception
    {
      if (m_Attribute == -1) {
        return m_HoldOutError;
      }
      

      double errorTree = 0.0D;
      for (int i = 0; i < m_Successors.length; i++) {
        errorTree += m_Successors[i].reducedErrorPrune();
      }
      

      if (errorTree >= m_HoldOutError) {
        m_Attribute = -1;
        m_Successors = null;
        return m_HoldOutError;
      }
      return errorTree;
    }
    






    protected void insertHoldOutSet(Instances data)
      throws Exception
    {
      for (int i = 0; i < data.numInstances(); i++) {
        insertHoldOutInstance(data.instance(i), data.instance(i).weight(), this);
      }
    }
    










    protected void insertHoldOutInstance(Instance inst, double weight, Tree parent)
      throws Exception
    {
      if (inst.classAttribute().isNominal())
      {

        m_HoldOutDist[((int)inst.classValue())] += weight;
        int predictedClass = 0;
        if (m_ClassProbs == null) {
          predictedClass = Utils.maxIndex(m_ClassProbs);
        } else {
          predictedClass = Utils.maxIndex(m_ClassProbs);
        }
        if (predictedClass != (int)inst.classValue()) {
          m_HoldOutError += weight;
        }
      }
      else
      {
        m_HoldOutDist[0] += weight;
        m_HoldOutDist[1] += weight * inst.classValue();
        double diff = 0.0D;
        if (m_ClassProbs == null) {
          diff = m_ClassProbs[0] - inst.classValue();
        } else {
          diff = m_ClassProbs[0] - inst.classValue();
        }
        m_HoldOutError += diff * diff * weight;
      }
      

      if (m_Attribute != -1)
      {

        if (inst.isMissing(m_Attribute))
        {

          for (int i = 0; i < m_Successors.length; i++) {
            if (m_Prop[i] > 0.0D) {
              m_Successors[i].insertHoldOutInstance(inst, weight * m_Prop[i], this);
            }
            
          }
          
        }
        else if (m_Info.attribute(m_Attribute).isNominal())
        {

          m_Successors[((int)inst.value(m_Attribute))].insertHoldOutInstance(inst, weight, this);



        }
        else if (inst.value(m_Attribute) < m_SplitPoint) {
          m_Successors[0].insertHoldOutInstance(inst, weight, this);
        } else {
          m_Successors[1].insertHoldOutInstance(inst, weight, this);
        }
      }
    }
    







    protected void backfitHoldOutSet()
      throws Exception
    {
      if (m_Info.classAttribute().isNominal())
      {

        if (m_ClassProbs == null) {
          m_ClassProbs = new double[m_Info.numClasses()];
        }
        System.arraycopy(m_Distribution, 0, m_ClassProbs, 0, m_Info.numClasses());
        for (int i = 0; i < m_HoldOutDist.length; i++) {
          m_ClassProbs[i] += m_HoldOutDist[i];
        }
        if (Utils.sum(m_ClassProbs) > 0.0D) {
          Utils.normalize(m_ClassProbs);
        } else {
          m_ClassProbs = null;
        }
      }
      else
      {
        double sumOfWeightsTrainAndHoldout = m_Distribution[1] + m_HoldOutDist[0];
        if (sumOfWeightsTrainAndHoldout <= 0.0D) {
          return;
        }
        if (m_ClassProbs == null) {
          m_ClassProbs = new double[1];
        } else {
          m_ClassProbs[0] *= m_Distribution[1];
        }
        m_ClassProbs[0] += m_HoldOutDist[1];
        m_ClassProbs[0] /= sumOfWeightsTrainAndHoldout;
      }
      

      if (m_Attribute != -1) {
        for (int i = 0; i < m_Successors.length; i++) {
          m_Successors[i].backfitHoldOutSet();
        }
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 10275 $");
    }
  }
  

  protected Tree m_Tree = null;
  

  protected int m_NumFolds = 3;
  

  protected int m_Seed = 1;
  

  protected boolean m_NoPruning = false;
  

  protected double m_MinNum = 2.0D;
  


  protected double m_MinVarianceProp = 0.001D;
  

  protected int m_MaxDepth = -1;
  




  public String noPruningTipText()
  {
    return "Whether pruning is performed.";
  }
  





  public boolean getNoPruning()
  {
    return m_NoPruning;
  }
  





  public void setNoPruning(boolean newNoPruning)
  {
    m_NoPruning = newNoPruning;
  }
  




  public String minNumTipText()
  {
    return "The minimum total weight of the instances in a leaf.";
  }
  





  public double getMinNum()
  {
    return m_MinNum;
  }
  





  public void setMinNum(double newMinNum)
  {
    m_MinNum = newMinNum;
  }
  




  public String minVariancePropTipText()
  {
    return "The minimum proportion of the variance on all the data that needs to be present at a node in order for splitting to be performed in regression trees.";
  }
  







  public double getMinVarianceProp()
  {
    return m_MinVarianceProp;
  }
  





  public void setMinVarianceProp(double newMinVarianceProp)
  {
    m_MinVarianceProp = newMinVarianceProp;
  }
  




  public String seedTipText()
  {
    return "The seed used for randomizing the data.";
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setSeed(int newSeed)
  {
    m_Seed = newSeed;
  }
  




  public String numFoldsTipText()
  {
    return "Determines the amount of data used for pruning. One fold is used for pruning, the rest for growing the rules.";
  }
  






  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int newNumFolds)
  {
    m_NumFolds = newNumFolds;
  }
  




  public String maxDepthTipText()
  {
    return "The maximum tree depth (-1 for no restriction).";
  }
  





  public int getMaxDepth()
  {
    return m_MaxDepth;
  }
  





  public void setMaxDepth(int newMaxDepth)
  {
    m_MaxDepth = newMaxDepth;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(5);
    
    newVector.addElement(new Option("\tSet minimum number of instances per leaf (default 2).", "M", 1, "-M <minimum number of instances>"));
    


    newVector.addElement(new Option("\tSet minimum numeric class variance proportion\n\tof train variance for split (default 1e-3).", "V", 1, "-V <minimum variance for split>"));
    


    newVector.addElement(new Option("\tNumber of folds for reduced error pruning (default 3).", "N", 1, "-N <number of folds>"));
    


    newVector.addElement(new Option("\tSeed for random data shuffling (default 1).", "S", 1, "-S <seed>"));
    

    newVector.addElement(new Option("\tNo pruning.", "P", 0, "-P"));
    

    newVector.addElement(new Option("\tMaximum tree depth (default -1, no maximum)", "L", 1, "-L"));
    


    return newVector.elements();
  }
  





  public String[] getOptions()
  {
    String[] options = new String[12];
    int current = 0;
    options[(current++)] = "-M";
    options[(current++)] = ("" + (int)getMinNum());
    options[(current++)] = "-V";
    options[(current++)] = ("" + getMinVarianceProp());
    options[(current++)] = "-N";
    options[(current++)] = ("" + getNumFolds());
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSeed());
    options[(current++)] = "-L";
    options[(current++)] = ("" + getMaxDepth());
    if (getNoPruning()) {
      options[(current++)] = "-P";
    }
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





























  public void setOptions(String[] options)
    throws Exception
  {
    String minNumString = Utils.getOption('M', options);
    if (minNumString.length() != 0) {
      m_MinNum = Integer.parseInt(minNumString);
    } else {
      m_MinNum = 2.0D;
    }
    String minVarString = Utils.getOption('V', options);
    if (minVarString.length() != 0) {
      m_MinVarianceProp = Double.parseDouble(minVarString);
    } else {
      m_MinVarianceProp = 0.001D;
    }
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      m_NumFolds = Integer.parseInt(numFoldsString);
    } else {
      m_NumFolds = 3;
    }
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }
    m_NoPruning = Utils.getFlag('P', options);
    String depthString = Utils.getOption('L', options);
    if (depthString.length() != 0) {
      m_MaxDepth = Integer.parseInt(depthString);
    } else {
      m_MaxDepth = -1;
    }
    Utils.checkForRemainingOptions(options);
  }
  





  public int numNodes()
  {
    return m_Tree.numNodes();
  }
  





  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector(1);
    newVector.addElement("measureTreeSize");
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if (additionalMeasureName.equalsIgnoreCase("measureTreeSize")) {
      return numNodes();
    }
    throw new IllegalArgumentException(additionalMeasureName + " not supported (REPTree)");
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    Random random = new Random(m_Seed);
    
    m_zeroR = null;
    if (data.numAttributes() == 1) {
      m_zeroR = new ZeroR();
      m_zeroR.buildClassifier(data);
      return;
    }
    

    data.randomize(random);
    if (data.classAttribute().isNominal()) {
      data.stratify(m_NumFolds);
    }
    

    Instances train = null;
    Instances prune = null;
    if (!m_NoPruning) {
      train = data.trainCV(m_NumFolds, 0, random);
      prune = data.testCV(m_NumFolds, 0);
    } else {
      train = data;
    }
    

    int[][][] sortedIndices = new int[1][train.numAttributes()][0];
    double[][][] weights = new double[1][train.numAttributes()][0];
    double[] vals = new double[train.numInstances()];
    for (int j = 0; j < train.numAttributes(); j++) {
      if (j != train.classIndex()) {
        weights[0][j] = new double[train.numInstances()];
        if (train.attribute(j).isNominal())
        {


          sortedIndices[0][j] = new int[train.numInstances()];
          int count = 0;
          for (int i = 0; i < train.numInstances(); i++) {
            Instance inst = train.instance(i);
            if (!inst.isMissing(j)) {
              sortedIndices[0][j][count] = i;
              weights[0][j][count] = inst.weight();
              count++;
            }
          }
          for (int i = 0; i < train.numInstances(); i++) {
            Instance inst = train.instance(i);
            if (inst.isMissing(j)) {
              sortedIndices[0][j][count] = i;
              weights[0][j][count] = inst.weight();
              count++;
            }
          }
        }
        else
        {
          for (int i = 0; i < train.numInstances(); i++) {
            Instance inst = train.instance(i);
            vals[i] = inst.value(j);
          }
          sortedIndices[0][j] = Utils.sort(vals);
          for (int i = 0; i < train.numInstances(); i++) {
            weights[0][j][i] = train.instance(sortedIndices[0][j][i]).weight();
          }
        }
      }
    }
    

    double[] classProbs = new double[train.numClasses()];
    double totalWeight = 0.0D;double totalSumSquared = 0.0D;
    for (int i = 0; i < train.numInstances(); i++) {
      Instance inst = train.instance(i);
      if (data.classAttribute().isNominal()) {
        classProbs[((int)inst.classValue())] += inst.weight();
        totalWeight += inst.weight();
      } else {
        classProbs[0] += inst.classValue() * inst.weight();
        totalSumSquared += inst.classValue() * inst.classValue() * inst.weight();
        totalWeight += inst.weight();
      }
    }
    m_Tree = new Tree();
    double trainVariance = 0.0D;
    if (data.classAttribute().isNumeric()) {
      trainVariance = m_Tree.singleVariance(classProbs[0], totalSumSquared, totalWeight) / totalWeight;
      
      classProbs[0] /= totalWeight;
    }
    

    m_Tree.buildTree(sortedIndices, weights, train, totalWeight, classProbs, new Instances(train, 0), m_MinNum, m_MinVarianceProp * trainVariance, 0, m_MaxDepth);
    



    if (!m_NoPruning) {
      m_Tree.insertHoldOutSet(prune);
      m_Tree.reducedErrorPrune();
      m_Tree.backfitHoldOutSet();
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_zeroR != null) {
      return m_zeroR.distributionForInstance(instance);
    }
    return m_Tree.distributionForInstance(instance);
  }
  






  private static long PRINTED_NODES = 0L;
  





  protected static long nextID()
  {
    return PRINTED_NODES++;
  }
  


  protected static void resetID()
  {
    PRINTED_NODES = 0L;
  }
  







  public String toSource(String className)
    throws Exception
  {
    if (m_Tree == null) {
      throw new Exception("REPTree: No model built yet.");
    }
    StringBuffer[] source = m_Tree.toSource(className, m_Tree);
    return "class " + className + " {\n\n" + "  public static double classify(Object [] i)\n" + "    throws Exception {\n\n" + "    double p = Double.NaN;\n" + source[0] + "    return p;\n" + "  }\n" + source[1] + "}\n";
  }
  













  public int graphType()
  {
    return 1;
  }
  





  public String graph()
    throws Exception
  {
    if (m_Tree == null) {
      throw new Exception("REPTree: No model built yet.");
    }
    StringBuffer resultBuff = new StringBuffer();
    m_Tree.toGraph(resultBuff, 0, null);
    String result = "digraph Tree {\nedge [style=bold]\n" + resultBuff.toString() + "\n}\n";
    
    return result;
  }
  





  public String toString()
  {
    if (m_zeroR != null) {
      return "No attributes other than class. Using ZeroR.\n\n" + m_zeroR.toString();
    }
    if (m_Tree == null) {
      return "REPTree: No model built yet.";
    }
    return "\nREPTree\n============\n" + m_Tree.toString(0, null) + "\n" + "\nSize of the tree : " + numNodes();
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10275 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new REPTree(), argv);
  }
}
