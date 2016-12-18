import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is a image conversion program using reflection
 * which dynamically loads olink.jar and export image.
 *  
 * @author gajendra
 *
 */
public class MyExporter {

	private final File olinkJar;

	/**
	 * The Constructor expects path to OutsideIn olink.jar
	 * 
	 * @param pathToOutsideInOlinkJar
	 */
	public MyExporter(String pathToOutsideInOlinkJar) {
		olinkJar = new File(pathToOutsideInOlinkJar);
	}


	/**
	 * Convert any image to PNG format
	 * 
	 * @param inputFilePath
	 * @param outputFilePath
	 * @throws Exception
	 */
	public void exportToPNG(String inputFilePath, String outputFilePath) throws Exception {

		System.out.println("Started exporting " + inputFilePath + " to " + outputFilePath);
		File inputFile = new File(inputFilePath);
		File outputFile = new File(outputFilePath);

		/*URLClassLoader child = new URLClassLoader (new URL[]{olinkJar.toURI().toURL()},
				this.getClass().getClassLoader());*/

		URLClassLoader child = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
		addURL.invoke(child, olinkJar.toURI().toURL());

		Class<?> oinClass = Class.forName ("com.oracle.outsidein.OutsideIn", true, child);
		Class<?> exporterClass = Class.forName ("com.oracle.outsidein.Exporter", true, child);
		Class<?> fileFormatClass = Class.forName ("com.oracle.outsidein.FileFormat", true, child);

		Method method = oinClass.getMethod ("newLocalExporter");
		Object exporter = method.invoke(null);

		Method setSourceFile = exporterClass.getMethod("setSourceFile", File.class);
		setSourceFile.invoke(exporter, inputFile);

		Method setDestinationFile = exporterClass.getMethod("setDestinationFile", File.class);
		setDestinationFile.invoke(exporter, outputFile);

		Method setDestinationFormat = exporterClass.getMethod("setDestinationFormat", fileFormatClass);
		Field pngFormatField = fileFormatClass.getField("FI_PNG");
		Object pngFormat = pngFormatField.get(null);
		setDestinationFormat.invoke(exporter, pngFormat);

		Method export = exporterClass.getMethod("export");
		export.invoke(exporter);

		System.out.println("Export complete.");

	}
}
