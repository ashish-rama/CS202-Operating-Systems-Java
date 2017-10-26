import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Everything for handling the FCFS scheduling algorithm
 */

public class FCFS {
	
	public static void main(String[] args) throws IOException {
		
		Scheduler FCFSScheduler = new Scheduler();
		Scanner input2 = null;

		//checking if the user wants verbose output or not
		//arguments of --verbose, input text file, and the random numbers text file
		if (args[0].equals("-v")) {
			FCFSScheduler.verbose = true;
			FCFSScheduler.populateProcessesLists(args[1]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[2])));
		} else {
			FCFSScheduler.populateProcessesLists(args[0]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[1])));
		}
		
		ArrayList<Process> processList = FCFSScheduler.processList;
		FCFSScheduler.sortProcessesByArrival(processList);

		Process process = null;
		int time = 0;

		//queue for processes that are ready
		ArrayList<Process> readyProcessesQueue = new ArrayList<Process>();

		FCFSScheduler.verbose(processList, time);

		//while the processes are not all terminated
		while(!FCFSScheduler.isDone(processList)) {

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
			if(!readyProcessesQueue.isEmpty()) {
				process = readyProcessesQueue.get(0);
				readyProcessesQueue.remove(0);

				process.setCurrentCPUBurst(FCFSScheduler.randomOS(input2.nextInt(), process.getCPUBurst()));

				//based on the lab requirements
				if (process.getCurrentCPUBurst() > process.getRemainingCPUTime()) {
					process.setCurrentCPUBurst(process.getRemainingCPUTime());
				}

				process.setCurrrentIOTime(process.getCurrentCPUBurst() * process.getIOBurst());
				process.setStatus(process.RUNNING);

				while (process.getCurrentCPUBurst() > 0) {
					time++;
					FCFSScheduler.verbose(processList, time);
					FCFSScheduler.addWaitTime(processList);
					boolean processBlocked = false;

					for (int i = 0; i < processList.size(); i++) {
						Process currentProcess = processList.get(i);
						if (currentProcess.getStatus() == currentProcess.BLOCKED) {
							processBlocked = true;
							currentProcess.setCurrrentIOTime(currentProcess.getCurrentIOTime() - 1);
							currentProcess.setTotalIOTime(currentProcess.getTotalIOTime() + 1);

							// incremement io time
							if (currentProcess.getCurrentIOTime() == 0) {
								// set the status to ready if io time is up
								currentProcess.setStatus(currentProcess.READY);
							}
						}

					}
					if (processBlocked) {
						FCFSScheduler.IOUtilization++;
					}

					//add to the queue created above
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
				FCFSScheduler.verbose(processList, time);
				FCFSScheduler.addWaitTime(processList);

				boolean processBlocked = false;

				//unblock the process and decrement the io time
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
					FCFSScheduler.IOUtilization++;
				}
			}

		}
		
		FCFSScheduler.setFinishTime(time);
		System.out.println("The scheduling algorithm used was First Come First Serve\n");
		FCFSScheduler.printProcessInformation();
		FCFSScheduler.printSummaryData();

	}

}