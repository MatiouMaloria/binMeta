package fr.istic.bdanlos_mpays.hs;

import java.util.Arrays;
import java.util.Random;

public class HarmonySearch extends binMeta {

    /**
     * Represent an Harmony of HarmonyMemory
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

        @Override
        public String toString() {
            return "Harmony{" +
                    "data=" + data +
                    ", solution=" + solution +
                    ", grade=" + grade +
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

    /**
     * Initialisation of HarmonyMemory
     * Initialise the HarmonyMemory with a random values, which belong to the objective's set
     */
    private void initialisation(){
        this.HM = new Harmony[this.HMS];
        int best, worst;

        Data first = new Data(this.solution);
        Data second = new Data(obj.solutionSample());

        if (obj.value(first) > obj.value(second)){
            this.HM[0] = new Harmony(first,obj.value(first),2);
            this.HM[1] = new Harmony(second,obj.value(second),0);
            best = 1;
            worst = 0;
            this.solution = new Data(second);
        }else{
            this.HM[0] = new Harmony(first,obj.value(first),0);
            this.HM[1] = new Harmony(second,obj.value(second),2);
            best = 0;
            worst = 1;
        }

        for(int i = 1; i < this.HMS; i++){
            Data next = new Data(obj.solutionSample());
            if (this.HM[best].solution > obj.value(next)){
                this.HM[best].setGrade(1);
                this.HM[i] = new Harmony(next, obj.value(next), 0);
                best = i;
                this.solution = new Data(next);
            }else if(this.HM[worst].solution < obj.value(next)){
                this.HM[worst].setGrade(1);
                this.HM[i] = new Harmony(next, obj.value(next), 2);
                worst = i;
            }else{
                this.HM[i] = new Harmony(next, obj.value(next), 1);
            }
        }

    }

    @Override
    /**
     * Optimization of HarmonyMemory
     */
    public void optimize() {
        Random R = new Random();
        Data D = new Data(this.solution);
        long startime = System.currentTimeMillis();

        // main loop
        while (System.currentTimeMillis() - startime < this.maxTime)
        {

        }
    }

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
        hs.initialisation();
        System.out.println(hs);
        System.out.println("starting point : " + hs.getSolution());
        System.out.println("optimizing ...");
        hs.optimize();
        System.out.println(hs);
        System.out.println("solution : " + hs.getSolution());
        System.out.println();


    }

}
