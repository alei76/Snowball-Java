package clustering;

import java.io.IOException;
import java.util.LinkedList;

import tuples.Tuple;
import bin.Config;

public class Singlepass {
		
	public static void singlePassTFIDF(LinkedList<Tuple> tuples, LinkedList<SnowballPattern> patterns) throws IOException {
		System.out.println(tuples.size() + " tuples to process");		
		int count = 0;
				
		// initialize: first tuple is first cluster
		SnowballPattern c1 = new SnowballPattern(tuples.get(0));
		patterns.add(c1);		
		
		// calculate similarity with each cluster centroid */
		for (int i = 1; i < tuples.size(); i++) {
			double max_similarity = 0;
			int max_similarity_cluster_index = 0;
			if (count % 100 == 0) System.out.print(".");			
			for (int j = 0; j < patterns.size(); j++) {				
				SnowballPattern c = patterns.get(j);				
				double similarity = tuples.get(i).degreeMatchCosTFIDF(c.left_centroid, c.middle_centroid, c.right_centroid);
				if (similarity>max_similarity) {					
					max_similarity = similarity;
					max_similarity_cluster_index = j;
				}
			}
			
			// if max_similarity < min_degree_match create new cluster/patterns having this tuple as the centroid */			
			if (max_similarity<Config.parameters.get("min_degree_match")) {
				SnowballPattern c = new SnowballPattern(tuples.get(i));
				patterns.add(c);
			}
			// if max_similarity >= min_degree_match add to pattern to the cluster and recalculate centroid */ 			
			else {				
				patterns.get(max_similarity_cluster_index).addTuple(tuples.get(i));
				patterns.get(max_similarity_cluster_index).calculateCentroidTFIDF("left");
				patterns.get(max_similarity_cluster_index).calculateCentroidTFIDF("middle");
				patterns.get(max_similarity_cluster_index).calculateCentroidTFIDF("right");
			}
		count++;
		}
		System.out.println();		
	}
	
	
	public static void singlePassWord2Vec(LinkedList<Tuple> tuples, LinkedList<SnowballPattern> patterns) throws IOException {
		
		System.out.println(tuples.size() + " tuples to process");		
		int count = 0;
		
		// initialize: first tuple is first cluster
		SnowballPattern c1 = new SnowballPattern(tuples.get(0));
		patterns.add(c1);
		
		// calculate similarity with each cluster centroid */
		for (int i = 1; i < tuples.size(); i++) {
			double max_similarity = 0;
			int max_similarity_cluster_index = 0;
			if (count % 100 == 0) System.out.print(".");			
			for (int j = 0; j < patterns.size(); j++) {				
				SnowballPattern c = patterns.get(j);				
				double similarity = tuples.get(i).degreeMatchWord2Vec(c.w2v_left_sum_centroid, c.w2v_middle_sum_centroid, c.w2v_right_sum_centroid);
				if (similarity > max_similarity) {					
					max_similarity = similarity;
					max_similarity_cluster_index = j;
				}
			}
			
			// if max_similarity < min_degree_match create new cluster/patterns having this tuple as the centroid */			
			if (max_similarity<Config.parameters.get("min_degree_match")) {
				SnowballPattern c = new SnowballPattern(tuples.get(i));
				patterns.add(c);
			}
			// if max_similarity >= min_degree_match add to pattern to the cluster and recalculate centroid */ 			
			else {				
				patterns.get(max_similarity_cluster_index).addTuple(tuples.get(i));
				patterns.get(max_similarity_cluster_index).calculateCentroidWord2Vec();
				
			}
		count++;
		}
		System.out.println();		
	}
}


