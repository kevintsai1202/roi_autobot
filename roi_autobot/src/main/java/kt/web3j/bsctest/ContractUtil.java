package kt.web3j.bsctest;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetStorageAt;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

public class ContractUtil {
//	private final static WebSocketService wss = new WebSocketService("wss://rough-red-card.bsc.discover.quiknode.pro/0120ea61accaa10bcbadf5c4dfc90aee80a0c805/", false);
//	private static Web3j web3j = null;
//	private final static Web3j web3j = Web3j.build(new HttpService("https://rough-red-card.bsc.discover.quiknode.pro/0120ea61accaa10bcbadf5c4dfc90aee80a0c805/"));
//	private final static Web3j web3j = Web3j.build(new HttpService("https://bsc-dataseed.binance.org/"));
	
//	private final static Web3j web3j = Web3j.build(new HttpService("https://bsc-dataseed4.defibit.io/"));
	private final static Logger logger = LoggerFactory.getLogger(ContractUtil.class);
	private final static BigInteger botFee = Convert.toWei("0.05", Convert.Unit.ETHER).toBigInteger();
	private final static BigInteger guessGas = Convert.toWei("0.001", Convert.Unit.ETHER).toBigInteger();
	private final static BigInteger gasLimit = new BigInteger("500000");
	private final static BigInteger gasPrice = new BigInteger("5000000000");
	private static Web3j web3j = null; //Web3j.build(new HttpService("https://bsc-dataseed4.ninicoin.io"));
	private static Document wallets = null;
	private static ERC20 busd = null;
	private static ERC20 usdt = null;
	private static BigInteger nonce = null;

//	public static void init() throws Exception{
//		 try {
//		        wss.connect();
//		    } catch (Exception e) {
//		        System.out.println("Error while connecting to WSS service: " + e);
//		        throw e;
//		    }
//		web3j =  Web3j.build(wss);
//	}
	
	public static void init() throws DocumentException {
		String sRpc = null;
		wallets = XmlUtil.loadXml(BuyBOT.class.getClassLoader().getResourceAsStream("wallets.xml"));
		sRpc = XmlUtil.getXpathValue(wallets, "//rpc");
		logger.info("RPC Server:{}", sRpc);
		if (sRpc == null) {
			sRpc = "https://bsc-dataseed4.ninicoin.io";
		}
		web3j = Web3j.build(new HttpService(sRpc));
	}
	
	public static BigInteger fetchNonce(String address) {
		try {
//			logger.info("取得nonce");
			EthGetTransactionCount getNonce = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
			if (getNonce == null) {
				throw new RuntimeException("net error");
			}
			return getNonce.getTransactionCount();
		} catch (IOException e) {
			throw new RuntimeException("net error");
		}
	}

	public static String txStatus(String hash) {
		try {
			EthGetTransactionReceipt resp = web3j.ethGetTransactionReceipt(hash).send();
			Optional<TransactionReceipt> receipt = resp.getTransactionReceipt();

			if (receipt.isPresent()) {
				TransactionReceipt receipt1 = resp.getTransactionReceipt().get();
				String status = "0x1".equals(receipt1.getStatus()) ? "success" : "fail";
				return status;
			}
		} catch (Exception e) {
			logger.info("txStatusFail {}", e.getMessage(), e);
		}
		return "process";
	}

	public static void transaction(String toAddress, double ethBalance, Credentials credentials) throws Exception {
		String sAddress = credentials.getAddress();
		if (sAddress.equals(toAddress)) {
			logger.info("自己人無須能源!!");
		} else {
			TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, toAddress, BigDecimal.valueOf(ethBalance), Convert.Unit.ETHER)
					.send();
			// String sStatus = transactionReceipt.getStatus();
			// BigInteger gasUsed = transactionReceipt.getGasUsed();
			logger.info("已完成機器人能源轉換");
		}

	}

	public static String approve(String token, String sAmount, String contractAddress, Credentials credentials) throws Exception {
		BigInteger amountWei = Convert.toWei(sAmount, Convert.Unit.ETHER).toBigInteger();
		if (token.equals("BUSD")) {
			ContractUtil.busd = ERC20.load("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", web3j, credentials,
					new StaticGasProvider(web3j.ethGasPrice().send().getGasPrice(), new BigInteger("400000")));
			TransactionReceipt receipt = busd.approve(contractAddress, amountWei).send();
			if (receipt.getStatus().equals("0x1")) {
				return "已授權BUSD:" + sAmount;
			} else
				throw new Exception("授權BUSD失敗");
		} else if (token.equals("USDT")) {
			ContractUtil.usdt = ERC20.load("0x55d398326f99059ff775485246999027b3197955", web3j, credentials,
					new StaticGasProvider(web3j.ethGasPrice().send().getGasPrice(), new BigInteger("400000")));
			TransactionReceipt receipt = busd.approve(contractAddress, amountWei).send();
			if (receipt.getStatus().equals("0x1")) {
				return "已授權USDT:" + sAmount;
			} else
				throw new Exception("授權USDT失敗");
		} else {
			throw new Exception("Token Not Support");
		}
	}

	public static String writeContractCompound(String functionName,String input, String walletAddress, String contractAddress, Credentials credentials) throws IOException {
		List<Type> inputParameters = new ArrayList<>();
		List<TypeReference<?>> outputParameters = Arrays.asList();
		RawTransaction rawTransaction = null;
		// web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_PRICE;
		// //web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_LIMIT;//new
		// BigInteger("21000");
		if (input.equals("bool")) {
			inputParameters.add(new Bool(true));
		}else if(input.equals("address")){
			inputParameters.add(new Address(walletAddress));
		}else {
			logger.error("設定input方式錯誤:{}", input);
			return "error";
		}
		Function function = new Function(functionName, inputParameters, outputParameters);
		String functionEncode = FunctionEncoder.encode(function);
		BigInteger nonce = fetchNonce(walletAddress);
		rawTransaction = RawTransaction.createTransaction(fetchNonce(walletAddress), gasPrice, gasLimit, contractAddress, functionEncode);
		EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials))).send();
		if (response.hasError()) {
			logger.error("執行{}產生錯誤，錯誤為{}", functionName, response.getResult());
			return "error";
		} else {
			String sHash = response.getTransactionHash();
			logger.info("HASH Code:{}", sHash);
			for (int i = 0; i < 10; i++) {
				String sStatus = "";
				try {
					sStatus = txStatus(sHash);
					logger.info("STATUS:{}", sStatus);
					if (sStatus.equals("process")) {
						Thread.sleep(1000l);
					} else if (sStatus.equals("success")) {
						return "success";
					} else {
						return "fail";
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					return "fail";
				}
			}
		}
		return "unknow";
	}

	public static String writeContractNoPara(String functionName, String walletAddress, String contractAddress, Credentials credentials) throws IOException {
		List<Type> inputParameters = Arrays.asList();
		List<TypeReference<?>> outputParameters = Arrays.asList();
		RawTransaction rawTransaction = null;

		// web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_PRICE;
		// //web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_LIMIT;//new
		// BigInteger("21000");
		Function function = new Function(functionName, inputParameters, outputParameters);
		String functionEncode = FunctionEncoder.encode(function);
		rawTransaction = RawTransaction.createTransaction(fetchNonce(walletAddress), gasPrice, gasLimit, contractAddress, functionEncode);
		EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials))).send();
		if (response.hasError()) {
			logger.error("執行{}產生錯誤，錯誤為{}", functionName, response.getError());
			return "error";
		} else {
			String sHash = response.getTransactionHash();
			logger.info("HASH Code:{}", sHash);
			for (int i = 0; i < 10; i++) {
				String sStatus = "";
				try {
					sStatus = txStatus(sHash);
					logger.info("STATUS:{}", sStatus);
					if (sStatus.equals("process")) {
						Thread.sleep(1000l);
					} else if (sStatus.equals("success")) {
						return "success";
					} else {
						return "fail";
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					return "fail";
				}
			}
		}
		return "unknow";
	}
	
	public static String writeContractSell(String functionName, String walletAddress, String contractAddress, Credentials credentials) throws IOException {
		List<Type> inputParameters = Arrays.asList();
		List<TypeReference<?>> outputParameters = Arrays.asList();
		RawTransaction rawTransaction = null;

		// web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_PRICE;
		// //web3j.ethGasPrice().send().getGasPrice();
		// DefaultGasProvider.GAS_LIMIT;//new
		// BigInteger("21000");
		Function function = new Function(functionName, inputParameters, outputParameters);
		String functionEncode = FunctionEncoder.encode(function);
		rawTransaction = RawTransaction.createTransaction(fetchNonce(walletAddress), gasPrice, gasLimit, contractAddress, functionEncode);
		EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials))).send();
		if (response.hasError()) {
			logger.error("執行{}產生錯誤，錯誤為{}", functionName, response.getError());
			return "error";
		} else {
			String sHash = response.getTransactionHash();
			logger.info("HASH Code:{}", sHash);
			for (int i = 0; i < 10; i++) {
				String sStatus = "";
				try {
					sStatus = txStatus(sHash);
					logger.info("STATUS:{}", sStatus);
					if (sStatus.equals("process")) {
						Thread.sleep(1000l);
					} else if (sStatus.equals("success")) {
						return "success";
					} else {
						return "fail";
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					return "fail";
				}
			}
		}
		return "unknow";
	}
	
	public static BigDecimal getTokenBalanceOf(String token, String contractAddress,Credentials credentials) throws Exception {
		BigInteger ContractBalance = null;
		BigDecimal balance = null;
		if (token.equals("BUSD")) {
			if (ContractUtil.busd == null) {
			ContractUtil.busd = ERC20.load("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", web3j, credentials,
					new StaticGasProvider(web3j.ethGasPrice().send().getGasPrice(), new BigInteger("400000")));
			}		
			ContractBalance= ContractUtil.busd.balanceOf(contractAddress).send();
			logger.info("餘額為:{}", Convert.fromWei(ContractBalance.toString(), Convert.Unit.ETHER));
			balance = Convert.fromWei(ContractBalance.toString(), Convert.Unit.ETHER);
			
		} else if (token.equals("USDT")) {
			if (ContractUtil.usdt == null) {
			ContractUtil.usdt = ERC20.load("0x55d398326f99059ff775485246999027b3197955", web3j, credentials,
					new StaticGasProvider(web3j.ethGasPrice().send().getGasPrice(), new BigInteger("400000")));
			}	
			ContractBalance= ContractUtil.usdt.balanceOf(contractAddress).send();
			logger.info("餘額為:{}", Convert.fromWei(ContractBalance.toString(), Convert.Unit.ETHER));
			balance = Convert.fromWei(ContractBalance.toString(), Convert.Unit.ETHER);
		} else {
			throw new Exception("Token Not Support");
		}
		return balance;
	}

	public static boolean checkBalance(String walletAddress, String sToken, String buyAmount,Credentials credentials) throws Exception {
		BigInteger walletBNBBalance = web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().getBalance();
		BigInteger walletBalance = null;
		BigInteger amountWei = BigInteger.ZERO;
		if ((buyAmount != null) && (buyAmount.length()>0))
			amountWei = Convert.toWei(buyAmount, Convert.Unit.ETHER).toBigInteger();
//		ContractUtil.nonce =  getNonce(walletAddress);

		if (sToken.equals("BNB")) {
			walletBalance = walletBNBBalance;
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			logger.info("機器人能源及瓦斯費約需準備 0.06BNB 以上");
			logger.info("搶頭礦瓦斯費有可能飆高，請多準備一些BNB");
			if(!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")){
				if (walletBalance.compareTo(amountWei.add(ContractUtil.botFee).add(ContractUtil.guessGas)) < 0) {
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需準備 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBalance.compareTo(amountWei.add(ContractUtil.guessGas)) < 0) {
					logger.error("BNB餘額不足，瓦斯費約需準備 0.01BNB 以上");
					return false;
				}else {
					return true;
				}	
			}
		}else if (sToken.equals("BUSD")) {
			if (busd == null) {
				busd = ERC20.load("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", web3j, credentials, new StaticGasProvider(new BigInteger("5000000000"), new BigInteger("500000")));
			}
			walletBalance = busd.balanceOf(walletAddress).send();
			logger.info("BUSD餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			if (walletBalance.compareTo(amountWei) < 0) {
				logger.error("BUSD餘額不足");
				return false;
			}
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER));
			if (!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")) {
				if (walletBNBBalance.compareTo(botFee.add(ContractUtil.guessGas)) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBNBBalance.compareTo(ContractUtil.guessGas) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}
		}else if (sToken.equals("USDT")) {
			//
			if (usdt == null) {
				usdt = ERC20.load("0x55d398326f99059ff775485246999027b3197955", web3j, credentials, new StaticGasProvider(new BigInteger("5000000000"), new BigInteger("500000")));
			}
			walletBalance = usdt.balanceOf(walletAddress).send();
			logger.info("USDT餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			if (walletBalance.compareTo(amountWei) < 0) {
				logger.error("USDT餘額不足");
				return false;
			}
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER));
			if (!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")) {
				if (walletBNBBalance.compareTo(botFee.add(ContractUtil.guessGas)) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBNBBalance.compareTo(ContractUtil.guessGas) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}
		}else {
			logger.error("代幣輸入錯誤");
			return false;	
		}
	}
	
	public static boolean checkBatchBalance(String walletAddress, String sToken, String buyAmount,Credentials credentials,int sleep, int duration) throws Exception {
		BigInteger walletBNBBalance = web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().getBalance();
		BigInteger walletBalance = null;
		BigInteger amountWei = BigInteger.ZERO;
		if ((buyAmount != null) && (buyAmount.length()>0))
			amountWei = Convert.toWei(buyAmount, Convert.Unit.ETHER).toBigInteger();
//		ContractUtil.nonce =  getNonce(walletAddress);
		int iTimes = duration/sleep;
		BigInteger biTimes = new BigInteger(String.valueOf(iTimes));
		BigInteger totalGas = ContractUtil.guessGas.multiply(biTimes);
		
		if (sToken.equals("BNB")) {
			walletBalance = walletBNBBalance;
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			logger.info("機器人能源為 0.1BNB");
			logger.info("搶頭礦瓦斯費約需{}BNB",totalGas);
			if(!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")){
				if (walletBalance.compareTo(amountWei.add(ContractUtil.botFee).add(ContractUtil.botFee).add(totalGas)) < 0) {
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需準備 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBalance.compareTo(amountWei.add(ContractUtil.guessGas)) < 0) {
					logger.error("BNB餘額不足，瓦斯費約需準備 0.01BNB 以上");
					return false;
				}else {
					return true;
				}	
			}
		}else if (sToken.equals("BUSD")) {
			if (busd == null) {
				busd = ERC20.load("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", web3j, credentials, new StaticGasProvider(new BigInteger("5000000000"), new BigInteger("500000")));
			}
			walletBalance = busd.balanceOf(walletAddress).send();
			logger.info("BUSD餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			if (walletBalance.compareTo(amountWei) < 0) {
				logger.error("BUSD餘額不足");
				return false;
			}
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER));
			if (!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")) {
				if (walletBNBBalance.compareTo(botFee.add(ContractUtil.guessGas)) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBNBBalance.compareTo(ContractUtil.guessGas) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}
		}else if (sToken.equals("USDT")) {
			//
			if (usdt == null) {
				usdt = ERC20.load("0x55d398326f99059ff775485246999027b3197955", web3j, credentials, new StaticGasProvider(new BigInteger("5000000000"), new BigInteger("500000")));
			}
			walletBalance = usdt.balanceOf(walletAddress).send();
			logger.info("USDT餘額為:{}", Convert.fromWei(walletBalance.toString(), Convert.Unit.ETHER));
			if (walletBalance.compareTo(amountWei) < 0) {
				logger.error("USDT餘額不足");
				return false;
			}
			logger.info("BNB餘額為:{}", Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER));
			if (!walletAddress.equals("0xc0C3C272f2b998F17Ad6085376663b39A72467EE")) {
				if (walletBNBBalance.compareTo(botFee.add(ContractUtil.guessGas)) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}else {
				if (walletBNBBalance.compareTo(ContractUtil.guessGas) < 0) {//機器人運做加上預估瓦斯費
					logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
					return false;
				}else {
					return true;
				}
			}
		}else {
			logger.error("代幣輸入錯誤");
			return false;	
		}
	}

	public static String writeContractBuy(String functionName, String walletAddress, String contractAddress, Credentials credentials, Address refAdd,
			String sToken, String buyAmount, BigInteger biBoost, boolean inputAmount) throws Exception {
		// BigInteger nonce = null;
		List<Type> inputParameters = new ArrayList<>();
		List<TypeReference<?>> outputParameters = new ArrayList<>();
		RawTransaction rawTransaction = null;
		// BigInteger walletBalance = null;
		// BigInteger walletBNBBalance = null;

		// BigInteger gasPrice = new BigInteger("5000000000");
		// //web3j.ethGasPrice().send().getGasPrice();//
		// DefaultGasProvider.GAS_PRICE;
		// //new
		// //web3j.ethGasPrice().send().getGasPrice();
		BigInteger boostGasPrice = gasPrice.multiply(biBoost);
		// BigInteger gasLimit = new BigInteger("500000"); //
		// DefaultGasProvider.GAS_LIMIT;//new
		// BigInteger("21000");
		BigInteger amountWei = Convert.toWei(buyAmount, Convert.Unit.ETHER).toBigInteger();
		// BigInteger botFee = Convert.toWei("0.05",
		// Convert.Unit.ETHER).toBigInteger();
		// ContractGasProvider gasProvider = new StaticGasProvider(gasPrice,
		// gasLimit);
		/*
		 * walletBNBBalance = web3j.ethGetBalance(walletAddress,
		 * DefaultBlockParameterName.LATEST).send().getBalance(); if
		 * (sToken.equals("BNB")) { walletBalance = walletBNBBalance;
		 * logger.info("BNB餘額為:{}", Convert.fromWei(walletBalance.toString(),
		 * Convert.Unit.ETHER)); if
		 * (walletBalance.compareTo(amountWei.add(botFee).add(new
		 * BigInteger("0.01"))) < 0) {
		 * logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上"); return "結束程式"; } }
		 * else if (sToken.equals("BUSD")) { if (busd == null) { busd =
		 * ERC20.load("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56", web3j,
		 * credentials, new StaticGasProvider(gasPrice, gasLimit)); }
		 * walletBalance = busd.balanceOf(walletAddress).send();
		 * logger.info("BUSD餘額為:{}", Convert.fromWei(walletBalance.toString(),
		 * Convert.Unit.ETHER)); if (walletBalance.compareTo(amountWei) < 0) {
		 * logger.error("BUSD餘額不足"); return "結束程式"; } logger.info("BNB餘額為:{}",
		 * Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER)); if
		 * (walletBNBBalance.compareTo(botFee.add(new BigInteger("0.01"))) < 0)
		 * {//機器人運做加上預估瓦斯費 logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
		 * return "結束程式"; } // TransactionReceipt receipt =
		 * busd.approve(contractAddress, // amountWei).send(); //
		 * logger.info(receipt.toString()); } else if (sToken.equals("USDT")) {
		 * // if (usdt == null) { usdt =
		 * ERC20.load("0x55d398326f99059ff775485246999027b3197955", web3j,
		 * credentials, new StaticGasProvider(gasPrice, gasLimit)); }
		 * walletBalance = usdt.balanceOf(walletAddress).send();
		 * logger.info("USDT餘額為:{}", Convert.fromWei(walletBalance.toString(),
		 * Convert.Unit.ETHER)); if (walletBalance.compareTo(amountWei) < 0) {
		 * logger.error("USDT餘額不足"); return "結束程式"; } logger.info("BNB餘額為:{}",
		 * Convert.fromWei(walletBNBBalance.toString(), Convert.Unit.ETHER)); if
		 * (walletBNBBalance.compareTo(botFee.add(new BigInteger("0.01"))) < 0)
		 * {//機器人運做加上預估瓦斯費 logger.error("BNB餘額不足，機器人能源及瓦斯費約需 0.06BNB 以上");
		 * return "結束程式"; } // usdt.approve(contractAddress, amountWei).send();
		 * } else { logger.error("代幣輸入錯誤"); return "結束程式"; }
		 */

		logger.info("Gas Price:{}, Gas Limit:{}", boostGasPrice, gasLimit);
		inputParameters.add(refAdd);
		if (inputAmount) {
			inputParameters.add(new Uint256(amountWei));
			Function function = new Function(functionName, inputParameters, outputParameters);
			String functionEncode = FunctionEncoder.encode(function);
			rawTransaction = RawTransaction.createTransaction(fetchNonce(walletAddress), boostGasPrice, gasLimit, contractAddress, functionEncode);
		} else {
			Function function = new Function(functionName, inputParameters, outputParameters);
			String functionEncode = FunctionEncoder.encode(function);
			rawTransaction = RawTransaction.createTransaction(fetchNonce(walletAddress), boostGasPrice, gasLimit, contractAddress, amountWei, functionEncode);
		}
		// System.out.println("Gas Price:" + gasPrice);
		// System.out.println("Amount:" + amountWei);
		EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials))).send();
		if (response.hasError()) {
			// System.out.println("合約複投執行異常：" +
			// response.getError().getMessage());
			logger.error("執行{}產生錯誤，錯誤為{}", functionName, response.getError());
			return "error";
		} else {
			// System.out.println("合約複投執行完成，nonce=[" + nonce + "],hash=[" +
			// response.getTransactionHash() + "]");
			String sHash = response.getTransactionHash();
			logger.info("HASH Code=[{}]", sHash);
			for (int i = 0; i < 10; i++) {
				String sStatus = "";
				try {
					sStatus = txStatus(sHash);
					logger.info("STATUS:{}", sStatus);
					if (sStatus.equals("process")) {
						Thread.sleep(1000l);
					} else if (sStatus.equals("success")) {
						return "success";
					} else {
						return "fail";
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					return "fail";
				}
			}
		}
		return "unknow";
	}
	
	public static String writeContractBatchBuy(String functionName, String walletAddress, String contractAddress, Credentials credentials, Address refAdd,
			String sToken, String buyAmount, BigInteger biBoost, boolean inputAmount) throws Exception {
		List<Type> inputParameters = new ArrayList<>();
		List<TypeReference<?>> outputParameters = new ArrayList<>();
		RawTransaction rawTransaction = null;
		BigInteger boostGasPrice = gasPrice.multiply(biBoost);
		BigInteger amountWei = Convert.toWei(buyAmount, Convert.Unit.ETHER).toBigInteger();
		
		logger.info("Gas Price:{}, Gas Limit:{}", boostGasPrice, gasLimit);
		inputParameters.add(refAdd);
		if (inputAmount) {
			inputParameters.add(new Uint256(amountWei));
			Function function = new Function(functionName, inputParameters, outputParameters);
			String functionEncode = FunctionEncoder.encode(function);
			rawTransaction = RawTransaction.createTransaction(ContractUtil.getNonce(), boostGasPrice, gasLimit, contractAddress, functionEncode);
		} else {
			Function function = new Function(functionName, inputParameters, outputParameters);
			String functionEncode = FunctionEncoder.encode(function);
			rawTransaction = RawTransaction.createTransaction(ContractUtil.getNonce(), boostGasPrice, gasLimit, contractAddress, amountWei, functionEncode);
		}
		EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials))).send();
//		if (response.hasError()) {
//			logger.error("執行{}產生錯誤，錯誤為{}", functionName, response.getError());
//			return "error";
//		} else {
//			String sHash = response.getTransactionHash();
//			logger.info("HASH Code=[{}]", sHash);
//			for (int i = 0; i < 10; i++) {
//				String sStatus = "";
//				try {
//					sStatus = txStatus(sHash);
//					logger.info("STATUS:{}", sStatus);
//					if (sStatus.equals("process")) {
//						Thread.sleep(1000l);
//					} else if (sStatus.equals("success")) {
//						return "success";
//					} else {
//						return "fail";
//					}
//				} catch (InterruptedException e) {
//					logger.error(e.getMessage());
//					return "fail";
//				}
//			}
//		}
		return "success";
	}

	public static List<Type> readContract(String functionName, String walletAddress, String contractAddress) {
		// 生成需要调用函数的data
		List input = Arrays.asList();
		List output = Arrays.asList(new TypeReference<Uint256>() {
		});
		List<Type> returnResult = Arrays.asList();
		Function function = new Function(functionName, input, output);
		String data = FunctionEncoder.encode(function);
		// 组建请求的参数 调用者地址(可以为空)，合约地址、参数
		// EthCall response
		EthCall response;
		String sResponseValue = null;
		try {
			response = web3j.ethCall(Transaction.createEthCallTransaction(walletAddress, contractAddress, data), DefaultBlockParameterName.LATEST).send();
			sResponseValue = response.getValue();
			returnResult = FunctionReturnDecoder.decode(sResponseValue, function.getOutputParameters());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		}
		// 解析返回结果
		return returnResult;
	}

	public static boolean readContractBool(String functionName, String walletAddress, String contractAddress) {
		// 生成需要调用函数的data
		Bool returnValue = null;
		boolean boolReturn = false;  
		List input = Arrays.asList();
		List output = Arrays.asList(new TypeReference<Bool>() {
		});
		List<Type> returnResult = Arrays.asList();
		Function function = new Function(functionName, input, output);
		String data = FunctionEncoder.encode(function);
		// 组建请求的参数 调用者地址(可以为空)，合约地址、参数
		// EthCall response
		EthCall response;
		String sResponseValue = null;
		try {
			response = web3j.ethCall(Transaction.createEthCallTransaction(walletAddress, contractAddress, data), DefaultBlockParameterName.LATEST).send();
			sResponseValue = response.getValue();
			returnResult = FunctionReturnDecoder.decode(sResponseValue, function.getOutputParameters());
			returnValue = (Bool) returnResult.get(0);
			boolReturn = returnValue.getValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
		return boolReturn;
	}
	
	public static List<Type> readContract(String functionName, String walletAddress, String contractAddress, String input1) {
		// 生成需要调用函数的data
		List input = new ArrayList<>();
		List output = Arrays.asList(new TypeReference<Uint256>() {
		});
		List<Type> returnResult = Arrays.asList();
		input.add(new Address(input1));
		Function function = new Function(functionName, input, output);
		String data = FunctionEncoder.encode(function);
		// 组建请求的参数 调用者地址(可以为空)，合约地址、参数
		// EthCall response
		EthCall response;
		String sResponseValue = null;
		try {
			response = web3j.ethCall(Transaction.createEthCallTransaction(walletAddress, contractAddress, data), DefaultBlockParameterName.LATEST).send();
			sResponseValue = response.getValue();
			returnResult = FunctionReturnDecoder.decode(sResponseValue, function.getOutputParameters());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		}
		// 解析返回结果
		return returnResult;
	}

	/*
	 * public BigDecimal getBalance(String functionName) throws Exception {
	 * List<Type> result = readContract("getBalance"); Uint256 balance =
	 * (Uint256) result.get(0); return
	 * Convert.fromWei(balance.getValue().toString(), Convert.Unit.ETHER); }
	 */

	public static boolean ContractStorageReader(int iSlot,int iLocation, String contractAddress) {
		boolean bResult= false;
		String sResult = ContractStorageReader(iSlot,iLocation,"uint8",contractAddress);
		bResult = !sResult.equals("00");
		return bResult;
	}
	
	public static String ContractStorageReader(int iSlot,int iLocation, String sType, String contractAddress) {
		String sResult = null;
		BigInteger storageSlot = BigInteger.valueOf(iSlot);
		EthGetStorageAt response;
		BigInteger value=null;
		String sTemp = null;
		try {
			response = web3j.ethGetStorageAt(contractAddress, storageSlot, DefaultBlockParameterName.LATEST).send();
			if (response.hasError()) {
	            return null;
	        }
			
			if (sType.equalsIgnoreCase("uint8")) {
				String hexValue = response.getData();
				logger.info("Hex value is "+hexValue);
				sTemp = hexValue.substring(hexValue.length()-2*iLocation,hexValue.length());
				logger.info("Temp value is "+sTemp);
				value = new BigInteger(sTemp, 16);
			}else if(sType.equalsIgnoreCase("uint16")) {
				String hexValue = response.getData();
				logger.info("Hex value is "+hexValue);
				sTemp = hexValue.substring(hexValue.length()-4*iLocation,hexValue.length());
				logger.info("Temp value is "+sTemp);
				value = new BigInteger(sTemp, 16);
			}else if(sType.equalsIgnoreCase("uint32")) {
				String hexValue = response.getData();
				logger.info("Hex value is "+hexValue);
				sTemp = hexValue.substring(hexValue.length()-8*iLocation,hexValue.length());
				logger.info("Temp value is "+sTemp);
				value = new BigInteger(sTemp, 16);
			}else if(sType.equalsIgnoreCase("uint64")) {
				String hexValue = response.getData();
				logger.info("Hex value is "+hexValue);
				sTemp = hexValue.substring(hexValue.length()-16*iLocation,hexValue.length());
				logger.info("Temp value is "+sTemp);
				value = new BigInteger(sTemp, 16);
			}else if(sType.equalsIgnoreCase("uint256")) {
				String hexValue = response.getData();
				logger.info("Hex value is "+hexValue);
				sTemp = hexValue.substring(2);
				logger.info("Temp value is "+sTemp);
				value = new BigInteger(sTemp, 16);
			}
			logger.info("value is "+value);
			sResult = sTemp;
//			boolean boolValue = !value.equals(BigInteger.ZERO);
//			logger.info("Bool value is "+boolValue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		}
		
		return sResult;
	}
//	public static boolean ContractStorageReader(int iSlot, String contractAddress) {
//		boolean bResult = false;
//		BigInteger storageSlot = BigInteger.valueOf(iSlot);
//		EthGetStorageAt response;
//		try {
//			response = web3j.ethGetStorageAt(contractAddress, storageSlot, DefaultBlockParameterName.LATEST).send();
//			if (response.hasError()) {
//	            return false;
//	        }
//			String hexValue = response.getData();
//			logger.info("Hex value is "+hexValue);
//			BigInteger value = new BigInteger(hexValue.substring(2), 16);
//			logger.info("value is "+value);
////			boolean boolValue = !value.equals(BigInteger.ZERO);
////			logger.info("Bool value is "+boolValue);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage());
//			return false;
//		}
//		
//		return bResult;
//	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		 Credentials credentials = Credentials.create("99220a23cde03b5fe08b1b98b5efb6bbe5902cf7a12cde330c25d9922b344529");
		 try {
			ContractUtil.init();
			boolean bInitialized = ContractUtil.ContractStorageReader(7,1, "0xcd3D0Ea8b5D82d7D5976220807c5B4BF52A5D228");
			logger.info("合約啟動狀態: "+bInitialized);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		 ContractUtil.writeContractCompound("hireMiners","0xc0C3C272f2b998F17Ad6085376663b39A72467EE", "0x6DdbA3381663431Ce6c34874A9875b995788008b", credentials);
		// List<Type> result = ContractUtil.readContract("getUserInfo",
		// "0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA",
		// "0x6c5251F9eAD968b6BBFADDAA1DB7b3f4962387a5",
		// "0xf9ED530453CAadC7f894C6d33FE727453DE0aCCA");
		// Uint256 output5 = (Uint256) result.get();
		// System.out.println(output5);

		// BigInteger gaslimit = DefaultGasProvider.GAS_LIMIT;
		// System.out.println(gaslimit);
		// BigInteger gasfeeGwei = gasprice.multiply(gaslimit);
		// System.out.println(gasfeeGwei);
//		logger.info("result test: {}",ContractUtil.readContractBool("initialized", "0xc0C3C272f2b998F17Ad6085376663b39A72467EE", "0x254773b4348d7966D2AbdAAc5a908Cc4D1D85146"));
//		Dialog dialog = new Dialog(new Frame(), "餘額警示");
//        dialog.setSize(320, 200);
//        dialog.add(new Label("餘額警示!"));
//        dialog.setVisible(false);
//		int i = 0;
//		BigDecimal bigBalance;
//		while (true) {
//			try {
//				Thread.sleep(1000);
//				bigBalance = getTokenBalanceOf("BUSD", "0x17891dB447A950464e412390c7ca81903Cd9e695", credentials);
//				if (bigBalance.compareTo(new BigDecimal(100000))<0) {
//					dialog.setVisible(true);
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if ((i++ % 2) == 0)
//				System.out.print("偵測餘額機器人運行中-\r");
//			else
//				System.out.print("偵測餘額機器人運行中|\r");
//		}
	}

	public static BigInteger getNonce() {
		return nonce;
	}

	public static void setNonce(BigInteger nonce) {
		ContractUtil.nonce = nonce;
	}
}
