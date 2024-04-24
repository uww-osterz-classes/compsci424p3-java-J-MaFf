package compsci424.p3;

/**
 * Represents the Banker's Algorithm for resource allocation.
 */
public class BankersAlgo {

    private int numProcesses;
    private int numResources;
    private int[][] maxResources; // Maximum resources that can be allocated to processes
    private int[][] allocation; // Resources currently allocated to processes
    private int[][] need; // Resources needed by processes
    private int[] availableResources;

    /**
     * Initializes a new instance of the BankersAlgo class.
     * 
     * @param numProcesses       The number of processes.
     * @param numResources       The number of resources.
     * @param max                The maximum resources that can be allocated to
     *                           processes.
     * @param allocation         The resources currently allocated to processes.
     * @param availableResources The available resources.
     */
    public BankersAlgo(int numProcesses, int numResources, int[][] max, int[][] allocation, int[] availableResources) {
        this.numProcesses = numProcesses;
        this.numResources = numResources;
        this.maxResources = max;
        this.allocation = allocation;
        this.availableResources = availableResources;
        this.need = new int[numProcesses][numResources];
        calculateNeed();
    }

    /**
     * Calculates the need matrix by subtracting the allocation matrix from the
     * maximum matrix.
     */
    private void calculateNeed() {
        for (int processIndex = 0; processIndex < numProcesses; processIndex++) {
            for (int resourceIndex = 0; resourceIndex < numResources; resourceIndex++) {
                need[processIndex][resourceIndex] = maxResources[processIndex][resourceIndex]
                        - allocation[processIndex][resourceIndex];
            }
        }
    }

    /**
     * Checks if the current state of the system is safe using the Banker's
     * algorithm.
     * 
     * @return true if the system is in a safe state, false otherwise.
     */
    public boolean isSafe() {
        // Initialize a boolean array 'finish' to keep track of processes that have
        // finished execution
        boolean[] finish = new boolean[numProcesses];

        // Initialize an integer array 'work' to represent the available resources
        int[] work = new int[numResources];

        // Copy the available resources into the 'work' array
        System.arraycopy(availableResources, 0, work, 0, numResources);

        // Start an infinite loop to keep checking for processes that can be allocated
        // resources
        while (true) {
            // 'found' is a flag to check if we found a process that can be allocated
            // resources in the current iteration
            boolean found = false;

            // Iterate over all processes
            for (int i = 0; i < numProcesses; i++) {
                // If the process has not finished execution
                if (!finish[i]) {
                    // Assume that we can allocate resources to the process
                    boolean canAllocate = true;

                    // Check if all the needed resources can be allocated
                    for (int j = 0; j < numResources; j++) {
                        // If a needed resource cannot be allocated, set 'canAllocate' to false and
                        // break the loop
                        if (need[i][j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }

                    // If we can allocate all the needed resources
                    if (canAllocate) {
                        // Allocate the resources and update the 'work' array
                        for (int j = 0; j < numResources; j++) {
                            work[j] += allocation[i][j];
                        }
                        // Mark the process as finished
                        finish[i] = true;
                        // Set 'found' to true as we found a process that can be allocated resources
                        found = true;
                    }
                }
            }

            // If we did not find any process that can be allocated resources, break the
            // loop
            if (!found) {
                break;
            }
        }

        // Check if all processes have finished execution
        for (boolean f : finish) {
            // If a process has not finished execution, return false
            if (!f) {
                return false;
            }
        }

        // If all processes have finished execution, return true
        return true;
    }
}
