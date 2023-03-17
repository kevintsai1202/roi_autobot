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
			logger.info("���UEnter�h�X�{��");
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

			logger.info("��洣����]��:{}", this.walletAddress);
		}

		logger.info("�������j��:{}�@��", iSleep);
		logger.info("�H�W��ƥ��T�ЦA�u�X�������U�i�T�w�j�A��ƿ��~�ηQ�h�X�Ъ�������");
		JOptionPane.showMessageDialog(null, "��ƵL�~�Ы��U�i�T�w�j");
	}

	private BigDecimal checkData() throws Exception {
		List<Type> result = ContractUtil.readContract(this.checkFunction, this.walletAddress, this.contractAddress);
		Uint256 balance = null;
		if (result == null) {
			throw new Exception("�L�k���o�l�B!!");
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
		bot.logger.info("��洣������H��l�Ƨ����A�}�l�B��");
		// System.out.println("�m�Y�q�����H��l�Ƨ����A�}�l�B��");

		while (true) {
			try {
				checkData = bot.checkData();
				long lcheckData = checkData.longValue();
				if (lcheckData > bot.underLimit) {
					Thread.sleep(bot.iSleep);
					bot.logger.info("����:{} �����H��������:{}, �ثe�l�B��:{}", bot.projectName, iCount++, lcheckData);
					// System.out.println("�����H��������:"+iCount++);
					continue;
				} else {
					bot.logger.info("�ثe�l�B:{}�A�C���٭ȡA�}�l��洣��", lcheckData);
					if ((bot.emergencyFunction != null) && (bot.emergencyFunction.length() != 0)) {
						emergencyResult = ContractUtil.writeContractNoPara(bot.emergencyFunction, bot.walletAddress, bot.contractAddress, bot.credentials);
						if (emergencyResult.equals("success")) {
							bot.logger.info("��洣�⧹��");
						} else if (emergencyResult.equals("fail")) {
							bot.logger.info("��洣�⥢�ѡA�Ф�ʴ���");
						} else {
							bot.logger.info("�����H�B�@�L�[�A�гz�LHash Code�d��");
						}
						bot.logger.info("�����H������洣��");
						// System.out.println("�����H�����Y�q�m��");
						bot.logger.info("Emergency Result:{}", emergencyResult);
						// System.out.println(buyResult);
						JOptionPane.showMessageDialog(null, "�l�B�C��ĵ�١A�w�i���洣��");
					}
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				bot.logger.error("��洣������H�o�Ͳ��`",e);
			}
		}
	}

}
