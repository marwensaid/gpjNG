package net.sourceforge.gpj.jcremoteterminal;

import java.io.InputStream;
import java.io.OutputStream;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

public class CloudCard extends Card {

    private CloudChannel channel;
	private ATR atr;

	CloudCard(InputStream is, OutputStream os) throws CardException
	{
        channel = new CloudChannel(is, os, this);
        this.atr = new ATR(channel.sCardReset());
	}
	
	@Override
	public void beginExclusive() throws CardException {
		throw new CardException("Operation not supported");
	}

	@Override
	public void disconnect(boolean reset)  throws CardException {
        if (channel != null) {
            if (reset) {
                channel.sCardReset();
            }
            channel = null;
            atr = null;
        }
    }

	@Override
	public void endExclusive() throws CardException {
		throw new CardException("Operation not supported");
	}

	@Override
	public ATR getATR() {
		return atr;
	}

	@Override
	public CardChannel getBasicChannel() {
		return channel;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public CardChannel openLogicalChannel() throws CardException {
        return channel;
	}

	@Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        if (controlCode != 0) {
            throw new CardException("Operation not supported");
        }
        byte[] atrBytes = channel.sCardReset();
        this.atr = new ATR(channel.sCardReset());
        return atrBytes;
    }

}
