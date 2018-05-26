/**
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
/**
 * @author Haimonti
 *
 */
public class InterGrpPred {
	
	static LinkedHashMap<String, Integer> all_unique_tweets = new LinkedHashMap<String, Integer>();
	static LinkedHashMap<String,Integer> features_map = new LinkedHashMap<String,Integer>();
	static ArrayList<String> listofwords;
	static HashSet<String> all_group_words_set = new HashSet<String>();
	static HashSet<String> all_individual_words_set = new HashSet<String>();
	static HashSet<String> all_swear_words_set = new HashSet<String>();
	
	static String input_files_path = System.getProperty("user.dir") + File.separator + "RumourExtractionInput";
	// Input paths	
	//1. Posix when we need to use Part of speech features
	//2. Raw Data when we need to use the whole Tweets. Approx 50k or 60k
	//3. New Input are the more filtered Tweets. Wedge tweets were pre decided by manual annotation.	
	
	static String input_tweets_path = input_files_path + File.separator + "unique_tweets.csv";
	static String unigram_handcoded_file_path = input_files_path + File.separator + "unigrams_handcoding_new.csv";
	static String wedge_Ann = input_files_path + File.separator + "wedge_HumanAnn.csv";
	
	//static String input_tweets_path = "D:\\RumourExtractionInput\\raw_data_eng_new.csv";
	//static String input_tweets_path = input_files_path + File.separator + "new_input_set.csv";
	
	// output paths
	static String outputDirPath = System.getProperty("user.dir") + File.separator + "RumourExtractionOutput";	
	
	//Output files Needed when we use raw input file. Since in that case we will be annotating those tweets into 0 or 1
	static String duplicates = outputDirPath + File.separator + "Dups.csv";
	static String final_unique_tweets= outputDirPath + File.separator +  "unique_tweets_dups_checked.txt";
	static String url_rep_tweets= outputDirPath + File.separator +  "unique_tweets_url_rep.txt";
	static String url_dups = outputDirPath + File.separator +  "url_dups.txt";
	static String pro_tweet = outputDirPath + File.separator +  "pro_tweet.txt";
	static String feat_file = outputDirPath + File.separator +  "unigrams.txt";
	static String output_file_name = outputDirPath + File.separator +"FeatureVector_v1a.csv";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("********started***********");
		
		String [] next_line;
		String [] next_wedge_line;
		int tweet_count = 0;
		int column_number = 0;
		int dup=0;
		int unique_tweet_cnt=0;
		int unique_feat=0;
		HashSet<String> hashNegTweets = new HashSet<String>();
		HashSet<String> hashWedgeTweets = new HashSet<String>();
		HashSet<String> hashWdNegTweets = new HashSet<String>();
		
		//Read in the file with unique tweets
		//Check for duplicates
		//If no duplicates, add them to the file final_unique_tweets
		try 
		{
		CSVReader csv_reader = new CSVReader(new FileReader(input_tweets_path));
		CSVReader csv_wedge_reader = new CSVReader(new FileReader(wedge_Ann));
		CSVReader unigram_file_reader = new CSVReader(new FileReader(unigram_handcoded_file_path));	
		CSVWriter csv_writer_dups = new CSVWriter(new FileWriter(duplicates, true));
		CSVWriter csv_writer = new CSVWriter(new FileWriter(final_unique_tweets, true));
		CSVWriter csv_writer_url = new CSVWriter(new FileWriter(url_rep_tweets, true));
		CSVWriter csv_writer_dups_url = new CSVWriter(new FileWriter(url_dups,true));
		CSVWriter csv_writer_pro = new CSVWriter(new FileWriter(pro_tweet,true));
		CSVWriter csv_writer_feat=new CSVWriter(new FileWriter(feat_file,true));
		
		//initial findings for the group/individual/target generation conditions
		String[] nn = unigram_file_reader.readNext();
		while((next_line = unigram_file_reader.readNext()) != null)
		{
			String unigram_word = next_line[0].trim();
			String is_group_str = next_line[9];
			String is_individual_str = next_line[7];
			String is_swear_word_str = next_line[3];
			
			if(unigram_word.length() > 0)
			{
				if(is_group_str.contains("Yes"))
				{
					all_group_words_set.add(unigram_word);
					System.out.println(unigram_word);
				}
				if(is_individual_str.contains("1"))
			{
					all_individual_words_set.add(unigram_word);
				}
				if(is_swear_word_str.contains("1"))
				{
					all_swear_words_set.add(unigram_word);
				}					
			}
		}
		unigram_file_reader.close();
		
		//Read in the file with human annotations for wedge driving tweets
		while((next_wedge_line = csv_wedge_reader.readNext()) != null)
		{
			String curWedge = next_wedge_line[0].trim();
			hashWedgeTweets.add(curWedge);			
		}
		System.out.println("Human Ann set of wedge driving tweets "+hashWedgeTweets.size());
		while((next_line = csv_reader.readNext())!= null)
		{
			tweet_count++;
			String original_tweet = next_line[column_number];	
			//String filtered_tweet = Main.ApplyFilters(original_tweet);
			//String tweet = Main.UrlFilter(filtered_tweet);
			//String filtered_tweet = original_tweet;
			
			if(all_unique_tweets.containsKey(original_tweet)) 
			{
				csv_writer_dups.writeNext(new String[]{original_tweet});
				dup++;
			}
			else
			{	
				all_unique_tweets.put(original_tweet,unique_tweet_cnt);
				unique_tweet_cnt++;
				csv_writer.writeNext(new String[]{original_tweet});
			}
		}
		
		csv_reader.close();	
		csv_writer.close();
		csv_writer_dups.close();
		
		System.out.println("Total number of tweets read "+tweet_count);
		System.out.println("Number of duplicates found "+dup++);
		System.out.println("Number of unique tweets "+unique_tweet_cnt);
		
		//Mark up the tweets with negative words and keep them
		// The negative word list from LIWC along with swear word list is used
		// for the purpose of filtering.
		hashNegTweets=Main.findTweetsWithNegativeWords(all_unique_tweets);
		// create an iterator for iterating through the negative tweet set
		Iterator<String> itrNegTweets = hashNegTweets.iterator(); 
		//Replace all the URLs in negative tweets with "http"
		//This is done before the unigram extraction phase
		HashSet<String> hashURL = new HashSet<String>();
		int dupHere=0;
		while (itrNegTweets.hasNext())
		{
			String current_tweet =itrNegTweets.next();
			//csv_writer_url.writeNext(new String[]{current_tweet});
			String repURL = null;
			repURL=Main.UrlReplace(current_tweet);
			repURL.concat("\n");
			//csv_writer_url.writeNext(new String[]{repURL});
			//System.out.println(repURL);
			if(hashURL.add(repURL)==false)
			{
				dupHere++;
				csv_writer_dups_url.writeNext(new String[]{repURL});
			}
			else
			hashURL.add(repURL);
		}
		System.out.println("Duplicates eliminated "+dupHere);
		System.out.println("Size of HashSet after URL replacement "+hashURL.size());
		csv_writer_dups_url.close();
		for(String s : hashURL)
		{
			csv_writer_url.writeNext(new String[]{s});
		}	
		//System.out.println("Size of HashSet after URL replacement "+hashURLRepTweets.size());
		csv_writer_url.close();
		
		//Clean tweets - assume stop words have already been removed.
		//Create an iterator for iterating through the tweet set 
		//after applying URL processing
		Iterator<String> itrURL = hashURL.iterator(); 
		//System.out.println("Before Iterating: "+hashURL.size());
		while (itrURL.hasNext())
		{
			String cur_tweet =itrURL.next();
			//System.out.println(cur_tweet);
			//Remove occurences of #bostonmarathon and its variants only!
			String processTweet=Main.ApplyFilters(cur_tweet); 
			//System.out.println(processTweet);
			Iterator<String> itrHashWedge = hashWedgeTweets.iterator();
			while(itrHashWedge.hasNext())
			{
				String cur_Wedge = itrHashWedge.next();
				//System.out.println(cur_Wedge);
				if(cur_Wedge.contains(processTweet))
				{
					hashWdNegTweets.add(processTweet);
					//System.out.println(processTweet);
				}
			}
			
			csv_writer_pro.writeNext(new String[]{processTweet});
			//Create the bag-of-words for the data
			String [] words = processTweet.split("\\s+");
			for(int i = 0; i < words.length; i++)
			{
				//Main.AddToFeaturesMap(words[i]);
				//System.out.println(words[i]);
				if(features_map.containsKey(words[i].trim()))
				{
					unique_feat=features_map.get(words[i]);
					features_map.put(words[i], unique_feat+1);
				}
				else
				{
					// Map does not contain that word, so add it to map
					features_map.put(words[i], 1);
				}				
			}	
			}
		System.out.println("Number of wedge tweets with negative emotion - Human Ann "+hashWdNegTweets.size());
		listofwords = new ArrayList<String>(features_map.keySet());
		Collections.sort(listofwords);			
		
		for(String word:listofwords)
		{
			//System.out.println(word+" --> " + features_map.get(word));
			//Check for duplicates
			
			//Remove from unigrams with frequency 1
			if(features_map.get(word)==1)
			{
	
				features_map.remove(word);
			}
			else
			{
			//csv_writer_feat.writeNext(new String[]{word+" --> " + features_map.get(word)});
			csv_writer_feat.writeNext(new String[]{word});	
			}
		}
		
		System.out.println("Original Unigram space size "+features_map.keySet().size());
		csv_writer_feat.close();
		csv_writer_pro.close();
		
		//Now create the feature vector
		CreateFeatureVector(hashURL,hashWdNegTweets);
		
		System.out.println("****Mission Accomplished********");
		
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Function to create a feature vector
	/**
	 * @param unqTweets
	 */
	public static void CreateFeatureVector(HashSet<String> unqTweets,HashSet<String> wedgeHash)
	{	
	 try
	  {
		System.out.println("********Creating Feature Vector******");
		//File to print the wedge driving tweets identified programmatically
		String wedge_criteria_detected = outputDirPath + File.separator +"wedge-crit.txt";
		String non_wedge_criteria_detected = outputDirPath + File.separator +"non-wedge-crit.txt";
		CSVWriter csv_writer_wed_crit=new CSVWriter(new FileWriter(wedge_criteria_detected,true));
		CSVWriter csv_writer_non_wed_crit=new CSVWriter(new FileWriter(non_wedge_criteria_detected,true));
		
		ArrayList<String> all_words_list = new ArrayList<String>(features_map.keySet());
		HashMap<String, Integer> column_id_index = new HashMap<String, Integer>();
		FileWriter fw_csv = new FileWriter(output_file_name);
		BufferedWriter csv_writer_buf = new BufferedWriter(fw_csv);
				
		//Number of attributes in the data set
		int num_cols = 3+features_map.size()+9;
		String [] row = new String[num_cols];
		
		System.out.println("Number of features is " + num_cols);
		Collections.sort(all_words_list);	
		
		String [] row_header = new String[num_cols];	
		int column_id = 0;
		
		row_header[0] = "Tweet";
		column_id++;
		row_header[1] = "Target";
		column_id++;
		row_header[2] = "Target_negation";
		column_id++;
		row_header[3] = "Has url";
		column_id++;
		row_header[4] = "Has News-Media Org";
		column_id++;		
		row_header[5] = "Group";
		column_id++;
		row_header[6] = "Individual";
		column_id++;
		row_header[7] = "IsCaps";
		column_id++;
		row_header[8] = "CredibleNumber";
		column_id++;
		row_header[9] = "Empathetic-Feature";
		column_id++;
//		row_header[10] = "HasHelpOrDonate";
//		column_id++;
		row_header[10] = "HasKill";
		column_id++;
		
		row_header[11] = "HasBreaking";
		column_id++;
		
		//Initialize the bag of words list
//		System.out.println("Column id is "+column_id);
//		for(String word :all_words_list )
//		{
//			column_id_index.put(word, column_id);
//			row_header[column_id] = word;
//			column_id++;	
//		}
		// Get a set of the entries
	    Set set = features_map.entrySet();
	    //System.out.println("Size of set "+set.size());
	    // Get an iterator
	    Iterator itrFeatMap = set.iterator();
	    // Display elements
	    while(itrFeatMap.hasNext())
	    {
	         Map.Entry me = (Map.Entry)itrFeatMap.next();
	         column_id_index.put(me.getKey().toString(), column_id);
	         row_header[column_id] =me.getKey().toString();
	         column_id++;
	         //System.out.print(me.getKey() + "\n");
	         //jmSystem.out.println(me.getValue());
	      }
//		StringBuilder row_header_builder = new StringBuilder();
//		System.out.println("Row header length is "+row_header.length);
//		for(int i = 1; i < row_header.length; i++)
//		{
//			row_header_builder.append(row_header[i]);
//			
//			if(i != (row.length - 1))
//			{
//				row_header_builder.append(",");
//			}
//		}
//		csv_writer_buf.write(row_header_builder + "\n");
		Iterator<String> tweets_iterator = unqTweets.iterator();
		System.out.println("Number of unique tweets "+unqTweets.size());
		int y=0;
		//Keep track of tweets with target 0
		int m1=0;
		//Keep track of tweets with target 1
		int m2=0;
		while(tweets_iterator.hasNext())
			{
				//System.out.println("Tweet no: "+y);
				
				//Process the current tweet.
				String curTw=tweets_iterator.next();
				//Get the target value for this tweet.
				int target_val=getTargetValue(curTw);
				//If a tweet is not wedge driving
				//check the list of manual annotations.				
				if(target_val==0)
				{
					Iterator<String> itrWedge = wedgeHash.iterator();
					//m1++;
					while(itrWedge.hasNext())
					{
						String cur_Wedge = itrWedge.next();
						//System.out.println(cur_Wedge);
						if(cur_Wedge.contains(curTw))
						{
							target_val=1;
							//m2=m2+1;
							//System.out.println(curTw);
						}						
					}						
				}
				if(target_val==1)
				{
					m1++;
				}
				else
				{
					m2++;
				}
				
				//Print the wedge and non-wedge driving tweets
				if(target_val==1)
				{
					csv_writer_wed_crit.writeNext(new String[]{curTw});
				}
				else
				{
					csv_writer_non_wed_crit.writeNext(new String[]{curTw});
				}
				
				for( int i = 0; i < num_cols; i++)
				{
					row[i] = "" + 0;
				}
				row[0] = curTw;
				row[1] = target_val + "";	
				row[2] = Math.abs(target_val - 1) + "";
				//Do you need to split the tweet?
				if(curTw.contains("http"))
				{
					row[3] = 1+"";
				}
				// Check for News Org handles.
				// Also add national and Boston based media org
				if((Main.hasNewsOrg(curTw)=="1") || (Main.hasMedia(curTw,"@ABC")==1) || 
				(Main.hasMedia(curTw,"@abc")==1) || (Main.hasMedia(curTw,"@NBC")==1) ||
				(Main.hasMedia(curTw,"@nbc")==1) || (Main.hasMedia(curTw,"@CBS")==1) ||
				(Main.hasMedia(curTw,"@cbs")==1) || (Main.hasMedia(curTw,"@FOX")==1) ||
				(Main.hasMedia(curTw,"@fox")==1) || (Main.hasMedia(curTw,"@CNN")==1) ||
				(Main.hasMedia(curTw,"@cnn")==1) ||(Main.hasMedia(curTw,"@BOSTON")==1) ||
				(Main.hasMedia(curTw,"@boston")==1) || (Main.hasMedia(curTw,"@WSJ")==1) ||
				(Main.hasMedia(curTw,"@wsj")==1) || (Main.hasMedia(curTw,"@NYTIMES")==1) ||
				(Main.hasMedia(curTw,"@nytimes")==1) || (Main.hasMedia(curTw,"@Nytimes")==1) ||
				(Main.hasMedia(curTw,"@washingtonpost")==1) || (Main.hasMedia(curTw,"@WashingtonPost")==1) ||
				(Main.hasMedia(curTw,"@Washingtonpost")==1))
				{
				row[4] = 1+"";
				}
				
				row[5] = "" + isGroupTweet(curTw);
				row[6] = "" + isIndividualTweet(curTw);
				row[7] = "" + Main.isCapsTweet(curTw);
				row[8] = "" + Main.getNumberFeature(curTw);
				
				if((Main.getSaddenFeature(curTw) == 1) || (Main.getHeartFeature(curTw) == 1) || (Main.getThoughtsFeature(curTw) == 1)
						|| (Main.getPrayersFeature(curTw) == 1) || (Main.getTearsFeature(curTw) == 1) || (Main.getHelpOrDonateFeature(curTw)==1))
				{
					row[9] = "1";
				}
				if(Main.getKillFeature(curTw)==1)
				{
					row[10]="1";
				}
				if(Main.hasBreakingWord(curTw)==1)
				{
					row[11]="1";
				}
								
				String [] tweet_tokens = curTw.split("\\s+");								
				for(String word : tweet_tokens)
				{
					// find the column ID for that particular word, and set that to 1
					if(column_id_index.containsKey(word))
					{
						int col_id_of_word = column_id_index.get(word);
						row[col_id_of_word] = "" + 1;
					}
				}
				StringBuilder row_builder = new StringBuilder();
				for(int i = 1; i < row.length; i++)
				{
					row_builder.append(row[i]);
					
					if(i != (row.length - 1))
					{
						row_builder.append(",");
					}
				}
				
				String row_value = row_builder.toString();
				y++;
				csv_writer_buf.write(row_value + "\n");
				//System.out.println("Processed Tweet No: "+y);
			}//end of tweet iterator
			System.out.println("How many +ve tweets ?"+m1);
			System.out.println("How many -ve tweets?" +m2);
			csv_writer_buf.close();
			fw_csv.close();
			csv_writer_wed_crit.close();
			csv_writer_non_wed_crit.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Get target value for that particular tweet
		public static int getTargetValue(String tweet)
		{
			int target_value = 0;
			String [] words_in_tweet = tweet.split("\\s+"); 
			boolean is_group_word = false;
			boolean is_individual_word = false;
			boolean is_swear_word = false;	
			int num_target_ones=0;
			
			// Checking if the tweet contains any word from all group word set
			for(int i = 0; i < words_in_tweet.length; i++)
			{
				if(all_group_words_set.contains(words_in_tweet[i]))
				{
					is_group_word = true;
					break;
				}
			}
			
			// Checking if the tweet contains any word from all individual word set
			for(int i = 0; i < words_in_tweet.length; i++)
			{
				if(all_individual_words_set.contains(words_in_tweet[i]))
				{
					is_individual_word = true;
					break;
				}
			}
			
			// Checking if the tweet contains any word from all swear word set
			for(int i = 0; i < words_in_tweet.length; i++)
			{
				if(all_swear_words_set.contains(words_in_tweet[i]))
				{
					is_swear_word = true;
					break;
				}
			}
			
			// finding target_value
			if(is_swear_word && (is_group_word || is_individual_word))
			{
				num_target_ones++;
				target_value = 1;
			}				
			
			return target_value;
		}
		
		// Check if the tweet contains a word from group word set
		public static int isGroupTweet(String tweet)
		{
			String [] words = tweet.split("\\s+");	
			int return_value = 0;
			
			for(int i = 0; i < words.length; i++)
			{
				if(all_group_words_set.contains(words[i]))
				{
					return_value = 1;
					break;
				}
			}
			
			return return_value;
		}
		
		public static int isIndividualTweet(String tweet)
		{
			String [] words = tweet.split("\\s+");	
			int return_value = 0;
			
			for(int i = 0; i < words.length; i++)
			{
				if(all_individual_words_set.contains(words[i]))
				{
					return_value = 1;
					break;
				}
			}
			
			return return_value;
		}
		
		

}
