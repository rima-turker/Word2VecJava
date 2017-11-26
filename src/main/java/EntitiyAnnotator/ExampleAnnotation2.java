package EntitiyAnnotator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import util.MapUtil;
import util.NERTagger;
import util.SentenceProcessing;
import util.StopWordRemoval;
import util.Permutator;
import util.Touple;

public class ExampleAnnotation2 
{
	private static final Logger LOG  = Logger.getLogger(ExampleAnnotation2.class);
	//private final Word2Vec model1 = WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/model_wiki_latest_1E3W.bin");
	//private final Word2Vec modelEntities =  WordVectorSerializer.readWord2VecModel("/home/rtue/PycharmProjects/word2vec-text8/model_wiki_latest_AllE2WM0.bin");
	
//	private final Word2Vec model1 = null;
//	private final Word2Vec modelEntities = null;
	
	private final Word2Vec model1 = null;// WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/model_wiki_latest_1E3W.bin");
	private final Word2Vec modelEntities = null;// WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/modelWikiLinkSentences_0.3_onlyLinks.bin");
		
	public void startAnnotation() 
	{
		long now = System.currentTimeMillis();
		//sentence = "Armstrong was an American astronaut engineer and the first person to walk on the Moon";
		String fileName = "/home/rtue/workspace/Word2VecJava/TestSentences";
		Integer ifile=0;
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			
			while ((line = br.readLine()) != null) 
			{
				//System.out.println(line);
				//System.out.println(ifile);
				ifile++;

				final String[] strSplit = NERTagger.getNERTags(line).split("\t");
				final List<Touple> list= new ArrayList<>();
				
				for (int i = 0; i < strSplit.length; i+=2) 
				{
					list.add(new Touple(strSplit[i], strSplit[i+1]));
					//System.out.println(strSplit[i]+" "+strSplit[i+1]);
				}

				final Map<Touple, HashSet<String>> mapEntCamd = new LinkedHashMap<>();

				for (Touple t: list){
					HashSet<String> candidateListType = EntityAnnotator.getCandidateListType(t.getA(),t.getB());
					if(!candidateListType.isEmpty()){
						mapEntCamd.put(t, candidateListType);
					}
				}
				
				String cleanCenter = StopWordRemoval.removeStopWords(line.toLowerCase());
				final List<String> tokens = SentenceProcessing.getSentenceList(cleanCenter);
				
				Permutator t = new Permutator();
				for(Entry<Touple, HashSet<String>> e:mapEntCamd.entrySet()){
					t.add(new LinkedList<>(e.getValue()));
				}
				
				List<String> listOfSemiColonSeparatedCandidates = t.run();
				
				Map<String,Double> result = new HashMap<>();
				for(String candidates:listOfSemiColonSeparatedCandidates){
					
					String[] splited = candidates.split(";");
					
					double[] entitySimilarities = canlculateEntitySimilairties(splited);
					double[] tokenSimilarities = canlculateTokenSimilairties(splited,tokens);
					
					double average = aggregateSimilarities(entitySimilarities,tokenSimilarities);
					
					if(Double.isNaN(average)){
						average = 0;
					}
					result.put(candidates, average);
				}
				Map<String, Double> sortByValueDescending = MapUtil.sortByValueDescending(result);
				
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			              new FileOutputStream("finalResult"+File.separator+ifile.toString()), "utf-8"))) {
				
				for(Entry<String, Double> e:sortByValueDescending.entrySet()){
					 writer.write(e.getKey()+" "+ e.getValue());
					 writer.write("\n");
				}
				
				for(Entry<String, Double> e:sortByValueDescending.entrySet()){
					
					LOG.info(e);
				}
				LOG.info("--------------------------------------------------------------------------------------------------------");
			  
			}
				
			}
			br.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-now));
	}

	private double aggregateSimilarities(double[] entitySimilarities, double[] tokenSimilarities) {
		double sum = 0;
		int countNumbers=0;
		
		for(double a:entitySimilarities){
			if(!Double.isNaN(a)){
				sum+=a;
				countNumbers++;
			}
		}
		
		for(double a:tokenSimilarities){
			if(!Double.isNaN(a)){
				sum+=a;
				countNumbers++;
			}
		}
		
//		return sum/(entitySimilarities.length+tokenSimilarities.length);
		return sum/(countNumbers);
	}

	private double[] canlculateTokenSimilairties(String[] candidates, List<String> tokens) {
		double[] result = new double[candidates.length*tokens.size()];
		int k = 0;
		for(String entity:candidates){
			for(String token:tokens){
				//if (model1.hasWord("dbr:"+entity)&&modelEntities.hasWord(token)) {
					result[k++] = model1.similarity("dbr:"+entity, token);
				//}
			}
		}
		return result;
	}

	private double[] canlculateEntitySimilairties(String[] candidates) {
		
		double[] result = new double[(candidates.length*(candidates.length-1))/2];
		int  k = 0;
		for(int i=0;i<candidates.length;i++){
			for(int j=i+1;j<candidates.length;j++){
				//if (modelEntities.hasWord(candidates[i])&&modelEntities.hasWord(candidates[j])) {

					result[k++] = modelEntities.similarity(candidates[i], candidates[j]);
					//modelEntities.ne
//					if (Double.isNaN(result[k-1])) 
//					{
//						System.out.println(candidates[i] +" "+candidates[j]);
//					}
				//}
			}
		}
		return result;
	}

}
