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
	// Pretty print the document to ��X�y
			OutputFormat format = OutputFormat.createPrettyPrint();
			/*
			 * // Compact format to ��X�y OutputFormat format =
			 * OutputFormat.createCompactFormat();
			 */

			// �o�زo�A��������s�X�ഫ���D�G
			// �ϥΥH�W��kŪ��xml���o�쪺Document�A����xml���O���ؽs�X�A�bŪ�줺�s�س��|�ܦ�UTF-8
			// �Ӧb�NDocument�g�^���ɡA�p�G��new XMLWriter(new FileOutputStream(new
			// File("...")),
			// FileOutputStream�ҳX�ݪ��X��O���a�q�{����X��A�Ygb2312�C��FileOutputStream�]�@�بS�����w��X�s�X�Э㪺�c�y��k
			// �ҥH�A�ݭn�����ɧUOutputStreamWriter�Ӷi���X�y���s�X�����ഫ�C
			// �o�ˡA�N�i�H����UTF-8�g�^��xml���F�C
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
	
//�אּ�NNS�h��
//	public static String xpathAddNameSpace(String sXpath, String sNameSpace){
//		String sFinalXpath = sXpath;
//		
//		sFinalXpath = sFinalXpath.replace("/..", "$");//����/..�Ÿ�����$,�ݳB�z��/�A��^
//		sFinalXpath = sFinalXpath.replace("//", "^");//����//�Ÿ�����!,�ݳB�z��/�A�B�z!
//		sFinalXpath = sFinalXpath.replace("/", "/"+sNameSpace+":");
//		sFinalXpath = sFinalXpath.replace("^", "//"+sNameSpace+":");
//		sFinalXpath = sFinalXpath.replace("$", "/..");
//		
//		sFinalXpath = sFinalXpath.replace("[@", "!");//�קK�~���[@,���B�z��[�A��^
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
