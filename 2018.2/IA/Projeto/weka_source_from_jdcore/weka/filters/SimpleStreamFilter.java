package weka.filters;

import weka.core.Instance;
import weka.core.Instances;






















































































































































public abstract class SimpleStreamFilter
  extends SimpleFilter
  implements StreamableFilter
{
  private static final long serialVersionUID = 2754882676192747091L;
  
  public SimpleStreamFilter() {}
  
  protected boolean hasImmediateOutputFormat()
  {
    return true;
  }
  











  protected abstract Instances determineOutputFormat(Instances paramInstances)
    throws Exception;
  











  protected abstract Instance process(Instance paramInstance)
    throws Exception;
  











  protected Instances process(Instances instances)
    throws Exception
  {
    Instances result = new Instances(getOutputFormat(), 0);
    
    for (int i = 0; i < instances.numInstances(); i++) {
      result.add(process(instances.instance(i)));
    }
    return result;
  }
  









  protected void preprocess(Instances instances) {}
  









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
    try
    {
      if ((hasImmediateOutputFormat()) || (isFirstBatchDone())) {
        Instance processed = process((Instance)instance.copy());
        if (processed != null)
        {
          push(processed);
          return true;
        }
        return false;
      }
      
      bufferInput(instance);
      return false;
    }
    catch (Exception e) {}
    
    return false;
  }
  













  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    Instances inst = new Instances(getInputFormat());
    flushInput();
    
    if (!hasImmediateOutputFormat()) {
      preprocess(inst);
    }
    
    inst = process(inst);
    

    if ((!hasImmediateOutputFormat()) && (!isFirstBatchDone())) {
      setOutputFormat(inst);
    }
    
    for (int i = 0; i < inst.numInstances(); i++) {
      push(inst.instance(i));
    }
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
}
