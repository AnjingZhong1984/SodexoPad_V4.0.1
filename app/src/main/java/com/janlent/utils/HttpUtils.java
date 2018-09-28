package com.janlent.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {

	int down_bum = 0;

	String down_url = "";

	// 通过HttpPost发送get请求，返回请求结果
	public String queryStringForGet(String url) {
		// String result =null;
		StringBuffer result = new StringBuffer();
		WeakReference<StringBuffer> soft = new WeakReference<StringBuffer>(
				result);
		System.gc();
		if (url != null) {
			down_bum++;
			down_url = url;
			HttpMethod method = null;
			try {
				List<Header> headers = new ArrayList<Header>();

				HttpClient client = new HttpClient();

				client.getHostConfiguration().getParams()
						.setParameter("http.default-headers", headers);
				client.getParams()
						.setParameter(HttpMethodParams.USER_AGENT,
								"Mozilla/5.0 (X11; U; Linux i686; en; rv:1.9.1.2) Gecko/20090803 Fedora/3.5");

				HttpConnectionManagerParams managerParams = client
						.getHttpConnectionManager().getParams();
				// 设置连接超时时间(单位毫秒)
				managerParams.setConnectionTimeout(45000);
				// 设置读数据超时时间(单位毫秒)
				managerParams.setSoTimeout(45000);
				method = new GetMethod(url);
				client.executeMethod(method);
				if (method.getStatusCode() == HttpStatus.SC_OK) {

					byte[] buffer = new byte[1024];
					int n = 0;
					BufferedReader tBufferedReader = new BufferedReader(
							new InputStreamReader(
									method.getResponseBodyAsStream()));
					String sTempOneLine = "";
					Long start = System.currentTimeMillis();
					while ((sTempOneLine = tBufferedReader.readLine()) != null) {
						result.append(sTempOneLine);
						System.out.println(Runtime.getRuntime().freeMemory()
								+ "/" + Runtime.getRuntime().totalMemory());
					}
					down_bum = 0;
					down_url = "";
					System.gc();
					return result + "";
				} else {
					return null;
				}
			} catch (Exception e) {

				e.printStackTrace();
				//如果连接超时  就尝试5次连接
				if (down_bum <= 5) {
					queryStringForGet(down_url);
				} else {
					down_bum = 0;
					down_url = "";
					System.gc();
					throw new RuntimeException(e);
				}

			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
		return null;
	}

	int up_num = 0;
	String up_url = "";
	String up_json = "";

	public String writeStringForPost(String url, String jsonString) {
		if (url != null) {
			up_num++;
			up_url = url;
			up_json = jsonString;

			HttpPost httppost = new HttpPost(url);
			ArrayList<BasicNameValuePair> nameValuelist = new ArrayList<BasicNameValuePair>();

			BasicNameValuePair bnv = new BasicNameValuePair("json", jsonString);
			// 将BasicNameValuePair对象放到list集合中
			nameValuelist.add(bnv);
			DefaultHttpClient dhc = null;
			try {
				// 用list集合新建UrlEncodedFormEntity对象，

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						nameValuelist, "UTF-8");
				httppost.setEntity(entity);
				// 建立客服端的网络对象，用来通过网络连接url连接服务器
				dhc = new DefaultHttpClient();
				Log.e("CoreConnectionPNames.CONNECTION_TIMEOUT" + 45000);
				dhc.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 45000);
				dhc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
						45000);
				// 连接服务器并返回结果
				HttpResponse resp = dhc.execute(httppost);
				Log.e("resp.getStatusLine().getStatusCode()="
						+ resp.getStatusLine().getStatusCode());

				Log.e("result" + resp.getStatusLine().getStatusCode());
				// 根据返回结果来判断是否连接成功
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// 获取返回实体内容
					HttpEntity resultentity = resp.getEntity();
					// 接获取内容转换为string类型的数据
					String result = EntityUtils.toString(resultentity);
					Log.e("result=" + result);
					up_num = 0;
					up_url = "";
					up_json = "";
					System.gc();
					if (result != null) {
						return result;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//如果连接超时  就尝试5次连接
				if (up_num <= 5) {

					writeStringForPost(up_url, up_json);

				} else {
					up_num = 0;
					up_url = "";
					up_json = "";
					System.gc();
					throw new RuntimeException(e);
				}
			} finally {
				if (dhc != null) {
					dhc.getConnectionManager().shutdown();
				}
			}
		}
		return null;

	}

}