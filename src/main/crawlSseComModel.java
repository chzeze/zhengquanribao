/**   
* @Title: crawlSseComModel.java 
* @Package main 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年6月12日 下午11:20:38 
* @version V1.0   
*/
package main;

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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileWriteUtil;

/**
 * @ClassName: crawlSseComModel
 * @Description: 1、访问上交所的最新公告网页http://www.sse.com.cn/disclosure/listedinfo/
 *               announcement/ 输入我司的证券代码601377进行查询
 * 
 * @author zeze
 * @date 2017年6月12日 下午11:20:38
 * 
 */
public class crawlSseComModel {

	private static Logger logger = Logger.getLogger(crawlSseComModel.class);

	/**
	 * 
	 * @Title: main
	 * @param @param
	 *            args 设定文件
	 * @return void 返回类型
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (int i = 1; i <= 50; i++) {
			System.out.println("Page:" + i);
			getSseMothod("http://query.sse.com.cn/security/stock/queryCompanyStatementNew.do?"
					+ "jsonCallBack=jsonpCallback55415&isPagination=true&productId=601377&keyWord=&isNew=1&reportType2=&reportType=ALL"
					+ "&beginDate=2014-01-01&endDate=2017-06-12" + "&pageHelp.pageSize=25&pageHelp.pageCount=50"
					+ "&pageHelp.pageNo=" + i + "&pageHelp.beginPage=" + i
					+ "&pageHelp.cacheSize=1&pageHelp.endPage=21");
		}
	}

	/****
	 * 返回json信息
	 */
	public static void getSseMothod(String url) {
		// String url = "";
		// String cookie = "";
		HttpClient httpClient = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			getMethod.setRequestHeader("Host", "query.sse.com.cn");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			getMethod.setRequestHeader("Accept", "*/*");
			getMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
			getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			getMethod.setRequestHeader("Referer", "http://www.sse.com.cn/disclosure/listedinfo/announcement/");
			getMethod.setRequestHeader("Connection", "keep-alive");

			int statusCode = httpClient.executeMethod(getMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			System.out.println("返回状态码：" + statusCode);
			// 打印出返回数据，检验一下是否成功

			// 处理返回的页面
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer strBuffer = new StringBuffer();
			String str = "";
			while ((str = br.readLine()) != null) {
				strBuffer.append(str);
			}
			if (statusCode == 200) {
				String jsonStr = strBuffer.toString().substring(strBuffer.toString().indexOf("(") + 1);
				jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
				// System.out.println(jsonStr);
				JSONObject obj = new JSONObject(jsonStr); // 转为为json对象
				JSONArray jsonArray = obj.getJSONArray("result");
//				System.out.println(jsonArray);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jo = jsonArray.getJSONObject(i);
					String title = jo.getString("title");
					String urlStr = jo.getString("URL");
					String data = jo.getString("SSEDate");
					urlStr = "http://static.sse.com.cn" + urlStr;
					System.out.println(title + "," + urlStr);
					String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
					String savePath = "D:/xingye4/seepdf/";
					downloadNet(urlStr, fileName, savePath);
					String ouStr = "<title>" + title + "</title>\r\n<URL>" + urlStr + "</URL>\r\n<data>" + data
							+ "</data>";
					File file = new File(savePath + fileName + ".txt");
					if (file.exists())
						file.delete();
					new FileWriteUtil().WriteDocument(savePath + fileName + ".txt", ouStr);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/****
	 * 下载pdf文件
	 */
	public static void downloadNet(String urlStr, String fileName, String savaPath) throws MalformedURLException {
		// 下载网络文件
		int bytesum = 0;
		int byteread = 0;
		System.out.println(fileName);

		URL url = new URL(urlStr);

		try {
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			FileOutputStream fs = new FileOutputStream(savaPath + fileName);

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
