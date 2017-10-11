package EntitiyAnnotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class EntitiyPopularity 
{
		private static ExecutorService executor;
		private static final Logger LOG  = Logger.getLogger(EntitiyPopularity.class);
		private static final List<String> result = Collections.synchronizedList(new ArrayList<String>());
		
		public void findIndegreeCount_paralel(String fileEntityList, int thread){
			
			executor = Executors.newFixedThreadPool(thread);
			sendEntities(fileEntityList);

		}

		private static void sendEntities(String fileEntityList) {
			try {
				final long now = System.currentTimeMillis();
				try (BufferedReader br = new BufferedReader(new FileReader(fileEntityList))) {

					String sCurrentLine;
					while ((sCurrentLine = br.readLine()) != null) 
					{
						executor.execute(handle(sCurrentLine));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				
				for (String temp : result) 
				{
					LOG.info(temp);
				}
				System.err.println(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()-now));

			} catch (final Exception exception) {
				exception.printStackTrace();
			}
		}

		private static Runnable handle(String line) {
			final Runnable r = new Runnable() {
				@Override
				public void run() {
					result.add(line+"\t"+Caller.runPopularity(line));
				}
			};
			return r;
	}

	
	public void findIndegreeCount(String fileEntityList) 
	{
		List<String> result = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileEntityList))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				result.add(sCurrentLine+"\t"+Caller.runPopularity(sCurrentLine));
				
			}
			for (String temp : result) 
			{
				LOG.info(temp);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
