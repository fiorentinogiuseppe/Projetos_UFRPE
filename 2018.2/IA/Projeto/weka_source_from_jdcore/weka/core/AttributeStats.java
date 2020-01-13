package weka.core;

import java.io.Serializable;
import weka.experiment.Stats;
































public class AttributeStats
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 4434688832743939380L;
  public int intCount = 0;
  

  public int realCount = 0;
  

  public int missingCount = 0;
  

  public int distinctCount = 0;
  

  public int uniqueCount = 0;
  

  public int totalCount = 0;
  


  public Stats numericStats;
  

  public int[] nominalCounts;
  


  public AttributeStats() {}
  


  protected void addDistinct(double value, int count)
  {
    if (count > 0) {
      if (count == 1) {
        uniqueCount += 1;
      }
      if (Utils.eq(value, (int)value)) {
        intCount += count;
      } else {
        realCount += count;
      }
      if (nominalCounts != null) {
        nominalCounts[((int)value)] = count;
      }
      if (numericStats != null) {
        numericStats.add(value, count);
        numericStats.calculateDerived();
      }
    }
    distinctCount += 1;
  }
  





  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(Utils.padLeft("Type", 4)).append(Utils.padLeft("Nom", 5));
    sb.append(Utils.padLeft("Int", 5)).append(Utils.padLeft("Real", 5));
    sb.append(Utils.padLeft("Missing", 12));
    sb.append(Utils.padLeft("Unique", 12));
    sb.append(Utils.padLeft("Dist", 6));
    if (nominalCounts != null) {
      sb.append(' ');
      for (int i = 0; i < nominalCounts.length; i++) {
        sb.append(Utils.padLeft("C[" + i + "]", 5));
      }
    }
    sb.append('\n');
    

    long percent = Math.round(100.0D * intCount / totalCount);
    if (nominalCounts != null) {
      sb.append(Utils.padLeft("Nom", 4)).append(' ');
      sb.append(Utils.padLeft("" + percent, 3)).append("% ");
      sb.append(Utils.padLeft("0", 3)).append("% ");
    } else {
      sb.append(Utils.padLeft("Num", 4)).append(' ');
      sb.append(Utils.padLeft("0", 3)).append("% ");
      sb.append(Utils.padLeft("" + percent, 3)).append("% ");
    }
    percent = Math.round(100.0D * realCount / totalCount);
    sb.append(Utils.padLeft("" + percent, 3)).append("% ");
    sb.append(Utils.padLeft("" + missingCount, 5)).append(" /");
    percent = Math.round(100.0D * missingCount / totalCount);
    sb.append(Utils.padLeft("" + percent, 3)).append("% ");
    sb.append(Utils.padLeft("" + uniqueCount, 5)).append(" /");
    percent = Math.round(100.0D * uniqueCount / totalCount);
    sb.append(Utils.padLeft("" + percent, 3)).append("% ");
    sb.append(Utils.padLeft("" + distinctCount, 5)).append(' ');
    if (nominalCounts != null) {
      for (int i = 0; i < nominalCounts.length; i++) {
        sb.append(Utils.padLeft("" + nominalCounts[i], 5));
      }
    }
    sb.append('\n');
    return sb.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
}
