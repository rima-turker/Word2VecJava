package EntitiyAnnotator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.LexedTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;

/*
 * This class is for generating anchor-link map for wikipedia entities. 
 */
public class MapAnchorAndLink 
{
	private static final Logger LOG  = Logger.getLogger(MapAnchorAndLink.class);
	private Pattern patternTag, patternLink;
	private Matcher matcherTag, matcherLink;

	private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN =
			"\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


	public MapAnchorAndLink() {
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	}


	public Map<String , List<String>> getMap(String fileName)
	{
		Map<String , List<String>> map = new HashMap<String, List<String>>();

		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			//String filePath = new ClassPathResource(fileName).getFile().getAbsolutePath();
			//BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			System.out.println("in the function getMap");
			while ((line = br.readLine()) != null) 
			{
				matcherTag = patternTag.matcher(line);

				while (matcherTag.find()) {//you find the all the tags

					String href = matcherTag.group(1); // href
					String anchor = matcherTag.group(2).toLowerCase(); // link text

					matcherLink = patternLink.matcher(href);

					while (matcherLink.find()) {

						String link = matcherLink.group(1); // link
						HtmlLink obj = new HtmlLink();
						obj.setLink(link);
						obj.setLinkText(anchor);
						//						System.out.println(link);
						//						System.out.println(anchor);
						StringBuilder strLinkBuilder = new StringBuilder();

						try 
						{
							link = java.net.URLDecoder.decode(link);
							
							link=link.replaceAll("/n"," ").replaceAll("\"", "").replaceAll("\\(", "").replaceAll("\\)", "").toLowerCase();
							//System.out.println(link.replaceAll("\"", "").replaceAll("\\(", ""));

							if (!anchor.equals("")&&!anchor.equals(" ")&&!link.equals(" ")&&!link.equals("")) 
							{
								String[] linkSplit = link.split(" ");
								for (int i = 0; i < linkSplit.length; i++) 
								{
									strLinkBuilder.append( linkSplit[i]+"_");

								}
								strLinkBuilder=strLinkBuilder.replace(strLinkBuilder.length()-1, strLinkBuilder.length(), "");

								//							StringBuilder anchorBuilder = new StringBuilder();
								//							String[] anchorSplit= anchor.toLowerCase().split(" ");
								//							
								//							for (int i = 0; i <anchorSplit.length; i++) 
								//							{
								//								anchorBuilder.append( anchorSplit[i]+"_");
								//
								//							}

								//							anchorBuilder=anchorBuilder.replace(anchorBuilder.length()-1, anchorBuilder.length(), "");

								List<String> listLinks;

								if (map.containsKey(anchor)) 
								{


									listLinks = new ArrayList<>(map.get(anchor));
									listLinks.add(strLinkBuilder.toString());

									//System.out.println(map.get(anchorBuilder.toString())+ " "+strLinkBuilder.toString());
								}
								//							if (map.containsKey(anchorBuilder.toString())) 
								//							{
								//								setLinks = new HashSet<>(map.get(anchorBuilder.toString()));
								//								setLinks.add(strLinkBuilder.toString());
								//								
								//								//System.out.println(map.get(anchorBuilder.toString())+ " "+strLinkBuilder.toString());
								//							}
								else
								{
									listLinks = new ArrayList<>();
									listLinks.add(strLinkBuilder.toString());

									//map.put(anchorBuilder.toString(), hset);

									//System.out.println(map.get(anchorBuilder.toString())+ " "+strLinkBuilder.toString());
								}
								map.put(anchor, listLinks);
							}
							else
							{
								System.err.println("This line will not be added"+ anchor+" "+link);
							}
							
						} 
						catch (Exception e) 
						{
							System.out.println("URL "+ link);
						}
					}
				}
			}

			br.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> setEntities = new ArrayList<>();
		for (Entry<String, List<String>> entry : map.entrySet()) 
		{
			for (String str: entry.getValue()) 
			{
				//System.out.println(entry.getKey()+" "+str);
				LOG.info(entry.getKey()+"\t"+str);
			}

		}

		return map;

	}

	public Vector<HtmlLink> grabHTMLLinks(final String line) {

		Vector<HtmlLink> result = new Vector<HtmlLink>();

		List<CoreLabel> tokens = new ArrayList<CoreLabel>();

		final LexedTokenFactory<CoreLabel> tokenFactory = new CoreLabelTokenFactory();

		final PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(line), tokenFactory, "untokenizable=noneDelete");

		while (tokenizer.hasNext()) {
			tokens.add(tokenizer.next());
		}

		final List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>().process(tokens);
		int end;
		int start = 0;
		final ArrayList<String> sentenceList = new ArrayList<String>();
		for (List<CoreLabel> sentence: sentences) {
			end = sentence.get(sentence.size()-1).endPosition();
			sentenceList.add(line.substring(start, end).trim());
			start = end;
		}
		for(final String sentenceString :sentenceList){
			final String sentenceWithoutHtmlTag = sentenceString.replaceAll("<[^>]*>", "");
			matcherTag = patternTag.matcher(sentenceString);

			while (matcherTag.find()) {//you find the all the tags

				String href = matcherTag.group(1); // href
				String anchor = matcherTag.group(2); // link text

				matcherLink = patternLink.matcher(href);

				while (matcherLink.find()) {

					String link = matcherLink.group(1); // link
					HtmlLink obj = new HtmlLink();
					obj.setLink(link);
					obj.setLinkText(anchor);
					System.out.println(link);
					System.out.println(anchor);
					StringBuilder strLink = new StringBuilder();
					link = java.net.URLDecoder.decode(link);
					link.replaceAll("/n"," ");
					String[] hrefSplit = link.split(" ");
					for (int i = 0; i < hrefSplit.length; i++) 
					{
						strLink.append( hrefSplit[i]+"_");

					}
					strLink=strLink.replace(strLink.length()-1, strLink.length(), "");

					result.add(obj);
				}
			}
		}
		return result;
	}

	public class HtmlLink {

		String link;
		String linkText;
		String fullSentence;

		HtmlLink(){};

		public void setFullSentence(String sentenceWithoutHtmlTag) {
			fullSentence = sentenceWithoutHtmlTag;
		}

		@Override
		public String toString() {
			return "HtmlLink [link=" + link + ", anchor=" + linkText + ", fullSentence=" + fullSentence + "]";
		}

		public String getLink() {
			return link;
		}

		public String getFullSentence(){
			return fullSentence;
		}

		public void setLink(String link) {
			this.link = replaceInvalidChar(link);
		}

		public String getLinkText() {
			return linkText;
		}

		public void setLinkText(String linkText) {
			this.linkText = linkText;
		}

		private String replaceInvalidChar(String link){
			link = link.replaceAll("'", "");
			link = link.replaceAll("\"", "");
			return link;
		}
	}
}
