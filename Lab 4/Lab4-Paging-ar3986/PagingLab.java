import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author Ashish Ramachandran (ar3986)
 * Lab 4: Paging
 */

public class PagingLab {

	public static void main (String[] args) throws IOException {

		//check if there are enough arguments otherwise quit
		if(args.length < 6) {
			System.out.println("Not enough input variables.");
			return;
		}

		//define integer array representing the machine that will hold the machine size, page size, process size, job mix, and number of references
		//another variable for the replacement algorithm
		int MACHINE_SIZE = 0, PAGE_SIZE = 1, PROCESS_SIZE = 2, JOB_MIX = 3, NUM_REF = 4;
		String replacementAlg = args[5];
		int[] machine = new int[5];

		//populate the array
		for(int i = 0; i < 5; i++) {
			machine[i] = Integer.parseInt(args[i]);
		}

		//read input file for random numbers set by course
		StringTokenizer tokenizer = new StringTokenizer(new String(readAllBytes(get("random-numbers.txt"))));

		System.out.println("The machine size is " + machine[MACHINE_SIZE] +".\n"
				+ "The page size is " + machine[PAGE_SIZE] +".\n"
				+ "The process size is " + machine[PROCESS_SIZE] +".\n" 
				+ "The job mix number is " + machine[JOB_MIX] +".\n" 
				+ "The number of references per process is " + machine[NUM_REF] +".\n" 
				+ "The replacement algorithm is " + replacementAlg +".\n\n");

		//figure out how many processes there are from the job mix (default is 1)
		int numberProcesses = 1; 
		if(machine[JOB_MIX] != 1) 
			numberProcesses = 4;

		//create word array for the each process
		int[] word = new int[numberProcesses];

		//create a frame table to keep track of the simulation and set default values
		Integer[][] frametable = new Integer[machine[MACHINE_SIZE] / machine[PAGE_SIZE]][3];
		for(Integer[] row: frametable) {
			Arrays.fill(row, -1);
		}

		//create variable to keep track of faults, running time, evictions, residence and initialize
		int[] faultsTracker = new int[numberProcesses];
		int[] runningTime = new int[machine[MACHINE_SIZE]/machine[PAGE_SIZE]];
		int[] evictionsTracker = new int[numberProcesses];		
		double[] residenceTracker = new double[numberProcesses];

		for(int i = 0; i < numberProcesses; i++) {
			faultsTracker[i] = 0;
			evictionsTracker[i] = 0;
			residenceTracker[i] = 0;
		}

		int[] lines = new int[numberProcesses];
		Arrays.fill(lines, machine[NUM_REF]);

		//populate with words to start with for the processes
		for(int i = 0; i < numberProcesses; i++) { 
			word[i] = (111 * (i + 1)) % machine[PROCESS_SIZE];
		}


		//variables to help during simulation
		int firstFreeFrameIndex = -1;
		int unitToReplaceIndex = -1;
		int matchedFrameIndex = -1;

		//based on lab requirements, calculate the probabilities, keep in array list to keep track of the possible processes!!!
		ArrayList<double[]> probability = new ArrayList<double[]>();
		if(machine[JOB_MIX] == 1) { 
			probability.add(new double[]{1, 0, 0}); 
		} else if(machine[JOB_MIX] == 2) { 
			for(int i = 0; i < numberProcesses; i++)
				probability.add(new double[]{1, 0, 0}); 
		} else if(machine[JOB_MIX] == 3) { 
			for(int i = 0; i < numberProcesses; i++)
				probability.add(new double[]{0, 0, 0}); 
		} else if(machine[JOB_MIX] == 4) {
			probability.add(new double[]{0.75, 0.25, 0}); 
			probability.add(new double[]{0.75, 0, 0.25}); 
			probability.add(new double[]{0.75, 0.125, 0.125}); 
			probability.add(new double[]{0.5, 0.125, 0.125}); 
		}


		int process = 0; 

		//SIMULATION
		for(int processTracker = 1; processTracker < machine[NUM_REF] * numberProcesses + 1; processTracker++) {
			matchedFrameIndex = -1;

			//find a matching frame
			for(int i = 0; i < frametable.length; i++) {
				if(frametable[i][0] == process) 
					if(frametable[i][1] == word[process] / machine[PAGE_SIZE]) { 
						matchedFrameIndex = i;
						break;
					}
			}

			//if we find a matched frame
			if(matchedFrameIndex != -1) {
				frametable[matchedFrameIndex][2] = processTracker; 
			} 
			//otherwise page fault
			else {
				faultsTracker[process]++;
				firstFreeFrameIndex = -1;

				//find first free frame
				for(int i = 0; i < frametable.length; i++) {
					if(frametable[i][1] == -1) { 
						firstFreeFrameIndex = i;
						break;
					}
				}

				//if not found we need to replace find replacement index by inputed algorithm
				if(firstFreeFrameIndex == -1) {
					if(replacementAlg.equalsIgnoreCase("lru")) {
						unitToReplaceIndex = 0;
						for(int i = 1; i < frametable.length; i++) {
							if(frametable[unitToReplaceIndex][2] > frametable[i][2]) {
								unitToReplaceIndex = i;
							}
						}
					} else if(replacementAlg.equalsIgnoreCase("random")) { //specified by the lab requirements
						unitToReplaceIndex = (Integer.parseInt(tokenizer.nextToken()) + 1) % frametable.length;              
					} else if(replacementAlg.equalsIgnoreCase("lifo")) {
						unitToReplaceIndex = frametable.length - 1;
					} else {
						System.out.println("Please input a valid algorithm (lru, random, or lifo)");
						return;
					}

					//keeps track of residence and eviction trackers
					residenceTracker[frametable[unitToReplaceIndex][0]] += processTracker - runningTime[unitToReplaceIndex];
					evictionsTracker[frametable[unitToReplaceIndex][0]]++;
					
					
					frametable[unitToReplaceIndex][0] = process; 
					frametable[unitToReplaceIndex][1] = word[process] / machine[PAGE_SIZE]; 
					frametable[unitToReplaceIndex][2] = processTracker;   
					runningTime[unitToReplaceIndex] = processTracker;
				}
				//if firstFreeFrameIndex found
				else {
					frametable[firstFreeFrameIndex][0] = process; 
					frametable[firstFreeFrameIndex][1] = word[process] / machine[PAGE_SIZE]; 
					frametable[firstFreeFrameIndex][2] = processTracker; 
					runningTime[firstFreeFrameIndex] = processTracker;
				}
			}

			//find next word reference based on the requirements of the lab
			word[process] = findNextWordReference(probability.get(process)[0], probability.get(process)[1], probability.get(process)[2], machine[PROCESS_SIZE], word[process], tokenizer);
			lines[process]--;
			
			//this is for the reference for the process to keep track
			if(lines[process] == 0) {
				process++; 
			} 
			//since q = 3 from the lab requirements
			else if(processTracker % 3 == 0 && lines[process] > machine[NUM_REF] % 3 - 1) {
				process = (process + 1) % numberProcesses; 
			}
		}

		//print results
		for(int i = 0; i < faultsTracker.length; i++) {
			System.out.println("Process " + (i + 1) + " had " + faultsTracker[i] + " faults and average residency is "
					+ ((evictionsTracker[i] == 0) ? "undefined." : (residenceTracker[i]/evictionsTracker[i]) + "."));
		}
		int totalFaults = 0;
		double totalEvicts = 0;
		double totalResidency = 0;
		for(int i = 0; i < numberProcesses; i++) {
			totalFaults += faultsTracker[i];
			totalEvicts += evictionsTracker[i];
			totalResidency += residenceTracker[i];
		}
		System.out.println("\nThe total number of faults is " + totalFaults + " and the overall average residency is "
				+ ((totalEvicts == 0) ? "undefined." : (totalResidency/totalEvicts) + "."));
	}

	private static int findNextWordReference(double A, double B, double C, int S, int lastword, StringTokenizer st) {
		
		//using the recommendations on the lab, the logic is essentially the same as what the professor has provided us
		//logic is from the lab assignment PDF
		//recommendations on the modular arithmetic were implemented as well
		
		double y = Integer.parseInt(st.nextToken()) / (Integer.MAX_VALUE + 1d);

		if(y < A) {
			lastword = (lastword + 1) % S;
		} else if(y < A + B) {
			lastword = (lastword - 5 + S) % S;
		} else if(y < A + B + C) { 
			lastword = (lastword + 4) % S;
		} else {
			lastword = Integer.parseInt(st.nextToken()) % S;
		}
		return lastword;
	}
}