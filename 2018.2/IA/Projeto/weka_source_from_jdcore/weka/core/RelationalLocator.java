package weka.core;



















public class RelationalLocator
  extends AttributeLocator
{
  private static final long serialVersionUID = 4646872277151854732L;
  

















  public RelationalLocator(Instances data)
  {
    super(data, 4);
  }
  







  public RelationalLocator(Instances data, int fromIndex, int toIndex)
  {
    super(data, 4, fromIndex, toIndex);
  }
  






  public RelationalLocator(Instances data, int[] indices)
  {
    super(data, 4, indices);
  }
  












  public static void copyRelationalValues(Instance inst, Instances destDataset, AttributeLocator strAtts)
  {
    if (inst.dataset() == null)
      throw new IllegalArgumentException("Instance has no dataset assigned!!");
    if (inst.dataset().numAttributes() != destDataset.numAttributes()) {
      throw new IllegalArgumentException("Src and Dest differ in # of attributes: " + inst.dataset().numAttributes() + " != " + destDataset.numAttributes());
    }
    

    copyRelationalValues(inst, true, inst.dataset(), strAtts, destDataset, strAtts);
  }
  































  public static void copyRelationalValues(Instance instance, boolean instSrcCompat, Instances srcDataset, AttributeLocator srcLoc, Instances destDataset, AttributeLocator destLoc)
  {
    if (srcDataset == destDataset) {
      return;
    }
    if (srcLoc.getAttributeIndices().length != destLoc.getAttributeIndices().length) {
      throw new IllegalArgumentException("Src and Dest relational indices differ in length: " + srcLoc.getAttributeIndices().length + " != " + destLoc.getAttributeIndices().length);
    }
    

    if (srcLoc.getLocatorIndices().length != destLoc.getLocatorIndices().length) {
      throw new IllegalArgumentException("Src and Dest locator indices differ in length: " + srcLoc.getLocatorIndices().length + " != " + destLoc.getLocatorIndices().length);
    }
    

    for (int i = 0; i < srcLoc.getAttributeIndices().length; i++) {
      int instIndex = instSrcCompat ? srcLoc.getActualIndex(srcLoc.getAttributeIndices()[i]) : destLoc.getActualIndex(destLoc.getAttributeIndices()[i]);
      

      Attribute src = srcDataset.attribute(srcLoc.getActualIndex(srcLoc.getAttributeIndices()[i]));
      Attribute dest = destDataset.attribute(destLoc.getActualIndex(destLoc.getAttributeIndices()[i]));
      if (!instance.isMissing(instIndex)) {
        int valIndex = dest.addRelation(src.relation((int)instance.value(instIndex)));
        instance.setValue(instIndex, valIndex);
      }
    }
    

    int[] srcIndices = srcLoc.getLocatorIndices();
    int[] destIndices = destLoc.getLocatorIndices();
    for (int i = 0; i < srcIndices.length; i++) {
      int index = instSrcCompat ? srcLoc.getActualIndex(srcIndices[i]) : destLoc.getActualIndex(destIndices[i]);
      

      if (!instance.isMissing(index))
      {
        Instances rel = instSrcCompat ? instance.relationalValue(index) : instance.relationalValue(index);
        

        AttributeLocator srcRelAttsNew = srcLoc.getLocator(srcIndices[i]);
        Instances srcDatasetNew = srcRelAttsNew.getData();
        AttributeLocator destRelAttsNew = destLoc.getLocator(destIndices[i]);
        Instances destDatasetNew = destRelAttsNew.getData();
        for (int n = 0; n < rel.numInstances(); n++) {
          copyRelationalValues(rel.instance(n), instSrcCompat, srcDatasetNew, srcRelAttsNew, destDatasetNew, destRelAttsNew);
        }
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6226 $");
  }
}
