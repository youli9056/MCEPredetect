package our;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Status {

	private HashMap<Integer, Integer> candidate;
	private HashSet<Integer> notset;
	private ResultNode ren;
	private int level;
	private Integer vp;
	HashMap<Integer, HashSet<Integer>> deg2cand;
	TreeMap<Integer, HashSet<Integer>> od2c;
	public Status(Integer tmpKey, int lv, ResultNode rn, HashMap<Integer, Integer> vertex,
			HashSet<Integer> tnot,
			HashMap<Integer, HashSet<Integer>> deg2cand2,
			TreeMap<Integer, HashSet<Integer>> od) {
		vp = tmpKey;
		ren = rn;
		level = lv ;
		candidate = vertex;
		notset = tnot;
		deg2cand = deg2cand2;
		od2c = od;
	}

	public Integer getVp() {
		return vp;
	}

	public TreeMap<Integer, HashSet<Integer>> getOd2c() {
		return od2c;
	}

	public ResultNode getRen() {
		return ren;
	}

	public void setRen(ResultNode ren) {
		this.ren = ren;
	}

	public void setOd2c(TreeMap<Integer, HashSet<Integer>> od2c) {
		this.od2c = od2c;
	}

	public void setVp(int vp) {
		this.vp = vp;
	}

	public void setCandidate(HashMap<Integer, Integer> init) {
		this.candidate = init;
	}

	public HashMap<Integer, Integer> getCandidate() {
		return this.candidate;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public HashMap<Integer, HashSet<Integer>> getDeg2cand() {
		return deg2cand;
	}

	public void setDeg2cand(HashMap<Integer, HashSet<Integer>> deg2cand) {
		this.deg2cand = deg2cand;
	}

	public HashSet<Integer> getNotset() {
		return notset;
	}

	public void setNotset(HashSet<Integer> notset) {
		this.notset = notset;
	}
}