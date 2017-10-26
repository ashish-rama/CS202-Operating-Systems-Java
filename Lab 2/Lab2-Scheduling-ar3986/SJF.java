import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Everything for handling the SJF scheduling algorithm
 */

public class SJF {
	
	public static void main(String[] args) throws IOException {
		
		Scheduler SJFScheduler = new Scheduler();
		Scanner input2 = null;
		
		//checking if the user wants verbose output or not
		//arguments of --verbose, input text file, and the random numbers text file
		if (args[0].equals("-v")) {
			SJFScheduler.verbose = true;
			SJFScheduler.populateProcessesLists(args[1]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[2])));
		} else {
			SJFScheduler.populateProcessesLists(args[0]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[1])));

		}

		ArrayList<Process> processList = SJFScheduler.processList;
		SJFScheduler.sortProcessesByArrival(processList);

		Process process = null;
		int time = 0;
		ArrayList<Process> readyProcessesQueue = new ArrayList<Process>();

		SJFScheduler.verbose(processList, time);

		//while the processes are not all terminated
		while (!SJFScheduler.isDone(processList)) {

			//add processes to queue
			for (int i = 0; i < processList.size(); i++) {
				Process t = processList.get(i);
				if (!readyProcessesQueue.contains(t) && t.getArrivalTime() <= time
						&& (t.getStatus() == t.NOT_STARTED || t.getStatus() == t.READY)) {
					t.setStatus(t.READY);
					readyProcessesQueue.add(t);
				}
			}

			// sort by cpu time
			SJFScheduler.sortProcessesByRemCPUTime(readyProcessesQueue);

			//if there are processes that can be run
			if (!readyProcessesQueue.isEmpty()) {
				process = readyProcessesQueue.get(0);
				readyProcessesQueue.remove(0);

				//set its CPU burst and IO burst
				process.setCurrentCPUBurst(SJFScheduler.randomOS(input2.nextInt(), process.getCPUBurst()));

				//based on the lab requirements
				if (process.getCurrentCPUBurst() > process.getRemainingCPUTime()) {
					process.setCurrentCPUBurst(process.getRemainingCPUTime());
				}

				//set io 
				process.setCurrrentIOTime(process.getCurrentCPUBurst() * process.getIOBurst());
				process.setStatus(process.RUNNING);

				//run until burst is over
				while (process.getCurrentCPUBurst() > 0) {
					time++;
					SJFScheduler.verbose(processList, time);
					SJFScheduler.addWaitTime(processList);
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
						SJFScheduler.IOUtilization++;
					}

					//add to the queue
					for (int i = 0; i < processList.size(); i++) {
						Process currentProcess = processList.get(i);
						if (!readyProcessesQueue.contains(currentProcess) && currentProcess.getArrivalTime() <= time
								&& (currentProcess.getStatus() == currentProcess.NOT_STARTED || currentProcess.getStatus() == currentProcess.READY)) {
							currentProcess.setStatus(currentProcess.READY);
							readyProcessesQueue.add(currentProcess);
						}
					}

					if (process.getRemainingCPUTime() > 0) {
						process.setCurrentCPUBurst(process.getCurrentCPUBurst() - 1);
						process.setRemainingCPUTime(process.getRemainingCPUTime() - 1);
						if (process.getRemainingCPUTime() == 0) {
							process.setStatus(process.COMPLETED);
							process.setFinishingTime(time);
							process.setTurnAroundTime(process.getFinishingTime() - process.getArrivalTime());
							break;
						}
					}
				}

				if (process.getStatus() == process.RUNNING)
					process.setStatus(process.BLOCKED);

			} else {
				time++;
				SJFScheduler.verbose(processList, time);
				SJFScheduler.addWaitTime(processList);
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
					SJFScheduler.IOUtilization++;
				}
			}

		}
		SJFScheduler.setFinishTime(time);
		System.out.println("The scheduling algorithm used was Shortest Job First\n");
		SJFScheduler.printProcessInformation();
		SJFScheduler.printSummaryData();

	}

}