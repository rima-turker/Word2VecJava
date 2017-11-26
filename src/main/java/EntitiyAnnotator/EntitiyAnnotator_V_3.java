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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

public class EntitiyAnnotator_V_3<E> 
{
	private static final Logger LOG  = Logger.getLogger(EntitiyAnnotator_V_3.class);
	//private final Word2Vec model1 = WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/model_wiki_latest_1E3W.bin");
	//private final Word2Vec modelEntities =  WordVectorSerializer.readWord2VecModel("/home/rtue/PycharmProjects/word2vec-text8/model_wiki_latest_AllE2WM0.bin");

	//	private final Word2Vec model1 = null;
	//	private final Word2Vec modelEntities = null;

	private final Word2Vec model_EntityAnchor = null;//WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecModels/model_wiki_1E1A_5pad_2W_skip.bin");
	private final Word2Vec modelEntities = null;//WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/model_wiki_AllEntiesInSentence_7P_3W_skip.bin");


	public void getAnnotation() 
	{
		int numOfCand=10;
		long now = System.currentTimeMillis();
		for (int j = 1; j <=10; j++) {


			String resourceDic = "/home/rtue/workspace/Word2VecJava/MSNBC/CleanDocuments/";
			String file_sentences = resourceDic+j+"_sentences";
			String file_candidates = resourceDic+j+"_candidates";
			String file_annotations = resourceDic+j+"_annotations";

			Integer ifile=0;
			try 
			{
				BufferedReader br = new BufferedReader(new FileReader(file_candidates));
				String line;
				final HashMap<String,HashSet<String>> hmapCandidates = new LinkedHashMap();
				while ((line = br.readLine()) != null) 
				{
					String key = line;
					HashSet<String> setCand = new HashSet<>();
					line = br.readLine();
					String[] split = line.split(" ");
					
					for (int i = 0; i < split.length-1; i++) {
						setCand.add(split[i]);
					}
					
					hmapCandidates.put(key, setCand);
				}
				br.close();

				br = new BufferedReader(new FileReader(file_annotations));
				HashMap<String,String> hmapAnnotations = new HashMap();
				while ((line = br.readLine()) != null) 
				{
					String [] split = line.split("http://en.wikipedia.org/wiki/");
					for (int i = 0; i < split.length-1; i++) 
					{
						
					}
					hmapAnnotations.put(line.split(" ")[0].toLowerCase(), line.split(" ")[split.length].replace("ttp://en.wikipedia.org/wiki/", "").toLowerCase());
				}
				br.close();

				br = new BufferedReader(new FileReader(file_sentences));
				HashSet<String> hset = new HashSet<>();

				while ((line = br.readLine()) != null) 
				{
					System.out.println(line);
					//System.out.println(ifile);
					ifile++;

					final String[] strSplit = NERTagger.getNERTags(line).split("\t");
					final List<Touple> list= new ArrayList<>();
					
					for (int i = 0; i < strSplit.length; i+=2) 
					{
						list.add(new Touple(strSplit[i], strSplit[i+1]));
						System.out.println(strSplit[i]+" "+strSplit[i+1]);
					}

					final Map<Touple, HashSet<String>> mapEntCamd = new LinkedHashMap<>();

					for (Touple t: list){
						//HashSet<String> candidateListType = EntityAnnotator.getCandidateListType(t.getA(),t.getB());
						String key = t.getA().toLowerCase();
						String finalKey= "";
						
						if (key.split(" ").length>1) 
						{
							
							String[] split = key.split(" ");
							for (int i = 0; i < split.length; i++) 
							{
								finalKey=finalKey.concat(split[i]+"_");
							}
							finalKey=finalKey.substring(0,finalKey.length()-1);
							
						}
						else
							finalKey=key;
						
						if (!hmapCandidates.containsKey(finalKey)) 
						{
							System.out.println("ERROR");
							for (Entry<String,HashSet<String>> enry : hmapCandidates.entrySet()) 
							{
								System.out.println(enry.getKey());
							}
						}
						HashSet<String> candidateListType = hmapCandidates.get(finalKey);
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
		}
		System.err.println(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-now));
	}


	public void getEntityList() 
	{
		long now = System.currentTimeMillis();
		String fileName = "/home/rtue/workspace/Word2VecJava/MSNBC/RawTextsSimpleChars_utf8/USN16444287.txt";
		Integer ifile=0;
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			HashSet<String> hset = new HashSet<>();
			while ((line = br.readLine()) != null) 
			{
				//System.out.println(line);
				//System.out.println(ifile);
				ifile++;

				final String[] strSplit = NERTagger.getNERTags(line).split("\t");
				if (strSplit.length>2) {


					final List<Touple> list= new ArrayList<>();

					for (int i = 0; i < strSplit.length; i+=2) 
					{
						list.add(new Touple(strSplit[i], strSplit[i+1]));
						System.out.print(strSplit[i]+" "+strSplit[i+1]+ "\t");
						hset.add(strSplit[i].toLowerCase());
					}
					System.out.println();

				}

				//List<Touple> listFiltered = new  ArrayList<>(findEasyEntities(list));

				//				final Map<Touple, HashSet<String>> mapEntCamd = new LinkedHashMap<>();
				//
				//				for (Touple t: list){
				//					HashSet<String> candidateListType = EntityAnnotator.getCandidateListType(t.getA(),t.getB());
				//					if(!candidateListType.isEmpty()){
				//						mapEntCamd.put(t, candidateListType);
				//					}
				//				}
				//
				//				String cleanCenter = StopWordRemoval.removeStopWords(line.toLowerCase());
				//				final List<String> tokens = SentenceProcessing.getSentenceList(cleanCenter);
				//
				//				Permutator t = new Permutator();
				//				for(Entry<Touple, HashSet<String>> e:mapEntCamd.entrySet()){
				//					t.add(new LinkedList<>(e.getValue()));
				//				}
				//
				//				List<String> listOfSemiColonSeparatedCandidates = t.run();
				//
				//				Map<String,Double> result = new HashMap<>();
				//				for(String candidates:listOfSemiColonSeparatedCandidates){
				//
				//					String[] splited = candidates.split(";");
				//
				//					double[] entitySimilarities = canlculateEntitySimilairties(splited);
				//					double[] tokenSimilarities = canlculateTokenSimilairties(splited,tokens);
				//
				//					double average = aggregateSimilarities(entitySimilarities,tokenSimilarities);
				//
				//					if(Double.isNaN(average)){
				//						average = 0;
				//					}
				//					result.put(candidates, average);
				//				}
				//				Map<String, Double> sortByValueDescending = MapUtil.sortByValueDescending(result);
				//
				//				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				//						new FileOutputStream("finalResult"+File.separator+ifile.toString()), "utf-8"))) {
				//
				//					for(Entry<String, Double> e:sortByValueDescending.entrySet()){
				//						writer.write(e.getKey()+" "+ e.getValue());
				//						writer.write("\n");
				//					}
				//
				//					for(Entry<String, Double> e:sortByValueDescending.entrySet()){
				//
				//						LOG.info(e);
				//					}
				//					LOG.info("--------------------------------------------------------------------------------------------------------");
				//
				//				}

			}
			for(String str:hset){

				if (str.split(" ").length>1) 
				{
					String newstr = "";
					String[] split = str.split(" ");
					for (int i = 0; i < split.length; i++) 
					{
						newstr=newstr.concat(split[i]+"_");
					}
					newstr=newstr.substring(0,newstr.length()-1);
					System.out.println(newstr);
				}
				else
					System.out.println(str);
			}

			br.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-now));
	}

	private List<Touple> findEasyEntities(List<Touple> list) 
	{

		final List<Touple> resultList= new ArrayList<>();

		for (int i = 0; i < list.size(); i++) 
		{
			String entity = list.get(i).getA().toLowerCase();
			Collection<String> similarWords = model_EntityAnchor.wordsNearest(entity, 50);
			int count =0;
			HashSet<String> similarEntities = new HashSet<>();
			for (Iterator iterator = similarWords.iterator(); iterator.hasNext();) {
				String it = (String) iterator.next();
				if (it.contains("dbr:")) {
					similarEntities.add(it);
					count++;
				}
				if (count>9) {
					break;
				}
			}

			//			Levenshtein levenshtein = new Levenshtein();
			//			for (String simEnt : similarEntities) {
			//				System.out.println(simEnt+" "+ entity);
			//				System.out.println(levenshtein.distance(simEnt, entity));
			//			}




		}
		return resultList;
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
				//result[k++] = model1.similarity("dbr:"+entity, token);
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
