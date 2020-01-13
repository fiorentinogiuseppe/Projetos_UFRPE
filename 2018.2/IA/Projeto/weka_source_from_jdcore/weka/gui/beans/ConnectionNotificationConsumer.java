package weka.gui.beans;

public abstract interface ConnectionNotificationConsumer
{
  public abstract void connectionNotification(String paramString, Object paramObject);
  
  public abstract void disconnectionNotification(String paramString, Object paramObject);
}
