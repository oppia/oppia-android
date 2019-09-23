package org.oppia.app.player.content;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;

import android.widget.TextView;
import org.oppia.util.data.UrlImageParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.util.ArrayDeque;

public class HtmlParser implements Html.TagHandler, ContentHandler
{


  public interface TagHandler
  {
    boolean handleTag(boolean opening, String tag, Editable output, Attributes attributes);
  }

  public static Spanned buildSpannedText(TextView textView,Context context,String html, TagHandler handler)
  {
    // add a tag at the start that is not handled by default,
    // allowing custom tag handler to replace xmlReader contentHandler
    UrlImageParser imageGetter = new UrlImageParser(textView,context);
    return Html.fromHtml("<oppia-noninteractive-image/>" + html, imageGetter, new HtmlParser(handler));
  }

  public static String getValue(Attributes attributes, String name)
  {
    for (int i = 0, n = attributes.getLength(); i < n; i++)
    {
      if (name.equals(attributes.getLocalName(i)))
        return attributes.getValue(i);
    }
    return null;
  }
  public static String replaceValue(Attributes attributes, String name)
  {
    for (int i = 0, n = attributes.getLength(); i < n; i++)
    {
      if (name.equals(attributes.getLocalName(i))){
            name= attributes.getValue(i).replace("filepath-with-value","img");
        return name;
      }

    }
    return null;
  }

  private final TagHandler handler;
  private ContentHandler wrapped;
  private Editable text;
  private ArrayDeque<Boolean> tagStatus = new ArrayDeque<>();

  private HtmlParser(TagHandler handler)
  {
    this.handler = handler;
  }

  @Override
  public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
  {
    if (wrapped == null)
    {
      // record result object
      text = output;

      // record current content handler
      wrapped = xmlReader.getContentHandler();

      // replace content handler with our own that forwards to calls to original when needed
      xmlReader.setContentHandler(this);


      // handle endElement() callback for <inject/> tag
      tagStatus.addLast(Boolean.FALSE);
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
  {
    boolean isHandled = handler.handleTag(true, localName, text, attributes);
    tagStatus.addLast(isHandled);
    if (!isHandled)
      wrapped.startElement(uri, localName, qName, attributes);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (!tagStatus.removeLast())
      wrapped.endElement(uri, localName, qName);
    handler.handleTag(false, localName, text, null);
  }

  @Override
  public void setDocumentLocator(Locator locator)
  {
    wrapped.setDocumentLocator(locator);
  }

  @Override
  public void startDocument() throws SAXException
  {
    wrapped.startDocument();
  }

  @Override
  public void endDocument() throws SAXException
  {
    wrapped.endDocument();
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException
  {
    wrapped.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException
  {
    wrapped.endPrefixMapping(prefix);
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    wrapped.characters(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
    wrapped.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException
  {
    wrapped.processingInstruction(target, data);
  }

  @Override
  public void skippedEntity(String name) throws SAXException
  {
    wrapped.skippedEntity(name);
  }
}
