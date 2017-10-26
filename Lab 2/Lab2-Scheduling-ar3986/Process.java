
/**
 * Defines all the definitions of a process with all the getters and setters
 */

public class Process {
	
	//Listing all the possible states
	public final static int NOT_STARTED = 0;
	public final static int RUNNING = 1;
	public final static int BLOCKED = 2;
	public final static int READY = 3;
	public final static int COMPLETED = 4;

	//Things to track and later print
	private int turnAroundTime = 0;
	private int rank = 0;
	private int finishTime = 0;
	private int waitTime = 0;
	private int priority = 0;
	private int status = NOT_STARTED;
	private int totalCPUTime = 0;
	private int currCPUBurst = 0;
	private int ioBurst = 0;
	private int arrivalTime = 0;
	private int cpuBurst = 0;
	private int finishingTime = 0;
	private int remainingCPUTime = 0;
	private int totalIOTime = 0;
	private int currentIOTime = 0;

	public int getTurnAroundTime() {
		return turnAroundTime;
	}

	public void setTurnAroundTime(int turnAroundTime) {
		this.turnAroundTime = turnAroundTime;
	}

	public int getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(int endTime) {
		this.finishTime = endTime;
	}

	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public int getCurrentIOTime() {
		return currentIOTime;
	}

	public void setCurrrentIOTime(int currentIOTime) {
		this.currentIOTime = currentIOTime;
	}


	public void setTotalIOTime(int totalIOTime) {
		this.totalIOTime = totalIOTime;
	}

	public int getTotalCPUTime() {
		return totalCPUTime;
	}

	public void setTotalCPUTime(int totalCPUTime) {
		this.totalCPUTime = totalCPUTime;
	}

	public int getCurrentCPUBurst() {
		return currCPUBurst;
	}
	
	public void setCurrentCPUBurst(int currentcpuBurst) {
		this.currCPUBurst = currentcpuBurst;
	}
	
	public int getTotalIOTime() {
		return totalIOTime;
	}
	
	public int getRemainingCPUTime() {
		return remainingCPUTime;
	}

	public void setRemainingCPUTime(int remainingCPUTime) {
		this.remainingCPUTime = remainingCPUTime;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getCPUBurst() {
		return cpuBurst;
	}

	public void setCPUBurst(int cpuBurst) {
		this.cpuBurst = cpuBurst;
	}

	public int getIOBurst() {
		return ioBurst;
	}

	public void setIOBurst(int ioBurst) {
		this.ioBurst = ioBurst;
	}

	public int getFinishingTime() {
		return finishingTime;
	}

	public void setFinishingTime(int finishingTime) {
		this.finishingTime = finishingTime;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String toString() {
		return "( " + this.arrivalTime + " " + this.cpuBurst + " " + this.remainingCPUTime + " " + this.ioBurst + " ) "
				+ this.getStatus();
	}

}