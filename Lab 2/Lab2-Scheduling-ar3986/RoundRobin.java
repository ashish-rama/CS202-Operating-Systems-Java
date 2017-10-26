import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Everything for handling the Round Robin scheduling algorithm with quantum = 2
 */

public class RoundRobin {
	
	public static void main(String[] args) throws IOException {
		
		Scheduler roundRobinScheduler = new Scheduler();
		int quantum = 2;
		Scanner input2 = null;
		
		//checking if the user wants verbose output or not
		//arguments of --verbose, input text file, and the random numbers text file
		if (args[0].equals("-v")) {
			roundRobinScheduler.verbose = true;
			roundRobinScheduler.populateProcessesLists(args[1]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[2])));
		} else {
			roundRobinScheduler.populateProcessesLists(args[0]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[1])));
		}

		ArrayList<Process> processList = roundRobinScheduler.processList;
		roundRobinScheduler.sortProcessesByArrival(processList);
		roundRobinScheduler.setProcessOrder(processList);

		Process process = null;
		int time = 0;
		int io = 0;
		ArrayList<Process> readyProcessesQueue = new ArrayList<Process>();

		roundRobinScheduler.verbose(processList, time);

		//while the processes are not all terminated
		while (!roundRobinScheduler.isDone(processList)) {

			//add processes to queue
			for (int i = 0; i < processList.size(); i++) {
				Process currentProcess = processList.get(i);
				if (!readyProcessesQueue.contains(currentProcess) && currentProcess.getArrivalTime() <= time
						&& (currentProcess.getStatus() == currentProcess.NOT_STARTED || currentProcess.getStatus() == currentProcess.READY)) {
					currentProcess.setStatus(currentProcess.READY);
					readyProcessesQueue.add(currentProcess);
				}

			}

			//if there are processes that can be run
			if (!readyProcessesQueue.isEmpty()) {

				quantum = 2;
				process = readyProcessesQueue.get(0);
				readyProcessesQueue.remove(0);

				if (process.getCurrentCPUBurst() == 0) {
					//set its CPU burst and IO burst
					process.setCurrentCPUBurst(roundRobinScheduler.randomOS(input2.nextInt(), process.getCPUBurst()));

					//based on the lab requirements
					if (process.getCurrentCPUBurst() > process.getRemainingCPUTime()) {
						process.setCurrentCPUBurst(process.getRemainingCPUTime());
					}
					process.setCurrrentIOTime(process.getCurrentCPUBurst() * process.getIOBurst());

				}

				process.setStatus(process.RUNNING);

				//run until burst is over
				while (quantum > 0) {
					time++;
					roundRobinScheduler.verbose(processList, time);
					roundRobinScheduler.addWaitTime(processList);

					boolean processBlocked = false;

					for (int i = 0; i < processList.size(); i++) {
						Process currentPro = processList.get(i);
						
						if (currentPro.getStatus() == currentPro.BLOCKED) {
							processBlocked = true;
							currentPro.setCurrrentIOTime(currentPro.getCurrentIOTime() - 1);
							currentPro.setTotalIOTime(currentPro.getTotalIOTime() + 1);

							
							if (currentPro.getCurrentIOTime() == 0) {
								currentPro.setStatus(currentPro.READY);
							}
						}
					}
					
					if (processBlocked) {
						roundRobinScheduler.IOUtilization++;
					}

					if (process.getRemainingCPUTime() > 0) {
						quantum = quantum - 1;
						process.setCurrentCPUBurst(process.getCurrentCPUBurst() - 1);
						process.setRemainingCPUTime(process.getRemainingCPUTime() - 1);
					}

					if (process.getRemainingCPUTime() == 0) {
						process.setStatus(process.COMPLETED);
						process.setFinishingTime(time);
						process.setTurnAroundTime(process.getFinishingTime() - process.getArrivalTime());
						break;
					}
					
					if (process.getCurrentCPUBurst() == 0) {
						process.setStatus(process.BLOCKED);
						break;
					}

					if (quantum > 0) {
						for (int i = 0; i < processList.size(); i++) {
							Process currentProcess = processList.get(i);
							if (!readyProcessesQueue.contains(currentProcess) && currentProcess.getArrivalTime() <= time
									&& (currentProcess.getStatus() == currentProcess.NOT_STARTED || currentProcess.getStatus() == currentProcess.READY)) {
								currentProcess.setStatus(currentProcess.READY);
								readyProcessesQueue.add(currentProcess);
							}
						}
					}
				}
				if (process.getStatus() == process.RUNNING) {
					process.setStatus(process.READY);
					quantum = 2;
				}
			} else {
				time++;
				roundRobinScheduler.verbose(processList, time);
				roundRobinScheduler.addWaitTime(processList);

				boolean processBlocked = false;
				for (int i = 0; i < processList.size(); i++) {
					Process currentProcess = processList.get(i);

					if (currentProcess.getStatus() == currentProcess.BLOCKED) {
						processBlocked = true;
						currentProcess.setCurrrentIOTime(currentProcess.getCurrentIOTime() - 1);
						currentProcess.setTotalIOTime(currentProcess.getTotalIOTime() + 1);
						if (currentProcess.getCurrentIOTime() == 0) {
							currentProcess.setStatus(currentProcess.READY);
						}
					}
				}
				
				if (processBlocked) {
					roundRobinScheduler.IOUtilization++;
				}
			}

		}
		roundRobinScheduler.setFinishTime(time);
		System.out.println("The scheduling algorithm used was Round Robin\n");
		roundRobinScheduler.printProcessInformation();
		roundRobinScheduler.printSummaryData();

	}

}