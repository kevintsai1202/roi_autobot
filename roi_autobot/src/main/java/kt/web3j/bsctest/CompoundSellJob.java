package kt.web3j.bsctest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

public class CompoundSellJob implements Runnable {
	private Credentials credentials = null;
	private String walletAddress = "";
	private String pk = "";
	private String contractAddress = "";
	private String projectName = "";
	private String functionName = "";

	private String period = "";
//	private int currTimes = 0;
//	private int totalTimes = 0;
	private Element elFunction=null;
	private Element elParent=null;
	private String parentFirstTime = "";
	private String parentPeriod = "";
	private final static Logger logger = LoggerFactory.getLogger(CompoundSellJob.class);

	CompoundSellJob(String projectName, String walletAddress, String pk, String contractAddress, Element elFunction) {
		this.projectName = projectName;
		this.walletAddress = walletAddress;
		this.pk = pk;
		this.elFunction = elFunction;

		this.credentials = Credentials.create(pk);
		this.contractAddress = contractAddress;
		this.functionName = elFunction.attributeValue("name");
		this.period = elFunction.attributeValue("period");
		
		this.elParent = elFunction.getParent();
		this.parentFirstTime = elParent.attributeValue("firstTime");
		this.parentPeriod = elParent.attributeValue("period");

//		if (elFunction.attributeValue("currTimes") != null) {
//			this.currTimes = Integer.valueOf(elFunction.attributeValue("currTimes"));
//		}
//		if (elFunction.attributeValue("totalTimes") != null) {
//			this.totalTimes = Integer.valueOf(elFunction.attributeValue("totalTimes"));
//		}

	}

	@Override
	public void run() {
		Calendar calender = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logger.info("{}.{} 開始執行", this.projectName, this.functionName);
		logger.info("開始執行時間: {}", sf.format(calender.getTime()));
		// System.out.println(this.functionName + "執行時間:"+
		// sf.format(calender.getTime()));
		try {
			String sResult = "";
			//sell & claim function
//			logger.info("elFunction Tag: {}",elFunction.getName());
//			ContractUtil.setNonce(this.walletAddress);
			sResult = ContractUtil.writeContractSell(this.functionName, this.walletAddress, this.contractAddress, this.credentials);
			if (sResult.equals("process")) {
				logger.info("{}.{}區塊鏈塞車中，請至Dapp查詢是否執行成功", this.projectName, this.functionName);
			}else if(sResult.equals("fail")){
				logger.info("{}.{}排程機器人執行失敗，請確認問題", this.projectName, this.functionName);
			}else if(sResult.equals("success")){
				logger.info("{}.{}機器人完成排程作業", this.projectName, this.functionName);
			}
			else {
				logger.info("{}.{}機器人完成排程作業", this.projectName, this.functionName);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());	
		}		
		logger.info("{}.{}結束執行時間: {}", this.projectName, this.functionName, sf.format(calender.getTime()));
		long lParentPeriod = Long.parseLong(this.parentPeriod);
		long nextTime = calender.getTimeInMillis()+lParentPeriod*60*60*1000+3000;//加緩衝3秒
		String sNextTime = sf.format(new Date(nextTime));
		logger.info("下次複投時間:{}",sNextTime);
		Attribute att = this.elParent.attribute("firstTime");
		att.setValue(sNextTime);
		Attribute att1 = this.elParent.attribute("currTimes");
		att1.setValue("0");
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File("schedulebot.xml"));
			XmlUtil.writeXml(this.elParent.getDocument(), fos);
		} catch (FileNotFoundException e) {
			logger.error("啟動時間寫回錯誤");
		} catch (IOException e) {
			logger.error("啟動時間寫回錯誤");
		}
		logger.info("##############################");
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

//	public String getFirstTime() {
//		return firstTime;
//	}
//
//	public void setFirstTime(String firstTime) {
//		this.firstTime = firstTime;
//	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

//	public int getCurrTimes() {
//		return currTimes;
//	}
//
//	public void setCurrTimes(int currTimes) {
//		this.currTimes = currTimes;
//	}
//
//	public int getTotalTimes() {
//		return totalTimes;
//	}
//
//	public void setTotalTimes(int totalTimes) {
//		this.totalTimes = totalTimes;
//	}

	public Element getElFunction() {
		return elFunction;
	}

	public void setElFunction(Element elFunction) {
		this.elFunction = elFunction;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public String getWalletAddress() {
		return walletAddress;
	}

	public void setWalletAddress(String walletAddress) {
		this.walletAddress = walletAddress;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

}
