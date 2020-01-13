package weka.associations.tertius;

import weka.core.RevisionUtils;
































public class IndividualLiteral
  extends AttributeValueLiteral
{
  private static final long serialVersionUID = 4712404824517887435L;
  private int m_type;
  public static int INDIVIDUAL_PROPERTY = 0;
  public static int PART_PROPERTY = 1;
  

  public IndividualLiteral(Predicate predicate, String value, int index, int sign, int missing, int type)
  {
    super(predicate, value, index, sign, missing);
    m_type = type;
  }
  
  public int getType()
  {
    return m_type;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}
