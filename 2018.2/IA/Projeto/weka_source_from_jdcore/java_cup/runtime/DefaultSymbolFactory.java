package java_cup.runtime;











public class DefaultSymbolFactory
  implements SymbolFactory
{
  /**
   * @deprecated
   */
  public DefaultSymbolFactory() {}
  









  public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value)
  {
    return new Symbol(id, left, right, value);
  }
  
  public Symbol newSymbol(String name, int id, Symbol left, Symbol right) { return new Symbol(id, left, right); }
  
  public Symbol newSymbol(String name, int id, int left, int right, Object value) {
    return new Symbol(id, left, right, value);
  }
  
  public Symbol newSymbol(String name, int id, int left, int right) { return new Symbol(id, left, right); }
  
  public Symbol startSymbol(String name, int id, int state) {
    return new Symbol(id, state);
  }
  
  public Symbol newSymbol(String name, int id) { return new Symbol(id); }
  
  public Symbol newSymbol(String name, int id, Object value) {
    return new Symbol(id, value);
  }
}
