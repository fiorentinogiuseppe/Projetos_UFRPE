package weka.gui.beans;

public abstract interface DataSource
{
  public abstract void addDataSourceListener(DataSourceListener paramDataSourceListener);
  
  public abstract void removeDataSourceListener(DataSourceListener paramDataSourceListener);
  
  public abstract void addInstanceListener(InstanceListener paramInstanceListener);
  
  public abstract void removeInstanceListener(InstanceListener paramInstanceListener);
}
