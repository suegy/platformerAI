package org.platformer.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.platformer.benchmark.platform.engine.Art;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Configuration {


    private static transient Configuration instance = null;
    private transient boolean ready = false;

    private Map<String,String> options;
    private Map<String,String> artAssets;

    private transient String cfgName;
    private transient String fileLocation;
    private transient Gson gson;

    private Configuration()
    {
        cfgName = "/config.xml";
        //fileLocation = System.getProperty("user.dir")+ File.separator+"platformerAI"+File.separator+"rsrc"+File.separator;

        gson = new GsonBuilder().setPrettyPrinting().create();

    }

    private void readFile(){

        try {
            Reader reader = new BufferedReader(new FileReader(getClass().getResource(cfgName).getPath()));
            Configuration config = gson.fromJson(reader, Configuration.class);
            artAssets = config.artAssets;
            options = config.options;

            reader.close();
            ready = true;

        } catch (IOException e) {
            ready = false;
            write();
            System.err.println("Error: unable to read "+cfgName);
        }
    }

    public void write(){
        try {
            Writer writer = new BufferedWriter(new FileWriter(fileLocation+cfgName));
            options = ParameterContainer.defaultOptionsHashMap;
            artAssets = standardArtConfig();
            String configuration = gson.toJson(this);
            writer.write(configuration);
            writer.flush();
            writer.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static Map<String,String> standardArtConfig(){
        Map<String,String> defaults = new HashMap<String, String>();


        defaults.put(Art.SpriteSheets.BIGPLUMBER.name(), "/art/mariosheet.png");
        defaults.put(Art.SpriteSheets.RACOONPLUMBER.name(), "/art/racoonmariosheet.png");
        defaults.put(Art.SpriteSheets.SMALLPLUMBER.name(), "/art/smallmariosheet.png");
        defaults.put(Art.SpriteSheets.HOTPLUMBER.name(), "/art/firemariosheet.png");
        defaults.put(Art.SpriteSheets.ENEMIES.name(), "/art/enemysheet.png");
        defaults.put(Art.SpriteSheets.ITEMS.name(), "/art/itemsheet.png");
        defaults.put(Art.SpriteSheets.LEVEL.name(), "/art/mapsheet.png");
        defaults.put(Art.SpriteSheets.PARTICLES.name(), "/art/particlesheet.png");
        defaults.put(Art.SpriteSheets.NEAR_BACKGROUND.name(), "/art/bgsheet.png");
        defaults.put(Art.SpriteSheets.FAR_BACKGROUND.name(), "/art/bg_street.png");
        defaults.put(Art.SpriteSheets.FONTS.name(), "/art/font.gif");
        defaults.put(Art.SpriteSheets.PRINCESS.name(), "/art/princess.png");

        return defaults;

    }

    public static Configuration getConfiguration(){
        if (instance == null)
            instance = new Configuration();

        if (!instance.ready)
            instance.readFile();


        return instance;

    }

    public Map<String,String> getControlOptions(){
        return options;
    }

    public Map<String,String> getArtConfig(){
        return artAssets;
    }

}
