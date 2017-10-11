package EntitiyAnnotator;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class EntityAnnotator 
{
	

	public static HashSet<String> getCandidateListType(String surfaceForm, String type) 
	{
		//Map<String, String>  mapResult = new HashMap<String, String>();
		HashSet<String> setResult = new HashSet<String>();
		if(type.equals("<LOCATION>")){
			type= "<PLACE>";
		}
		Request r = new Request();
		r.setQuery(surfaceForm);
		r.setDataFormat(DataForomat.JSONN);

		String result = Caller.runYovisto(r);
		//String result = Caller.runDBpedia(r);

		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonNode rootNode = mapper.readTree(result);

			int size = rootNode.get("entities").size();
			Entity[] array = new Entity[size];
			for(int i=0;i<size;i++) {
				JsonNode jsonNode = rootNode.get("entities").get(i);
				Entity object = mapper.readValue(jsonNode.toString(), Entity.class);
				array[i] = object;
				
				if (array[i].iri.contains("http://dbpedia.org/resource/")) 
				{
					if (array[i].categoryIri.toLowerCase().equals(type.replaceAll(">", "").replaceAll("<", "").toLowerCase())){
						setResult.add(array[i].iri.replaceAll("http://dbpedia.org/resource/", "").toLowerCase());
					}
				}
//				System.out.println(array[i].label+" "+array[i].categoryIri);
//				System.out.println(type.replaceAll(">", "").replaceAll("<", "").toLowerCase());
				//(mapResult.put(array[i].label, array[i].categoryIri);
//				 
//				{
//					//System.out.println(array[i].iri);
//					setResult.add(array[i].iri);
//				}
				//System.err.println(array[i]);
				//System.err.println(array[i].label+" "+  array[i].categoryIri);
				//"categoryIri":"Person",
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return setResult;
	}
	//		String sentence="Japan Tobacco Inc. faces a suit from five smokers who accuse the government-owned company of hooking them on an addictive product ."+
	//						"Albert, physics.";
	//		
	//		System.err.println(NERTagger.getNERTags(sentence));
	//		
	//		List<String> tokens = new ArrayList<String>(SentenceProcessing.getSentenceList(sentence));

	//		for (int i = 0; i < tokens.size(); i++) 
	//		{
	//			System.out.println(tokens.get(i));
	//		}
}



