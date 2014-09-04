package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.*;

// 20:23 - 21:20 - Solve small case
// - 22:09 - Working on large case
public class C_CrimeHouse extends CodeJamBase {
    C_CrimeHouse(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_CrimeHouse(args[0]).run();
    }

    class Movement {
        public final boolean isEnter;
        public int identifier;
        public int returnTime = -1;

        Movement(boolean isEnter, int identifier) {
            this.isEnter = isEnter;
            this.identifier = identifier;
        }

        public String toString() {
            return (isEnter ? "E " : "L ") + identifier + "(" + returnTime + ")";
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

        // return simpleRun(movements);

        return fastRun(movements);
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
        public int unknownsInHouse;
        public Set<Integer> knownInHouse;
        public Set<Integer> knownOutHouse;

        State() {
            this.unknownsInHouse = 0;
            this.knownInHouse = new HashSet<Integer>();
            this.knownOutHouse = new HashSet<Integer>();
        }

        State(State that) {
            this.unknownsInHouse = that.unknownsInHouse;
            this.knownInHouse = new HashSet<Integer>(that.knownInHouse);
            this.knownOutHouse = new HashSet<Integer>(that.knownOutHouse);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (unknownsInHouse != state.unknownsInHouse) return false;
            if (!knownInHouse.equals(state.knownInHouse)) return false;
            if (!knownOutHouse.equals(state.knownOutHouse)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = unknownsInHouse;
            result = 31 * result + knownInHouse.hashCode();
            result = 31 * result + knownOutHouse.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "State{" +
                    "unknownsInHouse=" + unknownsInHouse +
                    ", knownInHouse=" + knownInHouse +
                    ", knownOutHouse=" + knownOutHouse +
                    '}';
        }
    }

    private String fastRun(List<Movement> movements) {
        annotateWithReturnTimes(movements);

        if (assignExitsToKnownPeople(movements)) {
            return "CRIME TIME";
        }
        if (assignEntriesToKnownPeople(movements)) {
            return "CRIME TIME";
        }


        // TODO: At this point, I think we've identified all the CRIME TIME cases,
        // but a greedy algorithm is clearly not good enough to optimise for minimum
        // unknown occupants at the end of day.

        // Collect the set of known people in the house at the end of the day...
        Set<Integer> knownIn = new HashSet<Integer>();
        for (Movement movement : movements) {
            if (movement.identifier != 0) {
                if (movement.isEnter) {
                    knownIn.add(movement.identifier);
                } else {
                    knownIn.remove(movement.identifier);
                }
            }
        }

        System.err.print(knownIn);

        // Assuming the house is empty at the end of the day, track the number of occupants, and find the minimum
        // number. This is the negation of the minimum number of occupants at the end of the day.
        int occupants = 0;
        int minOccupants = 0;
        for (int i = movements.size() - 1; i >= 0; i--) {
            Movement movement = movements.get(i);
            if (movement.identifier == 0) {
                occupants += movement.isEnter ? -1 : 1;
                minOccupants = Math.min(minOccupants, occupants);
            }
        }

        System.err.print(minOccupants);

        return "" + (knownIn.size() - minOccupants) ;
    }

    // Mark the movements that require an unknown in/out to make it work with when that person must
    // have returned by.
    private void annotateWithReturnTimes(List<Movement> movements) {
        // Annotate the movements with return times.
        int n = movements.size();
        Map<Integer, Integer> movedIn = new HashMap<Integer, Integer>();
        Map<Integer, Integer> movedOut = new HashMap<Integer, Integer>();
        for (int i = 0; i < n; i++) {
            Movement movement = movements.get(i);
            if (movement.identifier != 0) {
                if (movement.isEnter) {
                    // For isEnter, return time goes on first time...
                    if (movedIn.containsKey(movement.identifier)) {
                        movements.get(movedIn.get(movement.identifier)).returnTime = i;
                    }
                    movedIn.put(movement.identifier, i);
                    movedOut.remove(movement.identifier);
                } else {
                    // For exit, return time goes on second time.
                    if (movedOut.containsKey(movement.identifier)) {
                        movements.get(i).returnTime = movedOut.get(movement.identifier);
                    }
                    movedOut.put(movement.identifier, i);
                    movedIn.remove(movement.identifier);
                }
            }
        }

        System.err.println(movements);
    }

    // Now, assign movements outward to known people as early as possible - the unknown people leave late,
    // minimising the number that must be left.
    private boolean assignExitsToKnownPeople(List<Movement> movements) {
        SortedMap<Integer, Integer> toBringOut = new TreeMap<Integer, Integer>();
        int n = movements.size();
        for (int i = 0; i < n; i++) {
            if (!toBringOut.isEmpty() && toBringOut.firstKey() <= i) {
                System.err.println("Missed deadline");
                return true;
            }

            Movement movement = movements.get(i);
            int returnTime = movement.returnTime;
            int identifier = movement.identifier;
            if (movement.isEnter) {
                if (returnTime >= 0) {
                    toBringOut.put(returnTime, identifier);
                }
            } else {
                if (identifier == 0) {
                    if (!toBringOut.isEmpty()) {
                        int key = toBringOut.firstKey();
                        movement.identifier = toBringOut.get(key);
                        toBringOut.remove(key);
                    }
                }
            }
        }
        return false;
    }

    // Assign entries to known people as late as possible, so all the unknowns enter early and can leave.
    private boolean assignEntriesToKnownPeople(List<Movement> movements) {
        SortedMap<Integer, Integer> toBringIn = new TreeMap<Integer, Integer>();
        int n = movements.size();
        for (int i = n - 1; i >= 0; i--) {
            if (!toBringIn.isEmpty() && toBringIn.lastKey() >= i) {
                System.err.println("Missed deadline");
                return true;
            }

            Movement movement = movements.get(i);
            int returnTime = movement.returnTime;
            int identifier = movement.identifier;
            if (!movement.isEnter) {
                if (returnTime >= 0) {
                    toBringIn.put(returnTime, identifier);
                }
            } else {
                if (identifier == 0) {
                    if (!toBringIn.isEmpty()) {
                        int key = toBringIn.lastKey();
                        movement.identifier = toBringIn.get(key);
                        toBringIn.remove(key);
                    }
                }
            }
        }
        return false;
    }
}
