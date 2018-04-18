package net.smarthood.infcgi.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.smarthood.infcgi.enumerations.FcgiType;
import net.smarthood.infcgi.enumerations.FcgiVersion;

public class NameValueRequest extends FcgiRequest {

	private Map<String, String> params;

	public NameValueRequest(FcgiVersion version, FcgiType type, int requestId) {
		super(version, type, requestId);
		this.params = new HashMap<String, String>();

	}

	public void put(String key, String value) {
		this.params.put(key, value);
	}

	@Override
	protected byte[] toByteArray() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		for (String key : params.keySet()) {
			String value = params.get(key);

			stream.write(encodeLength(key.length()));
			stream.write(encodeLength(value.length()));
			stream.write(key.getBytes());
			stream.write(value.getBytes());
		}
		return stream.toByteArray();
	}

	private byte[] encodeLength(int length) throws IOException {
 		if (length < 128) {
			return new byte[] { (byte) (length & 0xFF) };
		} else {
			return new byte[] { (byte) ((length >> 24) & 0xFF | 0x80), //
					(byte) ((length >> 16) & 0xFF), //
					(byte) ((length >> 8) & 0xFF), //
					(byte) (length & 0xFF) };
		}
	}

}
