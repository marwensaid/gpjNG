package net.sourceforge.gpj.jcremoteterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class CloudChannel extends CardChannel {

    private static final byte NFC_CHANNEL    = 0x01;
    private static final byte SOCKET_CHANNEL = 0x11;
    private static final byte SOFT_CHANNEL   = 0x21;
    private static final byte DEVICE_CHANNEL = 0x31;

    private InputStream is;
    private OutputStream os;
    private CloudCard card;

    CloudChannel(InputStream is, OutputStream os, CloudCard card) {
        this.is = is;
        this.os = os;
        this.card = card;
    }

    @Override
    public void close() throws CardException {
        is = null;
        os = null;
    }

    @Override
    public Card getCard() {
        return card;
    }

    @Override
    public int getChannelNumber() {
        return 0;
    }

    private byte[] sRead() throws IOException {
        int len = 0;

        int r = is.read();
        if (r == -1)
            throw new IOException();
        byte a = (byte) r;

        r = is.read();
        if (r == -1)
            throw new IOException();

        if ((a & 0x40) != 0x00) {
            r = is.read();
            if (r == -1)
                throw new IOException();
            len = (int) (0xFF0000 & (r << 16));
        }

        r = is.read();
        if (r == -1)
            throw new IOException();
        len = (int) (0xFF00 & (r << 8));

        r = is.read();
        if (r == -1)
            throw new IOException();
        len |= r;

        byte[] pkt = new byte[len];
        is.read(pkt, 0, len);
        return pkt;
    }

    protected void sWrite(byte[] buffer) throws IOException {
        try {
            byte channel = NFC_CHANNEL;
            if (CloudCard.PROTOCOL_SOCKET.equals(card.getProtocol())) {
                channel = SOCKET_CHANNEL;
            }
            else if (CloudCard.PROTOCOL_SOFT.equals(card.getProtocol())) {
                channel = SOFT_CHANNEL;
            }
            else if (CloudCard.PROTOCOL_T0.equals(card.getProtocol())) {
                channel = DEVICE_CHANNEL;
            }

            byte[] tmp;
            if (buffer.length > 0xff) {
                tmp = new byte[buffer.length + 5];
                tmp[0] = (byte) (0x40 | channel);
                tmp[1] = 0x00;
                tmp[2] = (byte) ((buffer.length & 0xFF0000) >> 16);
                tmp[3] = (byte) ((buffer.length & 0x00FF00) >> 8);
                tmp[4] = (byte) (buffer.length & 0x0000FF);
                System.arraycopy(buffer, 0, tmp, 5, buffer.length);
            }
            else {
                tmp = new byte[buffer.length + 4];
                tmp[0] = channel;
                tmp[1] = 0x00;
                tmp[2] = (byte) ((buffer.length & 0xFF00) >> 8);
                tmp[3] = (byte) (buffer.length & 0x00FF);
                System.arraycopy(buffer, 0, tmp, 4, buffer.length);
            }
            buffer = tmp;
            os.write(buffer);
            os.flush();
        }
        catch (Exception e) {
            throw new IOException();
        }
    }

    byte[] sCardReset() throws CardException {
        try {
            os.write(new byte[] { 0x00, 0x00, 0x00, 0x00 });
            os.flush();
            return sRead(); // ATR bytes
        }
        catch (IOException e) {
            throw new CardException(e);
        }
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU apdu) throws CardException {
        try {
            sWrite(apdu.getBytes());
            byte[] apduR = sRead();
            return new ResponseAPDU(apduR);
        }
        catch (IOException e) {
            throw new CardException(e);
        }
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        byte[] commandBytes = new byte[command.remaining()];
        command.get(commandBytes);

        try {
            sWrite(commandBytes);
            byte[] responseBytes = sRead();
            response.put(responseBytes);
            return responseBytes.length;
        }
        catch (IOException e) {
            throw new CardException("Card I/O Error", e);
        }
    }

}
