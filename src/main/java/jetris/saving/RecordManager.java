package jetris.saving;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class RecordManager {

	public static final String TYPE_A = "A";

	private static Record lastRecord;
	private static List<Record> _records;
	
	private static File getFile(String gameType) {
		String fileName = Paths.get(Base.FOLDER, gameType+".score").toString();
		return new File(fileName);
	}
	
	/**
	 * Returns the current list of records for the given game type.
	 * @param gameType String 
	 * @return An unsorted list
	 */
	public static List<Record> getRecords(String gameType) {
		
		if (_records != null)
			return _records; //use cached value

		List<Record> records = new LinkedList<Record>(); //start to read records
		Scanner scoresScanner = null;
		try {
			File saveFile = getFile(gameType);
			
			//create file and its directory if they don't exist
			saveFile.getParentFile().mkdirs();
			saveFile.createNewFile();
		
			//read the file int per int
			FileReader reader = new FileReader(saveFile);
			scoresScanner = new Scanner(reader);
			while (scoresScanner.hasNext()) {
				Record rec = new Record(scoresScanner.nextInt(), scoresScanner.nextInt());
				if (rec.equals(lastRecord))
					rec.setIsNew(true); //if the score and line counts are the same multiple entries may be new
				records.add(rec);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (scoresScanner != null)
				scoresScanner.close();
		}

		_records = records; //save cached version
		return records;
	}
	
	/**
	 * Saves the record to file based on the type.
	 * @param record
	 * @param gameType
	 */
	public static void saveRecord(Record record, String gameType) {
		lastRecord = record;
		_records.add(record);

		File saveFile = getFile(gameType);
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
