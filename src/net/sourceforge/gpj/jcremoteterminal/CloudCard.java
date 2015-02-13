package net.sourceforge.gpj.jcremoteterminal;

import java.io.InputStream;
import java.io.OutputStream;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

public class CloudCard extends Card {

    // Supported APDU protocols.
    public static final String PROTOCOL_NFC    = "NFC";
    public static final String PROTOCOL_SOCKET = "SOCKET";
    public static final String PROTOCOL_SOFT   = "SOFT";
    public static final String PROTOCOL_T0     = "T0";

    private CloudChannel channel;
    private ATR atr;
    private String protocol;

    CloudCard(InputStream is, OutputStream os) throws CardException {
        // NFC is default protocol if not specified.
        this(is, os, PROTOCOL_NFC);
    }

    CloudCard(InputStream is, OutputStream os, String protocol) throws CardException {
        channel = new CloudChannel(is, os, this);
        this.atr = new ATR(channel.sCardReset());

        // NFC is default protocol if specified protocol is not known.
        this.protocol = PROTOCOL_NFC;
        if (PROTOCOL_SOCKET.equals(protocol) || 
            PROTOCOL_SOFT.equals(protocol) || 
            PROTOCOL_T0.equals(protocol)) {
            this.protocol = protocol;
        }
    }

    @Override
    public void beginExclusive() throws CardException {
        throw new CardException("Operation not supported");
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
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
        return protocol;
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
