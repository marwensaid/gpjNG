package net.sourceforge.gpj.jcremoteterminal;

import java.io.InputStream;
import java.io.OutputStream;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CloudTerminal extends CardTerminal {

    private InputStream is;
    private OutputStream os;

	public CloudTerminal(InputStream is, OutputStream os)
    {
        this.is = is;
        this.os = os;
	}

    @Override
    public Card connect(String protocol) throws CardException {
        try {
            if (protocol.equals(CloudCard.PROTOCOL_NFC) || 
                protocol.equals(CloudCard.PROTOCOL_SOCKET) || 
                protocol.equals(CloudCard.PROTOCOL_SOFT) || 
                protocol.equals(CloudCard.PROTOCOL_T0)) {
                return new CloudCard(is, os, protocol);
            }
            else {
                return new CloudCard(is, os);
            }
        }
        catch (Exception e) {
            CardException ce = new CardException("SCARD_E_NO_SMARTCARD");
            ce.initCause(new Throwable("SCARD_E_NO_SMARTCARD"));
            throw ce;
        }
    }

	@Override
	public String getName() {
		return "SimplyTapp";
	}

	@Override
	public boolean isCardPresent() throws CardException {
        return true;
	}

	@Override
	public boolean waitForCardAbsent(long timeout) throws CardException {
        throw new CardException("Operation not supported");
    }

	@Override
	public boolean waitForCardPresent(long timeout) throws CardException {
        return true;
	}

}
