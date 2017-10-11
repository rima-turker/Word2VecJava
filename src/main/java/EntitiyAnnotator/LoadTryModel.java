package EntitiyAnnotator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class LoadTryModel {

	
	
	public static void main(String[] args) 
	{
		
		final long now = System.currentTimeMillis();
		try {
			String filePath = new ClassPathResource("text8.bin").getFile().getAbsolutePath();
//			String filePath = "/home/rtue/PycharmProjects/Word2Vec-gensim/text8-gensim.bin";
			File gModel = new File(filePath);
		    Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
		    Collection<String> lst = vec.wordsNearest("neil_armstrong", 10);
	        System.out.println("10 Words closest to 'albert': " + lst);
	       
	        

		
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()-now) +" minutes");
	}

}
