package our;

public class ResultNode {
	ResultNode prev;
	Integer val;
	/**
	 * @param prev
	 * @param val
	 */
	public ResultNode(ResultNode prev, Integer val) {
		super();
		this.prev = prev;
		this.val = val;
	}
	public ResultNode getPrev() {
		return prev;
	}
	public void setPrev(ResultNode prev) {
		this.prev = prev;
	}
	public Integer getVal() {
		return val;
	}
	public void setVal(Integer val) {
		this.val = val;
	}
	

}
