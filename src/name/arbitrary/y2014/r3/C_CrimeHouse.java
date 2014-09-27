package name.arbitrary.y2014.r3;

import com.google.common.collect.Maps;
import name.arbitrary.CodeJamBase;

import java.util.*;

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

        return new Run(movements.toArray(new Movement[movements.size()])).go();
    }

    private static class Run {
        private final Movement[] movements;
        private final int[] nextUses;

        private Map<Integer, Integer> nextUseMap = Maps.newHashMap();

        public Run(Movement[] movements) {
            this.movements = movements;
            this.nextUses = nextUsages(movements);
        }

        // Run through possible statuses we can be in.
        private String go() {
            Set<State> states = new HashSet<State>();
            states.add(new State());

            for (int i = 0; i < movements.length; i++) {
                Movement movement = movements[i];
                if (movement.identifier != 0) {
                    nextUseMap.put(movement.identifier, nextUses[i]);
                }

                states = doMovement(states, i);

                states = eliminateDominated(states);

                // System.err.println(states);
                System.err.println(i + " (" + states.size() + ")");
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

        // '/' represents entry, '\' exit (looks like a graph of number of people in).
        //
        // We want to convert the graph of unknown entry/exits from \.../ to ... , to pull up the low point in the count
        // of people in the house (and thus decrease the occupants at the end, compared to the baseline).
        //
        // Just going from \... to ... is also possibly useful, as it removes a known person from the house at the end.
        //
        // We never want to convert /...\ to ..., since that can make the final occupancy count worse.
        private Set<State> doMovement(Set<State> states, int i) {
            Movement movement = movements[i];
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

                        // Choose someone who's out and needs to go out again, if there is one.
                        if (!state.toComeIn.isEmpty()) {
                            // Find earliest leaver and bring them in.
                            int nextTime = state.toComeIn.firstKey();
                            int p1 = state.toComeIn.get(nextTime);
                            state.toComeIn.remove(nextTime); // Not too worried about modifying object now, I guess.
                            sendKnownIn(p1, newStates, state);
                        } else {
                            // Only send an unknown person in if there are no knowns that must be sent in. Bringing knowns
                            // in early is fine, on the grounds that we can commute the sequence of unknown enter
                            // assignments, and an unknown enter followed by an unknown leave assigned to the same person
                            // can be thought of as just removing a transient /\ that we wouldn't otherwise touch.

                            State newState = new State(state);
                            newState.unknownsInHouse++;
                            newStates.add(newState);
                        }
                        // Never helps to assign an identity to someone entering, just for the sake of it...
                        // (cf 'last leaver' case on the leaving branch)
                    }
                } else {
                    // Leaving
                    if (movement.identifier != 0) {
                        // Known person leaving (if already out of the house, it's a CRIME TIME state).
                        if (!state.knownOutHouse.contains(movement.identifier)) {
                            sendKnownOut(movement.identifier, newStates, state);
                        }
                    } else {
                        // Unknown person leaving. Could be a new person, or one of the ones we know are in.
                        State newState = new State(state);
                        if (newState.unknownsInHouse > 0) {
                            newState.unknownsInHouse--;
                        }
                        newStates.add(newState);

                        // Find earliest enterer and send them out.
                        if (!state.toComeOut.isEmpty()) {
                            State newState2 = new State(state);
                            int nextTime = state.toComeOut.firstKey();
                            int p1 = state.toComeOut.get(nextTime);
                            newState2.toComeOut.remove(nextTime);
                            sendKnownOut(p1, newStates, newState2);
                        }

                        // Find last leaver, and send them out.
                        if (!state.spares.isEmpty()) {
                            State newState2 = new State(state);
                            int nextTime = state.spares.firstKey();
                            int p2 = state.spares.get(nextTime);
                            newState2.spares.remove(nextTime);
                            sendKnownOut(p2, newStates, newState2);
                        }
                    }
                }
            }
            return newStates;
        }

        private void sendKnownOut(int identifier, Set<State> newStates, State state) {
            State newState = new State(state);
            newState.knownOutHouse.add(identifier);

            int nextUse = nextUseMap.get(identifier);
            if (nextUse < 1000000) {
                Movement nextMovement = movements[nextUse];
                if (!nextMovement.isEnter) {
                    newState.toComeIn.put(nextUse, identifier);
                }
            }

            if (state.knownInHouse.contains(identifier)) {
                newState.knownInHouse.remove(identifier);
            } else {
                if (newState.unknownsInHouse > 0) {
                    newState.unknownsInHouse--;
                }
            }
            newStates.add(newState);
        }

        private void sendKnownIn(int identifier, Set<State> newStates, State state) {
            State newState = new State(state);

            newState.knownInHouse.add(identifier);

            int nextUse = nextUseMap.get(identifier);
            if (nextUse < 1000000) {
                Movement nextMovement = movements[nextUse];
                if (nextMovement.isEnter) {
                    newState.toComeOut.put(nextUse, identifier);
                } else {
                    newState.spares.put(-nextUse, identifier);
                }
            } else {
                newState.spares.put(-nextUse, identifier);
            }

            newState.knownOutHouse.remove(identifier);
            newStates.add(newState);
        }

        // Generate a map saying for movement n, what the next usage of that identifier is.
        // >= 1000000 means never used again. Use different values so that the values are
        // unique (and thus can be keys in maps, etc.)
        private static int[] nextUsages(Movement[] movements) {
            int[] result = new int[movements.length];
            for (int i = 0; i < movements.length; i++) {
                result[i] = 1000000 + i;
            }

            Map<Integer, Integer> lastUsages = new HashMap<Integer, Integer>();
            for (int i = 0; i < movements.length; i++) {
                Movement movement = movements[i];
                if (movement.identifier != 0) {
                    Integer lastUsage = lastUsages.get(movement.identifier);
                    if (lastUsage != null) {
                        result[lastUsage] = i;
                    }
                    lastUsages.put(movement.identifier, i);
                }
            }

            // System.err.print("Next uses: ");
            // for (int item : result) {
            //     System.err.print(item + " ");
            // }
            // System.err.println("");

            return result;
        }

        private Set<State> eliminateDominated(Set<State> states) {
            Set<State> result = new HashSet<State>();
            for (State candidate : states) {
                Iterator<State> iterator = result.iterator();
                boolean isCandidateDominated = false;
                while (iterator.hasNext()) {
                    State existing = iterator.next();
                    int comparison = checkDomination(existing, candidate);
                    if (comparison < 0) {
                        iterator.remove();
                    } else if (comparison > 0) {
                        isCandidateDominated = true;
                        break;
                    }
                }
                if (!isCandidateDominated) {
                    result.add(candidate);
                }
            }
            return result;
        }

        // Simple domination check we were doing before...
        private int checkDomination(State existing, State candidate) {
            // Sending out people who don't come back in again can improve final state, but not be registered
            // otherwise...
            if (existing.knownOutHouse.size() != candidate.knownOutHouse.size()) {
                return 0;
            }

            if (existing.toComeOut.size() != candidate.toComeOut.size()) {
                return 0;
            }
            if (existing.toComeIn.size() != candidate.toComeIn.size()) {
                return 0;
            }

            int test1 = checkDominationSet(existing.toComeIn.keySet(), candidate.toComeIn.keySet());
            int test2 = checkDominationSet(existing.toComeOut.keySet(), candidate.toComeOut.keySet());
            int test3 = candidate.unknownsInHouse - existing.unknownsInHouse;

            if (test1 <= 0 && test2 <= 0 && test3 <= 0 && (test1 < 0 || test2 < 0 || test3 < 0)) {
                return -1;
            } else if (test1 >= 0 && test2 >= 0 && test3 >= 0 && (test1 > 0 || test2 > 0 || test3 > 0)) {
                return 1;
            } else {
                return 0;
            }

            /*
            if (!existing.toComeIn.equals(candidate.toComeIn)) return 0;
            if (!existing.toComeOut.equals(candidate.toComeOut)) return 0;

            return candidate.unknownsInHouse - existing.unknownsInHouse;
            */
        }

        private int checkDominationSet(Set<Integer> a, Set<Integer> b) {
            boolean aNotWorse = true;
            boolean bNotWorse = true;
            Iterator<Integer> iterA = a.iterator();
            Iterator<Integer> iterB = b.iterator();
            while (iterA.hasNext()) {
                Integer nextA = iterA.next();
                Integer nextB = iterB.next();

                if (nextA < nextB) {
                    aNotWorse = false;
                } else if (nextA > nextB) {
                    bNotWorse = false;
                }
            }

            if (aNotWorse && !bNotWorse) {
                return 1;
            } else if (bNotWorse && !aNotWorse) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    static class State {
        public int unknownsInHouse;
        public Set<Integer> knownInHouse;
        public Set<Integer> knownOutHouse;
        // Subset of knownOutHouse that must be brought in, keyed on entry time.
        public SortedMap<Integer, Integer> toComeIn;
        // Symmetrically for knownInHouse
        public SortedMap<Integer, Integer> toComeOut;
        // Those that are known in, and will go out later, but are 'loanable' as an identity for unknowns
        // (reducing the unknowns in the house at the end). Keyed on minus the time to reverse sort.
        public SortedMap<Integer, Integer> spares;

        State() {
            this.unknownsInHouse = 0;
            this.knownInHouse = new HashSet<Integer>();
            this.knownOutHouse = new HashSet<Integer>();
            this.toComeIn = new TreeMap<Integer, Integer>();
            this.toComeOut = new TreeMap<Integer, Integer>();
            this.spares = new TreeMap<Integer, Integer>();
        }

        State(State that) {
            this.unknownsInHouse = that.unknownsInHouse;
            this.knownInHouse = new HashSet<Integer>(that.knownInHouse);
            this.knownOutHouse = new HashSet<Integer>(that.knownOutHouse);
            this.toComeIn = new TreeMap<Integer, Integer>(that.toComeIn);
            this.toComeOut = new TreeMap<Integer, Integer>(that.toComeOut);
            this.spares = new TreeMap<Integer, Integer>(that.spares);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (unknownsInHouse != state.unknownsInHouse) return false;
            if (!knownInHouse.equals(state.knownInHouse)) return false;
            if (!knownOutHouse.equals(state.knownOutHouse)) return false;
            if (!toComeIn.equals(state.toComeIn)) return false;
            if (!toComeOut.equals(state.toComeOut)) return false;
            if (!spares.equals(state.spares)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = unknownsInHouse;
            result = 31 * result + knownInHouse.hashCode();
            result = 31 * result + knownOutHouse.hashCode();
            result = 31 * result + toComeIn.hashCode();
            result = 31 * result + toComeOut.hashCode();
            result = 31 * result + spares.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "State{" +
                    "unknownsInHouse=" + unknownsInHouse +
                    ", knownInHouse=" + knownInHouse +
                    ", knownOutHouse=" + knownOutHouse +
                    ", toComeIn=" + toComeIn +
                    ", toComeOut=" + toComeOut +
                    ", spares=" + spares +
                    '}';
        }
    }
}
