package fr.istic.bdanlos_mpays.hs;

import java.util.Random;

public class HarmonySearch extends binMeta {

    /**
     * Represent an Harmony (a solution) of HarmonyMemory
     */
    private class Harmony{

        private final Data data;
        private final double solution;
        private int grade;

        /**
         *
         * @param data function with assigned values
         * @param solution function's solution
         * @param grade solution's grade (0 BEST, 1 MEDIUM, 2 WORST)
         */
        public Harmony(Data data, double solution, int grade){
            this.data = data;
            this.solution = solution;
            this.grade = grade;
        }

        public Data getData() {
            return data;
        }

        public double getSolution() {
            return solution;
        }

        public int getGrade() {
            return grade;
        }

        public void setGrade(int grade){
            this.grade = grade;
        }

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

    private Harmony HM[];
    private int HMS = 4; //Number of Harmony in HarmonyMemory (MINIMUM 2)
    private int HMCR;

    public HarmonySearch(Data startPoint, Objective obj, long maxTime){
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void printHM(String step){
        StringBuilder r = new StringBuilder("N=" + step + "\nHM{ \n");
        for (int i = 0; i < this.HMS; i++){
            r.append(HM[i]).append("\n");
        }
        r.append("}");
        System.out.println(r);
    }

    /**
     * Initialisation of HarmonyMemory
     * Initialise the HarmonyMemory with a random values, which belong to the objective's set
     */
    private void initialisation(){
        this.HM = new Harmony[this.HMS];
        int best = 0;
        int worst = 0;

        Data nextData = new Data(this.solution);
        double nextSolution = obj.value(nextData);
        this.HM[0] = new Harmony(nextData,nextSolution,1);

        for(int i = 1; i < this.HMS; i++){
            nextData = new Data(obj.solutionSample());
            nextSolution = obj.value(nextData);
            this.HM[i] = new Harmony(nextData,nextSolution,1);
            if (nextSolution < this.HM[best].getSolution()){
                best = i;
            }else if(nextSolution > this.HM[worst].getSolution()){
                worst = i;
            }
        }
        this.HM[best].setGrade(0);
        this.objValue = this.HM[best].getSolution();
        this.solution = this.HM[best].getData();
        this.HM[worst].setGrade(2);

        printHM("Init");
    }

    @Override
    /**
     * Optimization of HarmonyMemory
     */
    public void optimize() {
        long startime = System.currentTimeMillis();

        Random R = new Random();
        float ran;

        //Initialisation of HarmonyMemory
        initialisation();

        // main loop
        while (System.currentTimeMillis() - startime < this.maxTime)
        {
            ran = R.nextFloat();
        }
    }

    /**
     *
     * @param harmony
     */
    private void changeHarmony(Harmony harmony){

    }

    public static void main(String[] args)
    {
        int ITMAX = 10000;  // number of iterations

        // BitCounter
        int n = 50;
        Objective obj = new BitCounter(n);
        Data D = obj.solutionSample();
        HarmonySearch hs = new HarmonySearch(D,obj,ITMAX);
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
        hs = new HarmonySearch(D,obj,ITMAX);
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
        hs = new HarmonySearch(D,cp,ITMAX);
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
