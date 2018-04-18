package net.smarthood.infcgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import net.smarthood.infcgi.enumerations.FcgiFlags;
import net.smarthood.infcgi.enumerations.FcgiRoles;
import net.smarthood.infcgi.enumerations.FcgiType;
import net.smarthood.infcgi.enumerations.FcgiVersion;
import net.smarthood.infcgi.request.BeginRequest;
import net.smarthood.infcgi.request.BodyRequest;
import net.smarthood.infcgi.request.NameValueRequest;
import net.smarthood.infcgi.response.FcgiResponse;

/**
 * Hello world!
 *
 */
public class App {
	// private static final BufferedReader stdin = new BufferedReader(new
	// InputStreamReader(System.in));

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static Socket socket;

	private static String content = "name=john&address=beijing";
	private static String uri = "/test.php";
	private static String documentRoot = "/home/kam/test/";

	private static String host = "localhost";
	private static int port = 9000;

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {

		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		for (int i = 0; i < 10; i++) {
			long ms = System.currentTimeMillis();
			String ret = doTest();
			ms = System.currentTimeMillis() - ms;
			System.out.println("ret " + ret + " time " + ms + " ms");
			Thread.sleep(1000);
		}
	}

	public static String doTest() throws UnknownHostException, IOException {

		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();

		BeginRequest beginRequest = new BeginRequest(FcgiVersion.FCGI_VERSION_1, FcgiType.BEGIN, 1,
				FcgiRoles.FCGI_RESPONDER, FcgiFlags.FCGI_KEEP_CONN);
		beginRequest.writeTo(os);

		NameValueRequest nameValueRequest = new NameValueRequest(FcgiVersion.FCGI_VERSION_1, FcgiType.PARAMS, 1);

		nameValueRequest.put("GATEWAY_INTERFACE", "FastCGI/1.0");
		nameValueRequest.put("REQUEST_METHOD", "POST");
		nameValueRequest.put("SCRIPT_FILENAME", documentRoot + uri);
		nameValueRequest.put("SCRIPT_NAME", uri);
		nameValueRequest.put("QUERY_STRING", "");
		nameValueRequest.put("REQUEST_URI", uri);
		nameValueRequest.put("DOCUMENT_ROOT", documentRoot);
		nameValueRequest.put("REMOTE_ADDR", "127.0.0.1");
		nameValueRequest.put("REMOTE_PORT", "9985");
		nameValueRequest.put("SERVER_ADDR", "127.0.0.1");
		nameValueRequest.put("SERVER_NAME", "localhost");
		nameValueRequest.put("SERVER_PORT", "80");
		nameValueRequest.put("SERVER_PROTOCOL", "HTTP/1.1");
		nameValueRequest.put("CONTENT_TYPE", "application/x-www-form-urlencoded");
		nameValueRequest.put("CONTENT_LENGTH", content.length() + "");

		nameValueRequest.writeTo(os);

		nameValueRequest = new NameValueRequest(FcgiVersion.FCGI_VERSION_1, FcgiType.PARAMS, 1);
		nameValueRequest.writeTo(os);

		BodyRequest bodyRequest = new BodyRequest(FcgiVersion.FCGI_VERSION_1, FcgiType.STDIN, 1, content.getBytes());
		bodyRequest.writeTo(os);

		bodyRequest = new BodyRequest(FcgiVersion.FCGI_VERSION_1, FcgiType.STDIN, 1, null);
		bodyRequest.writeTo(os);

		FcgiResponse fcgiResponse = new FcgiResponse();

		while (true) {

			byte header[] = new byte[8];
			int nRead = is.read(header, 0, 8);
			fcgiResponse.decodeHeader(header);
			int len = fcgiResponse.getContentLength();

			byte data[] = new byte[len];
			nRead = is.read(data, 0, len);

			if (fcgiResponse.getPaddingLength() > 0) {
				is.skip(fcgiResponse.getPaddingLength());
			}

			if (fcgiResponse.getType() == FcgiType.STDOUT)// || fcgiResponse.getType() == FcgiType.STDOUT)
			{
				fcgiResponse.appendContent(data);
			} else if (fcgiResponse.getType() == FcgiType.END) {
				fcgiResponse.decodeEnd(data);
				break;
			}

		}

		return new String(fcgiResponse.getContent());

	}
}
