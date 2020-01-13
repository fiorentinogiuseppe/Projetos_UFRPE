package weka.classifiers.rules;

import weka.classifiers.trees.m5.M5Base;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
































































































public class M5Rules
  extends M5Base
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -1746114858746563180L;
  
  public String globalInfo()
  {
    return "Generates a decision list for regression problems using separate-and-conquer. In each iteration it builds a model tree using M5 and makes the \"best\" leaf into a rule.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  








  public M5Rules()
  {
    setGenerateRules(true);
  }
  








  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Geoffrey Holmes and Mark Hall and Eibe Frank");
    result.setValue(TechnicalInformation.Field.TITLE, "Generating Rule Sets from Model Trees");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Twelfth Australian Joint Conference on Artificial Intelligence");
    result.setValue(TechnicalInformation.Field.YEAR, "1999");
    result.setValue(TechnicalInformation.Field.PAGES, "1-12");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    result.add(super.getTechnicalInformation());
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new M5Rules(), args);
  }
}
