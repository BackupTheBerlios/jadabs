/*
 * Created on Nov 16, 2004
 *
 */
package ch.ethz.jadabs.jxme.im;


class NeighbourTuple
{
    String sipaddress;
    int status;
    
    public NeighbourTuple(String sipaddress, int status)
    {
        this.sipaddress = sipaddress;
        this.status = status;
    }
}