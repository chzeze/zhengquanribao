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
 * @Description: （2）证券日报 http://zqrb.ccstock.cn/html/2017-05/25/node_2.htm
 *               跳转到5月24号的报纸 先用浏览器的页面搜索，定位到版面位置，然后下载PDF版
 * 
 * @author zeze
 * @date 2017年6月13日16:17:20
 * 
 */
public class zqrbCrawlModel {

	private static Logger logger = Logger.getLogger(zqrbCrawlModel.class);

	private static String dataStr;

	private static String savePath = "D:/xingye4/Zqrb/";

	public static void main(String[] args) {
		String page = null;
		for (int year = 2014; year <= 2017; year++) {
			for (int mon = 1; mon <= 12; mon++) {
				for (int day = 14; day <= 31; day++) {
					dataStr = String.valueOf(year) + "-" + String.format("%02d", mon) + "-"
							+ String.format("%02d", day);
					System.out.println("Data:" + dataStr);

					page = "";
					String urlData = String.valueOf(year) + "-" + String.format("%02d", mon) + "/"
							+ String.format("%02d", day);
					String url = "http://zqrb.ccstock.cn/html/" + urlData + "/node_2.htm";
					System.out.println(url);
					page = getZqrbMothod(url);
					// System.out.println(page);
					if (page.length() > 10)
						extracorZqrbPage(page);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
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
	private static void extracorZqrbPage(String page) {
		Document doc = Jsoup.parse(page);
		Elements tableBlocks = doc.select("div[id=banmiancontent]").select("table");

		// System.out.println(tableBlocks);

		for (Element tabBlock : tableBlocks) {// 解析列表
//			System.out.println("zeze");
     
			if (tabBlock.text().contains("兴业证券")) {
//				System.out.println(tabBlock.text());
				Elements divPdfBlock = tabBlock.select("table").get(0).select("a");
//				System.out.println(divPdfBlock);
//				System.out.println(divPdfBlock.attr("href"));
				String pdfDownloadUrl = "http://zqrb.ccstock.cn"
						+ divPdfBlock.attr("href").replace("../../..", "");
				if(pdfDownloadUrl.contains("content_"))
					continue;
				System.out.println(pdfDownloadUrl);
//				// http://zqrb.ccstock.cn/images/
				String fileName = pdfDownloadUrl.substring(pdfDownloadUrl.lastIndexOf("/") + 1);
				// 开始下载pdf
				try {
					downloadNet(pdfDownloadUrl, fileName, savePath);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				// 保存信息到文本中
				String title = "";
				String urlStr = pdfDownloadUrl;
				String data = dataStr;
				Elements liBlocks = tabBlock.select("tbody").select("table").get(1).select("tr");
				for (Element liBlock : liBlocks) {
					if (liBlock.text().contains("兴业证券")) {
						title = liBlock.text();
						System.out.println(title);
					}
				}

				String ouStr = "<title>" + title + "</title>\r\n<URL>" + urlStr + "</URL>\r\n<data>" + data + "</data>";
				File file = new File(savePath + fileName + ".txt");
				if (file.exists())
					file.delete();
				new FileWriteUtil().WriteDocument(savePath + fileName + ".txt", ouStr);

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
	private static String getZqrbMothod(String url) {
		HttpClient httpClient = new HttpClient();
		String page = "";
		try {
			// 构造请求头
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			// getMethod.getParams().setSoTimeout(10000);
			getMethod.setRequestHeader("Host", "zqrb.ccstock.cn");
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
