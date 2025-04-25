package dev.pk7r.spigot.starter.core.convert;

import java.util.ArrayList;

public class StringListConvertService implements ConvertService<ArrayList<String>> {

    @Override
    public ArrayList<String> convert(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        String[] parts = value.replace("[", "").replace("]", "").split(",");
        ArrayList<String> list = new ArrayList<>();
        for (String part : parts) {
            list.add(part.trim());
        }
        return list;
    }

    @Override
    public boolean supports(Class<?> t) {
        return t == ArrayList.class;
    }
}
