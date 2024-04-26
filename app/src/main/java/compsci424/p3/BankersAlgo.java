/**
 * Represents the Banker's Algorithm for resource allocation.
 * 
 * ------ NOT CURRENTLY MULTITHREADED ------------
 */
package compsci424.p3;

public class BankersAlgo {

    int numProcesses;
    int numResources;
    int[] availableResources;
    int[][] maxResources; // Maximum resources that can be allocated to processes
    int[][] allocation; // Resources currently allocated to processes
    int[][] need; // Resources needed by processes

    /**
     * Initializes a new instance of the BankersAlgo class.
     * 
     * @param numProcesses       The number of processes.
     * @param numResources       The number of resources.
     * @param availableResources The available resources.
     * @param max                The maximum resources that can be allocated to
     *                           processes.
     * @param allocation         The resources currently allocated to processes.
     */
    public BankersAlgo(int numProcesses, int numResources, int[] availableResources, int[][] max, int[][] allocation) {
        this.numProcesses = numProcesses;
        this.numResources = numResources;
        this.availableResources = availableResources;
        this.maxResources = max;
        this.allocation = allocation;
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
        boolean[] finish = new boolean[numProcesses]; // keep track of processes that have finished execution

        int[] work = new int[numResources]; // represents the available resources

        System.arraycopy(availableResources, 0, work, 0, numResources);

        while (true) { // keep checking for processes that can be allocated resources
            boolean found = false;

            for (int i = 0; i < numProcesses; i++) {
                if (!finish[i]) {
                    // Assume that we can allocate resources to the process
                    boolean canAllocate = true;
                    for (int j = 0; j < numResources; j++) { // Check if all the needed resources can be allocated
                        // If a needed resource cannot be allocated, set 'canAllocate' to false
                        if (need[i][j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }
                    if (canAllocate) { // If we can allocate all the needed resources
                        for (int j = 0; j < numResources; j++) {
                            work[j] += allocation[i][j];
                        }
                        finish[i] = true;
                        found = true; // Set 'found' to true as we found a process that can be allocated resources
                    }
                }
            }
            if (!found) { // If no process can be allocated resources
                break;
            }
        }
        for (boolean f : finish) { // Check if all processes have finished execution
            if (!f) { // If a process has not finished execution, return false
                return false;
            }
        }
        return true; // If all processes have finished execution, return true
    }

    /**
     * Represents the status of a resource request in the Banker's Algorithm.
     * The possible values are:
     * - GRANTED: The request has been granted.
     * - DENIED: The request has been denied.
     * - INVALID: The request is invalid.
     */
    public enum RequestStatus {
        GRANTED, DENIED, INVALID
    }

    /**
     * Requests the specified resources for the given process.
     * 
     * @param processIndex the index of the process
     * @param request      an array representing the resources to be requested
     * @return the status of the request
     */
    public RequestStatus requestResources(int processIndex, int[] request) {
        // Check if the request is valid
        for (int i = 0; i < numResources; i++) {
            if (request[i] > need[processIndex][i]) {
                return RequestStatus.INVALID;
            }
            if (request[i] > availableResources[i]) {
                return RequestStatus.INVALID;
            }
        }

        // Assume that the request is granted
        for (int i = 0; i < numResources; i++) {
            availableResources[i] -= request[i];
            allocation[processIndex][i] += request[i];
            need[processIndex][i] -= request[i];
        }

        // Check if the system is in a safe state
        if (isSafe()) {
            return RequestStatus.GRANTED;
        } else {
            // If the system is not in a safe state, revert the changes
            for (int i = 0; i < numResources; i++) {
                availableResources[i] += request[i];
                allocation[processIndex][i] -= request[i];
                need[processIndex][i] += request[i];
            }
            return RequestStatus.DENIED;
        }
    }

    /**
     * Releases the specified resources from the given process.
     *
     * @param processIndex the index of the process
     * @param release      an array representing the resources to be released
     */
    public boolean releaseResources(int processIndex, int[] release) {
        // Check if the release is valid
        for (int i = 0; i < numResources; i++) {
            if (release[i] < 0 || release[i] > allocation[processIndex][i]) {
                System.out.println("Error: Invalid release amount");
                return false; // Do not change the arrays
            }
        }

        // Release the resources
        for (int i = 0; i < numResources; i++) {
            availableResources[i] += release[i];
            allocation[processIndex][i] -= release[i];
            need[processIndex][i] += release[i];
        }
        return true;
    }
}
