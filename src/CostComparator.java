import java.util.Comparator;

/**
 * Returns 1 if Node 1's cost (F) is larger than Node 2's
 * @author Michael A. Long
 *
 */
public class CostComparator implements Comparator<Node> {

	@Override
	public int compare(Node n1, Node n2) {
		int ret = Double.compare(n1.F, n2.F);
		if (ret > 0) return 1;
		else if (ret < 0) return -1;
		else return 0;
	}

}
