package ch.ethz.jadabs.amonem.manager;


/**
 * @author bam
 * 
 * this Skeleton is used to save the Information for a bundle, that
 * can be choosen to be installed at a Peer.
*/
public class RepositorySkeleton {
	
	private String jar;
	private String uuid;
	private String updatelocation;
	
	public RepositorySkeleton(){
		
	}
	/**
	 * @return jar
	 */
	public String getJar() {
		return jar;
	}
	/**
	 * @param jar
	 */
	public void setJar(String jar){
		this.jar = jar;
	}
	/**
	 * @return updatelocation
	 */
	public String getUpdatelocation() {
		return updatelocation;
	}
	/**
	 * @param updatelocation
	 */
	public void setUpdatelocation(String updatelocation) {
		this.updatelocation = updatelocation;
	}
	/**
	 * @return uuid
	 */
	public String getUuid() {
		return uuid;
	}
	/**
	 * @param uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
