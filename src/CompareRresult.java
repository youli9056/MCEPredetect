import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CompareRresult {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		new CompareRresult().test("/home/youli/workspace/MCEPredectJava/outpredect","/home/youli/workspace/MCEPredectJava/outbk");
//		new CompareRresult().test("/home/youli/workspace/MCEPredectJava/outpredect");
		ReadFile rf = new ReadFile();
		rf.readFileByLines("/home/youli/workspace/MCEPredectJava/outbk");
		rf.writeToFile("sortedbk");
	}
	private void test(String file1, String file2) {
		ReadFile rfi = new ReadFile();
		ReadFile rfj = new ReadFile();
		rfi.readFileByLines(file1);
		rfj.readFileByLines(file2);
		List<List<Integer>> cqi = rfi.resultfile;
		List<List<Integer>> cqj = rfj.resultfile;
		
	}
	private void test(String file){
		ReadFile rf = new ReadFile();
		rf.readFileByLines(file);
		List<List<Integer>> cq = rf.resultfile;
		for(int i = 0; i < cq.size()-1; i++){
			List<Integer> co = cq.get(i);
			for(int j = i+1; j < cq.size(); j++){
				List<Integer> ci = cq.get(j);
				if(duplicate(ci,co)){
					System.out.println(ci+" dup "+co);
				}
			}
		}
	}
	private boolean duplicate(List<Integer> ci, List<Integer> co) {
		List<Integer> big,small;
		if(ci.size()>co.size()){
			big = ci;
			small = co;
		}else{
			big = co;
			small = ci;
		}
		//可以根据有序另外做优化
		return big.containsAll(small);
	}
	static class ReadFile {
		public List<List<Integer>> resultfile = new ArrayList<List<Integer>>();

		public void readFileByLines(String fileName) {
			File file = new File(fileName);
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				int i = 0;
				String tempString = "";
				while ((tempString = reader.readLine()) != null) {
					// resultStr = "";
					String[] str = tempString.trim().split(" ");
					if(str.length<3)continue;
					ArrayList<Integer> tempdata = new ArrayList<Integer>(str.length);
					for (i = 0; i < str.length; i++) {
						if (str[i].trim().length()==0)
							continue;
						tempdata.add(Integer.parseInt(str[i]));
					}
					Collections.sort(tempdata);
					resultfile.add(tempdata);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
					}
				}
			}
		}
		public void writeToFile(String fileName){
			try {
				BufferedWriter writer = new BufferedWriter(new PrintWriter(fileName));
				for(List<Integer> cq:resultfile){
					writer.write(cq.toString());
					writer.write("\n");
				}
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
