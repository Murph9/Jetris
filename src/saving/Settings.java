package saving;

import java.util.HashMap;

import saving.SettingsManager.Key;

public class Settings implements ISettings {
	private HashMap<Key, Object> settings;

    protected Settings(HashMap<Key, Object> sets) {
        if (sets != null) {
            this.settings = sets;
        } else {
            this.settings = new HashMap<Key, Object>();
            //defaults
            this.settings.put(Key.ExpertMode, false);
            this.settings.put(Key.Ghost, true);
            this.settings.put(Key.GreyScale, false);
            this.settings.put(Key.HardDropLock, true);
            this.settings.put(Key.RandomColours, false);
        }
    }

    private boolean getBool(SettingsManager.Key key) {
        if (!settings.containsKey(key)) {
			try {
				throw new Exception("getBool() unknown key " + key);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-798045);
			}
        }
        
        return (boolean)settings.get(key);
    }

    public boolean randomColours() { return getBool(Key.RandomColours); }
    public boolean ghost() { return getBool(Key.Ghost); }
    public boolean expertMode() { return getBool(Key.ExpertMode); }
    public boolean hardDropLock() { return getBool(Key.HardDropLock); }
    public boolean greyScale() { return getBool(Key.GreyScale); }

    //TODO other things:
    //- random generator bag size, or toggle
    //- hold piece
    //- sound effects toggle
    //- background stars
}