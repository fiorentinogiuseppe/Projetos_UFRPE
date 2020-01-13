package java_cup.runtime;





public class ComplexSymbolFactory
  implements SymbolFactory
{
  public ComplexSymbolFactory() {}
  




  public static class Location
  {
    private String unit = "unknown";
    private int line;
    
    public Location(String unit, int line, int column) { this.unit = unit;
      this.line = line;
      this.column = column; }
    
    private int column;
    public Location(int line, int column) { this.line = line;
      this.column = column;
    }
    
    public String toString() { return unit + ":" + line + "/" + column; }
    
    public int getColumn() {
      return column;
    }
    
    public int getLine() { return line; }
    
    public String getUnit() {
      return unit;
    }
  }
  
  public static class ComplexSymbol extends Symbol {
    protected String name;
    protected ComplexSymbolFactory.Location xleft;
    protected ComplexSymbolFactory.Location xright;
    
    public ComplexSymbol(String name, int id) {
      super();
      this.name = name;
    }
    
    public ComplexSymbol(String name, int id, Object value) { super(value);
      this.name = name;
    }
    
    public String toString() { if ((xleft == null) || (xright == null)) return "Symbol: " + name;
      return "Symbol: " + name + " (" + xleft + " - " + xright + ")";
    }
    
    public ComplexSymbol(String name, int id, int state) { super(state);
      this.name = name;
    }
    
    public ComplexSymbol(String name, int id, Symbol left, Symbol right) { super(left, right);
      this.name = name;
      if (left != null) xleft = xleft;
      if (right != null) xright = xright;
    }
    
    public ComplexSymbol(String name, int id, ComplexSymbolFactory.Location left, ComplexSymbolFactory.Location right) { super();
      this.name = name;
      xleft = left;
      xright = right;
    }
    
    public ComplexSymbol(String name, int id, Symbol left, Symbol right, Object value) { super(value);
      this.name = name;
      if (left != null) xleft = xleft;
      if (right != null) xright = xright;
    }
    
    public ComplexSymbol(String name, int id, ComplexSymbolFactory.Location left, ComplexSymbolFactory.Location right, Object value) { super(value);
      this.name = name;
      xleft = left;
      xright = right;
    }
    
    public ComplexSymbolFactory.Location getLeft() { return xleft; }
    
    public ComplexSymbolFactory.Location getRight() {
      return xright;
    }
  }
  

  public Symbol newSymbol(String name, int id, Location left, Location right, Object value)
  {
    return new ComplexSymbol(name, id, left, right, value);
  }
  
  public Symbol newSymbol(String name, int id, Location left, Location right) { return new ComplexSymbol(name, id, left, right); }
  
  public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value) {
    return new ComplexSymbol(name, id, left, right, value);
  }
  
  public Symbol newSymbol(String name, int id, Symbol left, Symbol right) { return new ComplexSymbol(name, id, left, right); }
  
  public Symbol newSymbol(String name, int id) {
    return new ComplexSymbol(name, id);
  }
  
  public Symbol newSymbol(String name, int id, Object value) { return new ComplexSymbol(name, id, value); }
  
  public Symbol startSymbol(String name, int id, int state) {
    return new ComplexSymbol(name, id, state);
  }
}
