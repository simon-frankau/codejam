package name.arbitrary.y2014.r3;

import com.sun.tools.javac.util.Pair;
import name.arbitrary.CodeJamBase;

import java.util.*;

public class C_CrimeHouseAgain2 extends CodeJamBase {
    C_CrimeHouseAgain2(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_CrimeHouseAgain2(args[0]).run();
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

        public Run(Movement[] movements) {
            this.movements = movements;
        }

        private String go() throws CrimeTime {
            System.err.println(Arrays.asList(movements));

            assignForward();

            System.err.println(Arrays.asList(movements));

            assignBackward();

            System.err.println(Arrays.asList(movements));
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
    }
}
