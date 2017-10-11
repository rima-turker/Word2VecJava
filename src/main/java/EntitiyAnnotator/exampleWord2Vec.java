package EntitiyAnnotator;

import java.util.Collection;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class exampleWord2Vec {
	private static Logger log = LoggerFactory.getLogger(Word2VecRawTextExample.class);

    public static void main(String[] args) throws Exception {
	Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel("/home/rtue/workspace/Word2VecJava/Word2VecModels/model_wiki_latest_1E3W.bin");
	 Collection<String> lst = word2Vec.wordsNearest("obama", 10);
     System.out.println("10 Words closest to 'obama': " + lst);
     
     System.err.println(word2Vec.similarity("moon", "person"));
    }

}
