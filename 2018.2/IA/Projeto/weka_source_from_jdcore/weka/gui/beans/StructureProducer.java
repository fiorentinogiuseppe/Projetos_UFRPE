package weka.gui.beans;

import weka.core.Instances;

public abstract interface StructureProducer
{
  public abstract Instances getStructure(String paramString);
}
