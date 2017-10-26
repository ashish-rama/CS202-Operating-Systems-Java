import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Everything for handling the Uniprogrammed scheduling algorithm
 */

public class Uniprogrammed {
	
	public static void main(String[] args) throws IOException {
		
		Scheduler uniprogrammedScheduler = new Scheduler();
		Scanner input2 = null;
		
		//checking if the user wants verbose output or not
		//arguments of --verbose, input text file, and the random numbers text file
		if (args[0].equals("-v")) {
			uniprogrammedScheduler.verbose = true;
			uniprogrammedScheduler.populateProcessesLists(args[1]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[2])));

		} else {
			uniprogrammedScheduler.populateProcessesLists(args[0]);
			input2 = new Scanner(new BufferedReader(new FileReader(args[1])));

		}

		ArrayList<Process> processList = uniprogrammedScheduler.processList;
		uniprogrammedScheduler.sortProcessesByArrival(processList);
		Process process = null;
		int time = 0;
		ArrayList<Process> readyProcessesQueue = new ArrayList<Process>();

		uniprogrammedScheduler.verbose(processList, time);
		uniprogrammedScheduler.addWaitTime(processList);

		//while the processes are not all terminated
		while (!uniprogrammedScheduler.isDone(processList)) {

			//add processes to queue
			for (int i = 0; i < processList.size(); i++) {
				Process t = processList.get(i);
				if (!readyProcessesQueue.contains(t) && t.getArrivalTime() <= time
						&& (t.getStatus() == t.NOT_STARTED || t.getStatus() == t.READY)) {
					t.setStatus(t.READY);
					readyProcessesQueue.add(t);
				}
			}

			//if there are processes that can be run
			if (!readyProcessesQueue.isEmpty()) {

				process = readyProcessesQueue.get(0);
				readyProcessesQueue.remove(0);

				//set its CPU burst and IO burst
				process.setCurrentCPUBurst(uniprogrammedScheduler.randomOS(input2.nextInt(), process.getCPUBurst()));

				//based on the lab requirements
				if (process.getCurrentCPUBurst() > process.getRemainingCPUTime()) {
					process.setCurrentCPUBurst(process.getRemainingCPUTime());
				}

				process.setCurrrentIOTime(process.getCurrentCPUBurst() * process.getIOBurst());
				process.setStatus(process.RUNNING);

				while (process.getRemainingCPUTime() > 0) {
					//add to queue
					for (int i = 0; i < processList.size(); i++) {
						Process currentProcess = processList.get(i);
						if (!readyProcessesQueue.contains(currentProcess) && currentProcess.getArrivalTime() <= time && (currentProcess.getStatus() == currentProcess.NOT_STARTED)) {
							currentProcess.setStatus(currentProcess.READY);
							readyProcessesQueue.add(currentProcess);
						}
					}

					if (process.getCurrentCPUBurst() > 0) {
						time++;
						uniprogrammedScheduler.verbose(processList, time);
						uniprogrammedScheduler.addWaitTime(processList);
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
						while (process.getCurrentIOTime() > 0) {
							uniprogrammedScheduler.IOUtilization++;
							for (int i = 0; i < processList.size(); i++) {
								Process currentProcess = processList.get(i);
								if (!readyProcessesQueue.contains(currentProcess) && currentProcess.getArrivalTime() <= time
										&& (currentProcess.getStatus() == currentProcess.NOT_STARTED)) {
									currentProcess.setStatus(currentProcess.READY);
									readyProcessesQueue.add(currentProcess);
								}
							}
							time++;

							uniprogrammedScheduler.verbose(processList, time);
							uniprogrammedScheduler.addWaitTime(processList);
							process.setCurrrentIOTime(process.getCurrentIOTime() - 1);
							process.setTotalIOTime(process.getTotalIOTime() + 1);

						}
						process.setCurrentCPUBurst(uniprogrammedScheduler.randomOS(input2.nextInt(), process.getCPUBurst()));

					}
					if (process.getCurrentIOTime() == 0) {
						process.setCurrrentIOTime(process.getCurrentCPUBurst() * process.getIOBurst());
						process.setStatus(process.RUNNING);
					}
				}
			}

		}

		uniprogrammedScheduler.setFinishTime(time);
		System.out.println("The scheduling algorithm used was Uniprocessing\n");
		uniprogrammedScheduler.printProcessInformation();
		uniprogrammedScheduler.printSummaryData();

	}

}