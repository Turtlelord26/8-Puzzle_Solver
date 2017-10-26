/**
 * 
 */
package Project1;

/**
 * @author James
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Node<T> implements Comparable<Node<T>> {

	private T cargo;
	
	private int heuristic;
	
	private int depth;
	
	private Node<T> parent;
	
	private String move;
	
	/**
	 * 
	 */
 	public Node(T cargo) {
		this.cargo = cargo;
		parent = null;
		move = null;
		depth = 0;
		heuristic = -1;
	}
	
	/**
	 * 
	 * @return
	 */
	public Node<T> getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @return
	 */
	public T getCargo() {
		return cargo;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getHeuristic() {
		return heuristic;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMove() {
		return move;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * 
	 * @param parent
	 */
	public void setParent(Node<T> parent) {
		this.parent = parent;
	}
	
	/**
	 * 
	 * @param cargo
	 */
	public void setCargo(T cargo) {
		this.cargo = cargo;
	}
	
	/**
	 * 
	 * @param heuristic
	 */
	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}
	
	/**
	 * 
	 * @param move
	 */
	public void setMove(String move) {
		this.move = move;
	}
	
	/**
	 * 
	 * @param depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node<?>) {
			Node<?> node = (Node<?>) o;
			Object p = node.getCargo();
			if (cargo instanceof int[] && p instanceof int[]) {
				int[] mcargo = (int[]) cargo;
				int[] tcargo = (int[]) p;
				if (mcargo.length != tcargo.length) {return false;}
				for (int i = 0; i < mcargo.length; i++) {
					if (mcargo[i] != tcargo[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Comparison method used in beam search by the priority queue
	 * Note: This class has a natural ordering inconsistent with equals.
	 * @param node The node being compared to
	 * @return 1 if the calling node has a greater heuristic, -1 if the called node does, and 0 if they have equal heuristics.
	 */
	@Override
	public int compareTo(Node<T> node) {
		int heur1 = this.getHeuristic();
		int heur2 = node.getHeuristic();
		if (heur1 == heur2) {
			return 0;
		} else if (heur1 < heur2) {
			return -1;
		} else {
			return 1;
		}
	}
	
	@Override
	public int hashCode() {
		if (!(cargo instanceof int[])) {return -1;}
		int[] board = (int[]) cargo;
		int result = 0;
		for (int i = 0; i < 9; i++) {
			result = result + (int)(Math.pow(10, (double) i) * board[i]);
		}
		return result;
	}	
}