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

public class BuyBOT {
	private static Document config = null;
	private static Document wallets = null;
	private Credentials credentials = null;
	private String walletAddress = "";
	private String contractAddress = "";
	private String checkFunction = "";
	private String buyFunction = "";
	private String freeFunction = "";
	private String amount = "";
	private String token = "BNB";
	private String type = "payable";
	// private String transferAmount = "";
	private int iSlot = 0;
	private int iSleep = 500;
	private BigInteger biBoost = new BigInteger("1");
	private boolean approveToken = true;
	private Logger logger = LoggerFactory.getLogger(BuyBOT.class);
	private boolean needCheck = true;
	private long upperLimit = 0;

	BuyBOT() throws DocumentException {
		config = XmlUtil.loadXml(BuyBOT.class.getClassLoader().getResourceAsStream("buybot.xml"));
		wallets = XmlUtil.loadXml(BuyBOT.class.getClassLoader().getResourceAsStream("wallets.xml"));
		try {
			ContractUtil.init();
			setAttribute();
		} catch (Exception e) {
			logger.error("按下Enter退出程式");
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

		String sSleep = XmlUtil.getXpathValue(config, "//sleep");
		if (sSleep != null) {
			sSleep = sSleep.trim();
			this.iSleep = Integer.valueOf(sSleep);

		}
		String sBoostTmp = XmlUtil.getXpathValue(config, "//gasBoost");
		if (sBoostTmp != null) {
			sBoostTmp = sBoostTmp.trim();
			this.biBoost = new BigInteger(sBoostTmp);
		}

		String sApproveToken = XmlUtil.getXpathValue(config, "//approveToken").trim();
		if (sApproveToken != null) {
			sApproveToken = sApproveToken.trim();
			this.approveToken = Boolean.parseBoolean(sApproveToken);
		}

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
		
		String sSlot = XmlUtil.getXpathAttributeValue(elRoiProject, "./checkFunction", "slot");
		if (sSlot != null) {
			this.iSlot = Integer.parseInt(sSlot.trim());
		}

		this.freeFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./freeFunction", "name");
		if (this.freeFunction != null)
			this.freeFunction = this.freeFunction.trim();

		String sUpperLimit = XmlUtil.getXpathAttributeValue(elRoiProject, "./checkFunction", "upperLimit");
		if (sUpperLimit != null) {
			this.upperLimit = Long.parseLong(sUpperLimit.trim());
		}

		String sNeedCheck = XmlUtil.getXpathAttributeValue(elRoiProject, "./checkFunction", "active");
		if (sNeedCheck != null) {
			this.needCheck = Boolean.valueOf(sNeedCheck.trim());
		}

		Node nodeBuyFunction = XmlUtil.getXpathSingleNode(elRoiProject, "./buyFunction");
		Node nodefreeFunction = XmlUtil.getXpathSingleNode(elRoiProject, "./freeFunction");
		if (nodeBuyFunction != null) {
			this.buyFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./buyFunction", "name");
			if (this.buyFunction != null) {
				this.buyFunction = this.buyFunction.trim();
			}
			this.amount = XmlUtil.getXpathAttributeValue(elRoiProject, "./buyFunction", "amount");
			if (this.amount != null)
				this.amount = this.amount.trim();
			this.token = XmlUtil.getXpathAttributeValue(elRoiProject, "./buyFunction", "token");
			if (this.token != null)
				this.token = this.token.trim();
			this.type = XmlUtil.getXpathAttributeValue(elRoiProject, "./buyFunction", "type");
			if (this.type != null)
				this.type = this.type.trim();
			if (this.approveToken && !this.token.equalsIgnoreCase("BNB")) {
				String approveStatus = ContractUtil.approve(this.token, this.amount, this.contractAddress, this.credentials);
				logger.info("代幣授權狀況:{}", approveStatus);
			}
			logger.info("搶購錢包為:{}", this.walletAddress);
			logger.info("搶購金額為:{}", this.amount);
			logger.info("使用代幣為:{}", this.token);
			logger.info("代幣傳輸方式為:{}", this.type);
		}
		if (nodefreeFunction != null) {
			this.freeFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./freeFunction", "name");
			if (this.freeFunction != null) {
				this.freeFunction = this.freeFunction.trim();
			}
		}

		boolean hasBalance = ContractUtil.checkBalance(this.walletAddress, this.token, this.amount, this.credentials);
		if (hasBalance) {
			logger.info("錢包餘額檢驗通過，不過搶頭礦費可能讓瓦斯費爆增，建議多放一些 BNB 在錢包內");
		} else {
			logger.error("錢包餘額不足，將退出程式");
			throw new Exception("錢包餘額不足");
		}
		logger.info("是否執行偵測函式:{}", this.needCheck);
		if (this.upperLimit != 0) {
			logger.info("upperLimit:{}，upperLimit不為0，採用餘額偵測", this.upperLimit);
		} else {
			logger.info("upperLimit:{}，upperLimit為0，採用啟動變數偵測", this.upperLimit, this.buyFunction);
		}
		logger.info("偵測間隔為:{}毫秒", iSleep);
		logger.info("搶頭礦機器人會在完成搶購後收取 0.05BNB 作機器人運作及維護費用，失敗將不收取");
		logger.info("請確保搶購金額加上機器人運作費用後還有足夠的瓦斯，瓦斯費不夠將會執行失敗");
		logger.info("以上資料正確請再彈出視窗按下【確定】，資料錯誤或想退出請直接關閉");
		JOptionPane.showMessageDialog(null, "資料無誤請按下【確定】");
//		Scanner sc = new Scanner(System.in);
//		sc.nextLine();
	}

	private BigDecimal checkData() throws Exception {
		List<Type> result = ContractUtil.readContract(this.checkFunction, this.walletAddress, this.contractAddress);
		Uint256 balance = null;
		if (result == null) {
			return BigDecimal.ZERO;
		} else {
			balance = (Uint256) result.get(0);
			return Convert.fromWei(balance.getValue().toString(), Convert.Unit.ETHER);
		}
	}

	private boolean checkInit() {
		boolean bReturn = ContractUtil.readContractBool(this.checkFunction, this.walletAddress, this.contractAddress);
		return bReturn;
	}

	public static void main(String[] args) throws DocumentException {
		BuyBOT bot;
		// List<Type> result;
		BigDecimal checkData;
		boolean checkInit = false;
		int iCount = 0;
		String buyResult = null;
		String freResult = null;

		bot = new BuyBOT();
		bot.logger.info("搶頭礦機器人初始化完成，開始運行");
		// System.out.println("搶頭礦機器人初始化完成，開始運行");

		while (true) {
			try {
				ContractUtil.setNonce(ContractUtil.fetchNonce(bot.walletAddress));
				if (bot.needCheck) {
					if (bot.upperLimit != 0) {
						checkData = bot.checkData();
						if (checkData.equals(BigDecimal.ZERO)) {
							Thread.sleep(bot.iSleep);
							checkData = bot.checkData();
							bot.logger.info("機器人偵測次數:{}", iCount++);
							// System.out.println("機器人偵測次數:"+iCount++);
							continue;
						} else {
							long lcheckData = checkData.longValue();
							if (lcheckData < bot.upperLimit) {
								if ((bot.buyFunction != null) && (bot.buyFunction.length() != 0)) {
									if (bot.type.equalsIgnoreCase("payable")) {
										buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
												new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
									} else if (bot.type.equalsIgnoreCase("input")) {
										buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
												new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, true);
									} else {
										bot.logger.info("傳輸方式輸入錯誤，結束購買");
									}
									if (buyResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA")) ) {
										ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
										bot.logger.info("機器人頭礦購買成功");
									} else if (buyResult.equals("fail")) {
										bot.logger.info("機器人頭礦購買失敗，請重新執行或手動領取");
									} else {
										bot.logger.info("機器人運作過久，請透過Hash Code查詢");
									}
									// System.out.println("機器人完成頭礦搶購");
									bot.logger.info("Buy Result:{}", buyResult);
									// System.out.println(buyResult);
								}
								if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
									freResult = ContractUtil.writeContractNoPara(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
									if (freResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
										ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
										bot.logger.info("機器人完成免費礦工取得");
									} else if (freResult.equals("fail")) {
										bot.logger.info("機器免費礦工取得失敗，請重新執行或手動領取");
									} else {
										bot.logger.info("機器人運作過久，請透過Hash Code查詢");
									}
									bot.logger.info("Buy Result:{}", freResult);
									// System.out.println("機器人完成頭礦搶購");
								}
							} else {
								bot.logger.info("合約資金超過:{}，總額目前為:{},取消購買", bot.upperLimit, lcheckData);
								// System.out.println("合約餘額金額太大，取消購買");
							}
							break;
						}//!checkData.equals(BigDecimal.ZERO)
					} //bot.upperLimit != 0
					else {
						checkInit = bot.checkInit();
						if (!checkInit) {
							Thread.sleep(bot.iSleep);
							checkInit = bot.checkInit();
							bot.logger.info("機器人偵測次數:{}", iCount++);
							// System.out.println("機器人偵測次數:"+iCount++);
							continue;
						} 
						else {// true
							if ((bot.buyFunction != null) && (bot.buyFunction.length() != 0)) {
								if (bot.type.equalsIgnoreCase("payable")) {
									buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
											new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
								} else if (bot.type.equalsIgnoreCase("input")) {
									buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
											new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, true);
								} else {
									bot.logger.info("傳輸方式輸入錯誤，結束購買");
								}
								if (buyResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
									ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
									bot.logger.info("機器人頭礦購買成功");
								} else if (buyResult.equals("fail")) {
									bot.logger.info("機器人頭礦購買失敗，請重新執行或手動領取");
								} else {
									bot.logger.info("機器人運作過久，請透過Hash Code查詢");
								}
								// System.out.println("機器人完成頭礦搶購");
								bot.logger.info("Buy Result:{}", buyResult);
								// System.out.println(buyResult);
							}
							if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
								freResult = ContractUtil.writeContractNoPara(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
								if (freResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
									ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
									bot.logger.info("機器人完成免費礦工取得");
								} else if (freResult.equals("fail")) {
									bot.logger.info("機器免費礦工取得失敗，請重新執行或手動領取");
								} else {
									bot.logger.info("機器人運作過久，請透過Hash Code查詢");
								}
								bot.logger.info("Buy Result:{}", freResult);
								// System.out.println("機器人完成頭礦搶購");
							}
							break;
						}
					}
				}
				else {
					if ((bot.buyFunction != null) && (bot.buyFunction.length() != 0)) {
						if (bot.type.equalsIgnoreCase("payable")) {
							buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
									new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
						} else if (bot.type.equalsIgnoreCase("input")) {
							buyResult = ContractUtil.writeContractBuy(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
									new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, true);
						} else {
							bot.logger.info("傳輸方式輸入錯誤，結束購買");
						}
						if (buyResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
							ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
							bot.logger.info("機器人順利完成頭礦搶購");
						} else if (buyResult.equals("fail")) {
							bot.logger.info("機器頭礦搶購失敗，請重新執行或手動購買");
						} else if (buyResult.equals("unknow")) {
							bot.logger.info("執行狀況無法取得，請透過Hash Code查詢");
						}
						bot.logger.info("機器人結束運行，執行結果為:{}", buyResult);
						// System.out.println("機器人完成頭礦搶購");
						// bot.logger.info("Buy Result:{}", buyResult);
						// System.out.println(buyResult);

					}
					if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
						freResult = ContractUtil.writeContractNoPara(bot.freeFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
						// System.out.println("機器人完成頭礦搶購");
						if (freResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
							ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
							bot.logger.info("機器人完成免費礦工取得");
						} else if (freResult.equals("fail")) {
							bot.logger.info("機器免費礦工取得失敗，請重新執行或手動領取");
						} else if (freResult.equals("unknow")) {
							bot.logger.info("執行狀況無法取得，請透過Hash Code查詢");
						}
						bot.logger.info("機器人結束運行，執行結果為:{}", freResult);
					}
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				bot.logger.error(e.getMessage());
			}
		}
		bot.logger.info("請按下 Enter 退出");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();

	}

}
