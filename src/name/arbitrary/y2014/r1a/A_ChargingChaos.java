package name.arbitrary.y2014.r1a;

import name.arbitrary.CodeJamBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 11:35 - 11:40, 14:08 - 14:18, 14:20 - 14:24, 14:33 -
public class A_ChargingChaos extends CodeJamBase {
    A_ChargingChaos(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_ChargingChaos(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int n = Integer.parseInt(parts[0]);
        int l = Integer.parseInt(parts[1]);
        String[] outletStrings = getLine().split(" ");
        assert outletStrings.length == n;
        assert outletStrings[0].length() == l;
        String[] deviceStrings = getLine().split(" ");
        assert deviceStrings.length == n;
        assert deviceStrings[0].length() == l;

        List<Long> outlets = listConvert(outletStrings);
        List<Long> devices = listConvert(deviceStrings);

        Collections.sort(devices);

        int minFlips = Integer.MAX_VALUE;
        // Try plugging the first device into each of the outlets, set appropriately...
        for (long outlet : outlets) {
            minFlips = Math.min(tryDevice(outlet, outlets, devices), minFlips);
        }

        return minFlips != Integer.MAX_VALUE ? "" + minFlips : "NOT POSSIBLE";
    }

    private int tryDevice(long testOutlet, List<Long> outlets, List<Long> devices) {
        long toFlip = testOutlet ^ devices.get(0);
        List<Long> flippedOutlets = new ArrayList<Long>(outlets.size());
        for (long outlet : outlets) {
            flippedOutlets.add(outlet ^ toFlip);
        }
        Collections.sort(flippedOutlets);

        if (!flippedOutlets.equals(devices)) {
            return Integer.MAX_VALUE;
        }

        return Long.bitCount(toFlip);
    }

    private List<Long> listConvert(String[] strings) {
        List<Long> result = new ArrayList<Long>(strings.length);
        for (String deviceString : strings) {
            result.add(convert(deviceString));
        }
        return result;
    }

    private long convert(String string) {
        long l = 0;
        for (char c : string.toCharArray()) {
            l *= 2;
            switch (c) {
                case '0':
                    break;
                case '1':
                    l += 1;
                    break;
                default:
                    throw new RuntimeException("Bad char: " + c);
            }
        }
        return l;
    }
}
