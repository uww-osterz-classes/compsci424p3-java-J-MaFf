/* COMPSCI 424 Program 3
 * Name: Joey Maffiola
 * 
 * This is a template. Program3.java *must* contain the main class
 * for this program. 
 * 
 * You will need to add other classes to complete the program, but
 * there's more than one way to do this. Create a class structure
 * that works for you. Add any classes, methods, and data structures
 * that you need to solve the problem and display your solution in the
 * correct format.
 */

package compsci424.p3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Random;

/**
 * Main class for this program. To help you get started, the major
 * steps for the main program are shown as comments in the main
 * method. Feel free to add more comments to help you understand
 * your code, or for any reason. Also feel free to edit this
 * comment to be more helpful.
 */
public class Program3 {
    // Declare any class/instance variables that you need here.
    public static BankersAlgo bankersAlgo;
    public static int numResources, numProcesses;

    /**
     * @param args Command-line arguments.
     * 
     *             args[0] should be a string, either "manual" or "auto".
     * 
     *             args[1] should be another string: the path to the setup file
     *             that will be used to initialize your program's data structures.
     *             To avoid having to use full paths, put your setup files in the
     *             top-level directory of this repository.
     *             - For Test Case 1, use "424-p3-test1.txt".
     *             - For Test Case 2, use "424-p3-test2.txt".
     */
    public static void main(String[] args) {
        // Code to test command-line argument processing.
        // You can keep, modify, or remove this. It's not required.
        if (args.length < 2) {
            System.err.println("Not enough command-line arguments provided, exiting.");
            return;
        }
        System.out.println("Selected mode: " + args[0]);
        System.out.println("Setup file location: " + args[1]);

        // 1. Open the setup file using the path in args[1]
        String currentLine;
        BufferedReader setupFileReader;
        try {
            setupFileReader = new BufferedReader(new FileReader(args[1]));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find setup file at " + args[1] + ", exiting.");
            return;
        }

        // 2. Get the number of resources and processes from the setup
        // file, and use this info to create the Banker's Algorithm
        // data structures

        // For simplicity's sake, we'll use one try block to handle
        // possible exceptions for all code that reads the setup file.
        try {
            // Get number of resources
            currentLine = setupFileReader.readLine();
            if (currentLine == null) {
                System.err.println("Cannot find number of resources, exiting.");
                setupFileReader.close();
                return;
            } else {
                numResources = Integer.parseInt(currentLine.split(" ")[0]);
                System.out.println(numResources + " resources");
            }

            // Get number of processes
            currentLine = setupFileReader.readLine();
            if (currentLine == null) {
                System.err.println("Cannot find number of processes, exiting.");
                setupFileReader.close();
                return;
            } else {
                numProcesses = Integer.parseInt(currentLine.split(" ")[0]);
                System.out.println(numProcesses + " processes");
            }

            // Create the Banker's Algorithm data structures, in any
            // way you like as long as they have the correct size

            // 3. Use the rest of the setup file to initialize the
            // data structures

            int[] availableResources = readAvailableResources(numProcesses, numResources, setupFileReader);
            int[][] maxResources = readMaxResources(numProcesses, numResources, setupFileReader);
            int[][] allocation = readAllocation(numProcesses, numResources, setupFileReader);

            setupFileReader.close(); // done reading the file, so close it

            bankersAlgo = new BankersAlgo(numProcesses, numResources, availableResources, maxResources,
                    allocation);
        } catch (IOException e) {
            System.err.println("Something went wrong while reading setup file "
                    + args[1] + ". Stack trace follows. Exiting.");
            e.printStackTrace(System.err);
            System.err.println("Exiting.");
            return;
        }

        // 4. Check initial conditions to ensure that the system is
        // beginning in a safe state: see "Check initial conditions"
        // in the Program 3 instructions

        if (!bankersAlgo.isSafe()) {
            System.err.println("Initial conditions are not safe.");
            System.exit(-1); // exit with an error code
        }

        // 5. Go into either manual or automatic mode, depending on
        // the value of args[0]; you could implement these two modes
        // as separate methods within this class, as separate classes
        // with their own main methods, or as additional code within
        // this main method.
        while (true) {
            if (args[0].equalsIgnoreCase("Manual")) {
                manualMode();
                break;
            } else if (args[0].equalsIgnoreCase("Auto") || args[0].equalsIgnoreCase("Automatic")) {
                autoMode();
                break;
            } else {
                System.out.println("Mode '" + args[0]
                        + "' is not recognized. Please choose from 'Manual' or 'Auto / Automatic' modes.");
                Scanner scanner = new Scanner(System.in);
                args[0] = scanner.nextLine();
                scanner.close();
            }
        }

    }

    /**
     * Reads the available resources from the setup file.
     *
     * @param numProcesses    the number of processes
     * @param numResources    the number of resources
     * @param setupFileReader the BufferedReader object used to read the setup file
     * @return an array of integers representing the available resources
     * @throws IOException if an I/O error occurs while reading the setup file
     */
    private static int[] readAvailableResources(int numProcesses, int numResources, BufferedReader setupFileReader)
            throws IOException {
        String line = setupFileReader.readLine();
        if (!line.equals("Available")) {
            throw new IOException(
                    "Current line is not 'Available', which means the file format is wrong or another error has happened.");
        }
        line = setupFileReader.readLine(); // read the next line (the actual data)
        String[] text = line.split(" ");
        int[] parts = new int[numResources];
        for (int i = 0; i < numResources; i++) { // convert the string array to an int array
            parts[i] = Integer.parseInt(text[i]);
        }
        return parts;
    }

    /**
     * Reads the maximum resources for each process from the setup file.
     *
     * @param numProcesses    the number of processes
     * @param numResources    the number of resources
     * @param setupFileReader the BufferedReader used to read the setup file
     * @return a 2D array representing the maximum resources for each process
     * @throws IOException if an I/O error occurs while reading the setup file
     */
    private static int[][] readMaxResources(int numProcesses, int numResources, BufferedReader setupFileReader)
            throws IOException {
        String line = setupFileReader.readLine();
        if (!line.equals("Max")) {
            throw new IOException(
                    "Current line is not 'Max', which means the file format is wrong or another error has happened.");
        }
        int[][] maxResources = new int[numProcesses][numResources];
        for (int i = 0; i < numProcesses; i++) {
            line = setupFileReader.readLine();
            String[] text = line.split(" ");
            int[] parts = new int[numResources];
            for (int j = 0; j < numResources; j++) {
                parts[j] = Integer.parseInt(text[j]);
            } // At this point, you have the max resources for process i
            maxResources[i] = parts;
        }
        return maxResources;

    }

    /**
     * Reads the allocation of resources for each process from the setup file.
     *
     * @param numProcesses    the number of processes
     * @param numResources    the number of resources
     * @param setupFileReader the BufferedReader used to read the setup file
     * @return a 2D array representing the allocated resources for each process
     * @throws IOException if an I/O error occurs while reading the setup file
     */
    private static int[][] readAllocation(int numProcesses, int numResources, BufferedReader setupFileReader)
            throws IOException {
        String line = setupFileReader.readLine();
        if (!line.equals("Allocation")) {
            throw new IOException(
                    "Current line is not 'Allocation', which means the file format is wrong or another error has happened.");
        }
        int[][] allocatedResources = new int[numProcesses][numResources];
        for (int i = 0; i < numProcesses; i++) {
            line = setupFileReader.readLine();
            String[] text = line.split(" ");
            int[] parts = new int[numResources];
            for (int j = 0; j < numResources; j++) {
                parts[j] = Integer.parseInt(text[j]);
            } // At this point, you have the max resources for process i
            allocatedResources[i] = parts;
        }
        return allocatedResources;
    }

    private static void manualMode() {

    }

    private static void autoMode() {
        Thread[] threads = new Thread[numProcesses];

        for (int processID = 0; processID < numProcesses; processID++) {
            threads[processID] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 3; j++) {
                        // Generate random request
                        int[] request = generateRandomRequest(processID);
                        synchronized (bankersAlgo) {
                            bankersAlgo.requestResources(request);
                        }

                        // Generate random release
                        int[] release = generateRandomRelease();
                        synchronized (bankersAlgo) {
                            bankersAlgo.releaseResources(release);
                        }
                    }
                }
            });

            threads[processID].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates a random resource request for a given process ID.
     *
     * @param pID the process ID for which the resource request is generated
     * @return an array representing the randomly generated resource request
     */
    private static int[] generateRandomRequest(int pID) {
        Random random = new Random();
        int[] request = new int[numResources];

        for (int resourceId = 0; resourceId < numResources; resourceId++) {
            int maxRequest = bankersAlgo.maxResources[pID][resourceId]; // NOt sure
            // Generate a random number between 0 and maxRequest
            request[resourceId] = random.nextInt(maxRequest + 1);
        }
        return request;
    }

    private static int[] generateRandomRelease(int pID) {
        Random random = new Random();
        int[] release = new int[numResources];

        for (int resourceId = 0; resourceId < numResources; resourceId++) {
            int allocation = bankersAlgo.allocation[pID][resourceId];
            // Generate a random number between 0 and allocation
            release[resourceId] = random.nextInt(allocation + 1);
        }

        return release;
    }

    private static void output() {

    }

}
