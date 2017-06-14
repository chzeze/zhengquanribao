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
 * @Description: （4）中国证券报（需要用户名密码） http://epaper.cs.com.cn/dnis/
 *               http://epaper.cs.com.cn/html/2017-05/24/nbs.D110000zgzqb_A01.
 *               htm
 * 
 * @author zeze
 * @date 2017年6月13日23:16:28
 * 
 */
public class epaperCSCrawlModel {

	private static Logger logger = Logger.getLogger(epaperCSCrawlModel.class);

	private static String dataStr;

	private static String savePath = "D:/xingye4/epaperCS/";

	private static String hostUrl;

	private static String cookie = "JSESSIONID=CB2E95AF1152A327E489B3837EAE5DD4; _CURRENT_PAGE_URL=http://epaper.cs.com.cn/html/2017-05/24/nbs.D110000zgzqb_A01.htm";

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
					hostUrl = "http://epaper.cs.com.cn/html/" + urlData;
					String url = hostUrl + "/nbs.D110000zgzqb_A01.htm";
					System.out.println(url);
					page = getEpaperCSMothod(url);
					// System.out.println(page);
					extracorEpaperCSPage(page);
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
	private static void extracorEpaperCSPage(String page) {
		String title = "";
		String urlStr = "";
		String data = dataStr;
		Document doc = Jsoup.parse(page);
		/// html/body/table/tbody/tr[1]/td[2]/table[2]/tbody/tr/td[3]/table[1]/tbody/tr/td/table[2]/tbody/tr[3]/td/div/table[60]
		Elements tabBlocks = doc.select("table[width=280]");

		for (Element tabBlock : tabBlocks) {// 解析列表

			// System.out.println(tabBlock);

			if (tabBlock.text().contains("兴业证券")) {
				// System.out.println(tabBlock);
				Elements trBlocks = tabBlock.select("tr[class=default1]");
				for (Element trBlock : trBlocks) {
					title = trBlock.select("td").select("a").text();
					if (title.contains("兴业证券")) {
						String url = hostUrl + "/" + trBlock.select("td").select("a").attr("href");
						System.out.println(title + url);
						String pageInfo = getEpaperCSMothod(url);
						doc = Jsoup.parse(pageInfo);
						Elements pdfBlock = doc.select("td[style=PADDING-RIGHT: 5px]").select("a");
						urlStr = pdfBlock.attr("href").replace("../../..", "");
						urlStr = "http://epaper.cs.com.cn" + urlStr;
						// http://epaper.cs.com.cn/images/2017-05/24/B004/ZQBXP0040524C.pdf
						System.out.println(urlStr);

						// 下载pdf
						String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
						try {
							downloadNet(urlStr, fileName, savePath);
						} catch (MalformedURLException e) {
							System.out.println(e);
						}
						// 保存信息到文本中
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
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setRequestProperty("Host", "epaper.cs.com.cn");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			conn.setRequestProperty("Accept", " text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("Content-Encoding", "gzip");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
			conn.setRequestProperty("Cookie", cookie);
			conn.setRequestProperty("Cache-Control", "max-age=0");
			conn.setRequestProperty("Content-Type", "application/pdf");
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
	private static String getEpaperCSMothod(String url) {
		HttpClient httpClient = new HttpClient();
		String page = "";
		try {
			// 构造请求头
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			// getMethod.getParams().setSoTimeout(10000);
			getMethod.setRequestHeader("Host", "epaper.cs.com.cn");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			getMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			getMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
			getMethod.setRequestHeader("Connection", "keep-alive");
			getMethod.setRequestHeader("Upgrade-Insecure-Requests", "1");
			getMethod.setRequestHeader("Cookie", cookie);

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
