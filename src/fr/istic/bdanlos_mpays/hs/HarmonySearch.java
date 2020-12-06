package fr.istic.bdanlos_mpays.hs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * My class of Thread to make a Thread in search of harmony
 */
class MyThread extends Thread{

    private HarmonySearch hs;
    private Semaphore sem;

    /**
     * MyThread constructor
     * @param hs HarmonySearch
     * @param sem Semaphore
     */
    public MyThread(HarmonySearch hs, Semaphore sem){
        this.hs = hs;
        this.sem = sem;
    }

    @Override
    public void run() {
        Data newHarmonyValue;
        while (System.currentTimeMillis() - this.hs.startime < this.hs.maxTime && this.hs.gn < this.hs.iterationNumber){
            newHarmonyValue = this.hs.searchOfNewHarmony(this.hs.gn);
            try{
                sem.acquire();
                this.hs.harmoniesMemory.replaceWorstHarmony(newHarmonyValue);
                this.hs.gn++;
                sem.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class HarmonySearch extends binMeta {

    /**
     * Represent an Harmony (a solution) of HarmonyMemory
     */
    public static class Harmony extends Data{

        private final Data data;
        private final double solution;

        /**
         * Harmony Constructor
         * @param data function with assigned values
         * @param solution function's solution
         */
        public Harmony(Data data, double solution){
            super(data);
            this.data = data;
            this.solution = solution;
        }

        /**
         * Get the data
         * @return Data function with assigned values
         */
        public Data getData() {
            return data;
        }

        /**
         * Get the solution
         * @return double function's solution
         */
        public double getSolution() {
            return solution;
        }

        @Override
        public String toString() {
            return "Harmony{" +
                    "data=" + data +
                    ", solution=" + solution +
                    '}' + "\n";
        }

    }

    /**
     * Represent the harmonies' memory
     */
    public class HarmoniesMemory{

        private Harmony[] HM;
        private int bestHarmonyIndex = 0;
        private int worstHarmonyIndex = 0;
        private int HMS;

        /**
         * HarmoniesMemory Constructor
         * @param HMS Number of Harmony in HarmonyMemory (MINIMUM 2)
         * Initialisation of HarmonyMemory
         * Initialise the HarmonyMemory with a random values, which belong to the objective's set
         */
        public HarmoniesMemory(int HMS){
            this.HMS = HMS;
            this.HM = new Harmony[HMS];
            for(int i = 0; i < HMS; i++){
                Data nextData = new Data(obj.solutionSample());
                double nextSolution = obj.value(nextData);
                this.HM[i] = new Harmony(nextData, nextSolution);
                if (this.HM[i].getSolution() >= this.HM[worstHarmonyIndex].getSolution()){
                    this.worstHarmonyIndex = i;
                }
                if (this.HM[i].getSolution() < this.HM[bestHarmonyIndex].getSolution()){
                    this.bestHarmonyIndex = i;
                }
            }
            objValue = this.HM[bestHarmonyIndex].getSolution();
            solution = this.HM[bestHarmonyIndex].getData();
        }

        /**
         * Get the harmony of HarmonyMemory by the index
         * @param index index of the HarmonyMemory
         * @return Harmony in the index of HarmonyMemory
         */
        public Harmony getHarmony(int index){
            return this.HM[index];
        }

        /**
         * Replace the worstHarmony by the newHarmonyData if this is better
         * @param newHarmonyData The NewHarmony generated
         */
        public void replaceWorstHarmony(Data newHarmonyData){
            double newHarmonyValue = obj.value(newHarmonyData);
            if (newHarmonyValue < this.HM[worstHarmonyIndex].getSolution()){
                Harmony newHarmony = new Harmony(newHarmonyData, newHarmonyValue);
                this.HM[worstHarmonyIndex] = newHarmony;
                if (newHarmonyValue < this.HM[bestHarmonyIndex].getSolution()){
                    bestHarmonyIndex = worstHarmonyIndex;
                }
                for (int i = 0; i < this.HMS; i++){
                    if (this.HM[i].getSolution() >= this.HM[worstHarmonyIndex].getSolution()){
                        worstHarmonyIndex = i;
                    }
                }
                objValue = this.HM[bestHarmonyIndex].getSolution();
                solution = this.HM[bestHarmonyIndex].getData();
            }
        }

        @Override
        public String toString() {
            return "HarmoniesMemory{" +
                    "HM=" + Arrays.toString(HM) +
                    ",\n bestHarmonyIndex=" + bestHarmonyIndex +
                    ", worstHarmonyIndex=" + worstHarmonyIndex +
                    '}';
        }
    }

    public HarmoniesMemory harmoniesMemory;
    private int HMS; //Number of Harmony in HarmonyMemory (MINIMUM 2)
    private int musicNoteNumber; //Number of notes(variables) in a harmony
    public int iterationNumber; //Number of iteration of optimization's step Solution optimum avec 8000000 ITMAX 2000000
    public int gn;
    public long startime;
    /*
    * Search parameter | Probability of search's type
     */
    private float HMCR = 0.84f; //(1-HMCR) Probability of full random note creation (random selection)
    private float PARmin = 0.38f; //Minimum probability between pitch adjusting and memory consideration
    private float PARmax = 0.99f; //Maximum probability between pitch adjusting and memory consideration
    private double bwmin = 0f; //Minimal distance of jump
    private double bwmax; //Maximal distance of jump

    /**
     * HarmonySearch Constructor
     * @param obj Function to minimize
     * @param maxTime The maximum time allowed for the search for solutions
     * @param musicNoteNumber Number of notes(variables) of the function
     * @param maxIteration The maximum iteration allowed for the search for solutions
     */
    public HarmonySearch(Objective obj, long maxTime, int musicNoteNumber, int maxIteration){
        try
        {
            String msg = "Impossible to create HarmonySearch object: ";
            if (maxTime <= 0) throw new Exception(msg + "the maximum execution time is 0 or even negative");
            this.maxTime = maxTime;
            if (obj == null) throw new Exception(msg + "the reference to the objective is null");
            this.obj = obj;
            this.solution = obj.solutionSample();
            this.objValue = this.obj.value(this.solution);
            this.metaName = "HarmonySearch";
            if (obj.solutionSample().numberOfBits() % musicNoteNumber != 0) throw  new Exception(msg + "division of decision variables impossible");
            this.musicNoteNumber = musicNoteNumber;
            this.bwmax = Math.pow(2,(float) (this.solution.numberOfBits()/musicNoteNumber));
            this.bwmin = 0f;
            this.HMS = 4;
            this.iterationNumber = maxIteration;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Calculate and return of PAR with the mathematical function of the HS's algorithm
     * This mathematical function allows to change the probability of simple copy and copy and modification of new musical notes as the iterations.
     * At the start of the program, the algorithm has a better chance of to copy and change the musical notes (pitch adjustment) than just copying the notes (memory consideration).
     * @param gn Generation number
     * @return par Probability between pitch adjustment and memory consideration
     */
    private double getPar(int gn){
        return PARmin + ((PARmax - PARmin) / iterationNumber) * gn;
    }

    /**
     * Calculate and return of BW with the mathematical function of the HS's algorithm
     * This mathematical function allows you to change the jump distance when changing the musical note (pitch adjustment).
     * At the start of the program, the algorithm will make a big jump when changing variables(pitch adjustment) and a small jump at the end
     * @param gn Generation number
     * @return bw Distance of jump during the pitch adjustment
     */
    private double getBw(int gn){
        return bwmax * Math.exp((Math.log(bwmin/bwmax)/iterationNumber*gn));
    }

    @Override
    public void optimize() {
        this.harmoniesMemory = new HarmoniesMemory(this.HMS);
        Data newHarmonyValue;
        this.gn = 1; //generation Number
        this.startime = System.currentTimeMillis();

        Semaphore sem = new Semaphore(1,true);
        MyThread thread2 = new MyThread(this,sem);
        MyThread thread3 = new MyThread(this,sem);
        thread2.start();
        thread3.start();

        // Main loop
        while (System.currentTimeMillis() - this.startime < this.maxTime && this.gn < iterationNumber){
            newHarmonyValue = searchOfNewHarmony(gn);
            try{
                sem.acquire();
                harmoniesMemory.replaceWorstHarmony(newHarmonyValue);
                this.gn++;
                sem.release();
                thread2.join();
                thread3.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n" + obj.getName() + " Nombre d'itération " + gn);
    }

    /**
     * Create the Data of new harmony with different methods (Pitch adjustment, Memory consideration, Random selection)
     * @param gn Nth Iterations, using by the bw(), par() function
     * @return A function with variable to create a new Harmony
     */
    public Data searchOfNewHarmony(int gn){
        Random R = new Random();
        List<Data> newHarmonyValue = new ArrayList<Data>();
        int noteLength = this.solution.numberOfBits()/this.musicNoteNumber;
        float ran;
        double par;
        double bw;
        // Creation of new harmony. Each loop turn is the creation of a new musical note
        for (int i = 0; i < this.musicNoteNumber; i++){
            par = getPar(gn);
            bw = getBw(gn);
            ran = R.nextFloat();
            if (ran < HMCR){
                int musicNoteCopyIndex = (int)(ran*HMS);
                Data musicNoteCopy = new Data(this.harmoniesMemory.getHarmony(musicNoteCopyIndex).getData(),i*noteLength,Math.min((i+1)*noteLength-1, this.solution.numberOfBits()-1));
                if (ran < par){
                    // Pitch adjustment
                    int musicNoteValue = musicNoteCopy.intValue();
                    int distanceJump = (int) (ran*bw);
                    if (ran < 0.5){
                        // Adjust pitch up
                        newHarmonyValue.add(new Data(new Data(musicNoteValue + distanceJump),32-noteLength,31));
                    }else{
                        // Adjust pitch down
                        newHarmonyValue.add(new Data(new Data(musicNoteValue - distanceJump),32-noteLength,31));
                    }
                }else{
                    //Memory consideration
                    newHarmonyValue.add(musicNoteCopy);
                }
            }else{
                //Random selection
                newHarmonyValue.add(new Data((Math.min((i+1)*noteLength, this.solution.numberOfBits()) - i*noteLength),0.5f));

            }
        }
        return new Data(newHarmonyValue);
    }

    public static void main(String[] args)
    {
        int TMAX = 10000;  // max time

        // BitCounter
        int n = 50;
        Objective obj = new BitCounter(n);
        HarmonySearch hs = new HarmonySearch(obj,TMAX, 50, 20000);
        System.out.println(hs);
        System.out.println("starting point : " + hs.getSolution());
        System.out.println("optimizing ...");
        hs.optimize();
        System.out.println(hs);
        System.out.println("solution : " + hs.getSolution());
        System.out.println();

        // Fermat
        int exp = 2;
        int ndigits = 10;
        obj = new Fermat(exp,ndigits);
        hs = new HarmonySearch(obj,TMAX, 3, 3000000);
        System.out.println(hs);
        System.out.println("starting point : " + hs.getSolution());
        System.out.println("optimizing ...");
        hs.optimize();
        System.out.println(hs);
        System.out.println("solution : " + hs.getSolution());
        Data x = new Data(hs.solution,0,ndigits-1);
        Data y = new Data(hs.solution,ndigits,2*ndigits-1);
        Data z = new Data(hs.solution,2*ndigits,3*ndigits-1);
        System.out.print("equivalent to the equation : " + x.posLongValue() + "^" + exp + " + " + y.posLongValue() + "^" + exp);
        if (hs.objValue == 0.0)
            System.out.print(" == ");
        else
            System.out.print(" ?= ");
        System.out.println(z.posLongValue() + "^" + exp);
        System.out.println();

        // ColorPartition
        n = 4;  int m = 14;
        ColorPartition cp = new ColorPartition(n,m);
        hs = new HarmonySearch(cp,TMAX, 56, 1000000);
        System.out.println(hs);
        System.out.println("starting point : " + hs.getSolution());
        System.out.println("optimizing ...");
        hs.optimize();
        System.out.println(hs);
        System.out.println("solution : " + hs.getSolution());
        cp.value(hs.solution);
        System.out.println("corresponding to the matrix :\n" + cp.show());
    }

}
