package name.arbitrary.y2014.r2;

import name.arbitrary.CodeJamBase;

import java.util.*;

// Basic min-flow = max-cut
public class C_DontBreakTheNile  extends CodeJamBase {
    C_DontBreakTheNile(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_DontBreakTheNile(args[0]).run();
    }

    @Override
    protected String runCase() {
        List<Integer> nums = getLineNumericElements();
        int w = nums.get(0);
        int h = nums.get(1);
        int b = nums.get(2);

        List<Building> buildings = new ArrayList<Building>();
        for (int i = 0; i < b; i++) {
            List<Integer> coords = getLineNumericElements();
            buildings.add(new Building(coords.get(0), coords.get(1), coords.get(2), coords.get(3)));
        }

        Building startBuilding = new Building(-1, -1, -1, h + 1);
        Building endBuilding   = new Building(w,  -1,  w, h + 1);
        buildings.add(startBuilding);
        buildings.add(endBuilding);

        return "" + calcDistance(buildings, startBuilding, endBuilding);
    }

    private static class Building {
        private final int x0;
        private final int x1;
        private final int y0;
        private final int y1;

        public Building(int x0, int y0, int x1, int y1) {
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.y1 = y1;
        }

        // Distance between buildings is minimum number of cells we must cross
        public int distanceTo(Building that) {
            int xDist = singleAxisDistance(x0, x1, that.x0, that.x1);
            int yDist = singleAxisDistance(y0, y1, that.y0, that.y1);
            return Math.max(xDist, yDist);
        }

        // If a and b cover extents a0-a1, b0-b1 in a dimension, what's the distance between them, projected on that
        // dimension? a0 < a1, b0 < b1
        private int singleAxisDistance(int a0, int a1, int b0, int b1) {
            if (b1 < a0) {
                return a0 - b1 - 1;
            } else if (a1 < b0) {
                return b0 - a1 - 1;
            } else {
                return 0; // Overlap
            }
        }

        @Override
        public String toString() {
            return "Building{" +
                    "x0=" + x0 +
                    ", x1=" + x1 +
                    ", y0=" + y0 +
                    ", y1=" + y1 +
                    '}';
        }
    }

    private int calcDistance(List<Building> buildings, Building startBuilding, Building endBuilding) {
        Set<Building> remainingBuildings = new HashSet<Building>(buildings);
        SortedMap<Integer, List<Building>> distances = new TreeMap<Integer, List<Building>>();
        Map<Building, Integer> backMap = new HashMap<Building, Integer>();
        addEdge(distances, backMap, startBuilding, 0);

        while (true) {
            System.err.println(remainingBuildings.size());
            // System.err.println("Remaining: " + remainingBuildings);
            // System.err.println("Scheduled: " + distances);
            int nearestDistance = distances.firstKey();
            List<Building> nearestBuildings = distances.get(nearestDistance);
            if (nearestBuildings.isEmpty()) {
                distances.remove(nearestDistance);
                continue;
            }
            Building nearestBuilding = nearestBuildings.get(0);
            // System.err.println("Processing " + nearestBuilding);
            nearestBuildings.remove(0);
            remainingBuildings.remove(nearestBuilding);
            if (nearestBuilding == endBuilding) {
                return nearestDistance;
            }
            for (Building building : remainingBuildings) {
                addEdge(distances, backMap, building, nearestDistance + nearestBuilding.distanceTo(building));
            }
        }
    }

    private void addEdge(Map<Integer, List<Building>> distances,
                         Map<Building, Integer> backMap,
                         Building building,
                         int distance) {
        Integer existingDistance = backMap.get(building);
        if (existingDistance != null) {
            if (existingDistance <= distance) {
                return;
            }
            distances.get(existingDistance).remove(building);
        }
        List<Building> buildings = distances.get(distance);
        if (buildings == null) {
            buildings = new LinkedList<Building>();
            distances.put(distance, buildings);
        }
        buildings.add(building);
        backMap.put(building, distance);
    }
}
