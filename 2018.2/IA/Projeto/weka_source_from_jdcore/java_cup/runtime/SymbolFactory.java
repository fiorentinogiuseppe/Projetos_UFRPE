package java_cup.runtime;

public abstract interface SymbolFactory
{
  public abstract Symbol newSymbol(String paramString, int paramInt, Symbol paramSymbol1, Symbol paramSymbol2, Object paramObject);
  
  public abstract Symbol newSymbol(String paramString, int paramInt, Symbol paramSymbol1, Symbol paramSymbol2);
  
  public abstract Symbol newSymbol(String paramString, int paramInt, Object paramObject);
  
  public abstract Symbol newSymbol(String paramString, int paramInt);
  
  public abstract Symbol startSymbol(String paramString, int paramInt1, int paramInt2);
}
