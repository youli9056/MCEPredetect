package our;
import java.util.List;
import java.util.Set;


class Subs{
		Set<Integer> cand;
		Set<Integer>not;
		ResultNode res;
		public Subs(Set<Integer> ca, Set<Integer> n, ResultNode re){
			this.cand = ca;
			this.not =n;
			this.res = re;
		}
		public Set<Integer> getCand() {
			return cand;
		}
		public void setCand(Set<Integer> cand) {
			this.cand = cand;
		}
		public Set<Integer> getNot() {
			return not;
		}
		public void setNot(Set<Integer> not) {
			this.not = not;
		}
		public ResultNode getRes() {
			return res;
		}
		public void setRes(ResultNode res) {
			this.res = res;
		}
		
	}
