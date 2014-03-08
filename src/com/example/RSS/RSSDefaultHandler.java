package com.example.RSS;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by Alex on 14.01.14.
 */

public class RSSDefaultHandler extends DefaultHandler {
		private ArrayList<Node> nodeArrayList;
		private boolean isItem;
		private Node node;
		String string;

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			nodeArrayList = new ArrayList<Node>();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			string = "";
			if (qName.equals("item")) {
				isItem = true;
				node = new Node();
			}
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void characters(char[] c, int start, int length) throws SAXException {
			super.characters(c, start, length);
			if (isItem) {
				string += new String(c, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("item")) {
				isItem = false;
				nodeArrayList.add(node);
			}

			if (isItem) {
				if (qName.equals("title")) {
					node.setTitle(string);
					string = "";
				}
				if (qName.equals("pubDate")) {
					node.setDate(string);
					string = "";
				}
				if (qName.equals("description")) {
					string = string.replaceAll("<br(.*)?>", "\n");
					string = string.replaceAll("&quot;", "");
					string = string.replaceAll("&#039;", "");
					string = string.replaceAll("&lt;", "");
					string = string.replaceAll("&gt;", "");
					node.setDescription(string);
					string = "";
				}
			}
			super.endElement(uri, localName, qName);
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}

		public ArrayList<Node> getNodeArrayList() {
			return this.nodeArrayList;
		}

		public ArrayList<Node> getResult() {
			return this.nodeArrayList;
		}
	}

