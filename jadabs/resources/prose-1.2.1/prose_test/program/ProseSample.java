package program;

import ch.ethz.prose.ProseSystem;

public class ProseSample {

    protected int invocations;

    public void test() {
        System.out.println(++invocations);
    }

    public void testDummy() {
        // Do nothing.
    }

    public static void main(String[] args) throws Exception {
        ProseSystem.startup();
        ProseSample app = new ProseSample();
        while (true) {
            app.test();
            System.out.println();
            waitOneSecond();
        }
    }
    
    public static void waitOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
    }

}