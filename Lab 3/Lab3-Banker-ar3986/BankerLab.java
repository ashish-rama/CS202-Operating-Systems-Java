import java.io.IOException;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

/** 
 * Lab 3 Bankers Lab by Ashish Ramachandran (ar3986)
 */
public class BankerLab {

	public static void main (String[] args) throws IOException { 
		//take input file
		String inputFile = new String(readAllBytes(get(args[0])));
		StringTokenizer tokenizer = new StringTokenizer(inputFile);
		
		//initialize variables to keep track of tasks
		int numTasks = Integer.parseInt(tokenizer.nextToken());
		int numResources = Integer.parseInt(tokenizer.nextToken());
		int[] availableResources = new int[numResources];
		int[] releasedResources = new int[numResources];
		ArrayList<ArrayList<String[]>> processTaskLine= new ArrayList<ArrayList<String[]>>(numTasks);
		int terminatedTasks = 0;
		HashMap<Integer, int[]> tasks = new HashMap<Integer, int[]>(numTasks);
		Integer[] taskCycles = new Integer[numTasks], 
				taskWaits = new Integer[numTasks], taskDelays = new Integer[numTasks];
		Integer[][] claims = new Integer[numTasks][numResources], 
				originalClaim = new Integer[numTasks][numResources];
		ArrayList<Integer> runningQueue = new ArrayList<Integer>(numTasks);
		int totalRunTime = 0; 
		int totalWaitTime = 0;
		int tasksLeft = 0;
		
		//initialize all data structures to avoid mistakes
		Arrays.fill(taskCycles, 0);
		Arrays.fill(taskWaits, 0);
		Arrays.fill(taskDelays, 0);
		for(Integer[] temp: claims)
			Arrays.fill(temp, 0);
		for (int i = 0; i < numResources; i++)
			availableResources[i] = Integer.parseInt(tokenizer.nextToken());
		Arrays.fill(releasedResources, 0);
		for(int i = 0; i < numTasks; i++)
			processTaskLine.add(new ArrayList<String[]>());

		//read input file and store task info
		while(tokenizer.hasMoreTokens()) {     
			String activity = tokenizer.nextToken();
			int taskId = Integer.parseInt(tokenizer.nextToken());
			processTaskLine.get(taskId - 1).add(new String[]{ activity, 
					tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken()});
		} 

		
		
		
		
		//FIFO Algorithm
		int deadlock = 0;
		int deadlockIteration = 0;
		
		//while not all the tasks are terminated
		while(terminatedTasks != numTasks) {
			deadlockIteration = 0;
			tasksLeft = numTasks - terminatedTasks;

			//check if deadlock: if all tasks are blocked
			if(deadlock == tasksLeft) {
				deadlockIteration = 1;
				for(int task = 0; task < numTasks; task++) { 
					//if task is not aborted or terminated, then abort and release all resources and terminate
					if(!(processTaskLine.get(task).isEmpty())) {
						taskCycles[task] = null;
						for(int resource = 0; resource < numResources; resource++) {
							availableResources[resource] += tasks.get(task)[resource]; 
							tasks.get(task)[resource] = 0;
						}
						processTaskLine.get(task).clear();
						terminatedTasks++;
						break; 
					}
				}
			}
			deadlock = 0; 
			
			//go through the tasks
			for(int task = 0;task < numTasks; task++) {
				//add previously aborted or terminated tasks back into order queue
				if(!runningQueue.contains(task) && !(processTaskLine.get(task).isEmpty()))
					runningQueue.add(task);
			}

			//go through remaining tasks
			tasksLeft = numTasks - terminatedTasks;
			for(int task = 0; task < tasksLeft; task++) {
				int taskId = runningQueue.get(0);
				if(!(processTaskLine.get(taskId).isEmpty())) {
					String activity = processTaskLine.get(taskId).get(0)[0];
					int delay = Integer.parseInt(processTaskLine.get(taskId).get(0)[1]);
					int resourceRequest = Integer.parseInt(processTaskLine.get(taskId).get(0)[2]);
					int claim = Integer.parseInt(processTaskLine.get(taskId).get(0)[3]); 

					//if delayed
					if(delay != taskDelays[taskId]) {
						taskDelays[taskId]++;
						taskCycles[taskId]++;
					} 
					//if not delayed run like normal
					else {
						taskDelays[taskId] = 0;
						if(activity.equalsIgnoreCase("initiate")) {
							tasks.put(taskId, new int[numResources]);
							tasks.get(taskId)[resourceRequest - 1] = 0; 
							taskCycles[taskId]++;
							processTaskLine.get(taskId).remove(0);
						} else if(activity.equalsIgnoreCase("request")) {
							//if can't grant the request, then make it wait
							if(claim > availableResources[resourceRequest - 1]) {
								if (deadlockIteration == 0) {
									taskCycles[taskId]++;
									taskWaits[taskId]++;
								}
								runningQueue.add(taskId);
								deadlock++;
							} 
							//if it CAN grant the request
							else {
								tasks.get(taskId)[resourceRequest - 1] += claim;
								availableResources[resourceRequest-1] -= claim;
								taskCycles[taskId]++;
								processTaskLine.get(taskId).remove(0);
							}
						} else if(activity.equalsIgnoreCase("release")) {
							releasedResources[resourceRequest - 1] += claim; 
							tasks.get(taskId)[resourceRequest - 1] -= claim;
							taskCycles[taskId]++;
							processTaskLine.get(taskId).remove(0);
						} else if(activity.equals("terminate")) {
							terminatedTasks++;
							processTaskLine.get(taskId).remove(0);
						}   
					}
				} else {
					//fixes an off by one error adding it back to the queue
					task--;
				}
				runningQueue.remove(0);
			}

			//make all resources available again
			for (int resource = 0; resource < numResources; resource++) {
				availableResources[resource] += releasedResources[resource]; 
			}
			Arrays.fill(releasedResources, 0);
		} 

		System.out.println("FIFO Algorithm: ");
		int k = 0;
		for(Integer cycle: taskCycles) {
			System.out.print("Task " + (k + 1) + ": ");
			if(cycle != null) {
				System.out.println(cycle + " " + taskWaits[k] + " " +  Math.round(taskWaits[k]/(double)cycle * 100) + "%");
				totalRunTime += cycle;
				totalWaitTime += taskWaits[k];
			} else 
				System.out.println("aborted  ");
			k++;
		} 
		System.out.println("Total: " + totalRunTime + " " + totalWaitTime + " " + Math.round(totalWaitTime/(double)totalRunTime * 100) + "%\n");

		//end of FIFO Algorithm
		
		
		
		//reset all info from input file again
		totalRunTime = 0;
		totalWaitTime = 0;
		tokenizer = new StringTokenizer(inputFile);
		numTasks = Integer.parseInt(tokenizer.nextToken());
		numResources = Integer.parseInt(tokenizer.nextToken());

		for(int i = 0; i < numResources; i++) {
			availableResources[i] = Integer.parseInt(tokenizer.nextToken());
		}
		Arrays.fill(releasedResources, 0);

		processTaskLine.clear();
		for(int i = 0; i < numTasks; i++) {
			processTaskLine.add(new ArrayList<String[]>());
		}

		terminatedTasks = 0;

		tasks.clear();
		runningQueue.clear();
		
		Arrays.fill(taskCycles, 0);
		Arrays.fill(taskWaits, 0);
		Arrays.fill(taskDelays, 0);
		for(Integer[] temp: claims)
			Arrays.fill(temp, 0);
		for(Integer[] temp: claims)
			Arrays.fill(temp, 0);

		//read input file and store task info
		while(tokenizer.hasMoreTokens()) {     
			String activity = tokenizer.nextToken();
			int taskId = Integer.parseInt(tokenizer.nextToken());
			processTaskLine.get(taskId - 1).add(new String[]{ activity,
					tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken()});
		} 

		
		
		
		//Bankers Algorithm
		int safe = 0;
		
		//while not all the tasks are terminated
		while(terminatedTasks != numTasks) {
			//go through the tasks
			for(int task = 0;task < numTasks; task++) {
				if(!runningQueue.contains(task) && !(processTaskLine.get(task).isEmpty()))
					runningQueue.add(task);
			}

			tasksLeft = numTasks - terminatedTasks;
			for(int task = 0; task < tasksLeft; task++) {
				int taskId = runningQueue.get(0);
				if(!(processTaskLine.get(taskId).isEmpty())) {
					String activity = processTaskLine.get(taskId).get(0)[0];
					int delay = Integer.parseInt(processTaskLine.get(taskId).get(0)[1]);
					int resourceRequest = Integer.parseInt(processTaskLine.get(taskId).get(0)[2]);
					int claim = Integer.parseInt(processTaskLine.get(taskId).get(0)[3]); 

					//if delayed
					if(delay != taskDelays[taskId]) {
						taskDelays[taskId]++;
						taskCycles[taskId]++;
					} 
					//if not delayed run like normal
					else {
						if(activity.equalsIgnoreCase("initiate")) {
							//if it is claiming more than the available resources abort
							if (claim > availableResources[resourceRequest - 1]) {
								System.out.println("Banker aborts task " + (taskId + 1) + " before run begins:"
										+ "\n\tClaim for resource " + resourceRequest
										+ " (" + claim + ") exceeds number of units present (" + availableResources[resourceRequest-1] + ")");
								taskCycles[taskId] = null; 
								processTaskLine.get(taskId).clear();
								terminatedTasks++;
							} 
							//else initiate
							else {
								taskDelays[taskId] = 0;
								tasks.put(taskId, new int[numResources]);
								tasks.get(taskId)[resourceRequest - 1] = 0; 
								taskCycles[taskId]++;
								processTaskLine.get(taskId).remove(0);
								//purpose is to better keep track of the claims
								claims[taskId][resourceRequest - 1] = -claim;
								originalClaim[taskId][resourceRequest - 1] = claim;
							}
						} else if(activity.equals("request")) {
							//check if everything is safe
							//if not granted, make it wait
							if (claim > availableResources[resourceRequest-1] && claims[taskId][resourceRequest - 1] < 0) {
								taskCycles[taskId]++;
								taskWaits[taskId]++;
								runningQueue.add(taskId);
							} 
							//else grant it AND MAKE SURE ITS SAFE
							else {
								tasks.get(taskId)[resourceRequest - 1] += claim;
								taskCycles[taskId]++;
								processTaskLine.get(taskId).remove(0);
								
								//keep track of available resources
								if(claims[taskId][resourceRequest - 1]  < 0) {
									claims[taskId][resourceRequest - 1]  += claim;
									availableResources[resourceRequest - 1] -= claim;
								} else {
									claims[taskId][resourceRequest - 1]  -= claim;
									if (claims[taskId][resourceRequest - 1]  < 0) {
										//abort, release all resources, and terminate
										taskCycles[taskId] = null;
										for(int resource = 0; resource < numResources; resource++) {
											releasedResources[resource] += tasks.get(taskId)[resource]; 
											availableResources[resource] += claims[taskId][resourceRequest - 1] ;
											tasks.get(task)[resource] = 0;
										}
										processTaskLine.get(taskId).clear(); //terminated
										terminatedTasks++;
									}
								}

								//check for safety
								for(int resource = 0; resource < numResources; resource++) {
									if (availableResources[resource] >= Math.abs(claims[taskId][resource])) {
										safe++;
									}
								}
								if(safe == numResources) {
									for(int resource = 0; resource < numResources; resource++) {
										claims[taskId][resource] = Math.abs(claims[taskId][resource]);
										availableResources[resource] -= claims[taskId][resource];
									}
								}
								safe = 0;
								taskDelays[taskId] = 0;
							}
						} else if(activity.equalsIgnoreCase("release")) {
							//making sure claims and resources and safety are in check
							taskDelays[taskId] = 0;  
							if (claims[taskId][resourceRequest - 1]  >= 0) {
								claims[taskId][resourceRequest - 1]  *= -1;
								for(int i = 0; i < numResources; i++) {  
									if (claims[taskId][i] <= 0 || Math.abs(claims[taskId][i]) == originalClaim[taskId][i])
										safe++;
								}
								if (safe == numResources) {
									releasedResources[resourceRequest - 1] += claim;
									for(int i = 0; i < numResources; i++) {  
										if (claims[taskId][i] > 0)
											claims[taskId][i] *= -1;
										availableResources[i] -= claims[taskId][i]; 
									}
								}
								safe = 0;
								claims[taskId][resourceRequest - 1] -= claim;
							} else { claims[taskId][resourceRequest - 1] -= claim; }
							tasks.get(taskId)[resourceRequest - 1] -= claim;
							taskCycles[taskId]++;
							processTaskLine.get(taskId).remove(0);
						} else if(activity.equals("terminate")) {
							taskDelays[taskId] = 0;
							terminatedTasks++;
							processTaskLine.get(taskId).remove(0);
						}   
					}
				} else {
					//fixes an off by one error by not accidentally adding a task back to the running queue
					task--;
				}
				runningQueue.remove(0);
			}
			
			//make resources available again
			for (int resource = 0; resource < numResources; resource++) {
				availableResources[resource] += releasedResources[resource]; 
			}
			Arrays.fill(releasedResources, 0);

		} 

		System.out.println("Banker's Algorithm:");
		int j = 0;
		for (Integer cycle : taskCycles) {
			System.out.print("Task " + (j + 1) + ": ");
			if (cycle != null) {
				System.out.println(cycle + " " + taskWaits[j] + " " +  Math.round(taskWaits[j] / (double) cycle * 100) + "%");
				totalRunTime += cycle;
				totalWaitTime += taskWaits[j];
			} else 
				System.out.println("aborted");
			j++;
		} 
		System.out.println("Total: " + totalRunTime + " " + totalWaitTime + " " + Math.round(totalWaitTime / (double) totalRunTime * 100) + "%\n");

	}
}