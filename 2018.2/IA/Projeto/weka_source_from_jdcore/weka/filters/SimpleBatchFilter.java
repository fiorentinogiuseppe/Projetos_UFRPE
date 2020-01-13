package weka.filters;

import weka.core.Instance;
import weka.core.Instances;





















































































































































public abstract class SimpleBatchFilter
  extends SimpleFilter
{
  private static final long serialVersionUID = 8102908673378055114L;
  
  public SimpleBatchFilter() {}
  
  protected boolean hasImmediateOutputFormat()
  {
    return false;
  }
  











  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    bufferInput((Instance)instance.copy());
    
    if (isFirstBatchDone()) {
      Instances inst = new Instances(getInputFormat());
      inst = process(inst);
      for (int i = 0; i < inst.numInstances(); i++)
        push(inst.instance(i));
      flushInput();
    }
    
    return m_FirstBatchDone;
  }
  
















  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    Instances inst = new Instances(getInputFormat());
    

    if ((!hasImmediateOutputFormat()) && (!isFirstBatchDone())) {
      setOutputFormat(determineOutputFormat(new Instances(inst, 0)));
    }
    


    if (inst.numInstances() > 0)
    {
      inst = process(inst);
      

      flushInput();
      

      for (int i = 0; i < inst.numInstances(); i++) {
        push(inst.instance(i));
      }
    }
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
}
