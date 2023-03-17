package kt.web3j.bsctest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;

public class EmergencyBOT {
	private static Document config = null;
	private static Document wallets = null;
	private Credentials credentials = null;
	private String projectName = "";
	private String walletAddress = "";
	private String contractAddress = "";
	private String checkFunction = "";
	private String emergencyFunction = "";
	// private String freeFunction = "";
	// private String amount = "";
	// private String token = "BNB";
	// private String type = "payable";
	// private String transferAmount = "";
	private int iSleep = 1000;
	// private BigInteger biBoost = new BigInteger("1");
	// private boolean approveToken = true;
	private Logger logger = LoggerFactory.getLogger(EmergencyBOT.class);
	// private boolean needCheck = true;
	private long underLimit = 0;

	EmergencyBOT() throws DocumentException {
		config = XmlUtil.loadXml(EmergencyBOT.class.getClassLoader().getResourceAsStream("EmergencyBot.xml"));
		wallets = XmlUtil.loadXml(EmergencyBOT.class.getClassLoader().getResourceAsStream("wallets.xml"));
		try {
			setAttribute();
		} catch (Exception e) {
			logger.info("按下Enter退出程式");
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			System.exit(0);
		}
	}

	private void setAttribute() throws Exception {
		// Node walletNode = XmlUtil.getXpathSingleNode(config, "//wallet");
		String sWalletNAme = XmlUtil.getXpathAttributeValue(config, "//wallet", "name");

		Node roiProjectNode = XmlUtil.getXpathSingleNode(config, "//roiProject");
		Element elWallet = (Element) XmlUtil.getXpathSingleNode(wallets, "//wallet[@name='" + sWalletNAme + "']");
		Element elRoiProject = (Element) roiProjectNode;

		this.projectName = elRoiProject.attributeValue("name");
		if (this.projectName != null)
			this.projectName = this.projectName.trim();
		// System.out.println("執行間格時間為:"+iSleep+"毫秒");
		String pk = XmlUtil.getXpathValue(elWallet, "./privateKey").trim();
		if (pk != null) {
			pk = pk.trim();
			this.credentials = Credentials.create(pk);
		}
		this.walletAddress = XmlUtil.getXpathValue(elWallet, "./walletAddress");
		if (this.walletAddress != null)
			this.walletAddress = this.walletAddress.trim();
		this.contractAddress = XmlUtil.getXpathValue(elRoiProject, "./contractAddress").trim();
		if (this.contractAddress != null)
			this.contractAddress = this.contractAddress.trim();
		this.checkFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./checkFunction", "name");
		if (this.checkFunction != null)
			this.checkFunction = this.checkFunction.trim();

		String sUnderLimit = XmlUtil.getXpathAttributeValue(elRoiProject, "./checkFunction", "underLimit");
		if (sUnderLimit != null) {
			this.underLimit = Long.parseLong(sUnderLimit.trim());
		}

		Node emergencyFunction = XmlUtil.getXpathSingleNode(elRoiProject, "./emergencyFunction");
		// Node nodefreeFunction = XmlUtil.getXpathSingleNode(elRoiProject,
		// "./freeFunction");
		if (emergencyFunction != null) {
			this.emergencyFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./emergencyFunction", "name");
			if (this.emergencyFunction != null) {
				this.emergencyFunction = this.emergencyFunction.trim();
			}

			logger.info("緊急提領錢包為:{}", this.walletAddress);
		}

		logger.info("偵測間隔為:{}毫秒", iSleep);
		logger.info("以上資料正確請再彈出視窗按下【確定】，資料錯誤或想退出請直接關閉");
		JOptionPane.showMessageDialog(null, "資料無誤請按下【確定】");
	}

	private BigDecimal checkData() throws Exception {
		List<Type> result = ContractUtil.readContract(this.checkFunction, this.walletAddress, this.contractAddress);
		Uint256 balance = null;
		if (result == null) {
			throw new Exception("無法取得餘額!!");
		} else {
			balance = (Uint256) result.get(0);
			return Convert.fromWei(balance.getValue().toString(), Convert.Unit.ETHER);
		}
	}

	public static void main(String[] args) throws DocumentException {
		EmergencyBOT bot;
		// List<Type> result;
		BigDecimal checkData;
		int iCount = 0;
		String emergencyResult = null;
		// String freResult = null;

		bot = new EmergencyBOT();
		bot.logger.info("緊急提領機器人初始化完成，開始運行");
		// System.out.println("搶頭礦機器人初始化完成，開始運行");

		while (true) {
			try {
				checkData = bot.checkData();
				long lcheckData = checkData.longValue();
				if (lcheckData > bot.underLimit) {
					Thread.sleep(bot.iSleep);
					bot.logger.info("項目:{} 機器人偵測次數:{}, 目前餘額為:{}", bot.projectName, iCount++, lcheckData);
					// System.out.println("機器人偵測次數:"+iCount++);
					continue;
				} else {
					bot.logger.info("目前餘額:{}，低於緊戒值，開始緊急提領", lcheckData);
					if ((bot.emergencyFunction != null) && (bot.emergencyFunction.length() != 0)) {
						emergencyResult = ContractUtil.writeContractNoPara(bot.emergencyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
						if (emergencyResult.equals("success")) {
							bot.logger.info("緊急提領完成");
						} else if (emergencyResult.equals("fail")) {
							bot.logger.info("緊急提領失敗，請手動提領");
						} else {
							bot.logger.info("機器人運作過久，請透過Hash Code查詢");
						}
						bot.logger.info("機器人完成緊急提領");
						// System.out.println("機器人完成頭礦搶購");
						bot.logger.info("Emergency Result:{}", emergencyResult);
						// System.out.println(buyResult);
						JOptionPane.showMessageDialog(null, "餘額低於警戒，已進行緊急提領");
					}
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				bot.logger.error("緊急提領機器人發生異常",e);
			}
		}
	}

}
