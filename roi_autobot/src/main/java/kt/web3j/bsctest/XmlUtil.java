package kt.web3j.bsctest;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XmlUtil {
//	private static Logger logger = LoggerFactory.getLogger(XmlUtil.class);
	public static Document loadXml(String sFileName) throws DocumentException{
		Document doc = null;
		SAXReader reader = new SAXReader();
	   	doc = (Document) reader.read(sFileName);
		return doc;
	}
	
	public static void writeXml(Document doc, FileOutputStream os) throws IOException {
	// Pretty print the document to 輸出流
			OutputFormat format = OutputFormat.createPrettyPrint();
			/*
			 * // Compact format to 輸出流 OutputFormat format =
			 * OutputFormat.createCompactFormat();
			 */

			// 這裏牽涉到複雜的編碼轉換問題：
			// 使用以上方法讀取xml文件得到的Document，不管xml文件是那種編碼，在讀到內存裏都會變成UTF-8
			// 而在將Document寫回文件時，如果用new XMLWriter(new FileOutputStream(new
			// File("...")),
			// FileOutputStream所訪問的碼表是本地默認中文碼表，即gb2312。而FileOutputStream也一種沒有指定輸出編碼標准的構造方法
			// 所以，需要中間借助OutputStreamWriter來進行輸出流的編碼類型轉換。
			// 這樣，就可以按照UTF-8寫回原xml文件了。
			XMLWriter writer = new XMLWriter(new OutputStreamWriter(os, "UTF-8"),format);
			writer.write(doc);
			writer.close();
	}
	
	public static Document loadXml(URL url) throws DocumentException{
		Document doc = null;
		SAXReader reader = new SAXReader();
		doc = (Document) reader.read(url);
		return doc;
	}
	
	public static Document loadXml(FileInputStream fs) throws DocumentException{
		Document doc = null;
		SAXReader reader = new SAXReader();
		doc = (Document) reader.read(fs);
		return doc;
	}

	public static Document loadXml(InputStream is) throws DocumentException{
		Document doc = null;
		SAXReader reader = new SAXReader();
		doc = (Document) reader.read(is);
		return doc;
	}
	
//改為將NS去掉
//	public static String xpathAddNameSpace(String sXpath, String sNameSpace){
//		String sFinalXpath = sXpath;
//		
//		sFinalXpath = sFinalXpath.replace("/..", "$");//先把/..符號換為$,待處理完/再改回
//		sFinalXpath = sFinalXpath.replace("//", "^");//先把//符號換為!,待處理完/再處理!
//		sFinalXpath = sFinalXpath.replace("/", "/"+sNameSpace+":");
//		sFinalXpath = sFinalXpath.replace("^", "//"+sNameSpace+":");
//		sFinalXpath = sFinalXpath.replace("$", "/..");
//		
//		sFinalXpath = sFinalXpath.replace("[@", "!");//避免誤改到[@,等處理完[再改回
//		sFinalXpath = sFinalXpath.replace("[", "["+sNameSpace+":");
//		sFinalXpath = sFinalXpath.replace("!", "[@");
//		return sFinalXpath;
//	}
	

	public static Node getXpathSingleNode(Document doc,String sXpath){
		Node node = null;
		if (sXpath.equals("../")){
			node = doc;
		}else{
			node = doc.selectSingleNode(sXpath);
		}
		return node;
	}
	
	public static Node getXpathSingleNode(Element el, String sXpath){
		Node node = null;
		if (sXpath.equals("../")){
			node = el.getParent();
		}else{
			node = el.selectSingleNode(sXpath);
		}
		return node;
	}
	
	public static String getXpathAttributeValue(Element parentEl, String sXpath, String sAttribute){
	String sAttValue = null;
	Element el = (Element) getXpathSingleNode(parentEl, sXpath);
	if (el ==null) {
		return null;
	}
	sAttValue = el.attributeValue(sAttribute);
	return sAttValue;	
}
	
	public static String getXpathAttributeValue(Document parentEl, String sXpath, String sAttribute){
		String sAttValue = null;
		Element el = (Element) getXpathSingleNode(parentEl, sXpath);
		if (el ==null) {
			return null;
		}
		sAttValue = el.attributeValue(sAttribute);
		return sAttValue;	
	}
	
	public static String getXpathValue(Document doc,String sXpath, String sMultiFlag){
		Element el = doc.getRootElement();
		String sData = null;
		sData = getXpathValue(el,sXpath, sMultiFlag);
		return sData;
	}
	
	public static String getXpathValue(Element el,String sXpath, String sMultiFlag){
		String sData = null;
		StringBuffer sbData = new StringBuffer();
		if ((sMultiFlag == null) || (sMultiFlag.length() == 0)){
			Node node = el.selectSingleNode(sXpath);
			if (node != null){
				sData = node.getText();
			}
		}
		else {
			List list = el.selectNodes(sXpath);
			for (int i = 0 ; i < list.size() ; i ++){
				Node node = (Node) list.get(i);
				String sTemp = node.getText();
				if (sTemp != null){ 
					sbData.append(sTemp);
					if ((i+1) != list.size()) sbData.append(sMultiFlag);
				}
			}
			sData = sbData.toString();
		}
		return sData;
	}
	
	public static String nodesToString(Element el, String sXpath, String regex){
		StringBuffer sbRefdes = new StringBuffer();
		List list = null;
		list = el.selectNodes(sXpath);
		for (int i = 0 ; i < list.size() ; i++){
			sbRefdes.append(list.get(i));
			if (i != list.size()) sbRefdes.append(regex);
		}
		return sbRefdes.toString();
	}
	
	public static String getXpathValue(Element el,String sXpath){
		String sData = null;
		Node node = el.selectSingleNode(sXpath);
		if (node != null){
			sData = node.getText();
		}
		return sData;
	}
	
	public static String getXpathValue(Document doc,String sXpath){
		String sData = null;
		Node node = doc.selectSingleNode(sXpath);
		if (node != null){
			sData = node.getText();
		}
		return sData;
	}
	
	public static List getXpathNodes(Document doc,String sXpath){
		List list = null;
		list = doc.selectNodes(sXpath);
		return list;
	}
	
	public static List getXpathNodes(Element el,String sXpath){
		List list = null;
		list = el.selectNodes(sXpath);
		return list;
	}
	
	public static void main(String[] args){
//		try {
//			Document doc = XmlUtil.loadXml("config.xml");
//			System.out.println("test");
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
			Document doc = XmlUtil.loadXml("agile.xml");
			Element el = (Element) XmlUtil.getXpathSingleNode(doc, "//Documents/Attachments[../PageThree/FlexAttributes/FlexAttribute/name = 'Specially Certificate Type']");
			String test = XmlUtil.getXpathValue(el, "../PageThree/FlexAttributes/FlexAttribute/value/Value", ",");
			System.out.println(test);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
//	public static String getAxmlValue(Document doc,String sXpath, String sMultiFlag){
//		Element el = doc.getRootElement();
//		String sData = null;
//		sData = getAxmlValue(el,sXpath, sMultiFlag);
//		return sData;
//	}
//	
//	public static String getAxmlValue(Element el,String sXpath, String sMultiFlag){
//		String sData = null;
//		StringBuffer sbData = new StringBuffer();
//		String sNameSpaceXpath = null;
//		XPath xpath = null;
//		HashMap xmlMap = new HashMap();
//		xmlMap.put("agile", "http://support.agile.com/misc/axml/2009/06/");
//		
//		if ((sMultiFlag == null) || (sMultiFlag.length() == 0)){
//			sNameSpaceXpath =  XmlUtil.xpathAddNameSpace(sXpath, "agile");
//			xpath = el.createXPath(sNameSpaceXpath);
//			xpath.setNamespaceContext(new SimpleNamespaceContext(xmlMap));
//			Node node = xpath.selectSingleNode(el);
//			if (node != null){
//				sData = node.getText();
//			}
//		}
//		else {
//			sNameSpaceXpath =  XmlUtil.xpathAddNameSpace(sXpath+"/*", "agile");
//			xpath = el.createXPath(sNameSpaceXpath);
//			xpath.setNamespaceContext(new SimpleNamespaceContext(xmlMap));
//			List list = xpath.selectNodes(el);
//			for (int i = 0 ; i < list.size() ; i ++){
//				Node node = (Node) list.get(i);
//				String sTemp = node.getText();
//				if (sTemp != null){ 
//					sbData.append(sTemp);
//					if ((i+1) != list.size()) sbData.append(sMultiFlag);
//				}
//			}
//			sData = sbData.toString();
//		}
//		return sData;
//	}
	

	
	
	
//	public static List getAxmlNodes(Document doc,String sXpath){
//		List list = null;
//		String sNameSpaceXpath =  XmlUtil.xpathAddNameSpace(sXpath, "agile");
//		XPath xpath = doc.createXPath(sNameSpaceXpath);
//		HashMap xmlMap = new HashMap();
//		xmlMap.put("agile", "http://support.agile.com/misc/axml/2009/06/");
//		xpath.setNamespaceContext(new SimpleNamespaceContext(xmlMap));
//		list = xpath.selectNodes(doc);
//		return list;
//	}
	
//	public static Node getAxmlSingleNode(Document doc,String sXpath){
//		Node node = null;
//		if (sXpath.equals("../")){
//			node = doc;
//		}else{
//			String sNameSpaceXpath =  XmlUtil.xpathAddNameSpace(sXpath, "agile");
//			XPath xpath = doc.createXPath(sNameSpaceXpath);
//			HashMap xmlMap = new HashMap();
//			xmlMap.put("agile", "http://support.agile.com/misc/axml/2009/06/");
//			xpath.setNamespaceContext(new SimpleNamespaceContext(xmlMap));
//			node = xpath.selectSingleNode(doc);
//		}
//		return node;
//	}
	
//	public static Node getAxmlSingleNode(Element el, String sXpath){
//		Node node = null;
//		if (sXpath.equals("../")){
//			node = el.getParent();
//		}else{
//		String sNameSpaceXpath =  XmlUtil.xpathAddNameSpace(sXpath, "agile");
//		XPath xpath = el.createXPath(sNameSpaceXpath);
//		HashMap xmlMap = new HashMap();
//		xmlMap.put("agile", "http://support.agile.com/misc/axml/2009/06/");
//		xpath.setNamespaceContext(new SimpleNamespaceContext(xmlMap));
//		node = xpath.selectSingleNode(el);
//		}
//		return node;
//	}
	
//	public static String getAxmlAttributeValue(Element parentEl, String sXpath, String sAttribute){
//		String sAttValue = null;
//		Element el = (Element) getAxmlSingleNode(parentEl, sXpath);
//		sAttValue = el.attributeValue(sAttribute);
//		return sAttValue;	
//	}
}
