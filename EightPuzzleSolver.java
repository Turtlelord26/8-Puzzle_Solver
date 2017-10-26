package Project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Scanner;

public class EightPuzzleSolver {
	
	/**
	 * Field used to store the moves made by a successful beam search
	 */
	public LinkedList<String> beamMoves = new LinkedList<String>();
	
	/**
	 * Field used to store which type of heuristic is of interest at a given time
	 */
	public int heuristicType;
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println("Excess Inputs");
			return;
		} else if (args.length == 1) {
			new EightPuzzleSolver(args[1]);
		} else {
			new EightPuzzleSolver(""); //Since "" will never be a filepath, this avoids having two constructors
		}
	}
	
	public EightPuzzleSolver(String fileName) {
		int[] board = {0,1,2,3,4,5,6,7,8};
		board = randomize(board, 100);
		heuristicType = 1;
		printBoard(board);
		System.out.println("Enter Commands.");
		if (fileName == "") {
			manualCommands(board, 100, 1000); //default values for randomization steps and maxNodes
		} else {
			fileCommands(board, fileName, 100, 1000);
		}
	}
	
	public void fileCommands(int[] board, String fileName, int steps, int maxNodes) {
		try {
			Scanner scnr = new Scanner(new File(fileName));
			scnr.useDelimiter("\n");
			commandsLoop(board, steps, maxNodes, scnr);
			scnr.close(); //This line is only reached if the file includes an Exit command
			return;
		} catch (FileNotFoundException e) {
			System.out.println("Could Not Find the Specified File");
			return;
		} catch (NoSuchElementException e) { //Triggers when the scanner has finished reading the file
			manualCommands(board, steps, maxNodes); //Allows manual input of further commands, but (BUG) resets steps and maxNodes.
		}
		
	}
	
	public void manualCommands(int[] board, int steps, int maxNodes) { //-1 maxNodes corresponds to no limit.
		Scanner scnr = new Scanner(System.in);
		scnr.useDelimiter("\n");
		commandsLoop(board, steps, maxNodes, scnr);
		scnr.close(); //This line is only reached when a user commands Exit
	}
	
	private void commandsLoop(int[] board, int steps, int maxNodes, Scanner scnr) {
		int zpos;
		String protoCommand = scnr.next();
		String command = protoCommand.substring(0, protoCommand.length() - 1);
		while (! command.equals("exit")) {
			switch (command) {
			case "reset":
				int[] reset = {0,1,2,3,4,5,6,7,8};
				board = reset;
				break;
			case "randomizeState":
				board = randomize(board, steps);
				break;
			case "printState":
				printBoard(board);
				break;
			case "move up":
				try {
					board = moveUp(board);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Illegal Move, Board Not Changed.");
				}
				break;
			case "move down":
				try {
					board = moveDown(board);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Illegal Move, Board Not Changed.");
				}
				break;
			case "move left":
				try {
					board = moveLeft(board);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Illegal Move, Board Not Changed.");
				}
				break;
			case "move right":
				try {
					board = moveRight(board);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Illegal Move, Board Not Changed.");
				}
				break;
			case "experiments":
				experiments();
				break;
			//other cases as directed in instructions.
			default:
				//Case setState xxx xxx xxx
				if (command.length() == 20 && command.substring(0, 8).equals("setState")) {
					String state = command.substring(9);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < 11; i++) {
						char c = state.charAt(i);
						if (c != ' ')
							sb.append(state.charAt(i));
					}
					state = sb.toString(); //We have transformed the ugly xxx xxx xxx into a nice xxxxxxxxx
					zpos = state.indexOf('b');
					for (int i = 0; i < 9; i++) {
						if (i == zpos)
							board[i] = 0;
						else
							board[i] = Integer.parseInt(state.substring(i, i+1));
					}
				}
				//Case randomizeState n
				else if (command.length() >= 16 && command.substring(0, 14).equals("randomizeState")) {
					try {
						steps = Integer.parseInt(command.substring(15));
						randomize(board, steps);
					} catch (NumberFormatException e) {
						System.out.println("Command not Recognized. (Error 2)");
					}
				}
				//case solve A-star hn
				else if (command.length() == 15 && command.substring(0, 14).equals("solve A-star h")) {
					try {
						int heur = Integer.parseInt(command.substring(14));
						if (heur != 1 && heur != 2) {
							System.out.println("Valid Heuristic Types are h1 and h2");
						}
						heuristicType = heur;
						Node<int[]> goal = aStar(board, maxNodes);
						if (goal == null) {
							System.out.println("Maximum Node Count Exceeded: A* Search Failed");
						} else {
							LinkedList<String> moves = extractMoves(goal);
							int numMoves = 0;
							System.out.println("Search Successful. Goal State Achieved By:");
							while (! moves.isEmpty()) {
								System.out.print(moves.removeFirst() + ", ");
								numMoves++;
							}
							System.out.println("\nSolved in " + ((Integer) numMoves).toString() + " moves.");
						}
					} catch (NumberFormatException e) {
						System.out.println("Command not Recognized. (Error 3)");
					}
				}
				//Case maxNodes n
				else if (command.length() >= 10 && command.substring(0, 8).equals("maxNodes")) {
					try {
						maxNodes = Integer.parseInt(command.substring(9));
					} catch (NumberFormatException e) {
						System.out.println("Command not Recognized. (Error 4)");
					}
				}
				//Case solve beam k
				else if (command.length() >= 12 && command.substring(0, 10).equals("solve beam")) {
					try {
						if (localBeam(board, Integer.parseInt(command.substring(11)), maxNodes)) {
							System.out.println("Search Successful. Goal State Achieved By:");
							int numMoves = beamMoves.size();
							while (! beamMoves.isEmpty()) {
								System.out.print(beamMoves.removeFirst() + ", ");
							}
							System.out.print("\n");
							System.out.println("Solved in " + numMoves + " moves.");
						} else {
							System.out.println("Maximum Node Count Exceeded: Beam Search Failed");
						}
					} catch (NumberFormatException e) {
						System.out.println("Command not Recognized. (Error 5)");
					}
				} 
				//Case Everything Else
				else {
					if (command.length() >= 2 && command.substring(0,2).equals("//")) { //Enables comments in command files.
						break;
					}
					System.out.println("Command not recognized. (Error 1)");
				}
			}
			protoCommand = scnr.next();
			command = protoCommand.substring(0, protoCommand.length() - 1);
		}
	}
	
	/**
	 * Method to print the board state to the console.
	 * @param board The current board state.
	 */
	public void printBoard(int[] board) {
		for (int i = 0; i < 9; i++) {
			System.out.print(board[i]);
			if (i == 2 || i == 5 || i == 8)
				System.out.print('\n');
			else
				System.out.print(", ");
		}
	}

	
	/**
	 * Method to randomize the board via making a number of random legal moves.
	 * @param board The board being randomized.
	 * @param steps The number of random steps to take.
	 * @return
	 */
	public int[] randomize(int[] board, int steps) {
		int choice;
		int i = 0;
		while (i < steps) {
			choice = (int) Math.floor(4 * Math.random());
			if (choice == 0) {
				try {
					board = moveUp(board);
					i++;
				} catch (ArrayIndexOutOfBoundsException e) {}
			} else if (choice == 1) {
				try {
					board = moveLeft(board);
					i++;
				} catch (ArrayIndexOutOfBoundsException e) {}
			} else if (choice == 2) {
				try {
					board = moveRight(board);
					i++;
				} catch (ArrayIndexOutOfBoundsException e) {}
			} else if (choice == 3) {
				try {
					board = moveDown(board);
					i++;
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
		return board;
	}
	
	/**
	 * Method to calculate the heuristic of a board state.
	 * @param state The board for which the heuristic is desired.
	 * @param heuristicType Determines which heuristic is being calculated.
	 * @return
	 */
	public int calculateHeuristic(int[] state, int heuristicType) {
		int heur = 0;
		if (heuristicType == 1) {
			for (int i = 0; i < 9; i++) {
				if (state[i] != i)
					heur++;
			}
			return heur;
		} else if (heuristicType == 2) {
			int pos;
			for (int j = 0; j < 9; j++) {
				pos = find(j, state);
				if (pos > j) {
					while (pos != j) {
						if (pos - j >= 3) {
							pos = pos - 3;
							heur++;
						} else {
							heur = heur + pos - j;
							pos = j;
						}
					}
				} else if (j > pos) {
					while (pos != j) {
						if (j - pos >= 3) {
							pos = pos + 3;
							heur++;
						} else {
							heur = heur + j - pos;
							pos = j;
						}
					}
				}
			}
			return heur;
		} else {
			return -1;
		}
	}
	
	/**
	 * Method to find the first instance particular value in an int[].
	 * Used above to find zero positions prior to calling movement methods.
	 * @param val The value to be found
	 * @param board The int[] to be searched
	 * @return The index of the first instance of val in board, or -1 if val is not in board.
	 */
	public int find(int val, int[] board) {
		int i = 0;
		while (i < board.length && board[i] != val) {
			i++;
		}
		if (i >= board.length) {
			return -1;
		} else {
			return i;
		}
	}
	
	/**
	 * Movement method for up-shift of zero
	 * @param board Current board state.
	 * @return New board state.
	 */
	public int[] moveUp(int[] board) {
		int zpos = find(0, board);
		return shift(zpos, zpos - 3, board);
	}
	
	/**
	 * Movement method for left-shift of zero
	 * @param board Current board state.
	 * @return New board state.
	 */
	public int[] moveLeft(int[] board) {
		int zpos = find(0, board);
		if (zpos != 3 && zpos != 6) {
			return shift(zpos, zpos - 1, board);
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	/**
	 * Movement method for down-shift of zero
	 * @param board Current board state.
	 * @return New board state.
	 */
	public int[] moveDown(int[] board) {
		int zpos = find(0, board);
		return shift(zpos, zpos + 3, board);
	}
	
	/**
	 * Movement method for right-shift of zero
	 * @param board Current board state.
	 * @return New board state.
	 */
	public int[] moveRight(int[] board) {
		int zpos = find(0, board);
		if (zpos != 2 && zpos != 5) {
			return shift(zpos, zpos + 1, board);
		}
		else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	/**
	 * Helper method called by movement methods.
	 * @param zpos Zero Position
	 * @param tpos Shift Target Position
	 * @param board Current board state.
	 */
	public int[] shift(int zpos, int tpos, int[] board) {
		board[zpos] = board[tpos];
		board[tpos] = 0;
		return board;
	}
	
	public Node<int[]> aStar(int[] board, int maxNodes) {
		PriorityQueue<Node<int[]>> pq = new PriorityQueue<Node<int[]>>(100);
		Node<int[]> root = new Node<int[]>(board);
		root.setHeuristic(calculateHeuristic(root.getCargo(), heuristicType)); //root depth is zero
		pq.add(root);
		return starLoop(pq, maxNodes, 1);
	}
	
	private Node<int[]> starLoop(PriorityQueue<Node<int[]>> pq, int maxNodes, int currentNodes) {
		HashSet<Node<int[]>> hash = new HashSet<Node<int[]>>();
		Node<int[]> expandable;
		Node<int[]> first;
		Node<int[]> second;
		Node<int[]> third;
		Node<int[]> fourth;
		int[] boardPrime = new int[9];
		int[] board;
		while (currentNodes < maxNodes) {
			expandable = pq.poll();
			if (expandable == null) {
				System.out.println("Unexpected Behavior, see source line ~403");
				return null; //Should be impossible to ever trigger this.
			}
			if (expandable.getHeuristic() == expandable.getDepth()) { //i.e., h(n) = 0
				return expandable; //Search success
			} //Otherwise the search must continue. The lines below are effectively one large loop variable iterator
			if (! hash.contains(expandable)) {
				hash.add(expandable);
				currentNodes++;
				first = new Node<int[]>(null);
				second = new Node<int[]>(null);
				third = new Node<int[]>(null);
				fourth = new Node<int[]>(null);
				for (int i = 0; i < 9; i++) {
					boardPrime[i] = expandable.getCargo()[i];
				}
				int newDepth = expandable.getDepth() + 1;
				try {
					board = boardPrime.clone();
					first.setCargo(moveUp(board));
					if (! hash.contains(first)) {
						first.setMove("Up");
						first.setParent(expandable);
						first.setDepth(newDepth);
						first.setHeuristic(newDepth + calculateHeuristic(first.getCargo(), heuristicType));
						pq.add(first);
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					second.setCargo(moveDown(board));
					if (! hash.contains(second)) {
						second.setMove("Down");
						second.setParent(expandable);
						second.setDepth(newDepth);
						second.setHeuristic(newDepth + calculateHeuristic(second.getCargo(), heuristicType));
						pq.add(second);
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					third.setCargo(moveLeft(board));
					if (! hash.contains(third)) {
						third.setMove("Left");
						third.setParent(expandable);
						third.setDepth(newDepth);
						third.setHeuristic(newDepth + calculateHeuristic(third.getCargo(), heuristicType));
						pq.add(third);
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					fourth.setCargo((moveRight(board)));
					if (! hash.contains(fourth)) {
						fourth.setMove("Right");
						fourth.setParent(expandable);
						fourth.setDepth(newDepth);
						fourth.setHeuristic(newDepth + calculateHeuristic(fourth.getCargo(), heuristicType));
						pq.add(fourth);
					}
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
		return null;
	}
	
	public boolean localBeam(int[] state, int numStates, int maxNodes) {
		int[] goal = {0,1,2,3,4,5,6,7,8};
		if (Arrays.equals(state, goal)) {
			return true; //Check on being fed the goal state
		}
		PriorityQueue<Node<int[]>> stateMemory = new PriorityQueue<Node<int[]>>(numStates);
		stateMemory.add(new Node<int[]>(state));
		return beamLoop(stateMemory, numStates, maxNodes, 0);
	}
	
	private boolean beamLoop(PriorityQueue<Node<int[]>> stateMemory, int numStates, int maxNodes, int currentNodes) {
		PriorityQueue<Node<int[]>> stateCollector = new PriorityQueue<Node<int[]>>(4*numStates);
		Node<int[]> expandable;
		Node<int[]> first;
		Node<int[]> second;
		Node<int[]> third;
		Node<int[]> fourth;
		int[] boardPrime = new int[9];
		int[] board;
		while (currentNodes < maxNodes) {
			while (stateMemory.peek() != null) {
				currentNodes++;
				expandable = stateMemory.poll();
				boardPrime = expandable.getCargo().clone();
				first = new Node<int[]>(null);
				second = new Node<int[]>(null);
				third = new Node<int[]>(null);
				fourth = new Node<int[]>(null);
				//try-catch blocks generate children of the nodes being expanded, when they exist
				try {
					board = boardPrime.clone();
					first.setCargo(moveUp(board));
					first.setMove("Up");
					first.setParent(expandable);
					first.setHeuristic(calculateHeuristic(first.getCargo(), 2));
					if (first.getHeuristic() == 0) {
						beamMoves = extractMoves(first);
						return true;
					}
					stateCollector.add(first);
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					second.setCargo(moveDown(board));
					second.setMove("Down");
					second.setParent(expandable);
					second.setHeuristic(calculateHeuristic(second.getCargo(), 2));
					if (second.getHeuristic() == 0) {
						beamMoves = extractMoves(second);
						return true;
					}
					stateCollector.add(second);
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					third.setCargo(moveLeft(board));
					third.setMove("Left");
					third.setParent(expandable);
					third.setHeuristic(calculateHeuristic(third.getCargo(), 2));
					if (third.getHeuristic() == 0) {
						beamMoves = extractMoves(third);
						return true;
					}
					stateCollector.add(third);
				} catch (ArrayIndexOutOfBoundsException e) {}
				try {
					board = boardPrime.clone();
					fourth.setCargo(moveRight(board));
					fourth.setMove("Right");
					fourth.setParent(expandable);
					fourth.setHeuristic(calculateHeuristic(fourth.getCargo(), 2));
					if (fourth.getHeuristic() == 0) {
						beamMoves = extractMoves(fourth);
						return true;
					}
					stateCollector.add(fourth);
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			for (int k = 0; stateCollector.peek() != null && k < numStates; k++) {
				stateMemory.add(stateCollector.poll());
			}
			stateCollector.clear();
		}
		return false;//Failure case of exceeding the node limit
	}
	
	private LinkedList<String> extractMoves(Node<int[]> node) {
		LinkedList<String> moves = new LinkedList<String>();
		while (node != null && node.getParent() != null) {
			moves.addFirst(node.getMove());
			node = node.getParent();
		}
		return moves;
	}
	
	public void experiments() {
		//3a,b,c,d
		double star1Solvable = 0;
		double star1Length = 0;
		double star2Solvable = 0;
		double star2Length = 0;
		double beamSolvable = 0;
		double beamLength = 0;
		int[] blank = {0,1,2,3,4,5,6,7,8};
		int[] board;
		Node<int[]> goal;
		Node<int[]> goal2;
		int maxNodes = 1000;
		while (maxNodes < 181440) {
			star1Solvable = 0;
			star1Length = 0;
			star2Solvable = 0;
			star2Length = 0;
			beamSolvable = 0;
			beamLength = 0;
			for (int i = 0; i < 1000; i++) {
				board = randomize(blank.clone(), 200);
				heuristicType = 1;
				goal = aStar(board, maxNodes);
				if (goal != null) {
					star1Solvable += 0.1;
					star1Length += ((double) extractMoves(goal).size());
				}
				heuristicType = 2;
				goal2 = aStar(board, maxNodes);
				if (goal2 != null) {
					star2Solvable += 0.1;
					star2Length += ((double) extractMoves(goal2).size());
				}
				if (localBeam(board, 400, maxNodes)) {
					beamSolvable += 0.1;
					beamLength += (double) beamMoves.size();
				}
			}
			star1Length /= (10.0 * star1Solvable);
			star2Length /= (10.0 * star2Solvable);
			beamLength /= (10.0 * beamSolvable);
			star1Solvable = Math.round(100.0 * star1Solvable) / 100.0;
			star2Solvable = Math.round(100.0 * star2Solvable) / 100.0;
			beamSolvable = Math.round(100.0 * beamSolvable) / 100.0;
			star1Length = Math.round(100.0 * star1Length) / 100.0;
			star2Length = Math.round(100.0 * star2Length) / 100.0;
			beamLength = Math.round(100.0 * beamLength) / 100.0;
			System.out.println("maxNodes = " + maxNodes + ": A-star h1 solves " + star1Solvable +
					"%, A-star h2 solves " + star2Solvable + "%, beam with k=400 solves " + beamSolvable + "%");
			System.out.println("Average Solution Lengths: A-star h1 - " + star1Length + ", A-star h2: " + star2Length +
					", beam: " + beamLength);
			System.out.println();
			
			if (maxNodes < 6000) {maxNodes += 1000;} else if (maxNodes < 60000) {maxNodes += 5000;} else {maxNodes +=30000;}
		}
	}
}
