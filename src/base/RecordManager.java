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

public class RecordManager {
	
	private static final String FOLDER = System.getProperty("user.home")+"/.murph9/tetris/";

	private static Record lastRecord;
	
	//TODO caching? (on read we don't need to read it again)
	
	private static File getFile(String gameType, int bVersion) {
		String fileName;
		if (bVersion == 0) {
			fileName = Paths.get(FOLDER, gameType+".score").toString(); //leaderboardA.txt
		} else {
			fileName = Paths.get(FOLDER, gameType + bVersion + ".score").toString(); //leaderboardBn.txt
		}
		return new File(fileName);
	}
	
	public static List<Record> getRecords(String gameType, int bVersion) {
		
		List<Record> records = new LinkedList<Record>(); //start to read records
		Scanner scoresScanner = null;
		try {
			File saveFile = getFile(gameType, bVersion);
			
			//create file if it doesn't exist
			saveFile.getParentFile().mkdirs();//create the directory if it doesn't exist
			saveFile.createNewFile(); //try to create a new file
		
			//read the file
			FileReader reader = new FileReader(saveFile);
			scoresScanner = new Scanner(reader);
			while(scoresScanner.hasNext()) {
				Record rec = new Record(scoresScanner.nextInt(), scoresScanner.nextInt());
				if (rec.equals(lastRecord))
					rec.setIsNew(true);
				records.add(rec);
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
	
	public static void saveRecord(Record record, String gameType, int bVersion) {
		lastRecord = record;
		
		File saveFile = getFile(gameType, bVersion);
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
