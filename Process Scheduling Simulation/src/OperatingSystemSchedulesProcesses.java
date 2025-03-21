//Importing the necessary libraries
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

//This defines the main class for simulating OS process scheduling and memory allocation
public class OperatingSystemSchedulesProcesses {
	//This class represents an individual process
	static class Process {
		//These variables store process ID, arrival time, burst time, priority, waiting time, turnaround time, completion time, remaining burst time and CPU initialization time
		int pid, arrival, burst, priority, waiting, turnaround, completion, remaining, cpuInit;
		//This variable represents the memory requirement for the process
		int memoryRequirement;

		//This constructor initializes a process with all given parameters
		Process(int pid, int arrival, int burst, int priority, int memoryRequirement) {
			this.pid = pid; //Assign the process ID
			this.arrival = arrival; //Assign the arrival time
			this.burst = burst; //Assign the CPU burst time
			this.priority = priority; //Assign the process priority
			this.memoryRequirement = memoryRequirement; //Assign the memory requirement
			this.waiting = 0; //Initialize waiting time to 0
			this.turnaround = 0; //Initialize turnaround time to 0
			this.completion = 0; //Initialize completion time to 0
			this.remaining = burst; //Set remaining burst time equal to burst time
			this.cpuInit = -1; //Initialize CPU initialization time to -1 (unset)
		}

		//This constructor initializes a process with a default memory requirement
		Process(int pid, int arrival, int burst, int priority) {
			//Call the main constructor with a default memory requirement of 100
			this(pid, arrival, burst, priority, 100);
		}

		//This copy constructor creates a new process from an existing process
		Process(Process p) {
			this.pid = p.pid; //Copy the process ID
			this.arrival = p.arrival; //Copy the arrival time
			this.burst = p.burst; //Copy the burst time
			this.priority = p.priority; //Copy the process priority
			this.waiting = p.waiting; //Copy the waiting time
			this.turnaround = p.turnaround; //Copy the turnaround time
			this.completion = p.completion; //Copy the completion time
			this.remaining = p.remaining; //Copy the remaining burst time
			this.cpuInit = p.cpuInit; //Copy the CPU initialization time
			this.memoryRequirement = p.memoryRequirement; //Copy the memory requirement
		}
	}




	//This class represents a segment in a Gantt chart
	static class GanttSegment {
		//This variable stores the label for the segment (for example "P1" or "Idle")
		String label;
		//These variables store the start and finish times of the segment
		int start, finish;

		//This constructor initializes a Gantt segment with the given label, start time and finish time
		GanttSegment(String label, int start, int finish) {
			this.label = label; //Assign the segment label
			this.start = start; //Assign the start time
			this.finish = finish; //Assign the finish time
		}
	}




	//This class represents a free memory block (a memory hole)
	static class MemoryHole {
		//These variables represent the starting address and size of the memory hole
		int start, size;

		//This constructor initializes a memory hole with a starting address and size
		MemoryHole(int start, int size) {
			this.start = start; //Assign the starting address
			this.size = size; //Assign the size of the memory hole
		}
	}




	//This method reads process data from a file and returns a list of Process objects
	static List<Process> readProcesses(String filename) {
		//Create a list to store processes
		List<Process> processes = new ArrayList<>();
		//Construct a Path object for the given filename
		Path filePath = Paths.get(filename);
		//Print the absolute file path
		System.out.println("Reading file from: " + filePath.toAbsolutePath());

		//Open the file using BufferedReader
		try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
			//Read the header line and ignore it
			String line = br.readLine();
			//Loop through each subsequent line in the file
			while ((line = br.readLine()) != null) {
				//If the line is empty after trimming whitespace then skip it
				if (line.trim().isEmpty())
					continue;
				//Split the line by one or more whitespace characters
				String[] parts = line.trim().split("\\s+");
				//If there are at least 4 parts (PID, Arrival, Burst, Priority)
				if (parts.length >= 4) {
					//Parse the process ID, arrival time, burst time and priority
					int pid = Integer.parseInt(parts[0]);
					int arrival = Integer.parseInt(parts[1]);
					int burst = Integer.parseInt(parts[2]);
					int priority = Integer.parseInt(parts[3]);
					//Set a default memory requirement of 100
					int memoryReq = 100;
					//If a fifth column is present then parse the memory requirement
					if (parts.length >= 5) {
						memoryReq = Integer.parseInt(parts[4]);
					}
					//Create a new Process and add it to the list
					processes.add(new Process(pid, arrival, burst, priority, memoryReq));
				}
			}
		} 
		//Catch any I/O exceptions that occur while reading the file
		catch (IOException e) {
			//Print an error message if the file cannot be read
			System.out.println("Error: File " + filename + " not found.");
		}
		//Return the list of processes
		return processes;
	}



	//This method prints a text-based Gantt chart representing the scheduling timeline
	static void printGanttChart(List<GanttSegment> gantt) {
		//If the Gantt chart is empty then inform the user and return
		if (gantt.isEmpty()) {
			System.out.println("\nNo Gantt chart to display.");
			return;
		}
		int blockWidth = 6; //Define the width of each block in the chart
		//Create a StringBuilder for the top line (labels) of the chart
		StringBuilder topLine = new StringBuilder();
		//Create a StringBuilder for the bottom line (time markers) of the chart
		StringBuilder bottomLine = new StringBuilder();
		//Iterate over each Gantt segment to build the top line
		for (GanttSegment seg : gantt) {
			//Append the segment label formatted within the block
			topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));
		}
		//Iterate over each Gantt segment to build the bottom line
		for (GanttSegment seg : gantt) {
			//Append the start time formatted within the block
			bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));
		}
		//Append the final finish time to the bottom line
		bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);
		//Print the header for the Gantt chart
		System.out.println("\nGantt Chart:");
		//Print the top line of the chart
		System.out.println(topLine.toString());
		//Print the bottom line of the chart
		System.out.println(bottomLine.toString());
	}






	//This method simulates First-Come, First-Served scheduling
	static void fcfsScheduling(List<Process> processes) {
		//Create a new list to hold copies of the processes so as not to modify the original list
		List<Process> procs = new ArrayList<>();
		//For each process in the input list, add a copy to the new list
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		//If there are no processes to schedule, inform the user and return
		if (procs.isEmpty()) {
			System.out.println("\n--- FCFS Scheduling ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0; //Initialize simulation time to 0
		//Create a list to store Gantt chart segments
		List<GanttSegment> gantt = new ArrayList<>();
		//Print the header for FCFS scheduling
		System.out.println("\n--- FCFS Scheduling ---");
		//For each process in FCFS order
		for (Process p : procs) {
			//If the current time is less than the process arrival time, add an idle segment and update time
			if (time < p.arrival) {
				gantt.add(new GanttSegment("Idle", time, p.arrival));
				time = p.arrival;
			}
			int start = time; //Record the start time for the process
			//If the CPU initialization time is not yet set, set it now
			if (p.cpuInit == -1) {
				p.cpuInit = start;
			}
			p.waiting = time - p.arrival; //Calculate the waiting time
			time += p.burst; //Increment time by the burst time of the process
			p.completion = time; //Set the completion time
			p.turnaround = p.completion - p.arrival; //Calculate the turnaround time
			//Add a Gantt segment for the process
			gantt.add(new GanttSegment("P" + p.pid, start, time));
		}
		//Calculate the average waiting time for all processes
		double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0);
		//Calculate the average turnaround time for all processes
		double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		//Print the Gantt chart
		printGanttChart(gantt);
		//For each process, print the process details and computed metrics
		for (Process p : procs) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		//Print the average waiting time and average turnaround time
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	//This method simulates Shortest Job First scheduling (non-preemptive)
	static void sjfScheduling(List<Process> processes) {
		//Create a list to hold copies of the processes
		List<Process> procs = new ArrayList<>();
		//For each process, add a copy to the new list
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		//If there are no processes to schedule, inform the user and return
		if (procs.isEmpty()) {
			System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0; //Initialize simulation time to 0
		//Create a list to store Gantt chart segments
		List<GanttSegment> gantt = new ArrayList<>();
		//Create a list to store finished processes
		List<Process> finished = new ArrayList<>();
		//Print the header for SJF scheduling
		System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
		//Continue scheduling until all processes are finished
		while (!procs.isEmpty()) {
			//Create a list for processes that have arrived by the current time
			List<Process> available = new ArrayList<>();
			//For each process, if it has arrived then add it to the available list
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			//If no process is available, add an idle segment and update the time to the next arrival
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				continue;
			}
			//Assume the first available process is the shortest job
			Process current = available.get(0);
			//For each available process, update the current process if a shorter job is found
			for (Process p : available) {
				if (p.burst < current.burst)
					current = p;
			}
			//Remove the selected process from the process list
			procs.remove(current);
			int start = time; //Record the start time for the process
			//If the CPU initialization time is not set, set it now
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			current.waiting = time - current.arrival; //Calculate the waiting time
			time += current.burst; //Increment time by the burst time
			current.completion = time; //Set the completion time
			current.turnaround = current.completion - current.arrival; //Calculate the turnaround time
			//Add a Gantt segment for the process
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			//Add the process to the finished list
			finished.add(current);
		}
		//Calculate the average waiting time and average turnaround time for the finished processes
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		//Print the Gantt chart
		printGanttChart(gantt);
		//For each finished process, print the process details and computed metrics
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		//Print the average waiting time and average turnaround time
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}






	//This method simulates Round Robin scheduling with a specified time quantum
	static void roundRobinScheduling(List<Process> processes, int timeQuantum) {
		//Create a list to hold copies of the processes
		List<Process> procs = new ArrayList<>();
		//For each process, add a copy to the list
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		//If there are no processes to schedule, inform the user and return
		if (procs.isEmpty()) {
			System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
			System.out.println("No processes to schedule.");
			return;
		}
		//Initialize the remaining burst time for each process
		for (Process p : procs) {
			p.remaining = p.burst;
		}
		int time = 0; //Initialize simulation time to 0
		//Create a list to store Gantt chart segments
		List<GanttSegment> gantt = new ArrayList<>();
		//Create a list to store finished processes
		List<Process> finished = new ArrayList<>();
		//Create a list for processes that have not yet been added to the scheduling queue
		List<Process> notAdded = new ArrayList<>(procs);
		//Sort the notAdded list by arrival time
		notAdded.sort(Comparator.comparingInt(p -> p.arrival));
		//Create a scheduling queue
		List<Process> queue = new ArrayList<>();
		//Print the header for Round Robin scheduling
		System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
		//Continue scheduling until both the queue and the notAdded list are empty
		while (!queue.isEmpty() || !notAdded.isEmpty()) {
			//If the queue is empty then the CPU is idle
			if (queue.isEmpty()) {
				Process nextProc = notAdded.get(0);
				//If the current time is less than the arrival time of the next process, add an idle segment and update time
				if (time < nextProc.arrival) {
					gantt.add(new GanttSegment("Idle", time, nextProc.arrival));
					time = nextProc.arrival;
				}
				//Add all processes that have arrived by the current time to the queue
				while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
					queue.add(notAdded.remove(0));
				}
			}
			//Remove the first process from the queue for execution
			Process current = queue.remove(0);
			int start = time; //Record the start time for the process
			//If the CPU initialization time is not set, set it now
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			//Determine the execution time as the minimum of the time quantum and the remaining burst time
			int execTime = Math.min(timeQuantum, current.remaining);
			time += execTime; //Increment time by the execution time
			current.remaining -= execTime; //Decrease the remaining burst time
			//Add a Gantt segment for the execution of the process
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			//Add any processes that have arrived during execution to the queue
			while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
				queue.add(notAdded.remove(0));
			}
			//If the process is not yet finished, re-add it to the queue; otherwise, calculate its metrics and add it to finished list
			if (current.remaining > 0) {
				queue.add(current);
			} else {
				current.completion = time;
				current.turnaround = current.completion - current.arrival;
				current.waiting = current.turnaround - current.burst;
				finished.add(current);
			}
		}
		//Sort the finished processes by process ID
		finished.sort(Comparator.comparingInt(p -> p.pid));
		//Calculate the average waiting time and average turnaround time for the finished processes
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		//Print the Gantt chart for Round Robin scheduling
		printGanttChart(gantt);
		//For each finished process, print the process details and computed metrics
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		//Print the average waiting time and average turnaround time
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}






	//This method simulates Priority scheduling (non-preemptive) with a reversed priority order (higher value indicates higher priority)
	static void priorityScheduling(List<Process> processes) {
		//Create a list to hold copies of the processes
		List<Process> procs = new ArrayList<>();
		//For each process, add a copy to the list
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		//If there are no processes to schedule, inform the user and return
		if (procs.isEmpty()) {
			System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0; //Initialize simulation time to 0
		//Create a list to store Gantt chart segments
		List<GanttSegment> gantt = new ArrayList<>();
		//Create a list to store finished processes
		List<Process> finished = new ArrayList<>();
		//Print the header for Priority scheduling
		System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
		//Continue scheduling until all processes are finished
		while (!procs.isEmpty()) {
			//Create a list for processes that have arrived by the current time
			List<Process> available = new ArrayList<>();
			//For each process, if it has arrived then add it to the available list
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			//If no process is available, add an idle segment and update the time to the next arrival
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				continue;
			}
			//Assume the first available process is the highest priority process
			Process current = available.get(0);
			//For each available process, update the current process if one with a higher priority (larger value) is found
			for (Process p : available) {
				if (p.priority > current.priority)
					current = p;
			}
			//Remove the selected process from the process list
			procs.remove(current);
			int start = time; //Record the start time for the process
			//If the CPU initialization time is not set, set it now
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			current.waiting = time - current.arrival; //Calculate the waiting time
			time += current.burst; //Increment time by the burst time
			current.completion = time; //Set the completion time
			current.turnaround = current.completion - current.arrival; //Calculate the turnaround time
			//Add a Gantt segment for the process
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			//Add the process to the finished list
			finished.add(current);
		}
		//Calculate the average waiting time and average turnaround time for the finished processes
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		//Print the Gantt chart for Priority scheduling
		printGanttChart(gantt);
		//For each finished process, print the process details and computed metrics including priority
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n", 
							  p.pid, p.cpuInit, p.waiting, p.turnaround, p.priority);
		}
		//Print the average waiting time and average turnaround time
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	//This method implements the first-fit memory allocation strategy
	static MemoryHole firstFitAllocation(List<MemoryHole> holes, int request) {
		//Iterate over the list of memory holes
		for (int i = 0; i < holes.size(); i++) {
			//Get the current memory hole
			MemoryHole hole = holes.get(i);
			//If the hole is large enough for the request, allocate memory from it
			if (hole.size >= request) {
				MemoryHole allocated = new MemoryHole(hole.start, request);
				//Calculate the new start address after allocation
				int newStart = hole.start + request;
				//Calculate the remaining size of the hole
				int newSize = hole.size - request;
				//If there is remaining space, update the hole; otherwise, remove it
				if (newSize > 0) {
					holes.set(i, new MemoryHole(newStart, newSize));
				} else {
					holes.remove(i);
				}
				//Return the allocated memory block
				return allocated;
			}
		}
		//Return null if no suitable memory hole is found
		return null;
	}






	//This method simulates memory allocation for processes using the first-fit strategy
	//It automatically determines the number of memory holes based on the number of processes
	static void simulateMemoryAllocationFirstFit(List<Process> processes) {
		//Determine the number of processes (and thus memory holes)
		int numHoles = processes.size();
		//Create a list to hold memory holes (one per process)
		List<MemoryHole> holes = new ArrayList<>();
		Random rand = new Random();
		int start = 0;
		//For each process, create a memory hole with a random size
		for (int i = 0; i < numHoles; i++) {
			//Generate a random size for the memory hole between 100 and 200 units
			int size = 100 + rand.nextInt(101);
			holes.add(new MemoryHole(start, size));
			//Set the next hole's start address with a gap of 10 units
			start += size + 10;
		}
		//Print the simulation header for memory allocation
		System.out.println("\nMemory Allocation Simulation using FIRST_FIT (Automatically Generated Memory Holes):");
		//For each process, attempt to allocate memory using the first-fit strategy
		for (Process p : processes) {
			int request = p.memoryRequirement; //Get the memory requirement for the process
			MemoryHole allocated = firstFitAllocation(holes, request); //Attempt allocation
			//If allocation was successful, print the allocation details
			if (allocated != null) {
				System.out.println("Process " + p.pid + " (memory request: " + request +
								") allocated at address " + allocated.start + " with size " + allocated.size);
			}
			//If allocation failed, print a failure message
			else {
				System.out.println("Process " + p.pid + " allocation of size " + request + " failed.");
			}
		}
		//Print the header for remaining free memory holes
		System.out.println("Remaining free holes:");
		//For each remaining memory hole, print its starting address and size
		for (MemoryHole hole : holes) {
			System.out.println("Start: " + hole.start + ", Size: " + hole.size);
		}
	}



	//This method simulates paging using the FIFO algorithm and returns the total number of page faults
	static int simulatePagingFIFO(int[] pageReferences, int numFrames) {
		//Create a list to represent memory frames
		List<Integer> frames = new ArrayList<>();
		int pageFaults = 0; //Initialize the page fault counter to 0
		//For each page reference
		for (int page : pageReferences) {
			//If the page is not currently in memory
			if (!frames.contains(page)) {
				pageFaults++; //Increment the page fault counter
				//If there is space in memory, add the page; otherwise, remove the oldest page and add the new page
				if (frames.size() < numFrames) {
					frames.add(page);
				} else {
					frames.remove(0);
					frames.add(page);
				}
			}
		}
		//Return the total number of page faults
		return pageFaults;
	}



	//This method simulates paging using the LRU algorithm and returns the total number of page faults
	static int simulatePagingLRU(int[] pageReferences, int numFrames) {
		//Create a list to represent memory frames
		List<Integer> frames = new ArrayList<>();
		int pageFaults = 0; //Initialize the page fault counter to 0
		//For each page reference
		for (int page : pageReferences) {
			//If the page is not currently in memory
			if (!frames.contains(page)) {
				pageFaults++; //Increment the page fault counter
				//If there is space in memory, add the page; otherwise, remove the least recently used page and add the new page
				if (frames.size() < numFrames) {
					frames.add(page);
				} else {
					frames.remove(0);
					frames.add(page);
				}
			} else {
				//If the page is already in memory, update its recency by removing and re-adding it
				frames.remove(Integer.valueOf(page));
				frames.add(page);
			}
		}
		//Return the total number of page faults
		return pageFaults;
	}


    
	//This helper method prompts the user for a yes/no input and returns true for "y" and false for "n"
	static boolean getYesNo(String prompt, Scanner sc) {
		//Loop until valid input is received
		while (true) {
			System.out.print(prompt); //Print the prompt
			String input = sc.nextLine().trim().toLowerCase(); //Read and normalize the input
			if (input.equals("y")) { //If the input is "y"
				return true;
			} else if (input.equals("n")) { //If the input is "n"
				return false;
			} else { //If the input is invalid
				System.out.println("Invalid option. Please enter 'y' or 'n'.");
			}
		}
	}






	//This is the main method, the entry point of the program
	public static void main(String[] args) {
		//Create a Scanner object to read user input
		Scanner sc = new Scanner(System.in);
		//Define the filename for the processes file
		String filename = "processes.txt";
		//Construct a Path object for the filename
		Path filePath = Paths.get(filename);
		//Print the absolute file path of the processes file
		System.out.println("The processes file is located at: " + filePath.toAbsolutePath());
		//Prompt the user to confirm whether to use the file
		boolean useFile = getYesNo("Do you want to run the program using this file? (y/n): ", sc);
		//If the user chooses not to use the file, exit the program
		if (!useFile) {
			System.out.println("Exiting program.");
			sc.close();
			return;
		}
		//Read the processes from the file
		List<Process> processes = readProcesses(filename);
		//If no processes were read, inform the user
		if (processes.isEmpty()) {
			System.out.println("No processes to schedule. Please check your processes.txt file.");
		} else {
			//Sort the processes by arrival time
			processes.sort(Comparator.comparingInt(p -> p.arrival));
			//Prompt the user to run FCFS scheduling and execute if confirmed
			if (getYesNo("Run FCFS Scheduling? (y/n): ", sc)) {
				fcfsScheduling(processes);
				System.out.println("\n--------------------\n");
			}
			//Prompt the user to run SJF scheduling and execute if confirmed
			if (getYesNo("Run SJF Scheduling? (y/n): ", sc)) {
				sjfScheduling(processes);
				System.out.println("\n--------------------\n");
			}
			//Prompt the user to run Round Robin scheduling and execute if confirmed with a time quantum of 4
			if (getYesNo("Run Round Robin Scheduling? (y/n): ", sc)) {
				roundRobinScheduling(processes, 4);
				System.out.println("\n--------------------\n");
			}
			//Prompt the user to run Priority scheduling and execute if confirmed
			if (getYesNo("Run Priority Scheduling? (y/n): ", sc)) {
				priorityScheduling(processes);
				System.out.println("\n--------------------\n");
			}
		}
		//Print the header for memory allocation simulation
		System.out.println("\nMemory Allocation Simulation:");
		//Simulate memory allocation using the first-fit strategy
		simulateMemoryAllocationFirstFit(processes);
		
		//Define an array of page references for paging simulation
		int[] pageRefs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
		//Define the number of memory frames for the paging simulation
		int numFrames = 3;
		//Simulate FIFO paging and capture the number of page faults
		int fifoFaults = simulatePagingFIFO(pageRefs, numFrames);
		//Simulate LRU paging and capture the number of page faults
		int lruFaults = simulatePagingLRU(pageRefs, numFrames);
		//Print the header for paging simulation
		System.out.println("\nPaging Simulation:");
		//Print the number of FIFO page faults
		System.out.println("FIFO Page Faults: " + fifoFaults);
		//Print the number of LRU page faults
		System.out.println("LRU Page Faults: " + lruFaults);
		//Close the Scanner resource
		sc.close();
	}
}