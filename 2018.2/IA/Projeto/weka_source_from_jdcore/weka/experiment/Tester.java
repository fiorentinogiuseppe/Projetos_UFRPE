package weka.experiment;

import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;

public abstract interface Tester
  extends Serializable
{
  public abstract String getDisplayName();
  
  public abstract String getToolTipText();
  
  public abstract void assign(Tester paramTester);
  
  public abstract void setResultMatrix(ResultMatrix paramResultMatrix);
  
  public abstract ResultMatrix getResultMatrix();
  
  public abstract void setShowStdDevs(boolean paramBoolean);
  
  public abstract boolean getShowStdDevs();
  
  public abstract int getNumDatasets();
  
  public abstract int getNumResultsets();
  
  public abstract String getResultsetName(int paramInt);
  
  public abstract boolean displayResultset(int paramInt);
  
  public abstract PairedStats calculateStatistics(Instance paramInstance, int paramInt1, int paramInt2, int paramInt3)
    throws Exception;
  
  public abstract String resultsetKey();
  
  public abstract String header(int paramInt);
  
  public abstract int[][] multiResultsetWins(int paramInt, int[][] paramArrayOfInt)
    throws Exception;
  
  public abstract String multiResultsetSummary(int paramInt)
    throws Exception;
  
  public abstract String multiResultsetRanking(int paramInt)
    throws Exception;
  
  public abstract String multiResultsetFull(int paramInt1, int paramInt2)
    throws Exception;
  
  public abstract Range getResultsetKeyColumns();
  
  public abstract void setResultsetKeyColumns(Range paramRange);
  
  public abstract int[] getDisplayedResultsets();
  
  public abstract void setDisplayedResultsets(int[] paramArrayOfInt);
  
  public abstract double getSignificanceLevel();
  
  public abstract void setSignificanceLevel(double paramDouble);
  
  public abstract Range getDatasetKeyColumns();
  
  public abstract void setDatasetKeyColumns(Range paramRange);
  
  public abstract int getRunColumn();
  
  public abstract void setRunColumn(int paramInt);
  
  public abstract int getFoldColumn();
  
  public abstract void setFoldColumn(int paramInt);
  
  public abstract String getSortColumnName();
  
  public abstract int getSortColumn();
  
  public abstract void setSortColumn(int paramInt);
  
  public abstract Instances getInstances();
  
  public abstract void setInstances(Instances paramInstances);
}
