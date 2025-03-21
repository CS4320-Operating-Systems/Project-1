import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

public class OperatingSystemSchedulesProcesses {
    static class Process {
        int pid, arrival, burst, priority, waiting, turnaround, completion, remaining, cpuInit;
        int memoryRequirement;

        Process(int pid, int arrival, int burst, int priority, int memoryRequirement) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.memoryRequirement = memoryRequirement;
            this.waiting = 0;
            this.turnaround = 0;
            this.completion = 0;
            this.remaining = burst;
            this.cpuInit = -1;
        }
        Process(int pid, int arrival, int burst, int priority) {
            this(pid, arrival, burst, priority, 100);
        }
        Process(Process p) {
            this.pid = p.pid;
            this.arrival = p.arrival;
            this.burst = p.burst;
            this.priority = p.priority;
            this.waiting = p.waiting;
            this.turnaround = p.turnaround;
            this.completion = p.completion;
            this.remaining = p.remaining;
            this.cpuInit = p.cpuInit;
            this.memoryRequirement = p.memoryRequirement;
        }
    }

    static class GanttSegment {
        String label;
        int start, finish;

        GanttSegment(String label, int start, int finish) {
            this.label = label;
            this.start = start;
            this.finish = finish;
        }
    }



    static List<Process> readProcesses(String filename) {
        List<Process> processes = new ArrayList<>();
        Path filePath = Paths.get(filename);
        System.out.println("Reading file from: " + filePath.toAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    int pid = Integer.parseInt(parts[0]);
                    int arrival = Integer.parseInt(parts[1]);
                    int burst = Integer.parseInt(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    int memoryReq = 100;
                    if (parts.length >= 5) {
                        memoryReq = Integer.parseInt(parts[4]);
                    }
                    processes.add(new Process(pid, arrival, burst, priority, memoryReq));
                }
            }
        } catch (IOException e) {
            System.out.println("Error: File " + filename + " not found.");
        }
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


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String filename = "processes.txt";
        List<Process> processes = readProcesses(filename);
        if (processes.isEmpty()) {
            System.out.println("No processes to schedule. Please check your processes.txt file.");
        } else {
            processes.sort(Comparator.comparingInt(p -> p.arrival));
            fcfsScheduling(processes);
            sjfScheduling(processes);
        }
    }
}