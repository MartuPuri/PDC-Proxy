package itba.pdc.proxy.model;

public enum StatusRequest {
	OK(200), BAD_REQUEST(400), METHOD_NOT_ALLOWED(405), VERSION_NOT_SUPPORTED(505), CONFLICT(409), LENGTH_REQUIRED(411);
	
	private final int sId;
	
	private StatusRequest(int id) {
		this.sId = id;
	}
	
	public int getId() {
		return sId;
	}
}
