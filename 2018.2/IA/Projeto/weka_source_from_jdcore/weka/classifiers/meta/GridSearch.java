package weka.classifiers.meta;

import java.awt.Point;
import java.awt.geom.Point2D.Double;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.functions.LinearRegression;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MathematicalExpression;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.PropertyPath;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SerializedObject;
import weka.core.Summarizable;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.PLSFilter;
import weka.filters.unsupervised.attribute.NumericCleaner;
import weka.filters.unsupervised.instance.Resample;



































































































































































































































































public class GridSearch
  extends RandomizableSingleClassifierEnhancer
  implements AdditionalMeasureProducer, Summarizable
{
  private static final long serialVersionUID = -3034773968581595348L;
  public static final int EVALUATION_CC = 0;
  public static final int EVALUATION_RMSE = 1;
  public static final int EVALUATION_RRSE = 2;
  public static final int EVALUATION_MAE = 3;
  public static final int EVALUATION_RAE = 4;
  public static final int EVALUATION_COMBINED = 5;
  public static final int EVALUATION_ACC = 6;
  public static final int EVALUATION_KAPPA = 7;
  
  protected class PointDouble
    extends Point2D.Double
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 7151661776161898119L;
    
    public PointDouble(double x, double y)
    {
      super(y);
    }
    








    public boolean equals(Object obj)
    {
      PointDouble pd = (PointDouble)obj;
      
      return (Utils.eq(getX(), pd.getX())) && (Utils.eq(getY(), pd.getY()));
    }
    




    public String toString()
    {
      return super.toString().replaceAll(".*\\[", "[");
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  






  protected class PointInt
    extends Point
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -5900415163698021618L;
    





    public PointInt(int x, int y)
    {
      super(y);
    }
    




    public String toString()
    {
      return super.toString().replaceAll(".*\\[", "[");
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  



  protected class Grid
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 7290732613611243139L;
    


    protected double m_MinX;
    


    protected double m_MaxX;
    


    protected double m_StepX;
    


    protected String m_LabelX;
    


    protected double m_MinY;
    


    protected double m_MaxY;
    


    protected double m_StepY;
    


    protected String m_LabelY;
    


    protected int m_Width;
    

    protected int m_Height;
    


    public Grid(double minX, double maxX, double stepX, double minY, double maxY, double stepY)
    {
      this(minX, maxX, stepX, "", minY, maxY, stepY, "");
    }
    
















    public Grid(double minX, double maxX, double stepX, String labelX, double minY, double maxY, double stepY, String labelY)
    {
      m_MinX = minX;
      m_MaxX = maxX;
      m_StepX = stepX;
      m_LabelX = labelX;
      m_MinY = minY;
      m_MaxY = maxY;
      m_StepY = stepY;
      m_LabelY = labelY;
      m_Height = ((int)StrictMath.round((m_MaxY - m_MinY) / m_StepY) + 1);
      m_Width = ((int)StrictMath.round((m_MaxX - m_MinX) / m_StepX) + 1);
      

      if (m_MinX >= m_MaxX)
        throw new IllegalArgumentException("XMin must be smaller than XMax!");
      if (m_MinY >= m_MaxY) {
        throw new IllegalArgumentException("YMin must be smaller than YMax!");
      }
      
      if (m_StepX <= 0.0D)
        throw new IllegalArgumentException("XStep must be a positive number!");
      if (m_StepY <= 0.0D) {
        throw new IllegalArgumentException("YStep must be a positive number!");
      }
      
      if (!Utils.eq(m_MinX + (m_Width - 1) * m_StepX, m_MaxX)) {
        throw new IllegalArgumentException("X axis doesn't match! Provided max: " + m_MaxX + ", calculated max via min and step size: " + (m_MinX + (m_Width - 1) * m_StepX));
      }
      

      if (!Utils.eq(m_MinY + (m_Height - 1) * m_StepY, m_MaxY)) {
        throw new IllegalArgumentException("Y axis doesn't match! Provided max: " + m_MaxY + ", calculated max via min and step size: " + (m_MinY + (m_Height - 1) * m_StepY));
      }
    }
    










    public boolean equals(Object o)
    {
      Grid g = (Grid)o;
      
      boolean result = (width() == g.width()) && (height() == g.height()) && (getMinX() == g.getMinX()) && (getMinY() == g.getMinY()) && (getStepX() == g.getStepX()) && (getStepY() == g.getStepY()) && (getLabelX().equals(g.getLabelX())) && (getLabelY().equals(g.getLabelY()));
      







      return result;
    }
    




    public double getMinX()
    {
      return m_MinX;
    }
    




    public double getMaxX()
    {
      return m_MaxX;
    }
    




    public double getStepX()
    {
      return m_StepX;
    }
    




    public String getLabelX()
    {
      return m_LabelX;
    }
    




    public double getMinY()
    {
      return m_MinY;
    }
    




    public double getMaxY()
    {
      return m_MaxY;
    }
    




    public double getStepY()
    {
      return m_StepY;
    }
    




    public String getLabelY()
    {
      return m_LabelY;
    }
    




    public int height()
    {
      return m_Height;
    }
    




    public int width()
    {
      return m_Width;
    }
    






    public GridSearch.PointDouble getValues(int x, int y)
    {
      if (x >= width())
        throw new IllegalArgumentException("Index out of scope on X axis (" + x + " >= " + width() + ")!");
      if (y >= height()) {
        throw new IllegalArgumentException("Index out of scope on Y axis (" + y + " >= " + height() + ")!");
      }
      return new GridSearch.PointDouble(GridSearch.this, m_MinX + m_StepX * x, m_MinY + m_StepY * y);
    }
    













    public GridSearch.PointInt getLocation(GridSearch.PointDouble values)
    {
      int x = 0;
      double distance = m_StepX;
      for (int i = 0; i < width(); i++) {
        double currDistance = StrictMath.abs(values.getX() - getValues(i, 0).getX());
        if (Utils.sm(currDistance, distance)) {
          distance = currDistance;
          x = i;
        }
      }
      

      int y = 0;
      distance = m_StepY;
      for (i = 0; i < height(); i++) {
        double currDistance = StrictMath.abs(values.getY() - getValues(0, i).getY());
        if (Utils.sm(currDistance, distance)) {
          distance = currDistance;
          y = i;
        }
      }
      
      GridSearch.PointInt result = new GridSearch.PointInt(GridSearch.this, x, y);
      return result;
    }
    





    public boolean isOnBorder(GridSearch.PointDouble values)
    {
      return isOnBorder(getLocation(values));
    }
    





    public boolean isOnBorder(GridSearch.PointInt location)
    {
      if (location.getX() == 0.0D)
        return true;
      if (location.getX() == width() - 1)
        return true;
      if (location.getY() == 0.0D)
        return true;
      if (location.getY() == height() - 1) {
        return true;
      }
      return false;
    }
    








    public Grid subgrid(int top, int left, int bottom, int right)
    {
      return new Grid(GridSearch.this, getValues(left, top).getX(), getValues(right, top).getX(), getStepX(), getLabelX(), getValues(left, bottom).getY(), getValues(left, top).getY(), getStepY(), getLabelY());
    }
    





    public Grid extend(GridSearch.PointDouble values)
    {
      double minX;
      




      double minX;
      



      if (Utils.smOrEq(values.getX(), getMinX())) {
        double distance = getMinX() - values.getX();
        double minX;
        if (Utils.eq(distance, 0.0D)) {
          minX = getMinX() - getStepX() * (StrictMath.round(distance / getStepX()) + 1L);
        } else {
          minX = getMinX() - getStepX() * StrictMath.round(distance / getStepX());
        }
      } else {
        minX = getMinX();
      }
      double maxX;
      double maxX;
      if (Utils.grOrEq(values.getX(), getMaxX())) {
        double distance = values.getX() - getMaxX();
        double maxX;
        if (Utils.eq(distance, 0.0D)) {
          maxX = getMaxX() + getStepX() * (StrictMath.round(distance / getStepX()) + 1L);
        } else {
          maxX = getMaxX() + getStepX() * StrictMath.round(distance / getStepX());
        }
      } else {
        maxX = getMaxX();
      }
      double minY;
      double minY;
      if (Utils.smOrEq(values.getY(), getMinY())) {
        double distance = getMinY() - values.getY();
        double minY;
        if (Utils.eq(distance, 0.0D)) {
          minY = getMinY() - getStepY() * (StrictMath.round(distance / getStepY()) + 1L);
        } else {
          minY = getMinY() - getStepY() * StrictMath.round(distance / getStepY());
        }
      } else {
        minY = getMinY();
      }
      double maxY;
      double maxY;
      if (Utils.grOrEq(values.getY(), getMaxY())) {
        double distance = values.getY() - getMaxY();
        double maxY;
        if (Utils.eq(distance, 0.0D)) {
          maxY = getMaxY() + getStepY() * (StrictMath.round(distance / getStepY()) + 1L);
        } else {
          maxY = getMaxY() + getStepY() * StrictMath.round(distance / getStepY());
        }
      } else {
        maxY = getMaxY();
      }
      
      Grid result = new Grid(GridSearch.this, minX, maxX, getStepX(), getLabelX(), minY, maxY, getStepY(), getLabelY());
      

      if (equals(result)) {
        throw new IllegalStateException("Grid extension failed!");
      }
      return result;
    }
    









    public Enumeration<GridSearch.PointDouble> row(int y)
    {
      Vector result = new Vector();
      
      for (int i = 0; i < width(); i++) {
        result.add(getValues(i, y));
      }
      return result.elements();
    }
    









    public Enumeration<GridSearch.PointDouble> column(int x)
    {
      Vector result = new Vector();
      
      for (int i = 0; i < height(); i++) {
        result.add(getValues(x, i));
      }
      return result.elements();
    }
    






    public String toString()
    {
      String result = "X: " + m_MinX + " - " + m_MaxX + ", Step " + m_StepX;
      if (m_LabelX.length() != 0)
        result = result + " (" + m_LabelX + ")";
      result = result + "\n";
      
      result = result + "Y: " + m_MinY + " - " + m_MaxY + ", Step " + m_StepY;
      if (m_LabelY.length() != 0)
        result = result + " (" + m_LabelY + ")";
      result = result + "\n";
      
      result = result + "Dimensions (Rows x Columns): " + height() + " x " + width();
      
      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  




  protected class Performance
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = -4374706475277588755L;
    



    protected GridSearch.PointDouble m_Values;
    


    protected double m_CC;
    


    protected double m_RMSE;
    


    protected double m_RRSE;
    


    protected double m_MAE;
    


    protected double m_RAE;
    


    protected double m_ACC;
    


    protected double m_Kappa;
    



    public Performance(GridSearch.PointDouble values, Evaluation evaluation)
      throws Exception
    {
      m_Values = values;
      
      m_RMSE = evaluation.rootMeanSquaredError();
      m_RRSE = evaluation.rootRelativeSquaredError();
      m_MAE = evaluation.meanAbsoluteError();
      m_RAE = evaluation.relativeAbsoluteError();
      try
      {
        m_CC = evaluation.correlationCoefficient();
      }
      catch (Exception e) {
        m_CC = NaN.0D;
      }
      try {
        m_ACC = evaluation.pctCorrect();
      }
      catch (Exception e) {
        m_ACC = NaN.0D;
      }
      try {
        m_Kappa = evaluation.kappa();
      }
      catch (Exception e) {
        m_Kappa = NaN.0D;
      }
    }
    







    public double getPerformance(int evaluation)
    {
      double result = NaN.0D;
      
      switch (evaluation) {
      case 0: 
        result = m_CC;
        break;
      case 1: 
        result = m_RMSE;
        break;
      case 2: 
        result = m_RRSE;
        break;
      case 3: 
        result = m_MAE;
        break;
      case 4: 
        result = m_RAE;
        break;
      case 5: 
        result = 1.0D - StrictMath.abs(m_CC) + m_RRSE + m_RAE;
        break;
      case 6: 
        result = m_ACC;
        break;
      case 7: 
        result = m_Kappa;
        break;
      default: 
        throw new IllegalArgumentException("Evaluation type '" + evaluation + "' not supported!");
      }
      
      return result;
    }
    




    public GridSearch.PointDouble getValues()
    {
      return m_Values;
    }
    







    public String toString(int evaluation)
    {
      String result = "Performance (" + getValues() + "): " + getPerformance(evaluation) + " (" + new SelectedTag(evaluation, GridSearch.TAGS_EVALUATION) + ")";
      


      return result;
    }
    







    public String toGnuplot(int evaluation)
    {
      String result = getValues().getX() + "\t" + getValues().getY() + "\t" + getPerformance(evaluation);
      


      return result;
    }
    







    public String toString()
    {
      String result = "Performance (" + getValues() + "): ";
      
      for (int i = 0; i < GridSearch.TAGS_EVALUATION.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + getPerformance(GridSearch.TAGS_EVALUATION[i].getID()) + " (" + new SelectedTag(GridSearch.TAGS_EVALUATION[i].getID(), GridSearch.TAGS_EVALUATION) + ")";
      }
      

      return result;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  






  protected class PerformanceComparator
    implements Comparator<GridSearch.Performance>, Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 6507592831825393847L;
    




    protected int m_Evaluation;
    





    public PerformanceComparator(int evaluation)
    {
      m_Evaluation = evaluation;
    }
    





    public int getEvaluation()
    {
      return m_Evaluation;
    }
    












    public int compare(GridSearch.Performance o1, GridSearch.Performance o2)
    {
      double p1 = o1.getPerformance(getEvaluation());
      double p2 = o2.getPerformance(getEvaluation());
      int result;
      int result; if (Utils.sm(p1, p2)) {
        result = -1; } else { int result;
        if (Utils.gr(p1, p2)) {
          result = 1;
        } else {
          result = 0;
        }
      }
      

      if ((getEvaluation() != 0) && (getEvaluation() != 6) && (getEvaluation() != 7))
      {

        result = -result;
      }
      return result;
    }
    





    public boolean equals(Object obj)
    {
      if (!(obj instanceof PerformanceComparator)) {
        throw new IllegalArgumentException("Must be PerformanceComparator!");
      }
      return m_Evaluation == m_Evaluation;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  





  protected class PerformanceTable
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 5486491313460338379L;
    




    protected GridSearch.Grid m_Grid;
    



    protected Vector<GridSearch.Performance> m_Performances;
    



    protected int m_Type;
    



    protected double[][] m_Table;
    



    protected double m_Min;
    



    protected double m_Max;
    




    public PerformanceTable(Vector<GridSearch.Performance> grid, int performances)
    {
      m_Grid = grid;
      m_Type = type;
      m_Performances = performances;
      
      generate();
    }
    






    protected void generate()
    {
      m_Table = new double[getGrid().height()][getGrid().width()];
      m_Min = 0.0D;
      m_Max = 0.0D;
      
      for (int i = 0; i < getPerformances().size(); i++) {
        GridSearch.Performance perf = (GridSearch.Performance)getPerformances().get(i);
        GridSearch.PointInt location = getGrid().getLocation(perf.getValues());
        m_Table[(getGrid().height() - (int)location.getY() - 1)][((int)location.getX())] = perf.getPerformance(getType());
        

        if (i == 0) {
          m_Min = perf.getPerformance(m_Type);
          m_Max = m_Min;
        }
        else {
          if (perf.getPerformance(m_Type) < m_Min)
            m_Min = perf.getPerformance(m_Type);
          if (perf.getPerformance(m_Type) > m_Max) {
            m_Max = perf.getPerformance(m_Type);
          }
        }
      }
    }
    



    public GridSearch.Grid getGrid()
    {
      return m_Grid;
    }
    




    public Vector<GridSearch.Performance> getPerformances()
    {
      return m_Performances;
    }
    




    public int getType()
    {
      return m_Type;
    }
    






    public double[][] getTable()
    {
      return m_Table;
    }
    




    public double getMin()
    {
      return m_Min;
    }
    




    public double getMax()
    {
      return m_Max;
    }
    








    public String toString()
    {
      String result = "Table (" + new SelectedTag(getType(), GridSearch.TAGS_EVALUATION).getSelectedTag().getReadable() + ") - " + "X: " + getGrid().getLabelX() + ", Y: " + getGrid().getLabelY() + ":\n";
      




      for (int i = 0; i < getTable().length; i++) {
        if (i > 0) {
          result = result + "\n";
        }
        for (int n = 0; n < getTable()[i].length; n++) {
          if (n > 0)
            result = result + ",";
          result = result + getTable()[i][n];
        }
      }
      
      return result;
    }
    








    public String toGnuplot()
    {
      StringBuffer result = new StringBuffer();
      Tag type = new SelectedTag(getType(), GridSearch.TAGS_EVALUATION).getSelectedTag();
      
      result.append("Gnuplot (" + type.getReadable() + "):\n");
      result.append("# begin 'gridsearch.data'\n");
      result.append("# " + type.getReadable() + "\n");
      for (int i = 0; i < getPerformances().size(); i++)
        result.append(((GridSearch.Performance)getPerformances().get(i)).toGnuplot(type.getID()) + "\n");
      result.append("# end 'gridsearch.data'\n\n");
      
      result.append("# begin 'gridsearch.plot'\n");
      result.append("# " + type.getReadable() + "\n");
      result.append("set data style lines\n");
      result.append("set contour base\n");
      result.append("set surface\n");
      result.append("set title '" + m_Data.relationName() + "'\n");
      result.append("set xrange [" + getGrid().getMinX() + ":" + getGrid().getMaxX() + "]\n");
      result.append("set xlabel 'x (" + getFilter().getClass().getName() + ": " + getXProperty() + ")'\n");
      result.append("set yrange [" + getGrid().getMinY() + ":" + getGrid().getMaxY() + "]\n");
      result.append("set ylabel 'y - (" + getClassifier().getClass().getName() + ": " + getYProperty() + ")'\n");
      result.append("set zrange [" + (getMin() - (getMax() - getMin()) * 0.1D) + ":" + (getMax() + (getMax() - getMin()) * 0.1D) + "]\n");
      result.append("set zlabel 'z - " + type.getReadable() + "'\n");
      result.append("set dgrid3d " + getGrid().height() + "," + getGrid().width() + ",1\n");
      result.append("show contour\n");
      result.append("splot 'gridsearch.data'\n");
      result.append("pause -1\n");
      result.append("# end 'gridsearch.plot'");
      
      return result.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  



  protected class PerformanceCache
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 5838863230451530252L;
    


    protected Hashtable m_Cache = new Hashtable();
    


    protected PerformanceCache() {}
    


    protected String getID(int cv, GridSearch.PointDouble values)
    {
      return cv + "\t" + values.getX() + "\t" + values.getY();
    }
    






    public boolean isCached(int cv, GridSearch.PointDouble values)
    {
      return get(cv, values) != null;
    }
    






    public GridSearch.Performance get(int cv, GridSearch.PointDouble values)
    {
      return (GridSearch.Performance)m_Cache.get(getID(cv, values));
    }
    





    public void add(int cv, GridSearch.Performance p)
    {
      m_Cache.put(getID(cv, p.getValues()), p);
    }
    




    public String toString()
    {
      return m_Cache.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9733 $");
    }
  }
  




















  public static final Tag[] TAGS_EVALUATION = { new Tag(0, "CC", "Correlation coefficient"), new Tag(1, "RMSE", "Root mean squared error"), new Tag(2, "RRSE", "Root relative squared error"), new Tag(3, "MAE", "Mean absolute error"), new Tag(4, "RAE", "Root absolute error"), new Tag(5, "COMB", "Combined = (1-abs(CC)) + RRSE + RAE"), new Tag(6, "ACC", "Accuracy"), new Tag(7, "KAP", "Kappa") };
  




  public static final int TRAVERSAL_BY_ROW = 0;
  



  public static final int TRAVERSAL_BY_COLUMN = 1;
  



  public static final Tag[] TAGS_TRAVERSAL = { new Tag(0, "row-wise", "row-wise"), new Tag(1, "column-wise", "column-wise") };
  


  public static final String PREFIX_CLASSIFIER = "classifier.";
  


  public static final String PREFIX_FILTER = "filter.";
  


  protected Filter m_Filter;
  

  protected Filter m_BestFilter;
  

  protected Classifier m_BestClassifier;
  

  protected PointDouble m_Values = null;
  

  protected int m_Evaluation = 0;
  


  protected String m_Y_Property = "classifier.ridge";
  

  protected double m_Y_Min = -10.0D;
  

  protected double m_Y_Max = 5.0D;
  

  protected double m_Y_Step = 1.0D;
  

  protected double m_Y_Base = 10.0D;
  














  protected String m_Y_Expression = "pow(BASE,I)";
  


  protected String m_X_Property = "filter.numComponents";
  

  protected double m_X_Min = 5.0D;
  

  protected double m_X_Max = 20.0D;
  

  protected double m_X_Step = 1.0D;
  

  protected double m_X_Base = 10.0D;
  














  protected String m_X_Expression = "I";
  

  protected boolean m_GridIsExtendable = false;
  

  protected int m_MaxGridExtensions = 3;
  

  protected int m_GridExtensionsPerformed = 0;
  

  protected double m_SampleSize = 100.0D;
  

  protected int m_Traversal = 1;
  

  protected File m_LogFile = new File(System.getProperty("user.dir"));
  

  protected Grid m_Grid;
  

  protected Instances m_Data;
  

  protected PerformanceCache m_Cache;
  

  protected boolean m_UniformPerformance = false;
  





  public GridSearch()
  {
    m_Classifier = new LinearRegression();
    ((LinearRegression)m_Classifier).setAttributeSelectionMethod(new SelectedTag(1, LinearRegression.TAGS_SELECTION));
    ((LinearRegression)m_Classifier).setEliminateColinearAttributes(false);
    

    m_Filter = new PLSFilter();
    PLSFilter filter = new PLSFilter();
    filter.setPreprocessing(new SelectedTag(2, PLSFilter.TAGS_PREPROCESSING));
    filter.setReplaceMissing(true);
    try
    {
      m_BestClassifier = Classifier.makeCopy(m_Classifier);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    try {
      m_BestFilter = Filter.makeCopy(filter);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  





  public String globalInfo()
  {
    return "Performs a grid search of parameter pairs for the a classifier (Y-axis, default is LinearRegression with the \"Ridge\" parameter) and the PLSFilter (X-axis, \"# of Components\") and chooses the best pair found for the actual predicting.\n\nThe initial grid is worked on with 2-fold CV to determine the values of the parameter pairs for the selected type of evaluation (e.g., accuracy). The best point in the grid is then taken and a 10-fold CV is performed with the adjacent parameter pairs. If a better pair is found, then this will act as new center and another 10-fold CV will be performed (kind of hill-climbing). This process is repeated until no better pair is found or the best pair is on the border of the grid.\nIn case the best pair is on the border, one can let GridSearch automatically extend the grid and continue the search. Check out the properties 'gridIsExtendable' (option '-extend-grid') and 'maxGridExtensions' (option '-max-grid-extensions <num>').\n\nGridSearch can handle doubles, integers (values are just cast to int) and booleans (0 is false, otherwise true). float, char and long are supported as well.\n\nThe best filter/classifier setup can be accessed after the buildClassifier call via the getBestFilter/getBestClassifier methods.\nNote on the implementation: after the data has been passed through the filter, a default NumericCleaner filter is applied to the data in order to avoid numbers that are getting too small and might produce NaNs in other schemes.";
  }
  




























  protected String defaultClassifierString()
  {
    return LinearRegression.class.getName();
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    String desc = "";
    for (int i = 0; i < TAGS_EVALUATION.length; i++) {
      SelectedTag tag = new SelectedTag(TAGS_EVALUATION[i].getID(), TAGS_EVALUATION);
      desc = desc + "\t" + tag.getSelectedTag().getIDStr() + " = " + tag.getSelectedTag().getReadable() + "\n";
    }
    

    result.addElement(new Option("\tDetermines the parameter used for evaluation:\n" + desc + "\t(default: " + new SelectedTag(0, TAGS_EVALUATION) + ")", "E", 1, "-E " + Tag.toOptionList(TAGS_EVALUATION)));
    




    result.addElement(new Option("\tThe Y option to test (without leading dash).\n\t(default: classifier.ridge)", "y-property", 1, "-y-property <option>"));
    



    result.addElement(new Option("\tThe minimum for Y.\n\t(default: -10)", "y-min", 1, "-y-min <num>"));
    



    result.addElement(new Option("\tThe maximum for Y.\n\t(default: +5)", "y-max", 1, "-y-max <num>"));
    



    result.addElement(new Option("\tThe step size for Y.\n\t(default: 1)", "y-step", 1, "-y-step <num>"));
    



    result.addElement(new Option("\tThe base for Y.\n\t(default: 10)", "y-base", 1, "-y-base <num>"));
    



    result.addElement(new Option("\tThe expression for Y.\n\tAvailable parameters:\n\t\tBASE\n\t\tFROM\n\t\tTO\n\t\tSTEP\n\t\tI - the current iteration value\n\t\t(from 'FROM' to 'TO' with stepsize 'STEP')\n\t(default: 'pow(BASE,I)')", "y-expression", 1, "-y-expression <expr>"));
    










    result.addElement(new Option("\tThe filter to use (on X axis). Full classname of filter to include, \n\tfollowed by scheme options.\n\t(default: weka.filters.supervised.attribute.PLSFilter)", "filter", 1, "-filter <filter specification>"));
    




    result.addElement(new Option("\tThe X option to test (without leading dash).\n\t(default: filter.numComponents)", "x-property", 1, "-x-property <option>"));
    



    result.addElement(new Option("\tThe minimum for X.\n\t(default: +5)", "x-min", 1, "-x-min <num>"));
    



    result.addElement(new Option("\tThe maximum for X.\n\t(default: +20)", "x-max", 1, "-x-max <num>"));
    



    result.addElement(new Option("\tThe step size for X.\n\t(default: 1)", "x-step", 1, "-x-step <num>"));
    



    result.addElement(new Option("\tThe base for X.\n\t(default: 10)", "x-base", 1, "-x-base <num>"));
    



    result.addElement(new Option("\tThe expression for the X value.\n\tAvailable parameters:\n\t\tBASE\n\t\tMIN\n\t\tMAX\n\t\tSTEP\n\t\tI - the current iteration value\n\t\t(from 'FROM' to 'TO' with stepsize 'STEP')\n\t(default: 'pow(BASE,I)')", "x-expression", 1, "-x-expression <expr>"));
    










    result.addElement(new Option("\tWhether the grid can be extended.\n\t(default: no)", "extend-grid", 0, "-extend-grid"));
    



    result.addElement(new Option("\tThe maximum number of grid extensions (-1 is unlimited).\n\t(default: 3)", "max-grid-extensions", 1, "-max-grid-extensions <num>"));
    



    result.addElement(new Option("\tThe size (in percent) of the sample to search the inital grid with.\n\t(default: 100)", "sample-size", 1, "-sample-size <num>"));
    



    result.addElement(new Option("\tThe type of traversal for the grid.\n\t(default: " + new SelectedTag(1, TAGS_TRAVERSAL) + ")", "traversal", 1, "-traversal " + Tag.toOptionList(TAGS_TRAVERSAL)));
    



    result.addElement(new Option("\tThe log file to log the messages to.\n\t(default: none)", "log-file", 1, "-log-file <filename>"));
    



    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    if ((getFilter() instanceof OptionHandler)) {
      result.addElement(new Option("", "", 0, "\nOptions specific to filter " + getFilter().getClass().getName() + " ('-filter'):"));
      



      en = ((OptionHandler)getFilter()).listOptions();
      while (en.hasMoreElements()) {
        result.addElement(en.nextElement());
      }
    }
    
    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-E");
    result.add("" + getEvaluation());
    
    result.add("-y-property");
    result.add("" + getYProperty());
    
    result.add("-y-min");
    result.add("" + getYMin());
    
    result.add("-y-max");
    result.add("" + getYMax());
    
    result.add("-y-step");
    result.add("" + getYStep());
    
    result.add("-y-base");
    result.add("" + getYBase());
    
    result.add("-y-expression");
    result.add("" + getYExpression());
    
    result.add("-filter");
    if ((getFilter() instanceof OptionHandler)) {
      result.add(getFilter().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getFilter()).getOptions()));

    }
    else
    {
      result.add(getFilter().getClass().getName());
    }
    
    result.add("-x-property");
    result.add("" + getXProperty());
    
    result.add("-x-min");
    result.add("" + getXMin());
    
    result.add("-x-max");
    result.add("" + getXMax());
    
    result.add("-x-step");
    result.add("" + getXStep());
    
    result.add("-x-base");
    result.add("" + getXBase());
    
    result.add("-x-expression");
    result.add("" + getXExpression());
    
    if (getGridIsExtendable()) {
      result.add("-extend-grid");
      result.add("-max-grid-extensions");
      result.add("" + getMaxGridExtensions());
    }
    
    result.add("-sample-size");
    result.add("" + getSampleSizePercent());
    
    result.add("-traversal");
    result.add("" + getTraversal());
    
    result.add("-log-file");
    result.add("" + getLogFile());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  










































































































































































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0) {
      setEvaluation(new SelectedTag(tmpStr, TAGS_EVALUATION));
    } else {
      setEvaluation(new SelectedTag(0, TAGS_EVALUATION));
    }
    tmpStr = Utils.getOption("y-property", options);
    if (tmpStr.length() != 0) {
      setYProperty(tmpStr);
    } else {
      setYProperty("classifier.ridge");
    }
    tmpStr = Utils.getOption("y-min", options);
    if (tmpStr.length() != 0) {
      setYMin(Double.parseDouble(tmpStr));
    } else {
      setYMin(-10.0D);
    }
    tmpStr = Utils.getOption("y-max", options);
    if (tmpStr.length() != 0) {
      setYMax(Double.parseDouble(tmpStr));
    } else {
      setYMax(10.0D);
    }
    tmpStr = Utils.getOption("y-step", options);
    if (tmpStr.length() != 0) {
      setYStep(Double.parseDouble(tmpStr));
    } else {
      setYStep(1.0D);
    }
    tmpStr = Utils.getOption("y-base", options);
    if (tmpStr.length() != 0) {
      setYBase(Double.parseDouble(tmpStr));
    } else {
      setYBase(10.0D);
    }
    tmpStr = Utils.getOption("y-expression", options);
    if (tmpStr.length() != 0) {
      setYExpression(tmpStr);
    } else {
      setYExpression("pow(BASE,I)");
    }
    tmpStr = Utils.getOption("filter", options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setFilter((Filter)Utils.forName(Filter.class, tmpStr, tmpOptions));
    }
    
    tmpStr = Utils.getOption("x-property", options);
    if (tmpStr.length() != 0) {
      setXProperty(tmpStr);
    } else {
      setXProperty("filter.filters[0].kernel.gamma");
    }
    tmpStr = Utils.getOption("x-min", options);
    if (tmpStr.length() != 0) {
      setXMin(Double.parseDouble(tmpStr));
    } else {
      setXMin(-10.0D);
    }
    tmpStr = Utils.getOption("x-max", options);
    if (tmpStr.length() != 0) {
      setXMax(Double.parseDouble(tmpStr));
    } else {
      setXMax(10.0D);
    }
    tmpStr = Utils.getOption("x-step", options);
    if (tmpStr.length() != 0) {
      setXStep(Double.parseDouble(tmpStr));
    } else {
      setXStep(1.0D);
    }
    tmpStr = Utils.getOption("x-base", options);
    if (tmpStr.length() != 0) {
      setXBase(Double.parseDouble(tmpStr));
    } else {
      setXBase(10.0D);
    }
    tmpStr = Utils.getOption("x-expression", options);
    if (tmpStr.length() != 0) {
      setXExpression(tmpStr);
    } else {
      setXExpression("pow(BASE,I)");
    }
    setGridIsExtendable(Utils.getFlag("extend-grid", options));
    if (getGridIsExtendable()) {
      tmpStr = Utils.getOption("max-grid-extensions", options);
      if (tmpStr.length() != 0) {
        setMaxGridExtensions(Integer.parseInt(tmpStr));
      } else {
        setMaxGridExtensions(3);
      }
    }
    tmpStr = Utils.getOption("sample-size", options);
    if (tmpStr.length() != 0) {
      setSampleSizePercent(Double.parseDouble(tmpStr));
    } else {
      setSampleSizePercent(100.0D);
    }
    tmpStr = Utils.getOption("traversal", options);
    if (tmpStr.length() != 0) {
      setTraversal(new SelectedTag(tmpStr, TAGS_TRAVERSAL));
    } else {
      setTraversal(new SelectedTag(0, TAGS_TRAVERSAL));
    }
    tmpStr = Utils.getOption("log-file", options);
    if (tmpStr.length() != 0) {
      setLogFile(new File(tmpStr));
    } else {
      setLogFile(new File(System.getProperty("user.dir")));
    }
    super.setOptions(options);
  }
  







  public void setClassifier(Classifier newClassifier)
  {
    Capabilities cap = newClassifier.getCapabilities();
    
    boolean numeric = (cap.handles(Capabilities.Capability.NUMERIC_CLASS)) || (cap.hasDependency(Capabilities.Capability.NUMERIC_CLASS));
    

    boolean nominal = (cap.handles(Capabilities.Capability.NOMINAL_CLASS)) || (cap.hasDependency(Capabilities.Capability.NOMINAL_CLASS)) || (cap.handles(Capabilities.Capability.BINARY_CLASS)) || (cap.hasDependency(Capabilities.Capability.BINARY_CLASS)) || (cap.handles(Capabilities.Capability.UNARY_CLASS)) || (cap.hasDependency(Capabilities.Capability.UNARY_CLASS));
    





    if ((m_Evaluation == 0) && (!numeric)) {
      throw new IllegalArgumentException("Classifier needs to handle numeric class for chosen type of evaluation!");
    }
    
    if (((m_Evaluation == 6) || (m_Evaluation == 7)) && (!nominal)) {
      throw new IllegalArgumentException("Classifier needs to handle nominal class for chosen type of evaluation!");
    }
    
    super.setClassifier(newClassifier);
    try
    {
      m_BestClassifier = Classifier.makeCopy(m_Classifier);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  





  public String filterTipText()
  {
    return "The filter to be used (only used for setup).";
  }
  




  public void setFilter(Filter value)
  {
    m_Filter = value;
    try
    {
      m_BestFilter = Filter.makeCopy(m_Filter);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public Filter getFilter()
  {
    return m_Filter;
  }
  





  public String evaluationTipText()
  {
    return "Sets the criterion for evaluating the classifier performance and choosing the best one.";
  }
  






  public void setEvaluation(SelectedTag value)
  {
    if (value.getTags() == TAGS_EVALUATION) {
      m_Evaluation = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getEvaluation()
  {
    return new SelectedTag(m_Evaluation, TAGS_EVALUATION);
  }
  





  public String YPropertyTipText()
  {
    return "The Y property to test (normally the classifier).";
  }
  




  public String getYProperty()
  {
    return m_Y_Property;
  }
  




  public void setYProperty(String value)
  {
    m_Y_Property = value;
  }
  





  public String YMinTipText()
  {
    return "The minimum of Y (normally the classifier).";
  }
  




  public double getYMin()
  {
    return m_Y_Min;
  }
  




  public void setYMin(double value)
  {
    m_Y_Min = value;
  }
  





  public String YMaxTipText()
  {
    return "The maximum of Y.";
  }
  




  public double getYMax()
  {
    return m_Y_Max;
  }
  




  public void setYMax(double value)
  {
    m_Y_Max = value;
  }
  





  public String YStepTipText()
  {
    return "The step size of Y.";
  }
  




  public double getYStep()
  {
    return m_Y_Step;
  }
  




  public void setYStep(double value)
  {
    m_Y_Step = value;
  }
  





  public String YBaseTipText()
  {
    return "The base of Y.";
  }
  




  public double getYBase()
  {
    return m_Y_Base;
  }
  




  public void setYBase(double value)
  {
    m_Y_Base = value;
  }
  





  public String YExpressionTipText()
  {
    return "The expression for the Y value (parameters: BASE, FROM, TO, STEP, I).";
  }
  




  public String getYExpression()
  {
    return m_Y_Expression;
  }
  




  public void setYExpression(String value)
  {
    m_Y_Expression = value;
  }
  





  public String XPropertyTipText()
  {
    return "The X property to test (normally the filter).";
  }
  




  public String getXProperty()
  {
    return m_X_Property;
  }
  




  public void setXProperty(String value)
  {
    m_X_Property = value;
  }
  





  public String XMinTipText()
  {
    return "The minimum of X.";
  }
  




  public double getXMin()
  {
    return m_X_Min;
  }
  




  public void setXMin(double value)
  {
    m_X_Min = value;
  }
  





  public String XMaxTipText()
  {
    return "The maximum of X.";
  }
  




  public double getXMax()
  {
    return m_X_Max;
  }
  




  public void setXMax(double value)
  {
    m_X_Max = value;
  }
  





  public String XStepTipText()
  {
    return "The step size of X.";
  }
  




  public double getXStep()
  {
    return m_X_Step;
  }
  




  public void setXStep(double value)
  {
    m_X_Step = value;
  }
  





  public String XBaseTipText()
  {
    return "The base of X.";
  }
  




  public double getXBase()
  {
    return m_X_Base;
  }
  




  public void setXBase(double value)
  {
    m_X_Base = value;
  }
  





  public String XExpressionTipText()
  {
    return "The expression for the X value (parameters: BASE, FROM, TO, STEP, I).";
  }
  




  public String getXExpression()
  {
    return m_X_Expression;
  }
  




  public void setXExpression(String value)
  {
    m_X_Expression = value;
  }
  





  public String gridIsExtendableTipText()
  {
    return "Whether the grid can be extended.";
  }
  




  public boolean getGridIsExtendable()
  {
    return m_GridIsExtendable;
  }
  




  public void setGridIsExtendable(boolean value)
  {
    m_GridIsExtendable = value;
  }
  





  public String maxGridExtensionsTipText()
  {
    return "The maximum number of grid extensions, -1 for unlimited.";
  }
  




  public int getMaxGridExtensions()
  {
    return m_MaxGridExtensions;
  }
  




  public void setMaxGridExtensions(int value)
  {
    m_MaxGridExtensions = value;
  }
  





  public String sampleSizePercentTipText()
  {
    return "The sample size (in percent) to use in the initial grid search.";
  }
  




  public double getSampleSizePercent()
  {
    return m_SampleSize;
  }
  




  public void setSampleSizePercent(double value)
  {
    m_SampleSize = value;
  }
  





  public String traversalTipText()
  {
    return "Sets type of traversal of the grid, either by rows or columns.";
  }
  




  public void setTraversal(SelectedTag value)
  {
    if (value.getTags() == TAGS_TRAVERSAL) {
      m_Traversal = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getTraversal()
  {
    return new SelectedTag(m_Traversal, TAGS_TRAVERSAL);
  }
  





  public String logFileTipText()
  {
    return "The log file to log the messages to.";
  }
  




  public File getLogFile()
  {
    return m_LogFile;
  }
  




  public void setLogFile(File value)
  {
    m_LogFile = value;
  }
  




  public Filter getBestFilter()
  {
    return m_BestFilter;
  }
  




  public Classifier getBestClassifier()
  {
    return m_BestClassifier;
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector result = new Vector();
    
    result.add("measureX");
    result.add("measureY");
    result.add("measureGridExtensionsPerformed");
    
    return result.elements();
  }
  





  public double getMeasure(String measureName)
  {
    if (measureName.equalsIgnoreCase("measureX"))
      return evaluate(getValues().getX(), true);
    if (measureName.equalsIgnoreCase("measureY"))
      return evaluate(getValues().getY(), false);
    if (measureName.equalsIgnoreCase("measureGridExtensionsPerformed")) {
      return getGridExtensionsPerformed();
    }
    throw new IllegalArgumentException("Measure '" + measureName + "' not supported!");
  }
  




  public PointDouble getValues()
  {
    return m_Values;
  }
  






  public int getGridExtensionsPerformed()
  {
    return m_GridExtensionsPerformed;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    

    Capabilities result;
    

    if (getFilter() == null) {
      result = super.getCapabilities();
    } else {
      result = getFilter().getCapabilities();
    }
    
    Capabilities classes = result.getClassCapabilities();
    Iterator iter = classes.capabilities();
    while (iter.hasNext()) {
      Capabilities.Capability capab = (Capabilities.Capability)iter.next();
      if ((capab != Capabilities.Capability.BINARY_CLASS) && (capab != Capabilities.Capability.UNARY_CLASS) && (capab != Capabilities.Capability.NOMINAL_CLASS) && (capab != Capabilities.Capability.NUMERIC_CLASS) && (capab != Capabilities.Capability.DATE_CLASS))
      {



        result.disable(capab);
      }
    }
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    if (result.getMinimumNumberInstances() < 1) {
      result.setMinimumNumberInstances(1);
    }
    result.setOwner(this);
    
    return result;
  }
  





  protected void log(String message)
  {
    log(message, false);
  }
  








  protected void log(String message, boolean onlyLog)
  {
    if ((getDebug()) && (!onlyLog)) {
      System.out.println(message);
    }
    
    if (!getLogFile().isDirectory()) {
      Debug.writeToFile(getLogFile().getAbsolutePath(), message, true);
    }
  }
  













  protected String[] updateOption(String[] options, String option, String value)
    throws Exception
  {
    Utils.getOption(option, options);
    

    Vector tmpOptions = new Vector();
    tmpOptions.add("-" + option);
    tmpOptions.add("" + value);
    

    for (int i = 0; i < options.length; i++) {
      if (options[i].length() != 0) {
        tmpOptions.add(options[i]);
      }
    }
    String[] result = (String[])tmpOptions.toArray(new String[tmpOptions.size()]);
    
    return result;
  }
  


  protected double evaluate(double value, boolean isX)
  {
    double step;
    

    String expr;
    
    double base;
    
    double min;
    
    double max;
    
    double step;
    
    if (isX) {
      String expr = getXExpression();
      double base = getXBase();
      double min = getXMin();
      double max = getXMax();
      step = getXStep();
    }
    else {
      expr = getYExpression();
      base = getYBase();
      min = getYMin();
      max = getYMax();
      step = getYStep();
    }
    double result;
    try {
      HashMap symbols = new HashMap();
      symbols.put("BASE", new Double(base));
      symbols.put("FROM", new Double(min));
      symbols.put("TO", new Double(max));
      symbols.put("STEP", new Double(step));
      symbols.put("I", new Double(value));
      result = MathematicalExpression.evaluate(expr, symbols);
    }
    catch (Exception e) {
      result = NaN.0D;
    }
    
    return result;
  }
  












  protected Object setValue(Object o, String path, double value)
    throws Exception
  {
    PropertyDescriptor desc = PropertyPath.getPropertyDescriptor(o, path);
    Class c = desc.getPropertyType();
    

    if ((c == Float.class) || (c == Float.TYPE)) {
      PropertyPath.setValue(o, path, new Float((float)value));
    }
    else if ((c == Double.class) || (c == Double.TYPE)) {
      PropertyPath.setValue(o, path, new Double(value));
    }
    else if ((c == Character.class) || (c == Character.TYPE)) {
      PropertyPath.setValue(o, path, new Integer((char)(int)value));
    }
    else if ((c == Integer.class) || (c == Integer.TYPE)) {
      PropertyPath.setValue(o, path, new Integer((int)value));
    }
    else if ((c == Long.class) || (c == Long.TYPE)) {
      PropertyPath.setValue(o, path, new Long(value));
    }
    else if ((c == Boolean.class) || (c == Boolean.TYPE))
      PropertyPath.setValue(o, path, value == 0.0D ? new Boolean(false) : new Boolean(true)); else {
      throw new Exception("Could neither set double nor integer nor boolean value for '" + path + "'!");
    }
    
    return o;
  }
  









  protected Object setup(Object original, double valueX, double valueY)
    throws Exception
  {
    Object result = new SerializedObject(original).getObject();
    
    if ((original instanceof Classifier)) {
      if (getXProperty().startsWith("classifier.")) {
        setValue(result, getXProperty().substring("classifier.".length()), valueX);
      }
      


      if (getYProperty().startsWith("classifier.")) {
        setValue(result, getYProperty().substring("classifier.".length()), valueY);
      }
      

    }
    else if ((original instanceof Filter)) {
      if (getXProperty().startsWith("filter.")) {
        setValue(result, getXProperty().substring("filter.".length()), valueX);
      }
      


      if (getYProperty().startsWith("filter.")) {
        setValue(result, getYProperty().substring("filter.".length()), valueY);
      }
      
    }
    else
    {
      throw new IllegalArgumentException("Object must be either classifier or filter!");
    }
    
    return result;
  }
  











  protected String logPerformances(Grid grid, Vector<Performance> performances, Tag type)
  {
    StringBuffer result = new StringBuffer(type.getReadable() + ":\n");
    PerformanceTable table = new PerformanceTable(grid, performances, type.getID());
    
    result.append(table.toString() + "\n");
    result.append("\n");
    result.append(table.toGnuplot() + "\n");
    result.append("\n");
    
    return result.toString();
  }
  








  protected void logPerformances(Grid grid, Vector performances)
  {
    for (int i = 0; i < TAGS_EVALUATION.length; i++) {
      log("\n" + logPerformances(grid, performances, TAGS_EVALUATION[i]), true);
    }
  }
  
























  protected PointDouble determineBestInGrid(Grid grid, Instances inst, int cv)
    throws Exception
  {
    Vector<Performance> performances = new Vector();
    
    log("Determining best pair with " + cv + "-fold CV in Grid:\n" + grid + "\n");
    int size;
    int size; if (m_Traversal == 1) {
      size = grid.width();
    } else {
      size = grid.height();
    }
    boolean allCached = true;
    
    for (int i = 0; i < size; i++) { Enumeration<PointDouble> enm;
      Enumeration<PointDouble> enm; if (m_Traversal == 1) {
        enm = grid.column(i);
      } else {
        enm = grid.row(i);
      }
      Filter filter = null;
      Instances data = null;
      
      while (enm.hasMoreElements()) {
        PointDouble values = (PointDouble)enm.nextElement();
        

        boolean cached = m_Cache.isCached(cv, values);
        if (cached) {
          performances.add(m_Cache.get(cv, values));
        }
        else {
          allCached = false;
          
          double x = evaluate(values.getX(), true);
          double y = evaluate(values.getY(), false);
          

          if (filter == null) {
            filter = (Filter)setup(getFilter(), x, y);
            filter.setInputFormat(inst);
            data = Filter.useFilter(inst, filter);
            
            Filter cleaner = new NumericCleaner();
            cleaner.setInputFormat(data);
            data = Filter.useFilter(data, cleaner);
          }
          

          Classifier classifier = (Classifier)setup(getClassifier(), x, y);
          

          Evaluation eval = new Evaluation(data);
          eval.crossValidateModel(classifier, data, cv, new Random(getSeed()), new Object[0]);
          performances.add(new Performance(values, eval));
          

          m_Cache.add(cv, new Performance(values, eval));
        }
        
        log("" + performances.get(performances.size() - 1) + ": cached=" + cached);
      }
    }
    
    if (allCached) {
      log("All points were already cached - abnormal state!");
      throw new IllegalStateException("All points were already cached - abnormal state!");
    }
    

    Collections.sort(performances, new PerformanceComparator(m_Evaluation));
    
    PointDouble result = ((Performance)performances.get(performances.size() - 1)).getValues();
    

    m_UniformPerformance = true;
    Performance p1 = (Performance)performances.get(0);
    for (i = 1; i < performances.size(); i++) {
      Performance p2 = (Performance)performances.get(i);
      if (p2.getPerformance(m_Evaluation) != p1.getPerformance(m_Evaluation)) {
        m_UniformPerformance = false;
        break;
      }
    }
    if (m_UniformPerformance) {
      log("All performances are the same!");
    }
    logPerformances(grid, performances);
    log("\nBest performance:\n" + performances.get(performances.size() - 1));
    
    return result;
  }
  













  protected PointDouble findBest()
    throws Exception
  {
    log("Step 1:\n");
    Instances sample;
    Instances sample;
    if (getSampleSizePercent() == 100.0D) {
      sample = m_Data;
    }
    else {
      log("Generating sample (" + getSampleSizePercent() + "%)");
      Resample resample = new Resample();
      resample.setRandomSeed(getSeed());
      resample.setSampleSizePercent(getSampleSizePercent());
      resample.setInputFormat(m_Data);
      sample = Filter.useFilter(m_Data, resample);
    }
    
    boolean finished = false;
    int iteration = 0;
    m_GridExtensionsPerformed = 0;
    m_UniformPerformance = false;
    

    log("\n=== Initial grid - Start ===");
    PointDouble result = determineBestInGrid(m_Grid, sample, 2);
    log("\nResult of Step 1: " + result + "\n");
    log("=== Initial grid - End ===\n");
    
    finished = m_UniformPerformance;
    
    if (!finished) {
      do {
        iteration++;
        PointDouble resultOld = (PointDouble)result.clone();
        PointInt center = m_Grid.getLocation(result);
        
        if (m_Grid.isOnBorder(center)) {
          log("Center is on border of grid.");
          

          if (getGridIsExtendable())
          {
            if (m_GridExtensionsPerformed == getMaxGridExtensions()) {
              log("Maximum number of extensions reached!\n");
              finished = true;
            }
            else {
              m_GridExtensionsPerformed += 1;
              m_Grid = m_Grid.extend(result);
              center = m_Grid.getLocation(result);
              log("Extending grid (" + m_GridExtensionsPerformed + "/" + getMaxGridExtensions() + "):\n" + m_Grid + "\n");
            }
            
          }
          else {
            finished = true;
          }
        }
        


        if (!finished) {
          Grid neighborGrid = m_Grid.subgrid((int)center.getY() + 1, (int)center.getX() - 1, (int)center.getY() - 1, (int)center.getX() + 1);
          

          result = determineBestInGrid(neighborGrid, sample, 10);
          log("\nResult of Step 2/Iteration " + iteration + ":\n" + result);
          finished = m_UniformPerformance;
          

          if (result.equals(resultOld)) {
            finished = true;
            log("\nNo better point found.");
          }
          
        }
      } while (!finished);
    }
    
    log("\nFinal result: " + result);
    
    return result;
  }
  










  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    m_Data = new Instances(data);
    m_Data.deleteWithMissingClass();
    
    m_Cache = new PerformanceCache();
    String strX;
    String strX; if (getXProperty().startsWith("filter.")) {
      strX = m_Filter.getClass().getName();
    } else
      strX = m_Classifier.getClass().getName();
    String strY;
    String strY; if (getYProperty().startsWith("classifier.")) {
      strY = m_Classifier.getClass().getName();
    } else {
      strY = m_Filter.getClass().getName();
    }
    m_Grid = new Grid(getXMin(), getXMax(), getXStep(), strX + ", property " + getXProperty() + ", expr. " + getXExpression() + ", base " + getXBase(), getYMin(), getYMax(), getYStep(), strY + ", property " + getYProperty() + ", expr. " + getYExpression() + ", base " + getYBase());
    



    log("\n" + getClass().getName() + "\n" + getClass().getName().replaceAll(".", "=") + "\n" + "Options: " + Utils.joinOptions(getOptions()) + "\n");
    




    m_Values = findBest();
    

    double x = evaluate(m_Values.getX(), true);
    double y = evaluate(m_Values.getY(), false);
    m_BestFilter = ((Filter)setup(getFilter(), x, y));
    m_BestClassifier = ((Classifier)setup(getClassifier(), x, y));
    

    m_Filter = ((Filter)setup(getFilter(), x, y));
    m_Filter.setInputFormat(m_Data);
    Instances transformed = Filter.useFilter(m_Data, m_Filter);
    

    m_Classifier = ((Classifier)setup(getClassifier(), x, y));
    m_Classifier.buildClassifier(transformed);
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    m_Filter.input(instance);
    m_Filter.batchFinished();
    Instance transformed = m_Filter.output();
    
    return m_Classifier.distributionForInstance(transformed);
  }
  






  public String toString()
  {
    String result = "";
    
    if (m_Values == null) {
      result = "No search performed yet.";
    }
    else {
      result = getClass().getName() + ":\n" + "Filter: " + getFilter().getClass().getName() + ((getFilter() instanceof OptionHandler) ? " " + Utils.joinOptions(((OptionHandler)getFilter()).getOptions()) : "") + "\n" + "Classifier: " + getClassifier().getClass().getName() + " " + Utils.joinOptions(getClassifier().getOptions()) + "\n\n" + "X property: " + getXProperty() + "\n" + "Y property: " + getYProperty() + "\n\n" + "Evaluation: " + getEvaluation().getSelectedTag().getReadable() + "\n" + "Coordinates: " + getValues() + "\n";
      









      if (getGridIsExtendable()) {
        result = result + "Grid-Extensions: " + getGridExtensionsPerformed() + "\n";
      }
      result = result + "Values: " + evaluate(getValues().getX(), true) + " (X coordinate)" + ", " + evaluate(getValues().getY(), false) + " (Y coordinate)" + "\n\n" + m_Classifier.toString();
    }
    






    return result;
  }
  






  public String toSummaryString()
  {
    String result = "Best filter: " + getBestFilter().getClass().getName() + ((getBestFilter() instanceof OptionHandler) ? " " + Utils.joinOptions(((OptionHandler)getBestFilter()).getOptions()) : "") + "\n" + "Best classifier: " + getBestClassifier().getClass().getName() + " " + Utils.joinOptions(getBestClassifier().getOptions());
    




    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9733 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new GridSearch(), args);
  }
}
