package fr.istic.bdanlos_mpays.hs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class HarmonySearch extends binMeta {

    /**
     * Represent an Harmony (a solution) of HarmonyMemory
     */
    private class Harmony{

        private final Data data;
        private final double solution;
        private int grade;

        /**
         * Harmony Constructor
         * @param data function with assigned values
         * @param solution function's solution
         * @param grade solution's grade (0 BEST, 1 MEDIUM, 2 WORST)
         */
        public Harmony(Data data, double solution, int grade){
            this.data = data;
            this.solution = solution;
            this.grade = grade;
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

        /**
         * Get the solution's grade
         * @return int solution's grade
         */
        public int getGrade() {
            return grade;
        }

        /**
         * Set the solution's grade
         * @param grade int solution's grade (0 BEST, 1 MEDIUM, 2 WORST)
         */
        public void setGrade(int grade){
            this.grade = grade;
        }

        /**
         * Return the solution's grade to string format
         * @return string solution's grade to string BEST, WORST or emptyString
         */
        private String gradeToString(){
            String r = "";
            switch (grade){
                case 0:
                    r = "BEST";
                    break;
                case 2:
                    r = "WORST";
                    break;
                default:
                    break;
            }
            return r;
        }

        @Override
        public String toString() {
            return "Harmony{" +
                    "data=" + data +
                    ", solution=" + solution +
                    "," + gradeToString() +
                    '}';
        }

    }

    /**
     * My class of Thread to make a Thread in search of harmony
     */
    private class MyThread extends Thread{

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
            Data newHarmony;
            while (System.currentTimeMillis() - this.hs.startime < this.hs.maxTime && this.hs.gn < this.hs.iterationNumber){
                newHarmony = this.hs.searchOfNewHarmony(this.hs.gn);
                try{
                    sem.acquire();
                    int worst = this.hs.getIndexofWorstHarmony();
                    if (this.hs.obj.value(newHarmony) < this.hs.harmoniesMemory[worst].getSolution()){
                        this.hs.harmoniesMemory[worst] = new Harmony(newHarmony, this.hs.obj.value(newHarmony), 1);
                        this.hs.setAllGrade();
                    }
                    this.hs.gn++;
                    sem.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Harmony[] harmoniesMemory;
    private int HMS = 7; //Number of Harmony in HarmonyMemory (MINIMUM 2)
    private int musicNoteNumber; //Number of notes(variables) in a harmony
    private final int iterationNumber = 3000000; //Number of iteration of optimization's step Solution optimum avec 8000000 ITMAX 2000000
    /*
    * Search parameter | Probability of search's type
     */
    private float HMCR = 0.90f; //(1-HMCR) Probability of full random note creation (random selection)
    private float PARmin = 0.45f; //Minimum probability between pitch adjusting and memory consideration
    private float PARmax = 0.85f; //Maximum probability between pitch adjusting and memory consideration
    private double bwmin = 0.1f; //Minimal distance of jump
    private double bwmax = 20f; //Maximal distance of jump
    /*
    * Generalization of variable for multithreading
     */
    private int gn;
    private long startime;

    /**
     * HarmonySearch Constructor
     * @param startPoint First solution
     * @param obj Function to minimize
     * @param maxTime The maximum time allowed for the search for solutions
     * @param musicNoteNumber Number of notes(variables) of the function
     */
    public HarmonySearch(Data startPoint, Objective obj, long maxTime, int musicNoteNumber){
        try
        {
            String msg = "Impossible to create HarmonySearch object: ";
            if (maxTime <= 0) throw new Exception(msg + "the maximum execution time is 0 or even negative");
            this.maxTime = maxTime;
            if (startPoint == null) throw new Exception(msg + "the reference to the starting point is null");
            this.solution = startPoint;
            if (obj == null) throw new Exception(msg + "the reference to the objective is null");
            this.obj = obj;
            this.objValue = this.obj.value(this.solution);
            this.metaName = "HarmonySearch";
            if (startPoint.numberOfBits() % musicNoteNumber != 0) throw  new Exception(msg + "division of decision variables impossible");
            this.musicNoteNumber = musicNoteNumber;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Print the HarmoniesMemory
     * @param step Search algorithm step
     */
    private void printHM(String step){
        StringBuilder r = new StringBuilder("Problem : " + obj.name + " N=" + step + "\nHM{ \n");
        for (int i = 0; i < this.HMS; i++){
            r.append(harmoniesMemory[i]).append("\n");
        }
        r.append("}");
        System.out.println(r);
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

    /**
     * Initialisation of HarmonyMemory
     * Initialise the HarmonyMemory with a random values, which belong to the objective's set
     */
    private void initialisation(){
        this.harmoniesMemory = new Harmony[this.HMS];
        Data nextData = new Data(this.solution);
        double nextSolution = obj.value(nextData);
        this.harmoniesMemory[0] = new Harmony(nextData, nextSolution, 1);

        for(int i = 1; i < this.HMS; i++){
            nextData = new Data(obj.solutionSample());
            nextSolution = obj.value(nextData);
            this.harmoniesMemory[i] = new Harmony(nextData, nextSolution, 1);
        }
        setAllGrade();
        //printHM("Init");
    }

    @Override
    public void optimize() {
        Data newHarmony;
        this.gn = 1; //generation Number
        this.startime = System.currentTimeMillis();

        Semaphore sem = new Semaphore(1,true);
        MyThread thread2 = new MyThread(this,sem);
        MyThread thread3 = new MyThread(this,sem);
        MyThread thread4 = new MyThread(this,sem);
        thread2.start();
        thread3.start();
        thread4.start();

        // Main loop
        while (System.currentTimeMillis() - this.startime < this.maxTime && this.gn < iterationNumber){
            //System.out.println(this.gn);
            newHarmony = searchOfNewHarmony(gn);
            try{
                sem.acquire();
                int worst = getIndexofWorstHarmony();
                if (obj.value(newHarmony) < harmoniesMemory[worst].getSolution()){
                    this.harmoniesMemory[worst] = new Harmony(newHarmony, obj.value(newHarmony), 1);
                    setAllGrade();
                }
                this.gn++;
                sem.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            thread2.join();
            thread3.join();
            thread4.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Data searchOfNewHarmony(int gn){
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
                Data musicNoteCopy = this.harmoniesMemory[musicNoteCopyIndex].getData();
                if (ran < par){
                    // Pitch adjustment
                    int distanceJump = (int) (ran*bw)+1;
                    newHarmonyValue.add(new Data(new Data(musicNoteCopy,i*noteLength,Math.min((i+1)*noteLength-1, this.solution.numberOfBits()-1)).randomSelectInNeighbour(distanceJump)));
                }else{
                    //Memory consideration
                    newHarmonyValue.add(new Data(musicNoteCopy,i*noteLength,Math.min((i+1)*noteLength-1, this.solution.numberOfBits()-1)));
                }
            }else{
                //Random selection
                newHarmonyValue.add(new Data((Math.min((i+1)*noteLength, this.solution.numberOfBits()) - i*noteLength),0.5f));
            }
        }
        return new Data(newHarmonyValue);
    }

    /**
     * Get the index of worst harmony in the harmony memory
     * @return int Index of worst harmony
     */
    private int getIndexofWorstHarmony(){
        int worst  = 0;
        for (int i = 1; i < HMS; i++){
            if (harmoniesMemory[i].getSolution() > harmoniesMemory[worst].getSolution()){
                worst = i;
            }
        }
        return worst;
    }

    /**
     * Get the index of best harmony in the harmony memory
     * @return int Index of best harmony
     */
    private int getIndexofBestHarmony(){
        int best  = 0;
        for (int i = 1; i < HMS; i++){
            if (harmoniesMemory[i].getSolution() < harmoniesMemory[best].getSolution()){
                best = i;
            }
        }
        return best;
    }

    /**
     * Updates all the notes attributed to our harmonies present in the harmony memory
     */
    private void setAllGrade(){
        int best = getIndexofBestHarmony();
        int worst = getIndexofWorstHarmony();
        for (int i = 0; i < HMS; i++){
            this.harmoniesMemory[i].setGrade(1);
        }
        this.harmoniesMemory[best].setGrade(0);
        this.objValue = this.harmoniesMemory[best].getSolution();
        this.solution = this.harmoniesMemory[best].getData();
        this.harmoniesMemory[worst].setGrade(2);
    }

    public static void main(String[] args)
    {
        int ITMAX = 10000;  // max time

        // BitCounter
        int n = 50;
        Objective obj = new BitCounter(n);
        Data D = obj.solutionSample();
        HarmonySearch hs = new HarmonySearch(D,obj,ITMAX, 1);
        //Initialisation of HarmonyMemory
        hs.initialisation();
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
        D = obj.solutionSample();
        hs = new HarmonySearch(D,obj,ITMAX, 3);
        //Initialisation of HarmonyMemory
        hs.initialisation();
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
        D = cp.solutionSample();
        hs = new HarmonySearch(D,cp,ITMAX, 1);
        //Initialisation of HarmonyMemory
        hs.initialisation();
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
