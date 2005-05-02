package ch.ethz.jadabs.sbbws.ksoap;



/**
 * Sent from server to client if at least one of the two stations are ambiguous. Contains a selection of possible matches.
 * This class is never used, it's here only for the sake of completeness.
 * 
 * @author Stefan Vogt
 */
public abstract class TimetableInquiry
{
	/** Ambiguous stations in the from field */
	public String[] from;
	
	/** Ambiguous stations in the to field */
	public String[] to;

	/*  
	 *  The following values are the ones submitted by the user. They are returned again,
	 *  so that the mobile device doesn't need to maintain a state.
	 */
	public String requestedFrom;
	public String requestedTo;
	public String requestedDate;
	public String requestedTime;
	public int    requestedTimeToggle;
}