package ch.ethz.jadabs.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class HttpServerSocket extends ServerSocket 
{
    public HttpServerSocket(int port) throws IOException
    {
        super(port);
    }

    public Socket accept () throws IOException
    {
        HttpSocket s = new HttpSocket();
        implAccept(s);
        s.getRequest();
        return s;
    }
    
}