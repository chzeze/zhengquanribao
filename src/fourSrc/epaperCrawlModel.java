/**   
* @Title: epaperCrawlModel.java 
* @Package fourSrc 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年6月13日 上午10:35:55 
* @version V1.0   
*/
package fourSrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.FileWriteUtil;

/**
 * @ClassName: epaperCrawlModel
 * @Description: （1）证券时报 电子版入口地址
 *               http://epaper.stcn.com/paper/zqsb/html/epaper/index/index.htm
 *               跳转到5月24号的报纸 先用浏览器的页面搜索，定位到版面位置，然后下载PDF版
 * 
 * @author zeze
 * @date 2017年6月13日 上午10:35:55
 * 
 */
public class epaperCrawlModel {

	private static Logger logger = Logger.getLogger(epaperCrawlModel.class);

	private static String dataStr;

	private static String savePath = "D:/xingye4/Epaper/";

	public static void main(String[] args) {
		String page = null;
		for (int year = 2014; year <= 2017; year++) {
			for (int mon = 1; mon <= 12; mon++) {
				for (int day = 1; day <= 31; day++) {
					dataStr = String.valueOf(year) + "-" + String.format("%02d", mon) + "-"
							+ String.format("%02d", day);
					System.out.println("Data:" + dataStr);

					page = "";
					String urlData = String.valueOf(year) + "-" + String.format("%02d", mon) + "/"
							+ String.format("%02d", day);
					String url = "http://epaper.stcn.com/paper/zqsb/html/" + urlData + "/node_2.htm";
					System.out.println(url);
					page = getEpaperMothod(url);
					if (!page.equals("<script>window.location=\"/paper/zqsb/html/epaper/index/index.htm\";</script>"))
						extracorEpaperPage(page);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	/**
	 * 抽取证券时报的页面
	 * 
	 * @Title: extracorEpaperPage
	 * @param 设定文件
	 * @return void 返回类型
	 */
	private static void extracorEpaperPage(String page) {
		Document doc = Jsoup.parse(page);
		Elements dlBlocks = doc.select("dl");
		if (dlBlocks.size() == 0) {// 旧版本
			dlBlocks = doc.select("div[class=area]");

			for (Element dlBlock : dlBlocks) {// 解析列表
				Elements news_listBlock = dlBlock.select("ul");
				if (news_listBlock.text().contains("兴业证券")) {
					// System.out.println(news_listBlock.text());
					Elements divPdfBlock = dlBlock.select("h2").select("a[class=more]");
					// System.out.println(divPdfBlock.attr("href"));
					String pdfDownloadUrl = "http://epaper.stcn.com/paper/zqsb"
							+ divPdfBlock.attr("href").replace("../../..", "");
					System.out.println(pdfDownloadUrl);

					String fileName = pdfDownloadUrl.substring(pdfDownloadUrl.lastIndexOf("/") + 1);
					//开始下载pdf
					try {
						downloadNet(pdfDownloadUrl, fileName, savePath);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					// 保存信息到文本中
					String title = "";
					String urlStr = pdfDownloadUrl;
					String data = dataStr;
					Elements liBlocks = news_listBlock.select("li");
					for (Element liBlock : liBlocks) {
						if (liBlock.text().contains("兴业证券")) {
							title = liBlock.text();
							System.out.println(title);
						}
					}

					String ouStr = "<title>" + title + "</title>\r\n<URL>" + urlStr + "</URL>\r\n<data>" + data
							+ "</data>";
					File file = new File(savePath + fileName + ".txt");
					if (file.exists())
						file.delete();
					new FileWriteUtil().WriteDocument(savePath + fileName + ".txt", ouStr);

				}
			}

		} else {
			for (Element dlBlock : dlBlocks) {// 解析列表
				Elements news_listBlock = dlBlock.select("ul[class=news_list]");
				if (news_listBlock.text().contains("兴业证券")) {
					// System.out.println(news_listBlock.text());
					Elements divPdfBlock = dlBlock.select("div[class=pdf]").select("a");
					// System.out.println(divPdfBlock.attr("href"));
					String pdfDownloadUrl = "http://epaper.stcn.com/paper/zqsb"
							+ divPdfBlock.attr("href").replace("../../..", "");
					System.out.println(pdfDownloadUrl);

					String fileName = pdfDownloadUrl.substring(pdfDownloadUrl.lastIndexOf("/") + 1);
					// String savePath = "D:/xingye4/Epaper/";
					try {
						downloadNet(pdfDownloadUrl, fileName, savePath);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					// 保存信息到文本中
					String title = "";
					String urlStr = pdfDownloadUrl;
					String data = dataStr;
					Elements liBlocks = news_listBlock.select("li");
					for (Element liBlock : liBlocks) {
						if (liBlock.text().contains("兴业证券")) {
							title = liBlock.text();
							System.out.println(title);
						}
					}

					String ouStr = "<title>" + title + "</title>\r\n<URL>" + urlStr + "</URL>\r\n<data>" + data
							+ "</data>";
					File file = new File(savePath + fileName + ".txt");
					if (file.exists())
						file.delete();
					new FileWriteUtil().WriteDocument(savePath + fileName + ".txt", ouStr);

				}
			}
		}

	}

	/****
	 * 下载pdf文件
	 */
	public static void downloadNet(String urlStr, String fileName, String savePath) throws MalformedURLException {
		// 下载网络文件
		int bytesum = 0;
		int byteread = 0;
		// System.out.println(fileName);

		URL url = new URL(urlStr);

		try {
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			FileOutputStream fs = new FileOutputStream(savePath + fileName);

			byte[] buffer = new byte[1204];
			int length;
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread;
				// System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * 
	 * 证券时报获取HTML,返回String Host: epaper.stcn.com
	 * 
	 */
	private static String getEpaperMothod(String url) {
		HttpClient httpClient = new HttpClient();
		String page = "";
		try {
			// 构造请求头
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			// getMethod.getParams().setSoTimeout(10000);
			getMethod.setRequestHeader("Host", " epaper.stcn.com");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			getMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			getMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
			getMethod.setRequestHeader("Connection", "keep-alive");
			getMethod.setRequestHeader("Upgrade-Insecure-Requests", "1");

			int statusCode = httpClient.executeMethod(getMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			System.out.println("statusCode：" + statusCode);

			// 处理返回的页面
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer strBuffer = new StringBuffer();
			String str = "";
			while ((str = br.readLine()) != null) {
				strBuffer.append(str);
			}
			if (statusCode == 200) {
				page = strBuffer.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return page;
	}
}
