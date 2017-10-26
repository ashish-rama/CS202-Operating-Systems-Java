import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This will help with the logic of all the scheduling for the algorithms
 */
public class Scheduler {

	//list of processes
	public static ArrayList<Process> processList = new ArrayList<Process>();
	//list of inputs
	public static ArrayList<Process> inputList = new ArrayList<Process>();

	//keep track of when it finishes
	public static int finishTime = 0;
	public static double IOUtilization = 0.0;
	public static double CPUUtilization = 0.0;

	//if we want verbose output or not (default is not)
	public static boolean verbose = false;

	public static void populateProcessesLists(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = null;
		String[] splitString = null;

		while (true) {
			if(line == null)
				break;
			line = line.replace("(", "");
			line = line.replace(")", "");
			splitString = line.split(" +");
		}

		//list of processes
		int numProcesses = Integer.parseInt(splitString[0]);
		int tracker = 1;
		// Original input
		System.out.print("The original list was: " + numProcesses);
		for (int i = 0; i < numProcesses; i++) {
			Process newProcess = new Process();
			newProcess.setArrivalTime(Integer.parseInt(splitString[tracker++]));
			System.out.print(" ( " + newProcess.getArrivalTime() + " ");
			newProcess.setCPUBurst(Integer.parseInt(splitString[tracker++]));
			System.out.print(newProcess.getCPUBurst() + " ");

			int processCPUTime = Integer.parseInt(splitString[tracker++]);
			newProcess.setRemainingCPUTime(processCPUTime);
			newProcess.setTotalCPUTime(processCPUTime);
			System.out.print(processCPUTime + " ");

			newProcess.setIOBurst(Integer.parseInt(splitString[tracker++]));
			System.out.print(newProcess.getIOBurst() + " )");
			processList.add(newProcess);
			inputList.add(newProcess);
		}
		System.out.println();

		//sort the inputs by their arrival times
		sortProcessesByArrival(inputList);

		System.out.print("The (sorted) input is: " + numProcesses);
		for (int i = 0; i < numProcesses; i++) {
			Process process = inputList.get(i);
			System.out.print(" ( " + process.getArrivalTime() + " " + process.getCPUBurst() + " " 
					+ process.getTotalCPUTime() + " " + process.getIOBurst() + " ) ");
		}

		System.out.println("\n");

		if (verbose) {
			System.out.println("The detailed printout gives the state and remaining burst for each process");
			System.out.println();
		}
		
		in.close();

	}


	public static void sortProcessesByArrival(ArrayList<Process> processes) {
		for (int i = 1; i < processes.size(); i++) {
			for (int j = i; j > 0; j--) {
				if (processes.get(j).getArrivalTime() < processes.get(j - 1).getArrivalTime()) {
					Process temp = processes.get(j);
					processes.set(j, processes.get(j - 1));
					processes.set(j - 1, temp);
				}

			}
		}
	}

	public static void printProcessInformation() {
		for (int i = 0; i < processList.size(); i++) {
			Process p = processList.get(i);
			System.out.println("Process " + i + ":");
			System.out.println("\t(A,B,C,M) = (" + p.getArrivalTime() + ", " + p.getCPUBurst() + ", "
					+ p.getTotalCPUTime() + ", " + p.getIOBurst() + ")");
			System.out.println("\tFinishing time: " + p.getFinishingTime());
			System.out.println("\tTurnaround time: " + p.getTurnAroundTime());
			System.out.println("\tI/O time: " + p.getTotalIOTime());
			System.out.println("\tWaiting time: " + p.getWaitTime());
			System.out.println();
		}
	}

	public static void printSummaryData() {
		double sumWaiting = 0.0;
		double sumTurnAround = 0.0;
		for (int i = 0; i < processList.size(); i++) {
			Process p = processList.get(i);
			sumWaiting += p.getWaitTime();
			sumTurnAround += p.getTurnAroundTime();
		}

		System.out.println("Summary Data");
		System.out.println("\tFinishing Time:  " + finishTime);
		System.out.printf("\tCPU Utilization: %.6f%n", CPUUtilization / finishTime);
		System.out.printf("\tI/O Utilization: %.6f%n", IOUtilization / finishTime);
		System.out.printf("\tThroughput: %.6f processes per hundred cycles%n", processList.size() / (finishTime / 100.0));
		System.out.printf("\tAverage Turnaround Time: %.6f%n", sumTurnAround / processList.size());
		System.out.printf("\tAverage Waiting Time: %.6f%n", sumWaiting / processList.size());
	}

	//To check if a process is done
	public static boolean isDone(ArrayList<Process> processList) {
		for (int i = 0; i < processList.size(); i++) {
			Process process = processList.get(i);
			if (process.getStatus() != process.COMPLETED) {
				return false;
			}
		}
		return true;
	}

	//gets a cpu burst time from the random numbers
	public static int randomOS(int X, int B) {
		return 1 + (X % B);
	}

	public static void setProcessOrder(ArrayList<Process> processes) {
		for (int i = 0; i < processes.size(); i++) {
			Process process = processes.get(i);
			process.setPriority(i);
		}
	}

	public static void sortProcessesByPriority(ArrayList<Process> processes) {
		for (int i = 1; i < processes.size(); i++) {
			for (int j = i; j > 0; j--) {
				if (processes.get(j).getPriority() < processes.get(j - 1).getPriority()) {
					Process temp = processes.get(j);
					processes.set(j, processes.get(j - 1));
					processes.set(j - 1, temp);
				}

			}
		}
	}

	public static void sortProcessesByRemCPUTime(ArrayList<Process> processes) {
		for (int i = 1; i < processes.size(); i++) {
			for (int j = i; j > 0; j--) {
				if (processes.get(j).getRemainingCPUTime() < processes.get(j - 1).getRemainingCPUTime()) {
					Process temp = processes.get(j);
					processes.set(j, processes.get(j - 1));
					processes.set(j - 1, temp);
				}
			}
		}
	}

	public static void addWaitTime(ArrayList<Process> processes) {
		for (int i = 0; i < processes.size(); i++) {
			Process process = processes.get(i);
			if (process.getStatus() == process.RUNNING) {
				CPUUtilization++;
			}
			if (process.getStatus() == process.READY) {
				process.setWaitTime(process.getWaitTime() + 1);
			}
		}

	}

	//print verbose information
	public static void verbose(ArrayList<Process> processes, int cycle) {
		if(verbose) {
			String status = "";
			int remaining = 0;
			String fullString = "";
			for (int i = 0; i < processes.size(); i++) {
				Process process = processes.get(i);
				if (process.getStatus() == 0) {
					status = "unstarted";
					remaining = 0;
				} else if (process.getStatus() == 1) {
					status = "running";
					remaining = process.getCurrentCPUBurst();
				} else if (process.getStatus() == 2) {
					status = "blocked";
					remaining = process.getCurrentIOTime();
				} else if (process.getStatus() == 3) {
					status = "ready";
					remaining = 0;
				} else if (process.getStatus() == 4) {
					status = "terminated";
					remaining = 0;
				}
				fullString += String.format("%15s%5s", status, remaining);
			}
			System.out.printf("Before cycle %3d:  %3s%n", cycle, fullString);
		}
	}

	public static int getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(int finishingTime) {
		this.finishTime = finishingTime;
	}

}