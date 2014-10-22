package ourorigin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import our.ResultNode;
import our.Status;

public class MCEBinary {
	public static HashSet<Integer> nodeSet;
	public static HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>(
			3000);
	public static int minCliqueSize = 4;
	public static int cliqueNum = 0;
	public static int treesize = 0;
	public static Stack<Status> stack = new Stack<Status>();

	static int dup = 0;// 这个用来统计通过not集判断allcontains的节点个数

	/**
	 * @param args
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static void main(String[] args) throws NumberFormatException,
			IOException {
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
			ResultNode ren = new ResultNode(null, node);
			Status size1_status = new Status(node, 1, ren, candInit, excl,
					null, null);
			 addIntoStack(size1_status);//
			// 程序有个不变条件:stack中的子图都是可以切分的,无意义的子图不加到stack中
//			stack.add(size1_status);// 这里想看下用clique切分的效果,size-1子图的数目先不算
			while (!stack.empty()) {
				Status top = stack.pop();
				ResultNode prsn = top.getRen();
				// not集
				HashSet<Integer> notset = top.getNotset();
				// 候选集
				HashMap<Integer, Integer> cand = top.getCandidate();
				// 度数到点的集合
				HashMap<Integer, HashSet<Integer>> d2c;
				// 有序的度数到点集合
				TreeMap<Integer, HashSet<Integer>> od2c;
				Integer level = top.getLevel();
				if (level + cand.size() < minCliqueSize
						|| allContained(cand, notset)) {
					dup++;
					continue;
				}
				d2c = top.getDeg2cand();
				od2c = top.getOd2c();
				if (d2c == null) {// 初始状态或者右边包含某个点的子图,度数集合需要新计算
					d2c = new HashMap<Integer, HashSet<Integer>>();
					od2c = new TreeMap<Integer, HashSet<Integer>>();
					updateDeg(cand, d2c, od2c);
				}
				if (judgeClique(d2c)) {// 第一次计算出度数集合后就可以很方便的O(1)地判读是否为clique
					emitClique(prsn, cand);
					continue;// 跳出当前stack中的子图计算,继续计算stack中的下一个子图
				} else {
					/** 当前状态不是clique需要切分计算--下面是程序的主要切分逻辑 */

					Integer aim = -1;// 切分点
					Integer mindeg = Integer.MAX_VALUE;// 切分点的度数
					while (cand.size() + level > minCliqueSize
							&& cand.size() > 1) {
						Map.Entry<Integer, HashSet<Integer>> firstEntry = od2c
								.firstEntry();
						aim = firstEntry.getValue().iterator().next();
						mindeg = firstEntry.getKey();
						HashMap<Integer, Integer> aimSet = updateMarkDeg(aim,
								mindeg, cand, d2c, od2c);
						if (level + aimSet.size() + 1 >= minCliqueSize) {
							HashSet<Integer> aimnotset = genInterSet(notset,
									aim);
							Status ss = new Status(aim, level + 1,
									new ResultNode(prsn, aim), aimSet,
									aimnotset, null, null);
							addIntoStack(ss);
						}
						notset.add(aim);// 如果这个点不能与之前的图形成clique,这里也就应该不需要加入到not集中;另外这里可以集中通过度数将小于阀值的全部删掉---有待验证

						if (judgeClique(d2c)) {
							if (cand.size() > 0) {
								Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
										.lastEntry();
								aim = lastEntry.getValue().iterator().next();
								notset.retainAll(graph.get(aim));
							}
							if (level + cand.size() < minCliqueSize)
								break;
							if (allContained(cand, notset))
								break;
							emitClique(prsn, cand);
							break;
						}
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		printResultInfo(end - start);
		close();
	}

	private static void printResultInfo(long l) {
		System.out.println("time: " + (l / 1000));
		System.out.println("tree: " + treesize);
		System.out.println("cqnm: " + cliqueNum);
	}

	static PrintWriter writer;
	static StringBuilder sb;
	static {
		try {
			writer = new PrintWriter("outbinary");
			sb = new StringBuilder();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void close() {
		writer.close();
	}

	private static void emitClique(ResultNode trsn,
			HashMap<Integer, Integer> cand) {
//		while (trsn != null) {
//			sb.append(trsn.getVal()).append(" ");
//			trsn = trsn.getPrev();
//		}
//		for (int i : cand.keySet()) {
//			sb.append(i).append(" ");
//		}
//		writer.write(sb.toString().trim());
//		writer.write("\n");
//		sb.setLength(0);
		cliqueNum++;

	}

	/**
	 * 找出notset中与aim点相邻的点集合
	 * 
	 * @param notset
	 * @param aim
	 * @return notset中与aim点相邻的点集合
	 */
	static HashSet<Integer> genInterSet(Set<Integer> notset, int aim) {
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

	static void addIntoStack(Status ss) {
		stack.add(ss);
		treesize++;
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

	private static HashMap<Integer, Integer> updateMarkDeg(int aim, int mindeg,
			HashMap<Integer, Integer> cand,
			HashMap<Integer, HashSet<Integer>> d2c,
			TreeMap<Integer, HashSet<Integer>> od2c) {
		cand.remove(aim);
		HashSet<Integer> li = d2c.get(mindeg);
		li.remove(aim);
		if (li.isEmpty()) {
			d2c.remove(mindeg);
			od2c.remove(mindeg);
		}
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		int acc = 0;
		HashSet<Integer> adj = graph.get(aim);
		if (adj.size() > cand.size()) {
			Iterator<Map.Entry<Integer, Integer>> it = cand.entrySet()
					.iterator();
			while (it.hasNext() && acc < mindeg) {
				Map.Entry<Integer, Integer> en = it.next();
				if (adj.contains(en.getKey())) {
					int point = en.getKey(), deg = en.getValue();
					HashSet<Integer> lis = d2c.get(deg);
					lis.remove(point);
					if (lis.isEmpty()) {
						d2c.remove(deg);
						od2c.remove(deg);
					}
					deg--;
					HashSet<Integer> list = d2c.get(deg);
					if (list == null) {
						list = new HashSet<Integer>();
						d2c.put(deg, list);
						od2c.put(deg, list);
					}
					list.add(point);
					en.setValue(deg);
					acc++;
					result.put(point, 0);
				}
			}
		} else {
			Iterator<Integer> it = adj.iterator();
			while (it.hasNext() && acc < mindeg) {
				int point = it.next();
				// Map.Entry<Integer, Integer> en = cand.
				if (cand.containsKey(point)) {
					int deg = cand.get(point);
					HashSet<Integer> lis = d2c.get(deg);
					lis.remove(point);
					if (lis.isEmpty()) {
						d2c.remove(deg);
						od2c.remove(deg);
					}
					deg--;
					HashSet<Integer> list = d2c.get(deg);
					if (list == null) {
						list = new HashSet<Integer>();
						d2c.put(deg, list);
						od2c.put(deg, list);
					}
					list.add(point);
					cand.put(point, deg);
					acc++;
					result.put(point, 0);
				}
			}
		}
		return result;
	}

	private static boolean judgeClique(HashMap<Integer, HashSet<Integer>> d2c) {
		if (d2c.size() > 1)
			return false;
		if (d2c.size() == 0)
			return true;
		if (d2c.size() == 1) {
			Map.Entry<Integer, HashSet<Integer>> first = d2c.entrySet()
					.iterator().next();
			if (first.getKey() + 1 == first.getValue().size())
				return true;
		}
		return false;
	}

	private static void updateDeg(HashMap<Integer, Integer> cand,
			HashMap<Integer, HashSet<Integer>> d2c,
			TreeMap<Integer, HashSet<Integer>> od2c) {
		int deg = 0;
		HashSet<Integer> adj;
		for (Map.Entry<Integer, Integer> en : cand.entrySet()) {
			int cad = en.getKey();
			adj = graph.get(cad);
			deg = 0;
			if (adj.size() > cand.size()) {
				for (int k : cand.keySet()) {
					if (adj.contains(k)) {
						deg++;
					}
				}
			} else {
				for (int k : adj) {
					if (cand.containsKey(k)) {
						deg++;
					}
				}
			}
			HashSet<Integer> d2cset = d2c.get(deg);
			if (d2cset == null) {
				d2cset = new HashSet<Integer>();
				d2c.put(deg, d2cset);
				od2c.put(deg, d2cset);
			}
			d2cset.add(cad);
			en.setValue(deg);
		}
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
			// String[] edgepoint = line.split(" ");
			// int node1 = Integer.valueOf(edgepoint[0]);
			// int node2 = Integer.valueOf(edgepoint[1]);
			int node1 = Integer.valueOf(line.substring(0, line.indexOf(split)));
			int node2 = Integer.valueOf(line.substring(
					line.lastIndexOf(split) + 1, line.length()));
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
}
