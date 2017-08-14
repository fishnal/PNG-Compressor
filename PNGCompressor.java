import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class PNGCompressor {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("usage: java -jar pngc.jar [input directory] [output directory]");
			System.exit(1);
		}

		// retrieve pngquant.exe from the jar file, store as a temporary executable file as 
		File PNGQUANT_PATH = new File("pngquant.exe");
		PNGQUANT_PATH.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(PNGQUANT_PATH);
		InputStream is = PNGCompressor.class.getResourceAsStream("/pngquant.exe");
		pipe(is, fos);
		is.close();
		fos.close();

		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		for (File f : files) {
			String name = f.getName();
			if (!name.endsWith(".png") || !f.isFile()) {
				continue;
			}
			File out = new File(args[1]);
			out.mkdirs();
			out = new File(out + "/" + name);

			while (Thread.activeCount() >= 1 + Runtime.getRuntime().availableProcessors());

			String command = String.format("\"%s\" --force --quality 50-50 \"%s\" --output \"%s\"", PNGQUANT_PATH, f, out);
			new Thread(() -> {
				try {
					Process p = Runtime.getRuntime().exec(command);
					p.waitFor();
					if (p.exitValue() != 0) {
						InputStream es = p.getErrorStream();
						int data;
						while ((data = es.read()) != -1) {
							System.out.write(data);
							System.out.flush();
						}
						System.exit(2);
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
					System.exit(3);
				}

				System.out.println(f);
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