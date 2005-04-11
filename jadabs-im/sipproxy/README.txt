Modifications from original proxy :
Deleted all classes related to the gui.
+ modifications in :
gov.nist.sip.proxy.Proxy (line 710-721 + 1194-1197)
gov.nist.sip.proxy.authentication.DigestServerAuthentication (line 184-198)
gov.nist.sip.proxy.presenceserver.PresentityManager (line 43-44)
To run : 
- run a DB
- configure the connection using cayenne
- configure IP and port for the proxy (in govARGnistARGsipARGconfARGfileAUSECOURS.xml)
- change the realm in build.xml
- set the smtp gateway address in build.xml
- ant run (will compile and run Proxy, if needed adapt options in build.xml)