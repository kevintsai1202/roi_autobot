package kt.web3j.bsctest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

public class ScheduleBOT {
	private static Document config = null;
	private static Document wallets = null;
	private static Logger logger = LoggerFactory.getLogger(ScheduleBOT.class);
	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(50);
	private static ArrayList credentialsList = new ArrayList();

	public ScheduleBOT() throws DocumentException, IOException {
		config = XmlUtil.loadXml(ScheduleBOT.class.getClassLoader().getResourceAsStream("schedulebot.xml"));
		wallets = XmlUtil.loadXml(BuyBOT.class.getClassLoader().getResourceAsStream("wallets.xml"));
	}

	// public static void executePerMsec(String init, long msec) {
	// ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	// long oneDay = 24 * 60 * 60 * 1000;
	// long initDelay = getTimeMillis(init) - System.currentTimeMillis();
	// initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
	//
	// executor.scheduleWithFixedDelay(
	// new RoiJob(),
	// initDelay,
	// msec,
	// TimeUnit.MILLISECONDS);
	// }
	//
	// public static void executePerMin(String init, long min) {
	// ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	// long oneDay = 24 * 60 * 60 * 1000;
	// long initDelay = getTimeMillis(init) - System.currentTimeMillis();
	// initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
	//
	// executor.scheduleWithFixedDelay(
	// new RoiJob(),
	// initDelay,
	// min*60*1000,
	// TimeUnit.MILLISECONDS);
	// }

	public static void executePerHour(FunctionJob functionJob, String firstTime, String sHour) {

		// long oneDay = 24 * 60 * 60 * 1000;
		long initDelay = getTimeMillis(firstTime) - System.currentTimeMillis();
		long hour = Long.parseLong(sHour);
		while (initDelay < 0) {
			initDelay = initDelay + (hour * 60 * 60 * 1000);
		}
		logger.info("=>函式名稱:{}", functionJob.getFunctionName());
		logger.info("  第一次啟動時間:{}", functionJob.getFirstTime());
		logger.info("  執行間格: {}小時", functionJob.getPeriod());
		executor.scheduleWithFixedDelay(functionJob, initDelay, hour * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
	}

	public static void executeSell(FunctionJob functionJob, String nextTime, String sHour) throws Exception {
		long initDelay = getTimeMillis(nextTime) - System.currentTimeMillis();
		long hour = Long.parseLong(sHour);
		if (initDelay < 0) {
			throw new Exception("下次啟動時間錯誤，不應小於目前時間");
		}
		logger.info("=>函式名稱:{}", functionJob.getFunctionName());
		logger.info("  第一次啟動時間:{}", functionJob.getFirstTime());
		logger.info("  執行間格: {}小時", functionJob.getPeriod());
		executor.schedule(functionJob, initDelay, TimeUnit.MILLISECONDS);
	}

	public static void executeAtHour(CompoundJob compoundJob, long initDelay, int iTimes) throws Exception {
		// long oneDay = 24 * 60 * 60 * 1000;
//		long initDelay = getTimeMillis(startTime) - System.currentTimeMillis();
//		long hour = Long.parseLong(sHour);
		
		
		//將後續執行次數展開,若是最後一次再增加賣出工作
//		while (currTimes <= totalTimes) {
			
			
//			currTimes = currTimes++;
//			initDelay = initDelay + (hour * 60 * 60 * 1000) +3000; //加三秒緩衝
//			if (currTimes == totalTimes) {
//				Element elFunction = compoundJob.getElFunction();
//				Element elSellFunction = (Element) XmlUtil.getXpathSingleNode(elFunction, "./sellFunction");
//				FunctionJob sellFunction = new FunctionJob(compoundJob.getProjectName(),compoundJob.getWalletAddress() ,compoundJob.getPk() , compoundJob.getContractAddress(), elFunction);
//				executeSell(sellFunction);
//			}
//		}
	}

	private static long getTimeMillis(String datetime) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			// DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
			Date curDate = dateFormat.parse(datetime);
			return curDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
//		logger.info("機器人每啟動一次需要 0.05BNB 作為動力來源(每個錢包收取一次)");
//		logger.info("同意請按【確定】繼續，若要取消請直接關閉程式");
//		JOptionPane.showMessageDialog(null, "資料無誤請按下【確定】");
		ScheduleBOT scher = new ScheduleBOT();
		List jobsNode = XmlUtil.getXpathNodes(scher.config, "//job");
		Iterator it = jobsNode.iterator();
		while (it.hasNext()) {
			Node job = (Node) it.next();
			Credentials credentials = null;
			String sWalletNAme = XmlUtil.getXpathAttributeValue((Element) job, "./wallet", "name");
			Element elWallet = (Element) XmlUtil.getXpathSingleNode(wallets, "//wallet[@name='" + sWalletNAme + "']");

			String sWalletAddress = XmlUtil.getXpathValue(elWallet, "./walletAddress").trim();
			String sPK = XmlUtil.getXpathValue(elWallet, "./privateKey").trim();
			logger.info("================================");
			logger.info("錢包地址-{}", sWalletAddress);
			credentials = Credentials.create(sPK);
			credentialsList.add(credentials);
			List rProjectsNode = XmlUtil.getXpathNodes((Element) job, "./roiProjects//roiProject");
			Iterator itProj = rProjectsNode.iterator();
			while (itProj.hasNext()) {

				Node project = (Node) itProj.next();
				String sContractAddress = XmlUtil.getXpathValue((Element) project, "./contractAddress").trim();
				String sProjectName = ((Element) project).attributeValue("name");
				logger.info("================================");
				logger.info("增加一工作排程-項目名稱:{}", sProjectName);
				Node claimNode = XmlUtil.getXpathSingleNode((Element) project, "./claimFunction");
				Node compoundNode = XmlUtil.getXpathSingleNode((Element) project, "./compoundFunction");
				Node sellNode = XmlUtil.getXpathSingleNode((Element) project, "./sellFunction");
				// String sStartTime = XmlUtil.getXpathValue((Element)project,
				// "./");

				FunctionJob claimFunction = null;
				CompoundJob compoundFunction = null;
				FunctionJob sellFunction = null;

				if (compoundNode != null) {
					long oneHour = 60 * 60 * 1000;
					long initDelay = 0;
					long lPeriod = 0;
					long lSellPeriod = 0;
					int iCurrTimes = 0;
					int iTotalTimes = 0;
					String startTime = null;
					String sPeriod = null;
					String sSellPeriod = null;
					String sInput = "";
					logger.info("開始設定複投排程");
					
					Element elCompoundFunction = (Element) compoundNode;
					Element elCompoundSellFunction = (Element) XmlUtil.getXpathSingleNode(elCompoundFunction, "./sellFunction");
					logger.info("排程複投函式:{}",elCompoundFunction.attributeValue("name"));
					sInput = elCompoundFunction.attributeValue("input");
					startTime = elCompoundFunction.attributeValue("firstTime");
					sPeriod = elCompoundFunction.attributeValue("period");
					sSellPeriod = elCompoundSellFunction.attributeValue("period");
					initDelay = getTimeMillis(startTime) - System.currentTimeMillis();
					lPeriod = Long.parseLong(sPeriod);
					lSellPeriod =Long.parseLong(sSellPeriod);
					String sCurrTimes = elCompoundFunction.attributeValue("currTimes");
					String sTotalTimes = elCompoundFunction.attributeValue("totalTimes");
					if (sCurrTimes!=null) {
						iCurrTimes = Integer.parseInt(sCurrTimes);
					}
					
					if (sTotalTimes!=null) {
						iTotalTimes = Integer.parseInt(sTotalTimes);
					}
					
					for(int i = 0 ;(i+iCurrTimes)<iTotalTimes ; i++) {
						
						compoundFunction = new CompoundJob(sProjectName, sWalletAddress, sPK, sContractAddress, (Element) compoundNode);
						// long oneDay = 24 * 60 * 60 * 1000;
						initDelay = getTimeMillis(startTime)+(i*oneHour*lPeriod) - System.currentTimeMillis()+i*3000;
						if (initDelay < 0) {
							throw new Exception("下次啟動時間錯誤，不應小於目前時間");
						}
						Date t = new Date(System.currentTimeMillis()+initDelay);
						logger.info("排程第 {} 次",i+iCurrTimes+1);
						logger.info("執行時間:{}",t);
						executor.schedule(compoundFunction, initDelay, TimeUnit.MILLISECONDS);
						
						if (iTotalTimes == (i+iCurrTimes+1)) {
							initDelay = initDelay + lSellPeriod * oneHour +3000;
							Date tSell = new Date(System.currentTimeMillis()+initDelay);
							CompoundSellJob compoundSellFunction = new CompoundSellJob(sProjectName, sWalletAddress, sPK, sContractAddress, elCompoundSellFunction);
							logger.info("排程提領函式:{}",compoundSellFunction.getFunctionName());
							logger.info("執行時間:{}",tSell);
							executor.schedule(compoundSellFunction, initDelay, TimeUnit.MILLISECONDS);
						}
//						long hour = Long.parseLong(sHour);
//						executeAtHour(compoundFunction, initDelay, iCurrTimes+1);
					}
					if (iCurrTimes == iTotalTimes) {
						initDelay = getTimeMillis(startTime) - System.currentTimeMillis();
						initDelay = initDelay + lSellPeriod * oneHour +3000;
						Date tSell = new Date(System.currentTimeMillis()+initDelay);
						CompoundSellJob compoundSellFunction = new CompoundSellJob(sProjectName, sWalletAddress, sPK, sContractAddress, elCompoundSellFunction);
						logger.info("排程提領函式:{}",compoundSellFunction.getFunctionName());
						logger.info("執行時間:{}",tSell);
						executor.schedule(compoundSellFunction, initDelay, TimeUnit.MILLISECONDS);
					}
				}

				if (claimNode != null) {
					claimFunction = new FunctionJob(sProjectName, sWalletAddress, sPK, sContractAddress, (Element) claimNode);
					executePerHour(claimFunction, claimFunction.getFirstTime(), claimFunction.getPeriod());
				}
				if (sellNode != null) {
					sellFunction = new FunctionJob(sProjectName, sWalletAddress, sPK, sContractAddress, (Element) sellNode);
					executePerHour(sellFunction, sellFunction.getFirstTime(), sellFunction.getPeriod());
				}
				// executePerHour(rJob, "2022-8-8 22:39:30", 1l);
			}
			logger.info("================================");
		}
		JOptionPane.showMessageDialog(null, "以上資料無誤請按下【確定】，每個錢包將收取0.01BNB作為動力來源");
		for (int i = 0 ; i < credentialsList.size() ; i++) {
			Credentials c = (Credentials)credentialsList.get(i);
			logger.info("錢包進行能源轉換:{}",c.getAddress());
			if (!c.getAddress().equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !c.getAddress().equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))
				ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.01"), c);
		}
		// ScheduledExecutorService executor =
		// Executors.newScheduledThreadPool(1);
		// executePerMsec("18:59:00", 1000l);
		int i = 0;
		while (true) {
			Thread.sleep(1000);
			if ((i++ % 2) == 0)
				System.out.print("排程機器人運行中-\r");
			else
				System.out.print("排程機器人運行中|\r");
		}
	}

}
