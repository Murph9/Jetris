package saving;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

public class SettingsManager {
	
	private static ISettings CURRENT = null;
	
	public enum Key {
        RandomColours("randColours"),
        Ghost("ghost"),
        HardDropLock("hardDropLock"),
        GreyScale("greyScale"),
        ExpertMode("expertMode"),
		SoundEffects("SoundEffects");

        private final String key;
        private final Class<?> type;
        Key(String key) {
            this.key = key;
            this.type = boolean.class;
        }

        public String get() {
            return this.key;
        }

        public Class<?> type() {
            return this.type; //use when there are other setting types
        }
    }

	private static File getFile() {
		String fileName = Paths.get(Base.FOLDER, "jetris.settings").toString();
		return new File(fileName);
	}
	
	public static ISettings load() {
		return load(false);
	}
    public static ISettings load(boolean force) {
    	if (!force && CURRENT != null)
    		return CURRENT; //because we load on save this should 'always' be correct (enough)
    	
    	List<Setting> settings = new LinkedList<Setting>(); //start to read records
		Scanner settingScanner = null;
		try {
			File saveFile = getFile();
			
			//create file if it doesn't exist
			saveFile.getParentFile().mkdirs();//create the directory if it doesn't exist
			saveFile.createNewFile(); //try to create a new file
		
			//read the file
			FileReader reader = new FileReader(saveFile);
			settingScanner = new Scanner(reader);
			while(settingScanner.hasNext()) {
				Setting set = new SettingsManager.Setting(settingScanner.next(), settingScanner.next());
				settings.add(set);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-21311);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(-21312);
		} finally {
			if (settingScanner != null)
				settingScanner.close();
		}
		
		Key[] keys = Key.values();
		HashMap<Key, Object> sets = new HashMap<Key, Object>();
		for (Setting set: settings) {
			
			for (Key key: keys) {
				if (key.key.equals(set.key)) {
					System.out.println("read in " + set.key + " as " + set.value);
					sets.put(key, Boolean.parseBoolean(set.value)); //only handles boolean entries so far
				}
			}
		}
		
		CURRENT = new Settings(sets); 
        return CURRENT;
    }

    public static void save(HashMap<Key, Object> settings) {
    	File saveFile = getFile();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(saveFile, false)));
			
			for (Entry<Key, Object> val: settings.entrySet()) {
				out.println(val.getKey().key + " " + val.getValue());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
		
		load(true); //then re-cache it
    }
    
    static class Setting {
    	String key;
    	String value;
    	
    	public Setting() {}
    	public Setting(String key, String value) {
    		this.key = key;
    		this.value = value;
    	}
    }
}

