package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 20:23 - 21:20 - Solve small case
public class C_CrimeHouse extends CodeJamBase {
    C_CrimeHouse(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_CrimeHouse(args[0]).run();
    }

    class Movement {
        public final boolean isEnter;
        public final int identifier;

        Movement(boolean isEnter, int identifier) {
            this.isEnter = isEnter;
            this.identifier = identifier;
        }

        public String toString() {
            return (isEnter ? "E " : "L ") + identifier;
        }
    }

    @Override
    protected String runCase() {
        int n = Integer.parseInt(getLine());
        List<Movement> movements = new ArrayList<Movement>();
        for (int i = 0; i < n; i++) {
            String[] parts = getLineElements();
            movements.add(new Movement(parts[0].equals("E"), Integer.parseInt(parts[1])));
        }

        return simpleRun(movements);
    }

    // Run through possible statuses we can be in.
    private String simpleRun(List<Movement> movements) {
        Set<State> states = new HashSet<State>();
        states.add(new State());

        int i = 0;
        for (Movement movement : movements) {
            states = doMovement(states, movement);
            // System.err.println(states);
            System.err.println(i++);
        }

        if (states.isEmpty()) {
            return "CRIME TIME";
        }

        int minNum = Integer.MAX_VALUE;
        for (State state : states) {
            int thisCase = state.unknownsInHouse + state.knownInHouse.size();
            if (thisCase < minNum) {
                minNum = thisCase;
            }
        }
        return "" + minNum;
    }

    private Set<State> doMovement(Set<State> states, Movement movement) {
        Set<State> newStates = new HashSet<State>();
        for (State state : states) {
            if (movement.isEnter) {
                // Entering
                if (movement.identifier != 0) {
                    // Known person coming in.
                    if (!state.knownInHouse.contains(movement.identifier)) {
                        sendKnownIn(movement.identifier, newStates, state);
                    }
                } else {
                    // Unknown person entering. Could be a new person, or one of the ones we know are out.
                    State newState = new State(state);
                    newState.unknownsInHouse++;
                    newStates.add(newState);

                    for (int person : state.knownOutHouse) {
                        sendKnownIn(person, newStates, state);
                    }
                }
            } else {
                // Leaving
                if (movement.identifier != 0) {
                    // Known person leaving
                    if (!state.knownOutHouse.contains(movement.identifier)) {
                        sendKnownOut(movement.identifier, newStates, state);
                    }
                } else {
                    // Unknown person leaving. Could be a new person, or one of the ones we know are in.
                    State newState = new State(state);
                    if (state.unknownsInHouse > 0) {
                        newState.unknownsInHouse--;
                    } else {
                        newState.inHouseAtStartOfDay++;
                    }
                    newStates.add(newState);

                    for (int person : state.knownInHouse) {
                        sendKnownOut(person, newStates, state);
                    }
                }
            }
        }
        return newStates;
    }

    private void sendKnownOut(int identifier, Set<State> newStates, State state) {
        State newState = new State(state);
        newState.knownOutHouse.add(identifier);
        if (state.knownInHouse.contains(identifier)) {
            newState.knownInHouse.remove(identifier);
        } else {
            if (state.unknownsInHouse > 0) {
                newState.unknownsInHouse--;
            } else {
                newState.inHouseAtStartOfDay++;
            }
        }
        newStates.add(newState);
    }

    private void sendKnownIn(int identifier, Set<State> newStates, State state) {
        State newState = new State(state);
        newState.knownOutHouse.remove(identifier);
        newState.knownInHouse.add(identifier);
        newStates.add(newState);
    }

    class State {
        public int inHouseAtStartOfDay;
        public int unknownsInHouse;
        public Set<Integer> knownInHouse;
        public Set<Integer> knownOutHouse;

        State() {
            this.inHouseAtStartOfDay = 0;
            this.unknownsInHouse = 0;
            this.knownInHouse = new HashSet<Integer>();
            this.knownOutHouse = new HashSet<Integer>();
        }

        State(State that) {
            this.inHouseAtStartOfDay = that.inHouseAtStartOfDay;
            this.unknownsInHouse = that.unknownsInHouse;
            this.knownInHouse = new HashSet<Integer>(that.knownInHouse);
            this.knownOutHouse = new HashSet<Integer>(that.knownOutHouse);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (inHouseAtStartOfDay != state.inHouseAtStartOfDay) return false;
            if (unknownsInHouse != state.unknownsInHouse) return false;
            if (!knownInHouse.equals(state.knownInHouse)) return false;
            if (!knownOutHouse.equals(state.knownOutHouse)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = inHouseAtStartOfDay;
            result = 31 * result + unknownsInHouse;
            result = 31 * result + knownInHouse.hashCode();
            result = 31 * result + knownOutHouse.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "State{" +
                    "inHouseAtStartOfDay=" + inHouseAtStartOfDay +
                    ", unknownsInHouse=" + unknownsInHouse +
                    ", knownInHouse=" + knownInHouse +
                    ", knownOutHouse=" + knownOutHouse +
                    '}';
        }
    }
}
