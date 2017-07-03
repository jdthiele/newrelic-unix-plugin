package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class LinuxMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "Linux";
	public List<Integer> linuxvmstatignores = Arrays.asList(13, 14, 15, 16, 17);
	
	public LinuxMetrics() {
		
		// Linux doesn't use "pagesize" command
		super(new String[]{"getconf","PAGESIZE"});
		
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
			new String[]{kColumnMetricDiskName, "1024-blocks", "Used", "Available", "Capacity"});
		allCommands.put("df", new UnixCommand(new String[]{"df","-Pk", "-x iso9660", "-x cdfs", "-x hsfs"}, commandTypes.REGEXDIM, defaultignores, 0, dfMapping));
		allMetrics.put(CommandMetricUtils.mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Capacity"), new MetricDetail("Disk", "Used", "percent", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'diskstats' command
		 */
		HashMap<Pattern, String[]> diskstatsMapping = new HashMap<Pattern, String[]>();
		diskstatsMapping.put(Pattern.compile("\\d+\\s+\\d+\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)" +
			 "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "reads", "readsmerged", "sectorsread", "readtime", "writes", "writesmerged", "sectorswritten", "writetime", "inprogress", "iotime", "iotime_weighted"});

		allCommands.put("diskstats", new UnixCommand(new String[]{"cat","/proc/diskstats"}, commandTypes.REGEXDIM, defaultignores, 0, diskstatsMapping));

		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "reads"), new MetricDetail("DiskIO", "Reads Per Interval", "transfers", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "readsmerged"), new MetricDetail("DiskIO", "Reads Merged Per Interval", "transfers", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "sectorsread"), new MetricDetail("DiskIO", "Sectors Read Per Interval", "sectors", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "readtime"), new MetricDetail("DiskIO", "Time Spent Reading", "ms", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "writes"), new MetricDetail("DiskIO", "Writes Per Interval", "transfers", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "writesmerged"), new MetricDetail("DiskIO", "Writes Merged Per Interval", "transfers", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "sectorswritten"), new MetricDetail("DiskIO", "Sectors Per Interval", "sectors", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "writetime"), new MetricDetail("DiskIO", "Time Spent Writing", "ms", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "inprogress"), new MetricDetail("IO", "IO In progress", "count", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "iotime"), new MetricDetail("IO", "Time Spent on IO", "ms", metricTypes.DELTA, 1));
		 allMetrics.put(CommandMetricUtils.mungeString("diskstats", "iotime_weighted"), new MetricDetail("IO", "Time Spent on IO (Weighted)", "ms", metricTypes.DELTA, 1));
		
		/*
		 * Parsers & declaration for 'iostat' command
		 */
		HashMap<Pattern, String[]> iostatMapping = new HashMap<Pattern, String[]>();
		// Getting CPU from top
		// iostatMapping.put(Pattern.compile("\\s*([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)"),
		//	new String[]{"%user", "%nice", "%system", "%iowait", "%steal", "%idle"});
		iostatMapping.put(Pattern.compile("(\\S+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+"
			+ "([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)"),
			new String[]{kColumnMetricPrefix, "rrqm-s", "wrqm-s", "r-s", "w-s", "rkB-s", "wkB-s", "avgrq-sz", "avgqu-sz", "await", "svctm", "%util"});
		iostatMapping.put(Pattern.compile("(\\S+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+"
			+ "([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)"),
			new String[]{kColumnMetricPrefix, "rrqm-s", "wrqm-s", "r-s", "w-s", "rkB-s", "wkB-s", "avgrq-sz", "avgqu-sz", "await", "r_await", "w_await", "svctm", "%util"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat", "-k", "-x", kExecutionDelay, kExecutionCount}, commandTypes.REGEXDIM, defaultignores, 0, iostatMapping));

		/* Getting these from Top
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%user"), new MetricDetail("CPU", "User", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%nice"), new MetricDetail("CPU", "Nice", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%system"), new MetricDetail("CPU", "System", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%iowait"), new MetricDetail("CPU", "Waiting", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%steal"), new MetricDetail("CPU", "Stolen", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%idle"), new MetricDetail("CPU", "Idle", "percent", metricTypes.NORMAL, 1));
		*/
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "rrqm-s"), new MetricDetail("DiskIO", "Queued Merged Read Requests", "requests", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "wrqm-s"), new MetricDetail("DiskIO", "Queued Merged Write Requests", "requests", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "r-s"), new MetricDetail("DiskIO", "Reads Per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "w-s"), new MetricDetail("DiskIO", "Writes Per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "rkB-s"), new MetricDetail("DiskIO", "Data Read Per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "wkB-s"), new MetricDetail("DiskIO", "Data Written Per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "avgrq-sz"), new MetricDetail("DiskIO", "Average Request Size", "sectors", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "avgqu-sz"), new MetricDetail("DiskIO", "Average Requests Queued", "requests", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "await"), new MetricDetail("DiskIO", "Average Response Time", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "r_await"), new MetricDetail("DiskIO", "Average Response Time (read)", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "w_await"), new MetricDetail("DiskIO", "Average Response Time (write)", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "svctm"), new MetricDetail("DiskIO", "Total Request Time", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%util"), new MetricDetail("DiskIO", "Percentage of Time Busy", "percent", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for "NetworkIO"
		 */
		HashMap<Pattern, String[]> networkIOMapping = new HashMap<Pattern, String[]>();
		networkIOMapping.put(Pattern.compile("\\/sys\\/class\\/net\\/[\\w\\d]+\\/statistics\\/([\\w_]+):(\\d+)"),
			new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("NetworkIO", new UnixCommand(new String[]{"grep", "-r", ".", "/sys/class/net/" + kMemberPlaceholder + "/statistics", "2>&1"}, 
				commandTypes.REGEXLISTDIM, defaultignores, 0, networkIOMapping));

		allMetrics.put(CommandMetricUtils.mungeString("networkio", "collisions"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "multicast"), new MetricDetail("Network", "Multicast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_bytes"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_compressed"), new MetricDetail("Network", "Receive/Compressed", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_crc_errors"), new MetricDetail("Network", "Receive/CRC Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_dropped"), new MetricDetail("Network", "Receive/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_errors"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_fifo_errors"), new MetricDetail("Network", "Receive/FIFO Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_frame_errors"), new MetricDetail("Network", "Receive/Frame Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_length_errors"), new MetricDetail("Network", "Receive/Length Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_missed_errors"), new MetricDetail("Network", "Receive/Missed Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_over_errors"), new MetricDetail("Network", "Receive/Overrun Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "rx_packets"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_aborted_errors"), new MetricDetail("Network", "Transmit/Aborted Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_bytes"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_carrier_errors"), new MetricDetail("Network", "Transmit/Carrier Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_compressed"), new MetricDetail("Network", "Transmit/Compressed", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_dropped"), new MetricDetail("Network", "Transmit/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_errors"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_fifo_errors"), new MetricDetail("Network", "Transmit/FIFO Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_heartbeat_errors"), new MetricDetail("Network", "Transmit/Heartbeat Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_packets"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("networkio", "tx_window_errors"), new MetricDetail("Network", "Transmit/Window Errors", "errors", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'netstat' command
		 * ** NOT USED IN FAVOR OF NETWORKIO **
		 */
		HashMap<Pattern, String[]> netstatMapping = new HashMap<Pattern, String[]>();
		netstatMapping.put(Pattern.compile("(\\w+\\d*)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
			new String[]{kColumnMetricPrefix, "MTU", "Met", "RX-OK", "RX-ERR", "RX-DRP", "RX-OVR", "TX-OK", "TX-ERR", "TX-DRP", "TX-OVR"});	
		allCommands.put("netstat", new UnixCommand(new String[]{"netstat", "-i"}, commandTypes.REGEXDIM, defaultignores, 0, netstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "MTU"), new MetricDetail("Network", "MTU", "packets", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Met"), new MetricDetail("Network", "Metric", "metric", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-OK"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-ERR"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-DRP"), new MetricDetail("Network", "Receive/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-OVR"), new MetricDetail("Network", "Receive/Overrun Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-OK"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-ERR"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-DRP"), new MetricDetail("Network", "Transmit/Drops", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-OVR"), new MetricDetail("Network", "Transmit/Overrun Errors", "errors", metricTypes.DELTA, 1));

		/*
		 * Parser & declaration for 'ps' command
		 */
		HashMap<Pattern, String[]> psMapping = new HashMap<Pattern, String[]>();
		psMapping.put(Pattern.compile("([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)\\s+(.*)"),
				new String[]{"%CPU", "%MEM", "RSS", kColumnMetricProcessName});
		allCommands.put("ps", new UnixCommand(new String[]{"ps", "-ewwo", "%cpu,%mem,rss,command"}, 
			commandTypes.REGEXDIM, defaultignores, 0, psMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("ps", kColumnMetricProcessName), new MetricDetail("Processes", "Instance Count", "processes", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%CPU"), new MetricDetail("Processes", "Aggregate CPU", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%MEM"), new MetricDetail("Processes", "Aggregate Memory", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "RSS"), new MetricDetail("Processes", "Aggregate Resident Size", "kb", metricTypes.INCREMENT, 1));
			
		/*
		 * Parsers & declaration for 'top' command
		 */		
		HashMap<Pattern, String[]> topMapping = new HashMap<Pattern, String[]>();
		topMapping.put(Pattern.compile("top.*load average:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+)"), 
			new String[]{"la1", "la5", "la15"});
		topMapping.put(Pattern.compile("Tasks:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+running,\\s+(\\d+)\\s+sleeping,\\s+(\\d+)\\s+stopped,\\s+(\\d+)\\s+zombie"), 
			new String[]{"proctot", "procrun", "proczzz", "procstop", "proczomb"});
		topMapping.put(Pattern.compile(".*Mem:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+buffers"), 
			new String[]{"memtot", "memused", "memfree", "membuff"});
		topMapping.put(Pattern.compile(".*Swap:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+cached"), 
			new String[]{"swaptot", "swapused", "swapfree", "swapbuff"});
		topMapping.put(Pattern.compile("KiB Mem:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+used,\\s+(\\d+)\\s+free,\\s+(\\d+)\\s+buffers"), 
				new String[]{"memtot", "memused", "memfree", "membuff"});
			topMapping.put(Pattern.compile("KiB Swap:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+used,\\s+(\\d+)\\s+free,\\s+(\\d+)\\s+cached"), 
				new String[]{"swaptot", "swapused", "swapfree", "swapbuff"});
		topMapping.put(Pattern.compile("[%]*Cpu[^:]*:\\s+([\\d\\.]+)%\\s*us,\\s+([\\d\\.]+)%\\s*sy,"
			 	+ "\\s+([\\d\\.]+)%\\s*ni,\\s+([\\d\\.]+)%\\s*id,"
			 	+ "\\s+([\\d\\.]+)%\\s*wa,\\s+([\\d\\.]+)%\\s*hi,"
			 	+ "\\s+([\\d\\.]+)%\\s*si,\\s+([\\d\\.]+)%\\s*st"),
			 	new String[]{"cpuus", "cpusy", "cpuni", "cpuid", "cpuwa", "cpuhi", "cpusi", "cpust"});

		allCommands.put("top", new UnixCommand(new String[]{"top","-b","-n","2"}, commandTypes.REGEXDIM, defaultignores, 5, topMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procrun"), new MetricDetail("Processes", "Running", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proczzz"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procstop"), new MetricDetail("Processes", "Stopped", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proczomb"), new MetricDetail("Processes", "Zombie", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memtot"), new MetricDetail("Memory", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memused"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfree"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "membuff"), new MetricDetail("Memory", "Buffer", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapused"), new MetricDetail("MemoryDetailed", "Swap/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapbuff"), new MetricDetail("MemoryDetailed", "Swap/Buffer", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuus"), new MetricDetail("CPU", "User", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuni"), new MetricDetail("CPU", "Nice", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpusy"), new MetricDetail("CPU", "System", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuwa"), new MetricDetail("CPU", "Waiting", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpust"), new MetricDetail("CPU", "Stolen", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuid"), new MetricDetail("CPU", "Idle", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuhi"), new MetricDetail("CPU", "Interrupt-Hardware", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpusi"), new MetricDetail("CPU", "Interrupt-Software", "percent", metricTypes.NORMAL, 1));
            
		/*
		 * Parsers & declaration for 'vmstat' command
		 */	
		HashMap<Pattern, String[]> vmstatMapping = new HashMap<Pattern, String[]>();
		vmstatMapping.put(Pattern.compile("\\s*(\\d+)\\s+(.*)"), new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat", "-s"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatMapping));
			
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "page swapped in"),new MetricDetail("Page", "Swapped In", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "page swapped out"),new MetricDetail("Page", "Swapped Out", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pages paged in"),new MetricDetail("Page", "Paged In", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pages paged out"),new MetricDetail("Page", "Paged Out", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "CPU context switches"),new MetricDetail("Faults", "CPU Context Switches", "switches", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "interrupts"),new MetricDetail("Faults", "Interrupts", "interrupts", metricTypes.DELTA, 1));
		
        /*
		 * Parsers & declaration for 'vmstat' command
		 */	
		HashMap<Pattern, String[]> vmstatKernelMapping = new HashMap<Pattern, String[]>();
		vmstatKernelMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+"),
			new String[]{"r", "b", "swpd", "free", "buff", "cache", "si", "so", "bi", "bo", "in", "cs"});
		allCommands.put("VmstatKernel", new UnixCommand(new String[]{"vmstat", "-s"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatKernelMapping));

		allMetrics.put(CommandMetricUtils.mungeString("VmstatKernel", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatKernel", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "swpd"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, getPageSize()*1024));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, getPageSize()*1024));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "buff"), new MetricDetail("Memory", "Buffer", "kb", metricTypes.NORMAL, getPageSize()*1024));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cache"), new MetricDetail("Memory", "Cache", "kb", metricTypes.NORMAL, getPageSize()*1024));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "si"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "so"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "bi"), new MetricDetail("IO", "Sent", "Blocks", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "bo"), new MetricDetail("IO", "Received", "Blocks", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		//allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		/*
		 * Skipping last 5 columns of vmstat for CPU measurement - using iostat instead.
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "percent", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "percent", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "percent", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "wa"), new MetricDetail("CPU", "Waiting", "percent", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "st"), new MetricDetail("CPU", "Stolen", "percent", metricTypes.NORMAL, 1));
	     */

		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEDIM, defaultignores));
	}
}
