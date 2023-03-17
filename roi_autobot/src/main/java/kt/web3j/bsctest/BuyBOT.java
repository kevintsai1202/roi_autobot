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
			logger.error("���UEnter�h�X�{��");
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

		// System.out.println("���涡��ɶ���:"+iSleep+"�@��");
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
				logger.info("�N�����v���p:{}", approveStatus);
			}
			logger.info("�m�ʿ��]��:{}", this.walletAddress);
			logger.info("�m�ʪ��B��:{}", this.amount);
			logger.info("�ϥΥN����:{}", this.token);
			logger.info("�N���ǿ�覡��:{}", this.type);
		}
		if (nodefreeFunction != null) {
			this.freeFunction = XmlUtil.getXpathAttributeValue(elRoiProject, "./freeFunction", "name");
			if (this.freeFunction != null) {
				this.freeFunction = this.freeFunction.trim();
			}
		}

		boolean hasBalance = ContractUtil.checkBalance(this.walletAddress, this.token, this.amount, this.credentials);
		if (hasBalance) {
			logger.info("���]�l�B����q�L�A���L�m�Y�q�O�i�����˴��O�z�W�A��ĳ�h��@�� BNB �b���]��");
		} else {
			logger.error("���]�l�B�����A�N�h�X�{��");
			throw new Exception("���]�l�B����");
		}
		logger.info("�O�_���氻���禡:{}", this.needCheck);
		if (this.upperLimit != 0) {
			logger.info("upperLimit:{}�AupperLimit����0�A�ĥξl�B����", this.upperLimit);
		} else {
			logger.info("upperLimit:{}�AupperLimit��0�A�ĥαҰ��ܼư���", this.upperLimit, this.buyFunction);
		}
		logger.info("�������j��:{}�@��", iSleep);
		logger.info("�m�Y�q�����H�|�b�����m�ʫ᦬�� 0.05BNB �@�����H�B�@�κ��@�O�ΡA���ѱN������");
		logger.info("�нT�O�m�ʪ��B�[�W�����H�B�@�O�Ϋ��٦��������˴��A�˴��O�����N�|���楢��");
		logger.info("�H�W��ƥ��T�ЦA�u�X�������U�i�T�w�j�A��ƿ��~�ηQ�h�X�Ъ�������");
		JOptionPane.showMessageDialog(null, "��ƵL�~�Ы��U�i�T�w�j");
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
		bot.logger.info("�m�Y�q�����H��l�Ƨ����A�}�l�B��");
		// System.out.println("�m�Y�q�����H��l�Ƨ����A�}�l�B��");

		while (true) {
			try {
				ContractUtil.setNonce(ContractUtil.fetchNonce(bot.walletAddress));
				if (bot.needCheck) {
					if (bot.upperLimit != 0) {
						checkData = bot.checkData();
						if (checkData.equals(BigDecimal.ZERO)) {
							Thread.sleep(bot.iSleep);
							checkData = bot.checkData();
							bot.logger.info("�����H��������:{}", iCount++);
							// System.out.println("�����H��������:"+iCount++);
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
										bot.logger.info("�ǿ�覡��J���~�A�����ʶR");
									}
									if (buyResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA")) ) {
										ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
										bot.logger.info("�����H�Y�q�ʶR���\");
									} else if (buyResult.equals("fail")) {
										bot.logger.info("�����H�Y�q�ʶR���ѡA�Э��s����Τ�ʻ��");
									} else {
										bot.logger.info("�����H�B�@�L�[�A�гz�LHash Code�d��");
									}
									// System.out.println("�����H�����Y�q�m��");
									bot.logger.info("Buy Result:{}", buyResult);
									// System.out.println(buyResult);
								}
								if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
									freResult = ContractUtil.writeContractNoPara(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
									if (freResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
										ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
										bot.logger.info("�����H�����K�O�q�u���o");
									} else if (freResult.equals("fail")) {
										bot.logger.info("�����K�O�q�u���o���ѡA�Э��s����Τ�ʻ��");
									} else {
										bot.logger.info("�����H�B�@�L�[�A�гz�LHash Code�d��");
									}
									bot.logger.info("Buy Result:{}", freResult);
									// System.out.println("�����H�����Y�q�m��");
								}
							} else {
								bot.logger.info("�X������W�L:{}�A�`�B�ثe��:{},�����ʶR", bot.upperLimit, lcheckData);
								// System.out.println("�X���l�B���B�Ӥj�A�����ʶR");
							}
							break;
						}//!checkData.equals(BigDecimal.ZERO)
					} //bot.upperLimit != 0
					else {
						checkInit = bot.checkInit();
						if (!checkInit) {
							Thread.sleep(bot.iSleep);
							checkInit = bot.checkInit();
							bot.logger.info("�����H��������:{}", iCount++);
							// System.out.println("�����H��������:"+iCount++);
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
									bot.logger.info("�ǿ�覡��J���~�A�����ʶR");
								}
								if (buyResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
									ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
									bot.logger.info("�����H�Y�q�ʶR���\");
								} else if (buyResult.equals("fail")) {
									bot.logger.info("�����H�Y�q�ʶR���ѡA�Э��s����Τ�ʻ��");
								} else {
									bot.logger.info("�����H�B�@�L�[�A�гz�LHash Code�d��");
								}
								// System.out.println("�����H�����Y�q�m��");
								bot.logger.info("Buy Result:{}", buyResult);
								// System.out.println(buyResult);
							}
							if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
								freResult = ContractUtil.writeContractNoPara(bot.buyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
								if (freResult.equals("success") && (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
									ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
									bot.logger.info("�����H�����K�O�q�u���o");
								} else if (freResult.equals("fail")) {
									bot.logger.info("�����K�O�q�u���o���ѡA�Э��s����Τ�ʻ��");
								} else {
									bot.logger.info("�����H�B�@�L�[�A�гz�LHash Code�d��");
								}
								bot.logger.info("Buy Result:{}", freResult);
								// System.out.println("�����H�����Y�q�m��");
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
							bot.logger.info("�ǿ�覡��J���~�A�����ʶR");
						}
						if (buyResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
							ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
							bot.logger.info("�����H���Q�����Y�q�m��");
						} else if (buyResult.equals("fail")) {
							bot.logger.info("�����Y�q�m�ʥ��ѡA�Э��s����Τ���ʶR");
						} else if (buyResult.equals("unknow")) {
							bot.logger.info("���檬�p�L�k���o�A�гz�LHash Code�d��");
						}
						bot.logger.info("�����H�����B��A���浲�G��:{}", buyResult);
						// System.out.println("�����H�����Y�q�m��");
						// bot.logger.info("Buy Result:{}", buyResult);
						// System.out.println(buyResult);

					}
					if ((bot.freeFunction != null) && (bot.freeFunction.length() != 0)) {
						freResult = ContractUtil.writeContractNoPara(bot.freeFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
						// System.out.println("�����H�����Y�q�m��");
						if (freResult.equals("success")&& (!bot.walletAddress.equalsIgnoreCase("0xc0C3C272f2b998F17Ad6085376663b39A72467EE") && !bot.walletAddress.equalsIgnoreCase("0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA"))) {
							ContractUtil.transaction("0xc0C3C272f2b998F17Ad6085376663b39A72467EE", Double.valueOf("0.05"), bot.credentials);
							bot.logger.info("�����H�����K�O�q�u���o");
						} else if (freResult.equals("fail")) {
							bot.logger.info("�����K�O�q�u���o���ѡA�Э��s����Τ�ʻ��");
						} else if (freResult.equals("unknow")) {
							bot.logger.info("���檬�p�L�k���o�A�гz�LHash Code�d��");
						}
						bot.logger.info("�����H�����B��A���浲�G��:{}", freResult);
					}
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				bot.logger.error(e.getMessage());
			}
		}
		bot.logger.info("�Ы��U Enter �h�X");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();

	}

}
