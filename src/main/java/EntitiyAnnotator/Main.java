package EntitiyAnnotator;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import util.StopWordRemoval;
import util.Permutator;



public class Main {

	
	
	public static void main(String[] args) 
	{
		
		//System.err.println(StopWordRemoval.removeStopWords("I love this phone, its super fast and there's so much new and cool things with jelly bean....but of recently I've seen some bugs."));;
		//new ExampleAnnotation().startAnnotation("");
//		
//		LinkedList<LinkedList<String>> outerList = new LinkedList<LinkedList<String>>();
//
//        LinkedList<String> list1 = new LinkedList<String>();
//        LinkedList<String> list2 = new LinkedList<String>();
//
//        list1.add("A");
//        list1.add("B");
//
//        list2.add("C");
//        list2.add("D");
//
//        Test.add(list1);
//        Test.add(list2);
//
//        System.err.println(Test.run());
        
		new EntitiyAnnotator_V_3().getAnnotation();
//		EntitiyPopularity ent = new EntitiyPopularity();
//		ent.findIndegreeCount("/home/rtue/workspace/Word2VecJava/src/main/resources/dbpediaEntities_10.2016_clean");
//		ent.findIndegreeCount_paralel("/home/rima/playground/GenerateTree/dbpediaEntities_10.2016_clean",50);
//		ent.findIndegreeCount(args[0]);
		
	}

}
