package edu.rit.se.design.arcode.fspecminer.graam.code2vecApi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

public class App
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{

		URL url = new App().getClass().getResource("/code2vec_testcases");
		File testcases = new File(url.getPath().replaceAll( "%20", " " ));

		Files.walk( testcases.toPath() ).filter( Files::isRegularFile ).forEach(path -> {
			try {
				File file = path.toFile();
				String testMethod = Files.readString(file.toPath());
				System.out.println(testMethod + "\n");

				Map<String, Double> result=Code2VecUtil.getRankedMethodNameMap(testMethod);
				print(result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} );
	}

	static void print(Map<String,Double> SuggestedMethodNames) {
		SuggestedMethodNames.entrySet()
				.forEach(System.out::println);
		System.out.println("==========================================\n");
	}

}
