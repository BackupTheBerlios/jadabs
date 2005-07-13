package ch.ethz.jadabs.im.sip.servertest;

/*
 * Created on 10 Nov, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;

/**
 * @author sky
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerSipListener implements SipListener {
	public void processRequest(RequestEvent requestEvent) {
		System.out.println("\n\n\n***************************************\nSipListener.processRequest\n");
		System.out.println("------\nREQUEST\n------\n" + requestEvent.getRequest().toString());
		System.out.println("------------\nREQUEST EVENT\n------------\n" + requestEvent.toString());
		System.out.println("***************************************\n\n\n\n\n");

	}	
	public void processResponse(ResponseEvent responseEvent) {
		//		System.out.println("SipListener.processResponse\n" + responseEvent.toString());
	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
		System.out.println("***************************************\n");
		System.out.println("TIMEOUT\n"+timeoutEvent.getTimeout().toString());
		System.out.println("***************************************\n\n\n\n\n");
	}

}