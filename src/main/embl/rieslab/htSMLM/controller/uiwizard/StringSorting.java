package main.embl.rieslab.htSMLM.controller.uiwizard;


public class StringSorting{
	
    public static String[] sort(String[] input)
    {
        String temp;
        String[] sorted = input;
	
        for(int i=0; i<input.length; i++){
            for(int j=1; j<input.length; j++){
                if(sorted[j-1].compareTo(sorted[j])>0){
                    temp=sorted[j-1];
                    sorted[j-1]=sorted[j];
                    sorted[j]=temp;
                }
            }
        }
		
		return sorted;
    }
}
