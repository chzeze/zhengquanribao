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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
 * @Description: （3）上海证券报（需要用户名密码） http://paper.cnstock.com
 *               http://psearch.cnstock.com/News_Search.aspx?q=兴业证券&PageNo=2
 * 
 * @author zeze
 * @date 2017年6月13日16:17:20
 * 
 */
public class shanghaiCnstockCrawlModel {

	private static Logger logger = Logger.getLogger(shanghaiCnstockCrawlModel.class);

	private static String dataStr;
	private static String cookie = "cnstock_username=lir; cnstock_ss=76bc1c3daac84cab88454ab1d4cae9f8; cnstock_record=1497341850832; CNSTOCK_SSO=\"ex=&BBS=lir|ThCvwRnSc0Yzc1PVyLDVWQ%3D%3D&SCHOOL=lir|YtG%2Bf%2FuqJDn2zvt%2F0j3cZw%3D%3D&SHOP=lir|oBXGRJHvXCBJfb%2FLhNe4Iw%3D%3D&ec=\"; CNSTOCK_BLOG=SUQ9MjY2OTI1Jk5BTUU9bGlyJkVNQUlMPVZpcF8yMDExMDUzMDA0NTUyNkBjbnN0b2NrLmNvbQ%3D%3D; CNSTOCK_PASSPORT=\"ex=&ID=266925&NAME=lir&EMAIL=Vip_20110530045526%40cnstock.com&ec=\"; CNSTOCK_REALSSO=eWswWm9obDV6WC80YkhiSTlZYjNvZmptTlVwdlBqVEdqMlRjSXNuUEdtT096KzRQOGlMdzlqK3FIdDRmS004eCwyNjY5MjUsbGlyLFZpcF8yMDExMDUzMDA0NTUyNkBjbnN0b2NrLmNvbSwxNDk3MzQxNzY0NDM1; __FTabcjffgh=2017-6-13-16-25-37; __NRUabcjffgh=1497342337631; __RTabcjffgh=2017-6-13-16-25-37; PHPSESSID=spagi9mn2govgrb2a0ih58glm2";

	private static String savePath = "D:/xingye4/shanghaiStock/";

	public static void main(String[] args) {
		String page = null;
		for (int i = 1; i <= 2848; i++) {
			String url = "http://psearch.cnstock.com/News_Search.aspx?q=%u5174%u4E1A%u8BC1%u5238&PageNo=" + i;
			System.out.println(url);
			page = getShanghaiStockMothod(url);
			// System.out.println(page);
			extracorShanghaiCnstockPage(page);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 抽取上海证券报的页面
	 * 
	 * @Title: extracorEpaperPage
	 * @param 设定文件
	 * @return void 返回类型
	 */
	private static void extracorShanghaiCnstockPage(String page) {
		Document doc = Jsoup.parse(page);
		//// *[@id="form1"]/table[2]/tbody/tr[1]/td/div[1]
		Elements divBlocks = doc.select("*[id=form1]").select("table").get(2).select("tr").get(0).select("td")
				.select("div[style=width: 75%;]");//// *[@id="form1"]/table[2]/tbody/tr[1]

		// System.out.println(divBlocks);

		for (Element divBlock : divBlocks) {// 解析列表

			Element titleBlock = divBlock.select("a").get(0);
			// System.out.println(titleBlock.text());

			if (titleBlock.text().contains("兴业证券")) {
				System.out.println(titleBlock.text());
				// System.out.println(tabBlock.text());

				String pdfDownloadUrl = divBlock.select("a").get(1).attr("href").replace("../../..", "");
				// http://paper.cnstock.com/images_new/3/2017-06/10/25/2017061025_pdf.pdf

				System.out.println(pdfDownloadUrl);

				String fileName = pdfDownloadUrl.substring(pdfDownloadUrl.lastIndexOf("/") + 1);
				// 开始下载pdf

				try {
					downloadNet(pdfDownloadUrl, fileName, savePath);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 保存信息到文本中
				String title = titleBlock.text();
				String urlStr = pdfDownloadUrl;
				String data = divBlock.select("span[class=r_green]").get(1).text();
				System.out.println(data);

				String ouStr = "<title>" + title + "</title>\r\n<URL>" + urlStr + "</URL>\r\n<data>" + data + "</data>";
				File file = new File(savePath + fileName + ".txt");
				if (file.exists())
					file.delete();
				new FileWriteUtil().WriteDocument(savePath + fileName + ".txt", ouStr);

			}

		}

	}

	/***
	 * 
	 * 证券时报获取HTML,返回String Host: epaper.stcn.com
	 * 
	 */
	private static String getShanghaiStockMothod(String url) {
		HttpClient httpClient = new HttpClient();
		String page = "";
		try {
			// 构造请求头
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			// getMethod.getParams().setSoTimeout(10000);
			getMethod.setRequestHeader("Host", "psearch.cnstock.com");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			getMethod.setRequestHeader("Accept", " text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			getMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
			getMethod.setRequestHeader("Referer",
					"http://psearch.cnstock.com/News_Search.aspx?q=%u5174%u4E1A%u8BC1%u5238");
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
			conn.setRequestProperty("Host", "paper.cnstock.com");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "utf8, deflate");
			conn.setRequestProperty("Content-Encoding", "utf8");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
			conn.setRequestProperty("Cookie", cookie);
			conn.setRequestProperty("Cache-Control", "max-age=0");
			conn.setRequestProperty("Content-Type", "application/pdf");

			// savePage(page,savePath,fileName);

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
}
