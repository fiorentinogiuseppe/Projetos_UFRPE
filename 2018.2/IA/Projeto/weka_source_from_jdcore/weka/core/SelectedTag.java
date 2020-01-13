package weka.core;

import java.util.HashSet;











































public class SelectedTag
  implements RevisionHandler
{
  protected int m_Selected;
  protected Tag[] m_Tags;
  
  public SelectedTag(int tagID, Tag[] tags)
  {
    HashSet ID = new HashSet();
    HashSet IDStr = new HashSet();
    for (int i = 0; i < tags.length; i++) {
      ID.add(new Integer(tags[i].getID()));
      IDStr.add(tags[i].getIDStr());
    }
    if (ID.size() != tags.length)
      throw new IllegalArgumentException("The IDs are not unique!");
    if (IDStr.size() != tags.length) {
      throw new IllegalArgumentException("The ID strings are not unique!");
    }
    for (int i = 0; i < tags.length; i++) {
      if (tags[i].getID() == tagID) {
        m_Selected = i;
        m_Tags = tags;
        return;
      }
    }
    
    throw new IllegalArgumentException("Selected tag is not valid");
  }
  







  public SelectedTag(String tagText, Tag[] tags)
  {
    for (int i = 0; i < tags.length; i++) {
      if ((tags[i].getReadable().equalsIgnoreCase(tagText)) || (tags[i].getIDStr().equalsIgnoreCase(tagText)))
      {
        m_Selected = i;
        m_Tags = tags;
        return;
      }
    }
    throw new IllegalArgumentException("Selected tag is not valid");
  }
  





  public boolean equals(Object o)
  {
    if ((o == null) || (!o.getClass().equals(getClass()))) {
      return false;
    }
    SelectedTag s = (SelectedTag)o;
    if ((s.getTags() == m_Tags) && (s.getSelectedTag() == m_Tags[m_Selected]))
    {
      return true;
    }
    return false;
  }
  






  public Tag getSelectedTag()
  {
    return m_Tags[m_Selected];
  }
  




  public Tag[] getTags()
  {
    return m_Tags;
  }
  




  public String toString()
  {
    return getSelectedTag().toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
}
