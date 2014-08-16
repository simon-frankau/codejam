package name.arbitrary.y2014.r1a;

import name.arbitrary.CodeJamBase;

// 22:07 - 23:25
public class C_ProperShuffle extends CodeJamBase {
    private static int NUM = 1000;
    private static int SAMPLES = 200000;
    private double[][] badDistrib;

    // badDistrib[i][j] is probability of finding i at position j, given bad shuffle.

    C_ProperShuffle(String fileName) {
        super(fileName);
        initDistrib();
    }

    private void initDistrib() {
        badDistrib = new double[NUM][NUM];
        int[] shuffle = new int[NUM];
        for (int i = 0; i < SAMPLES; ++i) {
            if (i % 100 == 0) {
                System.err.println(i);
            }
            for (int j = 0; j < NUM; ++j) {
                shuffle[j] = j;
            }
            badShuffle(shuffle);
            for (int j = 0; j < NUM; ++j) {
                badDistrib[shuffle[j]][j]++;
            }
        }
        for (int i = 0; i < NUM; i++) {
            for (int j = 0; j < NUM; j++) {
                badDistrib[i][j] /= SAMPLES;
                // Add 20% normal to smooth noisiness. Without these, I think noise in the randomly sampled
                // distribution has too much influence in the results.
                badDistrib[i][j] *= 0.80;
                badDistrib[i][j] += 0.20 / NUM;
            }
        }
    }

    void badShuffle(int[] vals) {
        for (int i = 0; i < vals.length; i++) {
            int idx = (int)Math.floor(Math.random() * vals.length);
            int tmp = vals[i];
            vals[i] = vals[idx];
            vals[idx] = tmp;
        }
    }

    public static void main(String[] args) {
        new C_ProperShuffle(args[0]).run();
    }

    @Override
    protected String runCase() {
        int n = Integer.parseInt(getLine());
        String parts[] = getLine().split(" ");
        int[] nums = new int[parts.length];
        int i = 0;
        for (String part : parts) {
            nums[i++] = Integer.parseInt(part);
        }

        double pGood = calcProb(nums);
        System.err.println(pGood);
        return pGood > 0.5 ? "GOOD" : "BAD";
    }

    // Bayesian stuff: P(Bad | Pos) = P(Bad n Pos) / P(Pos) = P(Bad n Pos) / P(Pos)
    // = (P(Pos | Bad) * P(Bad)) / (P(Pos | Bad) * P(Bad) + P(Pos | Good) * P(Good))
    private double calcProb(int[] nums) {
        double pGood = 0.5;
        double pPosGivenGood = 1.0 / NUM;
        for (int i = 0; i < nums.length; ++i) {
            // System.err.println(".." + pGood);
            double pBad = 1 - pGood;
            double pPosGivenBad = badDistrib[nums[i]][i];
            double pPosAndBad = pPosGivenBad * pBad;
            double pPosAndGood = pPosGivenGood * pGood;
            double pPos = pPosAndBad + pPosAndGood;
            double pGoodGivenPos = pPosAndGood / pPos;
            pGood = pGoodGivenPos;
        }
        return pGood;
    }
}
