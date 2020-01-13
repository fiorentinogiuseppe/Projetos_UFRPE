package weka.core;




















public class StringLocator
  extends AttributeLocator
{
  private static final long serialVersionUID = 7805522230268783972L;
  


















  public StringLocator(Instances data)
  {
    super(data, 2);
  }
  







  public StringLocator(Instances data, int fromIndex, int toIndex)
  {
    super(data, 2, fromIndex, toIndex);
  }
  






  public StringLocator(Instances data, int[] indices)
  {
    super(data, 2, indices);
  }
  











  public static void copyStringValues(Instance inst, Instances destDataset, AttributeLocator strAtts)
  {
    if (inst.dataset() == null)
      throw new IllegalArgumentException("Instance has no dataset assigned!!");
    if (inst.dataset().numAttributes() != destDataset.numAttributes()) {
      throw new IllegalArgumentException("Src and Dest differ in # of attributes: " + inst.dataset().numAttributes() + " != " + destDataset.numAttributes());
    }
    

    copyStringValues(inst, true, inst.dataset(), strAtts, destDataset, strAtts);
  }
  






























  public static void copyStringValues(Instance instance, boolean instSrcCompat, Instances srcDataset, AttributeLocator srcLoc, Instances destDataset, AttributeLocator destLoc)
  {
    if (srcDataset == destDataset) {
      return;
    }
    if (srcLoc.getAttributeIndices().length != destLoc.getAttributeIndices().length) {
      throw new IllegalArgumentException("Src and Dest string indices differ in length: " + srcLoc.getAttributeIndices().length + " != " + destLoc.getAttributeIndices().length);
    }
    

    if (srcLoc.getLocatorIndices().length != destLoc.getLocatorIndices().length) {
      throw new IllegalArgumentException("Src and Dest locator indices differ in length: " + srcLoc.getLocatorIndices().length + " != " + destLoc.getLocatorIndices().length);
    }
    

    for (int i = 0; i < srcLoc.getAttributeIndices().length; i++) {
      int instIndex = instSrcCompat ? srcLoc.getActualIndex(srcLoc.getAttributeIndices()[i]) : destLoc.getActualIndex(destLoc.getAttributeIndices()[i]);
      

      Attribute src = srcDataset.attribute(srcLoc.getActualIndex(srcLoc.getAttributeIndices()[i]));
      Attribute dest = destDataset.attribute(destLoc.getActualIndex(destLoc.getAttributeIndices()[i]));
      if (!instance.isMissing(instIndex)) {
        int valIndex = dest.addStringValue(src, (int)instance.value(instIndex));
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
        

        AttributeLocator srcStrAttsNew = srcLoc.getLocator(srcIndices[i]);
        Instances srcDatasetNew = srcStrAttsNew.getData();
        AttributeLocator destStrAttsNew = destLoc.getLocator(destIndices[i]);
        Instances destDatasetNew = destStrAttsNew.getData();
        for (int n = 0; n < rel.numInstances(); n++) {
          copyStringValues(rel.instance(n), instSrcCompat, srcDatasetNew, srcStrAttsNew, destDatasetNew, destStrAttsNew);
        }
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6226 $");
  }
}
