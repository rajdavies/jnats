/**
 * 
 */
package io.nats.client;

import java.util.Arrays;

/**
 * @author Larry McQueary
 *
 */
public final class Message {
	private String subject;
	private String replyTo;
	private byte[] data;  
	protected SubscriptionImpl sub;

	public Message() {
        this.subject = null;
        this.replyTo = null;
        this.data    = null;
        this.sub     = null;

	}
	
	public Message(String subject, String reply, byte[] data)
    {
        if (subject==null || subject.trim().length()==0)
        {
            throw new IllegalArgumentException(
                "Subject cannot be null, empty, or whitespace.");
        }

        this.subject = subject;
        this.replyTo = reply;
        this.data = data;
    }
	
	protected Message(MsgArg msgArgs, SubscriptionImpl sub, byte[] msg, long length) {
		this.subject = msgArgs.subject;
		this.replyTo = msgArgs.reply;
		this.sub = sub;
        // make a deep copy of the bytes for this message.
        this.data = new byte[(int) length];
        System.arraycopy(msg, 0, this.data, 0, (int)length);
	}

	public byte[] getData() {
		return data;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public Message setSubject(String subject) {
		this.subject = subject;
		return this;
	}
	
	public String getReplyTo() {
		return this.replyTo;
	}
	
	public Message setReplyTo(String replyTo) {
		this.replyTo = replyTo;
		return this;
	}
	
	public io.nats.client.Subscription getSubscription() {
		// TODO Auto-generated method stub
		return this.sub;
	}
	
	public Message setData(byte[] data, int offset, int len) {
		this.data = new byte[len];
		if (data != null)
			this.data = Arrays.copyOfRange(data, offset, len);
		return this;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * @return a string reprsenetation of the message
	 */
	@Override
    public String toString()
    {
		int maxBytes = 32;
		byte[] b = getData();
		int len = b.length;

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(
				"{Subject=%s;Reply=%s;Payload=<", 
				getSubject(), getReplyTo()));
		
		for (int i=0; i<maxBytes && i<len; i++) {
			sb.append((char)b[i]);
		}

//		int remainder = maxBytes - len;
		int remainder = len - maxBytes;
		if (remainder > 0) {
            sb.append(String.format("%d more bytes", remainder));
		}
		
		sb.append(">}");

        return sb.toString();
    }
}
