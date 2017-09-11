package io.nshusa.bsp.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * https://gist.github.com/alex-rnv/1541945a4ee243390ff5
 */
public final class MultiMapAdapter<K,V> implements JsonSerializer<Multimap<K,V>>, JsonDeserializer<Multimap<K,V>> {
    private static final Type asMapReturnType;
    static {
        try {
            asMapReturnType = Multimap.class.getDeclaredMethod("asMap").getGenericReturnType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public JsonElement serialize(Multimap<K, V> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.asMap(), asMapType(typeOfSrc));
    }
    @Override
    public Multimap<K, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Map<K, Collection<V>> asMap = context.deserialize(json, asMapType(typeOfT));
        Multimap<K, V> multimap = ArrayListMultimap.create();
        for (Map.Entry<K, Collection<V>> entry : asMap.entrySet()) {
            multimap.putAll(entry.getKey(), entry.getValue());
        }
        return multimap;
    }

    private static Type asMapType(Type multimapType) {
        return TypeToken.of(multimapType).resolveType(asMapReturnType).getType();
    }
}