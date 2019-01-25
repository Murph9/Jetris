package saving;

import java.util.HashMap;

public class SettingsManager {
	
	public enum Key {
        RandomColours("randColours"),
        Ghost("ghost"),
        HardDropLock("hardDropLock"),
        GreyScale("greyScale"),
        ExpertMode("expertMode");

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

    public static ISettings load() {
        //TODO
    	//use String fileName = Paths.get(Base.FOLDER, "settings.txt").toString();
        return new Settings(null);
    }

    public static void save(HashMap<Key, Object> settings) {
        //TODO
    }
}

