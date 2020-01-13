package weka.core.mathematicalexpression;

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
  public static final int YYINITIAL = 0;
  private static final int[] ZZ_LEXSTATE = { 0, 0 };
  





  private static final char[] ZZ_CMAP = { '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', ' ', ' ', '\000', ' ', ' ', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', '\000', ' ', '\b', '\000', '\000', '\000', '\000', '\t', '\000', '"', '#', '\003', '\002', '!', '\001', '\036', '\004', '\035', '\035', '\035', '\035', '\035', '\035', '\035', '\035', '\035', '\035', '\000', '\000', '\005', '\006', '\007', '\000', '\000', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\037', '\000', '\000', '\000', '\000', '\000', '\000', '\020', '\023', '\033', '\000', '\016', '\017', '\026', '\000', '\031', '\000', '\000', '\021', '\000', '\032', '\025', '\030', '\024', '\f', '\022', '\013', '\r', '\000', '\034', '\027', '\000', '\000', '\000', '\n', '\000', '\000', '\000' };
  












  private static final int[] ZZ_ACTION = zzUnpackAction();
  


  private static final String ZZ_ACTION_PACKED_0 = "\001\000\001\001\001\002\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\n\001\001\f\001\r\001\016\001\017\001\020\001\021\001\022\001\023\016\000\001\f\001\000\001\024\001\000\001\025\002\000\001\026\001\027\001\000\001\030\001\031\002\000\001\032\001\033\001\034\002\000\001\035\001\000\001\036\001\037\001 \001\000\001!";
  



  private static int[] zzUnpackAction()
  {
    int[] result = new int[70];
    int offset = 0;
    offset = zzUnpackAction("\001\000\001\001\001\002\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\n\001\001\f\001\r\001\016\001\017\001\020\001\021\001\022\001\023\016\000\001\f\001\000\001\024\001\000\001\025\002\000\001\026\001\027\001\000\001\030\001\031\002\000\001\032\001\033\001\034\002\000\001\035\001\000\001\036\001\037\001 \001\000\001!", offset, result);
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
  




  private static final String ZZ_ROWMAP_PACKED_0 = "\000\000\000$\000H\000$\000$\000$\000l\000$\000\000$\000$\000$\000´\000Ø\000ü\000Ġ\000ń\000Ũ\000ƌ\000ư\000ǔ\000Ǹ\000Ȝ\000ɀ\000$\000$\000$\000$\000$\000$\000ɤ\000ʈ\000ʬ\000ː\000˴\000̘\000̼\000͠\000΄\000Ψ\000ό\000ϰ\000Д\000и\000ќ\000Ҁ\000$\000Ҥ\000$\000ӈ\000Ӭ\000$\000$\000Ԑ\000$\000$\000Դ\000՘\000$\000$\000$\000ռ\000֠\000$\000ׄ\000$\000$\000$\000ר\000$";
  




  private static int[] zzUnpackRowMap()
  {
    int[] result = new int[70];
    int offset = 0;
    offset = zzUnpackRowMap("\000\000\000$\000H\000$\000$\000$\000l\000$\000\000$\000$\000$\000´\000Ø\000ü\000Ġ\000ń\000Ũ\000ƌ\000ư\000ǔ\000Ǹ\000Ȝ\000ɀ\000$\000$\000$\000$\000$\000$\000ɤ\000ʈ\000ʬ\000ː\000˴\000̘\000̼\000͠\000΄\000Ψ\000ό\000ϰ\000Д\000и\000ќ\000Ҁ\000$\000Ҥ\000$\000ӈ\000Ӭ\000$\000$\000Ԑ\000$\000$\000Դ\000՘\000$\000$\000$\000ռ\000֠\000$\000ׄ\000$\000$\000$\000ר\000$", offset, result);
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
  


  private static final String ZZ_TRANS_PACKED_0 = "\001\002\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\001\f\001\r\001\016\001\002\001\017\001\020\001\021\001\022\001\023\005\002\001\024\001\025\001\002\001\026\001\002\001\027\001\002\001\030\001\031\001\032\001\033\001\034A\000\001\027\f\000\001\035#\000\001\036)\000\001\037\003\000\001 ,\000\001!!\000\001\"\034\000\001#\001$%\000\001%%\000\001&\"\000\001'\004\000\001(\037\000\001)\035\000\001*\"\000\001+\006\000\001,+\000\001\027\001-$\000\001\030\021\000\001.0\000\001/#\000\0010!\000\0011\034\000\0012'\000\0013 \000\0014'\000\0015\031\000\00161\000\0017%\000\0018\025\000\0019.\000\001:\034\000\001;.\000\001-\024\000\001< \000\001=*\000\001>&\000\001?\031\000\001@)\000\001A#\000\001B \000\001C!\000\001D)\000\001E\037\000\001F\025\000";
  


  private static final int ZZ_UNKNOWN_ERROR = 0;
  


  private static final int ZZ_NO_MATCH = 1;
  

  private static final int ZZ_PUSHBACK_2BIG = 2;
  


  private static int[] zzUnpackTrans()
  {
    int[] result = new int['،'];
    int offset = 0;
    offset = zzUnpackTrans("\001\002\001\003\001\004\001\005\001\006\001\007\001\b\001\t\001\n\001\013\001\f\001\r\001\016\001\002\001\017\001\020\001\021\001\022\001\023\005\002\001\024\001\025\001\002\001\026\001\002\001\027\001\002\001\030\001\031\001\032\001\033\001\034A\000\001\027\f\000\001\035#\000\001\036)\000\001\037\003\000\001 ,\000\001!!\000\001\"\034\000\001#\001$%\000\001%%\000\001&\"\000\001'\004\000\001(\037\000\001)\035\000\001*\"\000\001+\006\000\001,+\000\001\027\001-$\000\001\030\021\000\001.0\000\001/#\000\0010!\000\0011\034\000\0012'\000\0013 \000\0014'\000\0015\031\000\00161\000\0017%\000\0018\025\000\0019.\000\001:\034\000\001;.\000\001-\024\000\001< \000\001=*\000\001>&\000\001?\031\000\001@)\000\001A#\000\001B \000\001C!\000\001D)\000\001E\037\000\001F\025\000", offset, result);
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
  
  private static final String ZZ_ATTRIBUTE_PACKED_0 = "\001\000\001\t\001\001\003\t\001\001\001\t\001\001\003\t\f\001\006\t\016\000\001\001\001\000\001\t\001\000\001\t\002\000\002\t\001\000\002\t\002\000\003\t\002\000\001\t\001\000\003\t\001\000\001\t";
  
  private Reader zzReader;
  private int zzState;
  
  private static int[] zzUnpackAttribute()
  {
    int[] result = new int[70];
    int offset = 0;
    offset = zzUnpackAttribute("\001\000\001\t\001\001\003\t\001\001\001\t\001\001\003\t\f\001\006\t\016\000\001\001\001\000\001\t\001\000\001\t\002\000\002\t\001\000\002\t\002\000\003\t\002\000\001\t\001\000\003\t\001\000\001\t", offset, result);
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
  

  protected SymbolFactory sf;
  

  public Scanner(InputStream r, SymbolFactory sf)
  {
    this(r);
    this.sf = sf;
  }
  






  public Scanner(Reader in)
  {
    zzReader = in;
  }
  





  public Scanner(InputStream in)
  {
    this(new InputStreamReader(in));
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
      case 11: 
        return sf.newSymbol("Or", 30);
      case 34: 
        break;
      case 24: 
        return sf.newSymbol("Sin", 13);
      case 35: 
        break;
      case 23: 
        return sf.newSymbol("Log", 11);
      case 36: 
        break;
      case 16: 
        return sf.newSymbol("Left Bracket", 3);
      case 37: 
        break;
      case 27: 
        return sf.newSymbol("True", 21);
      case 38: 
        break;
      case 1: 
        System.err.println("Illegal character: " + yytext());
      case 39: 
        break;
      case 29: 
        return sf.newSymbol("Sqrt", 10);
      case 40: 
        break;
      case 30: 
        return sf.newSymbol("Ceil", 19);
      case 41: 
        break;
      case 31: 
        return sf.newSymbol("False", 22);
      case 42: 
        break;
      case 18: 
        return sf.newSymbol("Less or equal than", 24);
      case 43: 
        break;
      case 17: 
        return sf.newSymbol("Right Bracket", 4);
      case 44: 
        break;
      case 26: 
        return sf.newSymbol("Cos", 14);
      case 45: 
        break;
      case 21: 
        return sf.newSymbol("Exp", 12);
      case 46: 
        break;
      case 12: 
        return sf.newSymbol("Number", 31, new Double(yytext()));
      case 47: 
        break;
      case 3: 
        return sf.newSymbol("Plus", 6);
      case 48: 
        break;
      case 13: 
        return sf.newSymbol("Variable", 33, new String(yytext()));
      case 49: 
        break;
      case 22: 
        return sf.newSymbol("Abs", 9);
      case 50: 
        break;
      case 20: 
        return sf.newSymbol("Tan", 15);
      case 51: 
        break;
      case 15: 
        return sf.newSymbol("Comma", 2);
      case 52: 
        break;
      case 33: 
        return sf.newSymbol("IfElse", 20);
      case 53: 
        break;
      case 28: 
        return sf.newSymbol("Rint", 16);
      case 54: 
        break;
      case 8: 
        return sf.newSymbol("Greater than", 25);
      case 55: 
        break;
      case 19: 
        return sf.newSymbol("Greater or equal than", 26);
      case 56: 
        break;
      case 5: 
        return sf.newSymbol("Division", 8);
      case 57: 
        break;
      case 7: 
        return sf.newSymbol("Equals", 27);
      case 58: 
        break;
      case 10: 
        return sf.newSymbol("And", 29);
      case 59: 
        break;
      case 9: 
        return sf.newSymbol("Not", 28);
      case 60: 
        break;
      case 4: 
        return sf.newSymbol("Times", 7);
      case 61: 
        break;
      case 32: 
        return sf.newSymbol("Floor", 17);
      case 62: 
        break;
      case 6: 
        return sf.newSymbol("Less than", 23);
      case 63: 
        break;
      case 25: 
        return sf.newSymbol("Pow", 18);
      case 64: 
        break;
      case 14: 
      case 65: 
        break;
      
      case 2: 
        return sf.newSymbol("Minus", 5);
      case 66: 
        break;
      default: 
        if ((zzInput == -1) && (zzStartRead == zzCurrentPos)) {
          zzAtEOF = true;
          return sf.newSymbol("EOF", 0);
        }
        

        zzScanError(1);
      }
    }
  }
}
