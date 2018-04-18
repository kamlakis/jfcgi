package net.smarthood.infcgi.request;

import java.io.IOException;

import net.smarthood.infcgi.enumerations.FcgiType;
import net.smarthood.infcgi.enumerations.FcgiVersion;

public class BodyRequest extends FcgiRequest {

	byte[] content;

	public BodyRequest(FcgiVersion version, FcgiType type, int requestId, byte[] content) {
		super(version, type, requestId);
		this.content = content;
	}

	@Override
	protected byte[] toByteArray() throws IOException {
		return content;
	}
}