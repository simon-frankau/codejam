package name.arbitrary.y2014.r2;

import name.arbitrary.CodeJamBase;

import java.util.*;

// Brute-force solution. Still too slow to run on the small example in time!
public class D_TrieSharding extends CodeJamBase {
    D_TrieSharding(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new D_TrieSharding(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int m = Integer.parseInt(parts[0]);
        int n = Integer.parseInt(parts[1]);
        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < m; i++) {
            strings.add(getLine());
        }

        System.err.println(strings);

        Trie trie = constructTrie(strings);
        System.err.println(trie);
        int count = countTrie(trie);

        return simpleSolution(n, strings)  + " / " + countWorstCase(n, strings);
    }

    private String simpleSolution(int n, List<String> strings) {
        Set<Map<String, Integer>> mappings = new HashSet<Map<String, Integer>>();
        mappings.add(new HashMap<String, Integer>());

        for (String string : strings) {
            Set<Map<String, Integer>> newMappings = new HashSet<Map<String, Integer>>();
            for (int i = 0; i < n; i++) {
                for (Map<String, Integer> mapping : mappings) {
                    Map<String, Integer> newMapping = new HashMap<String, Integer>(mapping);
                    newMapping.put(string, i);
                    newMappings.add(newMapping);
                }
            }
            // System.err.println(newMappings);
            mappings = newMappings;
        }

        SortedMap<Integer, Integer> solutionCounts = new TreeMap<Integer, Integer>();

        for (Map<String, Integer> mapping : mappings) {
            int cost = findCost(mapping);
            Integer oldCount = solutionCounts.get(cost);
            solutionCounts.put(cost, (oldCount == null ? 0 : oldCount) + 1);
        }

        int worstCost = solutionCounts.lastKey();
        return worstCost + " " + solutionCounts.get(worstCost);
    }

    private int findCost(Map<String, Integer> mapping) {
        Map<Integer, Set<String>> nodeMap = new HashMap<Integer, Set<String>>();
        for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
            int shard = entry.getValue();
            Set<String> nodeSet = nodeMap.get(shard);
            if (nodeSet == null) {
                nodeSet = new HashSet<String>();
                nodeMap.put(shard, nodeSet);
            }
            insertPrefices(entry.getKey(), nodeSet);
        }

        // System.err.println(nodeMap);

        int cost = 0;
        for (Set<String> nodes : nodeMap.values()) {
            cost += nodes.size();
        }
        return cost;
    }

    private void insertPrefices(String key, Set<String> nodeSet) {
        nodeSet.add(key);
        for (int i = 0; i <= key.length(); i++) {
            nodeSet.add(key.substring(0, i));
        }
    }

    // To generate the worst case count, we count the number of child strings of each node, and know that we
    // can shard them up to n ways.
    private String countWorstCase(int n, List<String> strings) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (String string : strings) {
            for (int i = 0; i <= string.length(); i++) {
                String subString = string.substring(0, i);
                Integer oldCount = counts.get(subString);
                counts.put(subString, (oldCount != null ? oldCount : 0) + 1);
            }
        }

        int cost = 0;
        for (Integer count : counts.values()) {
            cost += Math.min(n, count);
        }
        return "" + cost;
    }

    // Right, let's generate the actual trie:
    static class Trie {
        public Map<Character, Trie> children;
        public boolean isTerminal;

        Trie() {
            this.children = new HashMap<Character, Trie>();
            this.isTerminal = false;
        }

        public void add(LinkedList<Character> chars) {
            if (chars.isEmpty()) {
                isTerminal = true;
            } else {
                char c = chars.removeFirst();
                Trie child = children.get(c);
                if (child == null) {
                    child = new Trie();
                    children.put(c, child);
                }
                child.add(chars);
            }
        }

        public String toString() {
            return (isTerminal ? "!!! " : "") + children;
        }
    }

    Trie constructTrie(List<String> strings) {
        Trie root = new Trie();
        for (String string : strings) {
            LinkedList<Character> path = new LinkedList<Character>();
            for (char c : string.toCharArray()) {
                path.add(c);
            }
            root.add(path);
        }
        return root;
    }

    private int countTrie(Trie trie) {
        return 0;
    }
}
