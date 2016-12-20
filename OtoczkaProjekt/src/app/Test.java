package app;

import chan.Chan;
import divideandconquer.DivideAndConquer;
import graham.Graham;
import jarvis.Jarvis;
import quickhull.QuickHull;
import util.DataProvider;

public class Test {
	private static String[] args = null;
	
	public static void main(String[] args) {
		CHService service = null;
		Test.args = args;
		
		String alg = getProperty("alg");
		
		switch(alg) {
		case "GRAHAM":
			service = new Graham();
			break;
		case "JARVIS":
			service = new Jarvis();
			break;
		case "QH":
			service = new QuickHull();
			break;
		case "DAC":
			String k1 = getProperty("maxSubCHsize");
			service = new DivideAndConquer(Integer.parseInt(k1));
			break;
		case "CHAN":
			String k2 = getProperty("maxSubCHsize");
			String gen = getProperty("subCHgenerator");
			service = new Chan(Integer.parseInt(k2), Integer.parseInt(gen));
			break;
		default:
			printUsage();
		}
		
		String dataPath = getProperty("data");
		
		service.computeCH(new DataProvider().getPointsFromFile(dataPath));
	}
	
	private static void printUsage() {
		System.out.println("Proper usage: ");
		System.out.println("java -jar CH.jar data=<file> alg=<GRAHAM | JARVIS | QH | DAC | CHAN>");
		System.out.println("Additional properties: ");
		System.out.println("\tFor DAC:");
		System.out.println("\t\tmaxSubCHsize=<n> - max size of sub convex hull");
		System.out.println("\t    Chan:");
		System.out.println("\t\tmaxSubCHsize=<n> - max size of sub convex hull");
		System.out.println("\t\tsubCHgenerator=<1 | 2> - 1-random 2-clusters");
		System.exit(-1);
	}
	
	private static String getProperty(String propertyName) {
		for (String arg : args) {
			if (arg.startsWith(propertyName)) {
				String[] splitted = arg.split("=");
				if (splitted.length != 2) {
					printUsage();
				}
				return splitted[1];
			}
		}
		printUsage();
		return "";
	}
}
