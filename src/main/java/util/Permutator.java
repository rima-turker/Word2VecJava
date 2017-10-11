package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Permutator
{  

	private final LinkedList<LinkedList<String>> outerList = new LinkedList<LinkedList<String>>();
	private final List<String> result = new ArrayList<>();
	
    public void generate(LinkedList<LinkedList<String>> outerList, String outPut) {
        LinkedList<String> list = outerList.get(0);

        for(String str : list) {
            LinkedList<LinkedList<String>> newOuter = new LinkedList<LinkedList<String>>(outerList);
            newOuter.remove(list);

            if(outerList.size() > 1) {
            	if(outPut.isEmpty()){
            		generate(newOuter, outPut+str);
           	 	}
            	else{
            		generate(newOuter, outPut+";"+str);
            	}
             } else {
            	 if(outPut.isEmpty()){
            		 result.add(str);
            	 }else{
            	 result.add(outPut+";"+str);
            	 }
             }
        }
    }

    public void add(LinkedList<String> list){
    	outerList.add(list);
    }
    
    private List<String> getResult(){
    	return result;
    }
    
    public List<String>  run(){
    	generate(outerList, "");
    	return getResult();
    }
}