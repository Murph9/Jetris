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
        }
            
        //set defaults that don't exist
        if (!this.settings.containsKey(Key.ExpertMode))
        	this.settings.put(Key.ExpertMode, false);
        if (!this.settings.containsKey(Key.Ghost))
        	this.settings.put(Key.Ghost, true);
        if (!this.settings.containsKey(Key.GreyScale))
        	this.settings.put(Key.GreyScale, false);
        if (!this.settings.containsKey(Key.HardDropLock))
        	this.settings.put(Key.HardDropLock, true);
        if (!this.settings.containsKey(Key.RandomColours))
        	this.settings.put(Key.RandomColours, false);
        if (!this.settings.containsKey(Key.SoundEffects))
        	this.settings.put(Key.SoundEffects, true);
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
    public boolean useSoundEffects() { return getBool(Key.SoundEffects); }

    //TODO other things:
    //- random generator bag size, or toggle
    //- hold piece
    //- sound effects toggle
    //- background stars
}