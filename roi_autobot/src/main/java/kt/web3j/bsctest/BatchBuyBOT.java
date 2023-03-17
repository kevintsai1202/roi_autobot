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

public class BatchBuyBOT {
	private static Document config = null;
	private static Document wallets = null;
	private Credentials credentials = null;
	private String walletAddress = "";
	private String contractAddress = "";
	private String checkFunction = "";
	private String batchBuyFunction = "";
	private String freeFunction = "";
	private String amount = "";
	private String token = "BNB";
	private String type = "payable";
	// private String transferAmount = "";
	private int iSleep = 500;
	private int iDuration = 3000;
	private int iTimes = 0;
	private BigInteger biBoost = new BigInteger("1");
	private boolean approveToken = true;
	private Logger logger = LoggerFactory.getLogger(BatchBuyBOT.class);
	private boolean needCheck = true;
	private long upperLimit = 0;

	BatchBuyBOT() throws DocumentException {
		config = XmlUtil.loadXml(BatchBuyBOT.class.getClassLoader().getResourceAsStream("batchbuybot.xml"));
		wallets = XmlUtil.loadXml(BatchBuyBOT.class.getClassLoader().getResourceAsStream("wallets.xml"));
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

		String sSleep = XmlUtil.getXpathValue(config, "//sleep");
		if (sSleep != null) {
			sSleep = sSleep.trim();
			this.iSleep = Integer.valueOf(sSleep);

		}
		
		String sDuration = XmlUtil.getXpathValue(config, "//duration");
		if (sDuration != null) {
			sDuration = sDuration.trim();
			this.iDuration = Integer.valueOf(sDuration);
		}
		
		iTimes = this.iDuration/this.iSleep;
//		BigInteger biTimes = new BigInteger(String.valueOf(iTimes));
		
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

		Node nodeBuyFunction = XmlUtil.getXpathSingleNode(elRoiProject, "./batchBuyFunction");
		Node nodefreeFunction = XmlUtil.getXpathSingleNode(elRoiProject, "./freeFunction");
		if (nodeBuyFunction != null) {
			this.batchBuyFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./batchBuyFunction", "name");
			if (this.batchBuyFunction != null) {
				this.batchBuyFunction = this.batchBuyFunction.trim();
			}
			this.amount = XmlUtil.getXpathAttributeValue(elRoiProject, "./batchBuyFunction", "amount");
			if (this.amount != null)
				this.amount = this.amount.trim();
			this.token = XmlUtil.getXpathAttributeValue(elRoiProject, "./batchBuyFunction", "token");
			if (this.token != null)
				this.token = this.token.trim();
			this.type = XmlUtil.getXpathAttributeValue(elRoiProject, "./batchBuyFunction", "type");
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
			logger.info("upperLimit:{}，upperLimit為0，採用啟動變數偵測", this.upperLimit, this.batchBuyFunction);
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

//	private boolean checkInit() {
//		boolean bReturn = ContractUtil.readContractBool(this.checkFunction, this.walletAddress, this.contractAddress);
//		return bReturn;
//	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		BatchBuyBOT bot;
		// List<Type> result;
		BigDecimal checkData;
//		boolean checkInit = false;
		int iCount = 0;
		String buyResult = null;
		String freResult = null;

		bot = new BatchBuyBOT();
		bot.logger.info("搶頭礦機器人初始化完成，開始運行");
		JOptionPane.showMessageDialog(null, "因批次搶頭礦需耗費大量瓦斯，請先確認瓦斯費足夠，若超過執行區間或瓦斯費不足都會停止執行，每次基本交易瓦斯費約0.0002BNB，執行120次就會消耗0.024BNB");
		// System.out.println("搶頭礦機器人初始化完成，開始運行");
		BigInteger biNonce = ContractUtil.fetchNonce(bot.walletAddress);
		
		if ((!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA")) ) {
			JOptionPane.showMessageDialog(null, "批次搶礦需先支付 0.1BNB 機器人開發能源，按下【確定】即開始轉帳，若要停止請直接關閉程式");
			ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.1"), bot.credentials);
		}
		bot.logger.info("機器人開始搶頭礦");
		while (true) {
			try {
				iCount++;
				if (iCount > bot.iTimes) {
					break;
				}
				biNonce = biNonce.add(BigInteger.ONE);
				ContractUtil.setNonce(biNonce);
				if (bot.needCheck) {
					checkData = bot.checkData();
					if (checkData.equals(BigDecimal.ZERO)) {
						bot.logger.info("機器人交易次數:{}", iCount++);
						if ((bot.batchBuyFunction != null) && (bot.batchBuyFunction.length() != 0)) {
							if (bot.type.equalsIgnoreCase("payable")) {
								buyResult = ContractUtil.writeContractBatchBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
										new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
							} else if (bot.type.equalsIgnoreCase("input")) {
								buyResult = ContractUtil.writeContractBatchBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
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
						
						// System.out.println("機器人偵測次數:"+iCount++);
						Thread.sleep(bot.iSleep);
						continue;
					} else {
						long lcheckData = checkData.longValue();
						if (lcheckData < bot.upperLimit) {
							if ((bot.batchBuyFunction != null) && (bot.batchBuyFunction.length() != 0)) {
								if (bot.type.equalsIgnoreCase("payable")) {
									buyResult = ContractUtil.writeContractBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
											new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
								} else if (bot.type.equalsIgnoreCase("input")) {
									buyResult = ContractUtil.writeContractBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
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
								Thread.sleep(bot.iSleep);
								continue;
							}
							if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
								freResult = ContractUtil.writeContractNoPara(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
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
				}
				else {
					if ((bot.batchBuyFunction != null) && (bot.batchBuyFunction.length() != 0)) {
						if (bot.type.equalsIgnoreCase("payable")) {
							buyResult = ContractUtil.writeContractBatchBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
									new Address("0xc0C3C272f2b998F17Ad6085376663b39A72467EE"), bot.token, bot.amount, bot.biBoost, false);
						} else if (bot.type.equalsIgnoreCase("input")) {
							buyResult = ContractUtil.writeContractBatchBuy(bot.batchBuyFunction, bot.walletAddress, bot.contractAddress, bot.credentials,
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
						Thread.sleep(bot.iSleep);
						continue;

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
				bot.logger.warn(e.getMessage());
			}
		}
		bot.logger.info("請按下 Enter 退出");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();

	}

}
