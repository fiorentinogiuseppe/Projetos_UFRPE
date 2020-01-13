package weka.core;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
































public class TechnicalInformation
  implements RevisionHandler
{
  protected static final String MISSING_ID = "missing_id";
  
  public static enum Type
  {
    ARTICLE("article", "An article from a journal or magazine."), 
    
    BOOK("book", "A book with an explicit publisher."), 
    
    BOOKLET("booklet", "A work that is printed and bound, but without a named publisher or sponsoring institution."), 
    
    CONFERENCE("conference", "The same as inproceedings."), 
    
    INBOOK("inbook", "A part of a book, which may be a chapter (or section or whatever) and/or a range of pages."), 
    
    INCOLLECTION("incollection", "A part of a book having its own title."), 
    
    INPROCEEDINGS("inproceedings", "An article in a conference proceedings."), 
    
    MANUAL("manual", "Technical documentation."), 
    
    MASTERSTHESIS("mastersthesis", "A Master's thesis."), 
    
    MISC("misc", "Use this type when nothing else fits."), 
    
    PHDTHESIS("phdthesis", "A PhD thesis."), 
    
    PROCEEDINGS("proceedings", "The proceedings of a conference."), 
    
    TECHREPORT("techreport", "A report published by a school or other institution, usually numbered within a series."), 
    
    UNPUBLISHED("unpublished", "A document having an author and title, but not formally published.");
    



    protected String m_Display;
    


    protected String m_Comment;
    


    private Type(String display, String comment)
    {
      m_Display = display;
      m_Comment = comment;
    }
    




    public String getDisplay()
    {
      return m_Display;
    }
    




    public String getComment()
    {
      return m_Comment;
    }
    




    public String toString()
    {
      return m_Display;
    }
  }
  

  public static enum Field
  {
    ADDRESS("address", "Usually the address of the publisher or other type of institution. For major publishing houses, van Leunen recommends omitting the information entirely. For small publishers, on the other hand, you can help the reader by giving the complete address."), 
    
    ANNOTE("annote", "An annotation. It is not used by the standard bibliography styles, but may be used by others that produce an annotated bibliography."), 
    
    AUTHOR("author", "The name(s) of the author(s), in the format described in the LaTeX book."), 
    
    BOOKTITLE("booktitle", "Title of a book, part of which is being cited. See the LaTeX book for how to type titles. For book entries, use the title field instead."), 
    
    CHAPTER("chapter", "A chapter (or section or whatever) number."), 
    
    CROSSREF("crossref", "The database key of the entry being cross referenced. Any fields that are missing from the current record are inherited from the field being cross referenced."), 
    
    EDITION("edition", "The edition of a book---for example, ``Second''. This should be an ordinal, and should have the first letter capitalized, as shown here; the standard styles convert to lower case when necessary."), 
    
    EDITOR("editor", "Name(s) of editor(s), typed as indicated in the LaTeX book. If there is also an author field, then the editor field gives the editor of the book or collection in which the reference appears."), 
    
    HOWPUBLISHED("howpublished", "How something strange has been published. The first word should be capitalized."), 
    
    INSTITUTION("institution", "The sponsoring institution of a technical report."), 
    
    JOURNAL("journal", "A journal name. Abbreviations are provided for many journals."), 
    
    KEY("key", "Used for alphabetizing, cross referencing, and creating a label when the ``author'' information is missing. This field should not be confused with the key that appears in the cite command and at the beginning of the database entry."), 
    
    MONTH("month", "The month in which the work was published or, for an unpublished work, in which it was written. You should use the standard three-letter abbreviation, as described in Appendix B.1.3 of the LaTeX book."), 
    
    NOTE("note", "Any additional information that can help the reader. The first word should be capitalized."), 
    
    NUMBER("number", "The number of a journal, magazine, technical report, or of a work in a series. An issue of a journal or magazine is usually identified by its volume and number; the organization that issues a technical report usually gives it a number; and sometimes books are given numbers in a named series."), 
    
    ORGANIZATION("organization", "The organization that sponsors a conference or that publishes a manual."), 
    
    PAGES("pages", "One or more page numbers or range of numbers, such as 42--111 or 7,41,73--97 or 43+ (the `+' in this last example indicates pages following that don't form a simple range). To make it easier to maintain Scribe-compatible databases, the standard styles convert a single dash (as in 7-33) to the double dash used in TeX to denote number ranges (as in 7--33)."), 
    
    PUBLISHER("publisher", "The publisher's name."), 
    
    SCHOOL("school", "The name of the school where a thesis was written."), 
    
    SERIES("series", "The name of a series or set of books. When citing an entire book, the the title field gives its title and an optional series field gives the name of a series or multi-volume set in which the book is published."), 
    
    TITLE("title", "The work's title, typed as explained in the LaTeX book."), 
    
    TYPE("type", "The type of a technical report---for example, ``Research Note''."), 
    
    VOLUME("volume", "The volume of a journal or multi-volume book."), 
    
    YEAR("year", "The year of publication or, for an unpublished work, the year it was written. Generally it should consist of four numerals, such as 1984, although the standard styles can handle any year whose last four nonpunctuation characters are numerals, such as `\\hbox{(about 1984)}'."), 
    

    AFFILIATION("affiliation", "The authors affiliation."), 
    
    ABSTRACT("abstract", "An abstract of the work."), 
    
    CONTENTS("contents", "A Table of Contents "), 
    
    COPYRIGHT("copyright", "Copyright information."), 
    
    ISBN("ISBN", "The International Standard Book Number (10 digits)."), 
    
    ISBN13("ISBN-13", "The International Standard Book Number (13 digits)."), 
    
    ISSN("ISSN", "The International Standard Serial Number. Used to identify a journal."), 
    
    KEYWORDS("keywords", "Key words used for searching or possibly for annotation."), 
    
    LANGUAGE("language", "The language the document is in."), 
    
    LOCATION("location", "A location associated with the entry, such as the city in which a conference took place."), 
    
    LCCN("LCCN", "The Library of Congress Call Number. I've also seen this as lib-congress."), 
    
    MRNUMBER("mrnumber", "The Mathematical Reviews number."), 
    
    PRICE("price", "The price of the document."), 
    
    SIZE("size", "The physical dimensions of a work."), 
    
    URL("URL", "The WWW Universal Resource Locator that points to the item being referenced. This often is used for technical reports to point to the ftp site where the postscript source of the report is located."), 
    

    PS("PS", "A link to a postscript file."), 
    
    PDF("PDF", "A link to a PDF file."), 
    
    HTTP("HTTP", "A hyperlink to a resource.");
    



    protected String m_Display;
    


    protected String m_Comment;
    


    private Field(String display, String comment)
    {
      m_Display = display;
      m_Comment = comment;
    }
    




    public String getDisplay()
    {
      return m_Display;
    }
    




    public String getComment()
    {
      return m_Comment;
    }
    




    public String toString()
    {
      return m_Display;
    }
  }
  




  protected Type m_Type = null;
  


  protected String m_ID = "";
  

  protected Hashtable m_Values = new Hashtable();
  

  protected Vector m_Additional = new Vector();
  





  public TechnicalInformation(Type type)
  {
    this(type, "");
  }
  






  public TechnicalInformation(Type type, String id)
  {
    m_Type = type;
    m_ID = id;
  }
  




  public Type getType()
  {
    return m_Type;
  }
  




  protected String[] getAuthors()
  {
    return getValue(Field.AUTHOR).split(" and ");
  }
  










  protected String generateID()
  {
    String result = m_ID;
    

    if ((result.length() == 0) && 
      (exists(Field.AUTHOR)) && (exists(Field.YEAR))) {
      String[] authors = getAuthors();
      if (authors[0].indexOf(",") > -1) {
        String[] parts = authors[0].split(",");
        result = parts[0];
      }
      else {
        String[] parts = authors[0].split(" ");
        if (parts.length == 1) {
          result = parts[0];
        } else
          result = parts[(parts.length - 1)];
      }
      result = result + getValue(Field.YEAR);
      result = result.replaceAll(" ", "");
    }
    


    if (result.length() == 0) {
      result = "missing_id";
    }
    return result;
  }
  





  public String getID()
  {
    return generateID();
  }
  





  public void setValue(Field field, String value)
  {
    m_Values.put(field, value);
  }
  






  public String getValue(Field field)
  {
    if (m_Values.containsKey(field)) {
      return (String)m_Values.get(field);
    }
    return "";
  }
  






  public boolean exists(Field field)
  {
    return (m_Values.containsKey(field)) && (((String)m_Values.get(field)).length() != 0);
  }
  





  public Enumeration fields()
  {
    return m_Values.keys();
  }
  




  public boolean hasAdditional()
  {
    return m_Additional.size() > 0;
  }
  





  public Enumeration additional()
  {
    return m_Additional.elements();
  }
  





  public void add(TechnicalInformation value)
  {
    if (value == this)
      throw new IllegalArgumentException("Can't add object to itself!");
    m_Additional.add(value);
  }
  







  public TechnicalInformation add(Type type)
  {
    TechnicalInformation result = new TechnicalInformation(type);
    add(result);
    
    return result;
  }
  











  public String toString()
  {
    String result = "";
    String[] authors = getAuthors();
    

    if (getType() == Type.BOOK) {
      for (int i = 0; i < authors.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + authors[i];
      }
      if (exists(Field.YEAR)) {
        result = result + " (" + getValue(Field.YEAR) + ").";
      } else
        result = result + ".";
      result = result + " " + getValue(Field.TITLE) + ".";
      result = result + " " + getValue(Field.PUBLISHER);
      if (exists(Field.ADDRESS))
        result = result + ", " + getValue(Field.ADDRESS);
      result = result + ".";

    }
    else if (getType() == Type.ARTICLE) {
      for (int i = 0; i < authors.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + authors[i];
      }
      if (exists(Field.YEAR)) {
        result = result + " (" + getValue(Field.YEAR) + ").";
      } else
        result = result + ".";
      result = result + " " + getValue(Field.TITLE) + ".";
      

      if (exists(Field.JOURNAL)) {
        result = result + " " + getValue(Field.JOURNAL) + ".";
        
        if (exists(Field.VOLUME))
          result = result + " " + getValue(Field.VOLUME);
        if (exists(Field.NUMBER))
          result = result + "(" + getValue(Field.NUMBER) + ")";
        if (exists(Field.PAGES)) {
          result = result + ":" + getValue(Field.PAGES);
        }
        result = result + ".";
      }
      



      if (exists(Field.URL)) {
        result = result + " URL " + getValue(Field.URL) + ".";
      }
    }
    else if ((getType() == Type.CONFERENCE) || (getType() == Type.INPROCEEDINGS)) {
      for (int i = 0; i < authors.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + authors[i];
      }
      result = result + ": " + getValue(Field.TITLE) + ".";
      result = result + " In: " + getValue(Field.BOOKTITLE);
      
      if (exists(Field.ADDRESS))
        result = result + ", " + getValue(Field.ADDRESS);
      if (exists(Field.PAGES)) {
        result = result + ", " + getValue(Field.PAGES);
      }
      if (exists(Field.YEAR)) {
        result = result + ", " + getValue(Field.YEAR) + ".";
      } else {
        result = result + ".";
      }
    }
    else if (getType() == Type.INCOLLECTION) {
      for (int i = 0; i < authors.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + authors[i];
      }
      result = result + ": " + getValue(Field.TITLE) + ".";
      result = result + " In ";
      if (exists(Field.EDITOR))
        result = result + getValue(Field.EDITOR) + ", editors, ";
      result = result + getValue(Field.BOOKTITLE);
      
      if (exists(Field.ADDRESS))
        result = result + ", " + getValue(Field.ADDRESS);
      if (exists(Field.PAGES)) {
        result = result + ", " + getValue(Field.PAGES);
      }
      if (exists(Field.YEAR)) {
        result = result + ", " + getValue(Field.YEAR) + ".";
      } else {
        result = result + ".";
      }
    }
    else {
      for (int i = 0; i < authors.length; i++) {
        if (i > 0)
          result = result + ", ";
        result = result + authors[i];
      }
      if (exists(Field.YEAR)) {
        result = result + " (" + getValue(Field.YEAR) + ").";
      } else
        result = result + ".";
      result = result + " " + getValue(Field.TITLE) + ".";
      if (exists(Field.ADDRESS))
        result = result + " " + getValue(Field.ADDRESS) + ".";
      if (exists(Field.URL)) {
        result = result + " URL " + getValue(Field.URL) + ".";
      }
    }
    
    Enumeration enm = additional();
    while (enm.hasMoreElements()) {
      result = result + "\n\n" + enm.nextElement().toString();
    }
    
    return result;
  }
  













  public String toBibTex()
  {
    String result = "@" + getType() + "{" + getID() + "";
    

    Vector list = new Vector();
    Enumeration enm = fields();
    while (enm.hasMoreElements())
      list.add(enm.nextElement());
    Collections.sort(list);
    

    for (int i = 0; i < list.size(); i++) {
      Field field = (Field)list.get(i);
      if (exists(field))
      {
        String value = getValue(field);
        value = value.replaceAll("\\~", "\\\\~");
        result = result + ",\n   " + field + " = {" + value + "}";
      }
    }
    result = result + "\n}";
    

    enm = additional();
    while (enm.hasMoreElements()) {
      result = result + "\n\n" + ((TechnicalInformation)enm.nextElement()).toBibTex();
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
  

























  public static void main(String[] args)
    throws Exception
  {
    if (args.length != 0) {
      TechnicalInformation info = null;
      
      String tmpStr = Utils.getOption('W', args);
      if (tmpStr.length() != 0) {
        Class cls = Class.forName(tmpStr);
        TechnicalInformationHandler handler = (TechnicalInformationHandler)cls.newInstance();
        info = handler.getTechnicalInformation();
      }
      else {
        throw new IllegalArgumentException("A classname has to be provided with the -W option!"); }
      TechnicalInformationHandler handler;
      Class cls;
      if (Utils.getFlag("bibtex", args)) {
        System.out.println("\n" + handler.getClass().getName() + ":\n" + info.toBibTex());
      }
      if (Utils.getFlag("plaintext", args)) {
        System.out.println("\n" + handler.getClass().getName() + ":\n" + info.toString());
      }
    }
    else {
      TechnicalInformation info = new TechnicalInformation(Type.BOOK);
      info.setValue(Field.AUTHOR, "Ross Quinlan");
      info.setValue(Field.YEAR, "1993");
      info.setValue(Field.TITLE, "C4.5: Programs for Machine Learning");
      info.setValue(Field.PUBLISHER, "Morgan Kaufmann Publishers");
      info.setValue(Field.ADDRESS, "San Mateo, CA");
      TechnicalInformation additional = info;
      
      System.out.println("\ntoString():\n" + info.toString());
      System.out.println("\ntoBibTex():\n" + info.toBibTex());
      

      info = new TechnicalInformation(Type.INPROCEEDINGS);
      info.setValue(Field.AUTHOR, "Freund, Y. and Mason, L.");
      info.setValue(Field.YEAR, "1999");
      info.setValue(Field.TITLE, "The alternating decision tree learning algorithm");
      info.setValue(Field.BOOKTITLE, "Proceeding of the Sixteenth International Conference on Machine Learning");
      info.setValue(Field.ADDRESS, "Bled, Slovenia");
      info.setValue(Field.PAGES, "124-133");
      
      System.out.println("\ntoString():\n" + info.toString());
      System.out.println("\ntoBibTex():\n" + info.toBibTex());
      

      info = new TechnicalInformation(Type.ARTICLE);
      info.setValue(Field.AUTHOR, "R. Quinlan");
      info.setValue(Field.YEAR, "1986");
      info.setValue(Field.TITLE, "Induction of decision trees");
      info.setValue(Field.JOURNAL, "Machine Learning");
      info.setValue(Field.VOLUME, "1");
      info.setValue(Field.NUMBER, "1");
      info.setValue(Field.PAGES, "81-106");
      
      additional = new TechnicalInformation(Type.BOOK);
      additional.setValue(Field.AUTHOR, "Ross Quinlan");
      additional.setValue(Field.YEAR, "1993");
      additional.setValue(Field.TITLE, "C4.5: Programs for Machine Learning");
      additional.setValue(Field.PUBLISHER, "Morgan Kaufmann Publishers");
      additional.setValue(Field.ADDRESS, "San Mateo, CA");
      info.add(additional);
      
      System.out.println("\ntoString():\n" + info.toString());
      System.out.println("\ntoBibTex():\n" + info.toBibTex());
    }
  }
}
