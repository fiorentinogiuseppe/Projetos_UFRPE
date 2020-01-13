package weka.associations.tertius;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

































public class IndividualInstances
  extends Instances
{
  private static final long serialVersionUID = -7355054814895636733L;
  
  public IndividualInstances(Instances individuals, Instances parts)
    throws Exception
  {
    super(individuals, individuals.numInstances());
    
    Attribute individualIdentifier = attribute("id");
    if (individualIdentifier == null) {
      throw new Exception("No identifier found in individuals dataset.");
    }
    Attribute partIdentifier = parts.attribute("id");
    if (partIdentifier == null) {
      throw new Exception("No identifier found in parts dataset.");
    }
    
    Enumeration enumIndividuals = individuals.enumerateInstances();
    while (enumIndividuals.hasMoreElements()) {
      Instance individual = (Instance)enumIndividuals.nextElement();
      Instances partsOfIndividual = new Instances(parts, 0);
      Enumeration enumParts = parts.enumerateInstances();
      while (enumParts.hasMoreElements()) {
        Instance part = (Instance)enumParts.nextElement();
        if (individual.value(individualIdentifier) == part.value(partIdentifier))
        {
          partsOfIndividual.add(part);
        }
      }
      add(new IndividualInstance(individual, partsOfIndividual));
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
