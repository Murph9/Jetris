package base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import logic.Tetris.GameType;

public class RecordManager {
	
	private static final String FOLDER = System.getProperty("user.home")+"/.murph9/tetris/";

	//TODO caching? (on read we don't need to read it again)
	
	private static File getFile(GameType type, int bVersion) {
		String fileName;
		if (type == GameType.TYPE_A) {
			fileName = Paths.get(FOLDER, "A.score").toString(); //leaderboardA.txt
		} else {
			fileName = Paths.get(FOLDER, "B" + bVersion + ".score").toString(); //leaderboardBn.txt
		}
		return new File(fileName);
	}
	
	public static List<Record> getRecords(GameType type, int bVersion) {
		
		List<Record> records = new LinkedList<Record>(); //start to read records
		Scanner scoresScanner = null;
		try {
			File saveFile = getFile(type, bVersion);
			
			//create file if it doesn't exist
			saveFile.getParentFile().mkdirs();//create the directory if it doesn't exist
			saveFile.createNewFile(); //try to create a new file
		
			//read the file
			FileReader reader = new FileReader(saveFile);
			scoresScanner = new Scanner(reader);
			while(scoresScanner.hasNext()) {
				records.add(new Record(scoresScanner.nextInt(), scoresScanner.nextInt()));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-21311);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(-21312);
		} finally {
			if (scoresScanner != null)
				scoresScanner.close();
		}
		
		Collections.sort(records);
		return records;
	}
	
	public static void saveRecord(Record record, GameType type, int bVersion) {
		File saveFile = getFile(type, bVersion);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(saveFile, true)));
			out.println(record);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}
}
