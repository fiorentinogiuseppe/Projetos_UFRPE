package weka.core;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java_cup.runtime.DefaultSymbolFactory;
import java_cup.runtime.SymbolFactory;
import weka.core.mathematicalexpression.Parser;
import weka.core.mathematicalexpression.Scanner;














































































































public class MathematicalExpression
  implements RevisionHandler
{
  public MathematicalExpression() {}
  
  public static double evaluate(String expr, HashMap symbols)
    throws Exception
  {
    SymbolFactory sf = new DefaultSymbolFactory();
    ByteArrayInputStream parserInput = new ByteArrayInputStream(expr.getBytes());
    Parser parser = new Parser(new Scanner(parserInput, sf), sf);
    parser.setSymbols(symbols);
    parser.parse();
    
    return parser.getResult().doubleValue();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4942 $");
  }
}
