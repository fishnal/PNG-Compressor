import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class PNGCompressor {
	private final static Map<String, String[]> OPTIONS_MAP = new HashMap<>();
	private final static Map<String, String> OPTIONS_VALUES = new HashMap<>();
	private final static String formatter;
	private final static String[] EMPTY_VALUES = new String[0];
	private final static String EMPTY_VALUE = "";

	static {
		OPTIONS_MAP.put("multi-core", EMPTY_VALUES);
		OPTIONS_MAP.put("mc", OPTIONS_MAP.get("multi-core"));
		StringBuilder fsb = new StringBuilder("%-0s");
		OPTIONS_MAP.forEach((k, v) -> {
		    OPTIONS_VALUES.put(k, null);
		    int len = k.length();
		    int a = fsb.indexOf("-") + 1;
		    int b = fsb.indexOf("s");
		    int currLen = Integer.parseInt(fsb.subSequence(a, b).toString());
		    if (len > currLen) {
			fsb.replace(a, b, "" + len + "");
		    }
		});
		formatter = fsb.toString() + " %s\n";
	}

	private static boolean verifyArguments(String[] args) {
		boolean bad = false;
		
		if (args.length < 2) {
			bad = true;
		} else {
			String in = args[args.length - 2];
			String out = args[args.length - 1];
			
			if (OPTIONS_MAP.containsKey(in) || OPTIONS_MAP.containsKey(out)) {
				bad = true;
			} else {
				mainLoop : for (int i = 0; i < args.length - 2; i += 2) {
					String option = args[i];
					
					if (!option.startsWith("--")) {
						System.out.println("invalid start of option: use \"--option-name\"");
						return false;
					}
					
					String[] validValues = OPTIONS_MAP.get(option = option.substring(2));
					
					if (validValues == null) {
						System.out.println(String.format("option \"%s\" does not exist", option));
						return false;
					}
					
					if (validValues != EMPTY_VALUES) {
						if (args[i + 1].equals(in)) {
							System.out.println(String.format("no value set for option \"%s\"", option));
							return false;
						}
						
						String value = args[i + 1];
						
						for (String vv : validValues) {
							if (!vv.equals(value)) continue;
							OPTIONS_VALUES.put(option, value);
							continue mainLoop;
						}
						
						System.out.println(String.format("value \"%s\" is not valid for option \"%s\"", value, option));
						continue;
					}
					
					OPTIONS_VALUES.put(option, EMPTY_VALUE);
					i--;
				}
			}
		}
		
		if (bad) {
			String usage = "usage: java -jar pngc.jar <options> [input directory] [output directory]\n";
			StringBuilder osb = new StringBuilder(String.format(formatter, "options", "values"));
			
			OPTIONS_MAP.forEach((k, v) -> {
				String vs = java.util.Arrays.toString(v);
				vs = vs.substring(1, vs.length() - 1);
				
				if ((vs = vs.replace(", ", "|")).isEmpty()) {
					vs = "NO VALUE NEEDED";
				}
				
				osb.append(String.format(formatter, k, vs));
			});
			
			String options = osb.toString();
			System.out.println(usage + options);
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		if (!verifyArguments(args)) {
			System.exit(2);
		}
		
		File PNGQUANT_PATH = new File("pngquant.exe");
		PNGQUANT_PATH.deleteOnExit();
		
		{
			FileOutputStream pngquantOS = new FileOutputStream(PNGQUANT_PATH);
			InputStream pngquantIS = PNGCompressor.class.getResourceAsStream("/pngquant.exe");
			pipe(pngquantIS, pngquantOS);
			pngquantIS.close();
			pngquantOS.close();
		}
		
		File dir = new File(args[args.length - 2]);
		
		if (!dir.exists()) {
			System.out.println(dir + " does not exist");
			System.exit(1);
		}
		
		int numberOfCores = OPTIONS_VALUES.get("multi-core") != null || OPTIONS_VALUES.get("mc") != null ? Runtime.getRuntime().availableProcessors() : 1;
		File[] files = dir.listFiles();
		
		for (File f : dir.listFiles()) {
			String string = f.getName();
			
			if (!string.endsWith(".png") || !f.isFile()) continue;
			
			File out = new File(args[args.length - 1]);
			out.mkdirs();
			out = new File(out + "/" + string);
			
			while (Thread.activeCount() >= 1 + numberOfCores);
			
			String command = String.format("\"%s\" --force --quality 50-50 \"%s\" --output \"%s\"", PNGQUANT_PATH, f, out);
			
			new Thread(() -> {
				try {
					Process process = Runtime.getRuntime().exec(command);
					process.waitFor();
					
					if (process.exitValue() != 0) {
						pipe(process.getErrorStream(), System.out);
						System.exit(3);
					}
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					System.exit(4);
				}
				
				System.out.println("COMPRESSED\t" + f);
			}).start();
		}
	}

	private static void pipe(InputStream is, OutputStream os) throws IOException {
		int data;
		
		while ((data = is.read()) != -1) {
			os.write(data);
			os.flush();
		}
	}
}
