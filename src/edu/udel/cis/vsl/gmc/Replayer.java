package edu.udel.cis.vsl.gmc;

import java.io.File;
import java.io.PrintStream;

/**
 * A Replayer is used to replay an execution trace of a transition system. The
 * trace is typically stored in a file created by method
 * {@link DfsSearcher#writeStack(File)}.
 * 
 * @author siegel
 * 
 * @param <STATE>
 *            the type for the states in the transition system
 * @param <TRANSITION>
 *            the type for the transitions in the transition system
 * @param <TRANSITIONSEQUENCE>
 *            the type for a sequence of transitions emanating from a single
 *            state
 */
public class Replayer<STATE, TRANSITION> {

	// Instance fields...

	/**
	 * The state manager: the object used to determine the next state given a
	 * state and a transition.
	 */
	private StateManagerIF<STATE, TRANSITION> manager;

	/**
	 * The stream to which the human-readable output should be sent when
	 * replayin a trace.
	 */
	private PrintStream out;

	/**
	 * Print the states at each step in the trace? If this is false, only the
	 * initial and the final states will be printed.
	 */
	private boolean printAllStates = true;

	// Constructors...

	/**
	 * 
	 * @param enabler
	 *            enabler used to determine the set of enabled transitions at a
	 *            given state
	 * @param manager
	 *            state manager; used to compute the next state given a state
	 *            and transition
	 * @param out
	 *            stream to which the trace should be written in human-readable
	 *            form
	 */
	public Replayer(StateManagerIF<STATE, TRANSITION> manager, PrintStream out) {
		this.manager = manager;
		this.out = out;
	}

	// Static methods....

	// Instance methods: helpers...

	/**
	 * Prints out those states which should be printed. A utility method used by
	 * play method.
	 * 
	 * @param step
	 *            the step number to use in the printout
	 * @param numStates
	 *            the number of states in the array states
	 * @param executionNames
	 *            the names to use for each state; array of length numStates
	 * @param print
	 *            which states should be printed; array of boolean of length
	 *            numStates
	 * @param states
	 *            the states; array of STATE of length numStates
	 */
	private void printStates(int step, int numStates, String[] executionNames,
			boolean[] print, STATE[] states) {
		for (int i = 0; i < numStates; i++) {
			if (print[i]) {
				// out.println("State " + step + executionNames[i] + ":");
				out.println();
				manager.printStateLong(out, states[i]);
				out.println();
			}
		}
	}

	// Instance methods: public...

	public void setPrintAllStates(boolean value) {
		this.printAllStates = value;
	}

	public boolean getPrintAllStates() {
		return printAllStates;
	}

	/**
	 * Plays the trace. This method accepts an array of initial states, and will
	 * create executions in parallel, one for each initial state. All of the
	 * executions will use the same sequence of transitions, but may start from
	 * different initial states. The common use case has two initial states, the
	 * first one a symbolic state and the second a concrete state obtained by
	 * solving the path condition.
	 * 
	 * @param states
	 *            the states from which the execution should start. The first
	 *            state in the initial state (index 0) will be the one assumed
	 *            to execute according to the guide. This method will modify
	 *            this array so that upon returning the array will hold the
	 *            final states.
	 * @param print
	 *            which states should be printed at a point when states will be
	 *            printed. Array of length states.length.
	 * @param names
	 *            the names to use for the different executions. Array of length
	 *            states.length
	 * @param guide
	 *            sequence of integers used to guide execution when a state is
	 *            reached that has more than one enabled transition. The initial
	 *            state of index 0 is the one that will work with the guide
	 * @throws MisguidedExecutionException
	 */
	public void play(STATE states[], boolean[] print, String[] names,
			TransitionChooser<STATE, TRANSITION> chooser)
			throws MisguidedExecutionException {
		int numExecutions = states.length;
		int step = 0;
		String[] executionNames = new String[numExecutions];

		for (int i = 0; i < numExecutions; i++) {
			String name = names[i];

			if (name == null)
				executionNames[i] = "";
			else
				executionNames[i] = " (" + names + ")";
		}
		printStates(step, numExecutions, executionNames, print, states);
		while (true) {
			TRANSITION transition = chooser.chooseEnabledTransition(states[0]);

			if (transition == null)
				break;
			step++;
			out.print("Step " + step + ": ");
			manager.printTransitionLong(out, transition);
			out.println();
			for (int i = 0; i < numExecutions; i++)
				states[i] = manager.nextState(states[i], transition);
			// TODO: question: can the same transition be re-used?
			// this is not specified in the contract and in some cases
			// info is cached in the transition. Maybe duplicate the
			// transition, or clear it???
			if (printAllStates)
				printStates(step, numExecutions, executionNames, print, states);
		}
		// always print the last state:
		if (!printAllStates)
			printStates(step, numExecutions, executionNames, print, states);
		out.println("Trace ends after " + step + " steps.");
	}

	public void play(STATE initialState,
			TransitionChooser<STATE, TRANSITION> chooser)
			throws MisguidedExecutionException {
		@SuppressWarnings("unchecked")
		STATE[] stateArray = (STATE[]) new Object[] { initialState };
		boolean[] printArray = new boolean[] { true };
		String[] names = new String[] { null };

		play(stateArray, printArray, names, chooser);
	}

	public void play(STATE initialSymbolicState, STATE initialConcreteState,
			boolean printSymbolicStates,
			TransitionChooser<STATE, TRANSITION> chooser)
			throws MisguidedExecutionException {
		@SuppressWarnings("unchecked")
		STATE[] stateArray = (STATE[]) new Object[] { initialSymbolicState,
				initialConcreteState };
		boolean[] printArray = new boolean[] { printSymbolicStates, true };
		String[] names = new String[] { "Symbolic", "Concrete" };

		play(stateArray, printArray, names, chooser);
	}

}
