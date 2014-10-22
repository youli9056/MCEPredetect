package our;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class MCEPredect {
	public static HashSet<Integer> nodeSet;
	public static HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>(
			3000);
	public static int minCliqueSize = 4;
	public static int cliqueNum = 0;
	public static int treesize = 0;
	public static Stack<Status> stack = new Stack<Status>();

	static int dup = 0;//这个用来统计通过not集判断allcontains的节点个数
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
			addIntoStack(size1_status);// 程序有个不变条件:stack中的子图都是可以切分的,无意义的子图不加到stack中
//			stack.add(size1_status);//这里想看下用clique切分的效果,size-1子图的数目先不算
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
						|| allContained(cand, notset)){
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
					boolean finishedthis = false;// 这个子图有没有切完
					/** 这里是Predetect的主要逻辑,要求尽快从cand找出一个不被not集中的点都邻接的小clique */
					// 先找单个的不与not集中的点都相邻的点
//					HashSet<Integer> unconnected = getUnConnectedPoints(
//							cand.size(), od2c, notset);
//					while (!unconnected.isEmpty()) {// 第一个点的候选点必然都是不相邻的；有个问题,直接切分
//													//是按第一次的度数大小排序,没有使用之后的度数更新值也就
//													//是说每次切分的不是最小度数点
////						Iterator<Integer> iter = unconnected.iterator();
////						aim = iter.next();
////						iter.remove();// 用过就没用了
////						mindeg = cand.get(aim);
//						boolean foundpoint = false;
//						for(Set<Integer> ns:od2c.values()){
//							for(Integer cpoint:ns){//这样做应该可以保证用最小度数切,但是会带来重复扫描的性能损失
//								if(unconnected.contains(cpoint)){
//									aim = cpoint;
//									mindeg = cand.get(aim);
//									unconnected.remove(cpoint);
//									foundpoint = true;
//									break;
//								}
//							}
//							if(foundpoint)
//								break;
//						}
//						HashMap<Integer, Integer> aimSet = updateMarkDeg(aim,
//								mindeg, cand, d2c, od2c);
//						if (level + aimSet.size() + 1 >= minCliqueSize) {
//							HashSet<Integer> aimnotset = genInterSet(notset,
//									aim);
//							Status ss = new Status(aim, level + 1,
//									new ResultNode(prsn, aim), aimSet,
//									aimnotset, null, null);
//							addIntoStack(ss);
//						}
//						notset.add(aim);
//						unconnected.removeAll(graph.get(aim));// aim点加入到not集中,要更新unconnected
//						if (judgeClique(d2c)) {
//							finishedthis = true;
//							if (cand.size() > 0) {
//								Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
//										.lastEntry();
//								aim = lastEntry.getValue().iterator().next();
//								notset.retainAll(graph.get(aim));
//							}
//							if (level + cand.size() < minCliqueSize)
//								break;
//							if (allContained(cand, notset))
//								break;
//							emitClique(prsn, cand);
//							break;
//						}
//					}
					if (finishedthis)
						continue;
					while (cand.size() + level > minCliqueSize && cand.size()>1 && !finishedthis) {// &&
																					// cand.size()
																					// >
																					// 1这一段在前面的judgeclique里面已经保证
						// 按点的度数由小到大找一个最小的Clique出来
						Map.Entry<Integer, HashSet<Integer>> firstEntry = od2c
								.firstEntry();
						aim = firstEntry.getValue().iterator().next();//
						mindeg = firstEntry.getKey();
						HashMap<Integer, Integer> aimSet = updateMarkDeg(aim,
								mindeg, cand, d2c, od2c);
						List<Integer> clique = null;
						HashSet<Integer> aimnotset = null;
						Status ss=null;
						if (level + aimSet.size() + 1 >= minCliqueSize) {
							aimnotset = genInterSet(notset,
									aim);
							 ss = new Status(aim, level + 1,
									new ResultNode(prsn, aim), aimSet,
									aimnotset, null, null);
						}
						notset.add(aim);
							if (judgeClique(d2c)) {
								finishedthis = true;
								if(ss!=null)
									addIntoStack(ss);
								if (cand.size() > 0) {
									Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
											.lastEntry();
									aim = lastEntry.getValue().iterator()
											.next();
									notset.retainAll(graph.get(aim));
								}
								if (level + cand.size() < minCliqueSize)
									break;
								if (allContained(cand, notset))
									break;
								emitClique(prsn, cand);
								break;
							}
							if (level + aimSet.size() + 1 >= minCliqueSize) {
							if(aimnotset.isEmpty()){
								addIntoStack(ss);
							}else{
								treesize++;// 因为ss这个子图没有在其他地方加入,这里要计数
								clique = getSmallestUnCoveredCliuqe(ss, stack);//这里判断下aimnotset是不是空,是空的话不找了,直接加到stack中--待验证
							}
							}
//						notset.add(aim);//如果这个点不能与之前的图形成clique,这里也就应该不需要加入到not集中;另外这里可以集中通过度数将小于阀值的全部删掉---有待验证
//						if (clique == null||clique.isEmpty()) {// 没有找到这样一个小clique,则这个点无效,删除
//							if (judgeClique(d2c)) {
//								finishedthis = true;
//								if (cand.size() > 0) {
//									Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
//											.lastEntry();
//									aim = lastEntry.getValue().iterator()
//											.next();
//									notset.retainAll(graph.get(aim));
//								}
//								if (level + cand.size() < minCliqueSize)
//									break;
//								if (allContained(cand, notset))
//									break;
//								emitClique(prsn, cand);
//								break;
//							}
//						} else {// 找到了这样一个小clique,以这个clique的点作为切分点,切分子图
						if(clique!=null&&!clique.isEmpty()){
							for (Integer an : clique) {//这样的个小clique的点是不是保持最小度数的条件?
								aim = an;
								mindeg = cand.get(aim);
								HashMap<Integer, Integer> aimCand = updateMarkDeg(
										aim, mindeg, cand, d2c, od2c);
								if (level + aimSet.size() + 1 >= minCliqueSize) {
									HashSet<Integer> aimnot = genInterSet(
											notset, aim);
									Status aimss = new Status(aim, level + 1,
											new ResultNode(prsn, aim), aimCand,
											aimnot, null, null);
									addIntoStack(aimss);
								}
								notset.add(aim);
								if (judgeClique(d2c)) {
									finishedthis = true;
									if (cand.size() > 0) {
										Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
												.lastEntry();
										aim = lastEntry.getValue().iterator()
												.next();
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

	/**
	 * 和updatemarkdeg的作用一样,只有由于不要返回结果,这个操作不涉及结果的问题
	 * 
	 * @param aim
	 * @param mindeg
	 * @param cand
	 * @param d2c
	 * @param od2c
	 */
	private static void deleteUnUsefulNode(Integer aim, Integer mindeg,
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
				}
			}
		}
	}

	/**
	 * 整体上来将,这一段的过程是广度搜索,有可能会有膨胀问题
	 * 
	 * @param ss
	 * @param stack
	 * @return 找到的小clique,不包括anchor点;由原度数从小到大排列
	 */
	private static List<Integer> getSmallestUnCoveredCliuqe(Status ss,
			Stack<Status> stack) {
		List<Status> smallStack = new LinkedList<Status>();
		Integer anchor = ss.getVp();
		smallStack.add(ss);
		while (!smallStack.isEmpty()) {
			Status top = smallStack.get(0);
			smallStack.remove(0);
			HashSet<Integer> notset = top.getNotset();
			HashMap<Integer, Integer> cand = top.getCandidate();
			HashMap<Integer, HashSet<Integer>> d2c;
			TreeMap<Integer, HashSet<Integer>> od2c;
			ResultNode trsn = top.getRen();
			int level = top.getLevel();
			if (allContained(cand, notset)) {//要不要判断度数和结果集和的大小
				continue;
			}
			d2c = top.getDeg2cand();
			od2c = top.getOd2c();
			if (d2c == null) {
				d2c = new HashMap<Integer, HashSet<Integer>>();
				od2c = new TreeMap<Integer, HashSet<Integer>>();
				updateDeg(cand, d2c, od2c);
			}
			if (judgeClique(d2c)) {
				emitClique(trsn, cand);
				// 是clique,找到!
				addIntoStack(smallStack);
				List<Integer> result = new ArrayList<Integer>();
				while (!trsn.getVal().equals(anchor)) {
					result.add(trsn.getVal());
					trsn = trsn.getPrev();
				}
				for (Set<Integer> buk : od2c.values()) {
					result.addAll(buk);
				}
				return result;
			} else {
				Integer aim = 0, mindeg = Integer.MAX_VALUE;
				boolean finished = false;
				while (cand.size() + level > minCliqueSize && !finished) {// 即使
					Map.Entry<Integer, HashSet<Integer>> firstEntry = od2c
							.firstEntry();
					aim = firstEntry.getValue().iterator().next();//
					mindeg = firstEntry.getKey();
					HashMap<Integer, Integer> aimSet = updateMarkDeg(aim,
							mindeg, cand, d2c, od2c);
					if (level + aimSet.size() + 1 >= minCliqueSize) {
						HashSet<Integer> aimnotset = genInterSet(notset, aim);
						Status sts = new Status(aim, level + 1, new ResultNode(
								trsn, aim), aimSet, aimnotset, null, null);
						smallStack.add(sts);

						if (aimnotset.isEmpty()) {
							// 找到了一个小clique满足条件
							ResultNode rnrs = sts.getRen();
							List<Integer> result = new ArrayList<Integer>();
							while (!rnrs.getVal().equals(anchor)) {
								result.add(rnrs.getVal());
								rnrs = rnrs.getPrev();
							}
							addIntoStack(smallStack);
							notset.add(aim);// 由于提前返回,要把aim加到not集中
							stack.add(top);// 当前子图不再切分；因为加入时计过数了,这里不需要再计数
							return result;
						}
					}
					notset.add(aim);
					if (judgeClique(d2c)) {
						if (cand.size() > 0) {
							Map.Entry<Integer, HashSet<Integer>> lastEntry = od2c
									.lastEntry();
							aim = lastEntry.getValue().iterator().next();
							notset.retainAll(graph.get(aim));
						}
						if (level + cand.size() < minCliqueSize) {
							break;
						}
						if (allContained(cand, notset)) {
							break;
						} else {
							emitClique(trsn, cand);
							// 是clique,找到!
							addIntoStack(smallStack);
							List<Integer> result = new ArrayList<Integer>();
							while (!trsn.getVal().equals(anchor)) {
								result.add(trsn.getVal());
								trsn = trsn.getPrev();
							}
							for (Set<Integer> buk : od2c.values()) {
								result.addAll(buk);
							}
							return result;
						}
					}
				}
			}
		}
		return null;
	}

	static PrintWriter writer;
	static StringBuilder sb;
	static {
		try {
			writer = new PrintWriter("outpredect");
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
//		 while (trsn != null) {
//		 sb.append(trsn.getVal()).append(" ");
//		 trsn = trsn.getPrev();
//		 }
//		 for (int i : cand.keySet()) {
//		 sb.append(i).append(" ");
//		 }
//		 writer.write(sb.toString().trim());
//		 writer.write("\n");
//		 sb.setLength(0);
		cliqueNum++;

	}

	/**
	 * 找到由点aim出发最小的一个不被notset中的点都覆盖的clique 这里有部分工作白做了, 问题:要不要检查not集做剪枝
	 * 要不要将子图状态返回,不然搜索树白搜索了
	 * 
	 * @param aim
	 * @param mindeg
	 * @param cand
	 * @param od2c
	 * @param notset
	 * @return
	 */
	private static ResultNode getSmallestUnCoveredCliuqe(Integer aim,
			Integer mindeg, HashMap<Integer, Integer> cand,
			TreeMap<Integer, HashSet<Integer>> od2c, HashSet<Integer> notset) {
		HashSet<Integer> tnot = new HashSet<Integer>(notset);
		tnot.retainAll(graph.get(aim));
		Set<Integer> tcand = new HashSet<Integer>(mindeg);
		HashSet<Integer> adj = graph.get(aim);
		for (Integer i : cand.keySet()) {
			if (adj.contains(i))
				tcand.add(i);
		}
		ResultNode res = new ResultNode(null, aim);
		Subs sbs = new Subs(tcand, tnot, res);
		List<Subs> queue = new LinkedList<Subs>();// 广度优先搜索
		queue.add(sbs);
		while (!queue.isEmpty()) {
			Subs top = queue.get(0);
			queue.remove(0);
			Set<Integer> Cand = top.getCand();
			Set<Integer> Not = top.getNot();
			ResultNode Res = top.getRes();
			for (Integer p : Cand) {
				Set<Integer> tNot = genInterSet(Not, p);
				ResultNode tRes = new ResultNode(Res, p);
				if (tNot.isEmpty()) {
					queue.clear();
					return tRes;
				} else {
					Set<Integer> tCand = genInterSet(Cand, p);
					queue.add(new Subs(tCand, tNot, tRes));
				}
			}
		}
		return null;
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

	static void addIntoStack(Collection<Status> col) {
		stack.addAll(col);
		treesize += col.size();
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

	/**
	 * 找出cand中与所有notset中的点都不相邻的结果
	 * 这里有保序
	 * @param size
	 *            cand的初始大小
	 * @param od2c
	 *            有序的点集
	 * @param notset
	 *            not集
	 * @return 找出cand中与所有notset中的点都不相邻的点
	 */
	static HashSet<Integer> getUnConnectedPoints(int size,
			TreeMap<Integer, HashSet<Integer>> od2c, HashSet<Integer> notset) {
		LinkedHashSet<Integer> res = new LinkedHashSet<Integer>(size);
		for (Set<Integer> val : od2c.values()) {
			res.addAll(val);
		}
		for (Integer p : notset) {
			res.removeAll(graph.get(p));
		}
		return res;
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
