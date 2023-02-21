import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import java.util.Random;
import java.io.FileWriter; // Import the FileWriter class
import java.io.IOException; // Import the IOException class to handle errors
import java.io.File; // Import the File class
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Comparator;

// MA= max(B+C)*MD*MT+MZ*MB
// E = B*MD + C*MT*a
public class Lab1_v2 implements Runnable {

    private int minSize = 100;
    private int maxSize = 1000;

    private int minFloat = 3;
    private int maxFloat = 9;

    private float a = 1.5f;
    private int m;
    private int b;

    private float[] B;
    private float[] C;
    private float[][] MD;
    private float[][] MT;
    private float[][] MZ;
    private float[][] MB;

    //глобальні значення результатів
    private float[][] NN;
    private float[][] MM;
    private float[][] KK;
    private float[][] LL;

    private long[][] timing;
    private long[][] timing2;
    private int i;

    FileWriter resultWriter;

    public void generateMatrix(float[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            generateVector(matrix[i]);
        }
    }

    public void generateVector(float[] vector) {
        Random r = new Random();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = minFloat + r.nextFloat() * (maxFloat - minFloat);
        }
    }

    public synchronized void syncWriter(String text) {
        try {
            resultWriter.write(text);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void generateInputData() {
        // generate sizes
        m = ThreadLocalRandom.current().nextInt(minSize, maxSize + 1);
        b = ThreadLocalRandom.current().nextInt(minSize, maxSize + 1);

        // generate vectors
        B = new float[m];
        generateVector(B);
        C = new float[m];
        generateVector(C);

        // generate matrices
        MD = new float[m][m];
        generateMatrix(MD);
        MT = new float[m][m];
        generateMatrix(MT);
        MZ = new float[m][b];
        generateMatrix(MZ);
        MB = new float[b][m];
        generateMatrix(MB);

        try {
            FileWriter myWriter = new FileWriter("input_data.txt");
            myWriter.write(Arrays.toString(B).toString() + "\n");
            myWriter.write(Arrays.toString(C).toString() + "\n");
            for (int i = 0; i < MD.length; i++) {
                myWriter.write(Arrays.toString(MD[i]).toString());
                if (i == MD.length - 1)
                    myWriter.write("\n");
                else
                    myWriter.write("\n");
            }
            for (int i = 0; i < MT.length; i++) {
                myWriter.write(Arrays.toString(MT[i]).toString());
                if (i == MT.length - 1)
                    myWriter.write("\n");
                else
                    myWriter.write("\n");
            }
            for (int i = 0; i < MZ.length; i++) {
                myWriter.write(Arrays.toString(MZ[i]).toString());
                if (i == MZ.length - 1)
                    myWriter.write("\n");
                else
                    myWriter.write("\n");
            }
            for (int i = 0; i < MB.length; i++) {
                myWriter.write(Arrays.toString(MB[i]).toString());
                if (i == MB.length - 1)
                    myWriter.write("\n");
                else
                    myWriter.write("\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void importInputData() {
        try {
            File file = new File("input_data.txt");
            Scanner myReader = new Scanner(file);
            int i = 0;

            while (myReader.hasNextLine()) {
                i++;
                String data = myReader.nextLine();
                data = data.substring(1, data.length() - 1);
                String[] nums = data.split(", ");
                if (i == 1) {
                    m = nums.length;
                    B = new float[m];
                    C = new float[m];
                    MD = new float[m][m];
                    MT = new float[m][m];
                    for (int j = 0; j < m; j++) {
                        B[j] = Float.valueOf(nums[j]);
                    }
                } else if (i == 2) {
                    for (int j = 0; j < m; j++) {
                        C[j] = Float.valueOf(nums[j]);
                    }
                } else if (i > 2 && i <= 2 + m) {
                    for (int j = 0; j < m; j++) {
                        MD[i - 3][j] = Float.valueOf(nums[j]);
                    }
                } else if (i > 2 + m && i <= 2 + 2 * m) {
                    for (int j = 0; j < m; j++) {
                        MT[i - 3 - m][j] = Float.valueOf(nums[j]);
                    }
                } else if (i > 2 + 2 * m && i <= 2 + 3 * m) {
                    b = nums.length;
                    if (i == 3 + 2 * m) {
                        MZ = new float[m][b];
                        MB = new float[b][m];
                    }
                    for (int j = 0; j < b; j++) {
                        MZ[i - 3 - 2 * m][j] = Float.valueOf(nums[j]);
                    }
                } else if (i > 2 + 3 * m) {
                    for (int j = 0; j < m; j++) {
                        MB[i - 3 - 3 * m][j] = Float.valueOf(nums[j]);
                    }
                }
            }
            myReader.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public float[] addTwoVectors(float[] A, float[] B) {
        float[] result = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            result[i] = addKahan(new float[] { A[i], B[i] });
        }
        return result;
    }

    public float[][] multiplyMatrices(float[][] A, float[][] B) {
        float[][] result = new float[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                result[i][j] = 0;
                for (int k = 0; k < A[i].length; k++) {
                    result[i][j] = addKahan(new float[] { result[i][j], A[i][k] * B[k][j] });
                }
            }
        }
        return result;
    }

    public float[][] multiplyMatrixByInteger(float[][] A, float a) {
        float[][] result = new float[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                result[i][j] = a * A[i][j];
            }
        }
        return result;
    }

    public float maxValue(float[] A) {
        float maxValue = A[0];
        for (int i = 1; i < A.length; i++) {
            maxValue = Math.max(maxValue, A[i]);
        }
        return maxValue;
    }

    public float[][] addMatrices(float[][] A, float[][] B) {
        float[][] result = new float[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                result[i][j] = addKahan(new float[] { A[i][j], B[i][j] });
            }
        }
        return result;
    }

    public void printMatrix(float[][] A, String name, long time) {
        Thread t = Thread.currentThread();
        String matrix = "\n" + t.getName() + ": " + name + "\n";
        if (time != 0) matrix += "Time, calculating: " + time + " nanoseconds\n" + name + "\n";
        for (int i = 0; i < A.length; i++) {
            matrix += Arrays.toString(A[i]) + "\n";
        }
        System.out.println(matrix);
        syncWriter(matrix);
    }

    public void printMatrix(long[][] A, String name, long time) {
        Thread t = Thread.currentThread();
        String matrix = "\n" + t.getName() + ": " + name + "\n";
        if (time != 0) matrix += "Time, calculating: " + time + " nanoseconds\n" + name + "\n";
        for (int i = 0; i < A.length; i++) {
            matrix += Arrays.toString(A[i]) + "\n";
        }
        System.out.println(matrix);
        syncWriter(matrix);
    }

    public float addKahan(float[] arr) {
        float sum = 0.0f;
        float err = 0.0f;
        for (float item : arr) {
            float y = item - err;
            float t = sum + y;
            err = (t - sum) - y;
            sum = t;
        }
        return sum;
    }

    public float[][] splitMatrix(float[][] matrix, int start, int n) {
        float[][] res = new float[matrix.length][n - start];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = start; j < n; j++) {
                res[i][j - start] = matrix[i][j];
            }
        }
        return res;

    }

    public float[][] calcFunc1() {
        float[] res1 = addTwoVectors(B, C);
        float res2 = maxValue(res1);
        float[][] res3 = multiplyMatrixByInteger(MD, res2);

        int n = (int)Math.ceil(MT.length/2);
        NN = new float[m][m];
        MM = new float[m][m];

        Thread t1 = new Thread()
        {
            public void run() {
                float[][] MT_part1 = splitMatrix(MT, 0, n);
                float[][] res4 = multiplyMatrices(res3, MT_part1);
                synchronized(NN) {
                    for (int i = 0; i < res4.length; i++) {
                        for (int j = 0; j < n; j++) {
                            NN[i][j] = res4[i][j];
                        }
                    }
                }

            }
        };

        Thread t2 = new Thread()
        {
            public void run() {
                float[][] MT_part2 = splitMatrix(MT, n, MT.length); 
                float[][] res4 = multiplyMatrices(res3, MT_part2);
                synchronized(NN) {
                    for (int i = 0; i < res4.length; i++) {
                        for (int j = 0; j < n; j++) {
                            NN[i][j + n] = res4[i][j];
                        }
                    }
                }
                
            }
        };

        Thread t3 = new Thread()
        {
            public void run() {
                MM = multiplyMatrices(MZ, MB);
            }
        };
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch ( InterruptedException е ) {
            System.out.println("Error!");
        }       
        
        return addMatrices(NN, MM);
    }

    public float[][] calcFunc2() {
        KK = new float[1][m];
        LL = new float[1][m];
        int n = (int)Math.ceil(MT.length/2);

        Thread t1 = new Thread()
        {
            public void run() {
                float[][] MD_part1 = splitMatrix(MD, 0, n);
                float[][] res1 = multiplyMatrices(new float[][] { B }, MD_part1);
                synchronized(KK) {
                    for (int i = 0; i < res1.length; i++) {
                        for (int j = 0; j < n; j++) {
                            KK[i][j] = res1[i][j];
                        }
                    }
                }
            }
        };

        Thread t2 = new Thread()
        {
            public void run() {
                float[][] MD_part2 = splitMatrix(MD, n, MD.length); 
                float[][] res2 = multiplyMatrices(new float[][] { B }, MD_part2);
                synchronized(KK) {
                    for (int i = 0; i < res2.length; i++) {
                        for (int j = 0; j < n; j++) {
                            KK[i][j + n] = res2[i][j];
                        }
                    }
                }
                
            }
        };

        Thread t3 = new Thread()
        {
            public void run() {
                LL = multiplyMatrices(new float[][] { C }, MT);
                LL = multiplyMatrixByInteger(LL, a);
            }
        };
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch ( InterruptedException е ) {
            System.out.println("Error!");
        }       
        
        return addMatrices(KK, LL);
    }

    public void run() {
        long startTime = System.nanoTime();
        float[][] MA = calcFunc1();
        long estimatedTime = System.nanoTime() - startTime;
        timing2[i][0] = m;
        timing2[i][1] = estimatedTime;
        printMatrix(MA, "MA", estimatedTime);
    }

    public Lab1_v2() {
        try {
            resultWriter = new FileWriter("result_v2.txt");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        importInputData();
        // generateInputData();
    }

    public static void main(String args[]) {
        Lab1_v2 lab1 = new Lab1_v2();

        lab1.timing = new long[100][2]; 
        lab1.timing2 = new long[100][2]; 

        for (int i = 0; i < 100; i++) {
            lab1.generateInputData();
            System.out.println("Згенерована розмірність матриці: " + lab1.m);

            lab1.importInputData();
            lab1.i = i;

            Thread t2 = new Thread(lab1);
            t2.start();

            long startTime = System.nanoTime();
            float[][] E = lab1.calcFunc2();
            long estimatedTime = System.nanoTime() - startTime;
            lab1.printMatrix(E, "E", estimatedTime);
            lab1.timing[i][0] = lab1.m;
            lab1.timing[i][1] = estimatedTime;
            
            try {
                t2.join();
            } catch ( InterruptedException е ) {
                System.out.println("Error!");
            }
        }

        Arrays.sort(lab1.timing, Comparator.comparingDouble(o -> o[0]));
        lab1.printMatrix(lab1.timing, "Timing", 0);

        Arrays.sort(lab1.timing2, Comparator.comparingDouble(o -> o[0]));
        lab1.printMatrix(lab1.timing2, "Timing", 0);

        String x = "";
        String y = "";
        String x2 = "";
        String y2 = "";
        for (int i = 0; i < lab1.timing.length; i++) {
            x += lab1.timing[i][0] + ", ";
            y += lab1.timing[i][1] + ", ";
            x2 += lab1.timing2[i][0] + ", ";
            y2 += lab1.timing2[i][1] + ", ";
        }

        lab1.syncWriter("x: " + x.substring(0, x.length() - 2) + "\n");
        lab1.syncWriter("y: " + y.substring(0, y.length() - 2) + "\n\n");
        lab1.syncWriter("x2: " + x2.substring(0, x2.length() - 2) + "\n");
        lab1.syncWriter("y2: " + y2.substring(0, y2.length() - 2));

        try {
            lab1.resultWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
