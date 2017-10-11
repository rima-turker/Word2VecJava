package util;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class SentenceProcessing 
{
	public static List<String> getSentenceList(String sentence)
	{
		List<String> tokens = new ArrayList<String>();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(sentence),
				new CoreLabelTokenFactory(), "");
		while (ptbt.hasNext()) {
			tokens.add(ptbt.next().toString());
		}
		return tokens;
	}
}
