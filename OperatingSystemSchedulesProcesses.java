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



	static void printGanttChart(List<GanttSegment> gantt) {
		if (gantt.isEmpty()) {
			System.out.println("\nNo Gantt chart to display.");
			return;
		}
		int blockWidth = 6;
		StringBuilder topLine = new StringBuilder();
		StringBuilder bottomLine = new StringBuilder();
		for (GanttSegment seg : gantt) {
			topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));
		}
		for (GanttSegment seg : gantt) {
			bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));
		}
		bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);
		System.out.println("\nGantt Chart:");
		System.out.println(topLine.toString());
		System.out.println(bottomLine.toString());
	}

	static void fcfsScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- FCFS Scheduling ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		System.out.println("\n--- FCFS Scheduling ---");
		for (Process p : procs) {
			if (time < p.arrival) {
				gantt.add(new GanttSegment("Idle", time, p.arrival));
				time = p.arrival;
			}
			int start = time;
			if (p.cpuInit == -1) {
				p.cpuInit = start;
			}
			p.waiting = time - p.arrival;
			time += p.burst;
			p.completion = time;
			p.turnaround = p.completion - p.arrival;
			gantt.add(new GanttSegment("P" + p.pid, start, time));
		}
		double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		printGanttChart(gantt);
		for (Process p : procs) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	static void sjfScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
		while (!procs.isEmpty()) {
			List<Process> available = new ArrayList<>();
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				continue;
			}
			Process current = available.get(0);
			for (Process p : available) {
				if (p.burst < current.burst)
					current = p;
			}
			procs.remove(current);
			int start = time;
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			current.waiting = time - current.arrival;
			time += current.burst;
			current.completion = time;
			current.turnaround = current.completion - current.arrival;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			finished.add(current);
		}
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		printGanttChart(gantt);
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	static void roundRobinScheduling(List<Process> processes, int timeQuantum) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
			System.out.println("No processes to schedule.");
			return;
		}
		for (Process p : procs) {
			p.remaining = p.burst;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		List<Process> notAdded = new ArrayList<>(procs);
		notAdded.sort(Comparator.comparingInt(p -> p.arrival));
		List<Process> queue = new ArrayList<>();
		System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
		while (!queue.isEmpty() || !notAdded.isEmpty()) {
			if (queue.isEmpty()) {
				Process nextProc = notAdded.get(0);
				if (time < nextProc.arrival) {
					gantt.add(new GanttSegment("Idle", time, nextProc.arrival));
					time = nextProc.arrival;
				}
				while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
					queue.add(notAdded.remove(0));
				}
			}
			Process current = queue.remove(0);
			int start = time;
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			int execTime = Math.min(timeQuantum, current.remaining);
			time += execTime;
			current.remaining -= execTime;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
				queue.add(notAdded.remove(0));
			}
			if (current.remaining > 0) {
				queue.add(current);
			} else {
				current.completion = time;
				current.turnaround = current.completion - current.arrival;
				current.waiting = current.turnaround - current.burst;
				finished.add(current);
			}
		}
		finished.sort(Comparator.comparingInt(p -> p.pid));
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		printGanttChart(gantt);
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
		}
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	static void priorityScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
		while (!procs.isEmpty()) {
			List<Process> available = new ArrayList<>();
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				continue;
			}
			Process current = available.get(0);
			for (Process p : available) {
				if (p.priority > current.priority)
					current = p;
			}
			procs.remove(current);
			int start = time;
			if (current.cpuInit == -1) {
				current.cpuInit = start;
			}
			current.waiting = time - current.arrival;
			time += current.burst;
			current.completion = time;
			current.turnaround = current.completion - current.arrival;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			finished.add(current);
		}
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		printGanttChart(gantt);
		for (Process p : finished) {
			System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n", p.pid, p.cpuInit, p.waiting, p.turnaround, p.priority);
		}
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
	}

	static boolean getYesNo(String prompt, Scanner sc) {
		while (true) {
			System.out.print(prompt);
			String input = sc.nextLine().trim().toLowerCase();
			if (input.equals("y")) {
				return true;
			} else if (input.equals("n")) {
				return false;
			} else {
				System.out.println("Invalid option. Please enter 'y' or 'n'.");
			}
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String filename = "processes.txt";
		Path filePath = Paths.get(filename);
		System.out.println("The processes file is located at: " + filePath.toAbsolutePath());
		boolean useFile = getYesNo("Do you want to run the program using this file? (y/n): ", sc);
		if (!useFile) {
			System.out.println("Exiting program.");
			sc.close();
			return;
		}
		List<Process> processes = readProcesses(filename);
		if (processes.isEmpty()) {
			System.out.println("No processes to schedule. Please check your processes.txt file.");
		} else {
			processes.sort(Comparator.comparingInt(p -> p.arrival));
			if (getYesNo("Run FCFS Scheduling? (y/n): ", sc)) {
				fcfsScheduling(processes);
				System.out.println("\n--------------------\n");
			}
			if (getYesNo("Run SJF Scheduling? (y/n): ", sc)) {
				sjfScheduling(processes);
				System.out.println("\n--------------------\n");
			}
			if (getYesNo("Run Round Robin Scheduling? (y/n): ", sc)) {
				roundRobinScheduling(processes, 4);
				System.out.println("\n--------------------\n");
			}
			if (getYesNo("Run Priority Scheduling? (y/n): ", sc)) {
				priorityScheduling(processes);
				System.out.println("\n--------------------\n");
			}
		}
		sc.close();
	}
}