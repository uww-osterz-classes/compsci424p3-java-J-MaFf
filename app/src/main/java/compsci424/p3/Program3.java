/* COMPSCI 424 Program 3
 * Name: Joey Maffiola
 */

package compsci424.p3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import compsci424.p3.BankersAlgo.RequestStatus;

import java.util.Random;

public class Program3 {
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

    /**
     * Enters the manual mode where the user can input commands to interact with the
     * program.
     * The user can enter commands to request or release resources for a specific
     * process.
     * The method reads the user's input, parses the command, and performs the
     * corresponding action.
     * The manual mode continues until the user enters the "end" command to exit the
     * program.
     */
    public static void manualMode() {
        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            System.out.print("Enter command: ");
            command = scanner.nextLine();
            String[] parts = command.split(" ");
            if (parts[0].equalsIgnoreCase("end")) {
                System.out.println("Exiting program...");
                break;
            } else if (parts[0].equalsIgnoreCase("request")) {
                int numUnits = Integer.parseInt(parts[1]);
                int resourceType = Integer.parseInt(parts[3]);
                int processId = Integer.parseInt(parts[5]);
                int[] request = new int[numResources];
                request[resourceType] = numUnits;
                RequestStatus status = bankersAlgo.requestResources(processId, request);
                output(processId, request, status, true);
            } else if (parts[0].equalsIgnoreCase("release")) {
                int numUnits = Integer.parseInt(parts[1]);
                int resourceType = Integer.parseInt(parts[3]);
                int processId = Integer.parseInt(parts[5]);
                int[] release = new int[numResources];
                release[resourceType] = numUnits;
                bankersAlgo.releaseResources(processId, release);
                output(processId, release, RequestStatus.GRANTED, false);
            } else {
                System.out.println("Invalid command. Please enter a valid command.");
            }
        }
        scanner.close();
    }

    private static void autoMode() {
        Thread[] threads = new Thread[numProcesses];

        for (int processID = 0; processID < numProcesses; processID++) {
            int pID = processID;
            threads[pID] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 3; j++) {
                        // Generate random request

                        int[] request = generateRandomRequest(pID);
                        synchronized (bankersAlgo) {
                            RequestStatus status = bankersAlgo.requestResources(pID, request);
                            output(pID, request, status, true);
                            // if (status == RequestStatus.GRANTED) {
                            // System.out.println("Process " + pID + " has been granted resources.");
                            // } else if (status == RequestStatus.DENIED) {
                            // System.out.println("Process " + pID + " has been denied resources.");
                            // } else {
                            // System.out.println("Process " + pID + " has made an invalid request.");
                            // }
                        }

                        // Generate random release
                        int[] release = generateRandomRelease(pID);
                        synchronized (bankersAlgo) {
                            bankersAlgo.releaseResources(pID, release);
                            output(pID, release, RequestStatus.GRANTED, false);
                        }
                    }
                }
            });

            threads[pID].start();
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

    /**
     * Generates a random release array for a given process ID.
     *
     * @param pID the process ID
     * @return an array of randomly generated release values
     */
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

    public static void output(int processIndex, int[] requestOrRelease, RequestStatus status, boolean isRequest) {
        String action = isRequest ? "requests" : "releases";
        String result = "";
        switch (status) {
            case GRANTED:
                result = isRequest ? "granted" : "completed";
                break;
            case DENIED:
                result = "denied";
                break;
            case INVALID:
                result = "invalid";
                break;
        }
        for (int i = 0; i < requestOrRelease.length; i++) {
            if (requestOrRelease[i] > 0) {
                System.out.println("Process " + processIndex + " " + action + " " + requestOrRelease[i]
                        + " units of resource " + i + ": " + result);
            }
        }
    }

}
