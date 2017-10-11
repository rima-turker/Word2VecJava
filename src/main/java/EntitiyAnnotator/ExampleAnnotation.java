package EntitiyAnnotator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import util.NERTagger;

public class ExampleAnnotation 
{
	private static final Logger LOG  = Logger.getLogger(ExampleAnnotation.class);

	public void startAnnotation(String sentence) 
	{
		sentence = "Armstrong (August 5, 1930 – August 25, 2012) was an American astronaut, engineer, and the first person to walk on the Moon.";
		//		//David and Victoria added spice to their marriage

		String[] strSplit = NERTagger.getNERTags(sentence).split(" ");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < strSplit.length; i+=2) 
		{
			map.put(strSplit[i], strSplit[i+1]);
			System.out.println(strSplit[i]+ strSplit[i+1]);
		}

		Map<String, HashSet<String>> mapEntCamd = new HashMap<String, HashSet<String>>();

		for (Entry<String, String> entry:map.entrySet()){
			mapEntCamd.put(entry.getKey(), EntityAnnotator.getCandidateListType(entry.getKey(),entry.getValue()));

		}
		for (Entry<String, HashSet<String>> entry:mapEntCamd.entrySet())
		{
			HashSet<String> setCandidates = new HashSet<>(entry.getValue());
			System.out.println(entry.getKey()+" "+setCandidates.size());
			for (String str: setCandidates) 
			{
				System.out.println(str.replace("http://dbpedia.org/resource/", "").toLowerCase());
			}
			
		}
		
		
		for (Entry<String, HashSet<String>> entry:mapEntCamd.entrySet())
		{
			Map<Integer, String> mapPopEnt = new HashMap<Integer, String>();
			HashSet<String> setCandidates = new HashSet<>(entry.getValue());
			for (String can: setCandidates) 
			{
				mapPopEnt.put(Integer.parseInt(Caller.runPopularity(can)), can);
			}
			
			LinkedHashSet<String> lsetCandidates = new LinkedHashSet<>(getMostFam(5, mapPopEnt));
			
			for(String str:lsetCandidates)
			{
				System.out.println(str);
			}

		}


		//		GetAnnonation an = new GetAnnonation();
		//		Map<String, String> map = new HashMap<String, String>(an.getCandidateListType("Einstein", ""));
		//		
		//		
		//		System.out.println(map.size());
		//		for (Entry<String, String> entry: map.entrySet()){
		//			
		//			if (entry.getValue().equals("Person")) 
		//			{
		//				String name = entry.getKey();
		//				
		//				   
		//		            System.out.println(entry.getKey()+">}");  
		//			}
	}

	private LinkedHashSet<String> getMostFam(int topN,Map<Integer, String> mapPopEnt) 
	{
		Map<Integer, String> map = new TreeMap<Integer, String>(mapPopEnt); 
		LinkedHashSet<String> lset = new LinkedHashSet<>();
		int count =0;
		
		for (Entry<Integer, String> entry:map.entrySet())
		{
			lset.add(entry.getValue());
			count++;
			if (count>topN) 
			{
				break;
			}
		}
		return lset;
	}


}
