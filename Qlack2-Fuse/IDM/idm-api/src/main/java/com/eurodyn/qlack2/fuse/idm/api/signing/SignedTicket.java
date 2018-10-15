/*
* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
*
* Licensed under the EUPL, Version 1.1 only (the "License").
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
* https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
package com.eurodyn.qlack2.fuse.idm.api.signing;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A signed version of a {@link Ticket}. You should be careful to only use
 * fields of the Ticket which have been annotated with {@link Signed}, as these
 * are the only fields guaranteed to not have been tampered with; of course, you
 * should validate the ticket first before using such fields.
 *
 * @author European Dynamics SA
 *
 */
@XmlRootElement
@XmlType(name = "signedTicket")
public class SignedTicket extends Ticket {
	private static final Logger LOGGER = Logger.getLogger(SignedTicket.class.getName());
	private static final long serialVersionUID = 5541985644686044631L;
	private String signature;
	private static final ObjectMapper mapper = new ObjectMapper();

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Parses a String into a SignedTicket. You can optionally URLDecode the
	 * value passed (which is what you should do if you just grabbed a Cookie
	 * from your HTTP Context).
	 *
	 * @param cookieVal
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static SignedTicket fromVal(String val, boolean decode) {
		SignedTicket retVal = null;
		if (val != null) {
			try {
				if (decode) {
					val = URLDecoder.decode(val, "UTF-8");
				}
				// If the JSON string is enclosed in quotes, remove them.
				if (val.startsWith("\"")) {
					val = val.substring(1);
				}
				if (val.endsWith("\"")) {
					val = val.substring(0, val.length() - 1);
				}
				LOGGER.log(Level.FINEST, MessageFormat.format(
						"JSON value of ticket to parse:\n{0}", new Object[]{val}));
				retVal = mapper.readValue(val, SignedTicket.class);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Could not parse SignedTicket.", e);
			}
		}

		return retVal;
	}
	public static SignedTicket fromVal(String val) {
		return fromVal(val, false);
	}
}
