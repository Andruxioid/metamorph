package mchorse.metamorph.api;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.util.ResourceLocation;

/**
 * Thing responsible for doing some awesome cool stuff!
 */
public class ModelAdapter implements JsonDeserializer<Model>
{
    private Gson gson = new GsonBuilder().create();

    @Override
    public Model deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Model model = gson.fromJson(json, Model.class);
        String type = json.getAsJsonObject().get("default").getAsString();

        model.defaultTexture = new ResourceLocation(type);

        return model;
    }
}