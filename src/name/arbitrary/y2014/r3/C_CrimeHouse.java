package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.*;

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

        Movement(boolean isEnter, int identifier) {
            this.isEnter = isEnter;
            this.identifier = identifier;
        }

        public String toString() {
            return (isEnter ? "E " : "L ") + identifier;
        }
    }

    static class Loop implements Comparable<Loop> {
        public final int start;
        public final int end;
        public final int identifier;

        Loop(int start, int end, int identifier) {
            this.start = start;
            this.end = end;
            this.identifier = identifier;
        }

        public String toString() {
            return start + "-" + end + " (" + identifier + ")";
        }

        @Override
        public int compareTo(Loop loop) {
            // No overflow worries.
            int result = start - loop.start;
            if (result != 0) {
                return result;
            }
            result = end - loop.end;
            if (result != 0) {
                return result;
            }
            return identifier - loop.identifier;
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

        public Run(Movement[] movements) {
            this.movements = movements;
        }

        private String go() throws CrimeTime {
            System.err.println(Arrays.asList(movements));

            assignForward();

            System.err.println(Arrays.asList(movements));

            assignBackward();

            System.err.println(Arrays.asList(movements));

            // Still no idea why these loops are optimal, at least for the cases tested...
            System.err.println(forwardLoops());
            System.err.println(backwardLoops());

            applyLoops(backwardLoops());

            return "" + countPeopleInHouse();
        }

        void assignForward() throws CrimeTime {
            // Set of people who should exit, sorted by deadline.
            SortedMap<Integer, Integer> toExit = new TreeMap<Integer, Integer>();
            for (int i = 0; i < movements.length; i++) {
                Movement movement = movements[i];
                if (movement.identifier != 0 && movement.isEnter) {
                    int identifier = movement.identifier;
                    int j = nextMovement(identifier, i);
                    if (j == movements.length) {
                        // Would like to assign an exit to a known person
                        toExit.put(movements.length + i, identifier); // NB: Making key unique
                    } else if (j < movements.length && movements[j].isEnter) {
                        // Assign to avoid CRIME TIME
                        toExit.put(j, identifier);
                    }
                } else if (movement.identifier == 0 && !movement.isEnter && !toExit.isEmpty()) {
                    int key = toExit.firstKey();
                    if (key <= i) {
                        throw new CrimeTime();
                    }
                    movement.identifier = toExit.get(key);
                    toExit.remove(key);
                }
            }
            if (!toExit.isEmpty() && toExit.firstKey() < movements.length) {
                throw new CrimeTime();
            }
        }

        private int nextMovement(int identifier, int i) {
            while (++i < movements.length && movements[i].identifier != identifier)
                {}
            return i;
        }

        void assignBackward() throws CrimeTime {
            // Set of people who should enter, sorted by deadline.
            SortedMap<Integer, Integer> toEnter = new TreeMap<Integer, Integer>();
            for (int i = movements.length - 1; i >= 0; i--) {
                Movement movement = movements[i];
                if (movement.identifier != 0 && !movement.isEnter) {
                    int identifier = movement.identifier;
                    int j = prevMovement(identifier, i);
                    if (j == -1) {
                        // Would like to assign an entry to a known person
                        toEnter.put(-1 - i, identifier); // NB: Making key unique
                    } else if (j < movements.length && !movements[j].isEnter) {
                        // Assign to avoid CRIME TIME
                        toEnter.put(j, identifier);
                    }
                } else if (movement.identifier == 0 && movement.isEnter && !toEnter.isEmpty()) {
                    int key = toEnter.lastKey();
                    if (key >= i) {
                        throw new CrimeTime();
                    }
                    movement.identifier = toEnter.get(key);
                    toEnter.remove(key);
                }
            }
            if (!toEnter.isEmpty() && toEnter.lastKey() >= 0) {
                throw new CrimeTime();
            }
        }

        private int prevMovement(int identifier, int i) {
            while (--i >= 0 && movements[i].identifier != identifier)
                {}
            return i;
        }

        private Set<Loop> forwardLoops() {
            Set<Loop> result = new TreeSet<Loop>();
            Set<Integer> candidates = new HashSet<Integer>();
            Set<Integer> assigned = new HashSet<Integer>();
            for (int i = 0; i < movements.length; i++ ) {
                Movement movement = movements[i];
                if (movement.identifier != 0) {
                    // Keep track of who's in, who can be looped out and back in...
                    if (movement.isEnter) {
                        candidates.add(movement.identifier);
                    } else {
                        candidates.remove(movement.identifier);
                    }
                } else {
                    if (!movement.isEnter) {
                        int bestIdentifier = -1;
                        int bestReturnTime = 0;
                        for (int candidate : candidates) {
                            int returnTime = prevUnknown(assigned, nextMovement(candidate, i), i);
                            if (returnTime < movements.length && returnTime > bestReturnTime) {
                                bestIdentifier = candidate;
                                bestReturnTime = returnTime;
                            }
                        }
                        if (bestIdentifier >= 0) {
                            result.add(new Loop(i, bestReturnTime, bestIdentifier));
                            candidates.remove(bestIdentifier);
                            assigned.add(bestReturnTime);
                        }
                    }
                }
            }
            return result;
        }

        private int prevUnknown(Set<Integer> assigned, int end, int start) {
            if (end >= movements.length) {
                return end;
            }

            for (int i = end; i > start; i--) {
                Movement movement = movements[i];
                if (movement.identifier == 0 && movement.isEnter && !assigned.contains(i)) {
                    return i;
                }
            }

            return movements.length;
        }

        private Set<Loop> backwardLoops() {
            Set<Loop> result = new TreeSet<Loop>();
            Set<Integer> candidates = new HashSet<Integer>();
            Set<Integer> assigned = new HashSet<Integer>();
            for (int i = movements.length - 1; i >= 0; i-- ) {
                Movement movement = movements[i];
                if (movement.identifier != 0) {
                    // Keep track of who's in, who can be looped out and back in...
                    if (!movement.isEnter) {
                        candidates.add(movement.identifier);
                    } else {
                        candidates.remove(movement.identifier);
                    }
                } else {
                    if (movement.isEnter) {
                        int bestIdentifier = -1;
                        int bestReturnTime = Integer.MAX_VALUE;
                        for (int candidate : candidates) {
                            int returnTime = nextUnknown(assigned, prevMovement(candidate, i), i);
                            if (returnTime >= 0 && returnTime < bestReturnTime) {
                                bestIdentifier = candidate;
                                bestReturnTime = returnTime;
                            }
                        }
                        if (bestIdentifier >= 0) {
                            result.add(new Loop(bestReturnTime, i, bestIdentifier));
                            candidates.remove(bestIdentifier);
                            assigned.add(bestReturnTime);
                        }
                    }
                }
            }
            return result;
        }

        private int nextUnknown(Set<Integer> assigned, int start, int end) {
            if (start < 0) {
                return start;
            }

            for (int i = start; i < end; i++) {
                Movement movement = movements[i];
                if (movement.identifier == 0 && !movement.isEnter && !assigned.contains(i)) {
                    return i;
                }
            }

            return -1;
        }

        private void applyLoops(Set<Loop> loops) {
            for (Loop loop : loops) {
                movements[loop.start].identifier = loop.identifier;
                movements[loop.end].identifier = loop.identifier;
            }
        }

        private int countPeopleInHouse() {
            Set<Integer> knownsInHouse = new HashSet<Integer>();
            // Strictly speaking, unnecessary, but good for bug detection:
            Set<Integer> knownsOutHouse = new HashSet<Integer>();
            int unknownsInHouse = 0;
            int i = 0;
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
                System.err.println(i++ + ": " + (unknownsInHouse + knownsInHouse.size()) + " " + movement);
            }
            return unknownsInHouse + knownsInHouse.size();
        }
    }
}
