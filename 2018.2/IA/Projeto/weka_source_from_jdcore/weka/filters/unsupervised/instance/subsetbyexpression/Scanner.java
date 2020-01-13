package weka.filters.unsupervised.instance.subsetbyexpression;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;






































public class Scanner
  implements java_cup.runtime.Scanner
{
  public static final int YYEOF = -1;
  private static final int ZZ_BUFFERSIZE = 16384;
  public static final int STRING = 2;
  public static final int YYINITIAL = 0;
  private static final int[] ZZ_LEXSTATE = { 0, 0, 1, 1 };
  







  private static final String ZZ_CMAP_PACKED = "\t\000\001#\001(\001\000\002#\022\000\001#\006\000\001\033\001%\001&\001\003\001\002\001$\001\001\001\035\001\004\n\034\002\000\001\005\001\006\001\007\002\000\001\036\001\000\001 \b\000\001!\006\000\001\"\001\037\f\000\001\r\001\024\001\031\001\016\001\021\001\022\001\026\001\000\001\b\002\000\001\023\001'\001\n\001\013\001\030\001\025\001\017\001\t\001\f\001\020\001\000\001\032\001\027ﾇ\000";
  







  private static final char[] ZZ_CMAP = zzUnpackCMap("\t\000\001#\001(\001\000\002#\022\000\001#\006\000\001\033\001%\001&\001\003\001\002\001$\001\001\001\035\001\004\n\034\002\000\001\005\001\006\001\007\002\000\001\036\001\000\001 \b\000\001!\006\000\001\"\001\037\f\000\001\r\001\024\001\031\001\016\001\021\001\022\001\026\001\000\001\b\002\000\001\023\001'\001\n\001\013\001\030\001\025\001\017\001\t\001\f\001\020\001\000\001\032\001\027ﾇ\000");
  



  private static final int[] ZZ_ACTION = zzUnpackAction();
  



  private static final String ZZ_ACTION_PACKED_0 = "\002\000\001\001\001\002\001\003\001\004\001\005\001\006\001\007\001\b\f\001\001\t\001\n\002\001\001\013\001\f\001\r\001\016\001\017\001\020\001\021\001\022\001\023\003\000\001\024\f\000\001\n\003\000\001\025\001\000\001\026\001\027\001\000\001\030\001\031\001\000\001\032\002\000\001\033\001\034\001\035\004\000\001\036\001\037\001 \002\000\001!\001\"\002\000\001#\001$\001%\003\000\001&";
  



  private static int[] zzUnpackAction()
  {
    int[] result = new int[89];
    int offset = 0;
    offset = zzUnpackAction("\002\000\001\001\001\002\001\003\001\004\001\005\001\006\001\007\001\b\f\001\001\t\001\n\002\001\001\013\001\f\001\r\001\016\001\017\001\020\001\021\001\022\001\023\003\000\001\024\f\000\001\n\003\000\001\025\001\000\001\026\001\027\001\000\001\030\001\031\001\000\001\032\002\000\001\033\001\034\001\035\004\000\001\036\001\037\001 \002\000\001!\001\"\002\000\001#\001$\001%\003\000\001&", offset, result);
    return result;
  }
  
  private static int zzUnpackAction(String packed, int offset, int[] result) {
    int i = 0;
    int j = offset;
    int l = packed.length();
    int count; for (; i < l; 
        

        count > 0)
    {
      count = packed.charAt(i++);
      int value = packed.charAt(i++);
      result[(j++)] = value;count--;
    }
    return j;
  }
  




  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();
  





  private static final String ZZ_ROWMAP_PACKED_0 = "\000\000\000)\000R\000{\000R\000R\000R\000¤\000R\000Í\000ö\000ğ\000ň\000ű\000ƚ\000ǃ\000Ǭ\000ȕ\000Ⱦ\000ɧ\000ʐ\000ʹ\000R\000ˢ\000̋\000̴\000R\000R\000R\000R\000R\000R\000R\000R\000͝\000Ά\000ί\000Ϙ\000R\000Ё\000Ъ\000ѓ\000Ѽ\000ҥ\000ӎ\000ӷ\000Ԡ\000Չ\000ղ\000֛\000ׄ\000׭\000ؖ\000ؿ\000٨\000R\000ڑ\000R\000R\000ں\000R\000R\000ۣ\000R\000܌\000ܵ\000R\000R\000R\000ݞ\000އ\000ް\000ߙ\000R\000R\000R\000ࠂ\000ࠫ\000R\000އ\000ࡔ\000ࡽ\000R\000R\000R\000ࢦ\000࣏\000ࣸ\000R";
  






  private static int[] zzUnpackRowMap()
  {
    int[] result = new int[89];
    int offset = 0;
    offset = zzUnpackRowMap("\000\000\000)\000R\000{\000R\000R\000R\000¤\000R\000Í\000ö\000ğ\000ň\000ű\000ƚ\000ǃ\000Ǭ\000ȕ\000Ⱦ\000ɧ\000ʐ\000ʹ\000R\000ˢ\000̋\000̴\000R\000R\000R\000R\000R\000R\000R\000R\000͝\000Ά\000ί\000Ϙ\000R\000Ё\000Ъ\000ѓ\000Ѽ\000ҥ\000ӎ\000ӷ\000Ԡ\000Չ\000ղ\000֛\000ׄ\000׭\000ؖ\000ؿ\000٨\000R\000ڑ\000R\000R\000ں\000R\000R\000ۣ\000R\000܌\000ܵ\000R\000R\000R\000ݞ\000އ\000ް\000ߙ\000R\000R\000R\000ࠂ\000ࠫ\000R\000އ\000ࡔ\000ࡽ\000R\000R\000R\000ࢦ\000࣏\000ࣸ\000R", offset, result);
    return result;
  }
  
  private static int zzUnpackRowMap(String packed, int offset, int[] result) {
    int i = 0;
    int j = offset;
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << '\020';
      result[(j++)] = (high | packed.charAt(i++));
    }
    return j;
  }
  



  private static final int[] ZZ_TRANS = zzUnpackTrans();
  



  private static final String ZZ_TRANS_PACKED_0 = "\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\001\f\001\r\001\016\001\017\001\020\001\003\001\021\001\003\001\022\001\023\001\024\004\003\001\025\001\026\001\003\001\027\001\030\001\003\001\031\001\003\001\032\002\003\001\033\001\034\001\035\001\036\001\003\001\033\033\037\001 \f\037F\000\001\030\022\000\001!(\000\001\"+\000\001#'\000\001$\f\000\001%\036\000\001&,\000\001'&\000\001(\001\000\001)#\000\001*\t\000\001+\034\000\001,7\000\001-\036\000\001.\005\000\001/ \000\0010(\000\0011(\000\0012\005\000\00133\000\001\030\0014*\000\0015*\000\0016.\000\0017\013\000\0018-\000\0019%\000\001:&\000\001;.\000\001<&\000\001=#\000\001>)\000\001?6\000\001@#\000\001A \000\001B3\000\001C,\000\001D\027\000\001E'\000\001F<\000\0014+\000\001G'\000\001H\022\000\001I,\000\001J-\000\001K#\000\001L%\000\001M*\000\001N0\000\001O1\000\001P.\000\001Q\017\000\001R0\000\001S&\000\001T;\000\001U\017\000\001V'\000\001W*\000\001X4\000\001Y\022\000";
  



  private static final int ZZ_UNKNOWN_ERROR = 0;
  


  private static final int ZZ_NO_MATCH = 1;
  


  private static final int ZZ_PUSHBACK_2BIG = 2;
  



  private static int[] zzUnpackTrans()
  {
    int[] result = new int['ड'];
    int offset = 0;
    offset = zzUnpackTrans("\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\001\f\001\r\001\016\001\017\001\020\001\003\001\021\001\003\001\022\001\023\001\024\004\003\001\025\001\026\001\003\001\027\001\030\001\003\001\031\001\003\001\032\002\003\001\033\001\034\001\035\001\036\001\003\001\033\033\037\001 \f\037F\000\001\030\022\000\001!(\000\001\"+\000\001#'\000\001$\f\000\001%\036\000\001&,\000\001'&\000\001(\001\000\001)#\000\001*\t\000\001+\034\000\001,7\000\001-\036\000\001.\005\000\001/ \000\0010(\000\0011(\000\0012\005\000\00133\000\001\030\0014*\000\0015*\000\0016.\000\0017\013\000\0018-\000\0019%\000\001:&\000\001;.\000\001<&\000\001=#\000\001>)\000\001?6\000\001@#\000\001A \000\001B3\000\001C,\000\001D\027\000\001E'\000\001F<\000\0014+\000\001G'\000\001H\022\000\001I,\000\001J-\000\001K#\000\001L%\000\001M*\000\001N0\000\001O1\000\001P.\000\001Q\017\000\001R0\000\001S&\000\001T;\000\001U\017\000\001V'\000\001W*\000\001X4\000\001Y\022\000", offset, result);
    return result;
  }
  
  private static int zzUnpackTrans(String packed, int offset, int[] result) {
    int i = 0;
    int j = offset;
    int l = packed.length();
    int count; for (; i < l; 
        


        count > 0)
    {
      count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      result[(j++)] = value;count--;
    }
    return j;
  }
  







  private static final String[] ZZ_ERROR_MSG = { "Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large" };
  







  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();
  
  private static final String ZZ_ATTRIBUTE_PACKED_0 = "\002\000\001\t\001\001\003\t\001\001\001\t\r\001\001\t\003\001\b\t\001\001\003\000\001\t\f\000\001\001\003\000\001\t\001\000\002\t\001\000\002\t\001\000\001\t\002\000\003\t\004\000\003\t\002\000\001\t\001\001\002\000\003\t\003\000\001\t";
  
  private Reader zzReader;
  
  private int zzState;
  
  private static int[] zzUnpackAttribute()
  {
    int[] result = new int[89];
    int offset = 0;
    offset = zzUnpackAttribute("\002\000\001\t\001\001\003\t\001\001\001\t\r\001\001\t\003\001\b\t\001\001\003\000\001\t\f\000\001\001\003\000\001\t\001\000\002\t\001\000\002\t\001\000\001\t\002\000\003\t\004\000\003\t\002\000\001\t\001\001\002\000\003\t\003\000\001\t", offset, result);
    return result;
  }
  
  private static int zzUnpackAttribute(String packed, int offset, int[] result) {
    int i = 0;
    int j = offset;
    int l = packed.length();
    int count; for (; i < l; 
        

        count > 0)
    {
      count = packed.charAt(i++);
      int value = packed.charAt(i++);
      result[(j++)] = value;count--;
    }
    return j;
  }
  







  private int zzLexicalState = 0;
  


  private char[] zzBuffer = new char['䀀'];
  


  private int zzMarkedPos;
  


  private int zzCurrentPos;
  


  private int zzStartRead;
  


  private int zzEndRead;
  


  private int yyline;
  


  private int yychar;
  

  private int yycolumn;
  

  private boolean zzAtBOL = true;
  


  private boolean zzAtEOF;
  

  protected SymbolFactory m_SymFactory;
  

  protected StringBuffer m_String = new StringBuffer();
  
  public Scanner(InputStream r, SymbolFactory sf) {
    this(r);
    m_SymFactory = sf;
  }
  






  public Scanner(Reader in)
  {
    zzReader = in;
  }
  





  public Scanner(InputStream in)
  {
    this(new InputStreamReader(in));
  }
  





  private static char[] zzUnpackCMap(String packed)
  {
    char[] map = new char[65536];
    int i = 0;
    int j = 0;
    int count; for (; i < 112; 
        

        count > 0)
    {
      count = packed.charAt(i++);
      char value = packed.charAt(i++);
      map[(j++)] = value;count--;
    }
    return map;
  }
  








  private boolean zzRefill()
    throws IOException
  {
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead, zzBuffer, 0, zzEndRead - zzStartRead);
      



      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }
    

    if (zzCurrentPos >= zzBuffer.length)
    {
      char[] newBuffer = new char[zzCurrentPos * 2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }
    

    int numRead = zzReader.read(zzBuffer, zzEndRead, zzBuffer.length - zzEndRead);
    

    if (numRead > 0) {
      zzEndRead += numRead;
      return false;
    }
    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      }
      zzBuffer[(zzEndRead++)] = ((char)c);
      return false;
    }
    


    return true;
  }
  


  public final void yyclose()
    throws IOException
  {
    zzAtEOF = true;
    zzEndRead = zzStartRead;
    
    if (zzReader != null) {
      zzReader.close();
    }
  }
  









  public final void yyreset(Reader reader)
  {
    zzReader = reader;
    zzAtBOL = true;
    zzAtEOF = false;
    zzEndRead = (this.zzStartRead = 0);
    zzCurrentPos = (this.zzMarkedPos = 0);
    yyline = (this.yychar = this.yycolumn = 0);
    zzLexicalState = 0;
  }
  



  public final int yystate()
  {
    return zzLexicalState;
  }
  





  public final void yybegin(int newState)
  {
    zzLexicalState = newState;
  }
  



  public final String yytext()
  {
    return new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
  }
  











  public final char yycharat(int pos)
  {
    return zzBuffer[(zzStartRead + pos)];
  }
  



  public final int yylength()
  {
    return zzMarkedPos - zzStartRead;
  }
  







  private void zzScanError(int errorCode)
  {
    String message;
    





    try
    {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[0];
    }
    
    throw new Error(message);
  }
  








  public void yypushback(int number)
  {
    if (number > yylength()) {
      zzScanError(2);
    }
    zzMarkedPos -= number;
  }
  












  public Symbol next_token()
    throws IOException
  {
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzCMapL = ZZ_CMAP;
    
    int[] zzTransL = ZZ_TRANS;
    int[] zzRowMapL = ZZ_ROWMAP;
    int[] zzAttrL = ZZ_ATTRIBUTE;
    for (;;)
    {
      int zzMarkedPosL = zzMarkedPos;
      
      yychar += zzMarkedPosL - zzStartRead;
      
      int zzAction = -1;
      
      int zzCurrentPosL = this.zzCurrentPos = this.zzStartRead = zzMarkedPosL;
      
      zzState = ZZ_LEXSTATE[zzLexicalState];
      
      int zzInput;
      for (;;)
      {
        int zzInput;
        if (zzCurrentPosL < zzEndReadL) {
          zzInput = zzBufferL[(zzCurrentPosL++)];
        } else { if (zzAtEOF) {
            int zzInput = -1;
            break;
          }
          

          zzCurrentPos = zzCurrentPosL;
          zzMarkedPos = zzMarkedPosL;
          boolean eof = zzRefill();
          
          zzCurrentPosL = zzCurrentPos;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          zzEndReadL = zzEndRead;
          if (eof) {
            int zzInput = -1;
            break;
          }
          
          zzInput = zzBufferL[(zzCurrentPosL++)];
        }
        
        int zzNext = zzTransL[(zzRowMapL[zzState] + zzCMapL[zzInput])];
        if (zzNext == -1) break;
        zzState = zzNext;
        
        int zzAttributes = zzAttrL[zzState];
        if ((zzAttributes & 0x1) == 1) {
          zzAction = zzState;
          zzMarkedPosL = zzCurrentPosL;
          if ((zzAttributes & 0x8) == 8) {
            break;
          }
        }
      }
      

      zzMarkedPos = zzMarkedPosL;
      
      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
      case 9: 
        yybegin(2);m_String.setLength(0);
      case 39: 
        break;
      case 25: 
        return m_SymFactory.newSymbol("Abs", 10);
      case 40: 
        break;
      case 1: 
        System.err.println("Illegal character: " + yytext());
      case 41: 
        break;
      case 15: 
        m_String.append(yytext());
      case 42: 
        break;
      case 21: 
        return m_SymFactory.newSymbol("Sin", 14);
      case 43: 
        break;
      case 14: 
        return m_SymFactory.newSymbol("Right Bracket", 4);
      case 44: 
        break;
      case 11: 
      case 45: 
        break;
      
      case 17: 
        return m_SymFactory.newSymbol("Less or equal than", 24);
      case 46: 
        break;
      case 28: 
        return m_SymFactory.newSymbol("Pow", 19);
      case 47: 
        break;
      case 8: 
        return m_SymFactory.newSymbol("Greater than", 25);
      case 48: 
        break;
      case 16: 
        yybegin(0);return m_SymFactory.newSymbol("String", 35, new String(m_String.toString()));
      case 49: 
        break;
      case 23: 
        return m_SymFactory.newSymbol("Tan", 16);
      case 50: 
        break;
      case 12: 
        return m_SymFactory.newSymbol("Comma", 2);
      case 51: 
        break;
      case 19: 
        return m_SymFactory.newSymbol("Is", 31);
      case 52: 
        break;
      case 13: 
        return m_SymFactory.newSymbol("Left Bracket", 3);
      case 53: 
        break;
      case 29: 
        return m_SymFactory.newSymbol("Cos", 15);
      case 54: 
        break;
      case 33: 
        return m_SymFactory.newSymbol("Ceil", 20);
      case 55: 
        break;
      case 6: 
        return m_SymFactory.newSymbol("Less than", 23);
      case 56: 
        break;
      case 4: 
        return m_SymFactory.newSymbol("Times", 8);
      case 57: 
        break;
      case 37: 
        return m_SymFactory.newSymbol("Class", 34, new String(yytext()));
      case 58: 
        break;
      case 36: 
        return m_SymFactory.newSymbol("Floor", 18);
      case 59: 
        break;
      case 27: 
        return m_SymFactory.newSymbol("Log", 12);
      case 60: 
        break;
      case 35: 
        return m_SymFactory.newSymbol("False", 22);
      case 61: 
        break;
      case 31: 
        return m_SymFactory.newSymbol("True", 21);
      case 62: 
        break;
      case 7: 
        return m_SymFactory.newSymbol("Equals", 27);
      case 63: 
        break;
      case 18: 
        return m_SymFactory.newSymbol("Greater or equal than", 26);
      case 64: 
        break;
      case 26: 
        return m_SymFactory.newSymbol("Exp", 13);
      case 65: 
        break;
      case 20: 
        return m_SymFactory.newSymbol("Or", 30);
      case 66: 
        break;
      case 34: 
        return m_SymFactory.newSymbol("Attribute", 34, new String(yytext()));
      case 67: 
        break;
      case 30: 
        return m_SymFactory.newSymbol("Sqrt", 11);
      case 68: 
        break;
      case 5: 
        return m_SymFactory.newSymbol("Division", 9);
      case 69: 
        break;
      case 22: 
        return m_SymFactory.newSymbol("Not", 28);
      case 70: 
        break;
      case 10: 
        return m_SymFactory.newSymbol("Number", 32, new Double(yytext()));
      case 71: 
        break;
      case 3: 
        return m_SymFactory.newSymbol("Plus", 7);
      case 72: 
        break;
      case 38: 
        return m_SymFactory.newSymbol("Missing", 5);
      case 73: 
        break;
      case 32: 
        return m_SymFactory.newSymbol("Rint", 17);
      case 74: 
        break;
      case 24: 
        return m_SymFactory.newSymbol("And", 29);
      case 75: 
        break;
      case 2: 
        return m_SymFactory.newSymbol("Minus", 6);
      case 76: 
        break;
      default: 
        if ((zzInput == -1) && (zzStartRead == zzCurrentPos)) {
          zzAtEOF = true;
          return m_SymFactory.newSymbol("EOF", 0);
        }
        

        zzScanError(1);
      }
    }
  }
}
