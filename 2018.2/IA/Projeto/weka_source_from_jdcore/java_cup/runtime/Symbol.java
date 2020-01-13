package java_cup.runtime;









public class Symbol
{
  public int sym;
  






  public int parse_state;
  







  public Symbol(int id, Symbol left, Symbol right, Object o)
  {
    this(id, left, right, o);
  }
  
  public Symbol(int id, Symbol left, Symbol right) { this(id, left, right); }
  



  public Symbol(int id, int l, int r, Object o)
  {
    this(id);
    left = l;
    right = r;
    value = o;
  }
  



  public Symbol(int id, Object o)
  {
    this(id, -1, -1, o);
  }
  



  public Symbol(int id, int l, int r)
  {
    this(id, l, r, null);
  }
  



  public Symbol(int sym_num)
  {
    this(sym_num, -1);
    left = -1;
    right = -1;
  }
  



  Symbol(int sym_num, int state)
  {
    sym = sym_num;
    parse_state = state;
  }
  














  boolean used_by_parser = false;
  

  public int left;
  
  public int right;
  
  public Object value;
  

  public String toString()
  {
    return "#" + sym;
  }
}
