package bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import nlp.PortugueseTokenizer;
import nlp.Stopwords;
import tuples.Seed;
import vsm.VectorSpaceModel;

public class SnowballConfig {
		
	public static PortugueseTokenizer PTtokenizer;	
	public static VectorSpaceModel vsm = null;
	
	public static String e1_type = null;
	public static String e2_type = null;
	public static Set<Seed> seedTuples = new HashSet<Seed>();	
	
	/* General configuration parameters */
	public static int max_tokens_away;
	public static int min_tokens_away;
	public static int context_window_size;
		
	public static double min_degree_match;
	public static double min_tuple_confidence;
	public static int min_pattern_support;
	
	public static double weight_left_context;
	public static double weight_middle_context;
	public static double weight_right_context;
	
	public static int number_iterations;
	public static double wUpdt;
	public static boolean use_RlogF;
	public static String stopwords;

	public static void init(String parameters, String sentencesFile) throws Exception {		
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader( new File(parameters)) );
			String line = null;
			try {
				while ( ( line = f.readLine() ) != null) {					
					if (line.isEmpty() || line.startsWith("#")) continue;					
					if (line.startsWith("max_tokens_away")) max_tokens_away = Integer.parseInt(line.split("=")[1]);						
					if (line.startsWith("min_tokens_away")) min_tokens_away = Integer.parseInt(line.split("=")[1]);						
					if (line.startsWith("context_window_size")) context_window_size = Integer.parseInt(line.split("=")[1]);					
					if (line.startsWith("weight_left_context")) weight_left_context = Double.parseDouble(line.split("=")[1]);
					if (line.startsWith("weight_middle_context")) weight_middle_context = Double.parseDouble(line.split("=")[1]);
					if (line.startsWith("weight_right_context")) weight_right_context = Double.parseDouble(line.split("=")[1]);					
					if (line.startsWith("min_pattern_support")) min_pattern_support = Integer.parseInt(line.split("=")[1]);										
					if (line.startsWith("min_degree_match")) min_degree_match = Double.parseDouble(line.split("=")[1]);					
					if (line.startsWith("min_tuple_confidence")) min_tuple_confidence = Double.parseDouble(line.split("=")[1]);					
					if (line.startsWith("wUpdt")) wUpdt = Double.parseDouble(line.split("=")[1]);
					if (line.startsWith("number_iterations")) number_iterations = Integer.parseInt(line.split("=")[1]);
					if (line.startsWith("use_RlogF")) use_RlogF = Boolean.parseBoolean(line.split("=")[1]);					
					if (line.startsWith("stopwords")) SnowballConfig.stopwords = line.split("=")[1];
				}				
			} catch (IOException e) {
				System.out.println("I/O error reading paramters.cfg");
				e.printStackTrace();
				System.exit(0);
			}
		} catch (FileNotFoundException e1) {
			System.out.println("paramters.cfg not found");
			System.exit(0);
		}
			
		// Initialize a Tokenizer and load Stopwords		
		PTtokenizer = new PortugueseTokenizer();				
		System.out.print("Loading stopwords ...");
		try {
			Stopwords.loadStopWords(SnowballConfig.stopwords);
		} catch (IOException e) {
			System.out.println("Stopwords file not found!");
			System.exit(0);
		}		
		System.out.println("done");
		// Vector Space Model, TF-IDF: calculate vocabulary term overall frequency
		try {
			calculateTF(sentencesFile);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
		
	// Read seed instances from file
	static void readSeeds(String seedsFile){
		BufferedReader f = null;
		try {
			f = new BufferedReader(new FileReader( new File(seedsFile)));
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
			System.exit(0);
		}
		String line = null;
		String e1 = null;
		String e2 = null;
		Seed seed = null;
		try {
			while ( ( line = f.readLine() ) != null) {
				try {
					if (line.startsWith("#") || line.isEmpty()) continue;
					if (line.startsWith("e1")) e1_type = line.split(":")[1];
					else if (line.startsWith("e2")) e2_type = line.split(":")[1];					
					else {
						e1 = line.split(";")[0];
						e2 = line.split(";")[1];
						seed = new Seed(e1, e2);
						seedTuples.add(seed);						
					}					
				} catch (Exception e) {
					System.out.println("Error parsing: " + line);
					e.printStackTrace();
					System.exit(0);
				}				
			}
			f.close();
		} catch (IOException e) {
			System.out.println("I/O error");
			e.printStackTrace();
		}
	}

	// Calculate terms frequency over whole document collection
	static void calculateTF(String sentencesFile) throws IOException, FileNotFoundException, ClassNotFoundException {
		VectorSpaceModel vsm;
		File f = new File("vsm.obj");
		if (!f.exists()) {
			System.out.println("Calculating TF for each term");			
			vsm = new VectorSpaceModel(sentencesFile);
			SnowballConfig.vsm = vsm;
			System.out.println("\nTF-IDF vocabulary size: " + SnowballConfig.vsm.term_document_frequency.keySet().size());
			
			try {
				// Save to disk
				FileOutputStream out = new FileOutputStream("vsm.obj");
				ObjectOutputStream oo = new ObjectOutputStream(out);
				oo.writeObject(vsm);
				oo.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			// Load an already calculated Term Frequency
			System.out.println("Loading already calculated TF from disk");
			FileInputStream in = new FileInputStream("vsm.obj");
			ObjectInputStream objectInput = new ObjectInputStream(in);
			vsm = (VectorSpaceModel) objectInput.readObject();
			SnowballConfig.vsm = vsm;
			System.out.println("TF-IDF vocabulary size: " + SnowballConfig.vsm.term_document_frequency.keySet().size());						
			in.close();			
		}
	}
}