package clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tuples.Seed;
import tuples.SnowballTuple;
import utils.SortMaps;
import bin.SnowballConfig;

public class SnowballPattern {
	
	public Set<SnowballTuple> tuples;
	public Set<List<String>> patterns = new HashSet<List<String>>();
	
	// Expanded pattern
	public boolean expanded = false;
	
	// TF-IDF representations
	public Map<String,Double> left_centroid = new HashMap<String, Double>();
	public Map<String,Double> middle_centroid = new HashMap<String, Double>();
	public Map<String,Double> right_centroid = new HashMap<String, Double>();
	
	public int positive = 0;
	public int negative = 0;	
	public double confidence = 0;
	public double confidence_old = 0;
	public double RlogF = 0;	
	public double RlogF_old = 0;
	
	// Create a new cluster with just one tuple, which will be the centroid
	public SnowballPattern(SnowballTuple tuple){ 
		tuples = new HashSet<SnowballTuple>();	
		
		this.tuples.add(tuple);
		
		left_centroid = tuple.left;
		middle_centroid = tuple.middle;
		right_centroid = tuple.right;
	}
		
	public SnowballPattern() {
		super();
		tuples = new HashSet<SnowballTuple>();
	}
	
	public void updateConfidencePattern(){
		if (SnowballConfig.use_RlogF) {
			confidence = this.RlogF * SnowballConfig.wUpdt + this.RlogF_old * (1 - SnowballConfig.wUpdt);
		}
		else {
			confidence = this.confidence * SnowballConfig.wUpdt + this.confidence_old * (1 - SnowballConfig.wUpdt);
		}		
	}

	public void updatePatternSelectivity(String e1, String e2) {
		for (Seed s : SnowballConfig.seedTuples) {
			if (s.e1.equals(e1.trim()) || s.e1.trim().equals(e1.trim())) {
				if (s.e2.equals(e2.trim()) || s.e2.trim().equals(e2.trim())) {
					positive++;
				}
				else negative++;
			}
		}
	}
	
	public String getWords(Map<String,Double> words) {
		// put keys and values in to an arraylist using entryset
		ArrayList myArrayList=new ArrayList(words.entrySet());
	
		// sort the values based on values first and then keys.
		Collections.sort(myArrayList, new SortMaps.StringDoubleComparator());
		
		// show sorted results
		Iterator itr=myArrayList.iterator();
		String key="";
		double value=0;
		int cnt=0;
		StringBuffer output = new StringBuffer();
		while(itr.hasNext()){	
			cnt++;
			Map.Entry<String,Double> e=(Map.Entry<String,Double>)itr.next();		
			key = e.getKey();
			value = e.getValue().doubleValue();
			output.append(key+", "+value+'\n');
		}
		return output.toString();
		
	}
	
	public String printPattern() {
		StringBuffer out = new StringBuffer();
		out.append("confidence: " + this.confidence + "\n");
		out.append("#tuples: " + tuples.size() + "\n");
		out.append("LEFT:\n");
		out.append(getWords(left_centroid));
		out.append("\n");		
		out.append("MIDDLE:\n");
		out.append(getWords(middle_centroid));
		out.append("\n");		
		out.append("RIGHT:\n");
		out.append(getWords(right_centroid));
		out.append("\n\n");
		return out.toString();
	}
	
	public void calculateCentroidTFIDF(String vector) {
		//get the tuple with the highest number of words
		int max_size = 0;
		Map<String,Double> max_t = null;
		
		if (vector.equals("left")) {
			for (SnowballTuple tuple : tuples) {				
				if (tuple.left.keySet().size()>max_size) {
					max_t = tuple.left;
					max_size = tuple.left.size();
				}
			}
		}

		if (vector.equals("middle")) {
			for (SnowballTuple tuple : tuples) {
				if (tuple.middle.keySet().size()>max_size) {
					max_t = tuple.middle;
					max_size = tuple.middle.size();
				}
			}
		}
		if (vector.equals("right")) {
			for (SnowballTuple tuple : tuples) {
				if (tuple.right.keySet().size()>max_size) {
					max_t = tuple.middle;
					max_size = tuple.right.size();
				}
			}
		}
		
		// Creates a new centroid: get the current value of each word and add it to the centroid
		// or if word is not in centroid create a new entry for that word, with the score of the word in the tuple where it was found
		Map<String,Double> centroid_t = new HashMap<String, Double>();
		Set<String> max_keys = null;
		
		if (max_t!=null) {
			max_keys = max_t.keySet();
			Set<String> t_keys = null;
			Map<String,Double> t_vector = null;

			for (SnowballTuple t : tuples) {
				
				if (vector.equals("left")) {
					t_keys = t.left.keySet();
					t_vector = t.left;
				}
				
				if (vector.equals("middle")) {
					t_keys = t.middle.keySet();
					t_vector = t.middle;
				}
				
				if (vector.equals("right")) {
					t_keys = t.right.keySet();
					t_vector = t.right;
				}
				
				for (String word : max_keys) {
					if (t_keys.contains(word)) {
						Double current = centroid_t.get(word);
						if (current==null) centroid_t.put(word,t_vector.get(word));
						else {
							current += t_vector.get(word);
							centroid_t.put(word, current);
						}
					}
				}
			}		
		}
		
		//divide all entries of the centroid by the total number of tuples
		if (centroid_t.size()>0) {
			Set<String> centroid_keys = centroid_t.keySet();
			for (String word : centroid_keys) {
				Double v = centroid_t.get(word);
				v = v / (double) centroid_keys.size();
				centroid_t.put(word, v);
			}			
			if (vector.equals("left")) this.left_centroid = centroid_t;
			if (vector.equals("middle")) this.middle_centroid = centroid_t;
			if (vector.equals("right")) this.right_centroid = centroid_t;			
		}
	}
		
	public double confidence(){
		double conf = 0;
		if ((this.positive + this.negative)>0) {
			conf = (double) this.positive / (double) (this.positive + this.negative);
			this.confidence = conf; 
		}
		return conf;
	}
	
	// the RlogF confidence of pattern P/
	public void ConfidencePatternRlogF() {		
		if (this.confidence>0) {
			this.RlogF = this.confidence*(1+(Math.log(this.positive)/(Math.log(2))));
		}
		else this.RlogF = 0;		
	}
	
	public void addTuple(SnowballTuple t){
		tuples.add(t);
	}	
}
