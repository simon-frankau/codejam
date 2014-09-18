package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.*;

import static com.google.common.base.MoreObjects.firstNonNull;

// To calculate the final score, we run through the timesteps, arbitrarily starting with 0 unknown occupants in the
// house (we allow negative counts). The final number of unknown occupants minus the minimum number of unknown
// occupants at any time is the number of unknown occupants at the end. To this we add the number of known occupants
// (known ids whose last movement was entering), to get the final number of occupants.
//
// What we then have to do is assign identities to some of the unknown transitions such that:
// * There are no enter-then-enter/leave-then-leaves by known people (if this cannot be done, CRIME TIME)
// * We minimise the final occupant count
//
// The simplest solution is to brute force all the possible enterings and leavings, but we can eliminate some moves:
// * If bringing a known person in, to avoid a double-leave, choose the earliest deadline first. The other person
//   will have to come in at some time, and either it's too late for them the other way around, or switching the two
//   around makes no difference.
// * Ditto for double-entering.
// * If bringing in a known person in order to reduce the number of unknown people entering, bring in the person
//   with the longest deadline - similar reasoning.
//
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

        return simpleRun(movements);
    }

    // Run through possible statuses we can be in.
    private String simpleRun(List<Movement> movements) {
        Map<State, Integer> states = new HashMap<State, Integer>();
        states.put(new State(), 0);

        int i = 0;
        for (Movement movement : movements) {
            states = doMovement(states, movements, i);
            // System.err.println(states);
            System.err.println(i++ + " (" + states.size() + ")");
        }

        if (states.isEmpty()) {
            return "CRIME TIME";
        }

        int minNum = Integer.MAX_VALUE;
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            int thisCase = entry.getValue() + entry.getKey().knownInHouse.size();
            if (thisCase < minNum) {
                minNum = thisCase;
            }
        }
        return "" + minNum;
    }

    private Map<State, Integer> doMovement(Map<State, Integer> states, List<Movement> movements, int i) {
        Movement movement = movements.get(i);
        Map<State, Integer> newStates = new HashMap<State, Integer>();
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            State state = entry.getKey();
            int unknownsInHouse = entry.getValue();
            if (movement.isEnter) {
                // Entering
                if (movement.identifier != 0) {
                    // Known person coming in.
                    if (!state.knownInHouse.contains(movement.identifier)) {
                        sendKnownIn(movement.identifier, newStates, state, unknownsInHouse);
                    }
                } else {
                    // Unknown person entering. Could be a new person, or one of the ones we know are out.

                    // Choose someone who's out and needs to go out again, if there is one.
                    int p1 = earliestLeaver(state.knownOutHouse, movements, i);
                    if (p1 != 0) {
                        sendKnownIn(p1, newStates, state, unknownsInHouse);
                    } else {
                        // Only send an unknown person in if there are no knowns that must be sent in. We want the
                        // unknown entries as late as possible.
                        State newState = new State(state);
                        unknownsInHouse++;
                        if (firstNonNull(newStates.get(newState), Integer.MAX_VALUE) > unknownsInHouse) {
                            newStates.put(newState, unknownsInHouse);
                        }
                    }
/* Never helps to assign an identity to someone entering, just for the sake of it...
                    int p2 = lastEnterer(state.knownOutHouse, movements, i);
                    if (p2 != 0) {
                        sendKnownIn(p2, newStates, state);
                    }
 */               }
            } else {
                // Leaving
                if (movement.identifier != 0) {
                    // Known person leaving
                    if (!state.knownOutHouse.contains(movement.identifier)) {
                        sendKnownOut(movement.identifier, newStates, state, unknownsInHouse);
                    }
                } else {
                    // Unknown person leaving. Could be a new person, or one of the ones we know are in.
                    State newState = new State(state);
                    int newUnknownsInHouse = unknownsInHouse;
                    if (newUnknownsInHouse > 0) {
                        newUnknownsInHouse--;
                    }
                    if (firstNonNull(newStates.get(newState), Integer.MAX_VALUE) > newUnknownsInHouse) {
                        newStates.put(newState, newUnknownsInHouse);
                    }

                    int p1 = earliestEnterer(state.knownInHouse, movements, i);
                    if (p1 != 0) {
                        sendKnownOut(p1, newStates, state, unknownsInHouse);
                    }

                    int p2 = lastLeaver(state.knownInHouse, movements, i);
                    if (p2 != 0) {
                        sendKnownOut(p2, newStates, state, unknownsInHouse);
                    }
                }
            }
        }
        return newStates;
    }

    private int earliestLeaver(Set<Integer> knownOutHouse, List<Movement> movements, int i) {
        Set<Integer> candidates = new HashSet<Integer>(knownOutHouse);
        while (++i < movements.size()) {
            Movement movement = movements.get(i);
            if (movement.identifier != 0) {
                if (movement.isEnter) {
                    candidates.remove(movement.identifier);
                } else {
                    if (candidates.contains(movement.identifier)) {
                        return movement.identifier;
                    }
                }
            }
        }
        return 0;
    }

    private int earliestEnterer(Set<Integer> knownInHouse, List<Movement> movements, int i) {
        Set<Integer> candidates = new HashSet<Integer>(knownInHouse);
        while (++i < movements.size()) {
            Movement movement = movements.get(i);
            if (movement.identifier != 0) {
                if (!movement.isEnter) {
                    candidates.remove(movement.identifier);
                } else {
                    if (candidates.contains(movement.identifier)) {
                        return movement.identifier;
                    }
                }
            }
        }
        return 0;
    }

    private int lastLeaver(Set<Integer> knownInHouse, List<Movement> movements, int i) {
        Set<Integer> candidates = new HashSet<Integer>(knownInHouse);
        int candidate = 0;
        while (++i < movements.size()) {
            Movement movement = movements.get(i);
            if (movement.identifier != 0) {
                if (!movement.isEnter && candidates.contains(movement.identifier)) {
                    candidate = movement.identifier;
                }
                candidates.remove(movement.identifier);
            }
        }

        // Never leaves counts as a last leaver.
        if (!candidates.isEmpty()) {
            return candidates.iterator().next();
        }

        return candidate;
    }

    private void sendKnownOut(int identifier, Map<State, Integer> newStates, State state, int unknownsInHouse) {
        State newState = new State(state);
        newState.knownOutHouse.add(identifier);
        if (state.knownInHouse.contains(identifier)) {
            newState.knownInHouse.remove(identifier);
        } else {
            if (unknownsInHouse > 0) {
                unknownsInHouse--;
            }
        }
        if (firstNonNull(newStates.get(newState), Integer.MAX_VALUE) > unknownsInHouse) {
            newStates.put(newState, unknownsInHouse);
        }
    }

    private void sendKnownIn(int identifier, Map<State, Integer> newStates, State state, int unknownsInHouse) {
        State newState = new State(state);
        newState.knownOutHouse.remove(identifier);
        newState.knownInHouse.add(identifier);
        if (firstNonNull(newStates.get(newState), Integer.MAX_VALUE) > unknownsInHouse) {
            newStates.put(newState, unknownsInHouse);
        }
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
}
