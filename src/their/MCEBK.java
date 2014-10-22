package their;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class MCEBK {
	public static HashSet<Integer> nodeSet;
	public static HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>(
			3000);
	public static int minCliqueSize = 4;
	public static int cliqueNum = 0;
	public static int treesize = 0;
	public static Stack<CPD> stack = new Stack<CPD>();
	public static List<Integer> result = new LinkedList<Integer>();

	private static int maxdeg;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		readInData(args[0]);
		nodeSet = new HashSet<Integer>(graph.size());
		nodeSet.addAll(graph.keySet());
		long start = System.currentTimeMillis();
		for (Integer node : nodeSet) {
			// 为每一个初始候选点建立size-1子图结构
			HashSet<Integer> adj = graph.get(node);
			HashSet<Integer> excl = new HashSet<Integer>();
			HashMap<Integer, Integer> candInit = new HashMap<Integer, Integer>();
			for (Integer nadj : adj) {
				if (nadj > node)
					candInit.put(nadj, null);
				else if (nadj < node)
					excl.add(nadj);
			}
			if (candInit.size() + 1 < minCliqueSize)
				continue;
			CPD top = new CPD(node, 1, candInit, excl);
			addIntoStack(top);
//			stack.add(top);
			while (!stack.empty()) {
				top = stack.pop();
				HashSet<Integer> notset = top.getExcl();
				HashMap<Integer, Integer> cand = top.getCand();
				int level = top.getLevel();
				int vp = top.getVisitedPoint();
				if (allContained(cand, notset)) {
					continue;
				}
				if (result.size() + 1 == level) {
					result.add(vp);
				} else {
					result.set(level - 1, vp);
				}
				if (cand.isEmpty()) {
					if (notset.isEmpty()) {
						emitClique(result, level, cand);
					}
					continue;
				}
				int fixp = findMaxDegreePoint(cand);
				ArrayList<Integer> noneFixp = new ArrayList<Integer>(
						cand.size() - maxdeg);
				HashMap<Integer, Integer> tmpcand = genInterSet(cand, fixp,
						maxdeg, noneFixp);
				if(level+1+tmpcand.size()>=minCliqueSize){
					HashSet<Integer> tmpnot = genInterSet(notset, fixp);
					CPD tmp = new CPD(fixp, level + 1, tmpcand, tmpnot);
					addIntoStack(tmp);
				}
				notset.add(fixp);
				for (int fix : noneFixp) {
					HashMap<Integer, Integer> tcd = genInterSet(cand, fix);
					if(level+1+tcd.size()>=minCliqueSize){
						HashSet<Integer> tnt = genInterSet(notset, fix);
						CPD temp = new CPD(fix, level + 1, tcd, tnt);
						addIntoStack(temp);
					}
					notset.add(fix);
				}
			}
		}
		long end = System.currentTimeMillis();
		printResultInfo(end-start);
		close();
	}
	private static void addIntoStack(their.CPD temp) {
		treesize++;
		stack.add(temp);
	}
	private static HashMap<Integer, Integer> genInterSet(
			HashMap<Integer, Integer> cand, int aim) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		int acc = 0;
		HashSet<Integer> adj = graph.get(aim);
		cand.remove(aim);
		Set<Integer> small, big;
		if (adj.size() > cand.size()) {
			small = cand.keySet();
			big = adj;
		} else {
			big = cand.keySet();
			small = adj;
		}
		Iterator<Integer> it = small.iterator();
		int tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (big.contains(tmp)) {
				acc++;
				result.put(tmp, 0);
			}
		}
		return result;
	}
	private static HashMap<Integer, Integer> genInterSet(
			HashMap<Integer, Integer> cand, int aim, int maxdeg,
			ArrayList<Integer> noneFixp) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		int acc = 0;
		HashSet<Integer> adj = graph.get(aim);
		cand.remove(aim);
		Iterator<Integer> it = cand.keySet().iterator();
		int tmp;
		while (acc < maxdeg && it.hasNext()) {
			tmp = it.next();
			if (adj.contains(tmp)) {
				result.put(tmp, 0);
			} else {
				noneFixp.add(tmp);
			}
		}
		while (it.hasNext())
			noneFixp.add(it.next());
		return result;
	}
	private static HashSet<Integer> genInterSet(HashSet<Integer> notset, int aim) {
		HashSet<Integer> result = new HashSet<Integer>();
		HashSet<Integer> adj = graph.get(aim);
		if (adj.size() > notset.size()) {
			for (int i : notset) {
				if (adj.contains(i))
					result.add(i);
			}
		} else {
			for (int i : adj) {
				if (notset.contains(i))
					result.add(i);
			}
		}
		return result;
	}
	private static int findMaxDegreePoint(HashMap<Integer, Integer> cand) {
		int maxpoint = 0, tmpdeg = 0;
		maxdeg = -1;

		for (Map.Entry<Integer, Integer> en : cand.entrySet()) {
			HashSet<Integer> adj = graph.get(en.getKey());
			tmpdeg = computeDeg(adj, cand.keySet());
			if (tmpdeg > maxdeg) {
				maxdeg = tmpdeg;
				maxpoint = en.getKey();
			}
		}
		return maxpoint;
	}
	private static int computeDeg(HashSet<Integer> adj, Set<Integer> keySet) {
		int deg = 0;
		if (adj.size() > keySet.size()) {
			for (int k : keySet) {
				if (adj.contains(k))
					deg++;
			}
		} else {
			for (int k : adj) {
				if (keySet.contains(k))
					deg++;
			}
		}
		return deg;
	}
	static PrintWriter writer;
	static StringBuilder sb;
	static{
		 try {
			writer = new PrintWriter("outbk");
			sb = new StringBuilder();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void close(){
		writer.close();
	}
	private static void emitClique(List<Integer> result2, int level,
			HashMap<Integer, Integer> cand) {
//		for (int i = 0; i < level; i++) {
//			sb.append(result.get(i)).append(" ");
//		}
//		for (int i : cand.keySet()) {
//			sb.append(i).append(" ");
//		}
//		writer.write(sb.toString().trim());
//		writer.write("\n");
//		sb.setLength(0);
		cliqueNum++;
	}

	private static boolean allContained(HashMap<Integer, Integer> cand,
			HashSet<Integer> notset) {
		for (int nt : notset) {
			HashSet<Integer> nadj = graph.get(nt);
			if (nadj.containsAll(cand.keySet()))
				return true;
		}
		return false;
	}

	static String split = "\t";
	public static void readInData(String filename)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = "";
		line = reader.readLine();
		if(!line.contains("\t"))
			split = " ";
		reader.close();
		reader = new BufferedReader(new FileReader(filename));
		while ((line = reader.readLine()) != null) {
//			String[] edgepoint = line.split(" ");
//			int node1 = Integer.valueOf(edgepoint[0]);
//			int node2 = Integer.valueOf(edgepoint[1]);
			int node1 = Integer.valueOf(line.substring(0, line.indexOf(split)));
			int node2 = Integer.valueOf(line.substring(line.lastIndexOf(split)+1,line.length()));
			HashSet<Integer> tmp = graph.get(node1);
			if (tmp == null) {
				tmp = new HashSet<Integer>();
				graph.put(node1, tmp);
			}
			tmp.add(node2);
			tmp = graph.get(node2);
			if (tmp == null) {
				tmp = new HashSet<Integer>();
				graph.put(node2, tmp);
			}
			tmp.add(node1);
		}
	}
	private static void printResultInfo(long l) {
		System.out.println("time: "+(l/1000));
		System.out.println("tree: "+treesize);
		System.out.println("cqnm: "+cliqueNum);
	}
}
