package name.arbitrary.y2014.r3;

import com.sun.tools.javac.util.Pair;
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
public class C_CrimeHouseAgain extends CodeJamBase {
    C_CrimeHouseAgain(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_CrimeHouseAgain(args[0]).run();
    }

    class Movement {
        public final boolean isEnter;
        public int identifier;

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

        try {
            return new Run(movements.toArray(new Movement[movements.size()])).go();
        } catch (CrimeTime e) {
            return e.toString();
        }
    }

    private static class CrimeTime extends Exception {
        private final String message;

        CrimeTime() {
            this("CRIME TIME");
        }

        CrimeTime(String message) {
            this.message = message;
        }

        public String toString() {
            return message;
        }
    }

    private static class Run {
        private final Movement[] movements;

        private final Set<Integer> knownInside = new HashSet<Integer>();
        private final Set<Integer> knownOutside = new HashSet<Integer>();
        // Sorted on deadline (time next entry happens).
        private final SortedMap<Integer, Integer> toAssignExits = new TreeMap<Integer, Integer>();
        private final SortedMap<Integer, Integer> toAssignEntries = new TreeMap<Integer, Integer>();
        // When we assign an entry that means we must assign an exit, it can be undone later to make way for
        // a better assignment. The key is the deadline, and the value the assigned entry time.
        private final SortedMap<Integer, Integer> optionalToAssignExits = new TreeMap<Integer, Integer>();

        public Run(Movement[] movements) {
            this.movements = movements;
        }

        private String go() throws CrimeTime {
            assignIdentities();
            assignFinalKnownExits();
            System.err.println(Arrays.asList(movements));
            return "" + countPeopleInHouse();
        }

        private void assignIdentities() throws CrimeTime {
            // We run backwards step-by-step, assigning identities to movements where appropriate, in order to
            // minimise the number of unknown people in the house.
            for (int i = movements.length - 1; i >= 0; i--) {
                Movement movement = movements[i];
                // System.err.println("STEP: " + i);
                // System.err.println("INSIDE: " + knownInside);
                // System.err.println("OUTSIDE: " + knownOutside);
                // System.err.println("TOEXIT: " + toAssignExits);
                // System.err.println("TOENTER: " + toAssignEntries);
                // System.err.println(movement);
                int identifier = movement.identifier;
                if (identifier != 0) {
                    // Known person, just update the state and continue.
                    moveKnownPerson(i);
                } else {
                    // Unknown person, plan the assignment of identity as necessary.
                    if (movement.isEnter) {
                        // Assigning entries is good, as it stops unknowns going in.
                        if (!toAssignEntries.isEmpty()) {
                            // If we have entries that are forced because they are surrounded by a pair of exits, this is
                            // great, since we are simply pulling an enter out of the list of unknown operations, meaning
                            // that the entire #unknowns graph to the left of the current position is shifted up.
                            //
                            // It's our preferred thing to do, and dominates assigning an entry that forces us to assign
                            // an exit earlier.
                            //
                            // Nearest deadline is last deadline, as we're working backwards.
                            int nearestDeadline = toAssignEntries.lastKey();
                            movement.identifier = toAssignEntries.get(nearestDeadline);
                            toAssignEntries.remove(nearestDeadline);
                            moveKnownPerson(i);
                        } else {
                            // If there's someone known inside, we can assign the move to them, knowing we'll have to
                            // assign an exit earlier (unless they're never seen earlier, in which case, woo-hoo, we've
                            // just pulled all of the left of the graph up.
                            //
                            // We want to choose the person next referenced as exiting in the lowest movement number
                            // (this dominates the alternatives), and never referenced with a lower movement number
                            // counts as best of all.
                            Pair<Integer, Integer> candidate = furthestEnterCandidate(knownInside, i);
                            int candidateIdentifier = candidate.fst;
                            int candidateTime = candidate.snd;
                            // if (nextMovement(candidateIdentifier, i) != candidateTime) {
                            //     throw new RuntimeException("Can't happen");
                            // }
                            if (candidateIdentifier != -1) {
                                // Extra paranoia thing. (Debugging this is a nightmare...)
                                if (!canAssignExit(0, -1, i)) {
                                    throw new CrimeTime();
                                }

                                unAssignIfRequired(candidateIdentifier, candidateTime, i);

                                if (canAssignExit(candidateIdentifier, candidateTime, i)) {
                                    movement.identifier = candidateIdentifier;
                                    optionalToAssignExits.put(candidateTime, i);
                                    moveKnownPerson(i);
                                }
                            }
                        }
                    } else {
                        // Assigning exits is unfortunate, because it means we're not getting unknown people out of the house
                        // We only assign exits if it's needed because the previous and next movements of that person were
                        // entries, and it's the only way to avoid CRIME TIME.

                        // We can safely assign those exits we must assign as soon as possible (using earliest deadline first)
                        // In a sequence of consecutive unknown exits, whichever exit we choose makes no difference.
                        // In a sequence exit enter^n exit, choosing which exit to assign isn't necessary - we can assign both
                        // and get the same effect.

                        if (!toAssignExits.isEmpty()) {
                            // Nearest deadline is last deadline, as we're working backwards.
                            int nearestDeadline = toAssignExits.lastKey();
                            movement.identifier = toAssignExits.get(nearestDeadline);
                            toAssignExits.remove(nearestDeadline);
                            optionalToAssignExits.remove(nearestDeadline); // May or may not be optional, still works.
                            moveKnownPerson(i);
                        }
                    }
                }
            }
        }

        private void unAssignIfRequired(int candidateIdentifier, int candidateTime, int i) throws CrimeTime {
            if (!canAssignExit(candidateIdentifier, candidateTime, i)) {
                // Couldn't assign an exit for the current candidate
                if (!optionalToAssignExits.isEmpty()) {
                    // There's something else wanting an exit assigned...
                    int lastDeadline = optionalToAssignExits.lastKey();
                    if (lastDeadline > candidateTime) {
                        // But its deadline is sooner...
                        // So undo the assignment, clearing the space.
                        movements[optionalToAssignExits.get(lastDeadline)].identifier = 0;
                        optionalToAssignExits.remove(lastDeadline);
                        toAssignExits.remove(lastDeadline);

                        if (!canAssignExit(candidateIdentifier, candidateTime, i)) {
                            throw new RuntimeException("Shouldn't happen");
                        }
                    }
                }
            }
        }

        private boolean canAssignExit(int candidateIdentifier, int candidateTime, int i) {
            SortedMap<Integer, Integer> toAssign = new TreeMap<Integer, Integer>(toAssignExits);
            toAssign.put(candidateTime, candidateIdentifier);
            while (--i >= 0) {
                if (toAssign.isEmpty()) {
                    return true;
                }
                Movement movement = movements[i];
                if (movement.identifier == 0 && !movement.isEnter) {
                    int lastIdx = toAssign.lastKey();
                    if (lastIdx > i) {
                        // Should have been assigned by now
                        return false;
                    } else {
                        toAssign.remove(lastIdx);
                    }
                } else if (movement.identifier != 0 && movement.isEnter) {
                    int prevUse = nextMovement(movement.identifier, i);
                    if (prevUse >= 0 && movements[prevUse].isEnter) {
                        toAssign.put(prevUse, movement.identifier);
                    }
                }
            }
            if (!toAssign.isEmpty() && toAssign.lastKey() >= 0) {
                // Something remains that should have been assigned.
                return false;
            }
            return true;
        }

        // Find the person who entered the longest time ago (so we can insert an extra leave/enter pair).
        // Never entering is best of all! Also returns when they entered
        private Pair<Integer, Integer> furthestEnterCandidate(Set<Integer> knownInside, int i) {
            Set<Integer> candidates = new HashSet<Integer>(knownInside);
            int candidate = -1;
            int candidateTime = -1;
            while (--i >= 0) {
                Movement movement = movements[i];
                if (movement.identifier != 0) {
                    if (movement.isEnter) {
                        if (candidates.contains(movement.identifier)) {
                            candidates.remove(movement.identifier);
                            candidate = movement.identifier;
                            candidateTime = i;
                        } else {
                            if (candidates.contains(movement.identifier)) {
                                throw new RuntimeException("CAN'T HAPPEN?!");
                            }
                        }
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return Pair.of(candidates.iterator().next(), -1);
            }
            return Pair.of(candidate, candidateTime);
        }

        private void moveKnownPerson(int i) throws CrimeTime {
            Movement movement = movements[i];
            int identifier = movement.identifier;
            if (movement.isEnter) {
                // Enter
                if (knownOutside.contains(identifier)) {
                    // NB: Running backwards, can't be known to be outside after an enter.
                    throw new CrimeTime();
                }
                knownInside.remove(identifier);
                knownOutside.add(identifier);
                int nextMove = nextMovement(identifier, i);
                if (nextMove >= 0 && movements[nextMove].isEnter) {
                    toAssignExits.put(nextMove, identifier);
                }
            } else {
                // Exit
                if (knownInside.contains(identifier)) {
                    throw new CrimeTime();
                }
                knownOutside.remove(identifier);
                knownInside.add(identifier);
                int nextMove = nextMovement(identifier, i);
                if (nextMove >= 0 && !movements[nextMove].isEnter) {
                    toAssignEntries.put(nextMove, identifier);
                }
            }
        }

        private int nextMovement(int identifier, int i) {
            while (--i >= 0 && movements[i].identifier != identifier)
                {}
            return i;
        }

        private int countPeopleInHouse() {
            Set<Integer> knownsInHouse = new HashSet<Integer>();
            // Strictly speaking, unnecessary, but good for bug detection:
            Set<Integer> knownsOutHouse = new HashSet<Integer>();
            int unknownsInHouse = 0;
            for (Movement movement : movements) {
                if (movement.identifier != 0) {
                    if (movement.isEnter) {
                        if (knownsInHouse.contains(movement.identifier)) {
                            throw new RuntimeException("Can't happen");
                        }
                        knownsOutHouse.remove(movement.identifier);
                        knownsInHouse.add(movement.identifier);
                    } else {
                        if (knownsOutHouse.contains(movement.identifier)) {
                            throw new RuntimeException("Can't happen");
                        }
                        knownsOutHouse.remove(movement.identifier);
                        knownsInHouse.remove(movement.identifier);
                    }
                } else {
                    if (movement.isEnter) {
                        unknownsInHouse++;
                    } else {
                        unknownsInHouse = Math.max(0, unknownsInHouse - 1);
                    }
                }
            }
            return unknownsInHouse + knownsInHouse.size();
        }

        // If there are known people in at the end, we might be able to assign them an unknown exit to reduce the
        // knowns-in cost.
        private SortedMap<Integer, Integer> getKnownsInAtEndByEntryTime() {
            Set<Integer> seen = new HashSet<Integer>();
            SortedMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
            seen.add(0);
            for (int i = movements.length - 1; i >= 0; i--) {
                Movement movement = movements[i];
                if (!seen.contains(movement.identifier) && movement.isEnter) {
                    result.put(i, movement.identifier);
                }
                seen.add(movement.identifier);
            }
            return result;
        }

        // TODO: See if undoing optional assignments could improve final score...
        private void assignFinalKnownExits() {
            SortedMap<Integer, Integer> knownsInAtEnd = getKnownsInAtEndByEntryTime();
            // Assign these exits as early as possible, this is optimal.
            for (int i = 0; i < movements.length; i++) {
                if (knownsInAtEnd.isEmpty()) {
                    return;
                }
                Movement movement = movements[i];
                if (movement.identifier == 0 && !movement.isEnter) {
                    int firstTarget = knownsInAtEnd.firstKey();
                    if (firstTarget < i) {
                        // Candidate has already done final movement.
                        movement.identifier = knownsInAtEnd.get(firstTarget);
                        knownsInAtEnd.remove(firstTarget);
                    }
                }
            }
        }
    }
}
